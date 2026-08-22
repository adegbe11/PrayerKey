package com.prayerkey.manna.ui.church

import com.prayerkey.manna.data.BibleCanon

/**
 * Turning recogniser output into something a person can read.
 *
 * Vosk emits lowercase words and nothing else. That is a property of the model,
 * not a setting — the small English model has no punctuation head, so no
 * configuration produces one.
 *
 * What it does give us is utterances. The recogniser returns a result at each
 * natural pause, so [SermonCleanup] already gets sentence boundaries for free
 * and capitalises and stops each one. The transcript is therefore not a wall of
 * text. What is missing is everything *inside* a sentence:
 *
 *     and jesus said unto peter i am the way and paul writes in romans that
 *     god has not given us a spirit of fear
 *
 * Every proper noun lowercase, every "I" lowercase, no question marks, and long
 * utterances running on. That is what this fixes, and all of it is decidable
 * from the words themselves — no model required:
 *
 *  - **Names.** The divine names, and all 66 books of the canon, which come
 *    from [BibleCanon] rather than a hand-typed list so they cannot drift.
 *  - **"I".** Always capital in English, and its absence is the single most
 *    obvious tell that a transcript was machine-made.
 *  - **Questions.** An utterance opening with an interrogative gets a question
 *    mark instead of a full stop. Preachers ask a great many questions.
 *  - **Breath commas.** A discourse marker at the head of a clause is where a
 *    speaker paused, and a comma there is almost always right.
 *
 * It is not a punctuation model and does not pretend to be. It will not find
 * the comma in a long subordinate clause. What it removes is the machine-made
 * look, which is most of the distance between unreadable and readable.
 */
object SermonPunctuation {

    /** Names that are always capitalised in this context. */
    private val DIVINE = listOf(
        "god", "jesus", "christ", "lord", "father", "saviour", "savior",
        "holy spirit", "holy ghost", "jehovah", "yahweh", "messiah",
        "almighty", "son of god", "son of man", "emmanuel", "immanuel",
    )

    /** People and places a sermon says constantly. */
    private val NAMES = listOf(
        "israel", "jerusalem", "judah", "egypt", "babylon", "zion", "bethlehem",
        "nazareth", "galilee", "calvary", "eden", "sinai", "jordan",
        "abraham", "isaac", "jacob", "joseph", "moses", "aaron", "joshua",
        "david", "solomon", "elijah", "elisha", "daniel", "samuel", "saul",
        "peter", "paul", "john", "james", "mary", "martha", "lazarus",
        "timothy", "barnabas", "stephen", "silas", "pharaoh", "satan", "pharisees",
    )

    /** An utterance that starts with one of these is a question. */
    private val INTERROGATIVE = setOf(
        "who", "what", "when", "where", "why", "how", "which", "whose",
        "is", "are", "was", "were", "do", "does", "did", "can", "could",
        "will", "would", "shall", "should", "have", "has", "am",
    )

    /** A speaker breathes before these. */
    private val MARKERS = listOf(
        "but", "and yet", "so", "now", "therefore", "however", "beloved",
        "listen", "watch this", "you see", "again", "finally", "brethren",
    )

    /** All 66 books, so "romans" and "first corinthians" capitalise properly. */
    private val BOOKS: List<String> by lazy {
        BibleCanon.books.map { it.name.lowercase() }
    }

    /**
     * Punctuates one utterance.
     *
     * [isQuestion] is decided here rather than by the caller because the opening
     * word is the only evidence available — the recogniser does not report
     * intonation.
     */
    fun polish(utterance: String): String {
        var text = utterance.trim().lowercase()
        if (text.isBlank()) return ""

        text = capitalise(text)

        // a comma where the speaker took a breath
        MARKERS.forEach { marker ->
            text = text.replace(Regex("""^($marker)\s+""", RegexOption.IGNORE_CASE), "$1, ")
        }

        return close(text.replaceFirstChar { it.uppercase() })
    }

    /**
     * The names and the "I", without touching anything else.
     *
     * Split out for [SermonPunctuator]: when the model has punctuated a
     * sentence, its commas and capitals must survive, so the rest of [polish]
     * — which lowercases first and inserts its own breath comma — cannot run.
     * What the model does not know is the canon, and this is that half.
     */
    fun capitalise(input: String): String {
        var text = input.replace(Regex("""\bi\b"""), "I")
        text = text.replace(Regex("""\bi'(m|ll|ve|d)\b"""), "I'$1")
        // longest first, so "holy spirit" wins before "spirit"
        (DIVINE + NAMES + BOOKS).sortedByDescending { it.length }.forEach { name ->
            text = text.replace(Regex("""\b${Regex.escape(name)}\b""", RegexOption.IGNORE_CASE)) { match ->
                match.value.split(' ').joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
            }
        }
        return text
    }

    /**
     * Ends the utterance, and decides between a full stop and a question mark.
     *
     * The punctuation model does not produce question marks. Every one of these
     * came back as a statement:
     *
     *     who do you say that i am  ->  Who do you say that I am.
     *
     * So the rule stays, and it applies even after the model has run — but not
     * on the first word alone, which is what it used to do. That version turned
     *
     *     when you are in trouble, when the money is gone, God is still God
     *
     * into a question, because "when" opens the list. Length is what separates
     * the two: a sentence that opens with an interrogative and then runs past a
     * dozen words has almost always opened a subordinate clause, not asked
     * anything. Short and interrogative is a question.
     */
    fun close(input: String): String {
        // a trailing comma is the model hedging at the end of a phrase; it is
        // never how a sentence ends, and appending to it would give ",."
        val text = input.trim().trimEnd(',', ';', ' ')
        if (text.isEmpty()) return ""
        val body = text.trimEnd('.', '!', '?')
        return body + if (asksSomething(body)) "?" else "."
    }

    private const val QUESTION_WORDS = 12

    private fun asksSomething(text: String): Boolean {
        val words = text.split(Regex("""\s+"""))
        if (words.size > QUESTION_WORDS) return false
        return words.first().lowercase().trim(',', '"') in INTERROGATIVE
    }

    /** Whole transcript, one utterance at a time. */
    fun polish(utterances: List<String>): String =
        utterances.map(::polish).filter { it.isNotBlank() }.joinToString(" ")
}
