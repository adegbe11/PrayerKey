package com.prayerkey.manna.ui.worlds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.R

/* Ivory on night, gold for the small marks. */
private val Ivory = Color(0xFFF6F0E1)
private val Gilt = Color(0xFFC9A24B)

/**
 * A verse inside its world — full bleed, edge to edge.
 *
 * The card IS the screen. An inset card with a drop shadow reads as a
 * printed object, which is a different product; the world has to fill the
 * frame so pulling feels like moving through places rather than shuffling
 * paper.
 *
 * Everything is centred on the vertical axis and the block sits low: the
 * sky stays open above it, and the words land where the thumb already is.
 */
@Composable
fun WorldVerseFace(
    reference: String,
    text: String,
    translation: String,
    front: Boolean,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 210.dp,
) {
    val world = remember(reference, text) { WorldPicker.forVerse(reference, text) }
    // PERF: only the front card animates, and never under reduce-motion
    val animate = front && !reduceMotion

    Box(modifier.clip(R.card)) {
        WorldScene(world, animate, Modifier.fillMaxSize())

        /* One scrim, weighted low: the scene stays open up top and the words
           never fight the art underneath them. */
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = .28f),
                    .34f to Color.Black.copy(alpha = .10f),
                    .62f to Color.Black.copy(alpha = .46f),
                    1f to Color.Black.copy(alpha = .80f),
                ),
            ),
        )

        Column(
            Modifier.fillMaxSize().padding(horizontal = 30.dp)
                .padding(top = 120.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (world.isPromise) {
                Box(
                    Modifier.clip(R.pill).background(Color(0xFFFFF4D6).copy(alpha = .92f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        "PROMISE CARD",
                        color = Color(0xFF3A2A08), fontSize = 9.sp,
                        letterSpacing = 2.6.sp, fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(18.dp))
            }

            Text(
                "“$text”",
                color = Ivory,
                fontFamily = FontFamily.Serif,
                fontSize = if (text.length > 190) 22.sp else 28.sp,
                lineHeight = if (text.length > 190) 32.sp else 39.sp,
                letterSpacing = (-0.2).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))

            // a short gold rule, so the reference does not crowd the verse
            Box(
                Modifier.height(1.dp).fillMaxWidth(.22f)
                    .background(Gilt.copy(alpha = .55f)),
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "$reference  ·  $translation",
                color = Gilt,
                fontSize = 12.sp, letterSpacing = 1.6.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
