package com.prayerkey.manna.ui.church

data class SermonSegment(val startMs: Long, val endMs: Long, val text: String)

object SermonCleanup {
    private val fillers = Regex("(?i)\\b(amen somebody|are you with me|praise god|hallelujah|can i get an amen|somebody shout)\\b[,.!? ]*")
    private val repeated = Regex("(?i)\\b(\\w+)(?:\\s+\\1){1,}\\b")
    fun clean(segments: List<SermonSegment>): String = segments.mapNotNull { segment ->
        val cleaned = segment.text.replace(fillers, "").replace(repeated, "$1").trim()
        cleaned.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercase() }?.let { if (it.last() in ".!?") it else "$it." }
    }.joinToString(" ")
}
