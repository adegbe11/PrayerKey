package com.prayerkey.manna.platform

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/*
 * Dates written the way the screens wrote them with java.time's
 * DateTimeFormatter, for the handful of patterns the app uses: EEEE / EEE,
 * d / dd, MMMM / MMM / MM / M, yyyy / uuuu / yy, h / hh / H / HH, mm, ss, a.
 * Text in quotes is literal. Names are English, as the app's copy is.
 */

private val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
private val MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

fun Int.pad2(): String = if (this in 0..9) "0$this" else toString()

private fun render(pattern: String, date: LocalDate?, time: LocalTime?): String = buildString {
    var i = 0
    while (i < pattern.length) {
        val c = pattern[i]
        if (c == '\'') {
            val end = pattern.indexOf('\'', i + 1).let { if (it < 0) pattern.length else it }
            append(pattern, i + 1, end)
            i = end + 1
            continue
        }
        var n = 1
        while (i + n < pattern.length && pattern[i + n] == c) n++
        when (c) {
            'E' -> date?.let { val name = DAYS[it.dayOfWeek.ordinal]; append(if (n >= 4) name else name.take(3)) }
            'd' -> date?.let { append(if (n >= 2) it.dayOfMonth.pad2() else it.dayOfMonth.toString()) }
            'M', 'L' -> date?.let {
                val name = MONTHS[it.monthNumber - 1]
                append(
                    when {
                        n >= 4 -> name
                        n == 3 -> name.take(3)
                        n == 2 -> it.monthNumber.pad2()
                        else -> it.monthNumber.toString()
                    },
                )
            }
            'y', 'u' -> date?.let { append(if (n == 2) (it.year % 100).pad2() else it.year.toString()) }
            'h' -> time?.let { val h = (it.hour % 12).let { h -> if (h == 0) 12 else h }; append(if (n >= 2) h.pad2() else h.toString()) }
            'H' -> time?.let { append(if (n >= 2) it.hour.pad2() else it.hour.toString()) }
            'm' -> time?.let { append(it.minute.pad2()) }
            's' -> time?.let { append(it.second.pad2()) }
            'a' -> time?.let { append(if (it.hour < 12) "AM" else "PM") }
            else -> repeat(n) { append(c) }
        }
        i += n
    }
}

fun LocalDate.format(pattern: String): String = render(pattern, this, null)
fun LocalDateTime.format(pattern: String): String = render(pattern, date, time)
fun LocalTime.format(pattern: String): String = render(pattern, null, this)

/** A moment, on this phone's calendar and clock. */
fun formatMillis(millis: Long, pattern: String): String =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).format(pattern)

/* What java.text.DateFormat gave in English, for the places that used it. */
fun mediumDate(millis: Long): String = formatMillis(millis, "MMM d, yyyy")
fun longDate(millis: Long): String = formatMillis(millis, "MMMM d, yyyy")
fun shortTime(millis: Long): String = formatMillis(millis, "h:mm a")
fun longDateShortTime(millis: Long): String = formatMillis(millis, "MMMM d, yyyy 'at' h:mm a")

fun Long.pad2(): String = if (this in 0L..9L) "0$this" else toString()
