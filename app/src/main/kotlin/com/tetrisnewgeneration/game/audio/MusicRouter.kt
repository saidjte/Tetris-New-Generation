package com.tetrisnewgeneration.game.audio

import com.tetrisnewgeneration.game.audio.MusicEngine.Track

/**
 * v1.6.0 (A3): single decision point for which music track a round plays.
 * Unifies the two intensity sources that already existed independently —
 * the level-8 ramp (v1.4.0) and the danger state (DangerTracker, v1.4.0) —
 * so an endangered low-level stack jumps straight to the intense track and
 * a relieved stack falls back to whatever its level dictates.
 *
 * Pure JVM; the ViewModel only switches tracks when this result *changes*.
 */
object MusicRouter {

    /** Level from which the intense track stays on (v1.4.0 behavior). */
    const val INTENSE_LEVEL = 8

    fun trackFor(zenMode: Boolean, level: Int, dangerActive: Boolean): Track = when {
        zenMode -> Track.ZEN
        dangerActive -> Track.PLAY_INTENSE
        level >= INTENSE_LEVEL -> Track.PLAY_INTENSE
        else -> Track.PLAY
    }
}
