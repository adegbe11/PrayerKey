package com.prayerkey.manna.ui.screens

import com.prayerkey.manna.platform.localDateOfEpochDay
import com.prayerkey.manna.platform.withDayOfMonth
import com.prayerkey.manna.ui.platform.openAttachment
import com.prayerkey.manna.ui.platform.loadThumbnail
import com.prayerkey.manna.ui.platform.mimeTypeOf
import com.prayerkey.manna.ui.platform.shareText
import com.prayerkey.manna.ui.platform.rememberDeviceUnlock
import com.prayerkey.manna.ui.platform.SecureScreen
import com.prayerkey.manna.ui.platform.fullScreenDialog
import kotlinx.datetime.isoDayNumber
import com.prayerkey.manna.platform.lengthOfMonth
import com.prayerkey.manna.platform.minusMonths
import com.prayerkey.manna.platform.plusMonths
import com.prayerkey.manna.data.mannaStore
import com.prayerkey.manna.platform.longDateShortTime
import com.prayerkey.manna.platform.shortTime
import com.prayerkey.manna.platform.mediumDate
import com.prayerkey.manna.platform.format
import com.prayerkey.manna.ui.share.Share
import com.prayerkey.manna.platform.now
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.resources.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.foundation.layout.aspectRatio
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Lightbulb
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.Modifier
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
import kotlinx.datetime.LocalDate

