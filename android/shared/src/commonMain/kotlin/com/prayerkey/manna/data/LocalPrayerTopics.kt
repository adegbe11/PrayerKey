package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.readAsset
import kotlin.concurrent.Volatile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

/**
 * The prayer deck, on the device.
 *
 * The deck used to be the only part of Manna that needed a network. With no
 * signal the fetch threw, nothing told the screen, and it span forever —
 * while the Bible, the journal and the sermon listener all carried on
 * working offline beside it.
 *
 * `assets/prayer-topics.json` is generated from the same source the website
 * serves, by `scripts/build-prayer-topics-asset.mjs`, so the two cannot
 * drift. Re-run that script when the topic list changes.
 */
object LocalPrayerTopics {

    /**
     * The 543 prayers, parsed once.
     *
     * There was no cache at all: six screens call load(), and each one read
     * and parsed the whole asset again. Home does it on every cold start
     * before Today can show a prayer.
     */
    @Volatile private var cached: List<PrayerTopic>? = null

    /* No lock: two screens racing on a cold start would both parse and both
       get the same list, which costs a moment once rather than a lock on
       every call after. */
    fun load(context: PlatformContext): List<PrayerTopic> =
        cached ?: read(context).also { if (it.isNotEmpty()) cached = it }

    private fun read(context: PlatformContext): List<PrayerTopic> = runCatching {
        val raw = context.readAsset("prayer-topics.json") ?: return emptyList()
        parse(Json.parseToJsonElement(raw).jsonObject.getArray("topics"))
    }.getOrDefault(emptyList())

    private fun parse(topics: JsonArray): List<PrayerTopic> = buildList {
        for (entry in topics) {
            val item = entry as? JsonObject ?: continue
            // one malformed entry must not cost the whole deck
            runCatching { add(topic(item)) }
        }
    }

    private fun topic(item: JsonObject): PrayerTopic {
        val scripture = item.optArray("scripture") ?: JsonArray(emptyList())
        val points = item.optArray("prayerPoints") ?: JsonArray(emptyList())
        return PrayerTopic(
            slug = item.getString("slug"),
            title = item.getString("title"),
            category = item.getString("category"),
            prayer = editorialPrayer(item.optString("prayer")),
            scripture = scripture.mapNotNull { (it as? JsonObject)?.let { s -> s.optString("ref") to s.optString("text") } },
            prayerPoints = points.map { (it as? JsonPrimitive)?.contentOrNull.orEmpty() },
        )
    }

    /** Removes outcome guarantees and fundraising-style clichés from legacy copy. */
    private fun editorialPrayer(source: String): String {
        val edits = linkedMapOf(
            "Cancel every debt and replace it with abundance." to "Give me discipline, honest counsel, and a workable path out of debt.",
            "Bring unexpected income and supernatural provision." to "Provide honest opportunities, sufficient income, and wisdom for every decision.",
            "Let people be drawn to help me without knowing why — because Your hand is on my life." to "Grant me favour with people whose help is honest, wise, and timely.",
            "I trust that they are with You." to "I entrust them to Your mercy.",
            "Regulate what doctors cannot fully control." to "Give my clinicians wisdom and help me manage this condition faithfully.",
            "supernatural intervention where medicine reaches its limit" to "effective treatment, wise care, and healing according to Your mercy",
            "I will rise from this stronger." to "Give me courage and patience throughout recovery.",
            "Let every setback become a setup for a greater testimony." to "Keep disappointment from ruling my heart when recovery is difficult.",
            "unexpected money, promotions, divine connections, and opportunities that only You can arrange" to "honest work, wise counsel, disciplined choices, and a sustainable way forward",
            "supernatural debt cancellation" to "freedom from debt",
            "supernatural, tangible relief" to "real and lasting relief",
            "By Your stripes I am healed." to "I place my hope in Your mercy and healing.",
            "I believe and receive it." to "I ask with faith and entrust the outcome to You.",
            "Let this baby develop perfectly — every organ, every system, every cell." to "Guard this baby through every stage of development — every organ, every system, every cell.",
            "Guard against any complication." to "Protect this pregnancy and guide every medical decision.",
        )
        return edits.entries.fold(source) { prayer, edit -> prayer.replace(edit.key, edit.value) }
    }
}
