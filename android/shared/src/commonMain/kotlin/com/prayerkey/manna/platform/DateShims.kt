package com.prayerkey.manna.platform

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/*
 * The java.time calls the screens were written with, on kotlinx-datetime, so
 * date logic moved across unchanged rather than rewritten: the same names,
 * the same results.
 */

fun LocalDate.plusDays(days: Long): LocalDate = plus(DatePeriod(days = days.toInt()))
fun LocalDate.plusDays(days: Int): LocalDate = plus(DatePeriod(days = days))
fun LocalDate.minusDays(days: Long): LocalDate = minus(DatePeriod(days = days.toInt()))
fun LocalDate.minusDays(days: Int): LocalDate = minus(DatePeriod(days = days))
fun LocalDate.plusWeeks(weeks: Long): LocalDate = plusDays(weeks * 7)
fun LocalDate.minusWeeks(weeks: Long): LocalDate = minusDays(weeks * 7)
fun LocalDate.plusMonths(months: Long): LocalDate = plus(months.toInt(), DateTimeUnit.MONTH)
fun LocalDate.minusMonths(months: Long): LocalDate = minus(months.toInt(), DateTimeUnit.MONTH)
fun LocalDate.plusMonths(months: Int): LocalDate = plus(months, DateTimeUnit.MONTH)
fun LocalDate.minusMonths(months: Int): LocalDate = minus(months, DateTimeUnit.MONTH)
fun LocalDate.withDayOfMonth(day: Int): LocalDate = LocalDate(year, monthNumber, day)
fun LocalDate.isBefore(other: LocalDate): Boolean = this < other
fun LocalDate.isAfter(other: LocalDate): Boolean = this > other

fun LocalDate.lengthOfMonth(): Int {
    val first = LocalDate(year, monthNumber, 1)
    return first.plus(1, DateTimeUnit.MONTH).minus(DatePeriod(days = 1)).dayOfMonth
}

/** The phone's midnight at the start of this date, in epoch milliseconds. */
fun LocalDate.startOfDayMillis(): Long = atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

fun LocalDate.atStartOfDay(): LocalDateTime = LocalDateTime(year, monthNumber, dayOfMonth, 0, 0)

fun localDateOfEpochDay(day: Long): LocalDate = LocalDate.fromEpochDays(day.toInt())
fun localDateOfEpochDay(day: Int): LocalDate = LocalDate.fromEpochDays(day)

fun LocalDateTime.toLocalDate(): LocalDate = date

/** The calendar day a moment fell on, on this phone. */
fun localDateOfMillis(millis: Long): LocalDate =
    kotlinx.datetime.Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date

/** Milliseconds from now until the next midnight on this phone, at least one second. */
fun millisUntilMidnight(): Long {
    val zone = TimeZone.currentSystemDefault()
    val next = today().plusDays(1).atStartOfDayIn(zone)
    return (next - kotlinx.datetime.Clock.System.now()).inWholeMilliseconds.coerceAtLeast(1_000L)
}
