package com.prayerkey.manna.ui.church

/**
 * Finds scripture references inside spoken text, entirely on device.
 *
 * A preacher never says "John 3:16" — he says "John chapter three verse
 * sixteen", or just "John three sixteen". Speech recognisers hand back
 * words, not digits, so this parses both.
 *
 * No network, no API, no cost. Works in a basement with no signal.
 */
object ReferenceDetector {

    /** Canonical book name -> every spoken spelling that should match it. */
    private val BOOKS: List<Pair<String, List<String>>> = listOf(
        "Genesis" to listOf("genesis"),
        "Exodus" to listOf("exodus"),
        "Leviticus" to listOf("leviticus"),
        "Numbers" to listOf("numbers"),
        "Deuteronomy" to listOf("deuteronomy"),
        "Joshua" to listOf("joshua"),
        "Judges" to listOf("judges"),
        "Ruth" to listOf("ruth"),
        "1 Samuel" to listOf("first samuel", "1 samuel", "one samuel", "first book of samuel"),
        "2 Samuel" to listOf("second samuel", "2 samuel", "two samuel", "second book of samuel"),
        "1 Kings" to listOf("first kings", "1 kings", "one kings", "first book of kings"),
        "2 Kings" to listOf("second kings", "2 kings", "two kings", "second book of kings"),
        "1 Chronicles" to listOf("first chronicles", "1 chronicles", "one chronicles"),
        "2 Chronicles" to listOf("second chronicles", "2 chronicles", "two chronicles"),
        "Ezra" to listOf("ezra"),
        "Nehemiah" to listOf("nehemiah"),
        "Esther" to listOf("esther"),
        "Job" to listOf("job"),
        "Psalms" to listOf("psalms", "psalm", "the psalms"),
        "Proverbs" to listOf("proverbs", "proverb"),
        "Ecclesiastes" to listOf("ecclesiastes"),
        "Song of Solomon" to listOf("song of solomon", "song of songs"),
        "Isaiah" to listOf("isaiah"),
        "Jeremiah" to listOf("jeremiah"),
        "Lamentations" to listOf("lamentations"),
        "Ezekiel" to listOf("ezekiel"),
        "Daniel" to listOf("daniel"),
        "Hosea" to listOf("hosea"),
        "Joel" to listOf("joel"),
        "Amos" to listOf("amos"),
        "Obadiah" to listOf("obadiah"),
        "Jonah" to listOf("jonah"),
        "Micah" to listOf("micah"),
        "Nahum" to listOf("nahum"),
        "Habakkuk" to listOf("habakkuk"),
        "Zephaniah" to listOf("zephaniah"),
        "Haggai" to listOf("haggai"),
        "Zechariah" to listOf("zechariah"),
        "Malachi" to listOf("malachi"),
        "Matthew" to listOf("matthew", "saint matthew"),
        "Mark" to listOf("mark", "saint mark"),
        "Luke" to listOf("luke", "saint luke"),
        "John" to listOf("john", "saint john", "gospel of john"),
        "Acts" to listOf("acts", "the acts", "acts of the apostles"),
        "Romans" to listOf("romans"),
        "1 Corinthians" to listOf("first corinthians", "1 corinthians", "one corinthians"),
        "2 Corinthians" to listOf("second corinthians", "2 corinthians", "two corinthians"),
        "Galatians" to listOf("galatians"),
        "Ephesians" to listOf("ephesians"),
        "Philippians" to listOf("philippians"),
        "Colossians" to listOf("colossians"),
        "1 Thessalonians" to listOf("first thessalonians", "1 thessalonians", "one thessalonians"),
        "2 Thessalonians" to listOf("second thessalonians", "2 thessalonians", "two thessalonians"),
        "1 Timothy" to listOf("first timothy", "1 timothy", "one timothy"),
        "2 Timothy" to listOf("second timothy", "2 timothy", "two timothy"),
        "Titus" to listOf("titus"),
        "Philemon" to listOf("philemon"),
        "Hebrews" to listOf("hebrews"),
        "James" to listOf("james"),
        "1 Peter" to listOf("first peter", "1 peter", "one peter"),
        "2 Peter" to listOf("second peter", "2 peter", "two peter"),
        "1 John" to listOf("first john", "1 john", "one john"),
        "2 John" to listOf("second john", "2 john", "two john"),
        "3 John" to listOf("third john", "3 john", "three john"),
        "Jude" to listOf("jude"),
        "Revelation" to listOf("revelation", "revelations", "the revelation"),
    )

    /** Chapter counts, so "John twenty five" is rejected as a chapter. */
    private val CHAPTERS: Map<String, Int> = mapOf(
        "Genesis" to 50, "Exodus" to 40, "Leviticus" to 27, "Numbers" to 36, "Deuteronomy" to 34,
        "Joshua" to 24, "Judges" to 21, "Ruth" to 4, "1 Samuel" to 31, "2 Samuel" to 24,
        "1 Kings" to 22, "2 Kings" to 25, "1 Chronicles" to 29, "2 Chronicles" to 36, "Ezra" to 10,
        "Nehemiah" to 13, "Esther" to 10, "Job" to 42, "Psalms" to 150, "Proverbs" to 31,
        "Ecclesiastes" to 12, "Song of Solomon" to 8, "Isaiah" to 66, "Jeremiah" to 52,
        "Lamentations" to 5, "Ezekiel" to 48, "Daniel" to 12, "Hosea" to 14, "Joel" to 3,
        "Amos" to 9, "Obadiah" to 1, "Jonah" to 4, "Micah" to 7, "Nahum" to 3, "Habakkuk" to 3,
        "Zephaniah" to 3, "Haggai" to 2, "Zechariah" to 14, "Malachi" to 4, "Matthew" to 28,
        "Mark" to 16, "Luke" to 24, "John" to 21, "Acts" to 28, "Romans" to 16,
        "1 Corinthians" to 16, "2 Corinthians" to 13, "Galatians" to 6, "Ephesians" to 6,
        "Philippians" to 4, "Colossians" to 4, "1 Thessalonians" to 5, "2 Thessalonians" to 3,
        "1 Timothy" to 6, "2 Timothy" to 4, "Titus" to 3, "Philemon" to 1, "Hebrews" to 13,
        "James" to 5, "1 Peter" to 5, "2 Peter" to 3, "1 John" to 5, "2 John" to 1,
        "3 John" to 1, "Jude" to 1, "Revelation" to 22,
    )

