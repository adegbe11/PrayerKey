package com.prayerkey.manna.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import com.prayerkey.manna.ui.church.SERMON_LANGUAGES
import com.prayerkey.manna.ui.church.sermonLanguageLabel
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.prayerkey.manna.data.SermonNote
import com.prayerkey.manna.ui.church.SermonArranger
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.prayerkey.manna.ui.church.SermonRefiner
import com.prayerkey.manna.ui.church.SermonService
import com.prayerkey.manna.ui.church.SpeechModel
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.prayerkey.manna.ui.theme.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class Stage { Ready, Listening, Arranging, Note }

@Composable
fun ChurchScreen(
    notes: List<SermonNote>,
    language: String,
    onLanguage: (String) -> Unit,
    onSaveNote: (SermonArranger.Note, Int, String) -> Unit,
    onRefineNote: (Long, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val listening by SermonService.listening.collectAsState()
    val caught by SermonService.references.collectAsState()
    val partial by SermonService.partial.collectAsState()
    val chunks by SermonService.chunks.collectAsState()
    val captureStatus by SermonService.status.collectAsState()
    val startedAt by SermonService.startedAt.collectAsState()

    var stage by remember { mutableStateOf(Stage.Ready) }
    var arranged by remember { mutableStateOf<SermonArranger.Note?>(null) }
    var minutes by remember { mutableIntStateOf(0) }
    var openNote by remember { mutableStateOf<SermonNote?>(null) }
    var pickLanguage by remember { mutableStateOf(false) }

    BackHandler(enabled = stage == Stage.Note) {
        arranged = null
        stage = Stage.Ready
    }

    // the service outlives this screen — pick the stage back up on return
    LaunchedEffect(listening) {
        if (listening) stage = Stage.Listening
        else if (stage == Stage.Listening) stage = Stage.Ready
    }

    /* One question at a time, then the recording. Both prompts used to be
       launched together; Android shows one and drops the other, so on a first
       Sunday Start asked about notifications and then did nothing - the
       microphone was never asked for and nothing was recorded until Start was
       tapped a second time. Now: the microphone, then (optionally) the
       notification that keeps the recording alive, then the recording -
       whatever is answered to the second. */
    val notifyPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        SermonService.start(context)
    }
    fun startRecording() {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        else SermonService.start(context)
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startRecording()
    }

    /* Never remember a decision to continue without captions. The old flow
       stored `captions-asked=true` after "Just record", so every later sermon
       silently opened as a blank recorder on that phone. Church is a live
       transcription feature: while the model is missing, every new attempt
       must offer the one-time install again. */
    var offerCaptions by remember { mutableStateOf(false) }

    fun record() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        SermonService.setLanguage(language)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecording()
        } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun begin() {
        // Android's continuous recognizer is the primary live-caption path.
        // The service falls back to the downloaded Vosk model only on devices
        // that do not provide a system recognizer.
        record()
    }

    if (offerCaptions) {
        CaptionsOffer(
            onSkip = {
                offerCaptions = false
                record()
            },
            onReady = {
                offerCaptions = false
                record()
            },
        )
    }

    fun end() {
        if (stage != Stage.Listening) return
        stage = Stage.Arranging
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        minutes = SermonService.elapsedMinutes()
        val transcript = SermonService.transcript(context)
        val refs = caught.map { it.reference }
        // read before stop() clears it — the note needs it to be re-transcribed later
        val audio = SermonService.audioPath.value
        SermonService.stop(context)
        if (transcript.isBlank()) {
            /* A service still happened. Discarding it because the recogniser
               produced nothing threw away the recording, the duration and any
               references caught — nothing reached the archive or the journal,
               and the audio was left on disk with no note pointing at it.
               The note is saved with its audio, so it appears where every other
               sermon appears and "Improve this transcript" can turn it into
               text afterwards using the larger model. */
            val note = SermonArranger.Note(
                title = "Sermon · ${todayLabel()}",
                scriptures = refs,
                points = emptyList(),
                quotes = emptyList(),
                takeaway = "",
                transcript = "",
            )
            onSaveNote(note, minutes, audio)
            stage = Stage.Ready
            // only promise the recording when there is one: with the phone's
            // own recogniser listening, nothing is recorded to go back to
            Toast.makeText(
                context,
                if (!audio.isNullOrBlank()) "Saved to your sermon notes. Open it to turn the recording into text."
                else "No words were heard. Hold the phone nearer the speaker next time.",
                Toast.LENGTH_LONG,
            ).show()
            return
        }
        arranged = SermonArranger.arrange(transcript, refs, language).also { onSaveNote(it, minutes, audio) }
    }

    val cs = MaterialTheme.colorScheme
    Box(
        Modifier.fillMaxSize().background(
            // the chosen theme, not a fixed daylight wash — this screen was
            // a sheet of white in every one of the eleven other themes
            Brush.verticalGradient(
                0f to androidx.compose.ui.graphics.lerp(cs.background, cs.primary, .13f),
                .5f to cs.background,
                1f to androidx.compose.ui.graphics.lerp(cs.background, Color.Black, .14f),
            ),
        ),
    ) {
        when (stage) {
            Stage.Ready -> ReadyView(
                notes = notes,
                language = language,
                onLanguage = { pickLanguage = true },
                onOpen = { openNote = it },
                onStart = { begin() },
            )
            Stage.Listening -> ListeningView(
                startedAt = startedAt,
                caught = caught,
                partial = partial,
                chunks = chunks,
                captureStatus = captureStatus,
                onEnd = { end() },
            )
            Stage.Arranging -> ArrangingView { stage = Stage.Note }
            Stage.Note -> arranged?.let { note ->
                NoteView(
                    note = note, minutes = minutes,
                    onSave = {
                        // the references live in the note itself; we don't mint
                        // saved-word cards here because detection gives us the
                        // reference, not the verse text — an empty card is junk
                        SermonService.reset()
                        arranged = null
                        stage = Stage.Ready
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    },
                    onShare = {
                        val text = SermonArranger.asShareText(note, todayLabel(), minutes)
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, note.title)
                                    putExtra(Intent.EXTRA_TEXT, text)
                                },
                                "Send my notes",
                            ),
                        )
                    },
                    onDiscard = { SermonService.reset(); arranged = null; stage = Stage.Ready },
                )
            }
        }
    }

    if (pickLanguage) LanguageSheet(
        current = language,
        onPick = { onLanguage(it); pickLanguage = false },
        onClose = { pickLanguage = false },
    )

    openNote?.let { saved ->
        SavedNoteSheet(saved, language, onRefine = onRefineNote, onClose = { openNote = null }, onDelete = { onDeleteNote(saved.id); openNote = null })
    }
}

