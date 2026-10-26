package com.tetrisnewgeneration.game.ui.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tetrisnewgeneration.game.TngApp
import com.tetrisnewgeneration.game.data.repo.PurchaseResult
import com.tetrisnewgeneration.game.data.repo.ProgressRepository
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.progression.AchievementDef
import com.tetrisnewgeneration.game.progression.DailyTaskDef
import com.tetrisnewgeneration.game.store.StoreItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Store + progress screens state. Handles purchase flow (with coins), equip
 * actions and daily task claiming. All logic local.
 */
class StoreViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as TngApp).container
    private val store get() = container.storeRepository
    private val settings get() = container.settings
    private val progress get() = container.progressRepository

    private val _items = MutableStateFlow<List<StoreItem>>(emptyList())
    val items: StateFlow<List<StoreItem>> = _items

    private val _owned = MutableStateFlow<Set<String>>(emptySet())
    val owned: StateFlow<Set<String>> = _owned

    private val _settings = MutableStateFlow(PlayerSettings())
    val settingsFlow: StateFlow<PlayerSettings> = _settings

    fun load() {
        viewModelScope.launch {
            _items.value = store.catalog()
            _owned.value = store.ownedIds()
            _settings.value = settings.current()
        }
    }

    fun isSelected(itemId: String, current: PlayerSettings): Boolean = when {
        itemId.startsWith("blocks_") -> current.blockPackId == itemId
        itemId.startsWith("fx_") -> current.effectPackId == itemId
        else -> current.themeId == itemId
    }

    /** Applies an owned item as the active customization. */
    fun select(item: StoreItem) {
        viewModelScope.launch {
            if (item.id !in _owned.value && !item.free) return@launch
            when {
                item.id.startsWith("blocks_") -> settings.setBlockPackId(item.id)
                item.id.startsWith("fx_") -> settings.setEffectPackId(item.id)
                else -> settings.setThemeId(item.id)
            }
            _settings.value = settings.current()
        }
    }

    /** Purchases with confirmation callback (thread-safe, single debit). */
    fun purchase(item: StoreItem, onResult: (PurchaseResult) -> Unit) {
        viewModelScope.launch {
            val snapshot = settings.current()
            val result = store.purchase(item, snapshot)
            if (result == PurchaseResult.SUCCESS) {
                _owned.value = store.ownedIds()
                _settings.value = settings.current()
                container.sfx.play(com.tetrisnewgeneration.game.audio.SfxEngine.Sound.UNLOCK)
                // Theme-ownership achievement.
                val themeCount = _owned.value.count { it != "classic_night" && it != "minimal" } +
                    2 // the two free themes count as owned
                progress.evaluateStat("themes_3", themeCount.toLong())
            }
            onResult(result)
        }
    }

    fun resetDefaultTheme() {
        viewModelScope.launch {
            settings.setThemeId("classic_night")
            _settings.value = settings.current()
        }
    }
}

/** Achievements + daily tasks screen state. */
class TasksViewModel(app: Application) : AndroidViewModel(app) {

    private val container = (app as TngApp).container
    private val progress get() = container.progressRepository

    data class TaskUi(
        val def: DailyTaskDef,
        val progress: Int,
        val claimed: Boolean
    )

    data class Ui(
        val achievements: List<Pair<AchievementDef, Int>> = emptyList(), // (def, progress)
        val tasks: List<TaskUi> = emptyList(),
        val coins: Long = 0
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui

    fun load() {
        viewModelScope.launch {
            val states = progress.achievementStates()
            val ach = AchievementOrder.order.mapNotNull { id ->
                com.tetrisnewgeneration.game.progression.AchievementDefs.byId(id)?.let { def ->
                    def to (states[def.id]?.progress ?: 0)
                }
            }
            val stamp = progress.todayStamp()
            val taskStates = progress.dailyTaskStates()
            val tasks = progress.todaysTasks().map { def ->
                val st = taskStates[def.id]
                TaskUi(
                    def = def,
                    progress = if (st?.dayStamp == stamp) st.progress else 0,
                    claimed = st?.claimed == true && st.dayStamp == stamp
                )
            }
            _ui.value = Ui(ach, tasks, container.settings.current().coins)
        }
    }

    fun claim(task: DailyTaskDef) {
        viewModelScope.launch {
            progress.claimDailyTask(task)
            load()
        }
    }
}

private object AchievementOrder {
    val order = listOf(
        "games_1", "games_25", "lines_100", "lines_1000", "tetris_1", "tetris_10",
        "combo_5", "level_10", "score_50k", "hard_drops_100", "challenges_5",
        "puzzles_4", "adventure_10", "themes_3", "zen_30m", "time_attack_1"
    )
}
