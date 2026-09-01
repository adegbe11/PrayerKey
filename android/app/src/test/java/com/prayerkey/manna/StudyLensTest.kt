package com.prayerkey.manna

import com.prayerkey.manna.data.StudyLens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyLensTest {
    @Test fun `source boundaries are always visible`() {
        val notes = StudyLens.notes("Philippians 4:7", "And the peace of God shall keep your hearts.")
        assertEquals("SCRIPTURE", notes.first().label)
        /* Pins the meaning, not the sentence. The boundary note was reworded
           when "MANNA" and the machine-facing phrasing came out of the app;
           what must never go is the claim that this is a study aid and not
           revelation. */
        val boundary = notes.first { it.label == "BOUNDARY" }.text
        assertTrue(boundary, boundary.contains("not revelation"))
        assertTrue(boundary, boundary.contains("not the only faithful reading"))
        assertTrue(notes.first { it.label == "OBSERVE" }.text.contains("peace"))
    }

    @Test fun `questions preserve observation and reflection boundaries`() {
        val observation = StudyLens.answer("John 14:27", "Peace I leave with you; my peace I give unto you.", "What does this say?")
        assertEquals("TEXT OBSERVATION", observation.label)
        assertTrue(observation.source.contains("John 14:27"))
        val application = StudyLens.answer("John 14:27", "Peace I leave with you.", "How do I apply this to life?")
        assertEquals("REFLECTION, NOT A COMMAND", application.label)
        assertTrue(application.text.contains("Test your response"))
    }

    @Test fun `context answers do not invent history`() {
        val answer = StudyLens.answer("Psalm 23:1", "The Lord is my shepherd.", "Who is the audience and context?")
        assertEquals("CONTEXT PATH", answer.label)
        assertTrue(answer.source.contains("no external facts asserted"))
    }
}
