package com.prayerkey.manna.ui.meditation

import com.prayerkey.manna.platform.pad2
import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.ui.platform.platformContext
import org.jetbrains.compose.resources.DrawableResource
import com.prayerkey.manna.resources.*
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.WbSunny
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.Ambience
import com.prayerkey.manna.data.MEDITATION_MINUTES
import com.prayerkey.manna.data.MeditationFocus
import com.prayerkey.manna.data.MeditationSetup
import com.prayerkey.manna.data.MeditationStore
import com.prayerkey.manna.data.MeditationVoice
import com.prayerkey.manna.data.meditationScript
import com.prayerkey.manna.ui.challenge.Ink
import com.prayerkey.manna.ui.challenge.Line
import com.prayerkey.manna.ui.challenge.PrimaryButton
import com.prayerkey.manna.ui.challenge.Stat
import com.prayerkey.manna.ui.challenge.StepFrame
import com.prayerkey.manna.ui.challenge.SummaryLine
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay

/**
 * Christian meditation.
 *
 * Choose what to sit with, for how long, in whose voice and over what sound,
 * and then a dark screen with a verse on it and a voice that speaks rarely. It
 * ends in a question - how was it - and what you write can go into the journal
 * beside everything else.
 */
private enum class MedStage { Welcome, Focus, Length, Voice, Sound, Summary, Playing, Reflect, Done }

@Composable
fun MeditationRoute(
    onClose: () -> Unit,
    /** Keeps a reflection in the journal: a heading, then what was written. */
    onKeep: (String, String) -> Unit,
) {
    val context = platformContext()
    var setup by remember { mutableStateOf(MeditationStore.setup(context)) }
    var stage by remember { mutableStateOf(MedStage.Welcome) }
    var sat by remember { mutableIntStateOf(0) }

    // one speech engine for the whole flow: previews and the session share it
    var speechReady by remember { mutableStateOf(false) }
    val speaker = remember {
        Speaker(context) { speechReady = it == Speaker.SUCCESS }
    }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }
    LaunchedEffect(speechReady, setup.voice) {
        if (speechReady) {
            speaker.language = null
            speaker.setPitch(setup.voice.pitch)
            speaker.setSpeechRate(setup.voice.rate)
        }
    }

    fun keep(next: MeditationSetup) {
        setup = next
        MeditationStore.saveSetup(context, next)
    }

    BackHandler {
        speaker.stop()
        stage = when (stage) {
            MedStage.Welcome -> return@BackHandler onClose()
            MedStage.Focus -> MedStage.Welcome
            MedStage.Length -> MedStage.Focus
            MedStage.Voice -> MedStage.Length
            MedStage.Sound -> MedStage.Voice
            MedStage.Summary -> MedStage.Sound
            MedStage.Playing -> MedStage.Summary
            MedStage.Reflect -> MedStage.Done
            MedStage.Done -> return@BackHandler onClose()
        }
    }

    Box(Modifier.fillMaxSize().background(if (stage == MedStage.Playing) Pk.DeepDark else Pk.Cream)) {
        when (stage) {
            MedStage.Welcome -> Welcome(onClose) { stage = MedStage.Focus }
            MedStage.Focus -> FocusStep(setup, ::keep, { stage = MedStage.Welcome }) { stage = MedStage.Length }
            MedStage.Length -> LengthStep(setup, ::keep, { stage = MedStage.Focus }) { stage = MedStage.Voice }
            MedStage.Voice -> VoiceStep(
                setup, ::keep,
                preview = { voice ->
                    if (speechReady) {
                        speaker.setPitch(voice.pitch)
                        speaker.setSpeechRate(voice.rate)
                        speaker.speak("Be still, and know that I am God.", Speaker.QUEUE_FLUSH, null, "preview")
                    }
                },
                onBack = { speaker.stop(); stage = MedStage.Length },
            ) { speaker.stop(); stage = MedStage.Sound }
            MedStage.Sound -> SoundStep(setup, ::keep, { stage = MedStage.Voice }) { stage = MedStage.Summary }
            MedStage.Summary -> Summary(setup, { stage = MedStage.Sound }) { stage = MedStage.Playing }
            MedStage.Playing -> Session(
                setup = setup,
                speaker = speaker,
                speechReady = speechReady,
                onEnd = { seconds ->
                    sat = seconds
                    if (seconds >= 30) {
                        MeditationStore.finish(context, setup, seconds, meditationScript(setup).ref)
                    }
                    stage = MedStage.Reflect
                },
            )
            MedStage.Reflect -> Reflect(
                setup = setup,
                onSave = { mood, text, toJournal ->
                    if (toJournal && (text.isNotBlank() || mood.isNotBlank())) {
                        val heading = "Meditation · ${setup.focus.label}" + if (mood.isNotBlank()) " · $mood" else ""
                        onKeep(heading, text.ifBlank { mood })
                    }
                    com.prayerkey.manna.data.Walk.mark(context)
                    stage = MedStage.Done
                },
            )
            MedStage.Done -> Done(
                setup = setup,
                seconds = sat,
                onAgain = { stage = MedStage.Focus },
                onClose = onClose,
            )
        }
    }
}

