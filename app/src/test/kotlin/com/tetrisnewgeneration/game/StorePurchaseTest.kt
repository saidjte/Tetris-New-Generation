package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.data.db.PurchaseDao
import com.tetrisnewgeneration.game.data.db.PurchaseEntity
import com.tetrisnewgeneration.game.data.repo.PurchaseResult
import com.tetrisnewgeneration.game.data.repo.StoreRepository
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.data.settings.WalletStore
import com.tetrisnewgeneration.game.store.Category
import com.tetrisnewgeneration.game.store.StoreItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Store purchase logic against pure-JVM fakes: successful charge happens
 * exactly once, duplicate purchases are impossible, insufficient funds are
 * rejected, and free items are granted without charge.
 */
class StorePurchaseTest {

    private class FakePurchaseDao : PurchaseDao {
        val rows = LinkedHashMap<String, PurchaseEntity>()
        override suspend fun all(): List<PurchaseEntity> = rows.values.toList()
        override suspend fun exists(itemId: String): Int = if (rows.containsKey(itemId)) 1 else 0
        override suspend fun insert(purchase: PurchaseEntity): Long =
            if (rows.putIfAbsent(purchase.itemId, purchase) == null) 1L else -1L
        override suspend fun delete(itemId: String) {
            rows.remove(itemId)
        }
    }

    private class FakeWallet(startCoins: Long) : WalletStore {
        var state = PlayerSettings(coins = startCoins)
        var writes = 0
        override suspend fun current(): PlayerSettings = state
        override suspend fun setWallet(coins: Long, xp: Long, playerLevel: Int) {
            writes++
            state = state.copy(coins = coins, xp = xp, playerLevel = playerLevel)
        }
    }

    private fun repo(dao: FakePurchaseDao, wallet: FakeWallet) =
        StoreRepository(dao, wallet, catalogLoader = { null })

    private val theme = StoreItem("neon", Category.THEME, price = 120)

    @Test
    fun `successful purchase debits exactly once`() = runBlocking {
        val dao = FakePurchaseDao()
        val wallet = FakeWallet(200)
        val store = repo(dao, wallet)
        val result = store.purchase(theme, wallet.state)
        assertEquals(PurchaseResult.SUCCESS, result)
        assertEquals(80L, wallet.state.coins)
        assertEquals(1, wallet.writes)
        assertTrue(store.isOwned("neon"))
    }

    @Test
    fun `duplicate purchase never debits twice`() = runBlocking {
        val dao = FakePurchaseDao()
        val wallet = FakeWallet(1000)
        val store = repo(dao, wallet)
        assertEquals(PurchaseResult.SUCCESS, store.purchase(theme, wallet.state))
        assertEquals(PurchaseResult.ALREADY_OWNED, store.purchase(theme, wallet.state))
        assertEquals(880L, wallet.state.coins) // 1000 - 120 exactly once
        assertEquals(1, wallet.writes)
    }

    @Test
    fun `insufficient funds rejected without debit`() = runBlocking {
        val dao = FakePurchaseDao()
        val wallet = FakeWallet(50)
        val store = repo(dao, wallet)
        val result = store.purchase(theme, wallet.state)
        assertEquals(PurchaseResult.INSUFFICIENT_FUNDS, result)
        assertEquals(50L, wallet.state.coins)
        assertEquals(0, wallet.writes)
        assertEquals(false, store.isOwned("neon"))
    }

    @Test
    fun `free items are granted without charge`() = runBlocking {
        val dao = FakePurchaseDao()
        val wallet = FakeWallet(0)
        val store = repo(dao, wallet)
        val free = StoreItem("minimal", Category.THEME, price = 0, free = true)
        assertEquals(PurchaseResult.SUCCESS, store.purchase(free, wallet.state))
        assertEquals(0L, wallet.state.coins)
        assertTrue(store.isOwned("minimal"))
        // granting again is harmless
        assertEquals(PurchaseResult.SUCCESS, store.purchase(free, wallet.state))
        assertEquals(0L, wallet.state.coins)
    }

    @Test
    fun `catalog fallback contains all themes and packs`() = runBlocking {
        val store = repo(FakePurchaseDao(), FakeWallet(0))
        val items = store.catalog()
        assertEquals(13, items.count { it.category == Category.THEME })
        assertEquals(6, items.count { it.category == Category.BLOCK_PACK })
        assertEquals(5, items.count { it.category == Category.EFFECT_PACK })
        // v1.7.0: sounds left the store — T99 recordings are the free base
        // sound of the game, so no sound merchandise may exist in any catalog.
        assertEquals(24, items.size)
        assertTrue(items.none { it.id.startsWith("sfx_") })
    }
}
