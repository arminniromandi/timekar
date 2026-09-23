package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Category
import com.example.domain.model.Priority
import com.example.domain.model.SubtaskItem
import com.example.domain.model.TaskItem
import org.json.JSONArray
import org.json.JSONObject

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
    val reminderText: String = "10 minutes prior via system banner",
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
            priority = Priority.fromString(priority),
            category = Category.fromKey(category),
            locationOrDetails = locationOrDetails,
            reminderText = reminderText,
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
                reminderText = task.reminderText,
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
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
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
