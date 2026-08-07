package com.prayerkey.manna.data

import java.time.LocalDate

/**
 * Today's devotion, and the two challenges.
 *
 * All three are derived from what the app already ships — the daily verses,
 * the 543 prayer topics, the canon — and are chosen by the date. That matters
 * for three reasons: they work with the radio off, they cost no backend, and
 * everyone opening the app on the same morning gets the same reading, which
 * is what makes a challenge something you can do *with* other people.
 *
 * Nothing here invents content. The devotion's reflection is a question put
 * to the verse, not a paragraph of commentary I would be making up.
 */

data class Devotion(
    val date: LocalDate,
    val reference: String,
    val verse: String,
    /** A question to sit with. */
    val reflection: String,
    /** A prayer from the deck that answers the verse. */
    val prayerSlug: String?,
    val prayerTitle: String?,
)

data class ChallengeDay(
    val index: Int,
    val total: Int,
    val label: String,
    val detail: String,
    val done: Boolean,
)

data class Challenge(
    val id: String,
    val title: String,
    val note: String,
    val today: ChallengeDay,
)

/**
 * Questions put to the verse rather than answers supplied for it.
 *
 * A devotional that tells you what a verse means is a book; one that asks you
 * something is a prompt you can actually write to, which is the point of
 * having a journal one tap away.
 */
private val REFLECTIONS = listOf(
    "Where do you need this to be true today?",
    "Who came to mind while you read that?",
    "What would change if you believed this before lunch?",
    "What is this asking you to put down?",
    "Say it back in your own words.",
    "What have you been carrying that this speaks to?",
    "Is there someone you should send this to?",
)

/** A day's index, stable for everyone in the same calendar day. */
private fun dayIndex(date: LocalDate): Int = date.toEpochDay().toInt()

fun devotionFor(
    date: LocalDate,
    verses: List<Triple<String, String, String>>,
    topics: List<PrayerTopic>,
): Devotion? {
    if (verses.isEmpty()) return null
    val day = dayIndex(date)
    val (reference, translation, text) = verses[Math.floorMod(day, verses.size)]
    val topic = topics.takeIf { it.isNotEmpty() }?.get(Math.floorMod(day * 7, topics.size))
    return Devotion(
        date = date,
        reference = reference,
        verse = text,
        reflection = REFLECTIONS[Math.floorMod(day, REFLECTIONS.size)],
        prayerSlug = topic?.slug,
        prayerTitle = topic?.title,
    )
}

/**
 * Read a gospel in a month.
 *
 * A plan needs a fixed length and a fixed order or "day 4" means nothing, so
 * this walks John's 21 chapters and then Mark's 16 — 37 days, then it comes
 * round again. [doneDays] is how many days the person has actually marked.
 */
fun bibleChallenge(date: LocalDate, doneDays: Set<Int>): Challenge {
    val plan = buildList {
        (1..21).forEach { add("John" to it) }
        (1..16).forEach { add("Mark" to it) }
    }
    val day = Math.floorMod(dayIndex(date), plan.size)
    val (book, chapter) = plan[day]
    return Challenge(
        id = "read-the-gospels",
        title = "Read the Gospels",
        note = "John, then Mark — one chapter a day",
        today = ChallengeDay(
            index = day + 1,
            total = plan.size,
            label = "$book $chapter",
            detail = "Day ${day + 1} of ${plan.size}",
            done = day in doneDays,
        ),
    )
}

/**
 * Twenty-one days of praying for something other than yourself.
 *
 * The topics come from the deck we already ship, walked in a fixed order from
 * the day the challenge started so the sequence is the same for everybody.
 */
fun prayerChallenge(
    date: LocalDate,
    topics: List<PrayerTopic>,
    doneDays: Set<Int>,
): Challenge? {
    if (topics.isEmpty()) return null
    val total = 21
    val day = Math.floorMod(dayIndex(date), total)
    val topic = topics[Math.floorMod(dayIndex(date) * 13, topics.size)]
    return Challenge(
        id = "twenty-one-days",
        title = "21 days of prayer",
        note = "One prayer a day, prayed for someone else",
        today = ChallengeDay(
            index = day + 1,
            total = total,
            label = topic.title,
            detail = "Day ${day + 1} of $total",
            done = day in doneDays,
        ),
    )
}
