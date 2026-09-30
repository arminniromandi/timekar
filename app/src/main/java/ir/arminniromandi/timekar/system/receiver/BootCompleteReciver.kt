package ir.arminniromandi.timekar.system.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import ir.arminniromandi.timekar.ChronosApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

class BootCompletedReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // ۱. دریافت وابستگی‌ها از Manual DI Container
        val app = context.applicationContext as ChronosApplication
        val getPendingTaskUseCase = app.container.getPendingTask
        val alarmScheduler = app.container.taskReminderScheduler
        val todayEpochDay = LocalDate.now().toEpochDay()

        // ۲. استفاده از goAsync تا سیستم‌عامل قبل از پایان کار دیتابیس پروسس را Kill نکند
        val pendingResult = goAsync()

        // ۳. اجرای عملیات در پس‌زمینه
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val pendingTasks = getPendingTaskUseCase(todayEpochDay)
                pendingTasks.forEach { entityDomain ->

                    if (entityDomain.hasReminder) {
                        alarmScheduler.schedule(entityDomain)
                    }
                }
            } finally {
                // آزادسازی Receiver پس از اتمام کار
                pendingResult.finish()
            }
        }
    }
}