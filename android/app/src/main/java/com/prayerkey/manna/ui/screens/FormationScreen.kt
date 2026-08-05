package com.prayerkey.manna.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.FormationState
import com.prayerkey.manna.ui.journal.WriteResult
import com.prayerkey.manna.ui.theme.*

internal data class PilgrimageDay(val title: String, val reference: String, val reflection: String, val action: String)
internal data class Pilgrimage(val id: String, val title: String, val subtitle: String, val days: List<PilgrimageDay>)

internal val PILGRIMAGES = listOf(
    Pilgrimage("peace", "Seven Days of Peace", "Release anxiety without denying reality.", listOf(
        PilgrimageDay("Name the weight", "1 Peter 5:7", "God welcomes the care you have been carrying alone.", "Write the one burden you most need to release."),
        PilgrimageDay("Return to the present", "Matthew 6:34", "Grace is given for today, not every imagined tomorrow.", "Take five slow breaths before your next decision."),
        PilgrimageDay("Ask without hiding", "Philippians 4:6–7", "Prayer makes room for honest need and guarding peace.", "Turn one worry into one specific request."),
        PilgrimageDay("Receive rest", "Matthew 11:28", "Rest is an invitation, not a reward for finishing everything.", "Protect twenty quiet minutes today."),
        PilgrimageDay("Remember presence", "Isaiah 41:10", "Courage begins with the promise that you are not alone.", "Repeat the verse when fear rises."),
        PilgrimageDay("Practice gratitude", "Psalm 118:24", "Gratitude widens attention beyond the threat.", "Name three ordinary gifts."),
        PilgrimageDay("Carry peace outward", "Matthew 5:9", "Received peace can become peace offered to another.", "Bring calm to one conversation."),
    )),
    Pilgrimage("forgiveness", "Seven Days of Forgiveness", "Move toward freedom with truth and wise boundaries.", listOf(
        PilgrimageDay("Tell the truth", "Psalm 62:8", "Forgiveness does not require pretending the wound was small.", "Write what happened without minimizing it."),
        PilgrimageDay("Separate forgiveness from access", "Proverbs 4:23", "A released debt does not erase the need for safety.", "Name one healthy boundary."),
        PilgrimageDay("Remember mercy", "Ephesians 4:32", "We forgive from mercy received, not moral superiority.", "Recall a mercy you did not earn."),
        PilgrimageDay("Release revenge", "Romans 12:19", "You can surrender judgment without calling evil good.", "Name what is not yours to repay."),
        PilgrimageDay("Bless carefully", "Luke 6:28", "Prayer can loosen resentment before feelings catch up.", "Pray one honest sentence for their good."),
        PilgrimageDay("Repair what is yours", "Matthew 5:23–24", "Sometimes our freedom includes owning our part.", "Make one apology if it is safe and appropriate."),
        PilgrimageDay("Walk forward", "Isaiah 43:18–19", "Your wound is part of your story, not its final author.", "Write what freedom could make possible."),
    )),
    Pilgrimage("calling", "Seven Days of Direction", "Discern the next faithful step instead of demanding the whole map.", listOf(
        PilgrimageDay("Begin with surrender", "Proverbs 3:5–6", "Direction starts by loosening control.", "Write the outcome you need to surrender."),
        PilgrimageDay("Notice your gifts", "1 Peter 4:10", "Calling often grows through gifts already entrusted to you.", "Name three ways you naturally serve others."),
        PilgrimageDay("Listen to wise people", "Proverbs 15:22", "Discernment is strengthened in trustworthy community.", "Ask one mature person what they see in you."),
        PilgrimageDay("Test the fruit", "Galatians 5:22–23", "A path can be examined by the character it cultivates.", "Ask what this direction is producing in you."),
        PilgrimageDay("Count the cost", "Luke 14:28", "Faith and practical wisdom belong together.", "List the real cost and the available support."),
        PilgrimageDay("Take the small step", "Psalm 119:105", "A lamp usually reveals the next step, not the horizon.", "Complete one reversible action today."),
        PilgrimageDay("Review with peace", "Colossians 3:15", "Do not confuse urgency with guidance.", "Sit quietly and review what became clearer."),
    )),
    returnToFaith(),
    walkWithJesus(),
)

