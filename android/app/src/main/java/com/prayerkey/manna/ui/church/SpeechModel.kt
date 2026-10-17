package com.prayerkey.manna.ui.church

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * The speech model that turns a sermon into words, on the device, for free.
 *
 * This replaces the Vosk model, for a reason that has nothing to do with
 * accuracy. Android 15 requires native libraries to be aligned for 16 KB
 * memory pages, and `libvosk.so` is built for 4 KB:
 *
 *     libandroidx.graphics.path.so   16384  OK
 *     libsherpa-onnx-jni.so          16384  OK
 *     libvosk.so                      4096  NOT
 *
 * On a 16 KB device — every Pixel from the 8 onward — that means the app may
 * fail to install, or install and crash. Vosk's Android build is from 2022 and
 * 0.3.47 is the last one published, so there was no version to upgrade to.
 *
 * sherpa-onnx was already in the app for punctuation and its library is
 * correctly aligned, so recognition moves onto the library that was already
 * there and the unaligned one leaves the build entirely.
 *
 * One model instead of two. It is 57 MB against Vosk's 41 MB for the live
 * model, and it replaces the 125 MB second model as well: [SermonRefiner] now
 * re-reads the recording with the same model but no clock running, which is a
 * real improvement — a live pass has to keep up with speech and drops what it
 * cannot — without a second download.
 *
 * Apache 2.0, from the project's own release host. No key, no account, no
 * server, and no network at all once installed.
 */
object SpeechModel {

    /**
     * The model.
     *
     * [expectedBytes] is the published size, checked against what arrived so a
     * dropped connection is rejected rather than unpacked into a half model
     * that looks installed and dies in native code.
     */
    enum class Kind(
        internal val url: String,
        internal val folder: String,
        internal val expectedBytes: Long,
        val megabytes: Int,
    ) {
        Speech(
            "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/" +
                "sherpa-onnx-streaming-zipformer-en-kroko-2025-08-06.tar.bz2",
            "speech-model",
            57_267_600L,
            57,
        ),
    }

    /* The model's state is the shared ModelState (SermonEngine.kt). */

    private val states = Kind.entries.associateWith { MutableStateFlow<ModelState>(ModelState.Absent) }

    fun state(kind: Kind): StateFlow<ModelState> = states.getValue(kind).asStateFlow()

    fun directory(context: Context, kind: Kind) =
        File(context.applicationContext.filesDir, kind.folder)

    /**
     * Whether a *usable* model is present.
     *
     * Checks for the four files the recogniser opens rather than just that the
     * folder exists — an interrupted unpack leaves a directory behind, and
     * building a recogniser from a partial one dies in native code where it
     * cannot be caught.
     */
    fun isInstalled(context: Context, kind: Kind): Boolean {
        val dir = directory(context, kind)
        return dir.isDirectory && FILES.all { File(dir, it).length() > 0 }
    }

    fun refresh(context: Context, kind: Kind) {
        states.getValue(kind).value = if (isInstalled(context, kind)) ModelState.Ready else ModelState.Absent
    }

    /**
     * Fetches and unpacks the model. Safe to call when it is already present.
     *
     * Downloads to a temporary file, unpacks to a staging directory, and swaps
     * it into place last, so an interrupted install never leaves a broken model
     * where [isInstalled] would find it.
     */
    suspend fun install(context: Context, kind: Kind): Boolean = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        val state = states.getValue(kind)
        if (isInstalled(app, kind)) { state.value = ModelState.Ready; return@withContext true }

        val target = directory(app, kind)
        val archive = File(app.cacheDir, "${kind.folder}.tar.bz2.part")
        val staging = File(app.filesDir, "${kind.folder}.staging")

        runCatching {
            staging.deleteRecursively()
            archive.delete()

            state.value = ModelState.Downloading(0f)
            val connection = (URL(kind.url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 60_000
                instanceFollowRedirects = true
            }
            connection.inputStream.use { input ->
                val total = connection.contentLengthLong.takeIf { it > 0 } ?: kind.expectedBytes
                archive.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var written = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        written += read
                        state.value = ModelState.Downloading((written.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
            connection.disconnect()

            if (archive.length() < kind.expectedBytes * 9 / 10) {
                error("the download was cut short at ${archive.length() / 1_048_576} MB")
            }

            state.value = ModelState.Unpacking
            untar(archive, staging)

            /* The archive holds one folder named for the model version. Find
               the one holding the four files rather than assuming the name, so
               the next release does not silently install nothing. */
            val root = findRoot(staging) ?: error("the archive did not contain a model")

            target.deleteRecursively()
            if (!root.renameTo(target)) {
                root.copyRecursively(target, overwrite = true)
                root.deleteRecursively()
            }

            require(isInstalled(app, kind)) { "the unpacked model was incomplete" }
            true
        }.onSuccess {
            state.value = ModelState.Ready
        }.onFailure { failure ->
            target.deleteRecursively()
            state.value = ModelState.Failed(failure.message ?: "the download did not finish")
        }.also {
            archive.delete()
            staging.deleteRecursively()
        }.getOrDefault(false)
    }

    fun remove(context: Context, kind: Kind) {
        directory(context, kind).deleteRecursively()
        states.getValue(kind).value = ModelState.Absent
    }

    internal val FILES = listOf("encoder.onnx", "decoder.onnx", "joiner.onnx", "tokens.txt")

    internal fun findRoot(from: File): File? {
        if (FILES.all { File(from, it).length() > 0 }) return from
        return from.listFiles()?.filter { it.isDirectory }?.firstNotNullOfOrNull { findRoot(it) }
    }

    /**
     * Unpacks the archive.
     *
     * Every entry's resolved path is checked to be inside [into] before it is
     * written. An entry named `../../databases/x` would otherwise be written
     * outside the target — the Zip Slip vulnerability, which tar has too. This
     * archive is trusted; an unpacker that only works on trusted input is one
     * refactor away from being a hole.
     */
    internal fun untar(archive: File, into: File) {
        into.mkdirs()
        val root = into.canonicalPath + File.separator
        TarArchiveInputStream(BZip2CompressorInputStream(archive.inputStream().buffered())).use { tar ->
            while (true) {
                val entry = tar.nextEntry ?: break
                val out = File(into, entry.name)
                /* Loud, not quiet. Skipping silently would unpack a
                   tampered archive minus the dangerous entries and report
                   success, which is how a hole survives a code review. */
                check(out.canonicalPath.startsWith(root)) {
                    "the archive tried to write outside the target: ${entry.name}"
                }
                if (entry.isDirectory) { out.mkdirs(); continue }
                out.parentFile?.mkdirs()
                out.outputStream().use { tar.copyTo(it, 64 * 1024) }
            }
        }
    }
}
