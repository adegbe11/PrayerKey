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
import androidx.compose.material.icons.Icons
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
    reduceMotion: Boolean = false,
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

    /* TINDER ANATOMY: the card IS the screen — full-bleed, edge to edge.
       Search + version chips FLOAT on top of it; five action buttons
       float at the bottom. Nothing stacks above the card. */
    val shareContext = LocalContext.current
    var searchOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Canvas)) {
        if (showMemory) {
            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp).padding(top = 60.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    FloatChip(onClick = { showMemory = false }) {
                        Icon(Icons.Outlined.Close, "Back to verses", tint = Ink, modifier = Modifier.size(19.dp))
                    }
                }
                MemoryTrainer(memory.firstOrNull(), onAdvanceMemory)
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
                            Icon(Icons.Outlined.Search, "Search", tint = Ink, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        FloatChip(onClick = { showMemory = true }) {
                            Icon(Icons.Outlined.School, "Memorize", tint = Ink, modifier = Modifier.size(19.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            onClick = { pickerOpen = true }, shape = RoundedCornerShape(99.dp),
                            color = Color.Transparent, shadowElevation = 8.dp,
                            modifier = Modifier.background(NightGloss, RoundedCornerShape(99.dp)),
                        ) {
                            Row(Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.KeyboardArrowDown, "Change version", tint = Gold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    if (searchOpen) OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 8.dp),
                        placeholder = { },
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
                                        Spacer(Modifier.width(8.dp))
                                        SourcePill(item.source)
                                    }
                                }
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
                LazyColumn(Modifier.heightIn(max = 620.dp)) {
                    items(chapterVerses, key = { it.reference }) { item ->
                        Row(Modifier.fillMaxWidth().clickable { chapterTitle = ""; selectedVerse = RemoteVerse(item.reference, item.text, "KJV") }.padding(vertical = 8.dp)) {
                        }
                    }
                }
            }
        }
    }
}

/** Floating white circular chip that sits on top of the card. */
@Composable
private fun FloatChip(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(42.dp)
            .shadow(8.dp, CircleShape, spotColor = Night.copy(alpha = .3f))
            .clip(CircleShape).background(Color.White)
            .border(0.5.dp, Hairline, CircleShape)
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
    }
}

