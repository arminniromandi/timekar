package ir.arminniromandi.timekar.system.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.arminniromandi.timekar.framework.notification.TaskNotificationManager

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: return
        val desc = intent.getStringExtra(EXTRA_TASK_DESC).orEmpty()
        val reminderMin = intent.getIntExtra(EXTRA_REMINDER_MIN, 0)

        val message = when {
            reminderMin > 0 -> "تسک «$title» تا $reminderMin دقیقه دیگر شروع می‌شود."
            desc.isNotBlank() -> desc
            else -> "زمان شروع تسک فرا رسیده است."
        }

        val notificationManager = TaskNotificationManager(context)
        notificationManager.showReminder(taskId, title, message)
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESC = "extra_task_desc"
        const val EXTRA_REMINDER_MIN = "extra_reminder_min"
    }
}