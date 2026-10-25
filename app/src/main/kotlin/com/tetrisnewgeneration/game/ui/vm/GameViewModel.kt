package com.tetrisnewgeneration.game.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tetrisnewgeneration.game.TngApp
import com.tetrisnewgeneration.game.audio.MusicEngine
import com.tetrisnewgeneration.game.audio.SfxEngine
import com.tetrisnewgeneration.game.data.repo.StatKeys
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.engine.GameAction
import com.tetrisnewgeneration.game.engine.GameEngine
import com.tetrisnewgeneration.game.engine.GameEvent
import com.tetrisnewgeneration.game.engine.GameSave
import com.tetrisnewgeneration.game.engine.TSpinKind
import com.tetrisnewgeneration.game.modes.ModeConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * Bridges the pure engine and the Compose UI: runs the game loop coroutine,
 * maps engine events to sound/haptics/persistence, and owns the round summary.
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as TngApp).container
    private val sfx: SfxEngine get() = container.sfx
    private val music: MusicEngine get() = container.music

    data class UiState(
        val config: ModeConfig? = null,
        val board: List<List<Int>> = emptyList(), // 0 empty else piece ordinal+1
        val activeCells: List<Triple<Int, Int, Int>> = emptyList(), // x, y, typeIdx
        val ghostCells: List<Pair<Int, Int>> = emptyList(),
        val nextQueue: List<Int> = emptyList(),
        val holdType: Int? = null,
        val canHold: Boolean = true,
        val score: Long = 0,
        val level: Int = 1,
        val lines: Int = 0,
        val combo: Int = -1,
        val tetrises: Int = 0,
        val elapsedMs: Long = 0,
        val timeLeftMs: Long? = null,
        val movesLeft: Int? = null,
        val goalLines: Int? = null,
        val goalScore: Long? = null,
        val goalCombo: Int? = null,
        val goalTetrises: Int? = null,
        val state: GameEngine.State = GameEngine.State.READY,
        val paused: Boolean = false,
        val finished: Boolean = false,
        val won: Boolean = false,
        val bestBefore: Long = 0,
        val newRecord: Boolean = false,
        val rewardCoins: Long = 0,
        val rewardXp: Long = 0,
        val levelsGained: Int = 0,
        val playerLevelAfter: Int = 1,
        val toast: String? = null,          // transient celebration text id marker
        val toastSeq: Int = 0,
        val toastKind: ToastKind? = null,
        // v1.3.0 presentation plumbing: consumed by the screen's FX layer.
        val clearSeq: Int = 0,              // increments on every line clear
        val clearedRows: List<Int> = emptyList(), // pre-collapse row indices
        val clearPoints: Long = 0L,         // points of the last clear (popup)
        val shakeSeq: Int = 0,              // increments on every shake request
        val shakeAmpPx: Float = 0f,
        val shakeDurationMs: Long = 0L
    )

    enum class ToastKind { TETRIS, TSPIN, TSPIN_MINI, B2B, PERFECT, LEVEL_UP, RECORD, COMBO }

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    private var engine: GameEngine? = null
    private var loopJob: Job? = null
    private val actions = Channel<GameAction>(Channel.UNLIMITED)

    private var playerSettings: PlayerSettings = PlayerSettings()
    private var lastSessionSaveMs = 0L
    private var clearCounter = 0
    private var shakeCounter = 0
    private var timeWarned = false

    /** Called by the screen when entering; loads settings + best score. */
    fun prepare() {
        viewModelScope.launch {
            playerSettings = container.settings.current()
            applyAudioSettings()
        }
    }

    fun start(config: ModeConfig, resumeSave: GameSave? = null, autoStart: Boolean = true) {
        loopJob?.cancel()
        val eng = GameEngine(config)
        if (resumeSave != null && resumeSave.modeId == config.id) {
            eng.restoreFrom(resumeSave)
        }
        engine = eng
        actions.tryReceive() // flush
        timeWarned = false
        viewModelScope.launch {
            playerSettings = container.settings.current()
            applyAudioSettings()
            _ui.value = buildUiState(eng, bestBefore = loadBest(config.id))
            if (autoStart) {
                beginRound()
            }
            // With autoStart = false the screen runs the 3-2-1-GO countdown
            // first, then calls [beginRound] (resumed sessions skip it).
        }
    }

    /** Starts (or resumes) the prepared engine after the countdown finishes. */
    fun beginRound() {
        val eng = engine ?: return
        if (eng.state != GameEngine.State.READY) return
        playerSettings.let {
            music.applySettings(it.musicEnabled, it.musicVolume)
        }
        music.play(if (eng.config.zenMode) MusicEngine.Track.ZEN else MusicEngine.Track.PLAY)
        eng.start()
        loopJob?.cancel()
        viewModelScope.launch { runLoop() }
    }

    /** One pulse of the start countdown: beeps on every step, fanfare on GO. */
    fun countdownTick(go: Boolean) {
        sfx.play(if (go) SfxEngine.Sound.GO else SfxEngine.Sound.COUNTDOWN)
    }

    private suspend fun loadBest(modeId: String): Long =
        container.scoreRepository.bestForMode(modeId)

    fun onAction(action: GameAction) {
        actions.trySend(action)
    }

    fun pauseRound(saveSession: Boolean = true) {
        engine?.pause()
        if (saveSession) persistSession(force = true)
        music.pause()
    }

    fun resumeRound() {
        engine?.resume()
        playerSettings.let {
            music.applySettings(it.musicEnabled, it.musicVolume)
        }
        music.play(if (engine?.config?.zenMode == true) MusicEngine.Track.ZEN else MusicEngine.Track.PLAY)
        loopJob?.cancel()
        viewModelScope.launch { runLoop() }
    }

    fun restart() {
        engine?.let { start(it.config) }
    }

    fun quitToHome() {
        loopJob?.cancel()
        engine = null
        music.play(MusicEngine.Track.MENU)
    }

    private suspend fun runLoop() {
        val eng = engine ?: return
        var last = System.nanoTime()
        var uiAccum = 0L
        while (kotlin.coroutines.coroutineContext.isActive &&
            eng.state == GameEngine.State.PLAYING
        ) {
            // Drain queued inputs first for responsiveness.
            while (true) {
                val action = actions.tryReceive().getOrNull() ?: break
                eng.perform(action)
                if (action == GameAction.HardDrop) {
                    sfx.play(SfxEngine.Sound.DROP)
                    bumpStat(StatKeys.HARD_DROPS, 1)
                    addDaily("daily_hard", 1)
                }
            }
            val now = System.nanoTime()
            val dt = (now - last) / 1_000_000
            last = now
            eng.tick(dt)
            // A4: single time-warning beep in the final 10 seconds of timed modes.
            if (!timeWarned) {
                val left = eng.timeLeftMs
                if (left != null && left in 1 until TIME_WARN_MS) {
                    timeWarned = true
                    sfx.play(SfxEngine.Sound.COUNTDOWN)
                }
            }
            handleEvents(eng.drainEvents())
            uiAccum += dt
            if (uiAccum >= 16) {
                uiAccum = 0
                _ui.value = buildUiState(eng, _ui.value.bestBefore, _ui.value.newRecord)
            }
            if (eng.state != GameEngine.State.PLAYING) break
            delay(6)
        }
        finishRoundIfNeeded()
    }

    private suspend fun finishRoundIfNeeded() {
        val eng = engine ?: return
        when (eng.state) {
            GameEngine.State.OVER, GameEngine.State.WON -> {
                music.play(MusicEngine.Track.MENU)
                if (eng.state == GameEngine.State.WON) {
                    sfx.play(SfxEngine.Sound.WIN)
                } else {
                    sfx.play(SfxEngine.Sound.ROUND_OVER)
                }
                persistRound(eng)
                persistSession(force = true, clearIfOver = true)
                _ui.value = buildUiState(eng, _ui.value.bestBefore, _ui.value.newRecord)
                    .copy(finished = true)
            }
            GameEngine.State.PAUSED -> {
                _ui.value = buildUiState(eng, _ui.value.bestBefore, _ui.value.newRecord)
                    .copy(paused = true)
            }
            else -> Unit
        }
    }

    private fun handleEvents(events: List<GameEvent>) {
        for (event in events) {
            when (event) {
                is GameEvent.PieceMoved -> sfx.play(SfxEngine.Sound.MOVE)
                is GameEvent.PieceRotated -> sfx.play(SfxEngine.Sound.ROTATE)
                is GameEvent.SoftDropped -> Unit // silent to avoid noise
                is GameEvent.HardDropped -> {
                    // C4: small impact shake for every hard drop.
                    if (event.rows > 0) pushShake(4f, 200L)
                }
                is GameEvent.PieceLocked -> sfx.play(SfxEngine.Sound.LOCK)
                is GameEvent.PieceHeld -> sfx.play(SfxEngine.Sound.CLICK)
                is GameEvent.LinesCleared -> {
                    // C2/C3: remember rows + points for the flash and popup FX.
                    if (event.clearedRows.isNotEmpty()) {
                        clearCounter++
                        _ui.value = _ui.value.copy(
                            clearSeq = clearCounter,
                            clearedRows = event.clearedRows,
                            clearPoints = event.points
                        )
                    }
                    // C4: shake strength follows the size of the moment.
                    when {
                        event.perfectClear -> pushShake(10f, 320L)
                        event.lines >= 4 -> pushShake(8f, 320L)
                        event.tspinKind == TSpinKind.FULL -> pushShake(6f, 260L)
                        event.tspinKind == TSpinKind.MINI -> pushShake(4f, 200L)
                    }
                    if (event.lines >= 4) {
                        sfx.play(SfxEngine.Sound.CLEAR)
                        pushToast(ToastKind.TETRIS)
                    } else if (event.tspinKind == TSpinKind.FULL) {
                        sfx.play(SfxEngine.Sound.CLEAR)
                        pushToast(ToastKind.TSPIN)
                    } else if (event.tspinKind == TSpinKind.MINI) {
                        sfx.play(SfxEngine.Sound.CLEAR)
                        pushToast(ToastKind.TSPIN_MINI)
                    } else if (event.combo >= 2) {
                        sfx.play(SfxEngine.Sound.COMBO)
                        pushToast(ToastKind.COMBO)
                    } else if (event.lines > 0) {
                        sfx.play(SfxEngine.Sound.CLEAR)
                    }
                    if (event.backToBack && event.lines > 0) pushToast(ToastKind.B2B)
                    if (event.perfectClear) pushToast(ToastKind.PERFECT)
                    if (event.lines >= 4) bumpStat(StatKeys.TETRISES, 1)
                    if (event.lines > 0) {
                        addDaily("daily_lines", event.lines)
                        addDaily("daily_combo", event.combo)
                        addDaily("daily_tetris", if (event.lines >= 4) 1 else 0)
                    }
                }
                is GameEvent.LevelUp -> {
                    sfx.play(SfxEngine.Sound.LEVEL_UP)
                    pushToast(ToastKind.LEVEL_UP)
                }
                is GameEvent.GoalReached -> sfx.play(SfxEngine.Sound.WIN)
                is GameEvent.GameOver -> Unit // handled in finishRoundIfNeeded
                is GameEvent.InvalidAction -> Unit
            }
        }
    }

    private var toastCounter = 0
    private fun pushToast(kind: ToastKind) {
        toastCounter++
        _ui.value = _ui.value.copy(toast = kind.name, toastSeq = toastCounter, toastKind = kind)
    }

    private fun pushShake(amplitudePx: Float, durationMs: Long) {
        shakeCounter++
        _ui.value = _ui.value.copy(
            shakeSeq = shakeCounter,
            shakeAmpPx = amplitudePx,
            shakeDurationMs = durationMs
        )
    }

    private fun bumpStat(key: String, amount: Long) {
        viewModelScope.launch {
            container.scoreRepository.bump(key, amount)
            container.progressRepository.evaluateStat(key, container.scoreRepository.stat(key))
        }
    }

    private fun addDaily(statKey: String, amount: Int) {
        viewModelScope.launch {
            container.progressRepository.addDailyProgress(statKey, amount)
        }
    }

    private suspend fun persistRound(eng: GameEngine) {
        val won = eng.state == GameEngine.State.WON
        val repo = container.scoreRepository

        val bestBefore = repo.bestForMode(eng.config.id)
        val isRecord = eng.score > bestBefore

        repo.saveRound(
            modeId = eng.config.id,
            score = eng.score,
            lines = eng.lines,
            level = eng.level,
            durationMs = eng.elapsedMs,
            won = won
        )
        repo.setMax(StatKeys.MAX_COMBO, eng.maxCombo.toLong())
        repo.setMax(StatKeys.MAX_LEVEL, eng.level.toLong())

        // Mode-specific side effects.
        when {
            eng.config.id.startsWith("ch_") && won -> {
                container.progressRepository.completeChallenge(eng.config.id)
                repo.bump(StatKeys.CHALLENGES_DONE, 1)
                if (eng.config.id == "ch_sprint") {
                    container.progressRepository.unlockEventAchievement("time_attack_1")
                }
                addDaily("daily_games", 1)
            }
            eng.config.id.startsWith("puzzle_") && won -> {
                val puzzleId = eng.config.id.removePrefix("puzzle_")
                repo.bump(StatKeys.PUZZLES_DONE, 1)
                repo.bump("puzzle_${puzzleId}_done", 1)
                addDaily("daily_games", 1)
            }
            eng.config.id.startsWith("adv_") && won -> {
                val levelId = eng.config.id.removePrefix("adv_").toIntOrNull() ?: 0
                container.progressRepository.completeAdventure(levelId, eng.score)
                repo.bump(StatKeys.ADVENTURE_DONE, 1)
                addDaily("daily_games", 1)
            }
            eng.config.id == "zen" -> {
                repo.bump(StatKeys.ZEN_TIME_MS, eng.elapsedMs)
                addDaily("daily_games", 1)
            }
            else -> addDaily("daily_games", 1)
        }

        // Daily score task.
        addDaily("daily_score", eng.score.toInt().coerceAtMost(100000))

        // XP/coins rewards.
        val snapshot = container.settings.current()
        val xpGain = container.progressionRepository.roundXp(eng.score, eng.lines, won)
        val coinGain = container.progressionRepository.roundCoins(eng.lines, eng.tetrises, won)
        val update = container.progressionRepository.applyGains(snapshot, xpGain, coinGain)
        if (update.levelsGained > 0) {
            sfx.play(SfxEngine.Sound.ACHIEVE)
        }

        // Achievements from aggregate counters.
        val stats = repo.allStats()
        container.progressRepository.evaluateAll(stats)

        _ui.value = _ui.value.copy(
            newRecord = isRecord,
            bestBefore = bestBefore,
            rewardCoins = coinGain,
            rewardXp = xpGain,
            levelsGained = update.levelsGained,
            playerLevelAfter = update.level
        )
    }

    /** Serializes the running round for "continue last session". */
    fun persistSession(force: Boolean = false, clearIfOver: Boolean = false) {
        val eng = engine ?: return
        if (!force && System.currentTimeMillis() - lastSessionSaveMs < 2000) return
        lastSessionSaveMs = System.currentTimeMillis()
        viewModelScope.launch {
            if (clearIfOver && (eng.state == GameEngine.State.OVER || eng.state == GameEngine.State.WON)) {
                container.settings.clearSession()
            } else if (eng.state == GameEngine.State.PLAYING || eng.state == GameEngine.State.PAUSED) {
                GameSave.fromEngine(eng)?.let { save ->
                    container.settings.saveSession(
                        kotlinx.serialization.json.Json.encodeToString(GameSave.serializer(), save)
                    )
                }
            }
        }
    }

    fun applyAudioSettings() {
        sfx.applySettings(playerSettings.sfxEnabled, playerSettings.sfxVolume, playerSettings.soundPackId)
        music.applySettings(playerSettings.musicEnabled, playerSettings.musicVolume)
    }

    private fun buildUiState(
        eng: GameEngine,
        bestBefore: Long,
        newRecord: Boolean = false
    ): UiState {
        val boardGrid = List(eng.board.rows) { y ->
            List(eng.board.cols) { x ->
                eng.board.cell(x, y)?.ordinal?.plus(1) ?: 0
            }
        }
        val typeIdx: (com.tetrisnewgeneration.game.engine.PieceType?) -> Int? = { it?.ordinal }
        return UiState(
            config = eng.config,
            board = boardGrid,
            activeCells = eng.piece?.cells()?.map { Triple(it.x, it.y, eng.piece!!.type.ordinal) } ?: emptyList(),
            ghostCells = if (eng.config.allowGhost) {
                eng.ghostPiece?.cells()?.map { it.x to it.y } ?: emptyList()
            } else emptyList(),
            nextQueue = eng.nextQueue.mapNotNull { typeIdx(it) },
            holdType = typeIdx(eng.holdType),
            canHold = eng.canHold,
            score = eng.score,
            level = eng.level,
            lines = eng.lines,
            combo = eng.combo,
            tetrises = eng.tetrises,
            elapsedMs = eng.elapsedMs,
            timeLeftMs = eng.timeLeftMs,
            movesLeft = eng.movesLeft,
            goalLines = eng.config.goalLines,
            goalScore = eng.config.goalScore,
            goalCombo = eng.config.goalCombo,
            goalTetrises = eng.config.goalTetrises,
            state = eng.state,
            paused = eng.state == GameEngine.State.PAUSED,
            finished = eng.state == GameEngine.State.OVER || eng.state == GameEngine.State.WON,
            won = eng.state == GameEngine.State.WON,
            bestBefore = bestBefore,
            newRecord = newRecord || _ui.value.newRecord,
            toast = _ui.value.toast,
            toastSeq = _ui.value.toastSeq,
            toastKind = _ui.value.toastKind,
            clearSeq = _ui.value.clearSeq,
            clearedRows = _ui.value.clearedRows,
            clearPoints = _ui.value.clearPoints,
            shakeSeq = _ui.value.shakeSeq,
            shakeAmpPx = _ui.value.shakeAmpPx,
            shakeDurationMs = _ui.value.shakeDurationMs
        )
    }

    override fun onCleared() {
        persistSession(force = true)
        loopJob?.cancel()
        super.onCleared()
    }

    companion object {
        /** Final window (ms) of timed modes that triggers the warning beep. */
        const val TIME_WARN_MS = 10_000L
    }
}