    private val UNITS = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
        "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
        "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14, "fifteen" to 15,
        "sixteen" to 16, "seventeen" to 17, "eighteen" to 18, "nineteen" to 19,
        // ordinals, because "verse the third" happens
        "first" to 1, "second" to 2, "third" to 3, "fourth" to 4, "fifth" to 5, "sixth" to 6,
        "seventh" to 7, "eighth" to 8, "ninth" to 9, "tenth" to 10, "eleventh" to 11,
        "twelfth" to 12, "thirteenth" to 13, "fourteenth" to 14, "fifteenth" to 15,
        "sixteenth" to 16, "seventeenth" to 17, "eighteenth" to 18, "nineteenth" to 19,
    )
    private val TENS = mapOf(
        "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50, "sixty" to 60,
        "seventy" to 70, "eighty" to 80, "ninety" to 90, "hundred" to 100,
        "twentieth" to 20, "thirtieth" to 30, "fortieth" to 40, "fiftieth" to 50,
        "sixtieth" to 60, "seventieth" to 70, "eightieth" to 80, "ninetieth" to 90,
    )

    data class Hit(val reference: String, val book: String, val chapter: Int, val verse: Int?)

    /**
     * Reads a spoken number starting at [i]. Handles "seventeen",
     * "twenty eight", "one hundred nineteen", and bare digits.
     * Returns the value and the index just past it, or null.
     */
    private fun readNumber(w: List<String>, i: Int): Pair<Int, Int>? {
        if (i >= w.size) return null
        w[i].toIntOrNull()?.let { if (it in 1..176) return it to i + 1 }

        var idx = i
        var total = 0
        var consumed = false

        // optional "one hundred" / "a hundred" prefix
        if (idx + 1 < w.size && (w[idx] == "one" || w[idx] == "a") && w[idx + 1] == "hundred") {
            total = 100; idx += 2; consumed = true
            if (idx < w.size && w[idx] == "and") idx++
        }

        TENS[w[idx]]?.let { tens ->
            if (tens != 100) {
                total += tens; idx++; consumed = true
                UNITS[w.getOrNull(idx)]?.let { u -> if (u in 1..9) { total += u; idx++ } }
                return if (total in 1..176) total to idx else null
            }
        }
        UNITS[w[idx]]?.let { u ->
            total += u; idx++; consumed = true
            return if (total in 1..176) total to idx else null
        }
        return if (consumed && total in 1..176) total to idx else null
    }

    /** Words that mean "the next number is a chapter/verse". */
    private val CHAPTER_CUES = setOf("chapter", "chapters")
    private val VERSE_CUES = setOf("verse", "verses", "vs")

    /**
     * Scans [text] and returns every scripture reference found, in order.
     * Digit form ("1 Kings 17:2") and spoken form both land here.
     */
    fun scan(text: String): List<Hit> {
        val cleaned = text.lowercase()
            .replace(":", " colon ")
            .replace("-", " ")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (cleaned.isEmpty()) return emptyList()
        val w = cleaned.split(" ")
        val hits = mutableListOf<Hit>()
        var i = 0

        while (i < w.size) {
            val match = matchBook(w, i)
            if (match == null) { i++; continue }
            val (book, afterBook) = match
            var idx = afterBook

            if (w.getOrNull(idx) in CHAPTER_CUES) idx++
            val chapterRead = readNumber(w, idx)
            if (chapterRead == null) { i = afterBook; continue }
            val (chapter, afterChapter) = chapterRead
            // a chapter beyond the book's length means we misheard
            if (chapter > (CHAPTERS[book] ?: 150)) { i = afterBook; continue }
            idx = afterChapter

            var verse: Int? = null
            val cue = w.getOrNull(idx)
            if (cue in VERSE_CUES || cue == "colon") {
                idx++
                if (w.getOrNull(idx) == "the") idx++
                readNumber(w, idx)?.let { (v, after) -> verse = v; idx = after }
            } else {
                // "John three sixteen" — bare verse straight after the chapter
                readNumber(w, idx)?.let { (v, after) -> verse = v; idx = after }
            }

            val ref = if (verse != null) "$book $chapter:$verse" else "$book $chapter"
            hits += Hit(ref, book, chapter, verse)
            i = idx
        }
        return hits
    }

    /** Longest-first book match so "first john" beats "john". */
    private fun matchBook(w: List<String>, i: Int): Pair<String, Int>? {
        var best: Pair<String, Int>? = null
        for ((canonical, spellings) in BOOKS) {
            for (spelling in spellings) {
                val parts = spelling.split(" ")
                if (i + parts.size > w.size) continue
                var ok = true
                for (k in parts.indices) if (w[i + k] != parts[k]) { ok = false; break }
                if (ok && (best == null || parts.size > best!!.second - i)) {
                    best = canonical to i + parts.size
                }
            }
        }
        return best
    }
}
