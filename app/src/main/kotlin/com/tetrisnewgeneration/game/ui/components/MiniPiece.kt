package com.tetrisnewgeneration.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tetrisnewgeneration.game.engine.PieceType
import com.tetrisnewgeneration.game.themes.ThemeDef

/**
 * Tiny piece preview used by the Next queue and Hold box.
 * Renders the spawn-state shape of [PieceType] at [typeIndex] (ordinal).
 */
@Composable
fun MiniPiece(
    theme: ThemeDef,
    typeIndex: Int,
    cellSize: Dp,
    alpha: Float = 1f
) {
    val type = PieceType.entries.getOrNull(typeIndex) ?: return
    val cells = com.tetrisnewgeneration.game.engine.Tetrominoes.shape(type, 0)
    val color = theme.colorOf(type).copy(alpha = alpha)

    Canvas(modifier = Modifier.size(cellSize * 4, cellSize * 2)) {
        val cs = cellSize.toPx()
        // Center inside the 4x2 preview area.
        val minX = cells.minOf { it.x }
        val maxX = cells.maxOf { it.x }
        val minY = cells.minOf { it.y }
        val maxY = cells.maxOf { it.y }
        val w = (maxX - minX + 1) * cs
        val h = (maxY - minY + 1) * cs
        val ox = (size.width - w) / 2f
        val oy = (size.height - h) / 2f

        for (c in cells) {
            drawRoundRect(
                color = color,
                topLeft = Offset(ox + (c.x - minX) * cs + 1, oy + (c.y - minY) * cs + 1),
                size = Size(cs - 2, cs - 2),
                cornerRadius = CornerRadius(cs * 0.2f, cs * 0.2f)
            )
        }
    }
}
