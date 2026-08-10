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
import com.prayerkey.manna.ui.theme.BookSerif

private val Ivory = Color(0xFFF4F8FF)
private val Gilt = Color(0xFF78A9FF)

/**
 * A prayer, in the same world language as the verses.
 *
 * The deck used to be paper-light with dark ink while the Bible deck was
 * full-bleed night — two products in one app. This is the same card: a
 * world behind it, ivory serif centred on the axis, gold for the marks.
 *
 * The card shows the whole prayer whenever it fits, and when it does not it
 * stops at the end of a sentence rather than mid-word, and says so. Finding
 * the right prayer is the deck's job; praying it is the sheet's — but a card
 * that trails off at "Where there is …" tells you nothing either way.
 *
 * Sharing is never abridged: CardShareRenderer.sharePrayer fits the type to
 * the prayer, because whoever receives it is meant to pray it.
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
                // 150dp of the card was empty sky above the category; most
                // prayers fit whole in the space that reclaims
                .padding(top = 96.dp, bottom = 190.dp),
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
                fontFamily = BookSerif,
                fontSize = if (topic.title.length > 34) 27.sp else 32.sp,
                lineHeight = if (topic.title.length > 34) 35.sp else 40.sp,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))
            Box(Modifier.height(1.dp).fillMaxWidth(.22f).background(Gilt.copy(alpha = .55f)))
            Spacer(Modifier.height(20.dp))

            val (body, trimmed) = remember(topic.slug) { preview(topic.prayer) }
            Text(
                body,
                color = Ivory.copy(alpha = .86f),
                fontSize = 15.sp, lineHeight = 26.sp,
                textAlign = TextAlign.Center,
                maxLines = 12,
                overflow = TextOverflow.Ellipsis,
            )
            if (trimmed) {
                Spacer(Modifier.height(14.dp))
                Text(
                    "Read the full prayer",
                    color = Gilt, fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = .6.sp,
                )
            }

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

/**
 * How much of a prayer the card shows.
 *
 * A fixed character budget rather than a measured layout: it is coarse, but
 * it is deterministic, and it lets the cut land on a full stop. The old
 * six-line clamp ended wherever the line happened to break — usually
 * mid-word, always mid-thought.
 *
 * Returns the text to draw and whether anything was held back.
 */
private const val CARD_BUDGET = 380

internal fun preview(prayer: String): Pair<String, Boolean> {
    val clean = prayer.replace(Regex("""\s+"""), " ").trim()
    if (clean.length <= CARD_BUDGET) return clean to false

    val head = clean.take(CARD_BUDGET)
    val stop = maxOf(head.lastIndexOf(". "), head.lastIndexOf("! "), head.lastIndexOf("? "))
    // only honour a sentence break if it leaves a worthwhile amount of prayer
    return if (stop > CARD_BUDGET / 2) clean.take(stop + 1) to true
    else head.substringBeforeLast(' ') + "…" to true
}
