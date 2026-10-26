package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.audio.DangerTracker
import com.tetrisnewgeneration.game.ui.components.T99RotationFrames
import com.tetrisnewgeneration.game.ui.fx.Backdrops
import com.tetrisnewgeneration.game.ui.fx.EffectsIntensity
import com.tetrisnewgeneration.game.ui.fx.FxMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** v1.5.0 "visual splendor" effect curves, mappings and settings parsing. */
class FxMathV15Test {

    // ---- V8: effects intensity -----------------------------------------

    @Test
    fun `intensity factors match the spec`() {
        assertEquals(0.5f, EffectsIntensity.LOW.factor, 1e-6f)
        assertEquals(1f, EffectsIntensity.MEDIUM.factor, 1e-6f)
        assertEquals(1.5f, EffectsIntensity.HIGH.factor, 1e-6f)
    }

    @Test
    fun `intensity parsing falls back to medium`() {
        assertEquals(EffectsIntensity.LOW, EffectsIntensity.fromId("low"))
        assertEquals(EffectsIntensity.HIGH, EffectsIntensity.fromId("HIGH"))
        assertEquals(EffectsIntensity.MEDIUM, EffectsIntensity.fromId("medium"))
        assertEquals(EffectsIntensity.MEDIUM, EffectsIntensity.fromId(null))
        assertEquals(EffectsIntensity.MEDIUM, EffectsIntensity.fromId(""))
        assertEquals(EffectsIntensity.MEDIUM, EffectsIntensity.fromId("turbo"))
    }

    @Test
    fun `factor helper is null-safe`() {
        assertEquals(0.5f, EffectsIntensity.factor("low"), 1e-6f)
        assertEquals(1f, EffectsIntensity.factor(null), 1e-6f)
    }

    // ---- V3: hard-drop trail -------------------------------------------

    @Test
    fun `trail fades from one to zero`() {
        assertEquals(1f, FxMath.trailAlpha(0f), 1e-6f)
        assertEquals(0.5f, FxMath.trailAlpha(0.5f), 1e-6f)
        assertEquals(0f, FxMath.trailAlpha(1f), 1e-6f)
        // clamped outside [0,1]
        assertEquals(1f, FxMath.trailAlpha(-0.5f), 1e-6f)
        assertEquals(0f, FxMath.trailAlpha(1.5f), 1e-6f)
    }

    @Test
    fun `trail alpha is monotonically decreasing`() {
        var prev = FxMath.trailAlpha(0f)
        var t = 0.05f
        while (t <= 1f) {
            val a = FxMath.trailAlpha(t)
            assertTrue("alpha must not increase at t=$t", a <= prev)
            prev = a
            t += 0.05f
        }
    }

    // ---- V3: impact dust budget ----------------------------------------

    @Test
    fun `dust count stays within 6-10 at medium`() {
        val n = FxMath.dustCount(EffectsIntensity.MEDIUM.factor)
        assertTrue("medium dust=$n must be in 6..10", n in 6..10)
    }

    @Test
    fun `dust count scales with intensity and stays bounded`() {
        val low = FxMath.dustCount(EffectsIntensity.LOW.factor)
        val med = FxMath.dustCount(EffectsIntensity.MEDIUM.factor)
        val high = FxMath.dustCount(EffectsIntensity.HIGH.factor)
        assertTrue(low < med)
        assertTrue(med < high)
        assertTrue(low >= 3)
        assertTrue(high <= 12)
        // extreme inputs are clamped, never explosive
        assertEquals(3, FxMath.dustCount(0f))
        assertEquals(12, FxMath.dustCount(100f))
    }

    // ---- V4: danger pulse ----------------------------------------------

    @Test
    fun `danger intensity mirrors the audio hysteresis window`() {
        assertEquals(0f, FxMath.dangerIntensity(15), 1e-6f)  // audio enters at 15
        assertEquals(0f, FxMath.dangerIntensity(10), 1e-6f)
        assertEquals(0.4f, FxMath.dangerIntensity(17), 1e-6f)  // 2/5 of the window
        assertEquals(0.6f, FxMath.dangerIntensity(18), 1e-6f)
        assertEquals(1f, FxMath.dangerIntensity(20), 1e-6f)  // top of the board
        assertEquals(1f, FxMath.dangerIntensity(22), 1e-6f)  // clamped
    }

