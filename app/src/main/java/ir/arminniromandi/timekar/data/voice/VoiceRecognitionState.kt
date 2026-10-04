package ir.arminniromandi.timekar.data.voice
sealed class VoiceRecognitionState {
    object Idle : VoiceRecognitionState()
    object Listening : VoiceRecognitionState()
    object Paused : VoiceRecognitionState()
    data class SpokenText(
        val fullText: String,      // کل متن تجمیع شده تا الان
        val currentPart: String,   // بخش جدید فعلی
        val isFinal: Boolean
    ) : VoiceRecognitionState()
    data class Error(val message: String, val errorCode: Int) : VoiceRecognitionState()
}