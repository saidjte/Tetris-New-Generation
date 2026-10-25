package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.ui.fx.CountdownSteps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Structure of the round-start 3-2-1-GO countdown. */
class CountdownStepsTest {

    @Test
    fun `countdown has exactly four steps in order`() {
        val steps = CountdownSteps.build()
        assertEquals(4, steps.size)
        assertEquals(listOf("3", "2", "1", CountdownSteps.GO_LABEL), steps.map { it.label })
    }

    @Test
    fun `step durations match the spec`() {
        val steps = CountdownSteps.build()
        assertEquals(CountdownSteps.STEP_MS, steps[0].durationMs)
        assertEquals(CountdownSteps.STEP_MS, steps[1].durationMs)
        assertEquals(CountdownSteps.STEP_MS, steps[2].durationMs)
        assertEquals(CountdownSteps.GO_MS, steps[3].durationMs)
        assertEquals(550L, CountdownSteps.STEP_MS)
        assertEquals(650L, CountdownSteps.GO_MS)
    }

    @Test
    fun `total duration is about two and a half seconds`() {
        assertEquals(CountdownSteps.STEP_MS * 3 + CountdownSteps.GO_MS, CountdownSteps.totalMs())
        assertEquals(2300L, CountdownSteps.totalMs())
        assertTrue(CountdownSteps.totalMs() < 3000L)
    }

    @Test
    fun `go label is language neutral`() {
        assertEquals("GO", CountdownSteps.GO_LABEL)
    }
}
