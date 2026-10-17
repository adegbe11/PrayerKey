package com.prayerkey.manna

import kotlinx.datetime.toKotlinTimeZone
import com.prayerkey.manna.data.FastLength
import com.prayerkey.manna.data.FastMachine
import com.prayerkey.manna.data.FastMachine.Moment
import com.prayerkey.manna.data.FastType
import com.prayerkey.manna.data.FastingChallenge
import com.prayerkey.manna.data.PrayerMemory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

/** The fast machine's timing and words, and when the prayer memory asks. */
class MachineTest {

    private val start = 1_790_000_000L
    private val hour = 3_600L

    @Test
    fun `a day's fast speaks early, halfway, near the end and at the end`() {
        val fast = FastingChallenge(length = FastLength.DAY, startedAt = start)
        val plan = FastMachine.plan(fast)
        assertEquals(listOf(Moment.EARLY, Moment.HALF, Moment.LAST_HOUR, Moment.DONE), plan.map { it.moment })
        assertEquals(listOf(3L, 12L, 23L, 24L), plan.map { (it.at - start) / hour })
    }

    @Test
    fun `a seven day fast greets every new day`() {
        val fast = FastingChallenge(length = FastLength.SEVEN, startedAt = start)
        val plan = FastMachine.plan(fast)
        assertEquals((2..7).toList(), plan.filter { it.moment == Moment.NEW_DAY }.map { it.day })
        assertFalse("no halfway for a week-long fast", plan.any { it.moment == Moment.HALF })
        assertEquals(Moment.DONE, plan.last().moment)
        assertEquals(start + 168 * hour, plan.last().at)
        assertTrue(plan.zipWithNext().all { (a, b) -> a.at < b.at })
    }

    @Test
    fun `a short fast is not over-talked`() {
        val fast = FastingChallenge(length = FastLength.SIX, startedAt = start)
        assertEquals(listOf(Moment.HALF, Moment.LAST_HOUR, Moment.DONE), FastMachine.plan(fast).map { it.moment })
    }

    @Test
    fun `a fast not begun plans nothing`() {
        assertTrue(FastMachine.plan(FastingChallenge()).isEmpty())
    }

    @Test
    fun `the words carry the day, the purpose and Scripture, without quotation marks`() {
        val fast = FastingChallenge(length = FastLength.SEVEN, type = FastType.WATER, purpose = "Breakthrough", startedAt = start)
        val (title, text) = FastMachine.message(fast, Moment.NEW_DAY, 3)
        assertEquals("Day 3 of 7", title)
        assertTrue(text, Regex("""\d+:\d+""").containsMatchIn(text))
        Moment.entries.forEach { m ->
            val (t, body) = FastMachine.message(fast, m, 2)
            assertFalse("$t: $body", body.contains('"') || body.contains('“'))
        }
        val (_, half) = FastMachine.message(fast.copy(length = FastLength.DAY), Moment.HALF, 1)
        assertTrue(half, half.contains("for breakthrough"))
    }

    @Test
    fun `the prayer memory asks a week later, in the evening`() {
        val zone = ZoneId.of("Africa/Lagos")
        val prayed = LocalDateTime.of(2026, 10, 4, 7, 15).atZone(zone).toInstant().toEpochMilli()
        val ask = java.time.Instant.ofEpochMilli(PrayerMemory.followUpAt(prayed, zone.toKotlinTimeZone())).atZone(zone).toLocalDateTime()
        assertEquals(LocalDateTime.of(2026, 10, 11, 19, 0), ask)
    }
}
