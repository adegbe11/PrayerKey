package com.prayerkey.manna.ui.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class PrayerSessionRecord(
    val startedAt: Long,
    val minutes: Int,
    val mode: String,
    val completed: Boolean,
    val notes: String,
)

class PrayerJourneyStore(context: Context) {
    private val prefs = context.getSharedPreferences("prayer_journey", Context.MODE_PRIVATE)

    fun records(): List<PrayerSessionRecord> = runCatching {
        val array = JSONArray(prefs.getString("sessions", "[]"))
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            PrayerSessionRecord(
                item.getLong("startedAt"), item.getInt("minutes"), item.getString("mode"),
                item.optBoolean("completed"), item.optString("notes"),
            )
        }
    }.getOrDefault(emptyList())

    fun save(record: PrayerSessionRecord) {
        val all = records().toMutableList().apply { add(0, record) }.take(400)
        val array = JSONArray()
        all.forEach { record ->
            array.put(JSONObject().apply {
                put("startedAt", record.startedAt); put("minutes", record.minutes)
                put("mode", record.mode); put("completed", record.completed); put("notes", record.notes)
            })
        }
        prefs.edit().putString("sessions", array.toString()).apply()
    }
}

private val prayerSections = listOf(
    "Praise and worship" to "Tell God who He is. Thank Him for His goodness before asking for anything.",
    "Thanksgiving" to "Name three gifts from today, including one small thing you nearly missed.",
    "Repentance" to "Speak honestly about what was wrong. Ask for mercy, then receive God's forgiveness.",
    "My prayer points" to "Bring your needs to God one by one. Be clear, simple and truthful.",
    "Pray for others" to "Pray for your family, church, leaders and anyone God brings to mind.",
    "Scripture and closing" to "Read Philippians 4:6–7 slowly. End by trusting the answer and the timing to God.",
)

@Composable
fun PrayerSessionScreen(
    onClose: () -> Unit,
    onSaveNote: (String) -> Unit,
) {
    val context = LocalContext.current
    val store = remember { PrayerJourneyStore(context) }
    var stage by remember { mutableIntStateOf(0) }
    var minutes by remember { mutableIntStateOf(10) }
    var mode by remember { mutableStateOf("Guided") }
    var remaining by remember { mutableIntStateOf(minutes * 60) }
    var running by remember { mutableStateOf(false) }
    var section by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var noteOpen by remember { mutableStateOf(false) }
    val startedAt = remember { System.currentTimeMillis() }
    BackHandler { if (stage == 0) onClose() else stage = 0 }

    LaunchedEffect(running, remaining) {
        if (running && remaining > 0) { delay(1000); remaining-- }
        if (remaining == 0) running = false
    }

    Box(Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding()) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 34.dp)) {
            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = { if (stage == 0) onClose() else stage = 0 },
                    modifier = Modifier.size(44.dp), shape = CircleShape,
                    color = Color.White.copy(alpha = .88f), border = BorderStroke(1.dp, Pk.Hair), shadowElevation = 2.dp,
                ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Pk.Charcoal, modifier = Modifier.size(20.dp)) } }
                Text(if (stage == 0) "Prayer session" else "Praying now", Modifier.weight(1f), fontFamily = DisplaySerif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Pk.Charcoal)
                if (stage == 1) TextButton({ noteOpen = true }) { Text("+ NOTE", color = Pk.Oxblood, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(34.dp))
            if (stage == 0) {
                Text("How long do you want to pray?", fontFamily = DisplaySerif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Pk.Charcoal)
                Surface(
                    Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = .84f), border = BorderStroke(1.dp, Pk.Hair), shadowElevation = 1.dp,
                ) {
                    Row(Modifier.padding(5.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(5, 10, 15, 30, 60).forEach { value ->
                            val selected = minutes == value
                            Box(
                                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(17.dp))
                                    .background(if (selected) Pk.Oxblood else Color.Transparent)
                                    .clickable { minutes = value },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("$value", fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (selected) Color.White else Pk.Charcoal)
                            }
                        }
                    }
                }
                Text("Choose your prayer style", Modifier.padding(top = 34.dp), fontFamily = DisplaySerif, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Pk.Charcoal)
                Surface(
                    Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(30.dp),
                    color = Color.White.copy(alpha = .86f), border = BorderStroke(1.dp, Pk.Hair), shadowElevation = 2.dp,
                ) {
                    Column {
                        listOf("Guided" to "Prayer prompts and Scripture", "Light guidance" to "A gentle prompt when you need it", "Timer only" to "Quiet space with no prompts").forEachIndexed { index, item ->
                            Row(
                                Modifier.fillMaxWidth().clickable { mode = item.first }.padding(horizontal = 20.dp, vertical = 17.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.size(25.dp).clip(CircleShape).border(1.5.dp, if (mode == item.first) Pk.Oxblood else Pk.Muted, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) { if (mode == item.first) Box(Modifier.size(13.dp).background(Pk.Oxblood, CircleShape)) }
                                Column(Modifier.weight(1f).padding(start = 16.dp)) {
                                    Text(item.first, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Pk.Charcoal)
                                    Text(item.second, Modifier.padding(top = 4.dp), fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.Muted)
                                }
                            }
                            if (index < 2) HorizontalDivider(Modifier.padding(start = 61.dp), color = Pk.Hair)
                        }
                    }
                }
                Button({ remaining = minutes * 60; running = true; stage = 1 }, Modifier.fillMaxWidth().padding(top = 28.dp).height(60.dp), shape = RoundedCornerShape(30.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) {
                    Text("Start praying", fontWeight = FontWeight.Bold)
                }
            } else {
                val active = prayerSections[section]
                Text(String.format(java.util.Locale.US, "%02d:%02d", remaining / 60, remaining % 60), Modifier.align(Alignment.CenterHorizontally), fontFamily = DisplaySerif, fontSize = 58.sp, fontWeight = FontWeight.Bold, color = Pk.Oxblood)
                LinearProgressIndicator({ 1f - remaining.toFloat() / (minutes * 60).coerceAtLeast(1) }, Modifier.fillMaxWidth().padding(top = 14.dp).height(5.dp), color = Pk.Oxblood, trackColor = Pk.Oxblood.copy(alpha = .1f))
                Surface(Modifier.fillMaxWidth().padding(top = 30.dp), shape = RoundedCornerShape(30.dp), color = Pk.DeepDark) {
                    Column(Modifier.padding(26.dp)) {
                        Text("${section + 1} OF ${prayerSections.size}", fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Pk.GoldLight)
                        Text(active.first, Modifier.padding(top = 12.dp), fontFamily = DisplaySerif, fontSize = 31.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        if (mode != "Timer only") Text(active.second, Modifier.padding(top = 16.dp), fontFamily = UtilitySans, fontSize = 17.sp, lineHeight = 25.sp, color = Color.White.copy(alpha = .82f))
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton({ running = !running }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp)) { Text(if (running) "Pause" else "Continue", color = Pk.Oxblood) }
                    Button({ if (section < prayerSections.lastIndex) section++ else remaining = 0 }, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) { Text(if (section == prayerSections.lastIndex) "Finish" else "Next") }
                }
                TextButton({ section = (section + 1).coerceAtMost(prayerSections.lastIndex) }, Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)) { Text("I don't know what to pray next", color = Pk.Oxblood) }
                Button({
                    store.save(PrayerSessionRecord(startedAt, minutes, mode, true, notes))
                    if (notes.isNotBlank()) onSaveNote(notes)
                    onClose()
                }, Modifier.fillMaxWidth().padding(top = 22.dp).height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Charcoal)) { Text("Complete session") }
            }
        }
    }
    if (noteOpen) AlertDialog(
        onDismissRequest = { noteOpen = false }, title = { Text("Prayer note", fontFamily = DisplaySerif) },
        text = { OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), minLines = 5, placeholder = { Text("Write what came to mind while you prayed") }) },
        confirmButton = { TextButton({ noteOpen = false }) { Text("Keep note", color = Pk.Oxblood) } },
        dismissButton = { TextButton({ noteOpen = false }) { Text("Cancel") } },
    )
}

