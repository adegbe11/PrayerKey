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
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.VolunteerActivism
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
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.prayerkey.manna.ui.theme.BookSerif


// markdown the toolbar inserts at the caret
private val MARKS = listOf(
    "H" to "\n## ",
    "B" to "**bold**",
    "I" to "*italic*",
    "•" to "\n- ",
    "☐" to "\n- [ ] ",
    "❝" to "\n> ",
)

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
    // the sheet follows the theme too, or a dark journal hands you a white
    // card the moment you decide to write
    val cs = MaterialTheme.colorScheme
    val ink = cs.onBackground
    val muted = cs.onSurfaceVariant
    val accent = cs.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = cs.background,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 34.dp)) {
            Text("What do you want to write about?", color = ink, fontFamily = BookSerif, fontSize = 23.sp)

            Text("TEMPLATES", color = muted, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 15.dp))
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
                        .clip(R.card).background(cs.surface)
                        .clickable { onPick(prompt) },
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(prompt.kicker, color = accent, fontSize = 9.5.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
                            Text(prompt.text, color = ink, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 6.dp))
                        }
                        Icon(Icons.Outlined.KeyboardArrowRight, null, tint = muted)
                    }
                }
            }

            // the escape hatch, for people who already know what to say
            Surface(
                onClick = { onPick(null) },
                shape = R.card, color = Color.Transparent,
                border = BorderStroke(1.dp, cs.outlineVariant),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                Text(
                    "Start from a blank page",
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = accent,
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
    /**
     * What the sheet was opened to do: "media", "voice" or "verse".
     *
     * The journal home offers Photo, Voice and Bible as ways in. Without this
     * they would all land on the same blank page with the tools sitting in a
     * bar at the bottom, and three buttons that do one thing are three lies.
     * The sheet performs the action it was asked for, once, as it opens.
     */
    openWith: String? = null,
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
    var entryAt by remember { mutableLongStateOf(initialEntryAt) }
    var attachToday by remember { mutableStateOf(false) }
    var attachedRef by remember { mutableStateOf(prompt?.verseRef) }
    var attachedText by remember { mutableStateOf(prompt?.verseText) }
    var voiceDisclosure by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(editing) }
    var moodOpen by remember { mutableStateOf(false) }
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
    androidx.compose.runtime.LaunchedEffect(openWith) {
        when (openWith) {
            "media" -> mediaPicker.launch(arrayOf("image/*", "video/*", "audio/*", "application/pdf"))
            "voice" -> voiceDisclosure = true
            /* Details too, or the tick lands inside a panel that is closed by
               default on a new entry - so Bible would set something true and
               show the user a blank page, which is indistinguishable from a
               button that does nothing. */
            "verse" -> { attachToday = true; showDetails = true }
        }
    }

    val voiceInput = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { spoken ->
            body = if (body.isBlank()) spoken else "$body\n$spoken"
        }
    }

    val heavy = mood in HEAVY
    val cs = MaterialTheme.colorScheme

    /* The editor wears the chosen theme now. It used to be a light bottom
       sheet, so picking a dark theme and then tapping write dropped you onto
       a cream card — the screen you spend the most time in was the one that
       ignored the choice. It is full height for the same reason a diary page
       is: writing should not happen in a drawer. */
    val paper by animateColorAsState(
        // a faint cool cast when the mood is heavy; it reads on light and
        // dark themes alike, which a fixed cream-to-blue swap did not
        if (heavy) lerp(cs.background, Color(0xFF6F86C4), .07f) else cs.background,
        label = "write-canvas",
    )
    val ink = cs.onBackground
    val muted = cs.onSurfaceVariant
    val accent = cs.primary
    val panel = cs.surface
    val line = cs.outlineVariant

    // scripture the person typed, recognised on device — the same parser the
    // sermon listener uses, so it costs nothing and works offline
    val detected = remember(body) {
        ReferenceDetector.scan(body).map { it.reference }.distinct().take(3)
            .filter { it != attachedRef }
    }

    val entryDate = remember(entryAt) {
        java.time.Instant.ofEpochMilli(entryAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }
    val openDatePicker = {
        DatePickerDialog(context, { _, year, month, day ->
            val old = java.time.Instant.ofEpochMilli(entryAt).atZone(java.time.ZoneId.systemDefault())
            entryAt = old.withYear(year).withMonth(month + 1).withDayOfMonth(day).toInstant().toEpochMilli()
        }, entryDate.year, entryDate.monthValue - 1, entryDate.dayOfMonth).show()
    }

    // borderless fields — a boxed input inside a page reads as a form
    val bare = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = accent,
        focusedTextColor = ink,
        unfocusedTextColor = ink,
        // derived from the ink, so it is legible against any theme's paper
        focusedPlaceholderColor = ink.copy(alpha = .42f),
        unfocusedPlaceholderColor = ink.copy(alpha = .42f),
    )

    fun submit() = onSubmit(
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

    /* A real page, not a panel. As plain content it sat inside the tab and
       left the bottom dock showing underneath it, so you were still looking
       at the app while writing. A full-bleed dialog covers the chrome and
       gives Back the meaning it should have here: close the page. */
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
    Box(Modifier.fillMaxSize().background(paper)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.ArrowBack, "Close editor", tint = ink)
                }
                Text(
                    if (editing) "Edit entry" else "New journal entry",
                    Modifier.weight(1f),
                    color = ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                )
                onDelete?.let {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Outlined.Delete, "Delete entry", tint = muted)
                    }
                }
                IconButton(onClick = { showDetails = !showDetails }) {
                    Icon(Icons.Outlined.Tune, "Entry details", tint = if (showDetails) accent else muted)
                }
                /* Done, in the accent, as text. A filled grey pill reading
                   SAVE was the heaviest object on an empty page and shouted at
                   somebody who had not written a word yet. */
                val ready = body.isNotBlank()
                Text(
                    "Done",
                    color = if (ready) accent else muted.copy(alpha = .5f),
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = ready) { submit() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            /* The date is the page's masthead, the way a diary opens with the
               day rather than a field label. Tapping it moves the entry. */
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 2.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.clickable { openDatePicker() },
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        "%02d".format(entryDate.dayOfMonth),
                        color = ink, fontSize = 34.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                    )
                    Text(
                        "  " + entryDate.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()) +
                            " " + entryDate.year,
                        color = muted, fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    Icon(
                        Icons.Outlined.CalendarMonth, null, tint = muted.copy(alpha = .7f),
                        modifier = Modifier.padding(start = 6.dp, bottom = 6.dp).size(15.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .background(accent.copy(alpha = .15f))
                        .clickable { moodOpen = !moodOpen },
                    contentAlignment = Alignment.Center,
                ) { Text(mood, fontSize = 21.sp) }
            }

            AnimatedVisibility(moodOpen) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MOOD_LIST.forEach { (emoji, _) ->
                        Box(
                            Modifier.size(42.dp).clip(CircleShape)
                                .background(if (mood == emoji) accent.copy(alpha = .18f) else Color.Transparent)
                                .clickable { mood = emoji; moodOpen = false },
                            contentAlignment = Alignment.Center,
                        ) { Text(emoji, fontSize = 20.sp) }
                    }
                }
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            ) {
                prompt?.let {
                    Text(
                        it.text, color = accent, fontSize = 12.5.sp, lineHeight = 19.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                    )
                }

                TextField(
                    title, { title = it },
                    placeholder = { Text("Title", fontFamily = BookSerif, fontSize = 23.sp) },
                    singleLine = true, colors = bare,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = BookSerif, fontSize = 23.sp, color = ink,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    body, { body = it },
                    placeholder = { Text("Write more here…", fontSize = 15.5.sp) },
                    colors = bare, minLines = 8,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.5.sp, lineHeight = 26.sp, color = ink,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                attachedRef?.let { ref ->
                    Surface(
                        shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = .13f),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(ref, color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text("Remove", color = muted, fontSize = 11.sp, modifier = Modifier.clickable { attachedRef = null; attachedText = null })
                        }
                    }
                }

                AnimatedVisibility(detected.isNotEmpty()) {
                    Column(Modifier.padding(top = 10.dp)) {
                        Text("SCRIPTURE YOU MENTIONED", color = muted, fontSize = 9.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                        Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            detected.forEach { ref ->
                                Surface(
                                    onClick = { attachedRef = ref; attachedText = null },
                                    shape = RoundedCornerShape(99.dp),
                                    color = accent.copy(alpha = .13f),
                                    border = BorderStroke(1.dp, accent.copy(alpha = .3f)),
                                ) {
                                    Text(
                                        "+ " + ref, color = accent, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                if (media.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        media.forEachIndexed { index, _ ->
                            Surface(shape = RoundedCornerShape(10.dp), color = panel, border = BorderStroke(1.dp, line)) {
                                Row(Modifier.padding(start = 10.dp, top = 7.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.InsertDriveFile, null, tint = accent, modifier = Modifier.size(16.dp))
                                    Text(" " + (index + 1), color = ink, fontSize = 11.sp)
                                    IconButton(onClick = { media = media.filterIndexed { i, _ -> i != index } }, modifier = Modifier.size(30.dp)) {
                                        Icon(Icons.Outlined.Close, "Remove attachment", tint = muted, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(showDetails) {
                    Column {
                        HorizontalDivider(Modifier.padding(top = 16.dp, bottom = 12.dp), color = line)

                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            listOf("My Journey", "Prayer", "Gratitude", "Family", "Church").forEach { name ->
                                FilterChip(selected = journal == name, onClick = { journal = name }, label = { Text(name, fontSize = 11.sp) })
                            }
                        }
                        OutlinedTextField(gratitude, { gratitude = it }, label = { Text("Grateful for") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), shape = R.control)
                        OutlinedTextField(tags, { tags = it }, label = { Text("Tags") }, placeholder = { Text("faith, family, healing") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = R.control)
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(location, { location = it }, label = { Text("Place") }, leadingIcon = { Icon(Icons.Outlined.Place, null) }, singleLine = true, modifier = Modifier.weight(1f), shape = R.control)
                            OutlinedTextField(weather, { weather = it }, label = { Text("Weather") }, leadingIcon = { Icon(Icons.Outlined.WbSunny, null) }, singleLine = true, modifier = Modifier.weight(1f), shape = R.control)
                        }

                        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Favourite", color = ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { favorite = !favorite }) {
                                Icon(if (favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder, if (favorite) "Remove favourite" else "Add favourite", tint = if (favorite) accent else muted)
                            }
                        }
                        if (!editing && attachedRef == null) {
                            Row(
                                Modifier.fillMaxWidth().clickable { attachToday = !attachToday },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(attachToday, { attachToday = it })
                                Text("Attach today's word (" + todayCard.reference + ")", color = ink, fontSize = 12.5.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
            }

            /*
             * The page, then one quiet row of tools above the keyboard.
             *
             * It used to be an eight-tile grid, an "Add to prayer list" row,
             * a full-width Save button and a bar of formatting marks - four
             * layers of controls on a page whose only job is to hold words.
             * Apple's journal composer is the date, the text, and one slim
             * row of glyphs, and it is right: the tools should be reachable
             * without ever being the first thing you see.
             *
             * So the grid folds into five glyphs. Formatting hides behind Aa
             * until it is wanted. Place, mood, tags and journal already live
             * in the details sheet behind the slider icon at the top. The
             * prayer flag becomes a glyph that fills when it is on.
             */
            var marks by remember { mutableStateOf(false) }
            Column(Modifier.fillMaxWidth().background(cs.background).navigationBarsPadding()) {
                AnimatedVisibility(marks) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        MARKS.forEach { (label, token) ->
                            Box(
                                Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                                    .clickable { body += token },
                                contentAlignment = Alignment.Center,
                            ) { Text(label, color = ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToolGlyph(Icons.Outlined.Image, "Add photo", muted) {
                        mediaPicker.launch(arrayOf("image/*", "video/*"))
                    }
                    ToolGlyph(Icons.Outlined.Mic, "Dictate", muted) { voiceDisclosure = true }
                    ToolGlyph(Icons.Outlined.MenuBook, "Attach today's verse", if (attachToday) accent else muted) {
                        attachToday = !attachToday
                    }
                    ToolGlyph(Icons.Outlined.VolunteerActivism, if (isPrayer) "On prayer list" else "Add to prayer list", if (isPrayer) accent else muted) {
                        isPrayer = !isPrayer
                    }
                    ToolGlyph(Icons.Outlined.AttachFile, "Attach a file", muted) {
                        mediaPicker.launch(arrayOf("*/*"))
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Aa",
                        color = if (marks) accent else muted,
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            .clickable { marks = !marks }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
    }
    if (voiceDisclosure) AlertDialog(
        onDismissRequest = { voiceDisclosure = false },
        title = { Text("Use voice journaling?") },
        text = { Text("Your phone's speech-recognition service may process audio online. PrayerKey does not receive or store the recording. Only the returned words are placed in this entry.") },
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

@Composable
private fun ToolGlyph(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, label, tint = tint, modifier = Modifier.size(21.dp)) }
}
