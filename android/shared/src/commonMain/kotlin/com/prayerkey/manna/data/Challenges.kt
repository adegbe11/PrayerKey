package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.platform.readAsset
import kotlin.concurrent.Volatile
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Named challenges.
 *
 * What this replaces was three configurators. The prayer challenge asked four
 * questions before day one — a topic from 543, a length from five — which is
 * **over 2,700 possible challenges**, none of which has a name. That sounds
 * like power and is the opposite: nothing with 2,700 variants can be given a
 * cover, written well, recommended to a friend, or remembered. It is the same
 * trap the devotional generator fell into, where 365 days turned out to be
 * twelve wearing costumes.
 *
 * A challenge here is a fixed thing with a name, a length and one honest line
 * about what it is for. You tap it, you tap Start, you are on day one. No
 * questions.
 *
 * Every day has the same three parts — a passage, a written reflection, and
 * points to pray aloud — because a shape learned once is a shape you stop
 * having to learn. That is the single most copied thing about the plans people
 * actually finish.
 *
 * Scripture is stored as a reference and resolved from the bundled KJV, so a
 * verse cannot be misquoted in this file and the text cannot drift from the
 * Bible the rest of the app reads.
 */
data class ChallengePlan(
    val id: String,
    val title: String,
    val days: Int,
    /** One line, on the shelf card. What this is actually for. */
    val promise: String,
    val cover: String,
    val written: List<ChallengePlanDay>,
    /** When the church keeps it, for a seasonal challenge. Null for the year-round shelf. */
    val season: ChallengeSeason? = null,
)

/**
 * When a seasonal challenge belongs: a fixed window in the calendar (Advent,
 * the new year's fast) or a moveable feast keyed to Easter (Lent, Holy Week,
 * Pentecost). It is shown from a week before it begins so it can be started on
 * day one, and leaves the top of the shelf when its window closes.
 */
data class ChallengeSeason(
    val startMonth: Int = 0, val startDay: Int = 0,
    val endMonth: Int = 0, val endDay: Int = 0,
    val moveable: String? = null,
) {
    /** The first and last day of the season in a given year. */
    fun window(year: Int): Pair<LocalDate, LocalDate>? = when (moveable) {
        // Ash Wednesday to Holy Saturday; the forty days do not count Sundays
        "lent" -> easter(year).let { it.minus(46, DateTimeUnit.DAY) to it.minus(1, DateTimeUnit.DAY) }
        // Palm Sunday to Easter Sunday
        "holy-week" -> easter(year).let { it.minus(7, DateTimeUnit.DAY) to it }
        // the ten days between Ascension and Pentecost, ending on Pentecost
        "pentecost" -> easter(year).let { it.plus(40, DateTimeUnit.DAY) to it.plus(49, DateTimeUnit.DAY) }
        null -> runCatching { LocalDate(year, startMonth, startDay) to LocalDate(year, endMonth, endDay) }.getOrNull()
        else -> null
    }

    /** This year's season, or next year's once this year's has passed. */
    fun upcoming(today: LocalDate): Pair<LocalDate, LocalDate>? {
        val now = window(today.year) ?: return null
        return if (now.second >= today) now else window(today.year + 1)
    }

    /** True from a week before the season until its last day. */
    fun isNear(today: LocalDate): Boolean {
        val (start, end) = upcoming(today) ?: return false
        return today >= start.minus(7, DateTimeUnit.DAY) && today <= end
    }

    /** True while the season itself is running. */
    fun isOn(today: LocalDate): Boolean {
        val (start, end) = upcoming(today) ?: return false
        return today >= start && today <= end
    }

    /** The line on the card: Begins 1 December, or In season until 24 December. */
    fun label(today: LocalDate): String {
        val (start, end) = upcoming(today) ?: return ""
        fun d(x: LocalDate) = "${x.dayOfMonth} ${x.month.name.lowercase().replaceFirstChar { it.uppercase() }}"
        return if (today < start) "Begins ${d(start)}" else "In season until ${d(end)}"
    }

    companion object {
        /** Western Easter Sunday (the anonymous Gregorian computus). */
        fun easter(year: Int): LocalDate {
            val a = year % 19; val b = year / 100; val c = year % 100
            val d = b / 4; val e = b % 4; val f = (b + 8) / 25; val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30; val i = c / 4; val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7; val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31; val day = (h + l - 7 * m + 114) % 31 + 1
            return LocalDate(year, month, day)
        }
    }
}

data class ChallengePlanDay(
    val day: Int,
    val title: String,
    val read: DevoReading,
    /** Paragraphs, split on blank lines. */
    val reflection: List<String>,
    /**
     * The prayer, written out to be read aloud.
     *
     * This replaced a list of prayer points. A point is an instruction — you
     * still have to compose the prayer yourself, at six in the morning, before
     * coffee. A written prayer costs nothing to begin, which is the difference
     * between praying and reading about praying.
     */
    val prayer: String,
    val question: String,
    /** For the house. Optional — a day may leave both blank. */
    val familyQuestion: String,
    val familyPrayer: String,
) {
    val hasFamily: Boolean get() = familyQuestion.isNotBlank() || familyPrayer.isNotBlank()
}

