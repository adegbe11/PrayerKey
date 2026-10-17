package com.prayerkey.manna.data

/**
 * The prayer machine: what someone brings, turned into a prayer to pray.
 *
 * Not a voice that answers for God. It finds, among the 543 written prayers,
 * the one that meets what was brought, and walks the person through it the way
 * the church has always taught prayer - be still, stand on the Word, worship,
 * confess, ask, listen, Amen. Every word on the way is Scripture or written
 * prayer; nothing is generated. It runs on the phone, offline, at once.
 *
 * Teaching is in the shape, not in lessons: each step is named (Worship,
 * Confess, Ask, Listen), so after a month of praying this way a person who
 * said they did not know how to pray has learned the pattern by doing it.
 */
object PrayerMachine {

    enum class Kind { Still, Word, Worship, Confess, Ask, Listen, Amen }

    data class Step(
        val kind: Kind,
        /** the step's name, shown small above it */
        val label: String,
        val text: String,
        /** a Scripture reference under the text, or blank */
        val reference: String = "",
    )

    data class Guided(
        /** what was brought, in the person's own words */
        val request: String,
        val topic: PrayerTopic,
        val steps: List<Step>,
        /** who is being prayed for, when it is someone else */
        val forWhom: Person? = null,
    )

    /** Someone prayed for, and the words that stand for them. */
    data class Person(val name: String, val obj: String, val possessive: String)

    /**
     * Who the prayer is for, when it is not the one praying.
     *
     * "My mother is sick" asks for the mother, not the person holding the
     * phone. The written prayers are mostly in the first person - "by Your
     * stripes I am healed" - so prayed as written they would be prayed over
     * the wrong body.
     */
    fun personIn(request: String): Person? {
        val m = PERSON.find(request.lowercase()) ?: return null
        val who = "${m.groupValues[1]} ${m.groupValues[2]}"
        val noun = m.groupValues[2]
        return when (noun) {
            in HER -> Person(who, "her", "her")
            in HIM -> Person(who, "him", "his")
            else -> Person(who, "them", "their")
        }
    }

    /** Builds the prayer for [request], or null when there are no prayers to draw on. */
    fun guide(request: String, topics: List<PrayerTopic>, day: Int = java.time.LocalDate.now().dayOfYear): Guided? {
        val topic = match(request, topics) ?: return null
        val (verseRef, verseText) = topic.scripture.firstOrNull() ?: ("Psalm 46:10" to "Be still, and know that I am God.")
        val steps = buildList {
            add(Step(Kind.Still, "Be still", request.trim().replaceFirstChar { it.uppercase() }))
            add(Step(Kind.Word, "His Word", verseText.trim(), verseRef))
            add(Step(Kind.Worship, "Worship", worship(topic.category, day)))
            add(Step(Kind.Confess, "Confess", CONFESSIONS[day % CONFESSIONS.size]))
            val person = personIn(request)
            val asking = if (person != null) intercession(topic.category, person) else topic.prayer
            chunks(asking).forEach { add(Step(Kind.Ask, "Ask", it)) }
            add(Step(Kind.Listen, "Listen", "Be still, and know that I am God.", "Psalm 46:10"))
            add(Step(Kind.Amen, "Amen", "Amen."))
        }
        return Guided(request.trim(), topic, steps, personIn(request))
    }

    /**
     * The prayer that best meets what was written.
     *
     * Words are widened by meaning first - "worried" also looks for anxiety
     * and peace, "rent" for finance - then each prayer is scored: a word in
     * its title counts most, in its category next, in its body least. With
     * nothing to go on it falls back to a prayer for the day, never to none.
     */
    fun match(request: String, topics: List<PrayerTopic>): PrayerTopic? {
        if (topics.isEmpty()) return null
        val words = words(request)
        val wanted = (words + words.flatMap { SENSES[it].orEmpty() }).map(::stem).toSet()
        val categories = words.flatMap { CATEGORY_OF[it].orEmpty() }.toSet()
        val best = topics.maxByOrNull { t ->
            val title = words(t.title)
            val category = t.category.lowercase()
            val body = words(t.prayer)
            var score = 0.0
            wanted.forEach { w ->
                if (w in title) score += 6
                if (category.contains(w)) score += 3
                if (w in body) score += 1
            }
            if (t.category in categories) score += 5
            // an occasion prayer only when the occasion was named
            if ("birthday" in title && "birthday" !in words) score -= 8
            if ("morning" in title && "morning" !in words) score -= 7
            if ("night" in title && "night" !in words) score -= 7
            score
        }
        val bestScore = best?.let { b -> wanted.count { it in words(b.title) || it in words(b.prayer) } + (if (b.category in categories) 1 else 0) } ?: 0
        if (best != null && bestScore > 0) return best
        return topics.firstOrNull { it.category == "Daily Prayer" } ?: topics.first()
    }

