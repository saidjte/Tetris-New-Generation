package com.tetrisnewgeneration.game

import android.content.Context
import com.tetrisnewgeneration.game.audio.MusicEngine
import com.tetrisnewgeneration.game.audio.SfxEngine
import com.tetrisnewgeneration.game.data.db.AppDatabase
import com.tetrisnewgeneration.game.data.repo.ProgressionRepository
import com.tetrisnewgeneration.game.data.repo.ProgressRepository
import com.tetrisnewgeneration.game.data.repo.ScoreRepository
import com.tetrisnewgeneration.game.data.repo.StoreRepository
import com.tetrisnewgeneration.game.data.settings.SettingsStore

/**
 * Hand-rolled dependency container. Kept simple on purpose: one instance owned
 * by the Application, everything scoped to the process lifetime.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: AppDatabase by lazy { AppDatabase.get(appContext) }
    val settings: SettingsStore by lazy { SettingsStore(appContext) }

    val scoreRepository: ScoreRepository by lazy {
        ScoreRepository(database.scoreDao(), database.statDao())
    }
    val storeRepository: StoreRepository by lazy {
        StoreRepository(database.purchaseDao(), settings) {
            appContext.assets.open("store.json").bufferedReader().use { it.readText() }
        }
    }
    val progressionRepository: ProgressionRepository by lazy {
        ProgressionRepository(settings)
    }
    val progressRepository: ProgressRepository by lazy {
        ProgressRepository(
            database.achievementDao(),
            database.challengeDao(),
            database.dailyTaskDao(),
            database.adventureDao(),
            settings,
            progressionRepository
        )
    }

    val sfx: SfxEngine by lazy { SfxEngine(appContext) }
    val music: MusicEngine by lazy { MusicEngine(appContext) }
}
