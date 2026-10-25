package com.tetrisnewgeneration.game.engine

import com.tetrisnewgeneration.game.modes.ModeConfig
import kotlin.random.Random

/**
 * Pure-Kotlin falling-block game engine implementing guideline-style play:
 * SRS rotation with wall kicks, 7-bag queue, hold, ghost piece, lock delay,
 * T-spin detection, back-to-back, combos, perfect clears, levels and goals.
 * No Android dependency: fully unit-testable.
 */
class GameEngine(
    val config: ModeConfig,
    seed: Long = System.nanoTime(),
    random: Random = Random(seed)
) {
    enum class State { READY, PLAYING, PAUSED, OVER, WON }

    val board = Board()
    private val bag = SevenBag(random)

    var piece: Piece? = null
        private set
    var holdType: PieceType? = null
        private set
    var canHold: Boolean = true
        private set
    private val queue = ArrayDeque<PieceType>()
    private var fixedIndex = 0

    var score: Long = 0
        private set
    var lines: Int = 0
        private set
    var level: Int = config.startLevel
        private set
    var tetrises: Int = 0
        private set
    var maxCombo: Int = 0
        private set
    var combo: Int = -1
        private set
    private var backToBack = false

    var elapsedMs: Long = 0
        private set
    var softDropping = false
        private set
    private var gravityAccum = 0L
    private var lockTimerMs = -1L
    private var lockResets = 0
    private var piecesPlaced = 0
    private var lastActionWasRotation = false
    private var lastKickIndex = 0

    var state: State = State.READY
        private set

    private val pendingEvents = ArrayDeque<GameEvent>()

    // ---------------------------------------------------------------- init

    init {
        loadInitialBoard()
        refillQueue()
        spawnNext(announce = false)
    }

    private fun loadInitialBoard() {
        val rows = config.initialBoard
        if (rows.isEmpty()) return
        require(rows.size <= board.rows) { "initial board too tall" }
        for ((y, row) in rows.withIndex()) {
            require(row.length == board.cols) { "initial board row must be ${board.cols} wide" }
            for ((x, ch) in row.withIndex()) {
                val type = when (ch) {
                    'I' -> PieceType.I; 'O' -> PieceType.O; 'T' -> PieceType.T
                    'S' -> PieceType.S; 'Z' -> PieceType.Z; 'J' -> PieceType.J
                    'L' -> PieceType.L; else -> null
                }
                if (type != null) board.setCell(x, y, type)
            }
        }
    }

    // ---------------------------------------------------------------- helpers

    fun drainEvents(): List<GameEvent> {
        val out = pendingEvents.toList()
        pendingEvents.clear()
        return out
    }

    private fun emit(event: GameEvent) = pendingEvents.add(event)

    val nextQueue: List<PieceType>
        get() = queue.take(PREVIEW_COUNT)

    val ghostPiece: Piece?
        get() {
            var p = piece ?: return null
            while (true) {
                val moved = p.moved(0, 1)
                if (board.collides(moved)) return p
                p = moved
            }
        }

    val timeLeftMs: Long?
        get() = config.timeLimitMs?.let { limit -> (limit - elapsedMs).coerceAtLeast(0) }

    /** Number of remaining allowed moves for puzzle modes, or null if unlimited. */
    val movesLeft: Int?
        get() = config.moveLimit?.let { (it - piecesPlaced).coerceAtLeast(0) }

    fun start() {
        if (state == State.READY) state = State.PLAYING
    }

    /**
     * Restores a saved snapshot (board, queue, hold, stats). Used by
     * "resume last session". The active falling piece is respawned because
     * mid-air positions are transient; the queue is preserved verbatim.
     */
    fun restoreFrom(save: GameSave): Boolean {
        if (save.board.size != board.rows || save.board.firstOrNull()?.length != board.cols) return false
        try {
            board.clearAll()
            for ((y, row) in save.board.withIndex()) {
                for ((x, ch) in row.withIndex()) {
                    val type = when (ch) {
                        'I' -> PieceType.I; 'O' -> PieceType.O; 'T' -> PieceType.T
                        'S' -> PieceType.S; 'Z' -> PieceType.Z; 'J' -> PieceType.J
                        'L' -> PieceType.L; else -> null
                    }
                    if (type != null) board.setCell(x, y, type)
                }
            }
            queue.clear()
            fixedIndex = config.fixedPieces.size
            save.queue.forEach { name -> queue.addLast(PieceType.valueOf(name)) }
            holdType = save.hold?.let { PieceType.valueOf(it) }
            canHold = save.canHold
            score = save.score
            lines = save.lines
            level = save.level
            tetrises = save.tetrises
            combo = save.combo
            elapsedMs = save.elapsedMs
            spawnNext(announce = false)
            return state != State.OVER
        } catch (_: Exception) {
            return false
        }
    }

    fun pause() {
        if (state == State.PLAYING) state = State.PAUSED
    }

    fun resume() {
        if (state == State.PAUSED) state = State.PLAYING
    }

    // ---------------------------------------------------------------- main tick

    fun tick(deltaMs: Long) {
        if (state != State.PLAYING || deltaMs <= 0) return
        elapsedMs += deltaMs

        if (config.survivalRampMs != null) {
            val ramped = config.startLevel + (elapsedMs / config.survivalRampMs).toInt()
            if (ramped > level) setLevel(ramped)
        }

        if (config.timeLimitMs != null && elapsedMs >= config.timeLimitMs) {
            endByTime()
            return
        }

        val p = piece ?: return

        if (board.collides(p.moved(0, 1))) {
            // Piece is grounded: run lock delay.
            lockTimerMs += deltaMs
            if (lockTimerMs >= LOCK_DELAY_MS) lockPiece()
            return
        }

        lockTimerMs = -1L
        val interval = if (softDropping) SOFT_DROP_INTERVAL_MS
                       else Scoring.gravityIntervalMs(level, config.gravityScale)
        gravityAccum += deltaMs
        while (gravityAccum >= interval && state == State.PLAYING) {
            gravityAccum -= interval
            val moved = piece?.moved(0, 1) ?: return
            if (board.collides(moved)) break
            piece = moved
            if (softDropping) {
                score += Scoring.SOFT_DROP_POINTS_PER_CELL
                emit(GameEvent.SoftDropped(1))
            }
        }
    }

    // ---------------------------------------------------------------- actions

    fun perform(action: GameAction) {
        when (action) {
            GameAction.MoveLeft -> move(-1)
            GameAction.MoveRight -> move(1)
            GameAction.RotateCw -> rotate(1)
            GameAction.RotateCcw -> rotate(-1)
            GameAction.SoftDropOn -> if (state == State.PLAYING) softDropping = true
            GameAction.SoftDropOff -> softDropping = false
            GameAction.HardDrop -> hardDrop()
            GameAction.Hold -> hold()
            GameAction.TogglePause -> if (state == State.PLAYING) pause() else resume()
        }
    }

    fun move(dx: Int): Boolean {
        if (state != State.PLAYING) return false
        val p = piece ?: return false
        val moved = p.moved(dx, 0)
        if (board.collides(moved)) {
            emit(GameEvent.InvalidAction)
            return false
        }
        piece = moved
        lastActionWasRotation = false
        resetLockDelayIfGrounded()
        emit(GameEvent.PieceMoved(horizontal = true))
        return true
    }

    fun rotate(direction: Int): Boolean {
        if (state != State.PLAYING) return false
        val current = piece ?: return false
        val targetRotation = (current.rotation + direction + 4) % 4
        val candidate = current.copy(rotation = targetRotation)
        val kicks = Tetrominoes.kicks(current.type, current.rotation, targetRotation)
        for ((index, kick) in kicks.withIndex()) {
            // Kick table uses +y = UP; our board uses +y = DOWN, so invert y.
            val translated = candidate.moved(kick.x, -kick.y)
            if (!board.collides(translated)) {
                piece = translated
                lastActionWasRotation = true
                lastKickIndex = index
                resetLockDelayIfGrounded()
                emit(GameEvent.PieceRotated)
                return true
            }
        }
        emit(GameEvent.InvalidAction)
        return false
    }

    fun hardDrop() {
        if (state != State.PLAYING) return
        val current = piece ?: return
        var rows = 0
        var p = current
        while (true) {
            val moved = p.moved(0, 1)
            if (board.collides(moved)) break
            p = moved
            rows++
        }
        piece = p
        lastActionWasRotation = false
        score += Scoring.HARD_DROP_POINTS_PER_CELL * rows
        emit(GameEvent.HardDropped(rows))
        lockPiece()
    }

    fun hold() {
        if (state != State.PLAYING) return
        if (!config.allowHold) {
            emit(GameEvent.InvalidAction)
            return
        }
        val current = piece ?: return
        if (!canHold) {
            emit(GameEvent.InvalidAction)
            return
        }
        val previous = holdType
        holdType = current.type
        canHold = false
        if (previous != null) {
            spawnOf(previous)
        } else {
            spawnNext()
        }
        emit(GameEvent.PieceHeld(swapped = previous != null))
    }

    private fun resetLockDelayIfGrounded() {
        val p = piece ?: return
        if (board.collides(p.moved(0, 1)) && lockResets < MAX_LOCK_RESETS) {
            lockTimerMs = 0
            lockResets++
        }
    }

    // ---------------------------------------------------------------- locking

    private fun lockPiece() {
        val p = piece ?: return
        val tspin = detectTSpin(p)
        board.lock(p)
        piecesPlaced++
        emit(GameEvent.PieceLocked)

        val clearedRows = board.clearFullRows()
        val cleared = clearedRows.size
        lines += cleared
        if (cleared >= 4) tetrises++

        // Combo chain
        combo = if (cleared > 0) combo + 1 else -1
        if (combo > maxCombo) maxCombo = combo

        val b2bEligible = cleared >= 4 || (tspin && cleared > 0)
        val b2bUsed = b2bEligible && backToBack
        if (b2bEligible) backToBack = true
        else if (cleared > 0) backToBack = false

        val perfect = cleared > 0 && boardIsEmpty()
        val points = Scoring.pointsForLock(cleared, level, tspin, b2bUsed, combo, perfect)
        score += points

        if (cleared > 0 || tspin) {
            emit(
                GameEvent.LinesCleared(
                    lines = cleared,
                    tspin = tspin,
                    backToBack = b2bUsed,
                    combo = combo,
                    perfectClear = perfect,
                    points = points
                )
            )
        }

        // Level progression (skip for survival mode, which ramps by time).
        if (config.survivalRampMs == null && config.linesPerLevel > 0) {
            val target = config.startLevel + lines / config.linesPerLevel
            if (target > level) setLevel(target)
        }

        if (checkWinConditions(cleared)) return
        if (board.hasCellAboveVisible() && topOutBecauseOfLock(p)) {
            gameOver()
            return
        }

        spawnNext()
        canHold = true
    }

    private fun topOutBecauseOfLock(p: Piece): Boolean =
        p.cells().all { it.y < board.rows - board.visibleHeight }

    private fun boardIsEmpty(): Boolean {
        for (y in 0 until board.rows) {
            for (x in 0 until board.cols) {
                if (board.cell(x, y) != null) return false
            }
        }
        return true
    }

    private fun detectTSpin(p: Piece): Boolean {
        if (p.type != PieceType.T || !lastActionWasRotation) return false
        // T center is the middle of the 3x3 box.
        val cx = p.x + 1
        val cy = p.y + 1
        val corners = listOf(
            Point(cx - 1, cy - 1), Point(cx + 1, cy - 1),
            Point(cx - 1, cy + 1), Point(cx + 1, cy + 1)
        )
        val filled = corners.count { !board.isInside(it) || board.cell(it.x, it.y) != null }
        return filled >= 3
    }

    private fun setLevel(newLevel: Int) {
        level = newLevel
        emit(GameEvent.LevelUp(newLevel))
    }

    private fun checkWinConditions(clearedNow: Int): Boolean {
        if (!config.endOnGoal) return false
        config.goalLines?.let { if (lines >= it) { win("lines"); return true } }
        config.goalScore?.let { if (score >= it) { win("score"); return true } }
        config.goalCombo?.let { if (combo >= it) { win("combo"); return true } }
        config.goalTetrises?.let { if (tetrises >= it) { win("tetris"); return true } }
        if (config.moveLimit != null && piecesPlaced >= config.moveLimit && clearedNow > 0) {
            // Puzzle out of pieces but just cleared something: judge by goal.
            if (config.goalLines == null) win("moves")
            return true
        }
        return false
    }

    private fun endByTime() {
        // Reaching the time limit is a success for Time Attack / Quick Play.
        state = State.WON
        piece = null
        emit(GameEvent.GoalReached("time"))
        emit(GameEvent.GameOver)
    }

    private fun win(kind: String) {
        state = State.WON
        piece = null
        emit(GameEvent.GoalReached(kind))
        emit(GameEvent.GameOver)
    }

    private fun gameOver() {
        state = State.OVER
        piece = null
        emit(GameEvent.GameOver)
    }

    // ---------------------------------------------------------------- spawning

    private fun refillQueue() {
        if (config.fixedPieces.isNotEmpty()) {
            while (queue.size <= PREVIEW_COUNT && fixedIndex < config.fixedPieces.size) {
                queue.addLast(config.fixedPieces[fixedIndex++])
            }
        } else {
            while (queue.size <= PREVIEW_COUNT) queue.addLast(bag.next())
        }
    }

    private fun spawnNext(announce: Boolean = true) {
        refillQueue()
        val type = queue.removeFirstOrNull()
        if (type == null) {
            gameOver()
            return
        }
        spawnOf(type)
        refillQueue()
    }

    private fun spawnOf(type: PieceType) {
        // Spawn one row down so the piece is partly visible immediately.
        val spawned = Tetrominoes.spawn(type, board.cols, spawnY = 1)
        if (board.collides(spawned)) {
            // Try one row up (cell may poke into buffer zone).
            val shifted = spawned.moved(0, -1)
            if (board.collides(shifted)) {
                piece = spawned // keep for rendering, then top-out
                gameOver()
                return
            }
            piece = shifted
        } else {
            piece = spawned
        }
        gravityAccum = 0
        lockTimerMs = -1
        lockResets = 0
        lastActionWasRotation = false
        lastKickIndex = 0
        if (movesLeft == 0 && config.moveLimit != null) {
            // Used the last allowed piece; decide outcome on its lock.
        }
    }

    companion object {
        const val PREVIEW_COUNT = 5
        const val LOCK_DELAY_MS = 500L
        const val MAX_LOCK_RESETS = 15
        const val SOFT_DROP_INTERVAL_MS = 25L
    }
}
