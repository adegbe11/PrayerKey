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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
) {
    var tab by remember { mutableStateOf(JournalTab.Journal) }
    var composing by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<JournalEntry?>(null) }
    var answering by remember { mutableStateOf<SavedWord?>(null) }
    var query by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(Canvas)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp).padding(top = 24.dp)) {
            Text("Journal", fontFamily = FontFamily.Serif, fontSize = 32.sp)
            Text("Your walk with God, written down.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))

            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(tab == JournalTab.Journal, { tab = JournalTab.Journal }, label = { Text("Journal ${entries.size}") })
                FilterChip(tab == JournalTab.Saved, { tab = JournalTab.Saved }, label = { Text("Saved ${words.count { it.answeredAt == null }}") })
                FilterChip(tab == JournalTab.Answered, { tab = JournalTab.Answered }, label = { Text("Answered ${words.count { it.answeredAt != null }}") })
            }

            when (tab) {
                JournalTab.Journal -> JournalTimeline(
                    entries = entries, streak = journalStreak, query = query,
                    onQuery = { query = it }, onEdit = { editing = it },
                )
                JournalTab.Saved -> WordList(words.filter { it.answeredAt == null }, canAnswer = true) { answering = it }
                JournalTab.Answered -> WordList(words.filter { it.answeredAt != null }, canAnswer = false) {}
            }
        }

        if (tab == JournalTab.Journal) {
            ExtendedFloatingActionButton(
                onClick = { composing = true },
                containerColor = Electric, contentColor = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp),
            ) {
                Icon(Icons.Outlined.Create, null); Spacer(Modifier.width(8.dp)); Text("Write", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (composing) EntryComposer(
        title = "Today's entry",
        initialMood = MOODS.first().first, initialBody = "", initialGratitude = "",
        todayCard = todayCard, showAttach = true,
        onDismiss = { composing = false },
        onSubmit = { mood, body, gratitude, attach ->
            onAdd(mood, body, gratitude, if (attach) todayCard.reference else null, if (attach) todayCard.verse else null)
            composing = false
        },
    )

    editing?.let { entry ->
        EntryComposer(
            title = "Edit entry",
            initialMood = entry.mood, initialBody = entry.body, initialGratitude = entry.gratitude,
            todayCard = todayCard, showAttach = false,
            onDismiss = { editing = null },
            onSubmit = { mood, body, gratitude, _ -> onUpdate(entry.id, mood, body, gratitude); editing = null },
            onDelete = { onDelete(entry.id); editing = null },
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatPill("🔥 $streak", "day streak", Modifier.weight(1f))
            StatPill("${entries.size}", "entries", Modifier.weight(1f))
            StatPill("${entries.count { it.gratitude.isNotBlank() }}", "gratitudes", Modifier.weight(1f))
        }

        OutlinedTextField(
            query, onQuery,
            placeholder = { Text("Search your journal…", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Muted) },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Electric, unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = AppleGray, unfocusedContainerColor = AppleGray,
            ),
        )

        if (entries.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(top = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📖", fontSize = 44.sp)
                Text("Your story starts here", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
                Text(
                    "Write what's on your heart today. In a year,\nyou'll be holding proof of how God moved.",
                    color = Muted, fontSize = 13.sp, lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            return
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 96.dp),
        ) {
            memory?.let { m ->
                item(key = "memory") {
                    val ago = if (m.entryDay == today - 365) "One year ago today" else "One month ago today"
                    Surface(shape = RoundedCornerShape(18.dp), color = Night) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
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
                items(dayEntries, key = { it.id }) { entry -> EntryCard(entry) { onEdit(entry) } }
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
private fun StatPill(value: String, label: String, modifier: Modifier) {
    // Apple style: soft gray fill on pure white, no border
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = AppleGray) {
        Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun EntryCard(entry: JournalEntry, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp), color = Ivory,
        border = BorderStroke(1.dp, Hairline),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.mood, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(MOODS.firstOrNull { it.first == entry.mood }?.second ?: "", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
        }
    }
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

@Composable
private fun WordList(words: List<SavedWord>, canAnswer: Boolean, onAnswer: (SavedWord) -> Unit) {
    if (words.isEmpty()) {
        Column(Modifier.fillMaxWidth().padding(top = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (canAnswer) "🗂" else "🏆", fontSize = 44.sp)
            Text(
                if (canAnswer) "Push a verse up when it speaks to you." else "When God does it, mark the word answered.\nYour proof grows here.",
                color = Muted, fontSize = 13.sp, lineHeight = 19.sp,
                modifier = Modifier.padding(top = 12.dp),
            )
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
