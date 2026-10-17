package com.prayerkey.manna.ui.home

import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.ui.platform.PcmOut
import com.prayerkey.manna.ui.platform.openPcmOut
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * A quiet, copyright-free worship pad generated on the phone. It needs no
 * download, works offline, and fades its chord changes so there are no clicks.
 */
class AmbientPrayerPlayer {
    @Volatile private var playing = false
    private var worker: Job? = null
    private var track: PcmOut? = null

    fun start() {
        if (playing) return
        playing = true
        worker = CoroutineScope(ioDispatcher).launch {
            val rate = 22_050
            val audio = openPcmOut(rate, .12f)
            track = audio
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
                if (audio.write(buffer, buffer.size) < 0) break
            }
            audio.release()
            track = null
        }
    }

    fun stop() {
        playing = false
        track?.release()
        worker?.cancel()
        worker = null
    }
}