// ── 1. welcome ──────────────────────────────────────────────────────────

@Composable
private fun Welcome(onClose: () -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().aspectRatio(.9f)) {
            Image(
                painterResource(Res.drawable.rhythm_reflection), null,
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Transparent, .7f to Color.Transparent, 1f to Pk.Cream),
                ),
            )
            Box(
                Modifier.padding(8.dp).size(40.dp).clip(CircleShape)
                    .background(Color.Black.copy(alpha = .22f)).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
        Column(
            Modifier.weight(1f).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(58.dp).clip(CircleShape).background(Pk.Oxblood),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Spa, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
            Text(
                "Christian meditation",
                Modifier.padding(top = 16.dp),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 29.sp,
            )
            Text(
                "Be still. Reflect. Listen.",
                Modifier.padding(top = 7.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 15.sp,
            )
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Get started", onClick = onStart)
        }
    }
}

// ── the rows every setup step uses ─────────────────────────────────────

@Composable
private fun Choice(icon: ImageVector, title: String, blurb: String, on: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(if (on) Pk.Oxblood.copy(alpha = .07f) else Color.White)
            .border(if (on) 1.6.dp else 1.dp, if (on) Pk.Oxblood else Line, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Pk.Oxblood.copy(alpha = .08f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp)) }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                title,
                color = Ink, fontFamily = UtilitySans, fontSize = 15.5.sp,
                fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
            )
            Text(blurb, Modifier.padding(top = 2.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp)
        }
        if (on) Icon(Icons.Outlined.CheckCircle, null, tint = Pk.Oxblood, modifier = Modifier.size(20.dp))
    }
}

// ── 2. what to meditate on ──────────────────────────────────────────────

