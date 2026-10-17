package com.prayerkey.manna.data

import org.jetbrains.compose.resources.DrawableResource
import android.content.Context
import androidx.annotation.DrawableRes
import java.time.LocalDate

/**
 * Bible plans: a short journey through Scripture on one theme.
 *
 * Each day is one passage and four small things around it - a devotion on
 * the passage, a prayer point, a question to sit with, and space to write.
 * The plan does not lock days to the calendar: a person who wants to read
 * day two tonight can, and a person who misses a day picks up where they
 * stopped rather than being told they are behind.
 */
data class PlanDay(
    val title: String,
    val book: String,
    val chapter: Int,
    val from: Int,
    val to: Int,
    val devotionTitle: String,
    val devotion: String,
    val prayerPoint: String,
    val question: String,
) {
    /** "Psalm 23:1-6" - the KJV calls the book Psalms, a single chapter is a psalm. */
    val reference: String
        get() {
            val name = if (book == "Psalms") "Psalm" else book
            return if (from == to) "$name $chapter:$from" else "$name $chapter:$from–$to"
        }
}

data class ReadingPlanDef(
    val id: String,
    val title: String,
    val topic: String,
    val blurb: String,
    val verse: String,
    val verseRef: String,
    val art: DrawableResource,
    val days: List<PlanDay>,
) {
    val length: Int get() = days.size
}

/** Where someone is in a plan. */
data class PlanProgress(
    val planId: String = "",
    val startedOn: Long = 0L,
    val reminder: Boolean = true,
    val done: Set<Int> = emptySet(),
    val reflections: Int = 0,
    val prayers: Int = 0,
) {
    val active: Boolean get() = planId.isNotBlank()

    /** The first day not yet read, counted from 1. */
    fun current(length: Int): Int = (1..length).firstOrNull { it !in done } ?: length

    fun complete(length: Int): Boolean = done.size >= length
}

object ReadingPlanStore {
    private fun prefs(context: Context) = context.getSharedPreferences("reading_plans", Context.MODE_PRIVATE)

    fun plan(id: String): ReadingPlanDef? = READING_PLANS.firstOrNull { it.id == id }

    fun load(context: Context): PlanProgress {
        val p = prefs(context)
        return PlanProgress(
            planId = p.getString("plan", "").orEmpty(),
            startedOn = p.getLong("started", 0L),
            reminder = p.getBoolean("reminder", true),
            done = p.getString("done", "").orEmpty().split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
            reflections = p.getInt("reflections", 0),
            prayers = p.getInt("prayers", 0),
        )
    }

    fun save(context: Context, progress: PlanProgress) {
        prefs(context).edit()
            .putString("plan", progress.planId)
            .putLong("started", progress.startedOn)
            .putBoolean("reminder", progress.reminder)
            .putString("done", progress.done.sorted().joinToString(","))
            .putInt("reflections", progress.reflections)
            .putInt("prayers", progress.prayers)
            .apply()
    }

    fun start(context: Context, id: String, on: LocalDate, reminder: Boolean) =
        save(context, PlanProgress(planId = id, startedOn = on.toEpochDay(), reminder = reminder))

    /** Plans finished, so a finished plan shows as such in the library. */
    fun finished(context: Context): Set<String> = prefs(context).getStringSet("finished", emptySet()).orEmpty()

    fun markFinished(context: Context, id: String) {
        prefs(context).edit().putStringSet("finished", finished(context) + id).apply()
    }

    fun clear(context: Context) {
        val kept = finished(context)
        prefs(context).edit().clear().putStringSet("finished", kept).apply()
    }
}
