package com.tetrisnewgeneration.game.themes

import androidx.compose.ui.graphics.Color
import com.tetrisnewgeneration.game.engine.PieceType

enum class BlockStyle { STANDARD, GLOW, OUTLINE, GEO, CONTRAST, PIXEL, T99 }
enum class BackgroundFx { NONE, STARS, AURORA, PULSE, WAVES, EMBERS, SNOW }
enum class ParticleStyle { SPARKS, BURST, TRAIL, SHATTER, CONFETTI }

/**
 * A complete visual theme: palette, per-piece colors, background animation and
 * default block styling. Themes are data-only; rendering is theme-agnostic.
 */
data class ThemeDef(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val boardBackground: Color,
    val boardBorder: Color,
    val gridLine: Color,
    val textPrimary: Color,
    val accent: Color,
    val surface: Color,
    val pieceColors: Map<PieceType, Color>,
    val backgroundFx: BackgroundFx = BackgroundFx.NONE,
    val blockStyle: BlockStyle = BlockStyle.STANDARD,
    val isDark: Boolean = true
) {
    fun colorOf(type: PieceType): Color = pieceColors.getValue(type)
}

object ThemeDefs {

    private val rainbow = mapOf(
        PieceType.I to Color(0xFF22D3EE),
        PieceType.O to Color(0xFFFACC15),
        PieceType.T to Color(0xFFA855F7),
        PieceType.S to Color(0xFF4ADE80),
        PieceType.Z to Color(0xFFF87171),
        PieceType.J to Color(0xFF60A5FA),
        PieceType.L to Color(0xFFFB923C)
    )

