package com.prayerkey.manna.ui.screens

import com.prayerkey.manna.ui.platform.platformContext
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.FormationState
import com.prayerkey.manna.ui.journal.WriteResult
import com.prayerkey.manna.ui.theme.*

data class PilgrimageDay(val title: String, val reference: String, val reflection: String, val action: String)
data class Pilgrimage(val id: String, val title: String, val subtitle: String, val days: List<PilgrimageDay>)

val PILGRIMAGES = listOf(
    Pilgrimage("peace", "Seven Days of Peace", "For when worry will not let you sleep.", listOf(
        PilgrimageDay("Say what it is", "1 Peter 5:7", "You do not have to carry it by yourself. He asked you to hand it over.", "Write down the one thing worrying you most."),
        PilgrimageDay("Only today", "Matthew 6:34", "God gives you strength for today, not for every bad thing that might happen next year.", "Before your next decision, stop and take five slow breaths."),
        PilgrimageDay("Ask plainly", "Philippians 4:6-7", "You are allowed to ask for exactly what you need.", "Turn one worry into one clear request."),
        PilgrimageDay("Rest", "Matthew 11:28", "Jesus says come and rest. He does not say finish everything first.", "Keep twenty minutes today with nothing in them."),
        PilgrimageDay("You are not alone", "Isaiah 41:10", "Fear thou not; for I am with thee. Say it out loud when the fear comes back.", "Learn the verse by heart today."),
        PilgrimageDay("Count three things", "Psalm 118:24", "When you thank God for small things, the big worry stops filling the whole room.", "Write down three ordinary things you are glad of."),
        PilgrimageDay("Pass it on", "Matthew 5:9", "Now that you have some peace, give some away.", "Be the calm person in one conversation today."),
    )),
    Pilgrimage("forgiveness", "Seven Days of Forgiveness", "For when someone hurt you and you cannot let it go.", listOf(
        PilgrimageDay("Say what happened", "Psalm 62:8", "Forgiving someone does not mean pretending it was small.", "Write down what happened, as it happened."),
        PilgrimageDay("Forgive, but stay safe", "Proverbs 4:23", "You can forgive someone and still not let them back into your house.", "Decide one line you will not let them cross."),
        PilgrimageDay("Remember your own mercy", "Ephesians 4:32", "God forgave you first. Start there, not from being better than them.", "Think of one time you were forgiven when you did not deserve it."),
        PilgrimageDay("Leave the paying back to God", "Romans 12:19", "What they did was wrong. Paying them back is still not your job.", "Name the thing you want to get back at them for, and give it to God."),
        PilgrimageDay("Pray for them", "Luke 6:28", "You can pray for someone before you feel kind towards them. The feeling often comes later.", "Pray one honest sentence for their good."),
        PilgrimageDay("Your part", "Matthew 5:23-24", "Sometimes some of it was our fault too.", "If it is safe, say sorry for your part."),
        PilgrimageDay("Walk on", "Isaiah 43:18-19", "What they did is part of your story. It does not get to write the ending.", "Write what you would do if this no longer held you back."),
    )),
    Pilgrimage("calling", "Seven Days of Direction", "For when you have a decision to make and cannot see the way.", listOf(
        PilgrimageDay("Let go first", "Proverbs 3:5-6", "You cannot be led while you are still gripping the wheel.", "Write down the outcome you want, and give it to God."),
        PilgrimageDay("What you are already good at", "1 Peter 4:10", "God often leads through the gifts He has already given you.", "Write three ways you already help people without trying."),
        PilgrimageDay("Ask someone wise", "Proverbs 15:22", "Without counsel purposes are disappointed. Do not decide alone.", "Ask one older Christian what they see in you."),
        PilgrimageDay("Look at the fruit", "Galatians 5:22-23", "A good path makes you more loving and more patient, not more anxious.", "Ask what this choice is doing to you."),
        PilgrimageDay("Do the sums", "Luke 14:28", "Jesus told us to sit down and count the cost. Faith does not skip the sums.", "Write down what it will really cost, and who can help."),
        PilgrimageDay("One small step", "Psalm 119:105", "The lamp lights the next step, not the whole road.", "Do one small thing today that you could still undo."),
        PilgrimageDay("Look back in peace", "Colossians 3:15", "Feeling rushed is not the same as being led.", "Sit quietly and write what is clearer now than a week ago."),
    )),
    returnToFaith(),
    walkWithJesus(),
)

