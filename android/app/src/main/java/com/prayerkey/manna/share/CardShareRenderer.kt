package com.prayerkey.manna.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Environment
import android.widget.Toast
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

    /** A portrait PrayerKey keepsake sized for WhatsApp/Instagram status. */
    fun shareStatus(context: Context, card: VerseCard) {
        send(context, brandedStatus(context, card), "${card.reference}\n\n${card.verse}\n\n— PrayerKey", "Share verse image")
    }

    fun saveStatus(context: Context, card: VerseCard) {
        val bitmap = brandedStatus(context, card)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "PrayerKey-${card.reference.replace(Regex("[^A-Za-z0-9]+"), "-")}-${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PrayerKey")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 96, it) }
            values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            Toast.makeText(context, "Saved to Pictures / PrayerKey", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(context, "Could not save image", Toast.LENGTH_SHORT).show()
        bitmap.recycle()
    }

    fun sharePrayerStatus(context: Context, prayer: String) {
        send(context, prayerStatus(context, prayer), "$prayer\n\n— PrayerKey", "Share today's prayer")
    }

    fun savePrayerStatus(context: Context, prayer: String) {
        val bitmap = prayerStatus(context, prayer)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "PrayerKey-prayer-${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PrayerKey")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 96, it) }
            values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            Toast.makeText(context, "Prayer image saved", Toast.LENGTH_SHORT).show()
        }
        bitmap.recycle()
    }

    fun shareClosingVerse(context: Context, verse: String, reference: String) {
        send(context, closingVerseStatus(context, verse, reference), "$reference\n\n$verse\n\n— PrayerKey", "Share closing verse")
    }

    fun saveClosingVerse(context: Context, verse: String, reference: String) {
        val bitmap = closingVerseStatus(context, verse, reference)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "PrayerKey-closing-verse-${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PrayerKey")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 96, it) }
            values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            Toast.makeText(context, "Closing verse saved", Toast.LENGTH_SHORT).show()
        }
        bitmap.recycle()
    }

    private fun closingVerseStatus(context: Context, verse: String, reference: String): Bitmap {
        val out = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val art = BitmapFactory.decodeResource(context.resources, com.prayerkey.manna.R.drawable.closing_verse_violet)
        canvas.drawBitmap(art, null, android.graphics.Rect(0, 0, 1080, 1920), Paint(Paint.ANTI_ALIAS_FLAG)); art.recycle()
        val shade = Paint().apply { shader = LinearGradient(0f, 450f, 0f, 1780f, Color.TRANSPARENT, Color.argb(230, 26, 10, 46), Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 1080f, 1920f, shade)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); paint.color = Color.rgb(239, 213, 168); paint.textSize = 25f; paint.letterSpacing = .18f
        canvas.drawText("A WORD TO CARRY", 540f, 990f, paint); paint.letterSpacing = 0f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); paint.color = Color.WHITE
        val fitted = fit(paint, "“$verse”", 820f, 520f, 58f, 38f)
        drawLines(canvas, paint, fitted.lines, 540f, 1110f, fitted.leading)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); paint.color = Color.rgb(239, 213, 168); paint.textSize = 25f; paint.letterSpacing = .16f
        canvas.drawText(reference.uppercase(), 540f, 1530f, paint)
        canvas.drawText("PRAYERKEY", 540f, 1710f, paint)
        return out
    }

    private fun prayerStatus(context: Context, prayer: String): Bitmap {
        val out = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val art = BitmapFactory.decodeResource(context.resources, com.prayerkey.manna.R.drawable.today_prayer_violet)
        canvas.drawBitmap(art, null, android.graphics.Rect(0, 0, 1080, 1920), Paint(Paint.ANTI_ALIAS_FLAG))
        art.recycle()
        val shade = Paint().apply { shader = LinearGradient(0f, 220f, 0f, 1680f, Color.argb(55, 26, 10, 46), Color.argb(190, 26, 10, 46), Shader.TileMode.CLAMP) }
        canvas.drawRect(0f, 0f, 1080f, 1920f, shade)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); paint.color = Color.rgb(239, 213, 168); paint.textSize = 26f; paint.letterSpacing = .2f
        canvas.drawText("TODAY'S PRAYER", 540f, 365f, paint); paint.letterSpacing = 0f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC); paint.color = Color.WHITE
        val fitted = fit(paint, prayer, 820f, 800f, 50f, 32f)
        val height = (fitted.lines.size - 1) * fitted.leading + paint.textSize
        drawLines(canvas, paint, fitted.lines, 540f, 980f - height / 2f, fitted.leading)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); paint.color = Color.rgb(239, 213, 168); paint.textSize = 24f; paint.letterSpacing = .16f
        canvas.drawText("PRAYERKEY", 540f, 1580f, paint)
        return out
    }

    private fun brandedStatus(context: Context, card: VerseCard): Bitmap {
        val out = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val art = BitmapFactory.decodeResource(context.resources, com.prayerkey.manna.R.drawable.verse_card_oxblood)
        val scale = maxOf(1080f / art.width, 1920f / art.height)
        val left = (1080f - art.width * scale) / 2f
        canvas.drawBitmap(art, null, android.graphics.RectF(left, 0f, left + art.width * scale, art.height * scale), Paint(Paint.ANTI_ALIAS_FLAG))
        art.recycle()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
        val violet = Color.rgb(98, 0, 237)
        val goldDeep = Color.rgb(111, 85, 40)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = goldDeep; paint.textSize = 25f; paint.letterSpacing = .20f
        canvas.drawText("VERSE OF THE DAY", 540f, 535f, paint)
        paint.letterSpacing = 0f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.color = violet; paint.textSize = 64f
        canvas.drawText(card.reference.uppercase(), 540f, 650f, paint)
        paint.color = Color.rgb(201, 162, 109); paint.strokeWidth = 3f
        canvas.drawLine(415f, 704f, 665f, 704f, paint)
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paint.color = Color.rgb(26, 10, 46)
        val verse = fit(paint, "“${card.verse}”", 700f, 520f, 52f, 34f)
        val blockHeight = (verse.lines.size - 1) * verse.leading + paint.textSize
        drawLines(canvas, paint, verse.lines, 540f, 930f - blockHeight / 2f, verse.leading)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = goldDeep; paint.textSize = 24f; paint.letterSpacing = .16f
        canvas.drawText("PRAYERKEY", 540f, 1460f, paint)
        return out
    }

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
