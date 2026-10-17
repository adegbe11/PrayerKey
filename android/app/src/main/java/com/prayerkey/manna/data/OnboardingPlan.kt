package com.prayerkey.manna.data

import android.content.Context

/**
 * What the first screens asked for, kept where the rest of the app can use it.
 *
 * The welcome asks where you would like to begin and what is on your heart,
 * and those answers were being written down and never read again - questions
 * asked for the sake of asking. They now decide which prayer comes first.
 */
object OnboardingPlan {
    private fun prefs(context: Context) =
        context.getSharedPreferences("onboarding_plan", Context.MODE_PRIVATE)

    fun goals(context: Context): Set<String> = prefs(context).getStringSet("goals", emptySet()).orEmpty()

    fun topics(context: Context): Set<String> = prefs(context).getStringSet("topics", emptySet()).orEmpty()

    /** The prompt packs this person said they came for, most wanted first. */
    fun packs(context: Context): List<String> {
        val chosen = topics(context) + goals(context)
        val order = listOf(
            "gratitude" to "Thanksgiving",
            "family" to "Family Prayer",
            "relationships" to "Family Prayer",
            "work" to "My Requests",
            "health" to "My Requests",
            "church" to "Intercession",
            "remember" to "Answered Prayer",
            "read" to "Praying Scripture",
            "grow" to "Listening to God",
            "fast" to "Surrender",
            "pray" to "Morning Prayer",
            "journal" to "Evening Prayer",
        )
        return order.filter { it.first in chosen }.map { it.second }.distinct()
    }
}
