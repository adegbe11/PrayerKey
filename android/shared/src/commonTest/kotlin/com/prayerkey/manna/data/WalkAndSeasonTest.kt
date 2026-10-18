package com.prayerkey.manna.data

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WalkAndSeasonTest {

    @Test fun easterDates() {
        assertEquals(LocalDate(2025, 4, 20), ChallengeSeason.easter(2025))
        assertEquals(LocalDate(2026, 4, 5), ChallengeSeason.easter(2026))
        assertEquals(LocalDate(2027, 3, 28), ChallengeSeason.easter(2027))
        assertEquals(LocalDate(2038, 4, 25), ChallengeSeason.easter(2038))
    }

    @Test fun lentStartsOnAshWednesday() {
        val (start, end) = ChallengeSeason(moveable = "lent").window(2026)!!
        assertEquals(LocalDate(2026, 2, 18), start)
        assertEquals(LocalDate(2026, 4, 4), end)
    }

    @Test fun seasonShowsAWeekEarly() {
        val advent = ChallengeSeason(12, 1, 12, 24)
        assertTrue(advent.isNear(LocalDate(2026, 11, 25)))
        assertFalse(advent.isNear(LocalDate(2026, 11, 20)))
        assertFalse(advent.isNear(LocalDate(2026, 12, 25)))
    }

    @Test fun walkGrowsDayByDay() {
        var s = WalkState()
        for (d in 100L..106L) s = Walk.step(s, d).state
        assertEquals(7, s.days)
        assertEquals(1, s.restDays) // earned at seven
    }

    @Test fun aRestDayKeepsTheWalk() {
        var s = WalkState()
        for (d in 100L..106L) s = Walk.step(s, d).state
        val step = Walk.step(s, 108L) // missed day 107
        assertEquals(8, step.state.days)
        assertEquals(1, step.restUsed)
        assertTrue(step.state.restedOn(107L))
    }

    @Test fun withoutRestTheWalkStartsAgain() {
        var s = WalkState()
        for (d in 100L..102L) s = Walk.step(s, d).state
        assertEquals(1, Walk.step(s, 105L).state.days)
        assertEquals(3, Walk.step(s, 105L).state.best)
    }

    @Test fun markingTwiceADayCountsOnce() {
        val s = Walk.step(WalkState(), 100L).state
        assertFalse(Walk.step(s, 100L).grew)
    }
}
