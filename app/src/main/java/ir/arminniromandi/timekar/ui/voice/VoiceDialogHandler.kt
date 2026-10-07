package ir.arminniromandi.timekar.ui.voice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    val apiReqState by sharedViewModel.aiTaskResult.collectAsStateWithLifecycle()

    if (showDialog) {
        SaveTaskFromVoiceDialog(
            voiceState = voiceUiState.voiceStatus,
            spokenText = voiceUiState.titleText,
            aiApiReq = apiReqState,
            strings = strings,
            onDismissRequest = {
                voiceViewModel.onStopClick()
                sharedViewModel.resetAiTaskResult()
                onDismiss()
            },
            onPauseListening = { voiceViewModel.onPauseClick() },
            onResumeListening = { voiceViewModel.onResumeClick() },
            onStopListening = {
                voiceViewModel.onStopClick()
                sharedViewModel.resetAiTaskResult()
            },
            onSaveVoiceText = { text ->
                sharedViewModel.receiveVoiceText(text)
            }
        )
    }
}
