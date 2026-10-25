package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.engine.GameEvent
import com.tetrisnewgeneration.game.engine.PieceType
import com.tetrisnewgeneration.game.engine.TSpinKind
import com.tetrisnewgeneration.game.modes.ModeConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.3.0 B1: the LinesCleared event must carry the pre-collapse indices of
 * the removed rows so the renderer can flash them (row beams).
 */
class ClearRowsFxTest {

    /**
     * Deterministic setup: bottom row (21) is fully filled, row 20 has a
     * 4-wide gap exactly where the I piece spawns (columns 3..6). One hard
     * drop fills the gap and clears BOTH rows.
     */
    private fun config() = ModeConfig(
        id = "fx_test",
        endOnGoal = false,
        fixedPieces = listOf(PieceType.I),
        initialBoard = List(20) { ".........." } + listOf(
            "III....III", // row 20: gap at columns 3-6 (I spawn columns)
            "IIIIIIIIII"  // row 21: full
        )
    )

    @Test
    fun `lines cleared event carries the removed row indices`() {
        val engine = com.tetrisnewgeneration.game.engine.GameEngine(config())
        engine.start()
        engine.hardDrop()

        val cleared = engine.drainEvents().filterIsInstance<GameEvent.LinesCleared>()
        assertEquals(1, cleared.size)
        val event = cleared.first()
        assertEquals(2, event.lines)
        assertEquals(listOf(20, 21), event.clearedRows)
    }

    @Test
    fun `board really contained the cleared rows before the drop`() {
        val engine = com.tetrisnewgeneration.game.engine.GameEngine(config())
        // Pre-conditions of the hand-drawn board.
        assertTrue(engine.board.cell(0, 21) != null)
        assertTrue(engine.board.cell(3, 20) == null)
        assertTrue(engine.board.cell(7, 20) != null)
    }

    @Test
    fun `clearedRows defaults to empty for source compatibility`() {
        // Old call sites (and tests) may construct the event without rows.
        val legacy = GameEvent.LinesCleared(
            lines = 2,
            tspinKind = TSpinKind.NONE,
            backToBack = false,
            combo = -1,
            perfectClear = false,
            points = 100
        )
        assertTrue(legacy.clearedRows.isEmpty())
    }
}
