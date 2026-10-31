package com.prayerkey.manna.ui.platform

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.prayerkey.manna.platform.PlatformContext
import java.util.Locale

actual fun voiceEngine(context: PlatformContext, onReady: (Boolean) -> Unit, listener: () -> UtteranceListener?): VoiceEngine =
    AndroidVoice(context, onReady, listener)

actual fun openVoiceSettings(context: PlatformContext) {
    runCatching { context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .recoverCatching { context.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

private class AndroidVoice(
    context: PlatformContext,
    onReady: (Boolean) -> Unit,
    listener: () -> UtteranceListener?,
) : VoiceEngine {
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) useBestVoice(Locale.getDefault())
        onReady(status == TextToSpeech.SUCCESS)
    }

    /* The engine starts on its plainest voice. Most phones carry far better
       ones for the same language; a prayer read in the flat default sounds
       like a timetable announcement. Highest quality wins, a voice that works
       offline beats one that needs the network, and nothing changes when the
       phone offers no better choice. */
    private fun useBestVoice(locale: Locale) {
        runCatching {
            val best = tts.voices.orEmpty()
                .filter { it.locale.language == locale.language && !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) }
                .sortedWith(
                    compareByDescending<android.speech.tts.Voice> { it.locale.country == locale.country }
                        .thenByDescending { it.quality }
                        .thenBy { it.isNetworkConnectionRequired }
                        .thenBy { it.latency },
                )
                .firstOrNull()
            if (best != null && best.quality > (tts.voice?.quality ?: 0)) tts.voice = best
        }
    }

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { listener()?.onStart(utteranceId) }
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                listener()?.onRangeStart(utteranceId, start, end, frame)
            }
            override fun onDone(utteranceId: String?) { listener()?.onDone(utteranceId) }
            @Deprecated("required by UtteranceProgressListener")
            override fun onError(utteranceId: String?) { listener()?.onError(utteranceId) }
        })
    }

    override fun speak(text: String, flush: Boolean, id: String?) {
        tts.speak(text, if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, id)
    }
    override fun stop() { tts.stop() }
    override fun shutdown() = tts.shutdown()
    override fun setRate(rate: Float) { tts.setSpeechRate(rate) }
    override fun setPitch(pitch: Float) { tts.setPitch(pitch) }
    override fun setLanguage(tag: String?) {
        val locale = tag?.let(Locale::forLanguageTag) ?: Locale.getDefault()
        tts.language = locale
        useBestVoice(locale)
    }
    override fun isSpeaking(): Boolean = tts.isSpeaking
}
