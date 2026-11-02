package com.prayerkey.manna.ui.platform

import com.prayerkey.manna.data.FastingChallenge
import com.prayerkey.manna.data.FastingStore
import com.prayerkey.manna.data.PrayerChallengeStore
import com.prayerkey.manna.data.PrayerMemory
import com.prayerkey.manna.data.PullsProgress
import com.prayerkey.manna.data.challengeDayPage
import com.prayerkey.manna.data.fastMoment
import com.prayerkey.manna.data.hourPrayer
import com.prayerkey.manna.platform.PlatformContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Every reminder the app sets, behind one door.
 *
 * Android keeps its alarm receivers, which build the words at the moment
 * they fire ("you stopped at six"); the app installs them at launch. iPhone
 * cannot run the app when a notification is due, so it plans the coming days'
 * notifications ahead, with the same words, from [ReminderContent].
 */
interface ReminderScheduler {
    fun schedule(context: PlatformContext, hour: Int, minute: Int, enabled: Boolean)
    fun scheduleFor(context: PlatformContext, key: String, hour: Int, minute: Int, enabled: Boolean)
    fun scheduleBibleReading(context: PlatformContext, enabled: Boolean)
    fun scheduleDailySuite(context: PlatformContext, enabled: Boolean)
    fun scheduleFast(context: PlatformContext, fast: FastingChallenge)
    fun cancelFast(context: PlatformContext)
    fun schedulePrayerMemory(context: PlatformContext, kept: PrayerMemory.Kept)
    fun cancelPrayerMemory(context: PlatformContext, id: Long)
}

object Reminders : ReminderScheduler {
    /** Android's receivers, installed by the app at launch; the iPhone planner by default there. */
    var scheduler: ReminderScheduler = defaultReminders()

    override fun schedule(context: PlatformContext, hour: Int, minute: Int, enabled: Boolean) = scheduler.schedule(context, hour, minute, enabled)
    override fun scheduleFor(context: PlatformContext, key: String, hour: Int, minute: Int, enabled: Boolean) =
        scheduler.scheduleFor(context, key, hour, minute, enabled)
    override fun scheduleBibleReading(context: PlatformContext, enabled: Boolean) = scheduler.scheduleBibleReading(context, enabled)
    override fun scheduleDailySuite(context: PlatformContext, enabled: Boolean) = scheduler.scheduleDailySuite(context, enabled)
    override fun scheduleFast(context: PlatformContext, fast: FastingChallenge) = scheduler.scheduleFast(context, fast)
    override fun cancelFast(context: PlatformContext) = scheduler.cancelFast(context)
    override fun schedulePrayerMemory(context: PlatformContext, kept: PrayerMemory.Kept) = scheduler.schedulePrayerMemory(context, kept)
    override fun cancelPrayerMemory(context: PlatformContext, id: Long) = scheduler.cancelPrayerMemory(context, id)
}

expect fun defaultReminders(): ReminderScheduler

/** The kinds of daily reminder, their default times and where each opens. Android's ReminderKind, shared. */
enum class ReminderKind(
    val key: String,
    val title: String,
    val message: String,
    val destination: String,
    val hour: Int,
    val minute: Int,
    val dayOfWeek: DayOfWeek? = null,
) {
    PRAYER("prayer", "Prayer of the day", "Your prayer is ready.", "prayer", 6, 0),
    BIBLE("bible", "Bible reading", "Today's passage is ready.", "bible", 7, 30),
    DEVOTIONAL("devotional", "Today's devotional", "Open today's devotional.", "devotional", 8, 0),
    PRAYER_CHALLENGE("prayer_challenge", "Prayer challenge", "Today's prayer is ready.", "prayer_challenge", 12, 0),
    JOURNAL("journal", "Journal", "What's on your mind today?", "journal", 20, 30),
    MIDNIGHT("midnight", "Midnight prayer", "The midnight watch is open.", "midnight", 23, 55),
    CHURCH("church", "Sunday worship", "Church begins with a ready heart.", "church", 8, 30, DayOfWeek.SUNDAY),
    CALENDAR("calendar", "PrayerKey", "", "home", 9, 0),
    FAST("fast", "Your fast", "Take a moment to pray.", "fasting", 12, 0),
    ;

    companion object {
        fun from(key: String?): ReminderKind = entries.firstOrNull { it.key == key } ?: PRAYER
    }
}

