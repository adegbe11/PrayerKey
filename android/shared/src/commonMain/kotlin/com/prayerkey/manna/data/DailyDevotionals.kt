package com.prayerkey.manna.data

import kotlinx.datetime.LocalDate
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
    val topic = topicFor(date, topics)
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
 * Which written prayer belongs to a date.
 *
 * The whole library rotated by date, which put the Good Friday prayer on an
 * October morning and a birthday prayer for someone's son on a stranger's
 * Tuesday. Feast days now get their own prayer on the day itself, the first
 * of the month gets the new-month prayer, and every other day draws from
 * prayers that suit anyone on any morning.
 */
fun topicFor(date: LocalDate, topics: List<PrayerTopic>): PrayerTopic {
    fun slug(s: String) = topics.firstOrNull { it.slug == s }
    val easter = ChallengeSeason.easter(date.year)
    val fromEaster = (date.toEpochDays() - easter.toEpochDays()).toInt()
    val christmas = LocalDate(date.year, 12, 25)
    // Advent begins on the fourth Sunday before Christmas
    val isAdventSunday = date.dayOfWeek.ordinal == 6 &&
        date.toEpochDays() in (christmas.toEpochDays() - 28)..(christmas.toEpochDays() - 22)
    val feast = when {
        date.monthNumber == 1 && date.dayOfMonth == 1 -> "new-year-prayer"
        date.monthNumber == 12 && date.dayOfMonth == 31 -> "prayer-for-new-years-eve"
        date.monthNumber == 12 && date.dayOfMonth == 25 -> "prayer-for-christmas"
        fromEaster == -46 -> "prayer-for-lent"
        fromEaster == -7 -> "prayer-for-palm-sunday"
        fromEaster == -2 -> "prayer-for-good-friday"
        fromEaster == 0 -> "prayer-for-easter-sunday"
        fromEaster == 49 -> "prayer-for-pentecost"
        isAdventSunday -> "prayer-for-advent"
        date.dayOfMonth == 1 -> "new-month-prayer"
        else -> null
    }
    feast?.let(::slug)?.let { return it }
    val pool = EverydayPrayers.mapNotNull(::slug).ifEmpty { topics }
    /* A stride coprime with the pool, so consecutive days land in different
       parts of it rather than walking it in order. */
    return pool[(date.toEpochDays().toLong() * 7).mod(pool.size.toLong()).toInt()]
}

/**
 * True for a prayer that belongs to one day or one occasion: a feast, a
 * birthday, a month, a weekday, a dinner. Those are found by searching or
 * on their own day, never handed out by rotation.
 */
fun PrayerTopic.isOccasion(): Boolean = OccasionWords.containsMatchIn(title)

private val OccasionWords = Regex(
    """christmas|easter|good friday|palm sunday|pentecost|advent|\blent\b|new year|new month|thanksgiving dinner|""" +
        """mother's day|father's day|valentine|halloween|birthday|anniversary|graduation|back to school|wedding|""" +
        """dinner|monday|tuesday|wednesday|thursday|friday|saturday|sunday|january|february|\bmarch\b|april|\bmay\b|june|""" +
        """july|august|september|october|november|december|for her\b|for him\b|my love|retirement|moving|meeting|""" +
        """night shift|lost things|funeral|surgery|\bexam""",
    RegexOption.IGNORE_CASE,
)

/** Prayers that fit anyone on any morning, by slug. */
private val EverydayPrayers = listOf(
    "strength", "hope", "faith", "forgiveness", "peace", "courage", "spiritual-growth", "worship", "perseverance", "trust",
    "waiting-on-god", "breakthrough", "hearing-god", "pride", "prayer-of-surrender", "prayer-for-good-luck",
    "prayer-based-on-psalm-51", "prayer-based-on-romans-8", "prayer-of-hannah", "prayer-of-moses", "prayer-of-elijah",
    "prayer-of-solomon", "prayer-of-st-francis", "declarative-prayer", "prayer-when-nothing-is-working",
    "prayer-for-holy-spirit", "prayer-based-on-psalm-23", "prayer-based-on-philippians-4", "prayer-based-on-isaiah-40-31",
    "prayer-of-jabez", "prayer-of-david", "prayer-based-on-ephesians-6", "prayer-of-paul", "prayer-when-god-feels-distant",
    "prayer-for-spiritual-renewal", "purpose", "wisdom", "direction", "calling", "discipline", "identity", "contentment",
    "new-beginnings", "letting-go-of-past", "prayer-for-divine-direction", "prayer-based-on-jeremiah-29-11",
    "prayer-of-nehemiah", "protection", "home-protection", "prayer-based-on-psalm-91", "prayer-based-on-psalm-121",
    "morning", "daily", "prayer-for-today", "prayer-for-a-good-day", "prayer-of-the-day", "prayer-for-this-morning",
    "intercessory-prayer", "declaration-prayer", "prayer-for-miracles",
)

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
