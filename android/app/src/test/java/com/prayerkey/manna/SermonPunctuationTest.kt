package com.prayerkey.manna

import com.prayerkey.manna.ui.church.SermonPunctuation.polish
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * What the recogniser hands over and what a person should read.
 *
 * Vosk returns "and jesus said unto peter i am the way" — every name lower
 * case, every "I" lower case. None of that needs a model to fix; it is
 * decidable from the words.
 */
class SermonPunctuationTest {

    @Test fun `the divine names are capitalised`() {
        assertEquals("Jesus said it.", polish("jesus said it"))
        assertEquals("The Lord is my shepherd.", polish("the lord is my shepherd"))
        assertEquals("Ask the Holy Spirit.", polish("ask the holy spirit"))
    }

    @Test fun `people and places are capitalised`() {
        assertEquals("Paul wrote to Timothy.", polish("paul wrote to timothy"))
        assertTrue(polish("they went up to jerusalem").contains("Jerusalem"))
    }

    /** The 66 books come from BibleCanon, so they cannot drift out of step. */
    @Test fun `book names are capitalised from the canon`() {
        assertTrue(polish("turn to romans chapter eight").contains("Romans"))
        assertTrue(polish("it is written in revelation").contains("Revelation"))
    }

    @Test fun `the pronoun I is always capital`() {
        assertEquals("I am the way.", polish("i am the way"))
        assertTrue(polish("i'm going to the father").startsWith("I'm"))
    }

    @Test fun `a question gets a question mark`() {
        assertEquals("Who do you say that I am?", polish("who do you say that i am"))
        assertEquals("Is anything too hard for the Lord?", polish("is anything too hard for the lord"))
    }

    @Test fun `a statement gets a full stop`() {
        assertEquals("God is faithful.", polish("god is faithful"))
    }

    @Test fun `a discourse marker takes a breath comma`() {
        assertEquals("But, God had another plan.", polish("but god had another plan"))
        assertEquals("Now, listen to me.", polish("now listen to me"))
    }

    /** The longest name must win, or "holy spirit" becomes "Holy spirit". */
    @Test fun `longer names win over shorter ones inside them`() {
        assertTrue(polish("the holy spirit came").contains("Holy Spirit"))
    }

    @Test fun `an empty utterance stays empty`() {
        assertEquals("", polish("   "))
    }

    @Test fun `existing punctuation is not doubled`() {
        assertTrue(!polish("god is good.").endsWith(".."))
    }
}