    /** The prayer in short pieces, two sentences at a time, the Amen kept for its own step. */
    internal fun chunks(prayer: String): List<String> {
        val body = prayer.trim()
            .replace(Regex("""\s*(In Jesus['’] (mighty )?name,?\s*)?Amen\.?\s*$""", RegexOption.IGNORE_CASE), "")
            .trim()
        val sentences = Regex("""[^.!?;]+[.!?;]+["”’]?\s*""").findAll(body).map { it.value.trim() }.filter { it.isNotBlank() }.toList()
            .ifEmpty { listOf(body) }
        // the name closes the last piece of the asking; alone on its own step it read thin
        val pieces = sentences.chunked(2).map { it.joinToString(" ") }
        return pieces.dropLast(1) + (pieces.last() + " In Jesus’ name.")
    }

    /** The asking, prayed for someone else: written for each area of life. */
    internal fun intercession(category: String, p: Person): String {
        val who = p.name; val o = p.obj; val pos = p.possessive
        return when (category) {
            "Health" -> "Lord, I lift $who up to You. You know $pos body and everything the doctors have said. Lay Your healing hand on $o and ease $pos pain. Give wisdom to everyone caring for $o, and give $o rest and courage. Let $o feel You near in every hour of this."
            "Mental Health" -> "Lord, I bring $who to You. You see the weight on $pos mind and heart. Quiet every fear, and guard $o with Your peace. Surround $o with people who are kind and wise. Let $o know, deep down, that $pos life is held by You."
            "Finance", "Work" -> "Lord, I bring $who to You. You know what $pos family needs and what $pos hands can do. Open honest doors for $o, and give $o favour and wisdom. Keep $o from fear and from shame. Provide for $o, Father, as You have promised."
            "Family", "Marriage", "Relationships" -> "Lord, I lift $who up to You. You love $o more than I ever could. Draw $o close to You, and heal what is broken between us. Protect $o, and fill $pos home with Your peace. Make me patient and kind toward $o."
            "Grief" -> "Lord, I bring $who to You in this sorrow. You are close to the brokenhearted. Hold $o when the grief comes in waves. Comfort $o with Your presence and with people who stay. Give $o hope that does not fade."
            "Education" -> "Lord, I bring $who to You. Give $o a clear mind, a good memory and steady nerves. Help $o prepare well and rest well. Let $o remember what $pos work has taught. Whatever the result, let $o know $pos worth is in You."
            "Travel", "Protection" -> "Lord, I place $who in Your hands. Go before $o and stay beside $o. Keep $o safe from every harm on the way. Give those around $o care and wisdom. Bring $o safely to where $pos journey ends."
            "Salvation", "Spiritual Life", "Faith" -> "Lord, I bring $who to You. You are not willing that any should perish. Open $pos eyes to Your love and $pos heart to Your truth. Send people who will show $o Jesus. Draw $o to Yourself, Father, in Your time."
            else -> "Lord, I lift $who up to You. You know everything $pos heart is carrying today. Meet $o in it with Your grace. Strengthen $o, guide $o and keep $o. Let $o know that $pos life is in Your hands."
        }
    }

    private val PERSON = Regex("""\b(my|our)\s+(mother|mum|mom|mummy|mommy|father|dad|daddy|husband|wife|son|daughter|brother|sister|child|children|kids|baby|grandmother|grandma|grandfather|grandpa|granny|aunt|aunty|auntie|uncle|friend|boss|pastor|family|parents|neighbour|neighbor|cousin|niece|nephew|fiance|fiancé|fiancee|fiancée|girlfriend|boyfriend|colleague|coworker|teacher|team|students|church|nation|country)\b""")
    private val HER = setOf("mother", "mum", "mom", "mummy", "mommy", "wife", "daughter", "sister", "grandmother", "grandma", "granny", "aunt", "aunty", "auntie", "niece", "fiancee", "fiancée", "girlfriend")
    private val HIM = setOf("father", "dad", "daddy", "husband", "son", "brother", "grandfather", "grandpa", "uncle", "nephew", "fiance", "fiancé", "boyfriend")