    val all: List<ThemeDef> = listOf(
        ThemeDef(
            id = "classic_night", nameAr = "الليلة الكلاسيكية", nameEn = "Classic Night",
            backgroundTop = Color(0xFF0B0E1A), backgroundBottom = Color(0xFF131A33),
            boardBackground = Color(0xFF0D1120), boardBorder = Color(0xFF3B4B77),
            gridLine = Color(0xFF1B2340), textPrimary = Color(0xFFE7ECFF),
            accent = Color(0xFF5B8CFF), surface = Color(0xFF151C36),
            pieceColors = rainbow, backgroundFx = BackgroundFx.PULSE
        ),
        ThemeDef(
            id = "minimal", nameAr = "مينيمال", nameEn = "Minimal",
            backgroundTop = Color(0xFFF4F5F7), backgroundBottom = Color(0xFFE3E6EB),
            boardBackground = Color(0xFFFFFFFF), boardBorder = Color(0xFFB9BFCC),
            gridLine = Color(0xFFECEEF2), textPrimary = Color(0xFF23272F),
            accent = Color(0xFF3B82F6), surface = Color(0xFFFAFBFC),
            pieceColors = mapOf(
                PieceType.I to Color(0xFF38BDF8), PieceType.O to Color(0xFFEAB308),
                PieceType.T to Color(0xFF8B5CF6), PieceType.S to Color(0xFF22C55E),
                PieceType.Z to Color(0xFFEF4444), PieceType.J to Color(0xFF3B82F6),
                PieceType.L to Color(0xFFF97316)
            ),
            backgroundFx = BackgroundFx.NONE, blockStyle = BlockStyle.STANDARD, isDark = false
        ),
        ThemeDef(
            id = "neon", nameAr = "نيون سايبر", nameEn = "Neon Cyber",
            backgroundTop = Color(0xFF05010F), backgroundBottom = Color(0xFF17063B),
            boardBackground = Color(0xFF0A0420), boardBorder = Color(0xFFB026FF),
            gridLine = Color(0xFF220A4A), textPrimary = Color(0xFFE9D5FF),
            accent = Color(0xFFE879F9), surface = Color(0xFF14082E),
            pieceColors = mapOf(
                PieceType.I to Color(0xFF00F0FF), PieceType.O to Color(0xFFFFEA00),
                PieceType.T to Color(0xFFFF00E5), PieceType.S to Color(0xFF39FF14),
                PieceType.Z to Color(0xFFFF3355), PieceType.J to Color(0xFF4D7CFF),
                PieceType.L to Color(0xFFFF8C00)
            ),
            backgroundFx = BackgroundFx.PULSE, blockStyle = BlockStyle.GLOW
        ),
        ThemeDef(
            id = "galaxy", nameAr = "المجرّة", nameEn = "Galaxy",
            backgroundTop = Color(0xFF02040E), backgroundBottom = Color(0xFF0F1B4D),
            boardBackground = Color(0xFF060A1C), boardBorder = Color(0xFF4C6EF5),
            gridLine = Color(0xFF12204A), textPrimary = Color(0xFFDDE7FF),
            accent = Color(0xFF818CF8), surface = Color(0xFF0C1533),
            pieceColors = mapOf(
                PieceType.I to Color(0xFF7DD3FC), PieceType.O to Color(0xFFFDE68A),
                PieceType.T to Color(0xFFC4B5FD), PieceType.S to Color(0xFF86EFAC),
                PieceType.Z to Color(0xFFFCA5A5), PieceType.J to Color(0xFF93C5FD),
                PieceType.L to Color(0xFFFDBA74)
            ),
            backgroundFx = BackgroundFx.STARS
        ),
        ThemeDef(
            id = "aurora", nameAr = "الشفق القطبي", nameEn = "Aurora",
            backgroundTop = Color(0xFF031622), backgroundBottom = Color(0xFF0B3B44),
            boardBackground = Color(0xFF041E28), boardBorder = Color(0xFF2DD4BF),
            gridLine = Color(0xFF0A3A3A), textPrimary = Color(0xFFCCFBF1),
            accent = Color(0xFF2DD4BF), surface = Color(0xFF073040),
            pieceColors = mapOf(
                PieceType.I to Color(0xFF5EEAD4), PieceType.O to Color(0xFFBEF264),
                PieceType.T to Color(0xFF93C5FD), PieceType.S to Color(0xFF6EE7B7),
                PieceType.Z to Color(0xFFF0ABFC), PieceType.J to Color(0xFF7DD3FC),
                PieceType.L to Color(0xFFFDE68A)
            ),
            backgroundFx = BackgroundFx.AURORA
        ),
        ThemeDef(
            id = "retro", nameAr = "أركيد ريترو", nameEn = "Retro Arcade",
            backgroundTop = Color(0xFF101008), backgroundBottom = Color(0xFF1E1B0A),
            boardBackground = Color(0xFF0D0D05), boardBorder = Color(0xFF8A8A3A),
            gridLine = Color(0xFF22220F), textPrimary = Color(0xFFEDEDC8),
            accent = Color(0xFFD3D36A), surface = Color(0xFF191711),
            pieceColors = mapOf(
                PieceType.I to Color(0xFF9AE6B4), PieceType.O to Color(0xFFF6E05E),
                PieceType.T to Color(0xFFD6BCFA), PieceType.S to Color(0xFF68D391),
                PieceType.Z to Color(0xFFFC8181), PieceType.J to Color(0xFF90CDF4),
                PieceType.L to Color(0xFFF6AD55)
            ),
            backgroundFx = BackgroundFx.NONE, blockStyle = BlockStyle.PIXEL
        ),
        ThemeDef(
            id = "crystal", nameAr = "الكريستال", nameEn = "Crystal",
            backgroundTop = Color(0xFF0E1526), backgroundBottom = Color(0xFF24344D),
            boardBackground = Color(0xFF101A2E), boardBorder = Color(0xFF93C5FD),
            gridLine = Color(0xFF1E2C48), textPrimary = Color(0xFFE0F2FE),
            accent = Color(0xFF7DD3FC), surface = Color(0xFF16233C),
            pieceColors = mapOf(
                PieceType.I to Color(0xFFA5F3FC), PieceType.O to Color(0xFFFEF08A),
                PieceType.T to Color(0xFFDDD6FE), PieceType.S to Color(0xFFBBF7D0),
                PieceType.Z to Color(0xFFFECACA), PieceType.J to Color(0xFFBFDBFE),
                PieceType.L to Color(0xFFFED7AA)
            ),
            backgroundFx = BackgroundFx.SNOW, blockStyle = BlockStyle.GEO
        ),
        ThemeDef(
            id = "lava", nameAr = "الحمم", nameEn = "Lava",
            backgroundTop = Color(0xFF160404), backgroundBottom = Color(0xFF4A0E0E),
            boardBackground = Color(0xFF1C0606), boardBorder = Color(0xFFF87171),
            gridLine = Color(0xFF3B0F0F), textPrimary = Color(0xFFFFE4E6),
            accent = Color(0xFFFB7185), surface = Color(0xFF260B0B),
            pieceColors = mapOf(
                PieceType.I to Color(0xFFFCA5A5), PieceType.O to Color(0xFFFBBF24),
                PieceType.T to Color(0xFFF472B6), PieceType.S to Color(0xFFFB923C),
                PieceType.Z to Color(0xFFEF4444), PieceType.J to Color(0xFFF97316),
                PieceType.L to Color(0xFFDC2626)
            ),
            backgroundFx = BackgroundFx.EMBERS, blockStyle = BlockStyle.GLOW
        ),
        ThemeDef(
            id = "ocean", nameAr = "المحيط", nameEn = "Ocean",
            backgroundTop = Color(0xFF02121F), backgroundBottom = Color(0xFF062E4A),
            boardBackground = Color(0xFF03182A), boardBorder = Color(0xFF38BDF8),
            gridLine = Color(0xFF0A2C44), textPrimary = Color(0xFFBAE6FD),
            accent = Color(0xFF38BDF8), surface = Color(0xFF052444),
            pieceColors = mapOf(
                PieceType.I to Color(0xFF67E8F9), PieceType.O to Color(0xFFFCD34D),
                PieceType.T to Color(0xFFA78BFA), PieceType.S to Color(0xFF34D399),
                PieceType.Z to Color(0xFFFB7185), PieceType.J to Color(0xFF60A5FA),
                PieceType.L to Color(0xFFFDBA74)
            ),
            backgroundFx = BackgroundFx.WAVES
        ),
        ThemeDef(
            id = "monochrome", nameAr = "أحادي اللون", nameEn = "Monochrome",
            backgroundTop = Color(0xFF0A0A0A), backgroundBottom = Color(0xFF1F1F1F),
            boardBackground = Color(0xFF111111), boardBorder = Color(0xFF888888),
            gridLine = Color(0xFF232323), textPrimary = Color(0xFFE5E5E5),
            accent = Color(0xFFD4D4D4), surface = Color(0xFF191919),
            pieceColors = mapOf(
                PieceType.I to Color(0xFFE5E5E5), PieceType.O to Color(0xFFBFBFBF),
                PieceType.T to Color(0xFFA3A3A3), PieceType.S to Color(0xFF8C8C8C),
                PieceType.Z to Color(0xFF737373), PieceType.J to Color(0xFF5C5C5C),
                PieceType.L to Color(0xFF474747)
            )
        ),
        ThemeDef(
            id = "golden", nameAr = "الذهبي", nameEn = "Golden",
            backgroundTop = Color(0xFF120C02), backgroundBottom = Color(0xFF3E2B05),
            boardBackground = Color(0xFF1A1204), boardBorder = Color(0xFFFCD34D),
            gridLine = Color(0xFF3A2A0A), textPrimary = Color(0xFFFEF3C7),
            accent = Color(0xFFFBBF24), surface = Color(0xFF241A06),
            pieceColors = mapOf(
                PieceType.I to Color(0xFFFDE68A), PieceType.O to Color(0xFFFCD34D),
                PieceType.T to Color(0xFFF59E0B), PieceType.S to Color(0xFFD97706),
                PieceType.Z to Color(0xFFB45309), PieceType.J to Color(0xFFFBBF24),
                PieceType.L to Color(0xFFFCD34D)
            ),
            backgroundFx = BackgroundFx.EMBERS, blockStyle = BlockStyle.GLOW
        ),
        ThemeDef(
            id = "nature", nameAr = "الطبيعة", nameEn = "Nature",
            backgroundTop = Color(0xFF04140A), backgroundBottom = Color(0xFF14532D),
            boardBackground = Color(0xFF06180D), boardBorder = Color(0xFF4ADE80),
            gridLine = Color(0xFF0D2E1A), textPrimary = Color(0xFFDCFCE7),
            accent = Color(0xFF4ADE80), surface = Color(0xFF0A2415),
            pieceColors = mapOf(
                PieceType.I to Color(0xFF86EFAC), PieceType.O to Color(0xFFFDE047),
                PieceType.T to Color(0xFFC084FC), PieceType.S to Color(0xFF4ADE80),
                PieceType.Z to Color(0xFFF87171), PieceType.J to Color(0xFF2DD4BF),
                PieceType.L to Color(0xFFF97316)
            ),
            backgroundFx = BackgroundFx.SNOW
        ),
        ThemeDef(
            id = "pixel", nameAr = "بكسل آرت", nameEn = "Pixel Art",
            backgroundTop = Color(0xFF0F0F23), backgroundBottom = Color(0xFF1F1F3A),
            boardBackground = Color(0xFF12122A), boardBorder = Color(0xFF6B6B9E),
            gridLine = Color(0xFF1E1E38), textPrimary = Color(0xFFE4E4F5),
            accent = Color(0xFF9F9FE8), surface = Color(0xFF181830),
            pieceColors = mapOf(
                PieceType.I to Color(0xFF55D6D6), PieceType.O to Color(0xFFD6D655),
                PieceType.T to Color(0xFFB455D6), PieceType.S to Color(0xFF55D655),
                PieceType.Z to Color(0xFFD65555), PieceType.J to Color(0xFF5555D6),
                PieceType.L to Color(0xFFD69555)
            ),
            backgroundFx = BackgroundFx.NONE, blockStyle = BlockStyle.PIXEL
        )
    )

    fun byId(id: String): ThemeDef = all.firstOrNull { it.id == id } ?: all.first()
}
