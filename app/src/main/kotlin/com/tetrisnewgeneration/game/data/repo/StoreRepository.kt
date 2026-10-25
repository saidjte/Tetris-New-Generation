package com.tetrisnewgeneration.game.data.repo

import com.tetrisnewgeneration.game.data.db.PurchaseDao
import com.tetrisnewgeneration.game.data.db.PurchaseEntity
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.data.settings.WalletStore
import com.tetrisnewgeneration.game.store.StoreCatalog
import com.tetrisnewgeneration.game.store.StoreItem

/**
 * Local store logic: catalog loading, ownership, purchases with the internal
 * wallet. Purchases are atomic — ownership is claimed with INSERT OR IGNORE on
 * the primary key, so an owned item can never be charged twice, even under
 * concurrent double-taps.
 *
 * The catalog is injected as a loader so the class stays unit-testable without
 * an Android Context.
 */
class StoreRepository(
    private val purchaseDao: PurchaseDao,
    private val settings: WalletStore,
    private val catalogLoader: suspend () -> String? = { null }
) {

    suspend fun catalog(): List<StoreItem> {
        val raw = try {
            catalogLoader()
        } catch (_: Exception) {
            null
        } ?: return StoreCatalog.DEFAULT_ITEMS
        return StoreCatalog.fromJson(raw) ?: StoreCatalog.DEFAULT_ITEMS
    }

    suspend fun ownedIds(): Set<String> = purchaseDao.all().map { it.itemId }.toSet()

    suspend fun isOwned(itemId: String): Boolean = purchaseDao.exists(itemId) > 0

    /** Grants ownership without charge (free items / rewards). Safe to repeat. */
    suspend fun grant(itemId: String) {
        purchaseDao.insert(PurchaseEntity(itemId, System.currentTimeMillis()))
    }

    /**
     * Attempts to buy [item] using the wallet snapshot.
     * @return exact outcome; the balance is debited at most once per success.
     */
    suspend fun purchase(item: StoreItem, settingsSnapshot: PlayerSettings): PurchaseResult {
        if (item.free) {
            grant(item.id)
            return PurchaseResult.SUCCESS
        }
        if (settingsSnapshot.coins < item.price) return PurchaseResult.INSUFFICIENT_FUNDS

        // Atomic ownership claim: -1 means somebody already owned it.
        val rowId = purchaseDao.insert(PurchaseEntity(item.id, System.currentTimeMillis()))
        if (rowId == -1L) return PurchaseResult.ALREADY_OWNED

        settings.setWallet(
            coins = settingsSnapshot.coins - item.price,
            xp = settingsSnapshot.xp,
            playerLevel = settingsSnapshot.playerLevel
        )
        return PurchaseResult.SUCCESS
    }

    suspend fun grantWelcomeItems() {
        StoreCatalog.DEFAULT_ITEMS.filter { it.free }.forEach { grant(it.id) }
    }
}

enum class PurchaseResult { SUCCESS, ALREADY_OWNED, INSUFFICIENT_FUNDS }
