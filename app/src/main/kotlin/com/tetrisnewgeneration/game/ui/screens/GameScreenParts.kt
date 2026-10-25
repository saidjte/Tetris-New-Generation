package com.tetrisnewgeneration.game.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.engine.GameAction
import com.tetrisnewgeneration.game.modes.AdventureLevels
import com.tetrisnewgeneration.game.modes.Challenges
import com.tetrisnewgeneration.game.modes.Puzzles
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.MiniPiece
import com.tetrisnewgeneration.game.ui.components.TngCard
import com.tetrisnewgeneration.game.ui.components.localizedText
import com.tetrisnewgeneration.game.ui.vm.GameViewModel
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.min

@Composable
fun HudStat(label: String, value: String, theme: ThemeDef) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(theme.surface.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(Color.Transparent)
    ) {
        Text(label, color = theme.textPrimary.copy(alpha = 0.7f), fontSize = 9.sp)
        Text(value, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun PauseButton(theme: ThemeDef, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(theme.surface.copy(alpha = 0.85f))
            .border(1.dp, theme.boardBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("⏸", color = theme.textPrimary, fontSize = 16.sp)
    }
}

@Composable
fun GoalProgress(ui: GameViewModel.UiState, theme: ThemeDef) {
    val (current, target) = when {
        ui.goalLines != null -> ui.lines to ui.goalLines
        ui.goalScore != null -> ui.score.toInt() to ui.goalScore!!.toInt()
        ui.goalTetrises != null -> ui.tetrises to ui.goalTetrises
        ui.goalCombo != null -> (ui.combo + 1).coerceAtLeast(0) to ui.goalCombo
        else -> 0 to 1
    }
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Row {
            Text(
                stringResource(R.string.hud_goal),
                color = theme.textPrimary.copy(alpha = 0.75f),
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${min(current, target)} / $target",
                color = theme.textPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            color = theme.accent,
            trackColor = theme.surface,
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun HoldBox(theme: ThemeDef, typeIndex: Int?, canHold: Boolean) {
    Box(
        modifier = Modifier
            .size(56.dp, 40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(theme.boardBackground.copy(alpha = 0.9f))
            .border(
                1.dp,
                if (canHold) theme.boardBorder else theme.boardBorder.copy(alpha = 0.25f),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (typeIndex != null) {
            MiniPiece(theme = theme, typeIndex = typeIndex, cellSize = 8.dp,
                alpha = if (canHold) 1f else 0.4f)
        }
    }
}

/** Touch control pad with DAS-style auto repeat on press-and-hold. */
@Composable
fun GameControls(
    theme: ThemeDef,
    enabled: Boolean,
    onPress: (GameAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ControlButton(theme, "⟲", Modifier.weight(1f), enabled) { onPress(GameAction.RotateCcw) }
            ControlButton(theme, "⟳", Modifier.weight(1f), enabled) { onPress(GameAction.RotateCw) }
            ControlButton(theme, "⤓", Modifier.weight(1f), enabled) { onPress(GameAction.HardDrop) }
            ControlButton(theme, "⇄", Modifier.weight(1f), enabled) { onPress(GameAction.Hold) }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RepeatingButton(theme, "◀", Modifier.weight(1f), enabled,
                onDown = { onPress(GameAction.MoveLeft) },
                onRepeat = { onPress(GameAction.MoveLeft) })
            RepeatingButton(theme, "▼", Modifier.weight(1f), enabled,
                onDown = { onPress(GameAction.SoftDropOn) },
                onRepeat = { },
                onUp = { onPress(GameAction.SoftDropOff) })
            RepeatingButton(theme, "▶", Modifier.weight(1f), enabled,
                onDown = { onPress(GameAction.MoveRight) },
                onRepeat = { onPress(GameAction.MoveRight) })
        }
    }
}

/**
 * Full-screen gesture layer (v2). It is composed as the BOTTOM child of the
 * screen box, so every button, HUD control and dialog drawn above it wins
 * hit-testing first — gestures only receive touches on empty space, which
 * turns the whole screen into the touch surface while the button pad stays
 * fully functional.
 *
 * Scheme (each action confirmed by a haptic tick):
 *   tap anywhere          -> rotate clockwise
 *   horizontal drag       -> move one cell per step
 *   slow drag down        -> soft drop while the finger is down
 *   fast flick down       -> hard drop on release
 *   deliberate drag up    -> hold
 */
@Composable
fun BoardGestureLayer(
    onAction: (GameAction) -> Unit,
    onHapticLight: () -> Unit,
    onHapticStrong: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    onHapticStrong()
                    onAction(GameAction.RotateCw)
                }
            }
            .pointerInput(Unit) {
                var accX = 0f
                var accY = 0f
                var softDropping = false
                var velocity = 0f          // smoothed px/ms, > 0 downward
                var lastTimeMs = 0L
                detectDragGestures(
                    onDragStart = {
                        accX = 0f; accY = 0f
                        softDropping = false
                        velocity = 0f
                        lastTimeMs = System.currentTimeMillis()
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        val now = System.currentTimeMillis()
                        val dt = (now - lastTimeMs).coerceAtLeast(1L)
                        lastTimeMs = now
                        accX += amount.x
                        accY += amount.y
                        velocity = 0.65f * velocity + 0.35f * (amount.y / dt)
                        val cell = size.width / 10f
                        while (accX >= cell) {
                            onHapticLight(); onAction(GameAction.MoveRight); accX -= cell
                        }
                        while (accX <= -cell) {
                            onHapticLight(); onAction(GameAction.MoveLeft); accX += cell
                        }
                        if (!softDropping && accY >= cell) {
                            softDropping = true
                            onAction(GameAction.SoftDropOn)
                        }
                        if (accY < 0f) softDropping = false // upward correction cancels soft drop
                    },
                    onDragEnd = {
                        if (softDropping) onAction(GameAction.SoftDropOff)
                        val cell = size.width / 10f
                        when (
                            GestureClassifier.classify(
                                totalDownPx = accY.coerceAtLeast(0f),
                                totalUpPx = (-accY).coerceAtLeast(0f),
                                velocityPxMs = velocity,
                                cellPx = cell
                            )
                        ) {
                            GestureClassifier.EndGesture.HARD_DROP -> {
                                onHapticStrong(); onAction(GameAction.HardDrop)
                            }
                            GestureClassifier.EndGesture.HOLD -> {
                                onHapticStrong(); onAction(GameAction.Hold)
                            }
                            GestureClassifier.EndGesture.NONE -> Unit
                        }
                    },
                    onDragCancel = {
                        if (softDropping) onAction(GameAction.SoftDropOff)
                    }
                )
            }
    )
}

/** Gentle infinite pulse that keeps the event banner alive on screen. */
@Composable
internal fun rememberBannerPulse(reducedMotion: Boolean): Float {
    if (reducedMotion) return 1f
    val transition = rememberInfiniteTransition(label = "bannerPulse")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bannerScale"
    ).value
}

/**
 * The big color-coded event pill (v2): near-opaque dark scrim so it reads on
 * any theme, accent-colored bold text, accent border and a soft shadow.
 */
@Composable
internal fun EventBanner(text: String, accent: Color, pulse: Float) {
    Text(
        text,
        color = accent,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        letterSpacing = 1.5.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(top = 96.dp)
            .scale(pulse)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xF0050713))
            .border(2.dp, accent, RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
private fun ControlButton(
    theme: ThemeDef,
    glyph: String,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) theme.surface else theme.surface.copy(alpha = 0.4f))
            .border(1.dp, theme.boardBorder.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .pointerInput(enabled) {
                if (enabled) detectTapGestures { _ -> onClick() }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            glyph,
            color = if (enabled) theme.textPrimary else theme.textPrimary.copy(alpha = 0.35f),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RepeatingButton(
    theme: ThemeDef,
    glyph: String,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onDown: () -> Unit,
    onRepeat: () -> Unit,
    onUp: (() -> Unit)? = null
) {
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed, enabled) {
        if (pressed && enabled) {
            onDown()
            while (pressed && enabled) {
                delay(170)
                if (!pressed) break
                onRepeat()
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (pressed && enabled) theme.accent.copy(alpha = 0.35f)
                else if (enabled) theme.surface else theme.surface.copy(alpha = 0.4f)
            )
            .border(1.dp, theme.boardBorder.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        if (enabled) {
                            pressed = true
                            try {
                                awaitRelease()
                            } finally {
                                pressed = false
                                onUp?.invoke()
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            glyph,
            color = if (enabled) theme.textPrimary else theme.textPrimary.copy(alpha = 0.35f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PauseOverlay(
    theme: ThemeDef,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB0050713)),
        contentAlignment = Alignment.Center
    ) {
        TngCard(theme = theme, modifier = Modifier.fillMaxWidth().padding(32.dp)) {
            Text(
                stringResource(R.string.pause_title),
                color = theme.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            SummaryAction(stringResource(R.string.pause_resume), theme, onResume)
            Spacer(Modifier.height(8.dp))
            SummaryAction(stringResource(R.string.pause_restart), theme, onRestart)
            Spacer(Modifier.height(8.dp))
            SummaryAction(stringResource(R.string.pause_home), theme, onHome)
        }
    }
}

@Composable
fun SummaryAction(text: String, theme: ThemeDef, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(theme.accent.copy(alpha = 0.16f))
            .clickable { onClick() }
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = theme.textPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RoundSummaryDialog(
    ui: GameViewModel.UiState,
    theme: ThemeDef,
    modeKey: String,
    onRetry: () -> Unit,
    onNextStage: () -> Unit,
    onHome: () -> Unit,
    onShare: () -> Unit
) {
    val won = ui.won
    val isChallenge = modeKey.startsWith("ch_")
    val isPuzzle = modeKey.startsWith("puzzle_")
    val isAdventure = modeKey.startsWith("adv_")

    val title = when {
        won && isChallenge -> stringResource(R.string.summary_challenge_done)
        won && isPuzzle -> stringResource(R.string.summary_puzzle_done)
        won && isAdventure -> stringResource(R.string.summary_round_complete)
        ui.timeLeftMs != null && won -> stringResource(R.string.summary_time_up)
        won -> stringResource(R.string.summary_round_complete)
        else -> stringResource(R.string.summary_game_over)
    }

    val desc = if (isChallenge && !won) stringResource(R.string.summary_challenge_failed) else null

    val adventure = if (isAdventure) {
        val id = modeKey.removePrefix("adv_").toIntOrNull() ?: 1
        AdventureLevels.byId(id)
    } else null

    val challenge = if (isChallenge) Challenges.byId(modeKey) else null
    val puzzle = if (isPuzzle) {
        val id = modeKey.removePrefix("puzzle_").toIntOrNull() ?: 1
        Puzzles.all.firstOrNull { it.id == id }
    } else null

    val hasNextAdventure = adventure != null && AdventureLevels.byId(adventure.id + 1) != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xC0050713))
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        TngCard(theme = theme, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                title,
                color = if (won) theme.accent else theme.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            desc?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = theme.textPrimary.copy(alpha = 0.8f),
                    fontSize = 13.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(14.dp))

            if (ui.newRecord) {
                Text(
                    stringResource(R.string.hud_new_record),
                    color = Color(0xFFFCD34D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            SummaryRow(stringResource(R.string.hud_score), formatNumber(ui.score), theme)
            SummaryRow(
                stringResource(R.string.summary_best_fmt, ui.bestBefore),
                "",
                theme
            )
            SummaryRow(stringResource(R.string.hud_lines), ui.lines.toString(), theme)
            SummaryRow(stringResource(R.string.hud_level), ui.level.toString(), theme)
            SummaryRow(stringResource(R.string.hud_time), formatTime(ui.elapsedMs), theme)

            Spacer(Modifier.height(12.dp))

            Text(
                stringResource(R.string.summary_rewards),
                color = theme.textPrimary.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
            SummaryRow(
                stringResource(R.string.summary_coins_fmt, ui.rewardCoins),
                stringResource(R.string.summary_xp_fmt, ui.rewardXp),
                theme
            )
            if (ui.levelsGained > 0) {
                Text(
                    stringResource(R.string.summary_level_up_fmt, ui.playerLevelAfter),
                    color = theme.accent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            if (won && isAdventure && hasNextAdventure) {
                SummaryAction(stringResource(R.string.summary_next_stage), theme, onNextStage)
                Spacer(Modifier.height(8.dp))
            }
            SummaryAction(stringResource(R.string.summary_retry), theme, onRetry)
            Spacer(Modifier.height(8.dp))
            SummaryAction(stringResource(R.string.summary_share), theme, onShare)
            Spacer(Modifier.height(8.dp))
            SummaryAction(stringResource(R.string.summary_home), theme, onHome)

            // Hint text for challenge/adventure rewards
            if (won && challenge != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    localizedText(
                        "مكافأة التحدي: ${challenge.rewardCoins} عملة",
                        "Challenge reward: ${challenge.rewardCoins} coins"
                    ),
                    color = theme.textPrimary.copy(alpha = 0.6f), fontSize = 11.sp
                )
            }
            if (won && puzzle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    localizedText(
                        "أُنجز اللغز ${puzzle.id}",
                        "Puzzle ${puzzle.id} cleared"
                    ),
                    color = theme.textPrimary.copy(alpha = 0.6f), fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, theme: ThemeDef) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = theme.textPrimary.copy(alpha = 0.85f), fontSize = 13.sp)
        if (value.isNotBlank()) {
            Text(value, color = theme.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}
