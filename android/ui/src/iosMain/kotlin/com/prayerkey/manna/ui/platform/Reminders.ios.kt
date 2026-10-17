package com.prayerkey.manna.ui.platform

import com.prayerkey.manna.data.FastMachine
import com.prayerkey.manna.data.FastingChallenge
import com.prayerkey.manna.data.PrayerMemory
import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.platform.today
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

actual fun defaultReminders(): ReminderScheduler = IosReminders

/**
 * The iPhone plans its reminders ahead. Android's alarms wake the app and it
 * decides what to say; iOS shows a notification without running the app, so
 * each one is written in advance, a few days at a time, and the plan is
 * rebuilt whenever the app opens (MannaApp re-arms the daily suite at launch).
 *
 * iOS keeps at most 64 waiting, so: six days of each daily kind, the next two
 * Sundays, calendar days within a fortnight, every moment of a running fast,
 * and each kept prayer's follow-up.
 */
object IosReminders : ReminderScheduler {
    private const val DAYS = 6
    private const val WEEKLY_DAYS = 14
    private const val MAX_FAST_MOMENTS = 40

    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override fun schedule(context: PlatformContext, hour: Int, minute: Int, enabled: Boolean) {
        context.keyValues("prayer_reminder").edit()
            .putInt("hour", hour).putInt("minute", minute).putBoolean("enabled", enabled).apply()
        plan(context, ReminderKind.PRAYER, hour, minute, enabled)
    }

    override fun scheduleFor(context: PlatformContext, key: String, hour: Int, minute: Int, enabled: Boolean) {
        val kind = ReminderKind.from(key)
        context.keyValues("chosen_reminders").edit()
            .putInt("${key}_hour", hour).putInt("${key}_minute", minute).putBoolean("${key}_on", enabled).apply()
        plan(context, kind, hour, minute, enabled)
    }

    override fun scheduleBibleReading(context: PlatformContext, enabled: Boolean) =
        scheduleKind(context, ReminderKind.BIBLE, enabled)

    override fun scheduleDailySuite(context: PlatformContext, enabled: Boolean) {
        val single = context.keyValues("onboarding_plan").getBoolean("single_reminder", false)
        val chosen = setOf(ReminderKind.PRAYER_CHALLENGE, ReminderKind.FAST)
        ReminderKind.entries.forEach {
            val on = if (it in chosen) true else enabled && (!single || it == ReminderKind.PRAYER)
            scheduleKind(context, it, on)
        }
    }

    private fun scheduleKind(context: PlatformContext, kind: ReminderKind, enabled: Boolean) {
        when (kind) {
            ReminderKind.PRAYER_CHALLENGE, ReminderKind.FAST -> {
                val chosen = context.keyValues("chosen_reminders")
                plan(
                    context, kind,
                    chosen.getInt("${kind.key}_hour", kind.hour), chosen.getInt("${kind.key}_minute", kind.minute),
                    enabled && chosen.getBoolean("${kind.key}_on", false),
                )
            }
            ReminderKind.PRAYER -> {
                val prefs = context.keyValues("prayer_reminder")
                plan(context, kind, prefs.getInt("hour", kind.hour), prefs.getInt("minute", kind.minute), enabled && prefs.getBoolean("enabled", true))
            }
            else -> plan(context, kind, kind.hour, kind.minute, enabled)
        }
    }

    private fun plan(context: PlatformContext, kind: ReminderKind, hour: Int, minute: Int, enabled: Boolean) {
        val span = if (kind.dayOfWeek != null || kind == ReminderKind.CALENDAR) WEEKLY_DAYS else DAYS
        center.removePendingNotificationRequestsWithIdentifiers((0 until WEEKLY_DAYS).map { "${kind.key}-$it" })
        if (!enabled) return
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val start = today()
        for (offset in 0 until span) {
            val date = start.plus(DatePeriod(days = offset))
            if (kind.dayOfWeek != null && date.dayOfWeek != kind.dayOfWeek) continue
            val at = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minute)
            if (at.toInstant(zone) <= now) continue
            val (title, body) = ReminderContent.message(context, kind, date, isToday = offset == 0) ?: continue
            add("${kind.key}-$offset", title.ifBlank { kind.title }, body, kind.destination, at)
        }
    }

    override fun scheduleFast(context: PlatformContext, fast: FastingChallenge) {
        cancelFast(context)
        val now = Clock.System.now().epochSeconds
        val zone = TimeZone.currentSystemDefault()
        FastMachine.plan(fast).take(MAX_FAST_MOMENTS).forEachIndexed { i, planned ->
            if (planned.at <= now) return@forEachIndexed
            val (title, text) = FastMachine.message(fast, planned.moment, planned.day)
            add("fast-$i", title, text, "fasting", Instant.fromEpochSeconds(planned.at).toLocalDateTime(zone))
        }
    }

    override fun cancelFast(context: PlatformContext) {
        center.removePendingNotificationRequestsWithIdentifiers((0 until MAX_FAST_MOMENTS).map { "fast-$it" })
    }

    override fun schedulePrayerMemory(context: PlatformContext, kept: PrayerMemory.Kept) {
        val zone = TimeZone.currentSystemDefault()
        val (title, text) = ReminderContent.prayerMemory(kept)
        add("memory-${kept.id}", title, text, "answered:${kept.id}", Instant.fromEpochMilliseconds(PrayerMemory.followUpAt(kept.prayedAt)).toLocalDateTime(zone))
    }

    override fun cancelPrayerMemory(context: PlatformContext, id: Long) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf("memory-$id"))
    }

    private fun add(id: String, title: String, body: String, destination: String, at: LocalDateTime) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
            // read by the app delegate when the notification is tapped, like Android's intent extra
            setUserInfo(mapOf<Any?, Any?>("prayerkey_destination" to destination))
        }
        val when_ = NSDateComponents().apply {
            year = at.year.toLong(); month = at.monthNumber.toLong(); day = at.dayOfMonth.toLong()
            hour = at.hour.toLong(); minute = at.minute.toLong()
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(when_, repeats = false)
        center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(id, content, trigger), withCompletionHandler = null)
    }
}
