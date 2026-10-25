package com.tetrisnewgeneration.game.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tetrisnewgeneration.game.TngApp
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.ui.screens.GameScreen
import com.tetrisnewgeneration.game.ui.screens.HomeScreen
import com.tetrisnewgeneration.game.ui.screens.ModeSelectScreen
import com.tetrisnewgeneration.game.ui.screens.SettingsScreen
import com.tetrisnewgeneration.game.ui.screens.StatsScreen
import com.tetrisnewgeneration.game.ui.screens.StoreScreen
import com.tetrisnewgeneration.game.ui.screens.TasksScreen
import com.tetrisnewgeneration.game.ui.vm.StoreViewModel
import kotlinx.coroutines.launch

/** In-memory handoff for the saved-session JSON when resuming a round. */
object SessionHolder {
    var json: String? = null
}

object Routes {
    const val HOME = "home"
    const val MODES = "modes"
    const val GAME = "game/{modeKey}"
    const val STORE = "store"
    const val TASKS = "tasks"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val HOWTO = "howto"

    fun game(modeKey: String) = "game/$modeKey"
}

@Composable
fun AppNav(settings: PlayerSettings) {
    val nav = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as TngApp
    val container = app.container
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var hasSavedSession by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasSavedSession = container.settings.loadSessionJson() != null
    }

    NavHost(navController = nav, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            val storeVm: StoreViewModel = viewModel()
            // First-launch tutorial: shown exactly once, then never again.
            LaunchedEffect(Unit) {
                if (!container.settings.current().tutorialShown) {
                    container.settings.setTutorialShown(true)
                    nav.navigate(Routes.HOWTO)
                }
            }
            HomeScreen(
                settings = settings,
                theme = com.tetrisnewgeneration.game.themes.ThemeDefs.byId(settings.themeId),
                hasSavedSession = hasSavedSession,
                onPlay = { nav.navigate(Routes.game("classic")) },
                onContinue = {
                    scope.launch {
                        SessionHolder.json = container.settings.loadSessionJson()
                        val save = SessionHolder.json
                        val modeId = save?.let {
                            try {
                                val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                                json.decodeFromString(
                                    com.tetrisnewgeneration.game.engine.GameSave.serializer(), it
                                ).modeId
                            } catch (_: Exception) { null }
                        } ?: "classic"
                        hasSavedSession = false
                        nav.navigate(Routes.game(modeId))
                    }
                },
                onModes = { nav.navigate(Routes.MODES) },
                onHowTo = { nav.navigate(Routes.HOWTO) },
                onStore = { nav.navigate(Routes.STORE) },
                onTasks = { nav.navigate(Routes.TASKS) },
                onStats = { nav.navigate(Routes.STATS) },
                onSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.MODES) {
            ModeSelectScreen(
                theme = com.tetrisnewgeneration.game.themes.ThemeDefs.byId(settings.themeId),
                bestScores = { modeId -> container.scoreRepository.bestForMode(modeId) },
                adventureStates = {
                    container.progressRepository.adventureStates()
                        .mapValues { it.value.completed }
                },
                challengeStates = {
                    container.progressRepository.challengeStates()
                        .mapValues { it.value.completed }
                },
                puzzleStates = {
                    // Per-puzzle completion flags are stored as stat counters.
                    val ids = com.tetrisnewgeneration.game.modes.Puzzles.all.map { it.id }
                    buildSet {
                        for (id in ids) {
                            if (container.scoreRepository.stat("puzzle_${id}_done") > 0) add(id)
                        }
                    }
                },
                onBack = { nav.popBackStack() },
                onLaunchSimple = { nav.navigate(Routes.game(it)) },
                onLaunchTimeAttack = { nav.navigate(Routes.game("ta_$it")) },
                onLaunchSpeed = { nav.navigate(Routes.game("spd_$it")) },
                onLaunchChallenge = { nav.navigate(Routes.game(it)) },
                onLaunchPuzzle = { nav.navigate(Routes.game("puzzle_$it")) },
                onLaunchAdventure = { nav.navigate(Routes.game("adv_$it")) }
            )
        }

        composable(Routes.GAME) { entry ->
            val modeKey = entry.arguments?.getString("modeKey") ?: "classic"
            GameScreen(
                modeKey = modeKey,
                resumeSaveJson = SessionHolder.json,
                theme = com.tetrisnewgeneration.game.themes.ThemeDefs.byId(settings.themeId),
                reducedMotion = settings.reducedMotion,
                hapticsEnabled = settings.hapticsEnabled,
                gestureControls = settings.gestureControls,
                effectPackId = settings.effectPackId,
                onHome = {
                    SessionHolder.json = null
                    nav.popBackStack(Routes.HOME, inclusive = false)
                    hasSavedSession = container.settings.let { false }
                }
            )
        }

        composable(Routes.STORE) {
            val storeVm: StoreViewModel = viewModel()
            StoreScreen(
                settings = settings,
                theme = com.tetrisnewgeneration.game.themes.ThemeDefs.byId(settings.themeId),
                vm = storeVm,
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.TASKS) {
            TasksScreen(
                theme = com.tetrisnewgeneration.game.themes.ThemeDefs.byId(settings.themeId),
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                theme = com.tetrisnewgeneration.game.themes.ThemeDefs.byId(settings.themeId),
                loadStats = { container.scoreRepository.allStats() },
                loadModeBests = {
                    val ids = listOf(
                        com.tetrisnewgeneration.game.modes.Modes.CLASSIC,
                        com.tetrisnewgeneration.game.modes.Modes.MARATHON,
                        com.tetrisnewgeneration.game.modes.Modes.QUICK_PLAY,
                        com.tetrisnewgeneration.game.modes.Modes.TIME_ATTACK,
                        com.tetrisnewgeneration.game.modes.Modes.SURVIVAL,
                        com.tetrisnewgeneration.game.modes.Modes.SPEED,
                        com.tetrisnewgeneration.game.modes.Modes.ZEN,
                        com.tetrisnewgeneration.game.modes.Modes.PRACTICE
                    )
                    ids.map { it to container.scoreRepository.bestForMode(it) }
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                theme = com.tetrisnewgeneration.game.themes.ThemeDefs.byId(settings.themeId),
                onChange = { transform ->
                    scope.launch {
                        val current = container.settings.current()
                        val updated = transform(current)
                        container.settings.updateAll(updated)
                    }
                },
                onResetProgress = {
                    scope.launch {
                        container.progressRepository.resetAll()
                        container.storeRepository.grantWelcomeItems()
                        container.settings.setWallet(coins = 150, xp = 0, playerLevel = 1)
                        container.settings.clearSession()
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.HOWTO) {
            com.tetrisnewgeneration.game.ui.screens.HowToPlayScreen(
                theme = com.tetrisnewgeneration.game.themes.ThemeDefs.byId(settings.themeId),
                onBack = { nav.popBackStack() }
            )
        }
    }
}
