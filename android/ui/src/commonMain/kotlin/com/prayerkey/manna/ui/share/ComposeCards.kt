package com.prayerkey.manna.ui.share

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.model.DailyPrayer
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.resources.Res
import com.prayerkey.manna.ui.theme.PkFonts
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * The share cards drawn with Compose, for iPhone: each one a line-for-line
 * transcription of Android's Canvas renderer (CardShareRenderer and its
 * siblings), the same coordinates, sizes, colours and fitting rules. Sans
 * text is Inter and serif is Lora, the app's own faces, where Android's
 * renderers used the system's.
 */
class ComposeCards(private val context: PlatformContext) {

    private val measurer = TextMeasurer(cardFontResolver(context), Density(1f), LayoutDirection.Ltr)

    private fun painter(w: Int, h: Int) = CardPainter(w, h, measurer)

    private fun CardPainter.sans(bold: Boolean = true) { family = PkFonts.utility; weight = if (bold) FontWeight.Bold else FontWeight.Normal; italic = false }
    private fun CardPainter.serif(bold: Boolean = false, italic: Boolean = false) {
        family = PkFonts.book; weight = if (bold) FontWeight.Bold else FontWeight.Normal; this.italic = italic
    }

    private suspend fun art(path: String): ImageBitmap = Res.readBytes(path).decodeToImageBitmap()

    /** Scaled to cover the card, centred across and hung from the top - as the Canvas renderer drew it. */
    private fun CardPainter.cover(art: ImageBitmap) {
        val scale = maxOf(width.toFloat() / art.width, height.toFloat() / art.height)
        val w = art.width * scale
        val left = (width - w) / 2f
        draw {
            drawImage(
                art, srcOffset = IntOffset.Zero, srcSize = IntSize(art.width, art.height),
                dstOffset = IntOffset(left.toInt(), 0), dstSize = IntSize(w.toInt(), (art.height * scale).toInt()),
            )
        }
    }

    /** Drawn into the whole card, as `drawBitmap(art, null, Rect(0, 0, w, h))` did. */
    private fun CardPainter.stretch(art: ImageBitmap) = draw {
        drawImage(art, srcOffset = IntOffset.Zero, srcSize = IntSize(art.width, art.height), dstOffset = IntOffset.Zero, dstSize = IntSize(width, height))
    }

    private val ink = Color(23, 23, 25)
    private val gold = Color(176, 124, 31)
    private val paper = Color(255, 252, 244)
    private val muted = Color(90, 90, 94)
    private val cream = Color(239, 213, 168)
    private val night = Color(26, 10, 46)

    /* ── the 1080 x 1920 status images ─────────────────────────────── */

    suspend fun brandedStatus(card: VerseCard): ByteArray = painter(1080, 1920).run {
        cover(art("drawable/verse_card_oxblood.webp"))
        val violet = Color(98, 0, 237)
        val goldDeep = Color(111, 85, 40)
        sans(); color = goldDeep; size = 25f; spacing = .20f
        text("VERSE OF THE DAY", 540f, 535f)
        spacing = 0f
        serif(bold = true); color = violet; size = 64f
        text(card.reference.uppercase(), 540f, 650f)
        draw { drawLine(Color(201, 162, 109), Offset(415f, 704f), Offset(665f, 704f), strokeWidth = 3f) }
        serif(); color = night
        val verse = fit(card.verse, 700f, 520f, 52f, 34f)
        val blockHeight = (verse.lines.size - 1) * verse.leading + size
        lines(verse.lines, 540f, 930f - blockHeight / 2f, verse.leading)
        sans(); color = goldDeep; size = 24f; spacing = .16f
        text("PRAYERKEY", 540f, 1460f)
        png()
    }

