package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

object MeditationStore {
    private fun prefs(context: PlatformContext) = context.keyValues("meditation")

    fun setup(context: PlatformContext): MeditationSetup {
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

    fun saveSetup(context: PlatformContext, setup: MeditationSetup) {
        prefs(context).edit()
            .putString("focus", setup.focus.name)
            .putString("custom", setup.custom)
            .putInt("minutes", setup.minutes)
            .putString("voice", setup.voice.name)
            .putString("ambience", setup.ambience.name)
            .apply()
    }

    fun record(context: PlatformContext): MeditationRecord {
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
    fun finish(context: PlatformContext, setup: MeditationSetup, seconds: Int, ref: String) {
        val r = record(context)
        prefs(context).edit()
            .putInt("sessions", r.sessions + 1)
            .putLong("seconds", r.seconds + seconds)
            .putStringSet("topics", r.topics + setup.focus.name)
            .putStringSet("verses", r.verses + ref)
            .putStringSet("days", (r.days + Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochDays().toLong()).map { it.toString() }.toSet())
            .apply()
    }
}
