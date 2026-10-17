package com.prayerkey.manna.data

import kotlinx.serialization.Serializable

// The shapes of PrayerKey's data, shared by Android and iPhone. The saved
// ones are @Serializable for the iPhone's store, which keeps them in a file.

@Serializable
data class SavedWord(
    val id: Long,
    val reference: String,
    val translation: String,
    val verse: String,
    val savedAt: Long,
    val answeredAt: Long? = null,
    val testimony: String? = null,
)

@Serializable
data class MemoryVerse(val reference: String, val verse: String, val stage: Int, val correctCount: Int = 0, val nextReviewAt: Long = 0L)

@Serializable
data class JournalPrayer(val id: Long, val title: String, val request: String, val prayer: String, val scriptureRef: String?, val createdAt: Long)

@Serializable
data class JournalEntry(
    val id: Long,
    val entryDay: Long,          // epoch day — one calendar day per group
    val mood: String,            // emoji key, e.g. "🙏"
    val body: String,            // what's on your heart
    val gratitude: String,       // one thing you're grateful for
    val verseRef: String?,       // optionally attached word
    val verseText: String?,
    val createdAt: Long,
    val updatedAt: Long,
    /** How this entry began: "write", "sermon", "prayer" or "verse". Drives
     *  the origin icon on the card so a page traces back to its moment. */
    val source: String = "write",
    val isPrayer: Boolean = false,
    val answeredAt: Long? = null,
    val testimony: String? = null,
    val prayerStage: PrayerStage = PrayerStage.Praying,
    val nextAction: String = "",
    val title: String = "",
    val tags: List<String> = emptyList(),
    val journal: String = "My Journey",
    val favorite: Boolean = false,
    val location: String = "",
    val weather: String = "",
    val media: List<String> = emptyList(),
)

@Serializable
data class SermonNote(
    val id: Long,
    val title: String,
    val scriptures: List<String>,
    val points: List<String>,
    val quotes: List<String>,
    val takeaway: String,
    val transcript: String,
    val minutes: Int,
    val createdAt: Long,
    /** The AAC master this note was made from, if it still exists. Lets the
     *  transcript be redone later with a better model — see SermonRefiner. */
    val audioPath: String = "",
    /** True once the better model has re-read the recording. */
    val refined: Boolean = false,
)

data class BibleVerse(
    val book: String,
    val chapter: Int,
    val verse: Int,
    /** Display text: the 1611 braces are already resolved away. */
    val text: String,
    /** Where the translators supplied words, for setting them in italic. */
    val supplied: List<IntRange> = emptyList(),
) {
    val reference: String get() = "$book $chapter:$verse"
}

data class GeneratedPrayer(
    val id: String,
    val title: String,
    val prayer: String,
    val encouragement: String,
    val verses: List<Pair<String, String>>,
)

data class PrayerTopic(
    val slug: String,
    val title: String,
    val category: String,
    val prayer: String,
    val scripture: List<Pair<String, String>>,
    val prayerPoints: List<String>,
)

data class RemoteVerse(val reference: String, val text: String, val translation: String)

@Serializable
enum class PrayerStage(val key: String, val label: String) {
    Praying("praying", "Praying"),
    Waiting("waiting", "Waiting"),
    Acting("acting", "Taking action"),
    Released("released", "Released"),
    AnsweredDifferently("answered_differently", "Answered differently"),
    Answered("answered", "Answered"),
    ;

    companion object {
        fun from(raw: String?) = entries.firstOrNull { it.key == raw } ?: Praying
    }
}
