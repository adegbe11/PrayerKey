package com.prayerkey.manna.ui.worlds

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.R
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.R as AppR

/* Ivory on night, gold for the small marks. */
private val Ink = Color(0xFF222222)
private val Oxblood = Color(0xFF6200ED)
private val Gilt = Color(0xFFC9A26D)

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
    val artwork = remember(reference) {
        val cards = intArrayOf(
            AppR.drawable.verse_card_botanical,
            AppR.drawable.verse_card_dawn,
            AppR.drawable.verse_card_oxblood,
        )
        cards[(reference.hashCode() and Int.MAX_VALUE) % cards.size]
    }

    /* No rounded clip. The card IS the screen — corners belong to a card that
       sits on a page, and this one has no page behind it. */
    Box(modifier) {
        Image(
            painter = painterResource(artwork),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            Modifier.fillMaxSize().padding(horizontal = 54.dp)
                .padding(top = 230.dp, bottom = bottomPadding.coerceAtLeast(225.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (world.isPromise) {
                Box(
                    Modifier.clip(R.pill).background(Oxblood.copy(alpha = .08f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        "PROMISE CARD",
                        color = Oxblood, fontSize = 9.sp,
                        letterSpacing = 2.6.sp, fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(18.dp))
            }

            Text(
                "“$text”",
                color = Ink,
                fontFamily = BookSerif,
                fontSize = when {
                    text.length > 260 -> 19.sp
                    text.length > 170 -> 22.sp
                    else -> 27.sp
                },
                lineHeight = when {
                    text.length > 260 -> 28.sp
                    text.length > 170 -> 32.sp
                    else -> 38.sp
                },
                letterSpacing = (-0.2).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))

            // a short gold rule, so the reference does not crowd the verse
            Box(
                Modifier.height(1.dp).fillMaxWidth(.22f)
                    .background(Gilt.copy(alpha = .80f)),
            )

            Spacer(Modifier.height(16.dp))

            /* Reference only. The translation was printed here and again on
               the picker pill at the top of the screen — the same three
               letters twice on one small display. */
            Text(
                reference,
                color = Oxblood,
                fontSize = 12.sp, letterSpacing = 1.6.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
