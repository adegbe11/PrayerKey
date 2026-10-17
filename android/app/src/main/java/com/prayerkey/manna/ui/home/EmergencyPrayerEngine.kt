package com.prayerkey.manna.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.LocalPrayerTopics
import com.prayerkey.manna.data.PrayerPointEngine
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale

@Composable
internal fun EmergencyPrayerEngine(
    onClose: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    var topics by remember { mutableStateOf<List<PrayerTopic>>(emptyList()) }
    var search by remember { mutableStateOf("") }
    var need by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<PrayerTopic?>(null) }
    LaunchedEffect(Unit) { topics = withContext(Dispatchers.IO) { LocalPrayerTopics.load(context) } }
    selected?.let { topic ->
        EmergencyPrayerSession(topic, need, onBack = { selected = null }, onClose = onClose, onSave = onSave)
        return
    }
    val filtered = remember(search, topics) {
        if (search.isBlank()) topics.take(24) else topics.filter { topic ->
            topic.title.contains(search, true) || topic.category.contains(search, true) || topic.prayer.contains(search, true)
        }.take(50)
    }
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Close", tint = Pk.Charcoal) }
                Text("Private prayer", Modifier.weight(1f), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Pk.Oxblood)
            }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)) {
                item {
                    Text("What do you need prayer for?", fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 35.sp, lineHeight = 40.sp, color = Pk.Charcoal)
                    BasicTextField(need, { need = it }, textStyle = TextStyle(fontFamily = BookSerif, fontSize = 18.sp, color = Pk.Charcoal), modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(94.dp).background(Color.White, RoundedCornerShape(20.dp)).padding(16.dp), decorationBox = { field -> Box { if (need.isBlank()) Text("God, I need help with...", fontFamily = BookSerif, fontSize = 18.sp, color = Pk.Muted); field() } })
                    Text("Choose the closest situation", Modifier.padding(top = 25.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Pk.Oxblood)
                    BasicTextField(search, { search = it }, singleLine = true, textStyle = TextStyle(fontFamily = UtilitySans, fontSize = 15.sp, color = Pk.Charcoal), modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp).height(52.dp).background(Color.White, RoundedCornerShape(17.dp)).padding(horizontal = 16.dp, vertical = 15.dp), decorationBox = { field -> Box { if (search.isBlank()) Text("Search health, family, work, fear...", fontFamily = UtilitySans, fontSize = 14.sp, color = Pk.Muted); field() } })
                }
                items(filtered, key = { it.slug }) { topic ->
                    Surface(Modifier.fillMaxWidth().padding(bottom = 9.dp).clickable { selected = topic }, shape = RoundedCornerShape(18.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).background(Pk.Oxblood.copy(alpha = .08f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.SelfImprovement, null, tint = Pk.Oxblood, modifier = Modifier.size(20.dp)) }
                            Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
                                Text(topic.title.removePrefix("Prayer for "), fontFamily = BookSerif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Pk.Charcoal)
                                Text(topic.category, Modifier.padding(top = 3.dp), fontFamily = UtilitySans, fontSize = 10.sp, color = Pk.Muted)
                            }
                            Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Oxblood)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyPrayerSession(topic: PrayerTopic, need: String, onBack: () -> Unit, onClose: () -> Unit, onSave: (String, String) -> Unit) {
    val points = remember(topic.slug) { PrayerPointEngine.pointsFor(topic, LocalDate.now().dayOfYear).take(10) }
    val scripture = topic.scripture.firstOrNull()
    var checked by remember(topic.slug) { mutableStateOf(emptySet<Int>()) }
    var seconds by remember { mutableIntStateOf(5 * 60) }
    var running by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    LaunchedEffect(running, seconds) { if (running && seconds > 0) { delay(1_000); seconds-- } else if (seconds == 0) running = false }
    Surface(Modifier.fillMaxSize(), color = Pk.DeepDark) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Color.White, modifier = Modifier.size(16.dp)) }
                Text("Pray now", Modifier.weight(1f), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Pk.GoldLight)
                IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Close", tint = Color.White) }
            }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)) {
                item {
                    Text(topic.title.removePrefix("Prayer for "), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 39.sp, color = Color.White)
                    if (need.isNotBlank()) Text(need, Modifier.padding(top = 12.dp), fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 17.sp, lineHeight = 25.sp, color = Color.White.copy(alpha = .68f))
                    Surface(Modifier.fillMaxWidth().padding(top = 22.dp), shape = RoundedCornerShape(20.dp), color = Pk.Gold.copy(alpha = .12f)) {
                        Column(Modifier.padding(18.dp)) {
                            Text(scripture?.second ?: "God is our refuge and strength, a very present help in trouble.", fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 27.sp, color = Color.White)
                            Text((scripture?.first ?: "Psalm 46:1"), Modifier.padding(top = 10.dp), fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = 1.2.sp, color = Pk.GoldLight)
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 18.dp).background(Color.White.copy(alpha = .08f), RoundedCornerShape(18.dp)).clickable { if (seconds == 0) seconds = 300; running = !running }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Timer, null, tint = Pk.GoldLight)
                        Text("%02d:%02d".format(Locale.US, seconds / 60, seconds % 60), Modifier.weight(1f).padding(start = 12.dp), fontFamily = DisplaySerif, fontSize = 25.sp, color = Color.White)
                        Text(if (running) "Pause" else "Start", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Pk.GoldLight)
                    }
                    Text("Pray each point", Modifier.padding(top = 25.dp, bottom = 5.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Pk.GoldLight)
                }
                items(points.size) { index ->
                    val done = index in checked
                    Row(Modifier.fillMaxWidth().clickable { checked = if (done) checked - index else checked + index }.padding(vertical = 15.dp), verticalAlignment = Alignment.Top) {
                        Text("${index + 1}", Modifier.width(26.dp).padding(top = 3.dp), fontFamily = UtilitySans, fontSize = 10.sp, color = Color.White.copy(alpha = .35f))
                        Text(points[index], Modifier.weight(1f), fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 26.sp, color = if (done) Color.White.copy(alpha = .4f) else Color.White)
                        Box(Modifier.size(23.dp).background(if (done) Pk.GoldLight else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) { if (done) Icon(Icons.Outlined.Check, null, tint = Pk.DeepDark, modifier = Modifier.size(14.dp)) }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = .09f))
                }
                item {
                    Text("Full prayer", Modifier.padding(top = 26.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Pk.GoldLight)
                    Text(topic.prayer, Modifier.padding(top = 10.dp), fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 19.sp, lineHeight = 29.sp, color = Color.White)
                    OutlinedButton(onClick = { onSave(topic.title, need.ifBlank { topic.prayer }); saved = true }, enabled = !saved, modifier = Modifier.fillMaxWidth().padding(top = 22.dp).height(52.dp), shape = RoundedCornerShape(17.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Pk.GoldLight), colors = ButtonDefaults.outlinedButtonColors(contentColor = Pk.GoldLight)) { Text(if (saved) "Saved to my prayers" else "Save to my prayers", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    Spacer(Modifier.height(24.dp))
                }
            }
            Button(onClick = onClose, enabled = checked.size == points.size, modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp).height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.GoldLight, contentColor = Pk.DeepDark)) {
                Text(if (checked.size == points.size) "Finish prayer" else "Pray each point to finish", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
