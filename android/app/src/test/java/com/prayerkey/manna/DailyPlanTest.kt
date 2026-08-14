package com.prayerkey.manna

import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.bibleChallenge
import com.prayerkey.manna.data.devotionFor
import com.prayerkey.manna.data.passageFor
import com.prayerkey.manna.data.prayerChallenge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * The daily plan. The property that matters: the same calendar day gives
 * everybody the same reading, and it never lands out of range — a devotion
 * that differs per device is not something two people can do together, and an
 * index off the end of the plan is a crash on a specific date.
 */
class DailyPlanTest {

    private val verses = (1..12).map { Triple("Psalm $it:1", "KJV", "Verse $it text") }
    private val topics = (1..40).map {
        PrayerTopic("slug-$it", "Topic $it", "Health", "Prayer $it", emptyList(), emptyList())
    }

    @Test
    fun `the same day gives the same devotion`() {
        val d = LocalDate.of(2026, 8, 7)
        assertEquals(devotionFor(d, verses, topics), devotionFor(d, verses, topics))
    }

    @Test
    fun `consecutive days move on`() {
        val a = devotionFor(LocalDate.of(2026, 8, 7), verses, topics)!!
        val b = devotionFor(LocalDate.of(2026, 8, 8), verses, topics)!!
        assertTrue(a.reference != b.reference || a.reflection != b.reflection)
    }

    @Test
    fun `a year of days never falls outside the verse list`() {
        var d = LocalDate.of(2026, 1, 1)
        repeat(400) {
            val devotion = devotionFor(d, verses, topics)
            assertNotNull("no devotion for $d", devotion)
            assertTrue(verses.any { v -> v.first == devotion!!.reference })
            d = d.plusDays(1)
        }
    }

    @Test
    fun `no verses means no devotion rather than a crash`() {
        assertNull(devotionFor(LocalDate.of(2026, 8, 7), emptyList(), topics))
    }

    @Test
    fun `a devotion still works with no prayer deck`() {
        val devotion = devotionFor(LocalDate.of(2026, 8, 7), verses, emptyList())
        assertNotNull(devotion)
        assertNull(devotion!!.prayerSlug)
    }

    @Test
    fun `dates before the epoch do not produce a negative index`() {
        // floorMod, not %, or 1969 crashes the screen
        val devotion = devotionFor(LocalDate.of(1969, 6, 1), verses, topics)
        assertNotNull(devotion)
    }

    /**
     * A reference must never claim verses the chapter does not have.
     *
     * The old form was "$book $chapter:1–9" for every day of the year, which
     * put "Psalms 23:1–9" over a psalm of six verses on 21 days out of 365.
     * Nothing can clamp that here — the canon holds chapter counts only — so
     * the heading names the chapter, and the exact range comes from `rangeOf`
     * once real verses are loaded.
     */
    @Test
    fun `a daily reference never invents a verse range`() {
        var d = LocalDate.of(2026, 1, 1)
        repeat(365) {
            assertTrue(!passageFor(d).reference.contains(":"))
            d = d.plusDays(1)
        }
    }

    @Test
    fun `the range comes from the verses actually loaded`() {
        val p = passageFor(LocalDate.of(2026, 1, 1))
        assertEquals("${p.book} ${p.chapter}:1–6", p.rangeOf(listOf(1, 2, 3, 4, 5, 6)))
        // nothing loaded yet: fall back to the chapter rather than "1–0"
        assertEquals(p.reference, p.rangeOf(emptyList()))
    }

    @Test
    fun `the gospel plan stays inside its 37 days`() {
        var d = LocalDate.of(2026, 1, 1)
        repeat(400) {
            val c = bibleChallenge(d, emptySet())
            assertEquals(37, c.today.total)
            assertTrue(c.today.index in 1..37)
            assertTrue(c.today.label.startsWith("John") || c.today.label.startsWith("Mark"))
            d = d.plusDays(1)
        }
    }

    /* The plan is now a course, not a calendar: today's reading is the first
       one you have not finished, so these walk it by progress rather than by
       date. Both of these used to advance the date and assert on what came
       back, which no longer describes what the plan does. */

    @Test
    fun `the gospel plan covers John then Mark`() {
        val d = LocalDate.of(2026, 1, 1)
        val labels = (0 until 37).map { bibleChallenge(d, (0 until it).toSet()).today.label }
        assertEquals(21, labels.count { it.startsWith("John") })
        assertEquals(16, labels.count { it.startsWith("Mark") })
        assertEquals("John 1", labels.first())
        assertEquals("Mark 16", labels.last())
    }

    @Test
    fun `finishing a reading advances to the next one`() {
        val d = LocalDate.of(2026, 8, 7)
        assertEquals(1, bibleChallenge(d, emptySet()).today.index)
        assertEquals(2, bibleChallenge(d, setOf(0)).today.index)
        assertEquals(3, bibleChallenge(d, setOf(0, 1)).today.index)
    }

    /**
     * A gap is not progress. Marking day 5 without days 1–4 must still put you
     * on day 1, or someone who taps the wrong row skips four chapters.
     */
    @Test
    fun `an out of order tick does not skip the plan ahead`() {
        val c = bibleChallenge(LocalDate.of(2026, 8, 7), setOf(4))
        assertEquals(1, c.today.index)
        assertEquals("John 1", c.today.label)
    }

    /**
     * Pins a live consequence of making the plan a progression: because today
     * is *defined* as the first unfinished reading, `done` is false until the
     * whole plan is finished, and only then reports true on the last day. The
     * UI therefore has no "today complete" state to show between day 1 and day
     * 37. This test exists to make that visible, not to bless it.
     */
    @Test
    fun `done only reports true once the whole plan is finished`() {
        val d = LocalDate.of(2026, 8, 7)
        assertTrue(!bibleChallenge(d, setOf(0)).today.done)
        assertTrue(!bibleChallenge(d, (0 until 36).toSet()).today.done)
        assertTrue(bibleChallenge(d, (0 until 37).toSet()).today.done)
    }

    @Test
    fun `the prayer challenge runs 21 days and stays in range`() {
        var d = LocalDate.of(2026, 1, 1)
        repeat(200) {
            val c = prayerChallenge(d, topics, emptySet())!!
            assertEquals(21, c.today.total)
            assertTrue(c.today.index in 1..21)
            assertTrue(topics.any { t -> t.title == c.today.label })
            d = d.plusDays(1)
        }
    }

    @Test
    fun `no topics means no prayer challenge`() {
        assertNull(prayerChallenge(LocalDate.of(2026, 8, 7), emptyList(), emptySet()))
    }
}
