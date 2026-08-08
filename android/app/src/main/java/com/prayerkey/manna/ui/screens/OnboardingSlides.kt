package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import com.prayerkey.manna.ui.theme.goldKey
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.Path
import com.prayerkey.manna.ui.theme.Leaf
import com.prayerkey.manna.ui.theme.StampedGold
import com.prayerkey.manna.ui.theme.BookSerif
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.Canvas as CanvasColour
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.InkSoft
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.NightFill
import com.prayerkey.manna.ui.theme.R

private val Ivory = Color(0xFFF6F0E1)

/**
 * Three slides that say plainly what the app does — verses, prayers, and
 * the Sunday recording that becomes a journal.
 *
 * The weight pattern is deliberate: every headline opens with a verb, and
 * the bold falls on the verb and the payoff word. Only the middle slide
 * carries a subline, because it is the one with a number worth saying.
 */
private data class Slide(
    val headline: AnnotatedString,
    val sub: String?,
    val art: @Composable () -> Unit,
)

@Composable
private fun heavy(vararg parts: Pair<String, Boolean>): AnnotatedString = buildAnnotatedString {
    parts.forEachIndexed { i, (text, bold) ->
        if (i > 0) append(" ")
        if (bold) withStyle(SpanStyle(fontWeight = FontWeight.Black)) { append(text) } else append(text)
    }
}

@Composable
fun OnboardingSlides(onSkip: () -> Unit, onDone: () -> Unit, step: Int, onStep: (Int) -> Unit) {
    val slides = listOf(
        Slide(
            headline = heavy("Read" to true, "One Bible Verse" to false, "Every Morning" to true),
            sub = null,
            art = { VerseCardArt() },
        ),
        Slide(
            headline = heavy("Find" to true, "A Prayer For" to false, "Anything You Face" to true),
            sub = "543 prayers for healing, family, money, fear and grief. " +
                "Or type what's happening and get one written for you.",
            art = { PrayerFanArt() },
        ),
        Slide(
            headline = heavy("Record" to true, "The Sermon." to false, "Journal" to true, "It All." to false),
            sub = null,
            art = { ListenArt() },
        ),
    )
    val slide = slides[step.coerceIn(0, slides.lastIndex)]

    Column(
        Modifier.fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
            .padding(horizontal = 30.dp),
    ) {
        Spacer(Modifier.height(18.dp))

        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            slide.art()
        }

        Spacer(Modifier.height(26.dp))

        /* Garamond, like the rest of the app. This was Roboto Bold, so the
           first screen anyone saw was set in a different typeface from the
           dashboard it leads to. */
        Text(
            slide.headline,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
            fontFamily = BookSerif,
            fontSize = 36.sp, lineHeight = 44.sp,
            letterSpacing = (-0.8).sp,
            fontWeight = FontWeight.Normal,
        )

        slide.sub?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = .62f), fontSize = 13.5.sp, lineHeight = 21.sp)
        }

        Spacer(Modifier.height(26.dp))

        Row(
            Modifier.fillMaxWidth().padding(bottom = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Skip",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = .62f), fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onSkip),
            )
            Spacer(Modifier.weight(1f))

            // page dots, so three screens do not feel endless
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(slides.size) { i ->
                    Box(
                        Modifier.size(if (i == step) 20.dp else 6.dp, 6.dp)
                            .clip(R.pill)
                            .background(if (i == step) Gold else Color(0xFFDCDCE2)),
                    )
                }
            }
            Spacer(Modifier.weight(1f))

            Box(
                Modifier.size(56.dp)
                    .shadow(14.dp, CircleShape, spotColor = Gold.copy(alpha = .45f))
                    .clip(CircleShape).background(NightFill)
                    .clickable { if (step < slides.lastIndex) onStep(step + 1) else onDone() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.ArrowForward, "Next", tint = Gold, modifier = Modifier.size(22.dp))
            }
        }
    }
}

/* ───────────────────────── slide artwork ───────────────────────── */

/**
 * The card they pull each morning.
 *
 * The first version was a squarish navy box with a hard `drawRect` across it
 * at 62% for the "ground" — which read as a rendering seam rather than a
 * horizon — and the mark floating tiny in the middle of it. It is portrait
 * now, in the proportion the real card actually has, with a dusk sky that
 * falls to a hill rather than meeting a straight line, and a verse block
 * where the verse really sits.
 */
