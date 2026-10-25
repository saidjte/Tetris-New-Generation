package com.tetrisnewgeneration.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * Announcer voice engine (v1.4.0). Plays the 275 bundled real-voice clips
 * ("Raffina" recordings provided by the game owner) through MediaPlayer on a
 * single background thread — MediaPlayer streams the MP3 instead of decoding
 * it into memory like SoundPool would, so the whole pack costs ~0 runtime RAM.
 *
 * Semantics:
 *  - Random variant per event, never the same clip twice in a row.
 *  - Normal events are skipped while another announcement is still playing
 *    (the announcer never talks over a bigger moment).
 *  - [interrupt] = true (tetris, perfect clear, game over…) replaces the
 *    current announcement: the latest big moment always wins.
 */
class VoiceEngine(private val context: Context) {

    private var volume = 0.9f
    private var enabled = true
    private val lastIdx = HashMap<String, Int>()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var player: MediaPlayer? = null

    private val speechAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    fun applySettings(enabled: Boolean, volume: Float) {
        this.enabled = enabled
        this.volume = volume
        if (!enabled) quietStop()
    }

    fun play(event: VoiceEvent, interrupt: Boolean = false) {
        if (!enabled) return
        val clips = VoiceCatalog.clips(event)
        if (clips <= 0) return
        var idx = Random.nextInt(clips)
        if (clips > 1 && idx == lastIdx[event.key]) idx = (idx + 1) % clips
        lastIdx[event.key] = idx
        val name = VoiceCatalog.resourceName(event, idx)
        val vol = volume
        executor.execute {
            try {
                val current = player
                if (current != null) {
                    val playing = try { current.isPlaying } catch (_: Exception) { false }
                    if (playing && !interrupt) return@execute
                    try { current.stop() } catch (_: Exception) { }
                    try { current.release() } catch (_: Exception) { }
                    player = null
                }
                val resId = context.resources.getIdentifier(name, "raw", context.packageName)
                if (resId == 0) return@execute
                val afd = context.resources.openRawResourceFd(resId) ?: return@execute
                val p = MediaPlayer()
                p.setAudioAttributes(speechAttributes)
                p.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                p.setOnCompletionListener { it.release() }
                p.prepare()
                p.setVolume(vol, vol)
                p.start()
                player = p
            } catch (_: Exception) {
                // Voice is decorative: never let audio issues affect gameplay.
            }
        }
    }

    /** Stops any playing announcement without releasing the engine. */
    fun quietStop() {
        executor.execute {
            try { player?.stop() } catch (_: Exception) { }
            try { player?.release() } catch (_: Exception) { }
            player = null
        }
    }

    fun release() {
        quietStop()
        executor.shutdown()
    }
}
