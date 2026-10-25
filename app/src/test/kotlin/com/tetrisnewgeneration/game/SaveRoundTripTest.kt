package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.engine.GameSave
import com.tetrisnewgeneration.game.modes.Modes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Session save/restore round trip. */
class SaveRoundTripTest {

    @Test
    fun `engine state survives serialization round trip`() {
        var engine = com.tetrisnewgeneration.game.engine.GameEngine(
            Modes.byId(Modes.MARATHON)!!, seed = 42
        )
        engine.start()
        repeat(5) { engine.move(if (it % 2 == 0) 1 else -1) }
        engine.hold()
        engine.hardDrop()
        if (engine.state == com.tetrisnewgeneration.game.engine.GameEngine.State.OVER) {
            engine = com.tetrisnewgeneration.game.engine.GameEngine(
                Modes.byId(Modes.MARATHON)!!, seed = 42
            )
            engine.start()
        }
        val save = GameSave.fromEngine(engine)
        assertNotNull("playing round must be saveable", save)

        val json = kotlinx.serialization.json.Json.encodeToString(
            GameSave.serializer(), save!!
        )
        val decoded = kotlinx.serialization.json.Json.decodeFromString(
            GameSave.serializer(), json
        )
        val restored = GameSave.toEngine(decoded)
        assertNotNull(restored)
        restored!!
        assertEquals(save.board, boardToStrings(restored))
        assertEquals(save.score, restored.score)
        assertEquals(save.lines, restored.lines)
        assertEquals(save.level, restored.level)
        assertEquals(save.hold, restored.holdType?.name)
        assertEquals(save.elapsedMs, restored.elapsedMs)
        assertTrue(restored.state == com.tetrisnewgeneration.game.engine.GameEngine.State.READY ||
            restored.state == com.tetrisnewgeneration.game.engine.GameEngine.State.PLAYING)
    }

    @Test
    fun `corrupt save fails gracefully`() {
        val bogus = GameSave(
            modeId = "x", modeJson = "{}",
            board = List(3) { ".........." }, // wrong height
            queue = listOf("T"), hold = null,
            score = 0, lines = 0, level = 1, tetrises = 0, combo = -1,
            elapsedMs = 0, canHold = true
        )
        val engine = GameSave.toEngine(bogus)
        assertEquals(null, engine)
    }

    private fun boardToStrings(engine: com.tetrisnewgeneration.game.engine.GameEngine): List<String> {
        val rows = mutableListOf<String>()
        for (y in 0 until engine.board.rows) {
            val sb = StringBuilder(engine.board.cols)
            for (x in 0 until engine.board.cols) {
                val cell = engine.board.cell(x, y)
                sb.append(if (cell == null) '.' else cell.name[0])
            }
            rows.add(sb.toString())
        }
        return rows
    }
}
