package com.prayerkey.manna.ui.challenge

import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.ui.platform.platformContext
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.ChallengePlan
import com.prayerkey.manna.data.ChallengeProgress
import com.prayerkey.manna.data.Challenges
import com.prayerkey.manna.resources.Res
import com.prayerkey.manna.resources.*
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val Violet = Color(0xFF6200ED)
private val Gold = Color(0xFFFFD68C)
private val NightTop = Color(0xFF2C0A5C)
private val NightBottom = Color(0xFF0B0118)

/**
 * The shelf: every challenge, painted, on one screen.
 *
 * It reads like a church year rather than a list. The season that is coming -
 * Advent in November, Lent in February - stands at the top as a large painting
 * with the day it begins, so it can be started on day one. A challenge already
 * begun comes next, because at that point you do not need selling, you need
 * finding. Everything else sits in two rows you can swipe: the ones for any
 * week, and the seasons still to come.
 */
@Composable
fun ChallengeShelf(
    onOpen: (ChallengePlan) -> Unit,
    onClose: () -> Unit,
) {
    /* The gesture has to mean the same thing as the close button: the route
       is one Dialog, so without this a back swipe skipped the shelf. */
    BackHandler(onBack = onClose)
    val context = platformContext()
    var challenges by remember { mutableStateOf<List<ChallengePlan>>(emptyList()) }
    LaunchedEffect(Unit) { challenges = withContext(ioDispatcher) { Challenges.all(context) } }
    val today = remember { com.prayerkey.manna.platform.today() }

    val seasonal = challenges.filter { it.season != null }
        .sortedBy { it.season?.upcoming(today)?.first }
    val hero = seasonal.firstOrNull()
    val activeId = remember(challenges) { ChallengeProgress.activeId(context) }
    val active = challenges.firstOrNull { it.id == activeId }
        ?.takeIf { ChallengeProgress.completed(context, it.id) < it.days }
    val anyTime = challenges.filter { it.season == null && it != active }
    val later = seasonal.drop(1).filter { it != active }

    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(NightTop, NightBottom))),
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()),
        ) {
            Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp)) {
                Box(
                    Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f)).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp)) }
            }
            Text(
                "Challenges",
                Modifier.padding(start = 22.dp, top = 14.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 38.sp, color = Color.White,
            )
            Text(
                "Walk with God through the year.",
                Modifier.padding(start = 22.dp, top = 4.dp, bottom = 20.dp),
                fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 17.sp, color = Gold,
            )

            if (hero != null) {
                SeasonHero(hero, today, ChallengeProgress.completed(context, hero.id)) { onOpen(hero) }
            }
            if (active != null && active != hero) {
                Spacer(Modifier.height(16.dp))
                ContinueCard(active, ChallengeProgress.completed(context, active.id)) { onOpen(active) }
            }
            if (anyTime.isNotEmpty()) {
                RowHeading("For any week", "Start today")
                CardRow(anyTime) { c ->
                    PaintedCard(c, 210.dp, 290.dp, "${c.days} days", ChallengeProgress.completed(context, c.id)) { onOpen(c) }
                }
            }
            if (later.isNotEmpty()) {
                RowHeading("Through the year", "The church calendar")
                CardRow(later) { c ->
                    PaintedCard(
                        c, 170.dp, 236.dp, c.season?.label(today) ?: "${c.days} days",
                        ChallengeProgress.completed(context, c.id),
                    ) { onOpen(c) }
                }
            }
            Spacer(Modifier.height(36.dp))
        }
    }
}

/** The season that is near, as the painting the shelf opens on. */
@Composable
private fun SeasonHero(c: ChallengePlan, today: kotlinx.datetime.LocalDate, done: Int, onOpen: () -> Unit) {
    val on = c.season?.isOn(today) == true
    Box(
        Modifier.padding(horizontal = 18.dp).fillMaxWidth().height(440.dp)
            .clip(RoundedCornerShape(30.dp)).clickable(onClick = onOpen),
    ) {
        Image(painterResource(challengeArt(c)), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color.Transparent, .38f to Color.Transparent, 1f to Color(0xF012002A)),
            ),
        )
        Text(
            if (on) "IN SEASON NOW" else "COMING SEASON",
            Modifier.padding(18.dp).clip(RoundedCornerShape(99.dp)).background(Violet)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            fontFamily = UtilitySans, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = Color.White,
        )
        Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
            Text(
                "${c.days} DAYS  ·  ${c.season?.label(today).orEmpty().uppercase()}",
                fontFamily = UtilitySans, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = Gold,
            )
            Text(
                c.title,
                Modifier.padding(top = 8.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 36.sp, color = Color.White,
            )
            Text(
                c.promise,
                Modifier.padding(top = 8.dp),
                fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 22.sp, color = Color.White.copy(alpha = .85f),
            )
            Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when { done >= c.days -> "Read it again"; done > 0 -> "Continue day ${done + 1}"; on -> "Begin day 1"; else -> "Look inside" },
                    Modifier.clip(RoundedCornerShape(99.dp)).background(Gold).padding(horizontal = 22.dp, vertical = 12.dp),
                    fontFamily = UtilitySans, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E0078),
                )
            }
        }
    }
}

