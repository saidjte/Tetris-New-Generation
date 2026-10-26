package com.tetrisnewgeneration.game.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tng_settings")

/** Snapshot of all player settings + wallet/progress values. */
data class PlayerSettings(
    val language: String = "system",   // "system" | "en" | "ar"
    val sfxEnabled: Boolean = true,
    val sfxVolume: Float = 0.8f,
    val voicesEnabled: Boolean = true,    // announcer (v1.4.0)
    val voicesVolume: Float = 0.9f,
    val musicEnabled: Boolean = true,
    val musicVolume: Float = 0.5f,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val themeId: String = "classic_night",
    val blockPackId: String = "blocks_standard",
    val effectPackId: String = "fx_sparks",
    val soundPackId: String = "sfx_original",
    val gestureControls: Boolean = false,   // optional board gestures; buttons always remain available
    val effectsIntensity: String = "medium", // v1.5.0 (V8): low | medium | high
    val imageBackgrounds: Boolean = true,    // v1.5.0 (V1): image backdrop layer on game/home
    val tutorialShown: Boolean = false,
    val coins: Long = 150L,            // starter balance
    val xp: Long = 0L,
    val playerLevel: Int = 1
)

/** Minimal wallet surface, implemented by SettingsStore, faked in tests. */
interface WalletStore {
    suspend fun current(): PlayerSettings
    suspend fun setWallet(coins: Long, xp: Long, playerLevel: Int)
}

/**
 * DataStore-backed settings + wallet storage. DataStore is chosen over Room for
 * these values because they are small single-entries read as reactive streams;
 * structured history stays in Room (see AppDatabase).
 */
