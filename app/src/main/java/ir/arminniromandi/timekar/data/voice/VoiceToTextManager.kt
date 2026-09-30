package ir.arminniromandi.timekar.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class VoiceToTextManager(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null

    fun startListening(languageCode: String = "fa-IR"): Flow<VoiceRecognitionState> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            trySend(VoiceRecognitionState.Error("سرویس تشخیص گفتار در این دستگاه در دسترس نیست", -1))
            close()
            return@callbackFlow
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            cancel()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // تنظیم ۳ ثانیه سکوت پس از اتمام کلام قبل از پردازش نهایی
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }

        var autoCloseJob: Job? = null

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                trySend(VoiceRecognitionState.Listening)
            }

            override fun onBeginningOfSpeech() {
                autoCloseJob?.cancel()
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                // کاربر صحبتش تمام شده؛ منتظر آماده شدن متن نهایی در onResults می‌مانیم
            }

            override fun onError(errorCode: Int) {
                autoCloseJob?.cancel()
                val message = when (errorCode) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "صدایی تشخیص داده نشد"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "زمان صحبت به پایان رسید"
                    SpeechRecognizer.ERROR_AUDIO -> "خطای میکروفون"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "خطای اتصال به شبکه"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "سرویس مشغول است"
                    else -> "خطا در پردازش صوت ($errorCode)"
                }
                trySend(VoiceRecognitionState.Error(message, errorCode))
                close()
            }

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (text.isNotBlank()) {
                    trySend(VoiceRecognitionState.SpokenText(text = text, isFinal = true))
                }

                // شروع شمارش معکوس ۳ ثانیه‌ای برای قطع و بستن Flow
                autoCloseJob?.cancel()
                autoCloseJob = launch {
                    delay(3000L)
                    trySend(VoiceRecognitionState.Idle)
                    close() // بستن جریان (Flow) که باعث اجرای awaitClose و نابودی recognizer می‌شود
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                if (text.isNotBlank()) {
                    trySend(VoiceRecognitionState.SpokenText(text = text, isFinal = false))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        recognizer?.setRecognitionListener(listener)
        recognizer?.startListening(intent)

        awaitClose {
            autoCloseJob?.cancel()
            recognizer?.stopListening()
            recognizer?.cancel()
            recognizer?.destroy()
            recognizer = null
        }
    }

    fun stopListening() {
        recognizer?.stopListening()
        recognizer?.cancel()
    }
}