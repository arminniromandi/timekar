package ir.arminniromandi.timekar.domain.alarm

import ir.arminniromandi.timekar.domain.model.TaskItem

interface TaskReminderScheduler {
    fun schedule(task: TaskItem)
    fun cancel(taskId: Long)
}