private data class JournalMood(val key: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private val MOODS = listOf(
    JournalMood("praying", "Praying", Icons.Outlined.SelfImprovement),
    JournalMood("grateful", "Grateful", Icons.Outlined.Favorite),
    JournalMood("hopeful", "Hopeful", Icons.Outlined.WbSunny),
    JournalMood("faith", "Faith", Icons.Outlined.LocalFireDepartment),
    JournalMood("heavy", "Heavy", Icons.Outlined.Cloud),
    JournalMood("broken", "Broken", Icons.Outlined.WaterDrop),
)

private fun moodLabel(raw: String): String = MOODS.firstOrNull { it.key == raw }?.label ?: when (raw) {
    "🙏" -> "Praying"; "😊" -> "Grateful"; "✨" -> "Hopeful"
    "🔥" -> "Faith"; "😔" -> "Heavy"; "😭" -> "Broken"
    else -> "Reflection"
}

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
    val context = platformContext()
    SecureScreen(journalLocked)
    var unlocked by remember(journalLocked) { mutableStateOf(!journalLocked) }
    val unlock = rememberDeviceUnlock("Your journal is protected by your device lock") { ok -> if (ok) unlocked = true }
    if (!unlocked) {
        Box(Modifier.fillMaxSize().background(dayWash()), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                JournalMark()
                Text("Your Journal is private", fontFamily = BookSerif, fontSize = 24.sp, modifier = Modifier.padding(top = 18.dp))
                Button(onClick = {
                    // a phone with no lock set has nothing to unlock with, so the journal opens
                    if (unlock.available) unlock.unlock() else unlocked = true
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
    // + opens a blank page; templates and prompts have their own doors on the
    // journal home. `chosen` holds a prompt when one was picked there.
    var writing by remember { mutableStateOf(false) }
    /* Which of Voice, Photo or Bible opened the page, so the sheet can do that
       thing as it appears rather than showing a blank page with the tool
       hidden in a bar at the bottom. */
    var quickStart by remember { mutableStateOf<String?>(null) }
    /* The template library, its preview, and what the sheet was seeded with. */
    var browsingTemplates by remember { mutableStateOf(false) }
    var previewing by remember { mutableStateOf<com.prayerkey.manna.data.JournalTemplate?>(null) }
    var reading by remember { mutableStateOf<com.prayerkey.manna.data.JournalTemplate?>(null) }
    var seeded by remember { mutableStateOf<com.prayerkey.manna.data.JournalTemplate?>(null) }
    val templatePrefs = remember { context.keyValues("journal_templates") }
    var recentTemplates by remember {
        mutableStateOf(templatePrefs.getString("recent", "").orEmpty().split(",").filter { it.isNotBlank() })
    }
    var chosen by remember { mutableStateOf<com.prayerkey.manna.ui.journal.JournalSuggestions.Prompt?>(null) }
    var answeringEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var reviewingPrayer by remember { mutableStateOf<JournalEntry?>(null) }
    var libraryOpen by remember { mutableStateOf(false) }
    var promptLibraryOpen by remember { mutableStateOf(false) }
    /* Screens here are separate windows. Closing one and opening the next in
       the same frame let the journal home flash between them, so the new one
       is laid on top first and the old one leaves once it is covered. */
    val handoff = rememberCoroutineScope()

    BackHandler(enabled = tab != JournalTab.Journey) {
        tab = JournalTab.Journey
    }

    Box(Modifier.fillMaxSize().background(dayWash())) {
        /* An empty journal is the whole screen, edge to edge — the sky the
           user chose, with the header floating on top of it. Rendering it
           inside the padded column left a white band under the title and
           margins down both sides, which broke the illusion entirely. */
        // The Journal home must be useful on the first launch. The previous
        // five-second verse interlude hid writing and collections from a new
        // user; the premium overview below now handles both empty and active
        // journals immediately.
        val emptyJourney = false

        // below the status bar: the title was drawn under the clock
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 22.dp).padding(top = 16.dp)) {
            // Home is bare now, so Settings lives here — the one screen
            // that is already about the user rather than today's word.
            /* Over the full-bleed welcome sky the chrome has to switch to
               the theme's ink, or a dark title sits on a dark sky. */
            val headerInk = if (emptyJourney) MaterialTheme.colorScheme.onBackground else Ink
            val headerMuted = if (emptyJourney) MaterialTheme.colorScheme.onBackground.copy(alpha = .55f) else Muted
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (tab != JournalTab.Journey) IconButton(onClick = { tab = JournalTab.Journey }) { Icon(Icons.Outlined.ArrowBack, "Back to journal") }
                Column(Modifier.weight(1f)) {
                    Text(when (tab) { JournalTab.Journey -> "Journal"; JournalTab.Practices -> "Practices"; JournalTab.Saved -> "Saved words"; JournalTab.Answered -> "Answered prayers" }, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = headerInk)
                }
                if (tab == JournalTab.Journey) Box {
                    IconButton(onClick = { libraryOpen = true }) { Icon(Icons.Outlined.MoreHoriz, "Open journal library", tint = headerMuted) }
                    DropdownMenu(expanded = libraryOpen, onDismissRequest = { libraryOpen = false }) {
                        DropdownMenuItem(text = { Text("Practices") }, onClick = { tab = JournalTab.Practices; libraryOpen = false })
                        DropdownMenuItem(text = { Text("Saved words · ${words.count { it.answeredAt == null }}") }, onClick = { tab = JournalTab.Saved; libraryOpen = false })
                        DropdownMenuItem(text = { Text("Answered prayers · ${words.count { it.answeredAt != null } + entries.count { it.answeredAt != null }}") }, onClick = { tab = JournalTab.Answered; libraryOpen = false })
                    }
                }
                androidx.compose.material3.IconButton(onClick = onProfile) {
                    Icon(Icons.Outlined.Settings, "Settings", tint = headerMuted)
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
                    onWelcomeTap = { chosen = null; writing = true },
                    onQuickStart = { key ->
                        chosen = null
                        if (key == "template") browsingTemplates = true
                        else if (key == "prompt") promptLibraryOpen = true
                        else { quickStart = key; writing = true }
                    },
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
            /* Writing is the one primary action. Treat it like the clasp on a
               private journal: substantial, quiet, and always in the same
               place. Pulsing rings made the action look promotional and
               introduced the neon language this app deliberately avoids. */
            val accent = MaterialTheme.colorScheme.primary
            Box(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(58.dp)
                        .shadow(5.dp, CircleShape, spotColor = Color.Black.copy(alpha = .16f))
                        .clip(CircleShape).background(accent)
                        .clickable { chosen = null; writing = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Add, "Write an entry",
                        tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }

    if (promptLibraryOpen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { promptLibraryOpen = false },
            properties = fullScreenDialog(),
        ) {
            com.prayerkey.manna.ui.journal.PromptLibrary(
                onClose = { promptLibraryOpen = false },
                onPrompt = { question, pack ->
                    chosen = com.prayerkey.manna.ui.journal.JournalSuggestions.Prompt(pack.uppercase(), question, "prompt", suggestPrayer = pack in setOf("My Requests", "Intercession", "Family Prayer", "Waiting on God", "Spiritual Warfare"))
                    writing = true
                    handoff.launch { kotlinx.coroutines.delay(320); promptLibraryOpen = false }
                },
            )
        }
    }

    if (writing) com.prayerkey.manna.ui.journal.WriteSheet(
        prompt = chosen,
        todayCard = todayCard,
        openWith = quickStart,
        initialTitle = seeded?.title.orEmpty(),
        initialBody = seeded?.let { com.prayerkey.manna.data.starterFor(it) }.orEmpty(),
        initialJournal = seeded?.journal ?: "My Journey",
        initialIsPrayer = seeded?.prayer == true,
        onTemplates = { quickStart = null; browsingTemplates = true },
        onPrompts = { quickStart = null; promptLibraryOpen = true },
        onDismiss = { writing = false; chosen = null; quickStart = null; seeded = null },
        onSubmit = { result -> onAdd2(result); writing = false; chosen = null; quickStart = null; seeded = null },
    )

    if (browsingTemplates) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { browsingTemplates = false },
            properties = fullScreenDialog(),
        ) {
            com.prayerkey.manna.ui.journal.TemplateGallery(
                recent = recentTemplates,
                onPick = { previewing = it },
                onClose = { browsingTemplates = false },
            )
        }
    }

    previewing?.let { template ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { previewing = null },
            properties = fullScreenDialog(),
        ) {
            com.prayerkey.manna.ui.journal.TemplatePreview(
                template = template,
                onUse = {
                    /* Most recent first, no duplicates, six deep - a recents
                       list long enough to be useful and short enough that the
                       gallery still opens on the collections. */
                    recentTemplates = (listOf(it.id) + recentTemplates).distinct().take(6)
                    templatePrefs.edit().putString("recent", recentTemplates.joinToString(",")).apply()
                    /* Into the book, not the editor. A template is a run of
                       pages you turn; handing its questions to a scrolling form
                       was the thing that made it a questionnaire. */
                    reading = it
                    handoff.launch { kotlinx.coroutines.delay(320); previewing = null; browsingTemplates = false }
                },
                onClose = { previewing = null },
            )
        }
    }

    reading?.let { template ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { reading = null },
            properties = fullScreenDialog(),
        ) {
            com.prayerkey.manna.ui.journal.TemplateSheet(
                template = template,
                onClose = { reading = null },
                onFinish = { title, body, journalName, isPrayer ->
                    onAdd2(
                        com.prayerkey.manna.ui.journal.WriteResult(
                            mood = "\uD83D\uDE4F",
                            body = body,
                            gratitude = "",
                            verseRef = null,
                            verseText = null,
                            source = "write",
                            isPrayer = isPrayer,
                            title = title,
                            journal = journalName,
                        ),
                    )
                    reading = null
                    writing = false; chosen = null; quickStart = null; seeded = null
                },
            )
        }
    }

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
    onWelcomeTap: () -> Unit,
    onQuickStart: (String) -> Unit,
    onAnswer: (JournalEntry) -> Unit,
    insight: JourneyInsight?,
    concealPreviews: Boolean,
) {
    var view by remember { mutableStateOf(JourneyView.Entries) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var selectedCollection by remember { mutableStateOf<String?>(null) }
    var searchOpen by remember { mutableStateOf(false) }
    /* A collection filters the pages below the overview, off the bottom of
       the screen - so the tap scrolls down to them, or it looks like nothing
       happened. Back clears a filter or a search before it leaves Journal. */
    val listTop = remember { androidx.compose.foundation.relocation.BringIntoViewRequester() }
    val reveal = rememberCoroutineScope()
    BackHandler(enabled = selectedCollection != null || searchOpen) {
        selectedCollection = null
        searchOpen = false
    }
    val today = today().toEpochDays().toLong()
    val memory = remember(entries) {
        entries.firstOrNull { it.entryDay == today - 365 } ?: entries.firstOrNull { it.entryDay == today - 30 }
    }
    val shown = remember(entries, query, selectedTag, selectedCollection) {
        entries.filter {
            val inCollection = when (selectedCollection) {
                "prayers" -> it.isPrayer
                "answered" -> it.answeredAt != null
                "thanksgiving" -> it.gratitude.isNotBlank() || it.journal.equals("Gratitude", true)
                "dreams" -> it.journal.equals("Dreams", true)
                "sermon notes" -> it.source == "sermon"
                else -> true
            }
            inCollection && (selectedTag == null || selectedTag in it.tags) && (query.isBlank() ||
            it.title.contains(query, true) || it.body.contains(query, true) || it.gratitude.contains(query, true) ||
                (it.verseRef ?: "").contains(query, true) || it.tags.any { tag -> tag.contains(query, true) } ||
                it.journal.contains(query, true) || it.location.contains(query, true))
        }
    }
    val grouped = remember(shown) { shown.groupBy { it.entryDay }.entries.sortedByDescending { it.key }.associate { it.key to it.value } }

    /* One scroll for the whole page. The overview used to sit above a list
       with its own scroll, filled the screen, and left the list no height at
       all: nothing under Continue writing could be reached. */
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = Space.dock)) {
        /* An empty journal should be an invitation, not a dashboard. No
           stats, no filters, no search until there is something to count,
           sort or find — just the line that makes someone want to write. */
        // the empty state is drawn full-bleed by JournalScreen, behind the
        // header — the timeline just stands down
        JournalOverview(
            entries = entries,
            streak = streak,
            memory = memory,
            onWrite = onWelcomeTap,
            onOpen = onEdit,
            onCollection = { collection ->
                selectedCollection = collection
                reveal.launch { kotlinx.coroutines.delay(60); listTop.bringIntoView(androidx.compose.ui.geometry.Rect(0f, 0f, 1f, 4000f)) }
            },
            onQuickStart = onQuickStart,
        )

        if (entries.isEmpty()) return

        selectedCollection?.let { collection ->
            Surface(
                onClick = { selectedCollection = null },
                shape = RoundedCornerShape(99.dp),
                color = Pk.Oxblood.copy(alpha = .08f),
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Row(Modifier.padding(horizontal = 13.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(collection.replaceFirstChar(Char::uppercase), color = Pk.Oxblood, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Outlined.Close, "Show all pages", tint = Pk.Oxblood, modifier = Modifier.padding(start = 7.dp).size(14.dp))
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 18.dp)
                .bringIntoViewRequester(listTop),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
            singleLine = true, shape = Corners.control,
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
        JourneyView.Entries -> Column(
            Modifier.padding(top = Space.block),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            grouped.forEach { (day, dayEntries) ->
                key("day-$day") {
                    Text(
                        dayLabel(day, today),
                        color = Muted, fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                dayEntries.forEach { entry ->
                    key(entry.id) {
                        EntryCard(entry, conceal = concealPreviews, onAnswer = { onAnswer(entry) }) { onEdit(entry) }
                    }
                }
            }
        }
        JourneyView.Calendar -> JournalCalendar(entries, concealPreviews, onEdit)
        JourneyView.Memories -> JournalMemories(entries, memory, insight, concealPreviews, onEdit, onAnswer)
        }
    }
}

private data class JournalCollection(val title: String, val count: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector, val tint: Color)

@Composable
private fun JournalOverview(
    entries: List<JournalEntry>, streak: Int, memory: JournalEntry?,
    onWrite: () -> Unit, onOpen: (JournalEntry) -> Unit, onCollection: (String?) -> Unit,
    onQuickStart: (String) -> Unit,
) {
    val violet = Pk.Oxblood
    val ink = Pk.Charcoal
    val muted = Pk.Muted
    val latest = entries.maxByOrNull { it.updatedAt }
    val collections = listOf(
        JournalCollection("Prayers", entries.count { it.isPrayer }, Icons.Outlined.FavoriteBorder, violet),
        JournalCollection("Answered", entries.count { it.answeredAt != null }, Icons.Outlined.CheckCircleOutline, Color(0xFF237A57)),
        JournalCollection("Thanksgiving", entries.count { it.gratitude.isNotBlank() || it.journal.equals("Gratitude", true) }, Icons.Outlined.Celebration, Color(0xFFB56B00)),
        JournalCollection("Dreams", entries.count { it.journal.equals("Dreams", true) }, Icons.Outlined.Nightlight, Color(0xFF4C4FC7)),
        JournalCollection("Sermon notes", entries.count { it.source == "sermon" }, Icons.Outlined.GraphicEq, Color(0xFF8A3D72)),
        JournalCollection("All pages", entries.size, Icons.Outlined.AutoStories, ink),
    )
    val today = remember { today() }
    Column(Modifier.padding(top = 18.dp)) {
        /*
         * The hero carries the artwork and the day, not a flat violet block.
         *
         * It also asks a different question each day rather than the same one
         * forever - a journal that greets you identically on day one and day
         * two hundred is a form, and the point of the question is that it is
         * worth answering today.
         */
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                .background(Pk.DeepDark).clickable(onClick = onWrite),
        ) {
            Image(
                painterResource(Res.drawable.today_manuscript_hero),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
            Box(Modifier.matchParentSize().background(Pk.DeepDark.copy(alpha = .62f)))
            Column(Modifier.padding(24.dp)) {
                Text(
                    "TODAY" + "’" + "S JOURNAL",
                    color = Color.White.copy(alpha = .72f), fontFamily = UtilitySans,
                    fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.5.sp,
                )
                Text(
                    today.format("EEE, MMM d"),
                    Modifier.padding(top = 4.dp),
                    color = Color.White.copy(alpha = .60f), fontFamily = UtilitySans, fontSize = 11.5.sp,
                )
                Text(
                    HOME_QUESTIONS[(today.toEpochDays().toLong()).mod(HOME_QUESTIONS.size.toLong()).toInt()],
                    color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                    fontSize = 28.sp, lineHeight = 34.sp, modifier = Modifier.padding(top = 14.dp),
                )
                /* The + at the foot of the screen writes a page; a second button here
                   saying the same thing was one control too many. The card still opens
                   a page when tapped. */
            }
        }

        /* Quick entry points stay intentionally limited to four: capture a
           thought, attach a photo, open a guided template, or choose a prompt. */
        /* Four ways in. Each opens the page already doing the thing it names -
           the picker open, the dictation notice up, today's verse attached -
           because three buttons that all land on the same blank page are three
           of the same button. */
        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf(
                Triple("Voice", Icons.Outlined.Mic, "voice"),
                Triple("Photo", Icons.Outlined.Image, "media"),
                Triple("Template", Icons.Outlined.AutoStories, "template"),
                Triple("Prompt", Icons.Outlined.Lightbulb, "prompt"),
            ).forEach { (label, icon, key) ->
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                        .background(violet.copy(alpha = .07f))
                        .clickable { onQuickStart(key) }
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(icon, null, tint = violet, modifier = Modifier.size(21.dp))
                    Text(
                        label, Modifier.padding(top = 7.dp), color = ink,
                        fontFamily = UtilitySans, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        /* Three tiles reading 0, 0 and 0 on a new journal were the loudest
           thing on the page and said nothing. They appear once there is a page
           to count. */
        if (entries.isNotEmpty()) Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(Modifier.weight(1f), shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, violet.copy(alpha = .10f))) {
                Column(Modifier.padding(16.dp)) { Text(entries.size.toString(), color = ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 24.sp); Text("Pages", color = muted, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
            Surface(Modifier.weight(1f), shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, violet.copy(alpha = .10f))) {
                Column(Modifier.padding(16.dp)) { Text(streak.toString(), color = ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 24.sp); Text("Day streak", color = muted, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
            Surface(Modifier.weight(1f), shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, violet.copy(alpha = .10f))) {
                Column(Modifier.padding(16.dp)) { Text(entries.count { it.favorite }.toString(), color = ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 24.sp); Text("Favourites", color = muted, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        }

        /* A shelf of collections that all read "0 pages" is a table of contents
           for a book nobody has written yet. Each appears when it has a page. */
        val filled = collections.filter { it.count > 0 }
        if (filled.isNotEmpty()) Text("Your prayer journey", color = ink, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 25.dp, bottom = 11.dp))
        if (filled.isNotEmpty()) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            filled.forEach { item ->
                Surface(
                    Modifier.width(142.dp).height(112.dp).clickable { onCollection(if (item.title == "All pages") null else item.title.lowercase()) },
                    shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, item.tint.copy(alpha = .12f)),
                ) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Icon(item.icon, null, tint = item.tint, modifier = Modifier.size(22.dp))
                        Column { Text(item.title, color = ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1); Text("${item.count} ${if (item.count == 1) "page" else "pages"}", color = muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp)) }
                    }
                }
            }
        }

        latest?.let { entry ->
            Text("Continue writing", color = ink, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 25.dp, bottom = 11.dp))
            Surface(Modifier.fillMaxWidth().clickable { onOpen(entry) }, shape = RoundedCornerShape(22.dp), color = Color.White, border = BorderStroke(1.dp, violet.copy(alpha = .12f))) {
                Column(Modifier.padding(18.dp)) {
                    Text(entry.title.ifBlank { "Your latest page" }, color = ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    Text(entry.body, color = muted, fontFamily = BookSerif, fontSize = 13.sp, lineHeight = 20.sp, maxLines = 2, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        memory?.let { entry ->
            Text("On this day", color = ink, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(top = 25.dp, bottom = 11.dp))
            Surface(Modifier.fillMaxWidth().clickable { onOpen(entry) }, shape = RoundedCornerShape(22.dp), color = ink) {
                Column(Modifier.padding(20.dp)) { Text(entry.title.ifBlank { "A page from your story" }, color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 20.sp); Text(entry.body, color = Color.White.copy(alpha = .70f), fontFamily = BookSerif, fontSize = 13.sp, lineHeight = 20.sp, maxLines = 3, modifier = Modifier.padding(top = 7.dp)) }
            }
        }
    }
}

@Composable
private fun JournalMemories(
    entries: List<JournalEntry>, onThisDay: JournalEntry?, insight: JourneyInsight?, conceal: Boolean,
    onEdit: (JournalEntry) -> Unit, onAnswer: (JournalEntry) -> Unit,
) {
    val meaningful = remember(entries, onThisDay) { entries.filter { it.favorite || it.media.isNotEmpty() || it.answeredAt != null }.filterNot { it.id == onThisDay?.id } }
    Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        onThisDay?.let { memory -> run {
            Surface(onClick = { onEdit(memory) }, shape = Corners.card, color = Night, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("On this day", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(memory.title.ifBlank { "A page from your story" }, color = Color.White, fontFamily = BookSerif, fontSize = 21.sp, modifier = Modifier.padding(top = 8.dp))
                    Text(if (conceal) "Private entry · Tap to open" else memory.body, color = Color.White.copy(alpha = .72f), fontSize = 12.5.sp, lineHeight = 19.sp, maxLines = 3, modifier = Modifier.padding(top = 7.dp))
                }
            }
        } }
        insight?.let { JourneyInsightCard(it) }
        if (meaningful.isEmpty() && onThisDay == null && insight == null) Box(Modifier.fillMaxWidth().padding(vertical = 80.dp), contentAlignment = Alignment.Center) { Text("No saved moments", color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center) }
        meaningful.forEach { key("memory-${it.id}") { EntryCard(it, conceal, { onAnswer(it) }) { onEdit(it) } } }
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
    // the month shown, as its first day
    var month by remember { mutableStateOf(today().withDayOfMonth(1)) }
    val byDay = remember(entries) { entries.groupBy { com.prayerkey.manna.platform.localDateOfMillis(it.createdAt) } }
    val first = month
    val leading = first.dayOfWeek.isoDayNumber - 1
    val cells = List(leading) { null } + (1..month.lengthOfMonth()).map { month.withDayOfMonth(it) }
    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.Outlined.ChevronLeft, "Previous month") }
            Text("${month.month.name.lowercase().replaceFirstChar(Char::uppercase)} ${month.year}", fontFamily = BookSerif, fontSize = 19.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
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
        val monthEntries = entries.filter { com.prayerkey.manna.platform.localDateOfMillis(it.createdAt).let { d -> d.year == month.year && d.monthNumber == month.monthNumber } }
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
            Text(insight.title, color = Color.White, fontFamily = BookSerif, fontSize = 21.sp, modifier = Modifier.padding(top = 7.dp))
            Text(insight.body, color = Color(0xFFD8D5CC), fontSize = 12.5.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = .08f),
                modifier = Modifier.fillMaxWidth().padding(top = 13.dp),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("One next step", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(insight.nextStep, color = Color.White, fontSize = 12.5.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
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
    val context = platformContext()
    var showReflection by remember { mutableStateOf(false) }
    val reflection = remember(entry) { com.prayerkey.manna.data.JournalCoach.reflect(entry) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFFFFFCF4), sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 38.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(entry.journal, color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(longDateShortTime(entry.createdAt), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                }
                if (entry.favorite) Icon(Icons.Outlined.Favorite, "Favourite", tint = Gold, modifier = Modifier.size(20.dp))
            }
            Text(entry.title.ifBlank { moodLabel(entry.mood) }, fontFamily = BookSerif, fontSize = 28.sp, lineHeight = 34.sp, modifier = Modifier.padding(top = 18.dp))
            Text(entry.body, fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 16.dp))
            if (entry.gratitude.isNotBlank()) Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFEAF5EC), modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) { Text("Grateful for · ${entry.gratitude}", color = Color(0xFF257345), fontSize = 13.sp, modifier = Modifier.padding(14.dp)) }
            entry.verseRef?.let { ref -> Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFF4EDDC), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Column(Modifier.padding(14.dp)) { Text(ref, color = Gold, fontWeight = FontWeight.Bold); entry.verseText?.let { Text(it, fontFamily = BookSerif, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 6.dp)) } } } }
            if (entry.media.isNotEmpty()) {
                /* Photos and videos are shown, not listed. They were rows reading
                   "Attachment 1 - Open", which asked someone to leave the page to
                   see the picture they had put on it. */
                Column(Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    entry.media.forEach { value -> AttachmentView(value) }
                }
            }
            if (entry.tags.isNotEmpty() || entry.location.isNotBlank() || entry.weather.isNotBlank()) Text(buildList { addAll(entry.tags.map { "#$it" }); if (entry.location.isNotBlank()) add("⌖ ${entry.location}"); if (entry.weather.isNotBlank()) add(entry.weather) }.joinToString("  "), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 14.dp))
            Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(onClick = { showReflection = true }, modifier = Modifier.weight(1f)) { Text("Reflect") }
                OutlinedButton(onClick = {
                    val text = buildString { if (entry.title.isNotBlank()) appendLine(entry.title); appendLine(entry.body); if (entry.gratitude.isNotBlank()) appendLine("Grateful for: ${entry.gratitude}"); entry.verseRef?.let { appendLine(it) } }
                    shareText(context, text, "Share this entry")
                }, modifier = Modifier.weight(1f)) { Text("Share") }
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit entry") }
            }
        }
    }
    if (showReflection) AlertDialog(
        onDismissRequest = { showReflection = false },
        title = { Text("Reflect deeper", fontFamily = BookSerif) },
        text = { Column { Text("Private reflection", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(reflection.suggestedTitle, fontFamily = BookSerif, fontSize = 20.sp, modifier = Modifier.padding(top = 7.dp)); Text(reflection.highlight, color = Muted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp)); reflection.questions.forEach { Text("•  $it", fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 10.dp)) } } },
        confirmButton = { TextButton(onClick = { showReflection = false }) { Text("Done") } },
    )
}

