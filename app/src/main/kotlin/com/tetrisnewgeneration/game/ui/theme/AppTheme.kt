package com.tetrisnewgeneration.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.themes.ThemeDef

/**
 * Game typography (v1.4.0) built from the owner-provided font set:
 *  - [TngFonts.changa]   — Arabic + Latin display face for headings/titles.
 *  - [TngFonts.cairo]    — Arabic + Latin body face, highly readable.
 *  - [TngFonts.orbitron] — futuristic Latin face for digits (score, timer,
 *    coins). Apply it explicitly where numbers are shown.
 */
object TngFonts {
    val orbitron = FontFamily(
        Font(R.font.orbitron_medium, FontWeight.Medium),
        Font(R.font.orbitron_bold, FontWeight.Bold),
        Font(R.font.orbitron_black, FontWeight.Black)
    )
    val changa = FontFamily(
        Font(R.font.changa_regular, FontWeight.Normal),
        Font(R.font.changa_semibold, FontWeight.SemiBold),
        Font(R.font.changa_bold, FontWeight.Bold)
    )
    val cairo = FontFamily(
        Font(R.font.cairo_regular, FontWeight.Normal),
        Font(R.font.cairo_semibold, FontWeight.SemiBold),
        Font(R.font.cairo_bold, FontWeight.Bold)
    )
}

private val TngTypography = Typography(
    displayLarge = TextStyle(fontFamily = TngFonts.changa, fontWeight = FontWeight.Bold, fontSize = 44.sp),
    displayMedium = TextStyle(fontFamily = TngFonts.changa, fontWeight = FontWeight.Bold, fontSize = 34.sp),
    displaySmall = TextStyle(fontFamily = TngFonts.changa, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineLarge = TextStyle(fontFamily = TngFonts.changa, fontWeight = FontWeight.Bold, fontSize = 26.sp),
    headlineMedium = TextStyle(fontFamily = TngFonts.changa, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    headlineSmall = TextStyle(fontFamily = TngFonts.changa, fontWeight = FontWeight.SemiBold, fontSize = 19.sp),
    titleLarge = TextStyle(fontFamily = TngFonts.changa, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = TngFonts.changa, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = TngFonts.changa, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = TngFonts.cairo, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = TngFonts.cairo, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = TngFonts.cairo, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = TngFonts.cairo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = TngFonts.cairo, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = TngFonts.cairo, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

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
        typography = TngTypography,
        content = content
    )
}
