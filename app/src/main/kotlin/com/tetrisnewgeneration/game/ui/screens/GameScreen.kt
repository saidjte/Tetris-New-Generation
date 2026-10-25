package com.tetrisnewgeneration.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.collectAsState
import com.tetrisnewgeneration.game.R
import kotlinx.coroutines.delay
import com.tetrisnewgeneration.game.audio.SfxEngine
import com.tetrisnewgeneration.game.engine.GameAction
import com.tetrisnewgeneration.game.engine.GameSave
import com.tetrisnewgeneration.game.modes.AdventureLevels
import com.tetrisnewgeneration.game.modes.Challenges
import com.tetrisnewgeneration.game.modes.ModeConfig
import com.tetrisnewgeneration.game.modes.Modes
import com.tetrisnewgeneration.game.modes.Puzzles
import com.tetrisnewgeneration.game.themes.EffectPackDef
import com.tetrisnewgeneration.game.themes.PackDefs
import com.tetrisnewgeneration.game.themes.ParticleStyle
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.BoardCanvas
import com.tetrisnewgeneration.game.ui.components.MiniPiece
import com.tetrisnewgeneration.game.ui.components.Particle
import com.tetrisnewgeneration.game.ui.components.RowFlash
import com.tetrisnewgeneration.game.ui.components.ScorePopup
import com.tetrisnewgeneration.game.ui.components.ShockwaveFx
import com.tetrisnewgeneration.game.ui.components.localizedText
import com.tetrisnewgeneration.game.ui.fx.CountdownSteps
import com.tetrisnewgeneration.game.ui.fx.FxMath
import com.tetrisnewgeneration.game.ui.vm.GameViewModel
import androidx.compose.runtime.withFrameNanos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min
import kotlin.random.Random

/**
 * The playfield screen: HUD, board canvas, touch controls, pause and
 * round-summary dialogs.
 */
