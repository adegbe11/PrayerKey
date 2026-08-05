package com.prayerkey.manna.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.model.VerseCard
import java.io.File
import java.io.FileOutputStream

object CardShareRenderer {

    private val INK = Color.rgb(23, 23, 25)
    private val GOLD = Color.rgb(176, 124, 31)
    private val PAPER = Color.rgb(255, 252, 244)
    private val MUTED = Color.rgb(90, 90, 94)

    fun share(context: Context, card: VerseCard) {
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(PAPER)

        paint.color = GOLD; paint.strokeWidth = 3f; paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(52f, 52f, 1028f, 1298f, 46f, 46f, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paint.color = GOLD; paint.textSize = 46f
        canvas.drawText("⚿", 540f, 185f, paint)

        /* Fitted, not clipped. This used to take the first seven lines and
           drop the rest, so a long verse went out with its ending missing. */
        paint.color = INK
        val fitted = fit(paint, card.verse.replace('\n', ' '), 830f, 400f, 64f, 34f)
        drawLines(canvas, paint, fitted.lines, 540f, 380f, fitted.leading)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); paint.textSize = 30f; paint.color = MUTED
        canvas.drawText("${card.reference}  ·  ${card.translation}", 540f, 830f, paint)

        paint.shader = LinearGradient(0f, 900f, 0f, 1350f, Color.rgb(245, 218, 164), Color.rgb(90, 72, 64), Shader.TileMode.CLAMP)
        canvas.drawRect(53f, 900f, 1027f, 1297f, paint); paint.shader = null
        val mountain = Path().apply { moveTo(53f,1298f); lineTo(53f,1110f); lineTo(250f,970f); lineTo(430f,1150f); lineTo(670f,940f); lineTo(1027f,1130f); lineTo(1027f,1298f); close() }
        paint.color = Color.argb(190, 103, 78, 64); canvas.drawPath(mountain, paint)

        footer(canvas, paint, 1210f, 1255f, "Fresh every morning · PrayerKey")

        send(
            context, bitmap,
            caption = "${card.reference} — Received on MANNA by PrayerKey",
            chooser = "Share this word",
        )
    }

    /**
     * A prayer card, shared whole.
     *
     * The deck truncates on screen because a card you flick through has to
     * stay readable at a glance. A share has no such excuse: someone who
     * receives this is meant to actually pray it, and a prayer that stops at
     * "Where there is …" cannot be prayed. So the type is fitted to the
     * prayer rather than the prayer cut to the type, and the full text rides
     * along in the message body as well as the image.
     */
    fun sharePrayer(context: Context, topic: PrayerTopic) {
        // taller than the verse card, because a prayer is a paragraph
        val bitmap = Bitmap.createBitmap(1080, 1620, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(PAPER)

        paint.color = GOLD; paint.strokeWidth = 3f; paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(52f, 52f, 1028f, 1568f, 46f, 46f, paint)
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER

        /* Measured first, then drawn centred. Laying it out top-down left a
           short prayer floating above a hand-width of blank card. */
        val bandTop = 1430f
        val top = 210f

        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paint.color = INK
        val title = fit(paint, topic.title, 840f, 190f, 66f, 40f)
        val titleSize = paint.textSize
        val titleHeight = (title.lines.size - 1) * title.leading + titleSize

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val refsHeight = if (topic.scripture.isEmpty()) 0f else 74f
        val bodyRoom = bandTop - top - titleHeight - 190f - refsHeight
        val body = fit(paint, topic.prayer.replace(Regex("""\s+"""), " "), 860f, bodyRoom, 40f, 21f)
        val bodySize = paint.textSize
        val bodyHeight = (body.lines.size - 1) * body.leading + bodySize

        val block = 60f + titleHeight + 58f + 74f + bodyHeight + refsHeight
        var y = top + ((bandTop - top - block) / 2f).coerceAtLeast(0f) + titleSize

        // the category belongs to the title, not to the top of the card
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 26f; paint.color = GOLD
        paint.letterSpacing = .22f
        canvas.drawText(topic.category.uppercase(), 540f, y - titleSize - 34f, paint)
        paint.letterSpacing = 0f

        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paint.textSize = titleSize; paint.color = INK
        drawLines(canvas, paint, title.lines, 540f, y, title.leading)
        y += (title.lines.size - 1) * title.leading

        paint.color = GOLD; paint.strokeWidth = 2f
        canvas.drawLine(450f, y + 52f, 630f, y + 52f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = bodySize; paint.color = Color.rgb(48, 48, 52)
        y += 126f
        drawLines(canvas, paint, body.lines, 540f, y, body.leading)
        y += (body.lines.size - 1) * body.leading

        if (topic.scripture.isNotEmpty()) {
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 26f; paint.color = GOLD
            canvas.drawText(topic.scripture.take(3).joinToString("   ") { it.first }, 540f, y + 74f, paint)
        }

        paint.shader = LinearGradient(0f, 1430f, 0f, 1620f, Color.rgb(245, 218, 164), Color.rgb(90, 72, 64), Shader.TileMode.CLAMP)
        canvas.drawRect(53f, 1430f, 1027f, 1567f, paint); paint.shader = null

        footer(canvas, paint, 1500f, 1540f, "Pray with us · PrayerKey")

        // the words travel even where the image does not
        val refs = topic.scripture.take(3).joinToString(" · ") { it.first }
        send(
            context, bitmap,
            caption = buildString {
                append(topic.title).append("\n\n").append(topic.prayer)
                if (refs.isNotEmpty()) append("\n\n").append(refs)
                append("\n\n— PrayerKey")
            },
            chooser = "Share this prayer",
        )
    }

    private fun footer(canvas: Canvas, paint: Paint, brandY: Float, lineY: Float, line: String) {
        paint.textSize = 28f; paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("MANNA", 540f, brandY, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); paint.textSize = 21f
        canvas.drawText(line, 540f, lineY, paint)
    }

    private fun send(context: Context, bitmap: Bitmap, caption: String, chooser: String) {
        val directory = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(directory, "manna-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 96, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, caption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                chooser,
            ),
        )
    }

    private class Fitted(val lines: List<String>, val leading: Float)

    /**
     * Shrinks the type until every word fits the space, rather than keeping
     * the type and throwing words away.
     */
    private fun fit(
        paint: Paint,
        text: String,
        maxWidth: Float,
        maxHeight: Float,
        startSize: Float,
        minSize: Float,
    ): Fitted {
        var size = startSize
        while (size > minSize) {
            paint.textSize = size
            val lines = wrap(paint, text, maxWidth)
            val leading = size * 1.34f
            if ((lines.size - 1) * leading + size <= maxHeight) return Fitted(lines, leading)
            size -= 2f
        }
        paint.textSize = minSize
        return Fitted(wrap(paint, text, maxWidth), minSize * 1.34f)
    }

    private fun wrap(paint: Paint, text: String, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        var line = ""
        text.split(Regex("\\s+")).forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                lines += line; line = word
            } else line = candidate
        }
        if (line.isNotEmpty()) lines += line
        return lines
    }

    private fun drawLines(canvas: Canvas, paint: Paint, lines: List<String>, x: Float, startY: Float, leading: Float) {
        lines.forEachIndexed { index, value -> canvas.drawText(value, x, startY + index * leading, paint) }
    }
}
