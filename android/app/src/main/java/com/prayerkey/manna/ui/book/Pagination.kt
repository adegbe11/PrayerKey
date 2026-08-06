package com.prayerkey.manna.ui.book

/**
 * Splitting a chapter across pages.
 *
 * A real book has a fixed page, so the text must be broken to fit it. The
 * prototype dodged this by hand-picking short chapters; Psalm 119 is 176
 * verses and will never be one page, and there are 1,189 chapters to handle.
 *
 * The approach: lay the whole chapter out **once** as a single flowing text,
 * then show one window of lines at a time. A Bible flows its verses inside
 * paragraphs — verse 3 continues on the line verse 2 ended — so paginating
 * verse-by-verse would put every verse on a new line and read like a list of
 * quotes rather than a book. Windowing the real layout also makes the breaks
 * exact, because the measurements are the ones the renderer actually used.
 *
 * Compose owns the measuring — only it knows how wide a Garamond line runs
 * at a given size on a given screen. This file owns the arithmetic on top,
 * which is where the off-by-one that loses a verse would live, and which can
 * therefore be tested without a device.
 */

/** One page: a window of laid-out lines. */
data class PageWindow(
    val index: Int,
    val firstLine: Int,
    /** exclusive */
    val lastLine: Int,
) {
    val lines: Int get() = lastLine - firstLine
}

/**
 * Cuts a laid-out chapter into pages of at most [linesPerPage] lines.
 *
 * Every page gets the same room, because the reader reserves the chapter
 * heading's space on every leaf whether or not it draws one. Giving the first
 * page less would make the text block jump up a line on every turn, and would
 * make the page capacity depend on which page you are on — which feeds back
 * into how many pages there are.
 *
 * Always returns at least one page, so a chapter missing from the asset
 * renders an empty leaf you can turn rather than a dead screen.
 */
fun pageWindows(totalLines: Int, linesPerPage: Int): List<PageWindow> {
    val room = linesPerPage.coerceAtLeast(1)
    if (totalLines <= 0) return listOf(PageWindow(0, 0, 0))

    val windows = mutableListOf<PageWindow>()
    var line = 0
    while (line < totalLines) {
        val end = minOf(line + room, totalLines)
        windows += PageWindow(windows.size, line, end)
        line = end
    }
    return windows
}

/**
 * Which verses show up in a window of lines.
 *
 * [verseLines] is the line each verse *starts* on, paired with its number,
 * in ascending order. A verse counts as on the page if any part of it is —
 * so a verse spanning the break appears in both run-heads, which is what a
 * printed Bible does.
 *
 * Returns null when the window holds no verses at all.
 */
fun versesInWindow(
    verseLines: List<Pair<Int, Int>>,
    window: PageWindow,
): IntRange? {
    if (verseLines.isEmpty() || window.lines <= 0) return null

    var first: Int? = null
    var last: Int? = null
    for ((index, entry) in verseLines.withIndex()) {
        val (verse, startLine) = entry
        // the verse runs until the next one starts, or to the end of the text
        val endLine = verseLines.getOrNull(index + 1)?.second ?: Int.MAX_VALUE
        val overlaps = startLine < window.lastLine && endLine > window.firstLine
        if (overlaps) {
            if (first == null) first = verse
            last = verse
        }
    }
    val from = first ?: return null
    return from..(last ?: from)
}