/* ─────────────────────────── READY ─────────────────────────── */

/**
 * Turning live captions on.
 *
 * Recording always works and never depended on this. What did not work was the
 * transcription: the shipped build pointed at a socket relay that answers 404,
 * and the offline recogniser could never start because nothing ever installed
 * its model. This is the switch that installs it.
 *
 * It states the size before it spends the user's data, because 39 MB on a phone
 * plan is a real cost and a spinner that appears without warning is how you
 * lose someone's trust in a church car park.
 *
 * Once installed it says so once and gets out of the way — a permanent banner
 * advertising a solved problem is clutter.
 */
@Composable
private fun CaptionsOffer(onSkip: () -> Unit, onReady: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by SpeechModel.state(SpeechModel.Kind.Speech).collectAsState()
    LaunchedEffect(Unit) { SpeechModel.refresh(context, SpeechModel.Kind.Speech) }
    // installing is the answer to the question; go straight on to recording
    LaunchedEffect(state) { if (state is SpeechModel.State.Ready) onReady() }

    val cs = MaterialTheme.colorScheme
    val busy = state is SpeechModel.State.Downloading || state is SpeechModel.State.Unpacking

    Dialog(onDismissRequest = { if (!busy) onSkip() }) {
    Column(
        Modifier.fillMaxWidth()
            .clip(R.card)
            .background(cs.surface)
            .padding(22.dp),
    ) {
        Text(
            "Live captions",
            color = cs.onBackground, fontFamily = DisplaySerif,
            fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            when (val s = state) {
                is SpeechModel.State.Failed ->
                    "That did not download: ${s.reason}. Recording still works — you can try again."
                else ->
                    "See the sermon written out as it is spoken, and catch the verses " +
                        "quoted. It runs on your phone, so it works with no signal and costs " +
                        "nothing. You only need this once."
            },
            color = cs.onBackground.copy(alpha = .68f), fontSize = 13.5.sp, lineHeight = 20.sp,
        )

        when (val s = state) {
            is SpeechModel.State.Downloading -> {
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { s.fraction },
                    color = cs.primary,
                    trackColor = cs.onBackground.copy(alpha = .10f),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(R.pill),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Downloading · ${(s.fraction * SpeechModel.Kind.Speech.megabytes).toInt()} of ${SpeechModel.Kind.Speech.megabytes} MB",
                    color = cs.onBackground.copy(alpha = .5f), fontSize = 12.sp,
                )
            }
            SpeechModel.State.Unpacking -> {
                Spacer(Modifier.height(14.dp))
                Text("Installing…", color = cs.onBackground.copy(alpha = .5f), fontSize = 12.sp)
            }
            else -> {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.clip(R.pill).background(cs.primary)
                            .clickable { scope.launch { SpeechModel.install(context, SpeechModel.Kind.Speech) } }
                            .padding(horizontal = 18.dp, vertical = 11.dp),
                    ) {
                        Text(
                            if (state is SpeechModel.State.Failed) "Try again"
                            else "Turn on · ${SpeechModel.Kind.Speech.megabytes} MB",
                            color = cs.onPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    /* Recording has never needed the model, so declining has to
                       start the recording rather than cancel it — the sermon is
                       beginning and this question is not worth missing it for. */
                    Box(
                        Modifier.clip(R.pill).clickable(onClick = onSkip)
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                    ) {
                        Text(
                            "Just record",
                            color = cs.onBackground.copy(alpha = .6f),
                            fontSize = 13.5.sp, fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun ReadyView(
    notes: List<SermonNote>,
    language: String,
    onLanguage: () -> Unit,
    onOpen: (SermonNote) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).padding(top = 24.dp, bottom = 120.dp),
    ) {
        val cs = MaterialTheme.colorScheme

        /* The language picker used to sit in the middle of the screen under
           the button, a form control competing with the one thing you came
           here to press. It belongs beside the title. */
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Church", color = cs.onBackground,
                fontFamily = DisplaySerif, fontSize = 32.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Row(
                Modifier.clip(R.pill).clickable(onClick = onLanguage)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Language, "Change language",
                    tint = cs.onBackground.copy(alpha = .55f), modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    sermonLanguageLabel(language),
                    color = cs.onBackground.copy(alpha = .55f),
                    fontSize = 12.5.sp, fontWeight = FontWeight.Medium,
                )
            }
        }

        Box(
            Modifier.fillMaxWidth().padding(top = 28.dp)
                .shadow(24.dp, R.card, spotColor = cs.primary.copy(alpha = .30f))
                .clip(R.card).background(NightGloss)
                .border(1.dp, Ivory.copy(alpha = .12f), R.card),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Sanctuary(GiltLine, Ivory, Modifier.fillMaxWidth().height(390.dp).graphicsLayer(alpha = .18f))
            Box(Modifier.fillMaxWidth().height(390.dp).background(TopSheen))

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Sermon notes",
                    color = Ivory, fontFamily = DisplaySerif, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(20.dp))
                /* The circle and its word are one button. Only the circle
                   answered, so tapping "Start" - the word that says what to
                   do - did nothing. */
                Column(
                    Modifier.clip(RoundedCornerShape(28.dp))
                        .clickable(onClick = onStart)
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.size(106.dp)
                            .shadow(16.dp, CircleShape, spotColor = Color.Black.copy(alpha = .34f))
                            .clip(CircleShape)
                            .background(cs.primary)
                            .border(1.2.dp, Ivory.copy(alpha = .30f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Mic, "Listen to the Message",
                            tint = Ivory, modifier = Modifier.size(42.dp),
                        )
                    }
                    Text(
                        "Start",
                        color = Ivory, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                }
            }
        }

        if (notes.isNotEmpty()) {
            Text("Your Sundays", color = cs.onBackground, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 40.dp, bottom = 10.dp))
            notes.take(6).forEach { note ->
                Box(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        .clip(R.card).background(cs.surface)
                        .clickable { onOpen(note) },
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(note.title, color = cs.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            /* Only what the title does not already say: the date
                               is dropped when the title carries it, and a count
                               of none is not shown. */
                            listOfNotNull(
                                // an untitled note is already named for its day
                                dayLabel(note.createdAt).takeUnless { note.title.startsWith("Sermon · ") },
                                "${note.minutes} min",
                                note.scriptures.size.takeIf { it > 0 }?.let { if (it == 1) "1 scripture" else "$it scriptures" },
                            ).joinToString(" · "),
                            color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp),
                        )
                        if (note.takeaway.isNotBlank()) Text(
                            note.takeaway, color = Ink, fontSize = 13.sp, maxLines = 2,
                            fontFamily = BookSerif, modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

/* ────────────────────────── LISTENING ────────────────────────── */

/**
 * Listening.
 *
 * This screen used to carry a title, a 52sp timer, a waveform, a status line,
 * a "LIVE TRANSCRIPT" label, the transcript inside its own card, a "LISTENING
 * FOR SCRIPTURE" label, a list of caught references and a full-width button —
 * nine things stacked above the one thing the user is here to watch.
 *
 * Live Transcribe shows the words and a thin bar. That is the right answer,
 * and not because it is fashionable: during a sermon the phone is at arm's
 * length or face-down on a knee, and anything that is not the text is noise
 * you cannot read anyway.
 *
 * So: the transcript fills the screen and follows itself down. Everything else
 * is one bar at the bottom — a live dot, the elapsed time, the count of verses
 * caught, and stop. The references are not lost; they are on the note
 * afterwards, which is where anybody actually reads them.
 */
@Composable
private fun ListeningView(
    startedAt: Long,
    caught: List<SermonService.Caught>,
    partial: String,
    chunks: List<String>,
    captureStatus: String,
    onEnd: () -> Unit,
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt) {
        while (true) { now = System.currentTimeMillis(); delay(1000) }
    }
    val secs = if (startedAt == 0L) 0 else ((now - startedAt) / 1000L).toInt().coerceAtLeast(0)

    val scroll = rememberScrollState()
    // follow the words down, the way a transcript should
    LaunchedEffect(chunks.size, partial) { scroll.animateScrollTo(scroll.maxValue) }

    val cs = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize()) {

        Column(
            // below the status bar: "Listening…" was drawn under the clock
            Modifier.weight(1f).fillMaxWidth().statusBarsPadding().verticalScroll(scroll)
                .padding(horizontal = 24.dp).padding(top = 28.dp, bottom = 16.dp),
        ) {
            if (chunks.isEmpty() && partial.isBlank()) {
                Text(
                    if (captureStatus.contains("unavailable")) {
                        "Recording. Captions are off — the words can be added afterwards."
                    } else {
                        "Listening…"
                    },
                    color = Muted, fontFamily = BookSerif, fontSize = 21.sp, lineHeight = 31.sp,
                )
            } else {
                /* One flowing body rather than a stack of separate blocks. A
                   transcript is continuous speech; breaking it into cards
                   implies boundaries the speaker never made. */
                chunks.takeLast(40).forEach { chunk ->
                    Text(
                        chunk,
                        color = Ink, fontFamily = BookSerif,
                        fontSize = 22.sp, lineHeight = 33.sp,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                if (partial.isNotBlank()) {
                    Text(
                        partial,
                        color = Muted, fontFamily = BookSerif,
                        fontSize = 22.sp, lineHeight = 33.sp,
                    )
                }
            }
        }

        /* The only chrome. */
        Row(
            Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 108.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val pulse by rememberInfiniteTransition(label = "rec").animateFloat(
                initialValue = .35f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
                label = "rec-dot",
            )
            Box(
                Modifier.size(9.dp).clip(CircleShape)
                    .background(cs.primary.copy(alpha = pulse)),
            )
            Text(
                "%02d:%02d".format(secs / 60, secs % 60),
                color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 10.dp),
            )
            if (caught.isNotEmpty()) {
                Text(
                    "· ${caught.size} verse${if (caught.size == 1) "" else "s"}",
                    color = Muted, fontSize = 13.sp,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.clip(R.pill).background(cs.primary).clickable(onClick = onEnd)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Stop, null, tint = Color.White, modifier = Modifier.size(17.dp))
                Text(
                    "Stop",
                    color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp,
                    modifier = Modifier.padding(start = 7.dp),
                )
            }
        }
    }
}

/* ────────────────────────── ARRANGING ────────────────────────── */

@Composable
private fun ArrangingView(onDone: () -> Unit) {
    LaunchedEffect(Unit) { delay(1400); onDone() }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp, modifier = Modifier.size(42.dp))
        Text("Arranging your notes", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 22.dp))
    }
}

/* ──────────────────────────── NOTE ──────────────────────────── */

@Composable
private fun NoteView(
    note: SermonArranger.Note,
    minutes: Int,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onDiscard: () -> Unit,
) {
    var showTranscript by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).padding(top = 24.dp, bottom = 130.dp),
    ) {
        Text("Sunday notes", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(note.title, fontFamily = BookSerif, fontSize = 33.sp, lineHeight = 38.sp, modifier = Modifier.padding(top = 8.dp))
        Text(
            "${todayLabel()} · $minutes min · ${note.scriptures.size} scriptures caught",
            color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp),
        )

        if (note.scriptures.isNotEmpty()) {
            SectionLabel("Scriptures preached")
            FlowChips(note.scriptures)
        }

        if (note.points.isNotEmpty()) {
            SectionLabel("The main points")
            note.points.forEachIndexed { i, point ->
                Row(Modifier.padding(bottom = 13.dp)) {
                    Text("${i + 1}.", color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                    Text(point, fontSize = 15.sp, lineHeight = 23.sp)
                }
            }
        }

        if (note.quotes.isNotEmpty()) {
            SectionLabel("Lines worth keeping")
            note.quotes.forEach { quote ->
                Row(Modifier.padding(bottom = 16.dp)) {
                    Box(Modifier.width(3.dp).height(52.dp).background(Gold))
                    Text(
                        quote, fontFamily = BookSerif, fontSize = 19.sp,
                        lineHeight = 27.sp, modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }

        if (note.takeaway.isNotBlank()) {
            SectionLabel("Carry this into your week")
            Box(Modifier.fillMaxWidth().premiumCard(fill = NightFill).goldEdge()) {
                Text(
                    note.takeaway, color = Ivory, fontFamily = BookSerif,
                    fontSize = 19.sp, lineHeight = 27.sp, modifier = Modifier.padding(21.dp),
                )
            }
        }

        val empty = note.points.isEmpty() && note.quotes.isEmpty() && note.scriptures.isEmpty()
        if (empty) {
            Text(
                "Nothing was caught this time. The mic may not have picked up the preaching — try sitting closer to the front, or check that offline speech is installed for your language.",
                color = Muted, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 30.dp),
            )
        }

        // nothing caught means nothing worth keeping — don't offer to save junk
        if (!empty) Row(Modifier.fillMaxWidth().padding(top = 26.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier.weight(1f).height(52.dp)
                    .shadow(6.dp, R.control, spotColor = cs.onBackground.copy(alpha = .16f))
                    .clip(R.control).background(cs.primary).clickable(onClick = onSave),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Save to journal", color = cs.onPrimary,
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                )
            }
            /* Was a white pill with an unset text colour: once the type scale
               stopped carrying a baked-in ink, that text took the theme's and
               went pale-on-white. And its neighbour was electric blue, the
               loudest thing on a screen about listening. */
            Box(
                Modifier.weight(1f).height(52.dp)
                    .clip(R.control)
                    .border(1.dp, cs.outlineVariant, R.control).clickable(onClick = onShare),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Share", color = cs.onBackground,
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                )
            }
        }

        if (note.transcript.isNotBlank()) {
            Text(
                if (showTranscript) "Hide what he said" else "See everything he said",
                color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp).clickable { showTranscript = !showTranscript },
            )
            AnimatedVisibility(showTranscript) {
                Text(
                    note.transcript.replace(" | ", ". "),
                    color = Muted, fontSize = 13.sp, lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }

        Text(
            if (empty) "Back to Church" else "Discard this note",
            color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 26.dp).clickable(onClick = onDiscard),
        )
    }
}

/**
 * Offers the better transcript, once there is a rough one to compare it to.
 *
 * The small model has to keep up with speech in real time, which caps how good
 * it can be; the larger one is far too slow to caption live but has no clock to
 * beat once the sermon is over — and the recording was kept anyway. So the
 * quality ceiling of live captioning is not the quality ceiling of the note.
 *
 * Offered here rather than up front because this is where the value is legible:
 * the user is looking at a transcript they can see the flaws in. Asking on the
 * Church screen would be asking someone to spend 125 MB on a problem they have
 * not met yet.
 *
 * Hidden entirely once the note has been refined, and when the recording has
 * been deleted — an action with nothing behind it is worse than no action.
 */
@Composable
private fun RefineRow(note: SermonNote, language: String, onRefine: (Long, String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme
    val model by SpeechModel.state(SpeechModel.Kind.Speech).collectAsState()
    var progress by remember(note.id) { mutableStateOf<Float?>(null) }
    var failure by remember(note.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { SpeechModel.refresh(context, SpeechModel.Kind.Speech) }

    if (note.refined || note.audioPath.isBlank() || !java.io.File(note.audioPath).isFile) return

    fun run() = scope.launch {
        failure = null
        if (!SpeechModel.isInstalled(context, SpeechModel.Kind.Speech)) {
            if (!SpeechModel.install(context, SpeechModel.Kind.Speech)) {
                failure = "the download did not finish"
                return@launch
            }
        }
        progress = 0f
        SermonRefiner.refine(context, java.io.File(note.audioPath)) { progress = it }
            .onSuccess { onRefine(note.id, it) }
            .onFailure { failure = it.message ?: "it could not be read" }
        progress = null
    }

    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        when {
            progress != null -> {
                LinearProgressIndicator(
                    progress = { progress ?: 0f },
                    color = cs.primary, trackColor = cs.onBackground.copy(alpha = .10f),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(R.pill),
                )
                Text(
                    "Reading the recording · ${((progress ?: 0f) * 100).toInt()}%",
                    color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 7.dp),
                )
            }
            model is SpeechModel.State.Downloading -> {
                val f = (model as SpeechModel.State.Downloading).fraction
                LinearProgressIndicator(
                    progress = { f },
                    color = cs.primary, trackColor = cs.onBackground.copy(alpha = .10f),
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(R.pill),
                )
                Text(
                    "Getting ready · ${(f * SpeechModel.Kind.Speech.megabytes).toInt()} of ${SpeechModel.Kind.Speech.megabytes} MB",
                    color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 7.dp),
                )
            }
            else -> {
                Box(
                    Modifier.clip(R.pill).background(cs.primary.copy(alpha = .10f))
                        .clickable { run() }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Text(
                        if (SpeechModel.isInstalled(context, SpeechModel.Kind.Speech)) "Improve this transcript"
                        else "Improve this transcript · ${SpeechModel.Kind.Speech.megabytes} MB once",
                        color = cs.primary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    failure?.let { "That did not work: $it." }
                        ?: "A few minutes. Never leaves your phone.",
                    color = Muted, fontSize = 11.5.sp, lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 7.dp),
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SavedNoteSheet(
    note: SermonNote,
    language: String,
    onRefine: (Long, String) -> Unit,
    onClose: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onClose, containerColor = Canvas) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp).padding(bottom = 40.dp),
        ) {
            Text(note.title, fontFamily = BookSerif, fontSize = 28.sp, lineHeight = 33.sp)
            RefineRow(note, language, onRefine)
            Text(
                "${dayLabel(note.createdAt)} · ${note.minutes} min",
                color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp),
            )
            if (note.scriptures.isNotEmpty()) { SectionLabel("Scriptures"); FlowChips(note.scriptures) }
            if (note.points.isNotEmpty()) {
                SectionLabel("Main points")
                note.points.forEachIndexed { i, p ->
                    Row(Modifier.padding(bottom = 11.dp)) {
                        Text("${i + 1}.", color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 11.dp))
                        Text(p, fontSize = 14.sp, lineHeight = 21.sp)
                    }
                }
            }
            if (note.quotes.isNotEmpty()) {
                SectionLabel("Lines worth keeping")
                note.quotes.forEach { Text(it, fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, modifier = Modifier.padding(bottom = 11.dp)) }
            }
            if (note.takeaway.isNotBlank()) {
                SectionLabel("Carry this")
                Surface(shape = RoundedCornerShape(16.dp), color = Night, modifier = Modifier.fillMaxWidth()) {
                    Text(note.takeaway, color = Ivory, fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, modifier = Modifier.padding(18.dp))
                }
            }
            Button(
                onClick = { com.prayerkey.manna.share.MannaPack.share(context, note) },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Icon(Icons.Outlined.Share, "Share these Sunday notes")
                Spacer(Modifier.width(8.dp))
                Text("Share these notes")
            }
            Text(
                "Your private Journey is never included.",
                color = Muted, fontSize = 10.5.sp, lineHeight = 15.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            )
            Text(
                "Delete this note",
                color = Color(0xFFE0526B), fontSize = 13.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 26.dp).clickable(onClick = onDelete),
            )
        }
    }
}

