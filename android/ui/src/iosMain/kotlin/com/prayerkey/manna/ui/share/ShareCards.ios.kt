package com.prayerkey.manna.ui.share

import platform.UIKit.popoverPresentationController
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.createFontFamilyResolver
import com.prayerkey.manna.data.FormationState
import com.prayerkey.manna.data.JournalEntry
import com.prayerkey.manna.data.JournalPrayer
import com.prayerkey.manna.data.MemoryVerse
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.SavedWord
import com.prayerkey.manna.data.SermonNote
import com.prayerkey.manna.data.nowMillis
import com.prayerkey.manna.model.DailyPrayer
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.ui.platform.saveImageToPhotos
import com.prayerkey.manna.ui.platform.shareImage
import com.prayerkey.manna.ui.platform.shareText
import com.prayerkey.manna.ui.platform.showToast
import com.prayerkey.manna.ui.platform.toNSData
import com.prayerkey.manna.ui.platform.topController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIImage

actual fun ImageBitmap.encodePng(): ByteArray =
    Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes ?: ByteArray(0)

actual fun cardFontResolver(context: PlatformContext): FontFamily.Resolver = createFontFamilyResolver()

actual fun defaultShareCards(): ShareCards = IosShareCards

/** The iPhone's share cards: drawn by [ComposeCards], shared through the iOS share sheet. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
object IosShareCards : ShareCards {
    private val scope = MainScope()

    private fun cards(context: PlatformContext) = ComposeCards(context)

    private fun stamp() = nowMillis().toString()

    override fun shareStatus(context: PlatformContext, card: VerseCard) {
        scope.launch {
            shareImage(context, cards(context).brandedStatus(card), "${card.reference}\n\n${card.verse}\n\n— PrayerKey", "Share verse image")
        }
    }

    override fun saveStatus(context: PlatformContext, card: VerseCard) {
        scope.launch {
            val ok = saveImageToPhotos(context, cards(context).brandedStatus(card), "PrayerKey-${stamp()}")
            showToast(context, if (ok) "Saved to Photos" else "Could not save image")
        }
    }

    override fun sharePrayerStatus(context: PlatformContext, prayer: String) {
        scope.launch {
            shareImage(context, cards(context).prayerStatus(prayer), "$prayer\n\n— PrayerKey", "Share the prayer")
        }
    }

    override fun savePrayerStatus(context: PlatformContext, prayer: String) {
        scope.launch {
            if (saveImageToPhotos(context, cards(context).prayerStatus(prayer), "PrayerKey-prayer-${stamp()}")) {
                showToast(context, "Prayer image saved")
            }
        }
    }

    override fun shareClosingVerse(context: PlatformContext, verse: String, reference: String) {
        scope.launch {
            shareImage(context, cards(context).closingVerseStatus(verse, reference), "$reference\n\n$verse\n\n— PrayerKey", "Share closing verse")
        }
    }

    override fun saveClosingVerse(context: PlatformContext, verse: String, reference: String) {
        scope.launch {
            if (saveImageToPhotos(context, cards(context).closingVerseStatus(verse, reference), "PrayerKey-closing-verse-${stamp()}")) {
                showToast(context, "Closing verse saved")
            }
        }
    }

    override fun shareVerse(context: PlatformContext, card: VerseCard) =
        shareImage(context, cards(context).verseCard(card), "${card.reference} — PrayerKey", "Share this word")

    override fun sharePrayer(context: PlatformContext, topic: PrayerTopic) {
        val refs = topic.scripture.take(3).joinToString(" · ") { it.first }
        shareImage(
            context, cards(context).prayerCard(topic),
            buildString {
                append(topic.title).append("\n\n").append(topic.prayer)
                if (refs.isNotEmpty()) append("\n\n").append(refs)
                append("\n\n— PrayerKey")
            },
            "Share this prayer",
        )
    }

    override fun shareAnswered(context: PlatformContext, request: String, testimony: String, answeredAt: Long) =
        shareImage(context, cards(context).answeredCard(request, testimony, answeredAt), "", "Share answered prayer")

    override fun shareBadge(context: PlatformContext, title: String, detail: String) =
        shareImage(context, cards(context).badge(title, detail), "", "Share challenge badge")

    override fun sharePrayerCard(context: PlatformContext, prayer: DailyPrayer, format: CardFormat) =
        shareImage(context, cards(context).prayerOfTheDay(prayer, format), "${prayer.title} · ${prayer.ref} — prayerkey.com", "Share Prayer of the Day")

    override fun shareSundayNotes(context: PlatformContext, note: SermonNote) =
        shareText(context, sundayNotesText(note), "Share these notes")

    override fun shareRemembranceBook(
        context: PlatformContext, name: String, entries: List<JournalEntry>, sermons: List<SermonNote>,
        prayers: List<JournalPrayer>, saved: List<SavedWord>,
    ) {
        val zone = TimeZone.currentSystemDefault()
        val year = Clock.System.now().toLocalDateTime(zone).year
        val start = LocalDate(year, 1, 1).atStartOfDayIn(zone).toEpochMilliseconds()
        fun date(ms: Long) = Instant.fromEpochMilliseconds(ms).toLocalDateTime(zone).date.toString()
        val groups = listOf(
            Triple("Answered prayers", "TESTIMONY", entries.filter { it.createdAt >= start && it.answeredAt != null }.map { it.body to it.testimony.orEmpty() }),
            Triple("Prayers I carried", "PRAYER", entries.filter { it.createdAt >= start && it.isPrayer }.map { it.prayerStage.label to it.body }),
            Triple("Words that stayed", "SCRIPTURE", saved.filter { it.savedAt >= start }.map { "${it.reference} · ${it.translation}" to it.verse }),
            Triple("Sundays I carried home", "CHURCH", sermons.filter { it.createdAt >= start }.map { it.title to it.takeaway }),
            Triple("Pages from my journey", "REFLECTION", entries.filter { it.createdAt >= start && !it.isPrayer }.map { date(it.createdAt) to it.body }),
        )
        val c = cards(context)
        val pages = mutableListOf<ByteArray>()
        pages += c.remembrancePage(
            1, "${name.ifBlank { "My" }} Book of Remembrance", year.toString(),
            listOf(
                "A YEAR HELD WITH CARE" to "This book gathers the prayers, words, sermons, gratitude, and testimonies you chose to keep in PrayerKey.",
                "THE STORY SO FAR" to "${entries.count { it.createdAt >= start }} reflections · ${prayers.count { it.createdAt >= start }} prayers · ${sermons.count { it.createdAt >= start }} sermon notes · ${saved.count { it.savedAt >= start }} saved words",
                "REMEMBER" to "This is a record of your journey, not a measurement of your worth or faithfulness.",
            ),
        )
        groups.forEach { (title, kicker, blocks) -> blocks.chunked(4).forEach { pages += c.remembrancePage(pages.size + 1, title, kicker, it) } }

        // each page drawn into a PDF at the size Android's PdfDocument used
        val path = NSTemporaryDirectory() + "manna-book-of-remembrance-$year.pdf"
        val renderer = UIGraphicsPDFRenderer(bounds = CGRectMake(0.0, 0.0, 1240.0, 1754.0))
        val data = renderer.PDFDataWithActions { pdf ->
            pages.forEach { png ->
                pdf?.beginPage()
                UIImage(data = png.toNSData()).drawInRect(CGRectMake(0.0, 0.0, 1240.0, 1754.0))
            }
        }
        data.writeToFile(path, atomically = true)
        shareFile(path)
    }

    override fun exportArchive(
        context: PlatformContext, entries: List<JournalEntry>, sermons: List<SermonNote>, prayers: List<JournalPrayer>,
        saved: List<SavedWord>, memory: List<MemoryVerse>, formation: FormationState,
    ) {
        val path = NSTemporaryDirectory() + "manna-private-archive-${stamp()}.json"
        NSString.create(string = archiveJson(entries, sermons, prayers, saved, memory, formation))
            .writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        shareFile(path)
    }

    private fun shareFile(path: String) {
        val top = topController() ?: return
        val sheet = UIActivityViewController(activityItems = listOf(NSURL.fileURLWithPath(path)), applicationActivities = null)
        sheet.popoverPresentationController?.sourceView = top.view
        top.presentViewController(sheet, animated = true, completion = null)
    }
}
