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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.progression.AchievementDefs
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.CoinBadge
import com.tetrisnewgeneration.game.ui.components.TngCard
import com.tetrisnewgeneration.game.ui.components.localizedText
import com.tetrisnewgeneration.game.ui.vm.TasksViewModel

/** Achievements (permanent) + daily tasks with claimable rewards. */
@Composable
fun TasksScreen(theme: ThemeDef, onBack: () -> Unit) {
    val vm: TasksViewModel = viewModel()
    val ui by vm.ui.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { vm.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundTop)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.btn_tasks),
                color = theme.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            CoinBadge(theme, ui.coins)
        }

        TabRow(
            selectedTabIndex = tab,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = theme.accent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Tab(selected = tab == 0, onClick = { tab = 0 },
                text = { Text(stringResource(R.string.tab_achievements), fontSize = 12.sp, color = theme.textPrimary) })
            Tab(selected = tab == 1, onClick = { tab = 1 },
                text = { Text(stringResource(R.string.tab_daily), fontSize = 12.sp, color = theme.textPrimary) })
        }

        when (tab) {
            0 -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(6.dp))
                for ((def, prog) in ui.achievements) {
                    val unlocked = prog >= def.target
                    TngCard(theme, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Column {
                                    Text(
                                        localizedText(def.titleAr, def.titleEn),
                                        color = if (unlocked) theme.accent else theme.textPrimary,
                                        fontWeight = FontWeight.Bold, fontSize = 14.sp
                                    )
                                    Text(
                                        localizedText(def.descAr, def.descEn),
                                        color = theme.textPrimary.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = {
                                            (prog.toFloat() / def.target.toFloat()).coerceIn(0f, 1f)
                                        },
                                        color = theme.accent,
                                        trackColor = theme.boardBackground,
                                        modifier = Modifier.fillMaxWidth().height(5.dp)
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                            Spacer(Modifier.padding(horizontal = 6.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    stringResource(R.string.reward_fmt, def.rewardCoins),
                                    color = theme.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    stringResource(R.string.progress_fmt, minOf(prog, def.target), def.target),
                                    color = theme.textPrimary.copy(alpha = 0.7f), fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(6.dp))
                for (task in ui.tasks) {
                    val done = task.progress >= task.def.target
                    TngCard(theme, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) {
                                Column {
                                    Text(
                                        localizedText(task.def.titleAr, task.def.titleEn),
                                        color = theme.textPrimary,
                                        fontWeight = FontWeight.Bold, fontSize = 14.sp
                                    )
                                    Text(
                                        stringResource(
                                            R.string.progress_fmt,
                                            minOf(task.progress, task.def.target),
                                            task.def.target
                                        ),
                                        color = theme.textPrimary.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            val claimable = done && !task.claimed
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            task.claimed -> theme.boardBackground
                                            claimable -> theme.accent
                                            else -> theme.boardBackground
                                        }
                                    )
                                    .clickable(enabled = claimable) { vm.claim(task.def) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    when {
                                        task.claimed -> stringResource(R.string.claimed)
                                        claimable -> stringResource(R.string.claim)
                                        else -> stringResource(R.string.reward_fmt, task.def.rewardCoins)
                                    },
                                    color = if (task.claimed) theme.textPrimary.copy(alpha = 0.5f)
                                            else if (claimable) androidx.compose.ui.graphics.Color(0xFF0B0E1A)
                                            else theme.accent,
                                    fontSize = 12.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .padding(bottom = 18.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                .background(theme.accent.copy(alpha = 0.9f))
                .clickable { onBack() }
                .padding(horizontal = 26.dp, vertical = 12.dp)
        ) {
            Text(stringResource(R.string.close), color = androidx.compose.ui.graphics.Color(0xFF0B0E1A),
                fontWeight = FontWeight.Bold)
        }
    }
}

// Alias used by AchievementDefs import to keep compiler happy.
private val defs = AchievementDefs.all