object Challenges {

    private const val ASSET = "challenges.json"
    private const val SEASONS = "seasons.json"

    @Volatile private var cache: List<ChallengePlan>? = null

    suspend fun all(context: PlatformContext): List<ChallengePlan> = withContext(ioDispatcher) {
        cache ?: parse(context).also { cache = it }
    }

    suspend fun byId(context: PlatformContext, id: String): ChallengePlan? =
        all(context).firstOrNull { it.id == id }

    /**
     * Reads the asset.
     *
     * A malformed challenge is dropped rather than taking the shelf down with
     * it — one bad entry should cost one card, not the feature.
     */
    private fun parse(context: PlatformContext): List<ChallengePlan> =
        parse(context, ASSET) + parse(context, SEASONS)

    /** Every challenge whose season is near, soonest first. */
    suspend fun inSeason(context: PlatformContext, today: LocalDate): List<ChallengePlan> =
        all(context).filter { it.season?.isNear(today) == true }
            .sortedBy { it.season?.upcoming(today)?.first }

    private fun parse(context: PlatformContext, asset: String): List<ChallengePlan> = runCatching {
        val raw = context.readAsset(asset) ?: return emptyList()
        val array = Json.parseToJsonElement(raw).jsonObject.getArray("challenges")
        buildList {
            for (entry in array) {
                val o = entry as? JsonObject ?: continue
                runCatching { add(challenge(o)) }
            }
        }
    }.getOrDefault(emptyList())

    private fun challenge(o: JsonObject): ChallengePlan {
        val written = o.getArray("written")
        return ChallengePlan(
            id = o.getString("id"),
            title = o.getString("title"),
            days = o.getInt("days"),
            promise = o.getString("promise"),
            cover = o.optString("cover"),
            written = written.map { day(it.jsonObject) },
            season = (o["season"] as? JsonObject)?.let {
                ChallengeSeason(
                    startMonth = it.optInt("startMonth", 0), startDay = it.optInt("startDay", 0),
                    endMonth = it.optInt("endMonth", 0), endDay = it.optInt("endDay", 0),
                    moveable = it.optString("moveable").ifBlank { null },
                )
            },
        )
    }

    private fun day(o: JsonObject): ChallengePlanDay {
        val read = o.getObject("read")
        return ChallengePlanDay(
            day = o.getInt("day"),
            title = o.getString("title"),
            read = DevoReading(
                book = read.getString("book"),
                chapter = read.getInt("chapter"),
                verse = read.getInt("verse"),
                from = read.optInt("from", read.getInt("verse")),
                to = read.optInt("to", read.getInt("verse")),
            ),
            reflection = o.getString("reflection").split("\n\n").map { it.trim() }.filter { it.isNotEmpty() },
            prayer = o.optString("prayer"),
            question = o.optString("question"),
            familyQuestion = o.optString("familyQuestion"),
            familyPrayer = o.optString("familyPrayer"),
        )
    }
}

/**
 * Which day you are on, and which challenge you are in.
 *
 * One challenge at a time, deliberately. Two running at once is how a plan
 * stops being a plan — the point of a named challenge is that it is the thing
 * you are doing, not one of several things you started.
 */
object ChallengeProgress {

    private const val FILE = "challenges"

    private fun prefs(context: PlatformContext) =
        context.keyValues(FILE)

    /** The challenge currently running, or null. */
    fun activeId(context: PlatformContext): String? =
        prefs(context).getString("active", null)

    /** Days completed in [id]. 0 before it starts. */
    fun completed(context: PlatformContext, id: String): Int =
        prefs(context).getInt("done_$id", 0)

    fun start(context: PlatformContext, id: String) {
        prefs(context).edit().putString("active", id).apply()
    }

    /**
     * Marks a day done. Only ever moves forward, so re-reading day three of a
     * plan you are on day nine of cannot set you back six days.
     */
    fun complete(context: PlatformContext, id: String, day: Int) {
        if (day <= completed(context, id)) return
        prefs(context).edit().putInt("done_$id", day).apply()
    }

    /** Where the challenge opens: the next unread day, or the last one. */
    fun resumeDay(context: PlatformContext, challenge: ChallengePlan): Int =
        (completed(context, challenge.id) + 1).coerceAtMost(challenge.days)

    fun isFinished(context: PlatformContext, challenge: ChallengePlan): Boolean =
        completed(context, challenge.id) >= challenge.days

    fun abandon(context: PlatformContext) {
        prefs(context).edit().remove("active").apply()
    }
}
