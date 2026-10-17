package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues

object PrayerChallengeStore {
    private const val FILE = "prayer_challenge"

    private fun prefs(context: PlatformContext) = context.keyValues(FILE)

    fun load(context: PlatformContext): PrayerChallenge {
        val p = prefs(context)
        return PrayerChallenge(
            days = p.getInt("days", 21),
            focus = p.getString("focus", "").orEmpty(),
            category = p.getString("category", "").orEmpty(),
            practice = runCatching {
                ChallengePractice.valueOf(p.getString("practice", "BIBLE").orEmpty())
            }.getOrDefault(ChallengePractice.BIBLE),
            hour = p.getInt("hour", 6),
            minute = p.getInt("minute", 30),
            reminder = p.getBoolean("reminder", true),
            startedOn = p.getLong("started", 0L),
            done = p.getString("done", "").orEmpty()
                .split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
            answered = p.getBoolean("answered", false),
        )
    }

    fun save(context: PlatformContext, challenge: PrayerChallenge) {
        prefs(context).edit()
            .putInt("days", challenge.days)
            .putString("focus", challenge.focus)
            .putString("category", challenge.category)
            .putString("practice", challenge.practice.name)
            .putInt("hour", challenge.hour)
            .putInt("minute", challenge.minute)
            .putBoolean("reminder", challenge.reminder)
            .putLong("started", challenge.startedOn)
            .putString("done", challenge.done.sorted().joinToString(","))
            .putBoolean("answered", challenge.answered)
            .apply()
    }

    /** What was written at the end of a day, kept so the journey can be read back. */
    fun reflection(context: PlatformContext, day: Int): String =
        prefs(context).getString("reflection_$day", "").orEmpty()

    fun saveReflection(context: PlatformContext, day: Int, text: String, feeling: String, seconds: Int) {
        prefs(context).edit()
            .putString("reflection_$day", text)
            .putString("feeling_$day", feeling)
            .putInt("seconds_$day", seconds)
            .apply()
    }

    fun feeling(context: PlatformContext, day: Int): String = prefs(context).getString("feeling_$day", "").orEmpty()

    fun seconds(context: PlatformContext, day: Int): Int = prefs(context).getInt("seconds_$day", 0)

    /** Minutes prayed across the whole challenge. */
    fun minutesPrayed(context: PlatformContext, days: Int): Int =
        (1..days).sumOf { prefs(context).getInt("seconds_$it", 0) } / 60

    fun clear(context: PlatformContext) {
        prefs(context).edit().clear().apply()
    }
}
