package com.prayerkey.manna.reminder

import com.prayerkey.manna.data.FastingChallenge
import com.prayerkey.manna.data.PrayerMemory
import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.ui.platform.ReminderScheduler

/** The shared screens' reminders, set as Android always has: by the alarm receivers. */
object AndroidReminders : ReminderScheduler {
    override fun schedule(context: PlatformContext, hour: Int, minute: Int, enabled: Boolean) =
        ReminderReceiver.schedule(context, hour, minute, enabled)
    override fun scheduleFor(context: PlatformContext, key: String, hour: Int, minute: Int, enabled: Boolean) =
        ReminderReceiver.scheduleFor(context, key, hour, minute, enabled)
    override fun scheduleBibleReading(context: PlatformContext, enabled: Boolean) =
        ReminderReceiver.scheduleBibleReading(context, enabled)
    override fun scheduleDailySuite(context: PlatformContext, enabled: Boolean) =
        ReminderReceiver.scheduleDailySuite(context, enabled)
    override fun scheduleFast(context: PlatformContext, fast: FastingChallenge) =
        FastingMilestoneReceiver.schedule(context, fast)
    override fun cancelFast(context: PlatformContext) = FastingMilestoneReceiver.cancel(context)
    override fun schedulePrayerMemory(context: PlatformContext, kept: PrayerMemory.Kept) =
        PrayerMemoryReceiver.schedule(context, kept)
    override fun cancelPrayerMemory(context: PlatformContext, id: Long) = PrayerMemoryReceiver.cancel(context, id)
}
