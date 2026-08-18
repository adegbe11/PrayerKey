package com.prayerkey.manna.ui.home

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

/**
 * A quiet, copyright-free worship pad generated on the phone. It needs no
 * download, works offline, and fades its chord changes so there are no clicks.
 */
internal class AmbientPrayerPlayer {
    @Volatile private var playing = false
    private var worker: Thread? = null
    private var track: AudioTrack? = null

    fun start() {
        if (playing) return
        playing = true
        worker = thread(name = "PrayerKeyAmbientPad", isDaemon = true) {
            val rate = 22_050
            val size = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
                .coerceAtLeast(4096)
            val audio = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
                .setBufferSizeInBytes(size * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            track = audio
            audio.setVolume(.12f)
            audio.play()
            val buffer = ShortArray(2048)
            val chords = arrayOf(
                doubleArrayOf(130.81, 164.81, 196.00), // C
                doubleArrayOf(110.00, 130.81, 164.81), // Am
                doubleArrayOf(87.31, 130.81, 174.61),  // F
                doubleArrayOf(98.00, 146.83, 196.00),  // G
            )
            var sample = 0L
            while (playing) {
                for (i in buffer.indices) {
                    val seconds = sample.toDouble() / rate
                    val chordPosition = (seconds / 8.0)
                    val chord = chords[chordPosition.toInt() % chords.size]
                    val within = chordPosition - chordPosition.toInt()
                    val breath = .70 + .30 * sin(2.0 * PI * seconds / 6.0)
                    val edgeFade = (minOf(within, 1.0 - within) * 8.0).coerceIn(0.0, 1.0)
                    val wave = chord.sumOf { hz -> sin(2.0 * PI * hz * seconds) + .22 * sin(2.0 * PI * hz * .5 * seconds) } / (chord.size * 1.22)
                    buffer[i] = (wave * breath * edgeFade * 5_500).toInt().toShort()
                    sample++
                }
                if (audio.write(buffer, 0, buffer.size) < 0) break
            }
            runCatching { audio.pause(); audio.flush(); audio.release() }
            track = null
        }
    }

    fun stop() {
        playing = false
        runCatching { track?.pause() }
        worker?.interrupt()
        worker = null
    }
}
