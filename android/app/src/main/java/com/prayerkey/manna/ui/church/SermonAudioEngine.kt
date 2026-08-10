package com.prayerkey.manna.ui.church

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.prayerkey.manna.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.net.URI
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/** Continuous, gap-free sermon capture. Audio is always written locally first. */
class SermonAudioEngine(
    context: Context,
    private val language: String,
    private val onFinal: (String) -> Unit,
    private val onPartial: (String) -> Unit,
    private val onStatus: (String) -> Unit,
) {
    private val app = context.applicationContext
    private val running = AtomicBoolean(false)
    private val backfilling = AtomicBoolean(false)
    private val pending = ConcurrentLinkedQueue<ByteArray>()
    private val serviceId = UUID.randomUUID().toString()
    private var socket: Socket? = null
    private var recorder: AudioRecord? = null
    private var wav: RandomAccessFile? = null
    private var pcmBytes = 0L
    var audioFile: File? = null
        private set

    @SuppressLint("MissingPermission")
    fun start() {
        if (!running.compareAndSet(false, true)) return
        val min = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
        val bufferSize = maxOf(min, SAMPLE_RATE * 2)
        recorder = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize)
        if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
            running.set(false); onStatus("This microphone could not be opened"); return
        }
        openAudioMaster()
        connectSocket()
        recorder?.startRecording()
        thread(name = "manna-sermon-audio", isDaemon = true) {
            val buffer = ByteArray(3200) // 100 ms of mono 16 kHz PCM16
            while (running.get()) {
                val count = recorder?.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING) ?: -1
                if (count > 0) {
                    val chunk = buffer.copyOf(count)
                    synchronized(this) { wav?.write(chunk); pcmBytes += count }
                    val active = socket
                    if (active?.connected() == true && !backfilling.get()) active.emit("audio:chunk", chunk)
                    else enqueue(chunk)
                }
            }
        }
        onStatus("Recording safely · connecting")
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        runCatching { recorder?.stop() }
        recorder?.release(); recorder = null
        socket?.emit("service:leave", serviceId)
        socket?.disconnect(); socket?.off(); socket = null
        synchronized(this) { finalizeWave(); wav?.close(); wav = null }
        pending.clear()
        onStatus("Audio master saved")
    }

    private fun connectSocket() {
        val options = IO.Options.builder().setReconnection(true).setReconnectionAttempts(Int.MAX_VALUE)
            .setReconnectionDelay(500).setReconnectionDelayMax(5_000).setTransports(arrayOf("websocket")).build()
        socket = IO.socket(URI.create(BuildConfig.SERMON_SOCKET_URL), options).apply {
            on(Socket.EVENT_CONNECT) {
                emit("service:join", serviceId)
                emit("service:translation", "NIV", serviceId)
                emit("service:language", language.ifBlank { "en-US" }, serviceId)
                flushPendingInRealTime(this)
                onStatus("Live transcript · audio backed up")
            }
            on(Socket.EVENT_DISCONNECT) { onStatus("Network lost · audio still safe") }
            on(Socket.EVENT_CONNECT_ERROR) { onStatus("Offline · audio still safe") }
            on("transcript:update") { args ->
                val value = args.firstOrNull() as? JSONObject ?: return@on
                val text = value.optString("text").trim()
                if (text.isNotEmpty()) if (value.optBoolean("isFinal")) onFinal(text) else onPartial(text)
            }
            connect()
        }
    }

    private fun enqueue(chunk: ByteArray) {
        pending.add(chunk)
        // Keep the most recent five minutes for automatic reconnection; the full
        // session remains in the WAV master regardless of network duration.
        while (pending.size > 3000) pending.poll()
    }

    /** Deepgram consumes live PCM. Replaying an outage at CPU speed destroys
     * word timing, so queued 100 ms frames are restored at their real cadence. */
    private fun flushPendingInRealTime(active: Socket) {
        if (!backfilling.compareAndSet(false, true)) return
        thread(name = "manna-sermon-reconnect", isDaemon = true) {
            try {
                while (running.get() && active.connected()) {
                    val chunk = pending.poll() ?: break
                    active.emit("audio:chunk", chunk)
                    Thread.sleep(95)
                }
            } finally {
                backfilling.set(false)
                if (pending.isNotEmpty() && running.get() && active.connected()) {
                    flushPendingInRealTime(active)
                }
            }
        }
    }

    private fun openAudioMaster() {
        val dir = File(app.filesDir, "sermons").apply { mkdirs() }
        dir.listFiles { file -> file.extension.equals("wav", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(2)
            ?.forEach { runCatching { it.delete() } }
        audioFile = File(dir, "sermon-${System.currentTimeMillis()}.wav")
        wav = RandomAccessFile(audioFile!!, "rw").apply { setLength(0); write(ByteArray(44)) }
    }

    private fun finalizeWave() {
        val file = wav ?: return
        file.seek(0)
        val dataSize = pcmBytes.toInt()
        file.writeBytes("RIFF"); file.writeLe32(dataSize + 36); file.writeBytes("WAVE")
        file.writeBytes("fmt "); file.writeLe32(16); file.writeLe16(1); file.writeLe16(1)
        file.writeLe32(SAMPLE_RATE); file.writeLe32(SAMPLE_RATE * 2); file.writeLe16(2); file.writeLe16(16)
        file.writeBytes("data"); file.writeLe32(dataSize)
    }

    private fun RandomAccessFile.writeLe16(value: Int) { write(value and 0xff); write(value ushr 8 and 0xff) }
    private fun RandomAccessFile.writeLe32(value: Int) { writeLe16(value and 0xffff); writeLe16(value ushr 16 and 0xffff) }

    companion object {
        private const val SAMPLE_RATE = 16_000
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }
}
