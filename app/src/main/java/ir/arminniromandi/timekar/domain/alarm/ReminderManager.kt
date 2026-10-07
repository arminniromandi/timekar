package ir.arminniromandi.timekar.domain.alarm

import android.os.Build
import androidx.annotation.RequiresApi
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.domain.repository.SettingsRepository
import ir.arminniromandi.timekar.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ReminderManager(
    private val scheduler: TaskReminderScheduler,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend fun schedule(task: TaskItem) {
        if (task.reminderMin < 0) return
        if (task.isCompleted) return
        if (task.startTimeMinute < 0) return

        val settings = settingsRepository
            .getSettings()
            .first()

        if (!settings.taskRemindersEnabled) return

        scheduler.schedule(task)
    }

    suspend fun reschedule(task: TaskItem) {
        scheduler.cancel(task.id)
        schedule(task)
    }

    suspend fun cancel(taskId: Long) {
        scheduler.cancel(taskId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun onReminderSettingChanged(enabled: Boolean) {
        if (!enabled) {
            taskRepository
                .getTasksWithReminder()
                .forEach { task ->
                    scheduler.cancel(task.id)
                }
            return
        }

        val todayEpochDay = LocalDate
            .now()
            .toEpochDay()

        val tasks = taskRepository
            .getPendingTask(todayEpochDay)

        tasks.forEach { task ->
            schedule(task)
        }
    }
}
