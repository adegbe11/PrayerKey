package com.prayerkey.manna.ui.screens

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.ui.theme.R
import com.prayerkey.manna.ui.worlds.VerseWorld
import com.prayerkey.manna.ui.worlds.WorldScene

private val Ivory = Color(0xFFF6F0E1)
private val Gilt = Color(0xFFC9A24B)

/**
 * A prayer, in the same world language as the verses.
 *
 * The deck used to be paper-light with dark ink while the Bible deck was
 * full-bleed night — two products in one app. This is the same card: a
 * world behind it, ivory serif centred on the axis, gold for the marks.
 *
 * The card carries the opening of the prayer, not the whole thing. Finding
 * the right prayer is the deck's job; praying it is the sheet's.
 */
@Composable
fun PrayerDeckFace(
    topic: PrayerTopic,
    front: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier,
) {
    val world = remember(topic.slug) { worldFor(topic.category, topic.slug) }

    Box(
        (if (front) modifier.clickable(onClick = onOpen) else modifier).clip(R.card),
    ) {
        // PERF: only the front card animates its scene
        WorldScene(world, animate = front, Modifier.fillMaxSize())

        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = .42f),
                    .30f to Color.Black.copy(alpha = .22f),
                    .60f to Color.Black.copy(alpha = .52f),
                    1f to Color.Black.copy(alpha = .84f),
                ),
            ),
        )

        Column(
            Modifier.fillMaxSize().padding(horizontal = 30.dp)
                .padding(top = 150.dp, bottom = 200.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                topic.category.uppercase(),
                color = Gilt, fontSize = 9.5.sp,
                letterSpacing = 3.2.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(18.dp))

            Text(
                topic.title,
                color = Ivory,
                fontFamily = FontFamily.Serif,
                fontSize = if (topic.title.length > 34) 27.sp else 32.sp,
                lineHeight = if (topic.title.length > 34) 35.sp else 40.sp,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))
            Box(Modifier.height(1.dp).fillMaxWidth(.22f).background(Gilt.copy(alpha = .55f)))
            Spacer(Modifier.height(20.dp))

            Text(
                topic.prayer,
                color = Ivory.copy(alpha = .84f),
                fontSize = 15.sp, lineHeight = 26.sp,
                textAlign = TextAlign.Center,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
            )

            if (topic.scripture.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    topic.scripture.take(2).forEach { (ref, _) ->
                        Text(
                            ref, color = Gilt, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold, letterSpacing = 1.1.sp,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Gives each kind of prayer a world that matches what it is about, so the
 * deck reads as one place rather than a colour lottery.
 */
private fun worldFor(category: String, slug: String): VerseWorld {
    val c = category.lowercase()
    return when {
        c.contains("health") || c.contains("healing") -> VerseWorld.DAWN
        c.contains("mental") || c.contains("anxiet") || c.contains("fear") -> VerseWorld.WATCH
        c.contains("grief") || c.contains("loss") || c.contains("sorrow") -> VerseWorld.SEA
        c.contains("family") || c.contains("relationship") || c.contains("marriage") -> VerseWorld.PASTURE
        c.contains("financ") || c.contains("money") || c.contains("provision") -> VerseWorld.HARVEST
        c.contains("work") || c.contains("career") || c.contains("business") -> VerseWorld.CITY
        c.contains("direction") || c.contains("guidance") || c.contains("purpose") -> VerseWorld.HEIGHTS
        c.contains("faith") || c.contains("worship") || c.contains("spiritual") -> VerseWorld.THRONE
        c.contains("growth") || c.contains("fruit") -> VerseWorld.GARDEN
        c.contains("thirst") || c.contains("renew") -> VerseWorld.RIVER
        else -> {
            val pool = VerseWorld.entries.filter { !it.isPromise }
            pool[(slug.fold(11) { a, ch -> a * 31 + ch.code } and 0x7fffffff) % pool.size]
        }
    }
}
