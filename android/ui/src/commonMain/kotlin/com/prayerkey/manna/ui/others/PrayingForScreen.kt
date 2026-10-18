package com.prayerkey.manna.ui.others

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PrayerPerson
import com.prayerkey.manna.data.PrayingFor
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.platform.shareText
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans

internal val Violet = Color(0xFF6200ED)
internal val Gold = Color(0xFFFFD68C)
internal val DeepViolet = Color(0xFF2E0078)
private val Ink = Color(0xFF1D1D1F)
private val Muted = Color(0xFF6E6E73)

private fun nowMs() = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
private fun dayNow() = today().toEpochDays().toLong()

/** Opens WhatsApp with the message ready; any phone without it gets the share sheet. */
internal fun tellThem(uri: androidx.compose.ui.platform.UriHandler, context: com.prayerkey.manna.platform.PlatformContext, text: String) {
    val unreserved = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '_', '.', '~')
    val encoded = text.encodeToByteArray().joinToString("") { byte ->
        val i = byte.toInt() and 0xFF
        if (i < 128 && i.toChar() in unreserved) i.toChar().toString()
        else "%" + i.toString(16).uppercase().padStart(2, '0')
    }
    runCatching { uri.openUri("https://wa.me/?text=$encoded") }.onFailure { shareText(context, text, "Tell them") }
}

/**
 * Praying for others: the people you said you would pray for, kept.
 *
 * Each card holds a name and what they asked for. "I prayed" marks the day,
 * and once it is marked the card offers to tell them, over WhatsApp, with a
 * blessing from Numbers 6. When God answers, the card moves to the answered
 * list with what happened, so the answers can be counted later.
 */
@Composable
fun PrayingForScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val uri = LocalUriHandler.current
    LaunchedEffect(Unit) { PrayingFor.load(context) }
    val people by PrayingFor.people.collectAsState()
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PrayerPerson?>(null) }
    var answering by remember { mutableStateOf<PrayerPerson?>(null) }
    val waiting = people.filter { it.answered == 0L }
    val answered = people.filter { it.answered != 0L }
    val day = dayNow()

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2C0A5C), Color(0xFF12002A))))) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Box(
                        Modifier.padding(top = 12.dp).size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp)) }
                    Text("Praying for", Modifier.padding(top = 14.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, color = Color.White)
                    Text(
                        "Bear ye one another's burdens. Galatians 6:2",
                        Modifier.padding(top = 2.dp, bottom = 8.dp),
                        fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 16.sp, color = Gold,
                    )
                    if (people.isNotEmpty()) {
                        val prayedToday = people.count { it.prayedOn(day) }
                        Text(
                            "$prayedToday of ${waiting.size} prayed for today",
                            fontFamily = UtilitySans, fontSize = 13.sp, color = Color.White.copy(alpha = .65f),
                        )
                    }
                }
            }
            if (people.isEmpty()) item { EmptyList { adding = true } }
            items(waiting, key = { it.id }) { p ->
                PersonCard(
                    p, prayedToday = p.prayedOn(day),
                    onPrayed = { PrayingFor.prayed(context, p.id, day) },
                    onTell = { tellThem(uri, context, PrayingFor.message(p)) },
                    onEdit = { editing = p },
                    onAnswered = { answering = p },
                    onRemove = { PrayingFor.remove(context, p.id) },
                )
            }
            if (answered.isNotEmpty()) {
                item {
                    Text(
                        "GOD ANSWERED  ·  ${answered.size}", Modifier.padding(top = 18.dp),
                        fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.6.sp, color = Gold,
                    )
                }
                items(answered, key = { it.id }) { p -> AnsweredCard(p) { PrayingFor.reopen(context, p.id) } }
            }
        }
        // the add button, gold, always in reach
        Row(
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 20.dp, bottom = 24.dp)
                .clip(RoundedCornerShape(99.dp)).background(Gold).clickable { adding = true }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Add, null, tint = DeepViolet, modifier = Modifier.size(20.dp))
            Text("Add someone", Modifier.padding(start = 8.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepViolet)
        }
    }

    if (adding) PersonSheet(null, onDismiss = { adding = false }) { name, request, family ->
        PrayingFor.add(context, name, request, family, nowMs()); adding = false
    }
    editing?.let { p ->
        PersonSheet(p, onDismiss = { editing = null }) { name, request, family ->
            PrayingFor.edit(context, p.id, name, request, family); editing = null
        }
    }
    answering?.let { p ->
        AnswerSheet(p, onDismiss = { answering = null }) { note ->
            PrayingFor.answered(context, p.id, note, nowMs()); answering = null
        }
    }
}

@Composable
private fun EmptyList(onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 30.dp).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = .07f)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Who did you promise to pray for?", fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Color.White, textAlign = TextAlign.Center)
        Text(
            "A friend in hospital, a sister waiting on a visa, your pastor. Add them here and PrayerKey will keep the list for you, and let them know when you have prayed.",
            Modifier.padding(top = 10.dp), fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp,
            color = Color.White.copy(alpha = .7f), textAlign = TextAlign.Center,
        )
        Text(
            "Add the first name", Modifier.padding(top = 18.dp).clip(RoundedCornerShape(99.dp)).background(Gold).clickable(onClick = onAdd)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepViolet,
        )
    }
}