/* ─────────────────────────── PIECES ─────────────────────────── */

@Composable
private fun SectionLabel(text: String) = Text(
    text, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
    modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
)

@Composable
private fun FlowChips(items: List<String>) {
    Column {
        items.chunked(2).forEach { row ->
            Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    Surface(shape = RoundedCornerShape(99.dp), color = Gold.copy(alpha = .13f), border = BorderStroke(1.dp, Gold.copy(alpha = .3f))) {
                        Text(item, color = Gold, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp))
                    }
                }
            }
        }
    }
}

/** 26 bars; only the listening state animates, so Ready costs nothing. */
@Composable
private fun Wave(active: Boolean, modifier: Modifier = Modifier) {
    val heights = remember { List(26) { (6 + (it * 17) % 40).dp } }
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "wave-phase",
    )
    Row(
        modifier.fillMaxWidth().height(70.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        heights.forEachIndexed { i, h ->
            Box(
                Modifier.width(3.dp).height(h)
                    .graphicsLayer {
                        scaleY = if (!active) 1f else {
                            val offset = ((i * 0.17f) + phase) % 1f
                            0.5f + (if (offset < .5f) offset else 1f - offset) * 1.4f
                        }
                    }
                    .background(if (active) Electric else Hairline, CircleShape),
            )
        }
    }
}

