package com.prayerkey.manna.ui.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.components.PkChip
import com.prayerkey.manna.data.JournalEntry
import com.prayerkey.manna.data.SavedWord
import com.prayerkey.manna.data.JourneyInsight
import com.prayerkey.manna.data.JourneyInsights
import com.prayerkey.manna.data.PrayerStage
import com.prayerkey.manna.data.FormationState
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.ui.theme.*
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date

/* Mood vocabulary — emoji stored as the key */
private val MOODS = listOf(
    "🙏" to "Praying", "😊" to "Grateful", "✨" to "Hopeful",
    "🔥" to "Faith", "😔" to "Heavy", "😭" to "Broken",
)

private enum class JournalTab { Journey, Practices, Saved, Answered }
private enum class JourneyView(val label: String) { Entries("Entries"), Calendar("Calendar"), Memories("Memories") }

@Composable
fun JournalScreen(
    entries: List<JournalEntry>,
    journalStreak: Int,
    words: List<SavedWord>,
    todayCard: VerseCard,
    onAdd: (String, String, String, String?, String?) -> Unit,
    onUpdate: (Long, String, String, String) -> Unit,
    onDelete: (Long) -> Unit,
    onAnswered: (Long, String) -> Unit,
    onProfile: () -> Unit,
    sermonNotes: List<com.prayerkey.manna.data.SermonNote>,
    prayers: List<com.prayerkey.manna.data.JournalPrayer>,
    onAdd2: (com.prayerkey.manna.ui.journal.WriteResult) -> Unit,
    onUpdate2: (Long, com.prayerkey.manna.ui.journal.WriteResult) -> Unit,
    onAnswerEntry: (Long, String) -> Unit,
    onUpdatePrayerJourney: (Long, PrayerStage, String, String) -> Unit,
    formation: FormationState,
    onUpdateFormation: (FormationState) -> Unit,
    journalLocked: Boolean = false,
    concealPreviews: Boolean = false,
) {
    val context = LocalContext.current
    var unlocked by remember(journalLocked) { mutableStateOf(!journalLocked) }
    val unlock = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) unlocked = true
    }
    if (!unlocked) {
        Box(Modifier.fillMaxSize().background(dayWash()), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                JournalMark()
                Text("Your Journal is private", fontFamily = FontFamily.Serif, fontSize = 24.sp, modifier = Modifier.padding(top = 18.dp))
                Text("Unlock with your device security to read or write entries.", color = Muted, fontSize = 12.5.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 7.dp))
                Button(onClick = {
                    val manager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    manager.createConfirmDeviceCredentialIntent("Unlock MANNA Journal", "Your private journal stays behind your device lock")?.let(unlock::launch)
                        ?: run { unlocked = true }
                }, modifier = Modifier.padding(top = 20.dp)) { Text("Unlock Journal") }
            }
        }
        return
    }
    var tab by remember { mutableStateOf(JournalTab.Journey) }
    var composing by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<JournalEntry?>(null) }
    var viewing by remember { mutableStateOf<JournalEntry?>(null) }
    var answering by remember { mutableStateOf<SavedWord?>(null) }
    var query by remember { mutableStateOf("") }
    // Write is two steps now: pick a prompt, then write. `picking` is the
    // suggestion sheet; `writing` holds the chosen prompt (null = blank page).
    var picking by remember { mutableStateOf(false) }
    var writing by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf<com.prayerkey.manna.ui.journal.JournalSuggestions.Prompt?>(null) }
    var answeringEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var reviewingPrayer by remember { mutableStateOf<JournalEntry?>(null) }
    var libraryOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(dayWash())) {
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp).padding(top = 24.dp)) {
            // Home is bare now, so Settings lives here — the one screen
            // that is already about the user rather than today's word.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (tab != JournalTab.Journey) IconButton(onClick = { tab = JournalTab.Journey }) { Icon(Icons.Outlined.ArrowBack, "Back to journal") }
                Column(Modifier.weight(1f)) {
                    Text(when (tab) { JournalTab.Journey -> "Journal"; JournalTab.Practices -> "Practices"; JournalTab.Saved -> "Saved words"; JournalTab.Answered -> "Answered prayers" }, fontFamily = FontFamily.Serif, fontSize = 32.sp)
                }
                if (tab == JournalTab.Journey) Box {
                    IconButton(onClick = { libraryOpen = true }) { Icon(Icons.Outlined.MoreHoriz, "Open journal library", tint = Muted) }
                    DropdownMenu(expanded = libraryOpen, onDismissRequest = { libraryOpen = false }) {
                        DropdownMenuItem(text = { Text("Practices") }, onClick = { tab = JournalTab.Practices; libraryOpen = false })
                        DropdownMenuItem(text = { Text("Saved words · ${words.count { it.answeredAt == null }}") }, onClick = { tab = JournalTab.Saved; libraryOpen = false })
                        DropdownMenuItem(text = { Text("Answered prayers · ${words.count { it.answeredAt != null } + entries.count { it.answeredAt != null }}") }, onClick = { tab = JournalTab.Answered; libraryOpen = false })
                    }
                }
                androidx.compose.material3.IconButton(onClick = onProfile) {
                    Icon(Icons.Outlined.Settings, "Settings", tint = Muted)
                }
            }

            val journeyInsight = remember(entries, sermonNotes, prayers, words) {
                JourneyInsights.build(entries, sermonNotes, prayers, words)
            }
            when (tab) {
                JournalTab.Journey -> JournalTimeline(
                    entries = entries, streak = journalStreak, query = query,
                    onQuery = { query = it }, onEdit = { viewing = it },
                    onAnswer = { reviewingPrayer = it },
                    insight = journeyInsight,
                    concealPreviews = concealPreviews,
                )
                JournalTab.Practices -> FormationScreen(formation, onUpdateFormation, onAdd2)
                JournalTab.Saved -> WordList(words.filter { it.answeredAt == null }, canAnswer = true) { answering = it }
                JournalTab.Answered -> AnsweredList(
                    entries = entries.filter { it.answeredAt != null },
                    words = words.filter { it.answeredAt != null },
                    onEdit = { editing = it },
                )
            }
        }

        if (tab == JournalTab.Journey) {
            // gentle pull toward the one action, only while there is nothing else
            val transition = rememberInfiniteTransition(label = "fab")
            val pulse by transition.animateFloat(
                initialValue = 1f,
                targetValue = if (entries.isEmpty()) 1.06f else 1f,
                animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
                label = "fab-pulse",
            )
            // circular, soft-shadowed — Wallet's action button, not a Material pill
            Box(
                Modifier.align(Alignment.BottomEnd).padding(24.dp).scale(pulse)
                    .size(62.dp)
                    .shadow(16.dp, CircleShape, spotColor = Night.copy(alpha = .28f))
                    .clip(CircleShape).background(Night)
                    .clickable { picking = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Create, "Write an entry", tint = Gold, modifier = Modifier.size(25.dp))
            }
        }
    }

    if (picking) {
        val prompts = remember(sermonNotes, prayers, words) {
            com.prayerkey.manna.ui.journal.JournalSuggestions.build(
                sermons = sermonNotes, prayers = prayers, savedWords = words,
                nowMillis = System.currentTimeMillis(),
            )
        }
        com.prayerkey.manna.ui.journal.SuggestionSheet(
            prompts = prompts,
            onPick = { prompt -> chosen = prompt; picking = false; writing = true },
            onDismiss = { picking = false },
        )
    }

    if (writing) com.prayerkey.manna.ui.journal.WriteSheet(
        prompt = chosen,
        todayCard = todayCard,
        onDismiss = { writing = false; chosen = null },
        onSubmit = { result -> onAdd2(result); writing = false; chosen = null },
    )

    viewing?.let { entry ->
        EntryDetailSheet(entry, onDismiss = { viewing = null }, onEdit = { viewing = null; editing = entry })
    }

    editing?.let { entry ->
        com.prayerkey.manna.ui.journal.WriteSheet(
            prompt = null,
            todayCard = todayCard,
            initialMood = entry.mood,
            initialBody = entry.body,
            initialGratitude = entry.gratitude,
            initialIsPrayer = entry.isPrayer,
            initialTitle = entry.title,
            initialTags = entry.tags,
            initialJournal = entry.journal,
            initialFavorite = entry.favorite,
            initialLocation = entry.location,
            initialWeather = entry.weather,
            initialMedia = entry.media,
            initialEntryAt = entry.createdAt,
            editing = true,
            onDismiss = { editing = null },
            onSubmit = { result -> onUpdate2(entry.id, result); editing = null },
            onDelete = { onDelete(entry.id); editing = null },
        )
    }

    answeringEntry?.let { entry ->
        var testimony by remember(entry.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { answeringEntry = null },
            title = { Text("What did God do?") },
            text = {
                Column {
                    Text(entry.body, color = Muted, fontSize = 13.sp, maxLines = 3)
                    OutlinedTextField(
                        testimony, { testimony = it },
                        placeholder = { Text("Write one line of testimony…") },
                        minLines = 3, modifier = Modifier.padding(top = 10.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (testimony.isNotBlank()) { onAnswerEntry(entry.id, testimony); answeringEntry = null }
                }) { Text("Mark answered") }
            },
            dismissButton = { TextButton(onClick = { answeringEntry = null }) { Text("Cancel") } },
        )
    }

    reviewingPrayer?.let { entry ->
        PrayerJourneyDialog(
            entry = entry,
            onDismiss = { reviewingPrayer = null },
            onSave = { stage, action, testimony ->
                onUpdatePrayerJourney(entry.id, stage, action, testimony)
                reviewingPrayer = null
            },
        )
    }

    answering?.let { word ->
        var testimony by remember(word.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { answering = null },
            title = { Text("What did God do?") },
            text = { OutlinedTextField(testimony, { testimony = it }, placeholder = { Text("Write one line of testimony…") }, minLines = 3) },
            confirmButton = { TextButton(onClick = { if (testimony.isNotBlank()) { onAnswered(word.id, testimony); answering = null } }) { Text("Mark answered") } },
            dismissButton = { TextButton(onClick = { answering = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun JournalTimeline(
    entries: List<JournalEntry>,
    streak: Int,
    query: String,
    onQuery: (String) -> Unit,
    onEdit: (JournalEntry) -> Unit,
    onAnswer: (JournalEntry) -> Unit,
    insight: JourneyInsight?,
    concealPreviews: Boolean,
) {
    var view by remember { mutableStateOf(JourneyView.Entries) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var searchOpen by remember { mutableStateOf(false) }
    val today = LocalDate.now().toEpochDay()
    val memory = remember(entries) {
        entries.firstOrNull { it.entryDay == today - 365 } ?: entries.firstOrNull { it.entryDay == today - 30 }
    }
    val shown = remember(entries, query, selectedTag) {
        entries.filter {
            (selectedTag == null || selectedTag in it.tags) && (query.isBlank() ||
            it.title.contains(query, true) || it.body.contains(query, true) || it.gratitude.contains(query, true) ||
                (it.verseRef ?: "").contains(query, true) || it.tags.any { tag -> tag.contains(query, true) } ||
                it.journal.contains(query, true) || it.location.contains(query, true))
        }
    }
    val grouped = remember(shown) { shown.groupBy { it.entryDay }.toSortedMap(compareByDescending { it }) }

    Column(Modifier.fillMaxSize()) {
        /* An empty journal should be an invitation, not a dashboard. No
           stats, no filters, no search until there is something to count,
           sort or find — just the line that makes someone want to write. */
        if (entries.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                JournalMark()
                Text(
                    "Your story starts here",
                    fontWeight = FontWeight.SemiBold, fontSize = 17.sp,
                    modifier = Modifier.padding(top = 22.dp),
                )
            }
            return
        }

        Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(16.dp), color = AppleGray, modifier = Modifier.weight(1f)) {
                Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    JourneyView.entries.forEach { option ->
                        Surface(onClick = { view = option; searchOpen = false }, shape = RoundedCornerShape(12.dp), color = if (view == option) Color.White else Color.Transparent, shadowElevation = if (view == option) 2.dp else 0.dp, modifier = Modifier.weight(1f)) {
                            Text(option.label, color = if (view == option) Ink else Muted, fontSize = 12.sp, fontWeight = if (view == option) FontWeight.SemiBold else FontWeight.Normal, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 10.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { searchOpen = !searchOpen }) { Icon(Icons.Outlined.Search, "Search journal", tint = if (searchOpen) Electric else Muted) }
        }

        if (searchOpen) OutlinedTextField(
            query, onQuery,
            placeholder = { Text("Search your journal…", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Muted) },
            singleLine = true, shape = R.control,
            modifier = Modifier.fillMaxWidth().padding(top = Space.block),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Electric, unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = AppleGray, unfocusedContainerColor = AppleGray,
            ),
        )
        val popularTags = remember(entries) { entries.flatMap { it.tags }.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.take(8).map { it.key } }
        if (searchOpen && popularTags.isNotEmpty()) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            PkChip("All", selectedTag == null) { selectedTag = null }
            popularTags.forEach { tag -> PkChip("#$tag", selectedTag == tag) { selectedTag = tag } }
        }

        when (view) {
        JourneyView.Entries -> LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = Space.block, bottom = Space.dock),
        ) {
            grouped.forEach { (day, dayEntries) ->
                item(key = "day-$day") {
                    Text(
                        dayLabel(day, today),
                        color = Muted, fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                items(dayEntries, key = { it.id }) { entry ->
                    EntryCard(entry, conceal = concealPreviews, onAnswer = { onAnswer(entry) }) { onEdit(entry) }
                }
            }
        }
        JourneyView.Calendar -> JournalCalendar(entries, concealPreviews, onEdit)
        JourneyView.Memories -> JournalMemories(entries, memory, insight, concealPreviews, onEdit, onAnswer)
        }
    }
}

@Composable
private fun JournalMemories(
    entries: List<JournalEntry>, onThisDay: JournalEntry?, insight: JourneyInsight?, conceal: Boolean,
    onEdit: (JournalEntry) -> Unit, onAnswer: (JournalEntry) -> Unit,
) {
    val meaningful = remember(entries, onThisDay) { entries.filter { it.favorite || it.media.isNotEmpty() || it.answeredAt != null }.filterNot { it.id == onThisDay?.id } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 16.dp, bottom = Space.dock), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        onThisDay?.let { memory -> item(key = "on-this-day") {
            Surface(onClick = { onEdit(memory) }, shape = R.card, color = Night, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("ON THIS DAY", color = Gold, fontSize = 9.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold)
                    Text(memory.title.ifBlank { "A page from your story" }, color = Color.White, fontFamily = FontFamily.Serif, fontSize = 21.sp, modifier = Modifier.padding(top = 8.dp))
                    Text(if (conceal) "Private entry · Tap to open" else memory.body, color = Color.White.copy(alpha = .72f), fontSize = 12.5.sp, lineHeight = 19.sp, maxLines = 3, modifier = Modifier.padding(top = 7.dp))
                }
            }
        } }
        insight?.let { item(key = "insight") { JourneyInsightCard(it) } }
        if (meaningful.isEmpty() && onThisDay == null && insight == null) item { Box(Modifier.fillParentMaxHeight(.65f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Favorites, answered prayers and attached memories will gather here.", color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center) } }
        items(meaningful, key = { "memory-${it.id}" }) { EntryCard(it, conceal, { onAnswer(it) }) { onEdit(it) } }
    }
}

@Composable
private fun JournalSubset(entries: List<JournalEntry>, empty: String, conceal: Boolean, onEdit: (JournalEntry) -> Unit, onAnswer: (JournalEntry) -> Unit) {
    if (entries.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(empty, color = Muted, fontSize = 13.sp) }
    else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 14.dp, bottom = Space.dock), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(entries, key = { it.id }) { EntryCard(it, conceal, { onAnswer(it) }) { onEdit(it) } }
    }
}

@Composable
private fun JournalCalendar(entries: List<JournalEntry>, conceal: Boolean, onEdit: (JournalEntry) -> Unit) {
    var month by remember { mutableStateOf(java.time.YearMonth.now()) }
    val byDay = remember(entries) { entries.groupBy { java.time.Instant.ofEpochMilli(it.createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate() } }
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value - 1
    val cells = List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    Column(Modifier.fillMaxSize().padding(top = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.Outlined.ChevronLeft, "Previous month") }
            Text("${month.month.name.lowercase().replaceFirstChar(Char::uppercase)} ${month.year}", fontFamily = FontFamily.Serif, fontSize = 19.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.Outlined.ChevronRight, "Next month") }
        }
        Row(Modifier.fillMaxWidth()) { listOf("M","T","W","T","F","S","S").forEach { Text(it, color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f).padding(vertical = 7.dp)) } }
        LazyVerticalGrid(GridCells.Fixed(7), modifier = Modifier.fillMaxWidth().height(300.dp), userScrollEnabled = false) {
            gridItems(cells) { date ->
                val dayEntries = date?.let { byDay[it] }.orEmpty()
                Box(Modifier.aspectRatio(1f).padding(2.dp).clip(RoundedCornerShape(11.dp)).background(if (dayEntries.isNotEmpty()) Gold.copy(alpha = .13f) else Color.Transparent).clickable(enabled = dayEntries.isNotEmpty()) { dayEntries.firstOrNull()?.let(onEdit) }, contentAlignment = Alignment.Center) {
                    if (date != null) Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(date.dayOfMonth.toString(), fontSize = 12.sp, fontWeight = if (dayEntries.isNotEmpty()) FontWeight.Bold else FontWeight.Normal); if (dayEntries.isNotEmpty()) Box(Modifier.padding(top = 3.dp).size(4.dp).background(Gold, CircleShape)) }
                }
            }
        }
        val monthEntries = entries.filter { java.time.Instant.ofEpochMilli(it.createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate().let { d -> d.year == month.year && d.monthValue == month.monthValue } }
        Text("${monthEntries.size} ${if (monthEntries.size == 1) "entry" else "entries"} this month", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        monthEntries.firstOrNull()?.let { EntryCard(it, conceal, {}) { onEdit(it) } }
    }
}

@Composable
private fun JourneyInsightCard(insight: JourneyInsight) {
    Box(
        Modifier.fillMaxWidth().padding(top = Space.block)
            .premiumCard(fill = NightFill).goldEdge(),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text(insight.eyebrow, color = Gold, fontSize = 9.sp, letterSpacing = 1.7.sp, fontWeight = FontWeight.Bold)
            Text(insight.title, color = Color.White, fontFamily = FontFamily.Serif, fontSize = 21.sp, modifier = Modifier.padding(top = 7.dp))
            Text(insight.body, color = Color(0xFFD8D5CC), fontSize = 12.5.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = .08f),
                modifier = Modifier.fillMaxWidth().padding(top = 13.dp),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("ONE NEXT STEP", color = Gold, fontSize = 8.5.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                    Text(insight.nextStep, color = Color.White, fontSize = 12.5.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
            Text("Based only on patterns in your private journey—not a claim about what God is saying.", color = Color.White.copy(alpha = .48f), fontSize = 9.5.sp, lineHeight = 14.sp, modifier = Modifier.padding(top = 10.dp))
        }
    }

}

@Composable
private fun PrayerJourneyDialog(
    entry: JournalEntry,
    onDismiss: () -> Unit,
    onSave: (PrayerStage, String, String) -> Unit,
) {
    var stage by remember(entry.id) { mutableStateOf(entry.prayerStage) }
    var nextAction by remember(entry.id) { mutableStateOf(entry.nextAction) }
    var testimony by remember(entry.id) { mutableStateOf(entry.testimony.orEmpty()) }
    val answeredStage = stage == PrayerStage.Answered || stage == PrayerStage.AnsweredDifferently
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Where is this prayer now?") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(entry.body, color = Muted, fontSize = 13.sp, maxLines = 3)
                PrayerStage.entries.forEach { option ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { stage = option }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = stage == option, onClick = { stage = option })
                        Text(option.label, fontSize = 13.sp)
                    }
                }
                OutlinedTextField(
                    nextAction, { nextAction = it },
                    label = { Text("One faithful next step (optional)") },
                    minLines = 2, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                if (answeredStage) OutlinedTextField(
                    testimony, { testimony = it },
                    label = { Text("What happened?") },
                    minLines = 3, modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(stage, nextAction, testimony) }) { Text("Save journey") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryDetailSheet(entry: JournalEntry, onDismiss: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    var showReflection by remember { mutableStateOf(false) }
    val reflection = remember(entry) { com.prayerkey.manna.data.JournalCoach.reflect(entry) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFFFFFCF4), sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 38.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(entry.journal.uppercase(), color = Gold, fontSize = 9.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
                    Text(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(Date(entry.createdAt)), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                }
                if (entry.favorite) Text("★", color = Gold, fontSize = 20.sp)
            }
            Text(entry.title.ifBlank { MOODS.firstOrNull { it.first == entry.mood }?.second ?: "Journal entry" }, fontFamily = FontFamily.Serif, fontSize = 28.sp, lineHeight = 34.sp, modifier = Modifier.padding(top = 18.dp))
            Text(entry.body, fontFamily = FontFamily.Serif, fontSize = 17.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 16.dp))
            if (entry.gratitude.isNotBlank()) Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFEAF5EC), modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) { Text("Grateful for · ${entry.gratitude}", color = Color(0xFF257345), fontSize = 13.sp, modifier = Modifier.padding(14.dp)) }
            entry.verseRef?.let { ref -> Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF4EDDC), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Column(Modifier.padding(14.dp)) { Text(ref, color = Gold, fontWeight = FontWeight.Bold); entry.verseText?.let { Text(it, fontFamily = FontFamily.Serif, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 6.dp)) } } } }
            if (entry.media.isNotEmpty()) {
                Text("ATTACHMENTS", color = Muted, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 7.dp))
                entry.media.forEachIndexed { index, value ->
                    Surface(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value)).apply { addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }) } }, shape = RoundedCornerShape(13.dp), color = Color.White, border = BorderStroke(1.dp, Hairline), modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text("▣", color = Gold); Text("  Attachment ${index + 1}", fontSize = 12.5.sp, modifier = Modifier.weight(1f)); Text("Open", color = Electric, fontSize = 11.sp) }
                    }
                }
            }
            if (entry.tags.isNotEmpty() || entry.location.isNotBlank() || entry.weather.isNotBlank()) Text(buildList { addAll(entry.tags.map { "#$it" }); if (entry.location.isNotBlank()) add("⌖ ${entry.location}"); if (entry.weather.isNotBlank()) add(entry.weather) }.joinToString("  "), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 14.dp))
            Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(onClick = { showReflection = true }, modifier = Modifier.weight(1f)) { Text("Go deeper") }
                OutlinedButton(onClick = {
                    val text = buildString { if (entry.title.isNotBlank()) appendLine(entry.title); appendLine(entry.body); if (entry.gratitude.isNotBlank()) appendLine("Grateful for: ${entry.gratitude}"); entry.verseRef?.let { appendLine(it) } }
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share this entry"))
                }, modifier = Modifier.weight(1f)) { Text("Share") }
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit entry") }
            }
        }
    }
    if (showReflection) AlertDialog(
        onDismissRequest = { showReflection = false },
        title = { Text("Reflect deeper", fontFamily = FontFamily.Serif) },
        text = { Column { Text("ON-DEVICE REFLECTION", color = Gold, fontSize = 9.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold); Text(reflection.suggestedTitle, fontFamily = FontFamily.Serif, fontSize = 20.sp, modifier = Modifier.padding(top = 7.dp)); Text(reflection.highlight, color = Muted, fontSize = 12.5.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp)); reflection.questions.forEach { Text("•  $it", fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 10.dp)) }; Text("Generated only from this entry on your device—not divine guidance.", color = Muted, fontSize = 9.5.sp, modifier = Modifier.padding(top = 14.dp)) } },
        confirmButton = { TextButton(onClick = { showReflection = false }) { Text("Done") } },
    )
}

