package com.prayerkey.manna.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import com.prayerkey.manna.ui.theme.BookSerif

/**
 * The first thing you see in an empty Journal.
 *
 * A verse arrives, sits long enough to be read, then withdraws and leaves
 * the Bible in its place — the invitation is the book, not a button.
 *
 * The scripture is KJV, which is public domain, so this costs nothing and
 * works offline like everything else. It rotates by day so the screen is
 * never the same two mornings running.
 */
private data class Line(val text: String, val ref: String)

/** Verses about writing things down and remembering them. */
private val REMEMBRANCE = listOf(
    Line("Write the vision, and make it plain upon tables, that he may run that readeth it.", "Habakkuk 2:2"),
    Line("I will remember the works of the LORD: surely I will remember thy wonders of old.", "Psalm 77:11"),
    Line("This shall be written for the generation to come.", "Psalm 102:18"),
    Line("Write it before them in a table, and note it in a book, that it may be for the time to come.", "Isaiah 30:8"),
    Line("These stones shall be for a memorial unto the children of Israel for ever.", "Joshua 4:7"),
    Line("A book of remembrance was written before him for them that feared the LORD.", "Malachi 3:16"),
    Line("Thou tellest my wanderings: put thou my tears into thy bottle: are they not in thy book?", "Psalm 56:8"),
)

@Composable
fun JournalWelcome(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val line = remember { REMEMBRANCE[(LocalDate.now().toEpochDay() % REMEMBRANCE.size).toInt()] }

    // the verse holds long enough to be read, then hands over to the book
    var showVerse by remember { mutableStateOf(false) }
    var showBook by remember { mutableStateOf(false) }
    var written by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(260); showVerse = true
        delay(5400); showVerse = false
        delay(520); showBook = true
    }

    Box(
        modifier.fillMaxSize().background(
            /* The sky the user chose. These stops are opaque blends of the
               theme's own colours rather than a translucent wash over the
               app canvas — laying gold at 16% over white and ramping to navy
               produced a grey band through the middle of the screen. */
            Brush.verticalGradient(
                0f to lerp(scheme.background, scheme.primary, .17f),
                .42f to scheme.background,
                1f to lerp(scheme.background, Color.Black, .22f),
            ),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(
                visible = showVerse,
                // the writing is the entrance now, so the block itself only
                // needs to be there — a 900ms fade over it read as a stutter
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(500)) + slideOutVertically(tween(500)) { -it / 6 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "“",
                        color = scheme.primary,
                        fontFamily = BookSerif, fontSize = 34.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    StreamedVerse(line.text, scheme.onBackground, scheme.primary) { written = true }
                    Spacer(Modifier.height(20.dp))
                    AnimatedVisibility(visible = written, enter = fadeIn(tween(600))) {
                        Text(
                            line.ref,
                            color = scheme.primary,
                            fontSize = 12.sp, letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showBook,
                enter = fadeIn(tween(700)) +
                    scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = .82f),
            ) {
                Column(
                    Modifier.clickable(onClick = onOpen),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HolyBible(scheme.primary, scheme.surface, scheme.onBackground)
                    Spacer(Modifier.height(26.dp))
                    Text(
                        "Write your vision here",
                        color = scheme.onBackground,
                        fontWeight = FontWeight.SemiBold, fontSize = 17.sp,
                    )
                }
            }
        }
    }
}

/**
 * The verse writes itself, a word at a time.
 *
 * The whole string is laid out from the first frame and the unwritten part
 * is drawn transparent, so nothing reflows as it arrives — growing a
 * centred paragraph would make every line jump sideways on each word. The
 * word at the write head carries the accent and settles to ink behind it,
 * with the next word ghosted in so text reads as arriving rather than
 * blinking on.
 */
