package com.prayerkey.manna.ui.worlds

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.R

/**
 * A verse as a printed devotional card.
 *
 * Two decisions carried over from the physical ones:
 *
 *  - PAPER GROUND, DARK INK. Light card, dark words. A dark photo with
 *    white text over it is a wallpaper; this is a card, and it screenshots
 *    like one.
 *  - THE LOAD-BEARING WORDS ARE BOLD. "a REFUGE for the oppressed, a
 *    STRONGHOLD in times of trouble". That is not decoration — it is how
 *    people hold a verse in memory, and it gives the eye somewhere to land.
 */
@Composable
fun ScriptureCard(
    reference: String,
    text: String,
    translation: String,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 28.dp,
    topPadding: Dp = 34.dp,
) {
    val world = remember(reference, text) { WorldPicker.forVerse(reference, text) }
    val palette = remember(world, reference) { paletteFor(world, reference) }
    val motif = remember(world) { world.motif() }
    val body = remember(text, palette) { emphasise(text, palette.ink) }

    Box(modifier.clip(R.card).background(palette.ground)) {
        Canvas(Modifier.fillMaxSize()) {
            cardMotif(motif, palette, reference)
            settleMotif(palette.ground)
        }

        /* Words at the top, art raised to meet them. Centring the block
           inside a band only moved the hole from below the verse to above
           it — the fix is to close the gap from the art side instead. */
        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = 26.dp)
                .padding(top = topPadding),
            verticalArrangement = Arrangement.Top,
        ) {
            if (world.isPromise) {
                Text(
                    "PROMISE",
                    color = palette.accent, fontSize = 10.sp,
                    letterSpacing = 3.sp, fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(14.dp))
            }

            Text(
                body,
                color = palette.ink,
                fontSize = if (text.length > 190) 22.sp else 28.sp,
                lineHeight = if (text.length > 190) 32.sp else 39.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.2).sp,
            )

            Spacer(Modifier.height(20.dp))
            Text(
                "$reference · $translation",
                color = palette.ink.copy(alpha = .62f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = .4.sp,
            )
        }
    }
}

/**
 * Bolds the words the verse actually turns on.
 *
 * Heuristic, on device, no model: skip the function words, prefer the long
 * and concrete, and cap it at three so emphasis still means something. KJV
 * already sets LORD in caps, which is a free signal we honour.
 */
private val CARRIERS_SKIP = (
    "that this with from have will your shall unto they them were when what there " +
        "their into upon which whom whose because while would could should might must " +
        "thou thee thy thine ye and the for but not are was his her him she you our " +
        "hath doth then than also every much many more most any all some such " +
        "shalt art thereof therein whosoever saith said say came went thing things"
    ).split(" ").toHashSet()

/**
 * The words scripture actually turns on. Length alone picked "BURIED" over
 * "PEACE" in Genesis 15:15 — longer is not the same as load-bearing, so the
 * words that carry weight are named outright.
 */
private val CARRIERS_STRONG = (
    "love peace faith hope joy mercy grace trust rest refuge strength stronghold " +
        "salvation saviour redeemer shepherd comfort courage strong fear afraid " +
        "forgive forgiven righteous holy glory power mighty light life living " +
        "eternal everlasting heal healed healing whole save saved deliver " +
        "delivered rescue restore renew renewed new born spirit truth word " +
        "promise covenant faithful faithfulness compassion kindness gentle " +
        "patient humble wisdom understanding knowledge counsel guide lead " +
        "provide provision bless blessed blessing abundance enough " +
        "wait waited quiet still silence pray prayer answered answer " +
        "heart soul mind body strength broken contrite humble " +
        "victory overcome conquer triumph freedom free liberty " +
        "shame guilt sin repent return home father son king lord god " +
        "wings eagles mountains valley waters river fire refiner " +
        "harvest fruit vine branch seed grow " +
        "goodness good perfect complete lacking nothing all sufficient"
    ).split(" ").toHashSet()

internal fun emphasise(text: String, ink: androidx.compose.ui.graphics.Color): AnnotatedString {
    val tokens = Regex("[\\p{L}’']+|[^\\p{L}’']+").findAll(text).map { it.value }.toList()

    val scored = tokens.withIndex()
        .filter { (_, t) -> t.length > 3 && t.all { it.isLetter() || it == '\'' || it == '’' } }
        .filter { (_, t) -> t.lowercase() !in CARRIERS_SKIP }
        .map { (i, t) ->
            val low = t.lowercase()
            var score = 0
            if (low in CARRIERS_STRONG) score += 40      // meaning beats length
            if (t == t.uppercase() && t.length > 2) score += 12   // KJV sets LORD in caps
            score += t.length
            i to score
        }
        // a word has to be genuinely significant, not merely long
        .filter { it.second >= 12 }
        .sortedByDescending { it.second }

    val chosen = scored.take(3).map { it.first }.toHashSet()
    if (chosen.isEmpty()) return AnnotatedString(text)

    return buildAnnotatedString {
        tokens.forEachIndexed { i, t ->
            if (i in chosen) {
                withStyle(SpanStyle(fontWeight = FontWeight.Black, color = ink)) {
                    append(t.uppercase())
                }
            } else {
                append(t)
            }
        }
    }
}
