package ir.arminniromandi.timekar.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.arminniromandi.timekar.data.voice.VoiceRecognitionState
import ir.arminniromandi.timekar.data.voice.VoiceToTextManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VoiceUiState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val errorMessage: String? = null
)

open class VoiceTaskViewModel(
    private val voiceManager: VoiceToTextManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState = _uiState.asStateFlow()

    private var recognitionJob: Job? = null

    fun startListening() {
        recognitionJob?.cancel()
        recognitionJob = viewModelScope.launch {
            voiceManager.startListening().collect { state ->
                when (state) {
                    is VoiceRecognitionState.Listening -> {
                        _uiState.update { it.copy(isListening = true, errorMessage = null) }
                    }
                    is VoiceRecognitionState.Idle -> {
                        _uiState.update { it.copy(isListening = false) }
                    }
                    is VoiceRecognitionState.SpokenText -> {
                        _uiState.update { it.copy(spokenText = state.text) }
                    }
                    is VoiceRecognitionState.Error -> {
                        _uiState.update {
                            it.copy(
                                isListening = false,
                                errorMessage = state.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun stopListening() {
        voiceManager.stopListening()
        recognitionJob?.cancel()
        _uiState.update { it.copy(isListening = false) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.stopListening()
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