package com.prayerkey.manna.ui.home

import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.platform.format
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.resources.*
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.*
import com.prayerkey.manna.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

@Composable
fun DailyRhythmExperience(moment: String, onClose: () -> Unit, onComplete: () -> Unit) {
    val today = remember { today() }
    val passage = remember(today) { passageFor(today) }
    CinematicDailyRhythmPage(moment, today, passage, onClose, onComplete)
}

@Composable
private fun CinematicDailyRhythmPage(
    moment: String,
    date: LocalDate,
    passage: DailyPassage,
    onClose: () -> Unit,
    onComplete: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val prefs = remember { context.keyValues("daily_rhythm_notes") }
    var topics by remember { mutableStateOf<List<PrayerTopic>>(emptyList()) }
    var note by remember(moment, date) { mutableStateOf(prefs.getString("${date}_$moment", "").orEmpty()) }
    LaunchedEffect(Unit) { topics = withContext(ioDispatcher) { LocalPrayerTopics.load(context) } }
    val prayer = remember(date, topics) { dailyPrayerFor(date, topics) }
    val title = when (moment) {
        "morning" -> "Morning prayer"
        "reflection" -> "Prayer and reflection"
        else -> "Evening prayer"
    }
    val art = when (moment) {
        "morning" -> Res.drawable.rhythm_morning
        "reflection" -> Res.drawable.rhythm_reflection
        else -> Res.drawable.rhythm_evening
    }
    val reference = when (moment) {
        "morning" -> prayer.ref
        "reflection" -> passage.reference
        else -> "Psalm 92:2"
    }
    val opening = when (moment) {
        "morning" -> prayer.verse
        "reflection" -> passage.introduction
        else -> "It is good to proclaim Your faithful love in the morning and Your faithfulness at night."
    }
    val body = when (moment) {
        "morning" -> prayer.prayer
        "reflection" -> "Father, search my heart in the light of Your Word. Correct what is wrong in me, strengthen what is weak, and give me grace to obey what You reveal. Let Your truth shape my thoughts, my words, and my decisions today. Show me what You are saying through ${passage.reference}, and lead me in the way that pleases You. In Jesus’ name, Amen."
        else -> "Father, thank You for carrying me through this day. Forgive what was wrong, heal what was painful, and receive what was good. I release every unfinished matter into Your hands. Watch over my home, renew my body and mind, and give me peaceful sleep. Prepare me to meet tomorrow with faith. In Jesus’ name, Amen."
    }

    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(Modifier.fillMaxWidth().height(470.dp)) {
                    Image(painterResource(art), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(.16f), Color.Transparent, Color.Black.copy(.82f)))))
                    IconButton(onClick = onClose, modifier = Modifier.statusBarsPadding().padding(10.dp).align(Alignment.TopStart).background(Color.Black.copy(.28f), RoundedCornerShape(50))) {
                        Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Color.White, modifier = Modifier.size(17.dp))
                    }
                    Column(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                        Text(date.format("EEEE, d MMMM"), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.4.sp, color = Pk.GoldLight)
                        Text(title, Modifier.padding(top = 8.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 39.sp, color = Color.White)
                        Text(reference, Modifier.padding(top = 12.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White.copy(.82f))
                    }
                }
                Column(Modifier.fillMaxWidth().offset(y = (-20).dp).clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)).background(Pk.Cream).padding(horizontal = 24.dp, vertical = 28.dp)) {
                    Text(opening, fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 20.sp, lineHeight = 30.sp, color = Pk.Charcoal)
                    Box(Modifier.padding(top = 22.dp).width(44.dp).height(2.dp).background(Pk.Gold))
                    Text(body, Modifier.padding(top = 22.dp), fontFamily = BookSerif, fontSize = 19.sp, lineHeight = 30.sp, color = Pk.Charcoal)
                    if (moment != "morning") {
                        Text("Write what you want to remember", Modifier.padding(top = 28.dp, bottom = 10.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Pk.Charcoal)
                        RhythmNoteField(note) { note = it; prefs.edit().putString("${date}_$moment", it).apply() }
                    }
                    Button(onClick = onComplete, modifier = Modifier.fillMaxWidth().padding(top = 28.dp).height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) {
                        Icon(Icons.Outlined.Check, null, Modifier.size(18.dp))
                        Text("Amen", Modifier.padding(start = 9.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.navigationBarsPadding().height(10.dp))
                }
            }
        }
    }
}

@Composable private fun RhythmNoteField(value: String, onChange: (String) -> Unit) {
    BasicTextField(value, onChange, textStyle = TextStyle(fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, color = Pk.Charcoal), modifier = Modifier.fillMaxWidth().height(116.dp).background(Color.White, RoundedCornerShape(18.dp)).padding(16.dp), decorationBox = { field -> Box { if (value.isBlank()) Text("Write here…", fontFamily = BookSerif, fontSize = 17.sp, color = Pk.Muted); field() } })
}

