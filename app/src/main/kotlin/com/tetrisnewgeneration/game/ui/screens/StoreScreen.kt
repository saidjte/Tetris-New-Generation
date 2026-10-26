package com.tetrisnewgeneration.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.data.settings.PlayerSettings
import com.tetrisnewgeneration.game.engine.PieceType
import com.tetrisnewgeneration.game.store.Category
import com.tetrisnewgeneration.game.store.StoreItem
import com.tetrisnewgeneration.game.themes.PackDefs
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.themes.ThemeDefs
import com.tetrisnewgeneration.game.ui.components.ConfirmDialog
import com.tetrisnewgeneration.game.ui.components.CoinBadge
import com.tetrisnewgeneration.game.ui.components.localizedText
import com.tetrisnewgeneration.game.ui.vm.StoreViewModel

/**
 * Local store: themes, block packs, effect packs and sound packs bought with
 * in-game coins. Includes previews, confirm dialogs and equip/selected states.
 */
@Composable
fun StoreScreen(
    settings: PlayerSettings,
    theme: ThemeDef,
    vm: StoreViewModel,
    onBack: () -> Unit
) {
    val items by vm.items.collectAsState()
    val owned by vm.owned.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var pendingBuy by remember { mutableStateOf<StoreItem?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { vm.load() }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { com.tetrisnewgeneration.game.audio.UiSounds.back() }
    }

    val categories = listOf(Category.THEME, Category.BLOCK_PACK, Category.EFFECT_PACK, Category.SOUND_PACK)
    val filtered = items.filter { it.category == categories[tab] }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundTop)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.store_title),
                color = theme.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            CoinBadge(theme, settings.coins)
        }

        TabRow(
            selectedTabIndex = tab,
            containerColor = Color.Transparent,
            contentColor = theme.accent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            val labels = listOf(
                stringResource(R.string.store_tab_themes),
                stringResource(R.string.store_tab_blocks),
                stringResource(R.string.store_tab_effects),
                stringResource(R.string.store_tab_sounds)
            )
            labels.forEachIndexed { i, label ->
                Tab(selected = tab == i, onClick = {
                    com.tetrisnewgeneration.game.audio.UiSounds.tab()
                    tab = i
                },
                    text = { Text(label, fontSize = 12.sp, color = theme.textPrimary) })
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                StoreCard(
                    item = item,
                    theme = theme,
                    owned = item.id in owned,
                    selected = vm.isSelected(item.id, settings),
                    onBuy = { pendingBuy = item },
                    onSelect = {
                        vm.select(item)
                    },
                    // v1.6.0 (A7): sound packs get an audible try-before-buy.
                    onPreview = if (item.category == Category.SOUND_PACK) {
                        { vm.previewSoundPack(item.id) }
                    } else null
                )
            }
        }
    }

    pendingBuy?.let { item ->
        ConfirmDialog(
            title = stringResource(R.string.store_confirm_buy),
            message = stringResource(
                R.string.store_confirm_buy_msg,
                displayName(item),
                item.price
            ),
            confirmLabel = stringResource(R.string.confirm),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                pendingBuy = null
                vm.purchase(item) { result ->
                    toast = when (result) {
                        com.tetrisnewgeneration.game.data.repo.PurchaseResult.SUCCESS -> "purchased"
                        com.tetrisnewgeneration.game.data.repo.PurchaseResult.ALREADY_OWNED -> "already"
                        com.tetrisnewgeneration.game.data.repo.PurchaseResult.INSUFFICIENT_FUNDS -> "insufficient"
                    }
                }
            },
            onDismiss = { pendingBuy = null }
        )
    }

    toast?.let { key ->
        val msg = when (key) {
            "purchased" -> stringResource(R.string.store_purchased)
            "already" -> stringResource(R.string.store_already_owned)
            else -> stringResource(R.string.store_not_enough)
        }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { toast = null },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { toast = null }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(msg) }
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .padding(bottom = 18.dp)
                .clip(RoundedCornerShape(50))
                .background(theme.accent.copy(alpha = 0.9f))
                .clickable { onBack() }
                .padding(horizontal = 26.dp, vertical = 12.dp)
        ) {
            Text(stringResource(R.string.close), color = Color(0xFF0B0E1A), fontWeight = FontWeight.Bold)
        }
    }
}

private fun displayName(item: StoreItem): String = when (item.category) {
    Category.THEME -> ThemeDefs.all.firstOrNull { it.id == item.id }?.let {
        localizedTextStatic(it.nameAr, it.nameEn)
    } ?: item.id
    Category.BLOCK_PACK -> PackDefs.blockPacks.firstOrNull { it.id == item.id }?.let {
        localizedTextStatic(it.nameAr, it.nameEn)
    } ?: item.id
    Category.EFFECT_PACK -> PackDefs.effectPacks.firstOrNull { it.id == item.id }?.let {
        localizedTextStatic(it.nameAr, it.nameEn)
    } ?: item.id
    Category.SOUND_PACK -> localizedTextStatic(soundPackNameAr(item.id), soundPackNameEn(item.id))
}

