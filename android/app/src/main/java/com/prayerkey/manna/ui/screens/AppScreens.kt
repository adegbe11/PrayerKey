package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.prayerkey.manna.data.RemoteVerse
import com.prayerkey.manna.data.BibleVersion
import com.prayerkey.manna.data.BIBLE_VERSIONS
import com.prayerkey.manna.data.VersionSource
import com.prayerkey.manna.data.versionOf
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import android.app.TimePickerDialog
import android.os.Build

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleScreen(
    memory: List<MemoryVerse>,
    translation: String,
    onTranslation: (String) -> Unit,
    onSave: (VerseCard) -> Unit,
    onMemorize: (VerseCard) -> Unit,
    onAdvanceMemory: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var shown by remember { mutableStateOf<List<RemoteVerse>>(emptyList()) }
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

    /* Tinder rule: the CARD owns the screen. One slim header row +
       search — everything else lives on or under the card. */
    Column(Modifier.fillMaxSize().background(Canvas).padding(horizontal = 14.dp).padding(top = 14.dp)) {

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Bible", fontFamily = FontFamily.Serif, fontSize = 26.sp)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showMemory = !showMemory }) {
                Icon(Icons.Outlined.School, "Memorize", tint = if (showMemory) Gold else Muted)
            }
            Surface(onClick = { pickerOpen = true }, shape = RoundedCornerShape(99.dp), color = Night) {
                Row(Modifier.padding(start = 14.dp, end = 8.dp, top = 7.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(version.id, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Icon(Icons.Outlined.KeyboardArrowDown, "Change version", tint = Gold)
                }
            }
        }

        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            placeholder = { Text("Search", fontSize = 14.sp, color = Muted) },
            leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true,
            shape = RoundedCornerShape(16.dp), colors = fieldColors(),
            trailingIcon = { IconButton(onClick = { runSearch(query) }) { Icon(Icons.Outlined.ArrowForward, "Search") } },
        )

        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 6.dp), color = Gold, trackColor = Hairline)
        if (showMemory) {
            Spacer(Modifier.height(10.dp))
            MemoryTrainer(memory.firstOrNull(), onAdvanceMemory)
        } else Box(Modifier.weight(1f).fillMaxWidth().padding(top = 10.dp, bottom = 6.dp)) {
            com.prayerkey.manna.ui.components.VersePullDeck(
                verses = shown,
                onSave = { onSave(VerseCard(it.reference, it.translation, it.text, "")) },
                onMemorize = { onMemorize(VerseCard(it.reference, it.translation, it.text, "")) },
                onOpen = { selectedVerse = it },
            )
        }
    }

    /* ── Version picker — premium bottom sheet ── */
    if (pickerOpen) {
        ModalBottomSheet(onDismissRequest = { pickerOpen = false }, containerColor = Canvas) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 40.dp)) {
                Text("Choose your Bible", fontFamily = FontFamily.Serif, fontSize = 28.sp)
                Text("14 versions. All free, forever.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp, bottom = 16.dp))
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
                                    Text("${item.name} — ${item.tagline}", color = if (active) Color.White.copy(.7f) else Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
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
                    val source = runCatching { kotlinx.coroutines.runBlocking { bible.search(verse.reference, 1).firstOrNull() } }.getOrNull()
                    if (source != null) {
                        selectedVerse = null
                        chapterTitle = "${source.book} ${source.chapter}"
                        scope.launch { chapterVerses = bible.chapter(source.book, source.chapter) }
                    }
                },
            )
        }
    }
    if (chapterTitle.isNotBlank()) {
        ModalBottomSheet(onDismissRequest = { chapterTitle = ""; chapterVerses = emptyList() }, containerColor = Canvas) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 36.dp)) {
                Text(chapterTitle, fontFamily = FontFamily.Serif, fontSize = 30.sp)
                Text("King James Version · available offline", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 18.dp))
                LazyColumn(Modifier.heightIn(max = 620.dp)) {
                    items(chapterVerses, key = { it.reference }) { item ->
                        Row(Modifier.fillMaxWidth().clickable { chapterTitle = ""; selectedVerse = RemoteVerse(item.reference, item.text, "KJV") }.padding(vertical = 8.dp)) {
                            Text(item.verse.toString(), color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.width(34.dp))
                            Text(item.text, fontFamily = FontFamily.Serif, fontSize = 19.sp, lineHeight = 27.sp)
                        }
                    }
                }
            }
        }
    }
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
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
        Text(reference, fontFamily = FontFamily.Serif, fontSize = 30.sp)
        Text("$versionName ($versionId)", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
        Text(text, fontFamily = FontFamily.Serif, fontSize = 28.sp, lineHeight = 38.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 34.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Button(onClick = onSave, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.BookmarkBorder, null); Text(" Save") }
            OutlinedButton(onClick = onMemorize, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.School, null); Text(" Memorize") }
        }
        TextButton(onClick = onReadChapter, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Icon(Icons.Outlined.MenuBook, null); Text(" Read full chapter offline") }
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
}

