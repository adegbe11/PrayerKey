package com.prayerkey.manna.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.LocalPrayerTopics
import com.prayerkey.manna.data.PrayerPointEngine
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val PointBg = Color(0xFFF7F5FA)
private val PointInk = Color(0xFF1D1D1F)
private val PointMuted = Color(0xFF6E6E73)
private val PointLine = Color(0xFFE5DFEA)

private enum class PointPage { LIBRARY, OVERVIEW, PRAYING }

private val featuredSituations = listOf(
    "Salvation", "Repentance", "Thanksgiving", "Spiritual growth", "Faith",
    "Protection", "Healing", "Marriage", "Children", "Family", "Finances",
    "Business", "Career", "Job search", "Exams", "Direction", "Open doors",
    "Breakthrough", "Fear", "Anxiety", "Grief", "Restoration", "Travel", "Purpose",
)

@Composable
fun PrayerPointsScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    var topics by remember { mutableStateOf<List<PrayerTopic>>(emptyList()) }
    var page by remember { mutableStateOf(PointPage.LIBRARY) }
    var selected by remember { mutableStateOf<PrayerTopic?>(null) }
    var query by remember { mutableStateOf("") }
    var custom by remember { mutableStateOf("") }
    var length by remember { mutableIntStateOf(40) }

    LaunchedEffect(Unit) {
        topics = withContext(Dispatchers.IO) { LocalPrayerTopics.load(context) }
    }

    fun openTopic(label: String) {
        val words = label.lowercase().split(Regex("\\W+")).filter { it.length > 2 }
        val match = topics.map { topic ->
            topic to words.count { it in (topic.title + " " + topic.category).lowercase() }
        }.maxByOrNull { it.second }?.takeIf { it.second > 0 }?.first
        selected = match ?: PrayerTopic(
            slug = "custom-${label.hashCode()}",
            title = label,
            category = "Personal",
            prayer = "Father, I bring $label before You. Give me wisdom, grace, courage, and peace. Lead me by Your Word and let Your will be done in Jesus' name, Amen.",
            scripture = listOf("Philippians 4:6-7" to "Bring this matter to God in prayer with thanksgiving."),
            prayerPoints = emptyList(),
        )
        page = PointPage.OVERVIEW
    }

    BackHandler {
        when (page) {
            PointPage.PRAYING -> page = PointPage.OVERVIEW
            PointPage.OVERVIEW -> page = PointPage.LIBRARY
            PointPage.LIBRARY -> onClose()
        }
    }

    when (page) {
        PointPage.LIBRARY -> PrayerPointsLibrary(
            topics = topics,
            query = query,
            custom = custom,
            onQuery = { query = it },
            onCustom = { custom = it },
            onClose = onClose,
            onSelect = ::openTopic,
            onCreate = { if (custom.isNotBlank()) openTopic(custom.trim()) },
        )
        PointPage.OVERVIEW -> selected?.let { topic ->
            PrayerPointsOverview(
                topic = topic,
                selectedLength = length,
                onLength = { length = it },
                onBack = { page = PointPage.LIBRARY },
                onStart = { page = PointPage.PRAYING },
            )
        }
        PointPage.PRAYING -> selected?.let { topic ->
            PrayerPointMode(topic, length, { page = PointPage.OVERVIEW }) { page = PointPage.LIBRARY }
        }
    }
}