@Composable
private fun DailyRhythmPage(
    moment: String,
    date: LocalDate,
    passage: DailyPassage,
    onClose: () -> Unit,
    onComplete: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val prefs = remember { context.keyValues("daily_rhythm_notes") }
    var topics by remember { mutableStateOf<List<PrayerTopic>>(emptyList()) }
    var verses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var note by remember(moment, date) { mutableStateOf(prefs.getString("${date}_$moment", "").orEmpty()) }
    LaunchedEffect(Unit) { topics = withContext(ioDispatcher) { LocalPrayerTopics.load(context) } }
    LaunchedEffect(passage.book, passage.chapter) {
        verses = withContext(ioDispatcher) { OfflineBible(context).chapter(passage.book, passage.chapter) }
    }
    val prayer = remember(date, topics) { dailyPrayerFor(date, topics) }
    val title = when (moment) {
        "morning" -> "Morning prayer"
        "devotional" -> "Daily devotional"
        "bible" -> "Today's Bible reading"
        "reflection" -> "Guided reflection"
        else -> "Evening prayer"
    }
    val subtitle = date.format("EEEE, d MMMM")

    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Charcoal, modifier = Modifier.size(16.dp)) }
                Text("PRAYERKEY", Modifier.weight(1f), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.5.sp, color = Pk.Oxblood)
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                Text(subtitle, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp, color = Pk.Muted, modifier = Modifier.padding(top = 10.dp))
                Text(title, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 35.sp, lineHeight = 40.sp, color = Pk.Charcoal, modifier = Modifier.padding(top = 8.dp))
                when (moment) {
                    "morning" -> {
                        ScriptureBlock(prayer.ref, prayer.verse)
                        RhythmHeading("Pray for today")
                        Text(prayer.prayer, fontFamily = BookSerif, fontSize = 19.sp, lineHeight = 29.sp, color = Pk.Charcoal)
                    }
                    "reflection" -> {
                        ScriptureBlock(passage.reference, passage.introduction)
                        RhythmHeading("Prayer for understanding")
                        Text("Father, search my heart in the light of Your Word. Correct what is wrong in me, strengthen what is weak, and give me grace to obey what You reveal. Let Your truth shape my thoughts, my words, and my decisions today. In Jesus' name, Amen.", fontFamily = DisplaySerif, fontSize = 22.sp, lineHeight = 31.sp, color = Pk.Charcoal)
                        RhythmHeading("Personal prayer")
                        Text("Lord, concerning ${passage.reference}—show me what You are saying and lead me in the way that pleases You. In Jesus' name, Amen.", fontFamily = BookSerif, fontSize = 19.sp, lineHeight = 28.sp, color = Pk.Charcoal)
                        RhythmNote(note) { note = it; prefs.edit().putString("${date}_$moment", it).apply() }
                    }
                    else -> {
                        ScriptureBlock("Psalm 92:2", "It is good to proclaim Your faithful love in the morning and Your faithfulness at night.")
                        RhythmHeading("Look back with God")
                        listOf("Where did you notice God's help today?", "What burden do you need to release before sleep?", "Who needs your prayer tonight?").forEachIndexed { index, prompt ->
                            Text("${index + 1}.  $prompt", fontFamily = BookSerif, fontSize = 19.sp, lineHeight = 28.sp, color = Pk.Charcoal, modifier = Modifier.padding(top = 14.dp))
                        }
                        RhythmHeading("Evening prayer")
                        Text("Father, thank You for carrying me through this day. Forgive what was wrong, heal what was painful, and receive what was good. I release every unfinished matter into Your hands. Watch over my home, renew my body and mind, and give me peaceful sleep. Prepare me to meet tomorrow with faith. In Jesus' name, Amen.", fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 19.sp, lineHeight = 29.sp, color = Pk.Charcoal)
                        RhythmNote(note) { note = it; prefs.edit().putString("${date}_$moment", it).apply() }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
            Button(onClick = onComplete, modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp).height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) {
                Text("Amen", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable private fun ScriptureBlock(reference: String, text: String) {
    Surface(Modifier.fillMaxWidth().padding(top = 24.dp), shape = RoundedCornerShape(22.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair)) {
        Column(Modifier.padding(20.dp)) {
            Text(reference, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.2.sp, color = Pk.Oxblood)
            Text(text, fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 19.sp, lineHeight = 28.sp, color = Pk.Charcoal, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable private fun RhythmHeading(text: String) { Text(text, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.3.sp, color = Pk.Oxblood, modifier = Modifier.padding(top = 26.dp, bottom = 10.dp)) }

@Composable private fun RhythmNote(value: String, onChange: (String) -> Unit) {
    RhythmHeading("Write what you want to remember")
    BasicTextField(value, onChange, textStyle = TextStyle(fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, color = Pk.Charcoal), modifier = Modifier.fillMaxWidth().height(110.dp).background(Color.White, RoundedCornerShape(18.dp)).padding(16.dp), decorationBox = { field -> Box { if (value.isBlank()) Text("Write here...", fontFamily = BookSerif, fontSize = 17.sp, color = Pk.Muted); field() } })
}
