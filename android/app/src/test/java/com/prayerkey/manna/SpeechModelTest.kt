package com.prayerkey.manna

import com.prayerkey.manna.ui.church.SpeechModel
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * The archive handling behind transcription.
 *
 * The download needs a network and the recogniser needs a device, but the part
 * most likely to be wrong is the unpacking — where the model ends up, and what
 * happens when an archive is not shaped the way we assumed. That runs on the
 * JVM, so it is tested rather than trusted.
 *
 * This replaces the Vosk version of the same tests. The model moved because
 * libvosk.so is aligned for 4 KB memory pages and Android 15 requires 16 KB;
 * the archive also changed from .zip to .tar.bz2, which is why the unpacker is
 * new code and needs its own cover.
 */
class SpeechModelTest {

    @get:Rule val temp = TemporaryFolder()

    private fun archiveOf(vararg entries: String): File {
        val file = temp.newFile("archive-${entries.hashCode()}.tar.bz2")
        TarArchiveOutputStream(BZip2CompressorOutputStream(file.outputStream())).use { tar ->
            tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
            entries.forEach { name ->
                val body = "x".toByteArray()
                tar.putArchiveEntry(TarArchiveEntry(name).apply { size = body.size.toLong() })
                tar.write(body)
                tar.closeArchiveEntry()
            }
        }
        return file
    }

    /** One model now, and it must still declare a size and a real archive. */
    @Test
    fun `the model declares where it comes from`() {
        val kinds = SpeechModel.Kind.entries
        assertEquals(1, kinds.size)
        kinds.forEach {
            assertTrue("${it.name} has no published size", it.expectedBytes > 1_000_000)
            assertTrue("${it.name} url is not a tar.bz2", it.url.endsWith(".tar.bz2"))
            assertTrue("${it.name} has no folder", it.folder.isNotBlank())
        }
    }

    @Test
    fun `unpacks an archive to the target directory`() {
        val into = temp.newFolder("into")
        SpeechModel.untar(archiveOf("m/encoder.onnx", "m/tokens.txt"), into)
        assertTrue(File(into, "m/encoder.onnx").isFile)
        assertTrue(File(into, "m/tokens.txt").isFile)
    }

    /**
     * The model sits inside a folder named for its release date, and that name
     * changes every time. Finding the folder that holds the four files means
     * an upgrade does not silently install nothing.
     */
    @Test
    fun `finds the model inside its version folder`() {
        val into = temp.newFolder("versioned")
        val name = "sherpa-onnx-streaming-zipformer-en-kroko-2025-08-06"
        SpeechModel.untar(
            archiveOf(*SpeechModel.FILES.map { "$name/$it" }.toTypedArray()),
            into,
        )
        assertEquals(name, SpeechModel.findRoot(into)?.name)
    }

    @Test
    fun `finds the model when it is already at the top level`() {
        val into = temp.newFolder("flat")
        SpeechModel.untar(archiveOf(*SpeechModel.FILES.toTypedArray()), into)
        assertEquals(into.canonicalPath, SpeechModel.findRoot(into)?.canonicalPath)
    }

    /**
     * A half-unpacked model must not look installed. An encoder without a
     * tokens file is what an interrupted download leaves behind, and opening
     * it takes the native recogniser down where Kotlin cannot catch it.
     */
    @Test
    fun `an incomplete model is not mistaken for a model`() {
        val into = temp.newFolder("partial")
        SpeechModel.untar(archiveOf("m/encoder.onnx"), into)
        assertNull(SpeechModel.findRoot(into))
    }

    /**
     * An entry named `../escaped` resolves outside the target and would let an
     * archive write anywhere the app can. Refused rather than written, even
     * though the archive we fetch is trusted.
     */
    @Test
    fun `an entry that escapes the target directory is refused`() {
        val into = temp.newFolder("guarded")
        val outside = File(into.parentFile, "escaped.txt")
        val failure = runCatching {
            SpeechModel.untar(archiveOf("../escaped.txt"), into)
        }.exceptionOrNull()

        assertTrue("expected the unpack to fail, it did not", failure is IllegalStateException)
        assertFalse("a file was written outside the target directory", outside.exists())
    }
}