@Composable
fun GameScreen(
    modeKey: String,
    resumeSaveJson: String?,
    theme: ThemeDef,
    reducedMotion: Boolean,
    hapticsEnabled: Boolean,
    gestureControls: Boolean,
    effectPackId: String,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val vm: GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val ui by vm.ui.collectAsState()
    val haptics = LocalHapticFeedback.current

    val config = remember(modeKey) { resolveConfig(modeKey) }

    // Particles + flash state
    val particles = remember { mutableStateListOf<Particle>() }
    var flashAlpha by remember { mutableStateOf(0f) }
    var flashColor by remember { mutableStateOf(Color.White) }
    var bannerText by remember { mutableStateOf<String?>(null) }
    var bannerVisible by remember { mutableStateOf(false) }
    var bannerColor by remember { mutableStateOf(theme.accent) }

    // v1.3.0 presentation FX state
    val fxPack = remember(effectPackId) { PackDefs.effectPack(effectPackId) }
    val rowFlashes = remember { mutableStateListOf<RowFlash>() }
    val scorePopups = remember { mutableStateListOf<ScorePopup>() }
    val shockwaves = remember { mutableStateListOf<ShockwaveFx>() }
    var shakeX by remember { mutableStateOf(0f) }
    var shakeY by remember { mutableStateOf(0f) }
    var shakeElapsed by remember { mutableStateOf(Float.MAX_VALUE) }
    var shakeAmp by remember { mutableStateOf(0f) }
    var shakeDur by remember { mutableStateOf(0f) }
    var countdownLabel by remember { mutableStateOf<String?>(null) }

    fun haptic() {
        if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun hapticLight() {
        if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // Start or resume the round once. Fresh rounds play the 3-2-1-GO
    // countdown (v1.3.0, A3) — resumed sessions start immediately.
    LaunchedEffect(modeKey) {
        vm.prepare()
        val save = resumeSaveJson?.let {
            try {
                val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                json.decodeFromString(GameSave.serializer(), it)
            } catch (_: Exception) { null }
        }
        vm.start(config, save, autoStart = save != null)
        if (save == null) {
            for (step in CountdownSteps.build()) {
                val go = step.label == CountdownSteps.GO_LABEL
                countdownLabel = step.label
                vm.countdownTick(go)
                if (go) {
                    haptic()
                    vm.beginRound() // the round opens with the GO flash
                } else {
                    hapticLight()
                }
                delay(step.durationMs)
            }
            countdownLabel = null
        }
    }

    // Celebration toasts trigger banner text + flash + particles + haptics.
    // v2: every kind is color-coded, haptic-confirmed and stays long enough
    // to be read mid-game; big moments last longer than small ones.
    LaunchedEffect(ui.toastSeq) {
        if (ui.toastSeq == 0) return@LaunchedEffect
        val kind = ui.toastKind ?: return@LaunchedEffect
        bannerText = when (kind) {
            GameViewModel.ToastKind.TETRIS -> context.getString(R.string.hud_tetris)
            GameViewModel.ToastKind.TSPIN -> context.getString(R.string.hud_tspin)
            GameViewModel.ToastKind.TSPIN_MINI -> context.getString(R.string.hud_tspin_mini)
            GameViewModel.ToastKind.B2B -> context.getString(R.string.hud_back_to_back)
            GameViewModel.ToastKind.PERFECT -> context.getString(R.string.hud_perfect)
            GameViewModel.ToastKind.LEVEL_UP -> context.getString(R.string.hud_level_up, ui.level)
            GameViewModel.ToastKind.RECORD -> context.getString(R.string.hud_new_record)
            GameViewModel.ToastKind.COMBO -> context.getString(R.string.hud_combo, ui.combo)
        }
        bannerColor = when (kind) {
            GameViewModel.ToastKind.TETRIS -> Color(0xFF22D3EE)
            GameViewModel.ToastKind.TSPIN -> Color(0xFFA855F7)
            GameViewModel.ToastKind.TSPIN_MINI -> Color(0xFFC084FC)
            GameViewModel.ToastKind.B2B -> Color(0xFFF97316)
            GameViewModel.ToastKind.PERFECT -> Color(0xFFFACC15)
            GameViewModel.ToastKind.LEVEL_UP -> Color(0xFF4ADE80)
            GameViewModel.ToastKind.RECORD -> Color(0xFFFCD34D)
            GameViewModel.ToastKind.COMBO -> Color(0xFF38BDF8)
        }
        when (kind) {
            GameViewModel.ToastKind.TETRIS -> {
                haptic(); flashColor = Color(0xFF22D3EE); flashAlpha = 0.45f
                spawnBurst(particles, (fxPack.density * 1.2f).toInt(), Color(0xFF22D3EE), 10f, fxPack.particle)
                shockwaves.add(ShockwaveFx())
            }
            GameViewModel.ToastKind.TSPIN -> {
                haptic(); flashColor = Color(0xFFA855F7); flashAlpha = 0.38f
                spawnBurst(particles, (fxPack.density * 0.9f).toInt(), Color(0xFFA855F7), 10f, fxPack.particle)
            }
            GameViewModel.ToastKind.TSPIN_MINI -> {
                hapticLight(); flashColor = Color(0xFFC084FC); flashAlpha = 0.28f
            }
            GameViewModel.ToastKind.B2B -> {
                haptic(); flashColor = Color(0xFFF97316); flashAlpha = 0.32f
            }
            GameViewModel.ToastKind.PERFECT -> {
                haptic(); flashColor = Color(0xFFFACC15); flashAlpha = 0.5f
                spawnBurst(particles, (fxPack.density * 1.4f).toInt(), Color(0xFFFACC15), 10f, fxPack.particle)
                shockwaves.add(ShockwaveFx())
            }
            GameViewModel.ToastKind.LEVEL_UP -> {
                hapticLight(); flashColor = Color(0xFF4ADE80); flashAlpha = 0.3f
            }
            GameViewModel.ToastKind.COMBO -> {
                hapticLight()
                spawnBurst(particles, (fxPack.density * 0.5f).toInt().coerceAtLeast(8), Color(0xFF38BDF8), 10f, fxPack.particle)
            }
            GameViewModel.ToastKind.RECORD -> {
                haptic(); flashColor = Color(0xFFFCD34D); flashAlpha = 0.5f
                spawnBurst(particles, (fxPack.density * 1.6f).toInt(), Color(0xFFFCD34D), 10f, fxPack.particle)
            }
        }
        bannerVisible = true
        // Big moments stay on screen longer so they are never missed.
        delay(
            when (kind) {
                GameViewModel.ToastKind.TETRIS,
                GameViewModel.ToastKind.PERFECT,
                GameViewModel.ToastKind.RECORD -> 1800L
                GameViewModel.ToastKind.TSPIN,
                GameViewModel.ToastKind.B2B,
                GameViewModel.ToastKind.LEVEL_UP -> 1500L
                else -> 1300L
            }
        )
        bannerVisible = false
    }

    // Particle physics loop + v1.3.0 FX timers (beams, popups, rings, shake).
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { now ->
                val dt = 1f / 60f
                val dtMs = dt * 1000f
                val it = particles.iterator()
                while (it.hasNext()) {
                    val p = it.next()
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.vy += 340f * dt // gravity on particles
                    p.angle += p.spin * dt
                    p.life -= p.decay * dt
                    if (p.life <= 0f) it.remove()
                }
                if (flashAlpha > 0f) flashAlpha = (flashAlpha - dt * 0.9f).coerceAtLeast(0f)

                val flashIt = rowFlashes.iterator()
                while (flashIt.hasNext()) {
                    val f = flashIt.next()
                    f.elapsedMs += dtMs
                    if (f.elapsedMs >= FxMath.BEAM_DURATION_MS) flashIt.remove()
                }
                val popupIt = scorePopups.iterator()
                while (popupIt.hasNext()) {
                    val p = popupIt.next()
                    p.elapsedMs += dtMs
                    if (p.elapsedMs >= FxMath.POPUP_DURATION_MS) popupIt.remove()
                }
                val ringIt = shockwaves.iterator()
                while (ringIt.hasNext()) {
                    val r = ringIt.next()
                    r.elapsedMs += dtMs
                    if (r.elapsedMs >= FxMath.RING_DURATION_MS) ringIt.remove()
                }
                if (shakeElapsed < shakeDur) {
                    shakeElapsed += dtMs
                    val off = FxMath.shakeOffset(shakeElapsed, shakeDur, shakeAmp)
                    shakeX = off
                    shakeY = off * 0.45f
                } else if (shakeX != 0f || shakeY != 0f) {
                    shakeX = 0f
                    shakeY = 0f
                }
            }
        }
    }

    // Cleared-row beams + score popup on every line clear (C2/C3).
    LaunchedEffect(ui.clearSeq) {
        if (ui.clearSeq == 0) return@LaunchedEffect
        for (row in ui.clearedRows) rowFlashes.add(RowFlash(row))
        if (ui.clearPoints > 0) scorePopups.add(ScorePopup(ui.clearPoints))
    }

    // Screen shake requests (C4). Reduced motion never shakes.
    LaunchedEffect(ui.shakeSeq) {
        if (ui.shakeSeq == 0 || reducedMotion) return@LaunchedEffect
        shakeAmp = ui.shakeAmpPx
        shakeDur = ui.shakeDurationMs.toFloat()
        shakeElapsed = 0f
    }

    // Pause on app background; save session.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    vm.pauseRound(saveSession = true)
                }
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    // Round stays paused until the player presses resume.
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = !ui.finished) {
        vm.pauseRound(saveSession = true)
    }

    Box(modifier = Modifier.fillMaxSize().background(theme.backgroundTop)) {
        // Full-screen gesture layer sits at the BOTTOM of the box: every
        // button, HUD control and dialog above it wins hit-testing first, so
        // gestures only receive touches on empty space — the whole screen
        // becomes the touch surface while the button pad stays functional.
        if (gestureControls && !ui.paused && !ui.finished) {
            BoardGestureLayer(
                onAction = { vm.onAction(it) },
                onHapticLight = { hapticLight() },
                onHapticStrong = { haptic() }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            // ---- Top HUD
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HudStat(stringResource(R.string.hud_score), formatNumber(ui.score), theme)
                Spacer(Modifier.width(6.dp))
                HudStat(stringResource(R.string.hud_level), ui.level.toString(), theme)
                Spacer(Modifier.width(6.dp))
                HudStat(stringResource(R.string.hud_lines), ui.lines.toString(), theme)
                Spacer(Modifier.weight(1f))
                ui.timeLeftMs?.let { left ->
                    HudStat(stringResource(R.string.hud_time), formatTime(left), theme)
                    Spacer(Modifier.width(6.dp))
                }
                PauseButton(theme) { vm.pauseRound(saveSession = true) }
            }

            // ---- Goal progress
            if (ui.goalLines != null || ui.goalScore != null || ui.goalTetrises != null || ui.goalCombo != null) {
                GoalProgress(ui, theme)
            }

            Spacer(Modifier.height(6.dp))

            // ---- Middle: hold + board + next
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(64.dp)
                ) {
                    Text(stringResource(R.string.hud_hold), color = theme.textPrimary, fontSize = 11.sp)
                    HoldBox(theme, ui.holdType, ui.canHold)
                    Spacer(Modifier.height(8.dp))
                    if (ui.combo > 0) {
                        Text(
                            stringResource(R.string.hud_combo, ui.combo),
                            color = theme.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    ui.movesLeft?.let {
                        Text(
                            stringResource(R.string.pieces_left, it),
                            color = theme.textPrimary.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // The board shares the weighted middle slot; full-screen
                // gestures are handled by the layer at the bottom of the root box.
                // graphicsLayer applies the v1.3.0 screen shake (C4).
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().graphicsLayer {
                        translationX = shakeX
                        translationY = shakeY
                    }
                ) {
                    BoardCanvas(
                        theme = theme,
                        boardGrid = ui.board,
                        activeCells = ui.activeCells,
                        ghostCells = ui.ghostCells,
                        particles = particles,
                        flashAlpha = flashAlpha,
                        flashColor = flashColor,
                        reducedMotion = reducedMotion,
                        rowFlashes = rowFlashes,
                        scorePopups = scorePopups,
                        shockwaves = shockwaves,
                        comboHeat = FxMath.comboHeat(ui.combo),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(64.dp)
                ) {
                    Text(stringResource(R.string.hud_next), color = theme.textPrimary, fontSize = 11.sp)
                    ui.nextQueue.take(3).forEach { idx ->
                        MiniPiece(theme = theme, typeIndex = idx, cellSize = 9.dp)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // ---- Controls
            GameControls(
                theme = theme,
                enabled = !ui.paused && !ui.finished,
                onPress = { action -> vm.onAction(action) }
            )
        }

        // Transient event banner (T-SPIN / B2B / TETRIS ...) — big, animated,
        // color-coded and pulsing so it is impossible to miss mid-game.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            AnimatedVisibility(
                visible = bannerVisible,
                enter = if (reducedMotion) {
                    fadeIn()
                } else {
                    scaleIn(
                        initialScale = 0.55f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn()
                },
                exit = if (reducedMotion) {
                    fadeOut()
                } else {
                    scaleOut(targetScale = 0.85f, animationSpec = tween(150)) +
                        fadeOut(animationSpec = tween(150))
                }
            ) {
                EventBanner(
                    text = bannerText.orEmpty(),
                    accent = bannerColor,
                    pulse = rememberBannerPulse(reducedMotion)
                )
            }
        }

        // Countdown overlay (v1.3.0, C8) — pure display, never blocks input.
        countdownLabel?.let { label ->
            CountdownOverlay(
                label = if (label == CountdownSteps.GO_LABEL) {
                    context.getString(R.string.countdown_go)
                } else label,
                accent = theme.accent,
                reducedMotion = reducedMotion
            )
        }

        // Pause overlay
        if (ui.paused && !ui.finished) {
            PauseOverlay(
                theme = theme,
                onResume = { vm.resumeRound() },
                onRestart = { vm.restart() },
                onHome = { vm.quitToHome(); onHome() }
            )
        }

        // Round summary dialog
        if (ui.finished) {
            RoundSummaryDialog(
                ui = ui,
                theme = theme,
                modeKey = modeKey,
                onRetry = { vm.restart() },
                onNextStage = { vm.quitToHome(); onHome() },
                onHome = { vm.quitToHome(); onHome() },
                onShare = {
                    val text = context.getString(
                        R.string.share_text_fmt,
                        formatNumber(ui.score), ui.lines, ui.level
                    )
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(
                        android.content.Intent.createChooser(
                            send,
                            context.getString(R.string.summary_share)
                        )
                    )
                }
            )
        }
    }
}

// ---------------------------------------------------------------- helpers

internal fun resolveConfig(modeKey: String): ModeConfig = when {
    modeKey.startsWith("puzzle_") -> {
        val id = modeKey.removePrefix("puzzle_").toIntOrNull() ?: 1
        Puzzles.all.firstOrNull { it.id == id }?.toConfig() ?: Puzzles.all.first().toConfig()
    }
    modeKey.startsWith("adv_") -> {
        val id = modeKey.removePrefix("adv_").toIntOrNull() ?: 1
        AdventureLevels.byId(id)?.config ?: AdventureLevels.all.first().config
    }
    modeKey.startsWith("ch_") -> {
        Challenges.byId(modeKey)?.config ?: Challenges.all.first().config
    }
    modeKey.startsWith("ta_") -> {
        Modes.timeAttack(modeKey.removePrefix("ta_").toIntOrNull() ?: 3)
    }
    modeKey.startsWith("spd_") -> {
        Modes.speed(modeKey.removePrefix("spd_").toIntOrNull() ?: 12)
    }
    else -> Modes.byId(modeKey) ?: Modes.simple.first()
}

internal fun spawnBurst(
    particles: MutableList<Particle>,
    density: Int,
    color: Color,
    width: Float,
    style: ParticleStyle = ParticleStyle.BURST
) {
    val rng = Random(System.nanoTime())
    repeat(density) {
        val angle = rng.nextFloat() * 2f * Math.PI.toFloat()
        val speed = 60f + rng.nextFloat() * 180f
        particles.add(
            Particle(
                x = width / 2 * 40f + rng.nextFloat() * width * 10f,
                y = width * 15f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 120f,
                life = 1f,
                decay = 1.1f + rng.nextFloat() * 0.8f,
                size = 3f + rng.nextFloat() * 4f,
                color = color,
                spin = if (style == ParticleStyle.SHATTER || style == ParticleStyle.CONFETTI) {
                    (rng.nextFloat() - 0.5f) * 720f
                } else 0f,
                style = style
            )
        )
    }
    if (particles.size > 220) {
        repeat(particles.size - 220) { particles.removeAt(0) }
    }
}

internal fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}

internal fun formatNumber(n: Long): String = n.toString()
