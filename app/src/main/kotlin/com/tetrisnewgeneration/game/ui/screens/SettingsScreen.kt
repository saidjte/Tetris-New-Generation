package com.tetrisnewgeneration.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.ConfirmDialog
import com.tetrisnewgeneration.game.ui.components.SectionTitle
import com.tetrisnewgeneration.game.ui.components.TngCard

/** Player settings: language, audio, haptics, motion, reset progress, about. */
@Composable
fun SettingsScreen(
    settings: PlayerSettings,
    theme: ThemeDef,
    onChange: (PlayerSettings.() -> PlayerSettings) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit
) {
    var confirmReset by remember { mutableStateOf(false) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { com.tetrisnewgeneration.game.audio.UiSounds.back() }
    }
    // v1.4.0: interface sounds — modal chime exactly when the dialog opens.
    androidx.compose.runtime.LaunchedEffect(confirmReset) {
        if (confirmReset) com.tetrisnewgeneration.game.audio.UiSounds.modalShow()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundTop)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            stringResource(R.string.settings_title),
            color = theme.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        // Language
        SectionTitle(stringResource(R.string.settings_language), theme)
        TngCard(theme, modifier = Modifier.fillMaxWidth()) {
            Row {
                LanguageChip("system", stringResource(R.string.lang_system),
                    settings.language == "system", theme) { onChange { copy(language = "system") } }
                Spacer(Modifier.weight(1f))
                LanguageChip("ar", stringResource(R.string.lang_arabic),
                    settings.language == "ar", theme) { onChange { copy(language = "ar") } }
                Spacer(Modifier.weight(1f))
                LanguageChip("en", stringResource(R.string.lang_english),
                    settings.language == "en", theme) { onChange { copy(language = "en") } }
            }
        }

        // Audio
        SectionTitle(stringResource(R.string.settings_music), theme)
        TngCard(theme, modifier = Modifier.fillMaxWidth()) {
            ToggleRow(
                stringResource(R.string.settings_music),
                settings.musicEnabled, theme
            ) { onChange { copy(musicEnabled = it) } }
            Slider(
                value = settings.musicVolume,
                onValueChange = { onChange { copy(musicVolume = it) } },
                enabled = settings.musicEnabled,
                modifier = Modifier.fillMaxWidth()
            )
            ToggleRow(
                stringResource(R.string.settings_sfx),
                settings.sfxEnabled, theme
            ) { onChange { copy(sfxEnabled = it) } }
            Slider(
                value = settings.sfxVolume,
                onValueChange = { onChange { copy(sfxVolume = it) } },
                enabled = settings.sfxEnabled,
                modifier = Modifier.fillMaxWidth()
            )
            // v1.4.0: real announcer voice pack settings.
            ToggleRow(
                stringResource(R.string.settings_voices),
                settings.voicesEnabled, theme
            ) { onChange { copy(voicesEnabled = it) } }
            Slider(
                value = settings.voicesVolume,
                onValueChange = { onChange { copy(voicesVolume = it) } },
                enabled = settings.voicesEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Experience
        SectionTitle(stringResource(R.string.settings_reduced_motion), theme)
        TngCard(theme, modifier = Modifier.fillMaxWidth()) {
            ToggleRow(
                stringResource(R.string.settings_haptics),
                settings.hapticsEnabled, theme
            ) { onChange { copy(hapticsEnabled = it) } }
            ToggleRow(
                stringResource(R.string.settings_gestures),
                settings.gestureControls, theme
            ) { onChange { copy(gestureControls = it) } }
            ToggleRow(
                stringResource(R.string.settings_reduced_motion),
                settings.reducedMotion, theme
            ) { onChange { copy(reducedMotion = it) } }
        }

        // v1.5.0 (V8/V1): effects intensity + image backgrounds
        SectionTitle(stringResource(R.string.settings_fx_intensity), theme)
        TngCard(theme, modifier = Modifier.fillMaxWidth()) {
            Row {
                FxChip("low", stringResource(R.string.fx_low),
                    settings.effectsIntensity == "low", theme) {
                    onChange { copy(effectsIntensity = "low") }
                }
                Spacer(Modifier.weight(1f))
                FxChip("medium", stringResource(R.string.fx_medium),
                    settings.effectsIntensity == "medium", theme) {
                    onChange { copy(effectsIntensity = "medium") }
                }
                Spacer(Modifier.weight(1f))
                FxChip("high", stringResource(R.string.fx_high),
                    settings.effectsIntensity == "high", theme) {
                    onChange { copy(effectsIntensity = "high") }
                }
            }
            Spacer(Modifier.height(4.dp))
            ToggleRow(
                stringResource(R.string.settings_image_backgrounds),
                settings.imageBackgrounds, theme
            ) { onChange { copy(imageBackgrounds = it) } }
        }

        // Reset
        SectionTitle(stringResource(R.string.settings_reset), theme)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                .background(androidx.compose.ui.graphics.Color(0x33F87171))
                .clickable { confirmReset = true }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.settings_reset),
                color = androidx.compose.ui.graphics.Color(0xFFF87171),
                fontWeight = FontWeight.Bold
            )
        }

        // About
        SectionTitle(stringResource(R.string.settings_about), theme)
        TngCard(theme, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.about_text),
                color = theme.textPrimary.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                .background(theme.accent.copy(alpha = 0.16f))
                .clickable { onBack() }
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.close), color = theme.textPrimary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(20.dp))
    }

        if (confirmReset) {
        ConfirmDialog(
            title = stringResource(R.string.settings_reset),
            message = stringResource(R.string.settings_reset_confirm),
            confirmLabel = stringResource(R.string.confirm),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                confirmReset = false
                onResetProgress()
            },
            onDismiss = { confirmReset = false }
        )
    }
}

@Composable
private fun LanguageChip(
    value: String,
    label: String,
    selected: Boolean,
    theme: ThemeDef,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(
                if (selected) theme.accent else theme.boardBackground
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            label,
            color = if (selected) androidx.compose.ui.graphics.Color(0xFF0B0E1A)
                    else theme.textPrimary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

/** v1.5.0 (V8): effects-intensity selector chip (same look as language chips). */
@Composable
private fun FxChip(
    value: String,
    label: String,
    selected: Boolean,
    theme: ThemeDef,
    onClick: () -> Unit
) {
    LanguageChip(value, label, selected, theme, onClick)
}

@Composable
private fun ToggleRow(label: String, value: Boolean, theme: ThemeDef, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = theme.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = value,
            onCheckedChange = {
                com.tetrisnewgeneration.game.audio.UiSounds.toggle(it)
                onChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = theme.accent,
                checkedThumbColor = androidx.compose.ui.graphics.Color(0xFF0B0E1A)
            )
        )
    }
}
