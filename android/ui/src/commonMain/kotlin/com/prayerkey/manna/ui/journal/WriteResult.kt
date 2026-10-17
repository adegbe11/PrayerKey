package com.prayerkey.manna.ui.journal

/** What the write flow hands back: one journal page, ready to keep. */
data class WriteResult(
    val mood: String,
    val body: String,
    val gratitude: String,
    val verseRef: String?,
    val verseText: String?,
    val source: String,
    val isPrayer: Boolean,
    val title: String = "",
    val tags: List<String> = emptyList(),
    val journal: String = "My Journey",
    val favorite: Boolean = false,
    val location: String = "",
    val weather: String = "",
    val media: List<String> = emptyList(),
    val entryAt: Long = com.prayerkey.manna.data.nowMillis(),
)
