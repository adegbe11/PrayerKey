package com.prayerkey.manna.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.prayerkey.manna.MainActivity
import com.prayerkey.manna.R
import com.prayerkey.manna.data.PrayerMemory

/**
 * A week after a prayer is kept, the machine asks whether God has answered.
 * Tapping it opens the question in the app: answered, or still praying.
 */
class PrayerMemoryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, 0L)
        val kept = PrayerMemory.find(context, id) ?: return
        // answered already, or prayed again since this alarm was set
        if (kept.answered || System.currentTimeMillis() < PrayerMemory.followUpAt(kept.prayedAt) - 60_000L) return

        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Answered prayer", NotificationManager.IMPORTANCE_DEFAULT),
        )
        val open = PendingIntent.getActivity(
            context, code(id),
            Intent(context, MainActivity::class.java).apply {
                putExtra(ReminderReceiver.DESTINATION, "answered:$id")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = "On ${kept.prayedLabel()} you prayed: ${kept.request}"
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.drawable.prayerkey_mark).toIcon(context))
            .setColor(0xFF6200ED.toInt())
            .setContentTitle("Has God answered?")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(code(id), notification)
        }
    }

    companion object {
        private const val CHANNEL = "answered_prayer"
        private const val EXTRA_ID = "kept_id"

        /** One alarm per remembered prayer, in its own range. */
        private fun code(id: Long): Int = 50_000 + (id % 10_000).toInt()

        fun schedule(context: Context, kept: PrayerMemory.Kept) {
            val alarm = context.getSystemService(AlarmManager::class.java) ?: return
            val pending = PendingIntent.getBroadcast(
                context, code(kept.id),
                Intent(context, PrayerMemoryReceiver::class.java).putExtra(EXTRA_ID, kept.id),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            runCatching { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, PrayerMemory.followUpAt(kept.prayedAt), pending) }
        }

        fun cancel(context: Context, id: Long) {
            val alarm = context.getSystemService(AlarmManager::class.java) ?: return
            PendingIntent.getBroadcast(
                context, code(id), Intent(context, PrayerMemoryReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )?.let { alarm.cancel(it); it.cancel() }
            NotificationManagerCompat.from(context).cancel(code(id))
        }

        /** After a restart Android has forgotten every alarm; ask again about what is still waiting. */
        fun rescheduleAll(context: Context) = PrayerMemory.waiting(context).forEach { schedule(context, it) }
    }
}
