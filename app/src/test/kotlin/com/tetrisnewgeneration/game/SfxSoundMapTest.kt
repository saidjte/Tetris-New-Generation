package com.tetrisnewgeneration.game

import com.tetrisnewgeneration.game.audio.SfxEngine
import com.tetrisnewgeneration.game.audio.SfxSoundMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.4.0 SFX pack mapping — suffix names and fallback chains are pure data
 * with termination guarantees.
 */
class SfxSoundMapTest {

    @Test
    fun `every sound maps to a valid resource suffix`() {
        val regex = Regex("[a-z0-9_]+")
        for (sound in SfxEngine.Sound.entries) {
            val s = SfxSoundMap.suffix(sound)
            assertTrue("bad suffix $s for $sound", regex.matches(s))
            assertTrue("empty suffix for $sound", s.isNotEmpty())
        }
    }

    @Test
    fun `fallback chains always terminate`() {
        for (sound in SfxEngine.Sound.entries) {
            val chain = SfxSoundMap.chain(sound)
            assertTrue("chain too long for $sound", chain.size <= SfxEngine.Sound.entries.size)
            assertEquals("chain has duplicates (cycle?) for $sound",
                chain.size, chain.toSet().size)
        }
    }

    @Test
    fun `t99 pack specific names map to real files`() {
        assertEquals("hard", SfxSoundMap.suffix(SfxEngine.Sound.DROP))
        assertEquals("levelup", SfxSoundMap.suffix(SfxEngine.Sound.LEVEL_UP))
        assertEquals("over", SfxSoundMap.suffix(SfxEngine.Sound.ROUND_OVER))
        assertEquals("tick", SfxSoundMap.suffix(SfxEngine.Sound.COUNTDOWN))
        assertEquals("clear2", SfxSoundMap.suffix(SfxEngine.Sound.CLEAR_DOUBLE))
        assertEquals("tspin3", SfxSoundMap.suffix(SfxEngine.Sound.TSPIN_TRIPLE))
        assertEquals("tetris", SfxSoundMap.suffix(SfxEngine.Sound.TETRIS))
        assertEquals("hold", SfxSoundMap.suffix(SfxEngine.Sound.HOLD))
    }

    @Test
    fun `old packs fall back to base events`() {
        assertEquals(SfxEngine.Sound.CLEAR, SfxSoundMap.fallback(SfxEngine.Sound.TETRIS))
        assertEquals(SfxEngine.Sound.CLEAR, SfxSoundMap.fallback(SfxEngine.Sound.TSPIN))
        assertEquals(SfxEngine.Sound.TSPIN, SfxSoundMap.fallback(SfxEngine.Sound.TSPIN_TRIPLE))
        assertEquals(SfxEngine.Sound.CLICK, SfxSoundMap.fallback(SfxEngine.Sound.HOLD))
        assertEquals(SfxEngine.Sound.WIN, SfxSoundMap.fallback(SfxEngine.Sound.RECORD))
        assertEquals(null, SfxSoundMap.fallback(SfxEngine.Sound.MOVE))
        assertEquals(null, SfxSoundMap.fallback(SfxEngine.Sound.CLEAR))
    }
}
