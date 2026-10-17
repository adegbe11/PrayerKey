package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.readAsset
import kotlin.concurrent.Volatile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * A year of written devotionals.
 *
 * What this replaces: twelve themes stapled onto a passage picked by an
 * unrelated formula, wrapped in two paragraphs of filler that were byte for
 * byte identical every single day. Because the theme index was `epochDay * 7
 * mod 12` and 7 and 12 are coprime, the *entire* devotional came round again
 * every twelve days. It was a mail merge wearing 365 dates.
 *
 * These are written days. Each one is a piece of writing about a specific
 * passage, and no two are assembled from the same parts.
 *
 * **Scripture is never stored in the content file.** A day carries references
 * only, and the text is resolved against the KJV asset the app already ships.
 * That is deliberate: it makes it impossible for a devotional to quote a verse
 * that does not exist or to drift from the translation on the Bible tab, which
 * is exactly how "Psalms 23:1–9" survived in the old daily passages.
 */

/** A block of the body: either a paragraph or a scripture pull-quote. */
sealed interface DevoBlock {
    data class Para(val text: String) : DevoBlock
    data class Quote(val ref: VerseRef) : DevoBlock
}

data class VerseRef(val book: String, val chapter: Int, val verse: Int) {
    val label: String get() = "$book $chapter:$verse"
}

/** The passage a day is built on, and the span worth reading around it. */
data class DevoReading(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val from: Int,
    val to: Int,
) {
    val ref: VerseRef get() = VerseRef(book, chapter, verse)
    val rangeLabel: String get() = "$book $chapter:$from–$to"
}

/**
 * The line built to leave the app.
 *
 * Two halves because the share card sets the second one in italic gold — the
 * turn in the thought is what makes a line worth sending, so it is stored as
 * a turn rather than as one string the renderer has to guess at.
 */
data class DevoLine(val first: String, val second: String) {
    val plain: String get() = "$first $second"
}

data class Devotional(
    /** Day within its Key — 1..31. */
    val day: Int,
    /** The Key this day belongs to: "The Key of Beginnings". */
    val series: String,
    /** What the Key is for: "Setting holy habits". */
    val theme: String,
    /** Calendar month the Key owns, 1..12. */
    val month: Int,
    val seriesDay: Int,
    val seriesOf: Int,
    val title: String,
    val minutes: Int,
    val reading: DevoReading,
    val body: List<DevoBlock>,
    val line: DevoLine,
    val after: List<String>,
    val prayer: String,
    /** Which part of the prayer library this day should draw from. */
    val prayerCategory: String,
    val addLabel: String,
    val question: String,
) {
    val seriesLabel: String get() = "${series.uppercase()} · DAY $seriesDay OF $seriesOf"
}

/**
 * Loads and indexes the year. Parsed once; the asset is small next to the
 * Bible and there is no reason to touch the disk twice.
 */
object Devotionals {

    @Volatile private var cache: List<Devotional>? = null

    suspend fun all(context: PlatformContext): List<Devotional> =
        cache ?: withContext(Dispatchers.IO) {
            val parsed = context.readAsset("devotionals.json")?.let(::parse).orEmpty()
            if (parsed.isNotEmpty()) cache = parsed
            parsed
        }

    /**
     * Today's devotional, by the calendar.
     *
     * The year is twelve Keys, one per month, and a Key only makes sense on its
     * own month — January is Beginnings for everybody or it is Beginnings for
     * nobody. That replaces the old walk-from-install-date, which would have
     * handed somebody The Key of Joy in February.
     *
     * Within the month the day of the month selects the day. While a Key is
     * still being written, its written days cycle rather than the screen going
     * blank — and [coverage] is what tells the truth about that.
     */
    suspend fun forDate(context: PlatformContext, date: LocalDate, startedOn: LocalDate?): Devotional? {
        val days = all(context)
        if (days.isEmpty()) return null
        val thisKey = days.filter { it.month == date.monthNumber }
        if (thisKey.isEmpty()) {
            // this Key is not written yet; fall back to whatever is written
            return days[(date.dayOfYear - 1).mod(days.size)]
        }
        return thisKey.firstOrNull { it.day == date.dayOfMonth }
            ?: thisKey[(date.dayOfMonth - 1).mod(thisKey.size)]
    }

    /** Written days against calendar slots, per Key. Honest progress. */
    suspend fun coverage(context: PlatformContext): List<Triple<Int, String, Pair<Int, Int>>> {
        val days = all(context)
        return (1..12).map { m ->
            val inMonth = days.filter { it.month == m }
            Triple(m, inMonth.firstOrNull()?.series ?: "", inMonth.size to (inMonth.firstOrNull()?.seriesOf ?: 0))
        }
    }

    /** The day after [day], for the "tomorrow" card. Wraps at the end. */
    suspend fun after(context: PlatformContext, day: Devotional): Devotional? {
        val days = all(context)
        if (days.isEmpty()) return null
        return days[day.day.mod(days.size)]
    }

