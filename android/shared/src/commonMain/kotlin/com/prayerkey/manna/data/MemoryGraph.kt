package com.prayerkey.manna.data

data class MemoryConnection(val kind: String, val label: String, val detail: String)

object MemoryGraph {
    fun connections(
        memory: MemoryVerse,
        saved: List<SavedWord>,
        entries: List<JournalEntry>,
        prayers: List<JournalPrayer>,
        sermons: List<SermonNote>,
    ): List<MemoryConnection> {
        val ref = normalize(memory.reference)
        val words = significant(memory.verse)
        val result = mutableListOf<MemoryConnection>()
        saved.firstOrNull { normalize(it.reference) == ref }?.let {
            result += MemoryConnection("SAVED WORD", it.reference, "You kept this passage for later.")
        }
        entries.firstOrNull { normalize(it.verseRef.orEmpty()) == ref || overlap(words, it.body + " " + it.gratitude) >= 2 }?.let {
            result += MemoryConnection("JOURNEY", it.verseRef ?: "A journal reflection", excerpt(it.body))
        }
        prayers.firstOrNull { normalize(it.scriptureRef.orEmpty()) == ref || overlap(words, it.request + " " + it.prayer) >= 2 }?.let {
            result += MemoryConnection("PRAYER", it.title.ifBlank { "A prayer you kept" }, excerpt(it.request))
        }
        sermons.firstOrNull { sermon -> sermon.scriptures.any { normalize(it) == ref } || overlap(words, sermon.transcript + " " + sermon.takeaway) >= 2 }?.let {
            result += MemoryConnection("SERMON", it.title, excerpt(it.takeaway))
        }
        return result.take(4)
    }

    private fun normalize(value: String) = value.lowercase().replace(Regex("[^a-z0-9]"), "")
    private fun significant(value: String) = value.lowercase().split(Regex("[^a-z]+"))
        .filter { it.length >= 5 && it !in setOf("shall", "therefore", "which", "their") }.toSet()
    private fun overlap(words: Set<String>, value: String) = significant(value).count { it in words }
    private fun excerpt(value: String) = value.trim().replace(Regex("\\s+"), " ").let {
        when { it.isBlank() -> "Connected by Scripture"; it.length <= 72 -> it; else -> it.take(69) + "…" }
    }
}
