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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.LivePrayer
import com.prayerkey.manna.data.LiveStep
import com.prayerkey.manna.data.LiveVerse
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.data.PrayerOfTheDay
import com.prayerkey.manna.data.PrayersOfTheDay
import com.prayerkey.manna.ui.platform.Haptic
import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.ui.platform.UtteranceListener
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.platform.rememberHaptics
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.LocalDate

private val LiveNight = Color(0xFF12002A)
private val LiveGold = Color(0xFFF1DDAE)
private val LiveRed = Color(0xFFFF4D5E)

/**
 * The morning live prayer, led step by step.
 *
 * The phone's voice leads each step the way a prayer leader would, then the
 * screen waits while you pray: a gold ring counts the time down and the phone
 * taps once when it is time to move on. Pause and skip are always there.
 */
@Composable
fun LivePrayerScreen(
    date: LocalDate,
    todaysPrayer: PrayerOfTheDay?,
    onClose: () -> Unit,
    onFinished: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val haptics = rememberHaptics()
    val day = remember(date) { LivePrayer.dayFor(date) }
    val steps = remember(date) { LivePrayer.steps(date) }

    // the KJV text of every verse the session uses
    val verses = remember { mutableStateMapOf<LiveVerse, String>() }
    LaunchedEffect(date) {
        val bible = OfflineBible(context)
        steps.forEach { step ->
            val v = when (step) {
                is LiveStep.Read -> step.verse
                is LiveStep.Pray -> step.verse
                else -> null
            } ?: return@forEach
            // a verse that stops on a comma is read as the end of a sentence
            bible.chapter(v.book, v.chapter).firstOrNull { it.verse == v.verse }
                ?.let { verses[v] = it.text.replace(Regex("""^\[[^\]]*\]\s*"""), "").trim().replace(Regex("[,;:]$"), ".") }
        }
    }

    var index by remember { mutableIntStateOf(-1) }       // -1 is the welcome card
    var line by remember { mutableIntStateOf(0) }         // which say-after-me line
    var paused by remember { mutableStateOf(false) }
    var voiceOn by remember { mutableStateOf(true) }
    var remaining by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    var speechReady by remember { mutableStateOf(false) }
    val speaker = remember { Speaker(context) { speechReady = it == Speaker.SUCCESS } }
    val spoken = remember { mutableStateOf<CompletableDeferred<Unit>?>(null) }
    DisposableEffect(speaker) {
        speaker.setOnUtteranceProgressListener(object : UtteranceListener() {
            override fun onDone(utteranceId: String?) { spoken.value?.complete(Unit) }
            override fun onError(utteranceId: String?) { spoken.value?.complete(Unit) }
        })
        onDispose { speaker.stop(); speaker.shutdown() }
    }

    /* Says the words and returns when they have been said. With the voice off
       it waits about as long as reading them would take. */
    suspend fun say(words: String) {
        if (voiceOn && speechReady) {
            val done = CompletableDeferred<Unit>()
            spoken.value = done
            speaker.setSpeechRate(.88f)
            speaker.speak(words, Speaker.QUEUE_FLUSH, null, "live")
            withTimeoutOrNull(60_000) { done.await() }
        } else {
            delay((words.split(' ').size * 330L).coerceAtLeast(1500))
        }
    }

    suspend fun countdown(seconds: Int) {
        total = seconds
        remaining = seconds
        while (remaining > 0) {
            delay(1000)
            if (!paused) remaining--
        }
    }

    // one pass through a step; pausing stops the voice, and resuming starts the step's words again
    LaunchedEffect(index, line, paused) {
        if (paused || index < 0 || finished) return@LaunchedEffect
        val step = steps.getOrNull(index) ?: return@LaunchedEffect
        when (step) {
            is LiveStep.Lead -> { say(step.words); if (step.seconds > 0) countdown(step.seconds) }
            is LiveStep.Read -> {
                say("Read it out loud with me. ${verses[step.verse].orEmpty()} ${step.verse.label}.")
                countdown(6)
            }
            is LiveStep.SayAfterMe -> {
                if (line == 0) say("Say this after me.")
                say(step.lines[line])
                countdown(5)
                if (line < step.lines.lastIndex) { line++; haptics.perform(Haptic.Tick); return@LaunchedEffect }
            }
            is LiveStep.Pray -> { say(step.words); haptics.perform(Haptic.Tap); countdown(step.seconds) }
            LiveStep.TodaysPrayer -> {
                say("Let us close with today's prayer. Pray it with me.")
                todaysPrayer?.let { say(it.text) }
                countdown(4)
            }
            is LiveStep.Blessing -> { say(step.words); return@LaunchedEffect }
        }
        haptics.perform(Haptic.Confirm)
        line = 0
        total = 0
        if (index < steps.lastIndex) index++
    }

    fun skip() {
        speaker.stop()
        spoken.value?.complete(Unit)
        line = 0
        total = 0
        if (index < steps.lastIndex) index++ else finished = true
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF2A0A6E), LiveNight, Color(0xFF07000F))),
        ),
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 18.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, "Close", tint = LiveGold, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                LivePill()
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(40.dp).clip(CircleShape).clickable { voiceOn = !voiceOn; if (!voiceOn) speaker.stop() }, contentAlignment = Alignment.Center) {
                    Icon(if (voiceOn) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeOff, if (voiceOn) "Voice on" else "Voice off", tint = LiveGold, modifier = Modifier.size(20.dp))
                }
            }
            // one segment per step, filling as the morning goes on
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                steps.indices.forEach { i ->
                    Box(
                        Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(99.dp))
                            .background(if (i <= index) Pk.GoldLight else Color.White.copy(alpha = .14f)),
                    )
                }
            }

            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp), contentAlignment = Alignment.Center) {
                val key = if (finished) "done" else "$index-$line"
                AnimatedContent(
                    key,
                    transitionSpec = { (fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 12 }) togetherWith fadeOut(tween(250)) },
                    label = "live-step",
                ) { _ ->
                    when {
                        finished -> Finished(day.theme)
                        index < 0 -> Welcome(day.theme, LivePrayer.minutes(date))
                        else -> StepView(steps[index], line, verses, todaysPrayer, remaining, total)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when {
                    finished -> LiveButton(Icons.Outlined.Close, "Done", Modifier.weight(1f), filled = true) { onClose() }
                    index < 0 -> LiveButton(Icons.Outlined.PlayArrow, "Start praying", Modifier.weight(1f), filled = true) {
                        haptics.perform(Haptic.Confirm); index = 0
                    }
                    index == steps.lastIndex -> LiveButton(Icons.Outlined.Check, "Amen", Modifier.weight(1f), filled = true) {
                        speaker.stop()
                        haptics.perform(Haptic.Confirm)
                        LivePrayer.markJoined(context, date)
                        PrayersOfTheDay.markPrayed(context, date)
                        finished = true
                        onFinished()
                    }
                    else -> {
                        LiveButton(if (paused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, if (paused) "Go on" else "Pause", Modifier.weight(1f)) {
                            paused = !paused
                            if (paused) { speaker.stop(); spoken.value?.complete(Unit) }
                        }
                        LiveButton(Icons.Outlined.SkipNext, "Next", Modifier.weight(1f)) { skip() }
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
private fun Welcome(theme: String, minutes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Eyebrow("Live prayer")
        val hour = com.prayerkey.manna.platform.now().hour
        Text(
            when { hour < 12 -> "Good morning."; hour < 17 -> "Good afternoon."; else -> "Good evening." },
            Modifier.padding(top = 16.dp), color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 38.sp, textAlign = TextAlign.Center)
        Text("Today we pray about", Modifier.padding(top = 22.dp), color = LiveGold.copy(alpha = .7f), fontFamily = UtilitySans, fontSize = 15.sp)
        Text(theme, Modifier.padding(top = 6.dp), color = LiveGold, fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 26.sp, textAlign = TextAlign.Center)
        Text("About $minutes minutes. I will lead you. Pray out loud if you can.", Modifier.padding(top = 26.dp), color = Color.White.copy(alpha = .6f), fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun StepView(step: LiveStep, line: Int, verses: Map<LiveVerse, String>, todaysPrayer: PrayerOfTheDay?, remaining: Int, total: Int) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        when (step) {
            is LiveStep.Lead -> {
                Eyebrow(step.title)
                BigWords(step.words)
                if (total > 0) Ring(remaining, total)
            }
            is LiveStep.Read -> {
                Eyebrow(step.title)
                Text(
                    verses[step.verse].orEmpty(), Modifier.padding(top = 18.dp),
                    color = Color.White, fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 23.sp, lineHeight = 33.sp, textAlign = TextAlign.Center,
                )
                Text(step.verse.label + " · KJV", Modifier.padding(top = 14.dp), color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            is LiveStep.SayAfterMe -> {
                Eyebrow("Say after me")
                Text(
                    step.lines[line], Modifier.padding(top = 22.dp),
                    color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 31.sp, lineHeight = 39.sp, textAlign = TextAlign.Center,
                )
                Row(Modifier.padding(top = 26.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    step.lines.indices.forEach { i ->
                        Box(Modifier.size(7.dp).clip(CircleShape).background(if (i <= line) Pk.GoldLight else Color.White.copy(alpha = .18f)))
                    }
                }
            }
            is LiveStep.Pray -> {
                Eyebrow(step.title)
                BigWords(step.words)
                step.verse?.let { v ->
                    verses[v]?.let { text ->
                        Text("$text  ${v.label}", Modifier.padding(top = 16.dp), color = LiveGold.copy(alpha = .65f), fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
                    }
                }
                if (total > 0) Ring(remaining, total)
            }
            LiveStep.TodaysPrayer -> {
                Eyebrow("Today's prayer")
                todaysPrayer?.let { p ->
                    Text(p.title, Modifier.padding(top = 12.dp), color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center)
                    p.lines.forEach { l ->
                        Text(l, Modifier.padding(top = 6.dp), color = if (l.startsWith("Amen")) Pk.GoldLight else Color.White.copy(alpha = .9f), fontFamily = DisplaySerif, fontSize = 18.sp, lineHeight = 26.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            is LiveStep.Blessing -> {
                Eyebrow("The blessing")
                Text(step.words, Modifier.padding(top = 18.dp), color = Color.White, fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 22.sp, lineHeight = 32.sp, textAlign = TextAlign.Center)
                Text("Numbers 6:24-26", Modifier.padding(top = 12.dp), color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun BigWords(words: String) = Text(
    words, Modifier.padding(top = 18.dp),
    color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 25.sp, lineHeight = 34.sp, textAlign = TextAlign.Center,
)

/** The time left to pray, as a gold ring closing around the seconds. */
@Composable
private fun Ring(remaining: Int, total: Int) {
    val target = if (total > 0) remaining / total.toFloat() else 0f
    val sweep by animateFloatAsState(target, tween(950), label = "ring")
    Box(Modifier.padding(top = 30.dp).size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 5.dp.toPx()
            val inset = stroke / 2
            val arc = Size(size.width - stroke, size.height - stroke)
            drawArc(Color.White.copy(alpha = .10f), 0f, 360f, false, Offset(inset, inset), arc, style = Stroke(stroke))
            drawArc(Pk.GoldLight, -90f, 360f * sweep, false, Offset(inset, inset), arc, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}", color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Text("keep praying", color = LiveGold.copy(alpha = .6f), fontFamily = UtilitySans, fontSize = 11.sp)
        }
    }
}

@Composable
private fun Finished(theme: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Eyebrow("Amen")
        Text("You prayed today.", Modifier.padding(top = 16.dp), color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, textAlign = TextAlign.Center)
        Text("$theme. Go into your day. He goes with you.", Modifier.padding(top = 16.dp), color = LiveGold.copy(alpha = .75f), fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
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
