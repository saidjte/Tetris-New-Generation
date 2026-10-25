package com.tetrisnewgeneration.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.themes.ThemeDef

/**
 * Reusable UI pieces shared across screens.
 */

@Composable
fun localizedText(ar: String, en: String): String {
    val isArabic = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language == "ar"
    return if (isArabic) ar else en
}

@Composable
fun TngCard(
    theme: ThemeDef,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(theme.surface.copy(alpha = 0.92f))
            .border(1.dp, theme.boardBorder.copy(alpha = 0.5f), shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    theme: ThemeDef,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = theme.accent,
            contentColor = Color(0xFF0B0E1A)
        )
    ) {
        Text(text, fontSize = MaterialTheme.typography.titleMedium.fontSize,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MenuButtonRow(
    label: String,
    theme: ThemeDef,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(theme.surface.copy(alpha = 0.9f))
            .border(1.dp, theme.boardBorder.copy(alpha = 0.45f), shape)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = theme.textPrimary,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            fontWeight = FontWeight.Medium
        )
        if (trailing != null) trailing()
    }
}

@Composable
fun CoinBadge(theme: ThemeDef, coins: Long, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(theme.accent.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "◉",
            color = theme.accent,
            fontWeight = FontWeight.Bold
        )
        Text(
            "  $coins",
            color = theme.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    theme: ThemeDef,
    modifier: Modifier = Modifier
) {
    TngCard(theme = theme, modifier = modifier) {
        Text(
            value,
            color = theme.accent,
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            label,
            color = theme.textPrimary.copy(alpha = 0.8f),
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
    }
}

@Composable
fun SectionTitle(text: String, theme: ThemeDef, modifier: Modifier = Modifier) {
    Text(
        text,
        color = theme.textPrimary,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButtonCompat(confirmLabel, onClick = onConfirm)
        },
        dismissButton = {
            TextButtonCompat(dismissLabel, onClick = onDismiss)
        }
    )
}

@Composable
fun TextButtonCompat(text: String, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) { Text(text) }
}
