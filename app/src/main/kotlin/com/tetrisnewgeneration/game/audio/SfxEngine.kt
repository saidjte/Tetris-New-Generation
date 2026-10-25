package com.tetrisnewgeneration.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.tetrisnewgeneration.game.R

/**
 * Local SFX playback. Sounds are WAV resources generated for this project
 * (res/raw), organized in selectable packs. No network is ever involved.
 */
class SfxEngine(private val context: Context) {

    enum class Sound {
        MOVE, ROTATE, DROP, LOCK, CLEAR, COMBO, LEVEL_UP,
        ACHIEVE, REWARD, UNLOCK, ROUND_OVER, WIN, CLICK
    }

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private data class Loaded(val soundId: Int, val resId: Int)
    private val loaded = HashMap<String, Loaded>()
    private var volume = 0.8f
    private var enabled = true
    private var packId = "sfx_original"

    fun applySettings(enabled: Boolean, volume: Float, packId: String) {
        this.enabled = enabled
        this.volume = volume
        if (this.packId != packId) {
            this.packId = packId
            releasePack()
        }
    }

    fun play(sound: Sound) {
        if (!enabled) return
        val entry = resolve(sound) ?: return
        pool.play(entry.soundId, volume, volume, 1, 0, 1f)
    }

    private fun resolve(sound: Sound): Loaded? {
        val key = "${packId}_${sound.name.lowercase()}"
        loaded[key]?.let { return it }
        val resId = context.resources.getIdentifier(key, "raw", context.packageName)
        if (resId == 0) return null
        val soundId = pool.load(context, resId, 1)
        val entry = Loaded(soundId, resId)
        loaded[key] = entry
        return entry
    }

    private fun releasePack() {
        for (entry in loaded.values) pool.unload(entry.soundId)
        loaded.clear()
    }

    fun release() {
        releasePack()
        pool.release()
    }
}
