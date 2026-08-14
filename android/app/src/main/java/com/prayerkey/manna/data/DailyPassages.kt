package com.prayerkey.manna.data

import java.time.LocalDate

data class DailyPassage(
    val title: String,
    val book: String,
    val chapter: Int,
    val firstVerse: Int = 1,
    /** How far the excerpt reaches *at most*. Short chapters simply end sooner. */
    val lastVerse: Int = 9,
    val introduction: String,
) {
    /**
     * The chapter, not a verse range.
     *
     * This used to read "$book $chapter:$firstVerse–$lastVerse", and because
     * [lastVerse] is a fixed 9 that nothing ever measured, **21 of the 365
     * days cited verses that do not exist** — "Psalms 23:1–9" over a psalm
     * with six verses, "Psalms 100:1–9" over one with five. The readers filter
     * to whatever the chapter actually has, so nothing crashed; the heading
     * just quietly lied, on the best-known psalm in the Bible.
     *
     * There is no verse-count table to clamp against — the canon carries
     * chapter counts only, and the verse text arrives from an asset long after
     * this string is built. So this stops claiming a precision it cannot have.
     * Naming the chapter is also what the reading instruction already says to
     * do, and [rangeOf] gives the exact range to anything holding real verses.
     */
    val reference: String get() = "$book $chapter"

    /** The precise range, for a caller that has actually loaded the verses. */
    fun rangeOf(verseNumbers: List<Int>): String =
        if (verseNumbers.isEmpty()) reference
        else "$book $chapter:${verseNumbers.min()}–${verseNumbers.max()}"
}

private fun passageTitle(book: BibleBook): String = when (book.name) {
    "Matthew", "Mark", "Luke", "John" -> "Walk With Jesus"
    "Acts" -> "The Church Begins"
    "Romans" -> "Grace That Changes Us"
    "1 Corinthians", "2 Corinthians" -> "Faith in Everyday Life"
    "Galatians", "Ephesians", "Philippians", "Colossians" -> "A New Way to Live"
    "Hebrews" -> "Hold On to Faith"
    "James" -> "Put Faith Into Practice"
    "Revelation" -> "Hope That Endures"
    "Psalms" -> "A Prayer for Today"
    else -> "Growing in Faith"
}

private fun introduction(book: BibleBook, chapter: Int): String =
    "Today’s reading is from ${book.name} $chapter. Read it slowly and notice the word, promise, warning, or invitation that holds your attention. You do not need to understand everything at once. Ask what this passage shows you about God, what it reveals about your life, and what one response you can carry into today."

/** 260 New Testament chapters + Psalms 1–105 = one unique passage per day. */
val DAILY_PASSAGES: List<DailyPassage> =
    (BibleCanon.books.filter { !it.oldTestament }.flatMap { book ->
        (1..book.chapters).map { chapter ->
            DailyPassage(passageTitle(book), book.name, chapter, introduction = introduction(book, chapter))
        }
    } + (1..105).map { chapter ->
        val psalms = BibleCanon.books.first { it.name == "Psalms" }
        DailyPassage(passageTitle(psalms), psalms.name, chapter, introduction = introduction(psalms, chapter))
    }).take(365)

fun passageFor(date: LocalDate): DailyPassage =
    DAILY_PASSAGES[Math.floorMod(date.toEpochDay().toInt(), DAILY_PASSAGES.size)]
