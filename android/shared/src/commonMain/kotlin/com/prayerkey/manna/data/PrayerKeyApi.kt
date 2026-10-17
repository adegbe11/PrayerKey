package com.prayerkey.manna.data

import com.prayerkey.manna.platform.encodeUrlComponent
import com.prayerkey.manna.platform.httpRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

data class DetectedVerse(val detected: Boolean, val reference: String, val text: String, val confidence: Double)

object PrayerKeyApi {
    // www is canonical — the bare domain 301s, and Android's HttpURLConnection
    // drops POST bodies on cross-host redirects (broke prayer/generate).
    private const val BASE_URL = "https://www.prayerkey.com"

    suspend fun createDonationCheckout(amount: Int, monthly: Boolean): String {
        val body = buildJsonObject {
            put("amount", amount)
            put("frequency", if (monthly) "monthly" else "once")
        }
        return request("/api/give/checkout", "POST", body).getString("url")
    }

    suspend fun generatePrayer(request: String, moods: List<String>): GeneratedPrayer {
        val body = buildJsonObject {
            put("userInput", request)
            putJsonArray("moods") { moods.forEach { add(JsonPrimitive(it)) } }
        }
        val json = request("/api/prayer/generate", "POST", body)
        val verses = json.optArray("verses")?.mapNotNull { it as? JsonObject }.orEmpty()
        return GeneratedPrayer(
            id = json.getString("id"), title = json.getString("title"), prayer = json.getString("prayer"),
            encouragement = json.optString("encouragement"),
            verses = verses.map { it.optString("ref") to it.optString("text") },
        )
    }

    /**
     * The prayer experience must never become a dead button because a phone is
     * offline or the API is briefly unavailable. Prefer the server's richer
     * response, then create a complete Scripture-shaped prayer locally.
     */
    suspend fun generatePrayerReliable(userRequest: String, moods: List<String>): GeneratedPrayer =
        runCatching { generatePrayer(userRequest, moods) }.getOrElse {
            localPrayer(userRequest, moods)
        }

    private fun localPrayer(userRequest: String, moods: List<String>): GeneratedPrayer {
        val need = userRequest.trim().ifBlank { "the need I carry today" }
        val lower = need.lowercase()
        val (reference, verse) = when {
            listOf("fear", "afraid", "anxious", "worry", "worried").any(lower::contains) ->
                "Philippians 4:6–7" to "In everything, through prayer and petition with thanksgiving, present your requests to God. And the peace of God will guard your hearts and minds in Christ Jesus."
            listOf("wisdom", "decision", "direction", "confused").any(lower::contains) ->
                "James 1:5" to "If any of you lacks wisdom, he should ask God—who gives to all generously and ungrudgingly—and it will be given to him."
            listOf("heal", "health", "sick", "pain").any(lower::contains) ->
                "Psalm 103:2–3" to "My soul, bless the Lord, and do not forget all his benefits. He forgives all your iniquity; he heals all your diseases."
            listOf("family", "marriage", "child", "children").any(lower::contains) ->
                "Joshua 24:15" to "As for me and my family, we will worship the Lord."
            listOf("work", "job", "money", "provision", "debt").any(lower::contains) ->
                "Philippians 4:19" to "My God will supply all your needs according to his riches in glory in Christ Jesus."
            else -> "Psalm 37:5" to "Commit your way to the Lord; trust in him, and he will act."
        }
        val feeling = moods.firstOrNull()?.lowercase()?.let { " You see that I feel $it, and I do not have to hide it from You." }.orEmpty()
        return GeneratedPrayer(
            id = "local-${nowMillis()}",
            title = when {
                "family" in lower -> "Peace for My Family"
                "wisdom" in lower || "decision" in lower -> "Wisdom for the Next Step"
                "heal" in lower || "health" in lower -> "Grace and Healing"
                else -> "I Place This in Your Hands"
            },
            prayer = "Father, I come to You honestly and without pretending.$feeling\n\nYou know this concern completely: $need\n\nLet Your Word steady my heart. Give me wisdom for what I must do, courage for what I must face, and peace about what I must leave in Your hands. Close every path that would lead me away from Your will, and make the next faithful step clear.\n\nGuard my heart from fear and impatience. Help me notice Your grace today and respond with obedience. I trust Your character even while I wait for the answer.\n\nIn the name of Jesus, amen.",
            // The prayer is the thing. Telling somebody how to pray it was
            // the app talking over the prayer it had just written.
            encouragement = "",
            verses = listOf(reference to verse),
        )
    }

    suspend fun prayerTopics(): List<PrayerTopic> {
        val topics = request("/api/prayer/topics", "GET").getArray("topics")
        return topics.map { element ->
            val item = element.jsonObject
            val scripture = item.optArray("scripture")?.mapNotNull { it as? JsonObject }.orEmpty()
            val points = item.optArray("prayerPoints").orEmpty()
            PrayerTopic(
                slug = item.getString("slug"), title = item.getString("title"),
                category = item.getString("category"), prayer = item.getString("prayer"),
                scripture = scripture.map { it.optString("ref") to it.optString("text") },
                prayerPoints = points.map { (it as JsonPrimitive).content },
            )
        }
    }

    suspend fun searchBible(query: String, translation: String): List<RemoteVerse> {
        val q = encodeUrlComponent(query)
        val json = request("/api/bible/search?q=$q&translation=$translation", "GET")
        val results = json.optArray("results")?.mapNotNull { it as? JsonObject }.orEmpty()
        return results.map { RemoteVerse(it.optString("ref"), it.optString("text"), it.optString("translation", translation)) }
    }

    /**
     * Public-domain translations served straight from bible-api.com —
     * no key, no cost, always available. Used for WEB/ASV/BBE/DARBY/YLT.
     * Reference lookups only; keyword search happens on the offline KJV
     * first, then the found references are re-fetched in this version.
     */
    suspend fun freeVerse(reference: String, apiId: String): RemoteVerse? = runCatching {
        val encoded = encodeUrlComponent(reference)
        val result = httpRequest("https://bible-api.com/$encoded?translation=$apiId", "GET", null, mapOf("Accept" to "application/json"))
        check(result.code in 200..299)
        val json = Json.parseToJsonElement(result.text).jsonObject
        val verseText = json.optString("text").trim().replace(Regex("\\s+"), " ")
        if (verseText.isBlank()) null
        else RemoteVerse(json.optString("reference", reference), verseText, json.optString("translation_id", apiId).uppercase())
    }.getOrNull()

    private suspend fun request(path: String, method: String, body: JsonObject? = null): JsonObject {
        val result = httpRequest(
            BASE_URL + path, method, body?.toString(),
            mapOf("Accept" to "application/json", "Content-Type" to "application/json"),
        )
        if (result.code !in 200..299) {
            val error = runCatching { Json.parseToJsonElement(result.text.ifBlank { "{}" }).jsonObject.optString("error", "") }.getOrDefault("")
            throw IllegalStateException(error.ifBlank { "PrayerKey request failed" })
        }
        return Json.parseToJsonElement(result.text).jsonObject
    }
}