private fun todayLabel(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

private fun dayLabel(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

private fun clockLabel(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a"))

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSheet(current: String, onPick: (String) -> Unit, onClose: () -> Unit) {
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onClose, containerColor = Canvas) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 30.dp)) {
            Text("Where is the service?", fontFamily = BookSerif, fontSize = 24.sp)
            Text(
                "English captions. Region tunes the spelling and the turns of phrase.",
                fontSize = 13.sp, color = Muted, modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
            )
            androidx.compose.foundation.lazy.LazyColumn(
                Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(SERMON_LANGUAGES, key = { it.tag }) { lang ->
                    val on = lang.tag == current
                    Surface(
                        onClick = { onPick(lang.tag) },
                        shape = R.control,
                        color = if (on) Ivory else Color.White,
                        border = BorderStroke(1.dp, if (on) Gold.copy(alpha = .5f) else Hairline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    lang.label, fontSize = 15.sp,
                                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                                )
                                // say plainly which languages get the sharper
                                // treatment rather than being quietly worse
                                if (SermonArranger.hasCuesFor(lang.tag)) {
                                    Text("Full notes", color = Gold, fontSize = 13.sp)
                                } else {
                                    Text("Basic notes", color = Muted, fontSize = 13.sp)
                                }
                            }
                            if (on) Icon(Icons.Outlined.Check, null, tint = Gold, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * A room to wait in.
 *
 * The idle screen was a large white field with a button in it. This puts a
 * sanctuary behind the button — three arches, a rose window and a pool of
 * light — drawn on a Canvas so it costs nothing in the APK and takes the
 * colour of whichever theme is on. The light breathes, so the screen is
 * alive before the service starts without playing a sound at anyone.
 */
@Composable
private fun Sanctuary(accent: Color, ink: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val w = size.width
        val h = size.height

        val line = ink.copy(alpha = .17f)
        val stroke = Stroke(width = 1.6f)

        // three arches, the middle one taller
        // the arches clear the button; at the old heights the mic sat on top
        // of the rose window and swallowed it
        listOf(
            Triple(w * .50f, w * .195f, h * .09f),
            Triple(w * .165f, w * .120f, h * .34f),
            Triple(w * .835f, w * .120f, h * .34f),
        ).forEach { (cx, half, top) ->
            val base = h * .80f
            val path = Path().apply {
                moveTo(cx - half, base)
                lineTo(cx - half, top + half)
                quadraticBezierTo(cx - half, top, cx, top)
                quadraticBezierTo(cx + half, top, cx + half, top + half)
                lineTo(cx + half, base)
            }
            drawPath(path, line, style = stroke)
        }

        // the rose window
        drawCircle(
            accent.copy(alpha = .38f), radius = w * .052f,
            center = Offset(w / 2f, h * .21f), style = Stroke(width = 1.4f),
        )
        for (i in 0 until 8) {
            val a = i * Math.PI.toFloat() / 4f
            drawLine(
                accent.copy(alpha = .22f),
                Offset(w / 2f, h * .21f),
                Offset(
                    w / 2f + kotlin.math.cos(a) * w * .052f,
                    h * .21f + kotlin.math.sin(a) * w * .052f,
                ),
                strokeWidth = 1f,
            )
        }

        // the floor
        drawLine(line, Offset(0f, h * .80f), Offset(w, h * .80f), strokeWidth = 1.4f)
    }
}
