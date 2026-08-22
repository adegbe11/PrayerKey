package com.prayerkey.manna

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The Bible we ship, checked for holes.
 *
 * Mark 4:40 was missing from this asset. Verses 1–39 were correct, and what
 * should have been 4:41 was stored as 4:40 — so every reference past the gap
 * was off by one, the chapter was a verse short, and nothing in the app would
 * ever have said so. It was found only because a devotional cited Mark 4:37–41
 * and a range test failed.
 *
 * These are spot checks against chapter lengths that are not in dispute. They
 * are not a full verification — see the total below — but they would have
 * caught the Mark gap on the day it was introduced.
 */
class BibleAssetTest {

    private val books: Map<String, List<Int>> by lazy {
        val root = JSONArray(File("src/main/assets/kjv.json").readText())
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

    private fun verses(book: String, chapter: Int) =
        (books[book] ?: error("no such book: $book"))[chapter - 1]

    @Test
    fun `the canon is complete`() {
        assertEquals("wrong number of books", 66, books.size)
        assertEquals("Psalms", 150, books.getValue("Psalms").size)
        assertEquals("Genesis", 50, books.getValue("Genesis").size)
        assertEquals("Revelation", 22, books.getValue("Revelation").size)
    }

    /** Chapter lengths nobody disputes. */
    @Test
    fun `well known chapters have all their verses`() {
        val expected = mapOf(
            ("Genesis" to 1) to 31,
            ("Exodus" to 20) to 26,
            ("Psalms" to 23) to 6,
            ("Psalms" to 117) to 2,
            ("Psalms" to 119) to 176,
            ("Isaiah" to 53) to 12,
            ("Matthew" to 5) to 48,
            ("Matthew" to 28) to 20,
            ("Mark" to 4) to 41,
            ("Mark" to 16) to 20,
            ("Luke" to 15) to 32,
            ("Luke" to 24) to 53,
            ("John" to 3) to 36,
            ("John" to 11) to 57,
            ("John" to 21) to 25,
            ("Acts" to 2) to 47,
            ("Acts" to 28) to 31,
            ("Romans" to 8) to 39,
            ("1 Corinthians" to 13) to 13,
            ("Revelation" to 22) to 21,
        )
        expected.forEach { (where, count) ->
            assertEquals(
                "${where.first} ${where.second} is short",
                count, verses(where.first, where.second),
            )
        }
    }

    /**
     * The KJV has 31,102 verses, and so does this asset.
     *
     * It was short by one for a long time and I could not find it by spot
     * check, because a missing verse leaves no gap: verses are stored as a
     * positional array, so losing one silently renumbers every verse after it
     * for the rest of the chapter. Mark 7 had been running one low since 7:12
     * without anything looking wrong.
     *
     * It was found by diffing this asset against the web app's copy and then
     * against the published per-book totals: Mark 7:11 — the Corban saying —
     * was absent, which is why 7:12 opens mid-argument with "And ye suffer him
     * no more" and no statement of what "ye say".
     *
     * Pinned exactly. A count that drifts in either direction is a corrupted
     * Bible, and this is the cheapest possible way to notice.
     */
    @Test
    fun `the asset holds the whole KJV`() {
        val total = books.values.sumOf { chapters -> chapters.sum() }
        assertEquals("the asset lost or gained verses", 31_102, total)
    }

    /** The verse whose absence renumbered the rest of Mark 7. */
    @Test
    fun `Mark 7 verse 11 is the Corban saying`() {
        val root = JSONArray(File("src/main/assets/kjv.json").readText())
        val mark = (0 until root.length()).map { root.getJSONObject(it) }
            .first { it.getString("name") == "Mark" }
        val chapter = mark.getJSONArray("chapters").getJSONArray(6)
        assertEquals(37, chapter.length())
        assertTrue(chapter.getString(10), chapter.getString(10).contains("Corban"))
        assertTrue(chapter.getString(11).startsWith("And ye suffer him no more"))
    }

    @Test
    fun `no verse is empty`() {
        val root = JSONArray(File("src/main/assets/kjv.json").readText())
        for (b in 0 until root.length()) {
            val book = root.getJSONObject(b)
            val chapters = book.getJSONArray("chapters")
            for (c in 0 until chapters.length()) {
                val verses = chapters.getJSONArray(c)
                for (v in 0 until verses.length()) {
                    assertTrue(
                        "${book.getString("name")} ${c + 1}:${v + 1} is empty",
                        verses.getString(v).trim().length > 8,
                    )
                }
            }
        }
    }
}
