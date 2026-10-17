package com.prayerkey.manna.ui.platform

import com.prayerkey.manna.platform.PlatformContext
import kotlinx.datetime.LocalDate

// The web build is a compile check only.
actual fun pickDate(context: PlatformContext, initial: LocalDate, earliest: LocalDate?, onPicked: (LocalDate) -> Unit) = Unit
actual fun pickTime(context: PlatformContext, hour: Int, minute: Int, onPicked: (hour: Int, minute: Int) -> Unit) = Unit
