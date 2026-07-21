package com.prayerkey.manna.ui.church

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SermonRecognizer(
    context: Context,
    private val onWords: (String) -> Unit,
    private val onStatus: (String) -> Unit,
) : RecognitionListener {
    private val handler = Handler(Looper.getMainLooper())
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context).also { it.setRecognitionListener(this) }
    private val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
    }
    private var continuous = false

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(recognizerContext)) {
            onStatus("Speech recognition is unavailable on this device")
            return
        }
        continuous = true; onStatus("Listening…"); recognizer.startListening(intent)
    }

    fun stop() { continuous = false; handler.removeCallbacksAndMessages(null); recognizer.stopListening(); onStatus("Ready") }
    fun destroy() { continuous = false; handler.removeCallbacksAndMessages(null); recognizer.destroy() }

    private fun restart() {
        if (continuous) handler.postDelayed({ if (continuous) recognizer.startListening(intent) }, 350)
    }

    override fun onResults(results: Bundle) {
        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onWords)
        restart()
    }
    override fun onPartialResults(partialResults: Bundle) {
        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onWords)
    }
    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            continuous = false; onStatus("Microphone permission is required")
        } else restart()
    }
    override fun onReadyForSpeech(params: Bundle?) { onStatus("Listening…") }
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private val recognizerContext: Context = context.applicationContext
}
