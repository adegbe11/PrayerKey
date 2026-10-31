package com.prayerkey.manna.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PrayerSeason
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans

private val Gold = Color(0xFFE8C46A)
private val NightCard = Brush.linearGradient(listOf(Color(0xFF2A1250), Color(0xFF120A24)))

/**
 * The one thing Today asks: the day's prayer points. Everything else on Home
 * sits below it, and most of it one tap further away.
 */
@Composable
fun TodayPointsHero(
    title: String,
    minutes: Int,
    points: Int,
    /** Points prayed today; equal to [points] once finished. */
    done: Int,
    /** False until the day's prayer has loaded; the card waits rather than offering an empty session. */
    ready: Boolean = true,
    /** The day's devotional, read first: the Word, then prayer. Null hides the line. */
    devotional: String? = null,
    openDevotional: () -> Unit = {},
    open: () -> Unit,
) {
    val finished = ready && points > 0 && done >= points
    Surface(
        onClick = open,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 6.dp,
    ) {
        Column(Modifier.background(NightCard).padding(24.dp)) {
            Text("TODAY'S PRAYER POINTS", color = Gold, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.8.sp)
            Text(title, Modifier.padding(top = 12.dp), color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 35.sp)
            Text(
                if (ready) "About $minutes ${if (minutes == 1) "minute" else "minutes"} · $points points · read to you" else "Getting today's points ready",
                Modifier.padding(top = 10.dp), color = Color.White.copy(alpha = .66f), fontFamily = UtilitySans, fontSize = 14.sp,
            )
            if (devotional != null) {
                Row(
                    Modifier.padding(top = 16.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .08f))
                        .clickable(onClick = openDevotional).padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Read first: ", color = Color.White.copy(alpha = .6f), fontFamily = UtilitySans, fontSize = 13.sp)
                    Text(devotional, Modifier.weight(1f, fill = false), color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Icon(Icons.Outlined.ChevronRight, null, tint = Color.White.copy(alpha = .6f), modifier = Modifier.size(18.dp))
                }
            }
            if (ready) {
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(if (finished) Color.Transparent else Gold)
                        .border(1.5.dp, Gold, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (finished) Text("✓", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    else Icon(Icons.Outlined.PlayArrow, null, tint = Color(0xFF1E0D38), modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        when {
                            finished -> "Prayed today"
                            done > 0 -> "Continue"
                            else -> "Begin"
                        },
                        color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                    )
                    Text(
                        when {
                            finished -> "Pray them again any time"
                            done > 0 -> "Point ${done + 1} of $points is next"
                            else -> "With a voice and quiet music"
                        },
                        color = Color.White.copy(alpha = .6f), fontFamily = UtilitySans, fontSize = 13.sp,
                    )
                }
            }
            }
            if (ready && points > 0) {
                Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(points) { i ->
                        Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (i < done) Gold else Color.White.copy(alpha = .14f)))
                    }
                }
            }
        }
    }
}

/** The season the church is praying in, when there is one. */
@Composable
fun SeasonCard(season: PrayerSeason, open: () -> Unit) {
    Surface(
        onClick = open,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        shape = RoundedCornerShape(22.dp),
        color = Pk.Sunken,
        border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(season.title.uppercase(), color = Pk.GoldDeep, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Text(season.line, Modifier.padding(top = 6.dp), color = Pk.Charcoal, fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 21.sp)
                Text(season.button, Modifier.padding(top = 10.dp), color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted)
        }
    }
}

/** Everything else Home used to hold, one tap away instead of one long scroll. */
@Composable
fun MoreWaysToPrayEntry(open: () -> Unit) {
    Surface(
        onClick = open,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        shape = RoundedCornerShape(22.dp),
        color = Pk.Sunken,
        border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("More ways to pray", color = Pk.Charcoal, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "Devotional, the Bible, prayer through the day, fasting, challenges",
                    Modifier.padding(top = 4.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.5.sp, lineHeight = 19.sp,
                )
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted)
        }
    }
}

/** The needs most people come to pray about, one tap each, under the prayer box. */
@Composable
fun PrayerNeeds(pick: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            "Healing" to "healing for my body",
            "Family" to "my family",
            "Finances" to "my finances and provision",
            "Protection" to "protection from every evil",
            "Fear" to "peace over my fear and anxiety",
            "Grief" to "comfort in my grief",
            "Breakthrough" to "a breakthrough I am waiting for",
            "Guidance" to "guidance for a decision",
        ).forEach { (label, need) ->
            Surface(onClick = { pick(need) }, shape = RoundedCornerShape(99.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair)) {
                Text(label, Modifier.padding(horizontal = 15.dp, vertical = 9.dp), color = Pk.Charcoal, fontFamily = UtilitySans, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
        }
    }
}

/** The longer journeys, one door: fasts, prayer challenges, seasons. */
@Composable
fun JourneysEntry(line: String, open: () -> Unit) {
    Surface(
        onClick = open,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
        shape = RoundedCornerShape(22.dp),
        color = Pk.Sunken,
        border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Fasts and challenges", color = Pk.Charcoal, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Text(line, Modifier.padding(top = 4.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.5.sp)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted)
        }
    }
}
