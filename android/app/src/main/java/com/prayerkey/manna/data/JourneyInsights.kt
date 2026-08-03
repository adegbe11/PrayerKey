package com.prayerkey.manna.data

/**
 * A deliberately conservative reflection over data that already belongs to
 * the person. It reports patterns; it never claims that God said something.
 */
data class JourneyInsight(
    val eyebrow: String,
    val title: String,
    val body: String,
    val nextStep: String,
    val signalCount: Int,
    val recommendedReference: String? = null,
)

object JourneyInsights {
    private data class Theme(val label: String, val words: Set<String>, val invitation: String, val reference: String)

    private val themes = listOf(
        Theme("peace", setOf("anxious", "anxiety", "afraid", "fear", "worry", "peace"), "Return to one prayer and name what you can release today.", "Philippians 4:6–7"),
        Theme("healing", setOf("heal", "healing", "sick", "pain", "hospital", "health"), "Pause and pray for the person or place that still needs care.", "Psalm 34:18"),
        Theme("direction", setOf("wisdom", "direction", "decision", "calling", "purpose", "choose"), "Write the next faithful step that is already clear.", "Proverbs 3:5–6"),
        Theme("relationships", setOf("family", "marriage", "friend", "forgive", "relationship", "love"), "Choose one relationship to cover in prayer and one loving action.", "John 3:16"),
        Theme("gratitude", setOf("grateful", "thank", "blessing", "joy", "praise"), "Record one specific gift you do not want to forget.", "Psalm 118:24"),
    )

    fun build(
        entries: List<JournalEntry>,
        sermons: List<SermonNote>,
        prayers: List<JournalPrayer>,
        savedWords: List<SavedWord>,
    ): JourneyInsight? {
        val recentEntries = entries.sortedByDescending { it.createdAt }.take(20)
        val recentSermons = sermons.sortedByDescending { it.createdAt }.take(6)
        val recentPrayers = prayers.sortedByDescending { it.createdAt }.take(12)
        val corpus = buildString {
            recentEntries.forEach { append(' ').append(it.body).append(' ').append(it.gratitude) }
            recentSermons.forEach { append(' ').append(it.title).append(' ').append(it.takeaway) }
            recentPrayers.forEach { append(' ').append(it.request) }
        }.lowercase()

        val signalCount = recentEntries.size + recentSermons.size + recentPrayers.size + savedWords.take(20).size
        if (signalCount < 2) return null

        val theme = themes
            .map { candidate -> candidate to candidate.words.sumOf { word -> Regex("\\b${Regex.escape(word)}\\w*\\b").findAll(corpus).count() } }
            .maxByOrNull { it.second }
            ?.takeIf { it.second >= 2 }
            ?.first

        val references = buildList {
            recentEntries.mapNotNullTo(this) { it.verseRef }
            recentSermons.flatMapTo(this) { it.scriptures }
            savedWords.take(20).mapTo(this) { it.reference }
        }
        val recurringReference = references
            .groupingBy { it.trim() }
            .eachCount()
            .maxByOrNull { it.value }
            ?.takeIf { it.value >= 2 }
            ?.key

        val openPrayers = recentEntries.count { it.isPrayer && it.answeredAt == null }
        val answered = recentEntries.count { it.answeredAt != null } + savedWords.count { it.answeredAt != null }

        val bodyParts = buildList {
            theme?.let { add("${it.label.replaceFirstChar(Char::uppercase)} has appeared more than once in your recent reflections.") }
            recurringReference?.let { add("$it has stayed close across your words, sermons, or saved Scripture.") }
            if (openPrayers > 0) add("You are carrying $openPrayers ${if (openPrayers == 1) "open prayer" else "open prayers"}.")
            if (answered > 0) add("Your journey also holds $answered ${if (answered == 1) "recorded answer" else "recorded answers"}.")
        }

        return JourneyInsight(
            eyebrow = "A PATTERN IN YOUR JOURNEY",
            title = theme?.let { "A season of ${it.label}" }
                ?: recurringReference?.let { "$it keeps returning" }
                ?: "Your story is taking shape",
            body = bodyParts.joinToString(" ").ifBlank { "Your prayers, words, and reflections are beginning to form a story worth remembering." },
            nextStep = theme?.invitation
                ?: recentSermons.firstOrNull()?.takeaway?.takeIf { it.isNotBlank() }
                ?: "Take one quiet minute to write what you want to carry forward.",
            signalCount = signalCount,
            recommendedReference = theme?.reference,
        )
    }
}
