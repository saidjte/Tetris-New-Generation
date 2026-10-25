package com.tetrisnewgeneration.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.tetrisnewgeneration.game.engine.PieceType
import com.tetrisnewgeneration.game.themes.BlockStyle
import com.tetrisnewgeneration.game.themes.BackgroundFx
import com.tetrisnewgeneration.game.themes.ParticleStyle
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.fx.FxMath
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Lightweight particle for line-clear effects. [style] comes from the
 * purchased effect pack so each pack finally renders differently:
 * sparks = long flying lines, burst = circles, trail = velocity streaks,
 * shatter = rotating squares, confetti = fluttering rectangles.
 */
class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var life: Float,           // 1.0 -> 0.0
    val decay: Float,
    val size: Float,
    val color: Color,
    val spin: Float = 0f,
    val style: ParticleStyle = ParticleStyle.BURST
) {
    var angle: Float = 0f
}

/** White-hot sweep over one cleared row (v1.3.0). */
class RowFlash(val row: Int) {
    var elapsedMs: Float = 0f
}

/** Floating "+points" popup (v1.3.0). */
class ScorePopup(val amount: Long) {
    var elapsedMs: Float = 0f
}

/** Expanding shockwave ring for tetris/perfect-clear moments (v1.3.0). */
class ShockwaveFx {
    var elapsedMs: Float = 0f
}

/**
 * High-performance playfield renderer: grid, locked cells, ghost piece, active
 * piece, background FX, particles and flashes. One Canvas, no per-frame
 * allocations beyond particles.
 */
@Composable
fun rememberFrameTimeMs(enabled: Boolean): Long {
    var frame by remember { mutableStateOf(0L) }
    if (enabled) {
        LaunchedEffect(Unit) {
            while (true) {
                withFrameNanos { frame = it / 1_000_000L }
            }
        }
    }
    return frame
}

