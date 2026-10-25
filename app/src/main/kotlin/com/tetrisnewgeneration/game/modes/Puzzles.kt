package com.tetrisnewgeneration.game.modes

import com.tetrisnewgeneration.game.engine.PieceType

/**
 * Handcrafted puzzle stages: a pre-filled board, a limited piece list, and a goal.
 * Boards are authored as 10-char rows: '.' empty, letter = locked piece type.
 */
data class PuzzleDef(
    val id: Int,
    val titleAr: String,
    val titleEn: String,
    val boardRows: List<String>,
    val pieces: List<PieceType>,
    val goalLines: Int
) {
    fun toConfig(): ModeConfig = ModeConfig(
        id = "puzzle_$id",
        initialBoard = boardRows,
        fixedPieces = pieces,
        goalLines = goalLines,
        moveLimit = pieces.size,
        allowHold = false
    )
}

object Puzzles {

    val all: List<PuzzleDef> = listOf(
        PuzzleDef(
            id = 1,
            titleAr = "صف واحد مبتدئ",
            titleEn = "Rookie Single",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "JJ...JJJJ.",
                "JJLLLJJJLL",
                "JJILIIJJLL"
            ),
            pieces = listOf(PieceType.I),
            goalLines = 1
        ),
        PuzzleDef(
            id = 2,
            titleAr = "ثنائية مرتّبة",
            titleEn = "Tidy Double",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "JJ..OJJLL.",
                "JJJOOJJLLL",
                "JJJOOJJLLL"
            ),
            pieces = listOf(PieceType.O, PieceType.I),
            goalLines = 2
        ),
        PuzzleDef(
            id = 3,
            titleAr = "فخ العمود الأوسط",
            titleEn = "Middle Trap",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "....LL....",
                "...LZZL...",
                "..LJZZJL..",
                ".LJJTTJJL.",
                "LJJTTTTJJL",
                "LJJITTIJJL"
            ),
            pieces = listOf(PieceType.I, PieceType.T),
            goalLines = 2
        ),
        PuzzleDef(
            id = 4,
            titleAr = "سكينة القاع",
            titleEn = "Bottom Skates",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "JL....SZZS",
                "JL....SSZZ",
                "JLZZZZSSII"
            ),
            pieces = listOf(PieceType.Z, PieceType.I),
            goalLines = 2
        ),
        PuzzleDef(
            id = 5,
            titleAr = "ترتيب قطري",
            titleEn = "Diagonal Order",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                ".........L",
                "......JJLL",
                "....JJJZLL",
                "..JJJSZZL.",
                "JJJJJSZZS.",
                "JJJJIJZZSS"
            ),
            pieces = listOf(PieceType.J, PieceType.I),
            goalLines = 2
        ),
        PuzzleDef(
            id = 6,
            titleAr = "بوابة التايتريس",
            titleEn = "Tetris Gate",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "......JJLL",
                ".....JJZLL",
                "..Z.JJSZL.",
                ".ZZJJJJSZZ"
            ),
            pieces = listOf(PieceType.J, PieceType.I),
            goalLines = 4
        ),
        PuzzleDef(
            id = 7,
            titleAr = "سلم مزدوج",
            titleEn = "Twin Ladder",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "....O.....",
                "...OOZ....",
                "..LOOZZI..",
                ".LLZZZZII.",
                "LLLSSZZIIJ",
                "LLSSSSIIIJ"
            ),
            pieces = listOf(PieceType.O, PieceType.L, PieceType.I),
            goalLines = 3
        ),
        PuzzleDef(
            id = 8,
            titleAr = "التحفة الأخيرة",
            titleEn = "Final Masterpiece",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                ".......L..",
                ".....TTL..",
                "....JTTLI.",
                "...JJTTII.",
                "..JJJZZI..",
                ".JJJSZZZS."
            ),
            pieces = listOf(PieceType.T, PieceType.L, PieceType.I),
            goalLines = 3
        ),
        // Puzzles 9-12 (v1.1.0): designs verified by hand — every gap forms an
        // unobstructed vertical descent corridor for its fixed piece.
        PuzzleDef(
            id = 9,
            titleAr = "الإبرة الرأسية",
            titleEn = "Vertical Needle",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                ".ZJSLLJTZI",
                ".ZJSLLJTZI",
                ".ZJSLLJTZI",
                ".ZJSLLJTZI"
            ),
            pieces = listOf(PieceType.I),
            goalLines = 4
        ),
        PuzzleDef(
            id = 10,
            titleAr = "عمود اليمين",
            titleEn = "Right Column",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "SJSLZT.IJL",
                "SJSLZ..IJL",
                "SJSLZT.IJI"
            ),
            pieces = listOf(PieceType.T),
            goalLines = 3
        ),
        PuzzleDef(
            id = 11,
            titleAr = "عمود اليسار",
            titleEn = "Left Column",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "SJS..TZJIL",
                "SJSL.TZJIL",
                "ZJSL.TZJII"
            ),
            pieces = listOf(PieceType.L),
            goalLines = 3
        ),
        PuzzleDef(
            id = 12,
            titleAr = "المهندس",
            titleEn = "The Engineer",
            boardRows = listOf(
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "SJSLZ....Z",
                "SJ..SLZTIZ",
                "SJ..SLZTIZ"
            ),
            pieces = listOf(PieceType.O, PieceType.I),
            goalLines = 3
        )
    )
}
