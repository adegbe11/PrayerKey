package com.prayerkey.manna.ui.journal

import com.prayerkey.manna.ui.platform.MediaKind
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import com.prayerkey.manna.platform.format
import com.prayerkey.manna.ui.platform.loadThumbnail
import com.prayerkey.manna.ui.platform.pickDate
import com.prayerkey.manna.ui.platform.rememberDictation
import com.prayerkey.manna.ui.platform.rememberMediaPicker
import com.prayerkey.manna.ui.platform.fullScreenDialog
import com.prayerkey.manna.platform.withDayOfMonth
import com.prayerkey.manna.platform.pad2
import com.prayerkey.manna.platform.now
import com.prayerkey.manna.ui.platform.platformContext
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Lightbulb

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.ui.church.ReferenceDetector
import com.prayerkey.manna.ui.theme.premiumCard
import com.prayerkey.manna.ui.theme.PaperFill
import com.prayerkey.manna.ui.theme.topHighlight
import com.prayerkey.manna.ui.theme.Corners
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

/* WriteResult lives in :ui (journal/WriteResult.kt), where the view model can see it. */

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
    initialEntryAt: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
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
    /** Open the template gallery or the prompt packs instead of this blank page. */
    onTemplates: (() -> Unit)? = null,
    onPrompts: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onSubmit: (WriteResult) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var mood by remember { mutableStateOf(initialMood) }
    var body by remember { mutableStateOf(initialBody.ifBlank { prompt?.starter.orEmpty() }) }
    var gratitude by remember { mutableStateOf(initialGratitude) }
    var isPrayer by remember { mutableStateOf(initialIsPrayer || prompt?.suggestPrayer == true) }
    // a prompt chosen while this page is open lands on it
    androidx.compose.runtime.LaunchedEffect(prompt) {
        if (prompt != null && body.isBlank()) body = prompt.starter.orEmpty()
        if (prompt?.suggestPrayer == true) isPrayer = true
    }
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
    val context = platformContext()
    val mediaPicker = rememberMediaPicker { picked ->
        media = (media + picked).distinct().take(30)
    }
    androidx.compose.runtime.LaunchedEffect(openWith) {
        when (openWith) {
            "media" -> mediaPicker.launch()
            "voice" -> voiceDisclosure = true
            /* Details too, or the tick lands inside a panel that is closed by
               default on a new entry - so Bible would set something true and
               show the user a blank page, which is indistinguishable from a
               button that does nothing. */
            "verse" -> { attachToday = true; showDetails = true }
        }
    }

    val voiceInput = rememberDictation("Speak what is on your heart") { spoken ->
        body = if (body.isBlank()) spoken else "$body\n$spoken"
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
        com.prayerkey.manna.platform.localDateOfMillis(entryAt)
    }
    val openDatePicker = {
        pickDate(context, entryDate, earliest = null) { picked ->
            // the new day, at the same time of day the entry already had
            val zone = kotlinx.datetime.TimeZone.currentSystemDefault()
            val old = kotlinx.datetime.Instant.fromEpochMilliseconds(entryAt).toLocalDateTime(zone)
            entryAt = kotlinx.datetime.LocalDateTime(picked, old.time).toInstant(zone).toEpochMilliseconds()
        }
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
        properties = fullScreenDialog(),
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
                val ready = body.isNotBlank() || title.isNotBlank() || media.isNotEmpty()
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
                        "${(entryDate.dayOfMonth).pad2()}",
                        color = ink, fontSize = 34.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                    )
                    Text(
                        "  " + entryDate.format("MMM") +
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
                    // the question sits above the page as a tag, like a note clipped to it
                    Text(
                        it.text, color = ink, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 4.dp)
                            .clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = .09f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                        Text("Scripture you mentioned", color = muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        media.forEachIndexed { index, value ->
                            key(value) {
                                EditorAttachment(value, panel, line, accent) {
                                    media = media.filterIndexed { i, _ -> i != index }
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
                        OutlinedTextField(gratitude, { gratitude = it }, label = { Text("Grateful for") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), shape = Corners.control)
                        OutlinedTextField(tags, { tags = it }, label = { Text("Tags") }, placeholder = { Text("faith, family, healing") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = Corners.control)
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(location, { location = it }, label = { Text("Place") }, leadingIcon = { Icon(Icons.Outlined.Place, null) }, singleLine = true, modifier = Modifier.weight(1f), shape = Corners.control)
                            OutlinedTextField(weather, { weather = it }, label = { Text("Weather") }, leadingIcon = { Icon(Icons.Outlined.WbSunny, null) }, singleLine = true, modifier = Modifier.weight(1f), shape = Corners.control)
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
            /* A blank page offers four ways to begin, and steps aside at the
               first letter typed - the page belongs to the words after that. */
            val blank = !editing && prompt == null && title.isBlank() && body.isBlank() && media.isEmpty()
            Column(Modifier.fillMaxWidth().background(cs.background).navigationBarsPadding()) {
                AnimatedVisibility(blank, enter = fadeIn(), exit = fadeOut() + shrinkVertically()) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            Modifier.padding(horizontal = 14.dp).fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, ink.copy(alpha = .10f), RoundedCornerShape(16.dp))
                                .background(cs.surface)
                                .padding(vertical = 10.dp),
                        ) {
                            StartTile(Icons.Outlined.Image, "Photos", ink, Modifier.weight(1f)) {
                                mediaPicker.launch(MediaKind.Photos)
                            }
                            if (onTemplates != null) StartTile(Icons.Outlined.EditNote, "Templates", ink, Modifier.weight(1f), onTemplates)
                            if (onPrompts != null) StartTile(Icons.Outlined.Lightbulb, "Prompts", ink, Modifier.weight(1f), onPrompts)
                            StartTile(Icons.Outlined.Mic, "Voice", ink, Modifier.weight(1f)) { voiceDisclosure = true }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
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
                        mediaPicker.launch(MediaKind.Photos)
                    }
                    ToolGlyph(Icons.Outlined.Mic, "Dictate", muted) { voiceDisclosure = true }
                    ToolGlyph(Icons.Outlined.MenuBook, "Attach today's verse", if (attachToday) accent else muted) {
                        attachToday = !attachToday
                    }
                    ToolGlyph(Icons.Outlined.VolunteerActivism, if (isPrayer) "On prayer list" else "Add to prayer list", if (isPrayer) accent else muted) {
                        isPrayer = !isPrayer
                    }
                    ToolGlyph(Icons.Outlined.AttachFile, "Attach a file", muted) {
                        mediaPicker.launch(MediaKind.AnyFile)
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
            voiceInput.launch()
        }) { Text("Start dictation") } },
        dismissButton = { TextButton(onClick = { voiceDisclosure = false }) { Text("Cancel") } },
    )
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Delete this entry?") },
        text = { Text("This cannot be undone.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete?.invoke() }) { Text("Delete", color = Color(0xFFB3402A)) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Keep entry") } },
    )
}

@Composable
private fun StartTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        Text(label, color = tint, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun ToolGlyph(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, label, tint = tint, modifier = Modifier.size(21.dp)) }
}


/** An attachment while writing: the photo itself, or a file mark, with a way to take it off. */
@Composable
private fun EditorAttachment(value: String, panel: Color, line: Color, accent: Color, onRemove: () -> Unit) {
    val context = platformContext()
    var picture by remember(value) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(value) { picture = loadThumbnail(context, value, 400) }
    Box(
        Modifier.size(96.dp).clip(RoundedCornerShape(14.dp)).background(panel)
            .border(1.dp, line, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        val image = picture
        if (image != null) {
            androidx.compose.foundation.Image(
                image, null, Modifier.matchParentSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        } else {
            Icon(Icons.Outlined.InsertDriveFile, null, tint = accent, modifier = Modifier.size(26.dp))
        }
        Box(
            Modifier.align(Alignment.TopEnd).padding(5.dp).size(24.dp).clip(CircleShape)
                .background(Color.Black.copy(alpha = .5f)).clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Close, "Remove attachment", tint = Color.White, modifier = Modifier.size(14.dp)) }
    }
}
