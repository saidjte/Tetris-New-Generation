package com.tetrisnewgeneration.game.data.repo

import com.tetrisnewgeneration.game.data.db.ScoreDao
import com.tetrisnewgeneration.game.data.db.ScoreEntity
import com.tetrisnewgeneration.game.data.db.StatDao
import com.tetrisnewgeneration.game.data.db.StatEntity

/**
 * Persistent round results and aggregate statistics. All local (Room).
 */
class ScoreRepository(
    private val scoreDao: ScoreDao,
    private val statDao: StatDao
) {

    suspend fun saveRound(
        modeId: String,
        score: Long,
        lines: Int,
        level: Int,
        durationMs: Long,
        won: Boolean
    ) {
        scoreDao.insert(
            ScoreEntity(
                modeId = modeId,
                score = score,
                lines = lines,
                level = level,
                durationMs = durationMs,
                won = won,
                finishedAt = System.currentTimeMillis()
            )
        )
        bump(StatKeys.GAMES_PLAYED, 1)
        bump(StatKeys.TOTAL_LINES, lines.toLong())
        bump(StatKeys.TOTAL_SCORE, score)
        bump(StatKeys.TOTAL_TIME_MS, durationMs)
    }

    suspend fun bestForMode(modeId: String): Long =
        scoreDao.bestForMode(modeId)?.score ?: 0L

    suspend fun recentBest(modeId: String, limit: Int = 10): List<ScoreEntity> =
        scoreDao.recentBest(modeId, limit)

    suspend fun totalRounds(): Long = statDao.get(StatKeys.GAMES_PLAYED) ?: 0L

    suspend fun stat(key: String): Long = statDao.get(key) ?: 0L

    suspend fun bump(key: String, amount: Long) {
        val current = statDao.get(key) ?: 0L
        statDao.put(StatEntity(key, current + amount))
    }

    suspend fun setMax(key: String, value: Long) {
        val current = statDao.get(key) ?: 0L
        if (value > current) statDao.put(StatEntity(key, value))
    }

    suspend fun allStats(): Map<String, Long> =
        statDao.all().associate { it.key to it.value }
}

object StatKeys {
    const val GAMES_PLAYED = "games_played"
    const val TOTAL_LINES = "total_lines"
    const val TOTAL_SCORE = "total_score"
    const val TOTAL_TIME_MS = "total_time_ms"
    const val TETRISES = "tetrises"
    const val MAX_COMBO = "max_combo"
    const val MAX_LEVEL = "max_level"
    const val HARD_DROPS = "hard_drops"
    const val ZEN_TIME_MS = "zen_time_ms"
    const val CHALLENGES_DONE = "challenges_done"
    const val PUZZLES_DONE = "puzzles_done"
    const val ADVENTURE_DONE = "adventure_done"
    const val THEMES_BOUGHT = "themes_bought"
}