    fun parse(json: String): List<Devotional> {
        val root = Json.parseToJsonElement(json).jsonObject
        val out = mutableListOf<Devotional>()
        for (k in root.getArray("keys")) {
            val key = k.jsonObject
            val name = key.getString("name")
            val theme = key.getString("theme")
            val month = key.getInt("month")
            val slots = key.getInt("days")
            for ((d, entry) in key.getArray("written").withIndex()) {
                val o = entry.jsonObject
                val read = o.getObject("read")
                val body = o.getArray("body")
                out += Devotional(
                    day = o.optInt("day", d + 1),
                    series = name,
                    theme = theme,
                    month = month,
                    seriesDay = o.optInt("day", d + 1),
                    seriesOf = slots,
                    title = o.getString("title"),
                    minutes = o.getInt("minutes"),
                    reading = DevoReading(
                        book = read.getString("book"),
                        chapter = read.getInt("chapter"),
                        verse = read.getInt("verse"),
                        from = read.getInt("from"),
                        to = read.getInt("to"),
                    ),
                    body = body.map { entry ->
                        val b = entry.jsonObject
                        if (b.has("q")) {
                            val q = b.getObject("q")
                            DevoBlock.Quote(
                                VerseRef(q.getString("book"), q.getInt("chapter"), q.getInt("verse")),
                            )
                        } else {
                            DevoBlock.Para(b.getString("p"))
                        }
                    },
                    line = o.getObject("line").let {
                        DevoLine(it.getString("a"), it.getString("b"))
                    },
                    after = o.getArray("after").map { it.jsonPrimitive.content },
                    prayer = o.getString("prayer"),
                    prayerCategory = o.getString("prayerCategory"),
                    addLabel = o.getString("addLabel"),
                    question = o.getString("question"),
                )
            }
        }
        return out
    }
}

/**
 * The day, assembled — one reading, one prayer, one question, all about the
 * same thing.
 *
 * Before this, Today ran four independent rotations. The passage came from
 * `epochDay mod 365`, the devotional from a different formula, the prayer from
 * a third and the quote from a fourth, so on a single morning the app offered
 * Revelation 2, a devotional about Simeon in Luke 2, a prayer about diabetes
 * and an unrelated quote — four strangers sharing a date. Each rotated
 * perfectly well and none of them knew the others existed.
 *
 * The devotional is the spine because it is the only part that was *written*
 * about a specific passage; everything else can be chosen to match it. So the
 * day's reading is the devotional's reading, and the day's prayer is drawn
 * from the part of the library the devotional is actually about.
 *
 * Matching is by category rather than by a hand-picked prayer per day: 23
 * categories across 543 prayers cover every theme the writing goes near, and it
 * keeps working as the remaining days are written without anyone maintaining a
 * lookup table of 365 slugs.
 */
data class TodayPlan(
    val devotional: Devotional,
    /** The prayer for today, chosen to match [devotional]. */
    val prayer: PrayerTopic?,
) {
    val reading: DevoReading get() = devotional.reading
    /** What Today should call the day's Bible reading. */
    val readingLabel: String get() = "${reading.book} ${reading.chapter}"
}

/**
 * Builds the day.
 *
 * [topics] may be empty on a cold start, in which case there is simply no
 * prayer rather than a wrong one.
 */
fun planFor(devotional: Devotional, topics: List<PrayerTopic>, date: LocalDate): TodayPlan {
    val pool = topics.filter { it.category.equals(devotional.prayerCategory, ignoreCase = true) }
        .ifEmpty { topics }
    val prayer = pool.takeIf { it.isNotEmpty() }?.let {
        /* Stable for the date so everyone praying together gets the same one,
           and stepped rather than sequential so a category with six prayers
           does not serve them in the same order every time it comes round. */
        it[(date.toEpochDays().toLong() * 7).mod(it.size.toLong()).toInt()]
    }
    return TodayPlan(devotional, prayer)
}

/**
 * Every verse a devotional needs, resolved from the KJV asset in one pass.
 *
 * One map rather than a lookup per block: a day quotes a handful of verses and
 * they cluster in one or two chapters, so this loads those chapters once.
 */
suspend fun resolveScripture(bible: OfflineBible, day: Devotional): Map<String, String> {
    val wanted = buildList {
        add(day.reading.ref)
        day.body.filterIsInstance<DevoBlock.Quote>().forEach { add(it.ref) }
    }
    val byChapter = wanted.groupBy { it.book to it.chapter }
    val out = mutableMapOf<String, String>()
    for ((where, refs) in byChapter) {
        val verses = bible.chapter(where.first, where.second)
        refs.forEach { ref ->
            verses.firstOrNull { it.verse == ref.verse }?.let { out[ref.label] = it.text }
        }
    }
    return out
}

/**
 * The span actually available, so the "read the passage" link never offers a
 * range the chapter does not reach.
 */
suspend fun availableRange(bible: OfflineBible, reading: DevoReading): String {
    val verses = bible.chapter(reading.book, reading.chapter)
    if (verses.isEmpty()) return "${reading.book} ${reading.chapter}"
    val last = minOf(reading.to, verses.maxOf { it.verse })
    return "${reading.book} ${reading.chapter}:${reading.from}–$last"
}
