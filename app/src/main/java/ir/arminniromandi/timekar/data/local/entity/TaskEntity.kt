package ir.arminniromandi.timekar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.arminniromandi.timekar.domain.model.Category
import ir.arminniromandi.timekar.domain.model.Priority
import ir.arminniromandi.timekar.domain.model.SubtaskItem
import ir.arminniromandi.timekar.domain.model.TaskItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateEpochDay: Long,
    val startTimeMinute: Int = -1,
    val endTimeMinute: Int = -1,
    val isAllDay: Boolean = false,
    val priority: String = "NORMAL",
    val category: String = "General",
    val locationOrDetails: String = "",
    val reminderMin: Int = 10,
    val recurrenceText: String = "Weekly on Thursday",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val subtasksJson: String = "[]",
    val iconType: String = "none",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): TaskItem {
        return TaskItem(
            id = id,
            title = title,
            description = description,
            dateEpochDay = dateEpochDay,
            startTimeMinute = startTimeMinute,
            endTimeMinute = endTimeMinute,
            isAllDay = isAllDay,
            priority = Priority.Companion.fromString(priority),
            category = Category.fromKey(category),
            locationOrDetails = locationOrDetails,
            reminderMin = reminderMin,
            recurrenceText = recurrenceText,
            isCompleted = isCompleted,
            completedAt = completedAt,
            subtasks = parseSubtasks(subtasksJson),
            iconType = iconType,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(task: TaskItem): TaskEntity {
            return TaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                dateEpochDay = task.dateEpochDay,
                startTimeMinute = task.startTimeMinute,
                endTimeMinute = task.endTimeMinute,
                isAllDay = task.isAllDay,
                priority = task.priority.name,
                category = task.category.key,
                locationOrDetails = task.locationOrDetails,
                reminderMin = task.reminderMin,
                recurrenceText = task.recurrenceText,
                isCompleted = task.isCompleted,
                completedAt = task.completedAt,
                subtasksJson = serializeSubtasks(task.subtasks),
                iconType = task.iconType,
                createdAt = task.createdAt
            )
        }

        fun parseSubtasks(jsonString: String): List<SubtaskItem> {
            if (jsonString.isBlank() || jsonString == "[]") return emptyList()
            return try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<SubtaskItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SubtaskItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.optString("title", ""),
                            isCompleted = obj.optBoolean("isCompleted", false)
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun serializeSubtasks(subtasks: List<SubtaskItem>): String {
            val array = JSONArray()
            for (item in subtasks) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("title", item.title)
                obj.put("isCompleted", item.isCompleted)
                array.put(obj)
            }
            return array.toString()
        }
    }
}