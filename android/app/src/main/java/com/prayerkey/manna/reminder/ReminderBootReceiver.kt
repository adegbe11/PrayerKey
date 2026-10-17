package com.prayerkey.manna.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prayerkey.manna.data.MannaStore

/** Restores the daily reminder after Android clears alarms on reboot/update. */
class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        val prefs = MannaStore.of(context).preferences()
        ReminderReceiver.scheduleDailySuite(context, prefs.reminderEnabled)
        ReminderReceiver.schedule(context, prefs.reminderHour, prefs.reminderMinute, prefs.reminderEnabled)
        // prayers still waiting to be asked about
        PrayerMemoryReceiver.rescheduleAll(context)
        // the moments of a fast that is still running
        FastingMilestoneReceiver.rescheduleRunning(context)
    }
}
