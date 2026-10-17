package com.prayerkey.manna.ui.platform

// The web build is a compile check only.
actual fun openPcmOut(rate: Int, volume: Float): PcmOut = object : PcmOut {
    override fun write(samples: ShortArray, count: Int) = -1
    override fun setVolume(volume: Float) = Unit
    override fun pause() = Unit
    override fun resume() = Unit
    override fun release() = Unit
}

actual class StreamPlayer actual constructor() {
    private var error: () -> Unit = {}
    actual fun setOnPreparedListener(listener: () -> Unit) = Unit
    actual fun setOnCompletionListener(listener: () -> Unit) = Unit
    actual fun setOnErrorListener(listener: () -> Unit) { error = listener }
    actual fun prepare(url: String) = error()
    actual val duration: Int get() = 0
    actual val currentPosition: Int get() = 0
    actual val isPlaying: Boolean get() = false
    actual fun start() = Unit
    actual fun pause() = Unit
    actual fun seekTo(ms: Int) = Unit
    actual fun setSpeed(speed: Float) = Unit
    actual fun release() = Unit
}

actual fun postToMain(block: () -> Unit) = block()
