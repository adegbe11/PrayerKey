package com.prayerkey.manna.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.ChallengePlan
import com.prayerkey.manna.data.Challenges
import com.prayerkey.manna.data.PrayingFor
import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.resources.Res
import com.prayerkey.manna.resources.*
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val Violet = Color(0xFF6200ED)
private val Gold = Color(0xFFFFD68C)
private val DeepViolet = Color(0xFF2E0078)

/**
 * Go deeper: the longer ways to seek God, as four painted tiles in two rows.
 *
 * They were four full-width cards of the same shape and the same gradient,
 * stacked, which read as a wall to scroll past. As a grid they are seen at
 * once, and the fourth tile is the church year: whatever season is coming,
 * with the day it begins.
 */
@Composable
fun GoDeeperGrid(
    challengeLine: String,
    fastLine: String,
    onChallenge: () -> Unit,
    onFast: () -> Unit,
    onStill: () -> Unit,
    onSeason: () -> Unit,
) {
    val context = platformContext()
    var season by remember { mutableStateOf<ChallengePlan?>(null) }
    val date = remember { today() }
    LaunchedEffect(Unit) {
        season = withContext(ioDispatcher) {
            Challenges.all(context).filter { it.season != null }.minByOrNull { it.season?.upcoming(date)?.first?.toEpochDays()?.toLong() ?: Long.MAX_VALUE }
        }
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
        Text(
            "Go deeper", Modifier.padding(bottom = 12.dp, top = 6.dp),
            fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Color(0xFF1D1D1F),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Tile(Res.drawable.discovery_cover_5, challengeLine, "Prayer challenge", Modifier.weight(1f), onChallenge)
            Tile(Res.drawable.discovery_cover_3, fastLine, "Start a fast", Modifier.weight(1f), onFast)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Tile(Res.drawable.rhythm_reflection, "Meditation", "Be still with God", Modifier.weight(1f), onStill)
            val s = season
            Tile(
                s?.let { com.prayerkey.manna.ui.challenge.challengeArt(it) } ?: Res.drawable.rhythm_evening,
                s?.season?.label(date) ?: "Through the year",
                s?.title ?: "Seasons of prayer",
                Modifier.weight(1f), onSeason,
                badge = if (s?.season?.isNear(date) == true) "SEASON" else null,
            )
        }
    }
}

@Composable
private fun Tile(art: DrawableResource, kicker: String, title: String, modifier: Modifier, onClick: () -> Unit, badge: String? = null) {
    Box(modifier.aspectRatio(.82f).clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick)) {
        Image(painterResource(art), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color.Transparent, .3f to Color.Transparent, 1f to Color(0xEE12002A)),
            ),
        )
        if (badge != null) {
            Text(
                badge, Modifier.padding(12.dp).clip(RoundedCornerShape(99.dp)).background(Violet).padding(horizontal = 9.dp, vertical = 4.dp),
                fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.2.sp, color = Color.White,
            )
        }
        Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
            Text(kicker.uppercase(), maxLines = 1, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 9.5.sp, letterSpacing = 1.1.sp, color = Gold)
            Text(
                title, Modifier.padding(top = 4.dp), maxLines = 3,
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 22.sp, color = Color.White,
            )
        }
    }
}

/**
 * Someone on the Praying For list who has not been prayed for today.
 *
 * The list is only kept if it is seen, and the place it is seen is Home: one
 * name at a time, with the prayer one tap away. It goes once everyone on the
 * list has been prayed for today.
 */
@Composable
fun PrayForTodayCard(onOpenList: () -> Unit) {
    val context = platformContext()
    LaunchedEffect(Unit) { PrayingFor.load(context) }
    val people by PrayingFor.people.collectAsState()
    val day = today().toEpochDays().toLong()
    val waiting = people.filter { it.answered == 0L && !it.prayedOn(day) }
    val next = waiting.firstOrNull() ?: return
    Row(
        Modifier.padding(horizontal = 22.dp, vertical = 6.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF2C0A5C), Color(0xFF4A12A8))))
            .clickable(onClick = onOpenList).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(Gold),
            contentAlignment = Alignment.Center,
        ) { Text(next.name.take(1).uppercase(), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = DeepViolet) }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(
                if (waiting.size > 1) "PRAYING FOR  ·  ${waiting.size} TODAY" else "PRAYING FOR",
                fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.4.sp, color = Gold,
            )
            Text(
                "Pray for ${next.name} today", Modifier.padding(top = 2.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, color = Color.White,
            )
            if (next.request.isNotBlank()) {
                Text(next.request, maxLines = 1, fontFamily = UtilitySans, fontSize = 13.sp, color = Color.White.copy(alpha = .7f))
            }
        }
        Text(
            "I prayed",
            Modifier.clip(RoundedCornerShape(99.dp)).background(Color.White)
                .clickable { PrayingFor.prayed(context, next.id, day) }.padding(horizontal = 14.dp, vertical = 10.dp),
            fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Violet,
        )
    }
}

/** The page ends the way a service does: with the blessing. Numbers 6:24-26, KJV. */
@Composable
fun HomeBenediction() {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 40.dp).padding(top = 36.dp, bottom = 118.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(Res.drawable.prayerkey_mark), null, Modifier.size(34.dp))
        Text(
            "The LORD bless thee, and keep thee: The LORD make his face shine upon thee, and be gracious unto thee: The LORD lift up his countenance upon thee, and give thee peace.",
            Modifier.padding(top = 16.dp),
            fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 27.sp,
            color = Color(0xFF4A2A86), textAlign = TextAlign.Center,
        )
        Text(
            "NUMBERS 6:24-26", Modifier.padding(top = 12.dp),
            fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.8.sp, color = Color(0xFF9A7A3A),
        )
    }
}
