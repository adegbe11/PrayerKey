package com.prayerkey.manna.ui.church

/**
 * Turns a raw sermon transcript into an arranged note — on device, with no
 * model and no API call.
 *
 * This works because preaching is heavily signposted. Preachers announce
 * their points ("the first thing I want you to see"), flag their best lines
 * ("write this down", "listen to me"), and repeat their theme words over
 * and over. Those signals are what we score on, so the note is built by
 * SELECTING the preacher's own sentences rather than inventing new ones.
 * Nothing is paraphrased, so nothing can be hallucinated.
 */
object SermonArranger {

    data class Note(
        val title: String,
        val scriptures: List<String>,
        val points: List<String>,
        val quotes: List<String>,
        val takeaway: String,
        val transcript: String,
    )

    /** "Here comes a point" — preachers say these before the meat. */
    private val POINT_CUES = listOf(
        "the first thing", "the second thing", "the third thing", "number one", "number two",
        "number three", "first of all", "secondly", "thirdly", "my first point", "point number",
        "i want you to see", "i want you to notice", "here is the point", "here's the point",
        "the point is", "watch this", "notice that", "understand this", "the reason is",
        "that is why", "that's why", "so what does that mean", "the truth is", "let me tell you",
    )

    /** "This line is the one" — preachers flag their own quotables. */
    private val QUOTE_CUES = listOf(
        "write this down", "put this in your notes", "somebody needs to hear", "hear me",
        "listen to me", "i am telling you", "i'm telling you", "let me say it again",
        "say it again", "receive this", "catch this", "don't miss this", "mark my words",
        "church", "somebody say amen", "can i tell you",
    )

    /** "Do this" — the takeaway sentence lives among these. */
    private val ACTION_CUES = listOf(
        "this week", "when you leave", "from today", "go home and", "i want you to go",
        "starting today", "tomorrow morning", "the next time", "when something", "stop ",
        "start ", "don't ", "do not ", "ask for", "ask god", "begin to",
    )

    private val STOP = (
        "the a an and or but if of to in on at for with is are was were be been being that this " +
        "those these it its as by from he she they we you i him her them us my your our their " +
        "not no so then than there here what which who whom when where why how all any both each " +
        "few more most other some such only own same too very can will just should now up down out " +
        "about into over after before again very said say says one two three thing things get got " +
        "going go went come came know knows let lets like also because while would could shall may " +
        "am does do did done have has had having make made take took give gave see saw seen look " +
        "amen god lord jesus christ holy spirit church brother sister pastor bible scripture verse " +
        "chapter man men woman people someone somebody everybody today day night time way life"
        ).split(" ").toSet()

    /**
     * Splits on sentence enders, and also on the long pauses a recogniser
     * reports as separate result chunks (we join those with " | ").
     */
    private fun sentences(text: String): List<String> =
        text.replace(" | ", ". ")
            .split(Regex("(?<=[.!?])\\s+|\\n+"))
            .map { it.trim().trim('.', ',', ';', ':', '-', ' ') }
            .filter { it.split(" ").size >= 5 }

    /** Theme words: most repeated meaningful words in the whole sermon. */
    private fun themeWords(text: String, take: Int): List<String> =
        text.lowercase().replace(Regex("[^a-z' ]"), " ").split(Regex("\\s+"))
            .filter { it.length > 3 && it !in STOP }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .take(take).map { it.key }

