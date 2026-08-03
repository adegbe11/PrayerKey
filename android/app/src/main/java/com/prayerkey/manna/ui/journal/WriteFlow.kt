package com.prayerkey.manna.ui.journal

import android.content.Intent
import android.app.DatePickerDialog
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.ui.church.ReferenceDetector
import com.prayerkey.manna.ui.theme.premiumCard
import com.prayerkey.manna.ui.theme.PaperFill
import com.prayerkey.manna.ui.theme.topHighlight
import com.prayerkey.manna.ui.theme.R
import com.prayerkey.manna.ui.theme.InkSoft
import com.prayerkey.manna.ui.theme.Electric
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Hairline
import com.prayerkey.manna.ui.theme.Ink
import com.prayerkey.manna.ui.theme.Muted

/** Mood vocabulary, shared by the sheet and the feed cards. */
val MOOD_LIST = listOf(
    "🙏" to "Praying", "😊" to "Grateful", "✨" to "Hopeful",
    "🔥" to "Faith", "😔" to "Heavy", "😭" to "Broken",
)

/** Moods that mean "this is hard" get a calmer canvas and a body cue. */
private val HEAVY = setOf("😔", "😭", "🙏")

data class WriteResult(
    val mood: String,
    val body: String,
    val gratitude: String,
    val verseRef: String?,
    val verseText: String?,
    val source: String,
    val isPrayer: Boolean,
    val title: String = "",
    val tags: List<String> = emptyList(),
    val journal: String = "My Journey",
    val favorite: Boolean = false,
    val location: String = "",
    val weather: String = "",
    val media: List<String> = emptyList(),
    val entryAt: Long = System.currentTimeMillis(),
)

