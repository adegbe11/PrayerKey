package com.prayerkey.manna.data

import android.content.Context
import java.time.LocalDate

/**
 * Where you stopped praying.
 *
 * This lives outside the screen deliberately. The whole mechanic depends on
 * the position surviving process death: you pull six movements from the 6 a.m.
 * notification, the app is killed by lunchtime, and the evening reminder has
 * to know it should say *"you stopped at six"* and open at seven. Screen state
 * cannot answer that, and neither can anything held in memory.
 *
 * One entry per date, and only today's is read. Yesterday's key is left where
 * it is rather than swept — a handful of bytes is cheaper than the code that
 * would tidy them, and it means a user crossing midnight mid-session does not
 * lose their place.
 */
object PrayerPullsStore {

    private const val FILE = "prayer_session"

    private fun key(date: LocalDate) = "at_" + date.toEpochDay()
    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** How many movements have been prayed today. 0 when untouched. */
    fun progress(context: Context, date: LocalDate = LocalDate.now()): Int =
        prefs(context).getInt(key(date), 0)

    /**
     * Records the position. Only ever moves forward, so re-opening a finished
     * session to read it again cannot reset the day to zero.
     */
    fun record(context: Context, at: Int, date: LocalDate = LocalDate.now()) {
        val now = at.coerceIn(0, PRAYER_PULLS)
        if (now <= progress(context, date)) return
        prefs(context).edit().putInt(key(date), now).apply()
    }

    fun isComplete(context: Context, date: LocalDate = LocalDate.now()): Boolean =
        progress(context, date) >= PRAYER_PULLS

    /** Where the deck should open. A finished day starts again from the top. */
    fun resumeAt(context: Context, date: LocalDate = LocalDate.now()): Int =
        progress(context, date).takeIf { it < PRAYER_PULLS } ?: 0

    /**
     * What the notification should say.
     *
     * Naming the number is the point. "Your prayer is ready" is an
     * advertisement for the app; "You stopped at six" is a fact about the
     * person's own morning, and it is the only line here that makes anyone
     * open anything.
     */
    fun notificationLine(context: Context, date: LocalDate = LocalDate.now()): Pair<String, String> {
        val at = progress(context, date)
        return when {
            at == 0 -> "Today's prayer" to "Ten pulls. Four minutes. Start with the first."
            at >= PRAYER_PULLS -> "You prayed today" to "All ten. Open it again if you want to."
            else -> "You stopped at $at" to "Pull ${at + 1} is waiting."
        }
    }
}