    fun arrange(transcript: String, references: List<String>): Note {
        val clean = transcript.trim()
        val all = sentences(clean)
        val theme = themeWords(clean, 8)

        fun themeScore(s: String): Int {
            val low = s.lowercase()
            return theme.withIndex().sumOf { (i, word) -> if (low.contains(word)) 8 - i else 0 }
        }
        fun cueScore(s: String, cues: List<String>): Int {
            val low = s.lowercase()
            return cues.count { low.contains(it) } * 12
        }

        /* ---- main points: cue-marked, theme-heavy, medium length ---- */
        val points = all
            .map { s ->
                var score = cueScore(s, POINT_CUES) + themeScore(s)
                val words = s.split(" ").size
                if (words in 9..34) score += 8          // a point is a sentence, not a speech
                if (words > 55) score -= 20
                if (s.endsWith("?")) score -= 6         // questions set up points, they aren't points
                s to score
            }
            .filter { it.second > 12 }
            .sortedByDescending { it.second }
            .map { stripCues(it.first) }
            .distinctBy { normalise(it) }
            .take(4)

        /* ---- quotes: flagged, short, punchy ---- */
        val quotes = all
            .map { s ->
                var score = cueScore(s, QUOTE_CUES) * 2 + themeScore(s) / 2
                val words = s.split(" ").size
                if (words in 6..22) score += 14          // quotable means short
                if (words > 30) score -= 18
                if (s.contains(" but ") || s.contains(" not ")) score += 6   // contrast lands
                s to score
            }
            .filter { it.second > 18 }
            .sortedByDescending { it.second }
            .map { stripCues(it.first) }
            .filter { pt -> points.none { normalise(it) == normalise(pt) } }
            .distinctBy { normalise(it) }
            .take(2)

        /* ---- takeaway: the clearest instruction he gave ---- */
        val takeaway = all
            .map { s ->
                var score = cueScore(s, ACTION_CUES) * 3 + themeScore(s) / 2
                val words = s.split(" ").size
                if (words in 7..28) score += 10
                s to score
            }
            .filter { it.second > 20 }
            .maxByOrNull { it.second }
            ?.let { stripCues(it.first) }
            ?: points.firstOrNull()
            ?: ""

        return Note(
            title = title(references, theme),
            scriptures = references.distinct(),
            points = points,
            quotes = quotes,
            takeaway = takeaway,
            transcript = clean,
        )
    }

    /**
     * Title from the sermon's own material: the two strongest theme words,
     * falling back to the passage he spent the most time in.
     */
    private fun title(references: List<String>, theme: List<String>): String {
        val main = references.groupingBy { it.substringBeforeLast(':') }.eachCount()
            .maxByOrNull { it.value }?.key
        val words = theme.take(2).map { w -> w.replaceFirstChar { it.uppercase() } }
        return when {
            words.size == 2 -> "${words[0]} and ${words[1]}"
            words.size == 1 && main != null -> "${words[0]} — $main"
            words.size == 1 -> words[0]
            main != null -> main
            else -> "Sunday service"
        }
    }

    /** Drops the signpost so the note reads as a statement, not a preamble. */
    private fun stripCues(sentence: String): String {
        var s = sentence
        for (cue in POINT_CUES + QUOTE_CUES) {
            val at = s.lowercase().indexOf(cue)
            if (at in 0..12) s = s.substring(at + cue.length)
        }
        s = s.trim().trimStart(',', '.', ':', ';', '-', ' ')
        s = s.removePrefix("that ").removePrefix("is that ").removePrefix("was that ")
        return s.trim().replaceFirstChar { it.uppercase() }
    }

    private fun normalise(s: String) =
        s.lowercase().replace(Regex("[^a-z ]"), "").split(" ").filter { it.length > 3 }.take(6).joinToString(" ")

    /** Plain-text export for the share sheet. */
    fun asShareText(note: Note, dateLabel: String, minutes: Int): String = buildString {
        appendLine(note.title)
        appendLine("$dateLabel · $minutes min")
        if (note.scriptures.isNotEmpty()) {
            appendLine()
            appendLine("SCRIPTURES PREACHED")
            note.scriptures.forEach { appendLine("• $it") }
        }
        if (note.points.isNotEmpty()) {
            appendLine()
            appendLine("THE MAIN POINTS")
            note.points.forEachIndexed { i, p -> appendLine("${i + 1}. $p") }
        }
        if (note.quotes.isNotEmpty()) {
            appendLine()
            appendLine("LINES WORTH KEEPING")
            note.quotes.forEach { appendLine("\"$it\"") }
        }
        if (note.takeaway.isNotBlank()) {
            appendLine()
            appendLine("CARRY THIS INTO YOUR WEEK")
            appendLine(note.takeaway)
        }
        appendLine()
        append("My notes from today — Manna")
    }
}
