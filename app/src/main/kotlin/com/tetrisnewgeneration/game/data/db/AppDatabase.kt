package com.tetrisnewgeneration.game.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------------------------
// Entities
// ---------------------------------------------------------------------------

/** A finished round (any mode). */
@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeId: String,
    val score: Long,
    val lines: Int,
    val level: Int,
    val durationMs: Long,
    val won: Boolean,
    val finishedAt: Long
)

/** Generic counter stats (games played, total lines, ...). */
@Entity(tableName = "stats")
data class StatEntity(
    @PrimaryKey val key: String,
    val value: Long
)

/** Unlocked achievements state. */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlocked: Boolean,
    val progress: Int,
    val unlockedAt: Long
)

/** Owned store items (purchased or granted for free). */
@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val itemId: String,
    val ownedAt: Long
)

/** Adventure stage progress. */
@Entity(tableName = "adventure_progress")
data class AdventureProgressEntity(
    @PrimaryKey val levelId: Int,
    val completed: Boolean,
    val bestScore: Long
)

/** Challenge completion state (completion stamp prevents double rewards). */
@Entity(tableName = "challenge_progress")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val completed: Boolean,
    val completedAt: Long,
    val rewardClaimed: Boolean
)

/** Daily task state keyed by task id + day stamp. */
@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey val id: String,
    val dayStamp: Int,
    val progress: Int,
    val claimed: Boolean
)

// ---------------------------------------------------------------------------
// DAOs (plain interfaces so pure-JVM fakes can implement them in tests)
// ---------------------------------------------------------------------------

@Dao
interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: ScoreEntity): Long

    @Query("SELECT * FROM scores WHERE modeId = :modeId ORDER BY score DESC LIMIT 1")
    suspend fun bestForMode(modeId: String): ScoreEntity?

    @Query("SELECT * FROM scores WHERE modeId = :modeId ORDER BY score DESC LIMIT :limit")
    suspend fun recentBest(modeId: String, limit: Int): List<ScoreEntity>

    @Query("SELECT COUNT(*) FROM scores")
    suspend fun count(): Int

    @Query("SELECT * FROM scores ORDER BY finishedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<ScoreEntity>
}

@Dao
interface StatDao {
    @Query("SELECT value FROM stats WHERE `key` = :key")
    suspend fun get(key: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(stat: StatEntity)

    @Query("SELECT * FROM stats")
    suspend fun all(): List<StatEntity>
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    suspend fun all(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(achievement: AchievementEntity)

    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1")
    suspend fun unlockedCount(): Int
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases")
    suspend fun all(): List<PurchaseEntity>

    @Query("SELECT COUNT(*) FROM purchases WHERE itemId = :itemId")
    suspend fun exists(itemId: String): Int

    /** Returns the new rowId, or -1 when the item was already owned (atomic guard). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(purchase: PurchaseEntity): Long

    @Query("DELETE FROM purchases WHERE itemId = :itemId")
    suspend fun delete(itemId: String)
}

@Dao
interface AdventureDao {
    @Query("SELECT * FROM adventure_progress")
    suspend fun all(): List<AdventureProgressEntity>

    @Query("SELECT * FROM adventure_progress WHERE levelId = :levelId")
    suspend fun get(levelId: Int): AdventureProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(progress: AdventureProgressEntity)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenge_progress")
    suspend fun all(): List<ChallengeEntity>

    @Query("SELECT * FROM challenge_progress WHERE id = :id")
    suspend fun get(id: String): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(challenge: ChallengeEntity)
}

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks")
    suspend fun all(): List<DailyTaskEntity>

    @Query("SELECT * FROM daily_tasks WHERE id = :id")
    suspend fun get(id: String): DailyTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(task: DailyTaskEntity)
}

// ---------------------------------------------------------------------------
// Database
// ---------------------------------------------------------------------------

@Database(
    entities = [
        ScoreEntity::class, StatEntity::class, AchievementEntity::class,
        PurchaseEntity::class, AdventureProgressEntity::class,
        ChallengeEntity::class, DailyTaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun statDao(): StatDao
    abstract fun achievementDao(): AchievementDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun adventureDao(): AdventureDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun dailyTaskDao(): DailyTaskDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tng_database"
                ).build().also { instance = it }
            }
    }
}
