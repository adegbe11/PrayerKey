package com.prayerkey.manna.ui.platform

import com.prayerkey.manna.platform.PlatformContext

// The web build is a compile check; it has no voice.
actual fun voiceEngine(context: PlatformContext, onReady: (Boolean) -> Unit, listener: () -> UtteranceListener?): VoiceEngine =
    object : VoiceEngine {
        override fun speak(text: String, flush: Boolean, id: String?) {}
        override fun stop() {}
        override fun shutdown() {}
        override fun setRate(rate: Float) {}
        override fun setPitch(pitch: Float) {}
        override fun setLanguage(tag: String?) {}
        override fun isSpeaking() = false
    }.also { onReady(false) }

actual fun openVoiceSettings(context: PlatformContext) {}