private fun returnToFaith(): Pilgrimage {
    data class Phase(val title: String, val reflection: String, val references: List<String>)
    val phases = listOf(
        Phase("Come home honestly", "Return begins with truth, not performance. God is not intimidated by your doubt, disappointment, or distance.", listOf("Luke 15:20", "Psalm 139:23–24", "Mark 9:24", "Psalm 62:8", "1 John 1:9", "Matthew 11:28", "Lamentations 3:22–23")),
        Phase("Rebuild trust slowly", "Faith becomes durable through small acts of attention, prayer, community, and obedience.", listOf("Proverbs 3:5–6", "Psalm 119:105", "John 15:4", "Hebrews 10:24–25", "Romans 10:17", "Philippians 1:6", "Isaiah 40:31")),
        Phase("Live renewed", "A returning faith turns outward through love, courage, service, reconciliation, and hope.", listOf("Romans 12:2", "Micah 6:8", "Galatians 5:22–23", "1 Peter 4:10", "Matthew 5:14–16", "Colossians 3:12–14", "2 Corinthians 5:17")),
    )
    val actions = listOf(
        "Write one completely honest sentence to God.",
        "Read the passage twice and circle one phrase.",
        "Sit in silence for three minutes without trying to feel anything.",
        "Tell one trustworthy person where you really are.",
        "Take one small action consistent with the passage.",
        "Name one belief you want to rebuild patiently.",
        "Review the week without grading yourself.",
    )
    return Pilgrimage("return", "21 Days of Returning", "Come back to faith with honesty, patience, and practice.", buildList {
        phases.forEach { phase -> phase.references.forEachIndexed { index, ref -> add(PilgrimageDay("${phase.title} · ${index + 1}", ref, phase.reflection, actions[index])) } }
    })
}

private fun walkWithJesus(): Pilgrimage {
    data class Phase(val title: String, val reflection: String, val references: List<String>)
    val phases = listOf(
        Phase("Invitation", "Jesus begins with an invitation to come, see, and follow.", listOf("John 1:39", "Matthew 4:19", "John 10:27", "Matthew 11:28", "Luke 9:23")),
        Phase("Attention", "Disciples learn to notice the Father, people, and the present moment.", listOf("Mark 1:35", "Luke 10:39", "Matthew 6:26", "John 5:19", "Psalm 46:10")),
        Phase("Character", "The way of Jesus forms humility, truth, mercy, purity, and courage.", listOf("Matthew 5:5", "Matthew 5:7", "Matthew 5:8", "Matthew 5:9", "John 8:31–32")),
        Phase("Prayer", "Prayer becomes communion, surrender, intercession, and trust.", listOf("Matthew 6:9", "Matthew 6:10", "Matthew 6:11", "Matthew 6:12", "Matthew 6:13")),
        Phase("Community", "Jesus forms a people who forgive, serve, welcome, and bear burdens.", listOf("John 13:34", "Mark 10:45", "Matthew 18:20", "Galatians 6:2", "Ephesians 4:32")),
        Phase("Justice and mercy", "Love of neighbor becomes concrete care for those overlooked or harmed.", listOf("Luke 4:18", "Matthew 25:40", "Luke 10:36–37", "Micah 6:8", "James 1:27")),
        Phase("Cross and surrender", "Following Jesus includes costly love, surrendered control, and faithful endurance.", listOf("Luke 22:42", "Mark 8:35", "John 12:24", "Romans 12:1", "Hebrews 12:2")),
        Phase("Resurrection life", "The journey ends in hope that sends us back into ordinary life renewed.", listOf("John 20:21", "Romans 6:4", "2 Corinthians 5:17", "Galatians 2:20", "Matthew 28:19–20")),
    )
    val rhythms = listOf(
        "Read slowly and notice the invitation.",
        "Ask what this reveals about Jesus.",
        "Name what resists this teaching in you.",
        "Practice one concrete response before evening.",
        "Review the phase and carry one sentence forward.",
    )
    return Pilgrimage("jesus40", "40 Days with Jesus", "A sustained journey through the character and way of Christ.", buildList {
        phases.forEach { phase -> phase.references.forEachIndexed { index, ref -> add(PilgrimageDay("${phase.title} · ${index + 1}", ref, phase.reflection, rhythms[index])) } }
    })
}

