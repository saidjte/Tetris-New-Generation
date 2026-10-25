package com.tetrisnewgeneration.game.themes

/**
 * Rendering variants for block packs and effect packs. Pure data: the canvas
 * renderer interprets these flags, so packs need no code per item.
 */
data class BlockPackDef(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val style: BlockStyle,
    val glow: Boolean
)

data class EffectPackDef(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val particle: ParticleStyle,
    val density: Int
)

object PackDefs {

    val blockPacks: List<BlockPackDef> = listOf(
        BlockPackDef("blocks_standard", "قياسي", "Standard", BlockStyle.STANDARD, glow = false),
        BlockPackDef("blocks_glow", "متوهج", "Glow", BlockStyle.GLOW, glow = true),
        BlockPackDef("blocks_outline", "محدد", "Outline", BlockStyle.OUTLINE, glow = false),
        BlockPackDef("blocks_geo", "هندسي", "Geometric", BlockStyle.GEO, glow = false),
        BlockPackDef("blocks_contrast", "تباين عالٍ", "High Contrast", BlockStyle.CONTRAST, glow = false),
        // v1.4.0: real sprite skin + board/panel frames from the provided asset pack.
        BlockPackDef("blocks_t99", "مظهر T99", "T99 Look", BlockStyle.T99, glow = true)
    )

    val effectPacks: List<EffectPackDef> = listOf(
        EffectPackDef("fx_sparks", "شرارات", "Sparks", ParticleStyle.SPARKS, density = 24),
        EffectPackDef("fx_burst", "انفجار", "Burst", ParticleStyle.BURST, density = 36),
        EffectPackDef("fx_trail", "أثر متحرك", "Trail", ParticleStyle.TRAIL, density = 18),
        EffectPackDef("fx_shatter", "تحطّم", "Shatter", ParticleStyle.SHATTER, density = 28),
        EffectPackDef("fx_confetti", "احتفال", "Confetti", ParticleStyle.CONFETTI, density = 40)
    )

    fun blockPack(id: String): BlockPackDef =
        blockPacks.firstOrNull { it.id == id } ?: blockPacks.first()

    fun effectPack(id: String): EffectPackDef =
        effectPacks.firstOrNull { it.id == id } ?: effectPacks.first()
}
