package com.prayerkey.manna.data

/**
 * Prayer points, generated from the prayer itself.
 *
 * The obvious way to add prayer points is a bank per category — one set for
 * Health, one for Family — and it is wrong. "Prayer for Kidney Disease" and
 * "Prayer for a Job Interview" are both real situations with nothing in common,
 * and giving them the same points is the mail merge this app has been having
 * removed from it all week.
 *
 * So nothing here is written per situation and nothing is shared between
 * situations. The points are **derived from each situation's own prayer**. All
 * 543 prayers are different, so all 543 point sets are different, and a new
 * situation added to the asset tomorrow gets its points for free.
 *
 * The frame is shared, and that is faithful rather than lazy: every prayer
 * meeting in this tradition opens with praise, covers the ground with the blood,
 * and seals at the end. What changes between meetings is the body, and the body
 * here comes entirely from the prayer that was written for that situation.
 *
 * Order, as it is prayed:
 *
 *  1. Praise            — 3
 *  2. Covering          — 2
 *  3. The Word          — 2   from this situation's own scripture
 *  4. Petitions         — up to 18, from this situation's own prayer
 *  5. Standing          — the remainder, framed by what kind of situation it is
 *  6. Sealing           — 2
 *                       ────
 *                         30
 */
object PrayerPointEngine {

    const val POINTS_PER_DAY = 30

    /** The seal every point carries. */
    private const val SEAL = ", in the name of Jesus."

    /* ── the shared frame ─────────────────────────────────────────────────
       Deliberately shared. A prayer meeting that does not open in praise is
       not this tradition, and rewriting the opening 543 times would be
       variation for its own sake. */

    private val PRAISE = listOf(
        "Father, I thank You for the gift of life and breath this day",
        "Thank You, Lord, for Your mercies that are new every morning",
        "I thank You, Father, for answers to prayer I have not yet seen",
        "Lord, I magnify Your name above every name that is named",
        "Thank You, Father, that You hear me every time I call",
        "I praise You, Lord, for You have never failed me once",
        "Father, I thank You that Your faithfulness does not depend on my strength",
        "Thank You, Lord, for the blood that speaks better things for me",
    )

    private val COVERING = listOf(
        "I cover myself and everything that concerns me with the blood of Jesus",
        "Father, let every unconfessed sin in my life be washed by the blood of Jesus",
        "I stand on the finished work of the cross this day",
        "Holy Spirit, take charge of this prayer and pray through me",
        "Father, let my prayer come before You as incense",
        "I close every door I have opened to the enemy through my own words",
    )

    private val SEALING = listOf(
        "Father, I thank You for hearing me, and I count it done",
        "Lord, let the answer to this prayer bring glory to Your name alone",
        "I refuse to worry over what I have just committed to God",
        "Father, seal this prayer with Your peace that passes understanding",
        "Thank You, Lord, for what You will do before I pray again",
    )

    /**
     * Petition verbs — the words a written prayer uses when it is asking.
     *
     * Used to tell a request apart from the narrative around it. "The house
     * feels quiet in a way I was not prepared for" is a true sentence and not a
     * prayer point; "Help me to embrace this new chapter" is.
     */
    private val ASKING = listOf(
        "give", "help", "let", "show", "fill", "renew", "reveal", "restore", "heal",
        "keep", "guard", "teach", "grant", "make", "send", "open", "break", "remove",
        "strengthen", "guide", "lead", "protect", "provide", "comfort", "deliver",
        "surround", "cover", "settle", "steady", "quiet", "hold", "carry", "meet",
        "speak", "walk", "go", "order", "establish", "bless", "may",
    )

    /** Situations where standing against something is the right posture. */
    private val CONTESTED = setOf(
        "health", "mental health", "protection", "deliverance", "grief", "nation",
    )

    /**
     * Subjects that name the answer rather than the affliction.
     *
     * Caught by "Prayer for Protection from Enemies", which sits in a contested
     * category and so generated "Every power assigned to protection from
     * enemies, be scattered" — a prayer against its own answer. When the
     * subject is the thing being asked for, the posture is petition even in a
     * contested category.
     */
    private val DESIRED = listOf(
        "protection", "healing", "health", "peace", "favour", "favor", "wisdom",
        "strength", "restoration", "provision", "deliverance", "breakthrough",
        "safety", "comfort", "guidance", "recovery", "victory", "freedom", "mercy",
        "grace", "hope", "joy", "rest", "sleep", "faith", "courage", "patience",
    )