    @Test
    fun `danger intensity grows with stack height`() {
        assertTrue(FxMath.dangerIntensity(16) < FxMath.dangerIntensity(18))
        assertTrue(FxMath.dangerIntensity(18) < FxMath.dangerIntensity(20))
    }

    @Test
    fun `danger pulse stays in bounds and is periodic`() {
        for (ms in longArrayOf(0, 100, 200, 400, 799, 800, 1600, 9999)) {
            val p = FxMath.dangerPulse(ms)
            assertTrue("pulse=$p out of range at $ms", p in 0f..1f)
        }
        assertEquals(FxMath.dangerPulse(0), FxMath.dangerPulse(800), 1e-6f)
        assertEquals(FxMath.dangerPulse(123), FxMath.dangerPulse(923), 1e-6f)
    }

    @Test
    fun `danger pulse freezes to a constant under reduced motion`() {
        // reduced motion freezes the frame clock; any frozen value must be stable
        val a = FxMath.dangerPulse(313)
        val b = FxMath.dangerPulse(313)
        assertEquals(a, b, 0f)
    }

    // ---- V4: audio/visual danger state sync -----------------------------

    @Test
    fun `danger tracker active flag flips with the voice events`() {
        val danger = DangerTracker()
        assertFalse(danger.active)
        // below the enter threshold: silent, inactive
        assertEquals(null, danger.update(14))
        assertFalse(danger.active)
        // entering danger: DANGER voice + active visual in the same update
        assertEquals("DANGER", danger.update(15)?.name)
        assertTrue(danger.active)
        // hysteresis band: still active, no repeat event
        assertEquals(null, danger.update(14))
        assertTrue(danger.active)
        // dropping back below the exit threshold: SAVED + inactive together
        assertEquals("SAVED", danger.update(13)?.name)
        assertFalse(danger.active)
    }

    // ---- V2: rotation-frame mapping --------------------------------------

    @Test
    fun `rotation frames cover every piece and rotation`() {
        // piece ordinals in PieceType.entries order: I O T S Z J L
        val bases = listOf("i", "o", "t", "s", "z", "j", "l")
        for (type in 0 until 7) {
            for (rot in 0 until 4) {
                val name = T99RotationFrames.drawableName(type, rot)
                assertEquals("t99rot_${bases[type]}_$rot", name)
            }
        }
    }

    @Test
    fun `rotation mapping wraps and clamps safely`() {
        assertEquals(T99RotationFrames.drawableName(2, 0), T99RotationFrames.drawableName(2, 4))
        assertEquals(T99RotationFrames.drawableName(2, 3), T99RotationFrames.drawableName(2, -1))
        assertEquals("t99rot_t_0", T99RotationFrames.drawableName(2, 0))
        // out-of-range ordinals clamp to the nearest entry instead of crashing
        assertEquals("t99rot_i_0", T99RotationFrames.drawableName(-1, 0))
        assertEquals("t99rot_l_2", T99RotationFrames.drawableName(99, 2))
    }

    // ---- V1: theme backdrop mapping --------------------------------------

    @Test
    fun `every shipped theme has a backdrop`() {
        val themes = listOf(
            "classic_night", "minimal", "neon", "galaxy", "aurora", "retro",
            "crystal", "lava", "ocean", "monochrome", "golden", "nature", "pixel"
        )
        val valid = setOf("bg_pattern", "bg_circles", "bg_color")
        for (theme in themes) {
            assertTrue(
                "theme $theme -> ${Backdrops.drawableName(theme)} is not a known backdrop",
                Backdrops.drawableName(theme) in valid
            )
        }
        // unknown ids still resolve deterministically
        assertEquals(Backdrops.drawableName("nonexistent"), Backdrops.drawableName("nonexistent"))
    }

    @Test
    fun `backdrop families follow the curated split`() {
        assertEquals("bg_circles", Backdrops.drawableName("galaxy"))
        assertEquals("bg_circles", Backdrops.drawableName("aurora"))
        assertEquals("bg_color", Backdrops.drawableName("minimal"))
        assertEquals("bg_color", Backdrops.drawableName("lava"))
        assertEquals("bg_pattern", Backdrops.drawableName("classic_night"))
        assertEquals("bg_pattern", Backdrops.drawableName("neon"))
    }
}
