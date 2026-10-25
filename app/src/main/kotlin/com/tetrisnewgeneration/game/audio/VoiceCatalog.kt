package com.tetrisnewgeneration.game.audio

/**
 * Pure catalog of announcer voice clips bundled in res/raw (v1.4.0).
 *
 * Assets: 275 real "Raffina" recordings provided by the game owner —
 * see CREDITS in the release notes. File name pattern:
 *   voice_<key>_<idx>.mp3   (idx = 0 until count-1)
 *
 * Design rules kept from the source pack:
 *  - combo9 has NO real take, so no event exists for it: combo 9 stays
 *    silent by design and never falls back to a synthetic voice.
 *  - modeselect / countdown clips are bundled for completeness but are
 *    not announced by the game engine.
 */
enum class VoiceEvent(val key: String) {
    START("start"), WARM("warm"),
    CLEAR1("clear1"), CLEAR2("clear2"), CLEAR3("clear3"),
    TETRIS("tetris"), TSPIN("tspin"), PC("pc"), B2B("b2b"),
    COMBO2("combo2"), COMBO3("combo3"), COMBO4("combo4"), COMBO5("combo5"),
    COMBO6("combo6"), COMBO7("combo7"), COMBO8("combo8"), COMBO10("combo10"),
    LEVELUP("levelup"), DANGER("danger"), SAVED("saved"),
    RECORD("record"), OVER("over"), WIN("win"), TIMEUP("timeup"),
    HURRY("hurry"), NEAR("near"), IDLE("idle"),
    PAUSE("pause"), RESUME("resume"),
    ZEN("zen"), ZEN_CALM("zen_calm"), SPEED("speed"),
    ULTRA("ultra"), ULTRA30("ultra30"), ULTRA60("ultra60"),
    SPRINT_INTRO("sprint_intro"),
    SPRINT10("sprint10"), SPRINT20("sprint20"),
    SPRINT30("sprint30"), SPRINT40("sprint40"),
    SCORE_10K("score10k"), SCORE_25K("score25k"), SCORE_50K("score50k"),
    SCORE_100K("score100k"), SCORE_250K("score250k"), SCORE_500K("score500k"),
    SCORE_1M("score1m"),
    LINES_25("lines25"), LINES_50("lines50"), LINES_100("lines100"),
    LINES_150("lines150"), LINES_200("lines200"),
    MODSELECT("modeselect"), COUNTDOWN("countdown")
}

/** Pure data + rules; no Android imports so it is unit-testable on the JVM. */
object VoiceCatalog {

    /** Number of bundled clips per event (mirrors res/raw contents). */
    val counts: Map<VoiceEvent, Int> = mapOf(
        VoiceEvent.START to 13, VoiceEvent.WARM to 6,
        VoiceEvent.CLEAR1 to 15, VoiceEvent.CLEAR2 to 15, VoiceEvent.CLEAR3 to 14,
        VoiceEvent.TETRIS to 30, VoiceEvent.TSPIN to 11, VoiceEvent.PC to 12,
        VoiceEvent.B2B to 13,
        VoiceEvent.COMBO2 to 1, VoiceEvent.COMBO3 to 1, VoiceEvent.COMBO4 to 1,
        VoiceEvent.COMBO5 to 3, VoiceEvent.COMBO6 to 1, VoiceEvent.COMBO7 to 2,
        VoiceEvent.COMBO8 to 1, VoiceEvent.COMBO10 to 2,
        VoiceEvent.LEVELUP to 12, VoiceEvent.DANGER to 8, VoiceEvent.SAVED to 4,
        VoiceEvent.RECORD to 9, VoiceEvent.OVER to 10, VoiceEvent.WIN to 8,
        VoiceEvent.TIMEUP to 2, VoiceEvent.HURRY to 4, VoiceEvent.NEAR to 1,
        VoiceEvent.IDLE to 12, VoiceEvent.PAUSE to 3, VoiceEvent.RESUME to 5,
        VoiceEvent.ZEN to 6, VoiceEvent.ZEN_CALM to 4, VoiceEvent.SPEED to 9,
        VoiceEvent.ULTRA to 2, VoiceEvent.ULTRA30 to 1, VoiceEvent.ULTRA60 to 1,
        VoiceEvent.SPRINT_INTRO to 2,
        VoiceEvent.SPRINT10 to 1, VoiceEvent.SPRINT20 to 1,
        VoiceEvent.SPRINT30 to 1, VoiceEvent.SPRINT40 to 1,
        VoiceEvent.SCORE_10K to 2, VoiceEvent.SCORE_25K to 2, VoiceEvent.SCORE_50K to 2,
        VoiceEvent.SCORE_100K to 1, VoiceEvent.SCORE_250K to 1,
        VoiceEvent.SCORE_500K to 1, VoiceEvent.SCORE_1M to 1,
        VoiceEvent.LINES_25 to 2, VoiceEvent.LINES_50 to 2, VoiceEvent.LINES_100 to 2,
        VoiceEvent.LINES_150 to 2, VoiceEvent.LINES_200 to 2,
        VoiceEvent.MODSELECT to 4, VoiceEvent.COUNTDOWN to 3
    )

