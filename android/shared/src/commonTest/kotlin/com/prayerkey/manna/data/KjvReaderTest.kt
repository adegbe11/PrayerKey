package com.prayerkey.manna.data

import kotlin.test.Test
import kotlin.test.assertEquals

class KjvReaderTest {

    @Test
    fun readsBooksChaptersAndVersesInOrder() {
        val json = """
            [{"abbrev": "gn", "chapters": [["In the beginning", "And the earth"], ["Thus the heavens"]], "name": "Genesis"},
             {"name": "Jude", "extra": {"a": [1, true, null]}, "chapters": [["Jude, the servant"]]}]
        """
        val seen = mutableListOf<Pair<String, List<List<String>>>>()
        KjvReader(json).books { name, chapters -> seen += name to chapters }
        assertEquals(listOf("Genesis", "Jude"), seen.map { it.first })
        assertEquals(listOf(listOf("In the beginning", "And the earth"), listOf("Thus the heavens")), seen[0].second)
        assertEquals(listOf(listOf("Jude, the servant")), seen[1].second)
    }

    @Test
    fun resolvesEscapes() {
        val json = """[{"name": "X", "chapters": [["He said, \"Come\" — and\\or \/ go\nnow"]]}]"""
        var verse = ""
        KjvReader(json).books { _, chapters -> verse = chapters[0][0] }
        assertEquals("He said, \"Come\" — and\\or / go\nnow", verse)
    }
}
