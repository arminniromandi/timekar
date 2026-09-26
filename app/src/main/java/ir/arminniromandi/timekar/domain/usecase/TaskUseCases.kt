package ir.arminniromandi.timekar.domain.usecase

import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<TaskItem>> = repository.getAllTasks()
}

class GetTasksForDateUseCase(private val repository: TaskRepository) {
    operator fun invoke(epochDay: Long): Flow<List<TaskItem>> = repository.getTasksForDate(epochDay)
}

class AddTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: TaskItem): Long = repository.insertTask(task)
}

class UpdateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: TaskItem) = repository.updateTask(task)
}

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteTaskById(id)
}

class ToggleTaskCompleteUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(id: Long) = repository.toggleTaskComplete(id)
}

class ToggleSubtaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Long, subtaskId: String) = repository.toggleSubtask(taskId, subtaskId)
}
