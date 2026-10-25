package com.tetrisnewgeneration.game.progression

/**
 * Achievement definitions. Each achievement observes one or two stat counters;
 * unlocking is deterministic and rewarded exactly once.
 */
data class AchievementDef(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val target: Int,
    val rewardCoins: Int,
    val statKey: String
)

object AchievementDefs {
    const val ACH_FIRST_ROUND = "games_1"
    const val ACH_GAMES_25 = "games_25"
    const val ACH_LINES_100 = "lines_100"
    const val ACH_LINES_1000 = "lines_1000"
    const val ACH_TETRIS_1 = "tetris_1"
    const val ACH_TETRIS_10 = "tetris_10"
    const val ACH_COMBO_5 = "combo_5"
    const val ACH_LEVEL_10 = "level_10"
    const val ACH_SCORE_50K = "score_50k"
    const val ACH_HARD_DROPS_100 = "hard_drops_100"
    const val ACH_CHALLENGES_5 = "challenges_5"
    const val ACH_PUZZLES_4 = "puzzles_4"
    const val ACH_ADVENTURE_ALL = "adventure_10"
    const val ACH_THEMES_3 = "themes_3"
    const val ACH_ZEN_30M = "zen_30m"
    const val ACH_SPRINT_TIME = "time_attack_1"

    val all: List<AchievementDef> = listOf(
        AchievementDef(ACH_FIRST_ROUND, "الجولة الأولى", "First Round",
            "أكمل جولتك الأولى", "Finish your first round", 1, 20, "games_played"),
        AchievementDef(ACH_GAMES_25, "مثابر", "Persistent",
            "العب 25 جولة", "Play 25 rounds", 25, 60, "games_played"),
        AchievementDef(ACH_LINES_100, "مائة صف", "Century Lines",
            "امسح 100 صف إجمالًا", "Clear 100 total lines", 100, 50, "total_lines"),
        AchievementDef(ACH_LINES_1000, "ألف صف", "Thousand Lines",
            "امسح 1000 صف إجمالًا", "Clear 1,000 total lines", 1000, 150, "total_lines"),
        AchievementDef(ACH_TETRIS_1, "أول رباعية", "First Tetris",
            "حقق أول تصفية رباعية", "Perform your first Tetris", 1, 40, "tetrises"),
        AchievementDef(ACH_TETRIS_10, "قائد الرباعيات", "Tetris Commander",
            "حقق 10 تصفيات رباعية", "Perform 10 Tetrises", 10, 120, "tetrises"),
        AchievementDef(ACH_COMBO_5, "سلسلة نارية", "Fire Chain",
            "حقق سلسلة من 5 تصفيات متتالية", "Reach a 5-combo chain", 5, 80, "max_combo"),
        AchievementDef(ACH_LEVEL_10, "المستوى العاشر", "Level Ten",
            "صل إلى المستوى 10 في أي جولة", "Reach level 10 in any round", 10, 70, "max_level"),
        AchievementDef(ACH_SCORE_50K, "نجم النقاط", "Score Star",
            "اجمع 50000 نقطة إجمالًا", "Score 50,000 total points", 50000, 100, "total_score"),
        AchievementDef(ACH_HARD_DROPS_100, "إسقاطات حاسمة", "Sharp Drops",
            "نفّذ 100 إسقاط فوري", "Perform 100 hard drops", 100, 50, "hard_drops"),
        AchievementDef(ACH_CHALLENGES_5, "محترف التحديات", "Challenge Pro",
            "أكمل 5 تحديات", "Complete 5 challenges", 5, 90, "challenges_done"),
        AchievementDef(ACH_PUZZLES_4, "حلّال الألغاز", "Puzzle Solver",
            "حل 4 ألغاز", "Solve 4 puzzles", 4, 80, "puzzles_done"),
        AchievementDef(ACH_ADVENTURE_ALL, "فاتح المغامرة", "Adventure Conqueror",
            "أكمل مراحل المغامرة العشر", "Complete all 10 adventure stages", 10, 200, "adventure_done"),
        AchievementDef(ACH_THEMES_3, "جامع الثيمات", "Theme Collector",
            "امتلك 3 ثيمات أو أكثر", "Own 3 or more themes", 3, 60, "themes_3"),
        AchievementDef(ACH_ZEN_30M, "صفاء ذهني", "Clear Mind",
            "العب 30 دقيقة في نمط الاسترخاء", "Play 30 minutes of Zen", 1_800_000, 70, "zen_time_ms"),
        AchievementDef(ACH_SPRINT_TIME, "برق", "Lightning",
            "امسح 8 صفوف في 90 ثانية (تحدٍ)", "Clear 8 lines in 90s (challenge)", 1, 90, "sprint_done")
    )

    fun byId(id: String): AchievementDef? = all.firstOrNull { it.id == id }
}

/** Daily renewable tasks (offline, seeded by day-of-year). */
data class DailyTaskDef(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val target: Int,
    val rewardCoins: Int,
    val statKey: String
)

object DailyTaskDefs {
    val pool: List<DailyTaskDef> = listOf(
        DailyTaskDef("daily_games", "العب 3 جولات", "Play 3 rounds", 3, 25, "daily_games"),
        DailyTaskDef("daily_lines", "امسح 15 صفاً", "Clear 15 lines", 15, 25, "daily_lines"),
        DailyTaskDef("daily_score", "اجمع 3000 نقطة", "Score 3,000 points", 3000, 25, "daily_score"),
        DailyTaskDef("daily_tetris", "حقق تصفية رباعية", "Perform a Tetris", 1, 30, "daily_tetris"),
        DailyTaskDef("daily_hard", "نفّذ 20 إسقاطًا فوريًا", "Hard-drop 20 times", 20, 20, "daily_hard"),
        DailyTaskDef("daily_combo", "حقق سلسلة من 3", "Reach a 3-combo", 3, 30, "daily_combo")
    )
}