/**
 * The intercept sheet. Tapping Write never lands on a blank page — it lands
 * on things this person already did, turned into questions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionSheet(
    prompts: List<JournalSuggestions.Prompt>,
    onPick: (JournalSuggestions.Prompt?) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 34.dp)) {
            Text("What do you want to write about?", fontFamily = FontFamily.Serif, fontSize = 23.sp)

            Text("TEMPLATES", color = Muted, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 15.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(
                    JournalSuggestions.Prompt("DAILY", "Reflect on the shape of today", "template", starter = "## Today\n\n## What I noticed\n\n## What I want to carry forward\n"),
                    JournalSuggestions.Prompt("GRATITUDE", "Name the gifts hidden in this day", "template", starter = "## Three gifts\n\n1. \n2. \n3. \n\n## Why they mattered\n"),
                    JournalSuggestions.Prompt("PRAYER", "Turn this page into an honest prayer", "template", suggestPrayer = true, starter = "## Praise\n\n## Confession\n\n## Request\n\n## Surrender\n"),
                    JournalSuggestions.Prompt("SERMON", "Capture Scripture, truth, and response", "template", starter = "## Scripture\n\n## What I heard\n\n## One response\n- [ ] \n"),
                ).forEach { template -> FilterChip(selected = false, onClick = { onPick(template) }, label = { Text(template.kicker, fontSize = 10.sp) }) }
            }

            prompts.forEach { prompt ->
                Box(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        .premiumCard(fill = PaperFill, lift = false)
                        .clickable { onPick(prompt) },
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(prompt.kicker, color = Gold, fontSize = 9.5.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
                            Text(prompt.text, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 6.dp))
                        }
                        Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Muted)
                    }
                }
            }

            // the escape hatch, for people who already know what to say
            Surface(
                onClick = { onPick(null) },
                shape = R.card, color = Color.White,
                border = BorderStroke(1.dp, Hairline),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text(
                    "Start from a blank page",
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Electric,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

/**
 * The editor. Three things earn their place here beyond a text box:
 * the canvas calms when the mood is heavy, references the person types are
 * recognised on device and offered as attachments, and an entry can be
 * marked a prayer so it can later be marked answered.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteSheet(
    prompt: JournalSuggestions.Prompt?,
    todayCard: VerseCard,
    initialMood: String = MOOD_LIST.first().first,
    initialBody: String = "",
    initialGratitude: String = "",
    initialIsPrayer: Boolean = false,
    initialTitle: String = "",
    initialTags: List<String> = emptyList(),
    initialJournal: String = "My Journey",
    initialFavorite: Boolean = false,
    initialLocation: String = "",
    initialWeather: String = "",
    initialMedia: List<String> = emptyList(),
    initialEntryAt: Long = System.currentTimeMillis(),
    editing: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (WriteResult) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var mood by remember { mutableStateOf(initialMood) }
    var body by remember { mutableStateOf(initialBody.ifBlank { prompt?.starter.orEmpty() }) }
    var gratitude by remember { mutableStateOf(initialGratitude) }
    var isPrayer by remember { mutableStateOf(initialIsPrayer || prompt?.suggestPrayer == true) }
    var title by remember { mutableStateOf(initialTitle) }
    var tags by remember { mutableStateOf(initialTags.joinToString(", ")) }
    var journal by remember { mutableStateOf(initialJournal) }
    var favorite by remember { mutableStateOf(initialFavorite) }
    var location by remember { mutableStateOf(initialLocation) }
    var weather by remember { mutableStateOf(initialWeather) }
    var media by remember { mutableStateOf(initialMedia) }
    var entryAt by remember { mutableStateOf(initialEntryAt) }
    var attachToday by remember { mutableStateOf(false) }
    var attachedRef by remember { mutableStateOf(prompt?.verseRef) }
    var attachedText by remember { mutableStateOf(prompt?.verseText) }
    var voiceDisclosure by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(editing) }
    val context = LocalContext.current
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingUri by remember { mutableStateOf<String?>(null) }
    fun startAudioRecording() {
        val dir = File(context.filesDir, "journal_audio").apply { mkdirs() }
        val file = File(dir, "reflection-${System.currentTimeMillis()}.m4a")
        val next = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        runCatching {
            next.setAudioSource(MediaRecorder.AudioSource.MIC)
            next.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            next.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            next.setAudioEncodingBitRate(96_000)
            next.setAudioSamplingRate(44_100)
            next.setOutputFile(file.absolutePath)
            next.prepare(); next.start()
            recorder = next
            recordingUri = FileProvider.getUriForFile(context, "${context.packageName}.files", file).toString()
        }.onFailure { next.release(); file.delete() }
    }
    val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) startAudioRecording() }
    DisposableEffect(Unit) { onDispose { runCatching { recorder?.stop() }; recorder?.release() } }
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri -> runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
        media = (media + uris.map { it.toString() }).distinct().take(30)
    }
    val voiceInput = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { spoken ->
            body = if (body.isBlank()) spoken else "$body\n$spoken"
        }
    }

    val heavy = mood in HEAVY
    val canvas by animateColorAsState(
        if (heavy) Color(0xFFF4F6FB) else Color(0xFFFFFCF4),
        label = "write-canvas",
    )

    // scripture the person typed, recognised on device — the same parser the
    // sermon listener uses, so it costs nothing and works offline
    val detected = remember(body) {
        ReferenceDetector.scan(body).map { it.reference }.distinct().take(3)
            .filter { it != attachedRef }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = canvas,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp).padding(bottom = 30.dp),
        ) {
            Text(
                if (editing) "Edit entry" else "Today's entry",
                fontFamily = FontFamily.Serif, fontSize = 24.sp,
            )
            prompt?.let {
                Text(
                    it.text, color = Muted, fontSize = 13.sp, lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            OutlinedTextField(
                title, { title = it }, placeholder = { Text("Title (optional)", fontFamily = FontFamily.Serif) },
                singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = R.control,
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, color = Ink),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold.copy(alpha = .5f), unfocusedBorderColor = Hairline, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            )
            AnimatedVisibility(showDetails) { Column {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("My Journey", "Prayer", "Gratitude", "Family", "Church").forEach { name ->
                    FilterChip(selected = journal == name, onClick = { journal = name }, label = { Text(name, fontSize = 11.sp) })
                }
            }
            OutlinedTextField(journal, { journal = it }, label = { Text("Journal collection") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = R.control)

            Text("HOW IS YOUR HEART?", color = Muted, fontSize = 9.5.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MOOD_LIST.forEach { (emoji, _) ->
                    Box(
                        Modifier.size(40.dp).clip(CircleShape)
                            .background(if (mood == emoji) Color(0xFFE7EDFF) else Color.Transparent)
                            .clickable { mood = emoji },
                        contentAlignment = Alignment.Center,
                    ) { Text(emoji, fontSize = 20.sp) }
                }
            }

            // somatic cue — journaling a hard thing tightens the body
            AnimatedVisibility(heavy) {
                Surface(
                    shape = RoundedCornerShape(12.dp), color = Color(0xFFE8EDF7),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                }
            }
            } }

            OutlinedTextField(
                body, { body = it },
                placeholder = { Text("What's on your heart today?", fontFamily = FontFamily.Serif, fontSize = 15.sp) },
                minLines = 6,
                trailingIcon = {
                    IconButton(onClick = { voiceDisclosure = true }) {
                        Icon(Icons.Outlined.Mic, "Dictate using the device speech service")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                shape = R.card,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 23.sp, color = InkSoft),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold.copy(alpha = .5f),
                    unfocusedBorderColor = Hairline,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(onClick = { showDetails = !showDetails }, shape = RoundedCornerShape(9.dp), color = if (showDetails) Gold.copy(alpha = .12f) else Color.White, border = BorderStroke(1.dp, if (showDetails) Gold.copy(alpha = .3f) else Hairline)) {
                    Row(Modifier.padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Tune, "Entry details", tint = if (showDetails) Gold else Ink, modifier = Modifier.size(16.dp)); Text(" Details", color = if (showDetails) Gold else Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
                listOf("H" to "\n## ", "B" to "**bold**", "•" to "\n- ", "☐" to "\n- [ ] ", "❝" to "\n> ").forEach { (label, token) ->
                    Surface(onClick = { body += token }, shape = RoundedCornerShape(9.dp), color = Color.White, border = BorderStroke(1.dp, Hairline)) {
                        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp))
                    }
                }
                Surface(onClick = { mediaPicker.launch(arrayOf("image/*", "video/*", "audio/*", "application/pdf")) }, shape = RoundedCornerShape(9.dp), color = Gold.copy(alpha = .1f), border = BorderStroke(1.dp, Gold.copy(alpha = .25f))) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.AttachFile, null, tint = Gold, modifier = Modifier.size(16.dp)); Text(" Media", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(onClick = {
                    if (recorder != null) {
                        runCatching { recorder?.stop() }; recorder?.release(); recorder = null
                        recordingUri?.let { media = (media + it).distinct() }; recordingUri = null
                    } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startAudioRecording()
                    else audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                }, shape = RoundedCornerShape(9.dp), color = if (recorder != null) Color(0xFFFFE9E5) else Color.White, border = BorderStroke(1.dp, if (recorder != null) Color(0xFFC94B36) else Hairline)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (recorder != null) Icons.Outlined.StopCircle else Icons.Outlined.Mic, if (recorder != null) "Stop audio recording" else "Record audio", tint = if (recorder != null) Color(0xFFC94B36) else Ink, modifier = Modifier.size(16.dp)); Text(if (recorder != null) " Stop" else " Record", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
            if (media.isNotEmpty()) {
                Text("${media.size} ${if (media.size == 1) "attachment" else "attachments"} · photos, video, audio and PDFs", color = Muted, fontSize = 10.5.sp, modifier = Modifier.padding(top = 7.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    media.forEachIndexed { index, _ ->
                        Surface(shape = RoundedCornerShape(10.dp), color = Color.White, border = BorderStroke(1.dp, Hairline)) {
                            Row(Modifier.padding(start = 10.dp, top = 7.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.InsertDriveFile, null, tint = Gold, modifier = Modifier.size(16.dp)); Text(" ${index + 1}", fontSize = 11.sp)
                                IconButton(onClick = { media = media.filterIndexed { i, _ -> i != index } }, modifier = Modifier.size(30.dp)) { Icon(Icons.Outlined.Close, "Remove attachment", modifier = Modifier.size(15.dp)) }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(detected.isNotEmpty()) {
                Column(Modifier.padding(top = 10.dp)) {
                    Text("SCRIPTURE YOU MENTIONED", color = Muted, fontSize = 9.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                    Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        detected.forEach { ref ->
                            Surface(
                                onClick = { attachedRef = ref; attachedText = null },
                                shape = RoundedCornerShape(99.dp),
                                color = Gold.copy(alpha = .13f),
                                border = BorderStroke(1.dp, Gold.copy(alpha = .3f)),
                            ) {
                                Text(
                                    "+ $ref", color = Gold, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
    )
                        }
                    }
                }
            }
            }

            attachedRef?.let { ref ->
                Surface(
                    shape = RoundedCornerShape(12.dp), color = Color(0xFFF4EDDC),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                ) {
                    Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("📜 $ref", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("Remove", color = Muted, fontSize = 11.sp, modifier = Modifier.clickable { attachedRef = null; attachedText = null })
                    }
                }
            }

            AnimatedVisibility(showDetails) { Column {
            OutlinedTextField(
                gratitude, { gratitude = it },
                placeholder = { Text("One thing you're grateful for…", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                shape = R.control,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold.copy(alpha = .5f),
                    unfocusedBorderColor = Hairline,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )

            HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Hairline)
            Text("DETAILS", color = Muted, fontSize = 9.5.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(tags, { tags = it }, label = { Text("Tags") }, placeholder = { Text("faith, family, healing") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = R.control)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(location, { location = it }, label = { Text("Place") }, leadingIcon = { Icon(Icons.Outlined.Place, null) }, singleLine = true, modifier = Modifier.weight(1f), shape = R.control)
                OutlinedTextField(weather, { weather = it }, label = { Text("Weather") }, leadingIcon = { Icon(Icons.Outlined.WbSunny, null) }, singleLine = true, modifier = Modifier.weight(1f), shape = R.control)
            }
            val entryDate = remember(entryAt) { java.time.Instant.ofEpochMilli(entryAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(onClick = {
                    DatePickerDialog(context, { _, year, month, day ->
                        val old = java.time.Instant.ofEpochMilli(entryAt).atZone(java.time.ZoneId.systemDefault())
                        entryAt = old.withYear(year).withMonth(month + 1).withDayOfMonth(day).toInstant().toEpochMilli()
                    }, entryDate.year, entryDate.monthValue - 1, entryDate.dayOfMonth).show()
                }, shape = R.control, color = Color.White, border = BorderStroke(1.dp, Hairline), modifier = Modifier.weight(1f)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.CalendarMonth, null, tint = Gold); Text("  $entryDate", fontSize = 12.sp) }
                }
                IconButton(onClick = { favorite = !favorite }) { Icon(if (favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder, if (favorite) "Remove favorite" else "Add favorite", tint = if (favorite) Gold else Muted) }
            }

            /* The lifecycle toggle. Marking this a prayer is what makes an
               "answered" badge possible months from now. */
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("This is a prayer request", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("So you can mark it answered later", color = Muted, fontSize = 11.sp)
                }
                Switch(isPrayer, { isPrayer = it })
            }

            if (!editing && attachedRef == null) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp).clickable { attachToday = !attachToday },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(attachToday, { attachToday = it })
                    Text("Attach today's word (${todayCard.reference})", fontSize = 12.5.sp)
                }
            }
            } }

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                onDelete?.let {
                    TextButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Outlined.Delete, null, tint = Color(0xFFB3402A), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("Delete", color = Color(0xFFB3402A))
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("Cancel", color = Muted) }
                Spacer(Modifier.width(4.dp))
                val ready = body.isNotBlank()
                Box(
                    Modifier.height(48.dp).clip(R.control)
                        .background(
                            if (ready) androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF4E70FF), Electric))
                            else androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFFDCDCE2), Color(0xFFD1D1D8))),
                        )
                        .topHighlight(R.control, strength = if (ready) .4f else .2f)
                        .clickable(enabled = ready) {
                            onSubmit(
                                WriteResult(
                                    mood = mood,
                                    body = body,
                                    gratitude = gratitude,
                                    verseRef = attachedRef ?: todayCard.reference.takeIf { attachToday },
                                    verseText = attachedText ?: todayCard.verse.takeIf { attachToday },
                                    source = prompt?.source ?: "write",
                                    isPrayer = isPrayer,
                                    title = title,
                                    tags = tags.split(',').map(String::trim).filter(String::isNotBlank).distinct(),
                                    journal = journal,
                                    favorite = favorite,
                                    location = location,
                                    weather = weather,
                                    media = media,
                                    entryAt = entryAt,
                                ),
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Save entry", color = Color.White, fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, modifier = Modifier.padding(horizontal = 22.dp),
                    )
                }
            }
        }
    }
    if (voiceDisclosure) AlertDialog(
        onDismissRequest = { voiceDisclosure = false },
        title = { Text("Use voice journaling?") },
        text = { Text("Your phone's speech-recognition service may process audio online. MANNA does not receive or store the recording; only the returned words are placed in this private entry.") },
        confirmButton = { TextButton(onClick = {
            voiceDisclosure = false
            voiceInput.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak what is on your heart")
            })
        }) { Text("Start dictation") } },
        dismissButton = { TextButton(onClick = { voiceDisclosure = false }) { Text("Cancel") } },
    )
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Delete this entry?") },
        text = { Text("This permanently removes the entry from this device and your next archive. This cannot be undone.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete?.invoke() }) { Text("Delete", color = Color(0xFFB3402A)) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Keep entry") } },
    )
}
