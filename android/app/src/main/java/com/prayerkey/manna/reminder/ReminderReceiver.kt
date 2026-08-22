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
import com.prayerkey.manna.data.PullsProgress
import java.time.LocalDate
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val kind = ReminderKind.from(intent?.getStringExtra(EXTRA_KIND))
        if (kind == ReminderKind.CALENDAR && calendarMessage(LocalDate.now()) == null) {
            scheduleKind(context, kind, true)
            return
        }
        createChannels(context)
        /* The prayer reminder names where you stopped. "Your prayer is ready"
           is an advertisement for the app; "You stopped at six" is a fact about
           the reader's own morning, and only one of the two gets opened. */
        val message = when (kind) {
            ReminderKind.CALENDAR -> calendarMessage(LocalDate.now())!!
            ReminderKind.PRAYER -> PullsProgress.Prayer.notificationLine(context)
            ReminderKind.BIBLE -> PullsProgress.Bible.notificationLine(context)
            else -> kind.message
        }
        val stateful = kind == ReminderKind.CALENDAR || kind == ReminderKind.PRAYER || kind == ReminderKind.BIBLE
        val headline = if (stateful) message.first else kind.title
        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(DESTINATION, kind.destination)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val open = PendingIntent.getActivity(
            context, 8_000 + kind.ordinal, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, kind.channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF6200ED.toInt())
            .setContentTitle(headline)
            .setContentText(if (kind == ReminderKind.CALENDAR) message.second else message.second)
            .setStyle(NotificationCompat.BigTextStyle().bigText(if (kind == ReminderKind.CALENDAR) message.second else message.second))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(1_000 + kind.ordinal, notification)
        }
        scheduleKind(context, kind, true)
    }

    companion object {
        const val DESTINATION = "prayerkey_destination"
        private const val EXTRA_KIND = "reminder_kind"

        fun schedule(context: Context, hour: Int, minute: Int, enabled: Boolean) {
            scheduleAt(context, ReminderKind.PRAYER, hour, minute, enabled)
        }

        fun scheduleDailySuite(context: Context, enabled: Boolean) {
            ReminderKind.entries.forEach { scheduleKind(context, it, enabled) }
        }

        private fun scheduleKind(context: Context, kind: ReminderKind, enabled: Boolean) {
            scheduleAt(context, kind, kind.hour, kind.minute, enabled, kind.dayOfWeek)
        }

        private fun scheduleAt(context: Context, kind: ReminderKind, hour: Int, minute: Int, enabled: Boolean, dayOfWeek: Int? = null) {
            val alarm = context.getSystemService(AlarmManager::class.java)
            val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_KIND, kind.key)
            val pending = PendingIntent.getBroadcast(
                context, 4_000 + kind.ordinal, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            alarm.cancel(pending)
            if (!enabled) return
            val time = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                dayOfWeek?.let { target ->
                    while (get(Calendar.DAY_OF_WEEK) != target || timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
                }
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pending)
        }

        private fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java)
            listOf(
                NotificationChannel("prayer", "Prayer", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel("scripture", "Bible and devotionals", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel("journal", "Journal", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel("church_calendar", "Church and Christian calendar", NotificationManager.IMPORTANCE_DEFAULT),
            ).forEach(manager::createNotificationChannel)
        }

        private fun calendarMessage(date: LocalDate): Pair<String, String>? {
            val easter = easterSunday(date.year)
            return when {
            date == easter.minusDays(46) -> "Ash Wednesday" to "Begin Lent with prayer and repentance."
            date == easter.minusDays(7) -> "Palm Sunday" to "Read Matthew 21:1–11."
            date == easter.minusDays(3) -> "Maundy Thursday" to "Read John 13 and remember the Lord's Supper."
            date == easter.minusDays(2) -> "Good Friday" to "Read the crucifixion account and pray."
            date == easter -> "Easter Sunday" to "Christ is risen. Read John 20."
            date == easter.plusDays(39) -> "Ascension Day" to "Read Acts 1:1–11."
            date == easter.plusDays(49) -> "Pentecost" to "Read Acts 2 and pray for the Church."
            date.monthValue == 1 && date.dayOfMonth == 1 -> "New Year prayer" to "Begin the year in prayer."
            date.monthValue == 1 && date.dayOfMonth == 6 -> "Epiphany" to "Read Matthew 2 today."
            date.monthValue == 4 && date.dayOfMonth == 7 -> "World Health Day" to "Pray for the sick and those who care for them."
            date.monthValue == 10 && date.dayOfMonth == 31 -> "Reformation Day" to "Read Romans 1:16–17."
            date.monthValue == 11 && date.dayOfWeek.value == 7 && date.dayOfMonth >= 27 -> "Advent" to "Prepare your heart for the coming of Christ."
            date.monthValue == 12 && date.dayOfMonth == 25 -> "Christmas Day" to "Read Luke 2 and give thanks for Christ."
            else -> null
            }
        }

        private fun easterSunday(year: Int): LocalDate {
            val a = year % 19
            val b = year / 100
            val c = year % 100
            val d = b / 4
            val e = b % 4
            val f = (b + 8) / 25
            val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30
            val i = c / 4
            val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7
            val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31
            val day = (h + l - 7 * m + 114) % 31 + 1
            return LocalDate.of(year, month, day)
        }
    }
}

private enum class ReminderKind(
    val key: String,
    val title: String,
    val message: Pair<String, String>,
    val destination: String,
    val channel: String,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: Int? = null,
) {
    PRAYER("prayer", "Prayer of the day", "" to "Your prayer is ready.", "prayer", "prayer", 6, 0),
    BIBLE("bible", "Bible reading", "" to "Today's passage is ready.", "bible", "scripture", 7, 30),
    DEVOTIONAL("devotional", "Today's devotional", "" to "Open today's devotional.", "devotional", "scripture", 8, 0),
    PRAYER_CHALLENGE("prayer_challenge", "Prayer challenge", "" to "Today's prayer is ready.", "prayer_challenge", "prayer", 12, 0),
    JOURNAL("journal", "Journal", "" to "What's on your mind today?", "journal", "journal", 20, 30),
    MIDNIGHT("midnight", "Midnight prayer", "" to "The midnight watch is open.", "midnight", "prayer", 23, 55),
    CHURCH("church", "Sunday worship", "" to "Church begins with a ready heart.", "church", "church_calendar", 8, 30, Calendar.SUNDAY),
    CALENDAR("calendar", "PrayerKey", "" to "", "home", "church_calendar", 9, 0),
    ;

    companion object {
        fun from(key: String?): ReminderKind = entries.firstOrNull { it.key == key } ?: PRAYER
    }
}
