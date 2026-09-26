package ir.arminniromandi.timekar.domain.model

enum class Priority(val level: Int, val defaultName: String, val colorHex: Long) {
    HIGH(1, "High Priority", 0xFFBA1A1A),
    MEDIUM(2, "Medium Priority", 0xFFEAB308),
    NORMAL(3, "Normal Priority", 0xFF2563EB),
    LOW(4, "Low / None", 0xFF737686);

    companion object {
        fun fromString(value: String): Priority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NORMAL
        }
    }
}
