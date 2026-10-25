package com.tetrisnewgeneration.game.modes

/**
 * Catalog of all play modes. Every mode is a real, playable configuration of the
 * shared engine — no decorative buttons.
 */
object Modes {

    const val CLASSIC = "classic"
    const val MARATHON = "marathon"
    const val QUICK_PLAY = "quick_play"
    const val TIME_ATTACK = "time_attack"
    const val SURVIVAL = "survival"
    const val SPEED = "speed"
    const val ZEN = "zen"
    const val PRACTICE = "practice"
    const val CHALLENGES = "challenges"
    const val PUZZLES = "puzzles"
    const val ADVENTURE = "adventure"

    /** Modes launched with a single id (special pickers exist for the rest). */
    val simple: List<ModeConfig> = listOf(
        ModeConfig(
            id = CLASSIC,
            startLevel = 1,
            linesPerLevel = 10
        ),
        ModeConfig(
            id = MARATHON,
            startLevel = 1,
            linesPerLevel = 10
        ),
        ModeConfig(
            id = QUICK_PLAY,
            timeLimitMs = 2 * 60_000L,
            startLevel = 1
        ),
        ModeConfig(
            id = SURVIVAL,
            startLevel = 1,
            survivalRampMs = 20_000L,
            linesPerLevel = 0 // level driven by time
        ),
        ModeConfig(
            id = SPEED,
            startLevel = 12,
            linesPerLevel = 10
        ),
        ModeConfig(
            id = ZEN,
            startLevel = 1,
            gravityScale = 0.35f,
            zenMode = true,
            linesPerLevel = 0 // keep it calm: constant gentle speed
        ),
        ModeConfig(
            id = PRACTICE,
            startLevel = 1,
            gravityScale = 0.5f,
            practiceMode = true,
            linesPerLevel = 0
        )
    )

    /** Speed Mode difficulty presets: (label level, config). */
    val speedPresets: List<Pair<Int, ModeConfig>> = listOf(
        8 to ModeConfig(id = SPEED, startLevel = 8),
        12 to ModeConfig(id = SPEED, startLevel = 12),
        18 to ModeConfig(id = SPEED, startLevel = 18)
    )

    /** Time Attack durations in minutes. */
    val timeAttackPresets: List<Pair<Int, ModeConfig>> = listOf(
        1 to ModeConfig(id = TIME_ATTACK, timeLimitMs = 60_000L, linesPerLevel = 0),
        3 to ModeConfig(id = TIME_ATTACK, timeLimitMs = 3 * 60_000L, linesPerLevel = 0),
        5 to ModeConfig(id = TIME_ATTACK, timeLimitMs = 5 * 60_000L, linesPerLevel = 0)
    )

    fun byId(id: String): ModeConfig? = simple.firstOrNull { it.id == id }
        ?: speedPresets.firstOrNull { it.second.id == id }?.second
        ?: timeAttackPresets.firstOrNull { it.second.id == id }?.second

    /** Appends a variant suffix to an id (e.g. time_attack_3). */
    fun timeAttack(minutes: Int): ModeConfig =
        timeAttackPresets.firstOrNull { it.first == minutes }?.second
            ?: timeAttackPresets[1].second

    fun speed(level: Int): ModeConfig =
        speedPresets.firstOrNull { it.first == level }?.second ?: speedPresets[1].second
}
