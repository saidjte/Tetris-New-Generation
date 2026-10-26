package com.tetrisnewgeneration.game.audio

/**
 * v1.6.0 (A1): pure decision table for Android audio-focus changes. Kept
 * free of android.media so every row is unit-testable; [AudioFocusHelper]
 * is the thin Android-facing half that maps system constants onto [Loss].
 */
object FocusPolicy {

    enum class Loss {
        /** Another app took focus permanently (call, navigation, media). */
        PERMANENT,

        /** Transient loss without ducking (notification beep, assistant). */
        TRANSIENT,

        /** Transient loss where lower volumes are permitted. */
        TRANSIENT_CAN_DUCK,

        /** Focus came back to us. */
        GAINED
    }

    enum class Action {
        /** Permanent loss: auto-pause the whole round (session is saved). */
        PAUSE_ROUND,

        /** Transient loss: silence our music, keep the round running. */
        PAUSE_MUSIC,

        /** Focus restored: bring our music back (if the round wants it). */
        RESUME_MUSIC,

        /** Nothing to do. */
        NONE
    }

    fun actionFor(loss: Loss, roundPlaying: Boolean): Action = when (loss) {
        Loss.PERMANENT -> if (roundPlaying) Action.PAUSE_ROUND else Action.NONE
        Loss.TRANSIENT -> Action.PAUSE_MUSIC
        Loss.TRANSIENT_CAN_DUCK -> Action.PAUSE_MUSIC
        Loss.GAINED -> if (roundPlaying) Action.RESUME_MUSIC else Action.NONE
    }
}
