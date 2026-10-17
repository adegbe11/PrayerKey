package com.prayerkey.manna.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import android.util.JsonReader

class OfflineBible(private val context: Context) {

    suspend fun search(query: String, limit: Int = 30): List<BibleVerse> = withContext(Dispatchers.IO) {
        val normalized = query.trim()
        if (normalized.isBlank()) return@withContext verses().take(30)
        val direct = Regex("^(.+?)\\s+(\\d+)(?::(\\d+))?$", RegexOption.IGNORE_CASE).matchEntire(normalized)
        if (direct != null) {
            val book = direct.groupValues[1]
            val chapter = direct.groupValues[2].toInt()
            val verse = direct.groupValues[3].toIntOrNull()
            return@withContext verses().filter {
                it.book.equals(book, true) && it.chapter == chapter && (verse == null || it.verse == verse)
            }.take(limit)
        }
        val terms = normalized.lowercase().split(Regex("\\s+")).filter { it.length > 1 }
        verses().asSequence().filter { candidate ->
            val haystack = "${candidate.reference} ${candidate.text}".lowercase()
            terms.all(haystack::contains)
        }.take(limit).toList()
    }

    suspend fun chapter(book: String, chapter: Int): List<BibleVerse> = withContext(Dispatchers.IO) {
        verses().filter { it.book == book && it.chapter == chapter }
    }

    /**
     * Every verse in the bundled KJV, shuffled for the pull-card experience.
     *
     * Keep this separate from [search]: a blank search used to fall back to
     * the first few matches for "peace", which made Pull look like a whole
     * Bible deck while it was actually cycling through ten verses.
     */
    suspend fun pullDeck(): List<BibleVerse> = withContext(Dispatchers.IO) {
        verses().shuffled()
    }

    suspend fun related(source: BibleVerse, limit: Int = 5): List<BibleVerse> = withContext(Dispatchers.Default) {
        val stop = setOf("that", "this", "with", "from", "have", "will", "your", "shall", "unto", "they", "them", "were", "when", "what", "there", "their", "which", "into", "upon")
        val themes = Regex("[A-Za-z]{4,}").findAll(source.text.lowercase()).map { it.value }.filterNot(stop::contains).toSet()
        verses().asSequence()
            .filter { it.reference != source.reference }
            .map { candidate ->
                val words = Regex("[A-Za-z]{4,}").findAll(candidate.text.lowercase()).map { it.value }.toSet()
                candidate to themes.count(words::contains)
            }
            .filter { it.second >= 2 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
            .toList()
    }

    /**
     * Every verse, parsed once for the life of the process.
     *
     * The cache used to be a field on the instance, and every screen builds its
     * own OfflineBible - the reader, the challenge day, the devotional, the
     * audio narrator, Today. So the whole King James Version, 4.5 MB and 31,102
     * verses, was read off disk and parsed again on each of them. It is the
     * single largest reason the app felt slow between screens.
     *
     * On the companion it is parsed on the first screen that needs it and free
     * on every screen after.
     */
    private fun verses(): List<BibleVerse> = cached ?: synchronized(OfflineBible) {
        cached ?: parse().also { cached = it }
    }

    private fun parse(): List<BibleVerse> {
        // Reference-specific apparatus repairs, shared with the Supabase importer.
        val corrections = context.assets.open("kjv-corrections.json").bufferedReader().use { org.json.JSONObject(it.readText()) }
        return context.assets.open("kjv.json").bufferedReader().use { source ->
            val reader = JsonReader(source)
            buildList(31_102) {
                reader.beginArray()
                while (reader.hasNext()) {
                    var name = ""
                    val chapters = mutableListOf<List<String>>()
                    reader.beginObject()
                    while (reader.hasNext()) when (reader.nextName()) {
                        "name" -> name = reader.nextString()
                        "chapters" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                val verses = mutableListOf<String>()
                                reader.beginArray()
                                while (reader.hasNext()) verses += reader.nextString()
                                reader.endArray(); chapters += verses
                            }
                            reader.endArray()
                        }
                        else -> reader.skipValue()
                    }
                    reader.endObject()
                    chapters.forEachIndexed { chapterIndex, chapter ->
                        chapter.forEachIndexed { verseIndex, raw ->
                            /* The asset carries the 1611 apparatus inline —
                               {was} for supplied words, {x: Heb. y} for
                               marginal notes — and it was all reaching the
                               screen with the braces showing. Resolve it once,
                               here, so every reader of this Bible benefits. */
                            val reference = "$name ${chapterIndex + 1}:${verseIndex + 1}"
                            val clean = cleanScripture(corrections.optString(reference, raw))
                            add(
                                BibleVerse(
                                    name, chapterIndex + 1, verseIndex + 1,
                                    clean.text, clean.supplied,
                                ),
                            )
                        }
                    }
                }
                reader.endArray()
            }
        }
    }

    companion object {
        @Volatile private var cached: List<BibleVerse>? = null
    }

}