    suspend fun prayerStatus(prayer: String): ByteArray = painter(1080, 1920).run {
        stretch(art("drawable/today_prayer_violet.webp"))
        draw { drawRect(Brush.verticalGradient(0f to Color(26, 10, 46, 55), 1f to Color(26, 10, 46, 190), startY = 220f, endY = 1680f)) }
        sans(); color = cream; size = 26f; spacing = .2f
        text("TODAY'S PRAYER", 540f, 365f); spacing = 0f
        serif(italic = true); color = Color.White
        val fitted = fit(prayer, 820f, 800f, 50f, 32f)
        val height = (fitted.lines.size - 1) * fitted.leading + size
        lines(fitted.lines, 540f, 980f - height / 2f, fitted.leading)
        sans(); color = cream; size = 24f; spacing = .16f
        text("PRAYERKEY", 540f, 1580f)
        png()
    }

    suspend fun closingVerseStatus(verse: String, reference: String): ByteArray = painter(1080, 1920).run {
        stretch(art("drawable/closing_verse_violet.png"))
        draw { drawRect(Brush.verticalGradient(0f to Color.Transparent, 1f to Color(26, 10, 46, 230), startY = 450f, endY = 1780f)) }
        sans(); color = cream; size = 25f; spacing = .18f
        text("A WORD TO CARRY", 540f, 990f); spacing = 0f
        serif(bold = true); color = Color.White
        val fitted = fit(verse, 820f, 520f, 58f, 38f)
        lines(fitted.lines, 540f, 1110f, fitted.leading)
        sans(); color = cream; size = 25f; spacing = .16f
        text(reference.uppercase(), 540f, 1530f)
        text("PRAYERKEY", 540f, 1710f)
        png()
    }

    /* ── the paper cards ───────────────────────────────────────────── */

    private fun CardPainter.footer(brandY: Float, lineY: Float, line: String) {
        size = 28f; color = Color.White; sans()
        text("PRAYERKEY", 540f, brandY)
        sans(bold = false); size = 21f
        text(line, 540f, lineY)
    }

    private fun CardPainter.dusk(top: Float, bottom: Float, gradientEnd: Float) = draw {
        drawRect(
            Brush.verticalGradient(0f to Color(245, 218, 164), 1f to Color(90, 72, 64), startY = top, endY = gradientEnd),
            topLeft = Offset(53f, top), size = Size(974f, bottom - top),
        )
    }

    fun verseCard(card: VerseCard): ByteArray = painter(1080, 1350).run {
        draw {
            drawRect(paper)
            drawRoundRect(gold, Offset(52f, 52f), Size(976f, 1246f), CornerRadius(46f), style = Stroke(3f))
        }
        serif(); color = gold; size = 46f
        text("⚿", 540f, 185f)
        color = ink
        val fitted = fit(card.verse.replace('\n', ' '), 830f, 400f, 64f, 34f)
        lines(fitted.lines, 540f, 380f, fitted.leading)
        sans(); size = 30f; color = muted
        text("${card.reference}  ·  ${card.translation}", 540f, 830f)
        dusk(900f, 1297f, 1350f)
        val mountain = Path().apply {
            moveTo(53f, 1298f); lineTo(53f, 1110f); lineTo(250f, 970f); lineTo(430f, 1150f)
            lineTo(670f, 940f); lineTo(1027f, 1130f); lineTo(1027f, 1298f); close()
        }
        draw { drawPath(mountain, Color(103, 78, 64, 190)) }
        footer(1210f, 1255f, "Fresh every morning · PrayerKey")
        png()
    }