/** The challenge already begun: where you stopped, and how far you have come. */
@Composable
private fun ContinueCard(c: ChallengePlan, done: Int, onOpen: () -> Unit) {
    Row(
        Modifier.padding(horizontal = 18.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = .09f)).clickable(onClick = onOpen).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(challengeArt(c)), null,
            Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop,
        )
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                "CONTINUE  ·  DAY ${done + 1} OF ${c.days}",
                fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = Gold,
            )
            Text(
                c.title, Modifier.padding(top = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis,
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, color = Color.White,
            )
            Box(
                Modifier.padding(top = 10.dp).fillMaxWidth().height(5.dp).clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(alpha = .15f)),
            ) {
                Box(
                    Modifier.fillMaxWidth(done.toFloat() / c.days).height(5.dp).clip(RoundedCornerShape(99.dp))
                        .background(Brush.horizontalGradient(listOf(Violet, Gold))),
                )
            }
        }
    }
}

@Composable
private fun RowHeading(title: String, kicker: String) {
    Column(Modifier.padding(start = 22.dp, top = 30.dp, bottom = 12.dp)) {
        Text(kicker.uppercase(), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp, color = Gold.copy(alpha = .8f))
        Text(title, Modifier.padding(top = 2.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Color.White)
    }
}

@Composable
private fun CardRow(items: List<ChallengePlan>, card: @Composable (ChallengePlan) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) { items(items, key = { it.id }) { card(it) } }
}

/** A tall painted card: the art, the length or the season, the name. */
@Composable
private fun PaintedCard(c: ChallengePlan, w: Dp, h: Dp, kicker: String, done: Int, onOpen: () -> Unit) {
    Box(Modifier.width(w).height(h).clip(RoundedCornerShape(24.dp)).clickable(onClick = onOpen)) {
        Image(painterResource(challengeArt(c)), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color.Transparent, .35f to Color.Transparent, 1f to Color(0xEE12002A)),
            ),
        )
        if (done > 0) {
            Text(
                if (done >= c.days) "Finished" else "Day $done of ${c.days}",
                Modifier.padding(12.dp).clip(RoundedCornerShape(99.dp)).background(Violet).padding(horizontal = 10.dp, vertical = 4.dp),
                fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
            )
        }
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(
                kicker.uppercase(), maxLines = 1,
                fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = Gold,
            )
            Text(
                c.title, Modifier.padding(top = 5.dp), maxLines = 3, overflow = TextOverflow.Ellipsis,
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 23.sp, color = Color.White,
            )
        }
    }
}

/**
 * Each challenge's painting. Chosen by hand, so no two neighbours share one and
 * each picture says what the challenge is for; the asset's own cover is the
 * fallback for anything added later.
 */
private fun challengeArt(c: ChallengePlan): DrawableResource = when (c.id) {
    "three-days-before-a-decision" -> Res.drawable.discovery_cover_5
    "standing-against-fear" -> Res.drawable.rhythm_evening
    "the-midnight-watch" -> Res.drawable.discovery_cover_2
    "standing-in-scripture" -> Res.drawable.discovery_cover_1
    "fasting-and-prayer" -> Res.drawable.discovery_cover_3
    "new-year-21-days" -> Res.drawable.welcome_prayer_dawn
    "lent-40" -> Res.drawable.today_prayer_violet
    "holy-week" -> Res.drawable.closing_verse_violet
    "pentecost-10" -> Res.drawable.discovery_cover_4
    "advent-24" -> Res.drawable.rhythm_reflection
    "thanksgiving-7" -> Res.drawable.prayerkey_hero_dawn
    "back-to-school-7" -> Res.drawable.onboarding_audio_v3
    "mothers-and-fathers" -> Res.drawable.discovery_cover_6
    else -> when (c.cover) {
        "discovery_cover_1" -> Res.drawable.discovery_cover_1
        "discovery_cover_2" -> Res.drawable.discovery_cover_2
        "discovery_cover_3" -> Res.drawable.discovery_cover_3
        "discovery_cover_4" -> Res.drawable.discovery_cover_4
        "discovery_cover_5" -> Res.drawable.discovery_cover_5
        else -> Res.drawable.discovery_cover_6
    }
}

/** The ground while a plan is read off disk. See ChallengeById. */
@Composable
fun ChallengeLoading() {
    Box(Modifier.fillMaxSize().background(Pk.Cream))
}
