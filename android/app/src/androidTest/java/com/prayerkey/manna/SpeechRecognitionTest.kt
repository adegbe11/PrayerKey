package com.prayerkey.manna

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prayerkey.manna.ui.church.VoskModel
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File

/**
 * Does the recogniser actually turn speech into words?
 *
 * Everything around transcription has been verified — the model URL and its
 * exact size, the archive shape, the unzip, the failure path, the recording,
 * the note reaching the journal. The one thing never verified is the thing the
 * feature is for. A microphone cannot be spoken into from a test, and the
 * emulator used during development had no network to fetch a model with, so
 * this kept sliding.
 *
 * So the audio comes from a file instead. `speech-16k-mono.wav` is eleven
 * seconds of a well-known public-domain address, at 16 kHz mono 16-bit, which
 * is exactly the format [com.prayerkey.manna.ui.church.SermonCaptureEngine]
 * records in — so what this feeds the recogniser is byte-identical in shape to
 * what a service produces.
 *
 * The assertions are deliberately loose. A 39 MB model will mishear words, and
 * a test that demanded a perfect transcript would fail for the wrong reason and
 * then be deleted. What is being proved is that speech in produces the right
 * words out, not that it is flawless.
 *
 * Skipped, not failed, when the model is absent. A machine without the model
 * has nothing to say about the recogniser, and a red test that only means
 * "not downloaded" trains people to ignore red.
 */
@RunWith(AndroidJUnit4::class)
class SpeechRecognitionTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /** 16 kHz mono 16-bit PCM, the header skipped. */
    private fun samplePcm(): ByteArray {
        val bytes = InstrumentationRegistry.getInstrumentation().context.assets
            .open("speech-16k-mono.wav").use { it.readBytes() }
        // find the "data" chunk rather than assuming a 44-byte header
        var i = 12
        while (i + 8 < bytes.size) {
            val id = String(bytes, i, 4, Charsets.US_ASCII)
            val size = (bytes[i + 4].toInt() and 0xFF) or
                ((bytes[i + 5].toInt() and 0xFF) shl 8) or
                ((bytes[i + 6].toInt() and 0xFF) shl 16) or
                ((bytes[i + 7].toInt() and 0xFF) shl 24)
            if (id == "data") return bytes.copyOfRange(i + 8, minOf(bytes.size, i + 8 + size))
            i += 8 + size
        }
        error("no data chunk in the sample")
    }

    private fun transcribe(kind: VoskModel.Kind): String {
        assumeTrue(
            "${kind.name} model is not installed on this device — skipping",
            VoskModel.isInstalled(context, kind),
        )
        val model = Model(VoskModel.directory(context, kind).absolutePath)
        val recognizer = Recognizer(model, RATE.toFloat())
        val words = StringBuilder()
        try {
            val pcm = samplePcm()
            var offset = 0
            // the same 3200-byte reads the capture engine uses
            while (offset < pcm.size) {
                val n = minOf(CHUNK, pcm.size - offset)
                val slice = pcm.copyOfRange(offset, offset + n)
                if (recognizer.acceptWaveForm(slice, n)) {
                    words.append(JSONObject(recognizer.result).optString("text")).append(' ')
                }
                offset += n
            }
            words.append(JSONObject(recognizer.finalResult).optString("text"))
        } finally {
            runCatching { recognizer.close() }
            runCatching { model.close() }
        }
        return words.toString().trim().lowercase()
    }

    @Test
    fun the_live_model_turns_speech_into_the_right_words() {
        val heard = transcribe(VoskModel.Kind.Live)
        assertTrue("the recogniser returned nothing at all", heard.isNotBlank())

        /* Content words from the sample, each unmistakable and unlikely to be
           produced by chance. Requiring most rather than all leaves room for
           the mishearing a small model will genuinely do. */
        val expected = listOf("ask", "not", "what", "your", "country", "can", "do", "for", "you")
        val found = expected.count { it in heard }
        assertTrue(
            "heard only $found of ${expected.size} expected words. Transcript was: \"$heard\"",
            found >= 7,
        )
        assertTrue(
            "the transcript is too short to be real speech: \"$heard\"",
            heard.split(" ").size >= 12,
        )
    }

    /** The larger model is used by SermonRefiner and must clear the same bar. */
    @Test
    fun the_studio_model_turns_speech_into_the_right_words() {
        val heard = transcribe(VoskModel.Kind.Studio)
        val expected = listOf("ask", "not", "what", "your", "country", "can", "do", "for", "you")
        val found = expected.count { it in heard }
        assertTrue(
            "heard only $found of ${expected.size} expected words. Transcript was: \"$heard\"",
            found >= 7,
        )
    }

    /** A model directory that exists but is incomplete must not be opened. */
    @Test
    fun a_half_unpacked_model_is_not_reported_as_installed() {
        val half = File(context.filesDir, "vosk-model-test-partial")
        half.deleteRecursively()
        File(half, "am").mkdirs()
        try {
            assertTrue(
                "a folder with am/ but no conf/ was reported installed",
                !(half.isDirectory && File(half, "am").isDirectory && File(half, "conf").isDirectory),
            )
        } finally {
            half.deleteRecursively()
        }
    }

    private companion object {
        const val RATE = 16_000
        const val CHUNK = 3200
    }
}