@Composable
private fun PersonCard(
    p: PrayerPerson,
    prayedToday: Boolean,
    onPrayed: () -> Unit,
    onTell: () -> Unit,
    onEdit: () -> Unit,
    onAnswered: () -> Unit,
    onRemove: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF8A3DFF), Violet))),
                contentAlignment = Alignment.Center,
            ) {
                Text(p.name.take(1).uppercase(), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.name, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, color = Ink)
                    if (p.family) {
                        Text(
                            "FAMILY", Modifier.padding(start = 8.dp).clip(RoundedCornerShape(99.dp)).background(Color(0xFFF1E9FF))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp, color = Violet,
                        )
                    }
                }
                Text(
                    if (p.prayed.isEmpty()) "Not prayed for yet" else "Prayed for ${p.prayed.size} ${if (p.prayed.size == 1) "time" else "times"}",
                    fontFamily = UtilitySans, fontSize = 12.sp, color = Muted,
                )
            }
            Box {
                Icon(
                    Icons.Outlined.MoreHoriz, "More", tint = Muted,
                    modifier = Modifier.size(36.dp).clip(CircleShape).clickable { menu = true }.padding(6.dp),
                )
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("God answered") }, onClick = { menu = false; onAnswered() })
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { menu = false; onEdit() })
                    DropdownMenuItem(text = { Text("Remove") }, onClick = { menu = false; onRemove() })
                }
            }
        }
        if (p.request.isNotBlank()) {
            Text(
                p.request, Modifier.padding(top = 12.dp),
                fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 17.sp, lineHeight = 24.sp, color = Ink,
            )
        }
        Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (prayedToday) {
                Row(
                    Modifier.clip(RoundedCornerShape(99.dp)).background(Color(0xFFF1E9FF)).padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Check, null, tint = Violet, modifier = Modifier.size(17.dp))
                    Text("Prayed today", Modifier.padding(start = 6.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Violet)
                }
                Text(
                    "Tell them", Modifier.clip(RoundedCornerShape(99.dp)).background(Color(0xFF25D366)).clickable(onClick = onTell)
                        .padding(horizontal = 18.dp, vertical = 11.dp),
                    fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White,
                )
            } else {
                Text(
                    "I prayed", Modifier.clip(RoundedCornerShape(99.dp)).background(Violet).clickable(onClick = onPrayed)
                        .padding(horizontal = 22.dp, vertical = 11.dp),
                    fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun AnsweredCard(p: PrayerPerson, onReopen: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).border(1.dp, Gold.copy(alpha = .5f), RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = .06f)).padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Check, null, tint = Gold, modifier = Modifier.size(18.dp))
            Text(p.name, Modifier.weight(1f).padding(start = 8.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, color = Color.White)
            Text("Still praying", Modifier.clickable(onClick = onReopen).padding(4.dp), fontFamily = UtilitySans, fontSize = 12.sp, color = Color.White.copy(alpha = .5f))
        }
        if (p.request.isNotBlank()) Text(p.request, Modifier.padding(top = 6.dp), fontFamily = UtilitySans, fontSize = 14.sp, color = Color.White.copy(alpha = .7f))
        if (p.answerNote.isNotBlank()) {
            Text(p.answerNote, Modifier.padding(top = 8.dp), fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 17.sp, lineHeight = 24.sp, color = Gold)
        }
    }
}

@Composable
private fun sheetField() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Violet, unfocusedBorderColor = Color(0x22000000),
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonSheet(p: PrayerPerson?, onDismiss: () -> Unit, onSave: (String, String, Boolean) -> Unit) {
    var name by remember { mutableStateOf(p?.name.orEmpty()) }
    var request by remember { mutableStateOf(p?.request.orEmpty()) }
    var family by remember { mutableStateOf(p?.family ?: false) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFFF7F4FC)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 30.dp)) {
            Text(if (p == null) "Who are you praying for?" else "Edit", fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Ink)
            OutlinedTextField(
                name, { name = it }, Modifier.fillMaxWidth().padding(top = 16.dp),
                placeholder = { Text("Their name") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = sheetField(),
            )
            OutlinedTextField(
                request, { request = it }, Modifier.fillMaxWidth().padding(top = 10.dp),
                placeholder = { Text("What to pray for: healing, a new job, peace at home") },
                minLines = 2, shape = RoundedCornerShape(16.dp), colors = sheetField(),
            )
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Family", fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Ink)
                    Text("Prayed for at the family altar", fontFamily = UtilitySans, fontSize = 12.sp, color = Muted)
                }
                Switch(family, { family = it }, colors = SwitchDefaults.colors(checkedTrackColor = Violet))
            }
            Text(
                "Keep", Modifier.padding(top = 18.dp).fillMaxWidth().clip(RoundedCornerShape(99.dp))
                    .background(if (name.isBlank()) Violet.copy(alpha = .35f) else Violet)
                    .clickable(enabled = name.isNotBlank()) { onSave(name, request, family) }.padding(vertical = 14.dp),
                fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnswerSheet(p: PrayerPerson, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var note by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFFF7F4FC)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 30.dp)) {
            Text("God answered", fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Ink)
            Text("What happened for ${p.name}? Write it down so it is remembered.", Modifier.padding(top = 4.dp), fontFamily = UtilitySans, fontSize = 14.sp, color = Muted)
            OutlinedTextField(
                note, { note = it }, Modifier.fillMaxWidth().padding(top = 14.dp),
                placeholder = { Text("She was discharged on Friday") }, minLines = 3, shape = RoundedCornerShape(16.dp), colors = sheetField(),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Give thanks", Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)).background(Violet).clickable { onSave(note) }.padding(vertical = 14.dp),
                fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, textAlign = TextAlign.Center,
            )
        }
    }
}