@Composable
private fun MemoryTrainer(memory: MemoryVerse?, onAdvance: (String) -> Unit) {
    if (memory == null) {
        EmptySaved(false)
        return
    }
    val words = memory.verse.split(" ")
    val masked = words.mapIndexed { index, word ->
        val hideEvery = (6 - memory.stage).coerceAtLeast(1)
        if ((index + 1) % hideEvery == 0) "_".repeat(word.length.coerceAtMost(8)) else word
    }.joinToString(" ")
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Night) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("MEMORIZE · LEVEL ${memory.stage}", color = Gold, fontSize = 11.sp, letterSpacing = 1.4.sp)
            Text(masked, color = Color.White, fontFamily = FontFamily.Serif, fontSize = 25.sp, lineHeight = 34.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 30.dp))
            Text(memory.reference, color = Color.White.copy(.65f))
            Button(onClick = { onAdvance(memory.reference) }, modifier = Modifier.fillMaxWidth().padding(top = 22.dp)) { Text(if (memory.stage >= 5) "Practice again" else "I recited it — hide more") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(journal: List<JournalPrayer>, topics: List<PrayerTopic>, onLoadTopics: () -> Unit, onSavePrayer: (String, GeneratedPrayer) -> Unit) {
    var request by remember { mutableStateOf("") }
    val moods = remember { mutableStateListOf<String>() }
    var generated by remember { mutableStateOf<GeneratedPrayer?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var deckMode by remember { mutableStateOf(false) }
    var selectedTopic by remember { mutableStateOf<PrayerTopic?>(null) }
    var topicQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    // decks come from the app-wide cache — instant after first load
    LaunchedEffect(Unit) { onLoadTopics() }
    val topicsLoading = deckMode && topics.isEmpty()
    ScreenFrame(if (deckMode) "Prayer decks" else "Pray for me", if (deckMode) "544 prayers for every season of life." else "Bring what is on your heart. PrayerKey will pray with you.") {
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(!deckMode, { deckMode = false }, label = { Text("Pray for me") }, leadingIcon = { Icon(Icons.Outlined.AutoAwesome, null) })
            FilterChip(deckMode, { deckMode = true }, label = { Text("Prayer decks") }, leadingIcon = { Icon(Icons.Outlined.Style, null) })
        }

        /* ── Prayer of the Day — same daily prayer as prayerkey.com ── */
        val potd = remember { com.prayerkey.manna.model.todaysPrayer() }
        var potdOpen by remember { mutableStateOf(false) }
        if (!deckMode) Surface(
            onClick = { potdOpen = true },
            shape = RoundedCornerShape(22.dp), color = Night,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("✦ PRAYER OF THE DAY", color = Gold, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                    Text(potd.title, color = Color.White, fontFamily = FontFamily.Serif, fontSize = 19.sp, modifier = Modifier.padding(top = 4.dp))
                    Text(potd.ref, color = Color.White.copy(.6f), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                }
                Text("Read", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Gold)
            }
        }
        if (potdOpen) {
            val shareContext = LocalContext.current
            ModalBottomSheet(onDismissRequest = { potdOpen = false }, containerColor = Canvas) {
                LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 24.dp), contentPadding = PaddingValues(bottom = 44.dp)) {
                    item {
                        Text("✦ PRAYER OF THE DAY ✦", color = Gold, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                        Text(potd.title, fontFamily = FontFamily.Serif, fontSize = 28.sp, modifier = Modifier.padding(top = 6.dp))
                        Text(potd.ref, color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 3.dp))
                        Surface(shape = RoundedCornerShape(14.dp), color = Ivory, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                            Text("“${potd.verse}”", fontFamily = FontFamily.Serif, fontSize = 17.sp, lineHeight = 25.sp, modifier = Modifier.padding(16.dp))
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
        if (deckMode) {
            OutlinedTextField(
                topicQuery, { topicQuery = it }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                placeholder = { Text("Healing, family, work, grief…") }, leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true, shape = RoundedCornerShape(18.dp), colors = fieldColors(),
            )
            if (topicsLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            val filtered = topics.filter { topicQuery.isBlank() || it.title.contains(topicQuery, true) || it.category.contains(topicQuery, true) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 48.dp)) {
                items(filtered, key = { it.slug }) { topic ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { selectedTopic = topic },
                        color = deckColor(topic.category), shape = RoundedCornerShape(20.dp),
                    ) {
                        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(45.dp).background(Color.White.copy(.72f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.FavoriteBorder, null, tint = Gold) }
                            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                Text(topic.title, fontFamily = FontFamily.Serif, fontSize = 20.sp)
                                Text(topic.category, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                            }
                            Icon(Icons.Outlined.KeyboardArrowRight, null)
                        }
                    }
                }
            }
        } else if (generated == null) {
            /* site's AI Prayer Generator design, in Manna's white theme */
            Spacer(Modifier.height(16.dp))
            Text(
                "Tell me what to\npray about.",
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                fontSize = 34.sp, lineHeight = 40.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Type anything: a worry, a thank you, a situation.\nA full prayer written for you in seconds.",
                color = Muted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            OutlinedTextField(
                request, { request = it }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                placeholder = { Text("e.g. I'm worried about my job. Please pray for my family. I'm thankful for healing...", fontSize = 14.sp, lineHeight = 20.sp) },
                minLines = 4,
                shape = RoundedCornerShape(18.dp), colors = fieldColors(),
            )
            Text("HOW ARE YOU FEELING? (OPTIONAL)", color = Muted, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            listOf(
                listOf("Grateful", "Anxious", "Sad", "Hopeful"),
                listOf("Confused", "Joyful", "Sick", "Tired"),
            ).forEach { rowMoods ->
                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    rowMoods.forEach { item ->
                        FilterChip(
                            selected = item in moods,
                            onClick = { if (item in moods) moods.remove(item) else moods.add(item) },
                            label = { Text(item, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp)) }
            Button(
                onClick = {
                    scope.launch {
                        loading = true; error = null
                        runCatching { PrayerKeyApi.generatePrayer(request, moods.toList()) }
                            .onSuccess { generated = it }
                            .onFailure { error = it.message ?: "Prayer could not be generated" }
                        loading = false
                    }
                }, enabled = request.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(56.dp), shape = RoundedCornerShape(17.dp),
            ) { if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Icon(Icons.Outlined.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text(if (loading) "Preparing your prayer…" else "Generate Prayer", fontWeight = FontWeight.SemiBold) }
            if (journal.isNotEmpty()) {
                Text("Prayer journal", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
                journal.take(2).forEach { entry ->
                    Surface(Modifier.fillMaxWidth().padding(bottom = 7.dp), color = Ivory, shape = RoundedCornerShape(15.dp)) {
                        Column(Modifier.padding(13.dp)) { Text(entry.title, fontFamily = FontFamily.Serif, fontSize = 17.sp); Text(entry.scriptureRef.orEmpty(), color = Gold, fontSize = 11.sp) }
                    }
                }
            }
        } else {
            Surface(Modifier.fillMaxWidth().padding(top = 18.dp), shape = RoundedCornerShape(24.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)) {
                Column(Modifier.padding(22.dp)) {
                    Text(generated!!.title, fontFamily = FontFamily.Serif, fontSize = 25.sp)
                    Text("Generated for you", color = Electric, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp, bottom = 20.dp))
                    Text(generated!!.prayer, lineHeight = 24.sp)
                    generated!!.verses.firstOrNull()?.let { Text(it.first, color = Gold, modifier = Modifier.padding(top = 20.dp)) }
                    if (generated!!.encouragement.isNotBlank()) Text(generated!!.encouragement, color = Muted, modifier = Modifier.padding(top = 14.dp))
                    Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { generated?.let { onSavePrayer(request, it) } }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.BookmarkBorder, null); Text(" Journal") }
                        Button(onClick = { generated = null }, modifier = Modifier.weight(1f)) { Text("Pray again") }
                    }
                }
            }
        }
    }
    selectedTopic?.let { topic ->
        ModalBottomSheet(onDismissRequest = { selectedTopic = null }, containerColor = Canvas) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
                Text(topic.title, fontFamily = FontFamily.Serif, fontSize = 29.sp)
                Text(topic.category, color = Gold, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 22.dp))
                Text(topic.prayer, fontSize = 17.sp, lineHeight = 27.sp)
                topic.scripture.forEach { (ref, text) ->
                    Surface(Modifier.fillMaxWidth().padding(top = 12.dp), color = Ivory, shape = RoundedCornerShape(15.dp)) {
                        Column(Modifier.padding(14.dp)) { Text(ref, fontWeight = FontWeight.Bold); Text(text, color = Muted, modifier = Modifier.padding(top = 4.dp)) }
                    }
                }
                if (topic.prayerPoints.isNotEmpty()) {
                    Text("Prayer points", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 22.dp))
                    topic.prayerPoints.forEach { Text("• $it", modifier = Modifier.padding(top = 7.dp)) }
                }
                Button(onClick = { selectedTopic = null }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) { Text("Amen") }
            }
        }
    }
}

