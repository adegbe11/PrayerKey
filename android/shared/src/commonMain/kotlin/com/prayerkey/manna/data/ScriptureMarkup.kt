package com.prayerkey.manna.data

/**
 * The KJV text we ship carries the 1611 apparatus inline, in braces:
 *
 *   "and darkness {was} upon the face of the deep"
 *   "God divided the light from the darkness. {the light from...: Heb. between
 *    the light and between the darkness}"
 *
 * There are 21,534 of the first kind and 7,859 of the second, and every one
 * of them was being drawn on screen with its braces showing — in the verse
 * deck, in shared images, everywhere. It reads as corrupted text.
 *
 * The two kinds are different things and want different treatment:
 *
 *  - **Supplied words.** Words the translators added for English sense that
 *    are not in the Hebrew or Greek. Printed KJVs set these in italic, and
 *    that convention is four centuries old, so the book italicises them and
 *    everywhere else just drops the braces.
 *  - **Marginal notes.** An alternative rendering, keyed by a catchphrase and
 *    an abbreviation — "Heb.", "Gr.", "Chal.". These belong in a margin, and
 *    a phone page has none, so they come out of the body. They are kept on
 *    the parsed verse rather than thrown away, so a footnote row can show
 *    them later without another pass over the asset.
 *
 * A note always contains a colon; a supplied word never does. That is the
 * whole test, and it holds across the asset.
 */
data class CleanVerse(
    val text: String,
    /** Character ranges in [text] that the translators supplied. */
    val supplied: List<IntRange>,
    /** Marginal notes lifted out of the body. */
    val notes: List<String>,
)

private val NOTE_MARK = Regex("""^[^:]*:\s""")

fun cleanScripture(raw: String): CleanVerse {
    if (!raw.contains('{')) return CleanVerse(raw.trim(), emptyList(), emptyList())

    val out = StringBuilder()
    val supplied = mutableListOf<IntRange>()
    val notes = mutableListOf<String>()

    var i = 0
    while (i < raw.length) {
        val open = raw.indexOf('{', i)
        if (open < 0) { out.append(raw, i, raw.length); break }
        val close = raw.indexOf('}', open + 1)
        // an unclosed brace is data damage; keep the rest verbatim rather
        // than silently truncating the verse
        if (close < 0) { out.append(raw, i, raw.length); break }

        out.append(raw, i, open)
        val inner = raw.substring(open + 1, close)

        if (inner.contains(':')) {
            notes += inner.trim()
            // the note sat between two spaces; leave only one behind
            while (out.isNotEmpty() && out.last() == ' ' &&
                close + 1 < raw.length && raw[close + 1] == ' '
            ) out.setLength(out.length - 1)
        } else {
            val start = out.length
            out.append(inner)
            supplied += start until out.length
        }
        i = close + 1
    }

    // collapse whatever double spaces the lifted notes left
    val squashed = StringBuilder()
    val moved = IntArray(out.length + 1)
    var j = 0
    while (j < out.length) {
        val c = out[j]
        moved[j] = squashed.length
        if (c == ' ' && squashed.isNotEmpty() && squashed.last() == ' ') { j++; continue }
        squashed.append(c)
        j++
    }
    moved[out.length] = squashed.length

    val text = squashed.toString().trim()
    val lead = squashed.length - squashed.toString().trimStart().length
    val shifted = supplied.mapNotNull { range ->
        val from = moved[range.first] - lead
        val to = moved[range.last + 1] - lead
        if (from in 0..text.length && to in 0..text.length && to > from) from until to else null
    }
    return CleanVerse(text, shifted, notes)
}
