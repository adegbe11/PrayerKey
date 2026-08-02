package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

private enum class JournalTab { Journal, Saved, Answered }

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
) {
    var tab by remember { mutableStateOf(JournalTab.Journal) }
    var composing by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<JournalEntry?>(null) }
    var answering by remember { mutableStateOf<SavedWord?>(null) }
    var query by remember { mutableStateOf("") }
    // Write is two steps now: pick a prompt, then write. `picking` is the
    // suggestion sheet; `writing` holds the chosen prompt (null = blank page).
    var picking by remember { mutableStateOf(false) }
    var writing by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf<com.prayerkey.manna.ui.journal.JournalSuggestions.Prompt?>(null) }
    var answeringEntry by remember { mutableStateOf<JournalEntry?>(null) }

    Box(Modifier.fillMaxSize().background(dayWash())) {
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp).padding(top = 24.dp)) {
            // Home is bare now, so Settings lives here — the one screen
            // that is already about the user rather than today's word.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Journal", fontFamily = FontFamily.Serif, fontSize = 32.sp)
                }
                androidx.compose.material3.IconButton(onClick = onProfile) {
                    Icon(Icons.Outlined.Settings, "Settings", tint = Muted)
                }
            }

            // Filters only earn their space once there is something to sort.
            // On a blank journal they were three pills all reading "0".
            val hasAnything = entries.isNotEmpty() || words.isNotEmpty()
            if (hasAnything) {
                Row(Modifier.fillMaxWidth().padding(vertical = Space.block), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PkChip("Journal ${entries.size}", tab == JournalTab.Journal) { tab = JournalTab.Journal }
                    PkChip("Saved ${words.count { it.answeredAt == null }}", tab == JournalTab.Saved) { tab = JournalTab.Saved }
                    // answered prayers live in BOTH places now
                    PkChip(
                        "Answered ${words.count { it.answeredAt != null } + entries.count { it.answeredAt != null }}",
                        tab == JournalTab.Answered,
                    ) { tab = JournalTab.Answered }
                }
            } else Spacer(Modifier.height(10.dp))

            when (tab) {
                JournalTab.Journal -> JournalTimeline(
                    entries = entries, streak = journalStreak, query = query,
                    onQuery = { query = it }, onEdit = { editing = it },
                    onAnswer = { answeringEntry = it },
                )
                JournalTab.Saved -> WordList(words.filter { it.answeredAt == null }, canAnswer = true) { answering = it }
                JournalTab.Answered -> AnsweredList(
                    entries = entries.filter { it.answeredAt != null },
                    words = words.filter { it.answeredAt != null },
                    onEdit = { editing = it },
                )
            }
        }

        if (tab == JournalTab.Journal) {
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
                    .shadow(18.dp, CircleShape, spotColor = Electric.copy(alpha = .5f))
                    .clip(CircleShape).background(ElectricGloss)
                    .clickable { picking = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Create, "Write an entry", tint = Color.White, modifier = Modifier.size(25.dp))
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

    editing?.let { entry ->
        com.prayerkey.manna.ui.journal.WriteSheet(
            prompt = null,
            todayCard = todayCard,
            initialMood = entry.mood,
            initialBody = entry.body,
            initialGratitude = entry.gratitude,
            initialIsPrayer = entry.isPrayer,
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
) {
    val today = LocalDate.now().toEpochDay()
    val memory = remember(entries) {
        entries.firstOrNull { it.entryDay == today - 365 } ?: entries.firstOrNull { it.entryDay == today - 30 }
    }
    val shown = remember(entries, query) {
        if (query.isBlank()) entries
        else entries.filter {
            it.body.contains(query, true) || it.gratitude.contains(query, true) || (it.verseRef ?: "").contains(query, true)
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

        // one quiet line instead of three grey blocks
        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🔥 $streak", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            StatDot()
            Text("${entries.size} ${if (entries.size == 1) "entry" else "entries"}", color = Muted, fontSize = 12.sp)
            val gratitudes = entries.count { it.gratitude.isNotBlank() }
            if (gratitudes > 0) { StatDot(); Text("$gratitudes grateful", color = Muted, fontSize = 12.sp) }
        }

        OutlinedTextField(
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = Space.block, bottom = Space.dock),
        ) {
            memory?.let { m ->
                item(key = "memory") {
                    val ago = if (m.entryDay == today - 365) "One year ago today" else "One month ago today"
                    Box(Modifier.fillMaxWidth().premiumCard(fill = NightFill).goldEdge()) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            Text("ON THIS DAY", color = Gold, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
                            Text(ago, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
                            Text(m.body, color = Color(0xFFD8D5CC), fontSize = 13.sp, lineHeight = 19.sp, maxLines = 3, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }
            grouped.forEach { (day, dayEntries) ->
                item(key = "day-$day") {
                    Text(
                        dayLabel(day, today),
                        color = Muted, fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                items(dayEntries, key = { it.id }) { entry ->
                    EntryCard(entry, onAnswer = { onAnswer(entry) }) { onEdit(entry) }
                }
            }
        }
    }
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
private fun EntryCard(entry: JournalEntry, onAnswer: () -> Unit, onClick: () -> Unit) {
    val answered = entry.answeredAt != null
    Box(
        Modifier.fillMaxWidth()
            .premiumCard(fill = if (answered) GoldFill else PaperFill)
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.mood, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(MOODS.firstOrNull { it.first == entry.mood }?.second ?: "", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                // where this page came from, so a moment traces back to itself
                originGlyph(entry.source)?.let { glyph ->
                    Spacer(Modifier.width(7.dp))
                    Text(glyph, color = Muted, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(entry.createdAt)),
                    color = Muted, fontSize = 10.sp,
                )
            }
            Text(entry.body, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 10.dp))
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
                        Text("Still praying", color = Muted, fontSize = 11.5.sp, modifier = Modifier.weight(1f))
                        Text("Mark answered", color = Electric, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    }
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
