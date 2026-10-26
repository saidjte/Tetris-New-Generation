package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.audio.ComboPitch
import com.tetrisnewgeneration.game.ui.fx.FxMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.6.0 (V5) — the HUD combo chip shares the audio pitch ramp; its visual
 * scale must stay in sync and stay bounded.
 */
class ComboChipScaleTest {

    @Test
    fun `rest scale when there is no escalation`() {
        assertEquals(1f, FxMath.comboChipScale(null), 1e-6f)
        assertEquals(1f, FxMath.comboChipScale(0.9f), 1e-6f)
        assertEquals(1f, FxMath.comboChipScale(ComboPitch.BASE_RATE), 1e-6f)
    }

    @Test
    fun `scale grows with the pitch ramp and stays bounded`() {
        val first = FxMath.comboChipScale(ComboPitch.rateFor(3))
        val mid = FxMath.comboChipScale(ComboPitch.rateFor(7))
        val last = FxMath.comboChipScale(ComboPitch.rateFor(40))
        assertTrue("chip must grow with the chain", first > 1f)
        assertTrue("chip must keep growing", mid > first)
        assertEquals(FxMath.COMBO_CHIP_MAX_SCALE, last!!, 1e-4f)
        assertTrue(FxMath.comboChipScale(2f) <= FxMath.COMBO_CHIP_MAX_SCALE + 1e-6f)
    }
}
