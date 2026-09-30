package ir.arminniromandi.timekar.domain.usecase

import ir.arminniromandi.timekar.domain.alarm.TaskReminderScheduler
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class AddTaskUseCase(
    private val repository: TaskRepository,
    private val reminderScheduler: TaskReminderScheduler
) {
    suspend operator fun invoke(task: TaskItem): Long {
        val id = repository.insertTask(task)

        // آی‌دی قبل از ذخیره صفر بود؛ از آی‌دی دیتابیس استفاده کن
        val savedTask = task.copy(id = id)

        if (savedTask.hasReminder) {
            reminderScheduler.schedule(savedTask)
        }

        return id
    }
}

class UpdateTaskUseCase(
    private val repository: TaskRepository,
    private val reminderScheduler: TaskReminderScheduler
) {
    suspend operator fun invoke(task: TaskItem) {
        repository.updateTask(task)

        // حتی اگر یادآوری خاموش شده باشد، آلارم قبلی باید لغو شود
        reminderScheduler.cancel(task.id)

        if (task.hasReminder) {
            reminderScheduler.schedule(task)
        }
    }
}

class DeleteTaskUseCase(
    private val repository: TaskRepository,
    private val reminderScheduler: TaskReminderScheduler
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteTaskById(id)
        reminderScheduler.cancel(id)
    }
}

class ToggleTaskCompleteUseCase(
    private val repository: TaskRepository,
    private val reminderScheduler: TaskReminderScheduler
) {
    suspend operator fun invoke(id: Long) {
        repository.toggleTaskComplete(id)

        val updatedTask = repository.getTaskById(id)

        reminderScheduler.cancel(id)

        // اگر تیک برداشته شده و یادآوری دارد، دوباره زمان‌بندی کن
        if (updatedTask != null && updatedTask.hasReminder) {
            reminderScheduler.schedule(updatedTask)
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
