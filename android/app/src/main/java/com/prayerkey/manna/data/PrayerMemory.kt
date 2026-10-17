package com.prayerkey.manna.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        fun prayedLabel(zone: ZoneId = ZoneId.systemDefault()): String =
            Instant.ofEpochMilli(prayedAt).atZone(zone).format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.UK))
    }

    /** How long before the machine asks. */
    const val FOLLOW_UP_DAYS = 7L

    /** It asks in the evening, when there is time to answer. */
    private val ASK_AT: LocalTime = LocalTime.of(19, 0)

    private fun prefs(context: Context) = context.getSharedPreferences("prayer_memory", Context.MODE_PRIVATE)

    fun all(context: Context): List<Kept> = runCatching {
        val arr = JSONArray(prefs(context).getString("kept", "[]"))
        List(arr.length()) { i ->
            arr.getJSONObject(i).let {
                Kept(it.getLong("id"), it.getString("request"), it.optString("title"), it.getLong("prayedAt"), it.optLong("answeredAt"))
            }
        }
    }.getOrDefault(emptyList())

    fun find(context: Context, id: Long): Kept? = all(context).firstOrNull { it.id == id }

    private fun save(context: Context, list: List<Kept>) {
        val arr = JSONArray()
        // a phone keeps the last 200; older answered prayers live on in the journal
        list.takeLast(200).forEach {
            arr.put(JSONObject().put("id", it.id).put("request", it.request).put("title", it.title).put("prayedAt", it.prayedAt).put("answeredAt", it.answeredAt))
        }
        prefs(context).edit().putString("kept", arr.toString()).apply()
    }

    /**
     * Remembers a prayer and returns it. Praying the same thing again does
     * not make a second memory - it moves the one already there forward, so
     * the question comes a week after the latest prayer, once.
     */
    fun remember(context: Context, request: String, title: String, now: Long = System.currentTimeMillis()): Kept {
        val list = all(context)
        val same = list.firstOrNull { !it.answered && it.request.trim().equals(request.trim(), ignoreCase = true) }
        val kept = same?.copy(prayedAt = now, title = title) ?: Kept(now, request.trim(), title, now)
        save(context, list.filterNot { it.id == kept.id } + kept)
        return kept
    }

    fun markAnswered(context: Context, id: Long, now: Long = System.currentTimeMillis()) {
        save(context, all(context).map { if (it.id == id) it.copy(answeredAt = now) else it })
    }

    /** Still praying: asked about again a week from now. */
    fun stillPraying(context: Context, id: Long, now: Long = System.currentTimeMillis()): Kept? {
        val kept = find(context, id)?.copy(prayedAt = now) ?: return null
        save(context, all(context).map { if (it.id == id) kept else it })
        return kept
    }

    /** The moment the machine asks about a prayer kept at [prayedAt]. */
    fun followUpAt(prayedAt: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
        val day: LocalDate = Instant.ofEpochMilli(prayedAt).atZone(zone).toLocalDate().plusDays(FOLLOW_UP_DAYS)
        return day.atTime(ASK_AT).atZone(zone).toInstant().toEpochMilli()
    }

    /** Prayers still waiting for an answer, to be asked about again after a restart. */
    fun waiting(context: Context): List<Kept> = all(context).filterNot { it.answered }
}
