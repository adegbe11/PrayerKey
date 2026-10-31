package com.prayerkey.manna.data

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * The season of prayer the church is in today.
 *
 * Churches pray in seasons: the January fast, Lent, Holy Week, the days before
 * Pentecost, Advent, the crossover night. Each one is a reason to pray that
 * someone already has before they open the app, so Today names the season
 * and opens the flow built for it. Between seasons, the first week of every
 * month is a midnight prayer week; the rest of the year has no banner at all.
 */
data class PrayerSeason(
    val id: String,
    val title: String,
    val line: String,
    /** What the season's button opens: "fast", "challenge", "midnight" or "points". */
    val action: String,
    val button: String,
)

fun seasonFor(date: LocalDate): PrayerSeason? {
    val easter = easterSunday(date.year)
    val ashWednesday = easter.minus(DatePeriod(days = 46))
    val palmSunday = easter.minus(DatePeriod(days = 7))
    val pentecost = easter.plus(DatePeriod(days = 49))
    val christmas = LocalDate(date.year, Month.DECEMBER, 25)
    // Advent begins on the fourth Sunday before Christmas
    val adventStart = christmas.minus(DatePeriod(days = christmas.dayOfWeek.ordinal + 1 + 21))

    fun dayOf(start: LocalDate) = date.toEpochDays() - start.toEpochDays() + 1

    return when {
        date.month == Month.JANUARY && date.dayOfMonth <= 21 -> PrayerSeason(
            "january-fast", "21 days of prayer and fasting",
            "Day ${date.dayOfMonth} of 21. Begin the year with God.", "fast", "Join the fast",
        )
        date == easter || date in easter..easter.plus(DatePeriod(days = 6)) -> PrayerSeason(
            "easter", "He is risen",
            "Easter week. Pray with the joy of the empty tomb.", "points", "Pray today's points",
        )
        date >= palmSunday && date < easter -> PrayerSeason(
            "holy-week", "Holy Week",
            "From Palm Sunday to the cross. Walk the week in prayer.", "challenge", "Pray through the week",
        )
        date >= ashWednesday && date < palmSunday -> PrayerSeason(
            "lent", "Lent",
            "Day ${dayOf(ashWednesday)} of 40. A season to pray and turn back to God.", "challenge", "Start the 40 days",
        )
        date >= pentecost.minus(DatePeriod(days = 9)) && date <= pentecost -> PrayerSeason(
            "pentecost", "10 days to Pentecost",
            "The disciples prayed ten days for the Holy Spirit. Pray with them.", "points", "Pray today's points",
        )
        date >= adventStart && date < christmas -> PrayerSeason(
            "advent", "Advent",
            "Prepare your heart for the coming of Christ.", "points", "Pray today's points",
        )
        date.month == Month.DECEMBER && date.dayOfMonth == 31 -> PrayerSeason(
            "crossover", "Crossover night",
            "Cross into the new year in prayer tonight.", "midnight", "Pray at midnight",
        )
        date >= christmas && date.month == Month.DECEMBER -> PrayerSeason(
            "christmas", "Christmas",
            "Give thanks for Christ, and get ready for the new year.", "points", "Pray today's points",
        )
        date.dayOfMonth <= 7 -> PrayerSeason(
            "midnight-week", "Midnight prayer week",
            "The first week of the month. Wake and pray at midnight.", "midnight", "Pray at midnight",
        )
        else -> null
    }
}

/** Western Easter Sunday (the anonymous Gregorian algorithm). */
fun easterSunday(year: Int): LocalDate {
    val a = year % 19
    val b = year / 100
    val c = year % 100
    val d = b / 4
    val e = b % 4
    val f = (b + 8) / 25
    val g = (b - f + 1) / 3
    val h = (19 * a + b - d - g + 15) % 30
    val i = c / 4
    val k = c % 4
    val l = (32 + 2 * e + 2 * i - h - k) % 7
    val m = (a + 11 * h + 22 * l) / 451
    val month = (h + l - 7 * m + 114) / 31
    val day = (h + l - 7 * m + 114) % 31 + 1
    return LocalDate(year, month, day)
}
