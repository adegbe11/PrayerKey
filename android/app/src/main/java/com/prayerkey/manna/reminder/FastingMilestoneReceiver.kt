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

class FastingMilestoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra("hour", 4)
        val spiritual = intent.getBooleanExtra("spiritual", false)
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(CHANNEL, "Fasting milestones", NotificationManager.IMPORTANCE_DEFAULT))
        val message = when {
            spiritual -> "Open PrayerKey for a 60-second verse and prayer. Let hunger turn your attention toward God."
            hour <= 4 -> "Your stomach might rumble. Slow down and grab a glass of water."
            hour <= 8 -> "You are moving through the hardest early hours. Rest when you can and keep drinking water."
            else -> "Pause and check your body. Break the fast if you feel unwell; faithfulness includes wisdom."
        }
        val open = PendingIntent.getActivity(context, 3200 + hour, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_notification).setColor(0xFF6200ED.toInt())
            .setContentTitle(if (spiritual) "A spiritual key for hour $hour" else "Fasting hour $hour")
            .setContentText(message).setStyle(NotificationCompat.BigTextStyle().bigText(message)).setContentIntent(open).setAutoCancel(true).build()
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) NotificationManagerCompat.from(context).notify(3200 + hour + if (spiritual) 100 else 0, notification)
    }

    companion object {
        private const val CHANNEL = "fasting_milestones"
        fun schedule(context: Context, start: Long, totalHours: Int) {
            val alarm = context.getSystemService(AlarmManager::class.java)
            listOf(4, 8, 12, 16, 24).filter { it < totalHours }.forEach { hour ->
                listOf(false, true).forEach { spiritual ->
                    val code = 3200 + hour + if (spiritual) 100 else 0
                    val pending = PendingIntent.getBroadcast(context, code, Intent(context, FastingMilestoneReceiver::class.java).putExtra("hour", hour).putExtra("spiritual", spiritual), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, start + hour * 3_600_000L + if (spiritual) 60_000L else 0L, pending)
                }
            }
        }

        fun cancel(context: Context) {
            val alarm = context.getSystemService(AlarmManager::class.java)
            listOf(4, 8, 12, 16, 24).forEach { hour ->
                listOf(false, true).forEach { spiritual ->
                    val code = 3200 + hour + if (spiritual) 100 else 0
                    val pending = PendingIntent.getBroadcast(
                        context,
                        code,
                        Intent(context, FastingMilestoneReceiver::class.java),
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
                    )
                    if (pending != null) {
                        alarm.cancel(pending)
                        pending.cancel()
                    }
                }
            }
        }
    }
}
