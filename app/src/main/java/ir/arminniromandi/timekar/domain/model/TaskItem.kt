package ir.arminniromandi.timekar.domain.model

data class TaskItem(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateEpochDay: Long,
    val startTimeMinute: Int = -1,
    val endTimeMinute: Int = -1,
    val isAllDay: Boolean = false,
    val priority: Priority = Priority.NORMAL,
    val category: Category = Category.GENERAL,
    val locationOrDetails: String = "",
    val reminderText: String = "10 minutes prior via system banner",
    val recurrenceText: String = "Weekly on Thursday",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val subtasks: List<SubtaskItem> = emptyList(),
    val iconType: String = "none",
    val createdAt: Long = System.currentTimeMillis()
) {
    val completedSubtasksCount: Int
        get() = subtasks.count { it.isCompleted }

    val formattedTimeSlot: String
        get() {
            if (isAllDay || startTimeMinute < 0) return "All Day"
            val startH = String.format("%02d", startTimeMinute / 60)
            val startM = String.format("%02d", startTimeMinute % 60)
            if (endTimeMinute < 0) return "$startH:$startM"
            val endH = String.format("%02d", endTimeMinute / 60)
            val endM = String.format("%02d", endTimeMinute % 60)
            return "$startH:$startM – $endH:$endM"
        }
}
