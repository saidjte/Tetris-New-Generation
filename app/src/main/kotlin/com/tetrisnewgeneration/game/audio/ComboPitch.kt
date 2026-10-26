package com.tetrisnewgeneration.game.audio

/**
 * v1.6.0 (A2): escalation of the combo sound. From combo 2 upward the COMBO
 * sample is re-pitched one semitone-ish step per chain link, so a long chain
 * is *heard* climbing. Pure JVM: both the SfxEngine (playback rate) and the
 * HUD combo chip (visual scale) read the same function so sound and visuals
 * stay in sync from a single source of truth (V5).
 */
object ComboPitch {

    /** SoundPool playback rate at the start of the ramp (combo 2). */
    const val BASE_RATE = 1.0f

    /** Rate increment per combo step above the base. */
    const val STEP = 0.05f

    /** Upper bound — SoundPool rates above 2.0 clamp anyway, 1.5 stays musical. */
    const val MAX_RATE = 1.5f

    /** First combo value that gets an escalation sound (combo 1 is routine). */
    const val MIN_COMBO = 2

    /**
     * Playback rate for the given combo, or `null` when the chain is too
     * short to deserve the COMBO sound (combo < 2 or no chain at all).
     */
    fun rateFor(combo: Int): Float? {
        if (combo < MIN_COMBO) return null
        return (BASE_RATE + STEP * (combo - MIN_COMBO)).coerceAtMost(MAX_RATE)
    }
}