@Composable
fun BoardCanvas(
    theme: ThemeDef,
    boardGrid: List<List<Int>>,
    activeCells: List<Triple<Int, Int, Int>>,
    ghostCells: List<Pair<Int, Int>>,
    particles: List<Particle>,
    flashAlpha: Float,
    flashColor: Color,
    reducedMotion: Boolean,
    rowFlashes: List<RowFlash> = emptyList(),
    scorePopups: List<ScorePopup> = emptyList(),
    shockwaves: List<ShockwaveFx> = emptyList(),
    comboHeat: Float = 0f,
    blockSprites: Map<Int, ImageBitmap>? = null,
    modifier: Modifier = Modifier
) {
    val frameMs = rememberFrameTimeMs(!reducedMotion)
    val pieceColors = remember(theme) {
        PieceType.entries.map { theme.colorOf(it) }
    }

    Canvas(modifier = modifier) {
        val cols = 10
        val visibleRows = 20
        val cell = kotlin.math.min(size.width / cols, size.height / visibleRows)
        val boardW = cell * cols
        val boardH = cell * visibleRows
        val offsetX = (size.width - boardW) / 2f
        val offsetY = (size.height - boardH) / 2f

        // Board background
        drawRoundRect(
            color = theme.boardBackground,
            topLeft = Offset(offsetX, offsetY),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(8f, 8f)
        )

        drawBackgroundFx(theme.backgroundFx, offsetX, offsetY, boardW, boardH, frameMs, theme)

        // Grid lines
        val grid = theme.gridLine
        var i = 1
        while (i < cols) {
            drawLine(grid, Offset(offsetX + i * cell, offsetY),
                Offset(offsetX + i * cell, offsetY + boardH), 1f)
            i++
        }
        i = 1
        while (i < visibleRows) {
            drawLine(grid, Offset(offsetX, offsetY + i * cell),
                Offset(offsetX + boardW, offsetY + i * cell), 1f)
            i++
        }

        // Ghost piece (outline) — alpha breathes slowly (v1.3.0, C7).
        // With reducedMotion the frame clock is frozen so this is a constant.
        val ghostAlpha = FxMath.ghostAlpha(frameMs)
        for ((gx, gy) in ghostCells) {
            if (gy < 0) continue
            val row = gy - 2 // buffer rows above the visible area
            if (row < 0) continue
            val color = pieceColors[activeCells.firstOrNull()?.third ?: 0]
            drawRoundRect(
                color = color.copy(alpha = ghostAlpha),
                topLeft = Offset(offsetX + gx * cell + 2, offsetY + row * cell + 2),
                size = Size(cell - 4, cell - 4),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 2f)
            )
        }

        // Locked cells
        for (y in 2 until minOf(boardGrid.size, 22)) {
            if (y - 2 >= visibleRows) continue
            val rowGrid = boardGrid[y]
            for (x in 0 until minOf(rowGrid.size, cols)) {
                val v = rowGrid[x]
                if (v > 0) {
                    drawBlock(
                        offsetX + x * cell, offsetY + (y - 2) * cell, cell,
                        pieceColors[v - 1], theme.blockStyle, blockSprites?.get(v - 1)
                    )
                }
            }
        }

        // Active piece
        for ((ax, ay, at) in activeCells) {
            val row = ay - 2
            if (row < 0) continue
            drawBlock(
                offsetX + ax * cell, offsetY + row * cell, cell,
                pieceColors[at], theme.blockStyle, blockSprites?.get(at)
            )
        }

        // Particles — one draw path per effect-pack style (v1.3.0, C1).
        for (p in particles) {
            val alpha = p.life.coerceIn(0f, 1f)
            val center = Offset(offsetX + p.x, offsetY + p.y)
            when (p.style) {
                ParticleStyle.SPARKS -> {
                    // Long thin rays flying outward along the velocity vector.
                    val tail = Offset(center.x - p.vx * 0.045f, center.y - p.vy * 0.045f)
                    drawLine(
                        color = p.color.copy(alpha = alpha),
                        start = tail,
                        end = center,
                        strokeWidth = 1.6f + p.size * 0.4f
                    )
                }
                ParticleStyle.BURST -> drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size * (0.5f + p.life / 2f),
                    center = center
                )
                ParticleStyle.TRAIL -> {
                    // Elongated streaks stretched along the direction of travel.
                    val tail = Offset(center.x - p.vx * 0.09f, center.y - p.vy * 0.09f)
                    drawLine(
                        color = p.color.copy(alpha = alpha * 0.55f),
                        start = tail,
                        end = center,
                        strokeWidth = p.size * 1.1f
                    )
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size * 0.6f * (0.5f + p.life / 2f),
                        center = center
                    )
                }
                ParticleStyle.SHATTER -> {
                    // Rotating debris squares tumbling with gravity.
                    val half = p.size * 1.4f
                    rotate(degrees = p.angle, pivot = center) {
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(center.x - half, center.y - half),
                            size = Size(half * 2, half * 2)
                        )
                    }
                }
                ParticleStyle.CONFETTI -> {
                    // Thin ribbons fluttering (wobble) as they fall.
                    val flutter = sin(p.angle * 2.2f) * 0.5f
                    rotate(degrees = p.angle + flutter * 40f, pivot = center) {
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(center.x - p.size * 0.5f, center.y - p.size * 1.6f),
                            size = Size(p.size, p.size * 3.2f)
                        )
                    }
                }
            }
        }

        // Cleared-row beams (v1.3.0, C2): white-hot sweep burning away.
        for (flash in rowFlashes) {
            val row = flash.row - 2 // buffer offset
            if (row < 0 || row >= visibleRows) continue
            val progress = (flash.elapsedMs / FxMath.BEAM_DURATION_MS).coerceIn(0f, 1f)
            val w = boardW * FxMath.beamWidth(progress)
            val a = FxMath.beamAlpha(progress)
            val top = offsetY + row * cell + cell * 0.04f
            val height = cell * 0.92f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.55f * a),
                topLeft = Offset(offsetX + (boardW - w) / 2f, top),
                size = Size(w, height),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFFFFF3B0).copy(alpha = 0.85f * a),
                topLeft = Offset(offsetX + (boardW - w) / 2f, top + height * 0.25f),
                size = Size(w, height * 0.5f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }

        // Shockwave rings (v1.3.0, C5).
        for (ring in shockwaves) {
            val progress = (ring.elapsedMs / FxMath.RING_DURATION_MS).coerceIn(0f, 1f)
            val maxR = sqrt(boardW * boardW + boardH * boardH) / 2f * 0.65f
            drawCircle(
                color = theme.accent.copy(alpha = FxMath.ringAlpha(progress)),
                radius = FxMath.ringRadius(progress, maxR),
                center = Offset(offsetX + boardW / 2f, offsetY + boardH / 2f),
                style = Stroke(width = 2f + (1f - progress) * cell * 0.35f)
            )
        }

        // Score popups (v1.3.0, C3): "+N" rises, then fades.
        if (scorePopups.isNotEmpty()) {
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD
                )
                color = android.graphics.Color.parseColor("#FFF7C2")
                setShadowLayer(6f, 0f, 2f, android.graphics.Color.argb(160, 0, 0, 0))
            }
            for (popup in scorePopups) {
                val progress = (popup.elapsedMs / FxMath.POPUP_DURATION_MS).coerceIn(0f, 1f)
                paint.textSize = cell * 1.15f * FxMath.popupScale(progress)
                paint.alpha = (FxMath.popupAlpha(progress) * 255).toInt()
                val cx = offsetX + boardW / 2f
                val cy = offsetY + boardH * 0.34f - FxMath.popupRise(progress, cell * 2.2f)
                drawContext.canvas.nativeCanvas.drawText(
                    "+${popup.amount}", cx, cy, paint
                )
            }
        }

        // Event flash overlay
        if (flashAlpha > 0.01f) {
            drawRoundRect(
                color = flashColor.copy(alpha = flashAlpha),
                topLeft = Offset(offsetX, offsetY),
                size = Size(boardW, boardH),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }

        // Combo heat glow (v1.3.0, C6): the frame warms up with the chain.
        if (comboHeat > 0f) {
            val glowColor = Color(0xFFFF8C1A)
            drawRoundRect(
                color = glowColor.copy(alpha = 0.12f + 0.28f * comboHeat),
                topLeft = Offset(offsetX, offsetY),
                size = Size(boardW, boardH),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 3f + comboHeat * 5f)
            )
            drawRoundRect(
                color = glowColor.copy(alpha = 0.05f + 0.12f * comboHeat),
                topLeft = Offset(offsetX - 3, offsetY - 3),
                size = Size(boardW + 6, boardH + 6),
                cornerRadius = CornerRadius(11f, 11f),
                style = Stroke(width = 3f + comboHeat * 4f)
            )
        }

        // Border
        drawRoundRect(
            color = theme.boardBorder,
            topLeft = Offset(offsetX, offsetY),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 3f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBlock(
    x: Float, y: Float, cell: Float, color: Color, style: BlockStyle,
    sprite: ImageBitmap? = null
) {
    val pad = cell * 0.06f
    when (style) {
        BlockStyle.T99 -> {
            // v1.4.0: real 32x32 sprite per piece type (pixel-snapped draw).
            if (sprite != null) {
                val inset = (cell * 0.03f).toInt().coerceAtLeast(0)
                val dst = (cell - inset * 2).toInt().coerceAtLeast(1)
                drawImage(
                    image = sprite,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(sprite.width, sprite.height),
                    dstOffset = IntOffset(x.toInt() + inset, y.toInt() + inset),
                    dstSize = IntSize(dst, dst)
                )
            } else {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x + pad, y + pad),
                    size = Size(cell - pad * 2, cell - pad * 2),
                    cornerRadius = CornerRadius(cell * 0.12f, cell * 0.12f)
                )
            }
        }
        BlockStyle.GLOW -> {
            drawRoundRect(
                color = color.copy(alpha = 0.25f),
                topLeft = Offset(x - 2, y - 2),
                size = Size(cell + 4, cell + 4),
                cornerRadius = CornerRadius(cell * 0.2f, cell * 0.2f)
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(cell - pad * 2, cell - pad * 2),
                cornerRadius = CornerRadius(cell * 0.18f, cell * 0.18f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(x + pad, y + pad),
                size = Size(cell - pad * 2, (cell - pad * 2) * 0.4f),
                cornerRadius = CornerRadius(cell * 0.14f, cell * 0.14f)
            )
        }
        BlockStyle.OUTLINE -> {
            drawRoundRect(
                color = Color(0xFF0A0D18),
                topLeft = Offset(x + pad, y + pad),
                size = Size(cell - pad * 2, cell - pad * 2),
                cornerRadius = CornerRadius(cell * 0.16f, cell * 0.16f)
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(cell - pad * 2, cell - pad * 2),
                cornerRadius = CornerRadius(cell * 0.16f, cell * 0.16f),
                style = Stroke(width = cell * 0.1f)
            )
        }
        BlockStyle.GEO -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(cell - pad * 2, cell - pad * 2),
                cornerRadius = CornerRadius(cell * 0.1f, cell * 0.1f)
            )
            val path = Path().apply {
                moveTo(x + pad, y + pad)
                lineTo(x + cell - pad, y + pad)
                lineTo(x + pad, y + cell - pad)
                close()
            }
            drawPath(path, Color.White.copy(alpha = 0.28f))
        }
        BlockStyle.CONTRAST -> {
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(x + 1, y + 1),
                size = Size(cell - 2, cell - 2),
                cornerRadius = CornerRadius(cell * 0.14f, cell * 0.14f)
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(x + cell * 0.12f, y + cell * 0.12f),
                size = Size(cell * 0.76f, cell * 0.76f),
                cornerRadius = CornerRadius(cell * 0.1f, cell * 0.1f)
            )
        }
        BlockStyle.PIXEL -> {
            drawRect(
                color = color,
                topLeft = Offset(x + 1, y + 1),
                size = Size(cell - 2, cell - 2)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.18f),
                topLeft = Offset(x + 2, y + 2),
                size = Size(cell - 4, (cell - 4) * 0.3f)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = Offset(x + 2, y + cell * 0.7f),
                size = Size(cell - 4, cell * 0.28f)
            )
        }
        BlockStyle.STANDARD -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(x + pad, y + pad),
                size = Size(cell - pad * 2, cell - pad * 2),
                cornerRadius = CornerRadius(cell * 0.18f, cell * 0.18f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.20f),
                topLeft = Offset(x + pad, y + pad),
                size = Size(cell - pad * 2, (cell - pad * 2) * 0.35f),
                cornerRadius = CornerRadius(cell * 0.12f, cell * 0.12f)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBackgroundFx(
    fx: BackgroundFx,
    x: Float, y: Float, w: Float, h: Float,
    frameMs: Long,
    theme: ThemeDef
) {
    when (fx) {
        BackgroundFx.NONE -> Unit
        BackgroundFx.PULSE -> {
            val t = (frameMs % 4000) / 4000f
            val alpha = 0.05f + 0.05f * sin(t * 2f * Math.PI.toFloat())
            drawRoundRect(
                color = theme.accent.copy(alpha = alpha),
                topLeft = Offset(x, y), size = Size(w, h),
                cornerRadius = CornerRadius(8f, 8f)
            )
        }
        BackgroundFx.STARS -> {
            val rng = Random(42)
            for (i in 0 until 40) {
                val sx = x + rng.nextFloat() * w
                val sy = y + rng.nextFloat() * h
                val phase = (frameMs % 3000 + i * 137) % 3000 / 3000f
                val alpha = 0.25f + 0.45f * (0.5f + 0.5f * sin(phase * 2f * Math.PI.toFloat()))
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 1.5f + (i % 3),
                    center = Offset(sx, sy)
                )
            }
        }
        BackgroundFx.AURORA -> {
            val t = (frameMs % 6000) / 6000f
            for (band in 0 until 3) {
                val cy = y + h * (0.2f + band * 0.25f) + 20f * sin((t + band) * 2f * Math.PI.toFloat())
                drawCircle(
                    color = (if (band % 2 == 0) Color(0xFF2DD4BF) else Color(0xFF818CF8))
                        .copy(alpha = 0.08f),
                    radius = h * 0.16f,
                    center = Offset(x + w * (0.3f + 0.4f * t * ((band % 2) * 2 - 1)), cy)
                )
            }
        }
        BackgroundFx.EMBERS -> {
            val rng = Random(7)
            for (i in 0 until 24) {
                val bx = x + rng.nextFloat() * w
                val speed = 12f + rng.nextFloat() * 18f
                val by = y + h - ((frameMs / 16f * speed / 10f + i * 60f) % h)
                val alpha = 0.35f * (1f - by / h)
                drawCircle(
                    color = Color(0xFFFB923C).copy(alpha = alpha),
                    radius = 2f + (i % 2),
                    center = Offset(bx, by)
                )
            }
        }
        BackgroundFx.WAVES -> {
            val t = (frameMs % 5000) / 5000f
            for (band in 0 until 2) {
                val cy = y + h * (0.3f + band * 0.4f) + 14f * sin((t + band * 0.5f) * 2f * Math.PI.toFloat())
                drawCircle(
                    color = theme.accent.copy(alpha = 0.06f),
                    radius = h * 0.2f,
                    center = Offset(x + w / 2, cy)
                )
            }
        }
        BackgroundFx.SNOW -> {
            val rng = Random(11)
            for (i in 0 until 26) {
                val bx = x + rng.nextFloat() * w
                val speed = 8f + rng.nextFloat() * 14f
                val by = y + ((frameMs / 16f * speed / 10f + i * 47f) % h)
                val sway = 8f * sin((frameMs / 700f) + i)
                drawCircle(
                    color = Color.White.copy(alpha = 0.16f),
                    radius = 1.5f + (i % 3),
                    center = Offset(bx + sway, by)
                )
            }
        }
    }
}
