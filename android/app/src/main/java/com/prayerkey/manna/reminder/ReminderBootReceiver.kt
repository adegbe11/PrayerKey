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
        val prefs = MannaStore(context.applicationContext).preferences()
        ReminderReceiver.schedule(context, prefs.reminderHour, prefs.reminderMinute, prefs.reminderEnabled)
    }
}