private fun returnToFaith(): Pilgrimage {
    data class Phase(val title: String, val reflection: String, val references: List<String>)
    val phases = listOf(
        Phase("Come home honestly", "You do not need to clean up before you come back. God is not shocked by your doubts or by how far you went.", listOf("Luke 15:20", "Psalm 139:23-24", "Mark 9:24", "Psalm 62:8", "1 John 1:9", "Matthew 11:28", "Lamentations 3:22-23")),
        Phase("Build again slowly", "Faith comes back the way it grew the first time: a little prayer, a little reading, a few people, every day.", listOf("Proverbs 3:5-6", "Psalm 119:105", "John 15:4", "Hebrews 10:24-25", "Romans 10:17", "Philippians 1:6", "Isaiah 40:31")),
        Phase("Live it out", "Coming back to God shows in how you treat the people around you.", listOf("Romans 12:2", "Micah 6:8", "Galatians 5:22-23", "1 Peter 4:10", "Matthew 5:14-16", "Colossians 3:12-14", "2 Corinthians 5:17")),
    )
    val actions = listOf(
        "Write one completely honest sentence to God.",
        "Read the passage twice and underline one phrase.",
        "Sit quietly for three minutes. You do not have to feel anything.",
        "Tell one person you trust where you really are.",
        "Do one small thing the passage asks.",
        "Write down one thing you want to believe again.",
        "Look back over the week. No marks out of ten.",
    )
    return Pilgrimage("return", "21 Days of Returning", "For when you drifted away and want to come back.", buildList {
        phases.forEach { phase -> phase.references.forEachIndexed { index, ref -> add(PilgrimageDay("${phase.title} · ${index + 1}", ref, phase.reflection, actions[index])) } }
    })
}

private fun walkWithJesus(): Pilgrimage {
    data class Phase(val title: String, val reflection: String, val references: List<String>)
    val phases = listOf(
        Phase("Come and see", "Jesus starts with an invitation: come and see.", listOf("John 1:39", "Matthew 4:19", "John 10:27", "Matthew 11:28", "Luke 9:23")),
        Phase("Paying attention", "Jesus noticed people other people walked past. Learn to notice with Him.", listOf("Mark 1:35", "Luke 10:39", "Matthew 6:26", "John 5:19", "Psalm 46:10")),
        Phase("What He is like", "Meek, merciful, pure in heart, a peacemaker. This is who you are becoming.", listOf("Matthew 5:5", "Matthew 5:7", "Matthew 5:8", "Matthew 5:9", "John 8:31-32")),
        Phase("Learning to pray", "Five days on the prayer Jesus taught His friends, one line at a time.", listOf("Matthew 6:9", "Matthew 6:10", "Matthew 6:11", "Matthew 6:12", "Matthew 6:13")),
        Phase("Each other", "Jesus never meant you to follow Him alone. His people forgive, serve and carry each other.", listOf("John 13:34", "Mark 10:45", "Matthew 18:20", "Galatians 6:2", "Ephesians 4:32")),
        Phase("The overlooked", "Loving your neighbour means the widow, the prisoner and the one nobody visits.", listOf("Luke 4:18", "Matthew 25:40", "Luke 10:36-37", "Micah 6:8", "James 1:27")),
        Phase("The cross", "Following Jesus will cost you something. He said so first.", listOf("Luke 22:42", "Mark 8:35", "John 12:24", "Romans 12:1", "Hebrews 12:2")),
        Phase("He is risen", "The forty days end at the empty tomb, and then send you back to ordinary life.", listOf("John 20:21", "Romans 6:4", "2 Corinthians 5:17", "Galatians 2:20", "Matthew 28:19-20")),
    )
    val rhythms = listOf(
        "Read it slowly, twice.",
        "Write what this shows you about Jesus.",
        "Notice the part of you that pushes back against it.",
        "Do one thing it asks before the evening.",
        "Write one sentence from this week to keep.",
    )
    return Pilgrimage("jesus40", "40 Days with Jesus", "Forty days reading about the life of Jesus.", buildList {
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
        }
        item {
            PracticeCard("RULE OF LIFE", "Plan your day around prayer", Icons.Outlined.Schedule) {
                RhythmSwitch("Morning Word", state.morningWord) { onUpdate(state.copy(morningWord = it)) }
                RhythmSwitch("Midday prayer", state.middayPrayer) { onUpdate(state.copy(middayPrayer = it)) }
                RhythmSwitch("Evening examen", state.eveningExamen) { onUpdate(state.copy(eveningExamen = it)) }
                Text("Your day of rest", color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday").forEach { day ->
                        FilterChip(selected = state.sabbathDay == day, onClick = { onUpdate(state.copy(sabbathDay = day)) }, label = { Text(day.take(3), fontSize = 11.sp) })
                    }
                }
            }
        }
        item {
            PracticeActionCard(
                "PILGRIMAGE", currentPath?.title ?: "Start a seven-day plan",
                currentPath?.let { "Day ${(state.pilgrimageDay + 1).coerceAtMost(it.days.size)} of ${it.days.size}" } ?: "Peace · Forgiveness · Direction",
                Icons.Outlined.Explore,
            ) { paths = true }
        }
        item { PracticeActionCard("EVENING EXAMEN", "Notice the day with honesty", "Gratitude · heaviness · forgiveness · surrender", Icons.Outlined.NightsStay) { examen = true } }
        item { PracticeActionCard("FAMILY ALTAR", "Pray and remember together", state.familyNames.ifBlank { "Prayer at home, together" }, Icons.Outlined.Groups) { family = true } }
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
            Icon(icon, null, tint = Gold); Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(kicker, color = Gold, fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold); Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 3.dp)) }; Icon(Icons.Outlined.KeyboardArrowRight, null, tint = Muted)
        }
    }
}

