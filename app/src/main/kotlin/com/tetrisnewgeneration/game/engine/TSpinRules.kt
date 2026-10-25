package com.tetrisnewgeneration.game.engine

/**
 * Pure T-spin classification (guideline corner rule), extracted from the engine
 * so it can be unit-tested directly on any board/piece position:
 *  - FULL: 3+ occupied corners with BOTH front corners filled (front = the side
 *          the T head points to, per SRS rotation state 0=up/1=right/2=down/3=left).
 *  - MINI: 3 corners occupied with only ONE front corner filled.
 *  - NONE: fewer than 3 occupied corners.
 * Out-of-board cells count as occupied (walls/floor).
 */
object TSpinRules {

    fun classify(board: Board, p: Piece): TSpinKind {
        if (p.type != PieceType.T) return TSpinKind.NONE
        val cx = p.x + 1
        val cy = p.y + 1
        fun filled(px: Int, py: Int) = !board.isInside(Point(px, py)) || board.cell(px, py) != null
        // Screen-space corners (y grows down).
        val tl = filled(cx - 1, cy - 1)
        val tr = filled(cx + 1, cy - 1)
        val bl = filled(cx - 1, cy + 1)
        val br = filled(cx + 1, cy + 1)
        val total = listOf(tl, tr, bl, br).count { it }
        if (total < 3) return TSpinKind.NONE
        val frontFilled = when (p.rotation and 3) {
            0 -> listOf(tl, tr)
            1 -> listOf(tr, br)
            2 -> listOf(bl, br)
            else -> listOf(tl, bl)
        }.count { it }
        return if (frontFilled == 2) TSpinKind.FULL else TSpinKind.MINI
    }
}
