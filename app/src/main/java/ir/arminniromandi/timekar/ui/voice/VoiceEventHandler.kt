package ir.arminniromandi.timekar.ui.voice

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.arminniromandi.timekar.domain.AppLanguage
import ir.arminniromandi.timekar.ui.shared.SharedTasksViewModel

/**
 * مدیریت رویدادهای مربوط به ویس
 */
@Composable
fun VoiceEventHandler(
    context: Context,
    showVoiceDialog: Boolean,
    language: AppLanguage,
    voiceViewModel: VoiceTaskViewModel,
    sharedViewModel: SharedTasksViewModel
) {
    val sharedUiState by sharedViewModel.uiState.collectAsStateWithLifecycle()


    // شروع ضبط صدا هنگام باز شدن دیالوگ
    LaunchedEffect(showVoiceDialog) {
        if (showVoiceDialog) {
            voiceViewModel.getLang(language)
            voiceViewModel.onRecordClick()
        }
    }

    // نمایش متن دریافتی از ویس
    LaunchedEffect(sharedUiState.voiceInputText) {
        sharedUiState.voiceInputText?.let { text ->
            Toast.makeText(
                context,
                "متن دریافت شده: $text",
                Toast.LENGTH_LONG
            ).show()

            sharedViewModel.sendAiRequest(text)
            // اینجا می‌توانید هر کاری با متن انجام دهید
            // مثلاً باز کردن صفحه ایجاد تسک با این متن
            // sharedViewModel.openNewTaskSheet(taskWithVoiceText)
            
            sharedViewModel.clearVoiceText()
        }
    }
}
