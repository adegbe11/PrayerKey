package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.platform.readAsset
import kotlin.concurrent.Volatile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class OfflineBible(private val context: PlatformContext) {

    suspend fun search(query: String, limit: Int = 30): List<BibleVerse> = withContext(ioDispatcher) {
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

    suspend fun chapter(book: String, chapter: Int): List<BibleVerse> = withContext(ioDispatcher) {
        verses().filter { it.book == book && it.chapter == chapter }
    }

    /**
     * Every verse in the bundled KJV, shuffled for the pull-card experience.
     *
     * Keep this separate from [search]: a blank search used to fall back to
     * the first few matches for "peace", which made Pull look like a whole
     * Bible deck while it was actually cycling through ten verses.
     */
    suspend fun pullDeck(): List<BibleVerse> = withContext(ioDispatcher) {
        /* A card has to be able to stand alone. The whole Bible shuffled put
           genealogies and the cleansing laws of Leviticus on the card that is
           meant to meet someone on their way to work. The deck is now the
           verses the app's own devotionals and challenges chose, and the
           chapters people turn to, with anything that needs its context to be
           read rightly left in the book. */
        val all = verses()
        val chosen = chosenReferences()
        val (mine, beloved) = all.filter { v ->
            v.reference in chosen ||
                (Beloved[v.book]?.contains(v.chapter) == true && v.text.length in 40..260 &&
                    v.text.trimEnd().last() in ".?!" &&
                    Blocked.none { v.text.contains(it, ignoreCase = true) })
        }.partition { it.reference in chosen }
        if (mine.isEmpty() && beloved.isEmpty()) return@withContext all.shuffled()
        // two hand-chosen verses for every one from the chapters around them
        val a = mine.shuffled().iterator()
        val b = beloved.shuffled().iterator()
        buildList {
            while (a.hasNext() || b.hasNext()) {
                repeat(2) { if (a.hasNext()) add(a.next()) }
                if (b.hasNext()) add(b.next())
            }
        }
    }

    /** The key verse of every devotional, challenge and season day. */
    private fun chosenReferences(): Set<String> = buildSet {
        fun walk(e: kotlinx.serialization.json.JsonElement) {
            when (e) {
                is kotlinx.serialization.json.JsonObject -> {
                    val book = (e["book"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                    val chapter = (e["chapter"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                    val verse = (e["verse"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                    if (book != null && chapter != null && verse != null) add("$book $chapter:$verse")
                    e.values.forEach(::walk)
                }
                is kotlinx.serialization.json.JsonArray -> e.forEach(::walk)
                else -> Unit
            }
        }
        listOf("devotionals.json", "challenges.json", "seasons.json").forEach { asset ->
            context.readAsset(asset)?.let { raw -> runCatching { walk(Json.parseToJsonElement(raw)) } }
        }
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
    private suspend fun verses(): List<BibleVerse> = cached ?: lock.withLock {
        cached ?: parse().also { if (it.isNotEmpty()) cached = it }
    }

    private fun parse(): List<BibleVerse> {
        // Reference-specific apparatus repairs, shared with the Supabase importer.
        val corrections = context.readAsset("kjv-corrections.json")
            ?.let { Json.parseToJsonElement(it).jsonObject }
        val text = context.readAsset("kjv.json") ?: return emptyList()
        return buildList(31_102) {
            KjvReader(text).books { name, chapters ->
                chapters.forEachIndexed { chapterIndex, chapter ->
                    chapter.forEachIndexed { verseIndex, raw ->
                        /* The asset carries the 1611 apparatus inline —
                           {was} for supplied words, {x: Heb. y} for
                           marginal notes — and it was all reaching the
                           screen with the braces showing. Resolve it once,
                           here, so every reader of this Bible benefits. */
                        val reference = "$name ${chapterIndex + 1}:${verseIndex + 1}"
                        val clean = cleanScripture(corrections?.optString(reference, raw) ?: raw)
                        add(
                            BibleVerse(
                                name, chapterIndex + 1, verseIndex + 1,
                                clean.text, clean.supplied,
                            ),
                        )
                    }
                }
            }
        }
    }

    companion object {
        @Volatile private var cached: List<BibleVerse>? = null
        private val lock = Mutex()

        /** The chapters people turn to, by book. */
        private val Beloved: Map<String, Set<Int>> = mapOf(
            "Psalms" to setOf(1, 4, 8, 16, 19, 23, 27, 31, 32, 34, 37, 40, 42, 46, 51, 55, 57, 62, 63, 84, 86, 90, 91, 96, 100, 103, 116, 118, 121, 130, 138, 139, 145, 146, 147),
            "Proverbs" to setOf(3, 4, 16),
            "Isaiah" to setOf(26, 40, 41, 43, 53, 55, 61),
            "Lamentations" to setOf(3),
            "Jeremiah" to setOf(29, 31),
            "Zephaniah" to setOf(3),
            "Matthew" to setOf(5, 6, 7, 11, 28),
            "John" to setOf(1, 3, 6, 10, 11, 14, 15, 16),
            "Romans" to setOf(5, 8, 12, 15),
            "1 Corinthians" to setOf(13),
            "2 Corinthians" to setOf(4, 5, 12),
            "Galatians" to setOf(2, 5),
            "Ephesians" to setOf(1, 2, 3, 6),
            "Philippians" to setOf(1, 2, 3, 4),
            "Colossians" to setOf(1, 3),
            "1 Thessalonians" to setOf(5),
            "2 Timothy" to setOf(1),
            "Hebrews" to setOf(4, 10, 12, 13),
            "James" to setOf(1),
            "1 Peter" to setOf(1, 2, 5),
            "1 John" to setOf(1, 3, 4),
            "Revelation" to setOf(3),
        )

        /** Words that mean a verse needs its chapter around it. */
        private val Blocked = listOf(
            "piss", "dung", "harlot", "whore", "concubine", "foreskin", "uncircumcised",
            "slew", "smite", "smote", "slain", "dash", "begat", "hate", "curse", "vengeance",
            "enemies be", "destroy them", "break their teeth", "wicked shall be turned",
        )
    }
}

/**
 * Reads kjv.json - an array of books, each `{"name", "chapters": [[verse, ...], ...]}` -
 * in one pass over the text.
 *
 * A general JSON parser would first build a tree of the whole Bible, 31,102
 * strings and a node for each, only for it to be thrown away the moment the
 * verses are copied out. This walks the text once and hands back each book's
 * chapters as it reaches them, which is what Android's streaming reader did
 * and the reason the Bible opens as quickly as it does.
 */
internal class KjvReader(private val s: String) {
    private var i = 0

    fun books(each: (name: String, chapters: List<List<String>>) -> Unit) {
        expect('[')
        while (more(']')) {
            expect('{')
            var name = ""
            val chapters = mutableListOf<List<String>>()
            while (more('}')) {
                val key = string()
                expect(':')
                when (key) {
                    "name" -> name = string()
                    "chapters" -> {
                        expect('[')
                        while (more(']')) {
                            expect('[')
                            val verses = ArrayList<String>(40)
                            while (more(']')) verses += string()
                            chapters += verses
                        }
                    }
                    else -> skip()
                }
            }
            each(name, chapters)
        }
    }

    private fun space() {
        while (i < s.length && (s[i] == ' ' || s[i] == '\n' || s[i] == '\r' || s[i] == '\t')) i++
    }

    private fun expect(c: Char) {
        space()
        check(i < s.length && s[i] == c) { "kjv.json: expected '$c' at $i" }
        i++
    }

    /** Inside an array or object: true while another element follows. Consumes the commas and the close. */
    private fun more(close: Char): Boolean {
        space()
        if (i < s.length && s[i] == ',') { i++; space() }
        check(i < s.length) { "kjv.json: ended early" }
        if (s[i] == close) { i++; return false }
        return true
    }

    private fun string(): String {
        expect('"')
        val start = i
        // almost every verse has no escapes: one substring, no copying
        while (i < s.length) {
            val c = s[i]
            if (c == '"') return s.substring(start, i++)
            if (c == '\\') break
            i++
        }
        val out = StringBuilder().appendRange(s, start, i)
        while (i < s.length) {
            when (val c = s[i++]) {
                '"' -> return out.toString()
                '\\' -> when (val e = s[i++]) {
                    'n' -> out.append('\n')
                    't' -> out.append('\t')
                    'r' -> out.append('\r')
                    'b' -> out.append('\b')
                    'f' -> out.append('\u000C')
                    'u' -> { out.append(s.substring(i, i + 4).toInt(16).toChar()); i += 4 }
                    else -> out.append(e)
                }
                else -> out.append(c)
            }
        }
        error("kjv.json: unterminated string")
    }

    private fun skip() {
        space()
        when (s[i]) {
            '"' -> string()
            '[' -> { i++; while (more(']')) skip() }
            '{' -> { i++; while (more('}')) { string(); expect(':'); skip() } }
            else -> while (i < s.length && s[i] !in ",]} \n\r\t") i++
        }
    }
}
