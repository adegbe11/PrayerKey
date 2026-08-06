package com.prayerkey.manna

import com.prayerkey.manna.data.cleanScripture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Stripping the 1611 apparatus. The ranges matter as much as the text: they
 * are what the book italicises, and an off-by-one italicises the wrong word.
 */
class ScriptureMarkupTest {

    @Test
    fun `plain text is untouched`() {
        val v = cleanScripture("In the beginning God created the heaven and the earth.")
        assertEquals("In the beginning God created the heaven and the earth.", v.text)
        assertTrue(v.supplied.isEmpty())
        assertTrue(v.notes.isEmpty())
    }

    @Test
    fun `supplied words lose their braces and keep their position`() {
        val v = cleanScripture("and darkness {was} upon the face of the deep.")
        assertEquals("and darkness was upon the face of the deep.", v.text)
        assertEquals(1, v.supplied.size)
        assertEquals("was", v.text.substring(v.supplied.first()))
    }

    @Test
    fun `a multi-word supplied phrase maps to the whole phrase`() {
        val v = cleanScripture("God saw the light, that {it was} good.")
        assertEquals("it was", v.text.substring(v.supplied.first()))
    }

    @Test
    fun `several supplied words each map correctly`() {
        val v = cleanScripture("{was} in the {land} of {Uz}")
        assertEquals("was in the land of Uz", v.text)
        assertEquals(listOf("was", "land", "Uz"), v.supplied.map { v.text.substring(it) })
    }

    @Test
    fun `marginal notes are lifted out of the body`() {
        val raw = "divided the light from the darkness. {the light from...: Heb. between the light and between the darkness}"
        val v = cleanScripture(raw)
        assertEquals("divided the light from the darkness.", v.text)
        assertEquals(1, v.notes.size)
        assertTrue(v.notes.first().startsWith("the light from"))
    }

    @Test
    fun `a note between supplied words does not shift their ranges`() {
        val v = cleanScripture("the {firmament} of heaven {firmament: Heb. expansion} and the {waters}")
        assertEquals(listOf("firmament", "waters"), v.supplied.map { v.text.substring(it) })
    }

    @Test
    fun `lifting a note does not leave a double space`() {
        val v = cleanScripture("so. {grass: Heb. tender grass} And God said")
        assertTrue("found a double space in: '${v.text}'", !v.text.contains("  "))
    }

    @Test
    fun `notes and supplied words can both appear`() {
        val v = cleanScripture("And the evening {was} the first day. {And the evening...: Heb. etc.}")
        assertEquals("And the evening was the first day.", v.text)
        assertEquals("was", v.text.substring(v.supplied.first()))
        assertEquals(1, v.notes.size)
    }

    @Test
    fun `an unclosed brace keeps the rest of the verse rather than dropping it`() {
        // damaged data must never cost us scripture
        val v = cleanScripture("and darkness {was upon the face of the deep.")
        assertTrue(v.text.contains("upon the face of the deep."))
    }

    @Test
    fun `a stray closing brace is left alone`() {
        val v = cleanScripture("the waters} above the firmament")
        assertTrue(v.text.contains("above the firmament"))
    }

    @Test
    fun `every supplied range is inside the text`() {
        val v = cleanScripture("{a} b {c} d {note: Heb. x} {e}")
        v.supplied.forEach {
            assertTrue("range $it outside 0..${v.text.length}", it.first >= 0 && it.last < v.text.length)
        }
    }

    @Test
    fun `leading and trailing whitespace is trimmed without breaking ranges`() {
        val v = cleanScripture("   {was} the light   ")
        assertEquals("was the light", v.text)
        assertEquals("was", v.text.substring(v.supplied.first()))
    }
}
