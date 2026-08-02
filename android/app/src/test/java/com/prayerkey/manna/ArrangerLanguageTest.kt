package com.prayerkey.manna

import com.prayerkey.manna.ui.church.SermonArranger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The arranger has to survive languages nobody wrote cue words for. The
 * core scores sentences on the sermon's own repeated vocabulary, finding
 * function words by document frequency rather than a stopword list — so
 * these tests exist to prove it degrades rather than breaks.
 */
class ArrangerLanguageTest {

    /** Yoruba. No cue set exists for this language. */
    private val yoruba = listOf(
        "Olorun je olotito ninu gbogbo ohun ti o se",
        "Olorun ni agbara lati yi ipo re pada loni",
        "Nigbati odo ba gbe ni Olorun n sọ pe akoko ti to",
        "Akoko ti to fun ipo tuntun ati fun ilana tuntun",
        "Olorun n pe wa si ipo tuntun ni ose yii",
        "Ẹ maa gbekele Olorun ninu ohun gbogbo ti e n se",
        "Ni ose yii ẹ bere si beere fun ilana tuntun lati odo Olorun",
    ).joinToString(" | ")

    /** Spanish. A cue set does exist, so this should be sharper. */
    private val spanish = listOf(
        "Dios es fiel en todo lo que hace por nosotros",
        "Lo primero que quiero que veas es que el arroyo se secó por una razón",
        "El arroyo se secó porque la temporada terminó y no porque Dios te dejó",
        "Escribe esto los cuervos dejaron de venir pero Dios no dejó de ser Dios",
        "En segundo lugar la provisión está ligada a la posición",
        "Esta semana cuando algo se seque pregunta por la próxima instrucción",
    ).joinToString(" | ")

    @Test
    fun `a language with no cue set still produces a note`() {
        val note = SermonArranger.arrange(yoruba, listOf("1 Kings 17:7"), "yo-NG")
        assertTrue("expected points, got ${note.points}", note.points.isNotEmpty())
        assertTrue("expected a title", note.title.isNotBlank())
        assertTrue("expected a takeaway", note.takeaway.isNotBlank())
        assertTrue(note.scriptures.contains("1 Kings 17:7"))
    }

    @Test
    fun `lines are always the preacher's own words, in any language`() {
        val note = SermonArranger.arrange(yoruba, emptyList(), "yo-NG")
        val spoken = yoruba.lowercase()
        (note.points + note.quotes).forEach { line ->
            line.lowercase().split(" ").filter { it.length > 4 }.take(3).forEach { w ->
                assertTrue("\"$w\" was not spoken", spoken.contains(w))
            }
        }
    }

    @Test
    fun `the full transcript is always kept whatever the language`() {
        val note = SermonArranger.arrange(yoruba, emptyList(), "yo-NG")
        assertTrue(note.transcript.contains("Olorun"))
    }

    @Test
    fun `cue languages are recognised, others are not`() {
        assertTrue(SermonArranger.hasCuesFor("en-NG"))
        assertTrue(SermonArranger.hasCuesFor("es-MX"))
        assertTrue(SermonArranger.hasCuesFor("fr-FR"))
        assertFalse(SermonArranger.hasCuesFor("yo-NG"))
        assertFalse(SermonArranger.hasCuesFor("zh-CN"))
    }

    @Test
    fun `a cue language picks up its flagged line`() {
        val note = SermonArranger.arrange(spanish, listOf("1 Reyes 17:7"), "es-ES")
        assertTrue("expected points, got ${note.points}", note.points.isNotEmpty())
        val all = (note.points + note.quotes + note.takeaway).joinToString(" ").lowercase()
        assertTrue(
            "expected a signposted line to surface, got $all",
            all.contains("cuervos") || all.contains("provisión") || all.contains("instrucción"),
        )
    }

    @Test
    fun `signposts are stripped so a point reads as a statement`() {
        val note = SermonArranger.arrange(spanish, emptyList(), "es-ES")
        note.points.forEach { p ->
            assertFalse("point still opens with its cue: $p", p.lowercase().startsWith("lo primero"))
            assertFalse("point still opens with its cue: $p", p.lowercase().startsWith("escribe esto"))
        }
    }

    @Test
    fun `empty input does not crash in any language`() {
        listOf("en-US", "yo-NG", "zh-CN", "ar-EG").forEach { tag ->
            val note = SermonArranger.arrange("", emptyList(), tag)
            assertTrue(note.points.isEmpty())
            assertTrue(note.title.isNotBlank())
        }
    }
}
