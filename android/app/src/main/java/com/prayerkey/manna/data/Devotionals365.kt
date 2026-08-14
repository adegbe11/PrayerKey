package com.prayerkey.manna.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate

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
    /** 1..365 */
    val day: Int,
    val series: String,
    val seriesDay: Int,
    val seriesOf: Int,
    val title: String,
    val minutes: Int,
    val reading: DevoReading,
    val body: List<DevoBlock>,
    val line: DevoLine,
    val after: List<String>,
    val prayer: String,
    val addLabel: String,
    val question: String,
) {
    val seriesLabel: String get() = "$series · DAY $seriesDay OF $seriesOf"
}

/**
 * Loads and indexes the year. Parsed once; the asset is small next to the
 * Bible and there is no reason to touch the disk twice.
 */
object Devotionals {

    @Volatile private var cache: List<Devotional>? = null

    suspend fun all(context: Context): List<Devotional> =
        cache ?: withContext(Dispatchers.IO) {
            val parsed = parse(
                context.assets.open("devotionals.json").bufferedReader().use { it.readText() },
            )
            cache = parsed
            parsed
        }

    /**
     * Today's devotional.
     *
     * Walks the written year rather than the calendar year: day 1 of the plan
     * is the day you start, so somebody joining in November does not begin at
     * number 318. [size] is whatever has actually been written so far, so the
     * app is correct while the year is still being filled in.
     */
    suspend fun forDate(context: Context, date: LocalDate, startedOn: LocalDate?): Devotional? {
        val days = all(context)
        if (days.isEmpty()) return null
        val since = startedOn?.let { java.time.temporal.ChronoUnit.DAYS.between(it, date) } ?: 0L
        return days[Math.floorMod(since, days.size.toLong()).toInt()]
    }

    /** The day after [day], for the "tomorrow" card. Wraps at the end. */
    suspend fun after(context: Context, day: Devotional): Devotional? {
        val days = all(context)
        if (days.isEmpty()) return null
        return days[Math.floorMod(day.day, days.size)]
    }

    internal fun parse(json: String): List<Devotional> {
        val root = JSONObject(json)
        val out = mutableListOf<Devotional>()
        val series = root.getJSONArray("series")
        for (s in 0 until series.length()) {
            val group = series.getJSONObject(s)
            val name = group.getString("name")
            val days = group.getJSONArray("days")
            for (d in 0 until days.length()) {
                val o = days.getJSONObject(d)
                val read = o.getJSONObject("read")
                val body = o.getJSONArray("body")
                out += Devotional(
                    day = out.size + 1,
                    series = name,
                    seriesDay = d + 1,
                    seriesOf = days.length(),
                    title = o.getString("title"),
                    minutes = o.getInt("minutes"),
                    reading = DevoReading(
                        book = read.getString("book"),
                        chapter = read.getInt("chapter"),
                        verse = read.getInt("verse"),
                        from = read.getInt("from"),
                        to = read.getInt("to"),
                    ),
                    body = (0 until body.length()).map { i ->
                        val b = body.getJSONObject(i)
                        if (b.has("q")) {
                            val q = b.getJSONObject("q")
                            DevoBlock.Quote(
                                VerseRef(q.getString("book"), q.getInt("chapter"), q.getInt("verse")),
                            )
                        } else {
                            DevoBlock.Para(b.getString("p"))
                        }
                    },
                    line = o.getJSONObject("line").let {
                        DevoLine(it.getString("a"), it.getString("b"))
                    },
                    after = o.getJSONArray("after").let { a ->
                        (0 until a.length()).map { a.getString(it) }
                    },
                    prayer = o.getString("prayer"),
                    addLabel = o.getString("addLabel"),
                    question = o.getString("question"),
                )
            }
        }
        return out
    }
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
