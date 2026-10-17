package com.prayerkey.manna.ui.platform

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.startOfDayMillis
import kotlinx.datetime.LocalDate

actual fun pickDate(context: PlatformContext, initial: LocalDate, earliest: LocalDate?, onPicked: (LocalDate) -> Unit) {
    DatePickerDialog(
        context,
        { _, y, m, d -> onPicked(LocalDate(y, m + 1, d)) },
        initial.year, initial.monthNumber - 1, initial.dayOfMonth,
    ).apply { earliest?.let { datePicker.minDate = it.startOfDayMillis() } }.show()
}

actual fun pickTime(context: PlatformContext, hour: Int, minute: Int, onPicked: (hour: Int, minute: Int) -> Unit) {
    TimePickerDialog(context, { _, h, m -> onPicked(h, m) }, hour, minute, false).show()
}
