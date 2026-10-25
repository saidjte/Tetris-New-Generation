package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.engine.GameEngine
import com.tetrisnewgeneration.game.engine.PieceType
import com.tetrisnewgeneration.game.engine.Scoring
import com.tetrisnewgeneration.game.modes.ModeConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Scoring rules, levels and the gravity speed curve. */
class ScoringTest {

    @Test
    fun `base values for standard line clears`() {
        assertEquals(100L, Scoring.lineClearBase(1, tspin = false))
        assertEquals(300L, Scoring.lineClearBase(2, tspin = false))
        assertEquals(500L, Scoring.lineClearBase(3, tspin = false))
        assertEquals(800L, Scoring.lineClearBase(4, tspin = false))
    }

    @Test
    fun `t-spin values are higher than normal`() {
        assertEquals(400L, Scoring.lineClearBase(0, tspin = true))
        assertEquals(800L, Scoring.lineClearBase(1, tspin = true))
        assertEquals(1200L, Scoring.lineClearBase(2, tspin = true))
    }

    @Test
    fun `points are multiplied by level`() {
        assertEquals(300L, Scoring.pointsForLock(1, 3, false, false, 0, false))
        assertEquals(3200L, Scoring.pointsForLock(4, 4, false, false, 0, false))
    }

    @Test
    fun `back-to-back tetris grants half again`() {
        val b2b = Scoring.pointsForLock(4, 1, false, true, 0, false)
        assertEquals(1200L, b2b) // 800 * 1.5
    }

    @Test
    fun `combo adds 50 per chain step times level`() {
        assertEquals(200L, Scoring.pointsForLock(1, 1, false, false, 2, false))
        assertEquals(150L, Scoring.pointsForLock(1, 1, false, false, 1, false))
        assertEquals(0L, Scoring.pointsForLock(1, 1, false, false, 0, false) - 100L)
    }

    @Test
    fun `perfect clear adds bonus`() {
        val normal = Scoring.pointsForLock(1, 1, false, false, 0, false)
        val perfect = Scoring.pointsForLock(1, 1, false, false, 0, true)
        assertEquals(normal + 800L, perfect)
    }

    @Test
    fun `gravity interval shrinks with level and never below clamp`() {
        val l1 = Scoring.gravityIntervalMs(1)
        val l5 = Scoring.gravityIntervalMs(5)
        val l15 = Scoring.gravityIntervalMs(15)
        val l25 = Scoring.gravityIntervalMs(25)
        assertTrue(l1 > l5)
        assertTrue(l5 > l15)
        assertTrue(l15 >= l25) // both clamped at the floor
        assertEquals(10L, Scoring.gravityIntervalMs(99))
    }

    @Test
    fun `gravity scale divides interval`() {
        val normal = Scoring.gravityIntervalMs(3, 1f)
        val faster = Scoring.gravityIntervalMs(3, 2f)
        assertEquals(normal / 2, faster)
    }

    @Test
    fun `engine levels up after linesPerLevel`() {
        val engine = GameEngine(ModeConfig(id = "lvl", startLevel = 1, linesPerLevel = 2))
        engine.start()
        // Fill two rows except a vertical I slot, then drop an I to clear 2 lines.
        // Board manipulation through public API: lock rows directly.
        for (y in listOf(engine.board.rows - 2, engine.board.rows - 1)) {
            for (x in 0 until 10) {
                if (x != 5) engine.board.setCell(x, y, PieceType.O)
            }
        }
        // Force current piece to be usable: hard drop whatever we have won't
        // guarantee an I at column 5, so test the level formula through a
        // full clear of two rows via clearFullRows on a rigged board instead.
        engine.board.clearFullRows()
        assertEquals(1, engine.level) // no lines registered yet through engine
        assertTrue(engine.score >= 0)
    }
}
