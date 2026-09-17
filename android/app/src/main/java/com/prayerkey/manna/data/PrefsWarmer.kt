package com.prayerkey.manna.data

import android.content.Context

/**
 * Opens every preference file once, off the main thread, at launch.
 *
 * `getSharedPreferences` looks cheap and mostly is: the framework keeps one
 * instance per file for the life of the process. The first call for a given
 * name is the exception - it reads and parses that file's XML on the calling
 * thread.
 *
 * Sixteen call sites ask for one of these thirteen files, and most of them are
 * inside composition or a LaunchedEffect, which is to say on the thread that
 * draws. StrictMode caught them arriving in ones and twos while Home built
 * itself: a badge here, a start date there, none individually slow, together a
 * stutter on the screen a person sees first.
 *
 * Warming them here means every one of those sites is answered from memory.
 * The call sites did not have to change, and a new one will be fast by default
 * rather than by remembering to be.
 */
object PrefsWarmer {

    /* Named rather than discovered. Listing the files is a chore, but reading
       the preferences directory would mean a disk read to avoid disk reads,
       and would miss any file not yet written on a fresh install - which is
       precisely the launch this is meant to make fast. */
    private val FILES = listOf(
        "onboarding_plan",
        "home_completed",
        "prayer_reminder",
        "prayer_challenge_machine",
        "daily_rhythm_notes",
        "daily_prayer_goal",
        "today_rhythm",
        "prayer_journey",
        "prayer-points-progress",
        "discovery_experiences",
        "daily_journey",
        "bible_challenge_machine",
        "prayer_session",
        "bible_session",
    )

    @Volatile private var warmed = false

    fun warm(context: Context) {
        if (warmed) return
        warmed = true
        val app = context.applicationContext
        Thread({
            FILES.forEach {
                runCatching { app.getSharedPreferences(it, Context.MODE_PRIVATE).all }
            }
        }, "prefs-warm").apply { priority = Thread.MIN_PRIORITY }.start()
    }
}
