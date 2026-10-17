package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Where you stopped.
 *
 * This lives outside the screen deliberately. The whole mechanic depends on
 * the position surviving process death: you pull six from the 6 a.m.
 * notification, the app is killed by lunchtime, and the evening reminder still
 * has to know to say *"you stopped at six"* and open at seven. Screen state
 * cannot answer that, and neither can anything held in memory.
 *
 * The total is stored alongside the position rather than assumed, because the
 * two decks are not the same length — prayer is always ten, and a chapter of
 * two verses is two pulls. A reminder that says "you stopped at 6 of 10" for a
 * two-pull psalm is worse than no reminder.
 *
 * One entry per date, and only today's is read. Yesterday's keys are left
 * where they are rather than swept: a handful of bytes is cheaper than the
 * code that would tidy them, and it means crossing midnight mid-session does
 * not lose your place.
 */
sealed class PullsProgress(private val file: String, private val noun: String) {

    object Prayer : PullsProgress("prayer_session", "prayer")
    object Bible : PullsProgress("bible_session", "reading")

    private fun at(date: LocalDate) = "at_" + date.toEpochDays()
    private fun of(date: LocalDate) = "of_" + date.toEpochDays()
    private fun prefs(context: PlatformContext) = context.keyValues(file)

    /** How many pulls are done today. 0 when untouched. */
    fun progress(context: PlatformContext, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Int =
        prefs(context).getInt(at(date), 0)

    /** How long today's deck is, as last seen. 0 before it has ever been opened. */
    fun total(context: PlatformContext, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Int =
        prefs(context).getInt(of(date), 0)

    /**
     * Records the position. Only ever moves forward, so re-opening a finished
     * deck to read it again cannot reset the day to zero.
     */
    fun record(context: PlatformContext, done: Int, outOf: Int, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())) {
        val editor = prefs(context).edit()
        if (outOf > 0) editor.putInt(of(date), outOf)
        val now = done.coerceIn(0, outOf.takeIf { it > 0 } ?: done)
        if (now > progress(context, date)) editor.putInt(at(date), now)
        editor.apply()
    }

    fun isComplete(context: PlatformContext, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Boolean {
        val outOf = total(context, date)
        return outOf > 0 && progress(context, date) >= outOf
    }

    /** Where the deck should open. A finished day starts again from the top. */
    fun resumeAt(context: PlatformContext, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Int =
        if (isComplete(context, date)) 0 else progress(context, date)

    /** "6/10" for the home badge, or null when today has not been started. */
    fun badge(context: PlatformContext, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): String? {
        val done = progress(context, date)
        val outOf = total(context, date)
        return if (done <= 0 || outOf <= 0) null else "$done/$outOf"
    }

    /**
     * What the notification should say.
     *
     * Naming the number is the point. "Your prayer is ready" is an
     * advertisement for the app; "You stopped at six" is a fact about the
     * reader's own morning, and only one of the two gets opened.
     */
    fun notificationLine(context: PlatformContext, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Pair<String, String> {
        val done = progress(context, date)
        val outOf = total(context, date)
        return when {
            done <= 0 -> opening
            outOf in 1..done -> "You finished today's $noun" to "Open it again if you want to."
            else -> "You stopped at $done" to "Pull ${done + 1} is waiting."
        }
    }

    private val opening: Pair<String, String>
        get() = when (this) {
            Prayer -> "Today's prayer" to "Ten pulls. Four minutes. Start with the first."
            Bible -> "Today's reading" to "Ten pulls through the chapter. Start with the first."
        }
}
