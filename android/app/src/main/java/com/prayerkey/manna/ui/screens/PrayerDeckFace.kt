package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.ui.theme.premiumCard
import com.prayerkey.manna.ui.theme.PaperFill
import com.prayerkey.manna.ui.theme.bloom
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Hairline
import com.prayerkey.manna.ui.theme.InkSoft
import com.prayerkey.manna.ui.theme.Ivory
import com.prayerkey.manna.ui.theme.IvoryGloss
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.R
import com.prayerkey.manna.ui.theme.TopSheenLight

/**
 * One prayer, as a full-bleed card in the shuffle deck.
 *
 * A prayer is longer than a verse, so the card shows the opening of it and
 * says so — tapping opens the full text, the scriptures and the prayer
 * points in the sheet that already exists. The deck is for finding the one
 * you need; the sheet is for praying it.
 */
@Composable
fun PrayerDeckFace(
    topic: PrayerTopic,
    front: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier,
) {
    Box(
        if (front) {
            modifier.premiumCard(fill = PaperFill).bloom(Gold, .07f).clickable(onClick = onOpen)
        } else {
            // PERF: the waiting card paints flat — no second full-screen gradient
            modifier.clip(R.card).background(Ivory)
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            if (front) {
                Box(Modifier.fillMaxSize().background(TopSheenLight))
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(230.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFFF3EDDC).copy(alpha = .94f)),
                            ),
                        ),
                )
            }

            Column(
                Modifier.fillMaxSize().padding(horizontal = 30.dp)
                    .padding(top = 132.dp, bottom = 200.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    topic.category.uppercase(),
                    color = Gold, fontSize = 10.sp, letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    topic.title,
                    color = InkSoft, fontFamily = FontFamily.Serif,
                    fontSize = if (topic.title.length > 34) 27.sp else 32.sp,
                    lineHeight = if (topic.title.length > 34) 34.sp else 39.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(20.dp))
                Text(
                    topic.prayer,
                    color = InkSoft.copy(alpha = .78f),
                    fontSize = 15.sp, lineHeight = 25.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 7,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )

                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(7.dp)) {
                    topic.scripture.take(2).forEach { (ref, _) ->
                        Surface(shape = R.pill, color = Gold.copy(alpha = .13f), border = BorderStroke(1.dp, Gold.copy(alpha = .3f))) {
                            Text(
                                ref, color = Gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                            )
                        }
                    }
                }
                if (front) {
                    Spacer(Modifier.height(12.dp))
                    Text("Tap to pray it in full", color = Muted, fontSize = 11.5.sp)
                }
            }
        }
    }
}
