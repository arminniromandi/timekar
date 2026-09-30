package ir.arminniromandi.timekar.framework.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import ir.arminniromandi.timekar.domain.alarm.TaskReminderScheduler
import ir.arminniromandi.timekar.domain.model.TaskItem
import ir.arminniromandi.timekar.system.receiver.TaskAlarmReceiver
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AndroidTaskReminderScheduler(
    private val context: Context
) : TaskReminderScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun schedule(task: TaskItem) {
        // اگر تسک یادآوری ندارد، کنسل شده یا زمان ندارد رد شو
        if (!task.hasReminder) return

        val triggerAtMillis = calculateTriggerMillis(
            dateEpochDay = task.dateEpochDay,
            startTimeMinute = task.startTimeMinute,
            reminderMin = task.reminderMin
        )

        // اگر زمان اجرای آلارم قبل از لحظه فعلی باشد، زمان‌بندی نکن
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val pendingIntent = createPendingIntent(task)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                return
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override fun cancel(taskId: Long) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateTriggerMillis(dateEpochDay: Long, startTimeMinute: Int, reminderMin: Int): Long {
        val date = LocalDate.ofEpochDay(dateEpochDay)
        val time = LocalTime.of(startTimeMinute / 60, startTimeMinute % 60)

        val taskZonedDateTime = date.atTime(time).atZone(ZoneId.systemDefault())

        // کسر مستقیم دقایق مشخص شده
        val triggerZonedDateTime = if (reminderMin > 0) {
            taskZonedDateTime.minusMinutes(reminderMin.toLong())
        } else {
            taskZonedDateTime
        }

        return triggerZonedDateTime.toInstant().toEpochMilli()
    }

    private fun createPendingIntent(task: TaskItem): PendingIntent {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_DESC, task.description)
            putExtra(TaskAlarmReceiver.EXTRA_REMINDER_MIN, task.reminderMin)
        }

        return PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}