    private fun words(text: String): Set<String> =
        text.lowercase().split(Regex("""[^a-z']+""")).map { it.trim('\'') }
            .filter { it.length > 2 && it !in STOP }.map(::stem).toSet()

    /** A plain ending trim, so "worries", "worried" and "worrying" meet. */
    private fun stem(w: String): String = when {
        // "travelling" -> "travel", not "travell"
        w.length > 5 && w.endsWith("ing") -> w.dropLast(3).let { b -> if (b.length > 3 && b.last() == b[b.length - 2] && b.last() != 's') b.dropLast(1) else b }
        w.length > 4 && w.endsWith("ies") -> w.dropLast(3) + "y"
        w.length > 4 && w.endsWith("ied") -> w.dropLast(3) + "y"
        w.length > 4 && w.endsWith("ed") -> w.dropLast(2)
        w.length > 4 && w.endsWith("s") && !w.endsWith("ss") -> w.dropLast(1)
        else -> w
    }

    private fun worship(category: String, day: Int): String {
        val lines = WORSHIP[category] ?: WORSHIP.getValue("Daily Prayer")
        return lines[day % lines.size]
    }

    private val STOP = setOf(
        "the", "and", "for", "with", "that", "this", "are", "was", "you", "your", "have", "has", "but", "not",
        "from", "they", "them", "she", "her", "him", "his", "our", "want", "need", "pray", "prayer", "about",
        "please", "help", "lord", "god", "jesus", "today", "really", "just", "very", "who", "what", "when",
        "how", "all", "can", "will", "would", "been", "being", "into", "out", "over", "some", "more",
    )

    /** Words people use, widened to the words the prayers use. */
    private val SENSES: Map<String, List<String>> = mapOf(
        "anxious" to listOf("anxiety", "peace", "worry", "fear"), "anxiety" to listOf("peace", "worry"),
        "worry" to listOf("anxiety", "peace"), "worri" to listOf("anxiety", "peace"), "stress" to listOf("anxiety", "peace", "rest"),
        "afraid" to listOf("fear", "courage"), "scar" to listOf("fear", "courage"), "fear" to listOf("courage", "peace"),
        "panic" to listOf("anxiety", "peace"), "overwhelm" to listOf("anxiety", "rest", "peace"),
        "sad" to listOf("depression", "comfort"), "depress" to listOf("depression", "hope"), "lonely" to listOf("loneliness", "comfort"),
        "tir" to listOf("rest", "strength"), "exhaust" to listOf("rest", "strength"), "weak" to listOf("strength"),
        "sick" to listOf("healing", "health"), "ill" to listOf("healing", "health"), "pain" to listOf("healing"),
        "hospital" to listOf("healing", "surgery"), "cancer" to listOf("healing", "cancer"), "surgery" to listOf("surgery", "healing"),
        "money" to listOf("finance", "provision", "debt"), "broke" to listOf("provision", "finance", "debt"), "debt" to listOf("debt", "finance"),
        "rent" to listOf("provision", "finance"), "bill" to listOf("provision", "finance"), "job" to listOf("work", "job", "employment"), "lost" to listOf("losing", "loss"), "lose" to listOf("losing", "loss"),
        "fight" to listOf("unhappy", "conflict", "peace", "separated"), "argu" to listOf("unhappy", "conflict", "peace"),
        "work" to listOf("work", "job"), "business" to listOf("business", "work"), "promotion" to listOf("work", "favour"),
        "marriage" to listOf("marriage"), "husband" to listOf("marriage", "husband"), "wife" to listOf("marriage", "wife"),
        "spouse" to listOf("marriage"), "divorce" to listOf("marriage", "restoration"),
        "child" to listOf("children", "family"), "children" to listOf("children", "family"), "kid" to listOf("children", "family"),
        "son" to listOf("children", "son"), "daughter" to listOf("children", "daughter"), "baby" to listOf("children", "pregnancy"),
        "mother" to listOf("mother", "family", "parents"), "mum" to listOf("mother", "family", "parents"), "mom" to listOf("mother", "family", "parents"),
        "father" to listOf("father", "family", "parents"), "dad" to listOf("father", "family", "parents"), "parent" to listOf("parents", "family"),
        "family" to listOf("family"), "home" to listOf("family", "home"),
        "died" to listOf("grief", "loss", "comfort"), "death" to listOf("grief", "loss"), "loss" to listOf("grief", "comfort"),
        "grief" to listOf("grief", "comfort"), "funeral" to listOf("grief", "comfort"), "miss" to listOf("grief", "comfort"),
        "exam" to listOf("exams", "education", "wisdom"), "school" to listOf("education", "school"), "study" to listOf("education", "wisdom"),
        "decision" to listOf("guidance", "wisdom", "direction"), "direction" to listOf("guidance", "direction"), "confus" to listOf("guidance", "wisdom"),
        "thank" to listOf("thanksgiving", "gratitude"), "grateful" to listOf("thanksgiving", "gratitude"), "gratitude" to listOf("thanksgiving"),
        "sin" to listOf("forgiveness", "repentance"), "guilt" to listOf("forgiveness"), "forgive" to listOf("forgiveness"), "shame" to listOf("forgiveness"),
        "angry" to listOf("anger", "forgiveness", "peace"), "anger" to listOf("anger", "peace"), "hurt" to listOf("healing", "forgiveness"),
        "travel" to listOf("travel", "journey", "protection"), "trip" to listOf("travel", "journey"), "safe" to listOf("protection", "safety"),
        "danger" to listOf("protection"), "attack" to listOf("protection", "deliverance"), "nation" to listOf("nation"), "country" to listOf("nation"),
        "church" to listOf("church"), "pastor" to listOf("pastor", "church", "ministry"), "faith" to listOf("faith"), "doubt" to listOf("faith", "doubt"),
        "addiction" to listOf("addiction", "deliverance", "freedom"), "temptation" to listOf("temptation"), "purpose" to listOf("purpose", "calling"),
        "friend" to listOf("friendship", "relationships"), "relationship" to listOf("relationships"), "single" to listOf("relationships", "spouse"),
        "salvation" to listOf("salvation"), "unsav" to listOf("salvation"), "pregnant" to listOf("pregnancy", "children"),
    )

