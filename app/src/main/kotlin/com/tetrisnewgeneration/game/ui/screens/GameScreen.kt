package com.tetrisnewgeneration.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.tetrisnewgeneration.game.audio.SfxEngine
import com.tetrisnewgeneration.game.engine.GameAction
import com.tetrisnewgeneration.game.engine.GameSave
import com.tetrisnewgeneration.game.modes.AdventureLevels
import com.tetrisnewgeneration.game.modes.Challenges
import com.tetrisnewgeneration.game.modes.ModeConfig
import com.tetrisnewgeneration.game.modes.Modes
import com.tetrisnewgeneration.game.modes.Puzzles
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.BoardCanvas
import com.tetrisnewgeneration.game.ui.components.MiniPiece
import com.tetrisnewgeneration.game.ui.components.Particle
import com.tetrisnewgeneration.game.ui.components.localizedText
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

    fun haptic() {
        if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Start or resume the round once.
    LaunchedEffect(modeKey) {
        vm.prepare()
        val save = resumeSaveJson?.let {
            try {
                val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                json.decodeFromString(GameSave.serializer(), it)
            } catch (_: Exception) { null }
        }
        vm.start(config, save)
    }

    // Celebration toasts trigger flash + particles + haptics.
    LaunchedEffect(ui.toastSeq) {
        if (ui.toastSeq == 0) return@LaunchedEffect
        val kind = ui.toastKind ?: return@LaunchedEffect
        when (kind) {
            GameViewModel.ToastKind.TETRIS -> {
                haptic(); flashColor = theme.accent; flashAlpha = 0.35f
                spawnBurst(particles, density = 40, color = theme.accent, width = 10f)
            }
            GameViewModel.ToastKind.TSPIN -> {
                haptic(); flashColor = Color(0xFFA855F7); flashAlpha = 0.3f
                spawnBurst(particles, density = 26, color = Color(0xFFA855F7), width = 10f)
            }
            GameViewModel.ToastKind.PERFECT -> {
                haptic(); flashColor = Color(0xFFFACC15); flashAlpha = 0.4f
                spawnBurst(particles, density = 50, color = Color(0xFFFACC15), width = 10f)
            }
            GameViewModel.ToastKind.LEVEL_UP -> {
                flashColor = theme.accent; flashAlpha = 0.25f
            }
            GameViewModel.ToastKind.COMBO -> {
                spawnBurst(particles, density = 14, color = theme.accent, width = 10f)
            }
            GameViewModel.ToastKind.RECORD -> {
                haptic(); flashColor = Color(0xFFFCD34D); flashAlpha = 0.45f
                spawnBurst(particles, density = 60, color = Color(0xFFFCD34D), width = 10f)
            }
            else -> {
                flashColor = theme.accent; flashAlpha = 0.2f
            }
        }
    }

    // Particle physics loop.
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { now ->
                val dt = 1f / 60f
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
            }
        }
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

                BoardCanvas(
                    theme = theme,
                    boardGrid = ui.board,
                    activeCells = ui.activeCells,
                    ghostCells = ui.ghostCells,
                    particles = particles,
                    flashAlpha = flashAlpha,
                    flashColor = flashColor,
                    reducedMotion = reducedMotion,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                )

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
                onHome = { vm.quitToHome(); onHome() }
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
    width: Float
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
                spin = 0f
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
