package com.prayerkey.manna

import com.prayerkey.manna.data.JournalCoach
import com.prayerkey.manna.data.JournalEntry
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalCoachTest {
    @Test fun `reflection stays anchored to private entry`() {
        val entry = JournalEntry(1, 1, "calm", "I need peace while making this decision.", "My family", "Philippians 4:7", null, 1, 1)
        val result = JournalCoach.reflect(entry)
        assertTrue(result.suggestedTitle.contains("peace"))
        assertTrue(result.highlight.contains("My family"))
        assertTrue(result.questions.size == 3)
    }
}
