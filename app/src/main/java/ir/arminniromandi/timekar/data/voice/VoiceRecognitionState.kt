package ir.arminniromandi.timekar.data.voice

sealed interface VoiceRecognitionState {
    object Idle : VoiceRecognitionState
    object Listening : VoiceRecognitionState
    data class SpokenText(val text: String, val isFinal: Boolean) : VoiceRecognitionState
    data class Error(val message: String, val errorCode: Int) : VoiceRecognitionState
}