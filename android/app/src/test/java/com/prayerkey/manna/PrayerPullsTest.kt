package com.prayerkey.manna

import com.prayerkey.manna.data.PullKind
import com.prayerkey.manna.data.PRAYER_PULLS
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.pullsFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * A year of sessions, checked without a device.
 *
 * The assertion that earns its place is the last one: no opening repeating
 * inside a calendar month. The devotional generator this app used to ship
 * passed every structural test it had while quietly serving twelve prayers on
 * a twelve-day loop, and a daily user noticed inside a fortnight. Shape tests
 * do not catch that. Repetition tests do.
 */
class PrayerPullsTest {

    private val year = (0 until 366).map { LocalDate.of(2026, 1, 1).plusDays(it.toLong()) }

    private val topic = PrayerTopic(
        slug = "waiting-on-god",
        title = "Waiting on God",
        category = "Faith",
        prayer = "Father, I have been waiting a long time. Teach me to wait without becoming bitter. " +
            "Keep my hope alive when nothing visible changes. Let the waiting do its work in me.",
        scripture = listOf("Isaiah 40:31" to "They that wait upon the LORD shall renew their strength."),
        prayerPoints = listOf("Patience in the delay", "Strength for the season"),
    )

    @Test fun `every day is exactly ten pulls`() {
        year.forEach { date ->
            assertEquals("$date", PRAYER_PULLS, pullsFor(date, topic).size)
        }
    }

    @Test fun `it opens at Dear Lord and closes at Amen`() {
        year.forEach { date ->
            val movements = pullsFor(date, topic).movements
            assertTrue("$date", movements.first().text.startsWith("Dear Lord"))
            assertTrue("$date", movements.last().text.endsWith("Amen."))
        }
    }

    @Test fun `the movements run in the order a prayer runs`() {
        val kinds = pullsFor(LocalDate.of(2026, 3, 9), topic).movements.map { it.kind }
        assertEquals(
            listOf(
                PullKind.OPEN, PullKind.THANKS, PullKind.SCRIPTURE,
                PullKind.PETITION, PullKind.PETITION, PullKind.PETITION, PullKind.PETITION,
                PullKind.DECLARATION, PullKind.INTERCESSION, PullKind.SEAL,
            ),
            kinds,
        )
    }

    @Test fun `indexes are one based and unbroken`() {
        year.forEach { date ->
            assertEquals("$date", (1..10).toList(), pullsFor(date, topic).movements.map { it.index })
        }
    }

    @Test fun `no movement is blank`() {
        year.forEach { date ->
            pullsFor(date, topic).movements.forEach {
                assertTrue("$date pull ${it.index}", it.text.trim().length > 20)
            }
        }
    }

    @Test fun `the day's own prayer is what gets prayed`() {
        val body = pullsFor(LocalDate.of(2026, 6, 2), topic).movements
            .filter { it.kind == PullKind.PETITION }
            .joinToString(" ") { it.text }
        assertTrue(body.contains("Teach me to wait without becoming bitter"))
        assertTrue(body.contains("Let the waiting do its work in me"))
    }

    @Test fun `the prayer is not reordered`() {
        val petitions = pullsFor(LocalDate.of(2026, 6, 2), topic).movements
            .filter { it.kind == PullKind.PETITION }
            .joinToString(" ") { it.text }
        val order = listOf("waiting a long time", "without becoming bitter", "hope alive", "its work in me")
        var at = -1
        order.forEach { fragment ->
            val next = petitions.indexOf(fragment)
            assertTrue("out of order: $fragment", next > at)
            at = next
        }
    }

    @Test fun `the topic's scripture is the third pull`() {
        val word = pullsFor(LocalDate.of(2026, 4, 4), topic).movements[2]
        assertEquals(PullKind.SCRIPTURE, word.kind)
        assertEquals("Isaiah 40:31", word.reference)
    }

    @Test fun `a cold start still gives a whole session`() {
        year.forEach { date ->
            val session = pullsFor(date, prayer = null, reading = "Psalm 27")
            assertEquals("$date", PRAYER_PULLS, session.size)
            assertTrue("$date", session.movements.none { it.text.isBlank() })
            assertEquals("$date", "Psalm 27", session.movements[2].reference)
        }
    }

    @Test fun `a one sentence prayer still fills four movements`() {
        val terse = topic.copy(prayer = "Father, hold me steady today.")
        year.take(40).forEach { date ->
            val petitions = pullsFor(date, terse).movements.filter { it.kind == PullKind.PETITION }
            assertEquals("$date", 4, petitions.size)
            assertTrue("$date", petitions.none { it.text.isBlank() })
        }
    }

    @Test fun `no opening repeats inside a calendar month`() {
        year.windowed(31).forEach { month ->
            val openings = month.map { pullsFor(it, topic).movements.first().text }
            assertEquals("${month.first()}", openings.size, openings.distinct().size)
        }
    }

    @Test fun `no thanksgiving repeats inside a calendar month`() {
        year.windowed(31).forEach { month ->
            val thanks = month.map { pullsFor(it, topic).movements[1].text }
            assertEquals("${month.first()}", thanks.size, thanks.distinct().size)
        }
    }

    @Test fun `the same day gives the same session`() {
        val date = LocalDate.of(2026, 9, 14)
        assertEquals(pullsFor(date, topic).movements, pullsFor(date, topic).movements)
    }

    @Test fun `a prayer point is said in the first person`() {
        val declaration = pullsFor(LocalDate.of(2026, 2, 2), topic).movements
            .first { it.kind == PullKind.DECLARATION }.text
        assertTrue(declaration, declaration.startsWith("Lord, I ask You for patience in the delay"))
        assertTrue(declaration, declaration.endsWith("."))
    }
}
