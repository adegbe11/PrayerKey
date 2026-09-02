package com.prayerkey.manna.ui.church

import com.k2fsa.sherpa.onnx.EndpointConfig
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import java.io.File

/**
 * Speech to text, streaming, on the device.
 *
 * A thin wrapper so the capture engine and the refiner talk to one thing. It
 * hands back finished utterances the way the old recogniser did — at the
 * natural pauses the model detects — because everything downstream, the
 * segments, the punctuation, the arranging, is built around an utterance at a
 * time rather than a growing block of text.
 *
 * Sixteen-bit PCM in, because that is what AudioRecord gives us; sherpa wants
 * floats between -1 and 1, so the conversion happens here rather than in three
 * callers.
 */
class SpeechEngine private constructor(
    private val recognizer: OnlineRecognizer,
    private var stream: OnlineStream,
) {

    /**
     * Feeds audio and returns a finished utterance, or null.
     *
     * Null is the normal case — most buffers are the middle of a sentence.
     * [partial] is what has been heard so far in the utterance in flight, for
     * the live caption only; it is replaced, never accumulated.
     */
    fun accept(pcm: ByteArray, count: Int, partial: (String) -> Unit): String? {
        stream.acceptWaveform(toFloats(pcm, count), SAMPLE_RATE)
        while (recognizer.isReady(stream)) recognizer.decode(stream)

        val text = recognizer.getResult(stream).text.trim()
        if (!recognizer.isEndpoint(stream)) {
            if (text.isNotEmpty()) partial(text)
            return null
        }

        /* An endpoint with nothing in it is a pause in silence, not a
           sentence. Reset and say nothing rather than emitting a blank
           segment that later reads as a gap in the sermon. */
        recognizer.reset(stream)
        partial("")
        return text.ifEmpty { null }
    }

    /** Whatever is left when the recording stops. */
    fun finish(): String? {
        stream.inputFinished()
        while (recognizer.isReady(stream)) recognizer.decode(stream)
        return recognizer.getResult(stream).text.trim().ifEmpty { null }
    }

    fun release() {
        runCatching { stream.release() }
        runCatching { recognizer.release() }
    }

    private fun toFloats(pcm: ByteArray, count: Int): FloatArray {
        val samples = FloatArray(count / 2)
        for (i in samples.indices) {
            val low = pcm[i * 2].toInt() and 0xFF
            val high = pcm[i * 2 + 1].toInt()
            samples[i] = ((high shl 8) or low) / 32768f
        }
        return samples
    }

    companion object {
        const val SAMPLE_RATE = 16_000

        /**
         * Opens the recogniser, or returns null when the model is absent or
         * unreadable.
         *
         * Never throws. A church screen that crashes at the start of a service
         * is not recoverable; one that records without captions is.
         */
        fun open(modelDir: File): SpeechEngine? = runCatching {
            val config = OnlineRecognizerConfig(
                featConfig = FeatureConfig(sampleRate = SAMPLE_RATE, featureDim = 80),
                modelConfig = OnlineModelConfig(
                    transducer = OnlineTransducerModelConfig(
                        encoder = File(modelDir, "encoder.onnx").absolutePath,
                        decoder = File(modelDir, "decoder.onnx").absolutePath,
                        joiner = File(modelDir, "joiner.onnx").absolutePath,
                    ),
                    tokens = File(modelDir, "tokens.txt").absolutePath,
                    modelType = "zipformer2",
                    numThreads = 2,
                ),
                endpointConfig = EndpointConfig(),
                enableEndpoint = true,
            )
            val recognizer = OnlineRecognizer(config = config)
            SpeechEngine(recognizer, recognizer.createStream())
        }.getOrNull()
    }
}
