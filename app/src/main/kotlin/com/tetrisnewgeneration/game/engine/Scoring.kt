package com.tetrisnewgeneration.game.engine

/**
 * Scoring rules, kept separate from the engine so each mode can tune them.
 * Values follow modern guideline-style scoring.
 */
object Scoring {

    const val SOFT_DROP_POINTS_PER_CELL = 1L
    const val HARD_DROP_POINTS_PER_CELL = 2L

    /** Base points (before level multiplier) for a simultaneous line clear. */
    fun lineClearBase(lines: Int, tspin: Boolean): Long = when {
        tspin && lines == 0 -> 400L
        tspin && lines == 1 -> 800L
        tspin && lines == 2 -> 1200L
        tspin && lines >= 3 -> 1600L
        lines == 1 -> 100L
        lines == 2 -> 300L
        lines == 3 -> 500L
        lines >= 4 -> 800L
        else -> 0L
    }

    /**
     * @param combo consecutive-clear counter (0 = no combo active)
     * @return total points for one locking event, already multiplied by level
     */
    fun pointsForLock(
        lines: Int,
        level: Int,
        tspin: Boolean,
        backToBack: Boolean,
        combo: Int,
        perfectClear: Boolean
    ): Long {
        var points = lineClearBase(lines, tspin) * level
        val b2bEligible = lines > 0 && (lines >= 4 || tspin)
        if (b2bEligible && backToBack) points = (points * 3) / 2
        if (combo > 0) points += 50L * combo * level
        if (perfectClear && lines > 0) points += when (lines) {
            1 -> 800L
            2 -> 1200L
            3 -> 1800L
            else -> 2000L
        } * level
        return points
    }

    /**
     * Simplified guideline speed curve in milliseconds per gravity step:
     * time = (0.8 - ((level-1) * 0.007)) ^ (level-1) seconds.
     * Clamped so the game never becomes physically unplayable.
     */
    fun gravityIntervalMs(level: Int, scale: Float = 1f): Long {
        val lvl = level.coerceAtLeast(1)
        val seconds = Math.pow(0.8 - (lvl - 1) * 0.007, (lvl - 1).toDouble())
        val ms = seconds * 1000.0
        return (ms.coerceIn(10.0, 10_000.0) / scale.coerceAtLeast(0.01f).toDouble()).toLong()
    }
}
