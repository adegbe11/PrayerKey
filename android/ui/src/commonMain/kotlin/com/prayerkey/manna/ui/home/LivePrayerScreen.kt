package com.prayerkey.manna.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.LivePrayer
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.ui.platform.Haptic
import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.platform.rememberDictation
import com.prayerkey.manna.ui.platform.rememberHaptics
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

private val LiveNight = Color(0xFF12002A)
private val LiveGold = Color(0xFFF1DDAE)
private val LiveRed = Color(0xFFFF4D5E)

private enum class Phase { Ask, Verses, Praying, Done }

/**
 * Live prayer.
 *
 * What do you want to pray about? You say it, the screen gives you the
 * verses for it, and when you are ready you start praying against a timer.
 * The phone taps once when the time is up.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LivePrayerScreen(
    date: LocalDate,
    topics: List<PrayerTopic>,
    onClose: () -> Unit,
    onFinished: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val haptics = rememberHaptics()

    var phase by remember { mutableStateOf(Phase.Ask) }
    var request by remember { mutableStateOf("") }
    var topicTitle by remember { mutableStateOf<String?>(null) }
    var verses by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var minutes by remember { mutableIntStateOf(LivePrayer.DEFAULT_MINUTES) }
    var remaining by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }

    var speechReady by remember { mutableStateOf(false) }
    val speaker = remember { Speaker(context) { speechReady = it == Speaker.SUCCESS } }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }
    fun say(words: String) {
        if (speechReady) { speaker.setSpeechRate(.9f); speaker.speak(words, Speaker.QUEUE_FLUSH, null, "live") }
    }

    val dictation = rememberDictation("What do you want to pray about?") { spoken ->
        request = if (request.isBlank()) spoken else "$request $spoken"
    }

    fun findVerses(what: String) {
        request = what
        val (title, found) = LivePrayer.versesFor(what, topics)
        topicTitle = title
        verses = found
        haptics.perform(Haptic.Tap)
        phase = Phase.Verses
    }

    // the clock, which only runs while praying and not paused
    LaunchedEffect(phase, paused) {
        if (phase != Phase.Praying || paused) return@LaunchedEffect
        while (remaining > 0) {
            delay(1000)
            remaining--
            if (remaining == 60) haptics.perform(Haptic.Tick)
        }
        haptics.perform(Haptic.Confirm)
        say("Amen.")
        LivePrayer.markJoined(context, date)
        onFinished()
        phase = Phase.Done
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2A0A6E), LiveNight, Color(0xFF07000F))))) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 48.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, "Close", tint = LiveGold, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                LivePill()
                Spacer(Modifier.weight(1f))
            }

            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 26.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    phase,
                    transitionSpec = { (fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 14 }) togetherWith fadeOut(tween(200)) },
                    label = "live-phase",
                ) { p ->
                    when (p) {
                        Phase.Ask -> Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                            Eyebrow("Pray now")
                            Text(
                                "What do you want to pray about?", Modifier.padding(top = 14.dp),
                                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, textAlign = TextAlign.Center,
                            )
                            Row(
                                Modifier.padding(top = 26.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp))
                                    .background(Color.White.copy(alpha = .07f))
                                    .border(1.dp, Pk.Gold.copy(alpha = .35f), RoundedCornerShape(22.dp))
                                    .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BasicTextField(
                                    request, { request = it },
                                    Modifier.weight(1f).heightIn(min = 44.dp).padding(vertical = 10.dp),
                                    textStyle = TextStyle(color = Color.White, fontFamily = UtilitySans, fontSize = 18.sp),
                                    cursorBrush = SolidColor(Pk.GoldLight),
                                    decorationBox = { inner ->
                                        Box { if (request.isEmpty()) Text("Type it or say it", color = Color.White.copy(alpha = .4f), fontFamily = UtilitySans, fontSize = 18.sp); inner() }
                                    },
                                )
                                Box(Modifier.size(44.dp).clip(CircleShape).clickable { dictation.launch() }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.MicNone, "Say it", tint = Pk.GoldLight, modifier = Modifier.size(24.dp))
                                }
                            }
                            FlowRow(
                                Modifier.padding(top = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LivePrayer.SUGGESTIONS.forEach { s ->
                                    Text(
                                        s,
                                        Modifier.clip(RoundedCornerShape(99.dp)).background(Color.White.copy(alpha = .08f))
                                            .clickable { findVerses(s) }.padding(horizontal = 14.dp, vertical = 9.dp),
                                        color = LiveGold, fontFamily = UtilitySans, fontSize = 14.sp,
                                    )
                                }
                            }
                        }

                        Phase.Verses -> Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                            Eyebrow(topicTitle ?: "Pray with these")
                            Text(
                                "“${request.trim()}”", Modifier.padding(top = 10.dp),
                                color = Color.White.copy(alpha = .75f), fontFamily = UtilitySans, fontSize = 15.sp, textAlign = TextAlign.Center,
                            )
                            Text(
                                "Pray these verses out loud.", Modifier.padding(top = 18.dp),
                                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, textAlign = TextAlign.Center,
                            )
                            verses.forEach { (ref, text) -> VerseBlock(ref, text) }
                            Text("How long will you pray?", Modifier.padding(top = 26.dp), color = LiveGold.copy(alpha = .7f), fontFamily = UtilitySans, fontSize = 14.sp)
                            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LivePrayer.LENGTHS.forEach { m ->
                                    val on = m == minutes
                                    Text(
                                        "$m min",
                                        Modifier.clip(RoundedCornerShape(99.dp))
                                            .background(if (on) Pk.GoldLight else Color.White.copy(alpha = .08f))
                                            .clickable { minutes = m; haptics.perform(Haptic.Tick) }
                                            .padding(horizontal = 16.dp, vertical = 9.dp),
                                        color = if (on) LiveNight else LiveGold, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                    )
                                }
                            }
                            Spacer(Modifier.size(12.dp))
                        }

                        Phase.Praying -> Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                            Eyebrow(if (paused) "Paused" else "Keep praying")
                            Text(
                                request.trim().replaceFirstChar { it.uppercase() }, Modifier.padding(top = 10.dp),
                                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, textAlign = TextAlign.Center,
                            )
                            Ring(remaining, minutes * 60)
                            // the verses stay in view to pray back to God
                            verses.forEach { (ref, text) ->
                                Text(
                                    "$text  $ref", Modifier.padding(top = 14.dp),
                                    color = LiveGold.copy(alpha = .7f), fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Phase.Done -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Eyebrow("Amen")
                            Text(
                                "You prayed.", Modifier.padding(top = 14.dp),
                                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 36.sp, textAlign = TextAlign.Center,
                            )
                            Text(
                                "God heard you. Leave it with Him, and go into your day.", Modifier.padding(top = 14.dp),
                                color = LiveGold.copy(alpha = .75f), fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                when (phase) {
                    Phase.Ask -> LiveButton(Icons.Outlined.Check, "Find my verses", Modifier.weight(1f), filled = request.isNotBlank()) {
                        if (request.isNotBlank()) findVerses(request)
                    }
                    Phase.Verses -> LiveButton(Icons.Outlined.PlayArrow, "Start praying", Modifier.weight(1f), filled = true) {
                        remaining = minutes * 60
                        paused = false
                        haptics.perform(Haptic.Confirm)
                        say("Start praying.")
                        phase = Phase.Praying
                    }
                    Phase.Praying -> {
                        LiveButton(if (paused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, if (paused) "Go on" else "Pause", Modifier.weight(1f)) { paused = !paused }
                        LiveButton(Icons.Outlined.Check, "Amen", Modifier.weight(1f), filled = true) { remaining = 0; paused = false; if (phase == Phase.Praying) { haptics.perform(Haptic.Confirm); LivePrayer.markJoined(context, date); onFinished(); phase = Phase.Done } }
                    }
                    Phase.Done -> {
                        LiveButton(Icons.Outlined.PlayArrow, "Pray again", Modifier.weight(1f)) { request = ""; phase = Phase.Ask }
                        LiveButton(Icons.Outlined.Close, "Done", Modifier.weight(1f), filled = true) { onClose() }
                    }
                }
            }
        }
    }
}

@Composable
private fun LivePill() {
    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        .35f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "dot",
    )
    Row(
        Modifier.clip(RoundedCornerShape(99.dp)).background(Color.White.copy(alpha = .08f))
            .border(1.dp, Color.White.copy(alpha = .12f), RoundedCornerShape(99.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(LiveRed.copy(alpha = pulse)))
        Text("LIVE PRAYER", Modifier.padding(start = 8.dp), color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.6.sp)
    }
}

@Composable
private fun Eyebrow(text: String) = Text(
    text.uppercase(), color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold,
    fontSize = 11.sp, letterSpacing = 2.sp, textAlign = TextAlign.Center,
)

@Composable
private fun VerseBlock(ref: String, text: String) {
    Column(
        Modifier.padding(top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = .06f))
            .border(1.dp, Pk.Gold.copy(alpha = .25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, color = Color.White, fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 26.sp, textAlign = TextAlign.Center)
        Text("$ref · KJV", Modifier.padding(top = 10.dp), color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

/** The time left to pray, as a gold ring closing around the minutes. */
@Composable
private fun Ring(remaining: Int, total: Int) {
    val target = if (total > 0) remaining / total.toFloat() else 0f
    val sweep by animateFloatAsState(target, tween(950), label = "ring")
    Box(Modifier.padding(top = 26.dp).size(190.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 6.dp.toPx()
            val inset = stroke / 2
            val arc = Size(size.width - stroke, size.height - stroke)
            drawArc(Color.White.copy(alpha = .10f), 0f, 360f, false, Offset(inset, inset), arc, style = Stroke(stroke))
            drawArc(Pk.GoldLight, -90f, 360f * sweep, false, Offset(inset, inset), arc, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Text(
            "${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}",
            color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 40.sp,
        )
    }
}

@Composable
private fun LiveButton(icon: ImageVector, label: String, modifier: Modifier, filled: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(99.dp))
            .background(if (filled) Pk.Gold else Color.White.copy(alpha = .10f))
            .border(1.dp, if (filled) Pk.Gold else Pk.Gold.copy(alpha = .45f), RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (filled) LiveNight else LiveGold, modifier = Modifier.size(18.dp))
        Text(label, Modifier.padding(start = 8.dp), color = if (filled) LiveNight else LiveGold, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
    }
}
