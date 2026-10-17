package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * What was written in the morning, so the evening can ask about it.
 *
 * This is the thread between the two offices and the reason they are one thing
 * rather than two screens. In the morning you write what you need; that night
 * the Thank movement says *"this morning you asked"* and puts it in front of
 * you, and asks how it went. Without this the evening's ledger note would have
 * to be invented, and an invented memory is worse than no memory.
 *
 * Kept by date. Nothing is deleted — a year of one-line asks is a few
 * kilobytes, and it is the only record of what somebody was carrying on a
 * particular Tuesday.
 */
object OfficeStore {

    private const val FILE = "daily_office"

    private fun prefs(context: PlatformContext) =
        context.keyValues(FILE)

    private fun key(prefix: String, date: LocalDate) = "$prefix${date.toEpochDays()}"

    /** What was asked for on [date]. Blank when nothing was written. */
    fun morningAsk(context: PlatformContext, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): String =
        prefs(context).getString(key("ask_", date), "").orEmpty()

    fun setMorningAsk(context: PlatformContext, text: String, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())) {
        prefs(context).edit().putString(key("ask_", date), text.trim()).apply()
    }

    /** What was handed over on the night of [date]. */
    fun eveningRelease(context: PlatformContext, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): String =
        prefs(context).getString(key("release_", date), "").orEmpty()

    fun setEveningRelease(context: PlatformContext, text: String, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())) {
        prefs(context).edit().putString(key("release_", date), text.trim()).apply()
    }

    /**
     * Which office the hour belongs to.
     *
     * Four offices, four windows, and no picker — the reader should open the
     * one card and find the prayer that fits the hour they are actually in.
     * Being asked to choose between Evening, Night and Midnight at eleven at
     * night is the kind of decision that ends with the phone put down.
     *
     *   04:00  Morning    the day is ahead
     *   17:00  Evening    the day is over, walk back through it
     *   21:30  Night      two minutes, then sleep
     *   00:00  Midnight   the watch
     */
    enum class Office { MORNING, AFTERNOON, EVENING, NIGHT, MIDNIGHT }

    fun officeFor(hour: Int, minute: Int = 0): Office = when {
        hour < 4 -> Office.MIDNIGHT
        hour < 12 -> Office.MORNING
        /* Noon to five belongs to the afternoon office. It used to be morning's,
           which meant somebody opening this at three was handed a prayer that
           begins "before this day takes me" - a day half over. */
        hour < 17 -> Office.AFTERNOON
        hour < 21 || (hour == 21 && minute < 30) -> Office.EVENING
        else -> Office.NIGHT
    }

    fun isEvening(hour: Int): Boolean = officeFor(hour) != Office.MORNING
}
