package com.tetrisnewgeneration.game.ui.fx

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.themes.ThemeDef
import com.tetrisnewgeneration.game.ui.components.rememberFrameTimeMs
import kotlin.math.sin
import kotlin.random.Random

/**
 * v1.5.0 (V1): curated backdrop image per theme. Pure map so every theme id
 * is covered and the choice is JVM-testable.
 *
 *  - bg_pattern — dotted texture for the tech/retro family
 *  - bg_circles — soft dot grid for the cosmic/cold family
 *  - bg_color   — calm wash for the warm/light family
 */
object Backdrops {

    /** @return drawable name (without extension) for a theme id. */
    fun drawableName(themeId: String): String = when (themeId) {
        "galaxy", "crystal", "ocean", "aurora" -> "bg_circles"
        "minimal", "lava", "golden", "nature" -> "bg_color"
        else -> "bg_pattern"
    }
}

/**
 * v1.5.0 (V1): the image backdrop layer shown behind the game and home
 * screens when "image backgrounds" is enabled. The theme-tinted photo layer
 * sits above the theme gradient with a low alpha, and a slow two-layer
 * parallax starfield keeps the picture alive. With reduced motion the stars
 * stand still.
 */
@Composable
fun BackdropLayer(theme: ThemeDef, reducedMotion: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        val drawableName = Backdrops.drawableName(theme.id)
        val resId = when (drawableName) {
            "bg_circles" -> R.drawable.bg_circles
            "bg_color" -> R.drawable.bg_color
            else -> R.drawable.bg_pattern
        }
        Image(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.30f,
            colorFilter = ColorFilter.tint(theme.backgroundBottom, BlendMode.Multiply)
        )
        ParallaxStars(theme = theme, reducedMotion = reducedMotion)
    }
}

/** Two-layer drifting starfield (v1.5.0, V1). Frozen in reduced-motion mode. */
@Composable
fun ParallaxStars(theme: ThemeDef, reducedMotion: Boolean, modifier: Modifier = Modifier) {
    val frameMs = rememberFrameTimeMs(!reducedMotion)
    Canvas(modifier = modifier.fillMaxSize()) {
        val rng = Random(20260)
        for (layer in 0 until 2) {
            val count = if (layer == 0) 14 else 10
            val speed = if (layer == 0) 0.006f else 0.014f // px per ms
            val starSize = if (layer == 0) 1.4f else 2.4f
            for (i in 0 until count) {
                val baseX = rng.nextFloat()
                val baseY = rng.nextFloat()
                val twinklePhase = rng.nextFloat()
                val drift = (baseX * size.width - frameMs * speed) % size.width
                val x = (if (drift < 0) drift + size.width else drift)
                val y = baseY * size.height
                val twinkle = 0.5f + 0.5f * sin((frameMs + twinklePhase * 3000f) / 900f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f + 0.20f * twinkle),
                    radius = starSize,
                    center = Offset(x, y)
                )
            }
        }
    }
}
