package com.prayerkey.manna.ui.session

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.VoiceOverOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PointStep
import com.prayerkey.manna.data.PointsDay
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.ui.home.AmbientPrayerPlayer
import com.prayerkey.manna.ui.platform.KeepScreenOn
import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.ui.platform.UtteranceListener
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.platform.postToMain
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay

/**
 * Today's prayer points, prayed rather than read.
 *
 * Each point is read aloud in the phone's best voice over a quiet pad, and
 * then the screen goes still for long enough to pray that point in your own
 * words before the next one is read. The countdown is shown, so silence never
 * feels like the app has stopped. Nothing else is on the screen: no dock, no
 * library, nothing to browse.
 */
private enum class Stage { Intro, Running, Done }

private val Night = Brush.verticalGradient(listOf(Color(0xFF1E0D38), Color(0xFF0C0718)))
private val Gold = Color(0xFFE8C46A)
private val Soft = Color.White.copy(alpha = .62f)

@Composable
fun PrayerPointsSession(
    day: PointsDay,
    startAt: Int = 0,
    onProgress: (Int) -> Unit,
    onFinished: () -> Unit,
    onClose: () -> Unit,
) {
    KeepScreenOn()
    val context = platformContext()
    val prefs = remember { context.keyValues("points_session_prefs") }
    var voiceOn by remember { mutableStateOf(prefs.getBoolean("voice", true)) }
    var musicOn by remember { mutableStateOf(prefs.getBoolean("music", true)) }

    var stage by remember { mutableStateOf(Stage.Intro) }
    var index by remember { mutableIntStateOf(startAt.coerceIn(0, day.steps.lastIndex)) }
    var praying by remember { mutableStateOf(false) }
    var remaining by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    val step = day.steps[index]

    // the voice
    var voiceReady by remember { mutableStateOf(false) }
    val finishedReading = remember { mutableStateOf<CompletableDeferred<Unit>?>(null) }
    val speaker = remember {
        Speaker(context) { voiceReady = it == Speaker.SUCCESS }.also { s ->
            s.setOnUtteranceProgressListener(object : UtteranceListener() {
                override fun onDone(utteranceId: String?) { postToMain { finishedReading.value?.complete(Unit) } }
                override fun onError(utteranceId: String?) { postToMain { finishedReading.value?.complete(Unit) } }
            })
        }
    }
    LaunchedEffect(voiceReady) { if (voiceReady) { speaker.setSpeechRate(.88f); speaker.setPitch(.97f) } }

    // the music, only while the session is actually running
    val music = remember { AmbientPrayerPlayer() }
    LaunchedEffect(musicOn, stage, paused) {
        if (musicOn && stage == Stage.Running && !paused) music.start() else music.stop()
    }
    DisposableEffect(Unit) {
        onDispose {
            music.stop()
            runCatching { speaker.stop(); speaker.shutdown() }
        }
    }

    fun go(to: Int) {
        speaker.stop()
        finishedReading.value?.complete(Unit)
        if (to > day.steps.lastIndex) {
            onProgress(day.steps.size)
            stage = Stage.Done
            onFinished()
            return
        }
        index = to.coerceAtLeast(0)
        praying = false
        remaining = day.steps[index].praySeconds
        onProgress(index)
    }

    /* One step: read it (aloud, or for as long as reading takes), then the quiet
       to pray it, then on. Restarts on pause, resume and skip, and a resume
       carries on with the time that was left rather than starting the quiet again. */
    LaunchedEffect(stage, index, paused, voiceOn) {
        if (stage != Stage.Running || paused) return@LaunchedEffect
        if (!praying) {
            if (voiceOn && voiceReady) {
                val done = CompletableDeferred<Unit>()
                finishedReading.value = done
                val words = if (step.reference != null) "${step.reference}. ${step.text}" else step.text
                speaker.speak(words, Speaker.QUEUE_FLUSH, null, "point-$index")
                try { done.await() } finally { if (finishedReading.value === done) finishedReading.value = null }
            } else {
                delay((step.text.length * 60L).coerceIn(3_000L, 14_000L))
            }
            praying = true
            if (remaining <= 0) remaining = step.praySeconds
        }
        while (remaining > 0) {
            delay(1_000)
            remaining -= 1
        }
        go(index + 1)
    }

    Box(Modifier.fillMaxSize().background(Night)) {
        AnimatedContent(
            targetState = stage,
            transitionSpec = { fadeIn(tween(420)) togetherWith fadeOut(tween(220)) },
            label = "points-stage",
        ) { now ->
            when (now) {
                Stage.Intro -> Intro(
                    day = day,
                    resumeAt = startAt,
                    onBegin = { from ->
                        index = from
                        praying = false
                        remaining = day.steps[from].praySeconds
                        paused = false
                        stage = Stage.Running
                    },
                    onClose = onClose,
                )
                Stage.Running -> Running(
                    day = day,
                    index = index,
                    step = step,
                    praying = praying,
                    remaining = remaining,
                    paused = paused,
                    voiceOn = voiceOn,
                    musicOn = musicOn,
                    onVoice = {
                        voiceOn = !voiceOn
                        prefs.edit().putBoolean("voice", voiceOn).apply()
                        if (!voiceOn) speaker.stop()
                    },
                    onMusic = {
                        musicOn = !musicOn
                        prefs.edit().putBoolean("music", musicOn).apply()
                    },
                    // pausing cancels the step in progress; carrying on reads the point again
                    onPause = {
                        paused = !paused
                        if (paused) speaker.stop()
                    },
                    onBack = { go(index - 1) },
                    onNext = { go(index + 1) },
                    onClose = onClose,
                )
                Stage.Done -> Done(day = day, onClose = onClose)
            }
        }
    }
}

