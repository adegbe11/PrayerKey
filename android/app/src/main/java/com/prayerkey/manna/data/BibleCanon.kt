package com.prayerkey.manna.data

/**
 * The canon: 66 books in order, with how many chapters each one has.
 *
 * This table already existed, private, inside the sermon reference parser.
 * The book reader needs the same facts — what follows what, how far a book
 * runs, which testament you are in — and two copies of the canon is exactly
 * the kind of duplication that quietly drifts. One list, both callers.
 */
data class BibleBook(
    val name: String,
    val chapters: Int,
    val oldTestament: Boolean,
) {
    /** "Genesis" -> "GEN", "1 Corinthians" -> "1 COR" — for the page edge. */
    val shortName: String
        get() {
            val parts = name.split(' ')
            return if (parts.size > 1 && parts[0].firstOrNull()?.isDigit() == true) {
                parts[0] + " " + parts[1].take(3).uppercase()
            } else name.take(3).uppercase()
        }
}

object BibleCanon {

    private val OT = listOf(
        "Genesis" to 50, "Exodus" to 40, "Leviticus" to 27, "Numbers" to 36, "Deuteronomy" to 34,
        "Joshua" to 24, "Judges" to 21, "Ruth" to 4, "1 Samuel" to 31, "2 Samuel" to 24,
        "1 Kings" to 22, "2 Kings" to 25, "1 Chronicles" to 29, "2 Chronicles" to 36, "Ezra" to 10,
        "Nehemiah" to 13, "Esther" to 10, "Job" to 42, "Psalms" to 150, "Proverbs" to 31,
        "Ecclesiastes" to 12, "Song of Solomon" to 8, "Isaiah" to 66, "Jeremiah" to 52,
        "Lamentations" to 5, "Ezekiel" to 48, "Daniel" to 12, "Hosea" to 14, "Joel" to 3,
        "Amos" to 9, "Obadiah" to 1, "Jonah" to 4, "Micah" to 7, "Nahum" to 3, "Habakkuk" to 3,
        "Zephaniah" to 3, "Haggai" to 2, "Zechariah" to 14, "Malachi" to 4,
    )

    private val NT = listOf(
        "Matthew" to 28, "Mark" to 16, "Luke" to 24, "John" to 21, "Acts" to 28, "Romans" to 16,
        "1 Corinthians" to 16, "2 Corinthians" to 13, "Galatians" to 6, "Ephesians" to 6,
        "Philippians" to 4, "Colossians" to 4, "1 Thessalonians" to 5, "2 Thessalonians" to 3,
        "1 Timothy" to 6, "2 Timothy" to 4, "Titus" to 3, "Philemon" to 1, "Hebrews" to 13,
        "James" to 5, "1 Peter" to 5, "2 Peter" to 3, "1 John" to 5, "2 John" to 1,
        "3 John" to 1, "Jude" to 1, "Revelation" to 22,
    )

    val books: List<BibleBook> =
        OT.map { BibleBook(it.first, it.second, oldTestament = true) } +
            NT.map { BibleBook(it.first, it.second, oldTestament = false) }

    val chapterCounts: Map<String, Int> = books.associate { it.name to it.chapters }

    /** Every chapter in the Bible, in order — what the edge scrubs through. */
    val chapters: List<ChapterRef> = books.flatMap { book ->
        (1..book.chapters).map { ChapterRef(book, it) }
    }

    fun indexOf(book: String, chapter: Int): Int =
        chapters.indexOfFirst { it.book.name == book && it.chapter == chapter }

    fun chapters(book: String): Int = chapterCounts[book] ?: 1
}

data class ChapterRef(val book: BibleBook, val chapter: Int) {
    val label: String get() = "${book.name} $chapter"
}
