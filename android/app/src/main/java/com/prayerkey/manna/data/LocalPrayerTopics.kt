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

    fun load(context: Context): List<PrayerTopic> = runCatching {
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
            prayer = item.optString("prayer"),
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
}
