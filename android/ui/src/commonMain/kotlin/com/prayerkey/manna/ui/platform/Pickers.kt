package com.prayerkey.manna.ui.platform

import com.prayerkey.manna.platform.PlatformContext
import kotlinx.datetime.LocalDate

/** The phone's date picker; [earliest] is the first day that can be chosen. Android's own dialog; a Material one on iPhone. */
expect fun pickDate(context: PlatformContext, initial: LocalDate, earliest: LocalDate?, onPicked: (LocalDate) -> Unit)

/** The phone's time picker, twelve-hour. Android's own dialog; a Material one on iPhone. */
expect fun pickTime(context: PlatformContext, hour: Int, minute: Int, onPicked: (hour: Int, minute: Int) -> Unit)
