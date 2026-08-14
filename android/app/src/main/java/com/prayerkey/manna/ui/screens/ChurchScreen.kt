package com.prayerkey.manna.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.prayerkey.manna.ui.church.SermonService
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
    onSaveNote: (SermonArranger.Note, Int) -> Unit,
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

    // the service outlives this screen — pick the stage back up on return
    LaunchedEffect(listening) {
        if (listening) stage = Stage.Listening
        else if (stage == Stage.Listening) stage = Stage.Ready
    }

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) SermonService.start(context)
    }
    val notifyPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun begin() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        SermonService.setLanguage(language)
        val power = context.getSystemService(PowerManager::class.java)
        if (!power.isIgnoringBatteryOptimizations(context.packageName)) {
            Toast.makeText(context, "Allow PrayerKey to run during the full sermon, then tap Start again.", Toast.LENGTH_LONG).show()
            runCatching {
                context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:${context.packageName}")))
            }.recoverCatching {
                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
            return
        }
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            SermonService.start(context)
        } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun end() {
        if (stage != Stage.Listening) return
        stage = Stage.Arranging
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        minutes = SermonService.elapsedMinutes()
        val transcript = SermonService.transcript()
        val refs = caught.map { it.reference }
        SermonService.stop(context)
        if (transcript.isBlank()) {
            stage = Stage.Ready
            Toast.makeText(context, "No words were transcribed. Audio backup kept.", Toast.LENGTH_LONG).show()
            return
        }
        arranged = SermonArranger.arrange(transcript, refs, language).also { onSaveNote(it, minutes) }
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
        SavedNoteSheet(saved, onClose = { openNote = null }, onDelete = { onDeleteNote(saved.id); openNote = null })
    }
}

/* ─────────────────────────── READY ─────────────────────────── */

