package com.prayerkey.manna.ui.church

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import com.prayerkey.manna.MainActivity
import com.prayerkey.manna.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Keeps listening with the screen off and the phone in a pocket.
 *
 * A plain composable cannot do this — Android throttles the mic and kills
 * background work the moment the activity stops. A foreground service with
 * a microphone type and a wake lock is the only way the promise holds for a
 * whole service.
 *
 * State lives in the companion object so the UI can come and go (rotate,
 * background, return) while the sermon keeps recording underneath.
 */
class SermonService : Service() {

    private var capture: SermonCaptureEngine? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopListening(); return START_NOT_STICKY }
            else -> startListening()
        }
        return START_STICKY
    }

    private fun startListening() {
        if (_listening.value) return

        startForeground(NOTIFICATION_ID, buildNotification(0))

        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "manna:sermon")
            .apply { runCatching { acquire(4 * 60 * 60 * 1000L) } }   // hard ceiling, never leaks

        _startedAt.value = System.currentTimeMillis()
        _chunks.value = emptyList()
        _references.value = emptyList()
        _partial.value = ""
        _listening.value = true

        // The previous implementation only streamed PCM to a Socket.IO server.
        // When that server was absent (the debug build points at 10.0.2.2:3001),
        // audio was saved but no words could ever reach the UI. Android's
        // recognizer is the dependable first engine: it produces partial and
        // final text directly on the device and continuously restarts between
        // utterances for a full sermon.
        capture = SermonCaptureEngine(
            context = this,
            onSegment = { segment ->
                _segments.value = _segments.value + segment
                _chunks.value = _chunks.value + segment.text
                _partial.value = ""
                harvestReferences(segment.text)
            },
            onPartial = { _partial.value = it },
            onStatus = { _status.value = it },
        ).also { it.start(_startedAt.value) }
    }

    /**
     * Reference scanning runs per finished chunk — not per partial result.
     * Partials fire many times a second; scanning those would burn battery
     * for nothing and re-report the same verse over and over.
     */
    private fun harvestReferences(chunk: String) {
        val hits = ReferenceDetector.scan(chunk)
        if (hits.isEmpty()) return
        val existing = _references.value
        val fresh = hits.map { it.reference }.filter { ref -> existing.none { it.reference == ref } }
        if (fresh.isEmpty()) return
        val now = System.currentTimeMillis()
        _references.value = existing + fresh.map { Caught(it, now) }
        notifyManager().notify(NOTIFICATION_ID, buildNotification(_references.value.size))
    }

    private fun stopListening() {
        capture?.stop(_startedAt.value)
        _audioPath.value = capture?.audioFile?.absolutePath.orEmpty()
        capture = null
        runCatching { wakeLock?.takeIf { it.isHeld }?.release() }
        wakeLock = null
        _listening.value = false
        _partial.value = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
        else @Suppress("DEPRECATION") stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        capture?.stop(_startedAt.value)
        _audioPath.value = capture?.audioFile?.absolutePath.orEmpty()
        capture = null
        runCatching { wakeLock?.takeIf { it.isHeld }?.release() }
        _listening.value = false
        super.onDestroy()
    }

    private fun notifyManager() = getSystemService(NotificationManager::class.java)

    private fun buildNotification(caught: Int): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifyManager().createNotificationChannel(
                NotificationChannel(CHANNEL, "Church listening", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Shows while Manna is taking your sermon notes"
                    setShowBadge(false)
                },
            )
        }
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, SermonService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Builder(this, CHANNEL)
            .setContentTitle("Taking your notes")
            .setContentText(if (caught == 0) "Listening for scripture" else "$caught scriptures caught")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(open)
            .setOngoing(true)
            .setUsesChronometer(true)
            .setWhen(_startedAt.value.takeIf { it > 0 } ?: System.currentTimeMillis())
            .addAction(Notification.Action.Builder(null as android.graphics.drawable.Icon?, "End service", stop).build())
            .build()
    }

    data class Caught(val reference: String, val atMillis: Long)

    companion object {
        private const val CHANNEL = "manna.church"
        private const val NOTIFICATION_ID = 4201
        const val ACTION_STOP = "com.prayerkey.manna.STOP_SERMON"

        private val _listening = MutableStateFlow(false)
        val listening = _listening.asStateFlow()
        private val _chunks = MutableStateFlow<List<String>>(emptyList())
        val chunks = _chunks.asStateFlow()
        private val _segments = MutableStateFlow<List<SermonSegment>>(emptyList())
        val segments = _segments.asStateFlow()
        private val _references = MutableStateFlow<List<Caught>>(emptyList())
        val references = _references.asStateFlow()
        private val _partial = MutableStateFlow("")
        val partial = _partial.asStateFlow()
        private val _status = MutableStateFlow("Ready")
        val status = _status.asStateFlow()
        private val _audioPath = MutableStateFlow("")
        val audioPath = _audioPath.asStateFlow()
        private val _startedAt = MutableStateFlow(0L)
        val startedAt = _startedAt.asStateFlow()
        private val _language = MutableStateFlow("")
        val language = _language.asStateFlow()

        /** Set before start(); the recogniser reads it when it spins up. */
        fun setLanguage(tag: String) { _language.value = tag }

        /** Chunks joined with a separator the arranger treats as a full stop. */
        fun transcript(): String = SermonCleanup.clean(_segments.value)

        fun elapsedMinutes(): Int {
            val started = _startedAt.value
            if (started == 0L) return 0
            return (((System.currentTimeMillis() - started) / 60_000L).toInt()).coerceAtLeast(1)
        }

        fun start(context: Context) {
            val intent = Intent(context, SermonService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
            else context.startService(intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, SermonService::class.java).setAction(ACTION_STOP))
        }

        /** Clears the finished session once its note has been saved. */
        fun reset() {
            _chunks.value = emptyList()
            _segments.value = emptyList()
            _references.value = emptyList()
            _partial.value = ""
            _startedAt.value = 0L
            _audioPath.value = ""
        }
    }
}
