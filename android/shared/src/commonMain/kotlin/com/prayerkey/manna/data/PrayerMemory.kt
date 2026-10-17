package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/**
 * The prayer machine's memory: what was prayed, and whether God has answered.
 *
 * Every guided prayer that is kept is remembered here, on the phone and nowhere
 * else. A week later the machine asks about it - "On 4 October you prayed for
 * your mother. Has God answered?" - and the answer becomes a testimony, or the
 * prayer is prayed again and asked about a week after that. Asking is how a
 * prayer life learns to notice answers; most answered prayers are never
 * counted because nobody comes back to look.
 */
object PrayerMemory {

    data class Kept(
        val id: Long,
        val request: String,
        val title: String,
        /** when it was last prayed, epoch millis */
        val prayedAt: Long,
        val answeredAt: Long = 0L,
    ) {
        val answered: Boolean get() = answeredAt > 0L

        /** "Sunday 4 October" */
        fun prayedLabel(zone: TimeZone = TimeZone.currentSystemDefault()): String {
            val d = Instant.fromEpochMilliseconds(prayedAt).toLocalDateTime(zone).date
            val day = d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
            val month = d.month.name.lowercase().replaceFirstChar { it.uppercase() }
            return "$day ${d.dayOfMonth} $month"
        }
    }

    /** How long before the machine asks. */
    const val FOLLOW_UP_DAYS = 7L

    /** It asks in the evening, when there is time to answer. */
    private const val ASK_HOUR = 19

    private fun prefs(context: PlatformContext) = context.keyValues("prayer_memory")

    private fun now() = Clock.System.now().toEpochMilliseconds()

    fun all(context: PlatformContext): List<Kept> = runCatching {
        (Json.parseToJsonElement(prefs(context).getString("kept", "[]") ?: "[]") as JsonArray).map { e ->
            val o = e.jsonObject
            Kept(
                id = o.getValue("id").jsonPrimitive.long,
                request = o.getValue("request").jsonPrimitive.content,
                title = o["title"]?.jsonPrimitive?.content.orEmpty(),
                prayedAt = o.getValue("prayedAt").jsonPrimitive.long,
                answeredAt = o["answeredAt"]?.jsonPrimitive?.longOrNull ?: 0L,
            )
        }
    }.getOrDefault(emptyList())

    fun find(context: PlatformContext, id: Long): Kept? = all(context).firstOrNull { it.id == id }

    private fun save(context: PlatformContext, list: List<Kept>) {
        // a phone keeps the last 200; older answered prayers live on in the journal
        val arr = buildJsonArray {
            list.takeLast(200).forEach {
                addJsonObject {
                    put("id", it.id); put("request", it.request); put("title", it.title)
                    put("prayedAt", it.prayedAt); put("answeredAt", it.answeredAt)
                }
            }
        }
        prefs(context).edit().putString("kept", arr.toString()).apply()
    }

    /**
     * Remembers a prayer and returns it. Praying the same thing again does
     * not make a second memory - it moves the one already there forward, so
     * the question comes a week after the latest prayer, once.
     */
    fun remember(context: PlatformContext, request: String, title: String, now: Long = now()): Kept {
        val list = all(context)
        val same = list.firstOrNull { !it.answered && it.request.trim().equals(request.trim(), ignoreCase = true) }
        val kept = same?.copy(prayedAt = now, title = title) ?: Kept(now, request.trim(), title, now)
        save(context, list.filterNot { it.id == kept.id } + kept)
        return kept
    }

    fun markAnswered(context: PlatformContext, id: Long, now: Long = now()) {
        save(context, all(context).map { if (it.id == id) it.copy(answeredAt = now) else it })
    }

    /** Still praying: asked about again a week from now. */
    fun stillPraying(context: PlatformContext, id: Long, now: Long = now()): Kept? {
        val kept = find(context, id)?.copy(prayedAt = now) ?: return null
        save(context, all(context).map { if (it.id == id) kept else it })
        return kept
    }

    /** The moment the machine asks about a prayer kept at [prayedAt]. */
    fun followUpAt(prayedAt: Long, zone: TimeZone = TimeZone.currentSystemDefault()): Long {
        val day = Instant.fromEpochMilliseconds(prayedAt).toLocalDateTime(zone).date
            .plus(FOLLOW_UP_DAYS.toInt(), DateTimeUnit.DAY)
        return day.atTime(ASK_HOUR, 0).toInstant(zone).toEpochMilliseconds()
    }

    /** Prayers still waiting for an answer, to be asked about again after a restart. */
    fun waiting(context: PlatformContext): List<Kept> = all(context).filterNot { it.answered }
}
