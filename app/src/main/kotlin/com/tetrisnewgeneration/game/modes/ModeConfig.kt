package com.tetrisnewgeneration.game.modes

import com.tetrisnewgeneration.game.engine.PieceType
import kotlinx.serialization.Serializable

/**
 * Declarative configuration for a play mode. The shared [com.tetrisnewgeneration.game.engine.GameEngine]
 * reads this config, so adding a mode never requires duplicating engine code.
 */
@Serializable
data class ModeConfig(
    val id: String,
    val startLevel: Int = 1,
    val linesPerLevel: Int = 10,
    val gravityScale: Float = 1f,
    val timeLimitMs: Long? = null,
    val survivalRampMs: Long? = null,
    val allowHold: Boolean = true,
    val allowGhost: Boolean = true,
    val allowRewind: Boolean = false,
    val zenMode: Boolean = false,
    val practiceMode: Boolean = false,
    val initialBoard: List<String> = emptyList(),
    val fixedPieces: List<PieceType> = emptyList(),
    val goalLines: Int? = null,
    val goalScore: Long? = null,
    val goalCombo: Int? = null,
    val goalTetrises: Int? = null,
    val moveLimit: Int? = null,
    val endOnGoal: Boolean = true
)