private fun dayLabel(day: Long, today: Long): String = when (day) {
    today -> "TODAY"
    today - 1 -> "YESTERDAY"
    else -> localDateOfEpochDay(day).format("MMM d, yyyy").uppercase()
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
    val context = platformContext()
    val prayerStore = remember { context.mannaStore() }
    var prayedCount by remember(entry.id) {
        mutableIntStateOf(prayerStore.prayerSessions(500).count { it.challenge == "Prayer ledger" && it.focus == entry.body })
    }
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
                Text(moodLabel(entry.mood), color = Muted, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .4.sp)
                // where this page came from, so a moment traces back to itself
                originGlyph(entry.source)?.let { glyph ->
                    Spacer(Modifier.width(7.dp))
                    Text(glyph, color = Muted, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                if (entry.favorite) Text("★", color = Gold, fontSize = 13.sp, modifier = Modifier.padding(end = 7.dp))
                Text(
                    shortTime(entry.createdAt),
                    color = Muted, fontSize = 10.sp,
                )
            }
            if (entry.title.isNotBlank() && !conceal) Text(entry.title, fontFamily = BookSerif, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
            Text(if (conceal) "Private entry · Tap to open" else entry.body, color = if (conceal) Muted else Ink, fontFamily = BookSerif, fontSize = 15.5.sp, lineHeight = 23.sp, maxLines = 5, modifier = Modifier.padding(top = 10.dp))
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
                            Text(it, fontFamily = BookSerif, fontSize = 13.5.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 5.dp))
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
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        prayerStore.logPrayerSession("Prayer ledger", entry.body, 0, 1)
                        prayedCount++
                    }) {
                        Text(if (prayedCount == 0) "Prayed" else "Prayed · $prayedCount", color = Electric, fontWeight = FontWeight.SemiBold)
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
                    MOODS.forEach { option ->
                        Box(
                            Modifier.size(38.dp).clip(CircleShape)
                                .background(if (mood == option.key) MaterialTheme.colorScheme.primary.copy(alpha = .10f) else Color.Transparent)
                                .clickable { mood = option.key },
                            contentAlignment = Alignment.Center,
                        ) { Icon(option.icon, option.label, tint = if (mood == option.key) MaterialTheme.colorScheme.primary else Muted, modifier = Modifier.size(20.dp)) }
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
    val context = platformContext()
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
            Column {
                EntryCard(entry, onAnswer = {}) { onEdit(entry) }
                TextButton(
                    onClick = { Share.cards.shareAnswered(context, entry.body, entry.testimony.orEmpty(), entry.answeredAt ?: entry.updatedAt) },
                    modifier = Modifier.align(Alignment.End),
                ) { Text("Share answer") }
            }
        }
        items(words, key = { "word-${it.id}" }) { word ->
            Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFFF1F8F3), border = BorderStroke(1.dp, Hairline)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row { Text(word.reference, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(word.translation, color = Muted, fontSize = 11.sp) }
                    Text(word.verse, fontFamily = BookSerif, fontSize = 20.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 12.dp))
                    word.testimony?.let { Text(it, color = Color(0xFF257345), modifier = Modifier.padding(top = 15.dp)) }
                    TextButton(
                        onClick = { Share.cards.shareAnswered(context, word.verse, word.testimony.orEmpty(), word.answeredAt ?: word.savedAt) },
                        modifier = Modifier.align(Alignment.End),
                    ) { Text("Share answer") }
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
                    Text(word.verse, fontFamily = BookSerif, fontSize = 20.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 12.dp))
                    word.testimony?.let { Text(it, color = Color(0xFF257345), modifier = Modifier.padding(top = 15.dp)) }
                    Text(mediumDate(word.answeredAt ?: word.savedAt), color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 10.dp))
                    if (canAnswer) TextButton(onClick = { onAnswer(word) }, modifier = Modifier.align(Alignment.End)) { Text("Mark answered") }
                }
            }
        }
    }
}

