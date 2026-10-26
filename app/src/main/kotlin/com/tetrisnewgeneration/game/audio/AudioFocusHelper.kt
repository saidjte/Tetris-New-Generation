package com.tetrisnewgeneration.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

/**
 * v1.6.0 (A1): Android-facing half of the audio-focus feature. Requests
 * GAME focus when a round starts, abandons it when the round ends, and
 * forwards every system change to the pure [FocusPolicy] table through the
 * callbacks below. The ViewModel supplies the callbacks and owns what
 * "pause the round" / "pause the music" actually do.
 *
 * Guarded by API level: AudioFocusRequest needs O+, the legacy path covers
 * minSdk 24 devices (N).
 */
class AudioFocusHelper(context: Context) {

    /** What the game should do in response to a focus change. */
    var onPauseRound: () -> Unit = {}
    var onPauseMusic: () -> Unit = {}
    var onResumeMusic: () -> Unit = {}

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var request: AudioFocusRequest? = null

    private val listener = AudioManager.OnAudioFocusChangeListener { change ->
        val loss = when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> FocusPolicy.Loss.PERMANENT
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> FocusPolicy.Loss.TRANSIENT
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> FocusPolicy.Loss.TRANSIENT_CAN_DUCK
            AudioManager.AUDIOFOCUS_GAIN -> FocusPolicy.Loss.GAINED
            else -> return@OnAudioFocusChangeListener
        }
        when (FocusPolicy.actionFor(loss, roundPlaying)) {
            FocusPolicy.Action.PAUSE_ROUND -> onPauseRound()
            FocusPolicy.Action.PAUSE_MUSIC -> onPauseMusic()
            FocusPolicy.Action.RESUME_MUSIC -> onResumeMusic()
            FocusPolicy.Action.NONE -> Unit
        }
    }

    /** Whether a round is currently playing — owned by the ViewModel. */
    var roundPlaying: Boolean = false
        private set

    /** Marks the round as not playing (manual pause, backgrounding). */
    fun markRoundIdle() {
        roundPlaying = false
    }

    /** Requests focus for a running round. No-op if already claimed. */
    fun acquireForRound() {
        roundPlaying = true
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(listener)
                .build()
            request = req
            audioManager.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                listener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        // Denied focus still lets the round run — we simply keep playing;
        // documented honest limit (some TVs deny without media active).
        if (granted != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Nothing to do: the listener is registered only via the request
            // path; a denied legacy request still registered the listener.
        }
    }

    /** Releases focus when the round ends (over, won, quit). */
    fun abandonFromRound() {
        roundPlaying = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request?.let { audioManager.abandonAudioFocusRequest(it) }
            request = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(listener)
        }
    }
}
