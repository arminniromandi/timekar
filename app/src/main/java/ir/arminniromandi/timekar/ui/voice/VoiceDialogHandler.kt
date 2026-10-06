package ir.arminniromandi.timekar.ui.voice

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.arminniromandi.timekar.ui.components.SaveTaskFromVoiceDialog
import ir.arminniromandi.timekar.ui.shared.SharedTasksViewModel
import ir.arminniromandi.timekar.ui.strings.AppStrings

@Composable
fun VoiceDialogHandler(
    showDialog: Boolean,
    voiceViewModel: VoiceTaskViewModel,
    sharedViewModel: SharedTasksViewModel,
    strings: AppStrings,
    onDismiss: () -> Unit
) {
    val voiceUiState by voiceViewModel.uiState.collectAsStateWithLifecycle()

    if (showDialog) {
        SaveTaskFromVoiceDialog(
            voiceState = voiceUiState.voiceStatus,
            spokenText = voiceUiState.titleText,
            strings = strings,
            onDismissRequest = {
                voiceViewModel.onStopClick()
                onDismiss()
            },
            onPauseListening = { voiceViewModel.onPauseClick() },
            onResumeListening = { voiceViewModel.onResumeClick() },
            onStopListening = { voiceViewModel.onStopClick() },
            onSaveVoiceText = { text ->
//                voiceViewModel.onStopClick()
                sharedViewModel.receiveVoiceText(text)
            }
        )
    }
}
