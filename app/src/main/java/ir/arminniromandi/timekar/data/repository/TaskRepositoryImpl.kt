package ir.arminniromandi.timekar.data.repository

import ir.arminniromandi.timekar.data.local.dao.TaskDao
import ir.arminniromandi.timekar.data.local.entity.TaskEntity
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<TaskItem>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTasksForDate(epochDay: Long): Flow<List<TaskItem>> {
        return taskDao.getTasksForDate(epochDay).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: Long): TaskItem? {
        return taskDao.getTaskById(id)?.toDomain()
    }

    override suspend fun getPendingTask(epochDay: Long): List<TaskItem> {
        return taskDao.getPendingTasksFromDate(epochDay).map { it.toDomain() }
    }

    override suspend fun insertTask(task: TaskItem): Long {
        return taskDao.insertTask(TaskEntity.Companion.fromDomain(task))
    }

    override suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(TaskEntity.Companion.fromDomain(task))
    }

    override suspend fun deleteTask(task: TaskItem) {
        taskDao.deleteTask(TaskEntity.Companion.fromDomain(task))
    }

    override suspend fun deleteTaskById(id: Long) {
        taskDao.deleteTaskById(id)
    }

    override suspend fun toggleTaskComplete(id: Long) {
        val current = taskDao.getTaskById(id) ?: return
        val newCompleted = !current.isCompleted
        val updated = current.copy(
            isCompleted = newCompleted,
            completedAt = if (newCompleted) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)
    }

    override suspend fun toggleSubtask(taskId: Long, subtaskId: String) {
        val current = taskDao.getTaskById(taskId) ?: return
        val subtasks = TaskEntity.Companion.parseSubtasks(current.subtasksJson).map { subtask ->
            if (subtask.id == subtaskId) {
                subtask.copy(isCompleted = !subtask.isCompleted)
            } else {
                subtask
            }
        }
        val updated = current.copy(
            subtasksJson = TaskEntity.Companion.serializeSubtasks(subtasks)
        )
        taskDao.updateTask(updated)
    }
}
