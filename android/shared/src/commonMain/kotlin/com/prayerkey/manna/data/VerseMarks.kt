package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * What a reader leaves in the margin: a verse coloured in, a note written
 * beside it, a ribbon laid in it. One record per verse, so a verse can be
 * gold, carry a note and be bookmarked all at once, the way a paper Bible is
 * marked.
 */
@Serializable
data class VerseMark(
    /** "John 3:16" */
    val ref: String,
    val text: String,
    /** 0 for none, otherwise an index into the reader's five colours. */
    val color: Int = 0,
    val note: String = "",
    val bookmark: Boolean = false,
    val at: Long = 0,
) {
    val book: String get() = ref.substringBeforeLast(' ')
    val chapter: Int get() = ref.substringAfterLast(' ').substringBefore(':').toIntOrNull() ?: 1
    val verse: Int get() = ref.substringAfterLast(':').toIntOrNull() ?: 1
    val isEmpty: Boolean get() = color == 0 && note.isBlank() && !bookmark
}

object VerseMarks {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(VerseMark.serializer())
    private val _all = MutableStateFlow<List<VerseMark>>(emptyList())
    /** Newest first. */
    val all: StateFlow<List<VerseMark>> = _all.asStateFlow()
    private var loaded = false

    fun load(context: PlatformContext): List<VerseMark> {
        if (loaded) return _all.value
        val raw = context.keyValues("verse_marks").getString("all", "").orEmpty()
        _all.value = runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyList())
        loaded = true
        return _all.value
    }

    fun of(ref: String): VerseMark? = _all.value.firstOrNull { it.ref == ref }

    /** The marks in one chapter, by verse number. */
    fun inChapter(book: String, chapter: Int): Map<Int, VerseMark> =
        _all.value.filter { it.book == book && it.chapter == chapter }.associateBy { it.verse }

    /** Changes one verse's mark; a mark with nothing left on it is removed. */
    fun update(context: PlatformContext, ref: String, text: String, now: Long, change: (VerseMark) -> VerseMark) {
        load(context)
        val before = of(ref) ?: VerseMark(ref = ref, text = text)
        val after = change(before).copy(at = now, text = before.text.ifBlank { text })
        val rest = _all.value.filterNot { it.ref == ref }
        _all.value = if (after.isEmpty) rest else listOf(after) + rest
        context.keyValues("verse_marks").edit().putString("all", json.encodeToString(serializer, _all.value)).apply()
    }
}