private fun dayLabel(day: Long, today: Long): String = when (day) {
    today -> "TODAY"
    today - 1 -> "YESTERDAY"
    else -> DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(day * 86_400_000L)).uppercase()
}

@Composable
private fun StatDot() = Text("·", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 7.dp))

/**
 * Line-art open journal with a gold ribbon — drawn, not an emoji, so the
 * empty state matches the stroke weight of the dock icons instead of
 * dropping a system glyph into a custom design.
 */
@Composable
private fun JournalMark() {
    androidx.compose.foundation.Canvas(Modifier.size(66.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * .035f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val top = h * .22f
        val bottom = h * .80f
        val spine = w / 2f

        // the two leaves, curving away from a shared spine
        val leaves = Path().apply {
            moveTo(spine, top + h * .045f)
            cubicTo(spine - w * .13f, top - h * .04f, w * .16f, top, w * .07f, top + h * .03f)
            lineTo(w * .07f, bottom - h * .03f)
            cubicTo(w * .18f, bottom - h * .06f, spine - w * .12f, bottom - h * .015f, spine, bottom)
            cubicTo(spine + w * .12f, bottom - h * .015f, w * .82f, bottom - h * .06f, w * .93f, bottom - h * .03f)
            lineTo(w * .93f, top + h * .03f)
            cubicTo(w * .84f, top, spine + w * .13f, top - h * .04f, spine, top + h * .045f)
            close()
        }
        drawPath(leaves, Ink.copy(alpha = .82f), style = stroke)
        // the spine itself
        drawLine(Ink.copy(alpha = .82f), Offset(spine, top + h * .045f), Offset(spine, bottom), strokeWidth = w * .03f, cap = StrokeCap.Round)

        // ruled lines, shorter as they fall away — suggests writing
        listOf(.36f to .30f, .48f to .26f, .60f to .20f).forEach { (y, len) ->
            drawLine(Muted.copy(alpha = .5f), Offset(w * .18f, h * y), Offset(w * (.18f + len), h * y), strokeWidth = w * .022f, cap = StrokeCap.Round)
            drawLine(Muted.copy(alpha = .5f), Offset(w * .53f, h * y), Offset(w * (.53f + len), h * y), strokeWidth = w * .022f, cap = StrokeCap.Round)
        }

        // gold ribbon marker, the one warm note
        drawLine(Gold, Offset(spine, bottom - h * .02f), Offset(spine, h * .95f), strokeWidth = w * .045f, cap = StrokeCap.Round)
    }
}

@Composable
private fun EntryCard(entry: JournalEntry, conceal: Boolean = false, onAnswer: () -> Unit, onClick: () -> Unit) {
    val answered = entry.answeredAt != null
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (answered) Gold.copy(alpha = .35f) else Hairline),
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(if (answered) Gold else Color(0xFF8A7350), CircleShape))
                Spacer(Modifier.width(9.dp))
                Text(MOODS.firstOrNull { it.first == entry.mood }?.second ?: "Reflection", color = Muted, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .4.sp)
                // where this page came from, so a moment traces back to itself
                originGlyph(entry.source)?.let { glyph ->
                    Spacer(Modifier.width(7.dp))
                    Text(glyph, color = Muted, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                if (entry.favorite) Text("★", color = Gold, fontSize = 13.sp, modifier = Modifier.padding(end = 7.dp))
                Text(
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(entry.createdAt)),
                    color = Muted, fontSize = 10.sp,
                )
            }
            if (entry.title.isNotBlank() && !conceal) Text(entry.title, fontFamily = FontFamily.Serif, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
            Text(if (conceal) "Private entry · Tap to open" else entry.body, color = if (conceal) Muted else Ink, fontFamily = FontFamily.Serif, fontSize = 15.5.sp, lineHeight = 23.sp, maxLines = 5, modifier = Modifier.padding(top = 10.dp))
            if (entry.media.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(12.dp), color = Night.copy(alpha = .06f), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Text("▣  ${entry.media.size} ${if (entry.media.size == 1) "memory" else "memories"} attached", color = InkSoft, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(11.dp))
                }
            }
            if (entry.tags.isNotEmpty() || entry.location.isNotBlank() || entry.weather.isNotBlank()) {
                Text(buildList { addAll(entry.tags.map { "#$it" }); if (entry.location.isNotBlank()) add("⌖ ${entry.location}"); if (entry.weather.isNotBlank()) add(entry.weather) }.joinToString("  "), color = Muted, fontSize = 10.5.sp, modifier = Modifier.padding(top = 8.dp), maxLines = 2)
            }
            if (entry.gratitude.isNotBlank()) {
                Text("Grateful for: ${entry.gratitude}", color = Color(0xFF257345), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            entry.verseRef?.let { ref ->
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF4EDDC), modifier = Modifier.padding(top = 10.dp)) {
                    Text("📜 $ref", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }

            /* The lifecycle: a prayer waits, then carries a gold seal and the
               testimony forever. This is the proof pile no feed can copy. */
            if (answered) {
                Surface(
                    shape = RoundedCornerShape(12.dp), color = Gold.copy(alpha = .14f),
                    border = BorderStroke(1.dp, Gold.copy(alpha = .45f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("✦ GOD ANSWERED", color = Gold, fontSize = 9.5.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
                        entry.testimony?.let {
                            Text(it, fontFamily = FontFamily.Serif, fontSize = 13.5.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 5.dp))
                        }
                    }
                }
            } else if (entry.isPrayer) {
                Surface(
                    onClick = onAnswer,
                    shape = RoundedCornerShape(12.dp), color = Color.White,
                    border = BorderStroke(1.dp, Hairline),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.prayerStage.label, color = Muted, fontSize = 11.5.sp, modifier = Modifier.weight(1f))
                        Text("Review prayer", color = Electric, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (entry.nextAction.isNotBlank()) {
                    Text("Next: ${entry.nextAction}", color = InkSoft, fontSize = 11.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

/** Small glyph telling the reader how an entry began. */
private fun originGlyph(source: String): String? = when (source) {
    "sermon" -> "· from Sunday"
    "prayer" -> "· from a prayer"
    "verse" -> "· from a word"
    else -> null
}

@Composable
private fun EntryComposer(
    title: String,
    initialMood: String,
    initialBody: String,
    initialGratitude: String,
    todayCard: VerseCard,
    showAttach: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var mood by remember { mutableStateOf(initialMood) }
    var body by remember { mutableStateOf(initialBody) }
    var gratitude by remember { mutableStateOf(initialGratitude) }
    var attach by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("How is your heart?", color = Muted, fontSize = 12.sp)
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MOODS.forEach { (emoji, _) ->
                        Box(
                            Modifier.size(38.dp).clip(CircleShape)
                                .background(if (mood == emoji) Color(0xFFE7EDFF) else Color.Transparent)
                                .clickable { mood = emoji },
                            contentAlignment = Alignment.Center,
                        ) { Text(emoji, fontSize = 19.sp) }
                    }
                }
                OutlinedTextField(
                    body, { body = it },
                    placeholder = { Text("What's on your heart today?") },
                    minLines = 4, modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                OutlinedTextField(
                    gratitude, { gratitude = it },
                    placeholder = { Text("One thing you're grateful for…") },
                    singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
                if (showAttach) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(12.dp))
                            .clickable { attach = !attach },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(attach, { attach = it })
                        Text("Attach today's word (${todayCard.reference})", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (body.isNotBlank()) onSubmit(mood, body, gratitude, attach) }) { Text("Save") }
        },
        dismissButton = {
            Row {
                onDelete?.let {
                    TextButton(onClick = it) {
                        Icon(Icons.Outlined.Delete, null, tint = Color(0xFFB3402A), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("Delete", color = Color(0xFFB3402A))
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

/** Everything God answered, whichever screen it was asked on. */
@Composable
private fun AnsweredList(entries: List<JournalEntry>, words: List<SavedWord>, onEdit: (JournalEntry) -> Unit) {
    if (entries.isEmpty() && words.isEmpty()) {
        Column(Modifier.fillMaxWidth().padding(top = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nothing marked answered yet", fontWeight = FontWeight.SemiBold)
        }
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 130.dp),
    ) {
        items(entries, key = { "entry-${it.id}" }) { entry ->
            EntryCard(entry, onAnswer = {}) { onEdit(entry) }
        }
        items(words, key = { "word-${it.id}" }) { word ->
            Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFF1F8F3), border = BorderStroke(1.dp, Hairline)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row { Text(word.reference, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(word.translation, color = Muted, fontSize = 11.sp) }
                    Text(word.verse, fontFamily = FontFamily.Serif, fontSize = 20.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 12.dp))
                    word.testimony?.let { Text("“$it”", color = Color(0xFF257345), modifier = Modifier.padding(top = 15.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WordList(words: List<SavedWord>, canAnswer: Boolean, onAnswer: (SavedWord) -> Unit) {
    if (words.isEmpty()) {
        Column(Modifier.fillMaxWidth().padding(top = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (canAnswer) "🗂" else "🏆", fontSize = 44.sp)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        items(words, key = { it.id }) { word ->
            Surface(shape = RoundedCornerShape(22.dp), color = if (word.answeredAt == null) Ivory else Color(0xFFF1F8F3), border = BorderStroke(1.dp, Hairline)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row { Text(word.reference, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(word.translation, color = Muted, fontSize = 11.sp) }
                    Text(word.verse, fontFamily = FontFamily.Serif, fontSize = 20.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 12.dp))
                    word.testimony?.let { Text("“$it”", color = Color(0xFF257345), modifier = Modifier.padding(top = 15.dp)) }
                    Text(DateFormat.getDateInstance().format(Date(word.answeredAt ?: word.savedAt)), color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 10.dp))
                    if (canAnswer) TextButton(onClick = { onAnswer(word) }, modifier = Modifier.align(Alignment.End)) { Text("Mark answered") }
                }
            }
        }
    }
}
