package com.prayerkey.manna

import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.dailyPrayerFor
import com.prayerkey.manna.data.passageFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * The daily prayer has to actually be different each day.
 *
 * The version this replaces looked fine by the obvious measure — 365 distinct
 * prayer strings across a year — and was not. Blanking the one substituted
 * verse reference collapsed it to **twelve**, repeating on a twelve-day cycle.
 * So the test that matters is distinctness *after* removing what varies
 * mechanically, which is what the old code would have failed.
 */
class DailyPrayerRotationTest {

    private val topics = (1..543).map {
        PrayerTopic(
            slug = "slug-$it", title = "Topic $it", category = "Health",
            prayer = "Prayer body number $it",
            scripture = listOf("Psalm $it:1" to "Verse text $it"),
            prayerPoints = emptyList(),
        )
    }

    private fun year(from: LocalDate = LocalDate.of(2026, 1, 1)) =
        generateSequence(from) { it.plusDays(1) }.take(365).toList()

    @Test
    fun `a year of prayers is not a template wearing dates`() {
        val bodies = year().map { date ->
            // strip what is substituted in, exactly as the old bug hid behind
            dailyPrayerFor(date, topics).prayer.replace(passageFor(date).reference, "<REF>")
        }
        val distinct = bodies.toSet().size
        assertTrue(
            "only $distinct distinct prayers in a year once the reference is removed",
            distinct > 300,
        )
    }

    @Test
    fun `the same day gives everybody the same prayer`() {
        val d = LocalDate.of(2026, 8, 14)
        assertEquals(dailyPrayerFor(d, topics), dailyPrayerFor(d, topics))
    }

    @Test
    fun `consecutive days do not walk the library in order`() {
        val days = year(LocalDate.of(2026, 8, 14)).take(5).map { dailyPrayerFor(it, topics).title }
        assertEquals("consecutive days repeated a prayer", 5, days.toSet().size)
    }

    /** Before the epoch, floorMod rather than % — or 1969 is a crash. */
    @Test
    fun `dates before the epoch do not throw`() {
        assertTrue(dailyPrayerFor(LocalDate.of(1969, 6, 1), topics).prayer.isNotBlank())
    }

    /** A cold start has no topics yet; it must still show something. */
    @Test
    fun `no topics still yields a prayer rather than a blank card`() {
        assertTrue(dailyPrayerFor(LocalDate.of(2026, 8, 14), emptyList()).prayer.isNotBlank())
    }
}