@Composable
private fun VersionPill(id: String) {
    Surface(shape = RoundedCornerShape(99.dp), color = Gold.copy(alpha = .15f)) {
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Button(onClick = onSave, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.BookmarkBorder, null); }
            OutlinedButton(onClick = onMemorize, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.School, null); }
        }
        TextButton(onClick = onReadChapter, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Icon(Icons.Outlined.MenuBook, null); }
        if (related.isEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth())
        related.forEach { item ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onRelated(item) },
                color = Color.White, shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Hairline),
            ) {
                Column(Modifier.padding(14.dp)) {
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
            Button(onClick = { onAdvance(memory.reference) }, modifier = Modifier.fillMaxWidth().padding(top = 22.dp)) { }
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
    var potdOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val potd = remember { com.prayerkey.manna.model.todaysPrayer() }
    // decks come from the app-wide cache — instant after first load
    LaunchedEffect(Unit) { onLoadTopics() }
    val topicsLoading = deckMode && topics.isEmpty()

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
        Box(Modifier.fillMaxSize().background(dayWash())) {
            if (topicsLoading) {
                Column(
                    Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = Electric, strokeWidth = 3.dp, modifier = Modifier.size(42.dp))
                }
            } else if (filtered.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 40.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        onClick = { topicQuery = "" }, shape = R.pill, color = ChipFill,
                        modifier = Modifier.padding(top = 18.dp),
                    ) {
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
                            com.prayerkey.manna.ui.components.DeckAction(
                                Icons.Outlined.Close, "Next prayer", Color(0xFFE0526B), 58.dp,
                            ) { controls.next() },
                            com.prayerkey.manna.ui.components.DeckAction(
                                Icons.Outlined.AutoAwesome, "Pray it", Gold, 50.dp,
                            ) { selectedTopic = topic },
                            com.prayerkey.manna.ui.components.DeckAction(
                                Icons.Outlined.BookmarkBorder, "Keep", Color(0xFF2E9E63), 58.dp,
                            ) { controls.keep() },
                            com.prayerkey.manna.ui.components.DeckAction(
                                Icons.Outlined.Share, "Share", Color(0xFF3C7BE0), 50.dp,
                            ) { selectedTopic = topic },
                        )
                    },
                    topOverlay = { position ->
                        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                com.prayerkey.manna.ui.components.PkGlassPill("‹  Pray for me", onClick = { deckMode = false })
                                Spacer(Modifier.weight(1f))
                                com.prayerkey.manna.ui.components.PkGlassPill("${position + 1} of ${filtered.size}")
                            }
                            OutlinedTextField(
                                topicQuery, { topicQuery = it },
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                placeholder = { },
                                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Muted) },
                                singleLine = true, shape = R.control,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold.copy(alpha = .5f),
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White.copy(alpha = .92f),
                                ),
                            )
                        }
                    },
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
        Column(
            Modifier.fillMaxSize().background(dayWash())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 24.dp, bottom = Space.dock),
        ) {
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
                    }
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
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 17.sp, lineHeight = 27.sp, color = InkSoft,
                        fontFamily = FontFamily.Serif,
                    ),
                    minLines = 3,
                    maxLines = 10,
                    shape = R.card,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold.copy(alpha = .7f),
                        unfocusedBorderColor = Hairline,
                        focusedContainerColor = Ivory,
                        unfocusedContainerColor = Ivory,
                    ),
                )

                /* Starter prompts, so an empty screen is never a dead end. A
                   blank input is the same blank-page problem the journal had,
                   and the cure is a way in, not a bigger box. */
                androidx.compose.animation.AnimatedVisibility(request.isBlank()) {
                    Column {
                        listOf(
                            "I am anxious" to "I am anxious about something and I need peace.",
                            "For my family" to "Please pray for my family.",
                            "To give thanks" to "I want to thank God for what He has done.",
                            "I cannot sleep" to "I cannot sleep and my mind will not rest.",
                            "For healing" to "I need healing in my body.",
                            "I need direction" to "I do not know what to do next and I need direction.",
                        ).chunked(2).forEach { row ->
                            Row(
                                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                row.forEach { pair ->
                                    Surface(
                                        onClick = { request = pair.second },
                                        shape = R.pill, color = ChipFill,
                                        modifier = Modifier.weight(1f),
                                    ) {
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

                error?.let { }

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
                    journal.take(2).forEach { entry ->
                        Box(Modifier.fillMaxWidth().padding(bottom = 8.dp).premiumCard(fill = PaperFill, lift = false)) {
                            Column(Modifier.padding(15.dp)) { ; }
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().premiumCard(fill = PaperFill)) {
                    Column(Modifier.padding(24.dp)) {
                        generated!!.verses.firstOrNull()?.let { }
                        if (generated!!.encouragement.isNotBlank())                         Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { generated?.let { onSavePrayer(request, it) } }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.BookmarkBorder, null); }
                            Button(onClick = { generated = null }, modifier = Modifier.weight(1f)) { }
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
                    Surface(shape = RoundedCornerShape(14.dp), color = Ivory, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                    }
                    potd.prayer.split("\n\n").forEach { para ->
                    }
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
                        }
                    }
                }
            }
        }
    }

    selectedTopic?.let { topic ->
        ModalBottomSheet(onDismissRequest = { selectedTopic = null }, containerColor = Canvas) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
                topic.scripture.forEach { (ref, text) ->
                    Surface(Modifier.fillMaxWidth().padding(top = 12.dp), color = Ivory, shape = RoundedCornerShape(15.dp)) {
                        Column(Modifier.padding(14.dp)) { ; }
                    }
                }
                if (topic.prayerPoints.isNotEmpty()) {
                    topic.prayerPoints.forEach { }
                }
                Button(onClick = { selectedTopic = null }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) { }
            }
        }
    }
}

@Composable
private fun ModeChips(deckMode: Boolean, onMode: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = Space.block), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        com.prayerkey.manna.ui.components.PkChip("Pray for me", !deckMode, Icons.Outlined.AutoAwesome) { onMode(false) }
        com.prayerkey.manna.ui.components.PkChip("Prayer decks", deckMode, Icons.Outlined.Style) { onMode(true) }
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
    ScreenFrame("Saved", "") {
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