@Composable
private fun PrayerPointsLibrary(
    topics: List<PrayerTopic>, query: String, custom: String,
    onQuery: (String) -> Unit, onCustom: (String) -> Unit,
    onClose: () -> Unit, onSelect: (String) -> Unit, onCreate: () -> Unit,
) {
    val results = remember(topics, query) {
        if (query.isBlank()) emptyList() else topics.filter {
            it.title.contains(query, true) || it.category.contains(query, true)
        }.take(30)
    }
    Column(Modifier.fillMaxSize().background(PointBg).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Outlined.ArrowBack, "Close") }
            Text("PRAYERKEY", color = Pk.Oxblood, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.4.sp)
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text("Prayer Points", fontFamily = DisplaySerif, fontSize = 38.sp, fontWeight = FontWeight.Bold, color = PointInk)
            }
            item {
                OutlinedTextField(
                    value = query, onValueChange = onQuery, singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    placeholder = { Text("Search prayer points") },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isBlank()) {
                item { SectionLabel("Pray by situation") }
                items(featuredSituations.chunked(2)) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { label -> SituationCard(label, Modifier.weight(1f)) { onSelect(label) } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                item { SectionLabel(if (results.isEmpty()) "No matches" else "${results.size} RESULTS") }
                items(results, key = { it.slug }) { topic ->
                    Surface(
                        onClick = { onSelect(topic.title) }, shape = RoundedCornerShape(17.dp),
                        color = Color.White, border = BorderStroke(1.dp, PointLine), modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.FormatListBulleted, null, tint = Pk.Oxblood)
                            Column(Modifier.weight(1f).padding(horizontal = 13.dp)) {
                                Text(topic.title, color = PointInk, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(topic.category, color = PointMuted, fontSize = 12.sp)
                            }
                            Icon(Icons.Outlined.ChevronRight, null, tint = PointMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicPrayerField(value: String, onChange: (String) -> Unit) {
    Surface(
        Modifier.fillMaxWidth().padding(top = 18.dp), shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = .13f), border = BorderStroke(1.dp, Color.White.copy(alpha = .25f)),
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = value, onValueChange = onChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 82.dp).padding(15.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp, lineHeight = 23.sp),
            decorationBox = { inner -> Box { if (value.isBlank()) Text("Tell PrayerKey what's on your heart…", color = Color.White.copy(.64f)); inner() } },
        )
    }
}

@Composable private fun SituationCard(label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick, modifier = modifier.height(88.dp), shape = RoundedCornerShape(19.dp),
        color = Color.White, border = BorderStroke(1.dp, PointLine),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.Outlined.AutoStories, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp))
            Text(label, color = PointInk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)
        }
    }
}

@Composable
private fun PrayerPointsOverview(
    topic: PrayerTopic, selectedLength: Int, onLength: (Int) -> Unit,
    onBack: () -> Unit, onStart: () -> Unit,
) {
    val points = remember(topic) { PrayerPointEngine.sessionPointsFor(topic) }
    var showAll by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(PointBg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Spacer(Modifier.weight(1f))
        }
        LazyColumn(
            Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 22.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(topic.category, color = Pk.Oxblood, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(cleanTitle(topic.title), Modifier.padding(top = 8.dp), color = PointInk, fontFamily = DisplaySerif, fontSize = 35.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
                Text("${points.size} Prayers", Modifier.padding(top = 8.dp), color = PointMuted, fontSize = 16.sp)
            }
            item { PreparationCard("SCRIPTURE", scriptureReference(topic), topic.scripture.firstOrNull()?.second.orEmpty()) }
            item {
                SectionLabel("Choose your prayer length")
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(points.size.coerceAtMost(5) to "Short", points.size to "Complete")
                        .distinctBy { it.first }.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (count, label) ->
                                FilterChip(
                                    selected = selectedLength == count, onClick = { onLength(count) },
                                    label = { Text("$label · $count", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Pk.Oxblood, selectedLabelColor = Color.White),
                                )
                            }
                        }
                    }
                }
            }
            item {
                TextButton(onClick = { showAll = !showAll }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (showAll) "Hide prayer points" else "View all prayer points", color = Pk.Oxblood, fontWeight = FontWeight.Bold)
                }
            }
            if (showAll) items(points) { point ->
                Text("${points.indexOf(point) + 1}. $point", color = PointInk, fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp)
                HorizontalDivider(color = PointLine)
            }
        }
        Button(
            onClick = onStart, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp).height(58.dp),
        ) { Text("Start praying", fontWeight = FontWeight.Bold) }
    }
}

