package com.prayerkey.manna.ui.platform

import com.prayerkey.manna.data.FastingChallenge
import com.prayerkey.manna.data.PrayerMemory
import com.prayerkey.manna.platform.PlatformContext

/** Until the app installs its alarm receivers at launch, nothing is scheduled. */
actual fun defaultReminders(): ReminderScheduler = object : ReminderScheduler {
    override fun schedule(context: PlatformContext, hour: Int, minute: Int, enabled: Boolean) = Unit
    override fun scheduleFor(context: PlatformContext, key: String, hour: Int, minute: Int, enabled: Boolean) = Unit
    override fun scheduleBibleReading(context: PlatformContext, enabled: Boolean) = Unit
    override fun scheduleDailySuite(context: PlatformContext, enabled: Boolean) = Unit
    override fun scheduleFast(context: PlatformContext, fast: FastingChallenge) = Unit
    override fun cancelFast(context: PlatformContext) = Unit
    override fun schedulePrayerMemory(context: PlatformContext, kept: PrayerMemory.Kept) = Unit
    override fun cancelPrayerMemory(context: PlatformContext, id: Long) = Unit
}
