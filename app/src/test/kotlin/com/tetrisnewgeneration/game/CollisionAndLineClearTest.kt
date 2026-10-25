package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.engine.Board
import com.tetrisnewgeneration.game.engine.Piece
import com.tetrisnewgeneration.game.engine.PieceType
import com.tetrisnewgeneration.game.engine.Point
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Collision detection, board bounds and line clearing. */
class CollisionAndLineClearTest {

    private val board = Board()

    @Test
    fun `piece collides with left wall`() {
        // J has a cell at box offset (0,0), so x=-1 puts a cell outside.
        val piece = Piece(PieceType.J, 0, x = -1, y = 1)
        assertTrue(board.collides(piece))
    }

    @Test
    fun `piece collides with floor`() {
        val piece = Piece(PieceType.O, 0, x = 4, y = Board.DEFAULT_HEIGHT)
        assertTrue(board.collides(piece))
    }

    @Test
    fun `piece collides with locked cells`() {
        board.lock(Piece(PieceType.I, 0, 3, 10)) // horizontal I at row 10 (real piece lock)
        val piece = Piece(PieceType.O, 0, 4, 10)
        assertTrue(board.collides(piece))
    }

    @Test
    fun `valid position inside empty board does not collide`() {
        val piece = Piece(PieceType.T, 0, 4, 5)
        assertFalse(board.collides(piece))
    }

    @Test
    fun `single full row is cleared and rows shift down`() {
        for (x in 0 until 10) board.lock(Piece(PieceType.J, 0, x, 21))
        // row 21 is now full except the piece overlaps itself; ensure all filled
        for (x in 0 until 10) if (board.cell(x, 21) == null) board.lock(Piece(PieceType.O, 0, x, 21))

        val cleared = board.clearFullRows()
        assertEquals(listOf(21), cleared)
        for (x in 0 until 10) assertEquals(null, board.cell(x, 21))
    }

    @Test
    fun `two full rows clear together`() {
        // J contributes one cell at (x, y) on its top row — perfect for filling.
        for (y in listOf(20, 21)) {
            for (x in 0 until 10) board.lock(Piece(PieceType.J, 0, x, y))
        }
        val cleared = board.clearFullRows()
        assertEquals(listOf(20, 21), cleared)
    }

    @Test
    fun `unfilled rows survive clearing`() {
        // Fill row 21 fully, and row 20 partially (J cell at x=0 only).
        for (x in 0 until 10) board.lock(Piece(PieceType.J, 0, x, 21))
        board.lock(Piece(PieceType.J, 0, 0, 20))
        board.clearFullRows()
        assertEquals(PieceType.J, board.cell(0, 21)) // partial row shifted down
        assertEquals(null, board.cell(5, 20))
    }

    @Test
    fun `top-out detection works`() {
        assertFalse(board.hasCellAboveVisible())
        board.lock(Piece(PieceType.O, 0, 4, 0)) // buffer zone
        assertTrue(board.hasCellAboveVisible())
    }

    @Test
    fun `cells method applies piece offset`() {
        val piece = Piece(PieceType.O, 0, 3, 2)
        val cells = piece.cells()
        assertTrue(cells.contains(Point(4, 2)))
        assertTrue(cells.contains(Point(5, 3)))
    }
}
