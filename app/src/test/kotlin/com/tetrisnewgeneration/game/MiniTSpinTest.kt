package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.engine.Board
import com.tetrisnewgeneration.game.engine.Piece
import com.tetrisnewgeneration.game.engine.PieceType
import com.tetrisnewgeneration.game.engine.Scoring
import com.tetrisnewgeneration.game.engine.TSpinKind
import com.tetrisnewgeneration.game.engine.TSpinRules
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Mini vs Full T-spin classification (guideline corner rule) and its scoring.
 * Boards are built cell-by-cell: y=21 is the bottom row, x=0..9 left to right.
 */
class MiniTSpinTest {

    private fun board(fill: (Int, Int) -> PieceType?): Board {
        val b = Board()
        for (y in 0 until b.rows) for (x in 0 until b.cols) b.setCell(x, y, fill(x, y))
        return b
    }

    private fun fullRow(type: PieceType) = { _: Int, _: Int -> type }

    /** T pointing up (rot 0) with its flat row just above the floor: center (4,20). */
    private fun tUp() = Piece(PieceType.T, rotation = 0, x = 3, y = 19)

    @Test
    fun `both front corners over floor is a full t-spin`() {
        val b = board { x, y ->
            when {
                y == 21 -> PieceType.J                          // floor: BL + BR
                y == 19 && (x == 3 || x == 5) -> PieceType.J    // overhangs: TL + TR
                else -> null
            }
        }
        assertEquals(TSpinKind.FULL, TSpinRules.classify(b, tUp()))
    }

    @Test
    fun `three corners with one front is a mini t-spin`() {
        val b = board { x, y ->
            when {
                y == 21 -> PieceType.J                          // BL + BR
                y == 19 && x == 5 -> PieceType.J                // TR only (TL open)
                else -> null
            }
        }
        assertEquals(TSpinKind.MINI, TSpinRules.classify(b, tUp()))
    }

    @Test
    fun `two corners is not a t-spin`() {
        val b = board { x, y -> if (y == 21) PieceType.J else null } // floor only
        assertEquals(TSpinKind.NONE, TSpinRules.classify(b, tUp()))
    }

    @Test
    fun `front corners follow the rotation direction`() {
        // Right wall fills TR + BR (front for rot 1); an extra block fills BL.
        val b = board { x, y ->
            if (x == 9 || (x == 7 && y == 11)) PieceType.J else null
        }
        val tRight = Piece(PieceType.T, rotation = 1, x = 7, y = 9)
        assertEquals(TSpinKind.FULL, TSpinRules.classify(b, tRight))
    }

    @Test
    fun `non t pieces never classify as spin`() {
        val b = board(fullRow(PieceType.J))
        assertEquals(TSpinKind.NONE, TSpinRules.classify(b, Piece(PieceType.L, 0, 3, 19)))
    }

    @Test
    fun `mini scoring is distinct from full scoring`() {
        assertEquals(100L, Scoring.lineClearBase(0, tspin = true, mini = true))
        assertEquals(200L, Scoring.lineClearBase(1, tspin = true, mini = true))
        assertEquals(400L, Scoring.lineClearBase(2, tspin = true, mini = true))
        assertEquals(400L, Scoring.lineClearBase(0, tspin = true, mini = false))
        assertEquals(800L, Scoring.lineClearBase(1, tspin = true, mini = false))
    }

    @Test
    fun `mini clears qualify for back-to-back multiplier`() {
        // Mini single at level 2 with B2B: 200 * 2 * 1.5 = 600
        val points = Scoring.pointsForLock(
            lines = 1, level = 2, tspin = true,
            backToBack = true, combo = 0, perfectClear = false, mini = true
        )
        assertEquals(600L, points)
        // Full single at level 2 with B2B: 800 * 2 * 1.5 = 2400
        val full = Scoring.pointsForLock(
            lines = 1, level = 2, tspin = true,
            backToBack = true, combo = 0, perfectClear = false, mini = false
        )
        assertEquals(2400L, full)
    }
}