    /** Words that name a whole area of life. */
    private val CATEGORY_OF: Map<String, List<String>> = mapOf(
        "anxious" to listOf("Mental Health"), "anxiety" to listOf("Mental Health"), "worri" to listOf("Mental Health"),
        "worry" to listOf("Mental Health"), "stress" to listOf("Mental Health"), "depress" to listOf("Mental Health"),
        "lonely" to listOf("Mental Health"), "sad" to listOf("Mental Health"), "panic" to listOf("Mental Health"),
        "sick" to listOf("Health"), "ill" to listOf("Health"), "pain" to listOf("Health"), "hospital" to listOf("Health"),
        "cancer" to listOf("Health"), "surgery" to listOf("Health"), "health" to listOf("Health"), "heal" to listOf("Health"),
        "money" to listOf("Finance"), "broke" to listOf("Finance"), "debt" to listOf("Finance"), "rent" to listOf("Finance"),
        "bill" to listOf("Finance"), "job" to listOf("Work", "Finance"), "work" to listOf("Work"), "business" to listOf("Work"),
        "marriage" to listOf("Marriage", "Family"), "husband" to listOf("Marriage"), "wife" to listOf("Marriage"),
        "child" to listOf("Family"), "children" to listOf("Family"), "son" to listOf("Family"), "daughter" to listOf("Family"),
        "mother" to listOf("Family"), "mum" to listOf("Family"), "mom" to listOf("Family"), "father" to listOf("Family"),
        "dad" to listOf("Family"), "family" to listOf("Family"), "parent" to listOf("Family"),
        "died" to listOf("Grief"), "death" to listOf("Grief"), "grief" to listOf("Grief"), "loss" to listOf("Grief"), "funeral" to listOf("Grief"),
        "exam" to listOf("Education"), "school" to listOf("Education"), "study" to listOf("Education"),
        "thank" to listOf("Thanksgiving", "Celebration"), "grateful" to listOf("Thanksgiving", "Celebration"),
        "travel" to listOf("Travel"), "trip" to listOf("Travel"), "safe" to listOf("Protection"), "danger" to listOf("Protection"),
        "nation" to listOf("Nation"), "country" to listOf("Nation"), "church" to listOf("Church", "Ministry"),
        "relationship" to listOf("Relationships"), "friend" to listOf("Relationships"), "purpose" to listOf("Purpose"),
        "sin" to listOf("Salvation", "Spiritual Life"), "forgive" to listOf("Spiritual Life", "Salvation"), "faith" to listOf("Faith"),
    )

