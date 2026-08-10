package com.prayerkey.manna.data

import java.time.LocalDate

data class DailyPassage(
    val title: String,
    val book: String,
    val chapter: Int,
    val firstVerse: Int = 1,
    val lastVerse: Int = 9,
    val introduction: String,
) {
    val reference: String get() = "$book $chapter:$firstVerse–$lastVerse"
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
