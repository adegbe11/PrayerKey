package com.prayerkey.manna.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.resources.Res
import com.prayerkey.manna.resources.*
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** One way to pray, as a tall painted tile. */
data class Moment(val kicker: String, val title: String, val art: DrawableResource, val open: () -> Unit)

/**
 * Pray at every hour: the prayers that were written for particular moments -
 * the hours of the day, the moment you need help, ten quiet minutes - as one
 * shelf you can swipe, each tile opening its own prayer.
 */
@Composable
fun MomentsShelf(moments: List<Moment>) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp)) {
        Text(
            "Pray at every hour",
            Modifier.padding(start = 22.dp, bottom = 12.dp),
            fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Pk.Charcoal,
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(moments, key = { it.title }) { m ->
                Box(
                    Modifier.width(150.dp).height(200.dp).clip(RoundedCornerShape(22.dp)).clickable(onClick = m.open),
                ) {
                    Image(painterResource(m.art), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD12002A)), startY = 140f)))
                    Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                        Text(m.kicker.uppercase(), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.2.sp, color = Pk.GoldLight)
                        Text(m.title, Modifier.padding(top = 4.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 21.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

/** The shelf's tiles, in the order a day is lived, with help first. */
fun prayerMoments(
    emergency: () -> Unit,
    office: (String) -> Unit,
    tenMinutes: () -> Unit,
    prayerPoints: () -> Unit,
): List<Moment> = listOf(
    Moment("Right now", "I need prayer", Res.drawable.welcome_prayer_dawn, emergency),
    Moment("Morning", "Morning Prayer", Res.drawable.rhythm_morning, { office("MORNING") }),
    Moment("Ten minutes", "Ten quiet minutes", Res.drawable.onboarding_pray_v3, tenMinutes),
    Moment("Afternoon", "Midday Prayer", Res.drawable.prayerkey_hero_dawn, { office("AFTERNOON") }),
    Moment("Evening", "Evening Prayer", Res.drawable.rhythm_evening, { office("EVENING") }),
    Moment("Night", "Night Prayer", Res.drawable.rhythm_reflection, { office("NIGHT") }),
    Moment("The watch", "Midnight Prayer", Res.drawable.discovery_cover_2, { office("MIDNIGHT") }),
    Moment("Declare", "Prayer points", Res.drawable.today_prayer_violet, prayerPoints),
)
