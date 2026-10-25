package com.tetrisnewgeneration.game.engine

/**
 * T-spin classification per the guideline corner rule.
 * FULL  = both front corners (the side the T points to) filled + at least one back corner.
 * MINI  = 3 corners filled with only one front corner filled (both back corners + one front).
 */
enum class TSpinKind { NONE, MINI, FULL }
