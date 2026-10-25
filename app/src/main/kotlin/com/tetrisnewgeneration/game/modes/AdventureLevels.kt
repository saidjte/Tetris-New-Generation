package com.tetrisnewgeneration.game.modes

/**
 * Adventure path: a sequence of stages, each with its own goal, difficulty,
 * theme hint and reward. Progress is stored locally.
 */
data class AdventureLevel(
    val id: Int,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val themeHint: String,
    val rewardCoins: Int,
    val rewardXp: Long,
    val config: ModeConfig
)

object AdventureLevels {

    val all: List<AdventureLevel> = listOf(
        AdventureLevel(
            1, "أولى الخطوات", "First Steps",
            "امسح 5 صفوف لتتعود على التحكم", "Clear 5 lines to learn the controls",
            "ocean", 30, 80,
            ModeConfig(id = "adv_1", goalLines = 5, startLevel = 1, linesPerLevel = 0)
        ),
        AdventureLevel(
            2, "حقول المينيمال", "Minimal Fields",
            "اجمع 1500 نقطة في رحلة هادئة", "Score 1,500 points on a calm ride",
            "minimal", 35, 90,
            ModeConfig(id = "adv_2", goalScore = 1500, startLevel = 1, linesPerLevel = 0)
        ),
        AdventureLevel(
            3, "شلال النيون", "Neon Cascade",
            "امسح 12 صفاً بين أضواء النيون", "Clear 12 lines under neon lights",
            "neon", 40, 110,
            ModeConfig(id = "adv_3", goalLines = 12, startLevel = 2, linesPerLevel = 0)
        ),
        AdventureLevel(
            4, "مقبرة القطع", "Piece Graveyard",
            "امسح 4 صفوف دفعة واحدة", "Clear 4 lines at once",
            "retro", 50, 130,
            ModeConfig(id = "adv_4", goalTetrises = 1, startLevel = 2, linesPerLevel = 0)
        ),
        AdventureLevel(
            5, "سديم الكريستال", "Crystal Nebula",
            "حقق سلسلة تصفية من 3", "Reach a 3-combo chain",
            "crystal", 55, 150,
            ModeConfig(id = "adv_5", goalCombo = 3, startLevel = 2, linesPerLevel = 0)
        ),
        AdventureLevel(
            6, "غابة الطبيعة", "Nature Grove",
            "امسح 18 صفاً قبل أن يثقل الهواء", "Clear 18 lines before gravity wins",
            "nature", 60, 170,
            ModeConfig(id = "adv_6", goalLines = 18, startLevel = 3, linesPerLevel = 0)
        ),
        AdventureLevel(
            7, "حقول الحمم", "Lava Fields",
            "اجمع 8000 نقطة وسط الحرارة", "Score 8,000 points in the heat",
            "lava", 70, 200,
            ModeConfig(id = "adv_7", goalScore = 8000, startLevel = 4, linesPerLevel = 0)
        ),
        AdventureLevel(
            8, "مجرّة الأنوار", "Starlit Galaxy",
            "صمد 90 ثانية بسرعة عالية", "Survive 90 seconds at high speed",
            "galaxy", 80, 230,
            ModeConfig(id = "adv_8", timeLimitMs = 90_000L, startLevel = 8, linesPerLevel = 0)
        ),
        AdventureLevel(
            9, "الشفق الذهبي", "Golden Aurora",
            "حقق تصفيتين رباعيتين", "Perform 2 Tetris clears",
            "golden", 90, 260,
            ModeConfig(id = "adv_9", goalTetrises = 2, startLevel = 5, linesPerLevel = 0)
        ),
        AdventureLevel(
            10, "قمة السرعة", "Summit of Speed",
            "امسح 25 صفاً بدءًا من المستوى 10", "Clear 25 lines starting at level 10",
            "monochrome", 150, 400,
            ModeConfig(id = "adv_10", goalLines = 25, startLevel = 10, linesPerLevel = 0)
        ),
        AdventureLevel(
            11, "أطلال القمامة", "Ruins of Garbage",
            "نظّف الأنقاض ثم امسح 10 صفوف", "Clean the ruins, then clear 10 lines",
            "aurora", 110, 300,
            ModeConfig(
                id = "adv_11",
                goalLines = 10, startLevel = 3, linesPerLevel = 0,
                initialBoard = listOf(
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "..........",
                    "SZJLTTZJLS",
                    "JLSZTZLSZJ",
                    "TZJLSSZJLT",
                    "LZTJJSZLZS"
                )
            )
        ),
        AdventureLevel(
            12, "عاصفة الثواني", "Storm of Seconds",
            "اجمع 5000 نقطة قبل انتهاء دقيقة واحدة", "Score 5,000 before one minute ends",
            "crystal", 120, 330,
            ModeConfig(id = "adv_12", goalScore = 5000, timeLimitMs = 60_000L, startLevel = 5, linesPerLevel = 0)
        ),
        AdventureLevel(
            13, "بقاء العمالقة", "Survival of Giants",
            "انجُ من التصعيد وامسح 20 صفاً", "Survive the ramp and clear 20 lines",
            "lava", 130, 360,
            ModeConfig(id = "adv_13", goalLines = 20, survivalRampMs = 18_000L, startLevel = 4, linesPerLevel = 0)
        ),
        AdventureLevel(
            14, "قلعة الكومبو", "Combo Castle",
            "ابنِ سلسلة كومبو من 4", "Build a 4-combo chain",
            "galaxy", 140, 390,
            ModeConfig(id = "adv_14", goalCombo = 4, startLevel = 3, linesPerLevel = 0)
        ),
        AdventureLevel(
            15, "الخاتمة الذهبية", "Golden Finale",
            "اجمع 20000 نقطة من المستوى 8", "Score 20,000 starting at level 8",
            "golden", 200, 550,
            ModeConfig(id = "adv_15", goalScore = 20_000, startLevel = 8, linesPerLevel = 10)
        )
    )

    fun byId(id: Int): AdventureLevel? = all.firstOrNull { it.id == id }
}