    fun prayerCard(topic: PrayerTopic): ByteArray = painter(1080, 1620).run {
        draw {
            drawRect(paper)
            drawRoundRect(gold, Offset(52f, 52f), Size(976f, 1516f), CornerRadius(46f), style = Stroke(3f))
        }
        val bandTop = 1430f
        val top = 210f

        serif(); color = ink
        val title = fit(topic.title, 840f, 190f, 66f, 40f)
        val titleSize = size
        val titleHeight = (title.lines.size - 1) * title.leading + titleSize

        sans(bold = false)
        val refsHeight = if (topic.scripture.isEmpty()) 0f else 74f
        val bodyRoom = bandTop - top - titleHeight - 190f - refsHeight
        val body = fit(topic.prayer.replace(Regex("""\s+"""), " "), 860f, bodyRoom, 40f, 21f)
        val bodySize = size
        val bodyHeight = (body.lines.size - 1) * body.leading + bodySize

        val block = 60f + titleHeight + 58f + 74f + bodyHeight + refsHeight
        var y = top + ((bandTop - top - block) / 2f).coerceAtLeast(0f) + titleSize

        sans(); size = 26f; color = gold; spacing = .22f
        text(topic.category.uppercase(), 540f, y - titleSize - 34f)
        spacing = 0f

        serif(); size = titleSize; color = ink
        lines(title.lines, 540f, y, title.leading)
        y += (title.lines.size - 1) * title.leading

        val ruleY = y + 52f
        draw { drawLine(gold, Offset(450f, ruleY), Offset(630f, ruleY), strokeWidth = 2f) }

        sans(bold = false); size = bodySize; color = Color(48, 48, 52)
        y += 126f
        lines(body.lines, 540f, y, body.leading)
        y += (body.lines.size - 1) * body.leading

        if (topic.scripture.isNotEmpty()) {
            sans(); size = 26f; color = gold
            text(topic.scripture.take(3).joinToString("   ") { it.first }, 540f, y + 74f)
        }
        dusk(1430f, 1567f, 1620f)
        footer(1500f, 1540f, "Pray with us · PrayerKey")
        png()
    }

    fun answeredCard(request: String, testimony: String, answeredAt: Long): ByteArray = painter(1080, 1350).run {
        draw {
            drawRect(Color(245, 245, 247))
            drawRoundRect(Color(92, 2, 216), Offset(62f, 62f), Size(956f, 1226f), CornerRadius(54f))
        }
        sans(); color = cream; size = 27f; spacing = .18f
        text("ANSWERED PRAYER", 540f, 150f); spacing = 0f
        serif(bold = true); color = Color.White; size = 62f
        text("God was faithful.", 540f, 246f)
        sans(); color = cream; size = 22f
        text("WHAT I PRAYED FOR", 540f, 350f)
        serif(); color = Color.White; size = 38f
        var y = wrappedTo(request, 414f, 5)
        y += 82f
        sans(); color = cream; size = 22f
        text("HOW IT WAS ANSWERED", 540f, y)
        serif(italic = true); color = Color.White; size = 38f
        y = wrappedTo(testimony.ifBlank { "I am grateful for God's answer." }, y + 64f, 6)
        sans(bold = false); color = Color(255, 255, 255, 180); size = 22f
        text(longDate(answeredAt), 540f, minOf(y + 74f, 1160f))
        sans(); color = cream; size = 24f; spacing = .2f
        text("PRAYERKEY", 540f, 1230f)
        png()
    }

    /** The answered card's wrapping: every line drawn, advancing [startY]; returns the next baseline. */
    private fun CardPainter.wrappedTo(text: String, startY: Float, maxLines: Int): Float {
        var y = startY
        wrap(text.replace('\n', ' '), 820f).take(maxLines).forEach { text(it, 540f, y); y += 54f }
        return y
    }

    fun badge(title: String, detail: String): ByteArray = painter(1080, 1080).run {
        draw {
            drawRect(Color(245, 245, 247))
            drawCircle(Color(92, 2, 216), 365f, Offset(540f, 500f))
            drawCircle(cream, 315f, Offset(540f, 500f), style = Stroke(14f))
        }
        sans(); color = cream; size = 25f; spacing = .2f
        text("PRAYERKEY · UNLOCKED", 540f, 315f); spacing = 0f
        serif(bold = true); color = Color.White; size = 58f
        charWrap(title, 22).take(3).forEachIndexed { index, line -> text(line, 540f, 445f + index * 70f) }
        sans(); color = cream; size = 25f
        text(detail.uppercase(), 540f, 690f)
        color = night; size = 26f
        text("A faithful step, completed.", 540f, 950f)
        png()
    }

    private fun charWrap(text: String, limit: Int): List<String> {
        val out = mutableListOf<String>()
        var line = ""
        text.split(' ').forEach { word ->
            if ((line + word).length > limit) { out += line; line = word } else line = if (line.isBlank()) word else "$line $word"
        }
        if (line.isNotBlank()) out += line
        return out
    }

