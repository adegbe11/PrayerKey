package com.prayerkey.manna.ui.platform

import com.prayerkey.manna.platform.PlatformContext
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechUtteranceDefaultSpeechRate
import platform.AVFAudio.AVSpeechUtteranceMaximumSpeechRate
import platform.AVFAudio.AVSpeechUtteranceMinimumSpeechRate
import platform.Foundation.NSRange
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject

actual fun voiceEngine(context: PlatformContext, onReady: (Boolean) -> Unit, listener: () -> UtteranceListener?): VoiceEngine =
    IosVoice(listener).also { onReady(true) }

actual fun openVoiceSettings(context: PlatformContext) {
    NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
        UIApplication.sharedApplication.openURL(it, emptyMap<Any?, Any>(), null)
    }
}

/**
 * AVSpeechSynthesizer behind Android's TextToSpeech shape.
 *
 * Android's rate 1.0 is a normal pace; iPhone's normal is
 * AVSpeechUtteranceDefaultSpeechRate (0.5 on a 0-1 scale), so rates are
 * scaled from that. Pitch means the same on both. Utterances queue by
 * themselves on iPhone; a flush stops what is playing first.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosVoice(private val listener: () -> UtteranceListener?) : VoiceEngine {
    private val synth = AVSpeechSynthesizer()
    private var rate = 1f
    private var pitch = 1f
    private var language: String? = null
    private val ids = mutableMapOf<AVSpeechUtterance, String?>()

    // the synthesizer holds its delegate weakly, so this field keeps it alive
    private val delegate = object : NSObject(), AVSpeechSynthesizerDelegateProtocol {
        @ObjCSignatureOverride
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didStartSpeechUtterance: AVSpeechUtterance) {
            listener()?.onStart(ids[didStartSpeechUtterance])
        }

        override fun speechSynthesizer(
            synthesizer: AVSpeechSynthesizer,
            willSpeakRangeOfSpeechString: CValue<NSRange>,
            utterance: AVSpeechUtterance,
        ) {
            val id = ids[utterance]
            willSpeakRangeOfSpeechString.useContents {
                listener()?.onRangeStart(id, location.toInt(), (location + length).toInt(), 0)
            }
        }

        @ObjCSignatureOverride
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didFinishSpeechUtterance: AVSpeechUtterance) {
            val id = ids.remove(didFinishSpeechUtterance)
            listener()?.onDone(id)
        }

        @ObjCSignatureOverride
        override fun speechSynthesizer(synthesizer: AVSpeechSynthesizer, didCancelSpeechUtterance: AVSpeechUtterance) {
            ids.remove(didCancelSpeechUtterance)
        }
    }

    init {
        synth.delegate = delegate
    }

    override fun speak(text: String, flush: Boolean, id: String?) {
        if (flush) stop()
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        utterance.rate = (AVSpeechUtteranceDefaultSpeechRate * rate)
            .coerceIn(AVSpeechUtteranceMinimumSpeechRate, AVSpeechUtteranceMaximumSpeechRate)
        utterance.pitchMultiplier = pitch.coerceIn(.5f, 2f)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(language ?: AVSpeechSynthesisVoice.currentLanguageCode())
        ids[utterance] = id
        synth.speakUtterance(utterance)
    }

    override fun stop() {
        synth.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }

    override fun shutdown() {
        stop()
        synth.delegate = null
    }

    override fun setRate(rate: Float) { this.rate = rate }
    override fun setPitch(pitch: Float) { this.pitch = pitch }
    override fun setLanguage(tag: String?) { language = tag }
    override fun isSpeaking(): Boolean = synth.speaking
}