    /**
     * The day's thirty points.
     *
     * [day] rotates every section, so day 5 is not day 4 — a twenty-one day
     * programme that prayed the same thirty points daily would be the same
     * failure in a new place.
     */
    fun pointsFor(topic: PrayerTopic, day: Int): List<String> {
        val n = day.coerceAtLeast(1)
        val out = mutableListOf<String>()

        val seed = seedOf(topic)
        out += rotate(PRAISE, n, 3, seed)
        out += rotate(COVERING, n, 2, seed)
        out += wordPoints(topic, n)

        val petitions = petitionsFrom(topic.prayer)
        out += rotate(petitions, n, 18)

        out += standingPoints(topic, n)
        out += rotate(SEALING, n, 2, seed)

        /* A prayer with few petitions of its own — "Prayer for Healing" yields
           six — must still produce a full day rather than a short one. Top up
           from the frame, moving through it, so the padding is not the same
           three lines every time. */
        /* A prayer with few petitions of its own — "Prayer for Healing" yields
           six — must still produce a full day. Topping up from the shared frame
           made two unrelated situations converge: kidney disease and a new job
           came out sharing 18 of 30. So the padding comes from the standing
           bank, every line of which carries this situation's own subject. */
        val filled = out.distinct().toMutableList()
        var pass = 1
        while (filled.size < POINTS_PER_DAY && pass < 40) {
            standingPoints(topic, n + pass, 12).forEach {
                if (filled.size < POINTS_PER_DAY && it !in filled) filled += it
            }
            if (filled.size < POINTS_PER_DAY) {
                (rotate(PRAISE, n + pass, 3, seed) + rotate(COVERING, n + pass, 3, seed)).forEach {
                    if (filled.size < POINTS_PER_DAY && it !in filled) filled += it
                }
            }
            pass++
        }
        return filled.take(POINTS_PER_DAY).map(::seal)
    }

    /** How many distinct petitions a situation's own prayer yields. */
    fun petitionCount(topic: PrayerTopic): Int = petitionsFrom(topic.prayer).size

    /* ── from this situation's scripture ──────────────────────────────── */

    private fun wordPoints(topic: PrayerTopic, day: Int): List<String> {
        val verses = topic.scripture.filter { it.first.isNotBlank() && it.second.isNotBlank() }
        if (verses.isEmpty()) {
            return listOf("Father, let Your written Word be my ground in this matter")
        }
        val verse = verses[(day - 1).mod(verses.size)]
        return listOf(
            "Father, I stand on Your promise in ${verse.first}: ${verse.second.trimEnd('.')}",
            "Lord, perform Your word in ${verse.first} concerning ${subject(topic)}",
        )
    }

    /* ── from this situation's own prayer ─────────────────────────────── */

