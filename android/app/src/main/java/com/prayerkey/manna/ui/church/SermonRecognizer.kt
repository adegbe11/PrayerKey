package com.prayerkey.manna.ui.church

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Listens for the length of a service and keeps the whole transcript.
 *
 * Two things it must get right:
 *  - ACCUMULATE. onResults hands back one utterance at a time; each
 *    finished chunk is appended, never assigned over. Without this there is
 *    no sermon, only the last few seconds of it.
 *  - PREFER OFFLINE. EXTRA_PREFER_OFFLINE keeps the audio on the phone,
 *    which is what the app promises the user and what makes it free.
 */
class SermonRecognizer(
    context: Context,
    /** BCP-47 tag, e.g. "en-NG". Empty means the device default. */
    language: String = "",
    /** Every finished chunk, as it lands. */
    private val onChunk: (String) -> Unit,
    /** The in-flight utterance, for the live caption only. */
    private val onPartial: (String) -> Unit,
    private val onStatus: (String) -> Unit,
) : RecognitionListener {

    private val app: Context = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(app).also { it.setRecognitionListener(this) }

    private val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, app.packageName)
        if (language.isNotBlank()) {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
        }
        // keep the audio on the device — free, private, works with no signal
        // don't cut the mic the moment he pauses for breath
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 8000L)
        // Continuous restarts below are more compatible than segmented mode:
        // several Google/OEM recognizers reject the segmented-session extra.
    }

    private var running = false
    /** Guards against two startListening calls racing after an error. */
    private var pendingRestart = false

    val available: Boolean get() = SpeechRecognizer.isRecognitionAvailable(app)

    fun start() {
        if (!available) { onStatus("Speech recognition is unavailable on this device"); return }
        running = true
        onStatus("Listening")
        safeStart()
    }

    fun stop() {
        running = false
        pendingRestart = false
        handler.removeCallbacksAndMessages(null)
        runCatching { recognizer.stopListening() }
        onStatus("Ready")
    }

    fun destroy() {
        running = false
        pendingRestart = false
        handler.removeCallbacksAndMessages(null)
        runCatching { recognizer.destroy() }
    }

    private fun safeStart() {
        pendingRestart = false
        runCatching { recognizer.startListening(intent) }
            .onFailure { handler.postDelayed({ if (running) safeStart() }, 600) }
    }

    /** Restart fast — every millisecond here is a word of the sermon lost. */
    private fun restart(delayMs: Long = 120) {
        if (!running || pendingRestart) return
        pendingRestart = true
        handler.postDelayed({ if (running) safeStart() }, delayMs)
    }

    override fun onResults(results: Bundle) {
        emitFinal(results)
        restart()
    }

    override fun onSegmentResults(segmentResults: Bundle) {
        emitFinal(segmentResults)
    }

    override fun onEndOfSegmentedSession() {
        restart(80)
    }

    private fun emitFinal(results: Bundle) {
        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(onChunk)
    }

    override fun onPartialResults(partialResults: Bundle) {
        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(onPartial)
    }

    override fun onError(error: Int) {
        when (error) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                running = false; onStatus("Microphone permission is required")
            }
            // no offline model on this device — fall back to the online one
            // once, rather than silently capturing nothing all service
            SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> {
                intent.removeExtra(RecognizerIntent.EXTRA_LANGUAGE)
                intent.removeExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)
                onStatus("Listening")
                restart(400)
            }
            // silence and no-match are normal in a service — just go again
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                onStatus("Listening")
                restart(80)
            }
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> restart(700)
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
            SpeechRecognizer.ERROR_SERVER -> {
                onStatus("Speech service reconnecting")
                restart(900)
            }
            SpeechRecognizer.ERROR_CLIENT -> restart(500)
            else -> { onStatus("Listening · retrying"); restart(400) }
        }
    }

    override fun onReadyForSpeech(params: Bundle?) { if (running) onStatus("Listening") }
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
