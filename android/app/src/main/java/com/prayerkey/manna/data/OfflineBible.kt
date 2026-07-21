package com.prayerkey.manna.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import android.util.JsonReader

data class BibleVerse(val book: String, val chapter: Int, val verse: Int, val text: String) {
    val reference: String get() = "$book $chapter:$verse"
}

class OfflineBible(private val context: Context) {
    @Volatile private var cached: List<BibleVerse>? = null

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

    private fun verses(): List<BibleVerse> = cached ?: synchronized(this) {
        cached ?: parse().also { cached = it }
    }

    private fun parse(): List<BibleVerse> {
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
                        chapter.forEachIndexed { verseIndex, text ->
                            add(BibleVerse(name, chapterIndex + 1, verseIndex + 1, text))
                        }
                    }
                }
                reader.endArray()
            }
        }
    }
}