@Composable private fun PreparationCard(kicker: String, title: String, body: String) {
    Surface(shape = RoundedCornerShape(19.dp), color = Color.White, border = BorderStroke(1.dp, PointLine), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(17.dp)) {
            Text(kicker, color = Pk.Oxblood, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Text(title, Modifier.padding(top = 7.dp), color = PointInk, fontFamily = BookSerif, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text(body, Modifier.padding(top = 5.dp), color = PointMuted, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun PrayerPointMode(topic: PrayerTopic, requested: Int, onBack: () -> Unit, onComplete: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prayer-points-progress", 0) }
    val canonical = remember(topic) { PrayerPointEngine.sessionPointsFor(topic) }
    val points = remember(canonical, requested) { canonical.take(requested.coerceAtMost(canonical.size)) }
    val saved = remember(topic.slug) { prefs.getInt("${topic.slug}-$requested", 0).coerceIn(0, points.size) }
    var index by remember { mutableIntStateOf(saved) }
    var ending by remember { mutableIntStateOf(0) }

    fun persist(next: Int) {
        prefs.edit().putInt("${topic.slug}-$requested", next).putLong("${topic.slug}-last", System.currentTimeMillis()).apply()
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF17051F)).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.Close, "Leave prayer", tint = Color.White) }
            Spacer(Modifier.weight(1f))
            Text(cleanTitle(topic.title), color = Color.White.copy(.68f), fontSize = 12.sp, maxLines = 1)
            Spacer(Modifier.weight(1f))
        }
        if (index < points.size) {
            LinearProgressIndicator(
                progress = { (index + 1f) / points.size }, color = Pk.GoldLight, trackColor = Color.White.copy(.12f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(3.dp),
            )
            Column(Modifier.weight(1f).padding(horizontal = 28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PRAYER ${index + 1} OF ${points.size}", color = Pk.GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
                Text(points[index], Modifier.padding(top = 30.dp), color = Color.White, fontFamily = BookSerif, fontSize = 29.sp, lineHeight = 41.sp, textAlign = TextAlign.Center)
            }
            Row(Modifier.fillMaxWidth().padding(22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { if (index > 0) { index--; persist(index) } }, enabled = index > 0,
                    shape = CircleShape, border = BorderStroke(1.dp, Color.White.copy(.25f)), modifier = Modifier.weight(.45f).height(58.dp),
                ) { Text("Previous", color = Color.White) }
                Button(
                    onClick = { index++; persist(index) }, shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood), modifier = Modifier.weight(.55f).height(58.dp),
                ) { Text(if (index == points.lastIndex) "Amen" else "AMEN / NEXT", fontWeight = FontWeight.Bold) }
            }
        } else {
            val endings = listOf(
                Triple("DECLARATION", "God is with me.", "I trust His Word, receive His wisdom, and remain steadfast in prayer. Fear will not govern me, and despair will not have the final word."),
                Triple("PERSONAL PRAYER", "Father, You know my heart.", "I place every person, decision, fear, and hope connected to this matter before You. Intervene with mercy, guide me in truth, and establish Your will in my life."),
                Triple("CLOSING PRAYER", "Father, thank You for hearing me.", "Strengthen me to trust You, obey You, and walk in Your peace. Let every answer bring glory to Your name. In Jesus' name, Amen."),
            )
            val current = endings[ending.coerceIn(endings.indices)]
            Column(Modifier.weight(1f).padding(horizontal = 30.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(current.first, color = Pk.GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.7.sp)
                Text(current.second, Modifier.padding(top = 24.dp), color = Color.White, fontFamily = DisplaySerif, fontSize = 31.sp, lineHeight = 38.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Text(current.third, Modifier.padding(top = 18.dp), color = Color.White.copy(.75f), fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 28.sp, textAlign = TextAlign.Center)
            }
            Button(
                onClick = {
                    if (ending < endings.lastIndex) ending++ else {
                        prefs.edit().remove("${topic.slug}-$requested").apply(); onComplete()
                    }
                }, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood),
                modifier = Modifier.fillMaxWidth().padding(22.dp).height(58.dp),
            ) { Text(if (ending == endings.lastIndex) "Complete prayer" else "Continue", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable private fun SectionLabel(text: String) = Text(text, color = PointMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)

private fun cleanTitle(title: String): String = title
    .replace(Regex("(?i)^prayer (for|of|to|before|after|during|against|when|about|in|on|at) "), "")
    .replaceFirstChar { it.uppercase() }

private fun confessionReference(topic: PrayerTopic): String = when {
    topic.category.contains("Salvation", true) || topic.title.contains("salvation", true) -> "Romans 10:9–10"
    topic.title.contains("forgiv", true) || topic.title.contains("repent", true) -> "1 John 1:9"
    else -> "Psalm 139:23–24"
}

private fun scriptureReference(topic: PrayerTopic): String = when {
    topic.category.contains("Salvation", true) || topic.title.contains("salvation", true) -> "John 3:1–21"
    else -> topic.scripture.firstOrNull()?.first ?: "Philippians 4:6–7"
}
