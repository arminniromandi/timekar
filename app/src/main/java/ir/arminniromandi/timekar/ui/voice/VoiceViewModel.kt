package ir.arminniromandi.timekar.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.arminniromandi.timekar.data.voice.VoiceRecognitionState
import ir.arminniromandi.timekar.data.voice.VoiceToTextManager
import ir.arminniromandi.timekar.domain.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// وضعیت کلی صفحه
data class TaskScreenUiState(
    val titleText: String = "",
    val voiceStatus: VoiceRecognitionState = VoiceRecognitionState.Idle,
    val isMicrophoneActive: Boolean = false
)

class VoiceTaskViewModel(
    private val voiceManager: VoiceToTextManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskScreenUiState())
    val uiState: StateFlow<TaskScreenUiState> = _uiState.asStateFlow()

    private val _langCode = MutableStateFlow("fa-IR")
    val langCode = _langCode.asStateFlow()


    init {
        observeVoiceState()
    }


    private fun observeVoiceState() {
        viewModelScope.launch {
            voiceManager.state.collect { state ->
                when (state) {
                    is VoiceRecognitionState.SpokenText -> {
                        _uiState.update { current ->
                            current.copy(
                                titleText = state.fullText,
                                voiceStatus = state,
                                isMicrophoneActive = true
                            )
                        }
                    }

                    is VoiceRecognitionState.Listening -> {
                        _uiState.update { it.copy(voiceStatus = state, isMicrophoneActive = true) }
                    }

                    is VoiceRecognitionState.Paused -> {
                        _uiState.update { it.copy(voiceStatus = state, isMicrophoneActive = false) }
                    }

                    is VoiceRecognitionState.Idle, is VoiceRecognitionState.Error -> {
                        _uiState.update { it.copy(voiceStatus = state, isMicrophoneActive = false) }
                    }
                }
            }
        }
    }

    fun getLang(lang: AppLanguage) {
        when (lang) {
            AppLanguage.PERSIAN -> _langCode.value = "fa-IR"
            AppLanguage.ENGLISH -> _langCode.value = "en-US"
        }
    }

    fun onRecordClick() = voiceManager.startListening(langCode.value)
    fun onPauseClick() = voiceManager.pauseListening()
    fun onResumeClick() = voiceManager.resumeListening(langCode.value)
    fun onStopClick() = voiceManager.stopListening()

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }

    companion object {
        fun provideFactory(
            voiceManager: VoiceToTextManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VoiceTaskViewModel(
                    voiceManager = voiceManager
                ) as T
            }
        }

    }
}