@Composable
fun PrayerCalendarScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val records = remember(context) { PrayerJourneyStore(context).records() }
    val zone = remember { ZoneId.systemDefault() }
    val grouped = remember(records) { records.groupBy { Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate() } }
    BackHandler(onBack = onClose)
    Column(Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding().verticalScroll(rememberScrollState()).padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClose) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Text("Prayer calendar", Modifier.weight(1f), fontFamily = DisplaySerif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Pk.Charcoal)
        }
        val totalMinutes = records.filter { it.completed }.sumOf { it.minutes }
        Surface(Modifier.fillMaxWidth().padding(top = 22.dp), shape = RoundedCornerShape(28.dp), color = Pk.DeepDark) {
            Row(Modifier.padding(24.dp), horizontalArrangement = Arrangement.SpaceAround) {
                CalendarStat(records.count { it.completed }.toString(), "sessions")
                CalendarStat(totalMinutes.toString(), "minutes")
                CalendarStat(grouped.size.toString(), "days")
            }
        }
        Text("Your prayer history", Modifier.padding(top = 30.dp, bottom = 8.dp), fontFamily = DisplaySerif, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Pk.Charcoal)
        grouped.toSortedMap(compareByDescending { it }).forEach { (date, sessions) ->
            Surface(Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(22.dp), color = Color.White, border = BorderStroke(1.dp, Pk.Hair)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(Pk.Oxblood.copy(alpha = .09f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Check, null, tint = Pk.Oxblood) }
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), fontWeight = FontWeight.SemiBold, color = Pk.Charcoal)
                        Text("${sessions.size} session${if (sessions.size == 1) "" else "s"} · ${sessions.sumOf { it.minutes }} minutes", Modifier.padding(top = 4.dp), fontSize = 12.sp, color = Pk.Muted)
                    }
                }
            }
        }
    }
}

@Composable private fun CalendarStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = DisplaySerif, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Pk.GoldLight)
        Text(label, fontFamily = UtilitySans, fontSize = 10.sp, color = Color.White.copy(alpha = .65f))
    }
}