@Composable
fun FormationScreen(state: FormationState, onUpdate: (FormationState) -> Unit, onWrite: (WriteResult) -> Unit) {
    var examen by remember { mutableStateOf(false) }
    var family by remember { mutableStateOf(false) }
    var crisis by remember { mutableStateOf(false) }
    var paths by remember { mutableStateOf(false) }
    val currentPath = PILGRIMAGES.firstOrNull { it.id == state.pilgrimageId }

    LazyColumn(contentPadding = PaddingValues(bottom = 130.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Practices", fontFamily = BookSerif, fontSize = 27.sp)
            Text("A quiet rhythm for living the Word—not another scoreboard.", color = Muted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
        }
        item {
            PracticeCard("RULE OF LIFE", "Build a gentle daily rhythm", Icons.Outlined.Schedule) {
                RhythmSwitch("Morning Word", state.morningWord) { onUpdate(state.copy(morningWord = it)) }
                RhythmSwitch("Midday prayer", state.middayPrayer) { onUpdate(state.copy(middayPrayer = it)) }
                RhythmSwitch("Evening examen", state.eveningExamen) { onUpdate(state.copy(eveningExamen = it)) }
                Text("Sabbath rhythm", color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday").forEach { day ->
                        FilterChip(selected = state.sabbathDay == day, onClick = { onUpdate(state.copy(sabbathDay = day)) }, label = { Text(day.take(3), fontSize = 11.sp) })
                    }
                }
            }
        }
        item {
            PracticeActionCard(
                "PILGRIMAGE", currentPath?.title ?: "Begin a seven-day journey",
                currentPath?.let { "Day ${(state.pilgrimageDay + 1).coerceAtMost(it.days.size)} of ${it.days.size}" } ?: "Peace · Forgiveness · Direction",
                Icons.Outlined.Explore,
            ) { paths = true }
        }
        item { PracticeActionCard("EVENING EXAMEN", "Notice the day with honesty", "Gratitude · heaviness · forgiveness · surrender", Icons.Outlined.NightsStay) { examen = true } }
        item { PracticeActionCard("FAMILY ALTAR", "Pray and remember together", state.familyNames.ifBlank { "A private household rhythm" }, Icons.Outlined.Groups) { family = true } }
        item {
            Surface(
                onClick = { crisis = true }, shape = RoundedCornerShape(22.dp), color = Color(0xFFFFF7F4),
                border = BorderStroke(1.dp, Color(0xFFE9C8BD)), modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.HealthAndSafety, "Open Crisis Mode", tint = Color(0xFF9C4A35))
                    Column(Modifier.weight(1f).padding(start = 14.dp)) { Text("Crisis Mode", fontWeight = FontWeight.SemiBold); Text("Grounding and human help when everything feels too much", color = Muted, fontSize = 11.5.sp) }
                    Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Muted)
                }
            }
        }
    }

    if (paths) PilgrimageDialog(state, onUpdate, onWrite) { paths = false }
    if (examen) ExamenDialog(onDismiss = { examen = false }) { onWrite(it); examen = false }
    if (family) FamilyDialog(state, onUpdate, onWrite) { family = false }
    if (crisis) CrisisDialog(state, onUpdate) { crisis = false }
}

@Composable private fun PracticeCard(kicker: String, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = Color.White, border = BorderStroke(1.dp, Hairline), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Gold); Column(Modifier.padding(start = 12.dp)) { Text(kicker, color = Gold, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold); Text(title, fontWeight = FontWeight.SemiBold) } }
            Column(Modifier.padding(top = 12.dp), content = content)
        }
    }
}

@Composable private fun PracticeActionCard(kicker: String, title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(22.dp), color = Color.White, border = BorderStroke(1.dp, Hairline), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Gold); Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(kicker, color = Gold, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold); Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 3.dp)); Text(subtitle, color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 3.dp)) }; Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Muted)
        }
    }
}

@Composable private fun RhythmSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) = Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f)); Switch(checked, onCheckedChange = onChange) }

