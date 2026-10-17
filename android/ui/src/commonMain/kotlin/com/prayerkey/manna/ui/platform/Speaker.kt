package com.prayerkey.manna.ui.platform

import com.prayerkey.manna.platform.PlatformContext

/**
 * The phone's own reading voice.
 *
 * Shaped like Android's TextToSpeech on purpose - the same speak, stop, rate,
 * pitch and progress callbacks - so the screens that read aloud kept their
 * logic when they became shared: the devotional's live progress, the office
 * moving on when a movement ends, the Bible narrator's verse highlight. On
 * Android it is TextToSpeech; on iPhone it is AVSpeechSynthesizer, which also
 * reports the range it is about to speak, so progress is just as real there.
 */
class Speaker(context: PlatformContext, onInit: (Int) -> Unit) {

    private var listener: UtteranceListener? = null
    private val engine: VoiceEngine = voiceEngine(context, { ok -> onInit(if (ok) SUCCESS else ERROR) }) { listener }

    /** The language to read in, as a tag such as "en-US"; null is the phone's own. */
    var language: String? = null
        set(value) { field = value; engine.setLanguage(value) }

    fun speak(text: CharSequence, queueMode: Int, params: Any?, utteranceId: String?): Int {
        engine.speak(text.toString(), queueMode == QUEUE_FLUSH, utteranceId)
        return SUCCESS
    }

    fun stop(): Int { engine.stop(); return SUCCESS }
    fun shutdown() = engine.shutdown()
    fun setSpeechRate(rate: Float): Int { engine.setRate(rate); return SUCCESS }
    fun setPitch(pitch: Float): Int { engine.setPitch(pitch); return SUCCESS }
    fun isSpeaking(): Boolean = engine.isSpeaking()

    fun setOnUtteranceProgressListener(listener: UtteranceListener?): Int {
        this.listener = listener
        return SUCCESS
    }

    companion object {
        const val SUCCESS = 0
        const val ERROR = -1
        const val QUEUE_FLUSH = 0
        const val QUEUE_ADD = 1
    }
}

/** Progress of what was handed to [Speaker.speak], by the id it was given. */
abstract class UtteranceListener {
    open fun onStart(utteranceId: String?) {}
    /** The voice is about to say characters [start] to [end] of that text. */
    open fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {}
    open fun onDone(utteranceId: String?) {}
    open fun onError(utteranceId: String?) {}
}

interface VoiceEngine {
    fun speak(text: String, flush: Boolean, id: String?)
    fun stop()
    fun shutdown()
    fun setRate(rate: Float)
    fun setPitch(pitch: Float)
    fun setLanguage(tag: String?)
    fun isSpeaking(): Boolean
}

/** [listener] is read on every callback, so a listener set later is still heard. */
expect fun voiceEngine(context: PlatformContext, onReady: (Boolean) -> Unit, listener: () -> UtteranceListener?): VoiceEngine

/** Where the phone's voices are managed: Android's voice data screen; iPhone's Settings. */
expect fun openVoiceSettings(context: PlatformContext)
