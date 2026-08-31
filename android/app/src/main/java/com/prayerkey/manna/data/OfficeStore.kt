package com.prayerkey.manna.data

import android.content.Context
import java.time.LocalDate

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

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    private fun key(prefix: String, date: LocalDate) = "$prefix${date.toEpochDay()}"

    /** What was asked for on [date]. Blank when nothing was written. */
    fun morningAsk(context: Context, date: LocalDate = LocalDate.now()): String =
        prefs(context).getString(key("ask_", date), "").orEmpty()

    fun setMorningAsk(context: Context, text: String, date: LocalDate = LocalDate.now()) {
        prefs(context).edit().putString(key("ask_", date), text.trim()).apply()
    }

    /** What was handed over on the night of [date]. */
    fun eveningRelease(context: Context, date: LocalDate = LocalDate.now()): String =
        prefs(context).getString(key("release_", date), "").orEmpty()

    fun setEveningRelease(context: Context, text: String, date: LocalDate = LocalDate.now()) {
        prefs(context).edit().putString(key("release_", date), text.trim()).apply()
    }

    /**
     * Which office to open now.
     *
     * Five in the afternoon, because the evening office is written to be prayed
     * after the day is over rather than at a clock time, and by five most
     * people's day has stopped being ahead of them.
     */
    fun isEvening(hour: Int): Boolean = hour >= 17 || hour < 4
}
