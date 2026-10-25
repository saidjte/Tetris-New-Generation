package com.tetrisnewgeneration.game.data.repo

import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.data.settings.WalletStore

/**
 * Player XP/level progression and coin rewards. XP curve:
 * xpNeeded(level) = 100 * level^1.4 (rounded). All local math, no clock tricks
 * needed — coins/XP are granted strictly by game results.
 */
class ProgressionRepository(private val settings: WalletStore) {

    fun xpNeededForLevel(level: Int): Long = xpNeeded(level)

    /** XP granted for a finished round. */
    fun roundXp(score: Long, lines: Int, won: Boolean): Long =
        score / 100 + lines * 5L + if (won) 50L else 0L

    /** Coins granted for a finished round. */
    fun roundCoins(lines: Int, tetrises: Int, won: Boolean): Long =
        lines / 2L + tetrises * 10L + if (won) 25L else 0L

    /**
     * Applies gains, possibly crossing several level thresholds.
     * @return the new state plus how many levels were gained.
     */
    suspend fun applyGains(
        snapshot: PlayerSettings,
        xpGain: Long,
        coinGain: Long
    ): ProgressUpdate {
        var xp = snapshot.xp + xpGain
        var level = snapshot.playerLevel
        var levelsGained = 0
        while (xp >= xpNeededForLevel(level)) {
            xp -= xpNeededForLevel(level)
            level++
            levelsGained++
        }
        val coins = snapshot.coins + coinGain + levelsGained * LEVEL_BONUS_COINS
        settings.setWallet(coins = coins, xp = xp, playerLevel = level)
        return ProgressUpdate(coins, xp, level, levelsGained)
    }

    data class ProgressUpdate(
        val coins: Long,
        val xp: Long,
        val level: Int,
        val levelsGained: Int
    )

    companion object {
        const val LEVEL_BONUS_COINS = 50L

        /** Pure XP curve usable without an instance (UI helpers). */
        fun xpNeeded(level: Int): Long =
            (100.0 * Math.pow(level.toDouble(), 1.4)).toLong().coerceAtLeast(100L)
    }
}
