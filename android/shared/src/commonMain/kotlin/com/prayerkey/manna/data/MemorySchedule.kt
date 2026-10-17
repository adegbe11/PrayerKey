package com.prayerkey.manna.data

data class MemoryReview(val stage: Int, val correctCount: Int, val nextReviewAt: Long)

object MemorySchedule {
    private val intervals = listOf(1, 3, 7, 14, 30)

    fun after(current: MemoryVerse, correct: Boolean, now: Long): MemoryReview {
        val stage = if (correct) (current.stage + 1).coerceAtMost(5) else (current.stage - 1).coerceAtLeast(1)
        val days = if (correct) intervals[stage - 1] else 1
        return MemoryReview(stage, current.correctCount + if (correct) 1 else 0, now + days * 86_400_000L)
    }
}
