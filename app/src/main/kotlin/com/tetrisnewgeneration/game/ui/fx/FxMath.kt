package com.tetrisnewgeneration.game.ui.fx

import kotlin.math.PI
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Pure math behind the v1.3.0 presentation effects (shake, beams, rings,
 * popups, combo heat, ghost breathing). No Android dependency: the canvas
 * renderer and UI layers only consume these functions, so every curve is
 * unit-testable on the JVM.
 */
object FxMath {

    // ------------------------------------------------ screen shake (C4)

    /** Oscillation count inside one shake; fractional cycles guarantee a decayed landing. */
    const val SHAKE_CYCLES = 4.5f

    /**
     * Damped shake offset in px for the given progress in time. Returns 0
     * before the start and after the end, and never exceeds the amplitude,
     * so the board always lands exactly back in place.
     */
    fun shakeOffset(elapsedMs: Float, durationMs: Float, amplitudePx: Float): Float {
        if (amplitudePx <= 0f || durationMs <= 0f) return 0f
        if (elapsedMs <= 0f || elapsedMs >= durationMs) return 0f
        val t = elapsedMs / durationMs
        val decay = (1f - t).pow(2)
        val oscillation = sin(t * SHAKE_CYCLES * 2f * PI.toFloat())
        return amplitudePx * decay * oscillation
    }

    // ------------------------------------------------ cleared-row beam (C2)

    /** How long the white-hot row sweep stays on screen. */
    const val BEAM_DURATION_MS = 260f

    /**
     * Width fraction of the sweeping beam: full at progress 0, shrinking to
     * zero with an ease-out quad — the line "burns away" from both edges
     * toward the middle.
     */
    fun beamWidth(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return (1f - p) * (1f - p)
    }

    /** Brightness of the beam: linear fade from 1 to 0. */
    fun beamAlpha(progress: Float): Float = 1f - progress.coerceIn(0f, 1f)

    // ------------------------------------------------ shockwave ring (C5)

    /** How long the tetris/perfect-clear shockwave expands. */
    const val RING_DURATION_MS = 450f

    /** Ease-out expansion from 0 to [maxRadius]: fast start, gentle landing. */
    fun ringRadius(progress: Float, maxRadius: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return maxRadius * (1f - (1f - p) * (1f - p))
    }

    /** Ring fades as it grows. */
    fun ringAlpha(progress: Float): Float = 0.8f * (1f - progress.coerceIn(0f, 1f))

    // ------------------------------------------------ score popup (C3)

    /** How long the floating "+points" popup lives. */
    const val POPUP_DURATION_MS = 700f

    /** Vertical rise in px with an ease-out cubic: jumps up, then floats. */
    fun popupRise(progress: Float, maxRisePx: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return maxRisePx * (1f - (1f - p).pow(3))
    }

    /** Scale pops in quickly (reaches full size at ~45% of life), no overshoot. */
    fun popupScale(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return 0.7f + 0.3f * (1f - (1f - min(p * 2.2f, 1f)).pow(2))
    }

    /** Fully opaque while rising, then fades out over the last 40% of life. */
    fun popupAlpha(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return if (p < 0.6f) 1f else 1f - (p - 0.6f) / 0.4f
    }

    // ------------------------------------------------ combo heat (C6)

    /**
     * 0..1 "heat" of the board frame from the combo chain: combo 5+ is
     * fully hot. Non-positive combos (no chain) are cold.
     */
    fun comboHeat(combo: Int): Float = (combo / 5f).coerceIn(0f, 1f)

    // ------------------------------------------------ ghost breathing (C7)

    /** One breathing cycle of the ghost piece. */
    const val GHOST_PERIOD_MS = 950f
    const val GHOST_ALPHA_MIN = 0.14f
    const val GHOST_ALPHA_MAX = 0.34f

    /**
     * Ghost piece alpha breathing between [GHOST_ALPHA_MIN] and
     * [GHOST_ALPHA_MAX]. With a frozen frame clock (reduced motion) this
     * collapses to the constant midpoint 0.24.
     */
    fun ghostAlpha(frameMs: Long): Float {
        val phase = (frameMs % GHOST_PERIOD_MS.toLong()) / GHOST_PERIOD_MS
        return 0.24f + 0.10f * sin(phase * 2f * PI.toFloat())
    }

    // --------------------------------------- hard-drop trail (v1.5.0, V3)

    /** How long the fading vertical hard-drop trail stays visible. */
    const val TRAIL_DURATION_MS = 200f

    /** Linear fade of the drop trail: fully bright at birth, gone at 200 ms. */
    fun trailAlpha(progress: Float): Float = (1f - progress.coerceIn(0f, 1f))

    // ------------------------------------------ danger pulse (v1.5.0, V4)

    /**
     * Visual danger intensity from the stack height. Mirrors the audio
     * DangerTracker hysteresis window (audio enters at 15 rows); the glow
     * then scales with how close the stack is to the top (20 rows).
     */
    const val DANGER_START_ROWS = 15
    const val DANGER_MAX_ROWS = 20

    fun dangerIntensity(stackHeightRows: Int): Float =
        ((stackHeightRows - DANGER_START_ROWS).toFloat() /
            (DANGER_MAX_ROWS - DANGER_START_ROWS)).coerceIn(0f, 1f)

    /** One pulse cycle of the danger glow. Freezes to a constant 0.5 when the frame clock is frozen (reduced motion). */
    const val DANGER_PULSE_PERIOD_MS = 800f

    fun dangerPulse(frameMs: Long): Float {
        val phase = (frameMs % DANGER_PULSE_PERIOD_MS.toLong()) / DANGER_PULSE_PERIOD_MS
        return 0.5f + 0.5f * sin(phase * 2f * PI.toFloat())
    }

    // --------------------------------------- combo chip sync (v1.6.0, V5)

    /**
     * Scale of the HUD combo chip, driven by the SAME combo-pitch ramp the
     * sound uses ([com.tetrisnewgeneration.game.audio.ComboPitch.rateFor]) —
     * sound and visuals stay in sync from one source of truth. A rate of
     * 1.0 (chain start) keeps the chip at rest; the max rate scales it to
     * 1.12. Non-positive rates are treated as rest.
     */
    const val COMBO_CHIP_MAX_SCALE = 1.12f

    fun comboChipScale(rate: Float?): Float {
        if (rate == null || rate <= 1f) return 1f
        val t = ((rate - 1f) / 0.5f).coerceIn(0f, 1f)
        return 1f + (COMBO_CHIP_MAX_SCALE - 1f) * t
    }

    // ------------------------------------------- impact dust (v1.5.0, V3)

    /**
     * Impact-dust particle budget for one piece lock: 6-10 particles at
     * MEDIUM, scaled by the effects-intensity factor and clamped so LOW
     * stays light and HIGH stays bounded.
     */
    fun dustCount(intensityFactor: Float): Int =
        (8f * intensityFactor).toInt().coerceIn(3, 12)
}

/**
 * v1.5.0 (V3): geometry of one hard-drop trail — the swept columns and the
 * board rows the piece travelled through. Pure data so the ViewModel can
 * compute it from the engine and the renderer can draw it.
 *
 * [cols] are board x columns occupied by the piece, [fromRow] the topmost
 * pre-drop row of the piece, [toRow] the bottom row after landing (both in
 * board coordinates including the 2 buffer rows).
 */
data class TrailSpec(
    val cols: List<Int>,
    val fromRow: Int,
    val toRow: Int,
    val typeIdx: Int
)
