package com.prayerkey.manna.ui.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus

/** One dictation at a time, drawn by [DictationPanel] from the root overlay. */
@OptIn(ExperimentalForeignApi::class)
internal object IosDictation {
    var prompt by mutableStateOf<String?>(null)
    var heard by mutableStateOf("")

    private var engine: AVAudioEngine? = null
    private var request: SFSpeechAudioBufferRecognitionRequest? = null
    private var task: SFSpeechRecognitionTask? = null
    private var deliver: (String) -> Unit = {}
    private var quiet: Job? = null
    private val scope = MainScope()

    fun begin(text: String, onResult: (String) -> Unit) {
        if (prompt != null) return
        deliver = onResult
        SFSpeechRecognizer.requestAuthorization { status ->
            onMain {
                if (status != SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized) {
                    showToast(com.prayerkey.manna.platform.AppContext, "Turn on Speech Recognition for PrayerKey in Settings", long = true)
                    return@onMain
                }
                listen(text)
            }
        }
    }

    private fun listen(text: String) {
        val speech = SFSpeechRecognizer()
        if (!speech.isAvailable()) {
            showToast(com.prayerkey.manna.platform.AppContext, "Dictation is unavailable right now")
            return
        }
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, mode = AVAudioSessionModeMeasurement, options = AVAudioSessionCategoryOptionDefaultToSpeaker, error = null)
        session.setActive(true, error = null)
        val audio = AVAudioEngine()
        val input = audio.inputNode
        val next = SFSpeechAudioBufferRecognitionRequest().apply {
            shouldReportPartialResults = true
            addsPunctuation = true
            if (speech.supportsOnDeviceRecognition) requiresOnDeviceRecognition = true
        }
        input.installTapOnBus(0u, bufferSize = 4096u, format = input.outputFormatForBus(0u)) { buffer, _ ->
            if (buffer != null) next.appendAudioPCMBuffer(buffer)
        }
        audio.prepare()
        if (!audio.startAndReturnError(null)) {
            input.removeTapOnBus(0u)
            return
        }
        engine = audio
        request = next
        heard = ""
        prompt = text
        task = speech.recognitionTaskWithRequest(next) { result, error ->
            onMain {
                result?.bestTranscription?.formattedString?.let { heard = it }
                // a pause of two and a half seconds ends it, as Android's dialog does
                quiet?.cancel()
                quiet = scope.launch { delay(2_500); finish() }
                if (result?.isFinal() == true || error != null) finish()
            }
        }
    }

    fun finish() {
        if (prompt == null) return
        quiet?.cancel()
        engine?.inputNode?.removeTapOnBus(0u)
        engine?.stop()
        request?.endAudio()
        task?.finish()
        engine = null; request = null; task = null
        prompt = null
        AVAudioSession.sharedInstance().setActive(false, error = null)
        val words = heard.trim()
        heard = ""
        if (words.isNotEmpty()) deliver(words)
    }
}

@Composable
actual fun rememberDictation(prompt: String, onResult: (String) -> Unit): Launcher {
    val callback = rememberUpdatedState(onResult)
    return remember(prompt) { Launcher { IosDictation.begin(prompt) { callback.value(it) } } }
}

/** The listening panel: the prompt, the words so far, and Done. */
@Composable
internal fun DictationPanel() {
    val prompt = IosDictation.prompt ?: return
    Box(Modifier.fillMaxSize().background(Color(0x66000000)), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF1C1C1E)).navigationBarsPadding().padding(24.dp),
        ) {
            Text(prompt, color = Color.White.copy(alpha = .6f), fontSize = 14.sp)
            Text(
                IosDictation.heard.ifBlank { "Listening…" },
                color = Color.White, fontSize = 20.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
            )
            Text(
                "Done", color = Color(0xFFB794FF), fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.End).clip(RoundedCornerShape(12.dp))
                    .clickable { IosDictation.finish() }.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}
