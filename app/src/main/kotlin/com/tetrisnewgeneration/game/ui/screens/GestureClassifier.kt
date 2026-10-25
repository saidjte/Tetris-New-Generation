package com.tetrisnewgeneration.game.ui.screens

/**
 * Pure decision logic for the end of a touch gesture on the play area.
 * Free of Android/Compose types so it can be unit-tested on the JVM.
 *
 * The gesture layer feeds it the accumulated travel and the smoothed vertical
 * velocity measured at finger-release; it answers what the release means.
 */
object GestureClassifier {

    /** What should happen when the finger leaves the screen. */
    enum class EndGesture { NONE, HARD_DROP, HOLD }

    /**
     * @param totalDownPx   accumulated downward travel in px (>= 0)
     * @param totalUpPx     accumulated upward travel in px (>= 0)
     * @param velocityPxMs  smoothed vertical velocity at release (px/ms, > 0 = downward)
     * @param cellPx        reference unit: one tenth of the gesture surface width
     */
    fun classify(
        totalDownPx: Float,
        totalUpPx: Float,
        velocityPxMs: Float,
        cellPx: Float
    ): EndGesture {
        if (cellPx <= 0f) return EndGesture.NONE
        val downDominant = totalDownPx >= totalUpPx
        return when {
            // Fast downward flick over at least ~1.2 cells -> slam the piece down.
            downDominant &&
                totalDownPx >= cellPx * 1.2f &&
                velocityPxMs >= HARD_DROP_VELOCITY_PX_MS -> EndGesture.HARD_DROP

            // Deliberate upward drag over at least ~2 cells -> hold the piece.
            totalUpPx >= cellPx * 2.0f && totalUpPx > totalDownPx -> EndGesture.HOLD

            else -> EndGesture.NONE
        }
    }

    /** px/ms above which a downward release counts as a flick, not a soft drag. */
    const val HARD_DROP_VELOCITY_PX_MS = 2.0f
}
