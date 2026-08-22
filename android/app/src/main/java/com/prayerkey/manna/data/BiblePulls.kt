package com.prayerkey.manna.data

/**
 * The day's chapter, in pulls.
 *
 * Reading is not swiping, and this is the place that distinction gets lost.
 * Cutting a chapter every four verses to reach a round ten is the obvious
 * implementation and the wrong one: a pull that ends mid-sentence tells the
 * reader a machine did the cutting, and they stop trusting the next one.
 *
 * So ten is a target, not a rule. Pulls are grown to roughly equal length and
 * then extended to the next verse that actually ends a sentence, which means
 * some days are nine and a two-verse psalm is two. The number bends; the
 * sentence does not.
 *
 * Verse numbers are kept on each pull so the reference stays exact — "Romans
 * 8:1–7" and not "Romans 8", because a reader who wants to go back to a line
 * needs to be able to find it.
 */
data class BiblePull(
    /** 1-based, because it is shown to the reader. */
    val index: Int,
    val text: String,
    /** "Romans 8:1–7", or "Romans 8:9" for a single verse. */
    val reference: String,
    val firstVerse: Int,
    val lastVerse: Int,
)

data class BibleReading(
    val book: String,
    val chapter: Int,
    val pulls: List<BiblePull>,
) {
    val title: String get() = "$book $chapter"
    val size: Int get() = pulls.size
}

/** The target. Chapters shorter than this simply give fewer pulls. */
const val BIBLE_PULL_TARGET = 10

/**
 * Cuts a chapter into pulls.
 *
 * [verses] must be in order — they come straight from [OfflineBible.chapter],
 * which reads them in order from the asset.
 */
fun readingFrom(book: String, chapter: Int, verses: List<BibleVerse>): BibleReading {
    if (verses.isEmpty()) return BibleReading(book, chapter, emptyList())

    val target = maxOf(1, verses.size / BIBLE_PULL_TARGET)
    val pulls = mutableListOf<BiblePull>()
    var group = mutableListOf<BibleVerse>()

    verses.forEachIndexed { i, verse ->
        group += verse
        /* Close the pull only where the text closes a thought. `remaining`
           stops the last pull swallowing the rest of a long chapter: once
           there is no room left for another full pull, cut at the next
           sentence rather than holding on. */
        val remaining = verses.size - (i + 1)
        val full = group.size >= target
        val ends = verse.text.trimEnd().lastOrNull() in TERMINAL
        val forced = remaining == 0
        if (forced || (full && ends && pulls.size < BIBLE_PULL_TARGET - 1)) {
            pulls += pullOf(pulls.size + 1, book, chapter, group)
            group = mutableListOf()
        }
    }
    if (group.isNotEmpty()) pulls += pullOf(pulls.size + 1, book, chapter, group)
    return BibleReading(book, chapter, pulls)
}

private fun pullOf(index: Int, book: String, chapter: Int, group: List<BibleVerse>): BiblePull {
    val first = group.first().verse
    val last = group.last().verse
    return BiblePull(
        index = index,
        text = group.joinToString(" ") { it.text.trim() },
        reference = if (first == last) "$book $chapter:$first" else "$book $chapter:$first–$last",
        firstVerse = first,
        lastVerse = last,
    )
}

private val TERMINAL = setOf('.', '!', '?', ':')
