package com.prayerkey.manna

import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.bibleChallenge
import com.prayerkey.manna.data.devotionFor
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

    @Test
    fun `the gospel plan covers John then Mark`() {
        val labels = (0 until 37).map {
            bibleChallenge(LocalDate.of(2026, 1, 1).plusDays(it.toLong()), emptySet()).today.label
        }
        assertTrue(labels.count { it.startsWith("John") } == 21)
        assertTrue(labels.count { it.startsWith("Mark") } == 16)
    }

    @Test
    fun `marking a day done is reflected`() {
        val d = LocalDate.of(2026, 8, 7)
        val index = bibleChallenge(d, emptySet()).today.index - 1
        assertTrue(bibleChallenge(d, setOf(index)).today.done)
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
