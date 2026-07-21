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
import com.prayerkey.manna.model.VerseCard
import java.io.File
import java.io.FileOutputStream

object CardShareRenderer {
    fun share(context: Context, card: VerseCard) {
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.rgb(255, 252, 244))

        paint.color = Color.rgb(176, 124, 31); paint.strokeWidth = 3f; paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(52f, 52f, 1028f, 1298f, 46f, 46f, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.CENTER; paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paint.color = Color.rgb(176, 124, 31); paint.textSize = 46f
        canvas.drawText("⚿", 540f, 185f, paint)

        paint.color = Color.rgb(23, 23, 25); paint.textSize = 64f
        drawCenteredLines(canvas, paint, card.verse.replace('\n', ' '), 540f, 390f, 830f, 82f)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); paint.textSize = 30f; paint.color = Color.rgb(90, 90, 94)
        canvas.drawText("${card.reference}  ·  ${card.translation}", 540f, 830f, paint)

        paint.shader = LinearGradient(0f, 900f, 0f, 1350f, Color.rgb(245, 218, 164), Color.rgb(90, 72, 64), Shader.TileMode.CLAMP)
        canvas.drawRect(53f, 900f, 1027f, 1297f, paint); paint.shader = null
        val mountain = Path().apply { moveTo(53f,1298f); lineTo(53f,1110f); lineTo(250f,970f); lineTo(430f,1150f); lineTo(670f,940f); lineTo(1027f,1130f); lineTo(1027f,1298f); close() }
        paint.color = Color.argb(190, 103, 78, 64); canvas.drawPath(mountain, paint)

        paint.textSize = 28f; paint.color = Color.WHITE; paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("MANNA", 540f, 1210f, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL); paint.textSize = 21f
        canvas.drawText("Fresh every morning · PrayerKey", 540f, 1255f, paint)

        val directory = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(directory, "manna-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 96, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "${card.reference} — Received on MANNA by PrayerKey")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Share this word"))
    }

    private fun drawCenteredLines(canvas: Canvas, paint: Paint, text: String, x: Float, startY: Float, maxWidth: Float, lineHeight: Float) {
        val words = text.split(Regex("\\s+")); val lines = mutableListOf<String>(); var line = ""
        words.forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) { lines += line; line = word } else line = candidate
        }
        if (line.isNotEmpty()) lines += line
        lines.take(7).forEachIndexed { index, value -> canvas.drawText(value, x, startY + index * lineHeight, paint) }
    }
}
