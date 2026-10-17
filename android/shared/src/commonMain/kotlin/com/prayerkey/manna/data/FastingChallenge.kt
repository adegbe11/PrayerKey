package com.prayerkey.manna.data

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn


/**
 * The Fasting Challenge: how long, what kind, what for, and when you pray
 * through it.
 *
 * A fast is time-bound in a way a prayer challenge is not - it runs against a
 * clock, and the clock is the point. So what is kept is a start instant and an
 * end instant, and everything the screens show is worked out from those two:
 * which day of it you are on, how long remains, how far through you are.
 */
enum class FastLength(val label: String, val blurb: String, val hours: Int) {
    SIX("6 hours", "A short, focused fast", 6),
    TWELVE("12 hours", "Build discipline", 12),
    SUNRISE("Sunrise to sunset", "A biblical fast", 12),
    DAY("24 hours", "Go deeper", 24),
    THREE("3 days", "Seek God intentionally", 72),
    SEVEN("7 days", "A transformational journey", 168),
    TWENTY_ONE("21 days", "A deeper commitment", 504),
    CUSTOM("Custom", "Set your own length", 0),
    ;

    val days: Int get() = ((hours + 23) / 24).coerceAtLeast(1)
    val multiDay: Boolean get() = hours >= 48
}

enum class FastType(val label: String, val blurb: String) {
    WATER("Water fast", "Only water"),
    DANIEL("Daniel fast", "Fruits, vegetables, whole foods"),
    ONE_MEAL("One meal a day", "Eat one meal daily"),
    EVENING("No food until evening", "Fast during the day"),
    PARTIAL("Partial fast", "Give up specific foods"),
    SOCIAL("Social media fast", "Fast from social media"),
    CUSTOM("Custom", "Set your own rules"),
}

val FAST_PURPOSES = listOf(
    "Breakthrough" to "A new season",
    "Direction" to "Clarity for decisions",
    "Healing" to "For you or a loved one",
    "Family" to "Lift up your family",
    "Deliverance" to "Freedom in every area",
    "Spiritual growth" to "Go deeper with God",
    "Gratitude" to "A heart of thankfulness",
    "Personal request" to "Write your own",
)

enum class PrayerRhythm(val label: String, val blurb: String, val hour: Int) {
    MORNING("Morning", "Start your day with God", 6),
    MIDDAY("Midday", "Stay aligned", 12),
    EVENING("Evening", "End your day with prayer", 19),
    MIDNIGHT("Midnight", "A quiet hour", 0),
}

data class FastingChallenge(
    val length: FastLength = FastLength.SEVEN,
    /** Hours, when the length is custom. */
    val customHours: Int = 24,
    val type: FastType = FastType.WATER,
    val purpose: String = "Breakthrough",
    val note: String = "",
    /** When the fast begins, as epoch seconds. 0 means not begun. */
    val startedAt: Long = 0L,
    val breakHour: Int = 18,
    val breakMinute: Int = 0,
    val dailyBreak: Boolean = false,
    val rhythms: Set<PrayerRhythm> = setOf(PrayerRhythm.MORNING),
    val reminders: Boolean = true,
    /** Days of a multi-day fast that were finished, counted from 1. */
    val kept: Set<Int> = emptySet(),
    val prayerSessions: Int = 0,
    val scriptures: Int = 0,
    val entries: Int = 0,
    val testimony: String = "",
    val answered: Boolean = false,
) {
    val hours: Int get() = if (length == FastLength.CUSTOM) customHours else length.hours

    val days: Int get() = ((hours + 23) / 24).coerceAtLeast(1)

    val running: Boolean get() = startedAt > 0L

    val title: String get() = if (days > 1) "$days day fast" else length.label + " fast"


    fun endsAt(): Long = startedAt + hours * 3600L

    /** Seconds still to go, floored at zero. */
    fun remaining(nowSeconds: Long): Long = (endsAt() - nowSeconds).coerceAtLeast(0)

    fun elapsed(nowSeconds: Long): Long = (nowSeconds - startedAt).coerceAtLeast(0)

    fun fraction(nowSeconds: Long): Float =
        if (hours <= 0) 0f else (elapsed(nowSeconds).toFloat() / (hours * 3600f)).coerceIn(0f, 1f)

    fun dayOf(nowSeconds: Long): Int = (elapsed(nowSeconds) / 86_400L).toInt() + 1

    val finished: Boolean get() = running && remaining(Clock.System.now().toEpochMilliseconds() / 1000) == 0L

    fun breakLabel(): String {
        val h = if (breakHour % 12 == 0) 12 else breakHour % 12
        return "$h:${breakMinute.toString().padStart(2, '0')} ${if (breakHour < 12) "AM" else "PM"}"
    }

    fun endLabel(): String =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(DatePeriod(days = days - 1)).let { d ->
            // "Mon, 6 Oct", as java.time's "EEE, d MMM" wrote it
            "${d.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}, ${d.dayOfMonth} " +
                d.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        }
}



/** The verse and the focus for this hour of the fast. */
data class FastMoment(val verse: String, val ref: String, val focus: String, val prayer: String)

private val FAST_VERSES = listOf(
    Triple(
        "When you seek me with all your heart, you will find me.",
        "Jeremiah 29:13",
        "Seek Him first, before the day takes you",
    ),
    Triple(
        "Those who hope in the Lord will renew their strength.",
        "Isaiah 40:31",
        "Hand Him the weakness you are feeling",
    ),
    Triple(
        "Man shall not live by bread alone, but by every word of God.",
        "Matthew 4:4",
        "Feed on the Word where the hunger is",
    ),
    Triple(
        "Is not this the fast that I have chosen? To loose the bands of wickedness.",
        "Isaiah 58:6",
        "Ask for freedom, for you and for someone else",
    ),
    Triple(
        "But this kind does not go out except by prayer and fasting.",
        "Matthew 17:21",
        "Keep asking for the thing that has not moved",
    ),
    Triple(
        "The Lord gives food to the hungry.",
        "Psalm 146:7",
        "Thank Him for what He has already provided",
    ),
    Triple(
        "Let us not grow weary in doing good, for at the proper time we will reap.",
        "Galatians 6:9",
        "Finish the hours you have left",
    ),
)

private val FAST_PRAYERS = listOf(
    "Father, I am hungry, and I am bringing the hunger to You. Let it turn into prayer every time I feel it. Amen.",
    "Lord, strengthen me for the hours ahead. Where my body is weak, let my spirit be willing. Amen.",
    "God, I am not fasting to be seen. I am fasting to be near You. Come close. Amen.",
    "Father, break what needs breaking in me, and set free what needs freeing. Amen.",
    "Lord, I ask again for the thing I have been asking for. Move, in Your time and Your way. Amen.",
)

fun fastMoment(fast: FastingChallenge, day: Int): FastMoment {
    val (verse, ref, focus) = FAST_VERSES[(day - 1) % FAST_VERSES.size]
    return FastMoment(
        verse = verse,
        ref = ref,
        focus = if (fast.note.isNotBlank()) fast.note else focus,
        prayer = FAST_PRAYERS[(day - 1) % FAST_PRAYERS.size],
    )
}

