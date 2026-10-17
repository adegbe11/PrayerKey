package com.prayerkey.manna.ui.church

import com.prayerkey.manna.platform.PlatformContext
import kotlinx.coroutines.flow.StateFlow

/**
 * Church listening, as the screen sees it: a live transcript, the scriptures
 * caught in it, and a recording kept so the transcript can be redone better.
 *
 * Android runs its foreground SermonService with the system recogniser and the
 * offline sherpa model (installed by the app at launch). iPhone uses Apple's
 * on-device speech recognition, which needs no download.
 */
interface SermonEngine {
    val listening: StateFlow<Boolean>
    val references: StateFlow<List<Caught>>
    val partial: StateFlow<String>
    val chunks: StateFlow<List<String>>
    val status: StateFlow<String>
    val startedAt: StateFlow<Long>
    val audioPath: StateFlow<String>

    /** Set before [start]; the recogniser reads it when it spins up. */
    fun setLanguage(tag: String)
    fun transcript(context: PlatformContext): String
    fun elapsedMinutes(): Int
    fun start(context: PlatformContext)
    fun stop(context: PlatformContext)
    /** Clears the finished session once its note has been saved. */
    fun reset()

    /* The sharper model that re-reads a recording. Android downloads it once; iPhone has its own. */
    val model: StateFlow<ModelState>
    val modelMegabytes: Int
    fun refreshModel(context: PlatformContext)
    fun isModelInstalled(context: PlatformContext): Boolean
    suspend fun installModel(context: PlatformContext): Boolean

    /** Re-transcribes the recording at [audioPath]; [onProgress] is how far through it is, 0 to 1. */
    suspend fun refine(context: PlatformContext, audioPath: String, onProgress: (Float) -> Unit): Result<String>
}

data class Caught(val reference: String, val atMillis: Long)

sealed interface ModelState {
    data object Absent : ModelState
    data class Downloading(val fraction: Float) : ModelState
    data object Unpacking : ModelState
    data object Ready : ModelState
    data class Failed(val reason: String) : ModelState
}

object Sermons {
    var engine: SermonEngine = defaultSermonEngine()
}

expect fun defaultSermonEngine(): SermonEngine

/** What every transcript loses before it is kept: the call-and-response, and stammered repeats. */
object TranscriptTidy {
    private val fillers = Regex("(?i)\\b(amen somebody|are you with me|praise god|hallelujah|can i get an amen|somebody shout)\\b[,.!? ]*")
    private val repeated = Regex("(?i)\\b(\\w+)(?:\\s+\\1){1,}\\b")

    fun tidy(text: String): String = text.replace(fillers, "").replace(repeated, "$1").trim()
}
