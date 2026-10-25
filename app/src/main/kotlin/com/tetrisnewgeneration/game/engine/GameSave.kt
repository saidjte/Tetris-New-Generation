package com.tetrisnewgeneration.game.engine

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Serializable snapshot of a running round, used for "resume last session".
 * The board is stored as one character per cell ('.' = empty).
 */
@Serializable
data class GameSave(
    val modeId: String,
    val modeJson: String,
    val board: List<String>,
    val queue: List<String>,
    val hold: String?,
    val score: Long,
    val lines: Int,
    val level: Int,
    val tetrises: Int,
    val combo: Int,
    val elapsedMs: Long,
    val canHold: Boolean
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

        fun fromEngine(engine: GameEngine): GameSave? {
            if (engine.state == GameEngine.State.OVER || engine.state == GameEngine.State.WON) return null
            val rows = mutableListOf<String>()
            for (y in 0 until engine.board.rows) {
                val sb = StringBuilder(engine.board.cols)
                for (x in 0 until engine.board.cols) {
                    val cell = engine.board.cell(x, y)
                    sb.append(if (cell == null) '.' else cell.name[0])
                }
                rows.add(sb.toString())
            }
            val modeJson = json.encodeToString(engine.config)
            return GameSave(
                modeId = engine.config.id,
                modeJson = modeJson,
                board = rows,
                queue = engine.nextQueue.map { it.name },
                hold = engine.holdType?.name,
                score = engine.score,
                lines = engine.lines,
                level = engine.level,
                tetrises = engine.tetrises,
                combo = engine.combo,
                elapsedMs = engine.elapsedMs,
                canHold = engine.holdType == null || engine.canHold
            )
        }

        fun toEngine(save: GameSave): GameEngine? {
            return try {
                val config = json.decodeFromString<com.tetrisnewgeneration.game.modes.ModeConfig>(save.modeJson)
                val engine = GameEngine(config, seed = 42L)
                engine.restoreFrom(save)
                engine
            } catch (_: Exception) {
                null
            }
        }
    }
}
