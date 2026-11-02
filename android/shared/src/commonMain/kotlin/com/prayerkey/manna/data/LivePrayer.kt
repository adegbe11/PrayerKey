package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import kotlinx.datetime.LocalDate

/**
 * Live prayer: say what you want to pray about, get the verses for it, and
 * pray against a timer. The 6 AM reminder opens it.
 *
 * The verses come from the prayer library's written topics, matched to the
 * person's own words by [PrayerMachine]; when nothing matches, two promises
 * about prayer itself stand in so nobody is left with an empty screen.
 */
object LivePrayer {

    /** How long to pray, in minutes, offered under the verses. */
    val LENGTHS = listOf(3, 5, 10, 15)
    const val DEFAULT_MINUTES = 5

    /** Quick answers to "what do you want to pray about?", for a sleepy thumb at 6 AM. */
    val SUGGESTIONS = listOf("My family", "Money", "My health", "My work", "Direction", "Peace of mind", "Protection", "Thanksgiving")

    val FALLBACK: List<Pair<String, String>> = listOf(
        "Jeremiah 33:3" to "Call unto me, and I will answer thee, and shew thee great and mighty things, which thou knowest not.",
        "Philippians 4:6" to "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God.",
    )

    /** The verses for what someone said they want to pray about, at most three. */
    fun versesFor(request: String, topics: List<PrayerTopic>): Pair<String?, List<Pair<String, String>>> {
        val topic = PrayerMachine.guide(request, topics)?.topic
        val verses = topic?.scripture?.take(3).orEmpty()
        return (topic?.title) to verses.ifEmpty { FALLBACK }
    }

    private const val STORE = "live_prayer"

    fun joined(context: PlatformContext, date: LocalDate): Boolean =
        context.keyValues(STORE).getBoolean(date.toString(), false)

    fun markJoined(context: PlatformContext, date: LocalDate) {
        context.keyValues(STORE).edit().putBoolean(date.toString(), true).apply()
    }
}