@Composable
private fun StreamedVerse(text: String, ink: Color, accent: Color, onDone: () -> Unit) {
    val words = remember(text) { text.split(" ") }
    var head by remember(text) { mutableIntStateOf(0) }
    LaunchedEffect(text) {
        while (head < words.size) {
            // longer words take a beat longer, the way real typing does
            delay(46L + words[head].length * 8L)
            head++
        }
        onDone()
    }

    Text(
        buildAnnotatedString {
            words.forEachIndexed { i, word ->
                val color = when {
                    i < head - 1 -> ink
                    i == head - 1 -> lerp(ink, accent, .45f)
                    i == head -> ink.copy(alpha = .16f)
                    else -> Color.Transparent
                }
                withStyle(SpanStyle(color = color)) { append(word) }
                if (i != words.lastIndex) append(" ")
            }
        },
        fontFamily = BookSerif,
        fontSize = 24.sp, lineHeight = 34.sp,
        letterSpacing = (-0.3).sp,
        textAlign = TextAlign.Center,
    )
}

/**
 * A closed Bible, drawn rather than illustrated — gold cross on the cover,
 * a visible page block, and a ribbon. Vector so it wears every theme.
 */
@Composable
private fun HolyBible(accent: Color, cover: Color, ink: Color) {
    Canvas(Modifier.size(150.dp, 172.dp)) {
        val w = size.width
        val h = size.height
        val bookW = w * .74f
        val bookH = h * .82f
        val left = (w - bookW) / 2f
        val top = (h - bookH) / 2f

        // page block, offset right so the book reads as three-dimensional
        drawRoundRect(
            Color(0xFFF2ECDC),
            topLeft = Offset(left + bookW * .05f, top + bookH * .035f),
            size = Size(bookW, bookH),
            cornerRadius = CornerRadius(w * .035f),
        )
        // the leaves
        for (i in 0 until 7) {
            drawLine(
                Color(0xFFCFC5AC),
                Offset(left + bookW * .07f + bookW, top + bookH * (.12f + i * .11f)),
                Offset(left + bookW * 1.02f, top + bookH * (.12f + i * .11f)),
                strokeWidth = 1.4f,
            )
        }

        // cover
        drawRoundRect(
            cover,
            topLeft = Offset(left, top),
            size = Size(bookW, bookH),
            cornerRadius = CornerRadius(w * .035f),
        )
        /* The spine reads as a spine only if it is darker than the cover.
           Tinting it with the ink colour made it lighter on a dark theme,
           so it looked like a separate panel leaning against the book. */
        drawRoundRect(
            Color.Black.copy(alpha = .26f),
            topLeft = Offset(left, top),
            size = Size(bookW * .11f, bookH),
            cornerRadius = CornerRadius(w * .035f),
        )
        // the crease where the spine rolls into the board
        drawLine(
            Color.Black.copy(alpha = .30f),
            Offset(left + bookW * .11f, top + bookH * .03f),
            Offset(left + bookW * .11f, top + bookH * .97f),
            strokeWidth = 1.6f,
        )
        // a hairline frame, the way a bound cover is embossed
        drawRoundRect(
            accent.copy(alpha = .38f),
            topLeft = Offset(left + bookW * .19f, top + bookH * .08f),
            size = Size(bookW * .68f, bookH * .84f),
            cornerRadius = CornerRadius(w * .02f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4f),
        )

        // the cross
        val cx = left + bookW * .53f
        val cy = top + bookH * .46f
        val armW = bookW * .30f
        val stemH = bookH * .40f
        drawRoundRect(
            accent,
            topLeft = Offset(cx - bookW * .035f, cy - stemH / 2f),
            size = Size(bookW * .07f, stemH),
            cornerRadius = CornerRadius(w * .01f),
        )
        drawRoundRect(
            accent,
            topLeft = Offset(cx - armW / 2f, cy - stemH * .16f),
            size = Size(armW, bookH * .055f),
            cornerRadius = CornerRadius(w * .01f),
        )

        // ribbon marker falling from the pages
        drawRect(
            accent.copy(alpha = .85f),
            topLeft = Offset(left + bookW * .80f, top + bookH * .92f),
            size = Size(bookW * .06f, bookH * .17f),
        )
    }
}
