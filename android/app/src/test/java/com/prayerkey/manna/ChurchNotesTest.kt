package com.prayerkey.manna

import com.prayerkey.manna.ui.church.ReferenceDetector
import com.prayerkey.manna.ui.church.SermonArranger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The whole Church feature is free because these two objects do the work
 * with no model behind them. If they are wrong, the feature is worthless —
 * so they are tested against how preaching actually sounds.
 */
class ChurchNotesTest {

    private fun refs(text: String) = ReferenceDetector.scan(text).map { it.reference }

    @Test
    fun `spoken chapter and verse`() {
        assertEquals(listOf("John 3:16"), refs("turn with me to john chapter three verse sixteen"))
    }

    @Test
    fun `bare spoken chapter and verse`() {
        assertEquals(listOf("Romans 8:28"), refs("we know romans eight twenty eight is true"))
    }

    @Test
    fun `numbered books beat the short name`() {
        assertEquals(listOf("1 Kings 17:2"), refs("look at first kings seventeen verse two"))
        assertEquals(listOf("1 John 4:8"), refs("first john four verse eight says God is love"))
    }

    @Test
    fun `digit form still works`() {
        assertEquals(listOf("Philippians 4:19"), refs("Philippians 4:19"))
    }

    @Test
    fun `chapter only when no verse is given`() {
        assertEquals(listOf("Psalms 23"), refs("the whole of psalm twenty three"))
    }

    @Test
    fun `hundreds parse for the long psalms`() {
        assertEquals(listOf("Psalms 119:105"), refs("psalm one hundred nineteen verse one hundred five"))
    }

    @Test
    fun `a chapter past the end of the book is rejected`() {
        // Jude has one chapter — "jude twelve" is a mishearing, not a reference
        assertTrue(refs("jude twelve").isEmpty())
    }

    @Test
    fun `plain speech with no scripture finds nothing`() {
        assertTrue(refs("good morning church it is a beautiful day to be alive").isEmpty())
    }

    @Test
    fun `several references across one passage of speech`() {
        val spoken = "open to first kings seventeen verse seven then look at matthew six " +
            "twenty six and finally isaiah forty three verse nineteen"
        assertEquals(listOf("1 Kings 17:7", "Matthew 6:26", "Isaiah 43:19"), refs(spoken))
    }

    /** A realistic stretch of preaching, chunked the way the recogniser reports it. */
    private val sermon = listOf(
        "and I want you to see it in first kings seventeen verse two",
        "the word of the Lord came to him saying get thee hence turn eastward hide thyself by the brook Cherith",
        "now watch this the same God who sent him to the brook is the same God who allowed the brook to dry",
        "verse seven and it came to pass after a while that the brook dried up",
        "church some of you are crying over a brook God is closing on purpose",
        "the first thing I want you to see is that a dry brook is not punishment it is a signal that the next instruction is coming",
        "write this down the ravens stopped coming because the season stopped but your God did not stop",
        "secondly provision is tied to position Elijah had to move to Zarephath before the next supply started",
        "this week when something dries up ask for the next instruction before you ask for the old supply back",
    ).joinToString(" | ")

    @Test
    fun `arranger pulls points quotes and a takeaway from real preaching`() {
        val found = ReferenceDetector.scan(sermon.replace(" | ", " ")).map { it.reference }
        val note = SermonArranger.arrange(sermon, found)

        assertTrue("expected scriptures, got ${note.scriptures}", note.scriptures.isNotEmpty())
        assertTrue("expected points, got ${note.points}", note.points.isNotEmpty())
        assertTrue("expected a takeaway", note.takeaway.isNotBlank())
        assertTrue("title should not be empty", note.title.isNotBlank())

        // the note must be the preacher's own words — never invented
        val spokenWords = sermon.lowercase().replace(Regex("[^a-z ]"), " ")
        (note.points + note.quotes).forEach { line ->
            val sample = line.lowercase().replace(Regex("[^a-z ]"), " ")
                .split(" ").filter { it.length > 4 }.take(3)
            sample.forEach { word ->
                assertTrue("\"$word\" was not in the transcript", spokenWords.contains(word))
            }
        }
    }

    @Test
    fun `takeaway prefers the instruction he actually gave`() {
        val found = ReferenceDetector.scan(sermon.replace(" | ", " ")).map { it.reference }
        val note = SermonArranger.arrange(sermon, found)
        assertTrue(
            "takeaway should come from the closing instruction, got \"${note.takeaway}\"",
            note.takeaway.lowercase().contains("instruction") || note.takeaway.lowercase().contains("dries up"),
        )
    }

    @Test
    fun `an empty service produces an empty note rather than crashing`() {
        val note = SermonArranger.arrange("", emptyList())
        assertTrue(note.points.isEmpty())
        assertTrue(note.scriptures.isEmpty())
        assertEquals("Sunday service", note.title)
    }

    @Test
    fun `share text carries every section`() {
        val found = ReferenceDetector.scan(sermon.replace(" | ", " ")).map { it.reference }
        val note = SermonArranger.arrange(sermon, found)
        val text = SermonArranger.asShareText(note, "Sunday, July 26", 62)
        assertTrue(text.contains("SCRIPTURES PREACHED"))
        assertTrue(text.contains("THE MAIN POINTS"))
        assertTrue(text.contains("62 min"))
        assertTrue(text.contains("Manna"))
    }
}
