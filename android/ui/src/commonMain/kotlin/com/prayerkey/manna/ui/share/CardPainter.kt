package com.prayerkey.manna.ui.share

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * A card drawn the way Android's Paint and Canvas drew it, so a layout can be
 * carried across line for line: text at a baseline, centred or left, a size
 * in pixels, letter-spacing in ems, and words wrapped by measured width.
 *
 * Density is 1, so a size of 52 is 52 pixels on the 1080-wide card, exactly
 * as `paint.textSize = 52f` was.
 */
class CardPainter(val width: Int, val height: Int, private val measurer: TextMeasurer) {
    val bitmap = ImageBitmap(width, height)
    private val canvas = Canvas(bitmap)
    private val scope = CanvasDrawScope()

    var size = 24f
    var color = Color.Black
    var family: FontFamily = FontFamily.SansSerif
    var weight = FontWeight.Normal
    var italic = false
    var spacing = 0f
    var centred = true

    fun draw(block: DrawScope.() -> Unit) {
        scope.draw(Density(1f), LayoutDirection.Ltr, canvas, Size(width.toFloat(), height.toFloat()), block)
    }

    private fun style() = TextStyle(
        color = color, fontSize = size.sp, fontFamily = family, fontWeight = weight,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal, letterSpacing = spacing.em,
    )

    private fun layout(text: String) = measurer.measure(text, style(), softWrap = false, maxLines = 1)

    fun measure(text: String): Float = layout(text).size.width.toFloat()

    /** [x] is the centre when [centred], else the left edge; [baseline] as Canvas.drawText took it. */
    fun text(text: String, x: Float, baseline: Float) {
        val layout = layout(text)
        val left = if (centred) x - layout.size.width / 2f else x
        draw { drawText(layout, topLeft = Offset(left, baseline - layout.firstBaseline)) }
    }

    fun wrap(text: String, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        var line = ""
        text.split(Regex("\\s+")).filter { it.isNotEmpty() }.forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (measure(candidate) > maxWidth && line.isNotEmpty()) {
                lines += line; line = word
            } else line = candidate
        }
        if (line.isNotEmpty()) lines += line
        return lines
    }

    class Fitted(val lines: List<String>, val leading: Float)

    /** Shrinks the type until every word fits, rather than throwing words away. */
    fun fit(text: String, maxWidth: Float, maxHeight: Float, startSize: Float, minSize: Float): Fitted {
        var s = startSize
        while (s > minSize) {
            size = s
            val lines = wrap(text, maxWidth)
            val leading = s * 1.34f
            if ((lines.size - 1) * leading + s <= maxHeight) return Fitted(lines, leading)
            s -= 2f
        }
        size = minSize
        return Fitted(wrap(text, maxWidth), minSize * 1.34f)
    }

    fun lines(lines: List<String>, x: Float, startY: Float, leading: Float) {
        lines.forEachIndexed { i, line -> text(line, x, startY + i * leading) }
    }

    /** At most [maxLines], the last ending in an ellipsis; returns the last baseline. */
    fun wrapped(text: String, x: Float, startY: Float, maxWidth: Float, lineHeight: Float, maxLines: Int): Float {
        val all = wrap(text, maxWidth)
        val shown = if (all.size > maxLines) all.take(maxLines - 1) + (all[maxLines - 1].trimEnd('.', ',') + "…") else all
        var y = startY
        shown.forEach { text(it, x, y); y += lineHeight }
        return y - lineHeight
    }

    fun png(): ByteArray = bitmap.encodePng()
}

/** The card as PNG bytes, for the share sheet or the photo library. */
expect fun ImageBitmap.encodePng(): ByteArray

/** The font machinery Compose measures text with, outside of a composition. */
expect fun cardFontResolver(context: com.prayerkey.manna.platform.PlatformContext): androidx.compose.ui.text.font.FontFamily.Resolver
