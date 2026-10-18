package com.prayerkey.manna.ui.walk

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.prayerkey.manna.data.Walk
import com.prayerkey.manna.data.WalkState
import com.prayerkey.manna.platform.localDateOfEpochDay
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.ui.platform.Haptic
import com.prayerkey.manna.ui.platform.fullScreenDialog
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.platform.rememberHaptics
import com.prayerkey.manna.ui.platform.shareText
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private fun todayIndex() = today().toEpochDays().toLong()

/** The small line under the date on Home: how long the walk is, or a gentle nudge to keep it. */
@Composable
fun WalkChip(state: WalkState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val today = todayIndex()
    val days = state.current(today)
    if (days <= 0) return
    val waiting = !state.walkedOn(today)
    Row(
        modifier.clip(RoundedCornerShape(50)).background(if (waiting) Pk.Oxblood.copy(alpha = .08f) else Pk.GoldLight.copy(alpha = .35f))
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Flame(lit = !waiting, modifier = Modifier.size(12.dp))
        Text(
            if (waiting) "Keep your $days-day walk today" else "$days ${if (days == 1) "day" else "days"} with God",
            Modifier.padding(start = 5.dp),
            fontFamily = UtilitySans, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
            color = if (waiting) Pk.Oxblood else Pk.GoldDeep,
        )
    }
}

/** A small lamp flame, lit gold when today is walked. */
@Composable
fun Flame(lit: Boolean, modifier: Modifier = Modifier) {
    val sway = rememberInfiniteTransition(label = "flame")
    val t by sway.animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse), label = "t")
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val lean = if (lit) (t - .5f) * w * .12f else 0f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w / 2 + lean, 0f)
            cubicTo(w * .95f, h * .45f, w * .9f, h, w / 2, h)
            cubicTo(w * .1f, h, w * .05f, h * .45f, w / 2 + lean, 0f)
            close()
        }
        drawPath(path, Brush.verticalGradient(if (lit) listOf(Color(0xFFFFE6A8), Color(0xFFE09A2A)) else listOf(Pk.Oxblood.copy(.5f), Pk.Oxblood)))
    }
}

/** The walk, opened: the number, the last four weeks, and the rest days in hand. */
@Composable
fun WalkSheet(state: WalkState, onClose: () -> Unit) {
    val today = todayIndex()
    val days = state.current(today)
    Dialog(onDismissRequest = onClose, properties = fullScreenDialog()) {
        Box(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2A0A58), Color(0xFF12002A), Color(0xFF07000F))))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Column(Modifier.padding(28.dp).clickable(enabled = false) {}, horizontalAlignment = Alignment.CenterHorizontally) {
                Flame(lit = state.walkedOn(today), modifier = Modifier.size(34.dp, 44.dp))
                Text("$days", Modifier.padding(top = 12.dp), fontFamily = DisplaySerif, fontSize = 92.sp, color = Color.White)
                Text(if (days == 1) "day with God" else "days with God", fontFamily = BookSerif, fontSize = 20.sp, color = Pk.GoldLight)
                Spacer(Modifier.height(30.dp))
                FourWeeks(state, today)
                Spacer(Modifier.height(26.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    Stat("${state.best}", "longest walk")
                    Stat("${state.restDays}", if (state.restDays == 1) "rest day kept" else "rest days kept")
                }
                Text(
                    "Pray, read, listen or write and the day is kept. Every seven days you earn a rest day, and it covers a day you miss.",
                    Modifier.padding(top = 26.dp).width(300.dp),
                    fontFamily = UtilitySans, fontSize = 13.sp, lineHeight = 19.sp, color = Color.White.copy(.7f), textAlign = TextAlign.Center,
                )
                Text("Close", Modifier.padding(top = 26.dp).clickable(onClick = onClose).padding(10.dp), fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = DisplaySerif, fontSize = 30.sp, color = Color.White)
        Text(label, fontFamily = UtilitySans, fontSize = 12.sp, color = Color.White.copy(.6f))
    }
}

@Composable private fun FourWeeks(state: WalkState, today: Long) {
    // four rows of seven, ending today, Sunday first
    val todayDate = localDateOfEpochDay(today)
    val offset = todayDate.dayOfWeek.ordinal.let { (it + 1) % 7 } // Sunday = 0
    val first = today - offset - 21
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(it, Modifier.width(30.dp), fontFamily = UtilitySans, fontSize = 11.sp, color = Color.White.copy(.45f), textAlign = TextAlign.Center)
            }
        }
        repeat(4) { w ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(7) { d ->
                    val day = first + w * 7 + d
                    val walked = state.walkedOn(day)
                    val rested = state.restedOn(day)
                    val future = day > today
                    Box(
                        Modifier.size(30.dp).clip(CircleShape)
                            .background(when { walked -> Pk.Oxblood; else -> Color.White.copy(if (future) .02f else .06f) })
                            .then(if (rested) Modifier.border(1.5.dp, Pk.GoldLight, CircleShape) else Modifier)
                            .then(if (day == today) Modifier.border(1.5.dp, Color.White, CircleShape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (walked) Box(Modifier.size(6.dp).clip(CircleShape).background(Pk.GoldLight))
                    }
                }
            }
        }
    }
}

