package com.prayerkey.manna.ui.challenge

import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.ui.platform.platformContext
import androidx.compose.ui.backhandler.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.ChallengePlan
import com.prayerkey.manna.data.ChallengeProgress
import com.prayerkey.manna.data.Challenges
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The shelf: every challenge, named, on one screen.
 *
 * There is nothing to configure here. A card shows what the thing is called,
 * how long it runs, and one line about what it is for — which is the entire
 * decision. Tap it and you are on day one.
 *
 * The card of a challenge already begun says which day you are on instead of
 * its promise, because at that point you do not need selling, you need
 * finding.
 */
@Composable
fun ChallengeShelf(
    onOpen: (ChallengePlan) -> Unit,
    onClose: () -> Unit,
) {
    /* The gesture has to mean the same thing as the arrow.
       Without this the whole route is one Dialog, so a back swipe dismissed
       it outright: Home -> shelf -> Day 1, swipe back, and you were on Home
       with the shelf skipped. The arrow returned to the shelf correctly, so
       the two controls disagreed about where you were. */
    BackHandler(onBack = onClose)
    val context = platformContext()
    var challenges by remember { mutableStateOf<List<ChallengePlan>>(emptyList()) }
    LaunchedEffect(Unit) { challenges = withContext(ioDispatcher) { Challenges.all(context) } }

    Column(Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(Color.White).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Pk.Charcoal, modifier = Modifier.size(17.dp)) }
        }
        Text(
            "Challenges",
            Modifier.padding(start = 20.dp, top = 6.dp),
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 34.sp,
            color = Pk.Charcoal,
        )
        Text(
            "Pick one. Start today.",
            Modifier.padding(start = 20.dp, top = 4.dp, bottom = 14.dp),
            fontFamily = UtilitySans,
            fontSize = 14.sp,
            color = Pk.Muted,
        )

        LazyColumn(
            Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 18.dp, end = 18.dp, bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(challenges, key = { it.id }) { challenge ->
                ShelfCard(
                    challenge = challenge,
                    done = ChallengeProgress.completed(context, challenge.id),
                    onOpen = { onOpen(challenge) },
                )
            }
        }
    }
}

@Composable
private fun ShelfCard(challenge: ChallengePlan, done: Int, onOpen: () -> Unit) {
    val started = done > 0
    val finished = done >= challenge.days

    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(Pk.CardRadius),
        color = Pk.Sunken,
        border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${challenge.days} days",
                    fontFamily = UtilitySans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Pk.GoldDeep,
                )
                if (started) {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (finished) "Finished" else "Day $done of ${challenge.days}",
                        fontFamily = UtilitySans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = if (finished) Pk.Sage else Pk.Oxblood,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                challenge.title,
                fontFamily = DisplaySerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                color = Pk.Charcoal,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                /* A begun challenge does not need selling, it needs finding. */
                if (started && !finished) "Continue where you stopped." else challenge.promise,
                fontFamily = UtilitySans,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Pk.Muted,
            )
            if (started && !finished) {
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier.fillMaxWidth().height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Pk.Charcoal.copy(alpha = .08f)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(done.toFloat() / challenge.days)
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Pk.Oxblood),
                    )
                }
            }
        }
    }
}

/** The ground while a plan is read off disk. See ChallengeById. */
@Composable
fun ChallengeLoading() {
    Box(Modifier.fillMaxSize().background(Pk.Cream))
}
