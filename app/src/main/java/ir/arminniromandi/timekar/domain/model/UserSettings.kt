package ir.arminniromandi.timekar.domain

enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean) {
    ENGLISH("en", "English", false),
    PERSIAN("fa", "فارسی", true)
}

enum class ThemeMode(val title: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

enum class AccentColor(val colorHex: Long, val displayNameEn: String, val displayNameFa: String) {
    EDITORIAL_BLUE(0xFF2563EB, "Editorial Blue", "آبی کلاسیک"),
    EMERALD_GREEN(0xFF059669, "Emerald Green", "سبز زمردی"),
    CRIMSON_RED(0xFFE11D48, "Crimson Red", "قرمز یاقوتی"),
    VIOLET_PURPLE(0xFF7C3AED, "Violet Purple", "بنفش روشن"),
    AMBER_GOLD(0xFFD97706, "Amber Gold", "کهربایی"),
    SLATE_GRAY(0xFF475569, "Slate Gray", "خاکستری تیره")
}

enum class StartDay(val titleEn: String, val titleFa: String) {
    MONDAY("Monday", "دوشنبه"),
    SATURDAY("Saturday", "شنبه"),
    SUNDAY("Sunday", "یکشنبه")
}

data class UserSettings(
    val language: AppLanguage = AppLanguage.PERSIAN,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val accentColor: AccentColor = AccentColor.EDITORIAL_BLUE,
    val startDay: StartDay = StartDay.SATURDAY,
    val taskRemindersEnabled: Boolean = true,
    val dailyBriefingEnabled: Boolean = true,
    val userName: String = "آرش محمدی",
    val userEmail: String = "arash@example.com",
    val isPro: Boolean = true
)
