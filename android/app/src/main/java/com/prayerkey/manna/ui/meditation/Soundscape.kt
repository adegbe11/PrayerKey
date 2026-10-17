package com.prayerkey.manna.ui.meditation

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.prayerkey.manna.data.Ambience
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * The sound under a meditation, made on the phone.
 *
 * Rain, sea, birdsong, piano and harp, synthesised a buffer at a time: no
 * files to ship, nothing to download, nobody's recording to license, and it
 * plays offline on the first launch. Each is deliberately plain - a bed to
 * sit on rather than music to listen to - and held well under the voice.
 */
internal class Soundscape(private val kind: Ambience) {
    @Volatile private var playing = false
    @Volatile private var paused = false
    private var worker: Thread? = null
    private var track: AudioTrack? = null

    fun start() {
        if (kind == Ambience.NONE || playing) return
        playing = true
        worker = thread(name = "PrayerKeySoundscape", isDaemon = true) { run() }
    }

    fun pause(value: Boolean) {
        paused = value
    }

    fun stop() {
        playing = false
        worker?.join(400)
        worker = null
        track?.runCatching { stop(); release() }
        track = null
    }

    private data class Note(val hz: Double, val start: Long, val decay: Double, val amp: Double, val bright: Double)

    private fun run() {
        val rate = 22_050
        val min = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            .coerceAtLeast(4096)
        val audio = AudioTrack.Builder()
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
        track = audio
        audio.setVolume(.55f)
        audio.play()

        val random = Random(kind.ordinal * 31 + 7)
        val buffer = ShortArray(2048)
        var n = 0L
        // one-pole filters: rain and wind are noise with the edge taken off
        var low = 0.0
        var lower = 0.0
        val notes = ArrayList<Note>()
        var nextEvent = 0L
        var chirpAt = -1L
        var chirpHz = 0.0
        // C major pentatonic, two octaves, for the instruments
        val scale = doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25)
        var arpeggio = 0

        while (playing) {
            if (paused) {
                java.util.Arrays.fill(buffer, 0)
                audio.write(buffer, 0, buffer.size)
                continue
            }
            for (i in buffer.indices) {
                val t = n.toDouble() / rate
                val fadeIn = (t / 3.0).coerceAtMost(1.0)
                val white = random.nextDouble() * 2 - 1
                var s = 0.0
                when (kind) {
                    Ambience.RAIN -> {
                        low += .22 * (white - low)
                        s = low * .55
                        // the odd heavier drop
                        if (random.nextDouble() < .0009) notes += Note(2200.0 + random.nextDouble() * 1800, n, 90.0, .22, 0.0)
                    }
                    Ambience.OCEAN -> {
                        lower += .018 * (white - lower)
                        low += .12 * (white - low)
                        val wave = sin(2 * PI * t / 9.0)
                        val swell = .35 + .65 * wave * wave
                        s = (lower * 3.2 * swell) + (low * .14 * swell * swell)
                    }
                    Ambience.NATURE -> {
                        lower += .03 * (white - lower)
                        val gust = .6 + .4 * sin(2 * PI * t / 13.0)
                        s = lower * 1.6 * gust
                        if (n >= nextEvent) {
                            chirpAt = n
                            chirpHz = 2600.0 + random.nextDouble() * 1600
                            nextEvent = n + (rate * (3.5 + random.nextDouble() * 6)).toLong()
                        }
                        if (chirpAt >= 0) {
                            val c = (n - chirpAt).toDouble() / rate
                            if (c < .55) {
                                val note = (c * 7).toInt()
                                val within = c * 7 - note
                                if (note % 2 == 0) {
                                    val hz = chirpHz + 900 * within + note * 120
                                    s += sin(2 * PI * hz * c) * .16 * sin(PI * within)
                                }
                            }
                        }
                    }
                    Ambience.PIANO -> {
                        if (n >= nextEvent) {
                            val root = scale[random.nextInt(scale.size)]
                            notes += Note(root / 2, n, 3.2, .30, .35)
                            if (random.nextDouble() < .35) notes += Note(root, n, 2.6, .16, .25)
                            nextEvent = n + (rate * (1.8 + random.nextDouble() * 1.6)).toLong()
                        }
                    }
                    Ambience.HARP -> {
                        if (n >= nextEvent) {
                            val phrase = arpeggio % 10
                            if (phrase < 6) {
                                notes += Note(scale[(arpeggio * 2) % scale.size], n, 1.9, .24, .6)
                                nextEvent = n + (rate * .42).toLong()
                            } else {
                                nextEvent = n + (rate * 1.1).toLong()
                            }
                            arpeggio++
                        }
                    }
                    Ambience.NONE -> Unit
                }
                // plucked and struck notes: a tone and its octave, dying away
                if (notes.isNotEmpty()) {
                    var k = 0
                    while (k < notes.size) {
                        val note = notes[k]
                        val age = (n - note.start).toDouble() / rate
                        val env = exp(-age * (if (note.decay > 20) note.decay else 3.0 / note.decay))
                        if (env < .002) {
                            notes.removeAt(k)
                            continue
                        }
                        val attack = (age * 90).coerceAtMost(1.0)
                        s += note.amp * env * attack *
                            (sin(2 * PI * note.hz * age) + note.bright * sin(4 * PI * note.hz * age))
                        k++
                    }
                }
                buffer[i] = (s * fadeIn * 9_000).coerceIn(-32_000.0, 32_000.0).toInt().toShort()
                n++
            }
            if (audio.write(buffer, 0, buffer.size) < 0) break
        }
    }
}
