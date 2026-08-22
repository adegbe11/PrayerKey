package com.prayerkey.manna

import com.prayerkey.manna.data.BIBLE_PULL_TARGET
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.readingFrom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rule this file exists to hold: a pull may end short, but it may not end
 * mid-sentence. Cutting every four verses to reach a round ten is the obvious
 * implementation, and the one a reader notices immediately.
 */
class BiblePullsTest {

    private fun verses(vararg text: String) =
        text.mapIndexed { i, line -> BibleVerse("Romans", 8, i + 1, line) }

    private fun chapter(n: Int) = (1..n).map {
        BibleVerse("Romans", 8, it, "This is verse $it and it closes properly.")
    }

    @Test fun `a long chapter gives about ten pulls`() {
        val reading = readingFrom("Romans", 8, chapter(39))
        assertTrue("${reading.size}", reading.size in 8..BIBLE_PULL_TARGET)
    }

    @Test fun `no pull ends mid sentence`() {
        val reading = readingFrom("Romans", 8, chapter(39))
        reading.pulls.forEach {
            assertTrue(it.text, it.text.trimEnd().last() in setOf('.', '!', '?', ':'))
        }
    }

    @Test fun `every verse survives the cutting, once, in order`() {
        val source = chapter(39)
        val reading = readingFrom("Romans", 8, source)
        assertEquals((1..39).toList(), reading.pulls.flatMap { it.firstVerse..it.lastVerse })
    }

    @Test fun `a short chapter is not padded to ten`() {
        val reading = readingFrom("Psalm", 117, verses("Praise the LORD.", "For his mercy is great."))
        assertEquals(2, reading.size)
    }

    @Test fun `a verse that does not close is carried into the next pull`() {
        val reading = readingFrom(
            "Romans", 8,
            verses(
                "There is therefore now no condemnation to them which are in Christ Jesus,",
                "who walk not after the flesh, but after the Spirit.",
                "For the law of the Spirit of life made me free.",
            ),
        )
        assertTrue(reading.pulls.first().text.endsWith("after the Spirit."))
    }

    @Test fun `references name the span, not the chapter`() {
        val reading = readingFrom("Romans", 8, chapter(39))
        reading.pulls.forEach { assertTrue(it.reference, it.reference.startsWith("Romans 8:")) }
        assertEquals("Romans 8:1", readingFrom("Romans", 8, chapter(1)).pulls.first().reference)
    }

    @Test fun `an empty chapter gives no pulls rather than a crash`() {
        assertEquals(0, readingFrom("Romans", 8, emptyList()).size)
    }

    @Test fun `indexes are one based and unbroken`() {
        val reading = readingFrom("Romans", 8, chapter(39))
        assertEquals((1..reading.size).toList(), reading.pulls.map { it.index })
    }
}
