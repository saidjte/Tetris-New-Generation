package com.tetrisnewgeneration.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.tetrisnewgeneration.game.R

/**
 * Local SFX playback. Sounds are bundled resources (res/raw), organized in
 * selectable packs. Packs ship different file sets: the v1.4.0 "T99" pack
 * (real arcade recordings) has dedicated sounds for every clear tier,
 * T-spin tiers, danger/relief and records, while the four synthesized packs
 * only cover the base events — [fallback] bridges the gap so every pack
 * keeps working without missing files.
 */
class SfxEngine(private val context: Context) {

    enum class Sound {
        MOVE, ROTATE, DROP, LOCK, CLEAR, CLEAR_DOUBLE, CLEAR_TRIPLE, TETRIS,
        COMBO, LEVEL_UP, ACHIEVE, REWARD, UNLOCK, ROUND_OVER, WIN, CLICK,
        COUNTDOWN, GO,
        HOLD, TSPIN, TSPIN_DOUBLE, TSPIN_TRIPLE, DANGER, SAVE, RECORD,
        PERFECT, B2B   // v1.6.0 (A5): dedicated perfect-clear / back-to-back
    }

    private fun suffix(sound: Sound): String = SfxSoundMap.suffix(sound)

    private fun fallback(sound: Sound): Sound? = SfxSoundMap.fallback(sound)

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

    /**
     * v1.6.0 (A2): [rate] re-pitches the sample (SoundPool rate 0.5–2.0),
     * driving the combo-pitch escalation. v1.6.0 (A7): [fromPack] plays the
     * sound from an arbitrary pack without switching the active one (store
     * previews); cached entries are keyed by pack so nothing leaks between
     * packs.
     */
    fun play(sound: Sound, rate: Float = 1f, fromPack: String? = null) {
        if (!enabled) return
        val entry = resolve(sound, fromPack ?: packId) ?: return
        pool.play(entry.soundId, volume, volume, 1, 0, rate.coerceIn(0.5f, 2f))
    }

    private fun resolve(sound: Sound, pack: String): Loaded? {
        var s: Sound? = sound
        while (s != null) {
            val key = "${pack}_${suffix(s)}"
            loaded[key]?.let { return it }
            val resId = context.resources.getIdentifier(key, "raw", context.packageName)
            if (resId != 0) {
                val soundId = pool.load(context, resId, 1)
                val entry = Loaded(soundId, resId)
                loaded[key] = entry
                return entry
            }
            s = fallback(s)
        }
        return null
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
