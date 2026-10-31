package com.prayerkey.manna.data

import kotlinx.datetime.LocalDate

/**
 * Today's prayer points: the one thing PrayerKey asks of anyone each day.
 *
 * A prayer meeting in this tradition, in about ten minutes: the Word read
 * first, then praise, covering, petitions, standing, the people you promised
 * to pray for by name, and the seal. Each point is read aloud and then left
 * quiet, because a prayer point is something to pray, not something to hear.
 * The quiet is the session; the voice only hands you the next point.
 *
 * Built from the same day as the rest of Today: [topic] is the day's matched
 * prayer, so the reading, the devotional and the points are about one thing.
 */
data class PointsDay(
    val date: LocalDate,
    val title: String,
    val steps: List<PointStep>,
    /** The people prayed for by name, to mark as prayed once the session ends. */
    val peopleIds: List<Long>,
    val peopleNames: List<String> = emptyList(),
) {
    /** Reading and quiet together, rounded to whole minutes. */
    val minutes: Int get() = ((steps.sumOf { it.praySeconds + it.text.length / 14 } + 59) / 60).coerceAtLeast(1)
}

data class PointStep(
    val label: String,
    val text: String,
    /** The verse reference, on the Word only. */
    val reference: String? = null,
    /** How long to leave quiet after the point is read, to pray it in your own words. */
    val praySeconds: Int,
)

private const val PERSON_SECONDS = 40

fun pointsDayFor(date: LocalDate, topic: PrayerTopic?, people: List<PrayerPerson>): PointsDay {
    val day = date.toEpochDays()
    val steps = mutableListOf<PointStep>()

    // the Word first: the verse the points stand on, read and then sat with
    val verse = topic?.scripture?.firstOrNull { it.first.isNotBlank() && it.second.isNotBlank() }
    if (verse != null) steps += PointStep("THE WORD", verse.second, verse.first, praySeconds = 20)

    val points = topic?.let { PrayerPointEngine.dailyPoints(it, day) }.orEmpty()
    val sealing = points.filter { it.first == PrayerPointEngine.Part.SEALING }
    points.filter { it.first != PrayerPointEngine.Part.SEALING }.forEach { (part, text) ->
        steps += PointStep(
            label = part.label,
            text = text,
            praySeconds = when (part) {
                PrayerPointEngine.Part.PETITION -> 60
                PrayerPointEngine.Part.WORD, PrayerPointEngine.Part.STANDING, PrayerPointEngine.Part.PRAISE -> 45
                else -> 30
            },
        )
    }

    /* For others, by name. Three at most, oldest promise first among those not
       yet prayed today, so a long list is still prayed through over the week. */
    val waiting = people.filter { it.answered == 0L }
        .sortedWith(compareBy<PrayerPerson> { it.prayedOn(day.toLong()) }.thenBy { it.prayed.lastOrNull() ?: 0L })
        .take(3)
    if (waiting.isEmpty()) {
        steps += PointStep("FOR OTHERS", "Father, I lift up my family, my church and my nation before You, in the name of Jesus.", praySeconds = PERSON_SECONDS)
    } else waiting.forEach { person ->
        val asked = person.request.trim().trimEnd('.')
        val text = if (asked.isEmpty()) "Father, I lift up ${person.name} before You. Meet them where they are today, in the name of Jesus."
        else "Father, I lift up ${person.name} before You: ${asked.replaceFirstChar { it.lowercase() }}. Answer them, in the name of Jesus."
        steps += PointStep("FOR ${person.name.uppercase()}", text, praySeconds = PERSON_SECONDS)
    }

    sealing.forEach { (_, text) -> steps += PointStep("SEAL", text, praySeconds = 20) }
    if (steps.isEmpty()) {
        // a cold start with nothing loaded still prays rather than showing an empty screen
        steps += PointStep("PRAY", "Father, I come before You this day. Hear my prayer and order my steps, in the name of Jesus.", praySeconds = 60)
    }

    return PointsDay(
        date = date,
        title = topic?.title ?: "Today's prayer points",
        steps = steps,
        peopleIds = waiting.map { it.id },
        peopleNames = waiting.map { it.name },
    )
}
