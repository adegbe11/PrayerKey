package com.prayerkey.manna

import com.prayerkey.manna.data.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryGraphTest {
    @Test fun `connects a memorized passage across private sources`() {
        val memory = MemoryVerse("Philippians 4:7", "The peace of God shall keep your hearts", 2)
        val saved = listOf(SavedWord(1, "Philippians 4:7", "KJV", memory.verse, 1))
        val entries = listOf(JournalEntry(1, 1, "calm", "I need the peace of God to keep our hearts in this decision", "", null, null, 1, 1))
        val prayers = listOf(JournalPrayer(1, "Peace", "Keep my heart in peace", "", "Philippians 4:7", 1))
        val sermons = listOf(SermonNote(1, "Guarded hearts", listOf("Philippians 4:7"), emptyList(), emptyList(), "Peace can guard us.", "", 20, 1))
        val kinds = MemoryGraph.connections(memory, saved, entries, prayers, sermons).map { it.kind }
        assertEquals(listOf("SAVED WORD", "JOURNEY", "PRAYER", "SERMON"), kinds)
    }

    @Test fun `unrelated private material is not connected`() {
        val memory = MemoryVerse("John 3:16", "For God so loved the world", 1)
        val entry = JournalEntry(1, 1, "calm", "Bought groceries today", "Thankful for rain", null, null, 1, 1)
        assertTrue(MemoryGraph.connections(memory, emptyList(), listOf(entry), emptyList(), emptyList()).isEmpty())
    }
}
