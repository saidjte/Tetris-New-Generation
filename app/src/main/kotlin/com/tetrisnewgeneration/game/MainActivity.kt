package com.tetrisnewgeneration.game

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.data.settings.SettingsStore
import com.tetrisnewgeneration.game.themes.ThemeDefs
import com.tetrisnewgeneration.game.ui.nav.AppNav
import com.tetrisnewgeneration.game.ui.theme.TngTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Single-activity Compose app. AppCompatActivity backs per-app language
 * switching (Arabic / English / system) with automatic RTL handling.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as TngApp).container

        lifecycleScope.launch {
            container.settings.settings
                .map { it.language }
                .distinctUntilChanged()
                .collect { lang ->
                    val desired = when (lang) {
                        "ar" -> LocaleListCompat.forLanguageTags("ar")
                        "en" -> LocaleListCompat.forLanguageTags("en")
                        else -> LocaleListCompat.getEmptyLocaleList()
                    }
                    if (AppCompatDelegate.getApplicationLocales().toLanguageTags() !=
                        desired.toLanguageTags()
                    ) {
                        AppCompatDelegate.setApplicationLocales(desired)
                    }
                }
        }

        setContent {
            val settings by container.settings.settings
                .collectAsState(initial = PlayerSettings())
            TngTheme(theme = ThemeDefs.byId(settings.themeId), settings = settings) {
                AppNav(settings = settings)
            }
        }
    }
}
