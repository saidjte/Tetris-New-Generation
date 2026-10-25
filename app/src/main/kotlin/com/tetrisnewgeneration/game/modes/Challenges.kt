package com.tetrisnewgeneration.game.modes

/**
 * Offline challenges with concrete goals. Each one is a genuine engine target,
 * completing it is verified by the engine (or by round statistics) and rewards
 * are granted once.
 */
data class ChallengeDef(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val rewardCoins: Int,
    val rewardXp: Long,
    val config: ModeConfig
)

object Challenges {

    val all: List<ChallengeDef> = listOf(
        ChallengeDef(
            id = "ch_lines_10",
            titleAr = "خطّاط محترف",
            titleEn = "Line Veteran",
            descAr = "امسح 10 صفوف في جولة واحدة",
            descEn = "Clear 10 lines in a single round",
            rewardCoins = 40, rewardXp = 120,
            config = ModeConfig(id = "ch_lines_10", goalLines = 10, startLevel = 1)
        ),
        ChallengeDef(
            id = "ch_score_5k",
            titleAr = "جامع النقاط",
            titleEn = "Point Collector",
            descAr = "اجمع 5000 نقطة في جولة واحدة",
            descEn = "Reach 5,000 points in one round",
            rewardCoins = 40, rewardXp = 120,
            config = ModeConfig(id = "ch_score_5k", goalScore = 5000, startLevel = 1)
        ),
        ChallengeDef(
            id = "ch_tetris_1",
            titleAr = "ضربة رباعية",
            titleEn = "Quad Strike",
            descAr = "أزل 4 صفوف دفعة واحدة",
            descEn = "Clear 4 lines at once",
            rewardCoins = 60, rewardXp = 180,
            config = ModeConfig(id = "ch_tetris_1", goalTetrises = 1, startLevel = 1)
        ),
        ChallengeDef(
            id = "ch_tetris_3",
            titleAr = "سلسلة الرباعيات",
            titleEn = "Tetris Streak",
            descAr = "حقق 3 عمليات تصفية رباعية في جولة",
            descEn = "Perform 3 Tetris clears in one round",
            rewardCoins = 120, rewardXp = 350,
            config = ModeConfig(id = "ch_tetris_3", goalTetrises = 3, startLevel = 3)
        ),
        ChallengeDef(
            id = "ch_combo_4",
            titleAr = "متسلسل",
            titleEn = "Chain Master",
            descAr = "حقق سلسلة تصفية من 4 متتاليات",
            descEn = "Reach a 4-clear combo chain",
            rewardCoins = 80, rewardXp = 220,
            config = ModeConfig(id = "ch_combo_4", goalCombo = 4, startLevel = 1)
        ),
        ChallengeDef(
            id = "ch_speed_5",
            titleAr = "على المكوى",
            titleEn = "On the Edge",
            descAr = "امسح 10 صفوف بدءًا من المستوى 10",
            descEn = "Clear 10 lines starting at level 10",
            rewardCoins = 100, rewardXp = 280,
            config = ModeConfig(id = "ch_speed_5", goalLines = 10, startLevel = 10)
        ),
        ChallengeDef(
            id = "ch_sprint",
            titleAr = "عدّاء الدقيقة",
            titleEn = "Minute Sprint",
            descAr = "امسح 8 صفوف قبل انتهاء 90 ثانية",
            descEn = "Clear 8 lines within 90 seconds",
            rewardCoins = 90, rewardXp = 260,
            config = ModeConfig(
                id = "ch_sprint", goalLines = 8, timeLimitMs = 90_000L, startLevel = 2
            )
        ),
        ChallengeDef(
            id = "ch_score_20k",
            titleAr = "صائد الأرقام",
            titleEn = "Score Hunter",
            descAr = "اجمع 20000 نقطة في جولة واحدة",
            descEn = "Reach 20,000 points in one round",
            rewardCoins = 150, rewardXp = 420,
            config = ModeConfig(id = "ch_score_20k", goalScore = 20000, startLevel = 2)
        )
    )

    fun byId(id: String): ChallengeDef? = all.firstOrNull { it.id == id }
}