@Composable private fun RhythmSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) = Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(label, fontSize = 13.sp, modifier = Modifier.weight(1f)); Switch(checked, onCheckedChange = onChange) }

@Composable private fun PilgrimageDialog(state: FormationState, onUpdate: (FormationState) -> Unit, onWrite: (WriteResult) -> Unit, onDismiss: () -> Unit) {
    val path = PILGRIMAGES.firstOrNull { it.id == state.pilgrimageId }
    if (path == null) AlertDialog(onDismissRequest = onDismiss, title = { Text("Choose a pilgrimage") }, text = { Column { PILGRIMAGES.forEach { item -> Surface(onClick = { onUpdate(state.copy(pilgrimageId = item.id, pilgrimageDay = 0)) }, color = Color(0xFFF8F4EA), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Column(Modifier.padding(14.dp)) { Text(item.title, fontWeight = FontWeight.SemiBold) } } } } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } })
    else {
        val index = state.pilgrimageDay.coerceIn(0, path.days.lastIndex); val day = path.days[index]
        AlertDialog(onDismissRequest = onDismiss, title = { Text(day.title, fontFamily = BookSerif) }, text = { Column { Text("DAY ${index + 1} · ${day.reference}", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(day.reflection, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 12.dp)); Surface(color = Gold.copy(alpha = .1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) { Text("TODAY · ${day.action}", fontSize = 12.sp, modifier = Modifier.padding(12.dp)) } } }, confirmButton = { TextButton(onClick = { onWrite(WriteResult("🙏", "${path.title}, ${day.title}\n${day.action}", "", day.reference, null, "pilgrimage", false)); if (index == path.days.lastIndex) onUpdate(state.copy(pilgrimageId = "", pilgrimageDay = 0)) else onUpdate(state.copy(pilgrimageDay = index + 1)); onDismiss() }) { Text(if (index == path.days.lastIndex) "Finish" else "Carry this today") } }, dismissButton = { TextButton(onClick = { onUpdate(state.copy(pilgrimageId = "", pilgrimageDay = 0)); onDismiss() }) { Text("Stop") } })
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
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current; var phone by remember { mutableStateOf(state.trustedPhone) }
    AlertDialog(onDismissRequest = onDismiss, icon = { Icon(Icons.Outlined.HealthAndSafety, null, tint = Color(0xFF9C4A35)) }, title = { Text("You do not have to carry this alone") }, text = { Column { Text("Put both feet on the floor. Name five things you can see, four you can feel, three you can hear, two you can smell, and one you can taste.", fontSize = 13.5.sp, lineHeight = 21.sp); Text("If you may hurt yourself or someone else, contact local emergency services or a crisis professional now. PrayerKey is not an emergency service.", color = Color(0xFF9C4A35), fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 14.dp)); OutlinedTextField(phone, { phone = it; onUpdate(state.copy(trustedPhone = it)) }, label = { Text("Trusted person's phone") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)); if (phone.isNotBlank()) Button(onClick = { uriHandler.openUri("tel:" + com.prayerkey.manna.platform.encodeUrlComponent(phone)) }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Call my trusted person") } } }, confirmButton = { TextButton(onClick = onDismiss) { Text("I am here") } })
}
