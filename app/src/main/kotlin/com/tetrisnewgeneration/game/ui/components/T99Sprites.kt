package com.tetrisnewgeneration.game.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.engine.PieceType

/**
 * T99 sprite skin (v1.4.0): the provided asset pack colors each piece type
 * with its own sprite (T=blue, Z=cyan, L=green, J=pink, I=purple, O=red,
 * S=yellow). Loaded once and shared by the board renderer and previews.
 */
@Composable
fun rememberT99Sprites(): Map<Int, ImageBitmap> {
    val blue = ImageBitmap.imageResource(R.drawable.t99_blue)     // T
    val cyan = ImageBitmap.imageResource(R.drawable.t99_cyan)     // Z
    val green = ImageBitmap.imageResource(R.drawable.t99_green)   // L
    val pink = ImageBitmap.imageResource(R.drawable.t99_pink)     // J
    val purple = ImageBitmap.imageResource(R.drawable.t99_purple) // I
    val red = ImageBitmap.imageResource(R.drawable.t99_red)       // O
    val yellow = ImageBitmap.imageResource(R.drawable.t99_yellow) // S
    return remember(blue, cyan, green, pink, purple, red, yellow) {
        mapOf(
            PieceType.T.ordinal to blue,
            PieceType.Z.ordinal to cyan,
            PieceType.L.ordinal to green,
            PieceType.J.ordinal to pink,
            PieceType.I.ordinal to purple,
            PieceType.O.ordinal to red,
            PieceType.S.ordinal to yellow
        )
    }
}
