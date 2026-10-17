package com.prayerkey.manna.data

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class KjvAssetTest {

    @Test
    fun theShippedBibleReadsWhole() {
        val text = File("../app/src/main/assets/kjv.json").readText()
        var books = 0
        var verses = 0
        var first = ""
        KjvReader(text).books { name, chapters ->
            if (books == 0) first = "$name ${chapters[0][0]}"
            books++
            verses += chapters.sumOf { it.size }
        }
        assertEquals(66, books)
        assertEquals(31_102, verses)
        assertEquals("Genesis In the beginning God created the heaven and the earth.", first)
    }
}