    /** Who He is, said first - written for each area of life. */
    private val WORSHIP: Map<String, List<String>> = mapOf(
        "Daily Prayer" to listOf(
            "Father, You are holy, and Your mercies are new this morning.",
            "Lord, You are good, and Your love endures for ever.",
            "God, You are near to all who call on You.",
        ),
        "Health" to listOf("Lord, You are the God who heals, and nothing in my body is hidden from You.", "Jesus, You touched the sick and made them whole. You have not changed."),
        "Mental Health" to listOf("Lord, You are the God of peace, and You keep in perfect peace the mind that rests on You.", "Father, You are near to the brokenhearted, and You know every thought before I think it."),
        "Family" to listOf("Father, every family in heaven and on earth is named by You.", "Lord, You set the lonely in families, and You love mine more than I do."),
        "Marriage" to listOf("Lord, You made marriage, and You are faithful when we are not.", "God, You are love itself, patient and kind, and You hold us together."),
        "Finance" to listOf("Lord, You are my provider, and every good gift comes from You.", "Father, the earth is Yours and everything in it, and You know what I need."),
        "Work" to listOf("Lord, You worked and rested, and You give purpose to the work of my hands.", "God, You open doors no one can shut."),
        "Grief" to listOf("Lord, You are close to the brokenhearted, and You weep with those who weep.", "Jesus, You are the resurrection and the life."),
        "Faith" to listOf("Lord, You are faithful in all You say and do.", "God, You are the author and finisher of my faith."),
        "Protection" to listOf("Lord, You are my refuge and my fortress, my God in whom I trust.", "God, You neither slumber nor sleep, and You keep me."),
        "Education" to listOf("Lord, all wisdom and knowledge are hidden in You, and You give it freely.", "God, You give understanding to those who ask."),
        "Relationships" to listOf("Lord, You call me friend, and You are a friend who sticks closer than a brother.", "God, You are love, and You teach me how to love."),
        "Thanksgiving" to listOf("Lord, You are good, and every good thing in my life came from Your hand.", "Father, You have done great things, and I am glad."),
        "Celebration" to listOf("Lord, You are the giver of every good and perfect gift.", "God, You have done this, and it is marvellous in my eyes."),
        "Nation" to listOf("Lord, You rule over the nations, and the hearts of kings are in Your hand.", "God, righteousness exalts a nation, and You are righteous."),
        "Salvation" to listOf("Lord, You are not willing that any should perish.", "Jesus, You came to seek and to save the lost."),
        "Spiritual Life" to listOf("Lord, You are Spirit and truth, and You draw near to those who draw near to You.", "God, You are the vine and I am the branch."),
        "Purpose" to listOf("Lord, You knew me before I was formed, and Your plans for me are good.", "God, You began a good work in me and You will finish it."),
        "Ministry" to listOf("Lord, the harvest is Yours, and You send the workers.", "Jesus, You came to serve, and You make servants strong."),
        "Church" to listOf("Lord Jesus, You love Your church and gave Yourself for her.", "God, You build Your church, and the gates of hell will not prevail."),
        "Deliverance" to listOf("Lord, You are my deliverer, and whom the Son sets free is free indeed.", "God, greater is He that is in me than he that is in the world."),
        "Youth" to listOf("Lord, You call the young to Yourself, and You are not ashamed of them.", "God, You are with the young and You make them strong."),
        "Travel" to listOf("Lord, You go before me and You keep my going out and my coming in.", "God, there is nowhere I can go from Your presence."),
    )

    /** A short honest turning, before the asking. */
    private val CONFESSIONS = listOf(
        "Search me, O God, and know my heart. Forgive me where I have gone my own way.",
        "Lord, I have not always trusted You. Forgive my sin and make my heart clean.",
        "Father, I bring You what I have done and what I have left undone. Have mercy on me.",
        "Create in me a clean heart, O God, and renew a right spirit within me.",
    )
}
