package ir.arminniromandi.timekar.domain.repository

import ir.arminniromandi.timekar.domain.model.TaskItem
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<TaskItem>>
    fun getTasksForDate(epochDay: Long): Flow<List<TaskItem>>
    suspend fun getTaskById(id: Long): TaskItem?
    suspend fun getPendingTask(epochDay: Long): List<TaskItem>
    suspend fun getTasksWithReminder(): List<TaskItem>
    suspend fun insertTask(task: TaskItem): Long
    suspend fun updateTask(task: TaskItem)
    suspend fun deleteTask(task: TaskItem)
    suspend fun deleteTaskById(id: Long)
    suspend fun toggleTaskComplete(id: Long)
    suspend fun toggleSubtask(taskId: Long, subtaskId: String)
}
