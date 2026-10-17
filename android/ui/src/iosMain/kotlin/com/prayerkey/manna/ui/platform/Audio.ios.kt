package com.prayerkey.manna.ui.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL
import platform.darwin.DISPATCH_TIME_FOREVER
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait

/**
 * AVAudioEngine fed buffer by buffer. At most four buffers wait in the
 * player at once; [write] blocks on a semaphore until one finishes, which
 * paces the generator the way AudioTrack's streaming mode does on Android.
 */
@OptIn(ExperimentalForeignApi::class)
private class IosPcmOut(private val rate: Int, volume: Float) : PcmOut {
    private val engine = AVAudioEngine()
    private val node = AVAudioPlayerNode()
    private val format = AVAudioFormat(standardFormatWithSampleRate = rate.toDouble(), channels = 1u)
    private val room = dispatch_semaphore_create(4)
    private var closed = false

    init {
        engine.attachNode(node)
        engine.connect(node, to = engine.mainMixerNode, format = format)
        node.volume = volume
        engine.startAndReturnError(null)
        node.play()
    }

    override fun write(samples: ShortArray, count: Int): Int {
        if (closed) return -1
        dispatch_semaphore_wait(room, DISPATCH_TIME_FOREVER)
        if (closed) return -1
        val buffer = AVAudioPCMBuffer(pCMFormat = format, frameCapacity = count.toUInt()) ?: return -1
        buffer.frameLength = count.toUInt()
        val channel = buffer.floatChannelData!![0]!!
        for (i in 0 until count) channel[i] = samples[i] / 32768f
        node.scheduleBuffer(buffer) { dispatch_semaphore_signal(room) }
        return count
    }

    override fun setVolume(volume: Float) { node.volume = volume }
    override fun pause() { node.pause() }
    override fun resume() { node.play() }
    override fun release() {
        closed = true
        node.stop()
        engine.stop()
        // let a writer blocked on a full output go, so its loop can end
        repeat(4) { dispatch_semaphore_signal(room) }
    }
}

actual fun openPcmOut(rate: Int, volume: Float): PcmOut = IosPcmOut(rate, volume)

/**
 * AVPlayer behind MediaPlayer's shape. AVPlayer reports readiness through
 * key-value observing, which Kotlin cannot subscribe to simply, so a small
 * loop on the main thread watches the item instead - readiness, failure and
 * the end - a few times a second.
 */
@OptIn(ExperimentalForeignApi::class)
actual class StreamPlayer actual constructor() {
    private var player: AVPlayer? = null
    private var onPrepared: () -> Unit = {}
    private var onComplete: () -> Unit = {}
    private var onError: () -> Unit = {}
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var watch: Job? = null
    private var prepared = false
    private var speed = 1f

    actual fun setOnPreparedListener(listener: () -> Unit) { onPrepared = listener }
    actual fun setOnCompletionListener(listener: () -> Unit) { onComplete = listener }
    actual fun setOnErrorListener(listener: () -> Unit) { onError = listener }

    actual fun prepare(url: String) {
        val item = NSURL.URLWithString(url)?.let { AVPlayerItem(uRL = it) } ?: run { onError(); return }
        val p = AVPlayer(playerItem = item)
        player = p
        watch = scope.launch {
            var ended = false
            while (isActive) {
                val status = p.currentItem?.status
                if (!prepared && status == AVPlayerItemStatusReadyToPlay) { prepared = true; onPrepared() }
                if (status == AVPlayerItemStatusFailed) { onError(); break }
                if (prepared && !ended && duration > 0 && currentPosition >= duration - 250 && !isPlaying) {
                    ended = true
                    onComplete()
                }
                delay(250)
            }
        }
    }

    private fun seconds(get: () -> Double): Int = get().takeIf { !it.isNaN() }?.let { (it * 1000).toInt() } ?: 0

    actual val duration: Int
        get() = seconds { player?.currentItem?.duration?.let { CMTimeGetSeconds(it) } ?: 0.0 }
    actual val currentPosition: Int
        get() = seconds { player?.currentTime()?.let { CMTimeGetSeconds(it) } ?: 0.0 }
    actual val isPlaying: Boolean
        get() = player?.timeControlStatus == AVPlayerTimeControlStatusPlaying

    actual fun start() { player?.play(); player?.rate = speed }
    actual fun pause() { player?.pause() }
    actual fun seekTo(ms: Int) { player?.seekToTime(CMTimeMakeWithSeconds(ms / 1000.0, 600)) }
    actual fun setSpeed(speed: Float) {
        this.speed = speed
        if (isPlaying) player?.rate = speed
    }
    actual fun release() {
        watch?.cancel()
        player?.pause()
        player = null
    }
}

actual fun postToMain(block: () -> Unit) = onMain(block)
