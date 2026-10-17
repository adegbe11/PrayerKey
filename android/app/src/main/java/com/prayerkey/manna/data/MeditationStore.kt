package com.prayerkey.manna.data

import android.content.Context
import java.time.LocalDate

object MeditationStore {
    private fun prefs(context: Context) = context.getSharedPreferences("meditation", Context.MODE_PRIVATE)

    fun setup(context: Context): MeditationSetup {
        val p = prefs(context)
        return MeditationSetup(
            focus = runCatching { MeditationFocus.valueOf(p.getString("focus", "PEACE").orEmpty()) }
                .getOrDefault(MeditationFocus.PEACE),
            custom = p.getString("custom", "").orEmpty(),
            minutes = p.getInt("minutes", 10),
            voice = runCatching { MeditationVoice.valueOf(p.getString("voice", "WARM").orEmpty()) }
                .getOrDefault(MeditationVoice.WARM),
            ambience = runCatching { Ambience.valueOf(p.getString("ambience", "NATURE").orEmpty()) }
                .getOrDefault(Ambience.NATURE),
        )
    }

    fun saveSetup(context: Context, setup: MeditationSetup) {
        prefs(context).edit()
            .putString("focus", setup.focus.name)
            .putString("custom", setup.custom)
            .putInt("minutes", setup.minutes)
            .putString("voice", setup.voice.name)
            .putString("ambience", setup.ambience.name)
            .apply()
    }

    fun record(context: Context): MeditationRecord {
        val p = prefs(context)
        return MeditationRecord(
            sessions = p.getInt("sessions", 0),
            seconds = p.getLong("seconds", 0L),
            topics = p.getStringSet("topics", emptySet()).orEmpty(),
            verses = p.getStringSet("verses", emptySet()).orEmpty(),
            days = p.getStringSet("days", emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }.toSet(),
        )
    }

    /** A session that was actually sat through, counted. */
    fun finish(context: Context, setup: MeditationSetup, seconds: Int, ref: String) {
        val r = record(context)
        prefs(context).edit()
            .putInt("sessions", r.sessions + 1)
            .putLong("seconds", r.seconds + seconds)
            .putStringSet("topics", r.topics + setup.focus.name)
            .putStringSet("verses", r.verses + ref)
            .putStringSet("days", (r.days + LocalDate.now().toEpochDay()).map { it.toString() }.toSet())
            .apply()
    }
}
