package com.tetrisnewgeneration.game.engine

/**
 * Core tetromino geometry, independent of Android so it can be unit-tested directly.
 * Coordinate system: x grows to the right, y grows DOWNWARD.
 * Rotation states follow SRS: 0=spawn, 1=R (CW), 2=180, 3=L (CCW).
 */
enum class PieceType { I, O, T, S, Z, J, L }

data class Point(val x: Int, val y: Int) {
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)
}

data class Piece(val type: PieceType, val rotation: Int, val x: Int, val y: Int) {

    /** Absolute occupied cells for the current rotation state. */
    fun cells(): List<Point> = Tetrominoes.shape(type, rotation).map { Point(x + it.x, y + it.y) }

    fun moved(dx: Int, dy: Int) = copy(x = x + dx, y = y + dy)

    fun rotatedCw() = copy(rotation = (rotation + 1) % 4)

    fun rotatedCcw() = copy(rotation = (rotation + 3) % 4)
}

object Tetrominoes {

    /** Bounding box size used for rotation math (I and O rotate inside 4x4, others inside 3x3). */
    fun boxSize(type: PieceType): Int = if (type == PieceType.I || type == PieceType.O) 4 else 3

    /** Spawn-state cells inside the piece bounding box. */
    private val SPAWN: Map<PieceType, List<Point>> = mapOf(
        PieceType.I to listOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(3, 1)),
        PieceType.O to listOf(Point(1, 0), Point(2, 0), Point(1, 1), Point(2, 1)),
        PieceType.T to listOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(2, 1)),
        PieceType.S to listOf(Point(1, 0), Point(2, 0), Point(0, 1), Point(1, 1)),
        PieceType.Z to listOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(2, 1)),
        PieceType.J to listOf(Point(0, 0), Point(0, 1), Point(1, 1), Point(2, 1)),
        PieceType.L to listOf(Point(2, 0), Point(0, 1), Point(1, 1), Point(2, 1))
    )

    private val ALL: Map<PieceType, List<List<Point>>> = buildMap {
        for (type in PieceType.entries) {
            val n = boxSize(type)
            var cells = SPAWN.getValue(type)
            if (type == PieceType.O) {
                // SRS: the O piece does not change cells across rotations.
                val fixed = listOf(cells, cells, cells, cells)
                put(type, fixed)
                continue
            }
            val rotations = mutableListOf(cells)
            repeat(3) {
                cells = cells.map { (x, y) -> Point(n - 1 - y, x) }
                rotations.add(cells)
            }
            put(type, rotations)
        }
    }

    fun shape(type: PieceType, rotation: Int): List<Point> = ALL.getValue(type)[rotation and 3]

    fun spawn(type: PieceType, boardWidth: Int, spawnY: Int = 0): Piece {
        val n = boxSize(type)
        return Piece(type, 0, (boardWidth - n) / 2, spawnY)
    }

    // ---- SRS wall-kick tables -----------------------------------------------
    // Encoded with the classic convention where +y is UP. When applying a kick,
    // the engine converts it to screen space by negating y (see GameEngine.rotate).
    // States are named 0, R(1), 2, L(3).
    private val KICKS_JLSTZ: Map<Pair<Int, Int>, List<Point>> = mapOf(
        (0 to 1) to listOf(Point(0, 0), Point(-1, 0), Point(-1, 1), Point(0, -2), Point(-1, -2)),
        (1 to 0) to listOf(Point(0, 0), Point(1, 0), Point(1, -1), Point(0, 2), Point(1, 2)),
        (1 to 2) to listOf(Point(0, 0), Point(1, 0), Point(1, -1), Point(0, 2), Point(1, 2)),
        (2 to 1) to listOf(Point(0, 0), Point(-1, 0), Point(-1, 1), Point(0, -2), Point(-1, -2)),
        (2 to 3) to listOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(0, -2), Point(1, -2)),
        (3 to 2) to listOf(Point(0, 0), Point(-1, 0), Point(-1, -1), Point(0, 2), Point(-1, 2)),
        (3 to 0) to listOf(Point(0, 0), Point(-1, 0), Point(-1, -1), Point(0, 2), Point(-1, 2)),
        (0 to 3) to listOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(0, -2), Point(1, -2))
    )

    private val KICKS_I: Map<Pair<Int, Int>, List<Point>> = mapOf(
        (0 to 1) to listOf(Point(0, 0), Point(-2, 0), Point(1, 0), Point(-2, -1), Point(1, 2)),
        (1 to 0) to listOf(Point(0, 0), Point(2, 0), Point(-1, 0), Point(2, 1), Point(-1, -2)),
        (1 to 2) to listOf(Point(0, 0), Point(-1, 0), Point(2, 0), Point(-1, 2), Point(2, -1)),
        (2 to 1) to listOf(Point(0, 0), Point(1, 0), Point(-2, 0), Point(1, -2), Point(-2, 1)),
        (2 to 3) to listOf(Point(0, 0), Point(2, 0), Point(-1, 0), Point(2, 1), Point(-1, -2)),
        (3 to 2) to listOf(Point(0, 0), Point(-2, 0), Point(1, 0), Point(-2, -1), Point(1, 2)),
        (3 to 0) to listOf(Point(0, 0), Point(1, 0), Point(-2, 0), Point(1, -2), Point(-2, 1)),
        (0 to 3) to listOf(Point(0, 0), Point(-1, 0), Point(2, 0), Point(-1, 2), Point(2, -1))
    )

    fun kicks(type: PieceType, from: Int, to: Int): List<Point> {
        if (type == PieceType.O) return listOf(Point(0, 0))
        val table = if (type == PieceType.I) KICKS_I else KICKS_JLSTZ
        return table[from to to]
            ?: table[from to ((to % 4) + 4) % 4]
            ?: listOf(Point(0, 0))
    }
}
