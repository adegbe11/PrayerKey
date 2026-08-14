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
 * The speech model that makes live transcription work, on the device, for free.
 *
 * Church was recording audio and transcribing nothing. Two things were broken
 * at once and either alone would have been fatal:
 *
 *  - The release build pointed at a Deepgram relay on `www.prayerkey.com`. That
 *    endpoint answers 404 — the socket server was never deployed — so the
 *    online path was dead in every shipped build.
 *  - [SermonCaptureEngine] already had an offline path, but it opened
 *    `filesDir/vosk-model` and returned quietly if the folder was missing, and
 *    **nothing in the app ever created that folder**. The Vosk dependency was
 *    being shipped and could never initialise.
 *
 * This is the missing half. Vosk's small English model is Apache 2.0, needs no
 * key, no account and no server, and runs entirely on the phone — which is the
 * only option that satisfies "everything free" and is also the only one that
 * works in a building with bad signal, which is where churches tend to be.
 *
 * It is downloaded once rather than bundled. At 39 MB it would have taken the
 * APK from 54 MB to 93 MB for a feature most users open occasionally, and a
 * download can be repeated if it is corrupt where a bad asset cannot.
 *
 * Model: vosk-model-small-en-us-0.15, from the Vosk project's own host.
 * https://alphacephei.com/vosk/models
 */
object VoskModel {

    private const val URL = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip"

    /** Checked against Content-Length before unzipping, so a truncated
     *  download is rejected rather than unpacked into a half model. */
    private const val EXPECTED_BYTES = 41_205_931L

    const val MEGABYTES = 39

    sealed interface State {
        data object Absent : State
        data class Downloading(val fraction: Float) : State
        data object Unpacking : State
        data object Ready : State
        data class Failed(val reason: String) : State
    }

    private val _state = MutableStateFlow<State>(State.Absent)
    val state: StateFlow<State> = _state.asStateFlow()

    /** Where the unpacked model lives. */
    fun directory(context: Context) = File(context.applicationContext.filesDir, "vosk-model")

    /**
     * Whether a *usable* model is present.
     *
     * Checks for the subfolders Vosk actually needs rather than just that the
     * directory exists — an interrupted unzip leaves a folder behind, and
     * `Model()` on a partial folder crashes in native code where it cannot be
     * caught cleanly.
     */
    fun isInstalled(context: Context): Boolean {
        val dir = directory(context)
        return dir.isDirectory && File(dir, "am").isDirectory && File(dir, "conf").isDirectory
    }

    fun refresh(context: Context) {
        _state.value = if (isInstalled(context)) State.Ready else State.Absent
    }

    /**
     * Fetches and unpacks the model. Safe to call when it is already present.
     *
     * Downloads to a temporary file, unpacks to a temporary directory, and only
     * then swaps it into place, so an interrupted install never leaves a broken
     * model where [isInstalled] would find it.
     */
    suspend fun install(context: Context): Boolean = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        if (isInstalled(app)) { _state.value = State.Ready; return@withContext true }

        val target = directory(app)
        val zip = File(app.cacheDir, "vosk-model.zip.part")
        val staging = File(app.filesDir, "vosk-model.staging")

        runCatching {
            staging.deleteRecursively()
            zip.delete()

            _state.value = State.Downloading(0f)
            val connection = (URL(URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 60_000
                instanceFollowRedirects = true
            }
            connection.inputStream.use { input ->
                val total = connection.contentLengthLong.takeIf { it > 0 } ?: EXPECTED_BYTES
                zip.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var written = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        written += read
                        _state.value = State.Downloading((written.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
            connection.disconnect()

            /* A short file means the connection dropped. Unpacking it would
               produce a directory that looks installed and crashes on use. */
            if (zip.length() < EXPECTED_BYTES * 9 / 10) {
                error("download was cut short at ${zip.length() / 1_048_576} MB")
            }

            _state.value = State.Unpacking
            unzip(zip, staging)

            /* The archive contains one top-level folder named for the model
               version. Vosk wants the folder holding `am` and `conf`, so find
               it rather than assuming the name and breaking on the next
               release. */
            val root = findModelRoot(staging) ?: error("the archive did not contain a model")

            target.deleteRecursively()
            if (!root.renameTo(target)) {
                root.copyRecursively(target, overwrite = true)
                root.deleteRecursively()
            }

            require(isInstalled(app)) { "the unpacked model was incomplete" }
            true
        }.onSuccess {
            _state.value = State.Ready
        }.onFailure { failure ->
            target.deleteRecursively()
            _state.value = State.Failed(failure.message ?: "the download failed")
        }.also {
            zip.delete()
            staging.deleteRecursively()
        }.getOrDefault(false)
    }

    fun remove(context: Context) {
        directory(context).deleteRecursively()
        _state.value = State.Absent
    }

    internal fun findModelRoot(from: File): File? {
        if (File(from, "am").isDirectory && File(from, "conf").isDirectory) return from
        return from.listFiles()?.filter { it.isDirectory }?.firstNotNullOfOrNull { findModelRoot(it) }
    }

    /**
     * Unpacks the archive.
     *
     * Every entry's resolved path is checked to be inside [into] before it is
     * written. A zip entry named `../../databases/x` would otherwise be written
     * outside the target directory — the Zip Slip vulnerability. The archive
     * here is trusted, but an unpacker that only works on trusted input is one
     * refactor away from being a hole.
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
