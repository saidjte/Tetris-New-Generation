package com.tetrisnewgeneration.game.audio

/**
 * v1.6.0 (A4): music ducking policy. Big announcer moments (tetris, perfect
 * clear, records, round end) momentarily lower the music so the voice
 * carries — the classic "sportscaster duck". Pure JVM constants + decision;
 * the MusicEngine applies the factor, the ViewModel owns the timer.
 */
object DuckPolicy {

    /** How long the music stays ducked after the latest interrupting voice. */
    const val DUCK_DURATION_MS = 1100L

    /** Music volume multiplier while ducked (45% of the player's setting). */
    const val DUCK_FACTOR = 0.45f

    /**
     * The VM requests a duck exactly when it asks the announcer to
     * [interrupt] whatever is currently playing — i.e. only for the big
     * moments; small voices ride over the music untouched.
     */
    fun shouldDuck(interrupt: Boolean): Boolean = interrupt
}
