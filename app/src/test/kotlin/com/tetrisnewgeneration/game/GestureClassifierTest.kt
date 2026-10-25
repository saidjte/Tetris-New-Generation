package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.ui.screens.GestureClassifier
import com.tetrisnewgeneration.game.ui.screens.GestureClassifier.EndGesture
import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests for the release-phase decisions of the full-screen gesture layer. */
class GestureClassifierTest {

    private val cell = 100f

    @Test
    fun `slow long downward drag does not hard drop`() {
        // Deliberate soft-drop drag: far, but far too slow.
        assertEquals(
            EndGesture.NONE,
            GestureClassifier.classify(totalDownPx = 500f, totalUpPx = 0f, velocityPxMs = 0.4f, cellPx = cell)
        )
    }

    @Test
    fun `fast downward flick triggers hard drop`() {
        assertEquals(
            EndGesture.HARD_DROP,
            GestureClassifier.classify(totalDownPx = 300f, totalUpPx = 0f, velocityPxMs = 5f, cellPx = cell)
        )
    }

    @Test
    fun `short fast jiggle does not hard drop`() {
        // Quick hand jitter must never slam the piece.
        assertEquals(
            EndGesture.NONE,
            GestureClassifier.classify(totalDownPx = 60f, totalUpPx = 0f, velocityPxMs = 6f, cellPx = cell)
        )
    }

    @Test
    fun `fast flick just under the distance bar does not hard drop`() {
        assertEquals(
            EndGesture.NONE,
            GestureClassifier.classify(totalDownPx = 100f, totalUpPx = 0f, velocityPxMs = 4f, cellPx = cell)
        )
    }

    @Test
    fun `upward drag over two cells triggers hold`() {
        assertEquals(
            EndGesture.HOLD,
            GestureClassifier.classify(totalDownPx = 0f, totalUpPx = 220f, velocityPxMs = -0.5f, cellPx = cell)
        )
    }

    @Test
    fun `small upward drift does not trigger hold`() {
        assertEquals(
            EndGesture.NONE,
            GestureClassifier.classify(totalDownPx = 0f, totalUpPx = 100f, velocityPxMs = -0.5f, cellPx = cell)
        )
    }

    @Test
    fun `mixed gesture dominated by slow downward travel is none`() {
        assertEquals(
            EndGesture.NONE,
            GestureClassifier.classify(totalDownPx = 400f, totalUpPx = 100f, velocityPxMs = 0.5f, cellPx = cell)
        )
    }

    @Test
    fun `upward dominated mixed gesture resolves to hold`() {
        assertEquals(
            EndGesture.HOLD,
            GestureClassifier.classify(totalDownPx = 50f, totalUpPx = 300f, velocityPxMs = -1f, cellPx = cell)
        )
    }

    @Test
    fun `degenerate cell size is safe`() {
        assertEquals(
            EndGesture.NONE,
            GestureClassifier.classify(totalDownPx = 300f, totalUpPx = 0f, velocityPxMs = 5f, cellPx = 0f)
        )
    }

    @Test
    fun `velocity threshold constant is documented value`() {
        assertEquals(2.0f, GestureClassifier.HARD_DROP_VELOCITY_PX_MS, 0f)
    }
}
