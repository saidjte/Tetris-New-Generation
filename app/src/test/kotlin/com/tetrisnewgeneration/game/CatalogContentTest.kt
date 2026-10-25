package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.modes.AdventureLevels
import com.tetrisnewgeneration.game.modes.Puzzles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Structural validation of the puzzle and adventure catalogs (v1.1.0 content). */
class CatalogContentTest {

    private val validChars = setOf('.', 'I', 'O', 'T', 'S', 'Z', 'J', 'L')

    @Test
    fun `puzzle catalog is sequential and structurally sound`() {
        val puzzles = Puzzles.all
        assertTrue("expected at least 12 puzzles", puzzles.size >= 12)
        puzzles.forEachIndexed { index, p ->
            assertEquals("puzzle ids must be 1..N sequential", index + 1, p.id)
            assertTrue("puzzle ${p.id} needs pieces", p.pieces.isNotEmpty())
            assertTrue("puzzle ${p.id} goal must be 1..4", p.goalLines in 1..4)
            assertTrue("puzzle ${p.id} board too tall", p.boardRows.size <= 22)
            p.boardRows.forEachIndexed { y, row ->
                assertEquals("puzzle ${p.id} row $y must be 10 wide", 10, row.length)
                row.forEach { ch ->
                    assertTrue("puzzle ${p.id} row $y has invalid char '$ch'", ch in validChars)
                }
            }
        }
    }

    @Test
    fun `adventure catalog is sequential with sane configs`() {
        val levels = AdventureLevels.all
        assertTrue("expected at least 15 adventure levels", levels.size >= 15)
        levels.forEachIndexed { index, lvl ->
            assertEquals("adventure ids must be 1..N sequential", index + 1, lvl.id)
            assertEquals("adventure ${lvl.id} config id mismatch", "adv_${lvl.id}", lvl.config.id)
            assertTrue("adventure ${lvl.id} coins must be positive", lvl.rewardCoins > 0)
            assertTrue("adventure ${lvl.id} xp must be positive", lvl.rewardXp > 0)
            val hasGoal = lvl.config.goalLines != null || lvl.config.goalScore != null ||
                lvl.config.goalCombo != null || lvl.config.goalTetrises != null ||
                lvl.config.timeLimitMs != null
            assertTrue("adventure ${lvl.id} needs a goal", hasGoal)
        }
    }

    @Test
    fun `garbage adventure board fills only the bottom rows`() {
        val garbage = AdventureLevels.all.first { it.id == 11 }
        val rows = garbage.config.initialBoard
        assertEquals(22, rows.size)
        rows.forEach { assertEquals(10, it.length) }
        val filled = rows.withIndex().filter { it.value.any { c -> c != '.' } }.map { it.index }
        assertEquals(listOf(18, 19, 20, 21), filled)
    }
}
