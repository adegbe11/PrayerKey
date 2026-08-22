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

        // "i" standing alone, and its contractions
        text = text.replace(Regex("""\bi\b"""), "I")
        text = text.replace(Regex("""\bi'(m|ll|ve|d)\b"""), "I'$1")

        // names, longest first so "holy spirit" wins before "spirit"
        (DIVINE + NAMES + BOOKS).sortedByDescending { it.length }.forEach { name ->
            text = text.replace(Regex("""\b${Regex.escape(name)}\b""")) { match ->
                match.value.split(' ').joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
            }
        }

        // a comma where the speaker took a breath
        MARKERS.forEach { marker ->
            text = text.replace(Regex("""^($marker)\s+""", RegexOption.IGNORE_CASE), "$1, ")
        }

        val first = text.substringBefore(' ').lowercase().trim(',')
        val ending = if (first in INTERROGATIVE) "?" else "."

        text = text.replaceFirstChar { it.uppercase() }
        return if (text.last() in ".!?") text else text + ending
    }

    /** Whole transcript, one utterance at a time. */
    fun polish(utterances: List<String>): String =
        utterances.map(::polish).filter { it.isNotBlank() }.joinToString(" ")
}
