package com.prayerkey.manna

import com.prayerkey.manna.data.PrayerMachine
import com.prayerkey.manna.data.PrayerTopic
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The prayer machine against the real 543 prayers. */
class PrayerMachineTest {

    private val topics: List<PrayerTopic> by lazy {
        val json = JSONObject(File("src/main/assets/prayer-topics.json").readText()).getJSONArray("topics")
        List(json.length()) { i ->
            val o = json.getJSONObject(i)
            val s = o.optJSONArray("scripture")
            PrayerTopic(
                o.getString("slug"), o.getString("title"), o.getString("category"), o.optString("prayer"),
                List(s?.length() ?: 0) { j -> s!!.getJSONObject(j).let { it.optString("ref") to it.optString("text") } },
                emptyList(),
            )
        }
    }

    private fun pick(request: String) = PrayerMachine.match(request, topics)!!

    @Test
    fun `everyday words find the right area of life`() {
        // what was said -> words the chosen prayer's title must carry
        val expect = mapOf(
            "I'm so worried about everything" to listOf("anxiety", "worry", "peace"),
            "my mother is sick in hospital" to listOf("healing", "sick"),
            "I can't pay my rent this month" to listOf("provision", "financial"),
            "my husband and I keep fighting" to listOf("unhappy", "separated"),
            "my exams are next week" to listOf("exam"),
            "my dad died last month" to listOf("loss", "grief"),
            "I lost my job" to listOf("losing a job"),
            "I just want to say thank you" to listOf("gratitude", "thanksgiving", "thank"),
            "travelling to Lagos tomorrow" to listOf("travel"),
            "our country is in trouble" to listOf("nation"),
        )
        val wrong = expect.mapNotNull { (said, want) ->
            val t = pick(said)
            println("$said  ->  ${t.title} [${t.category}]")
            if (want.any { t.title.lowercase().contains(it) }) null else "$said -> ${t.title}, wanted one of $want"
        }
        assertTrue(wrong.joinToString("\n"), wrong.isEmpty())
    }

    @Test
    fun `nothing recognisable still gives a prayer`() {
        assertEquals("Daily Prayer", pick("xyzzy qwerty").category)
    }

    @Test
    fun `the guided prayer walks the whole pattern`() {
        val g = PrayerMachine.guide("my mother is sick", topics, day = 10)!!
        val kinds = g.steps.map { it.kind }
        assertEquals(PrayerMachine.Kind.Still, kinds.first())
        assertEquals(PrayerMachine.Kind.Amen, kinds.last())
        listOf(PrayerMachine.Kind.Word, PrayerMachine.Kind.Worship, PrayerMachine.Kind.Confess, PrayerMachine.Kind.Ask, PrayerMachine.Kind.Listen)
            .forEach { assertTrue("missing $it", it in kinds) }
        // the asking ends with the name, and the Amen is its own step - never said twice
        val asking = g.steps.filter { it.kind == PrayerMachine.Kind.Ask }.joinToString(" ") { it.text }
        assertFalse(asking.contains("Amen"))
        g.steps.forEach { println("${it.label}: ${it.text} ${it.reference}") }
    }

    @Test
    fun `every prayer splits into readable pieces`() {
        topics.forEach { t ->
            val pieces = PrayerMachine.chunks(t.prayer)
            assertTrue(t.slug, pieces.isNotEmpty() && pieces.last().endsWith("In Jesus’ name."))
            assertTrue("${t.slug}: ${pieces.maxBy { it.length }}", pieces.all { it.length <= 420 })
        }
    }

    @Test
    fun `praying for someone prays for them, not for yourself`() {
        val g = PrayerMachine.guide("my mother is sick in hospital", topics)!!
        assertEquals("my mother", g.forWhom?.name)
        val asking = g.steps.filter { it.kind == PrayerMachine.Kind.Ask }.joinToString(" ") { it.text }
        println(asking)
        assertTrue(asking.contains("my mother") && asking.contains("her"))
        assertFalse("first-person healing words prayed over the wrong body", asking.contains("I am healed"))
    }

    @Test
    fun `the right words stand for each person`() {
        assertEquals("him", PrayerMachine.personIn("pray for my son's exams")?.obj)
        assertEquals("her", PrayerMachine.personIn("My wife lost her job")?.obj)
        assertEquals("them", PrayerMachine.personIn("my friend is depressed")?.obj)
        assertEquals("them", PrayerMachine.personIn("our children")?.obj)
        assertEquals(null, PrayerMachine.personIn("I am anxious"))
        assertEquals(null, PrayerMachine.personIn("my mummification hobby"))
    }
}