@Composable private fun PilgrimageDialog(state: FormationState, onUpdate: (FormationState) -> Unit, onWrite: (WriteResult) -> Unit, onDismiss: () -> Unit) {
    val path = PILGRIMAGES.firstOrNull { it.id == state.pilgrimageId }
    if (path == null) AlertDialog(onDismissRequest = onDismiss, title = { Text("Choose a pilgrimage") }, text = { Column { PILGRIMAGES.forEach { item -> Surface(onClick = { onUpdate(state.copy(pilgrimageId = item.id, pilgrimageDay = 0)) }, color = Color(0xFFF8F4EA), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Column(Modifier.padding(14.dp)) { Text(item.title, fontWeight = FontWeight.SemiBold); Text(item.subtitle, color = Muted, fontSize = 11.5.sp) } } } } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } })
    else {
        val index = state.pilgrimageDay.coerceIn(0, path.days.lastIndex); val day = path.days[index]
        AlertDialog(onDismissRequest = onDismiss, title = { Text(day.title, fontFamily = BookSerif) }, text = { Column { Text("DAY ${index + 1} · ${day.reference}", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(day.reflection, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 12.dp)); Surface(color = Gold.copy(alpha = .1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) { Text("TODAY · ${day.action}", fontSize = 12.sp, modifier = Modifier.padding(12.dp)) } } }, confirmButton = { TextButton(onClick = { onWrite(WriteResult("🙏", "${path.title} — ${day.title}\n${day.action}", "", day.reference, null, "pilgrimage", false)); if (index == path.days.lastIndex) onUpdate(state.copy(pilgrimageId = "", pilgrimageDay = 0)) else onUpdate(state.copy(pilgrimageDay = index + 1)); onDismiss() }) { Text(if (index == path.days.lastIndex) "Complete journey" else "Carry this today") } }, dismissButton = { TextButton(onClick = { onUpdate(state.copy(pilgrimageId = "", pilgrimageDay = 0)); onDismiss() }) { Text("Leave journey") } })
    }
}

@Composable private fun ExamenDialog(onDismiss: () -> Unit, onSave: (WriteResult) -> Unit) {
    var gratitude by remember { mutableStateOf("") }; var heavy by remember { mutableStateOf("") }; var release by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Evening examen", fontFamily = BookSerif) }, text = { Column { Text("Where did you receive grace today?", color = Muted, fontSize = 12.sp); OutlinedTextField(gratitude, { gratitude = it }, modifier = Modifier.fillMaxWidth()); Text("What felt heavy or unloving?", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)); OutlinedTextField(heavy, { heavy = it }, modifier = Modifier.fillMaxWidth()); Text("What do you want to release?", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)); OutlinedTextField(release, { release = it }, modifier = Modifier.fillMaxWidth()) } }, confirmButton = { TextButton(onClick = { onSave(WriteResult("✨", "Grace I noticed: $gratitude\n\nWhat felt heavy: $heavy\n\nWhat I release: $release", gratitude, null, null, "examen", release.isNotBlank())) }) { Text("Close the day") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable private fun FamilyDialog(state: FormationState, onUpdate: (FormationState) -> Unit, onWrite: (WriteResult) -> Unit, onDismiss: () -> Unit) {
    var names by remember { mutableStateOf(state.familyNames) }; var gratitude by remember { mutableStateOf("") }; var prayer by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Family altar", fontFamily = BookSerif) }, text = { Column { OutlinedTextField(names, { names = it; onUpdate(state.copy(familyNames = it)) }, label = { Text("Our household") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(gratitude, { gratitude = it }, label = { Text("What are we grateful for?") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)); OutlinedTextField(prayer, { prayer = it }, label = { Text("What are we praying for?") }, minLines = 3, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) } }, confirmButton = { TextButton(onClick = { onWrite(WriteResult("🙏", "Family altar${names.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}\n\n$prayer", gratitude, null, null, "family", prayer.isNotBlank())); onDismiss() }) { Text("Save this moment") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable private fun CrisisDialog(state: FormationState, onUpdate: (FormationState) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current; var phone by remember { mutableStateOf(state.trustedPhone) }
    AlertDialog(onDismissRequest = onDismiss, icon = { Icon(Icons.Outlined.HealthAndSafety, null, tint = Color(0xFF9C4A35)) }, title = { Text("You do not have to carry this alone") }, text = { Column { Text("Put both feet on the floor. Name five things you can see, four you can feel, three you can hear, two you can smell, and one you can taste.", fontSize = 13.5.sp, lineHeight = 21.sp); Text("If you may hurt yourself or someone else, contact local emergency services or a crisis professional now. MANNA is not an emergency service.", color = Color(0xFF9C4A35), fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 14.dp)); OutlinedTextField(phone, { phone = it; onUpdate(state.copy(trustedPhone = it)) }, label = { Text("Trusted person's phone") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)); if (phone.isNotBlank()) Button(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}"))) }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Call my trusted person") } } }, confirmButton = { TextButton(onClick = onDismiss) { Text("I am here") } })
}
