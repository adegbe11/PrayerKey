package com.prayerkey.manna.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.prayerkey.manna.model.DailyPrayer
import java.io.File
import java.io.FileOutputStream

/**
 * Prayer of the Day card renderer — the prayerkey.com cream design
 * (ivory canvas, gold cross, huge book name, golden chapter:verse,
 * verse, short prayer, PRAYERKEY.COM watermark) in every social format.
 */
enum class CardFormat(val label: String, val w: Int, val h: Int) {
    SQUARE("Instagram", 1080, 1080),
    STORY("Story / TikTok", 1080, 1920),
    FACEBOOK("Facebook", 1200, 630),
    TWITTER("Twitter / X", 1200, 675),
    PINTEREST("Pinterest", 1000, 1500),
}

object PrayerCardRenderer {

    fun share(context: Context, prayer: DailyPrayer, format: CardFormat) {
        val bitmap = render(prayer, format)
        val directory = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(directory, "prayer-of-the-day-${format.name.lowercase()}-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 96, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "${prayer.title} · ${prayer.ref} — prayerkey.com")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Share Prayer of the Day"))
    }

    private fun render(prayer: DailyPrayer, format: CardFormat): Bitmap {
        val w = format.w.toFloat(); val h = format.h.toFloat()
        val bitmap = Bitmap.createBitmap(format.w, format.h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val landscape = w > h * 1.2f
        val s = w / 1080f
        val ts = if (landscape) s * (h / w) * 1.5f else s
        val cx = w / 2f

        /* cream canvas */
        canvas.drawColor(Color.rgb(245, 239, 232))

        /* label */
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.rgb(155, 112, 64); paint.textSize = 26f * ts; paint.letterSpacing = .2f
        var y = h * (if (landscape) .12f else .08f)
        canvas.drawText("✦  PRAYER OF THE DAY  ✦", cx, y, paint)
        paint.letterSpacing = 0f

        /* gold cross */
        y += 34f * ts
        paint.color = Color.rgb(155, 112, 64)
        canvas.drawRect(cx - 5f * ts, y, cx + 5f * ts, y + 44f * ts, paint)
        canvas.drawRect(cx - 24f * ts, y + 12f * ts, cx + 24f * ts, y + 21f * ts, paint)
        y += 78f * ts

        /* book + chapter:verse */
        val match = Regex("^(.*?)\\s+(\\d+[:\\d–-]*)$").find(prayer.ref)
        val book = (match?.groupValues?.get(1) ?: prayer.ref).uppercase()
        val chapterVerse = match?.groupValues?.get(2) ?: ""
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.rgb(26, 26, 26); paint.textSize = 108f * ts
        y += 82f * ts
        canvas.drawText(book, cx, y, paint)
        paint.color = Color.rgb(155, 112, 64); paint.textSize = 88f * ts
        y += 96f * ts
        canvas.drawText(chapterVerse, cx, y, paint)

        /* divider — line · dot · line */
        y += 44f * ts
        paint.strokeWidth = 2.5f * ts
        canvas.drawLine(cx - 130f * ts, y, cx - 22f * ts, y, paint)
        canvas.drawLine(cx + 22f * ts, y, cx + 130f * ts, y, paint)
        canvas.drawCircle(cx, y, 7f * ts, paint)

        /* verse */
        y += 64f * ts
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paint.color = Color.rgb(42, 42, 42); paint.textSize = 34f * ts
        y = drawWrapped(canvas, paint, "“${prayer.verse}”", cx, y, w * .82f, 50f * ts, 4)

        /* short prayer — first sentence, italic */
        y += 40f * ts
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        paint.color = Color.rgb(90, 64, 32); paint.textSize = 29f * ts
        val short = prayer.prayer.replace("\n", " ").split(Regex("(?<=[.!?])\\s+"))
            .drop(1).firstOrNull { it.length > 30 } ?: prayer.prayer.replace("\n", " ").take(140)
        y = drawWrapped(canvas, paint, short, cx, y, w * .84f, 44f * ts, 3)

        /* watermark */
        val wy = h - (if (landscape) 34f * ts else 52f * ts)
        paint.strokeWidth = 1.6f * ts; paint.color = Color.argb(70, 155, 112, 64)
        canvas.drawLine(cx - 150f * ts, wy - 34f * ts, cx + 150f * ts, wy - 34f * ts, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.rgb(155, 112, 64); paint.textSize = 26f * ts; paint.letterSpacing = .25f
        canvas.drawText("PRAYERKEY.COM", cx, wy, paint)
        paint.letterSpacing = 0f
        return bitmap
    }

    private fun drawWrapped(canvas: Canvas, paint: Paint, text: String, x: Float, startY: Float, maxWidth: Float, lineHeight: Float, maxLines: Int): Float {
        val words = text.split(Regex("\\s+")); val lines = mutableListOf<String>(); var line = ""
        words.forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) { lines += line; line = word } else line = candidate
        }
        if (line.isNotEmpty()) lines += line
        val shown = if (lines.size > maxLines) lines.take(maxLines - 1) + (lines[maxLines - 1].trimEnd('.', ',') + "…") else lines
        var y = startY
        shown.forEach { canvas.drawText(it, x, y, paint); y += lineHeight }
        return y - lineHeight
    }
}