private fun deckColor(category: String): Color = when {
    category.contains("Health", true) -> Color(0xFFEAF6F0)
    category.contains("Mental", true) -> Color(0xFFEEF1FF)
    category.contains("Family", true) || category.contains("Relationship", true) -> Color(0xFFFFF0ED)
    category.contains("Finance", true) -> Color(0xFFFFF6DF)
    else -> Color(0xFFF4F1FA)
}

@Composable
fun SavedScreen(words: List<SavedWord>, onAnswered: (Long, String) -> Unit) {
    var answeredTab by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<SavedWord?>(null) }
    ScreenFrame("Saved", "Every word that met you, kept in one place.") {
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(!answeredTab, { answeredTab = false }, label = { Text("Saved ${words.count { it.answeredAt == null }}") })
            FilterChip(answeredTab, { answeredTab = true }, label = { Text("Answered ${words.count { it.answeredAt != null }}") })
        }
        val shown = words.filter { (it.answeredAt != null) == answeredTab }
        if (shown.isEmpty()) EmptySaved(answeredTab) else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 48.dp)) {
            items(shown, key = { it.id }) { word -> SavedCard(word, !answeredTab) { selected = word } }
        }
    }
    selected?.let { word ->
        var testimony by remember(word.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text("What did God do?") },
            text = { OutlinedTextField(testimony, { testimony = it }, placeholder = { Text("Write one line of testimony…") }, minLines = 3) },
            confirmButton = { TextButton(onClick = { if (testimony.isNotBlank()) { onAnswered(word.id, testimony); selected = null } }) { Text("Mark answered") } },
            dismissButton = { TextButton(onClick = { selected = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SavedCard(word: SavedWord, canAnswer: Boolean, onAnswer: () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = if (word.answeredAt == null) Ivory else Color(0xFFF1F8F3), border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row { Text(word.reference, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(word.translation, color = Muted, fontSize = 11.sp) }
            Text(word.verse, fontFamily = FontFamily.Serif, fontSize = 20.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 12.dp))
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
        Text(if (answered) "Your proof pile begins here" else "Push a card up to keep it", fontFamily = FontFamily.Serif, fontSize = 22.sp, modifier = Modifier.padding(top = 16.dp))
        Text(if (answered) "Answered prayers become your testimony over time." else "The words that meet you will wait here.", color = Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun ProfileScreen(savedCount: Int, streak: Int, prefs: UserPrefs, onBack: () -> Unit, onUpdate: (UserPrefs) -> Unit) {
    val context = LocalContext.current
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val next = prefs.copy(reminderEnabled = true); onUpdate(next)
            ReminderReceiver.schedule(context, next.reminderHour, next.reminderMinute, true)
        }
    }
    Column(Modifier.fillMaxSize().background(Canvas).padding(horizontal = 22.dp).padding(top = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Column { Text("You", fontFamily = FontFamily.Serif, fontSize = 32.sp); Text("Your quiet rhythm with God.", color = Muted, fontSize = 13.sp) }
        }
        OutlinedTextField(prefs.name, { onUpdate(prefs.copy(name = it)) }, label = { Text("Your name") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(16.dp))
        Surface(Modifier.fillMaxWidth().padding(vertical = 18.dp), color = Night, shape = RoundedCornerShape(26.dp)) {
            Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(streak.toString(), color = Color.White, fontFamily = FontFamily.Serif, fontSize = 48.sp); Text("day streak", color = Color.White.copy(.7f)) }
                Column(horizontalAlignment = Alignment.End) { Text(savedCount.toString(), color = Gold, fontSize = 30.sp); Text("words saved", color = Color.White.copy(.7f)) }
            }
        }
        SettingRow("Daily reminder", String.format("%02d:%02d", prefs.reminderHour, prefs.reminderMinute), onClick = {
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
        SettingRow("About MANNA", "Free · No ads") { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Muted) }
    }
}

@Composable
private fun SettingRow(label: String, value: String, onClick: () -> Unit = {}, action: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).clickable(onClick = onClick).border(0.5.dp, Hairline).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(label); Text(value, color = Muted, fontSize = 11.sp) }
        action()
    }
}

@Composable
fun ChurchScreen(
    sermons: List<SermonSession>,
    onStartSession: () -> Long,
    onVerseDetected: (Long, String, String) -> Unit,
    onEndSession: (Long) -> Unit,
    onSave: (VerseCard) -> Unit,
) {
    var listening by remember { mutableStateOf(false) }
    var transcript by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready") }
    var detected by remember { mutableStateOf<DetectedVerse?>(null) }
    var sessionId by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current
    val recognizer = remember(context) {
        SermonRecognizer(context, onWords = { transcript = it }, onStatus = { status = it })
    }
    DisposableEffect(recognizer) { onDispose { recognizer.destroy() } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) { sessionId = onStartSession(); listening = true; recognizer.start() } else status = "Microphone permission is required"
    }
    LaunchedEffect(listening, transcript) {
        if (listening && transcript.length >= 10) {
            delay(850)
            runCatching { PrayerKeyApi.detectVerse(transcript.takeLast(500)) }
                .onSuccess { result ->
                    if (result.detected) {
                        detected = result
                        sessionId?.let { onVerseDetected(it, result.reference, result.text) }
                    }
                }
        }
    }
    ScreenFrame("Church", "Take every verse from Sunday home with you.") {
        Column(Modifier.fillMaxWidth().padding(top = 34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (listening) "Listening…" else "Ready when service starts", color = if (listening) Electric else Ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(if (listening) "We are listening for Bible verses being mentioned." else "Your audio stays focused on finding Scripture.", color = Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            Row(Modifier.fillMaxWidth().height(82.dp).padding(vertical = 22.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                repeat(22) { index ->
                    Box(Modifier.width(3.dp).height(((index * 13) % 34 + if (listening) 10 else 3).dp).background(if (listening) Electric else Hairline, CircleShape))
                }
            }
            FilledIconButton(
                onClick = {
                    if (listening) { listening = false; recognizer.stop(); sessionId?.let(onEndSession); sessionId = null }
                    else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        sessionId = onStartSession(); listening = true; recognizer.start()
                    } else permission.launch(Manifest.permission.RECORD_AUDIO)
                },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Electric),
            ) { Icon(if (listening) Icons.Outlined.Stop else Icons.Outlined.Mic, if (listening) "Stop listening" else "Start listening", tint = Color.White, modifier = Modifier.size(31.dp)) }
            Text(if (listening) "Stop listening" else "Start listening", color = Electric, modifier = Modifier.padding(top = 10.dp))
            if (transcript.isNotBlank()) Text("“$transcript”", color = Muted, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.padding(top = 15.dp))
        }
        detected?.let { detected -> Surface(Modifier.fillMaxWidth().padding(top = 30.dp), shape = RoundedCornerShape(22.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)) {
            Column(Modifier.padding(20.dp)) {
                Row { Text("Detected verse", color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text("KJV", color = Muted, fontSize = 11.sp) }
                Text(detected.reference, fontWeight = FontWeight.Bold, fontSize = 19.sp, modifier = Modifier.padding(top = 8.dp))
                Text(detected.text, fontFamily = FontFamily.Serif, fontSize = 20.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 10.dp))
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onSave(VerseCard(detected.reference, "KJV", detected.text, "")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.BookmarkBorder, null); Text(" Save") }
                    OutlinedButton(onClick = { com.prayerkey.manna.share.CardShareRenderer.share(context, VerseCard(detected.reference, "KJV", detected.text, "")) }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.Share, null); Text(" Share") }
                }
            }
        } }
        if (!listening && sermons.isNotEmpty()) {
            Text("Sunday stacks", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
            sermons.take(2).forEach { sermon ->
                Surface(Modifier.fillMaxWidth().padding(bottom = 8.dp), color = Ivory, shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Style, null, tint = Gold)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(sermon.title, fontWeight = FontWeight.Bold)
                            Text("${sermon.verses.size} verses captured", color = Muted, fontSize = 11.sp)
                        }
                        Text(sermon.verses.firstOrNull()?.reference.orEmpty(), color = Electric, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenFrame(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Canvas).padding(horizontal = 22.dp).padding(top = 24.dp)) {
        Text(title, fontFamily = FontFamily.Serif, fontSize = 32.sp)
        Text(subtitle, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
        content()
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Electric, unfocusedBorderColor = Color.Transparent,
    focusedContainerColor = AppleGray, unfocusedContainerColor = AppleGray,
)