    /**
     * Pulls the requests out of a written prayer.
     *
     * A prayer is prose: address, then narrative, then petitions, then a
     * closing. Only the petitions become points. Compound requests are split,
     * because "Give me courage for today, wisdom for each decision, and
     * strength for the treatment ahead" is three points in this tradition, not
     * one.
     */
    internal fun petitionsFrom(prayer: String): List<String> {
        if (prayer.isBlank()) return emptyList()

        val body = prayer
            .replace(Regex("""(?i)\bin jesus'?s? name,?\s*amen\.?"""), " ")
            .replace(Regex("""(?i)\bamen\.?"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        val sentences = body.split(Regex("(?<=[.!?])\\s+")).map { it.trim() }.filter { it.length > 12 }

        return sentences
            .filter { isAsking(it) }
            .flatMap(::splitCompound)
            .map { it.trim().trim(',', '.', ';').trim() }
            .filter { it.length in 14..190 }
            .distinctBy { it.lowercase() }
    }

    private fun isAsking(sentence: String): Boolean {
        val first = sentence.substringBefore(' ').lowercase().trim(',', '.')
        if (first in ASKING) return true
        // "Lord, help me…" / "Father, let Your peace…" — the address comes first
        val afterAddress = sentence.substringAfter(',', "").trim().substringBefore(' ').lowercase()
        if (afterAddress in ASKING) return true
        /* A written prayer asks in more than one voice. "I pray for these
           kidneys", "I ask You to go before me", "Where there is pain, let Your
           peace reign" — all requests, none of them starting with a verb. */
        val lower = " " + sentence.lowercase() + " "
        return listOf(
            " i pray ", " i ask ", " we ask ", " i commit ", " i bring ", " i release ",
            " let ", " may ", " help me", " help us", " would you ", " i trust you ",
        ).any { it in lower }
    }

    /**
     * "Give me courage for today, wisdom for each decision, and strength for
     * the road" becomes three points, each carrying the governing verb.
     */
    private fun splitCompound(sentence: String): List<String> {
        val clean = sentence.trim()
        val head = Regex("""^((?:[A-Z][a-z]+,\s*)?(?:[A-Za-z]+)\s+(?:me|us|my|our)\b)\s*(.*)$""")
            .find(clean) ?: return listOf(clean)
        val verb = head.groupValues[1]
        val rest = head.groupValues[2]
        /* The head already carries the possessive — "Renew my" — so a part
           that repeats it produced "Renew my my friendships". Drop the echo. */
        val tail = verb.substringAfterLast(' ').lowercase()
        val parts = rest.split(Regex(""",\s*(?:and\s+)?|\s+and\s+"""))
            .map { it.trim() }
            .map { part ->
                val head = part.substringBefore(' ').lowercase()
                if (head == tail) part.substringAfter(' ').trim() else part
            }
            .filter { it.length > 6 }
        // only split when it is genuinely a list, or the verb is lost
        return if (parts.size < 2) listOf(clean) else parts.map { "$verb $it" }
    }

    /* ── the posture this situation calls for ─────────────────────────── */

    /**
     * What is being stood against, or stood upon.
     *
     * Never a person. Sickness, fear, debt and despair are named; nobody's
     * neighbour is. The app says so on the Warfare screen and it has to be true
     * in the generator as well, not only in the copy.
     */
    private fun standingPoints(topic: PrayerTopic, day: Int, count: Int = 5): List<String> {
        val what = subject(topic)
        val first = what.substringBefore(' ').trim()
        // do not scatter the very thing being asked for
        val contested = topic.category.lowercase() in CONTESTED && first !in DESIRED
        val bank = if (contested) listOf(
            "Every power assigned to $what in my life, be scattered",
            "I refuse to make peace with $what",
            "Father, let Your hand rest on $what until it answers to You",
            "Every root of $what that I cannot see, be uprooted",
            "I withdraw my name from the register of $what",
            "Lord, let what has resisted every other help bow to Your word",
            "Father, let Your mercy overrule every report concerning $what",
            "I release the peace of God into $what",
            "Lord, let the strength of $what be broken in my life",
            "Father, turn $what into a testimony that others will hear",
            "Let every fear attached to $what lose its grip on me",
            "Lord, do a work in $what that I could not do for myself",
        ) else listOf(
            "Father, let Your hand rest upon $what",
            "Lord, order every step concerning $what",
            "Let Your favour go before me in $what",
            "Father, do for me in $what what no effort of mine can do",
            "Let Your wisdom govern every decision concerning $what",
            "Lord, let $what become a testimony in my mouth",
            "Father, remove every obstacle standing in the way of $what",
            "Lord, let Your timing rather than my impatience rule $what",
            "Father, provide everything that $what requires",
            "Let Your peace guard my heart concerning $what",
            "Lord, let nothing hidden in $what take me by surprise",
            "Father, be glorified in the outcome of $what",
        )
        return rotate(bank, day, count)
    }

    /** "Prayer for Kidney Disease" -> "kidney disease". */
    internal fun subject(topic: PrayerTopic): String =
        topic.title
            .replace(Regex("""(?i)^\s*(a\s+)?prayer\s+(for|against|during|before|after|over|when)\s+"""), "")
            .replace(Regex("""(?i)^\s*prayer\s*[:\-]\s*"""), "")
            .replace(Regex("""(?i)^(my|the|your)\s+"""), "")
            .trim()
            .ifBlank { topic.title }
            .lowercase()

    /* ── helpers ──────────────────────────────────────────────────────── */

    /**
     * Takes [count] from [items], offset by the day *and* by the situation.
     *
     * Day alone was not enough: on day 1 every situation opened with the same
     * three praise points, so kidney disease and a new job came out sharing 12
     * of 30. Seeding with the situation as well means two people praying
     * different things on the same morning are not reading the same page.
     */
    private fun rotate(items: List<String>, day: Int, count: Int, seed: Int = 0): List<String> {
        if (items.isEmpty()) return emptyList()
        val start = ((day - 1) * count + seed).mod(items.size)
        return (0 until minOf(count, items.size)).map { items[(start + it).mod(items.size)] }
    }

    /** A stable per-situation offset. */
    private fun seedOf(topic: PrayerTopic): Int =
        (topic.slug.ifBlank { topic.title }.hashCode() and Int.MAX_VALUE)

    private fun seal(point: String): String {
        val trimmed = point.trim().trimEnd('.', ',', ';').trim()
        val cased = trimmed.replaceFirstChar { it.uppercase() }
        /* A point that already ends in the name — "…with the blood of Jesus" —
           does not need the seal appended after it. */
        val alreadySealed = cased.endsWith("of Jesus", ignoreCase = true) ||
            cased.contains("name of Jesus", ignoreCase = true)
        return if (alreadySealed) "$cased." else cased + SEAL
    }
}
