package com.prayerkey.manna.ui.platform

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.prayerkey.manna.platform.PlatformContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/** The open picker, if any, drawn by [PickerHost] from the root overlay. */
private object IosPickers {
    var date by mutableStateOf<DateRequest?>(null)
    var time by mutableStateOf<TimeRequest?>(null)
}

private class DateRequest(val initial: LocalDate, val earliest: LocalDate?, val onPicked: (LocalDate) -> Unit)
private class TimeRequest(val hour: Int, val minute: Int, val onPicked: (Int, Int) -> Unit)

actual fun pickDate(context: PlatformContext, initial: LocalDate, earliest: LocalDate?, onPicked: (LocalDate) -> Unit) {
    IosPickers.date = DateRequest(initial, earliest, onPicked)
}

actual fun pickTime(context: PlatformContext, hour: Int, minute: Int, onPicked: (hour: Int, minute: Int) -> Unit) {
    IosPickers.time = TimeRequest(hour, minute, onPicked)
}

/* Material's date picker works in UTC midnights; these convert at the edges. */
private fun LocalDate.utcMillis() = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
private fun utcDate(millis: Long) = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PickerHost() {
    IosPickers.date?.let { request ->
        val earliest = request.earliest?.utcMillis()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = request.initial.utcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = earliest == null || utcTimeMillis >= earliest
            },
        )
        DatePickerDialog(
            onDismissRequest = { IosPickers.date = null },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { request.onPicked(utcDate(it)) }
                    IosPickers.date = null
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { IosPickers.date = null }) { Text("Cancel") } },
        ) { DatePicker(state) }
    }
    IosPickers.time?.let { request ->
        val state = rememberTimePickerState(request.hour, request.minute, is24Hour = false)
        AlertDialog(
            onDismissRequest = { IosPickers.time = null },
            confirmButton = {
                TextButton(onClick = {
                    request.onPicked(state.hour, state.minute)
                    IosPickers.time = null
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { IosPickers.time = null }) { Text("Cancel") } },
            text = { TimePicker(state) },
        )
    }
}
