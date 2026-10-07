package ir.arminniromandi.timekar.data.remote.mapper

import android.annotation.SuppressLint
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import ir.arminniromandi.timekar.data.remote.model.ChatCompletionRequest
import ir.arminniromandi.timekar.data.remote.model.ChatCompletionResponse
import ir.arminniromandi.timekar.data.remote.model.Message
import ir.arminniromandi.timekar.data.remote.model.ResponseFormat
import ir.arminniromandi.timekar.domain.model.Category
import ir.arminniromandi.timekar.domain.model.Priority
import ir.arminniromandi.timekar.domain.model.TaskItem

/**
 * Mapper for converting between LLM API models and Domain models
 * Following Single Responsibility Principle - handles only data transformation
 * Following Open/Closed Principle - can be extended without modification
 */
object TaskDtoMapper {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Convert LLM response to Domain model
     * Parse JSON string from response.content to TaskItem
     */
    fun ChatCompletionResponse.toDomain(): TaskItem? {
        return try {
            // Extract content from LLM response
            val content = this.choices.firstOrNull()?.message?.content ?: return null
            
            // Parse JSON string to TaskItem
            val adapter = moshi.adapter(TaskItem::class.java)
            adapter.fromJson(content)
        } catch (_: Exception) {
            // Log error in production
            null
        }
    }

    /**
     * Create ChatCompletionRequest from user prompt
     * This is used to send natural language to LLM for task creation
     */
    fun String.toCreateTaskRequest(
        model: String = "gpt-4o-mini"
    ): ChatCompletionRequest {
        // Get current date and time for context using Calendar (API 24+ compatible)
        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(java.util.Calendar.MINUTE)
        
        // Calculate epoch day (days since 1970-01-01)
        val millisSinceEpoch = calendar.timeInMillis
        val currentEpochDay = millisSinceEpoch / (24 * 60 * 60 * 1000)
        
        // Calculate current time in minutes from midnight
        val currentTimeMinute = currentHour * 60 + currentMinute
        
        // Format current date time
        val currentDateTime = String.format("%04d-%02d-%02d %02d:%02d", 
            currentYear, currentMonth, currentDay, currentHour, currentMinute)
        
        // Get day of week
        val dayOfWeekInt = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val dayOfWeek = when(dayOfWeekInt) {
            java.util.Calendar.SUNDAY -> "SUNDAY"
            java.util.Calendar.MONDAY -> "MONDAY"
            java.util.Calendar.TUESDAY -> "TUESDAY"
            java.util.Calendar.WEDNESDAY -> "WEDNESDAY"
            java.util.Calendar.THURSDAY -> "THURSDAY"
            java.util.Calendar.FRIDAY -> "FRIDAY"
            java.util.Calendar.SATURDAY -> "SATURDAY"
            else -> "UNKNOWN"
        }
        
        val systemPrompt = """You are a task management assistant.
            |CURRENT DATE AND TIME: $currentDateTime ($dayOfWeek)
            |CURRENT EPOCH DAY: $currentEpochDay
            |CURRENT TIME IN MINUTES: $currentTimeMinute
            |
            |Convert user input into a structured task JSON matching this format:
            |{
            |  "title": "string",
            |  "description": "string",
            |  "dateEpochDay": long,
            |  "startTimeMinute": int,
            |  "endTimeMinute": int,
            |  "isAllDay": boolean,
            |  "priority": "HIGH|MEDIUM|NORMAL|LOW",
            |  "category": "PRODUCT|PRODUCT_CORE|SPECS|ARCHITECTURE|ENG|DESIGN|FINANCE|PERSONAL|GENERAL",
            |  "locationOrDetails": "string",
            |  "reminderMin": int,
            |  "recurrenceText": "string",
            |  "subtasks": [{"title": "string", "isCompleted": false}],
            |  "iconType": "string"
            |}
            |
            |IMPORTANT TIME CONVERSION RULES:
            |- When user says "today", "tonight", use the CURRENT EPOCH DAY ($currentEpochDay)
            |- When user says "tomorrow", add 1 to current epoch day
            |- When user says "now" or "current time", use the CURRENT TIME IN MINUTES ($currentTimeMinute)
            |- Convert time to minutes from midnight (e.g., 14:30 = 14*60+30 = 870 minutes)
            |- If no specific time mentioned, use reasonable defaults based on context
            |- For "morning" use 9:00 (540 min), "afternoon" use 14:00 (840 min), "evening" use 18:00 (1080 min)
            |
            |Only return valid JSON, no additional text.""".trimMargin()
        
        return ChatCompletionRequest(
            model = model,
            messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = this)
            ),
            response_format = ResponseFormat(type = "json_object")
        )
    }

    /**
     * Convert Domain model to ChatCompletionRequest for task update/modification
     * Use this when you want LLM to modify an existing task
     */
    fun TaskItem.toUpdateRequest(
        userPrompt: String,
        model: String = "gpt-4o-mini"
    ): ChatCompletionRequest {
        val currentTaskJson = this.toJsonString()
        val systemPrompt = """You are a task management assistant.
            |Current task data:
            |$currentTaskJson
            |
            |User wants to modify this task. Apply the requested changes and return the updated task as JSON.
            |Only return valid JSON, no additional text.""".trimMargin()

        return ChatCompletionRequest(
            model = model,
            messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object")
        )
    }

    /**
     * Convert TaskItem to JSON string for sending to LLM
     */
    private fun TaskItem.toJsonString(): String {
        val adapter = moshi.adapter(TaskItem::class.java)
        return adapter.toJson(this)
    }

    /**
     * Parse Priority from string (case-insensitive)
     */
    private fun parsePriority(priority: String): Priority {
        return try {
            Priority.valueOf(priority.uppercase())
        } catch (e: IllegalArgumentException) {
            Priority.NORMAL
        }
    }

    /**
     * Parse Category from string (case-insensitive)
     */
    private fun parseCategory(category: String): Category {
        return try {
            Category.valueOf(category.uppercase())
        } catch (e: IllegalArgumentException) {
            Category.GENERAL
        }
    }
}
