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
import com.prayerkey.manna.data.FastMachine
import com.prayerkey.manna.data.FastingStore

/**
 * Speaks at the moments of a running fast, planned by [FastMachine]. Each
 * notification opens the fast where it is, and says nothing once the fast has
 * ended, been cleared, or been started again.
 */
class FastingMilestoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val fast = FastingStore.load(context)
        // a fast that was ended, or begun again since this alarm was set
        if (!fast.running || fast.startedAt != intent.getLongExtra(EXTRA_START, -1L)) return
        val moment = runCatching { FastMachine.Moment.valueOf(intent.getStringExtra(EXTRA_MOMENT).orEmpty()) }.getOrNull() ?: return
        val day = intent.getIntExtra(EXTRA_DAY, 1)
        val (title, text) = FastMachine.message(fast, moment, day)

        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(CHANNEL, "Your fast", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(
            context, 3_100,
            Intent(context, MainActivity::class.java).apply {
                putExtra(ReminderReceiver.DESTINATION, "fasting")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.drawable.prayerkey_mark).toIcon(context))
            .setColor(0xFF6200ED.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // one notification for the fast, replaced as it goes, never a pile
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL = "fasting_milestones"
        private const val EXTRA_MOMENT = "moment"
        private const val EXTRA_DAY = "day"
        private const val EXTRA_START = "started_at"
        private const val NOTIFICATION_ID = 3_199
        private const val FIRST_CODE = 3_200
        private const val MAX_MOMENTS = 40

        /** Plans and sets every moment of [fast] still to come. */
        fun schedule(context: Context, fast: com.prayerkey.manna.data.FastingChallenge) {
            cancel(context)
            val alarm = context.getSystemService(AlarmManager::class.java) ?: return
            val now = System.currentTimeMillis() / 1000
            FastMachine.plan(fast).take(MAX_MOMENTS).forEachIndexed { i, planned ->
                if (planned.at <= now) return@forEachIndexed
                val pending = PendingIntent.getBroadcast(
                    context, FIRST_CODE + i,
                    Intent(context, FastingMilestoneReceiver::class.java)
                        .putExtra(EXTRA_MOMENT, planned.moment.name)
                        .putExtra(EXTRA_DAY, planned.day)
                        .putExtra(EXTRA_START, fast.startedAt),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                runCatching { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, planned.at * 1000, pending) }
            }
        }

        /** After a restart, the moments of a fast still running. */
        fun rescheduleRunning(context: Context) {
            val fast = FastingStore.load(context)
            if (fast.running && !fast.finished) schedule(context, fast)
        }

        fun cancel(context: Context) {
            val alarm = context.getSystemService(AlarmManager::class.java) ?: return
            // the old milestone codes (3204-3424) and the new ones share this range
            for (code in FIRST_CODE until FIRST_CODE + 260) {
                PendingIntent.getBroadcast(
                    context, code, Intent(context, FastingMilestoneReceiver::class.java),
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
                )?.let { alarm.cancel(it); it.cancel() }
            }
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
    }
}
