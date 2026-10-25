package com.tetrisnewgeneration.game.store

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Store item types. Everything is bought with locally earned coins; no real
 * payments, no server, no downloads — content ships inside the APK.
 */
@Serializable
enum class Category { THEME, BLOCK_PACK, EFFECT_PACK, SOUND_PACK }

@Serializable
data class StoreItem(
    val id: String,
    val category: Category,
    val price: Int,
    val free: Boolean = false
)

/**
 * The catalog lives in assets/store.json so prices and item sets can be tuned
 * without touching code. This object is the compiled-in fallback + validator.
 */
object StoreCatalog {

    val DEFAULT_ITEMS: List<StoreItem> = listOf(
        // Themes (12)
        StoreItem("classic_night", Category.THEME, 0, free = true),
        StoreItem("minimal", Category.THEME, 0, free = true),
        StoreItem("neon", Category.THEME, 120),
        StoreItem("galaxy", Category.THEME, 150),
        StoreItem("aurora", Category.THEME, 150),
        StoreItem("retro", Category.THEME, 100),
        StoreItem("crystal", Category.THEME, 140),
        StoreItem("lava", Category.THEME, 140),
        StoreItem("ocean", Category.THEME, 110),
        StoreItem("monochrome", Category.THEME, 90),
        StoreItem("golden", Category.THEME, 200),
        StoreItem("nature", Category.THEME, 120),
        StoreItem("pixel", Category.THEME, 130),
        // Block packs
        StoreItem("blocks_standard", Category.BLOCK_PACK, 0, free = true),
        StoreItem("blocks_glow", Category.BLOCK_PACK, 80),
        StoreItem("blocks_outline", Category.BLOCK_PACK, 60),
        StoreItem("blocks_geo", Category.BLOCK_PACK, 60),
        StoreItem("blocks_contrast", Category.BLOCK_PACK, 90),
        // Effect packs
        StoreItem("fx_sparks", Category.EFFECT_PACK, 0, free = true),
        StoreItem("fx_burst", Category.EFFECT_PACK, 70),
        StoreItem("fx_trail", Category.EFFECT_PACK, 70),
        StoreItem("fx_shatter", Category.EFFECT_PACK, 90),
        StoreItem("fx_confetti", Category.EFFECT_PACK, 100),
        // Sound packs
        StoreItem("sfx_original", Category.SOUND_PACK, 0, free = true),
        StoreItem("sfx_arcade", Category.SOUND_PACK, 80),
        StoreItem("sfx_future", Category.SOUND_PACK, 90),
        StoreItem("sfx_calm", Category.SOUND_PACK, 80)
    )

    fun fromJson(text: String): List<StoreItem>? = try {
        Json.decodeFromString<List<StoreItem>>(text)
    } catch (_: Exception) {
        null
    }

    fun toJson(items: List<StoreItem>): String = Json.encodeToString(items)
}
