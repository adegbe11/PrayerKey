package com.prayerkey.manna.ui.challenge

import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.ChallengePlan
import com.prayerkey.manna.data.ChallengeProgress
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans

/**
 * One day of a challenge.
 *
 * Three parts, in this order, on every day of every challenge: the passage,
 * the reflection, the points to pray aloud. It never varies, which is the
 * point — a shape learned once is a shape you stop having to learn, and it is
 * the single most copied thing about the plans people actually finish.
 *
 * The passage is read from the bundled KJV rather than stored with the day, so
 * the words here are the same words as everywhere else in the app and no verse
 * in the content file can drift from the Bible.
 */
@Composable
fun ChallengeDayScreen(
    challenge: ChallengePlan,
    /**
     * Whether the shelf is behind this.
     *
     * An arrow promises a level above it. Opened from the shelf there is one;
     * opened straight from a reminder there is not, and the arrow was telling
     * the reader they had come from somewhere they had never been.
     */
    pushed: Boolean = true,
    onClose: () -> Unit,
) {
    /* The gesture has to mean the same thing as the arrow.
       Without this the whole route is one Dialog, so a back swipe dismissed
       it outright: Home -> shelf -> Day 1, swipe back, and you were on Home
       with the shelf skipped. The arrow returned to the shelf correctly, so
       the two controls disagreed about where you were. */
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    var day by remember(challenge.id) {
        mutableIntStateOf(ChallengeProgress.resumeDay(context, challenge))
    }
    val today = challenge.written.firstOrNull { it.day == day } ?: return
    var passage by remember(today.day) { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }

    LaunchedEffect(challenge.id) { ChallengeProgress.start(context, challenge.id) }
    LaunchedEffect(today.day) {
        val verses = OfflineBible(context).chapter(today.read.book, today.read.chapter)
        passage = verses
            .filter { it.verse in today.read.from..today.read.to }
            .map { it.verse to it.text }
    }

    Column(Modifier.fillMaxSize().background(Pk.Cream)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(Color.White).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (pushed) Icons.Outlined.ArrowBack else Icons.Outlined.Close,
                    if (pushed) "Back" else "Close",
                    tint = Pk.Charcoal, modifier = Modifier.size(17.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                challenge.title.uppercase(),
                fontFamily = UtilitySans,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp,
                color = Pk.Muted,
                maxLines = 1,
            )
        }

        /* One bar per day. On a 21-day challenge these are thin; that is
           honest about the length rather than hiding it. */
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            repeat(challenge.days) { i ->
                Box(
                    Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(99.dp))
                        .background(if (i < day) Pk.Oxblood else Pk.Charcoal.copy(alpha = .10f)),
                )
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).navigationBarsPadding(),
        ) {
            Spacer(Modifier.height(22.dp))
            Text(
                "DAY $day OF ${challenge.days}",
                fontFamily = UtilitySans,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp,
                color = Pk.GoldDeep,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                today.title,
                fontFamily = DisplaySerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                color = Pk.Charcoal,
            )

            Spacer(Modifier.height(24.dp))
            Surface(
                shape = RoundedCornerShape(Pk.CardRadius),
                color = Pk.GoldWash,
                border = BorderStroke(1.dp, Pk.Gold.copy(alpha = .28f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "READ",
                        fontFamily = UtilitySans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.4.sp,
                        color = Pk.GoldDeep,
                    )
                    Spacer(Modifier.height(10.dp))
                    passage.forEach { (number, text) ->
                        Text(
                            "$number  $text",
                            fontFamily = BookSerif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 17.sp,
                            lineHeight = 27.sp,
                            color = Pk.Charcoal,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    Text(
                        /* DevoReading.rangeLabel always prints from-to, which
                           reads "MATTHEW 4:4-4" on a single verse. A range of
                           one is not a range. */
                        spanLabel(today.read).uppercase() + " · KJV",
                        fontFamily = UtilitySans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.6.sp,
                        color = Pk.Muted,
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            today.reflection.forEach { paragraph ->
                Text(
                    paragraph,
                    fontFamily = BookSerif,
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    color = Pk.Charcoal,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(Pk.CardRadius),
                color = Pk.Oxblood.copy(alpha = .06f),
                border = BorderStroke(1.dp, Pk.Oxblood.copy(alpha = .18f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "PRAY THIS ALOUD",
                        fontFamily = UtilitySans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.4.sp,
                        color = Pk.Oxblood,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        today.prayer,
                        fontFamily = BookSerif,
                        fontSize = 18.sp,
                        lineHeight = 30.sp,
                        color = Pk.Charcoal,
                    )
                }
            }

            if (today.question.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(Pk.CardRadius),
                    color = Pk.Sunken,
                    border = BorderStroke(1.dp, Pk.Hair),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            "SIT WITH THIS",
                            fontFamily = UtilitySans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.4.sp,
                            color = Pk.Muted,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            today.question,
                            fontFamily = DisplaySerif,
                            fontSize = 19.sp,
                            lineHeight = 27.sp,
                            color = Pk.Charcoal,
                        )
                    }
                }
            }

            if (today.hasFamily) {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(Pk.CardRadius),
                    color = Pk.Sunken,
                    border = BorderStroke(1.dp, Pk.Hair),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            "FOR THE HOUSE",
                            fontFamily = UtilitySans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.4.sp,
                            color = Pk.GoldDeep,
                        )
                        if (today.familyQuestion.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                today.familyQuestion,
                                fontFamily = DisplaySerif,
                                fontSize = 18.sp,
                                lineHeight = 26.sp,
                                color = Pk.Charcoal,
                            )
                        }
                        if (today.familyPrayer.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                today.familyPrayer,
                                fontFamily = BookSerif,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                color = Pk.Muted,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            DoneButton(
                next = day + 1,
                last = day >= challenge.days,
                onDone = {
                    ChallengeProgress.complete(context, challenge.id, day)
                    if (day < challenge.days) day++ else onClose()
                },
            )
            Spacer(Modifier.height(36.dp))
        }
    }
}

/** "Matthew 4:4" for one verse, "Joel 2:12-13" for more than one. */
private fun spanLabel(read: com.prayerkey.manna.data.DevoReading): String =
    if (read.from >= read.to) "${read.book} ${read.chapter}:${read.from}"
    else "${read.book} ${read.chapter}:${read.from}–${read.to}"

@Composable
private fun DoneButton(next: Int, last: Boolean, onDone: () -> Unit) {
    /* "Day 2" rather than "I have prayed this". The plans people finish end
       the day by naming the next one; asking a reader to certify their own
       praying adds a small verdict to every morning. */
    Surface(
        onClick = onDone,
        shape = RoundedCornerShape(99.dp),
        color = Pk.Oxblood,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                if (last) "Finish" else "Day $next",
                fontFamily = UtilitySans,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}
