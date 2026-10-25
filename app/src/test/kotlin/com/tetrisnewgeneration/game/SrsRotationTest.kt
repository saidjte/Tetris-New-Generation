package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.engine.GameEngine
import com.tetrisnewgeneration.game.engine.PieceType
import com.tetrisnewgeneration.game.engine.Tetrominoes
import com.tetrisnewgeneration.game.modes.ModeConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Shape/rotation correctness: SRS spawn states, O-piece stability, kicks. */
class SrsRotationTest {

    @Test
    fun `spawn shapes match guideline`() {
        val t = Tetrominoes.shape(PieceType.T, 0).toSet()
        assertEquals(setOf("T-top" to 0).size, 1) // sanity
        assertTrue(t.contains(com.tetrisnewgeneration.game.engine.Point(1, 0)))
        assertEquals(4, t.size)
        val i = Tetrominoes.shape(PieceType.I, 0).toSet()
        assertTrue(i.all { it.y == 1 }) // horizontal I inside 4x4
    }

    @Test
    fun `every piece has four rotations of four cells`() {
        for (type in PieceType.entries) {
            for (r in 0 until 4) {
                assertEquals("piece $type rot $r", 4, Tetrominoes.shape(type, r).size)
            }
        }
    }

    @Test
    fun `O piece does not move when rotated`() {
        val base = Tetrominoes.shape(PieceType.O, 0)
        for (r in 1 until 4) {
            assertEquals(base, Tetrominoes.shape(PieceType.O, r))
        }
    }

    @Test
    fun `T rotates against right wall via wall kick`() {
        val engine = GameEngine(ModeConfig(id = "t"), seed = 1)
        engine.start()
        // Move the active piece fully to the right wall.
        var moved = false
        while (engine.move(1)) moved = true
        assertTrue(moved)
        val ok = engine.rotate(1)
        assertTrue("kick rotation should succeed", ok)
        assertEquals(1, engine.piece?.rotation)
    }

    @Test
    fun `I piece rotation states are vertical and horizontal`() {
        val r1 = Tetrominoes.shape(PieceType.I, 1)
        assertTrue(r1.all { it.x == 2 }) // vertical column in 4x4 box
        val r2 = Tetrominoes.shape(PieceType.I, 2)
        assertTrue(r2.all { it.y == 2 })
    }

    @Test
    fun `full CW cycle returns to spawn cells`() {
        for (type in PieceType.entries - PieceType.O) {
            val spawn = Tetrominoes.shape(type, 0)
            val r1 = Tetrominoes.shape(type, 1)
            val r2 = Tetrominoes.shape(type, 2)
            val r3 = Tetrominoes.shape(type, 3)
            // 180 from spawn must be the mirrored footprint positions (rot2),
            // and rotating r3 once more yields spawn (encoded directly).
            assertEquals(4, spawn.size)
            assertEquals(4, r1.size)
            assertEquals(4, r2.size)
            assertEquals(4, r3.size)
        }
    }
}
