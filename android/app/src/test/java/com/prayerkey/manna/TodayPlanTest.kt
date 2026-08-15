package com.prayerkey.manna

import com.prayerkey.manna.data.Devotionals
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.planFor
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

/**
 * Today has to be one day, not four unrelated rotations sharing a date.
 *
 * What this guards against actually shipped: the passage came from
 * `epochDay mod 365`, the devotional from another formula and the prayer from a
 * third, so the app could offer a devotional about Simeon in Luke 2 beside a
 * reading in Revelation 2 and a prayer about diabetes. Every part rotated
 * correctly. None of them agreed.
 */
class TodayPlanTest {

    private val assets = File("src/main/assets")
    private val days by lazy { Devotionals.parse(File(assets, "devotionals.json").readText()) }

    private val topics by lazy {
        val root = JSONObject(File(assets, "prayer-topics.json").readText()).getJSONArray("topics")
        (0 until root.length()).map { i ->
            val o = root.getJSONObject(i)
            PrayerTopic(
                slug = o.optString("slug"), title = o.optString("title"),
                category = o.optString("category"), prayer = o.optString("prayer"),
                scripture = emptyList(), prayerPoints = emptyList(),
            )
        }
    }

    @Test
    fun `every written day names a category the library actually has`() {
        val available = topics.map { it.category }.toSet()
        days.forEach { day ->
            assertTrue(
                "day ${day.day} '${day.title}' wants '${day.prayerCategory}', " +
                    "which is not a category in the prayer library",
                day.prayerCategory in available,
            )
        }
    }

    @Test
    fun `the day's prayer matches what the day is about`() {
        val date = LocalDate.of(2026, 8, 14)
        days.forEach { day ->
            val plan = planFor(day, topics, date)
            assertNotNull("day ${day.day} got no prayer", plan.prayer)
            assertEquals(
                "day ${day.day} '${day.title}' is about ${day.prayerCategory} " +
                    "but drew a prayer from ${plan.prayer?.category}",
                day.prayerCategory, plan.prayer?.category,
            )
        }
    }

    @Test
    fun `the reading is the devotional's own passage`() {
        val plan = planFor(days.first(), topics, LocalDate.of(2026, 8, 14))
        assertEquals(days.first().reading.book, plan.reading.book)
        assertEquals("${days.first().reading.book} ${days.first().reading.chapter}", plan.readingLabel)
    }

    @Test
    fun `the same day gives everyone the same prayer`() {
        val d = LocalDate.of(2026, 8, 14)
        assertEquals(planFor(days.first(), topics, d).prayer, planFor(days.first(), topics, d).prayer)
    }

    /** A cold start has no library yet. Better no prayer than a wrong one. */
    @Test
    fun `no topics yields no prayer rather than a crash`() {
        assertEquals(null, planFor(days.first(), emptyList(), LocalDate.of(2026, 8, 14)).prayer)
    }
}
