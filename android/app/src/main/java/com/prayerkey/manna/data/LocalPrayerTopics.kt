package com.prayerkey.manna.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

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

    fun load(context: Context): List<PrayerTopic> =
        cached ?: synchronized(this) { cached ?: read(context).also { cached = it } }

    private fun read(context: Context): List<PrayerTopic> = runCatching {
        val raw = context.assets.open("prayer-topics.json")
            .bufferedReader().use { it.readText() }
        parse(JSONObject(raw).getJSONArray("topics"))
    }.getOrDefault(emptyList())

    private fun parse(topics: JSONArray): List<PrayerTopic> = buildList {
        for (index in 0 until topics.length()) {
            val item = topics.optJSONObject(index) ?: continue
            // one malformed entry must not cost the whole deck
            runCatching { add(topic(item)) }
        }
    }

    private fun topic(item: JSONObject): PrayerTopic {
        val scripture = item.optJSONArray("scripture") ?: JSONArray()
        val points = item.optJSONArray("prayerPoints") ?: JSONArray()
        return PrayerTopic(
            slug = item.getString("slug"),
            title = item.getString("title"),
            category = item.getString("category"),
            prayer = editorialPrayer(item.optString("prayer")),
            scripture = buildList {
                for (i in 0 until scripture.length()) {
                    scripture.optJSONObject(i)?.let {
                        add(it.optString("ref") to it.optString("text"))
                    }
                }
            },
            prayerPoints = buildList {
                for (i in 0 until points.length()) add(points.optString(i))
            },
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