/**
 * The question the journal opens with, one per day.
 *
 * Written rather than generated, and about a life with God rather than about
 * feelings in general - which is the difference between this and every other
 * journal that asks how your day was.
 */
private val HOME_QUESTIONS = listOf(
    "What is on your heart today?",
    "Where did you see God this week?",
    "What are you asking Him for?",
    "What has He already done that you have not thanked Him for?",
    "What do you want to remember about today?",
    "What are you carrying that you could put down?",
    "What is one way you have seen His faithfulness?",
)


/**
 * One attachment on a journal page.
 *
 * A photo or video is drawn from the system's own thumbnail, off the main
 * thread, at a size a phone screen can use; tapping it opens it full size. A
 * document or recording, which has no picture, is a quiet row naming what it
 * is. If the file has since been deleted or its permission lost, the row says
 * so instead of showing a broken image.
 */
@Composable
private fun AttachmentView(value: String) {
    val context = platformContext()
    val type = remember(value) { mimeTypeOf(context, value) }
    val visual = type.startsWith("image/") || type.startsWith("video/")
    var picture by remember(value) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var missing by remember(value) { mutableStateOf(false) }
    if (visual) {
        LaunchedEffect(value) {
            picture = loadThumbnail(context, value, 1080)
            missing = picture == null
        }
    }
    val open = {
        openAttachment(context, value)
        Unit
    }
    val image = picture
    if (visual && image != null) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = open),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Image(
                image, null,
                Modifier.fillMaxWidth().aspectRatio((image.width.toFloat() / image.height).coerceIn(.6f, 1.8f)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
            if (type.startsWith("video/")) {
                Box(
                    Modifier.size(54.dp).clip(CircleShape).background(Color.Black.copy(alpha = .45f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(28.dp)) }
            }
        }
    } else if (!visual || missing) {
        val label = when {
            missing -> "This file is no longer available"
            type == "application/pdf" -> "PDF document"
            type.startsWith("audio/") -> "Voice recording"
            else -> "Attached file"
        }
        Surface(
            onClick = open, enabled = !missing,
            shape = RoundedCornerShape(13.dp), color = Color.White, border = BorderStroke(1.dp, Hairline),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (type.startsWith("audio/")) Icons.Outlined.Mic else Icons.Outlined.AttachFile, null,
                    tint = Gold, modifier = Modifier.size(19.dp),
                )
                Text(label, fontSize = 13.5.sp, modifier = Modifier.weight(1f).padding(start = 12.dp))
                if (!missing) Text("Open", color = Electric, fontSize = 12.sp)
            }
        }
    } else {
        // still loading: hold the space so the page does not jump
        Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(18.dp)).background(Hairline.copy(alpha = .4f)))
    }
}
