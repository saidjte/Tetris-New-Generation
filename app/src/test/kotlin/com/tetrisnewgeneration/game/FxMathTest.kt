package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.ui.fx.FxMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/** Pure math curves behind the v1.3.0 presentation effects. */
class FxMathTest {

    // ---- screen shake (C4) ---------------------------------------------

    @Test
    fun `shake starts and ends at zero`() {
        assertEquals(0f, FxMath.shakeOffset(0f, 200f, 8f), 1e-6f)
        assertEquals(0f, FxMath.shakeOffset(200f, 200f, 8f), 1e-6f)
        assertEquals(0f, FxMath.shakeOffset(250f, 200f, 8f), 1e-6f)
        assertEquals(0f, FxMath.shakeOffset(-5f, 200f, 8f), 1e-6f)
    }

    @Test
    fun `shake never exceeds amplitude`() {
        var t = 1f
        while (t < 320f) {
            assertTrue(abs(FxMath.shakeOffset(t, 320f, 10f)) <= 10f + 1e-4f)
            t += 1f
        }
    }

    @Test
    fun `shake with zero amplitude or duration is silent`() {
        assertEquals(0f, FxMath.shakeOffset(100f, 200f, 0f), 1e-6f)
        assertEquals(0f, FxMath.shakeOffset(100f, 0f, 8f), 1e-6f)
    }

    @Test
    fun `shake decays towards the end`() {
        // The envelope near 90% of the duration is much smaller than at 10%.
        val early = abs(FxMath.shakeOffset(32f, 320f, 10f))
        val late = abs(FxMath.shakeOffset(288f, 320f, 10f))
        assertTrue(early > late)
    }

    // ---- cleared-row beam (C2) -------------------------------------------

    @Test
    fun `beam width runs full to empty`() {
        assertEquals(1f, FxMath.beamWidth(0f), 1e-6f)
        assertEquals(0f, FxMath.beamWidth(1f), 1e-6f)
    }

    @Test
    fun `beam width is monotonically decreasing and clamped`() {
        var prev = FxMath.beamWidth(-0.5f) // clamped to 0 progress -> 1
        assertEquals(1f, prev, 1e-6f)
        var p = 0f
        while (p <= 1f) {
            val w = FxMath.beamWidth(p)
            assertTrue(w <= prev + 1e-6f)
            prev = w
            p += 0.05f
        }
        assertEquals(0f, FxMath.beamWidth(1.5f), 1e-6f) // clamped high
    }

    // ---- shockwave ring (C5) ----------------------------------------------

    @Test
    fun `ring expands ease-out and stays bounded`() {
        val maxR = 100f
        var prev = 0f
        var p = 0f
        while (p <= 1f) {
            val r = FxMath.ringRadius(p, maxR)
            assertTrue(r >= prev - 1e-6f)
            assertTrue(r <= maxR + 1e-4f)
            prev = r
            p += 0.05f
        }
        assertEquals(maxR, FxMath.ringRadius(1f, maxR), 1e-4f)
        // Ease-out: the first half covers more than half the distance.
        assertTrue(FxMath.ringRadius(0.5f, maxR) > maxR / 2f)
    }

    @Test
    fun `ring alpha fades to zero`() {
        assertEquals(0.8f, FxMath.ringAlpha(0f), 1e-6f)
        assertEquals(0f, FxMath.ringAlpha(1f), 1e-6f)
    }

    // ---- score popup (C3) ---------------------------------------------------

    @Test
    fun `popup rise increases and bounds`() {
        var prev = 0f
        var p = 0f
        while (p <= 1f) {
            val r = FxMath.popupRise(p, 50f)
            assertTrue(r >= prev - 1e-6f)
            assertTrue(r <= 50f + 1e-4f)
            prev = r
            p += 0.05f
        }
        assertEquals(50f, FxMath.popupRise(1f, 50f), 1e-4f)
    }

    @Test
    fun `popup scale pops in fast then holds at one`() {
        assertTrue(FxMath.popupScale(0f) >= 0.7f)
        assertEquals(1f, FxMath.popupScale(0.5f), 1e-3f)
        assertEquals(1f, FxMath.popupScale(1f), 1e-3f)
        var p = 0f
        while (p <= 1f) {
            val s = FxMath.popupScale(p)
            assertTrue(s in 0.69f..1.01f)
            p += 0.05f
        }
    }

    @Test
    fun `popup alpha holds then fades`() {
        assertEquals(1f, FxMath.popupAlpha(0f), 1e-6f)
        assertEquals(1f, FxMath.popupAlpha(0.55f), 1e-6f)
        assertEquals(0f, FxMath.popupAlpha(1f), 1e-6f)
    }

    // ---- combo heat (C6) -----------------------------------------------------

    @Test
    fun `combo heat clamps between zero and one`() {
        assertEquals(0f, FxMath.comboHeat(-1), 1e-6f)
        assertEquals(0f, FxMath.comboHeat(0), 1e-6f)
        assertEquals(0.4f, FxMath.comboHeat(2), 1e-6f)
        assertEquals(1f, FxMath.comboHeat(5), 1e-6f)
        assertEquals(1f, FxMath.comboHeat(12), 1e-6f)
    }

    // ---- ghost breathing (C7) --------------------------------------------------

    @Test
    fun `ghost alpha stays inside the breathing band`() {
        var ms = 0L
        while (ms <= 950) {
            val a = FxMath.ghostAlpha(ms)
            assertTrue("alpha $a at $ms", a >= FxMath.GHOST_ALPHA_MIN - 1e-4f)
            assertTrue("alpha $a at $ms", a <= FxMath.GHOST_ALPHA_MAX + 1e-4f)
            ms += 25
        }
    }

    @Test
    fun `ghost alpha reaches both band edges`() {
        // Peak at a quarter period, trough at three quarters.
        val peak = FxMath.ghostAlpha(950L / 4)
        val trough = FxMath.ghostAlpha(950L * 3 / 4)
        assertTrue(peak > 0.33f)
        assertTrue(trough < 0.15f)
    }

    @Test
    fun `frozen frame clock gives constant midpoint`() {
        // reducedMotion freezes the frame clock at 0 -> phase 0 -> midpoint.
        assertEquals(0.24f, FxMath.ghostAlpha(0L), 1e-6f)
        assertEquals(FxMath.ghostAlpha(0L), FxMath.ghostAlpha(950L), 1e-6f)
    }
}
