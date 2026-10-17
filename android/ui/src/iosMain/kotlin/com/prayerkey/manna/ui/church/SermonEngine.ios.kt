package com.prayerkey.manna.ui.church

import com.prayerkey.manna.data.nowMillis
import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.ui.platform.onMain
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocale
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.Speech.SFSpeechURLRecognitionRequest

actual fun defaultSermonEngine(): SermonEngine = IosSermonEngine

/**
 * Church listening on iPhone: Apple's speech recognition, on the device where
 * the phone supports it, with punctuation, and the service recorded to a file
 * beside it so "Improve this transcript" can read it again.
 *
 * A recognition task is not meant to run for an hour, so the request is ended
 * every 55 seconds and a new one begun, the words of the old one kept - the
 * same rhythm as Android's recogniser, which hands over an utterance at a time.
 * The audio session keeps recording with the screen locked (the app declares
 * background audio).
 */
@OptIn(ExperimentalForeignApi::class)
object IosSermonEngine : SermonEngine {
    private val _listening = MutableStateFlow(false)
    override val listening: StateFlow<Boolean> = _listening.asStateFlow()
    private val _references = MutableStateFlow<List<Caught>>(emptyList())
    override val references: StateFlow<List<Caught>> = _references.asStateFlow()
    private val _partial = MutableStateFlow("")
    override val partial: StateFlow<String> = _partial.asStateFlow()
    private val _chunks = MutableStateFlow<List<String>>(emptyList())
    override val chunks: StateFlow<List<String>> = _chunks.asStateFlow()
    private val _status = MutableStateFlow("Ready")
    override val status: StateFlow<String> = _status.asStateFlow()
    private val _startedAt = MutableStateFlow(0L)
    override val startedAt: StateFlow<Long> = _startedAt.asStateFlow()
    private val _audioPath = MutableStateFlow("")
    override val audioPath: StateFlow<String> = _audioPath.asStateFlow()

    private var language = "en-US"
    private val scope = MainScope()
    private var engine: AVAudioEngine? = null
    private var recognizer: SFSpeechRecognizer? = null
    private var request: SFSpeechAudioBufferRecognitionRequest? = null
    private var task: SFSpeechRecognitionTask? = null
    private var recording: AVAudioFile? = null
    private var recordingPath = ""
    private var rotation: Job? = null
    private var pending = ""

    override fun setLanguage(tag: String) {
        language = tag.ifBlank { "en-US" }
    }

    override fun transcript(context: PlatformContext): String =
        _chunks.value.map(TranscriptTidy::tidy).filter { it.isNotBlank() }.joinToString(" ") { punctuate(it) }

    /** Apple's recogniser has punctuated already; this is the canon and the ending, as on Android after its model. */
    private fun punctuate(text: String): String =
        SermonPunctuation.close(SermonPunctuation.capitalise(text).replaceFirstChar { it.uppercase() })

    override fun elapsedMinutes(): Int {
        val started = _startedAt.value
        if (started == 0L) return 0
        return ((nowMillis() - started) / 60_000L).toInt().coerceAtLeast(1)
    }

    override fun start(context: PlatformContext) {
        if (_listening.value) return
        SFSpeechRecognizer.requestAuthorization { status ->
            onMain {
                if (status == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) begin()
                else _status.value = "Speech recognition is turned off for PrayerKey in Settings"
            }
        }
    }

    private fun sermonsFolder(): String {
        val docs = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String
        return "$docs/sermons".also { NSFileManager.defaultManager.createDirectoryAtPath(it, true, null, null) }
    }

    private fun begin() {
        val speech = SFSpeechRecognizer(locale = NSLocale(localeIdentifier = language))
        if (!speech.isAvailable()) {
            _status.value = "Speech recognition is unavailable right now"
            return
        }
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, mode = AVAudioSessionModeMeasurement, options = AVAudioSessionCategoryOptionDefaultToSpeaker, error = null)
        session.setActive(true, error = null)

        val audio = AVAudioEngine()
        val input = audio.inputNode
        val format = input.outputFormatForBus(0u)
        _startedAt.value = nowMillis()
        recordingPath = "${sermonsFolder()}/sermon-${_startedAt.value}.caf"
        recording = AVAudioFile(forWriting = NSURL.fileURLWithPath(recordingPath), settings = format.settings, error = null)

