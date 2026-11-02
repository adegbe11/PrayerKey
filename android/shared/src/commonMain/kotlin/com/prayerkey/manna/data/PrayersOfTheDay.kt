package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.platform.readAsset
import kotlin.concurrent.Volatile
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** One short prayer, set in lines, and the KJV verse it was written from. */
data class PrayerOfTheDay(
    val title: String,
    val ref: String,
    val verse: String,
    val lines: List<String>,
) {
    val text: String get() = lines.joinToString(" ")
}

/**
 * A prayer for every date of the year, plus the feasts that move with Easter.
 *
 * The year is keyed by calendar date ("MM-DD"), so 1 November is the same
 * prayer for everybody. Ash Wednesday, Palm Sunday, Good Friday, Easter,
 * Ascension and Pentecost replace the date's prayer when they fall on it.
 * Verses were pulled word for word from the app's own KJV when the file was
 * built, so they always agree with the Bible tab.
 */
object PrayersOfTheDay {

    private class Year(val byDate: Map<String, PrayerOfTheDay>, val moveable: Map<String, PrayerOfTheDay>)

    @Volatile private var cache: Year? = null

    private suspend fun year(context: PlatformContext): Year? =
        cache ?: withContext(ioDispatcher) {
            context.readAsset("prayers-of-the-day.json")?.let { raw -> runCatching { parse(raw) }.getOrNull() }
                ?.also { cache = it }
        }

    private fun parse(json: String): Year {
        val root = Json.parseToJsonElement(json).jsonObject
        fun read(o: kotlinx.serialization.json.JsonObject) = PrayerOfTheDay(
            title = o["title"]!!.jsonPrimitive.content,
            ref = o["ref"]!!.jsonPrimitive.content,
            verse = o["verse"]!!.jsonPrimitive.content,
            lines = o["lines"]!!.jsonArray.map { it.jsonPrimitive.content },
        )
        val days = root["days"]!!.jsonArray.associate { e ->
            val o = e.jsonObject
            o["date"]!!.jsonPrimitive.content to read(o)
        }
        val moveable = root["moveable"]?.jsonArray.orEmpty().associate { e ->
            val o = e.jsonObject
            o["key"]!!.jsonPrimitive.content to read(o)
        }
        return Year(days, moveable)
    }

    suspend fun forDate(context: PlatformContext, date: LocalDate): PrayerOfTheDay? {
        val year = year(context) ?: return null
        feastOn(date)?.let { key -> year.moveable[key]?.let { return it } }
        val key = "${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
        // 29 February outside a leap year never asks, but a missing day still gets a prayer
        return year.byDate[key] ?: year.byDate.values.elementAtOrNull((date.dayOfYear - 1).mod(year.byDate.size))
    }

    /** Which moveable feast, if any, falls on [date]. */
    fun feastOn(date: LocalDate): String? {
        val easter = easter(date.year)
        return when (date) {
            easter.minus(DatePeriod(days = 46)) -> "ash-wednesday"
            easter.minus(DatePeriod(days = 7)) -> "palm-sunday"
            easter.minus(DatePeriod(days = 2)) -> "good-friday"
            easter -> "easter"
            easter.plus(DatePeriod(days = 39)) -> "ascension"
            easter.plus(DatePeriod(days = 49)) -> "pentecost"
            else -> null
        }
    }

    /** Western Easter Sunday, by the anonymous Gregorian computus. */
    fun easter(year: Int): LocalDate {
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

    /* Amen, remembered per date. Just a mark, not a score: the card shows the
       week so a missed day is visible without anything nagging about it. */
    private const val STORE = "prayer_of_the_day"

    fun prayed(context: PlatformContext, date: LocalDate): Boolean =
        context.keyValues(STORE).getBoolean(date.toString(), false)

    fun markPrayed(context: PlatformContext, date: LocalDate) {
        context.keyValues(STORE).edit().putBoolean(date.toString(), true).apply()
    }

    /** The seven days ending on [date], oldest first, and whether each was prayed. */
    fun week(context: PlatformContext, date: LocalDate): List<Pair<LocalDate, Boolean>> =
        (6 downTo 0).map { back ->
            val d = date.minus(DatePeriod(days = back))
            d to prayed(context, d)
        }
}
