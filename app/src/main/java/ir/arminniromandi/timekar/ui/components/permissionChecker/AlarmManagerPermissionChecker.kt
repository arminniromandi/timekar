package ir.arminniromandi.timekar.ui.components.permissionChecker


import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun AlarmPermissionChecker(context : Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // وضعیت مجوز دقیق آلارم
    var hasExactAlarmPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
        )
    }

    // درخواست مجوز نوتیفیکیشن (اندروید ۱۳ به بالا)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // کاربر مجوز نداد، می‌توانید به او پیام بدهید که یادآورها کار نخواهند کرد
        }
    }

    // بررسی و درخواست خودکار نوتیفیکیشن هنگام لود شدن صفحه
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // اگر مجوز آلارم دقیق (Exact Alarm) را در اندروید 12+ ندارد، یک دیالوگ یا کارت به او نشان می‌دهیم
    if (!hasExactAlarmPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        AlertDialog(
            onDismissRequest = { /* فعلا کاری نمیکنیم، کاربر حتما باید تایید کند */ },
            title = { Text("مجوز یادآوری دقیق") },
            text = {
                Text("برای اینکه یادآور تسک‌ها دقیقاً سر ساعت تنظیم شده کار کند، لطفاً به برنامه اجازه تنظیم آلارم دقیق را بدهید.")
            },
            confirmButton = {
                Button(onClick = {
                    // هدایت کاربر به صفحه تنظیمات برای دادن مجوز آلارم دقیق
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }) {
                    Text("انتقال به تنظیمات")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // بروزرسانی وضعیت (شاید کاربر نخواهد مجوز بدهد)
                    hasExactAlarmPermission = alarmManager.canScheduleExactAlarms()
                }) {
                    Text("بعداً")
                }
            }
        )
    }
}