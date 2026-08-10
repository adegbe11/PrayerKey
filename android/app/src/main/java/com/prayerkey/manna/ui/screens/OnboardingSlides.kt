package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import com.prayerkey.manna.ui.theme.goldKey
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.Path
import com.prayerkey.manna.ui.theme.Leaf
import com.prayerkey.manna.ui.theme.StampedGold
import com.prayerkey.manna.ui.theme.BookSerif
import androidx.compose.foundation.layout.width
import com.prayerkey.manna.ui.worlds.VerseWorld
import com.prayerkey.manna.ui.worlds.WorldScene
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    val stories = listOf(
        com.prayerkey.manna.R.drawable.onboarding_bible to
            "Start every morning with one Bible verse.",
        com.prayerkey.manna.R.drawable.onboarding_prayer to
            "Tell God what you’re facing. Find a prayer for it.",
        com.prayerkey.manna.R.drawable.onboarding_church to
            "Record the sermon. Manna writes the notes.",
        com.prayerkey.manna.R.drawable.onboarding_journal to
            "Keep your prayers, answers and growth in one private journal.",
    )

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp),
    ) {
        Spacer(Modifier.height(18.dp))
        Text(
            "PRAYERKEY",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = BookSerif,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.2.sp,
        )
        Spacer(Modifier.height(18.dp))

        AnimatedContent(
        targetState = step.coerceIn(0, stories.lastIndex),
        modifier = Modifier.weight(1f),
        transitionSpec = {
            (androidx.compose.animation.slideInVertically(
                animationSpec = spring(dampingRatio = .9f, stiffness = Spring.StiffnessLow),
                initialOffsetY = { it / 3 },
            ) + fadeIn()) togetherWith
                (androidx.compose.animation.slideOutVertically(
                    animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessLow),
                    targetOffsetY = { -it / 4 },
                ) + fadeOut())
        },
        label = "onboarding-promise",
    ) { page ->
            val (world, message) = stories[page]
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    EditorialOnboardingImage(world)
                }
                Text(
                    message,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 31.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-.7).sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 22.dp),
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        AnimatedVisibility(step == stories.lastIndex, enter = fadeIn(tween(600))) {
            Box(
                Modifier.fillMaxWidth().height(56.dp)
                    .shadow(6.dp, R.pill, spotColor = Color.Black.copy(alpha = .16f))
                    .clip(R.pill).background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onDone),
                contentAlignment = Alignment.Center,
            ) {
                Text("Continue", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (step != stories.lastIndex) Spacer(Modifier.height(56.dp))
        Spacer(Modifier.height(28.dp))
    }
}

/**
 * One photographic language across onboarding. The image is intentionally
 * treated as an editorial plate rather than a full-screen wallpaper: the
 * rounded edge, quiet keyline and small natural shadow keep the type readable
 * and make the sequence feel like a considered book rather than an advert.
 */
@Composable
private fun EditorialOnboardingImage(imageRes: Int) {
    Box(
        modifier = Modifier.fillMaxSize()
            .shadow(10.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = .18f))
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .08f), RoundedCornerShape(28.dp)),
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
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
            .shadow(14.dp, R.card, spotColor = Color.Black.copy(alpha = .24f))
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

/**
 * Three prayer cards fanned, the way the deck feels in the hand.
 *
 * These were three blank tinted rectangles. They are three real cards now,
 * each a different world from the same set the deck actually draws from, with
 * a category, a title and the shape of a prayer on it — so the slide shows
 * the thing the app contains rather than a placeholder for it.
 *
 * Drawn rather than photographed, and deliberately: the deck's cards *are*
 * drawn worlds, so a photograph here would be a promise the app then breaks.
 */
