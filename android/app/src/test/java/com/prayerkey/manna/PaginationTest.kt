package com.prayerkey.manna

import com.prayerkey.manna.ui.book.pageWindows
import com.prayerkey.manna.ui.book.versesInWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Page breaking. The invariant that matters most: every laid-out line shows
 * up on exactly one page. Losing a line of scripture to an off-by-one, or
 * repeating one, is not an acceptable failure mode — and it is invisible
 * unless you check, because a dropped line just looks like the text.
 */
class PaginationTest {

    private fun linesCovered(windows: List<com.prayerkey.manna.ui.book.PageWindow>) =
        windows.flatMap { (it.firstLine until it.lastLine).toList() }

    @Test
    fun `a short chapter is one page`() {
        val windows = pageWindows(totalLines = 12, linesPerPage = 30)
        assertEquals(1, windows.size)
        assertEquals(0, windows.first().firstLine)
        assertEquals(12, windows.first().lastLine)
    }

    @Test
    fun `every line lands on exactly one page`() {
        val windows = pageWindows(totalLines = 512, linesPerPage = 21)
        assertEquals((0 until 512).toList(), linesCovered(windows))
    }

    @Test
    fun `no line appears twice`() {
        val all = linesCovered(pageWindows(totalLines = 300, linesPerPage = 17))
        assertEquals(all.size, all.distinct().size)
    }

    @Test
    fun `windows are contiguous`() {
        val windows = pageWindows(totalLines = 200, linesPerPage = 13)
        windows.zipWithNext { a, b -> assertEquals(a.lastLine, b.firstLine) }
    }

    @Test
    fun `every page gets the same room`() {
        // the heading's space is reserved on every leaf, so capacity cannot
        // depend on which page you are looking at
        val windows = pageWindows(totalLines = 30, linesPerPage = 10)
        assertTrue(windows.dropLast(1).all { it.lines == 10 })
    }

    @Test
    fun `no page is empty when there is text`() {
        val windows = pageWindows(totalLines = 41, linesPerPage = 8)
        assertTrue(windows.all { it.lines > 0 })
    }

    @Test
    fun `an exact multiple does not produce a trailing blank page`() {
        val windows = pageWindows(totalLines = 24, linesPerPage = 8)
        assertEquals(3, windows.size)
        assertTrue(windows.last().lines > 0)
    }

    @Test
    fun `a missing chapter still yields one turnable page`() {
        val windows = pageWindows(totalLines = 0, linesPerPage = 20)
        assertEquals(1, windows.size)
    }

    @Test
    fun `a single line of room still covers the text and terminates`() {
        // pathological: largest type size on a short screen
        val windows = pageWindows(totalLines = 5, linesPerPage = 1)
        assertEquals(5, windows.size)
        assertEquals((0 until 5).toList(), linesCovered(windows))
    }

    @Test
    fun `page indices are sequential from zero`() {
        val windows = pageWindows(totalLines = 100, linesPerPage = 9)
        windows.forEachIndexed { i, w -> assertEquals(i, w.index) }
    }

    // ── verse ranges for the run-head ──────────────────────────────────────

    @Test
    fun `verses are reported for the window they occupy`() {
        // verse 1 starts line 0, verse 2 line 3, verse 3 line 7
        val verses = listOf(1 to 0, 2 to 3, 3 to 7)
        val windows = pageWindows(totalLines = 10, linesPerPage = 5)
        assertEquals(1..2, versesInWindow(verses, windows[0]))
        assertEquals(2..3, versesInWindow(verses, windows[1]))
    }

    @Test
    fun `a verse spanning the break shows on both pages`() {
        // verse 2 runs from line 3 to line 7, straddling the break at 5
        val verses = listOf(1 to 0, 2 to 3, 3 to 7)
        val windows = pageWindows(totalLines = 10, linesPerPage = 5)
        assertTrue(2 in versesInWindow(verses, windows[0])!!)
        assertTrue(2 in versesInWindow(verses, windows[1])!!)
    }

    @Test
    fun `an empty window reports no verses rather than a bogus range`() {
        assertNull(versesInWindow(listOf(1 to 0), com.prayerkey.manna.ui.book.PageWindow(0, 0, 0)))
        assertNull(versesInWindow(emptyList(), com.prayerkey.manna.ui.book.PageWindow(0, 0, 4)))
    }

    @Test
    fun `verses keep real numbers when a chapter is opened mid-way`() {
        val verses = listOf(9 to 0, 10 to 2)
        val windows = pageWindows(totalLines = 4, linesPerPage = 20)
        assertEquals(9..10, versesInWindow(verses, windows[0]))
    }
}
