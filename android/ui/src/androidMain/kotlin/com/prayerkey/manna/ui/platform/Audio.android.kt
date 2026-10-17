package com.prayerkey.manna.ui.platform

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

actual fun openPcmOut(rate: Int, volume: Float): PcmOut {
    val min = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        .coerceAtLeast(4096)
    val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        .setAudioFormat(
            AudioFormat.Builder().setSampleRate(rate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).build(),
        )
        .setBufferSizeInBytes(min * 2)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()
    track.setVolume(volume)
    track.play()
    return object : PcmOut {
        override fun write(samples: ShortArray, count: Int) = track.write(samples, 0, count)
        override fun setVolume(volume: Float) { track.setVolume(volume) }
        override fun pause() { runCatching { track.pause() } }
        override fun resume() { runCatching { track.play() } }
        override fun release() { runCatching { track.pause(); track.flush(); track.release() } }
    }
}

actual class StreamPlayer actual constructor() {
    private val media = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build(),
        )
    }
    private var prepared = false

    actual fun setOnPreparedListener(listener: () -> Unit) {
        media.setOnPreparedListener { prepared = true; listener() }
    }
    actual fun setOnCompletionListener(listener: () -> Unit) { media.setOnCompletionListener { listener() } }
    actual fun setOnErrorListener(listener: () -> Unit) { media.setOnErrorListener { _, _, _ -> listener(); true } }
    actual fun prepare(url: String) {
        media.setDataSource(url)
        media.prepareAsync()
    }
    actual val duration: Int get() = if (prepared) media.duration else 0
    actual val currentPosition: Int get() = if (prepared) media.currentPosition else 0
    actual val isPlaying: Boolean get() = runCatching { media.isPlaying }.getOrDefault(false)
    actual fun start() = media.start()
    actual fun pause() = media.pause()
    actual fun seekTo(ms: Int) = media.seekTo(ms)
    actual fun setSpeed(speed: Float) { runCatching { media.playbackParams = media.playbackParams.setSpeed(speed) } }
    actual fun release() { runCatching { media.release() } }
}

private val main = Handler(Looper.getMainLooper())

actual fun postToMain(block: () -> Unit) {
    main.post(block)
}