    fun clips(event: VoiceEvent): Int = counts[event] ?: 0

    /** Raw resource name for [event] variant [idx] (0-based). */
    fun resourceName(event: VoiceEvent, idx: Int): String {
        val n = clips(event)
        val safe = if (n > 0) idx.coerceIn(0, n - 1) else 0
        return "voice_${event.key}_$safe"
    }

    /**
     * Voice to announce for a combo chain value (the engine's combo counter,
     * -1 = none). Combo 9 is intentionally silent (no real take exists);
     * combos below 2 are not announced; above 10 clamps to the combo-10 pool.
     */
    fun comboVoice(combo: Int): VoiceEvent? = when {
        combo < 2 -> null
        combo == 9 -> null // silent by design — never synthetic
        combo >= 10 -> VoiceEvent.COMBO10
        else -> VoiceEvent.entries.firstOrNull {
            it.key == "combo$combo"
        }
    }

    /** Events announced while playing; used by tests to keep the map honest. */
    val announced: Set<VoiceEvent> = counts.keys - setOf(
        VoiceEvent.MODSELECT, VoiceEvent.COUNTDOWN
    )
}

/**
 * One-shot milestone tracker: fires a voice event exactly once per threshold
 * crossing, per round. Pure JVM.
 */
class VoiceMilestones(
    scoreThresholds: List<Pair<Long, VoiceEvent>>,
    lineThresholds: List<Pair<Int, VoiceEvent>>
) {
    private val scoreFired = scoreThresholds.map { it.first }.toMutableSet()
    private val lineFired = lineThresholds.map { it.first }.toMutableSet()
    private val scoreMap = scoreThresholds.toMap()
    private val lineMap = lineThresholds.toMap()

    fun onScore(score: Long): VoiceEvent? {
        val hit = scoreMap.keys.filter { it <= score && it in scoreFired }
            .maxByOrNull { it } ?: return null
        // Consume every threshold crossed by this jump so lower ones never
        // fire late (score jumps can cross several milestones at once).
        scoreFired.removeAll { it <= score }
        return scoreMap[hit]
    }

    fun onLines(lines: Int): VoiceEvent? {
        val hit = lineMap.keys.filter { it <= lines && it in lineFired }
            .maxByOrNull { it } ?: return null
        lineFired.removeAll { it <= lines }
        return lineMap[hit]
    }

    companion object {
        /** Score milestones apply to every mode. */
        private val SCORES = listOf(
            10_000L to VoiceEvent.SCORE_10K,
            25_000L to VoiceEvent.SCORE_25K,
            50_000L to VoiceEvent.SCORE_50K,
            100_000L to VoiceEvent.SCORE_100K,
            250_000L to VoiceEvent.SCORE_250K,
            500_000L to VoiceEvent.SCORE_500K,
            1_000_000L to VoiceEvent.SCORE_1M
        )

        /**
         * Builds the milestone set for a mode. Short goal-line modes use the
         * sprint ladder (only thresholds that leave >= 5 lines of head-room,
         * the goal itself is celebrated by WIN instead); endless modes use
         * the classic lines ladder.
         */
        fun forMode(goalLines: Int?): VoiceMilestones {
            val lines = if (goalLines != null) {
                listOf(
                    10 to VoiceEvent.SPRINT10,
                    20 to VoiceEvent.SPRINT20,
                    30 to VoiceEvent.SPRINT30,
                    40 to VoiceEvent.SPRINT40
                ).filter { (n, _) -> n <= goalLines - 5 }
            } else {
                listOf(
                    25 to VoiceEvent.LINES_25,
                    50 to VoiceEvent.LINES_50,
                    100 to VoiceEvent.LINES_100,
                    150 to VoiceEvent.LINES_150,
                    200 to VoiceEvent.LINES_200
                )
            }
            return VoiceMilestones(SCORES, lines)
        }
    }
}

/**
 * Stack-height danger tracker with hysteresis: announces DANGER when the
 * stack reaches [enterRows], then SAVED once it drops back below [exitRows].
 * Pure JVM.
 */
class DangerTracker(
    private val enterRows: Int = 15,
    private val exitRows: Int = 13
) {
    private var inDanger = false

    /** @return DANGER / SAVED voice events on state transitions, else null. */
    fun update(stackHeightRows: Int): VoiceEvent? {
        if (!inDanger && stackHeightRows >= enterRows) {
            inDanger = true
            return VoiceEvent.DANGER
        }
        if (inDanger && stackHeightRows <= exitRows) {
            inDanger = false
            return VoiceEvent.SAVED
        }
        return null
    }
}
