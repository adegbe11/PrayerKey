package com.prayerkey.manna

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Every verse the prayer library quotes must be the bundled King James.
 *
 * It quoted a modern translation in 1,083 of 1,084 places - "By his wounds we
 * are healed", "Heal me, Lord, and I will be healed". Two problems, and the
 * second is the serious one:
 *
 *  - The app ships one Bible and reads KJV everywhere else, so a prayer that
 *    quoted differently was quoting a Bible the reader does not have.
 *  - Modern translations are licensed. A thousand quotations is far past any
 *    fair-use argument, and publishers do enforce it. That is an app removed
 *    from Play, not a stern letter.
 *
 * One reference was Sirach 38:2 - Apocrypha, outside the 66 books this app
 * ships and outside the canon most of its readers hold. Prayer for Doctors now
 * cites Exodus 15:26, "I am the LORD that healeth thee".
 */
class PrayerScriptureTest {

    private val assets = File("src/main/assets")

    private fun kjv(): Map<String, List<List<String>>> {
        val root = org.json.JSONArray(File(assets, "kjv.json").readText())
        return (0 until root.length()).associate { b ->
            val book = root.getJSONObject(b)
            val chapters = book.getJSONArray("chapters")
            book.getString("name") to (0 until chapters.length()).map { c ->
                val verses = chapters.getJSONArray(c)
                (0 until verses.length()).map { verses.getString(it) }
            }
        }
    }

    /**
     * The same rule [cleanScripture] uses on every other screen: a brace group
     * containing a colon is a marginal note and comes out of the body; one
     * without is a word the translators supplied and only loses its braces.
     *
     * Getting this wrong left "with his stripes we are healed. wounded: or,
     * tormented stripes: Heb. bruise" on the healing prayer.
     */
    private fun clean(raw: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < raw.length) {
            if (raw[i] == '{') {
                val close = raw.indexOf('}', i)
                if (close == -1) { out.append(raw[i]); i++; continue }
                val inner = raw.substring(i + 1, close)
                if (!Regex("""^[^:]*:\s""").containsMatchIn(inner)) out.append(inner)
                i = close + 1
            } else {
                out.append(raw[i]); i++
            }
        }
        return out.toString().replace(Regex("""\s+"""), " ")
            .replace(" ,", ",").replace(" .", ".").trim()
    }

    @Test
    fun `every quoted verse is the bundled KJV`() {
        val bible = kjv()
        val alias = mapOf("Psalm" to "Psalms", "Song of Songs" to "Song of Solomon")
        val pattern = Regex("""^((?:[123] )?[A-Za-z][A-Za-z ]*?) (\d+):(\d+)(?:\s*[-–]\s*(\d+))?$""")
        val topics = JSONObject(File(assets, "prayer-topics.json").readText()).getJSONArray("topics")

        var checked = 0
        val wrong = mutableListOf<String>()
        for (t in 0 until topics.length()) {
            val scripture = topics.getJSONObject(t).optJSONArray("scripture") ?: continue
            for (s in 0 until scripture.length()) {
                val entry = scripture.getJSONObject(s)
                val ref = entry.getString("ref").trim()
                val match = pattern.matchEntire(ref)
                if (match == null) {
                    wrong += "unparseable: $ref"
                } else {
                    val book = alias[match.groupValues[1]] ?: match.groupValues[1]
                    val rows = bible[book]?.getOrNull(match.groupValues[2].toInt() - 1)
                    if (rows == null) {
                        wrong += "not in the canon: $ref"
                    } else {
                        val from = match.groupValues[3].toInt()
                        val to = match.groupValues[4].ifBlank { match.groupValues[3] }.toInt()
                        val expected = (from..to).joinToString(" ") { clean(rows[it - 1]) }
                        if (entry.getString("text").trim() != expected) wrong += ref
                        checked++
                    }
                }
            }
        }
        assertTrue("nothing was checked", checked > 1_000)
        assertEquals("these do not match the bundled KJV: ${wrong.take(8)}", 0, wrong.size)
    }
}