        input.installTapOnBus(0u, bufferSize = 4096u, format = format) { buffer, _ ->
            if (buffer != null) {
                request?.appendAudioPCMBuffer(buffer)
                recording?.writeFromBuffer(buffer, error = null)
            }
        }
        audio.prepare()
        if (!audio.startAndReturnError(null)) {
            input.removeTapOnBus(0u)
            _status.value = "The microphone could not be started"
            return
        }
        engine = audio
        recognizer = speech
        _chunks.value = emptyList()
        _references.value = emptyList()
        _partial.value = ""
        _audioPath.value = ""
        _listening.value = true
        _status.value = "Listening"
        newTask()
        rotation = scope.launch {
            while (true) {
                delay(55_000)
                // ending the request makes the task deliver its final words; a fresh one follows
                request?.endAudio()
            }
        }
    }

    private fun newTask() {
        val speech = recognizer ?: return
        val next = SFSpeechAudioBufferRecognitionRequest().apply {
            shouldReportPartialResults = true
            if (speech.supportsOnDeviceRecognition) requiresOnDeviceRecognition = true
            addsPunctuation = true
        }
        request = next
        pending = ""
        task = speech.recognitionTaskWithRequest(next) { result, error ->
            onMain {
                if (request !== next) return@onMain
                val text = result?.bestTranscription?.formattedString.orEmpty()
                val done = result?.isFinal() == true || error != null
                if (!done) {
                    pending = text
                    _partial.value = text
                } else {
                    commit(text.ifBlank { pending })
                    if (_listening.value) newTask()
                }
            }
        }
    }

    private fun commit(text: String) {
        pending = ""
        _partial.value = ""
        val words = text.trim()
        if (words.isEmpty()) return
        _chunks.value = _chunks.value + words
        val hits = ReferenceDetector.scan(words)
        val existing = _references.value
        val fresh = hits.map { it.reference }.distinct().filter { ref -> existing.none { it.reference == ref } }
        if (fresh.isNotEmpty()) _references.value = existing + fresh.map { Caught(it, nowMillis()) }
    }

    override fun stop(context: PlatformContext) {
        if (!_listening.value) return
        _listening.value = false
        rotation?.cancel()
        rotation = null
        engine?.inputNode?.removeTapOnBus(0u)
        engine?.stop()
        engine = null
        // the words heard since the last window closed
        commit(pending)
        request?.endAudio()
        task?.finish()
        request = null
        task = null
        recording = null
        _audioPath.value = recordingPath
        _status.value = "Ready"
        AVAudioSession.sharedInstance().setActive(false, error = null)
    }

    override fun reset() {
        _chunks.value = emptyList()
        _references.value = emptyList()
        _partial.value = ""
        _startedAt.value = 0L
        _audioPath.value = ""
    }

    /* Apple's own recogniser re-reads the recording; nothing to download. */
    private val _model = MutableStateFlow<ModelState>(ModelState.Ready)
    override val model: StateFlow<ModelState> = _model.asStateFlow()
    override val modelMegabytes: Int = 0
    override fun refreshModel(context: PlatformContext) = Unit
    override fun isModelInstalled(context: PlatformContext): Boolean = true
    override suspend fun installModel(context: PlatformContext): Boolean = true

    override suspend fun refine(context: PlatformContext, audioPath: String, onProgress: (Float) -> Unit): Result<String> {
        if (!NSFileManager.defaultManager.fileExistsAtPath(audioPath)) return Result.failure(IllegalStateException("the recording is missing"))
        val speech = SFSpeechRecognizer(locale = NSLocale(localeIdentifier = language))
        val url = NSURL.fileURLWithPath(audioPath)
        val file = AVAudioFile(forReading = url, error = null)
        val seconds = file?.let { it.length.toDouble() / it.fileFormat.sampleRate } ?: 0.0
        val request = SFSpeechURLRecognitionRequest(uRL = url).apply {
            shouldReportPartialResults = true
            if (speech.supportsOnDeviceRecognition) requiresOnDeviceRecognition = true
            addsPunctuation = true
        }
        val answer = CompletableDeferred<Result<String>>()
        speech.recognitionTaskWithRequest(request) { result, error ->
            onMain {
                if (error != null && result == null) {
                    answer.complete(Result.failure(IllegalStateException(error.localizedDescription)))
                    return@onMain
                }
                val transcription = result?.bestTranscription ?: return@onMain
                val last = transcription.segments.lastOrNull() as? platform.Speech.SFTranscriptionSegment
                if (seconds > 0 && last != null) onProgress(((last.timestamp + last.duration) / seconds).toFloat().coerceIn(0f, 1f))
                if (result.isFinal()) {
                    onProgress(1f)
                    answer.complete(Result.success(punctuate(TranscriptTidy.tidy(transcription.formattedString))))
                }
            }
        }
        return answer.await()
    }
}