private fun milestoneLine(days: Int) = when (days) {
    3 -> "Three days. A habit is beginning."
    7 -> "A whole week with God."
    14 -> "Two weeks of showing up."
    21 -> "Twenty-one days. This is who you are now."
    30 -> "A month of meeting with God."
    40 -> "Forty days, like Moses on the mountain."
    50 -> "Fifty days. Keep going."
    100 -> "One hundred days with God."
    365 -> "A whole year with God."
    else -> "$days days with God."
}

/**
 * The moment a milestone is reached: the number rises out of gold light with
 * sparks around it, and a word for the road. Shown once, then gone.
 */
@Composable
fun WalkCelebration() {
    val step by Walk.celebrate.collectAsState()
    val reached = step ?: return
    val days = reached.milestone ?: return
    val context = platformContext()
    val haptics = rememberHaptics()
    val rise = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    LaunchedEffect(days) {
        haptics.perform(Haptic.Confirm)
        rise.animateTo(1f, spring(dampingRatio = .62f, stiffness = 140f))
    }
    LaunchedEffect(days) { burst.animateTo(1f, tween(1600, easing = FastOutSlowInEasing)) }
    Dialog(onDismissRequest = { Walk.celebrated() }, properties = fullScreenDialog()) {
        Box(
            Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF4A1A8A), Color(0xFF12002A), Color(0xFF07000F)), radius = 1400f)),
            contentAlignment = Alignment.Center,
        ) {
            // sparks thrown outward from the number
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2, size.height * .42f)
                repeat(28) { i ->
                    val a = (i / 28f) * 2 * PI.toFloat() + i
                    val r = (120f + (i % 5) * 50f) * burst.value
                    val alpha = (1f - burst.value).coerceIn(0f, 1f)
                    drawCircle(Color(0xFFFFD68C).copy(alpha = alpha), radius = 3f + (i % 3), center = c + Offset(cos(a) * r, sin(a) * r))
                }
                // the glow starts at nothing; a gradient cannot have a zero radius
                val glow = 420f * rise.value.coerceAtLeast(.01f)
                drawCircle(Brush.radialGradient(listOf(Color(0x66FFC46E), Color(0x00FFC46E)), center = c, radius = glow), radius = glow, center = c)
            }
            Column(
                Modifier.padding(32.dp).graphicsLayer { alpha = rise.value; translationY = (1f - rise.value) * 80f },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Flame(lit = true, modifier = Modifier.size(38.dp, 50.dp))
                Text("$days", Modifier.padding(top = 10.dp), fontFamily = DisplaySerif, fontSize = 120.sp, color = Color.White)
                Text(milestoneLine(days), fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 24.sp, lineHeight = 32.sp, color = Pk.GoldLight, textAlign = TextAlign.Center)
                Text(
                    "Let us not be weary in well doing: for in due season we shall reap, if we faint not.",
                    Modifier.padding(top = 26.dp).width(300.dp),
                    fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 24.sp, color = Color.White.copy(.82f), textAlign = TextAlign.Center,
                )
                Text("GALATIANS 6:9", Modifier.padding(top = 10.dp), fontFamily = UtilitySans, fontSize = 11.sp, letterSpacing = 2.sp, color = Pk.GoldLight.copy(.8f))
                if (reached.restEarned) {
                    Text("You earned a rest day for a hard day.", Modifier.padding(top = 22.dp), fontFamily = UtilitySans, fontSize = 13.sp, color = Color.White.copy(.7f))
                }
                Row(Modifier.padding(top = 34.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Share",
                        Modifier.clip(RoundedCornerShape(50)).border(1.dp, Color.White.copy(.4f), RoundedCornerShape(50))
                            .clickable { shareText(context, "$days days with God on PrayerKey. ${milestoneLine(days)}\nhttps://www.prayerkey.com") }
                            .padding(horizontal = 26.dp, vertical = 14.dp),
                        fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, color = Color.White,
                    )
                    Text(
                        "Amen",
                        Modifier.clip(RoundedCornerShape(50)).background(Pk.GoldLight).clickable { Walk.celebrated() }
                            .padding(horizontal = 30.dp, vertical = 14.dp),
                        fontFamily = UtilitySans, fontWeight = FontWeight.Bold, color = Color(0xFF2A1200),
                    )
                }
            }
        }
    }
}
