package com.prayerkey.manna.ui.church

import android.content.Context
import com.k2fsa.sherpa.onnx.OnlinePunctuation
import com.k2fsa.sherpa.onnx.OnlinePunctuationConfig
import com.k2fsa.sherpa.onnx.OnlinePunctuationModelConfig

/**
 * Real punctuation, from a model, on the device.
 *
 * [SermonPunctuation] does what can be decided from the words alone —
 * capitalise the names, capitalise "I", a question mark after an interrogative
 * opening. It cannot place a comma inside a clause, because nothing in the
 * spelling of the words says where the clause is. That needs a model, and this
 * is it: a CNN-BiLSTM trained on punctuated English, run through sherpa-onnx.
 *
 *     and jesus said unto peter i am the way the truth and the life
 *  →  And Jesus said unto Peter, I am the way, the truth, and the life.
 *
 * **It ships inside the app.** The int8 model is 7.5 MB, which is small enough
 * to sit in assets beside the Bible, so there is no download, no install
 * screen, and no first-Sunday-without-signal problem. sherpa-onnx reads it
 * straight out of the APK through the AssetManager — it is never copied to
 * storage, so it costs nothing on disk either.
 *
 * The rule-based pass still runs afterwards. The model punctuates and does not
 * know the difference between "peter" the name and a word; the rules know the
 * canon. Each does the half the other cannot.
 *
 * If anything about the native library or the model fails — an ABI we did not
 * ship, a corrupted read — [punctuate] returns the rule-based result rather
 * than throwing. A transcript with plain punctuation is a small loss; a Church
 * screen that crashes at the end of a service is not recoverable.
 */
object SermonPunctuator {

    private const val MODEL = "punct/model.onnx"
    private const val VOCAB = "punct/bpe.vocab"

    @Volatile private var engine: OnlinePunctuation? = null
    @Volatile private var unavailable = false

    /** Whether the model loaded. False means the rules are carrying it alone. */
    val isReady: Boolean get() = engine != null

    /**
     * Loads the model.
     *
     * Worth calling when Church opens rather than at the end of a service:
     * construction reads 7.5 MB and builds the graph, and doing that while the
     * user is waiting for their notes is the one moment it would be felt.
     */
    fun prepare(context: Context) {
        if (engine != null || unavailable) return
        synchronized(this) {
            if (engine != null || unavailable) return
            runCatching {
                OnlinePunctuation(
                    assetManager = context.applicationContext.assets,
                    config = OnlinePunctuationConfig(
                        model = OnlinePunctuationModelConfig(
                            cnnBilstm = MODEL,
                            bpeVocab = VOCAB,
                            numThreads = 1,
                        ),
                    ),
                )
            }.onSuccess { engine = it }.onFailure { unavailable = true }
        }
    }

    /**
     * Punctuates one utterance, model first, rules after.
     *
     * The model is given the raw lowercase words — it was trained on exactly
     * that, and feeding it text the rules have already capitalised makes it
     * worse, not better.
     */
    fun punctuate(context: Context, utterance: String): String {
        val raw = utterance.trim()
        if (raw.isEmpty()) return ""
        prepare(context)
        val model = engine ?: return SermonPunctuation.polish(raw)
        val punctuated = runCatching { model.addPunctuation(raw) }.getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: return SermonPunctuation.polish(raw)
        /* Only the name pass afterwards. The rest of polish() lowercases and
           adds its own comma, which would throw away what the model just did. */
        return SermonPunctuation.close(
            SermonPunctuation.capitalise(punctuated).replaceFirstChar { it.uppercase() },
        )
    }
}
