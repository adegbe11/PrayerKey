package com.prayerkey.manna.data

data class JournalReflection(val suggestedTitle: String, val highlight: String, val questions: List<String>)

object JournalCoach {
    private val themes = linkedMapOf(
        "peace" to setOf("peace", "anxious", "worry", "rest"),
        "healing" to setOf("heal", "healing", "hurt", "pain"),
        "direction" to setOf("decision", "direction", "wisdom", "choose"),
        "gratitude" to setOf("grateful", "thank", "gift", "blessing"),
        "relationship" to setOf("family", "friend", "marriage", "forgive"),
    )

    fun reflect(entry: JournalEntry): JournalReflection {
        val clean = entry.body.replace(Regex("""[#>*_`\[\]-]"""), " ").replace(Regex("\\s+"), " ").trim()
        val theme = themes.entries.maxByOrNull { (_, words) -> words.count { clean.contains(it, true) } }?.takeIf { (_, words) -> words.any { clean.contains(it, true) } }?.key
        val title = entry.title.ifBlank {
            theme?.let { "A reflection on $it" } ?: clean.substringBefore('.').take(52).trim().ifBlank { "A moment worth remembering" }
        }
        val highlight = when {
            entry.gratitude.isNotBlank() -> "Gratitude stands out: ${entry.gratitude.take(100)}"
            entry.verseRef != null -> "This entry is anchored to ${entry.verseRef}."
            theme != null -> "A thread of $theme appears in what you wrote."
            else -> "You gave honest attention to this moment."
        }
        val questions = listOfNotNull(
            "What feeling sits underneath the words you chose?",
            theme?.let { "Where else has $it appeared in your recent journey?" },
            entry.verseRef?.let { "How does $it challenge or comfort your first reaction?" },
            "What is one small response you want to remember tomorrow?",
        ).take(3)
        return JournalReflection(title, highlight, questions)
    }
}