@Composable
private fun ReadyView(
    notes: List<SermonNote>,
    language: String,
    onLanguage: () -> Unit,
    onOpen: (SermonNote) -> Unit,
    onStart: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
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
                Text("SERMON COMPANION", color = Ivory.copy(alpha = .60f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(
                    "Remember every word",
                    color = Ivory, fontFamily = DisplaySerif, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    Modifier.size(106.dp)
                        .shadow(16.dp, CircleShape, spotColor = Color.Black.copy(alpha = .34f))
                        .clip(CircleShape)
                        .background(cs.primary)
                        .border(1.2.dp, Ivory.copy(alpha = .30f), CircleShape)
                        .clickable(onClick = onStart),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Mic, "Listen to the Message",
                        tint = Ivory, modifier = Modifier.size(42.dp),
                    )
                }
                Text(
                    "Start listening",
                    color = Ivory, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 14.dp),
                )
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
                            "${dayLabel(note.createdAt)} · ${note.minutes} min · ${note.scriptures.size} scriptures",
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

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).padding(top = 24.dp, bottom = 120.dp),
    ) {
        Text("Listening", fontFamily = BookSerif, fontSize = 32.sp)

        Column(Modifier.fillMaxWidth().padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "%02d:%02d".format(secs / 60, secs % 60),
                fontSize = 52.sp, fontWeight = FontWeight.Light, color = Ink,
            )
            Wave(active = true, modifier = Modifier.padding(vertical = 26.dp))
            Text(
                captureStatus,
                color = if (captureStatus.startsWith("Live")) MaterialTheme.colorScheme.primary else Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }

        Text("LIVE TRANSCRIPT", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp, modifier = Modifier.padding(bottom = 10.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface).padding(18.dp),
        ) {
            if (chunks.isEmpty() && partial.isBlank()) {
                Text("Listening…", color = Muted, fontFamily = BookSerif, fontSize = 18.sp)
            } else {
                chunks.takeLast(12).forEach { chunk ->
                    Text(chunk, color = Ink, fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 27.sp, modifier = Modifier.padding(bottom = 9.dp))
                }
                if (partial.isNotBlank()) Text(
                    partial, color = Muted, fontFamily = BookSerif,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 17.sp, lineHeight = 25.sp,
                )
            }
        }

        Text(
            if (caught.isEmpty()) "LISTENING FOR SCRIPTURE" else "CAUGHT SO FAR",
            color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        )
        caught.reversed().forEach { hit ->
            Box(Modifier.fillMaxWidth().padding(bottom = 10.dp).premiumCard(lift = false)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(hit.reference, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Text(clockLabel(hit.atMillis), color = Muted, fontSize = 11.sp)
                }
            }
        }

        Box(
            Modifier.fillMaxWidth().padding(top = 22.dp).height(56.dp)
                .shadow(14.dp, R.control, spotColor = SoftShadow)
                .clip(R.control).background(NightGloss).clickable(onClick = onEnd),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Stop, null, tint = Gold, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(9.dp))
                Text("Service ended · Arrange my notes", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
        Text("SUNDAY NOTES · WRITTEN FOR YOU", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text(note.title, fontFamily = BookSerif, fontSize = 33.sp, lineHeight = 38.sp, modifier = Modifier.padding(top = 8.dp))
        Text(
            "${todayLabel()} · $minutes min · ${note.scriptures.size} scriptures caught",
            color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp),
        )

        if (note.scriptures.isNotEmpty()) {
            SectionLabel("SCRIPTURES PREACHED")
            FlowChips(note.scriptures)
        }

        if (note.points.isNotEmpty()) {
            SectionLabel("THE MAIN POINTS")
            note.points.forEachIndexed { i, point ->
                Row(Modifier.padding(bottom = 13.dp)) {
                    Text("${i + 1}.", color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp))
                    Text(point, fontSize = 15.sp, lineHeight = 23.sp)
                }
            }
        }

        if (note.quotes.isNotEmpty()) {
            SectionLabel("LINES WORTH KEEPING")
            note.quotes.forEach { quote ->
                Row(Modifier.padding(bottom = 16.dp)) {
                    Box(Modifier.width(3.dp).height(52.dp).background(Gold))
                    Text(
                        "“$quote”", fontFamily = BookSerif, fontSize = 19.sp,
                        lineHeight = 27.sp, modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }

        if (note.takeaway.isNotBlank()) {
            SectionLabel("CARRY THIS INTO YOUR WEEK")
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SavedNoteSheet(note: SermonNote, onClose: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    androidx.compose.material3.ModalBottomSheet(onDismissRequest = onClose, containerColor = Canvas) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp).padding(bottom = 40.dp),
        ) {
            Text(note.title, fontFamily = BookSerif, fontSize = 28.sp, lineHeight = 33.sp)
            Text(
                "${dayLabel(note.createdAt)} · ${note.minutes} min",
                color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp),
            )
            if (note.scriptures.isNotEmpty()) { SectionLabel("SCRIPTURES"); FlowChips(note.scriptures) }
            if (note.points.isNotEmpty()) {
                SectionLabel("MAIN POINTS")
                note.points.forEachIndexed { i, p ->
                    Row(Modifier.padding(bottom = 11.dp)) {
                        Text("${i + 1}.", color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 11.dp))
                        Text(p, fontSize = 14.sp, lineHeight = 21.sp)
                    }
                }
            }
            if (note.quotes.isNotEmpty()) {
                SectionLabel("LINES WORTH KEEPING")
                note.quotes.forEach { Text("“$it”", fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, modifier = Modifier.padding(bottom = 11.dp)) }
            }
            if (note.takeaway.isNotBlank()) {
                SectionLabel("CARRY THIS")
                Surface(shape = RoundedCornerShape(16.dp), color = Night, modifier = Modifier.fillMaxWidth()) {
                    Text(note.takeaway, color = Ivory, fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, modifier = Modifier.padding(18.dp))
                }
            }
            Button(
                onClick = { com.prayerkey.manna.share.MannaPack.share(context, note) },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Icon(Icons.Outlined.Share, "Share a weekly MANNA Pack")
                Spacer(Modifier.width(8.dp))
                Text("Share MANNA Pack")
            }
            Text(
                "Includes Scripture, discussion, prayer, and one action. Your private Journey is never included.",
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
            Text("What language is the service in?", fontFamily = BookSerif, fontSize = 24.sp)
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
                                    Text("FULL NOTES", color = Gold, fontSize = 10.5.sp)
                                } else {
                                    Text("BASIC NOTES", color = Muted, fontSize = 10.5.sp)
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
