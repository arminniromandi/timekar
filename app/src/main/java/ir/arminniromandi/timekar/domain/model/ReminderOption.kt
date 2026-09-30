package ir.arminniromandi.timekar.domain.model




enum class ReminderOption(val minutes: Int) {
    TEN_MINUTES(10),
    FIFTEEN_MINUTES(15),
    THIRTY_MINUTES(30),
    ONE_HOUR(60),
    AT_TIME(0),
    NONE(-1);

    fun getLabel(isPersian: Boolean): String {
        return if (isPersian) {
            when (this) {
                TEN_MINUTES -> "۱۰ دقیقه قبل از طریق بنر سیستم"
                FIFTEEN_MINUTES -> "15 دقیقه قبل از طریق بنر سیستم"
                THIRTY_MINUTES -> "۳۰ دقیقه قبل"
                ONE_HOUR -> "۱ ساعت قبل"
                AT_TIME -> "در زمان رویداد"
                NONE -> "هیچ"
            }
        } else {
            when (this) {
                TEN_MINUTES -> "10 minutes prior via system banner"
                FIFTEEN_MINUTES -> "15 minutes prior via system banner"
                THIRTY_MINUTES -> "30 minutes prior"
                ONE_HOUR -> "1 hour prior"
                AT_TIME -> "At time of event"
                NONE -> "None"
            }
        }
    }

    companion object {
        /**
         * تطبیق رشته موجود در دیتابیس با یکی از گزینه‌های Enum
         */
        fun fromText(text: String?): ReminderOption {
            if (text == null) return TEN_MINUTES
            val lower = text.lowercase()
            return when {
                lower.contains("none") || lower.contains("هیچ") -> NONE
                lower.contains("at time") || lower.contains("زمان رویداد") -> AT_TIME
                lower.contains("30") || lower.contains("۳۰") -> THIRTY_MINUTES
                lower.contains("1 hour") || lower.contains("۱ ساعت") -> ONE_HOUR
                lower.contains("10") || lower.contains("۱۰") -> TEN_MINUTES
                lower.contains("15") || lower.contains("15") -> TEN_MINUTES
                else -> TEN_MINUTES
            }
        }

        fun fromMinutes(minutes: Int?): ReminderOption {
            return entries.firstOrNull { it.minutes == minutes } ?: TEN_MINUTES
        }
    }
}