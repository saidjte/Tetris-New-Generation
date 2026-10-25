package com.tetrisnewgeneration.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.data.repo.ScoreRepository
import com.tetrisnewgeneration.game.data.repo.StatKeys
import com.tetrisnewgeneration.game.modes.Modes
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.SectionTitle
import com.tetrisnewgeneration.game.ui.components.StatTile
import com.tetrisnewgeneration.game.ui.components.TngCard

/** Lifetime statistics + best score per mode. */
@Composable
fun StatsScreen(
    theme: ThemeDef,
    loadStats: suspend () -> Map<String, Long>,
    loadModeBests: suspend () -> List<Pair<String, Long>>,
    onBack: () -> Unit
) {
    var stats by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var modeBests by remember { mutableStateOf<List<Pair<String, Long>>>(emptyList()) }

    LaunchedEffect(Unit) {
        stats = loadStats()
        modeBests = loadModeBests()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundTop)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            stringResource(R.string.stats_title),
            color = theme.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(14.dp))

        Row {
            StatTile(
                stringResource(R.string.stat_rounds),
                (stats[StatKeys.GAMES_PLAYED] ?: 0).toString(),
                theme, modifier = Modifier.weight(1f).padding(4.dp)
            )
            StatTile(
                stringResource(R.string.stat_lines),
                (stats[StatKeys.TOTAL_LINES] ?: 0).toString(),
                theme, modifier = Modifier.weight(1f).padding(4.dp)
            )
        }
        Row {
            StatTile(
                stringResource(R.string.stat_score),
                (stats[StatKeys.TOTAL_SCORE] ?: 0).toString(),
                theme, modifier = Modifier.weight(1f).padding(4.dp)
            )
            StatTile(
                stringResource(R.string.stat_tetrises),
                (stats[StatKeys.TETRISES] ?: 0).toString(),
                theme, modifier = Modifier.weight(1f).padding(4.dp)
            )
        }
        Row {
            StatTile(
                stringResource(R.string.stat_max_combo),
                (stats[StatKeys.MAX_COMBO] ?: 0).toString(),
                theme, modifier = Modifier.weight(1f).padding(4.dp)
            )
            StatTile(
                stringResource(R.string.stat_max_level),
                (stats[StatKeys.MAX_LEVEL] ?: 0).toString(),
                theme, modifier = Modifier.weight(1f).padding(4.dp)
            )
        }
        Row {
            StatTile(
                stringResource(R.string.stat_time),
                formatDuration(stats[StatKeys.TOTAL_TIME_MS] ?: 0),
                theme, modifier = Modifier.weight(1f).padding(4.dp)
            )
            StatTile(
                stringResource(R.string.stat_hard_drops),
                (stats[StatKeys.HARD_DROPS] ?: 0).toString(),
                theme, modifier = Modifier.weight(1f).padding(4.dp)
            )
        }

        SectionTitle(stringResource(R.string.stat_by_mode), theme)
        for ((modeId, best) in modeBests) {
            TngCard(theme, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row {
                    Text(
                        modeDisplayName(modeId),
                        color = theme.textPrimary, fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp, modifier = Modifier.weight(1f)
                    )
                    Text(best.toString(), color = theme.accent, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                .background(theme.accent.copy(alpha = 0.16f))
                .clickable { onBack() }
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.close), color = theme.textPrimary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun modeDisplayName(modeId: String): String = when (modeId) {
    Modes.CLASSIC -> stringResourceOr(modeId, "الكلاسيكي")
    Modes.MARATHON -> stringResourceOr(modeId, "الماراثون")
    Modes.QUICK_PLAY -> stringResourceOr(modeId, "اللعب السريع")
    Modes.TIME_ATTACK -> stringResourceOr(modeId, "سباق الوقت")
    Modes.SURVIVAL -> stringResourceOr(modeId, "البقاء")
    Modes.SPEED -> stringResourceOr(modeId, "السرعة القصوى")
    Modes.ZEN -> stringResourceOr(modeId, "الاسترخاء")
    Modes.PRACTICE -> stringResourceOr(modeId, "الممارسة")
    Modes.CHALLENGES -> stringResourceOr(modeId, "التحديات")
    Modes.PUZZLES -> stringResourceOr(modeId, "الألغاز")
    Modes.ADVENTURE -> stringResourceOr(modeId, "المغامرة")
    else -> modeId
}

@Composable
private fun stringResourceOr(modeId: String, ar: String): String {
    val isArabic = androidx.compose.ui.platform.LocalConfiguration.current
        .locales[0].language == "ar"
    return if (isArabic) ar else when (modeId) {
        Modes.CLASSIC -> "Classic"
        Modes.MARATHON -> "Marathon"
        Modes.QUICK_PLAY -> "Quick Play"
        Modes.TIME_ATTACK -> "Time Attack"
        Modes.SURVIVAL -> "Survival"
        Modes.SPEED -> "Speed Mode"
        Modes.ZEN -> "Zen"
        Modes.PRACTICE -> "Practice"
        Modes.CHALLENGES -> "Challenges"
        Modes.PUZZLES -> "Puzzles"
        Modes.ADVENTURE -> "Adventure"
        else -> modeId
    }
}

private fun formatDuration(ms: Long): String {
    val totalMin = ms / 60000
    val hours = totalMin / 60
    val minutes = totalMin % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
