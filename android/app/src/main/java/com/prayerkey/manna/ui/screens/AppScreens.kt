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
import android.app.TimePickerDialog
import android.os.Build

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleScreen(
    memory: List<MemoryVerse>,
    onSave: (VerseCard) -> Unit,
    onMemorize: (VerseCard) -> Unit,
    onAdvanceMemory: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var remoteResults by remember { mutableStateOf<List<RemoteVerse>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var translation by remember { mutableStateOf("KJV") }
    var translationMenu by remember { mutableStateOf(false) }
    var showMemory by remember { mutableStateOf(false) }
    var selectedVerse by remember { mutableStateOf<BibleVerse?>(null) }
    var related by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var chapterVerses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var chapterTitle by remember { mutableStateOf("") }
    val context = LocalContext.current.applicationContext
    val bible = remember(context) { OfflineBible(context) }
    val scope = rememberCoroutineScope()
    fun runSearch(value: String) {
        scope.launch {
            loading = true
            if (translation == "KJV") {
                results = bible.search(value.ifBlank { "peace" }); remoteResults = emptyList()
            } else {
                remoteResults = runCatching { PrayerKeyApi.searchBible(value.ifBlank { "peace" }, translation) }.getOrElse { emptyList() }
                results = emptyList()
            }
            loading = false
        }
    }
    LaunchedEffect(Unit) { runSearch("peace") }
    LaunchedEffect(selectedVerse) {
        related = selectedVerse?.let { bible.related(it) }.orEmpty()
    }
    ScreenFrame("Bible", "Search Scripture by reference, word, or what you are feeling.") {
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Try “I can't sleep” or Psalm 23") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true,
            shape = RoundedCornerShape(18.dp), colors = fieldColors(),
            trailingIcon = { IconButton(onClick = { runSearch(query) }) { Icon(Icons.Outlined.ArrowForward, "Search") } },
        )
        Box(Modifier.padding(top = 10.dp)) {
            AssistChip(onClick = { translationMenu = true }, label = { Text("$translation ${if (translation == "KJV") "· Offline" else "· Online"}") }, leadingIcon = { Icon(Icons.Outlined.Translate, null) })
            DropdownMenu(translationMenu, onDismissRequest = { translationMenu = false }) {
                listOf("KJV","NIV","ESV","NKJV","NLT","NASB","AMP","CSB","MSG","CEV","GNT").forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = {
                        translation = item; translationMenu = false; runSearch(query)
                    })
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Fear", "Healing", "Peace").forEach { word ->
                SuggestionChip(onClick = { query = word; runSearch(word) }, label = { Text(word) })
            }
            SuggestionChip(onClick = { showMemory = !showMemory }, label = { Text("Memorize ${memory.size}") }, icon = { Icon(Icons.Outlined.School, null) })
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (showMemory) {
            MemoryTrainer(memory.firstOrNull(), onAdvanceMemory)
        } else if (translation == "KJV") LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(results, key = { it.reference }) { card ->
                Surface(
                    modifier = Modifier.clickable { selectedVerse = card },
                    shape = RoundedCornerShape(20.dp), color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Hairline),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(card.reference, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onMemorize(VerseCard(card.reference, "KJV", card.text, "")) }) { Icon(Icons.Outlined.School, "Memorize") }
                            IconButton(onClick = { onSave(VerseCard(card.reference, "KJV", card.text, "")) }) { Icon(Icons.Outlined.BookmarkBorder, "Save") }
                        }
                        Text(card.text, fontFamily = FontFamily.Serif, fontSize = 20.sp, lineHeight = 28.sp)
                        Text("KJV", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
        } else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(remoteResults, key = { "${it.translation}-${it.reference}" }) { card ->
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(card.reference, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onSave(VerseCard(card.reference, card.translation, card.text, "")) }) { Icon(Icons.Outlined.BookmarkBorder, "Save") }
                        }
                        Text(card.text, fontFamily = FontFamily.Serif, fontSize = 20.sp, lineHeight = 28.sp)
                        Text("${card.translation} · Online", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
        }
    }
    selectedVerse?.let { verse ->
        ModalBottomSheet(onDismissRequest = { selectedVerse = null }, containerColor = Canvas) {
            VerseDetail(
                verse = verse, related = related,
                onSave = { onSave(VerseCard(verse.reference, "KJV", verse.text, "")) },
                onMemorize = { onMemorize(VerseCard(verse.reference, "KJV", verse.text, "")) },
                onRelated = { selectedVerse = it },
                onReadChapter = {
                    selectedVerse = null
                    chapterTitle = "${verse.book} ${verse.chapter}"
                    scope.launch { chapterVerses = bible.chapter(verse.book, verse.chapter) }
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
                        Row(Modifier.fillMaxWidth().clickable { chapterTitle = ""; selectedVerse = item }.padding(vertical = 8.dp)) {
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
private fun VerseDetail(
    verse: BibleVerse,
    related: List<BibleVerse>,
    onSave: () -> Unit,
    onMemorize: () -> Unit,
    onRelated: (BibleVerse) -> Unit,
    onReadChapter: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
        Text(verse.reference, fontFamily = FontFamily.Serif, fontSize = 30.sp)
        Text("King James Version", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
        Text(verse.text, fontFamily = FontFamily.Serif, fontSize = 28.sp, lineHeight = 38.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 34.dp))
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
fun PrayerScreen(journal: List<JournalPrayer>, onSavePrayer: (String, GeneratedPrayer) -> Unit) {
    var request by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("Healing") }
    var generated by remember { mutableStateOf<GeneratedPrayer?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var deckMode by remember { mutableStateOf(false) }
    var topics by remember { mutableStateOf<List<PrayerTopic>>(emptyList()) }
    var selectedTopic by remember { mutableStateOf<PrayerTopic?>(null) }
    var topicQuery by remember { mutableStateOf("") }
    var topicsLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(deckMode) {
        if (deckMode && topics.isEmpty()) {
            topicsLoading = true
            runCatching { PrayerKeyApi.prayerTopics() }.onSuccess { topics = it }.onFailure { error = it.message }
            topicsLoading = false
        }
    }
    ScreenFrame(if (deckMode) "Prayer decks" else "Pray for me", if (deckMode) "544 prayers for every season of life." else "Bring what is on your heart. PrayerKey will pray with you.") {
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(!deckMode, { deckMode = false }, label = { Text("Pray for me") }, leadingIcon = { Icon(Icons.Outlined.AutoAwesome, null) })
            FilterChip(deckMode, { deckMode = true }, label = { Text("Prayer decks") }, leadingIcon = { Icon(Icons.Outlined.Style, null) })
        }
        if (deckMode) {
            OutlinedTextField(
                topicQuery, { topicQuery = it }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                placeholder = { Text("Healing, family, work, grief…") }, leadingIcon = { Icon(Icons.Outlined.Search, null) },
                singleLine = true, shape = RoundedCornerShape(18.dp), colors = fieldColors(),
            )
            if (topicsLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            val filtered = topics.filter { topicQuery.isBlank() || it.title.contains(topicQuery, true) || it.category.contains(topicQuery, true) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Spacer(Modifier.height(18.dp))
            Text("What would you like\nPrayerKey to pray for you?", fontFamily = FontFamily.Serif, fontSize = 30.sp, lineHeight = 37.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                request, { request = it }, modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                placeholder = { Text("Type your prayer request…") }, minLines = 3,
                shape = RoundedCornerShape(18.dp), colors = fieldColors(),
            )
            Text("Choose a mood", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Peace", "Healing", "Strength").forEach { item ->
                    FilterChip(selected = mood == item, onClick = { mood = item }, label = { Text(item) })
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp)) }
            Button(
                onClick = {
                    scope.launch {
                        loading = true; error = null
                        runCatching { PrayerKeyApi.generatePrayer(request, listOf(mood)) }
                            .onSuccess { generated = it }
                            .onFailure { error = it.message ?: "Prayer could not be generated" }
                        loading = false
                    }
                }, enabled = request.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 10.dp), shape = RoundedCornerShape(17.dp),
            ) { if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Icon(Icons.Outlined.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text(if (loading) "Preparing your prayer…" else "Pray for me") }
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
        if (shown.isEmpty()) EmptySaved(answeredTab) else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        SettingRow("Bible translation", "KJV") { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Muted) }
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
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.Share, null); Text(" Share") }
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
    focusedBorderColor = Electric, unfocusedBorderColor = Hairline,
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
)
