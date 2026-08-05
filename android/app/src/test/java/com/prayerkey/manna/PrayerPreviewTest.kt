package com.prayerkey.manna

import com.prayerkey.manna.ui.screens.preview
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The card's prayer trim. The old clamp cut wherever the line happened to
 * break, so a card could end mid-word; these pin the replacement down.
 */
class PrayerPreviewTest {

    @Test
    fun `a short prayer is shown whole and unflagged`() {
        val prayer = "Lord, give me peace today. Amen."
        val (text, trimmed) = preview(prayer)
        assertEquals(prayer, text)
        assertFalse(trimmed)
    }

    @Test
    fun `a long prayer stops on a sentence, not mid-word`() {
        val sentence = "Lord Jesus you are the Great Physician and I come to you now in need. "
        val (text, trimmed) = preview(sentence.repeat(12))
        assertTrue(trimmed)
        assertTrue("should end on a full stop, was: ...${text.takeLast(24)}", text.trimEnd().endsWith("."))
    }

    @Test
    fun `question and exclamation endings count as sentence ends`() {
        val prayer = "Who is like you, O Lord? " + "Great is your faithfulness and mercy toward me. ".repeat(10)
        val (text, trimmed) = preview(prayer)
        assertTrue(trimmed)
        assertTrue(text.trimEnd().last() in charArrayOf('.', '?', '!'))
    }

    @Test
    fun `a wall of text with no sentence break still ends cleanly`() {
        // no full stops at all — must fall back to a word boundary, never a
        // half word, and must still say something was held back
        val (text, trimmed) = preview("mercy ".repeat(200))
        assertTrue(trimmed)
        assertTrue(text.endsWith("…"))
        assertFalse("must not cut a word in half", text.removeSuffix("…").endsWith("merc"))
    }

    @Test
    fun `newlines and runs of space are collapsed`() {
        val (text, _) = preview("Lord,\n\n  hear   me.")
        assertEquals("Lord, hear me.", text)
    }

    @Test
    fun `the trim keeps a worthwhile amount of the prayer`() {
        // an early full stop must not be honoured if it would leave a stub
        val prayer = "Amen. " + "Father I bring you every part of this long and heavy day. ".repeat(10)
        val (text, _) = preview(prayer)
        assertTrue("kept only ${text.length} chars", text.length > 150)
    }
}
