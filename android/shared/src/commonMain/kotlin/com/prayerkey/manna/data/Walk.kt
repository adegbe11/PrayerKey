package com.prayerkey.manna.data

import com.prayerkey.manna.platform.KeyValues
import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.platform.today
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The walk: how many days in a row someone has met with God in PrayerKey.
 *
 * Any real moment counts - praying the day's prayer, reading, listening,
 * writing, a sermon, a fast day - not one chosen activity. The cruelest thing a
 * streak can do is vanish over one hard day, so a missed day is covered by a
 * rest day when one is in hand: one is earned for every seven days walked, and
 * up to two are kept. It lives in its own small store so a widget on the home
 * screen can read it without opening the app.
 */
data class WalkState(
    val days: Int = 0,
    val best: Int = 0,
    val restDays: Int = 0,
    val lastDay: Long = -1,
    /** Days met with God, as days since 1970, newest last (the last 120 kept). */
    val active: List<Long> = emptyList(),
    /** Days kept by a rest day rather than by being there. */
    val rested: List<Long> = emptyList(),
) {
    fun walkedOn(day: Long) = day in active
    fun restedOn(day: Long) = day in rested
    /** True when today is not yet walked but the walk is still alive. */
    fun waitingToday(today: Long) = days > 0 && lastDay == today - 1
    /** What the walk shows today: it only counts days that are not broken. */
    fun current(today: Long): Int = when {
        lastDay == today || lastDay == today - 1 -> days
        lastDay >= 0 && today - lastDay - 1 <= restDays -> days
        else -> 0
    }
}

/** What happened when a moment was marked. */
data class WalkStep(val state: WalkState, val grew: Boolean, val milestone: Int?, val restUsed: Int, val restEarned: Boolean)

object Walk {
    val Milestones = listOf(3, 7, 14, 21, 30, 40, 50, 75, 100, 150, 200, 250, 300, 365, 500, 730, 1000)
    private const val MAX_REST = 2
    private const val KEEP = 120

    private val _state = MutableStateFlow(WalkState())
    val state: StateFlow<WalkState> = _state.asStateFlow()
    private var loaded = false

    /** The pure rule, kept apart from storage so it can be tested. */
    fun step(before: WalkState, today: Long): WalkStep {
        if (before.lastDay == today) return WalkStep(before, false, null, 0, false)
        val missed = if (before.lastDay < 0) 0L else today - before.lastDay - 1
        val canRest = missed in 1..before.restDays.toLong()
        val continues = before.lastDay >= 0 && (missed == 0L || canRest)
        val used = if (canRest) missed.toInt() else 0
        val days = if (continues) before.days + 1 else 1
        val earned = days % 7 == 0 && before.restDays - used < MAX_REST
        val rest = (before.restDays - used + if (earned) 1 else 0).coerceIn(0, MAX_REST)
        val restedDays = if (canRest) (1..missed).map { before.lastDay + it } else emptyList()
        val after = WalkState(
            days = days,
            best = maxOf(before.best, days),
            restDays = rest,
            lastDay = today,
            active = (before.active + today).distinct().takeLast(KEEP),
            rested = (before.rested + restedDays).distinct().takeLast(KEEP),
        )
        return WalkStep(after, true, days.takeIf { it in Milestones }, used, earned)
    }

    fun load(context: PlatformContext, legacyStreak: Int = 0, legacyLastDay: Long = -1): WalkState {
        if (loaded) return _state.value
        val kv = context.keyValues("walk")
        val read = if (kv.contains("days")) read(kv) else WalkState(
            // the old counter only ever moved for the daily prayer; carry it over once
            days = legacyStreak, best = legacyStreak, lastDay = if (legacyStreak > 0) legacyLastDay else -1,
            active = if (legacyStreak > 0 && legacyLastDay >= 0) listOf(legacyLastDay) else emptyList(),
        )
        _state.value = read
        loaded = true
        return read
    }

    /** Marks today as walked. Safe to call from any screen, any number of times. */
    fun mark(context: PlatformContext): WalkStep {
        load(context)
        val step = step(_state.value, today().toEpochDays().toLong())
        if (step.grew) {
            _state.value = step.state
            write(context.keyValues("walk"), step.state)
            Today.changed(context)
            if (step.milestone != null) _celebrate.value = step
        }
        return step
    }

    private val _celebrate = MutableStateFlow<WalkStep?>(null)
    /** A milestone reached and not yet celebrated on screen. */
    val celebrate: StateFlow<WalkStep?> = _celebrate.asStateFlow()
    fun celebrated() { _celebrate.value = null }

    private fun read(kv: KeyValues) = WalkState(
        days = kv.getInt("days", 0),
        best = kv.getInt("best", 0),
        restDays = kv.getInt("rest", 0),
        lastDay = kv.getLong("last", -1),
        active = kv.getString("active", "").orEmpty().split(',').mapNotNull { it.toLongOrNull() },
        rested = kv.getString("rested", "").orEmpty().split(',').mapNotNull { it.toLongOrNull() },
    )

    private fun write(kv: KeyValues, s: WalkState) {
        kv.edit()
            .putInt("days", s.days).putInt("best", s.best).putInt("rest", s.restDays).putLong("last", s.lastDay)
            .putString("active", s.active.joinToString(",")).putString("rested", s.rested.joinToString(","))
            .apply()
    }
}