@Composable
private fun Intro(day: PointsDay, resumeAt: Int, onBegin: (Int) -> Unit, onClose: () -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 28.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Close", tint = Soft) }
        }
        Spacer(Modifier.weight(1f))
        Text("TODAY'S PRAYER POINTS", color = Gold, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
        Text(day.title, Modifier.padding(top = 14.dp), color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp)
        Text(
            "About ${day.minutes} minutes · ${day.steps.size} points",
            Modifier.padding(top = 14.dp), color = Soft, fontFamily = UtilitySans, fontSize = 15.sp,
        )
        Text(
            "Each point is read to you. Then pray it in your own words before the next one.",
            Modifier.padding(top = 22.dp), color = Color.White.copy(alpha = .82f), fontFamily = BookSerif, fontSize = 19.sp, lineHeight = 28.sp,
        )
        Spacer(Modifier.weight(1.2f))
        val resuming = resumeAt in 1..day.steps.lastIndex
        Surface(
            onClick = { onBegin(if (resuming) resumeAt else 0) },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(29.dp),
            color = Gold,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (resuming) "Continue from point ${resumeAt + 1}" else "Begin",
                    color = Color(0xFF1E0D38), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                )
            }
        }
        if (resuming) {
            androidx.compose.material3.TextButton(onClick = { onBegin(0) }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text("Start again", color = Soft, fontFamily = UtilitySans, fontSize = 15.sp)
            }
        }
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun Running(
    day: PointsDay,
    index: Int,
    step: PointStep,
    praying: Boolean,
    remaining: Int,
    paused: Boolean,
    voiceOn: Boolean,
    musicOn: Boolean,
    onVoice: () -> Unit,
    onMusic: () -> Unit,
    onPause: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Close", tint = Soft) }
            Text(
                "${index + 1} of ${day.steps.size}",
                Modifier.weight(1f), color = Soft, textAlign = TextAlign.Center, fontFamily = UtilitySans, fontSize = 14.sp,
            )
            IconButton(onClick = onVoice) {
                Icon(if (voiceOn) Icons.Outlined.RecordVoiceOver else Icons.Outlined.VoiceOverOff, if (voiceOn) "Turn the voice off" else "Turn the voice on", tint = if (voiceOn) Color.White else Soft)
            }
            IconButton(onClick = onMusic) {
                Icon(if (musicOn) Icons.Outlined.MusicNote else Icons.Outlined.MusicOff, if (musicOn) "Turn the music off" else "Turn the music on", tint = if (musicOn) Color.White else Soft)
            }
        }
        // where you are in the session
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            day.steps.indices.forEach { i ->
                Box(
                    Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (i <= index) Gold else Color.White.copy(alpha = .16f)),
                )
            }
        }

        AnimatedContent(
            targetState = index,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = { fadeIn(tween(500, delayMillis = 120)) togetherWith fadeOut(tween(260)) },
            label = "point",
        ) { shown ->
            val s = day.steps[shown]
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 30.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))
                Text(s.label, color = Gold, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp, textAlign = TextAlign.Center)
                Text(
                    s.text,
                    Modifier.padding(top = 20.dp),
                    color = Color.White,
                    fontFamily = BookSerif,
                    fontSize = if (s.text.length > 180) 23.sp else 27.sp,
                    lineHeight = if (s.text.length > 180) 34.sp else 39.sp,
                    textAlign = TextAlign.Center,
                )
                s.reference?.let {
                    Text(it, Modifier.padding(top = 16.dp), color = Gold, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // the quiet: how much of it is left, so silence never looks like a stop
        val left by animateFloatAsState(
            if (praying && step.praySeconds > 0) remaining / step.praySeconds.toFloat() else 1f,
            tween(950), label = "quiet",
        )
        Column(Modifier.fillMaxWidth().height(54.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (praying) {
                Text(
                    if (paused) "Paused" else "Pray · ${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}",
                    color = Color.White.copy(alpha = .78f), fontFamily = UtilitySans, fontSize = 15.sp,
                )
                Box(Modifier.padding(top = 10.dp).size(width = 160.dp, height = 3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = .14f))) {
                    Box(Modifier.fillMaxWidth(left).height(3.dp).background(Gold))
                }
            } else if (paused) {
                Text("Paused", color = Color.White.copy(alpha = .78f), fontFamily = UtilitySans, fontSize = 15.sp)
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, enabled = index > 0, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Outlined.SkipPrevious, "Previous point", tint = if (index > 0) Color.White else Soft.copy(alpha = .3f), modifier = Modifier.size(30.dp))
            }
            Surface(onClick = onPause, shape = CircleShape, color = Gold, modifier = Modifier.size(76.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(if (paused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, if (paused) "Carry on" else "Pause", tint = Color(0xFF1E0D38), modifier = Modifier.size(36.dp))
                }
            }
            IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Outlined.SkipNext, if (index == day.steps.lastIndex) "Finish" else "Next point", tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun Done(day: PointsDay, onClose: () -> Unit) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(72.dp).clip(CircleShape).border(1.5.dp, Gold, CircleShape), contentAlignment = Alignment.Center) {
            Text("✦", color = Gold, fontSize = 26.sp)
        }
        Text("Amen.", Modifier.padding(top = 26.dp), color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 44.sp)
        Text(
            "You prayed today's points.",
            Modifier.padding(top = 12.dp), color = Soft, fontFamily = UtilitySans, fontSize = 16.sp, textAlign = TextAlign.Center,
        )
        val named = day.peopleNames
        if (named.isNotEmpty()) {
            Text(
                "You prayed for " + when (named.size) {
                    1 -> named[0]
                    2 -> "${named[0]} and ${named[1]}"
                    else -> named.dropLast(1).joinToString(", ") + " and " + named.last()
                } + ".",
                Modifier.padding(top = 6.dp), color = Soft, fontFamily = UtilitySans, fontSize = 16.sp, textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.weight(1.3f))
        Surface(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(58.dp).padding(bottom = 0.dp),
            shape = RoundedCornerShape(29.dp),
            color = Gold,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Done", color = Color(0xFF1E0D38), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}
