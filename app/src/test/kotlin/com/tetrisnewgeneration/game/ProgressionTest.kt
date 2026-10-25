package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.data.repo.ProgressionRepository
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.data.settings.WalletStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** XP curve, level-ups and round reward math. */
class ProgressionTest {

    private class FakeWallet(start: PlayerSettings = PlayerSettings()) : WalletStore {
        var state = start
        override suspend fun current(): PlayerSettings = state
        override suspend fun setWallet(coins: Long, xp: Long, playerLevel: Int) {
            state = state.copy(coins = coins, xp = xp, playerLevel = playerLevel)
        }
    }

    @Test
    fun `xp curve grows monotonically`() {
        val l1 = ProgressionRepository.xpNeeded(1)
        val l2 = ProgressionRepository.xpNeeded(2)
        val l10 = ProgressionRepository.xpNeeded(10)
        assertTrue(l1 >= 100)
        assertTrue(l2 > l1)
        assertTrue(l10 > l2)
    }

    @Test
    fun `applyGains grants coins and level-ups with bonus`() = runBlocking {
        val wallet = FakeWallet(PlayerSettings(coins = 10, xp = 0, playerLevel = 1))
        val repo = ProgressionRepository(wallet)
        val need = ProgressionRepository.xpNeeded(1)
        // Enough XP to cross level 1 -> level 3 exactly.
        val xpGain = need + ProgressionRepository.xpNeeded(2)
        val update = repo.applyGains(wallet.state, xpGain, coinGain = 40)
        assertEquals(3, update.level)
        assertEquals(2, update.levelsGained)
        assertEquals(0L, update.xp)
        // coins = 10 + 40 + 2 levels * 50 bonus
        assertEquals(150L, update.coins)
    }

    @Test
    fun `round rewards scale with score and lines`() {
        val repo = ProgressionRepository(FakeWallet())
        val xp = repo.roundXp(score = 1000, lines = 10, won = false)
        assertEquals(10L + 50L, xp)
        val coins = repo.roundCoins(lines = 10, tetrises = 1, won = false)
        assertEquals(5L + 10L, coins)
        val wonXp = repo.roundXp(score = 1000, lines = 10, won = true)
        assertEquals(xp + 50L, wonXp)
    }

    @Test
    fun `small gains keep level stable`() = runBlocking {
        val wallet = FakeWallet(PlayerSettings(coins = 0, xp = 0, playerLevel = 5))
        val repo = ProgressionRepository(wallet)
        val update = repo.applyGains(wallet.state, xpGain = 10, coinGain = 0)
        assertEquals(5, update.level)
        assertEquals(0, update.levelsGained)
        assertEquals(10L, update.xp)
    }
}
