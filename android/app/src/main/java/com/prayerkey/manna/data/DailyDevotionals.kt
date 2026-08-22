package com.prayerkey.manna.data

import java.time.LocalDate
import com.prayerkey.manna.model.DailyPrayer

/**
 * The day's prayer, drawn from the 543 written prayers the app already ships.
 *
 * What this replaces was a template. Measured across a year, it produced 365
 * "different" prayers — but blank out the one substituted verse reference and
 * **only twelve remained**, on the same twelve-day cycle as the old devotional
 * themes. "Faithful in Small Things" landed on the 14th of August and again on
 * the 26th. A daily user would have noticed inside a fortnight.
 *
 * There was no need to write anything: [LocalPrayerTopics] already holds 543
 * full prayers with their own scripture, bundled and offline. Rotating those by
 * date gives 543 genuinely distinct prayers instead of twelve wearing costumes,
 * and it uses content that was already there and already good.
 *
 * [topics] is passed in rather than loaded here because it comes from an asset
 * and this stays a pure function of the date — the property that makes the
 * whole plan the same for everybody on the same calendar day.
 *
 * Falls back to a single plain prayer when the topics have not loaded yet, so
 * a cold start shows a prayer rather than a gap.
 */
fun dailyPrayerFor(date: LocalDate, topics: List<PrayerTopic>): DailyPrayer {
    if (topics.isEmpty()) return openingPrayer(date)
    /* A stride coprime with almost any list length, so consecutive days land in
       different parts of the library rather than walking it in order — two
       prayers about grief on Monday and Tuesday read as a rut. */
    val topic = topics[Math.floorMod(date.toEpochDay() * 7, topics.size.toLong()).toInt()]
    return DailyPrayer(
        ref = topic.scripture.firstOrNull()?.first ?: passageFor(date).reference,
        title = topic.title,
        verse = topic.scripture.firstOrNull()?.second ?: "",
        prayer = buildString {
            append("Father, thank You for the gift of this morning and for the breath in my body. I place this new day in Your hands. ")
            append("Go before me, order my steps, guard my thoughts, and let my words and choices honour You.\n\n")
            append(topic.prayer)
            append("\n\nAs I leave this prayer, keep me aware of Your presence. Give me wisdom for every decision, strength for every responsibility, and kindness for every person I meet. ")
            append("Protect my home and everyone connected to me. Let me recognise Your help, obey Your direction, and return tonight with gratitude. In Jesus' name, Amen.")
        },
    )
}

/**
 * What to pray before the library has loaded.
 *
 * One prayer, said plainly, rather than the twelve-theme generator this
 * replaces — that produced 365 "different" prayers a year of which only twelve
 * were actually different, and it existed only to fill the second or two
 * before 543 real prayers finish parsing.
 */
private fun openingPrayer(date: LocalDate): DailyPrayer {
    val passage = passageFor(date)
    return DailyPrayer(
        ref = passage.reference,
        title = "A prayer to begin",
        verse = "",
        prayer = "Father, before I ask You for anything, thank You for this day and " +
            "for the breath to pray it. Order my steps, quiet my mind, and let " +
            "everything I do today be done in Your presence. In Jesus' name, Amen.",
    )
}
