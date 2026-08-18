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

object ChallengeBadgeRenderer {
    fun share(context: Context, title: String, detail: String) {
        val bitmap = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap); val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.rgb(245, 245, 247))
        paint.color = Color.rgb(92, 2, 216); canvas.drawCircle(540f, 500f, 365f, paint)
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 14f; paint.color = Color.rgb(239, 213, 168); canvas.drawCircle(540f, 500f, 315f, paint); paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER; paint.color = Color.rgb(239, 213, 168); paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); paint.textSize = 25f; paint.letterSpacing = .2f; canvas.drawText("PRAYERKEY · UNLOCKED", 540f, 315f, paint); paint.letterSpacing = 0f
        paint.color = Color.WHITE; paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); paint.textSize = 58f
        wrap(title, 22).take(3).forEachIndexed { index, line -> canvas.drawText(line, 540f, 445f + index * 70f, paint) }
        paint.color = Color.rgb(239, 213, 168); paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD); paint.textSize = 25f; canvas.drawText(detail.uppercase(), 540f, 690f, paint)
        paint.color = Color.rgb(26, 10, 46); paint.textSize = 26f; canvas.drawText("A faithful step, completed.", 540f, 950f, paint)
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }; val file = File(dir, "challenge-badge-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 96, it) }; bitmap.recycle()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "Share challenge badge"))
    }
    private fun wrap(text: String, limit: Int): List<String> { val out = mutableListOf<String>(); var line = ""; text.split(' ').forEach { word -> if ((line + word).length > limit) { out += line; line = word } else line = if (line.isBlank()) word else "$line $word" }; if (line.isNotBlank()) out += line; return out }
}
