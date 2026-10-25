package com.tetrisnewgeneration.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.themes.ThemeDef

/**
 * Material3 color scheme derived from the selected game theme, so menus feel
 * consistent with the playfield.
 */
@Composable
fun TngTheme(
    theme: ThemeDef,
    settings: PlayerSettings,
    content: @Composable () -> Unit
) {
    val darkTheme = if (settings.language == "system") isSystemInDarkTheme() else theme.isDark

    val scheme = if (darkTheme || theme.isDark) {
        darkColorScheme(
            primary = theme.accent,
            onPrimary = Color(0xFF0B0E1A),
            secondary = theme.accent.copy(alpha = 0.7f),
            background = theme.backgroundTop,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.boardBackground,
            onSurfaceVariant = theme.textPrimary.copy(alpha = 0.75f),
            outline = theme.boardBorder
        )
    } else {
        lightColorScheme(
            primary = theme.accent,
            onPrimary = Color.White,
            secondary = theme.accent.copy(alpha = 0.7f),
            background = theme.backgroundTop,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.boardBackground,
            onSurfaceVariant = theme.textPrimary.copy(alpha = 0.75f),
            outline = theme.boardBorder
        )
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