private fun localizedTextStatic(ar: String, en: String): String = try {
    val loc = java.util.Locale.getDefault()
    if (loc.language == "ar") ar else en
} catch (_: Exception) { en }

private fun soundPackNameAr(id: String) = when (id) {
    "sfx_original" -> "أصوات أصلية"
    "sfx_arcade" -> "أصوات أركيد"
    "sfx_future" -> "أصوات مستقبلية"
    "sfx_calm" -> "أصوات هادئة"
    "sfx_t99" -> "أصوات T99"
    else -> id
}

private fun soundPackNameEn(id: String) = when (id) {
    "sfx_original" -> "Original Sounds"
    "sfx_arcade" -> "Arcade Sounds"
    "sfx_future" -> "Future Sounds"
    "sfx_calm" -> "Calm Sounds"
    "sfx_t99" -> "T99 Sounds"
    else -> id
}

@Composable
private fun StoreCard(
    item: StoreItem,
    theme: ThemeDef,
    owned: Boolean,
    selected: Boolean,
    onBuy: () -> Unit,
    onSelect: () -> Unit,
    onPreview: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .clip(shape)
            .background(theme.surface.copy(alpha = 0.92f))
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) theme.accent else theme.boardBorder.copy(alpha = 0.5f),
                shape
            )
            .padding(12.dp)
    ) {
        // Preview swatch
        StorePreview(item, theme, modifier = Modifier.fillMaxWidth().height(64.dp))

        // v1.6.0 (A7): audible preview for sound-pack cards.
        if (onPreview != null) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.accent.copy(alpha = 0.10f))
                    .clickable { onPreview() }
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.store_preview_sound),
                    color = theme.accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            displayName(item),
            color = theme.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1
        )

        Spacer(Modifier.height(8.dp))

        val isCurrentlyUsed = selected
        val label = when {
            isCurrentlyUsed -> stringResource(R.string.store_selected)
            owned && item.free -> stringResource(R.string.store_free)
            owned -> stringResource(R.string.store_owned)
            item.free -> stringResource(R.string.store_free)
            else -> stringResource(R.string.store_buy) + " ◉${item.price}"
        }
        val enabled = !isCurrentlyUsed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when {
                        isCurrentlyUsed -> theme.accent.copy(alpha = 0.25f)
                        owned || item.free -> theme.accent.copy(alpha = 0.14f)
                        else -> theme.accent.copy(alpha = 0.2f)
                    }
                )
                .clickable(enabled = enabled) {
                    if (owned || item.free) onSelect() else onBuy()
                }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (isCurrentlyUsed) theme.textPrimary else theme.accent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

/** Visual preview per category. */
@Composable
private fun StorePreview(item: StoreItem, theme: ThemeDef, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(theme.boardBackground)
            .border(1.dp, theme.gridLine, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (item.category) {
            Category.THEME -> {
                val t = ThemeDefs.all.firstOrNull { it.id == item.id } ?: ThemeDefs.all.first()
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(PieceType.I, PieceType.T, PieceType.O, PieceType.Z).forEach {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(t.colorOf(it))
                        )
                    }
                }
            }
            Category.BLOCK_PACK -> {
                val pack = PackDefs.blockPack(item.id)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(PieceType.J, PieceType.S, PieceType.L).forEach { pt ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(if (pack.style == com.tetrisnewgeneration.game.themes.BlockStyle.PIXEL) 0.dp else 5.dp))
                                .background(theme.colorOf(pt))
                                .border(
                                    1.dp,
                                    if (pack.glow) theme.textPrimary.copy(alpha = 0.6f) else Color.Transparent,
                                    RoundedCornerShape(5.dp)
                                )
                        )
                    }
                }
            }
            Category.EFFECT_PACK -> {
                val color = when (item.id) {
                    "fx_burst" -> Color(0xFFF87171)
                    "fx_trail" -> Color(0xFF60A5FA)
                    "fx_shatter" -> Color(0xFFA855F7)
                    "fx_confetti" -> Color(0xFF4ADE80)
                    else -> theme.accent
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .size((6 + i * 3).dp)
                                .background(color.copy(alpha = 0.35f + i * 0.15f), CircleShape)
                        )
                    }
                }
            }
            Category.SOUND_PACK -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf(8, 16, 10, 20, 12).forEach { h ->
                        Box(
                            modifier = Modifier
                                .size(6.dp, h.dp)
                                .background(theme.accent.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}
