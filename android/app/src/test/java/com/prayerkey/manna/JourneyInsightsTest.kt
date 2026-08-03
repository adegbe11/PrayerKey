package com.prayerkey.manna

import com.prayerkey.manna.data.JournalEntry
import com.prayerkey.manna.data.JourneyInsights
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyInsightsTest {
    private fun entry(id: Long, body: String, prayer: Boolean = false) = JournalEntry(
        id = id, entryDay = 1, mood = "", body = body, gratitude = "",
        verseRef = null, verseText = null, createdAt = id, updatedAt = id,
        isPrayer = prayer,
    )

    @Test fun `no insight is invented from one signal`() {
        assertNull(JourneyInsights.build(listOf(entry(1, "A quiet day")), emptyList(), emptyList(), emptyList()))
    }

    @Test fun `repeated language becomes a careful theme`() {
        val insight = JourneyInsights.build(
            listOf(entry(2, "I need peace for my anxiety", true), entry(1, "I am worried and afraid", true)),
            emptyList(), emptyList(), emptyList(),
        )!!
        assertEquals("A season of peace", insight.title)
        assertTrue(insight.body.contains("2 open prayers"))
        assertTrue(insight.body.contains("appeared more than once"))
        assertEquals("Philippians 4:6–7", insight.recommendedReference)
    }

    @Test fun `copy explicitly avoids divine claims`() {
        val insight = JourneyInsights.build(
            listOf(entry(2, "I need wisdom for this decision"), entry(1, "Give me direction and wisdom")),
            emptyList(), emptyList(), emptyList(),
        )!!
        assertTrue(insight.body.contains("recent reflections"))
        assertTrue(insight.nextStep.isNotBlank())
    }
}
