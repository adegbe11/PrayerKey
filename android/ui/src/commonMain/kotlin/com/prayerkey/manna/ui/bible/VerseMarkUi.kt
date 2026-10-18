package com.prayerkey.manna.ui.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.VerseMark
import com.prayerkey.manna.data.VerseMarks
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.share.Share
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans

/** The five colours a verse can be marked in, soft enough to read through. */
val MarkColors = listOf(
    Color(0xFFFFD68C), // gold
    Color(0xFFDCC7FF), // violet
    Color(0xFFFFC9D6), // rose
    Color(0xFFBFE3FF), // sky
    Color(0xFFC9EBC4), // sage
)
private val MarkNames = listOf("Gold", "Violet", "Rose", "Sky", "Sage")
private val Violet = Color(0xFF6200ED)
private val Ink = Color(0xFF1D1D1F)
private val Muted = Color(0xFF6E6E73)

/** The colour a mark is drawn in, or null when it has none. */
fun markColor(mark: VerseMark?): Color? = mark?.color?.takeIf { it in 1..MarkColors.size }?.let { MarkColors[it - 1] }

private fun now() = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

/**
 * Colour, ribbon and note for one verse, in one strip. A second tap on the
 * chosen colour clears it, the way lifting a highlighter off the page would.
 */
@Composable
fun MarkStrip(ref: String, text: String, modifier: Modifier = Modifier) {
    val context = platformContext()
    LaunchedEffect(Unit) { VerseMarks.load(context) }
    val all by VerseMarks.all.collectAsState()
    val mark = all.firstOrNull { it.ref == ref }
    var noteOpen by remember(ref) { mutableStateOf(false) }
    var draft by remember(ref) { mutableStateOf(VerseMarks.of(ref)?.note.orEmpty()) }

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White)
                .border(1.dp, Color(0x14000000), RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MarkColors.forEachIndexed { i, c ->
                val on = mark?.color == i + 1
                Box(
                    Modifier.padding(end = 8.dp).size(30.dp).clip(CircleShape).background(c)
                        .border(if (on) 2.5.dp else 0.dp, if (on) Violet else Color.Transparent, CircleShape)
                        .clickable {
                            VerseMarks.update(context, ref, text, now()) { it.copy(color = if (on) 0 else i + 1) }
                        },
                    contentAlignment = Alignment.Center,
                ) { if (on) Icon(Icons.Outlined.Check, "${MarkNames[i]}, chosen", tint = Violet, modifier = Modifier.size(16.dp)) }
            }
            Spacer(Modifier.weight(1f))
            val marked = mark?.bookmark == true
            Icon(
                if (marked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                if (marked) "Remove bookmark" else "Bookmark this verse",
                tint = Violet,
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .clickable { VerseMarks.update(context, ref, text, now()) { it.copy(bookmark = !marked) } }
                    .padding(8.dp),
            )
            Icon(
                Icons.Outlined.EditNote, "Write a note",
                tint = if (mark?.note.isNullOrBlank()) Muted else Violet,
                modifier = Modifier.size(40.dp).clip(CircleShape).clickable { noteOpen = !noteOpen }.padding(7.dp),
            )
        }
        if (noteOpen) {
            OutlinedTextField(
                // kept as it is typed, so closing the sheet never loses a note
                value = draft, onValueChange = {
                    draft = it
                    VerseMarks.update(context, ref, text, now()) { m -> m.copy(note = it) }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                placeholder = { Text("What is God saying to you here?", color = Muted) },
                minLines = 3, shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Violet, unfocusedBorderColor = Color(0x22000000),
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                ),
            )
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    "Done",
                    Modifier.clip(RoundedCornerShape(99.dp)).background(Violet).clickable {
                        VerseMarks.update(context, ref, text, now()) { it.copy(note = draft.trim()) }
                        noteOpen = false
                    }.padding(horizontal = 18.dp, vertical = 10.dp),
                    color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                )
            }
        } else if (!mark?.note.isNullOrBlank()) {
            Text(
                mark!!.note,
                Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF3EEFF)).clickable { noteOpen = true }.padding(14.dp),
                fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 23.sp, color = Ink,
            )
        }
    }
}

