package com.prayerkey.manna.data

data class StudyNote(val label: String, val text: String, val source: String)
data class StudyAnswer(val label: String, val text: String, val source: String)

object StudyLens {
    private val themes = linkedMapOf(
        "peace" to setOf("peace", "rest", "still", "fear", "afraid", "anxious"),
        "trust" to setOf("trust", "faith", "believe", "hope"),
        "love" to setOf("love", "mercy", "grace", "kind", "forgive"),
        "wisdom" to setOf("wisdom", "understanding", "path", "way", "light"),
        "strength" to setOf("strength", "strong", "courage", "power", "help"),
    )

    fun notes(reference: String, verse: String): List<StudyNote> {
        val lower = verse.lowercase()
        val found = themes.filterValues { words ->
            words.any { Regex("\\b${Regex.escape(it)}\\w*\\b").containsMatchIn(lower) }
        }.keys
        val themeText = found.take(3).joinToString(", ").ifBlank { "the character of God and faithful response" }
        return listOf(
            StudyNote("SCRIPTURE", "$reference — $verse", "Bible text"),
            StudyNote("OBSERVE", "This verse contains language connected with $themeText. Notice what the text says before deciding what it means for you.", "On-device word observation"),
            StudyNote("CONTEXT", "Read the full chapter and notice who is speaking, who is listening, what happens before this verse, and what response follows it.", "Study practice — not generated history"),
            StudyNote("REFLECT", "What does this reveal about God? What response does the passage invite? Where might you need wisdom from a trusted pastor or mature reader?", "Reflection questions"),
            StudyNote("BOUNDARY", "MANNA distinguishes Bible text from observation and reflection. It does not claim this note is divine revelation or the only Christian interpretation.", "Transparency"),
        )
    }

    fun answer(reference: String, verse: String, question: String): StudyAnswer {
        val normalized = question.trim().lowercase()
        if (normalized.isBlank()) return StudyAnswer("ASK A QUESTION", "Try asking: What does this passage say? What might I reflect on? How should I study its context?", "Question guide")
        val keyWords = verse.lowercase().split(Regex("[^a-z']+"))
            .filter { it.length > 4 && it !in setOf("shall", "therefore", "which", "their", "about") }
            .distinct().take(5)
        val observed = keyWords.joinToString(", ").ifBlank { "the repeated words and actions in the verse" }
        return when {
            normalized.contains("context") || normalized.startsWith("who") || normalized.startsWith("when") -> StudyAnswer(
                "CONTEXT PATH",
                "This single verse cannot establish its full historical or literary context. Read the surrounding chapter and identify speaker, audience, setting, and the argument before and after $reference.",
                "Study method — no external facts asserted",
            )
            normalized.contains("mean") || normalized.contains("say") || normalized.contains("observe") -> StudyAnswer(
                "TEXT OBSERVATION",
                "The displayed text emphasizes $observed. Describe its subjects, actions, promises, or commands in your own words before forming an interpretation.",
                "$reference and on-device word observation",
            )
            normalized.contains("apply") || normalized.contains("do") || normalized.contains("life") -> StudyAnswer(
                "REFLECTION, NOT A COMMAND",
                "Consider one response consistent with the passage: a belief to examine, a person to love, a practice to begin, or help to seek. Test your response against the full chapter and wise Christian community.",
                "Guided reflection",
            )
            normalized.contains("god") || normalized.contains("jesus") -> StudyAnswer(
                "THEOLOGICAL REFLECTION",
                "Ask what the text explicitly says about God before adding conclusions. Compare the observation with the surrounding chapter and related passages; MANNA does not present its reflection as God's personal message to you.",
                "Guided reflection with safety boundary",
            )
            else -> StudyAnswer(
                "A CAREFUL NEXT STEP",
                "Begin with the words $observed. What is explicit in the verse, what are you inferring, and what question remains for the chapter or a trusted teacher?",
                "$reference plus guided reflection",
            )
        }
    }
}
