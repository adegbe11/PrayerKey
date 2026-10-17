package com.prayerkey.manna.ui.platform

/**
 * Sound made on the phone, a buffer at a time: 16-bit mono samples at [rate].
 *
 * The soundscapes and the prayer pad synthesise their audio rather than ship
 * recordings, so all they need from the platform is somewhere to write it.
 * [write] blocks while the output is full, exactly as Android's AudioTrack
 * does in streaming mode, which is what paces the generator.
 */
interface PcmOut {
    /** Returns how many samples were taken, or a negative number once closed. */
    fun write(samples: ShortArray, count: Int): Int
    fun setVolume(volume: Float)
    fun pause()
    fun resume()
    fun release()
}

expect fun openPcmOut(rate: Int, volume: Float): PcmOut

/**
 * A recording streamed from the web (the audio Bible's chapters), shaped like
 * Android's MediaPlayer, which the screen was written against.
 */
expect class StreamPlayer() {
    fun setOnPreparedListener(listener: () -> Unit)
    fun setOnCompletionListener(listener: () -> Unit)
    fun setOnErrorListener(listener: () -> Unit)
    /** Starts loading [url]; the prepared listener fires when it can play. */
    fun prepare(url: String)
    /** Milliseconds; 0 until prepared. */
    val duration: Int
    val currentPosition: Int
    val isPlaying: Boolean
    fun start()
    fun pause()
    fun seekTo(ms: Int)
    fun setSpeed(speed: Float)
    fun release()
}

/** Runs [block] on the main thread: for callbacks that arrive on another one. */
expect fun postToMain(block: () -> Unit)
