package com.prayerkey.manna.ui.journal

import com.prayerkey.manna.data.JournalPrayer
import com.prayerkey.manna.data.SavedWord
import com.prayerkey.manna.data.SermonNote
import java.time.LocalDate

/**
 * Cures blank-page anxiety by never opening on a blank page.
 *
 * Every suggestion is built from something the person already did inside
 * this app — a sermon they captured, a prayer they asked for, a word they
 * kept. No photo library, no location history, no calendar: the app promises
 * that nothing leaves the phone, and the cheapest way to keep that promise
 * is to not reach for data it has no business holding.
 */
object JournalSuggestions {

    /** [source] matches JournalEntry.source, so a card knows its own origin. */
    data class Prompt(
        val kicker: String,
        val text: String,
        val source: String,
        val verseRef: String? = null,
        val verseText: String? = null,
        val suggestPrayer: Boolean = false,
        val starter: String = "",
    )

    private const val TWO_DAYS = 48L * 60 * 60 * 1000

    /** Rotates daily so the same question never greets two mornings running. */
    private val DAILY = listOf(
        "What is one thing you want to surrender right now?",
        "Where did you see God move today, even slightly?",
        "Who came to mind today that you should be praying for?",
        "What are you carrying that you have not said out loud?",
        "What is one mercy you almost missed today?",
        "What would you tell someone walking through what you are in?",
        "What are you hoping for that still has not come?",
    )

    fun build(
        sermons: List<SermonNote>,
        prayers: List<JournalPrayer>,
        savedWords: List<SavedWord>,
        nowMillis: Long,
    ): List<Prompt> {
        val out = mutableListOf<Prompt>()

        // the sermon bridge — strongest prompt we can make, because it names
        // something specific they sat through this week
        sermons.firstOrNull { nowMillis - it.createdAt <= TWO_DAYS }?.let { note ->
            val scripture = note.scriptures.firstOrNull()
            out += Prompt(
                kicker = "FROM SUNDAY",
                text = buildString {
                    append("You captured “${note.title}”")
                    if (scripture != null) append(" on $scripture")
                    append(". What is the one thing you want to carry into this week?")
                },
                source = "sermon",
                verseRef = scripture,
            )
            if (note.takeaway.isNotBlank()) {
                out += Prompt(
                    kicker = "FROM SUNDAY",
                    text = "“${note.takeaway}” — did you do it?",
                    source = "sermon",
                )
            }
        }

        // the prayer bridge — closes the loop on something they already asked
        prayers.firstOrNull { nowMillis - it.createdAt <= TWO_DAYS * 4 }?.let { prayer ->
            out += Prompt(
                kicker = "YOU PRAYED",
                text = "You prayed about “${prayer.title}”. How does your heart feel about it today?",
                source = "prayer",
                verseRef = prayer.scriptureRef,
                suggestPrayer = true,
            )
        }

        // a word they chose to keep
        savedWords.firstOrNull { it.answeredAt == null }?.let { word ->
            out += Prompt(
                kicker = "A WORD YOU KEPT",
                text = "You saved ${word.reference}. What did it meet in you?",
                source = "verse",
                verseRef = word.reference,
                verseText = word.verse,
            )
        }

        // always at least one open question
        out += Prompt(
            kicker = "TODAY",
            text = DAILY[(LocalDate.now().toEpochDay() % DAILY.size).toInt()],
            source = "write",
        )
        return out.take(4)
    }
}