@Composable
private fun VerseCardArt() {
    Box(
        Modifier.fillMaxWidth(.70f).fillMaxHeight()
            .shadow(30.dp, R.card, spotColor = Color(0xFF000000).copy(alpha = .45f))
            .clip(R.card).background(NightFill)
            .border(1.dp, Leaf.copy(alpha = .28f), R.card),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // dusk, warm at the horizon and cool overhead
            drawRect(
                Brush.verticalGradient(
                    0f to Color(0xFF20294A),
                    .48f to Color(0xFF3A3350),
                    .70f to Color(0xFF7A5B3C),
                    1f to Color(0xFF2A1C13),
                ),
            )
            // the sun, low
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFFFF3D0), Color(0xFFE9C27E).copy(alpha = .35f), Color.Transparent),
                    center = Offset(w * .60f, h * .34f), radius = w * .46f,
                ),
                radius = w * .46f, center = Offset(w * .60f, h * .34f),
            )
            drawCircle(Color(0xFFFCF3DC), radius = w * .058f, center = Offset(w * .60f, h * .34f))

            // two hills, so the horizon is a shape and not a rule
            val far = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * .74f)
                cubicTo(w * .28f, h * .66f, w * .62f, h * .80f, w, h * .70f)
                lineTo(w, h); close()
            }
            drawPath(far, Color(0xFF2E2434).copy(alpha = .92f))
            val near = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * .86f)
                cubicTo(w * .34f, h * .79f, w * .70f, h * .92f, w, h * .84f)
                lineTo(w, h); close()
            }
            drawPath(near, Color(0xFF171326))
        }

        // where the verse sits on the real card
        /* Low on the card, the way WorldVerseFace sets it — the sky stays
           open above. Centred, the sun came up behind the verse and the gold
           rule ran straight into it. */
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp).padding(bottom = 30.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(Modifier.size(26.dp)) {
                goldKey(Offset(size.width / 2f, size.height / 2f), height = size.height)
            }
            Spacer(Modifier.height(10.dp))
            StampedGold("MANNA", size = 11.sp, tracking = 3.5.sp)
            Spacer(Modifier.height(20.dp))
            repeat(3) { i ->
                Box(
                    Modifier.padding(bottom = 7.dp)
                        .fillMaxWidth(if (i == 2) .58f else .9f)
                        .height(5.dp).clip(R.pill)
                        .background(Ivory.copy(alpha = .40f)),
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.width(46.dp).height(4.dp).clip(R.pill)
                    .background(Leaf.copy(alpha = .75f)),
            )
        }
    }
}

/** Three prayer cards fanned, the way the deck feels in the hand. */
@Composable
private fun PrayerFanArt() {
    Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
        listOf(
            Triple(-14f, (-64).dp, Color(0xFFDDE5EC)),
            Triple(7f, 46.dp, Color(0xFFF3DFDA)),
            Triple(-2f, 0.dp, Color(0xFFF6EEDC)),
        ).forEachIndexed { i, (deg, dx, fill) ->
            Box(
                Modifier.padding(start = if (dx > 0.dp) dx else 0.dp, end = if (dx < 0.dp) -dx else 0.dp)
                    .rotate(deg)
                    .fillMaxWidth(.46f).height(220.dp)
                    .shadow(if (i == 2) 22.dp else 10.dp, R.card, spotColor = Color(0xFF14182A).copy(alpha = .28f))
                    .clip(R.card).background(fill),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    // a motif hint at the foot of each card
                    drawRect(
                        Color.Black.copy(alpha = .07f),
                        topLeft = Offset(0f, size.height * .68f),
                        size = Size(size.width, size.height * .32f),
                    )
                    listOf(.30f, .40f, .50f).forEach { y ->
                        drawRect(
                            Color.Black.copy(alpha = .13f),
                            topLeft = Offset(size.width * .14f, size.height * y),
                            size = Size(size.width * .72f, 5f),
                        )
                    }
                }
            }
        }
    }
}

/** A mic ringed by sound, with a page catching the words. */
@Composable
private fun ListenArt() {
    Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width * .5f, size.height * .46f)
            listOf(.34f, .46f, .58f).forEachIndexed { i, r ->
                drawCircle(
                    Gold.copy(alpha = .30f - i * .08f),
                    size.minDimension * r, c,
                    style = Stroke(width = 2f),
                )
            }
            // the note page, tucked under
            val w = size.width * .30f
            val h = size.height * .34f
            drawRoundRect(
                Color.White,
                topLeft = Offset(size.width * .58f, size.height * .56f),
                size = Size(w, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f),
            )
            listOf(.62f, .70f, .78f).forEach { y ->
                drawRect(
                    Color(0xFFCFCFD6),
                    topLeft = Offset(size.width * .63f, size.height * y),
                    size = Size(w * .66f, 4f),
                )
            }
        }
        Box(
            Modifier.size(96.dp)
                .shadow(20.dp, CircleShape, spotColor = Color(0xFF14182A).copy(alpha = .4f))
                .clip(CircleShape).background(NightFill),
            contentAlignment = Alignment.Center,
        ) {
            // drawn, not an emoji: a system glyph renders in its own colours
            // and breaks a scene built from gold line work
            Canvas(Modifier.size(40.dp)) {
                val w = size.width
                val h = size.height
                drawRoundRect(
                    Gold,
                    topLeft = Offset(w * .32f, h * .06f),
                    size = Size(w * .36f, h * .50f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .18f),
                )
                drawArc(
                    Gold, 0f, 180f, false,
                    topLeft = Offset(w * .18f, h * .34f),
                    size = Size(w * .64f, h * .42f),
                    style = Stroke(width = w * .075f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                )
                drawLine(
                    Gold, Offset(w * .5f, h * .76f), Offset(w * .5f, h * .93f),
                    strokeWidth = w * .075f, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
                drawLine(
                    Gold, Offset(w * .32f, h * .95f), Offset(w * .68f, h * .95f),
                    strokeWidth = w * .075f, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
        }
    }
}
