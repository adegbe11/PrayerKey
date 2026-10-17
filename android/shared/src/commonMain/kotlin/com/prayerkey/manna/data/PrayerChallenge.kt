package com.prayerkey.manna.data

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

import com.prayerkey.manna.model.prayerForDay

/**
 * Prayer + Challenge: a number of days, a thing you are asking God for, and
 * a time you show up.
 *
 * The whole of it is set once - how long, what for, how you want to pray, and
 * when - and after that every day is the same short shape: a verse, a focus,
 * three things to pray, and a button you hold. What is kept is what can be
 * looked back on: which days were prayed, how long, what was written, and
 * whether the thing asked for was ever answered.
 */
enum class ChallengePractice(val label: String) {
    PRAYER("Prayer only"),
    FASTING("Prayer + fasting"),
    BIBLE("Prayer + Bible reading"),
    ALL("Prayer, fasting + Bible"),
    ;

    val readsScripture: Boolean get() = this == BIBLE || this == ALL
    val fasts: Boolean get() = this == FASTING || this == ALL
}

val CHALLENGE_LENGTHS = listOf(3, 7, 14, 21, 30, 40)

val CHALLENGE_FOCUSES = listOf(
    "Healing", "Family", "Work", "Breakthrough", "Gratitude", "Deliverance", "Purpose",
)

data class PrayerChallenge(
    val days: Int = 21,
    /** What this person is asking God for, in their own words. Optional. */
    val focus: String = "",
    /** The chosen heading, when they did not want to write their own. */
    val category: String = "",
    val practice: ChallengePractice = ChallengePractice.BIBLE,
    val hour: Int = 6,
    val minute: Int = 30,
    val reminder: Boolean = true,
    /** The day the challenge began, as an epoch day. 0 means not yet begun. */
    val startedOn: Long = 0L,
    /** Days completed, counted from 1. */
    val done: Set<Int> = emptySet(),
    /** Whether the thing asked for has been answered. */
    val answered: Boolean = false,
) {
    val started: Boolean get() = startedOn > 0L

    val heading: String get() = focus.ifBlank { category }.ifBlank { "This season" }

    /** Which day of the challenge today is, from 1. Runs past the end if unfinished. */
    fun dayFor(today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Int =
        if (!started) 1 else ((today.toEpochDays().toLong() - startedOn).toInt() + 1).coerceIn(1, days)

    val complete: Boolean get() = done.size >= days

    /** Days prayed in an unbroken run up to the current day. */
    fun streak(today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Int {
        var day = dayFor(today)
        var run = 0
        while (day >= 1 && day in done) {
            run++
            day--
        }
        return run
    }

    fun timeLabel(): String {
        val h = if (hour % 12 == 0) 12 else hour % 12
        val suffix = if (hour < 12) "AM" else "PM"
        return "$h:${minute.toString().padStart(2, '0')} $suffix"
    }
}



/** A day of the challenge: the verse, the focus, what to pray, what to read. */
data class ChallengeDayPage(
    val day: Int,
    val of: Int,
    val verse: String,
    val verseRef: String,
    val focus: String,
    val points: List<String>,
    val reading: String,
    val prayer: String,
)

private val POINTS: Map<String, List<String>> = mapOf(
    "Healing" to listOf(
        "Healing in my body, and in the body of the one I am carrying",
        "Steady hands and clear minds for the doctors and nurses",
        "Peace through the waiting and the tests",
        "Strength for the family who are caring for them",
        "Faith that does not depend on the report",
        "Rest tonight, and relief from pain",
    ),
    "Family" to listOf(
        "Peace in our home, starting with me",
        "Each of my children by name, and what they are facing",
        "My marriage, or the marriage God is preparing",
        "The relationship that has gone quiet",
        "Protection over everyone who sleeps under this roof",
        "Faith passed on, in what we do and not only what we say",
    ),
    "Work" to listOf(
        "Wisdom for today's decisions",
        "Favour with the people I answer to and the people I serve",
        "Provision for what this month requires",
        "Integrity when nobody is checking",
        "The door that only God can open",
        "Strength to do this work as unto Him",
    ),
    "Breakthrough" to listOf(
        "The thing I have prayed about for years",
        "Courage to keep asking without growing bitter",
        "Every obstacle that is not from God, removed",
        "Patience where He is asking me to wait",
        "A heart that will still worship if the answer is slow",
        "Eyes to see what He has already done",
    ),
    "Gratitude" to listOf(
        "Thank You for what I nearly overlooked today",
        "Thank You for the people You have put around me",
        "Thank You for what You protected me from",
        "Thank You for prayers already answered",
        "Thank You for enough, today",
        "Thank You that You are good when life is not",
    ),
    "Deliverance" to listOf(
        "Freedom from the habit I keep returning to",
        "The lie I have believed, replaced with truth",
        "Protection over my mind and my thoughts",
        "Everyone in my family bound by the same thing",
        "Strength at the hour I am usually weakest",
        "A way out, and the will to take it",
    ),
    "Purpose" to listOf(
        "Show me what You made me for",
        "Open the right door and close the wrong one",
        "Courage to say yes, and to say no",
        "The people I am meant to serve",
        "Faithfulness in the small things now",
        "That my life would matter to someone after me",
    ),
)

private val GENERAL = listOf(
    "What is on my heart today, said plainly",
    "The people God has put in front of me",
    "Wisdom for the decision I am facing",
    "Strength to keep showing up",
    "Someone else's need before my own",
    "That my life would look like what I pray",
)

private val READINGS = listOf(
    "Psalm 23", "Proverbs 3", "Matthew 6", "Psalm 91", "Romans 8", "Philippians 4",
    "Isaiah 40", "James 1", "Psalm 34", "John 15", "Ephesians 6", "Psalm 121",
    "Hebrews 11", "Psalm 51", "Luke 15", "1 Corinthians 13", "Psalm 139", "Colossians 3",
    "Psalm 46", "Isaiah 41",
)

/** The day's page, built from the challenge and the day. */
fun challengeDayPage(challenge: PrayerChallenge, day: Int): ChallengeDayPage {
    val prayer = prayerForDay(Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfYear + day)
    val pool = POINTS[challenge.category] ?: GENERAL
    val start = (day - 1) * 3
    val points = (0..2).map { pool[(start + it) % pool.size] }
    val heading = challenge.heading
    return ChallengeDayPage(
        day = day,
        of = challenge.days,
        verse = prayer.verse,
        verseRef = prayer.ref,
        focus = if (heading.isBlank()) "Bring today to God" else heading,
        points = points,
        reading = READINGS[(day - 1) % READINGS.size],
        prayer = prayer.prayer,
    )
}
