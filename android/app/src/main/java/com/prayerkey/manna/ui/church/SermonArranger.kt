package com.prayerkey.manna.ui.church

/**
 * Turns a raw sermon transcript into an arranged note — on device, with no
 * model and no API call.
 *
 * Two layers, deliberately:
 *
 *  1. A LANGUAGE-AGNOSTIC core. Sentences are scored on how much they carry
 *     the sermon's own repeated vocabulary. Function words are found by
 *     document frequency — a word appearing in most sentences is doing
 *     grammar, not meaning — so no stopword list is needed and this works
 *     in a language nobody wrote a list for.
 *  2. An OPTIONAL cue boost. Where we know a language's signposts ("the
 *     first thing", "write this down"), matching sentences score higher.
 *     Preachers signpost heavily, so this sharpens the result a lot — but
 *     its absence degrades quality rather than breaking it.
 *
 * Every line in the note is one of the preacher's own sentences. Nothing is
 * paraphrased, so nothing can be invented.
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

    /** Signposts per language tag. Absent language = core scoring only. */
    private class Cues(val point: List<String>, val quote: List<String>, val action: List<String>)

    private val CUES: Map<String, Cues> = mapOf(
        "en" to Cues(
            point = listOf(
                "the first thing", "the second thing", "the third thing", "number one", "number two",
                "number three", "first of all", "secondly", "thirdly", "my first point", "point number",
                "i want you to see", "i want you to notice", "here is the point", "here's the point",
                "the point is", "watch this", "notice that", "understand this", "the reason is",
                "that is why", "that's why", "the truth is", "let me tell you",
            ),
            quote = listOf(
                "write this down", "put this in your notes", "somebody needs to hear", "hear me",
                "listen to me", "i am telling you", "i'm telling you", "let me say it again",
                "say it again", "receive this", "catch this", "don't miss this", "mark my words",
                "church", "can i tell you",
            ),
            action = listOf(
                "this week", "when you leave", "from today", "go home and", "i want you to go",
                "starting today", "tomorrow morning", "the next time", "when something",
                "stop ", "start ", "don't ", "do not ", "ask for", "ask god", "begin to",
            ),
        ),
        "es" to Cues(
            point = listOf("lo primero", "lo segundo", "en primer lugar", "en segundo lugar", "quiero que veas", "el punto es", "por eso", "la verdad es"),
            quote = listOf("escribe esto", "escúchame", "te lo digo", "otra vez", "iglesia", "no te pierdas esto"),
            action = listOf("esta semana", "cuando salgas", "desde hoy", "manana", "la próxima vez", "empieza a", "deja de"),
        ),
        "fr" to Cues(
            point = listOf("la première chose", "la deuxième chose", "premièrement", "deuxièmement", "je veux que vous voyiez", "le point est", "c'est pourquoi", "la vérité est"),
            quote = listOf("écrivez ceci", "écoutez-moi", "je vous le dis", "encore une fois", "église", "ne manquez pas"),
            action = listOf("cette semaine", "quand vous partez", "dès aujourd'hui", "demain", "la prochaine fois", "commencez à", "arrêtez de"),
        ),
        "pt" to Cues(
            point = listOf("a primeira coisa", "a segunda coisa", "em primeiro lugar", "em segundo lugar", "quero que você veja", "o ponto é", "por isso", "a verdade é"),
            quote = listOf("escreva isso", "me ouça", "eu te digo", "de novo", "igreja", "não perca isso"),
            action = listOf("esta semana", "quando você sair", "a partir de hoje", "amanhã", "da próxima vez", "comece a", "pare de"),
        ),
    )

    /** Which languages get the sharper, signpost-aware treatment. */
    fun hasCuesFor(language: String): Boolean = CUES.containsKey(language.take(2).lowercase())

    /**
     * Splits on sentence enders, and on the long pauses a recogniser reports
     * as separate chunks (joined with " | " upstream).
     */
    private fun sentences(text: String): List<String> =
        text.replace(" | ", ". ")
            .split(Regex("(?<=[.!?。！？])\\s+|\\n+"))
            .map { it.trim().trim('.', ',', ';', ':', '-', ' ') }
            .filter { it.split(Regex("\\s+")).size >= 5 }

    private fun words(s: String): List<String> =
        s.lowercase().split(Regex("[^\\p{L}']+")).filter { it.length > 2 }

    fun arrange(transcript: String, references: List<String>, language: String = "en"): Note {
        val clean = transcript.trim()
        val all = sentences(clean)
        val cues = CUES[language.take(2).lowercase()]

        /* ---- language-agnostic vocabulary model ----
           A word that turns up in most sentences is grammar. A word that
           turns up often but not everywhere is what the sermon is about. */
        val perSentence = all.map { words(it).toSet() }
        val docFreq = HashMap<String, Int>()
        perSentence.forEach { set -> set.forEach { w -> docFreq[w] = (docFreq[w] ?: 0) + 1 } }
        val n = all.size.coerceAtLeast(1)
        val theme: Map<String, Double> = docFreq
            .filterValues { it >= 2 && it.toDouble() / n <= .45 }
            .mapValues { (_, df) -> df.toDouble() / n }

        val topTheme = theme.entries.sortedByDescending { it.value }.take(8).map { it.key }

        fun themeScore(s: String): Double {
            val ws = words(s)
            if (ws.isEmpty()) return 0.0
            val hit = ws.toSet().sumOf { theme[it] ?: 0.0 }
            // normalise so a long rambling sentence does not win on bulk
            return hit / Math.sqrt(ws.size.toDouble()) * 100.0
        }

        fun cueScore(s: String, list: List<String>?): Double {
            if (list == null) return 0.0
            val low = s.lowercase()
            return list.count { low.contains(it) } * 14.0
        }

        fun lengthFit(s: String, lo: Int, hi: Int, bonus: Double): Double {
            val w = s.split(Regex("\\s+")).size
            return if (w in lo..hi) bonus else if (w > hi + 20) -bonus else 0.0
        }

        /* ---- main points ---- */
        val points = all
            .map { s -> s to (themeScore(s) + cueScore(s, cues?.point) + lengthFit(s, 9, 34, 8.0)) }
            .filter { it.second > 12 }
            .sortedByDescending { it.second }
            .map { strip(it.first, cues) }
            .distinctBy { normalise(it) }
            .take(4)

        /* ---- quotes: short, flagged, punchy ---- */
        val quotes = all
            .map { s -> s to (cueScore(s, cues?.quote) * 2 + themeScore(s) * .5 + lengthFit(s, 6, 22, 14.0)) }
            .filter { it.second > 16 }
            .sortedByDescending { it.second }
            .map { strip(it.first, cues) }
            .filter { q -> points.none { normalise(it) == normalise(q) } }
            .distinctBy { normalise(it) }
            .take(2)

        /* ---- takeaway: the clearest instruction, or the closing thought ---- */
        val takeaway = all
            .mapIndexed { i, s ->
                // without cues, the last fifth of a sermon is where the
                // application lives, so position stands in for the signpost
                val tail = if (i > all.size * .8) 12.0 else 0.0
                s to (cueScore(s, cues?.action) * 3 + themeScore(s) * .5 + lengthFit(s, 7, 28, 10.0) + tail)
            }
            .filter { it.second > 18 }
            .maxByOrNull { it.second }
            ?.let { strip(it.first, cues) }
            ?: points.firstOrNull()
            ?: ""

        return Note(
            title = title(references, topTheme),
            scriptures = references.distinct(),
            points = points,
            quotes = quotes,
            takeaway = takeaway,
            transcript = clean,
        )
    }

    /** Title from the sermon's own vocabulary, or the passage he stayed in. */
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

    /** Drops the signpost so a line reads as a statement, not a preamble. */
    private fun strip(sentence: String, cues: Cues?): String {
        var s = sentence
        if (cues != null) {
            for (cue in cues.point + cues.quote) {
                val at = s.lowercase().indexOf(cue)
                if (at in 0..12) s = s.substring(at + cue.length)
            }
        }
        s = s.trim().trimStart(',', '.', ':', ';', '-', ' ')
        s = s.removePrefix("that ").removePrefix("is that ").removePrefix("was that ")
        return s.trim().replaceFirstChar { it.uppercase() }
    }

    private fun normalise(s: String) =
        s.lowercase().split(Regex("[^\\p{L}]+")).filter { it.length > 3 }.take(6).joinToString(" ")

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
