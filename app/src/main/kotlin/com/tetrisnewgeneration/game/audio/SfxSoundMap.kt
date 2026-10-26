package com.tetrisnewgeneration.game.audio

/**
 * Pure mapping between [SfxEngine.Sound] and raw-resource suffixes plus the
 * per-pack fallback chain. Pure JVM so the chains are unit-testable.
 *
 * Packs ship different file sets: the v1.4.0 T99 pack (real arcade
 * recordings) covers every event, while the four synthesized packs only have
 * base events — the fallback chain bridges the gaps.
 */
object SfxSoundMap {

    /** Resource suffix per sound (pack files are named `<pack>_<suffix>`). */
    fun suffix(sound: SfxEngine.Sound): String = when (sound) {
        SfxEngine.Sound.DROP -> "hard"
        SfxEngine.Sound.LEVEL_UP -> "levelup"
        SfxEngine.Sound.ROUND_OVER -> "over"
        SfxEngine.Sound.COUNTDOWN -> "tick"
        SfxEngine.Sound.CLEAR_DOUBLE -> "clear2"
        SfxEngine.Sound.CLEAR_TRIPLE -> "clear3"
        SfxEngine.Sound.TETRIS -> "tetris"
        SfxEngine.Sound.TSPIN_DOUBLE -> "tspin2"
        SfxEngine.Sound.TSPIN_TRIPLE -> "tspin3"
        SfxEngine.Sound.PERFECT -> "perfect"   // v1.6.0 (A5)
        SfxEngine.Sound.B2B -> "b2b"           // v1.6.0 (A5)
        else -> sound.name.lowercase()
    }

    /** Chain used when the current pack has no file for a sound. */
    fun fallback(sound: SfxEngine.Sound): SfxEngine.Sound? = when (sound) {
        SfxEngine.Sound.CLEAR_DOUBLE -> SfxEngine.Sound.CLEAR
        SfxEngine.Sound.CLEAR_TRIPLE -> SfxEngine.Sound.CLEAR
        SfxEngine.Sound.TETRIS -> SfxEngine.Sound.CLEAR
        SfxEngine.Sound.TSPIN -> SfxEngine.Sound.CLEAR
        SfxEngine.Sound.TSPIN_DOUBLE -> SfxEngine.Sound.TSPIN
        SfxEngine.Sound.TSPIN_TRIPLE -> SfxEngine.Sound.TSPIN
        SfxEngine.Sound.HOLD -> SfxEngine.Sound.CLICK
        SfxEngine.Sound.RECORD -> SfxEngine.Sound.WIN
        SfxEngine.Sound.ACHIEVE -> SfxEngine.Sound.LEVEL_UP
        SfxEngine.Sound.REWARD -> SfxEngine.Sound.SAVE
        SfxEngine.Sound.UNLOCK -> SfxEngine.Sound.LEVEL_UP
        SfxEngine.Sound.COMBO -> SfxEngine.Sound.CLEAR
        SfxEngine.Sound.PERFECT -> SfxEngine.Sound.TETRIS
        SfxEngine.Sound.B2B -> SfxEngine.Sound.COMBO
        else -> null
    }

    /** Walks the whole chain for [sound] (start included). */
    fun chain(sound: SfxEngine.Sound): List<SfxEngine.Sound> {
        val out = mutableListOf<SfxEngine.Sound>()
        var s: SfxEngine.Sound? = sound
        var guard = 0
        while (s != null && guard < SfxEngine.Sound.entries.size + 1) {
            out += s
            s = fallback(s)
            guard++
        }
        return out
    }
}
