package com.tetrisnewgeneration.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.tetrisnewgeneration.game.R

/**
 * Looping background music from local resources. Tracks:
 *  - music_menu  (calm, menus)
 *  - music_play  (energetic, standard play)
 *  - music_zen   (very calm, Zen mode)
 * Playback pauses automatically when the app goes to background (caller wires
 * lifecycle), volume follows the player's settings, and nothing is streamed.
 */
class MusicEngine(private val context: Context) {

    enum class Track { MENU, PLAY, ZEN }

    private var player: MediaPlayer? = null
    private var current: Track? = null
    private var volume = 0.5f
    private var enabled = true

    fun applySettings(enabled: Boolean, volume: Float) {
        this.enabled = enabled
        this.volume = volume
        updateVolume()
        if (!enabled) stop()
    }

    fun play(track: Track) {
        if (!enabled) return
        if (current == track && player?.isPlaying == true) return
        stop()
        val resId = when (track) {
            Track.MENU -> R.raw.music_menu
            Track.PLAY -> R.raw.music_play
            Track.ZEN -> R.raw.music_zen
        }
        player = MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setVolume(volume, volume)
            start()
        }
        current = track
    }

    fun pause() {
        if (player?.isPlaying == true) player?.pause()
    }

    fun resume() {
        if (enabled && player != null && player?.isPlaying != true) player?.start()
    }

    private fun updateVolume() {
        player?.setVolume(volume, volume)
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
        current = null
    }
}
