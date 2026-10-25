package com.tetrisnewgeneration.game.engine

/**
 * Playfield grid. Contains [visibleHeight] playable rows plus extra buffer rows
 * at the top where new pieces spawn.
 */
class Board(
    val width: Int = DEFAULT_WIDTH,
    val height: Int = DEFAULT_HEIGHT,
    val visibleHeight: Int = DEFAULT_VISIBLE
) {
    init {
        require(height >= visibleHeight) { "height must be >= visibleHeight" }
    }

    private val grid: Array<Array<PieceType?>> = Array(height) { arrayOfNulls(width) }

    val rows: Int get() = height
    val cols: Int get() = width

    fun cell(x: Int, y: Int): PieceType? = grid[y][x]

    fun isInside(p: Point): Boolean = p.x in 0 until width && p.y in 0 until height

    fun isEmpty(p: Point): Boolean = grid[p.y][p.x] == null

    /** True when the piece would overlap the walls, the floor, or locked cells. */
    fun collides(piece: Piece): Boolean = piece.cells().any { !isInside(it) || grid[it.y][it.x] != null }

    /** Writes a single cell directly (used by save-restore and puzzle loading). */
    fun setCell(x: Int, y: Int, type: PieceType?) {
        if (x in 0 until width && y in 0 until height) grid[y][x] = type
    }

    /** Writes the piece cells into the grid (locking). */
    fun lock(piece: Piece) {
        for (c in piece.cells()) {
            if (c.y in 0 until height && c.x in 0 until width) grid[c.y][c.x] = piece.type
        }
    }

    /** Removes every full row and returns its indices ordered top to bottom. */
    fun clearFullRows(): List<Int> {
        val full = (0 until height).filter { y -> (0 until width).all { x -> grid[y][x] != null } }
        for (row in full) {
            for (y in row downTo 1) grid[y] = grid[y - 1]
            grid[0] = arrayOfNulls(width)
        }
        return full
    }

    /** True when any locked cell sits above the visible area (top-out condition). */
    fun hasCellAboveVisible(): Boolean {
        for (y in 0 until (height - visibleHeight)) {
            for (x in 0 until width) if (grid[y][x] != null) return true
        }
        return false
    }

    fun forEachCell(action: (x: Int, y: Int, type: PieceType) -> Unit) {
        for (y in 0 until height) for (x in 0 until width) {
            grid[y]?.get(x)?.let { action(x, y, it) }
        }
    }

    fun clearAll() {
        for (y in 0 until height) grid[y] = arrayOfNulls(width)
    }

    fun copyFrom(other: Board) {
        for (y in 0 until height) for (x in 0 until width) grid[y][x] = other.cell(x, y)
    }

    companion object {
        const val DEFAULT_WIDTH = 10
        const val DEFAULT_HEIGHT = 22
        const val DEFAULT_VISIBLE = 20
    }
}
