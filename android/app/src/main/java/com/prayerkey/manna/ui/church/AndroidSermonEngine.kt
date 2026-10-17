package com.prayerkey.manna.ui.church

import com.prayerkey.manna.platform.PlatformContext
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/** The shared Church screen's listening, done as Android always has: SermonService, sherpa, the refiner. */
object AndroidSermonEngine : SermonEngine {
    override val listening: StateFlow<Boolean> get() = SermonService.listening
    override val references: StateFlow<List<Caught>> get() = SermonService.references
    override val partial: StateFlow<String> get() = SermonService.partial
    override val chunks: StateFlow<List<String>> get() = SermonService.chunks
    override val status: StateFlow<String> get() = SermonService.status
    override val startedAt: StateFlow<Long> get() = SermonService.startedAt
    override val audioPath: StateFlow<String> get() = SermonService.audioPath

    override fun setLanguage(tag: String) = SermonService.setLanguage(tag)
    override fun transcript(context: PlatformContext): String = SermonService.transcript(context)
    override fun elapsedMinutes(): Int = SermonService.elapsedMinutes()
    override fun start(context: PlatformContext) = SermonService.start(context)
    override fun stop(context: PlatformContext) = SermonService.stop(context)
    override fun reset() = SermonService.reset()

    override val model: StateFlow<ModelState> get() = SpeechModel.state(SpeechModel.Kind.Speech)
    override val modelMegabytes: Int get() = SpeechModel.Kind.Speech.megabytes
    override fun refreshModel(context: PlatformContext) = SpeechModel.refresh(context, SpeechModel.Kind.Speech)
    override fun isModelInstalled(context: PlatformContext): Boolean = SpeechModel.isInstalled(context, SpeechModel.Kind.Speech)
    override suspend fun installModel(context: PlatformContext): Boolean = SpeechModel.install(context, SpeechModel.Kind.Speech)

    override suspend fun refine(context: PlatformContext, audioPath: String, onProgress: (Float) -> Unit): Result<String> =
        SermonRefiner.refine(context, File(audioPath), onProgress)
}
