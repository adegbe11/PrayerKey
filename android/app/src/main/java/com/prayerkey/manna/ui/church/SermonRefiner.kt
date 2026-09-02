package com.prayerkey.manna.ui.church

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

/**
 * A second, better transcript, made after the sermon from the audio we kept.
 *
 * Live captioning and a good transcript pull in opposite directions. Captions
 * have to keep up with speech on a phone, which caps how large a model can be;
 * accuracy wants the largest model that will fit. The usual answer is to pick
 * one and live with it.
 *
 * We do not have to. [SermonCaptureEngine] already writes a full AAC master
 * alongside the live captions — it was built as a safety net for the case where
 * recognition fails — and that recording is a perfectly good input to run again
 * later. So the small model captions the sermon as it happens, and afterwards,
 * with no clock running, the larger model reads the same audio properly.
 *
 * The user gets both: something to watch during the sermon, and a transcript
 * worth keeping afterwards. Nothing is uploaded and nothing is paid for.
 *
 * This is deliberately not automatic. It is a few minutes of sustained CPU on
 * a device that may be on battery, so it is offered rather than imposed — and
 * offered *after* the first transcript exists, which is the moment its value is
 * obvious rather than theoretical.
 */
object SermonRefiner {

    sealed interface Progress {
        data object Idle : Progress
        data class Working(val fraction: Float) : Progress
        data class Done(val transcript: String) : Progress
        data class Failed(val reason: String) : Progress
    }

    /**
     * Re-transcribes [audio] with the Studio model.
     *
     * [onProgress] is called with how far through the recording we are, which
     * is worth showing: on a long sermon this is minutes of work, and an
     * unexplained spinner for four minutes reads as a hang.
     */
    suspend fun refine(
        context: Context,
        audio: File,
        onProgress: (Float) -> Unit = {},
    ): Result<String> = withContext(Dispatchers.Default) {
        val app = context.applicationContext
        if (!SpeechModel.isInstalled(app, SpeechModel.Kind.Speech)) {
            return@withContext Result.failure(IllegalStateException("the sharper listening is not installed yet"))
        }
        if (!audio.isFile || audio.length() == 0L) {
            return@withContext Result.failure(IllegalStateException("the recording is missing"))
        }

        var engine: SpeechEngine? = null
        runCatching {
            engine = SpeechEngine.open(SpeechModel.directory(app, SpeechModel.Kind.Speech))
                ?: error("the model could not be opened")
            val words = StringBuilder()
            decode(audio, onProgress) { pcm, size ->
                engine!!.accept(pcm, size) { }?.let { words.append(it).append(' ') }
            }
            engine!!.finish()?.let { words.append(it) }
            words.toString().trim().ifEmpty { error("no speech was recognised in the recording") }
        }.also {
            runCatching { engine?.release() }
        }
    }

    /**
     * Decodes the AAC master to raw 16-bit PCM and hands it over in chunks.
     *
     * No resampling: [SermonCaptureEngine] records at 16 kHz mono, which is
     * already exactly what the recogniser wants, so the decoded stream can go straight
     * into the recogniser. If that ever changes this needs a resampler, so it
     * checks rather than assuming.
     */
    private inline fun decode(audio: File, onProgress: (Float) -> Unit, sink: (ByteArray, Int) -> Unit) {
        val extractor = MediaExtractor()
        extractor.setDataSource(audio.absolutePath)

        val track = (0 until extractor.trackCount).firstOrNull {
            extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        } ?: error("the recording has no audio track")

        extractor.selectTrack(track)
        val format = extractor.getTrackFormat(track)
        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        check(sampleRate == RATE && channels == 1) {
            "expected 16 kHz mono, found $sampleRate Hz with $channels channel(s)"
        }
        val durationUs = format.getLong(MediaFormat.KEY_DURATION).coerceAtLeast(1L)

        val codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
        codec.configure(format, null, null, 0)
        codec.start()

        try {
            val info = MediaCodec.BufferInfo()
            var sawInputEnd = false
            var sawOutputEnd = false

            while (!sawOutputEnd) {
                if (!sawInputEnd) {
                    val index = codec.dequeueInputBuffer(10_000)
                    if (index >= 0) {
                        val buffer: ByteBuffer = codec.getInputBuffer(index)!!
                        val size = extractor.readSampleData(buffer, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            sawInputEnd = true
                        } else {
                            codec.queueInputBuffer(index, 0, size, extractor.sampleTime, 0)
                            onProgress((extractor.sampleTime.toFloat() / durationUs).coerceIn(0f, 1f))
                            extractor.advance()
                        }
                    }
                }

                val out = codec.dequeueOutputBuffer(info, 10_000)
                if (out >= 0) {
                    if (info.size > 0) {
                        val buffer = codec.getOutputBuffer(out)!!
                        val chunk = ByteArray(info.size)
                        buffer.position(info.offset)
                        buffer.get(chunk, 0, info.size)
                        sink(chunk, chunk.size)
                    }
                    codec.releaseOutputBuffer(out, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) sawOutputEnd = true
                }
            }
        } finally {
            runCatching { codec.stop() }
            runCatching { codec.release() }
            runCatching { extractor.release() }
        }
        onProgress(1f)
    }

    private const val RATE = 16_000
}
