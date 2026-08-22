package com.prayerkey.manna.ui.church

data class SermonSegment(val startMs: Long, val endMs: Long, val text: String)

object SermonCleanup {
    private val fillers = Regex("(?i)\\b(amen somebody|are you with me|praise god|hallelujah|can i get an amen|somebody shout)\\b[,.!? ]*")
    private val repeated = Regex("(?i)\\b(\\w+)(?:\\s+\\1){1,}\\b")
    /**
     * Strip the call-and-response, then punctuate.
     *
     * Capitalising the first letter and adding a full stop was all this did,
     * which left every proper noun and every "I" in lower case — the plainest
     * possible tell that a machine wrote it. [SermonPunctuation] handles the
     * inside of the sentence.
     */
    fun clean(context: android.content.Context, segments: List<SermonSegment>): String = segments.mapNotNull { segment ->
        val cleaned = segment.text.replace(fillers, "").replace(repeated, "$1").trim()
        cleaned.takeIf { it.isNotBlank() }?.let { SermonPunctuator.punctuate(context, it) }
    }.joinToString(" ")
}
