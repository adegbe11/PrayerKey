package com.prayerkey.manna

import com.prayerkey.manna.ui.church.VoskModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * The archive handling behind live transcription.
 *
 * The download itself needs a network and the recogniser needs a device, but
 * the part most likely to be wrong is the unpacking — where the model ends up,
 * and what happens when an archive is not shaped the way we assumed. That runs
 * fine on the JVM, so it is tested here rather than trusted.
 */
class VoskModelTest {

    @get:Rule val temp = TemporaryFolder()

    private fun zipOf(vararg entries: String): File {
        val file = temp.newFile("archive-${entries.hashCode()}.zip")
        ZipOutputStream(file.outputStream()).use { out ->
            entries.forEach { name ->
                out.putNextEntry(ZipEntry(name))
                if (!name.endsWith("/")) out.write("x".toByteArray())
                out.closeEntry()
            }
        }
        return file
    }

    @Test
    fun `unpacks an archive to the target directory`() {
        val into = temp.newFolder("into")
        VoskModel.unzip(zipOf("model/am/final.mdl", "model/conf/mfcc.conf"), into)
        assertTrue(File(into, "model/am/final.mdl").isFile)
        assertTrue(File(into, "model/conf/mfcc.conf").isFile)
    }

    /**
     * The model sits inside a folder named for its version
     * (`vosk-model-small-en-us-0.15/`), and that name changes with each
     * release. Finding the folder that holds `am` and `conf` means an upgrade
     * does not silently install nothing.
     */
    @Test
    fun `finds the model inside its version folder`() {
        val into = temp.newFolder("versioned")
        VoskModel.unzip(
            zipOf(
                "vosk-model-small-en-us-0.15/am/final.mdl",
                "vosk-model-small-en-us-0.15/conf/mfcc.conf",
                "vosk-model-small-en-us-0.15/graph/HCLr.fst",
            ),
            into,
        )
        val root = VoskModel.findModelRoot(into)
        assertEquals("vosk-model-small-en-us-0.15", root?.name)
    }

    @Test
    fun `finds the model when it is already at the top level`() {
        val into = temp.newFolder("flat")
        VoskModel.unzip(zipOf("am/final.mdl", "conf/mfcc.conf"), into)
        assertEquals(into.canonicalPath, VoskModel.findModelRoot(into)?.canonicalPath)
    }

    /**
     * A half-unpacked model must not look installed. `am` without `conf` is
     * what an interrupted download leaves behind, and opening it takes the
     * native recogniser down where Kotlin cannot catch it.
     */
    @Test
    fun `an incomplete model is not mistaken for a model`() {
        val into = temp.newFolder("partial")
        VoskModel.unzip(zipOf("vosk-model/am/final.mdl"), into)
        assertNull(VoskModel.findModelRoot(into))
    }

    /**
     * Zip Slip: an entry named `../escaped` resolves outside the target and
     * would let an archive write anywhere the app can. Rejected rather than
     * written, even though the archive we fetch is trusted.
     */
    @Test
    fun `an entry that escapes the target directory is refused`() {
        val into = temp.newFolder("guarded")
        val outside = File(into.parentFile, "escaped.txt")
        val failure = runCatching {
            VoskModel.unzip(zipOf("../escaped.txt"), into)
        }.exceptionOrNull()

        assertTrue("expected the unpack to fail, it did not", failure is IllegalStateException)
        assertFalse("a file was written outside the target directory", outside.exists())
    }
}
