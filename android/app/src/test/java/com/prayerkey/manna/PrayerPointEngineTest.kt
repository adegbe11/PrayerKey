package com.prayerkey.manna

import com.prayerkey.manna.data.PrayerPointEngine
import com.prayerkey.manna.data.PrayerTopic
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The generator, run against all 543 real prayers rather than a sample.
 *
 * The failure being designed against is the one this app keeps producing: a
 * bank of points shared between situations that have nothing to do with each
 * other. So the tests that matter are about *difference* — that kidney disease
 * and a job interview do not get the same points, and that day 5 is not day 4.
 */
class PrayerPointEngineTest {

    @Test
    fun `every prayer card has a unique title`() {
        val duplicates = topics.groupingBy { it.title }.eachCount().filterValues { it > 1 }
        assertTrue("duplicate prayer titles: $duplicates", duplicates.isEmpty())
    }

    @Test
    fun `prayers avoid unsafe outcome guarantees`() {
        val guarantees = Regex(
            "(?i)(cancel every debt and replace it with abundance|" +
                "I will rise from this stronger|aircraft perform perfectly|" +
                "develop every organ, every system perfectly|baby develop perfectly)",
        )
        topics.forEach { topic ->
            assertTrue(
                "'${topic.title}' contains an outcome guarantee: ${topic.prayer}",
                !guarantees.containsMatchIn(topic.prayer),
            )
        }
    }

    @Test
    fun `canonical prayer session contains only distinct written prayer sentences`() {
        val topic = topics.first { it.title.contains("salvation", true) }
        val points = PrayerPointEngine.sessionPointsFor(topic)
        assertTrue(points.isNotEmpty())
        assertEquals(points.size, points.distinct().size)
        assertTrue(points.size < 40)
    }

    @Test
    fun `prayer title is never inserted into a prayer point as the subject`() {
        val repentance = topics.first { it.title.equals("Prayer of Repentance", true) }
        val points = PrayerPointEngine.sessionPointsFor(repentance)
        assertTrue(points.none { it.contains("prayer of repentance", true) })
        assertTrue(points.none { it.contains("concerning repentance", true) })
    }

    private val topics: List<PrayerTopic> by lazy {
        val root = JSONObject(File("src/main/assets/prayer-topics.json").readText())
            .getJSONArray("topics")
        (0 until root.length()).map { i ->
            val o = root.getJSONObject(i)
            val sc = o.optJSONArray("scripture")
            PrayerTopic(
                slug = o.getString("slug"),
                title = o.getString("title"),
                category = o.getString("category"),
                prayer = o.optString("prayer"),
                scripture = (0 until (sc?.length() ?: 0)).mapNotNull {
                    sc!!.optJSONObject(it)?.let { v -> v.optString("ref") to v.optString("text") }
                },
                prayerPoints = emptyList(),
            )
        }
    }

    @Test
    fun `every situation yields a full day of points`() {
        topics.forEach { t ->
            val pts = PrayerPointEngine.pointsFor(t, 1)
            assertEquals("${t.title} produced ${pts.size} points", 30, pts.size)
        }
    }

    @Test
    fun `every point is sealed and none is empty`() {
        topics.forEach { t ->
            PrayerPointEngine.pointsFor(t, 3).forEach { p ->
                assertTrue("'${t.title}' produced a blank point", p.trim().length > 15)
                /* "in the name of Jesus" or "with the blood of Jesus" — both are
                   how a point closes in this tradition. What matters is that it
                   closes. */
                assertTrue("not sealed: $p", p.trimEnd().endsWith("of Jesus."))
            }
        }
    }

    @Test
    fun `a day never repeats itself`() {
        topics.forEach { t ->
            val pts = PrayerPointEngine.pointsFor(t, 2)
            assertEquals("${t.title} repeated a point within one day", pts.size, pts.toSet().size)
        }
    }

    /**
     * The whole point. Two unrelated situations must not pray the same thing.
     * Only the shared frame — praise, covering, sealing — may overlap.
     */
    @Test
    fun `different situations get different points`() {
        val a = topics.first { it.title.contains("Kidney", true) }
        val b = topics.first { it.category == "Work" }
        val pa = PrayerPointEngine.pointsFor(a, 1).toSet()
        val pb = PrayerPointEngine.pointsFor(b, 1).toSet()
        val shared = pa.intersect(pb).size
        assertTrue(
            "'${a.title}' and '${b.title}' shared $shared of 30 points",
            shared <= 8,
        )
    }

    /** Across the whole library, the body must be overwhelmingly distinct. */
    @Test
    fun `the library does not collapse into one prayer`() {
        val all = topics.flatMap { PrayerPointEngine.pointsFor(it, 1) }
        val distinct = all.toSet().size
        assertTrue(
            "543 situations produced only $distinct distinct points",
            distinct > 2500,
        )
    }

    @Test
    fun `consecutive days differ`() {
        topics.take(120).forEach { t ->
            val d4 = PrayerPointEngine.pointsFor(t, 4)
            val d5 = PrayerPointEngine.pointsFor(t, 5)
            assertTrue("${t.title}: day 4 and day 5 are identical", d4 != d5)
        }
    }

    /** The petitions come from the prayer, so most situations must yield some. */
    @Test
    fun `most prayers yield petitions of their own`() {
        val withPetitions = topics.count { PrayerPointEngine.petitionCount(it) >= 3 }
        assertTrue(
            "only $withPetitions of ${topics.size} prayers yielded 3+ petitions",
            withPetitions > topics.size * 3 / 4,
        )
    }

    /** Never aim a prayer point at a person. */
    @Test
    fun `no point is aimed at a human being`() {
        /* Only warfare framing counts. A quoted verse may legitimately mention
           a mother's womb; what must never appear is a person on the receiving
           end of "be scattered". */
        val aggression = Regex("(?i)(be scattered|be destroyed|be uprooted|refuse to make peace with|die by fire)")
        val person = Regex("(?i)\\b(enemy|enemies|neighbour|neighbor|boss|husband|wife|mother-in-law|colleague|that (man|woman|person))\\b")
        topics.forEach { t ->
            PrayerPointEngine.pointsFor(t, 1).forEach { p ->
                if (aggression.containsMatchIn(p)) {
                    assertTrue("'${t.title}' aimed a point at a person: $p", !person.containsMatchIn(p))
                }
            }
        }
    }

    /** "Renew my marriage and my friendships" once became "Renew my my friendships". */
    @Test
    fun `splitting a compound request does not stutter`() {
        topics.forEach { t ->
            PrayerPointEngine.pointsFor(t, 1).forEach { p ->
                assertTrue(
                    "'${t.title}' produced a stutter: $p",
                    !Regex("(?i)\\b(my my|our our|me me|the the)\\b").containsMatchIn(p),
                )
            }
        }
    }

    /** A point ending "with the blood of Jesus" must not then add "in the name of Jesus". */
    @Test
    fun `the name is not said twice`() {
        topics.forEach { t ->
            PrayerPointEngine.pointsFor(t, 2).forEach { p ->
                assertTrue(
                    "'${t.title}' sealed twice: $p",
                    !p.contains("of Jesus, in the name of Jesus"),
                )
            }
        }
    }

    @Test
    fun `the subject reads naturally`() {
        assertEquals("kidney disease", PrayerPointEngine.subject(topics.first { it.title == "Prayer for Kidney Disease" }))
    }
}