@Composable
private fun FocusStep(setup: MeditationSetup, keep: (MeditationSetup) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    val icons = mapOf(
        MeditationFocus.PRESENCE to Icons.Outlined.SelfImprovement,
        MeditationFocus.PEACE to Icons.Outlined.Spa,
        MeditationFocus.GRATITUDE to Icons.Outlined.FavoriteBorder,
        MeditationFocus.SLEEP to Icons.Outlined.Bedtime,
        MeditationFocus.HEALING to Icons.Outlined.HealthAndSafety,
        MeditationFocus.DIRECTION to Icons.Outlined.Explore,
        MeditationFocus.PSALM to Icons.Outlined.MenuBook,
        MeditationFocus.CUSTOM to Icons.Outlined.EditNote,
    )
    StepFrame(
        step = 1, of = 5,
        title = "What would you like to meditate on?",
        subtitle = "Choose a focus",
        onBack = onBack,
        footer = {
            PrimaryButton(
                "Next",
                enabled = setup.focus != MeditationFocus.CUSTOM || setup.custom.isNotBlank(),
                onClick = onNext,
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MeditationFocus.entries.forEach { focus ->
                Choice(icons.getValue(focus), focus.label, focus.blurb, setup.focus == focus) {
                    keep(setup.copy(focus = focus))
                }
            }
            if (setup.focus == MeditationFocus.CUSTOM) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                        .border(1.dp, Line, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 15.dp),
                ) {
                    if (setup.custom.isBlank()) {
                        Text("What is on your heart?", color = Pk.Muted, fontFamily = BookSerif, fontSize = 16.sp)
                    }
                    BasicTextField(
                        setup.custom, { keep(setup.copy(custom = it)) },
                        textStyle = TextStyle(color = Ink, fontFamily = BookSerif, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ── 3. how long ─────────────────────────────────────────────────────────

@Composable
private fun LengthStep(setup: MeditationSetup, keep: (MeditationSetup) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    val blurbs = mapOf(3 to "A quick reset", 5 to "Find your calm", 10 to "Go deeper", 15 to "More time with God", 20 to "A fuller stillness", 30 to "Extended stillness")
    StepFrame(
        step = 2, of = 5,
        title = "How long would you like to meditate?",
        subtitle = "You can change this any time",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MEDITATION_MINUTES.forEach { m ->
                Choice(Icons.Outlined.Schedule, "$m minutes", blurbs[m].orEmpty(), setup.minutes == m) {
                    keep(setup.copy(minutes = m))
                }
            }
        }
    }
}

// ── 4. whose voice ──────────────────────────────────────────────────────

@Composable
private fun VoiceStep(
    setup: MeditationSetup,
    keep: (MeditationSetup) -> Unit,
    preview: (MeditationVoice) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepFrame(
        step = 3, of = 5,
        title = "Choose a voice",
        subtitle = "Tap play to hear it",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            MeditationVoice.entries.forEach { voice ->
                val on = setup.voice == voice
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(if (on) Pk.Oxblood.copy(alpha = .07f) else Color.White)
                        .border(if (on) 1.6.dp else 1.dp, if (on) Pk.Oxblood else Line, RoundedCornerShape(18.dp))
                        .clickable { keep(setup.copy(voice = voice)) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(if (on) Pk.Oxblood else Pk.Oxblood.copy(alpha = .10f))
                            .clickable {
                                keep(setup.copy(voice = voice))
                                preview(voice)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.PlayArrow, "Play", tint = if (on) Color.White else Pk.Oxblood, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(
                            voice.label,
                            color = Ink, fontFamily = UtilitySans, fontSize = 15.5.sp,
                            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                        )
                        Text(voice.blurb, Modifier.padding(top = 2.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp)
                    }
                    if (on) Icon(Icons.Outlined.CheckCircle, null, tint = Pk.Oxblood, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ── 5. what you hear under it ───────────────────────────────────────────

private fun artOf(a: Ambience): DrawableResource? = when (a) {
    Ambience.NONE -> null
    Ambience.NATURE -> Res.drawable.discovery_cover_5
    Ambience.RAIN -> Res.drawable.onboarding_prayer
    Ambience.OCEAN -> Res.drawable.discovery_cover_2
    Ambience.PIANO -> Res.drawable.rhythm_evening
    Ambience.HARP -> Res.drawable.discovery_cover_4
}

@Composable
private fun SoundStep(setup: MeditationSetup, keep: (MeditationSetup) -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    // what is chosen plays, so the choice is heard rather than imagined
    val preview = remember(setup.ambience) { Soundscape(setup.ambience) }
    DisposableEffect(preview) {
        preview.start()
        onDispose { preview.stop() }
    }
    StepFrame(
        step = 4, of = 5,
        title = "Background sound",
        subtitle = "Set the atmosphere",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Ambience.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { a ->
                        val on = setup.ambience == a
                        Box(
                            Modifier.weight(1f).aspectRatio(1.08f).clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(if (on) 2.dp else 1.dp, if (on) Pk.Oxblood else Line, RoundedCornerShape(20.dp))
                                .clickable { keep(setup.copy(ambience = a)) },
                        ) {
                            val art = artOf(a)
                            if (art != null) {
                                Image(painterResource(art), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                Box(
                                    Modifier.fillMaxSize().background(
                                        Brush.verticalGradient(0f to Color.Transparent, .5f to Color.Transparent, 1f to Pk.DeepDark.copy(alpha = .86f)),
                                    ),
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.VolumeOff, null,
                                    Modifier.align(Alignment.Center).size(30.dp), tint = Pk.Muted,
                                )
                            }
                            Column(Modifier.align(Alignment.BottomStart).padding(13.dp)) {
                                Text(
                                    a.label,
                                    color = if (art != null) Color.White else Ink,
                                    fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                )
                                Text(
                                    a.blurb,
                                    color = if (art != null) Color.White.copy(alpha = .8f) else Pk.Muted,
                                    fontFamily = UtilitySans, fontSize = 11.5.sp,
                                )
                            }
                            if (on) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).padding(10.dp).size(22.dp).clip(CircleShape).background(Pk.Oxblood),
                                    contentAlignment = Alignment.Center,
                                ) { Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── 6. the summary ──────────────────────────────────────────────────────

@Composable
private fun Summary(setup: MeditationSetup, onBack: () -> Unit, onStart: () -> Unit) {
    val script = remember(setup) { meditationScript(setup) }
    StepFrame(
        step = 5, of = 5,
        title = "Your meditation",
        subtitle = "Review and begin",
        onBack = onBack,
        footer = { PrimaryButton("Start meditation", onClick = onStart) },
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SummaryLine(
                Icons.Outlined.Spa,
                if (setup.focus == MeditationFocus.CUSTOM) setup.custom.ifBlank { setup.focus.label } else setup.focus.label,
                "Focus",
            )
            SummaryLine(Icons.Outlined.Schedule, "${setup.minutes} minutes", "Duration")
            SummaryLine(Icons.Outlined.RecordVoiceOver, setup.voice.label, "Voice")
            SummaryLine(Icons.Outlined.GraphicEq, setup.ambience.label, "Background")
        }
        Column(
            Modifier.fillMaxWidth().padding(top = 18.dp).clip(RoundedCornerShape(20.dp))
                .background(Pk.Oxblood.copy(alpha = .05f)).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                script.verse,
                color = Ink, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center,
            )
            Text(script.ref, Modifier.padding(top = 8.dp), color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 12.5.sp)
        }
    }
}

// ── 7. the session ──────────────────────────────────────────────────────

@Composable
private fun Session(
    setup: MeditationSetup,
    speaker: Speaker,
    speechReady: Boolean,
    onEnd: (Int) -> Unit,
) {
    val script = remember(setup) { meditationScript(setup) }
    val total = setup.minutes * 60
    val starts = remember(script, total) { script.starts(total) }
    /* Each line is given its moment: the lines of a movement spread evenly
       across it, so the voice arrives, says one thing, and leaves the rest to
       silence. Scheduling by the clock rather than by a queue means skipping
       ahead or back simply lands on whatever belongs to that second. */
    val schedule = remember(script, total) {
        script.chapters.flatMapIndexed { ci, chapter ->
            val length = (chapter.share * total).toInt()
            val gap = (length / chapter.lines.size.coerceAtLeast(1)).coerceAtLeast(6)
            chapter.lines.mapIndexed { li, line -> (starts[ci] + 2 + li * gap) to line }
        }
    }
    var elapsed by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(true) }
    var spoken by remember { mutableStateOf(setOf<Int>()) }
    var showChapters by remember { mutableStateOf(false) }
    var showTranscript by remember { mutableStateOf(false) }

    val sound = remember(setup.ambience) { Soundscape(setup.ambience) }
    DisposableEffect(sound) {
        sound.start()
        onDispose { sound.stop(); speaker.stop() }
    }
    LaunchedEffect(running) {
        sound.pause(!running)
        if (!running) speaker.stop()
    }
    LaunchedEffect(running, speechReady) {
        while (running) {
            if (speechReady) {
                schedule.forEachIndexed { i, (at, line) ->
                    if (i !in spoken && elapsed >= at && elapsed < at + 4) {
                        speaker.speak(line, Speaker.QUEUE_ADD, null, "m$i")
                        spoken = spoken + i
                    }
                }
            }
            if (elapsed >= total) {
                running = false
                onEnd(elapsed)
                break
            }
            delay(1000)
            elapsed++
        }
    }

    fun seek(by: Int) {
        speaker.stop()
        elapsed = (elapsed + by).coerceIn(0, total)
        // anything already behind the new position counts as heard
        spoken = spoken + schedule.indices.filter { schedule[it].first < elapsed }
    }

    val chapterIndex = starts.indexOfLast { it <= elapsed }.coerceAtLeast(0)

    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(artOf(setup.ambience) ?: Res.drawable.rhythm_reflection), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Pk.DeepDark.copy(alpha = .62f), Pk.DeepDark.copy(alpha = .92f))),
            ),
        )
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable { running = false; onEnd(elapsed) },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Close, "End", tint = Color.White, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable { showChapters = true },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.FormatListBulleted, "Chapters", tint = Color.White, modifier = Modifier.size(20.dp)) }
            }

            Text(
                script.chapters[chapterIndex].title,
                Modifier.padding(top = 10.dp),
                color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            )
            Text(
                setup.focus.label,
                Modifier.padding(top = 4.dp),
                color = Color.White.copy(alpha = .72f), fontFamily = UtilitySans, fontSize = 13.sp,
            )

            Spacer(Modifier.weight(1f))
            Text(
                script.verse,
                color = Color.White, fontFamily = DisplaySerif,
                fontSize = 26.sp, lineHeight = 36.sp, textAlign = TextAlign.Center,
            )
            Text(
                script.ref,
                Modifier.padding(top = 14.dp),
                color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 13.sp,
            )
            Spacer(Modifier.weight(1f))

            // where you are in it
            Box(
                Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(99.dp)).background(Color.White.copy(alpha = .18f)),
            ) {
                Box(
                    Modifier.fillMaxWidth((elapsed / total.toFloat()).coerceIn(0f, 1f)).height(4.dp)
                        .clip(RoundedCornerShape(99.dp)).background(Pk.GoldLight),
                )
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(clock(elapsed), color = Color.White.copy(alpha = .72f), fontFamily = UtilitySans, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Text(clock(total), color = Color.White.copy(alpha = .72f), fontFamily = UtilitySans, fontSize = 12.sp)
            }

            Row(
                Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(34.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Seek("−15") { seek(-15) }
                Box(
                    Modifier.size(78.dp).clip(CircleShape).background(Color.White.copy(alpha = .16f))
                        .border(1.5.dp, Color.White.copy(alpha = .7f), CircleShape)
                        .clickable { running = !running },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        if (running) "Pause" else "Play",
                        tint = Color.White, modifier = Modifier.size(34.dp),
                    )
                }
                Seek("+15") { seek(15) }
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Tool(Icons.Outlined.Notes, "Transcript") { showTranscript = true }
                Tool(Icons.Outlined.FormatListBulleted, "Chapters") { showChapters = true }
                Tool(Icons.Outlined.Close, "End") { running = false; onEnd(elapsed) }
            }
        }

        if (showChapters) {
            Sheet(onClose = { showChapters = false }) {
                Text("Today's meditation", color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text(
                    "${setup.focus.label} · ${setup.minutes} min",
                    Modifier.padding(top = 4.dp, bottom = 16.dp),
                    color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.sp,
                )
                script.chapters.forEachIndexed { i, c ->
                    val now = i == chapterIndex
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(if (now) Pk.Oxblood.copy(alpha = .07f) else Color.Transparent)
                            .clickable {
                                seek(starts[i] - elapsed)
                                showChapters = false
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(32.dp).clip(CircleShape).background(if (now) Pk.Oxblood else Pk.Oxblood.copy(alpha = .08f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (now) {
                                Icon(Icons.Outlined.Pause, null, tint = Color.White, modifier = Modifier.size(15.dp))
                            } else {
                                Text("${i + 1}", color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Column(Modifier.weight(1f).padding(start = 13.dp)) {
                            Text(c.title, color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(c.subtitle, color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp)
                        }
                        Text(
                            clock((c.share * total).toInt()),
                            color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp,
                        )
                    }
                }
            }
        }
        if (showTranscript) {
            Sheet(onClose = { showTranscript = false }) {
                Text("Transcript", color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                script.chapters.forEach { c ->
                    Text(
                        c.title,
                        Modifier.padding(top = 18.dp),
                        color = Pk.GoldDeep, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    )
                    c.lines.forEach { line ->
                        Text(
                            line,
                            Modifier.padding(top = 6.dp),
                            color = Ink, fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 24.sp,
                        )
                    }
                }
            }
        }
    }
}

private fun clock(seconds: Int) = "${seconds / 60}:${(seconds % 60).pad2()}"

@Composable
private fun Seek(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = .45f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
}

@Composable
private fun Tool(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = .86f), modifier = Modifier.size(21.dp))
        Text(label, Modifier.padding(top = 5.dp), color = Color.White.copy(alpha = .76f), fontFamily = UtilitySans, fontSize = 12.sp)
    }
}

/** A sheet over the session: the chapters, or the words. */
@Composable
private fun Sheet(onClose: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .40f))
            .clickable(interactionSource = null, indication = null, onClick = onClose),
    ) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(Pk.Cream)
                .clickable(interactionSource = null, indication = null) {}
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            Box(
                Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                    .size(width = 40.dp, height = 4.dp).clip(RoundedCornerShape(99.dp)).background(Line),
            )
            content()
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── 8. how was it ───────────────────────────────────────────────────────

private val MOODS = listOf(
    "Peaceful" to Icons.Outlined.Spa,
    "Calm" to Icons.Outlined.SelfImprovement,
    "Refreshed" to Icons.Outlined.WbSunny,
    "Emotional" to Icons.Outlined.FavoriteBorder,
    "Challenged" to Icons.Outlined.LocalFireDepartment,
    "Other" to Icons.Outlined.AutoStories,
)

@Composable
private fun Reflect(setup: MeditationSetup, onSave: (String, String, Boolean) -> Unit) {
    var mood by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var toJournal by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        ) {
            Text(
                "How was your meditation?",
                Modifier.fillMaxWidth().padding(top = 26.dp),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 27.sp, lineHeight = 33.sp, textAlign = TextAlign.Center,
            )
            Column(Modifier.padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MOODS.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (name, icon) ->
                            val on = mood == name
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                                    .background(if (on) Pk.Oxblood.copy(alpha = .08f) else Color.White)
                                    .border(if (on) 1.6.dp else 1.dp, if (on) Pk.Oxblood else Line, RoundedCornerShape(18.dp))
                                    .clickable { mood = if (on) "" else name }
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(24.dp))
                                Text(
                                    name,
                                    Modifier.padding(top = 7.dp),
                                    color = Ink, fontFamily = UtilitySans, fontSize = 13.sp,
                                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }
            Box(
                Modifier.fillMaxWidth().padding(top = 18.dp).height(150.dp).clip(RoundedCornerShape(20.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(20.dp)).padding(16.dp),
            ) {
                if (text.isBlank()) {
                    Text(
                        "What did you sense, learn or feel?",
                        color = Pk.Muted, fontFamily = BookSerif, fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    text, { text = it },
                    textStyle = TextStyle(color = Ink, fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 25.sp),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Row(
                Modifier.padding(top = 14.dp).clip(RoundedCornerShape(10.dp)).clickable { toJournal = !toJournal }
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (toJournal) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank, null,
                    tint = Pk.Oxblood, modifier = Modifier.size(21.dp),
                )
                Text(
                    "Save to my journal",
                    Modifier.padding(start = 10.dp),
                    color = Ink, fontFamily = UtilitySans, fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(20.dp))
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Save and continue") { onSave(mood, text, toJournal) }
        }
    }
}

// ── 9. well done, and the journey ───────────────────────────────────────

@Composable
private fun Done(setup: MeditationSetup, seconds: Int, onAgain: () -> Unit, onClose: () -> Unit) {
    val context = platformContext()
    val record = remember { MeditationStore.record(context) }
    val minutes = (seconds + 30) / 60
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.padding(top = 30.dp).size(76.dp).clip(CircleShape).background(Pk.Oxblood),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(36.dp)) }
            Text(
                "Well done",
                Modifier.padding(top = 18.dp),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 29.sp,
            )
            Text(
                if (minutes > 0) "You sat with God for $minutes ${if (minutes == 1) "minute" else "minutes"}." else "You took a moment with God.",
                Modifier.padding(top = 6.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp,
            )

            Row(
                Modifier.fillMaxWidth().padding(top = 26.dp).clip(RoundedCornerShape(22.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.LocalFireDepartment, null, tint = Pk.Oxblood, modifier = Modifier.size(28.dp))
                Column(Modifier.padding(start = 14.dp)) {
                    Text(
                        "${record.streak()}",
                        color = Pk.Oxblood, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 26.sp,
                    )
                    Text("Day streak", color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp)
                }
            }
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${record.sessions}", "Sessions", Modifier.weight(1f))
                Stat("${record.seconds / 60}", "Minutes", Modifier.weight(1f))
            }
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${record.topics.size}", "Topics", Modifier.weight(1f))
                Stat("${record.verses.size}", "Verses", Modifier.weight(1f))
            }

            Text(
                "In quietness and trust is your strength.",
                Modifier.fillMaxWidth().padding(top = 24.dp),
                color = Pk.Muted, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 15.sp, textAlign = TextAlign.Center,
            )
            Text(
                "Isaiah 30:15",
                Modifier.padding(top = 4.dp, bottom = 20.dp),
                color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 12.sp,
            )
        }
        Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Try another meditation", onClick = onAgain)
            Box(
                Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(99.dp))
                    .clickable(onClick = onClose).padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Back to home", color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

/** The card on Home, under the fast. */
@Composable
fun MeditationCard(onOpen: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(168.dp).clip(RoundedCornerShape(28.dp)).clickable(onClick = onOpen),
    ) {
        Image(
            painterResource(Res.drawable.rhythm_reflection), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Pk.DeepDark.copy(alpha = .90f), Pk.Oxblood.copy(alpha = .30f))),
            ),
        )
        Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 24.dp)) {
            Text(
                "Meditation",
                color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            )
            Text(
                "Be still with God",
                Modifier.padding(top = 8.dp),
                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 25.sp, lineHeight = 30.sp,
            )
            Text(
                "Quiet your heart. Hear God. Find peace.",
                Modifier.padding(top = 10.dp),
                color = Color.White.copy(alpha = .82f), fontFamily = UtilitySans, fontSize = 13.sp,
            )
        }
    }
}
