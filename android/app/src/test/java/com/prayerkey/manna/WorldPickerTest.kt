package com.prayerkey.manna

import com.prayerkey.manna.ui.worlds.VerseWorld
import com.prayerkey.manna.ui.worlds.WorldPicker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldPickerTest {

    private fun world(ref: String, text: String) = WorldPicker.forVerse(ref, text)

    @Test
    fun `imagery in the verse picks the matching world`() {
        assertEquals(
            VerseWorld.PASTURE,
            world("Psalm 23:2", "He maketh me to lie down in green pastures: he leadeth me beside the still waters."),
        )
        assertEquals(
            VerseWorld.SEA,
            world("Mark 4:39", "And he arose, and rebuked the wind, and said unto the sea, Peace, be still."),
        )
        assertEquals(
            VerseWorld.STARFIELD,
            world("Genesis 15:5", "Look now toward heaven, and tell the stars, if thou be able to number them."),
        )
        assertEquals(
            VerseWorld.FIRE,
            world("Daniel 3:25", "I see four men loose, walking in the midst of the fire, and they have no hurt."),
        )
        assertEquals(
            VerseWorld.CITY,
            world("Revelation 21:2", "And I John saw the holy city, new Jerusalem, coming down from God out of heaven."),
        )
    }

    @Test
    fun `promise references get the gold card`() {
        assertTrue(world("Jeremiah 29:11", "For I know the thoughts that I think toward you").isPromise)
        assertTrue(world("Isaiah 41:10", "Fear thou not; for I am with thee").isPromise)
        // a range still matches on its opening verse
        assertTrue(world("Lamentations 3:22-23", "His compassions fail not.").isPromise)
    }

    @Test
    fun `an ordinary verse is never a promise card`() {
        assertTrue(!world("Genesis 24:21", "And the man wondering at her held his peace.").isPromise)
        assertTrue(!world("Numbers 3:4", "And Nadab and Abihu died before the LORD.").isPromise)
    }

    /**
     * Verses with no imagery fall back on a hash of the reference. The
     * scene is then decoration rather than meaning — which is fine, but it
     * MUST be stable, or the same verse would wear a different world every
     * time it came round and the deck would feel broken.
     */
    @Test
    fun `the fallback is stable for the same verse`() {
        val a = world("Genesis 24:21", "And the man wondering at her held his peace.")
        val b = world("Genesis 24:21", "And the man wondering at her held his peace.")
        assertEquals(a, b)
    }

    @Test
    fun `the fallback still spreads verses across worlds`() {
        val refs = (1..40).map { "Numbers $it:7" }
        val worlds = refs.map { world(it, "And the LORD spake unto Moses, saying,") }.toSet()
        assertTrue("expected variety, got $worlds", worlds.size >= 5)
    }

    @Test
    fun `neighbouring verses do not all land in one world`() {
        val a = world("Genesis 24:21", "And the man wondering at her held his peace.")
        val b = world("Genesis 24:22", "And it came to pass, as the camels had done drinking.")
        assertNotEquals(a, b)
    }

    @Test
    fun `every world has a readable sky`() {
        VerseWorld.entries.forEach { w ->
            assertTrue("${w.name} needs a gradient", w.sky.size >= 3)
        }
    }
}
