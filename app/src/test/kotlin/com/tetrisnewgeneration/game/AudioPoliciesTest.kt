package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.audio.ComboPitch
import com.tetrisnewgeneration.game.audio.DuckPolicy
import com.tetrisnewgeneration.game.audio.FocusPolicy
import com.tetrisnewgeneration.game.audio.MusicEngine
import com.tetrisnewgeneration.game.audio.MusicRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.6.0 (A2/A3/A4/A1) audio policies — pure JVM decision tables.
 */
class AudioPoliciesTest {

    // ---------- ComboPitch (A2) ----------

    @Test
    fun `combo pitch is silent below combo 2`() {
        assertNull(ComboPitch.rateFor(-1))
        assertNull(ComboPitch.rateFor(0))
        assertNull(ComboPitch.rateFor(1))
    }

    @Test
    fun `combo pitch starts at base rate on combo 2`() {
        assertEquals(ComboPitch.BASE_RATE, ComboPitch.rateFor(2)!!, 1e-6f)
    }

    @Test
    fun `combo pitch climbs monotonically and caps`() {
        var prev = ComboPitch.rateFor(2)!!
        for (combo in 3..40) {
            val rate = ComboPitch.rateFor(combo)!!
            assertTrue("not climbing at combo $combo", rate > prev || rate == ComboPitch.MAX_RATE)
            assertTrue("over cap at combo $combo", rate <= ComboPitch.MAX_RATE)
            prev = rate
        }
        assertEquals(ComboPitch.MAX_RATE, ComboPitch.rateFor(40)!!, 1e-6f)
    }

    // ---------- MusicRouter (A3) ----------

    @Test
    fun `zen always keeps its calm loop`() {
        assertEquals(
            MusicEngine.Track.ZEN,
            MusicRouter.trackFor(zenMode = true, level = 1, dangerActive = true)
        )
        assertEquals(
            MusicEngine.Track.ZEN,
            MusicRouter.trackFor(zenMode = true, level = 15, dangerActive = false)
        )
    }

    @Test
    fun `danger jumps low levels straight to the intense track`() {
        assertEquals(
            MusicEngine.Track.PLAY_INTENSE,
            MusicRouter.trackFor(zenMode = false, level = 2, dangerActive = true)
        )
    }

    @Test
    fun `level 8 and above play the intense track without danger`() {
        assertEquals(
            MusicEngine.Track.PLAY_INTENSE,
            MusicRouter.trackFor(zenMode = false, level = MusicRouter.INTENSE_LEVEL, dangerActive = false)
        )
        assertEquals(
            MusicEngine.Track.PLAY,
            MusicRouter.trackFor(zenMode = false, level = MusicRouter.INTENSE_LEVEL - 1, dangerActive = false)
        )
    }

    @Test
    fun `router is deterministic — no track thrash on stable state`() {
        val a = MusicRouter.trackFor(false, 3, false)
        val b = MusicRouter.trackFor(false, 3, false)
        assertEquals(a, b)
    }

    // ---------- DuckPolicy (A4) ----------

    @Test
    fun `duck only follows interrupting announcer moments`() {
        assertTrue(DuckPolicy.shouldDuck(interrupt = true))
        assertFalse(DuckPolicy.shouldDuck(interrupt = false))
    }

    @Test
    fun `duck window and factor stay audible but subordinate`() {
        assertTrue(DuckPolicy.DUCK_DURATION_MS in 500..2500)
        assertTrue(DuckPolicy.DUCK_FACTOR in 0.2f..0.8f)
    }

    // ---------- FocusPolicy (A1) ----------

    @Test
    fun `permanent loss pauses a playing round and ignores an idle one`() {
        assertEquals(
            FocusPolicy.Action.PAUSE_ROUND,
            FocusPolicy.actionFor(FocusPolicy.Loss.PERMANENT, roundPlaying = true)
        )
        assertEquals(
            FocusPolicy.Action.NONE,
            FocusPolicy.actionFor(FocusPolicy.Loss.PERMANENT, roundPlaying = false)
        )
    }

    @Test
    fun `transient losses silence music only`() {
        assertEquals(
            FocusPolicy.Action.PAUSE_MUSIC,
            FocusPolicy.actionFor(FocusPolicy.Loss.TRANSIENT, roundPlaying = true)
        )
        assertEquals(
            FocusPolicy.Action.PAUSE_MUSIC,
            FocusPolicy.actionFor(FocusPolicy.Loss.TRANSIENT_CAN_DUCK, roundPlaying = true)
        )
    }

    @Test
    fun `regained focus resumes music only for a live round`() {
        assertEquals(
            FocusPolicy.Action.RESUME_MUSIC,
            FocusPolicy.actionFor(FocusPolicy.Loss.GAINED, roundPlaying = true)
        )
        assertEquals(
            FocusPolicy.Action.NONE,
            FocusPolicy.actionFor(FocusPolicy.Loss.GAINED, roundPlaying = false)
        )
    }
}
