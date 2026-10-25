package com.tetrisnewgeneration.game.ui.fx

/**
 * One pulse of the round-start countdown. [label] is a language-neutral key:
 * numerals render as-is, [CountdownSteps.GO_LABEL] is mapped to a localized
 * string by the UI layer.
 */
data class CountdownStep(val label: String, val durationMs: Long)

/**
 * Pure structure of the 3-2-1-GO countdown that precedes every fresh round.
 * A resumed session never runs a countdown (documented behaviour, tested).
 */
object CountdownSteps {
    const val STEP_MS = 550L
    const val GO_MS = 650L
    const val GO_LABEL = "GO"

    /** Three shrinking number pulses, then the "go" flash. */
    fun build(): List<CountdownStep> = listOf(
        CountdownStep("3", STEP_MS),
        CountdownStep("2", STEP_MS),
        CountdownStep("1", STEP_MS),
        CountdownStep(GO_LABEL, GO_MS)
    )

    fun totalMs(): Long = STEP_MS * 3 + GO_MS
}
