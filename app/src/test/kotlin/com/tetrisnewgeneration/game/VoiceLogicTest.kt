package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.audio.DangerTracker
import com.tetrisnewgeneration.game.audio.VoiceCatalog
import com.tetrisnewgeneration.game.audio.VoiceEvent
import com.tetrisnewgeneration.game.audio.VoiceMilestones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.4.0 announcer logic — pure JVM rules that drive the voice engine.
 */
class VoiceLogicTest {

    // ---------- VoiceCatalog ----------

    @Test
    fun `every announced event has at least one clip`() {
        for (event in VoiceCatalog.announced) {
            assertTrue("no clips for $event", VoiceCatalog.clips(event) > 0)
        }
    }

    @Test
    fun `resource names are valid android raw identifiers`() {
        val regex = Regex("voice_[a-z0-9_]+_[0-9]+")
        for (event in VoiceEvent.entries) {
            val n = VoiceCatalog.clips(event)
            if (n == 0) continue
            for (i in 0 until n) {
                val name = VoiceCatalog.resourceName(event, i)
                assertTrue("bad name $name", regex.matches(name))
                assertTrue("index out of range in $name", !name.endsWith("_$n"))
            }
        }
    }

    @Test
    fun `resource name index is clamped`() {
        // Asking for a variant beyond the pool must clamp, not crash.
        assertEquals("voice_tetris_29", VoiceCatalog.resourceName(VoiceEvent.TETRIS, 29))
        assertEquals("voice_tetris_29", VoiceCatalog.resourceName(VoiceEvent.TETRIS, 99))
        assertEquals("voice_near_0", VoiceCatalog.resourceName(VoiceEvent.NEAR, 5))
    }

    @Test
    fun `catalog mirrors the 275 bundled clip files`() {
        assertEquals(275, VoiceCatalog.counts.values.sum())
    }

    // ---------- combo rule ----------

    @Test
    fun `combo voice mapping follows the silent-nine rule`() {
        assertNull(VoiceCatalog.comboVoice(-1))
        assertNull(VoiceCatalog.comboVoice(0))
        assertNull(VoiceCatalog.comboVoice(1))
        assertEquals(VoiceEvent.COMBO2, VoiceCatalog.comboVoice(2))
        assertEquals(VoiceEvent.COMBO8, VoiceCatalog.comboVoice(8))
        // combo 9 stays silent by design (no real take exists)
        assertNull(VoiceCatalog.comboVoice(9))
        assertEquals(VoiceEvent.COMBO10, VoiceCatalog.comboVoice(10))
        assertEquals(VoiceEvent.COMBO10, VoiceCatalog.comboVoice(14))
    }

    // ---------- VoiceMilestones ----------

    @Test
    fun `score milestones fire once each in order`() {
        val m = VoiceMilestones.forMode(null)
        assertNull(m.onScore(9_999))
        assertEquals(VoiceEvent.SCORE_10K, m.onScore(10_000))
        assertNull(m.onScore(10_500))
        // jumping ahead returns only the highest unfired threshold
        assertEquals(VoiceEvent.SCORE_100K, m.onScore(123_456))
        assertNull(m.onScore(200_000))
        assertEquals(VoiceEvent.SCORE_1M, m.onScore(1_000_000))
        assertNull(m.onScore(2_000_000))
    }

    @Test
    fun `skipped thresholds are consumed not delayed`() {
        // A big score jump crosses several milestones at once: announce the
        // highest, and never replay the skipped lower ones later.
        val m = VoiceMilestones.forMode(null)
        assertEquals(VoiceEvent.SCORE_250K, m.onScore(300_000))
        assertNull(m.onScore(400_000))
        assertEquals(VoiceEvent.SCORE_500K, m.onScore(500_000))
    }

    @Test
    fun `endless modes use the classic lines ladder`() {
        val m = VoiceMilestones.forMode(null)
        assertEquals(VoiceEvent.LINES_25, m.onLines(25))
        assertEquals(VoiceEvent.LINES_50, m.onLines(50))
        assertEquals(VoiceEvent.LINES_200, m.onLines(210))
        assertNull(m.onLines(250))
    }

    @Test
    fun `goal modes use the sprint ladder with head-room filter`() {
        // 40-line sprint: 10/20/30 fire, 40 is the goal itself (win moment).
        val m = VoiceMilestones.forMode(40)
        assertEquals(VoiceEvent.SPRINT10, m.onLines(10))
        assertEquals(VoiceEvent.SPRINT20, m.onLines(20))
        assertEquals(VoiceEvent.SPRINT30, m.onLines(30))
        assertNull(m.onLines(40))
        // 18-line speed mode: only the 10 milestone fits.
        val s = VoiceMilestones.forMode(18)
        assertEquals(VoiceEvent.SPRINT10, s.onLines(10))
        assertNull(s.onLines(18))
        // 8-line mode: too short for any milestone.
        val t = VoiceMilestones.forMode(8)
        assertNull(t.onLines(8))
    }

    // ---------- DangerTracker ----------

    @Test
    fun `danger enters at threshold and saves with hysteresis`() {
        val d = DangerTracker(enterRows = 15, exitRows = 13)
        assertNull(d.update(10))
        assertNull(d.update(14))
        assertEquals(VoiceEvent.DANGER, d.update(15))
        assertNull(d.update(14)) // between thresholds: no chatter
        assertEquals(VoiceEvent.SAVED, d.update(13))
        assertNull(d.update(5))
    }

    @Test
    fun `danger re-arms after save`() {
        val d = DangerTracker(enterRows = 15, exitRows = 13)
        assertEquals(VoiceEvent.DANGER, d.update(16))
        assertEquals(VoiceEvent.SAVED, d.update(10))
        assertEquals(VoiceEvent.DANGER, d.update(17))
    }

    @Test
    fun `danger never fires on empty board`() {
        val d = DangerTracker()
        assertNull(d.update(0))
        assertNull(d.update(12))
    }
}
