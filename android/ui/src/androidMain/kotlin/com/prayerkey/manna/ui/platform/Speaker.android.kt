package com.prayerkey.manna.ui.platform

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.prayerkey.manna.platform.PlatformContext
import java.util.Locale

actual fun voiceEngine(context: PlatformContext, onReady: (Boolean) -> Unit, listener: () -> UtteranceListener?): VoiceEngine =
    AndroidVoice(context, onReady, listener)

actual fun openVoiceSettings(context: PlatformContext) {
    runCatching {
        context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private class AndroidVoice(
    context: PlatformContext,
    onReady: (Boolean) -> Unit,
    listener: () -> UtteranceListener?,
) : VoiceEngine {
    private val tts = TextToSpeech(context.applicationContext) { onReady(it == TextToSpeech.SUCCESS) }

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
        tts.language = tag?.let(Locale::forLanguageTag) ?: Locale.getDefault()
    }
    override fun isSpeaking(): Boolean = tts.isSpeaking
}