/** One verse, with everything that can be done to it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkSheet(ref: String, text: String, onDismiss: () -> Unit) {
    val context = platformContext()
    val clipboard = LocalClipboardManager.current
    val all by VerseMarks.all.collectAsState()
    val tint = markColor(all.firstOrNull { it.ref == ref })
    var copied by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFFF7F4FC)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 34.dp)) {
            Text(ref, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Ink)
            Text(
                text,
                Modifier.padding(top = 14.dp, bottom = 18.dp).clip(RoundedCornerShape(10.dp))
                    .background(tint ?: Color.Transparent).padding(horizontal = if (tint != null) 8.dp else 0.dp, vertical = 4.dp),
                fontFamily = DisplaySerif, fontSize = 21.sp, lineHeight = 31.sp, color = Ink,
            )
            MarkStrip(ref, text)
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetButton(if (copied) "Copied" else "Copy", Icons.Outlined.ContentCopy, Modifier.weight(1f)) {
                    clipboard.setText(AnnotatedString("$text\n$ref (KJV)"))
                    copied = true
                }
                SheetButton("Share", Icons.Outlined.Share, Modifier.weight(1f)) {
                    Share.cards.shareVerse(context, VerseCard(ref, "KJV", text, ""))
                }
            }
        }
    }
}

@Composable
private fun SheetButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, Color(0x14000000), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Violet, modifier = Modifier.size(18.dp))
        Text(label, Modifier.padding(start = 8.dp), fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink)
    }
}

/**
 * Everything marked, in one place: the colours, the notes, the ribbons.
 * The margin of a well-used Bible, gathered.
 */
@Composable
fun MyBibleScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    LaunchedEffect(Unit) { VerseMarks.load(context) }
    val all by VerseMarks.all.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var open by remember { mutableStateOf<VerseMark?>(null) }
    val tabs = listOf(
        "Highlights" to all.filter { it.color > 0 },
        "Notes" to all.filter { it.note.isNotBlank() },
        "Bookmarks" to all.filter { it.bookmark },
    )
    val shown = tabs[tab].second

    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2C0A5C), Color(0xFF12002A), Color(0xFF12002A)))),
    ) {
        Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp)) {
            Box(
                Modifier.padding(top = 12.dp).size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f)).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp)) }
            Text("My Bible", Modifier.padding(top = 14.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, color = Color.White)
            Text(
                "Every verse you have marked, kept.",
                Modifier.padding(top = 2.dp), fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 16.sp, color = Color(0xFFFFD68C),
            )
            Row(Modifier.padding(top = 20.dp, bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { i, (name, list) ->
                    val on = i == tab
                    Text(
                        "$name  ${list.size}",
                        Modifier.clip(RoundedCornerShape(99.dp)).background(if (on) Color(0xFFFFD68C) else Color.White.copy(alpha = .1f))
                            .clickable { tab = i }.padding(horizontal = 14.dp, vertical = 9.dp),
                        fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        color = if (on) Color(0xFF2E0078) else Color.White.copy(alpha = .8f),
                    )
                }
            }
        }
        if (shown.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    when (tab) { 0 -> "No highlights yet"; 1 -> "No notes yet"; else -> "No bookmarks yet" },
                    fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Color.White,
                )
                Text(
                    "Tap a verse in the Bible, or press and hold a page of the book, then choose a colour, a ribbon or a note.",
                    Modifier.padding(top = 8.dp), fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp,
                    color = Color.White.copy(alpha = .7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        LazyColumn(
            Modifier.fillMaxSize().navigationBarsPadding(),
            // clear of the floating dock
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(shown, key = { it.ref }) { m ->
                Row(
                    Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min)
                        .clip(RoundedCornerShape(20.dp)).background(Color.White).clickable { open = m },
                ) {
                    Box(Modifier.width(6.dp).fillMaxHeight().background(markColor(m) ?: Violet))
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(m.ref, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Violet, modifier = Modifier.weight(1f))
                            if (m.bookmark) Icon(Icons.Filled.Bookmark, "Bookmarked", tint = Violet, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            m.text, Modifier.padding(top = 6.dp).background(markColor(m)?.copy(alpha = .55f) ?: Color.Transparent),
                            fontFamily = DisplaySerif, fontSize = 17.sp, lineHeight = 25.sp, color = Ink, maxLines = 4,
                        )
                        if (m.note.isNotBlank()) {
                            Text(
                                m.note, Modifier.padding(top = 8.dp), fontFamily = DisplaySerif, fontStyle = FontStyle.Italic,
                                fontSize = 15.sp, lineHeight = 21.sp, color = Muted, maxLines = 3,
                            )
                        }
                    }
                }
            }
        }
    }
    open?.let { m -> MarkSheet(m.ref, m.text) { open = null } }
}
