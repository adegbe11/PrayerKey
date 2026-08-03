package com.prayerkey.manna.share

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.prayerkey.manna.data.JournalEntry
import com.prayerkey.manna.data.JournalPrayer
import com.prayerkey.manna.data.SavedWord
import com.prayerkey.manna.data.SermonNote
import com.prayerkey.manna.data.MemoryVerse
import com.prayerkey.manna.data.FormationState
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

object RemembranceBook {
    private const val W = 1240
    private const val H = 1754

    fun share(context: Context, name: String, entries: List<JournalEntry>, sermons: List<SermonNote>, prayers: List<JournalPrayer>, saved: List<SavedWord>) {
        val year = ZonedDateTime.now().year
        val start = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant().toEpochMilli()
        val document = PdfDocument(); var pageNo = 0
        val groups = listOf(
            Triple("Answered prayers", "TESTIMONY", entries.filter { it.createdAt >= start && it.answeredAt != null }.map { it.body to it.testimony.orEmpty() }),
            Triple("Prayers I carried", "PRAYER", entries.filter { it.createdAt >= start && it.isPrayer }.map { it.prayerStage.label to it.body }),
            Triple("Words that stayed", "SCRIPTURE", saved.filter { it.savedAt >= start }.map { "${it.reference} · ${it.translation}" to it.verse }),
            Triple("Sundays I carried home", "CHURCH", sermons.filter { it.createdAt >= start }.map { it.title to it.takeaway }),
            Triple("Pages from my journey", "REFLECTION", entries.filter { it.createdAt >= start && !it.isPrayer }.map { date(it.createdAt) to it.body }),
        )
        pageNo = drawPage(document, ++pageNo, "${name.ifBlank { "My" }} Book of Remembrance", year.toString(), listOf(
            "A YEAR HELD WITH CARE" to "This book gathers the prayers, words, sermons, gratitude, and testimonies you chose to preserve in MANNA.",
            "THE STORY SO FAR" to "${entries.count { it.createdAt >= start }} reflections · ${prayers.count { it.createdAt >= start }} generated prayers · ${sermons.count { it.createdAt >= start }} sermon notes · ${saved.count { it.savedAt >= start }} saved words",
            "REMEMBER" to "This is a record of your journey, not a measurement of your worth or faithfulness.",
        ))
        groups.forEach { (title, kicker, blocks) -> blocks.chunked(4).forEach { pageNo = drawPage(document, ++pageNo, title, kicker, it) } }
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "manna-book-of-remembrance-$year.pdf")
        FileOutputStream(file).use(document::writeTo); document.close()
        shareFile(context, file, "application/pdf", "Share my private remembrance book")
    }

    private fun drawPage(document: PdfDocument, number: Int, title: String, kicker: String, blocks: List<Pair<String, String>>): Int {
        val page = document.startPage(PdfDocument.PageInfo.Builder(W, H, number).create()); val c = page.canvas
        c.drawColor(Color.rgb(247, 244, 236)); val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(176, 124, 31); p.textSize = 21f; p.letterSpacing = .16f; c.drawText(kicker.uppercase(), 92f, 105f, p)
        p.letterSpacing = 0f; p.color = Color.rgb(29, 29, 31); p.textSize = 48f; p.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); c.drawText(title.take(42), 92f, 175f, p)
        var y = 245f
        blocks.forEach { (heading, body) ->
            p.typeface = Typeface.DEFAULT_BOLD; p.textSize = 19f; p.color = Color.rgb(176, 124, 31); c.drawText(heading.take(80), 92f, y, p); y += 38f
            p.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL); p.textSize = 23f; p.color = Color.rgb(45, 45, 48)
            wrap(body, 88).take(8).forEach { line -> c.drawText(line, 92f, y, p); y += 33f }; y += 34f
        }
        p.typeface = Typeface.DEFAULT; p.textSize = 15f; p.color = Color.GRAY; c.drawText("MANNA · Private by design · $number", 92f, H - 65f, p)
        document.finishPage(page); return number
    }

    private fun wrap(text: String, width: Int): List<String> {
        val out = mutableListOf<String>(); var line = ""
        text.replace('\n', ' ').split(Regex("\\s+")).forEach { word ->
            if (line.length + word.length + 1 > width) { if (line.isNotBlank()) out += line; line = word }
            else line = if (line.isBlank()) word else "$line $word"
        }
        if (line.isNotBlank()) out += line; return out
    }

    private fun date(ms: Long) = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate().toString()
}

object JourneyArchive {
    fun share(context: Context, entries: List<JournalEntry>, sermons: List<SermonNote>, prayers: List<JournalPrayer>, saved: List<SavedWord>, memory: List<MemoryVerse>, formation: FormationState) {
        val root = JSONObject().put("format", "manna-journey-v3").put("createdAt", System.currentTimeMillis())
        root.put("entries", JSONArray().apply { entries.forEach { put(JSONObject().put("mood", it.mood).put("title", it.title).put("body", it.body).put("gratitude", it.gratitude).put("verseRef", it.verseRef).put("verseText", it.verseText).put("createdAt", it.createdAt).put("updatedAt", it.updatedAt).put("source", it.source).put("isPrayer", it.isPrayer).put("prayerStage", it.prayerStage.key).put("nextAction", it.nextAction).put("answeredAt", it.answeredAt).put("testimony", it.testimony).put("tags", JSONArray(it.tags)).put("journal", it.journal).put("favorite", it.favorite).put("location", it.location).put("weather", it.weather).put("media", JSONArray(it.media))) } })
        root.put("sermons", JSONArray().apply { sermons.forEach { put(JSONObject().put("title", it.title).put("scriptures", JSONArray(it.scriptures)).put("points", JSONArray(it.points)).put("quotes", JSONArray(it.quotes)).put("takeaway", it.takeaway).put("transcript", it.transcript).put("minutes", it.minutes).put("createdAt", it.createdAt)) } })
        root.put("prayers", JSONArray().apply { prayers.forEach { put(JSONObject().put("title", it.title).put("request", it.request).put("prayer", it.prayer).put("scriptureRef", it.scriptureRef).put("createdAt", it.createdAt)) } })
        root.put("savedWords", JSONArray().apply { saved.forEach { put(JSONObject().put("reference", it.reference).put("translation", it.translation).put("verse", it.verse).put("savedAt", it.savedAt).put("answeredAt", it.answeredAt).put("testimony", it.testimony)) } })
        root.put("memory", JSONArray().apply { memory.forEach { put(JSONObject().put("reference", it.reference).put("verse", it.verse).put("stage", it.stage).put("correctCount", it.correctCount).put("nextReviewAt", it.nextReviewAt)) } })
        root.put("formation", JSONObject().put("morningWord", formation.morningWord).put("middayPrayer", formation.middayPrayer).put("eveningExamen", formation.eveningExamen).put("sabbathDay", formation.sabbathDay).put("pilgrimageId", formation.pilgrimageId).put("pilgrimageDay", formation.pilgrimageDay).put("familyNames", formation.familyNames).put("trustedPhone", formation.trustedPhone))
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }; val file = File(dir, "manna-private-archive-${System.currentTimeMillis()}.json"); file.writeText(root.toString(2))
        shareFile(context, file, "application/json", "Export my private MANNA archive")
    }
}

private fun shareFile(context: Context, file: File, mime: String, title: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = mime; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, title))
}
