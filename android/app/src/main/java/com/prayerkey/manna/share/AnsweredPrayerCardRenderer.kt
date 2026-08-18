package com.prayerkey.manna.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** A private testimony becomes a shareable image only when the user asks. */
object AnsweredPrayerCardRenderer {
    fun share(context: Context, request: String, testimony: String, answeredAt: Long) {
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.rgb(245, 245, 247))

        paint.color = Color.rgb(92, 2, 216)
        canvas.drawRoundRect(62f, 62f, 1018f, 1288f, 54f, 54f, paint)
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.rgb(239, 213, 168)
        paint.textSize = 27f
        paint.letterSpacing = .18f
        canvas.drawText("ANSWERED PRAYER", 540f, 150f, paint)
        paint.letterSpacing = 0f

        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.color = Color.WHITE
        paint.textSize = 62f
        canvas.drawText("God was faithful.", 540f, 246f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.rgb(239, 213, 168)
        paint.textSize = 22f
        canvas.drawText("WHAT I PRAYED FOR", 540f, 350f, paint)
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paint.color = Color.WHITE
        paint.textSize = 38f
        var y = drawWrapped(canvas, paint, request, 540f, 414f, 820f, 54f, 5)

        y += 82f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.rgb(239, 213, 168)
        paint.textSize = 22f
        canvas.drawText("HOW IT WAS ANSWERED", 540f, y, paint)
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        paint.color = Color.WHITE
        paint.textSize = 38f
        y = drawWrapped(canvas, paint, testimony.ifBlank { "I am grateful for God's answer." }, 540f, y + 64f, 820f, 54f, 6)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.color = Color.argb(180, 255, 255, 255)
        paint.textSize = 22f
        canvas.drawText(java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG).format(java.util.Date(answeredAt)), 540f, minOf(y + 74f, 1160f), paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.rgb(239, 213, 168)
        paint.textSize = 24f
        paint.letterSpacing = .2f
        canvas.drawText("PRAYERKEY", 540f, 1230f, paint)

        val directory = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(directory, "answered-prayer-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 96, it) }
        bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Share answered prayer"))
    }

    private fun drawWrapped(canvas: Canvas, paint: Paint, text: String, x: Float, startY: Float, maxWidth: Float, lineHeight: Float, maxLines: Int): Float {
        val lines = mutableListOf<String>(); var line = ""
        text.replace('\n', ' ').split(Regex("\\s+")).forEach { word ->
            val next = if (line.isBlank()) word else "$line $word"
            if (paint.measureText(next) > maxWidth && line.isNotBlank()) { lines += line; line = word } else line = next
        }
        if (line.isNotBlank()) lines += line
        var y = startY
        lines.take(maxLines).forEach { canvas.drawText(it, x, y, paint); y += lineHeight }
        return y
    }
}