@Composable
private fun PrayerFanArt() {
    Box(Modifier.fillMaxWidth().fillMaxHeight(), contentAlignment = Alignment.Center) {
        listOf(
            Triple(VerseWorld.WATCH, "FEAR" to "When I cannot sleep", -13f),
            Triple(VerseWorld.PASTURE, "FAMILY" to "For those I love", 13f),
            Triple(VerseWorld.DAWN, "HEALING" to "For a body that hurts", -2f),
        ).forEachIndexed { i, (world, label, deg) ->
            val (category, title) = label
            val front = i == 2
            /* Offset, not padding. Padding squeezed the layout so the two
               back cards ended up almost entirely behind the front one —
               three cards showing as one card and two slivers. */
            Box(
                Modifier
                    .offset(
                        x = when (i) { 0 -> (-62).dp; 1 -> 62.dp; else -> 0.dp },
                        y = if (front) 10.dp else (-6).dp,
                    )
                    .rotate(deg)
                    .fillMaxWidth(.415f).fillMaxHeight(if (front) .86f else .76f)
                    .shadow(if (front) 14.dp else 7.dp, R.card, spotColor = Color.Black.copy(alpha = .22f))
                    .clip(R.card)
                    .border(1.dp, Leaf.copy(alpha = if (front) .3f else .16f), R.card),
            ) {
                WorldScene(world, animate = false, Modifier.fillMaxSize())
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = .34f),
                            .45f to Color.Black.copy(alpha = .12f),
                            1f to Color.Black.copy(alpha = .78f),
                        ),
                    ),
                )
                /* Only the front card is lettered. The two behind it are
                   overlapped by design, and a half-covered "FEA…" reads as a
                   clipping bug rather than as a card underneath. */
                if (front) Column(
                    Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        category,
                        color = Leaf, fontSize = 7.5.sp,
                        letterSpacing = 2.sp, fontWeight = FontWeight.Bold,
                    )
                    Text(
                        title,
                        color = Ivory, fontFamily = BookSerif,
                        fontSize = 13.sp, lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                    Box(
                        Modifier.padding(top = 8.dp).width(26.dp).height(2.dp)
                            .clip(R.pill).background(Leaf.copy(alpha = .8f)),
                    )
                }
            }
        }
    }
}

/**
 * The note the sermon becomes.
 *
 * This was a mic ringed by sound with a blank white rectangle beside it —
 * abstract, where the other two slides show a real artefact, and small enough
 * to leave a hand's depth of empty page under it. A slide about recording the
 * sermon should show the thing you get, which is a page with the sermon on it.
 */
@Composable
private fun ListenArt() {
    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {

        // the room listening, behind the page
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width * .5f, size.height * .40f)
            listOf(.28f, .40f, .52f, .64f).forEachIndexed { i, r ->
                drawCircle(
                    Gold.copy(alpha = .26f - i * .05f),
                    size.minDimension * r, c,
                    style = Stroke(width = 1.6f),
                )
            }
        }

        // the page
        Box(
            Modifier.fillMaxWidth(.66f).fillMaxHeight(.92f)
                .shadow(12.dp, R.card, spotColor = Color.Black.copy(alpha = .18f))
                .clip(R.card).background(Color(0xFFFFFDF8))
                .border(1.dp, Color(0xFFE6DCC6), R.card)
                .padding(horizontal = 18.dp, vertical = 20.dp),
        ) {
            Column {
                Text(
                    "SUNDAY", color = Gold,
                    fontSize = 8.5.sp, letterSpacing = 2.6.sp, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "The Weight of Grace",
                    color = Color(0xFF2C2A26), fontFamily = BookSerif,
                    fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(12.dp))

                // the references it heard, which is the feature's real trick
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Rom 8:28", "Eph 2:8").forEach { ref ->
                        Box(
                            Modifier.clip(R.pill).background(Gold.copy(alpha = .12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(ref, color = Gold, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Canvas(Modifier.fillMaxWidth().weight(1f)) {
                    /* The body, as ruled lines: real words at this size would
                       be a texture pretending to be readable. Filled to the
                       foot of the page — a fixed row count left the lower half
                       of the leaf blank, which read as a half-written note. */
                    val widths = listOf(1f, .92f, .97f, .74f, 1f, .88f, .95f, .58f, .94f, .8f, .96f, .69f)
                    var y = 0f
                    var i = 0
                    while (y < size.height - 4f) {
                        drawRect(
                            Color(0xFF2C2A26).copy(alpha = .16f),
                            topLeft = Offset(0f, y),
                            size = Size(size.width * widths[i % widths.size], 3.2f),
                        )
                        y += 15f
                        i++
                    }
                }
            }
        }

        // the mic, clipped to the page's corner the way a badge sits on paper
        Box(
            /* Top right, over the one empty corner. On the left it sat
               squarely on the SUNDAY kicker and clipped it to "UNDAY". */
            Modifier.align(Alignment.TopEnd).padding(end = 20.dp, top = 12.dp)
                .size(62.dp)
                .shadow(7.dp, CircleShape, spotColor = Color.Black.copy(alpha = .22f))
                .clip(CircleShape).background(NightFill),
            contentAlignment = Alignment.Center,
        ) {
            // drawn, not an emoji: a system glyph renders in its own colours
            // and breaks a scene built from gold line work
            Canvas(Modifier.size(28.dp)) {
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
                    style = Stroke(width = w * .085f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                )
                drawLine(
                    Gold, Offset(w * .5f, h * .76f), Offset(w * .5f, h * .93f),
                    strokeWidth = w * .085f, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
                drawLine(
                    Gold, Offset(w * .32f, h * .95f), Offset(w * .68f, h * .95f),
                    strokeWidth = w * .085f, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
            }
        }
    }
}
