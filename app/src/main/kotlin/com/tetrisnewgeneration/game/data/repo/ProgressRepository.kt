package com.tetrisnewgeneration.game.data.repo

import com.tetrisnewgeneration.game.data.db.AchievementDao
import com.tetrisnewgeneration.game.data.db.AchievementEntity
import com.tetrisnewgeneration.game.data.db.ChallengeDao
import com.tetrisnewgeneration.game.data.db.ChallengeEntity
import com.tetrisnewgeneration.game.data.db.DailyTaskDao
import com.tetrisnewgeneration.game.data.db.DailyTaskEntity
import com.tetrisnewgeneration.game.data.db.AdventureDao
import com.tetrisnewgeneration.game.data.db.AdventureProgressEntity
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.data.settings.SettingsStore
import com.tetrisnewgeneration.game.progression.AchievementDef
import com.tetrisnewgeneration.game.progression.AchievementDefs
import com.tetrisnewgeneration.game.progression.DailyTaskDef
import com.tetrisnewgeneration.game.progression.DailyTaskDefs
import com.tetrisnewgeneration.game.store.Category
import java.time.LocalDate

/**
 * Achievements, daily tasks and adventure progress. Everything is evaluated
 * locally from stat counters; rewards are granted exactly once thanks to the
 * unlocked flag persisted in Room.
 */
