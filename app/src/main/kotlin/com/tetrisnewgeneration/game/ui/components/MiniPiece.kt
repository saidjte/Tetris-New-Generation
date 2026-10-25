package com.tetrisnewgeneration.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.tetrisnewgeneration.game.engine.PieceType
import com.tetrisnewgeneration.game.themes.ThemeDef

/**
 * Tiny piece preview used by the Next queue and Hold box.
 * Renders the spawn-state shape of [PieceType] at [typeIndex] (ordinal).
 * When [sprites] is provided (T99 skin, v1.4.0) cells draw the real sprite
 * instead of the flat rounded rectangle.
 */
@Composable
fun MiniPiece(
    theme: ThemeDef,
    typeIndex: Int,
    cellSize: Dp,
    alpha: Float = 1f,
    sprites: Map<Int, ImageBitmap>? = null
) {
    val type = PieceType.entries.getOrNull(typeIndex) ?: return
    val cells = com.tetrisnewgeneration.game.engine.Tetrominoes.shape(type, 0)
    val color = theme.colorOf(type).copy(alpha = alpha)
    val sprite = sprites?.get(type.ordinal)

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
            val x = ox + (c.x - minX) * cs
            val y = oy + (c.y - minY) * cs
            if (sprite != null) {
                drawImage(
                    image = sprite,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(sprite.width, sprite.height),
                    dstOffset = IntOffset(x.toInt() + 1, y.toInt() + 1),
                    dstSize = IntSize((cs - 2).toInt().coerceAtLeast(1), (cs - 2).toInt().coerceAtLeast(1)),
                    alpha = alpha
                )
            } else {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x + 1, y + 1),
                    size = Size(cs - 2, cs - 2),
                    cornerRadius = CornerRadius(cs * 0.2f, cs * 0.2f)
                )
            }
        }
    }
}