class SettingsStore(private val context: Context) : WalletStore {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val VOICES_ENABLED = booleanPreferencesKey("voices_enabled")
        val VOICES_VOLUME = floatPreferencesKey("voices_volume")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val HAPTICS = booleanPreferencesKey("haptics")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val THEME = stringPreferencesKey("theme_id")
        val BLOCK_PACK = stringPreferencesKey("block_pack_id")
        val EFFECT_PACK = stringPreferencesKey("effect_pack_id")
        val SOUND_PACK = stringPreferencesKey("sound_pack_id")
        val GESTURE_CONTROLS = booleanPreferencesKey("gesture_controls")
        val EFFECTS_INTENSITY = stringPreferencesKey("effects_intensity")
        val IMAGE_BACKGROUNDS = booleanPreferencesKey("image_backgrounds")
        val TUTORIAL_SHOWN = booleanPreferencesKey("tutorial_shown")
        val COINS = longPreferencesKey("coins")
        val XP = longPreferencesKey("xp")
        val PLAYER_LEVEL = intPreferencesKey("player_level")
        val LAST_SESSION = stringPreferencesKey("last_session_json")
    }

    val settings: Flow<PlayerSettings> = context.dataStore.data.map { p ->
        PlayerSettings(
            language = p[Keys.LANGUAGE] ?: "system",
            sfxEnabled = p[Keys.SFX_ENABLED] ?: true,
            sfxVolume = p[Keys.SFX_VOLUME] ?: 0.8f,
            voicesEnabled = p[Keys.VOICES_ENABLED] ?: true,
            voicesVolume = p[Keys.VOICES_VOLUME] ?: 0.9f,
            musicEnabled = p[Keys.MUSIC_ENABLED] ?: true,
            musicVolume = p[Keys.MUSIC_VOLUME] ?: 0.5f,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            reducedMotion = p[Keys.REDUCED_MOTION] ?: false,
            themeId = p[Keys.THEME] ?: "classic_night",
            blockPackId = p[Keys.BLOCK_PACK] ?: "blocks_standard",
            effectPackId = p[Keys.EFFECT_PACK] ?: "fx_sparks",
            soundPackId = p[Keys.SOUND_PACK] ?: "sfx_original",
            gestureControls = p[Keys.GESTURE_CONTROLS] ?: false,
            effectsIntensity = p[Keys.EFFECTS_INTENSITY] ?: "medium",
            imageBackgrounds = p[Keys.IMAGE_BACKGROUNDS] ?: true,
            tutorialShown = p[Keys.TUTORIAL_SHOWN] ?: false,
            coins = p[Keys.COINS] ?: 150L,
            xp = p[Keys.XP] ?: 0L,
            playerLevel = p[Keys.PLAYER_LEVEL] ?: 1
        )
    }

    override suspend fun current(): PlayerSettings = settings.first()

    suspend fun setLanguage(value: String) = edit { it[Keys.LANGUAGE] = value }
    suspend fun setSfxEnabled(value: Boolean) = edit { it[Keys.SFX_ENABLED] = value }
    suspend fun setSfxVolume(value: Float) = edit { it[Keys.SFX_VOLUME] = value }
    suspend fun setVoicesEnabled(value: Boolean) = edit { it[Keys.VOICES_ENABLED] = value }
    suspend fun setVoicesVolume(value: Float) = edit { it[Keys.VOICES_VOLUME] = value }
    suspend fun setMusicEnabled(value: Boolean) = edit { it[Keys.MUSIC_ENABLED] = value }
    suspend fun setMusicVolume(value: Float) = edit { it[Keys.MUSIC_VOLUME] = value }
    suspend fun setHaptics(value: Boolean) = edit { it[Keys.HAPTICS] = value }
    suspend fun setReducedMotion(value: Boolean) = edit { it[Keys.REDUCED_MOTION] = value }
    suspend fun setThemeId(value: String) = edit { it[Keys.THEME] = value }
    suspend fun setBlockPackId(value: String) = edit { it[Keys.BLOCK_PACK] = value }
    suspend fun setEffectPackId(value: String) = edit { it[Keys.EFFECT_PACK] = value }
    suspend fun setSoundPackId(value: String) = edit { it[Keys.SOUND_PACK] = value }
    suspend fun setGestureControls(value: Boolean) = edit { it[Keys.GESTURE_CONTROLS] = value }
    suspend fun setEffectsIntensity(value: String) = edit { it[Keys.EFFECTS_INTENSITY] = value }
    suspend fun setImageBackgrounds(value: Boolean) = edit { it[Keys.IMAGE_BACKGROUNDS] = value }
    suspend fun setTutorialShown(value: Boolean) = edit { it[Keys.TUTORIAL_SHOWN] = value }

    override suspend fun setWallet(coins: Long, xp: Long, playerLevel: Int) = edit {
        it[Keys.COINS] = coins
        it[Keys.XP] = xp
        it[Keys.PLAYER_LEVEL] = playerLevel
    }

    suspend fun saveSession(json: String) = edit { it[Keys.LAST_SESSION] = json }
    suspend fun clearSession() = edit { it[Keys.LAST_SESSION] = "" }

    suspend fun loadSessionJson(): String? =
        (context.dataStore.data.first()[Keys.LAST_SESSION])?.takeIf { it.isNotBlank() }

    /** Persists an entire settings snapshot (used by the settings screen). */
    suspend fun updateAll(s: PlayerSettings) = edit {
        it[Keys.LANGUAGE] = s.language
        it[Keys.SFX_ENABLED] = s.sfxEnabled
        it[Keys.SFX_VOLUME] = s.sfxVolume
        it[Keys.VOICES_ENABLED] = s.voicesEnabled
        it[Keys.VOICES_VOLUME] = s.voicesVolume
        it[Keys.MUSIC_ENABLED] = s.musicEnabled
        it[Keys.MUSIC_VOLUME] = s.musicVolume
        it[Keys.HAPTICS] = s.hapticsEnabled
        it[Keys.REDUCED_MOTION] = s.reducedMotion
        it[Keys.THEME] = s.themeId
        it[Keys.BLOCK_PACK] = s.blockPackId
        it[Keys.EFFECT_PACK] = s.effectPackId
        it[Keys.SOUND_PACK] = s.soundPackId
        it[Keys.GESTURE_CONTROLS] = s.gestureControls
        it[Keys.EFFECTS_INTENSITY] = s.effectsIntensity
        it[Keys.IMAGE_BACKGROUNDS] = s.imageBackgrounds
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit { block(it) }
    }
}
