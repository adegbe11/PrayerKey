package com.prayerkey.manna

import com.prayerkey.manna.data.DevoBlock
import com.prayerkey.manna.data.Devotionals
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The written year, checked against the Bible we ship.
 *
 * The devotional this replaces cited "Psalms 23:1–9" over a psalm of six
 * verses on 21 days of the year, because the reference was computed from a
 * constant instead of measured. Nothing caught it, because nothing compared
 * the content to the text.
 *
 * This does. Every reference in every devotional is looked up in kjv.json, so
 * a verse that does not exist fails the build rather than shipping.
 */
class DevotionalsTest {

    private val assets = File("src/main/assets")

    private val days by lazy {
        Devotionals.parse(File(assets, "devotionals.json").readText())
    }

    /** book name -> chapter index -> verse count, straight from the asset. */
    private val canon: Map<String, List<Int>> by lazy {
        val root = JSONArray(File(assets, "kjv.json").readText())
        buildMap {
            for (b in 0 until root.length()) {
                val book = root.getJSONObject(b)
                val chapters = book.getJSONArray("chapters")
                put(
                    book.getString("name"),
                    (0 until chapters.length()).map { chapters.getJSONArray(it).length() },
                )
            }
        }
    }

    private fun versesIn(book: String, chapter: Int): Int {
        val chapters = canon[book] ?: error("no such book in kjv.json: '$book'")
        assertTrue("$book has no chapter $chapter", chapter in 1..chapters.size)
        return chapters[chapter - 1]
    }

    /**
     * The year is twelve Keys, one per calendar month, and each Key numbers its
     * own days from 1. Global numbering was the old shape and would now be
     * meaningless: day 1 exists twelve times, once per Key.
     */
    @Test
    fun `each Key numbers its days from one without gaps`() {
        assertTrue("no devotionals were parsed", days.isNotEmpty())
        days.groupBy { it.month }.forEach { (month, inKey) ->
            val numbers = inKey.map { it.day }
            assertEquals("Key for month $month is not sequential", (1..inKey.size).toList(), numbers)
            assertTrue("month $month is out of range", month in 1..12)
            assertTrue("month $month has no Key name", inKey.first().series.isNotBlank())
            assertTrue("month $month has no theme", inKey.first().theme.isNotBlank())
            assertTrue(
                "month $month claims ${inKey.first().seriesOf} slots",
                inKey.first().seriesOf in 28..31,
            )
            assertTrue(
                "month $month has more written days than it has calendar slots",
                inKey.size <= inKey.first().seriesOf,
            )
        }
    }

    @Test
    fun `every scripture reference exists in the bible we ship`() {
        days.forEach { day ->
            val refs = buildList {
                add(day.reading.ref)
                day.body.filterIsInstance<DevoBlock.Quote>().forEach { add(it.ref) }
            }
            refs.forEach { ref ->
                val total = versesIn(ref.book, ref.chapter)
                assertTrue(
                    "day ${day.day} '${day.title}' cites ${ref.label}, but that chapter has $total verses",
                    ref.verse in 1..total,
                )
            }
        }
    }

    /** The "read the passage" span has to fit the chapter too. */
    @Test
    fun `no reading range runs off the end of its chapter`() {
        days.forEach { day ->
            val r = day.reading
            val total = versesIn(r.book, r.chapter)
            assertTrue("day ${day.day}: range starts at ${r.from}", r.from in 1..total)
            assertTrue(
                "day ${day.day} '${day.title}' offers ${r.rangeLabel}, but ${r.book} ${r.chapter} has $total verses",
                r.to in r.from..total,
            )
            assertTrue("day ${day.day}: the quoted verse sits outside its own range", r.verse in r.from..r.to)
        }
    }

    /**
     * The failure being designed against is repetition, so it is worth
     * asserting rather than trusting. Titles, opening lines and shareable
     * lines all have to be unique across the year.
     */
    @Test
    fun `no devotional repeats another`() {
        fun assertDistinct(what: String, values: List<String>) {
            val dupes = values.groupBy { it.trim().lowercase() }.filterValues { it.size > 1 }
            assertTrue("repeated $what: ${dupes.keys.take(3)}", dupes.isEmpty())
        }
        assertDistinct("title", days.map { it.title })
        assertDistinct("shareable line", days.map { it.line.plain })
        assertDistinct("question", days.map { it.question })
        assertDistinct(
            "opening paragraph",
            days.mapNotNull { d -> d.body.filterIsInstance<DevoBlock.Para>().firstOrNull()?.text },
        )
    }

    @Test
    fun `every day is actually written`() {
        days.forEach { d ->
            val words = d.body.filterIsInstance<DevoBlock.Para>().sumOf { it.text.split(" ").size }
            assertTrue("day ${d.day} '${d.title}' has only $words words of body", words >= 120)
            assertTrue("day ${d.day} has no pull quote", d.body.any { it is DevoBlock.Quote })
            assertTrue("day ${d.day} has no prayer", d.prayer.length > 60)
            assertTrue("day ${d.day} question is not a question", d.question.endsWith("?"))
            assertTrue("day ${d.day} minutes look wrong", d.minutes in 2..9)
        }
    }
}
