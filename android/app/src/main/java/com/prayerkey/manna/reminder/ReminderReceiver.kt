package com.prayerkey.manna.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.prayerkey.manna.MainActivity
import com.prayerkey.manna.R
import java.util.Calendar
import java.time.LocalDate
import com.prayerkey.manna.data.quoteFor

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "Daily word", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "A quiet reminder to receive today's word"
            })
        }
        val open = PendingIntent.getActivity(context, 2, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val quote = quoteFor(LocalDate.now())
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF2F73EA.toInt())
            .setContentTitle("A new day with God")
            .setContentText(quote.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${quote.text} — ${quote.author}"))
            .setContentIntent(open).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(1001, notification)
        }
    }

    companion object {
        private const val CHANNEL = "daily_word"

        fun schedule(context: Context, hour: Int, minute: Int, enabled: Boolean) {
            val alarm = context.getSystemService(AlarmManager::class.java)
            val pending = PendingIntent.getBroadcast(context, 1, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarm.cancel(pending)
            if (!enabled) return
            val time = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.timeInMillis, AlarmManager.INTERVAL_DAY, pending)
        }
    }
}