class ProgressRepository(
    private val achievementDao: AchievementDao,
    private val challengeDao: ChallengeDao,
    private val dailyTaskDao: DailyTaskDao,
    private val adventureDao: AdventureDao,
    private val settings: SettingsStore,
    private val progression: ProgressionRepository
) {

    // ------------------------------------------------------------ achievements

    suspend fun achievementStates(): Map<String, AchievementEntity> =
        achievementDao.all().associateBy { it.id }

    /**
     * Feeds a counter value into every achievement watching it; newly completed
     * achievements are persisted and their coin reward is credited once.
     */
    suspend fun evaluateStat(statKey: String, value: Long): List<AchievementDef> {
        val newlyUnlocked = mutableListOf<AchievementDef>()
        val states = achievementStates()
        val snapshot = settings.current()
        var coins = snapshot.coins

        for (def in AchievementDefs.all) {
            if (def.statKey != statKey) continue
            val current = states[def.id]
            if (current?.unlocked == true) continue
            if (value >= def.target) {
                achievementDao.put(
                    AchievementEntity(def.id, unlocked = true, progress = def.target,
                        unlockedAt = System.currentTimeMillis())
                )
                coins += def.rewardCoins
                newlyUnlocked.add(def)
            } else {
                achievementDao.put(
                    AchievementEntity(def.id, unlocked = false, progress = value.toInt(),
                        unlockedAt = 0)
                )
            }
        }
        if (newlyUnlocked.isNotEmpty()) {
            settings.setWallet(coins = coins, xp = snapshot.xp, playerLevel = snapshot.playerLevel)
        }
        return newlyUnlocked
    }

    /** Unlocks sprint-style achievements that are event based (not counters). */
    suspend fun unlockEventAchievement(id: String): AchievementDef? {
        val def = AchievementDefs.byId(id) ?: return null
        val current = achievementDao.all().firstOrNull { it.id == id }
        if (current?.unlocked == true) return null
        achievementDao.put(
            AchievementEntity(id, unlocked = true, progress = def.target,
                unlockedAt = System.currentTimeMillis())
        )
        val snapshot = settings.current()
        settings.setWallet(
            coins = snapshot.coins + def.rewardCoins,
            xp = snapshot.xp,
            playerLevel = snapshot.playerLevel
        )
        return def
    }

    // ------------------------------------------------------------ challenges

    suspend fun challengeStates(): Map<String, ChallengeEntity> =
        challengeDao.all().associateBy { it.id }

    /** Marks a challenge complete and grants its reward exactly once. */
    suspend fun completeChallenge(id: String): Boolean {
        val existing = challengeDao.get(id)
        if (existing?.rewardClaimed == true) return false
        challengeDao.put(
            ChallengeEntity(id, completed = true, completedAt = System.currentTimeMillis(),
                rewardClaimed = true)
        )
        val def = com.tetrisnewgeneration.game.modes.Challenges.byId(id) ?: return true
        val snapshot = settings.current()
        val update = progression.applyGains(
            snapshot, def.rewardXp.toLong(), def.rewardCoins.toLong()
        )
        return update.levelsGained >= 0 // success marker
    }

    // ------------------------------------------------------------ adventure

    suspend fun adventureStates(): Map<Int, AdventureProgressEntity> =
        adventureDao.all().associateBy { it.levelId }

    suspend fun isAdventureUnlocked(levelId: Int): Boolean {
        if (levelId == 1) return true
        return adventureDao.get(levelId - 1)?.completed == true
    }

    suspend fun completeAdventure(levelId: Int, score: Long): Boolean {
        val existing = adventureDao.get(levelId)
        if (existing?.completed == true) {
            if (score > existing.bestScore) {
                adventureDao.put(existing.copy(bestScore = score))
            }
            return false // already rewarded once
        }
        adventureDao.put(
            AdventureProgressEntity(levelId, completed = true, bestScore = score)
        )
        val def = com.tetrisnewgeneration.game.modes.AdventureLevels.byId(levelId)
        if (def != null) {
            val snapshot = settings.current()
            progression.applyGains(snapshot, def.rewardXp, def.rewardCoins.toLong())
        }
        return true
    }

    // ------------------------------------------------------------ daily tasks

    fun todayStamp(): Int = LocalDate.now().toEpochDay().toInt()

    /** Deterministic 3-task selection for today, from the day-of-year seed. */
    fun todaysTasks(): List<DailyTaskDef> {
        val day = todayStamp()
        val rotated = DailyTaskDefs.pool.size
        val start = ((day % rotated) + rotated) % rotated
        return listOf(
            DailyTaskDefs.pool[start],
            DailyTaskDefs.pool[(start + 1) % rotated],
            DailyTaskDefs.pool[(start + 2) % rotated]
        )
    }

    suspend fun dailyTaskStates(): Map<String, DailyTaskEntity> =
        dailyTaskDao.all().associateBy { it.id }

    /** Adds progress to today's tasks. Progress resets automatically per day. */
    suspend fun addDailyProgress(statKey: String, amount: Int) {
        for (task in todaysTasks()) {
            if (task.statKey != statKey) continue
            val existing = dailyTaskDao.get(task.id)
            val stamp = todayStamp()
            val progress = if (existing?.dayStamp == stamp) existing.progress else 0
            if (existing?.claimed == true && existing.dayStamp == stamp) continue
            dailyTaskDao.put(
                DailyTaskEntity(task.id, stamp, (progress + amount).coerceAtLeast(0),
                    claimed = false)
            )
        }
    }

    /** Claims a finished task's reward; only claimable when progress >= target. */
    suspend fun claimDailyTask(task: DailyTaskDef): Boolean {
        val existing = dailyTaskDao.get(task.id)
        val stamp = todayStamp()
        if (existing?.dayStamp == stamp && existing.claimed) return false
        val progress = if (existing?.dayStamp == stamp) existing.progress else 0
        if (progress < task.target) return false
        dailyTaskDao.put(DailyTaskEntity(task.id, stamp, progress, claimed = true))
        val snapshot = settings.current()
        settings.setWallet(
            coins = snapshot.coins + task.rewardCoins,
            xp = snapshot.xp,
            playerLevel = snapshot.playerLevel
        )
        return true
    }

    // ------------------------------------------------------------ helpers

    /** Own-theme count achievement helper. */
    suspend fun evaluateThemeOwnership(ownedThemes: Int) {
        evaluateStat("themes_3", ownedThemes.toLong())
    }

    suspend fun evaluateAll(stats: Map<String, Long>): List<AchievementDef> {
        val unlocked = mutableListOf<AchievementDef>()
        for ((key, value) in stats) {
            unlocked += evaluateStat(key, value)
        }
        return unlocked
    }

    suspend fun resetAll() {
        for (a in achievementDao.all()) achievementDao.put(
            AchievementEntity(a.id, unlocked = false, progress = 0, unlockedAt = 0)
        )
        for (c in challengeDao.all()) challengeDao.put(
            ChallengeEntity(c.id, completed = false, completedAt = 0, rewardClaimed = false)
        )
        for (t in dailyTaskDao.all()) dailyTaskDao.put(
            DailyTaskEntity(t.id, 0, 0, claimed = false)
        )
        for (a in adventureDao.all()) adventureDao.put(
            AdventureProgressEntity(a.levelId, completed = false, bestScore = 0)
        )
    }
}
