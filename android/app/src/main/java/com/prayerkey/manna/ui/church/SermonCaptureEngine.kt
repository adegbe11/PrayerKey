package com.prayerkey.manna.ui.church

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/** One AudioRecord, two consumers: Vosk live captions and AAC durable master. */
class SermonCaptureEngine(
    context: Context,
    private val onSegment: (SermonSegment) -> Unit,
    private val onPartial: (String) -> Unit,
    private val onStatus: (String) -> Unit,
) {
    private val app = context.applicationContext
    private val running = AtomicBoolean(false)
    private var audioRecord: AudioRecord? = null
    private var codec: MediaCodec? = null
    private var muxer: MediaMuxer? = null
    private var track = -1
    private var muxerStarted = false
    private var capture: Thread? = null
    private var samples = 0L
    private var segmentStart = 0L
    private var speech: SpeechEngine? = null
    lateinit var audioFile: File
        private set
    val segments = mutableListOf<SermonSegment>()

    @SuppressLint("MissingPermission")
    fun start(startedAt: Long) {
        if (!running.compareAndSet(false, true)) return
        val dir = SermonSessionStore.directory(app)
        audioFile = File(dir, "sermon_${startedAt}.m4a")
        prepareSpeech(dir)
        prepareEncoder()
        val min = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(min, 32_000))
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) { running.set(false); onStatus("Microphone could not be opened"); return }
        SermonSessionStore.write(audioFile, startedAt, "recording", segments)
        audioRecord?.startRecording()
        onStatus(if (speech == null) "Recording · captions are not ready yet" else "Listening · recording safely")
        capture = thread(name = "sermon-capture", isDaemon = true) { captureLoop(startedAt) }
    }

    fun stop(startedAt: Long) {
        if (!running.compareAndSet(true, false)) return

        /* Wait for the capture thread before touching the encoder.
           Without this it kept calling queueInputBuffer while stop() was
           already tearing the MediaCodec down, and the thread died with
           "queueInputBuffer() is valid only at Executing states; currently
           during stop()". Clearing `running` is not enough on its own — the
           thread can be blocked inside a 3200-byte read at that moment and
           feeds one more buffer after it returns. Stopping the microphone
           first unblocks that read, and the join makes the ordering certain. */
        runCatching { audioRecord?.stop() }
        runCatching { capture?.join(1_500) }
        capture = null
        runCatching { audioRecord?.release() }; audioRecord = null
        drainEncoder(endOfStream = true)
        runCatching { codec?.stop() }; runCatching { codec?.release() }; codec = null
        if (muxerStarted) runCatching { muxer?.stop() }
        runCatching { muxer?.release() }; muxer = null
        /* Whatever was still being spoken when End was pressed. Without this
           the last sentence of every sermon was lost. */
        speech?.finish()?.let { tail ->
            synchronized(segments) { segments += SermonSegment(segmentStart, samples * 1000L / RATE, tail) }
        }
        runCatching { speech?.release() }; speech = null
        SermonSessionStore.write(audioFile, startedAt, "complete", segments)
        onStatus("Recording saved")
    }

    private fun captureLoop(startedAt: Long) {
        val pcm = ByteArray(3200)
        while (running.get()) {
            val count = audioRecord?.read(pcm, 0, pcm.size, AudioRecord.READ_BLOCKING) ?: break
            if (count <= 0) continue
            feedEncoder(pcm, count)
            val now = samples * 1000L / RATE
            speech?.let { engine ->
                engine.accept(pcm, count, onPartial)?.let { text ->
                    val segment = SermonSegment(segmentStart, now, text)
                    synchronized(segments) {
                        segments += segment
                        SermonSessionStore.write(audioFile, startedAt, "recording", segments)
                    }
                    segmentStart = now
                    onSegment(segment)
                }
            }
            samples += count / 2
        }
    }

    /**
     * Opens the offline recogniser, if the model has been installed.
     *
     * This used to test `exists() && list() not empty`, which passes for a
     * directory left behind by an interrupted unzip — and `Model()` on a
     * partial model dies in native code. [VoskModel.isInstalled] checks for the
     * subfolders Vosk actually reads.
     */
    private fun prepareSpeech(dir: File) {
        if (!SpeechModel.isInstalled(app, SpeechModel.Kind.Speech)) return
        speech = SpeechEngine.open(SpeechModel.directory(app, SpeechModel.Kind.Speech))
    }

    private fun prepareEncoder() {
        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
            configure(MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, RATE, 1).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, 64_000); setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16_384)
            }, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE); start()
        }
        muxer = MediaMuxer(audioFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    private fun feedEncoder(bytes: ByteArray, count: Int) {
        // the encoder is gone the moment stop() runs; never queue into it after
        if (!running.get()) return
        var offset = 0
        while (offset < count) {
            val index = codec?.dequeueInputBuffer(10_000) ?: -1
            if (index < 0) { drainEncoder(false); continue }
            val input = codec!!.getInputBuffer(index) ?: continue
            input.clear(); val amount = minOf(input.remaining(), count - offset); input.put(bytes, offset, amount)
            codec!!.queueInputBuffer(index, 0, amount, samples * 1_000_000L / RATE, 0); offset += amount
            drainEncoder(false)
        }
    }

    private fun drainEncoder(endOfStream: Boolean) {
        if (endOfStream) codec?.let { c -> c.dequeueInputBuffer(10_000).takeIf { it >= 0 }?.let { c.queueInputBuffer(it, 0, 0, samples * 1_000_000L / RATE, MediaCodec.BUFFER_FLAG_END_OF_STREAM) } }
        val info = MediaCodec.BufferInfo()
        while (true) {
            val index = codec?.dequeueOutputBuffer(info, if (endOfStream) 10_000 else 0) ?: break
            when {
                index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> if (!muxerStarted) { track = muxer!!.addTrack(codec!!.outputFormat); muxer!!.start(); muxerStarted = true }
                index >= 0 -> {
                    if (info.size > 0 && muxerStarted) codec!!.getOutputBuffer(index)?.let { it.position(info.offset); it.limit(info.offset + info.size); muxer!!.writeSampleData(track, it, info) }
                    codec!!.releaseOutputBuffer(index, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                }
                else -> break
            }
        }
    }

    companion object { private const val RATE = 16_000 }
}
