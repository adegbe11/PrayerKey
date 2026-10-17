package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues

object FastingStore {
    private fun prefs(context: PlatformContext) =
        context.keyValues("fasting_challenge")

    fun load(context: PlatformContext): FastingChallenge {
        val p = prefs(context)
        return FastingChallenge(
            length = runCatching { FastLength.valueOf(p.getString("length", "SEVEN").orEmpty()) }
                .getOrDefault(FastLength.SEVEN),
            customHours = p.getInt("customHours", 24),
            type = runCatching { FastType.valueOf(p.getString("type", "WATER").orEmpty()) }
                .getOrDefault(FastType.WATER),
            purpose = p.getString("purpose", "Breakthrough").orEmpty(),
            note = p.getString("note", "").orEmpty(),
            startedAt = p.getLong("startedAt", 0L),
            breakHour = p.getInt("breakHour", 18),
            breakMinute = p.getInt("breakMinute", 0),
            dailyBreak = p.getBoolean("dailyBreak", false),
            rhythms = p.getStringSet("rhythms", setOf("MORNING")).orEmpty()
                .mapNotNull { name -> runCatching { PrayerRhythm.valueOf(name) }.getOrNull() }.toSet(),
            reminders = p.getBoolean("reminders", true),
            kept = p.getString("kept", "").orEmpty().split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
            prayerSessions = p.getInt("prayerSessions", 0),
            scriptures = p.getInt("scriptures", 0),
            entries = p.getInt("entries", 0),
            testimony = p.getString("testimony", "").orEmpty(),
            answered = p.getBoolean("answered", false),
        )
    }

    fun save(context: PlatformContext, fast: FastingChallenge) {
        prefs(context).edit()
            .putString("length", fast.length.name)
            .putInt("customHours", fast.customHours)
            .putString("type", fast.type.name)
            .putString("purpose", fast.purpose)
            .putString("note", fast.note)
            .putLong("startedAt", fast.startedAt)
            .putInt("breakHour", fast.breakHour)
            .putInt("breakMinute", fast.breakMinute)
            .putBoolean("dailyBreak", fast.dailyBreak)
            .putStringSet("rhythms", fast.rhythms.map { it.name }.toSet())
            .putBoolean("reminders", fast.reminders)
            .putString("kept", fast.kept.sorted().joinToString(","))
            .putInt("prayerSessions", fast.prayerSessions)
            .putInt("scriptures", fast.scriptures)
            .putInt("entries", fast.entries)
            .putString("testimony", fast.testimony)
            .putBoolean("answered", fast.answered)
            .apply()
    }

    fun journal(context: PlatformContext, day: Int): String =
        prefs(context).getString("journal_$day", "").orEmpty()

    fun saveJournal(context: PlatformContext, day: Int, text: String) {
        prefs(context).edit().putString("journal_$day", text).apply()
    }

    fun clear(context: PlatformContext) {
        prefs(context).edit().clear().apply()
    }
}
