package com.prayerkey.manna.ui.church

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * The speech models that make transcription work, on the device, for free.
 *
 * Church recorded audio and transcribed nothing. [SermonCaptureEngine] already
 * had a complete offline path — it opened `filesDir/vosk-model` and returned
 * quietly when the folder was absent — but **nothing in the app ever created
 * that folder**, so the Vosk dependency shipped in every build and could never
 * initialise. This is the missing half.
 *
 * There are two models rather than one, because live captioning and a good
 * transcript are different problems:
 *
 *  - [Live] runs during the sermon, keeping up with speech in real time on a
 *    phone. It is 39 MB and it is small enough that its accuracy shows it,
 *    especially in a reverberant room at a distance from the speaker.
 *  - [Studio] is three times the size and correspondingly better, far too slow
 *    to caption live but perfectly happy running afterwards against the audio
 *    we already keep. See [SermonRefiner].
 *
 * Both are Apache 2.0 from the Vosk project's own host: no key, no account, no
 * server, and no network at all once installed — which matters twice, because
 * it is the only arrangement that is genuinely free and the only one that works
 * in a building with no signal, which is where churches tend to be.
 *
 * https://alphacephei.com/vosk/models
 */
object VoskModel {

    /**
     * A model that can be installed.
     *
     * [expectedBytes] is the published size, checked against what actually
     * arrived so a dropped connection is rejected rather than unpacked into a
     * half model that looks installed and crashes on use.
     */
    enum class Kind(
        internal val url: String,
        internal val folder: String,
        internal val expectedBytes: Long,
        val megabytes: Int,
    ) {
        /** vosk-model-small-en-us-0.15 — real time, modest accuracy. */
        Live(
            "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
            "vosk-model",
            41_205_931L,
            39,
        ),

        /** vosk-model-en-us-0.22-lgraph — too slow to caption, much better. */
        Studio(
            "https://alphacephei.com/vosk/models/vosk-model-en-us-0.22-lgraph.zip",
            "vosk-model-studio",
            130_557_655L,
            125,
        ),
    }

    sealed interface State {
        data object Absent : State
        data class Downloading(val fraction: Float) : State
        data object Unpacking : State
        data object Ready : State
        data class Failed(val reason: String) : State
    }

    private val states = Kind.entries.associateWith { MutableStateFlow<State>(State.Absent) }

    fun state(kind: Kind): StateFlow<State> = states.getValue(kind).asStateFlow()

    /** Where an unpacked model lives. */
    fun directory(context: Context, kind: Kind) =
        File(context.applicationContext.filesDir, kind.folder)

    /**
     * Whether a *usable* model is present.
     *
     * Checks for the subfolders Vosk actually reads rather than just that the
     * directory exists — an interrupted unzip leaves a folder behind, and
     * `Model()` on a partial folder dies in native code where it cannot be
     * caught.
     */
    fun isInstalled(context: Context, kind: Kind): Boolean {
        val dir = directory(context, kind)
        return dir.isDirectory && File(dir, "am").isDirectory && File(dir, "conf").isDirectory
    }

    fun refresh(context: Context, kind: Kind) {
        states.getValue(kind).value = if (isInstalled(context, kind)) State.Ready else State.Absent
    }

    /**
     * Fetches and unpacks a model. Safe to call when it is already present.
     *
     * Downloads to a temporary file, unpacks to a staging directory, and swaps
     * it into place last, so an interrupted install never leaves a broken model
     * where [isInstalled] would find it.
     */
    suspend fun install(context: Context, kind: Kind): Boolean = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        val state = states.getValue(kind)
        if (isInstalled(app, kind)) { state.value = State.Ready; return@withContext true }

        val target = directory(app, kind)
        val zip = File(app.cacheDir, "${kind.folder}.zip.part")
        val staging = File(app.filesDir, "${kind.folder}.staging")

        runCatching {
            staging.deleteRecursively()
            zip.delete()

            state.value = State.Downloading(0f)
            val connection = (URL(kind.url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 60_000
                instanceFollowRedirects = true
            }
            connection.inputStream.use { input ->
                val total = connection.contentLengthLong.takeIf { it > 0 } ?: kind.expectedBytes
                zip.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var written = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        written += read
                        state.value = State.Downloading((written.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
            connection.disconnect()

            if (zip.length() < kind.expectedBytes * 9 / 10) {
                error("the download was cut short at ${zip.length() / 1_048_576} MB")
            }

            state.value = State.Unpacking
            unzip(zip, staging)

            /* The archive holds one folder named for the model version. Find
               the one containing `am` and `conf` rather than assuming the name,
               so the next release does not silently install nothing. */
            val root = findModelRoot(staging) ?: error("the archive did not contain a model")

            target.deleteRecursively()
            if (!root.renameTo(target)) {
                root.copyRecursively(target, overwrite = true)
                root.deleteRecursively()
            }

            require(isInstalled(app, kind)) { "the unpacked model was incomplete" }
            true
        }.onSuccess {
            state.value = State.Ready
        }.onFailure { failure ->
            target.deleteRecursively()
            state.value = State.Failed(failure.message ?: "the download failed")
        }.also {
            zip.delete()
            staging.deleteRecursively()
        }.getOrDefault(false)
    }

    fun remove(context: Context, kind: Kind) {
        directory(context, kind).deleteRecursively()
        states.getValue(kind).value = State.Absent
    }

    internal fun findModelRoot(from: File): File? {
        if (File(from, "am").isDirectory && File(from, "conf").isDirectory) return from
        return from.listFiles()?.filter { it.isDirectory }?.firstNotNullOfOrNull { findModelRoot(it) }
    }

    /**
     * Unpacks the archive.
     *
     * Every entry's resolved path is checked to be inside [into] before it is
     * written. An entry named `../../databases/x` would otherwise be written
     * outside the target — the Zip Slip vulnerability. These archives are
     * trusted; an unpacker that only works on trusted input is one refactor
     * away from being a hole.
     */
    internal fun unzip(archive: File, into: File) {
        into.mkdirs()
        val root = into.canonicalPath + File.separator
        ZipInputStream(archive.inputStream().buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val out = File(into, entry.name)
                if (!out.canonicalPath.startsWith(root)) {
                    error("archive entry escapes the target directory: ${entry.name}")
                }
                if (entry.isDirectory) {
                    out.mkdirs()
                } else {
                    out.parentFile?.mkdirs()
                    out.outputStream().use { zip.copyTo(it, 64 * 1024) }
                }
                zip.closeEntry()
            }
        }
    }
}
