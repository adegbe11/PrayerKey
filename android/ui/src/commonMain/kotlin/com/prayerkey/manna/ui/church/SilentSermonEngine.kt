package com.prayerkey.manna.ui.church

import com.prayerkey.manna.platform.PlatformContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Hears nothing: the stand-in before a platform installs its engine. */
object SilentSermonEngine : SermonEngine {
    override val listening: StateFlow<Boolean> = MutableStateFlow(false)
    override val references: StateFlow<List<Caught>> = MutableStateFlow(emptyList())
    override val partial: StateFlow<String> = MutableStateFlow("")
    override val chunks: StateFlow<List<String>> = MutableStateFlow(emptyList())
    override val status: StateFlow<String> = MutableStateFlow("Unavailable")
    override val startedAt: StateFlow<Long> = MutableStateFlow(0L)
    override val audioPath: StateFlow<String> = MutableStateFlow("")
    override fun setLanguage(tag: String) = Unit
    override fun transcript(context: PlatformContext): String = ""
    override fun elapsedMinutes(): Int = 0
    override fun start(context: PlatformContext) = Unit
    override fun stop(context: PlatformContext) = Unit
    override fun reset() = Unit
    override val model: StateFlow<ModelState> = MutableStateFlow(ModelState.Absent)
    override val modelMegabytes: Int = 0
    override fun refreshModel(context: PlatformContext) = Unit
    override fun isModelInstalled(context: PlatformContext): Boolean = false
    override suspend fun installModel(context: PlatformContext): Boolean = false
    override suspend fun refine(context: PlatformContext, audioPath: String, onProgress: (Float) -> Unit): Result<String> =
        Result.failure(IllegalStateException("listening is unavailable"))
}
