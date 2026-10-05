package ir.arminniromandi.timekar.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class VoiceToTextManager(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()

    private var accumulatedText: StringBuilder = StringBuilder()
    private var isPaused: Boolean = false
    private var autoCloseJob: Job? = null

    private fun createRecognizerIntent(languageCode: String): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }
    }

    private fun initRecognizer() {
        if (recognizer != null) return

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (!isPaused) {
                        _state.value = VoiceRecognitionState.Listening
                    }
                }

                override fun onBeginningOfSpeech() {
                    autoCloseJob?.cancel()
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(errorCode: Int) {
                    autoCloseJob?.cancel()

                    // در هنگام توقف دستی یا تغییر وضعیت، این کدها خطا نیستند
                    if (isPaused && errorCode == SpeechRecognizer.ERROR_CLIENT) return
                    if (errorCode == SpeechRecognizer.ERROR_NO_MATCH && isPaused) return

                    val message = when (errorCode) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "صدایی تشخیص داده نشد"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "زمان صحبت به پایان رسید"
                        SpeechRecognizer.ERROR_AUDIO -> "خطای میکروفون"
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "خطای اتصال به شبکه"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "سرویس مشغول است"
                        else -> "خطا در پردازش صوت ($errorCode)"
                    }
                    _state.value = VoiceRecognitionState.Error(message, errorCode)
                }

                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull().orEmpty().trim()

                    if (text.isNotBlank()) {
                        if (accumulatedText.isNotEmpty()) {
                            accumulatedText.append(" ")
                        }
                        accumulatedText.append(text)

                        _state.value = VoiceRecognitionState.SpokenText(
                            fullText = accumulatedText.toString(),
                            currentPart = text,
                            isFinal = true
                        )
                    }

                    // اگر در حالت Pause نباشد، تایمر بستن یا ریست اجرا می‌شود
                    if (!isPaused) {
                        autoCloseJob?.cancel()
                        autoCloseJob = scope.launch {
                            delay(3000L)
                            stopListening()
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull().orEmpty().trim()

                    if (text.isNotBlank()) {
                        val previewText = if (accumulatedText.isNotEmpty()) {
                            "${accumulatedText} $text"
                        } else {
                            text
                        }

                        _state.value = VoiceRecognitionState.SpokenText(
                            fullText = previewText,
                            currentPart = text,
                            isFinal = false
                        )
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startListening(currentLanguage: String ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = VoiceRecognitionState.Error("سرویس تشخیص گفتار در دسترس نیست", -1)
            return
        }

        accumulatedText.clear()
        isPaused = false

        initRecognizer()
        recognizer?.startListening(createRecognizerIntent(currentLanguage))
    }

    fun pauseListening() {
        if (isPaused) return
        isPaused = true
        autoCloseJob?.cancel()

        // قطع شنود فعال بدون پاک شدن حافظه متن
        recognizer?.stopListening()
        _state.value = VoiceRecognitionState.Paused
    }

    fun resumeListening(currentLanguage : String) {
        if (!isPaused) return
        isPaused = false

        initRecognizer()
        recognizer?.startListening(createRecognizerIntent(currentLanguage))
        _state.value = VoiceRecognitionState.Listening
    }

    fun stopListening() {
        isPaused = false
        autoCloseJob?.cancel()
        recognizer?.stopListening()
        recognizer?.cancel()
        _state.value = VoiceRecognitionState.Idle
        _state.value = VoiceRecognitionState.SpokenText(
            fullText = "",
            currentPart = "",
            isFinal = false
        )
    }

    fun destroy() {
        stopListening()
        recognizer?.destroy()
        recognizer = null
    }
}