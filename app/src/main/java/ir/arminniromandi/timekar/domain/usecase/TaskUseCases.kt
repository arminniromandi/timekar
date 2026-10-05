package ir.arminniromandi.timekar.domain.usecase

import ir.arminniromandi.timekar.domain.alarm.ReminderManager
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class AddTaskUseCase(
    private val repository: TaskRepository,
    private val reminderManager: ReminderManager
) {

    suspend operator fun invoke(task: TaskItem): Long {
        val id = repository.insertTask(task)
        val savedTask = task.copy(id = id)
        reminderManager.schedule(savedTask)
        return id
    }
}

class UpdateTaskUseCase(
    private val repository: TaskRepository,
    private val reminderManager: ReminderManager
) {

    suspend operator fun invoke(task: TaskItem) {
        repository.updateTask(task)
        reminderManager.reschedule(task)
    }
}

class DeleteTaskUseCase(
    private val repository: TaskRepository,
    private val reminderManager: ReminderManager
) {

    suspend operator fun invoke(id: Long) {
        repository.deleteTaskById(id)
        reminderManager.cancel(id)
    }
}

class ToggleTaskCompleteUseCase(
    private val repository: TaskRepository,
    private val reminderManager: ReminderManager
) {

    suspend operator fun invoke(id: Long) {
        repository.toggleTaskComplete(id)
        val updatedTask = repository.getTaskById(id)
        if (updatedTask != null) {
            reminderManager.reschedule(updatedTask)
        }
    }
}




class GetTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<TaskItem>> = repository.getAllTasks()
}

class GetTasksForDateUseCase(private val repository: TaskRepository) {
    operator fun invoke(epochDay: Long): Flow<List<TaskItem>> = repository.getTasksForDate(epochDay)
}

class GetPendingTaskUseCase(private val repository: TaskRepository){
    suspend operator fun invoke(epochDay: Long): List<TaskItem> = repository.getPendingTask(epochDay)
}


class ToggleSubtaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: Long, subtaskId: String) = repository.toggleSubtask(taskId, subtaskId)
}
