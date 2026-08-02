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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.R

/**
 * A verse standing inside its world.
 *
 * Text sits at the bottom over a scrim rather than centred, so the scene
 * stays visible — that is the whole point of drawing it. Promise cards
 * announce themselves with a tag; everything else names its world quietly.
 */
@Composable
fun WorldVerseFace(
    reference: String,
    text: String,
    translation: String,
    front: Boolean,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 210.dp,
) {
    val world = remember(reference, text) { WorldPicker.forVerse(reference, text) }
    // PERF: only the front card animates, and never under reduce-motion
    val animate = front && !reduceMotion

    Box(modifier.clip(R.card)) {
        WorldScene(world, animate, Modifier.fillMaxSize())

        // scrim: keeps the sky readable at the top, the words readable below
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = .22f),
                    .42f to Color.Transparent,
                    .68f to Color.Black.copy(alpha = .38f),
                    1f to Color.Black.copy(alpha = .72f),
                ),
            ),
        )

        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp)
                .padding(top = 96.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.Bottom,
        ) {
            if (world.isPromise) {
                Box(
                    Modifier.clip(R.pill).background(Color(0xFFFFF4D6).copy(alpha = .92f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        "PROMISE CARD",
                        color = Color(0xFF3A2A08), fontSize = 9.sp,
                        letterSpacing = 2.4.sp, fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            Text(
                world.label.uppercase(),
                color = Color.White.copy(alpha = .62f),
                fontSize = 9.sp, letterSpacing = 2.8.sp, fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                "$reference  ·  $translation",
                color = Color.White.copy(alpha = .9f),
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .4.sp,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "“$text”",
                color = Color.White,
                fontFamily = FontFamily.Serif,
                fontSize = if (text.length > 190) 21.sp else 27.sp,
                lineHeight = if (text.length > 190) 30.sp else 37.sp,
                letterSpacing = (-0.3).sp,
            )
        }
    }
}
