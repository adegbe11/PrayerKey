package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.prayerkey.manna.data.SavedWord
import com.prayerkey.manna.data.GeneratedPrayer
import com.prayerkey.manna.data.PrayerKeyApi
import com.prayerkey.manna.data.APP_THEMES
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.MemoryVerse
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.model.DailyVerses
import com.prayerkey.manna.ui.theme.*
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.prayerkey.manna.data.DetectedVerse
import com.prayerkey.manna.ui.church.SermonRecognizer
import com.prayerkey.manna.data.UserPrefs
import com.prayerkey.manna.reminder.ReminderReceiver
import com.prayerkey.manna.data.SermonSession
import com.prayerkey.manna.data.JournalPrayer
import com.prayerkey.manna.data.JournalEntry
import com.prayerkey.manna.data.SermonNote
import com.prayerkey.manna.data.RemoteVerse
import com.prayerkey.manna.data.BibleVersion
import com.prayerkey.manna.data.BIBLE_VERSIONS
import com.prayerkey.manna.data.VersionSource
import com.prayerkey.manna.data.versionOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleScreen(
    readerTextSize: Int,
    onReaderTextSize: (Int) -> Unit,
    readerRibbon: String?,
    onReaderRibbon: (String?) -> Unit,
    memory: List<MemoryVerse>,
    saved: List<SavedWord>,
    entries: List<JournalEntry>,
    prayers: List<JournalPrayer>,
    sermons: List<SermonNote>,
    translation: String,
    onTranslation: (String) -> Unit,
    onSave: (VerseCard) -> Unit,
    onMemorize: (VerseCard) -> Unit,
    onReviewMemory: (String, Boolean) -> Unit,
    reduceMotion: Boolean = false,
) {
    var query by remember { mutableStateOf("") }
    /* Never open the Bible onto an empty canvas. OfflineBible has to parse
       the bundled scripture asset on its first query, which can take a few
       seconds on a cold, inexpensive device. A daily verse is already
       available in memory, so paint the first world immediately and replace
       it with search results as soon as the local index is ready. */
    var shown by remember {
        mutableStateOf(
            // Psalm 23 used to be the compulsory first card every time the
            // Bible opened. Start with the broader daily library instead.
            DailyVerses.drop(1).take(8).map { RemoteVerse(it.reference, it.verse, it.translation) },
        )
    }
    var loading by remember { mutableStateOf(false) }
    var pickerOpen by remember { mutableStateOf(false) }
    var showMemory by remember { mutableStateOf(false) }
    var selectedVerse by remember { mutableStateOf<RemoteVerse?>(null) }
    var related by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var chapterVerses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var chapterTitle by remember { mutableStateOf("") }
    val context = LocalContext.current.applicationContext
    val bible = remember(context) { OfflineBible(context) }
    val scope = rememberCoroutineScope()
    val version = versionOf(translation)

    fun runSearch(value: String) {
        scope.launch {
            val term = value.ifBlank { "peace" }
            val kjv = bible.search(term, 10)
            // INSTANT: offline results render immediately in every version —
            // the chosen translation streams in and replaces them when ready
            shown = kjv.map { RemoteVerse(it.reference, it.text, "KJV") }
            if (version.source == VersionSource.OFFLINE || kjv.isEmpty()) return@launch
            loading = true
            val upgraded = when (version.source) {
                VersionSource.FREE -> coroutineScope {
                    kjv.take(8).map { hit ->
                        async {
                            PrayerKeyApi.freeVerse(hit.reference, version.apiId)
                                ?: RemoteVerse(hit.reference, hit.text, "KJV")
                        }
                    }.awaitAll()
                }
                else -> runCatching { PrayerKeyApi.searchBible(term, version.id) }.getOrElse { emptyList() }
            }
            if (upgraded.isNotEmpty()) shown = upgraded
            loading = false
        }
    }
    LaunchedEffect(translation) { runSearch(query) }
    LaunchedEffect(selectedVerse) {
        related = selectedVerse?.let { sel ->
            bible.search(sel.reference, 1).firstOrNull()?.let { bible.related(it) }
        }.orEmpty()
    }

    /* TINDER ANATOMY: the card IS the screen — full-bleed, edge to edge.
       Search + version chips FLOAT on top of it; five action buttons
       float at the bottom. Nothing stacks above the card. */
    val shareContext = LocalContext.current
    var searchOpen by remember { mutableStateOf(false) }

    /* Two ways to be in the Bible, and they are different acts. The deck
       hands you a verse; the book is for sitting down and reading one. */
    var bookMode by remember { mutableStateOf(false) }
    var libraryMode by remember { mutableStateOf(false) }
    androidx.activity.compose.BackHandler(enabled = bookMode || libraryMode) { bookMode = false; libraryMode = false }

    if (bookMode) {
        com.prayerkey.manna.ui.book.BibleBookScreen(
            bible = bible,
            textSize = readerTextSize,
            onTextSize = onReaderTextSize,
            ribbon = readerRibbon,
            onRibbon = onReaderRibbon,
            onReadPlain = { book, chapter ->
                bookMode = false
                scope.launch {
                    chapterVerses = bible.chapter(book, chapter)
                    chapterTitle = "$book $chapter"
                }
            },
        )
        return
    }

    if (libraryMode) {
        com.prayerkey.manna.ui.book.BibleLibraryScreen(
            translation = translation,
            onChooseTranslation = { libraryMode = false; pickerOpen = true },
            onPull = { libraryMode = false },
            onFlip = { libraryMode = false; bookMode = true },
            onOpenChapter = { book, chapter ->
                libraryMode = false
                scope.launch {
                    chapterVerses = bible.chapter(book, chapter)
                    chapterTitle = "$book $chapter"
                }
            },
        )
        return
    }

    Box(Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.background)) {
        if (showMemory) {
            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp).padding(top = 60.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Memorize", fontFamily = BookSerif, fontSize = 24.sp, modifier = Modifier.weight(1f))
                    FloatChip(onClick = { showMemory = false }) {
                        Icon(Icons.Outlined.Close, "Back to verses", tint = Ivory, modifier = Modifier.size(19.dp))
                    }
                }
                MemoryTrainer(memory, saved, entries, prayers, sermons, onReviewMemory)
            }
        } else {
            com.prayerkey.manna.ui.components.VersePullDeck(
                verses = shown,
                reduceMotion = reduceMotion,
                topOverlay = {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FloatChip(onClick = { searchOpen = !searchOpen }) {
                            Icon(Icons.Outlined.Search, "Search", tint = Ivory, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        FloatChip(onClick = { bookMode = true }) {
                            Icon(Icons.Outlined.MenuBook, "Read the Bible as a book", tint = Ivory, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        FloatChip(onClick = { libraryMode = true }) {
                            Icon(Icons.Outlined.ViewList, "Browse the Bible library", tint = Ivory, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        FloatChip(onClick = { showMemory = true }) {
                            Icon(Icons.Outlined.School, "Memorize", tint = Ivory, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            onClick = { pickerOpen = true }, shape = RoundedCornerShape(99.dp),
                            color = Color.Transparent, shadowElevation = 8.dp,
                            modifier = Modifier.height(54.dp).background(NightGloss, RoundedCornerShape(99.dp)),
                        ) {
                            Row(Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(version.id, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Icon(Icons.Outlined.KeyboardArrowDown, "Change version", tint = Gold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    if (searchOpen) OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 8.dp),
                        placeholder = { Text("Search", fontSize = 14.sp, color = Muted) },
                        singleLine = true, shape = RoundedCornerShape(16.dp), colors = fieldColors(),
                        trailingIcon = {
                            IconButton(onClick = { runSearch(query); searchOpen = false }) {
                                Icon(Icons.Outlined.ArrowForward, "Search")
                            }
                        },
                    )
                    if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 14.dp), color = Gold, trackColor = Hairline)
                },
                onSave = { onSave(VerseCard(it.reference, it.translation, it.text, "")) },
                onMemorize = { onMemorize(VerseCard(it.reference, it.translation, it.text, "")) },
                onShare = { com.prayerkey.manna.share.CardShareRenderer.share(shareContext, VerseCard(it.reference, it.translation, it.text, "")) },
                onOpen = { selectedVerse = it },
            )
        }
    }

    /* ── Version picker — premium bottom sheet ── */
    if (pickerOpen) {
        ModalBottomSheet(onDismissRequest = { pickerOpen = false }, containerColor = Canvas) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 40.dp)) {
                Text("Choose your Bible", fontFamily = BookSerif, fontSize = 28.sp)
                Spacer(Modifier.height(16.dp))
                LazyColumn(Modifier.heightIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(BIBLE_VERSIONS, key = { it.id }) { item ->
                        val active = item.id == translation
                        Surface(
                            onClick = { onTranslation(item.id); pickerOpen = false },
                            shape = RoundedCornerShape(20.dp),
                            color = if (active) Night else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Night else Hairline),
                        ) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.id, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (active) Gold else Ink)
                                        Spacer(Modifier.width(8.dp))
                                        SourcePill(item.source)
                                    }
                                    Text(item.name, color = if (active) Color.White.copy(.7f) else Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                                if (active) Text("✓", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
    selectedVerse?.let { verse ->
        ModalBottomSheet(onDismissRequest = { selectedVerse = null }, containerColor = Canvas) {
            VerseDetail(
                reference = verse.reference, text = verse.text, versionId = verse.translation,
                versionName = versionOf(verse.translation).name,
                related = related,
                onSave = { onSave(VerseCard(verse.reference, verse.translation, verse.text, "")) },
                onMemorize = { onMemorize(VerseCard(verse.reference, verse.translation, verse.text, "")) },
                onRelated = { selectedVerse = RemoteVerse(it.reference, it.text, "KJV") },
                onReadChapter = {
                    /* This used to runBlocking on the main thread, and on a
                       cold cache that call parses the whole 4.4MB KJV — a
                       guaranteed freeze the first time anyone tapped it.
                       Both reads now happen off the main thread. */
                    scope.launch {
                        val source = runCatching { bible.search(verse.reference, 1).firstOrNull() }.getOrNull()
                        if (source != null) {
                            selectedVerse = null
                            chapterTitle = "${source.book} ${source.chapter}"
                            chapterVerses = bible.chapter(source.book, source.chapter)
                        }
                    }
                },
            )
        }
    }
    if (chapterTitle.isNotBlank()) {
        ModalBottomSheet(onDismissRequest = { chapterTitle = ""; chapterVerses = emptyList() }, containerColor = Canvas) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 36.dp)) {
                Text(chapterTitle, fontFamily = BookSerif, fontSize = 30.sp)
                Spacer(Modifier.height(18.dp))
                LazyColumn(Modifier.heightIn(max = 620.dp)) {
                    items(chapterVerses, key = { it.reference }) { item ->
                        Row(Modifier.fillMaxWidth().clickable { chapterTitle = ""; selectedVerse = RemoteVerse(item.reference, item.text, "KJV") }.padding(vertical = 8.dp)) {
                            Text(item.verse.toString(), color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                            Text(item.text, fontFamily = BookSerif, fontSize = 19.sp, lineHeight = 27.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * A chip floating on the card.
 *
 * Glass, not white. The five discs along the bottom of this screen became a
 * translucent dock a while back and these were left as opaque white circles,
 * so the top of the artwork carried the only bright white left in the app —
 * and on a photograph of a desert at dusk it read as a hole punched in the
 * picture.
 */
@Composable
private fun FloatChip(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(54.dp)
            .shadow(14.dp, CircleShape, spotColor = Color.Black.copy(alpha = .28f))
            .clip(CircleShape).background(Brush.verticalGradient(listOf(Color(0xCC285FC2), Color(0xDD071A3D))))
            .border(1.dp, Color.White.copy(alpha = .30f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun SourcePill(source: VersionSource) {
    val (label, bg, fg) = when (source) {
        VersionSource.OFFLINE -> Triple("Offline", Gold.copy(alpha = .18f), Gold)
        VersionSource.FREE -> Triple("Free online", Color(0xFF1E5A38).copy(alpha = .16f), Color(0xFF2E7D4F))
        VersionSource.PREMIUM -> Triple("Premium", Electric.copy(alpha = .13f), Electric)
    }
    Surface(shape = RoundedCornerShape(99.dp), color = bg) {
        Text(label, color = fg, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .4.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun VersionPill(id: String) {
    Surface(shape = RoundedCornerShape(99.dp), color = Gold.copy(alpha = .15f)) {
        Text(id, color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
    }
}

@Composable
private fun VerseDetail(
    reference: String,
    text: String,
    versionId: String,
    versionName: String,
    related: List<BibleVerse>,
    onSave: () -> Unit,
    onMemorize: () -> Unit,
    onRelated: (BibleVerse) -> Unit,
    onReadChapter: () -> Unit,
) {
    var showStudy by remember { mutableStateOf(false) }
    var studyQuestion by remember { mutableStateOf("") }
    var studyAnswer by remember { mutableStateOf<com.prayerkey.manna.data.StudyAnswer?>(null) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
        Text(reference, fontFamily = BookSerif, fontSize = 30.sp)
        Text("$versionName ($versionId)", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
        Text(text, fontFamily = BookSerif, fontSize = 28.sp, lineHeight = 38.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 34.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Button(onClick = onSave, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.BookmarkBorder, null); Text(" Save") }
            OutlinedButton(onClick = onMemorize, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.School, null); Text(" Memorize") }
        }
        TextButton(onClick = onReadChapter, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Icon(Icons.Outlined.MenuBook, null); Text(" Read full chapter offline") }
        TextButton(onClick = { showStudy = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Psychology, "Open transparent study lens")
            Text(" Study this passage safely")
        }
        Text("Related verses", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 28.dp, bottom = 10.dp))
        if (related.isEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth())
        related.forEach { item ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onRelated(item) },
                color = Color.White, shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Hairline),
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(item.reference, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(item.text, maxLines = 2, color = Muted, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
    if (showStudy) AlertDialog(
        onDismissRequest = { showStudy = false },
        title = { Text("Study lens", fontFamily = BookSerif) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                com.prayerkey.manna.data.StudyLens.notes(reference, text).forEach { note ->
                    Text(note.label, color = Gold, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                    Text(note.text, fontSize = 12.5.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
                    Text(note.source, color = Muted, fontSize = 9.5.sp, modifier = Modifier.padding(top = 3.dp))
                }
                HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Hairline)
                Text("ASK ABOUT THIS PASSAGE", color = Gold, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = studyQuestion, onValueChange = { studyQuestion = it }, label = { Text("Your question") }, placeholder = { Text("What does this passage say?") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Button(onClick = { studyAnswer = com.prayerkey.manna.data.StudyLens.answer(reference, text, studyQuestion) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), enabled = studyQuestion.isNotBlank()) { Text("Explore carefully") }
                studyAnswer?.let { answer ->
                    Text(answer.label, color = Gold, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp))
                    Text(answer.text, fontSize = 12.5.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
                    Text(answer.source, color = Muted, fontSize = 9.5.sp, modifier = Modifier.padding(top = 3.dp))
                }
            }
        },
        confirmButton = { TextButton(onClick = { showStudy = false }) { Text("Done") } },
    )
}

@Composable
private fun MemoryTrainer(
    memories: List<MemoryVerse>,
    saved: List<SavedWord>,
    entries: List<JournalEntry>,
    prayers: List<JournalPrayer>,
    sermons: List<SermonNote>,
    onReview: (String, Boolean) -> Unit,
) {
    if (memories.isEmpty()) {
        EmptySaved(false)
        return
    }
    val now = System.currentTimeMillis()
    val memory = memories.firstOrNull { it.nextReviewAt <= now } ?: memories.first()
    val connections = remember(memory, saved, entries, prayers, sermons) {
        com.prayerkey.manna.data.MemoryGraph.connections(memory, saved, entries, prayers, sermons)
    }
    val due = memories.count { it.nextReviewAt <= now }
    val onAdvance: (String) -> Unit = { onReview(it, true) }
    val words = memory.verse.split(" ")
    val masked = words.mapIndexed { index, word ->
        val hideEvery = (6 - memory.stage).coerceAtLeast(1)
        if ((index + 1) % hideEvery == 0) "_".repeat(word.length.coerceAtMost(8)) else word
    }.joinToString(" ")
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Night) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$due due", color = if (due > 0) Gold else Color.White.copy(.55f), fontSize = 11.sp)
                Text("${memories.count { it.stage >= 5 }} rooted · ${memories.size} total", color = Color.White.copy(.55f), fontSize = 11.sp)
            }
            Text("MEMORIZE · LEVEL ${memory.stage}", color = Gold, fontSize = 11.sp, letterSpacing = 1.4.sp)
            Text(masked, color = Color.White, fontFamily = BookSerif, fontSize = 25.sp, lineHeight = 34.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 30.dp))
            Text(memory.reference, color = Color.White.copy(.65f))
            OutlinedButton(
                onClick = { onReview(memory.reference, false) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) { Text("I need more practice") }
            Button(onClick = { onAdvance(memory.reference) }, modifier = Modifier.fillMaxWidth().padding(top = 22.dp)) { Text(if (memory.stage >= 5) "Practice again" else "I recited it — hide more") }
        }
    }
    if (connections.isNotEmpty()) {
        Text("WOVEN THROUGH YOUR JOURNEY", color = Gold, fontSize = 9.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
        connections.forEach { connection ->
            Surface(Modifier.fillMaxWidth().padding(bottom = 7.dp), shape = RoundedCornerShape(16.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)) {
                Column(Modifier.padding(13.dp)) {
                    Text(connection.kind, color = Gold, fontSize = 9.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
                    Text(connection.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
                    Text(connection.detail, color = Muted, fontSize = 11.sp, maxLines = 2, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    journal: List<JournalPrayer>,
    topics: List<PrayerTopic>,
    topicsReady: Boolean,
    onLoadTopics: () -> Unit,
    onSavePrayer: (String, GeneratedPrayer) -> Unit,
) {
    var request by remember { mutableStateOf("") }
    val moods = remember { mutableStateListOf<String>() }
    var generated by remember { mutableStateOf<GeneratedPrayer?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var deckMode by remember { mutableStateOf(false) }
    var selectedTopic by remember { mutableStateOf<PrayerTopic?>(null) }
    var topicQuery by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
    var potdOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val potd = remember { com.prayerkey.manna.model.todaysPrayer() }
    val prayerContext = LocalContext.current
    var prayerSpeechReady by remember { mutableStateOf(false) }
    val prayerSpeaker = remember { TextToSpeech(prayerContext.applicationContext) { prayerSpeechReady = it == TextToSpeech.SUCCESS } }
    LaunchedEffect(prayerSpeechReady) { if (prayerSpeechReady) { prayerSpeaker.language = Locale.getDefault(); prayerSpeaker.setSpeechRate(.86f) } }
    DisposableEffect(prayerSpeaker) { onDispose { prayerSpeaker.stop(); prayerSpeaker.shutdown() } }
    // decks come from the app-wide cache — instant after first load
    LaunchedEffect(Unit) { onLoadTopics() }
    /* Loading is a state, not an inference. Reading it off an empty list
       made "still loading", "the request failed" and "there is nothing here"
       the same thing on screen — an spinner that never stopped. */
    val topicsLoading = deckMode && topics.isEmpty() && !topicsReady

    androidx.activity.compose.BackHandler(enabled = deckMode) { deckMode = false }

    if (deckMode) {
        /* Prayer decks shuffle like Manna: the card is the screen, pull down
           for the next prayer, push up to keep it. 544 rows in a list was a
           directory; this is a deck you can actually browse with a thumb. */
        val filtered = remember(topics, topicQuery) {
            topics.filter {
                topicQuery.isBlank() ||
                    it.title.contains(topicQuery, true) ||
                    it.category.contains(topicQuery, true)
            }
        }
        val deckCs = MaterialTheme.colorScheme
        Box(Modifier.fillMaxSize().background(deckCs.background)) {
            if (topicsLoading) {
                Column(
                    Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = deckCs.primary, strokeWidth = 3.dp, modifier = Modifier.size(42.dp))
                }
            } else if (filtered.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 40.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    /* A search that found nothing and a deck that failed to
                       load are different problems and need different words.
                       Both used to say "No prayer for that yet · Clear
                       search", which is nonsense when the deck is empty. */
                    val searchMiss = topics.isNotEmpty()
                    Text(
                        if (searchMiss) "No prayer for that yet" else "The deck did not load",
                        color = deckCs.onBackground, fontWeight = FontWeight.SemiBold,
                    )
                    Surface(
                        onClick = { if (searchMiss) topicQuery = "" else onLoadTopics() },
                        shape = R.pill, color = deckCs.primary.copy(alpha = .14f),
                        modifier = Modifier.padding(top = 18.dp),
                    ) {
                        Text(
                            if (searchMiss) "Clear search" else "Try again",
                            color = deckCs.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        )
                    }
                }
            } else {
                com.prayerkey.manna.ui.components.PullDeck(
                    items = filtered,
                    pullStamp = "NEXT ✦",
                    keepStamp = "KEPT ♥",
                    onKeep = { topic ->
                        // keeping a prayer files it, with its first verse attached
                        onSavePrayer(
                            topic.title,
                            GeneratedPrayer(
                                id = topic.slug,
                                title = topic.title,
                                prayer = topic.prayer,
                                encouragement = "",
                                verses = topic.scripture,
                            ),
                        )
                    },
                    actions = { topic, controls ->
                        listOf(
                            /* Search lives here rather than on the card. 543
                               prayers are unfindable by shuffling alone, but a
                               field pinned over the art was the clutter. */
                            com.prayerkey.manna.ui.components.DeckAction(
                                Icons.Outlined.Search, "Find a prayer", Ivory.copy(alpha = .85f), 50.dp,
                            ) { searchOpen = true },
                            com.prayerkey.manna.ui.components.DeckAction(
                                Icons.Outlined.Close, "Next prayer", Ivory.copy(alpha = .85f), 58.dp,
                            ) { controls.next() },
                            com.prayerkey.manna.ui.components.DeckAction(
                                Icons.Outlined.AutoAwesome, "Pray it", Gold, 50.dp,
                            ) { selectedTopic = topic },
                            com.prayerkey.manna.ui.components.DeckAction(
                                Icons.Outlined.BookmarkBorder, "Keep", Gold, 58.dp,
                            ) { controls.keep() },
                            /* This used to open the detail sheet, so the
                               one button labelled Share was the one button
                               that did not share. */
                            com.prayerkey.manna.ui.components.DeckAction(
                                Icons.Outlined.Share, "Share", Ivory.copy(alpha = .85f), 50.dp,
                            ) {
                                com.prayerkey.manna.share.CardShareRenderer
                                    .sharePrayer(prayerContext, topic)
                            },
                        )
                    },
                    // no chrome on the deck at all: no back chip, no
                    // counter, no search field. The system back button
                    // leaves the deck, so nothing has to sit on the card.
                ) { topic, front, mod ->
                    PrayerDeckFace(topic, front, onOpen = { selectedTopic = topic }, modifier = mod)
                }
            }
        }
    } else {
        /* ONE scroll for the whole screen.
           Previously the header, mode chips and Prayer of the Day sat in a
           fixed Column above a nested scrolling region. Scrolling slid the
           headline under that hard viewport edge and clipped it mid-glyph,
           which read exactly like two views colliding. A single scroll has
           no interior edge to clip against. */
        val cs = MaterialTheme.colorScheme
        Column(
            Modifier.fillMaxSize()
                // the page follows the chosen theme; it was pinned to a light
                // wash, so eleven of the twelve themes stopped at this screen
                .background(
                    Brush.verticalGradient(
                        0f to androidx.compose.ui.graphics.lerp(cs.background, cs.primary, .10f),
                        .45f to cs.background,
                        1f to cs.background,
                    ),
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 24.dp, bottom = Space.dock),
        ) {
            /* "Pray" — the tab underneath already says "Pray for me", and the
               title said it again, twice on one screen. */
            Text(
                if (generated == null) "Pray" else "Your prayer",
                color = cs.onBackground, fontFamily = DisplaySerif, fontSize = 32.sp, fontWeight = FontWeight.Bold,
            )
            ModeChips(deckMode) { deckMode = it }

            /* ── Prayer of the Day — same daily prayer as prayerkey.com ── */
            Surface(
                onClick = { potdOpen = true },
                shape = R.card, color = Color.Transparent,
                // shadow on the OUTER modifier only — layering it with the
                // gradient background painted a doubled inner edge
                modifier = Modifier.fillMaxWidth().padding(top = Space.block, bottom = Space.block)
                    .shadow(24.dp, R.card, spotColor = Night.copy(alpha = .32f), ambientColor = Night.copy(alpha = .12f))
                    .shadow(3.dp, R.card, spotColor = Night.copy(alpha = .3f), ambientColor = Color.Transparent)
                    .clip(R.card)
                    .background(NightFill)
                    .bloom(Gold, .09f)
                    .goldEdge(R.card),
            ) {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("✦ PRAYER OF THE DAY", color = Gold, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                        Text(potd.title, color = Color.White, fontFamily = BookSerif, fontSize = 19.sp, modifier = Modifier.padding(top = 4.dp))
                        Text(potd.ref, color = Color.White.copy(.6f), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    Text("Read", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Gold)
                }
            }

            if (generated == null) {
                /* ONE headline. The screen title already said "Pray for me";
                   a second centred serif headline repeated it while everything
                   around it was left aligned. */

                /* The field is the hero, so it has to read as a control and
                   not a void. It rests at three lines and grows as you type —
                   the old six-line box was the largest thing on the screen and
                   it was empty. */
                OutlinedTextField(
                    request, { request = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Pour your heart out here...",
                            fontSize = 17.sp, color = cs.onBackground.copy(alpha = .42f),
                            fontFamily = BookSerif,
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 17.sp, lineHeight = 27.sp, color = cs.onBackground,
                        fontFamily = BookSerif,
                    ),
                    minLines = 3,
                    maxLines = 10,
                    shape = R.card,
                    colors = OutlinedTextFieldDefaults.colors(
                        // a tinted pane rather than a filled beige box; the
                        // solid fill read as a web contact form
                        focusedBorderColor = cs.primary.copy(alpha = .55f),
                        unfocusedBorderColor = cs.outlineVariant,
                        focusedContainerColor = cs.surface.copy(alpha = .55f),
                        unfocusedContainerColor = cs.surface.copy(alpha = .35f),
                        focusedTextColor = cs.onBackground,
                        unfocusedTextColor = cs.onBackground,
                    ),
                )

                /* Starter prompts, so an empty screen is never a dead end. A
                   blank input is the same blank-page problem the journal had,
                   and the cure is a way in, not a bigger box. */
                androidx.compose.animation.AnimatedVisibility(request.isBlank()) {
                    Column {
                        Text(
                            "OR START HERE",
                            color = cs.onBackground.copy(alpha = .5f), fontSize = 10.sp, letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = Space.block, bottom = 10.dp),
                        )
                        /* Tiles, not capsules. Six identical white pills read
                           as a multiple-choice question; a tile with its own
                           quiet glow and a mark reads as a door. */
                        STARTERS.chunked(2).forEach { row ->
                            Row(
                                Modifier.fillMaxWidth().padding(bottom = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(9.dp),
                            ) {
                                row.forEach { starter ->
                                    Column(
                                        Modifier.weight(1f)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        starter.tint.copy(alpha = .22f),
                                                        cs.surface.copy(alpha = .45f),
                                                    ),
                                                ),
                                            )
                                            .border(0.8.dp, starter.tint.copy(alpha = .28f), RoundedCornerShape(18.dp))
                                            .clickable { request = starter.body }
                                            .padding(horizontal = 14.dp, vertical = 13.dp),
                                    ) {
                                        Icon(
                                            starter.icon, null, tint = starter.tint,
                                            modifier = Modifier.size(19.dp),
                                        )
                                        Text(
                                            starter.label, color = cs.onBackground, fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium, maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 9.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                /* Moods stay out of the way until there is something to
                   colour. Asking someone to tick "Sick" before they have said
                   a word is a form; offering it after is a nuance. */
                androidx.compose.animation.AnimatedVisibility(request.isNotBlank()) {
                    Column {
                        Text(
                            "HOW ARE YOU FEELING? (OPTIONAL)",
                            color = Muted, fontSize = 10.sp, letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = Space.block, bottom = 10.dp),
                        )
                        Box {
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf("Grateful", "Anxious", "Sad", "Hopeful", "Confused", "Joyful", "Sick", "Tired").forEach { item ->
                                    val on = item in moods
                                    Surface(
                                        onClick = { if (on) moods.remove(item) else moods.add(item) },
                                        shape = R.pill,
                                        color = if (on) ChipFillSelected else ChipFill,
                                    ) {
                                        Text(
                                            item,
                                            color = if (on) Electric else InkSoft,
                                            fontSize = 13.sp,
                                            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 17.dp, vertical = 11.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.width(26.dp))
                            }
                            Box(
                                Modifier.align(Alignment.CenterEnd).width(34.dp).height(46.dp)
                                    .background(edgeFade()),
                            )
                        }
                    }
                }

                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp)) }

                /* The action appears when there is something to act on. A
                   greyed-out slab was dead weight and the least attractive
                   thing on the screen. */
                androidx.compose.animation.AnimatedVisibility(request.isNotBlank() || loading) {
                    com.prayerkey.manna.ui.components.PkButton(
                        label = if (loading) "Preparing your prayer..." else "Pray with me",
                        enabled = !loading,
                        icon = Icons.Outlined.AutoAwesome,
                        modifier = Modifier.fillMaxWidth().padding(top = Space.loose),
                    ) {
                        scope.launch {
                            loading = true; error = null
                            runCatching { PrayerKeyApi.generatePrayer(request, moods.toList()) }
                                .onSuccess { generated = it }
                                .onFailure { error = it.message ?: "Prayer could not be generated" }
                            loading = false
                        }
                    }
                }

                if (journal.isNotEmpty()) {
                    Text(
                        "PRAYERS YOU KEPT",
                        color = Muted, fontSize = 10.sp, letterSpacing = 1.6.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = Space.loose, bottom = 10.dp),
                    )
                    journal.take(2).forEach { entry ->
                        Box(Modifier.fillMaxWidth().padding(bottom = 8.dp).premiumCard(fill = PaperFill, lift = false)) {
                            Column(Modifier.padding(15.dp)) { Text(entry.title, fontFamily = BookSerif, fontSize = 17.sp); Text(entry.scriptureRef.orEmpty(), color = Gold, fontSize = 11.sp) }
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().premiumCard(fill = PaperFill)) {
                    Column(Modifier.padding(24.dp)) {
                        Text(generated!!.title, fontFamily = BookSerif, fontSize = 25.sp)
                        Spacer(Modifier.height(20.dp))
                        Text(generated!!.prayer, lineHeight = 24.sp)
                        generated!!.verses.firstOrNull()?.let { Text(it.first, color = Gold, modifier = Modifier.padding(top = 20.dp)) }
                        if (generated!!.encouragement.isNotBlank()) Text(generated!!.encouragement, color = Muted, modifier = Modifier.padding(top = 14.dp))
                        TextButton(
                            onClick = { generated?.let { prayerSpeaker.speak("${it.title}. ${it.prayer}", TextToSpeech.QUEUE_FLUSH, null, it.id) } },
                            enabled = prayerSpeechReady, modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        ) { Icon(Icons.Outlined.VolumeUp, "Listen to this prayer"); Spacer(Modifier.width(7.dp)); Text("Listen prayerfully") }
                        Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { generated?.let { onSavePrayer(request, it) } }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.BookmarkBorder, null); Text(" Journal") }
                            Button(onClick = { generated = null }, modifier = Modifier.weight(1f)) { Text("Pray again") }
                        }
                    }
                }
            }
        }
    }

    if (potdOpen) {
        val shareContext = LocalContext.current
        ModalBottomSheet(onDismissRequest = { potdOpen = false }, containerColor = Canvas) {
            LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 24.dp), contentPadding = PaddingValues(bottom = 44.dp)) {
                item {
                    Text("✦ PRAYER OF THE DAY ✦", color = Gold, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                    Text(potd.title, fontFamily = BookSerif, fontSize = 28.sp, modifier = Modifier.padding(top = 6.dp))
                    Text(potd.ref, color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 3.dp))
                    Surface(shape = RoundedCornerShape(14.dp), color = Ivory, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                        Text("“${potd.verse}”", fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, modifier = Modifier.padding(16.dp))
                    }
                    potd.prayer.split("\n\n").forEach { para ->
                        Text(para, fontSize = 15.sp, lineHeight = 24.sp, color = Ink, modifier = Modifier.padding(top = 14.dp))
                    }
                    Text("SHARE AS A CARD", color = Muted, fontSize = 11.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 26.dp, bottom = 10.dp))
                }
                items(com.prayerkey.manna.share.CardFormat.entries.toList(), key = { it.name }) { format ->
                    Surface(
                        onClick = { com.prayerkey.manna.share.PrayerCardRenderer.share(shareContext, potd, format) },
                        shape = RoundedCornerShape(16.dp), color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    ) {
                        Row(Modifier.padding(horizontal = 18.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Share, null, tint = Gold, modifier = Modifier.size(18.dp))
                            Text(format.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f).padding(start = 12.dp))
                            Text("${format.w}×${format.h}", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (searchOpen) {
        ModalBottomSheet(onDismissRequest = { searchOpen = false }, containerColor = Canvas) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 30.dp),
            ) {
                OutlinedTextField(
                    topicQuery, { topicQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Healing, family, work, grief…", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Muted) },
                    singleLine = true, shape = R.control, colors = fieldColors(),
                )
                Spacer(Modifier.height(14.dp))
                val hits = topics.filter {
                    topicQuery.isNotBlank() &&
                        (it.title.contains(topicQuery, true) || it.category.contains(topicQuery, true))
                }
                androidx.compose.foundation.lazy.LazyColumn(
                    Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(hits.take(40), key = { it.slug }) { topic ->
                        Surface(
                            onClick = { selectedTopic = topic; searchOpen = false },
                            shape = R.control, color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Hairline),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                                Text(topic.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(topic.category, color = Gold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedTopic?.let { topic ->
        ModalBottomSheet(onDismissRequest = { selectedTopic = null }, containerColor = Canvas) {
            /* This is where the prayer is actually prayed, so it is set like
               a page rather than a data dump: a centred head, the prayer
               broken into breathing paragraphs, then scripture and points as
               their own named sections. */
            Column(
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    topic.category.uppercase(),
                    color = Gold, fontSize = 9.5.sp,
                    letterSpacing = 3.2.sp, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    topic.title,
                    fontFamily = BookSerif, fontSize = 30.sp, lineHeight = 38.sp,
                    letterSpacing = (-0.4).sp, textAlign = TextAlign.Center, color = InkSoft,
                )
                Spacer(Modifier.height(18.dp))
                Box(Modifier.height(1.dp).fillMaxWidth(.20f).background(Gold.copy(alpha = .5f)))
                Spacer(Modifier.height(26.dp))

                /* One wall of text is hard to pray. Split on the sentence
                   breaks the writer already put in and give each movement
                   its own paragraph. */
                prayerParagraphs(topic.prayer).forEachIndexed { i, para ->
                    if (i > 0) Spacer(Modifier.height(16.dp))
                    Text(
                        para,
                        fontSize = 17.sp, lineHeight = 29.sp,
                        color = InkSoft, textAlign = TextAlign.Center,
                    )
                }

                if (topic.scripture.isNotEmpty()) {
                    SheetSection("THE WORD ON IT")
                    topic.scripture.forEach { (ref, text) ->
                        Box(
                            Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                .premiumCard(fill = PaperFill, lift = false),
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    ref, color = Gold, fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp,
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text, fontFamily = BookSerif,
                                    fontSize = 16.sp, lineHeight = 25.sp, color = InkSoft,
                                )
                            }
                        }
                    }
                }

                if (topic.prayerPoints.isNotEmpty()) {
                    SheetSection("PRAY THESE")
                    topic.prayerPoints.forEachIndexed { i, point ->
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                "${i + 1}", color = Gold, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 14.dp, top = 3.dp),
                            )
                            Text(point, fontSize = 16.sp, lineHeight = 26.sp, color = InkSoft)
                        }
                    }
                }

                Spacer(Modifier.height(30.dp))
                com.prayerkey.manna.ui.components.PkButton(
                    label = "Amen",
                    modifier = Modifier.fillMaxWidth(),
                ) { selectedTopic = null }
            }
        }
    }
}

/** A named divider between the movements of the sheet. */
@Composable
private fun SheetSection(label: String) {
    Spacer(Modifier.height(30.dp))
    Text(
        label, color = Muted, fontSize = 9.5.sp,
        letterSpacing = 2.6.sp, fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(14.dp))
}

/**
 * Breaks a prayer into paragraphs of two or three sentences.
 *
 * The source prayers arrive as one block. Praying a wall of text is hard —
 * the eye loses its place and there is nowhere to breathe — so this groups
 * whole sentences without altering a word of them.
 */
private fun prayerParagraphs(prayer: String): List<String> {
    val blank = prayer.split(Regex("\\n\\s*\\n")).map { it.trim() }.filter { it.isNotEmpty() }
    if (blank.size > 1) return blank

    val sentences = Regex("[^.!?]+[.!?]+|[^.!?]+$")
        .findAll(prayer.trim())
        .map { it.value.trim() }
        .filter { it.isNotEmpty() }
        .toList()
    if (sentences.size <= 3) return listOf(prayer.trim())

    return sentences.chunked(3).map { it.joinToString(" ") }
}

private data class Starter(
    val label: String,
    val body: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color,
)

/** Six ways in, for the days when the empty box is the hardest part. */
private val STARTERS = listOf(
    Starter("I am anxious", "I am anxious about something and I need peace.", Icons.Outlined.Air, Color(0xFF12161F)),
    Starter("For my family", "Please pray for my family.", Icons.Outlined.FavoriteBorder, Color(0xFF12161F)),
    Starter("To give thanks", "I want to thank God for what He has done.", Icons.Outlined.AutoAwesome, Color(0xFF12161F)),
    Starter("I cannot sleep", "I cannot sleep and my mind will not rest.", Icons.Outlined.DarkMode, Color(0xFF12161F)),
    Starter("For healing", "I need healing in my body.", Icons.Outlined.Spa, Color(0xFF12161F)),
    Starter("I need direction", "I do not know what to do next and I need direction.", Icons.Outlined.Explore, Color(0xFF12161F)),
)

@Composable
private fun ModeChips(deckMode: Boolean, onMode: (Boolean) -> Unit) {
    /* One track, two halves, and the selection slides between them —
       two free-floating pills read as tags rather than a switch. */
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(top = Space.block)
            .clip(RoundedCornerShape(15.dp))
            .background(cs.onBackground.copy(alpha = .07f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf("Pray for me" to false, "Prayer decks" to true).forEach { (label, deck) ->
            val on = deckMode == deck
            val fill by animateColorAsState(
                if (on) cs.surface else Color.Transparent, tween(220), label = "seg-fill",
            )
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(fill)
                    .clickable { onMode(deck) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = if (on) cs.onBackground else cs.onBackground.copy(alpha = .55f),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun deckColor(category: String): Color = Color(0xFFF2EBE0)


@Composable
private fun SavedCard(word: SavedWord, canAnswer: Boolean, onAnswer: () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = if (word.answeredAt == null) Ivory else Color(0xFFF1F8F3), border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row { Text(word.reference, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(word.translation, color = Muted, fontSize = 11.sp) }
            Text(word.verse, fontFamily = BookSerif, fontSize = 20.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 12.dp))
            word.testimony?.let { Text("“$it”", color = Color(0xFF257345), modifier = Modifier.padding(top = 15.dp)) }
            Text(DateFormat.getDateInstance().format(Date(word.answeredAt ?: word.savedAt)), color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 10.dp))
            if (canAnswer) TextButton(onClick = onAnswer, modifier = Modifier.align(Alignment.End)) { Text("Mark answered") }
        }
    }
}

@Composable
private fun EmptySaved(answered: Boolean) {
    Column(Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(if (answered) Icons.Outlined.FavoriteBorder else Icons.Outlined.BookmarkBorder, null, tint = Gold, modifier = Modifier.size(44.dp))
        Text(if (answered) "No answered prayers" else "No saved words", fontFamily = BookSerif, fontSize = 22.sp, modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun ProfileScreen(
    savedCount: Int, streak: Int, prefs: UserPrefs,
    entries: List<JournalEntry>, sermons: List<SermonNote>, prayers: List<JournalPrayer>, saved: List<SavedWord>,
    memory: List<MemoryVerse>, formation: com.prayerkey.manna.data.FormationState,
    onRestoreArchive: (String) -> Unit,
    onBack: () -> Unit, onUpdate: (UserPrefs) -> Unit,
) {
    val context = LocalContext.current
    var confirmRestore by remember { mutableStateOf(false) }
    val restoreArchive = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching { context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() } }
                .getOrNull()?.let(onRestoreArchive)
        }
    }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val next = prefs.copy(reminderEnabled = true); onUpdate(next)
            ReminderReceiver.schedule(context, next.reminderHour, next.reminderMinute, true)
        }
    }
    Column(Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(top = 20.dp, bottom = 42.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Text("You", fontFamily = BookSerif, fontSize = 32.sp)
        }
        OutlinedTextField(prefs.name, { onUpdate(prefs.copy(name = it)) }, label = { Text("Your name") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(16.dp))
        Surface(Modifier.fillMaxWidth().padding(vertical = 18.dp), color = Night, shape = RoundedCornerShape(26.dp)) {
            Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(streak.toString(), color = Color.White, fontFamily = BookSerif, fontSize = 48.sp); Text("day streak", color = Color.White.copy(.7f)) }
                Column(horizontalAlignment = Alignment.End) { Text(savedCount.toString(), color = Gold, fontSize = 30.sp); Text("words saved", color = Color.White.copy(.7f)) }
            }
        }
        /* ── Theme ──────────────────────────────────────────────────────
           This was the last screen of onboarding — a twelve-card carousel
           asked before anyone had seen a verse. It belongs here, where a
           choice about how the app looks can be changed on a whim. */
        ThemeSetting(prefs, onUpdate)

        SettingRow("Daily reminder", String.format(java.util.Locale.getDefault(), "%02d:%02d", prefs.reminderHour, prefs.reminderMinute), onClick = {
            TimePickerDialog(context, { _, hour, minute ->
                val next = prefs.copy(reminderHour = hour, reminderMinute = minute); onUpdate(next)
                ReminderReceiver.schedule(context, hour, minute, next.reminderEnabled)
            }, prefs.reminderHour, prefs.reminderMinute, false).show()
        }) { Switch(prefs.reminderEnabled, onCheckedChange = { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            else { val next = prefs.copy(reminderEnabled = enabled); onUpdate(next); ReminderReceiver.schedule(context, next.reminderHour, next.reminderMinute, enabled) }
        }) }
        var versionMenu by remember { mutableStateOf(false) }
        Box {
            SettingRow("Bible translation", "${prefs.translation} · ${versionOf(prefs.translation).name}", onClick = { versionMenu = true }) {
                Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Muted)
            }
            DropdownMenu(versionMenu, onDismissRequest = { versionMenu = false }) {
                BIBLE_VERSIONS.forEach { item ->
                    DropdownMenuItem(
                        text = { Text("${item.id} — ${item.name}") },
                        onClick = { onUpdate(prefs.copy(translation = item.id)); versionMenu = false },
                    )
                }
            }
        }
        SettingRow("Reduce motion", if (prefs.reduceMotion) "On" else "Off") {
            Switch(prefs.reduceMotion, onCheckedChange = { onUpdate(prefs.copy(reduceMotion = it)) })
        }
        SettingRow("Lock Journey", "Require your device PIN, pattern, or biometrics") {
            Switch(prefs.journalLock, onCheckedChange = { onUpdate(prefs.copy(journalLock = it)) })
        }
        SettingRow("Conceal entry previews", "Hide private writing in the timeline") {
            Switch(prefs.concealJournalPreviews, onCheckedChange = { onUpdate(prefs.copy(concealJournalPreviews = it)) })
        }
        SettingRow("Offline listening voices", "Manage downloaded voices for Scripture and prayer", onClick = {
            runCatching { context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)) }
                .recoverCatching { context.startActivity(Intent("com.android.settings.TTS_SETTINGS")) }
        }) { Icon(Icons.Outlined.RecordVoiceOver, null, tint = Gold) }
        SettingRow("Book of Remembrance", "Create my private ${java.time.LocalDate.now().year} PDF", onClick = {
            com.prayerkey.manna.share.RemembranceBook.share(context, prefs.name, entries, sermons, prayers, saved)
        }) { Icon(Icons.Outlined.AutoStories, null, tint = Gold) }
        SettingRow("Export private archive", "Portable JSON - You own your data", onClick = {
            com.prayerkey.manna.share.JourneyArchive.share(context, entries, sermons, prayers, saved, memory, formation)
        }) { Icon(Icons.Outlined.FileDownload, null, tint = Electric) }
        SettingRow("Restore private archive", "Replace this device's Journey from backup", onClick = { confirmRestore = true }) {
            Icon(Icons.Outlined.Restore, null, tint = Muted)
        }
        SettingRow("About MANNA", "Free · No ads") { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Muted) }
    }
    if (confirmRestore) AlertDialog(
        onDismissRequest = { confirmRestore = false },
        title = { Text("Restore your Journey?") },
        text = { Text("This replaces the current Journey, prayers, sermon notes, saved words, and memory progress on this device. Export the current archive first if you may need it.") },
        confirmButton = { TextButton(onClick = { confirmRestore = false; restoreArchive.launch(arrayOf("application/json", "text/plain")) }) { Text("Choose archive") } },
        dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Cancel") } },
    )
}

@Composable
private fun SettingRow(label: String, value: String, onClick: () -> Unit = {}, action: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).clickable(onClick = onClick).border(0.5.dp, Hairline).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(label); Text(value, color = Muted, fontSize = 11.sp) }
        action()
    }
}

@Composable
private fun ScreenFrame(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.background).padding(horizontal = 22.dp).padding(top = 24.dp)) {
        Text(title, fontFamily = BookSerif, fontSize = 32.sp)
        content()
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Electric, unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = AppleGray, unfocusedContainerColor = AppleGray,
)

/**
 * Picking a theme, in settings.
 *
 * Five rows, each showing its own accent against its own paper, so the choice
 * is made by looking at the thing rather than at a word. Plus the system
 * switch: with it on the phone decides light or dark and the chosen theme
 * only supplies the accent, which is why someone who picked Peace still gets
 * sage at 2am instead of being dragged back onto a white page.
 */
@Composable
private fun ThemeSetting(prefs: UserPrefs, onUpdate: (UserPrefs) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.padding(top = 20.dp)) {
        Text(
            "THEME",
            color = cs.onBackground.copy(alpha = .5f), fontSize = 9.5.sp,
            letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold,
        )
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            APP_THEMES.forEach { theme ->
                val chosen = theme.id == prefs.themeId
                Column(
                    Modifier.weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (chosen) theme.accent.copy(alpha = .12f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (chosen) theme.accent else cs.outlineVariant,
                            RoundedCornerShape(16.dp),
                        )
                        .clickable {
                            /* An explicit palette choice should look exactly
                               like its preview. Leaving system-follow enabled
                               silently replaced Vigil's navy paper with Grace
                               on a light phone, making the picker appear
                               broken. People can opt back into system mode
                               with the switch directly below. */
                            onUpdate(prefs.copy(themeId = theme.id, followSystemTheme = false))
                        }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // the paper, with the accent sitting on it
                    Box(
                        Modifier.size(30.dp).clip(CircleShape).background(theme.background)
                            .border(1.dp, theme.ink.copy(alpha = .18f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(15.dp).clip(CircleShape).background(theme.accent))
                    }
                    Text(
                        theme.name,
                        color = if (chosen) theme.accent else cs.onBackground.copy(alpha = .7f),
                        fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 7.dp),
                    )
                }
            }
        }
        Text(
            APP_THEMES.firstOrNull { it.id == prefs.themeId }?.note.orEmpty(),
            color = cs.onBackground.copy(alpha = .55f), fontSize = 12.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
        SettingRow(
            "Match my phone",
            if (prefs.followSystemTheme) "Light and dark follow the system" else "Always the theme I chose",
        ) {
            Switch(
                prefs.followSystemTheme,
                onCheckedChange = { onUpdate(prefs.copy(followSystemTheme = it)) },
            )
        }
    }
}
