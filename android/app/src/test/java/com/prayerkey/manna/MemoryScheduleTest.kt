package com.prayerkey.manna

import com.prayerkey.manna.data.MemorySchedule
import com.prayerkey.manna.data.MemoryVerse
import org.junit.Assert.assertEquals
import org.junit.Test

class MemoryScheduleTest {
    private val day = 86_400_000L

    @Test fun `correct reviews grow spacing`() {
        val next = MemorySchedule.after(MemoryVerse("Psalm 1:1", "Blessed", 2, 4), true, 100L)
        assertEquals(3, next.stage)
        assertEquals(5, next.correctCount)
        assertEquals(100L + 7 * day, next.nextReviewAt)
    }

    @Test fun `difficult review steps back gently`() {
        val next = MemorySchedule.after(MemoryVerse("Psalm 1:1", "Blessed", 4, 6), false, 200L)
        assertEquals(3, next.stage)
        assertEquals(6, next.correctCount)
        assertEquals(200L + day, next.nextReviewAt)
    }

    @Test fun `rooted verse remains rooted`() {
        val next = MemorySchedule.after(MemoryVerse("Psalm 1:1", "Blessed", 5, 9), true, 0L)
        assertEquals(5, next.stage)
        assertEquals(30 * day, next.nextReviewAt)
    }
}
