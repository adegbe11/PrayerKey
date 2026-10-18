package com.prayerkey.manna.data

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyPrayerChoiceTest {
    private fun t(slug: String, title: String) = PrayerTopic(slug, title, "Faith", "", emptyList(), emptyList())

    private val topics = listOf(
        t("strength", "Prayer for Strength"),
        t("peace", "Prayer for Peace"),
        t("prayer-for-good-friday", "Prayer for Good Friday"),
        t("prayer-for-easter-sunday", "Prayer for Easter Sunday"),
        t("prayer-for-christmas", "Prayer for Christmas"),
        t("prayer-for-advent", "Prayer for Advent"),
        t("new-month-prayer", "Prayer for the New Month"),
        t("birthday-prayer-for-son", "Birthday Prayer for My Son"),
    )

    @Test fun anOrdinaryDayNeverGetsAFeast() {
        var day = LocalDate(2026, 10, 2)
        repeat(25) {
            val chosen = topicFor(day, topics)
            assertTrue(chosen.slug in setOf("strength", "peace"), "$day got ${chosen.title}")
            day = LocalDate.fromEpochDays(day.toEpochDays() + 1)
        }
    }

    @Test fun feastsLandOnTheirDay() {
        assertEquals("prayer-for-good-friday", topicFor(LocalDate(2026, 4, 3), topics).slug)
        assertEquals("prayer-for-easter-sunday", topicFor(LocalDate(2026, 4, 5), topics).slug)
        assertEquals("prayer-for-christmas", topicFor(LocalDate(2026, 12, 25), topics).slug)
        assertEquals("prayer-for-advent", topicFor(LocalDate(2026, 11, 29), topics).slug)
        assertEquals("new-month-prayer", topicFor(LocalDate(2026, 11, 1), topics).slug)
    }

    @Test fun occasionsAreRecognised() {
        assertTrue(t("x", "Birthday Prayer for My Son").isOccasion())
        assertTrue(t("x", "Prayer for Lent").isOccasion())
        assertFalse(t("x", "Prayer for Strength").isOccasion())
        assertFalse(t("x", "Prayer for Patience in Lending").isOccasion())
    }
}
