package com.tetrisnewgeneration.game.ui.fx

/**
 * v1.5.0 (V8): global effects-intensity setting. Three levels multiply the
 * particle budget, screen-shake amplitude and glow strength so weak devices
 * can dial the presentation down and strong devices can dial it up.
 * Pure JVM: the settings screen stores [id], every FX consumer reads [factor].
 */
enum class EffectsIntensity(
    val id: String,
    val factor: Float
) {
    LOW("low", 0.5f),
    MEDIUM("medium", 1f),
    HIGH("high", 1.5f);

    companion object {
        /** Parses a stored id; unknown/blank values fall back to MEDIUM. */
        fun fromId(id: String?): EffectsIntensity =
            entries.firstOrNull { it.id == id?.lowercase() } ?: MEDIUM

        /** Convenience for call sites that only need the multiplier. */
        fun factor(id: String?): Float = fromId(id).factor
    }
}