/** The words of a reminder on a given day - what Android's receiver says when it fires. */
object ReminderContent {

    /** Title and text for [kind] on [date], or null when there is nothing to say that day. */
    fun message(context: PlatformContext, kind: ReminderKind, date: LocalDate, isToday: Boolean): Pair<String, String>? = when (kind) {
        ReminderKind.CALENDAR -> calendarMessage(date)
        // a future day has not been started, so it gets the opening line
        ReminderKind.PRAYER -> PullsProgress.Prayer.notificationLine(context, date).takeIf { isToday }
            ?: ("Today's prayer" to "Ten pulls. Four minutes. Start with the first.")
        ReminderKind.BIBLE -> PullsProgress.Bible.notificationLine(context, date).takeIf { isToday }
            ?: ("Today's reading" to "Ten pulls through the chapter. Start with the first.")
        ReminderKind.PRAYER_CHALLENGE -> {
            val challenge = PrayerChallengeStore.load(context)
            if (!challenge.started || challenge.complete) null else {
                val day = challenge.dayFor(date)
                if (day > challenge.days) null else {
                    val page = challengeDayPage(challenge, day)
                    "Day ${page.day} of ${page.of} · ${challenge.heading}" to "${page.verse} ${page.verseRef}"
                }
            }
        }
        ReminderKind.FAST -> {
            val fast = FastingStore.load(context)
            if (!fast.running || fast.finished) null else {
                val at = date.atStartOfDayIn(TimeZone.currentSystemDefault()).epochSeconds + kind.hour * 3600L
                val day = fast.dayOf(at)
                if (day > fast.days) null else {
                    val moment = fastMoment(fast, day)
                    "Day $day of ${fast.days} · your fast" to "${moment.focus}. ${moment.verse} ${moment.ref}"
                }
            }
        }
        ReminderKind.MIDNIGHT -> hourPrayer("midnight", date).let { "Midnight prayer · ${it.title}" to "${it.verse} ${it.ref}" }
        else -> kind.title to kind.message
    }

    fun calendarMessage(date: LocalDate): Pair<String, String>? {
        val easter = easterSunday(date.year)
        val m = date.monthNumber
        val d = date.dayOfMonth
        return when {
            date == easter.minus(DatePeriod(days = 46)) -> "Ash Wednesday" to "Begin Lent with prayer and repentance."
            date == easter.minus(DatePeriod(days = 7)) -> "Palm Sunday" to "Read Matthew 21:1–11."
            date == easter.minus(DatePeriod(days = 3)) -> "Maundy Thursday" to "Read John 13 and remember the Lord's Supper."
            date == easter.minus(DatePeriod(days = 2)) -> "Good Friday" to "Read the crucifixion account and pray."
            date == easter -> "Easter Sunday" to "Christ is risen. Read John 20."
            date == easter.plus(DatePeriod(days = 39)) -> "Ascension Day" to "Read Acts 1:1–11."
            date == easter.plus(DatePeriod(days = 49)) -> "Pentecost" to "Read Acts 2 and pray for the Church."
            m == 1 && d == 1 -> "New Year prayer" to "Begin the year in prayer."
            m == 1 && d == 6 -> "Epiphany" to "Read Matthew 2 today."
            m == 4 && d == 7 -> "World Health Day" to "Pray for the sick and those who care for them."
            m == 10 && d == 31 -> "Reformation Day" to "Read Romans 1:16–17."
            m == 11 && date.dayOfWeek == DayOfWeek.SUNDAY && d >= 27 -> "Advent" to "Prepare your heart for the coming of Christ."
            m == 12 && d == 25 -> "Christmas Day" to "Read Luke 2 and give thanks for Christ."
            else -> null
        }
    }

    fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate(year, month, day)
    }

    /** "On Sunday 4 October you prayed: ..." */
    fun prayerMemory(kept: PrayerMemory.Kept): Pair<String, String> =
        "Has God answered?" to "On ${kept.prayedLabel()} you prayed: ${kept.request}"
}
