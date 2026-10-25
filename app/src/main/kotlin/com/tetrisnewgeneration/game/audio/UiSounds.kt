package com.tetrisnewgeneration.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Menu/UI click-sound engine (v1.4.0). Thirteen short interface sounds
 * converted from the Steam Deck UI set: toggles, tabs, modal open/close,
 * game launch, achievement toast, navigation. Follows the global SFX
 * enabled/volume settings so one switch silences the whole app.
 *
 * Implemented as a process-wide object initialized once from [init] —
 * composables deep in the tree can call the wrappers without threading a
 * reference through every screen.
 */
object UiSounds {

    private var pool: SoundPool? = null
    private var appContext: Context? = null
    private val ids = HashMap<String, Int>()
    private val missing = HashSet<String>()
    private var enabled = true
    private var volume = 0.8f

    fun init(context: Context) {
        if (pool != null) return
        appContext = context.applicationContext
        pool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    fun applySettings(sfxEnabled: Boolean, sfxVolume: Float) {
        enabled = sfxEnabled
        volume = sfxVolume
    }

    /** Plays `res/raw/<name>.mp3` (e.g. "ui_tab"). Silently no-ops if absent. */
    fun play(name: String) {
        if (!enabled) return
        val p = pool ?: return
        val ctx = appContext ?: return
        val id = ids[name] ?: run {
            if (name in missing) return
            val resId = ctx.resources.getIdentifier(name, "raw", ctx.packageName)
            if (resId == 0) {
                missing.add(name)
                return
            }
            val loaded = p.load(ctx, resId, 1)
            ids[name] = loaded
            loaded
        }
        p.play(id, volume, volume, 1, 0, 1f)
    }

    // Convenience wrappers keep call sites readable.
    fun activate() = play("ui_activate")
    fun toggle(on: Boolean) = play(if (on) "ui_toggle_on" else "ui_toggle_off")
    fun tab() = play("ui_tab")
    fun modalShow() = play("ui_modal_show")
    fun modalHide() = play("ui_modal_hide")
    fun launch() = play("ui_launch")
    fun achievement() = play("ui_achievement")
    fun toast() = play("ui_toast")
    fun back() = play("ui_back")
    fun navigate() = play("ui_navigate")
    fun scroll() = play("ui_scroll")
}
