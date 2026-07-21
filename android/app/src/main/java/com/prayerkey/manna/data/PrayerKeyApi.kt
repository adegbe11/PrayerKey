package com.prayerkey.manna.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GeneratedPrayer(
    val id: String,
    val title: String,
    val prayer: String,
    val encouragement: String,
    val verses: List<Pair<String, String>>,
)

data class DetectedVerse(val detected: Boolean, val reference: String, val text: String, val confidence: Double)

data class PrayerTopic(
    val slug: String,
    val title: String,
    val category: String,
    val prayer: String,
    val scripture: List<Pair<String, String>>,
    val prayerPoints: List<String>,
)
data class RemoteVerse(val reference: String, val text: String, val translation: String)

object PrayerKeyApi {
    private const val BASE_URL = "https://prayerkey.com"

    suspend fun generatePrayer(request: String, moods: List<String>): GeneratedPrayer = withContext(Dispatchers.IO) {
        val body = JSONObject().put("userInput", request).put("moods", JSONArray(moods))
        val json = request("/api/prayer/generate", "POST", body)
        val verses = json.optJSONArray("verses") ?: JSONArray()
        GeneratedPrayer(
            id = json.getString("id"), title = json.getString("title"), prayer = json.getString("prayer"),
            encouragement = json.optString("encouragement"),
            verses = buildList {
                repeat(verses.length()) { index ->
                    val verse = verses.getJSONObject(index)
                    add(verse.optString("ref") to verse.optString("text"))
                }
            },
        )
    }

    suspend fun detectVerse(transcript: String): DetectedVerse = withContext(Dispatchers.IO) {
        val json = request("/api/detect-verses", "POST", JSONObject().put("transcript", transcript))
        DetectedVerse(json.optBoolean("detected"), json.optString("ref"), json.optString("text"), json.optDouble("confidence"))
    }

    suspend fun prayerTopics(): List<PrayerTopic> = withContext(Dispatchers.IO) {
        val topics = request("/api/prayer/topics", "GET").getJSONArray("topics")
        buildList {
            repeat(topics.length()) { index ->
                val item = topics.getJSONObject(index)
                val scripture = item.optJSONArray("scripture") ?: JSONArray()
                val points = item.optJSONArray("prayerPoints") ?: JSONArray()
                add(PrayerTopic(
                    slug = item.getString("slug"), title = item.getString("title"),
                    category = item.getString("category"), prayer = item.getString("prayer"),
                    scripture = buildList { repeat(scripture.length()) { i -> scripture.getJSONObject(i).let { add(it.optString("ref") to it.optString("text")) } } },
                    prayerPoints = buildList { repeat(points.length()) { i -> add(points.getString(i)) } },
                ))
            }
        }
    }

    suspend fun searchBible(query: String, translation: String): List<RemoteVerse> = withContext(Dispatchers.IO) {
        val q = java.net.URLEncoder.encode(query, Charsets.UTF_8.name())
        val json = request("/api/bible/search?q=$q&translation=$translation", "GET")
        val results = json.optJSONArray("results") ?: JSONArray()
        buildList { repeat(results.length()) { index ->
            val item = results.getJSONObject(index)
            add(RemoteVerse(item.optString("ref"), item.optString("text"), item.optString("translation", translation)))
        } }
    }

    /**
     * Public-domain translations served straight from bible-api.com —
     * no key, no cost, always available. Used for WEB/ASV/BBE/DARBY/YLT.
     * Reference lookups only; keyword search happens on the offline KJV
     * first, then the found references are re-fetched in this version.
     */
    suspend fun freeVerse(reference: String, apiId: String): RemoteVerse? = withContext(Dispatchers.IO) {
        runCatching {
            val encoded = URLEncoder.encode(reference, Charsets.UTF_8.name()).replace("+", "%20")
            val connection = (URL("https://bible-api.com/$encoded?translation=$apiId").openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000; readTimeout = 20_000
                setRequestProperty("Accept", "application/json")
            }
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val verseText = json.optString("text").trim().replace(Regex("\\s+"), " ")
            if (verseText.isBlank()) null
            else RemoteVerse(json.optString("reference", reference), verseText, json.optString("translation_id", apiId).uppercase())
        }.getOrNull()
    }

    private fun request(path: String, method: String, body: JSONObject? = null): JSONObject {
        val connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method; connectTimeout = 15_000; readTimeout = 30_000
            setRequestProperty("Accept", "application/json"); setRequestProperty("Content-Type", "application/json")
            if (body != null) { doOutput = true; outputStream.bufferedWriter().use { it.write(body.toString()) } }
        }
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (connection.responseCode !in 200..299) throw IllegalStateException(JSONObject(text.ifBlank { "{}" }).optString("error", "PrayerKey request failed"))
        return JSONObject(text)
    }
}
