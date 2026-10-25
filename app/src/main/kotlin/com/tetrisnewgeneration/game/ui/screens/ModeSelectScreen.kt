package com.tetrisnewgeneration.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.modes.AdventureLevels
import com.tetrisnewgeneration.game.modes.Challenges
import com.tetrisnewgeneration.game.modes.Modes
import com.tetrisnewgeneration.game.modes.Puzzles
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.SectionTitle
import com.tetrisnewgeneration.game.ui.components.TngCard
import com.tetrisnewgeneration.game.ui.components.localizedText

/**
 * Mode selection hub: simple modes as cards (with per-mode best score),
 * plus entry cards for Time Attack, Speed, Challenges, Puzzles and Adventure.
 */
@Composable
fun ModeSelectScreen(
    theme: ThemeDef,
    bestScores: suspend (String) -> Long,
    adventureStates: suspend () -> Map<Int, Boolean>,
    challengeStates: suspend () -> Map<String, Boolean>,
    puzzleStates: suspend () -> Set<Int>,
    onBack: () -> Unit,
    onLaunchSimple: (String) -> Unit,
    onLaunchTimeAttack: (Int) -> Unit,
    onLaunchSpeed: (Int) -> Unit,
    onLaunchChallenge: (String) -> Unit,
    onLaunchPuzzle: (Int) -> Unit,
    onLaunchAdventure: (Int) -> Unit
) {
    var best by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var adv by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }
    var chl by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var pzl by remember { mutableStateOf<Set<Int>>(emptySet()) }

    LaunchedEffect(Unit) {
        best = buildMap {
            for (m in Modes.simple) put(m.id, bestScores(m.id))
            put(Modes.TIME_ATTACK, bestScores(Modes.TIME_ATTACK))
            put(Modes.SPEED, bestScores(Modes.SPEED))
        }
        adv = adventureStates().mapValues { it.value }
        chl = challengeStates()
        pzl = puzzleStates()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundTop)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            stringResource(R.string.modes_title),
            color = theme.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(14.dp))

        // Simple modes
        for (mode in Modes.simple) {
            ModeCard(
                theme = theme,
                title = localizedText(arabicName(mode.id), englishName(mode.id)),
                desc = localizedText(arabicDesc(mode.id), englishDesc(mode.id)),
                best = best[mode.id] ?: 0L,
                onClick = { onLaunchSimple(mode.id) }
            )
            Spacer(Modifier.height(10.dp))
        }

        // Time Attack presets
        SectionTitle(stringResource(R.string.pick_duration), theme)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for ((minutes, _) in Modes.timeAttackPresets) {
                TngCard(theme, modifier = Modifier.weight(1f),
                    onClick = { onLaunchTimeAttack(minutes) }) {
                    Text(
                        stringResource(R.string.minutes_fmt, minutes),
                        color = theme.textPrimary, fontWeight = FontWeight.Bold
                    )
                    Text(
                        (best[Modes.TIME_ATTACK] ?: 0L).toString(),
                        color = theme.accent, fontSize = 12.sp
                    )
                }
            }
        }

        // Speed presets
        SectionTitle(stringResource(R.string.pick_difficulty), theme)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for ((level, _) in Modes.speedPresets) {
                TngCard(theme, modifier = Modifier.weight(1f),
                    onClick = { onLaunchSpeed(level) }) {
                    Text(
                        stringResource(R.string.level_fmt, level),
                        color = theme.textPrimary, fontWeight = FontWeight.Bold
                    )
                    Text(
                        (best[Modes.SPEED] ?: 0L).toString(),
                        color = theme.accent, fontSize = 12.sp
                    )
                }
            }
        }

        // Challenges
        SectionTitle(stringResource(R.string.challenges_title), theme)
        for (c in Challenges.all) {
            val done = chl[c.id] == true
            TngCard(theme, modifier = Modifier.fillMaxWidth(),
                onClick = { onLaunchChallenge(c.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            localizedText(c.titleAr, c.titleEn),
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp
                        )
                        Text(
                            localizedText(c.descAr, c.descEn),
                            color = theme.textPrimary.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                        Text(
                            localizedText(
                                "مكافأة: ${c.rewardCoins} عملة",
                                "Reward: ${c.rewardCoins} coins"
                            ),
                            color = theme.accent, fontSize = 11.sp
                        )
                    }
                    if (done) Text(
                        stringResource(R.string.completed),
                        color = theme.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Puzzles
        SectionTitle(stringResource(R.string.puzzles_title), theme)
        for (p in Puzzles.all) {
            val done = p.id in pzl
            TngCard(theme, modifier = Modifier.fillMaxWidth(),
                onClick = { onLaunchPuzzle(p.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.puzzle_fmt, p.id) + " — " +
                                localizedText(p.titleAr, p.titleEn),
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp
                        )
                        Text(
                            localizedText(
                                "القطع: ${p.pieces.size} • الهدف: ${p.goalLines} صف",
                                "Pieces: ${p.pieces.size} • Goal: ${p.goalLines} lines"
                            ),
                            color = theme.textPrimary.copy(alpha = 0.7f), fontSize = 11.sp
                        )
                    }
                    if (done) Text(
                        stringResource(R.string.completed),
                        color = theme.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Adventure
        SectionTitle(stringResource(R.string.adventure_title), theme)
        for (a in AdventureLevels.all) {
            val unlocked = a.id == 1 || adv[a.id - 1] == true
            val done = adv[a.id] == true
            TngCard(
                theme,
                modifier = Modifier.fillMaxWidth(),
                onClick = if (unlocked) ({ onLaunchAdventure(a.id) }) else null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.stage_fmt, a.id) + " — " +
                                localizedText(a.titleAr, a.titleEn),
                            color = if (unlocked) theme.textPrimary
                                    else theme.textPrimary.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold, fontSize = 14.sp
                        )
                        Text(
                            if (unlocked) localizedText(a.descAr, a.descEn)
                            else stringResource(R.string.locked),
                            color = theme.textPrimary.copy(alpha = 0.7f), fontSize = 11.sp
                        )
                    }
                    if (done) Text(
                        stringResource(R.string.completed),
                        color = theme.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                .background(theme.accent.copy(alpha = 0.16f))
                .clickable { onBack() }
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.summary_home), color = theme.textPrimary,
                fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ModeCard(
    theme: ThemeDef,
    title: String,
    desc: String,
    best: Long,
    onClick: () -> Unit
) {
    TngCard(theme = theme, modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Text(title, color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(
            desc,
            color = theme.textPrimary.copy(alpha = 0.72f),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            stringResource(R.string.best_score, best.toString()),
            color = theme.accent,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Localized metadata for the built-in simple modes.
private fun arabicName(id: String) = when (id) {
    Modes.CLASSIC -> "الكلاسيكي"
    Modes.MARATHON -> "الماراثون"
    Modes.QUICK_PLAY -> "اللعب السريع"
    Modes.SURVIVAL -> "البقاء"
    Modes.SPEED -> "السرعة القصوى"
    Modes.ZEN -> "الاسترخاء"
    Modes.PRACTICE -> "الممارسة"
    else -> id
}

private fun englishName(id: String) = when (id) {
    Modes.CLASSIC -> "Classic"
    Modes.MARATHON -> "Marathon"
    Modes.QUICK_PLAY -> "Quick Play"
    Modes.SURVIVAL -> "Survival"
    Modes.SPEED -> "Speed Mode"
    Modes.ZEN -> "Zen"
    Modes.PRACTICE -> "Practice"
    else -> id
}

private fun arabicDesc(id: String) = when (id) {
    Modes.CLASSIC -> "تجربة تقليدية نقية دون إضافات"
    Modes.MARATHON -> "استمر بأطول فترة مع تصاعد الصعوبة"
    Modes.QUICK_PLAY -> "جولة قصيرة من دقيقتين"
    Modes.SURVIVAL -> "السرعة تتزايد كل 20 ثانية"
    Modes.SPEED -> "ابدأ من مستوى عالٍ مباشرة"
    Modes.ZEN -> "سقوط هادئ وتجربة بلا ضغط"
    Modes.PRACTICE -> "تدرب على التحكم والاستراتيجية"
    else -> ""
}

private fun englishDesc(id: String) = when (id) {
    Modes.CLASSIC -> "Pure traditional experience"
    Modes.MARATHON -> "Play as long as you can as difficulty climbs"
    Modes.QUICK_PLAY -> "Short 2-minute round"
    Modes.SURVIVAL -> "Speed rises every 20 seconds"
    Modes.SPEED -> "Start directly at a high level"
    Modes.ZEN -> "Gentle fall speed, pressure-free play"
    Modes.PRACTICE -> "Train controls and strategy"
    else -> ""
}
