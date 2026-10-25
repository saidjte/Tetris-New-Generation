package com.tetrisnewgeneration.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.data.repo.ProgressionRepository
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.themes.BackgroundFx
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.CoinBadge
import com.tetrisnewgeneration.game.ui.components.MenuButtonRow
import com.tetrisnewgeneration.game.ui.components.PrimaryActionButton
import com.tetrisnewgeneration.game.ui.components.TngCard
import java.util.Locale

/**
 * Home screen: brand, big play button, continue-session shortcut and menu rows.
 * A light animated background keeps the mood of the selected theme.
 */
@Composable
fun HomeScreen(
    settings: PlayerSettings,
    theme: ThemeDef,
    hasSavedSession: Boolean,
    onPlay: () -> Unit,
    onContinue: () -> Unit,
    onModes: () -> Unit,
    onHowTo: () -> Unit,
    onStore: () -> Unit,
    onTasks: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    val xpNeeded = remember(settings.playerLevel) {
        ProgressionRepository.xpNeeded(settings.playerLevel)
    }

    Box(modifier = Modifier.fillMaxSize().background(theme.backgroundTop)) {
        HomeBackdrop(theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand block
            Spacer(Modifier.height(10.dp))
            LogoBlocks(theme)
            Spacer(Modifier.height(14.dp))
            Text(
                if (isArabic) stringResource(R.string.app_name_ar) else stringResource(R.string.app_name),
                color = theme.textPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            if (isArabic) {
                Text(
                    "TETRIS NEW GENERATION",
                    color = theme.accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 3.sp
                )
            } else {
                Text(
                    "تيتريس الجيل الجديد",
                    color = theme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                stringResource(R.string.tagline),
                color = theme.textPrimary.copy(alpha = 0.65f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(22.dp))

            // Player progress strip
            TngCard(theme = theme, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.player_level, settings.playerLevel),
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            stringResource(R.string.xp_to_next, (xpNeeded - settings.xp).coerceAtLeast(0)),
                            color = theme.textPrimary.copy(alpha = 0.65f),
                            fontSize = 10.sp
                        )
                    }
                    CoinBadge(theme, settings.coins)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = {
                        (settings.xp.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f)
                    },
                    color = theme.accent,
                    trackColor = theme.boardBackground,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                )
            }

            Spacer(Modifier.height(18.dp))

            PrimaryActionButton(
                text = stringResource(R.string.btn_play),
                theme = theme,
                onClick = onPlay
            )

            if (hasSavedSession) {
                Spacer(Modifier.height(10.dp))
                PrimaryActionButton(
                    text = stringResource(R.string.btn_continue),
                    theme = theme,
                    onClick = onContinue
                )
            }

            Spacer(Modifier.height(18.dp))

            MenuButtonRow(stringResource(R.string.btn_modes), theme, onClick = onModes)
            Spacer(Modifier.height(10.dp))
            MenuButtonRow(stringResource(R.string.btn_howto), theme, onClick = onHowTo)
            Spacer(Modifier.height(10.dp))
            MenuButtonRow(stringResource(R.string.btn_store), theme, onClick = onStore)
            Spacer(Modifier.height(10.dp))
            MenuButtonRow(stringResource(R.string.btn_tasks), theme, onClick = onTasks)
            Spacer(Modifier.height(10.dp))
            MenuButtonRow(stringResource(R.string.btn_stats), theme, onClick = onStats)
            Spacer(Modifier.height(10.dp))
            MenuButtonRow(stringResource(R.string.btn_settings), theme, onClick = onSettings)

            Spacer(Modifier.height(26.dp))
        }
    }
}

/** Decorative falling-block silhouettes behind the home content. */
@Composable
private fun HomeBackdrop(theme: ThemeDef) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val cell = size.width / 14f
        val shapes = listOf(
            Triple(1f, 2f, com.tetrisnewgeneration.game.engine.PieceType.T),
            Triple(7f, 4f, com.tetrisnewgeneration.game.engine.PieceType.I),
            Triple(10f, 1f, com.tetrisnewgeneration.game.engine.PieceType.O),
            Triple(3f, 8f, com.tetrisnewgeneration.game.engine.PieceType.L),
            Triple(9f, 10f, com.tetrisnewgeneration.game.engine.PieceType.S),
            Triple(5f, 13f, com.tetrisnewgeneration.game.engine.PieceType.Z)
        )
        for ((fx, fy, type) in shapes) {
            val color = theme.colorOf(type).copy(alpha = 0.10f)
            val cells = com.tetrisnewgeneration.game.engine.Tetrominoes.shape(type, 0)
            for (c in cells) {
                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(fx * cell + c.x * cell, fy * cell + c.y * cell),
                    size = androidx.compose.ui.geometry.Size(cell - 4, cell - 4),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cell * 0.2f, cell * 0.2f)
                )
            }
        }
    }
}

/** Small stylized logo made of four colored blocks. */
@Composable
private fun LogoBlocks(theme: ThemeDef) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        val cell = 15.dp.toPx()
        val ox = (size.width - cell * 4) / 2
        val oy = (size.height - cell * 2) / 2
        val layout = listOf(
            Triple(0, 0, com.tetrisnewgeneration.game.engine.PieceType.I),
            Triple(1, 0, com.tetrisnewgeneration.game.engine.PieceType.T),
            Triple(2, 0, com.tetrisnewgeneration.game.engine.PieceType.O),
            Triple(1, 1, com.tetrisnewgeneration.game.engine.PieceType.L)
        )
        for ((x, y, type) in layout) {
            drawRoundRect(
                color = theme.colorOf(type),
                topLeft = androidx.compose.ui.geometry.Offset(ox + x * cell + 2, oy + y * cell + 2),
                size = androidx.compose.ui.geometry.Size(cell - 4, cell - 4),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cell * 0.22f, cell * 0.22f)
            )
        }
    }
}
