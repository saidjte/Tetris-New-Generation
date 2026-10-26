package com.tetrisnewgeneration.game.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.tetrisnewgeneration.game.R
import com.tetrisnewgeneration.game.engine.PieceType

/**
 * v1.5.0 (V2): the T99 skin gains REAL rotation frames. The provided asset
 * pack ships four full-piece sprites per tetromino (frame r+1 == SRS rotation
 * r, piece-box aligned — verified cell-by-cell against the engine shapes), so
 * the active piece is drawn as one true-to-rotation image instead of a
 * rotation-blind stack of cells.
 *
 * Pure naming map (JVM-testable): drawable name for (piece ordinal, rotation).
 */
object T99RotationFrames {

    /** Drawable base names per piece ordinal, in PieceType.entries order. */
    private val BASE = listOf("i", "o", "t", "s", "z", "j", "l")

    /**
     * @param typeIdx piece type ordinal (PieceType.entries)
     * @param rotation SRS rotation state 0..3
     * @return drawable name without extension, e.g. "t99rot_t_2".
     */
    fun drawableName(typeIdx: Int, rotation: Int): String {
        val base = BASE.getOrNull(typeIdx.coerceIn(0, BASE.lastIndex)) ?: "t"
        val rot = ((rotation % 4) + 4) % 4
        return "t99rot_${base}_$rot"
    }
}

/** The 28 rotation-frame drawables in (piece ordinal, rotation) order. */
private val ROTATION_DRAWABLES: Map<Int, List<Int>> = mapOf(
    PieceType.I.ordinal to listOf(
        R.drawable.t99rot_i_0, R.drawable.t99rot_i_1,
        R.drawable.t99rot_i_2, R.drawable.t99rot_i_3
    ),
    PieceType.O.ordinal to listOf(
        R.drawable.t99rot_o_0, R.drawable.t99rot_o_1,
        R.drawable.t99rot_o_2, R.drawable.t99rot_o_3
    ),
    PieceType.T.ordinal to listOf(
        R.drawable.t99rot_t_0, R.drawable.t99rot_t_1,
        R.drawable.t99rot_t_2, R.drawable.t99rot_t_3
    ),
    PieceType.S.ordinal to listOf(
        R.drawable.t99rot_s_0, R.drawable.t99rot_s_1,
        R.drawable.t99rot_s_2, R.drawable.t99rot_s_3
    ),
    PieceType.Z.ordinal to listOf(
        R.drawable.t99rot_z_0, R.drawable.t99rot_z_1,
        R.drawable.t99rot_z_2, R.drawable.t99rot_z_3
    ),
    PieceType.J.ordinal to listOf(
        R.drawable.t99rot_j_0, R.drawable.t99rot_j_1,
        R.drawable.t99rot_j_2, R.drawable.t99rot_j_3
    ),
    PieceType.L.ordinal to listOf(
        R.drawable.t99rot_l_0, R.drawable.t99rot_l_1,
        R.drawable.t99rot_l_2, R.drawable.t99rot_l_3
    )
)

/**
 * Loads the 28 rotation-frame bitmaps once and keeps them indexed by
 * (piece ordinal) -> [rot0, rot1, rot2, rot3]. Decoding happens inside
 * [remember] so it runs exactly once per composition lifetime.
 */
@Composable
fun rememberT99RotationSprites(): Map<Int, List<ImageBitmap>> {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember(context) {
        ROTATION_DRAWABLES.mapValues { (_, ids) ->
            ids.map { id ->
                android.graphics.BitmapFactory
                    .decodeResource(context.resources, id)
                    .asImageBitmap()
            }
        }
    }
}