    fun prayerOfTheDay(prayer: DailyPrayer, format: CardFormat): ByteArray = painter(format.w, format.h).run {
        val w = format.w.toFloat(); val h = format.h.toFloat()
        val landscape = w > h * 1.2f
        val s = w / 1080f
        val ts = if (landscape) s * (h / w) * 1.5f else s
        val cx = w / 2f
        val brown = Color(155, 112, 64)
        draw { drawRect(Color(245, 239, 232)) }

        sans(); color = brown; size = 26f * ts; spacing = .2f
        var y = h * (if (landscape) .12f else .08f)
        text("✦  PRAYER OF THE DAY  ✦", cx, y); spacing = 0f

        y += 34f * ts
        val crossTop = y
        draw {
            drawRect(brown, Offset(cx - 5f * ts, crossTop), Size(10f * ts, 44f * ts))
            drawRect(brown, Offset(cx - 24f * ts, crossTop + 12f * ts), Size(48f * ts, 9f * ts))
        }
        y += 78f * ts

        val match = Regex("^(.*?)\\s+(\\d+[:\\d–-]*)$").find(prayer.ref)
        val book = (match?.groupValues?.get(1) ?: prayer.ref).uppercase()
        val chapterVerse = match?.groupValues?.get(2) ?: ""
        sans(); color = Color(26, 26, 26); size = 108f * ts
        y += 82f * ts
        text(book, cx, y)
        color = brown; size = 88f * ts
        y += 96f * ts
        text(chapterVerse, cx, y)

        y += 44f * ts
        val dividerY = y
        draw {
            drawLine(brown, Offset(cx - 130f * ts, dividerY), Offset(cx - 22f * ts, dividerY), strokeWidth = 2.5f * ts)
            drawLine(brown, Offset(cx + 22f * ts, dividerY), Offset(cx + 130f * ts, dividerY), strokeWidth = 2.5f * ts)
            drawCircle(brown, 7f * ts, Offset(cx, dividerY))
        }

        y += 64f * ts
        serif(); color = Color(42, 42, 42); size = 34f * ts
        y = wrapped(prayer.verse, cx, y, w * .82f, 50f * ts, 4)

        y += 40f * ts
        serif(italic = true); color = Color(90, 64, 32); size = 29f * ts
        val flat = prayer.prayer.replace("\n", " ")
        val short = flat.split(Regex("(?<=[.!?])\\s+")).drop(1).firstOrNull { it.length > 30 } ?: flat.take(140)
        wrapped(short, cx, y, w * .84f, 44f * ts, 3)

        val wy = h - (if (landscape) 34f * ts else 52f * ts)
        draw { drawLine(Color(155, 112, 64, 70), Offset(cx - 150f * ts, wy - 34f * ts), Offset(cx + 150f * ts, wy - 34f * ts), strokeWidth = 1.6f * ts) }
        sans(); color = brown; size = 26f * ts; spacing = .25f
        text("PRAYERKEY.COM", cx, wy)
        png()
    }

    /** One page of the Book of Remembrance, 1240 x 1754 as the Android PDF page. */
    fun remembrancePage(number: Int, title: String, kicker: String, blocks: List<Pair<String, String>>): ByteArray =
        painter(1240, 1754).run {
            draw { drawRect(Color(247, 244, 236)) }
            centred = false
            sans(); color = Color(176, 124, 31); size = 21f; spacing = .16f
            text(kicker.uppercase(), 92f, 105f); spacing = 0f
            serif(bold = true); color = Color(29, 29, 31); size = 48f
            text(title.take(42), 92f, 175f)
            var y = 245f
            blocks.forEach { (heading, body) ->
                sans(); size = 19f; color = Color(176, 124, 31)
                text(heading.take(80), 92f, y); y += 38f
                serif(); size = 23f; color = Color(45, 45, 48)
                charWrap(body.replace('\n', ' '), 88).take(8).forEach { line -> text(line, 92f, y); y += 33f }
                y += 34f
            }
            sans(bold = false); size = 15f; color = Color.Gray
            text("PrayerKey · Private by design · $number", 92f, 1754f - 65f)
            png()
        }

    private fun longDate(millis: Long): String {
        val d = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date
        val month = d.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$month ${d.dayOfMonth}, ${d.year}"
    }
}
