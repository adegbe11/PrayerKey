package com.prayerkey.manna.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.ui.theme.*

private data class TopicGroup(val title: String, val topics: List<String>, val color: Color)

private fun words(value: String) = value.split(" · ")
private val topicalGroups = listOf(
    TopicGroup("Money & work", words("money · wealth · riches · poverty · debt · borrowing · lending · tithing · giving · offering · firstfruits · generosity · saving · investing · greed · contentment · stewardship · work · business · employees · employers · success · ambition · hard work · laziness · rest · retirement · provision · inheritance"), Color(0xFF355C4A)),
    TopicGroup("Marriage & relationships", words("marriage · husbands · wives · dating · courtship · engagement · divorce · remarriage · adultery · sex · lust · purity · singleness · finding a spouse · unequally yoked · submission · love in marriage · friendship · betrayal · gossip · toxic people · enemies · neighbours · reconciliation · community · unity · fellowship"), Color(0xFF8B4262)),
    TopicGroup("Family", words("family · children · parenting · fathers · mothers · sons · daughters · siblings · in-laws · honouring parents · discipline · prodigal children · widows · orphans · adoption · the home · generational blessing · generational curses"), Color(0xFF7A5738)),
    TopicGroup("Health & body", words("health · healing · sickness · disease · the body · food · eating · fasting · exercise · beauty · appearance · aging · sleep · rest · medicine · mental health · addiction"), Color(0xFF39746B)),
    TopicGroup("Suffering & trouble", words("suffering · trials · pain · loss · grief · death · dying · injustice · persecution · oppression · poverty · hunger · waiting · endurance · perseverance · deliverance · restoration · comfort · new beginnings"), Color(0xFF4D5777)),
    TopicGroup("Faith & doctrine", words("faith · grace · mercy · salvation · being born again · baptism · communion · the Holy Spirit · spiritual gifts · fruit of the Spirit · tongues · holiness · sanctification · righteousness · works · the law · predestination · eternal security · backsliding · the Trinity · the blood of Jesus · the name of Jesus · the cross · resurrection"), Color(0xFF5C02D8)),
    TopicGroup("Prayer & practice", words("prayer · fasting · worship · praise · thanksgiving · meditation · Bible reading · memorisation · obedience · discipleship · evangelism · missions · serving · giving · vows · anointing"), Color(0xFF74436E)),
    TopicGroup("Spiritual warfare", words("spiritual warfare · the devil · demons · witchcraft · curses · covenants · soul ties · deliverance · evil spirits · dreams · nightmares · night attacks · principalities · the armour of God · binding and loosing · angels · temptation · protection · covering"), Color(0xFF273552)),
    TopicGroup("Character", words("love · kindness · humility · pride · patience · self-control · honesty · integrity · wisdom · discernment · discipline · gentleness · generosity · compassion · forgiveness · mercy · purity · repentance · truth · courage · faithfulness · gossip · slander · lying · anger · greed · laziness · jealousy · envy"), Color(0xFF9A593C)),
    TopicGroup("Church & leadership", words("the church · going to church · pastors · elders · deacons · leadership · authority · submission · false prophets · false teachers · prophecy · unity · division · church discipline · giving to the church"), Color(0xFF53607D)),
    TopicGroup("Nation & society", words("government · leaders · politics · voting · authority · taxes · war · peace · violence · justice · the poor · strangers · immigrants · refugees · slavery · racism · the persecuted church · the nations"), Color(0xFF385F78)),
    TopicGroup("Creation & the world", words("creation · the environment · animals · nature · the earth · seasons · time · work of God's hands"), Color(0xFF54714A)),
    TopicGroup("Life stages & seasons", words("birth · childhood · youth · graduation · new job · marriage · pregnancy · parenting · midlife · aging · retirement · death · new year · new month · birthdays · anniversaries · harvest"), Color(0xFF9A6B39)),
    TopicGroup("God", words("God's love · God's promises · God's timing · God's plan · God's will · God's protection · God's provision · God's presence · God's faithfulness · God's grace · God's mercy · God's power · God's goodness · God's holiness · God's justice · the fear of the Lord · hearing God · trusting God · knowing God"), Color(0xFF5C02D8)),
    TopicGroup("Identity & purpose", words("identity · purpose · calling · destiny · gifts · talents · direction · decisions · God's will for my life · change · moving · starting over"), Color(0xFF6A477D)),
    TopicGroup("Everyday", words("morning · evening · night · sleep · travel · safety · study · exams · decisions · conversations · food · money management · time"), Color(0xFF346F75)),
    TopicGroup("Heavy moods", words("anxious · afraid · worried · stressed · overwhelmed · depressed · sad · grieving · lonely · abandoned · rejected · angry · bitter · jealous · ashamed · guilty · regretful · doubting · confused · discouraged · burnt out · exhausted · restless · hopeless · numb · empty · broken · lost · stuck · tempted · weak · unworthy · misunderstood · betrayed · homesick"), Color(0xFF3D385E)),
    TopicGroup("Light moods", words("hopeful · peaceful · calm · joyful · happy · grateful · thankful · content · confident · courageous · strong · steady · loved · secure · forgiven · free · restored · expectant · at rest · assured · trusting · encouraged"), Color(0xFF4C7A67)),
    TopicGroup("Waiting", words("waiting · impatient · uncertain · in transition · praying and nothing is happening · about to give up · starting again"), Color(0xFF8A6844)),
)

@Composable
internal fun TopicalBibleScreen(onClose: () -> Unit) {
    var group by remember { mutableStateOf<TopicGroup?>(null) }
    var topic by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    val back = { when { topic != null -> topic = null; group != null -> group = null; else -> onClose() } }
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = back) { Icon(if (group == null && topic == null) Icons.Outlined.Close else Icons.Outlined.ArrowBack, "Back") }
                Column(Modifier.weight(1f)) {
                    Text(topic ?: group?.title ?: "Topical Bible Verses", fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = if (topic == null) 25.sp else 23.sp, color = Pk.Charcoal)
                    Text(when { topic != null -> "Scripture for this subject"; group != null -> "Choose a topic"; else -> "Verses by subject and mood" }, fontSize = 11.sp, color = Pk.Muted)
                }
            }
            when {
                topic != null -> TopicVerses(topic!!)
                group != null -> TopicPicker(group!!, query, { query = it }) { topic = it }
                else -> GroupPicker(query, { query = it }) { group = it; query = "" }
            }
        }
    }
}

@Composable private fun SearchBox(query: String, change: (String) -> Unit, hint: String) {
    OutlinedTextField(query, change, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 9.dp), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text(hint) }, shape = RoundedCornerShape(17.dp))
}

@Composable private fun GroupPicker(query: String, change: (String) -> Unit, open: (TopicGroup) -> Unit) {
    SearchBox(query, change, "Search any subject or feeling")
    val matches = topicalGroups.filter { query.isBlank() || it.title.contains(query, true) || it.topics.any { t -> t.contains(query, true) } }
    LazyVerticalGrid(GridCells.Fixed(2), contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 100.dp), horizontalArrangement = Arrangement.spacedBy(11.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        items(matches, key = { it.title }) { item ->
            Column(Modifier.fillMaxWidth().height(125.dp).clip(RoundedCornerShape(20.dp)).background(item.color).clickable { open(item) }.padding(15.dp), verticalArrangement = Arrangement.Bottom) {
                Icon(groupIcon(item.title), null, tint = Color.White.copy(.86f), modifier = Modifier.size(22.dp))
                Text(item.title, Modifier.padding(top = 11.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp, maxLines = 2)
                Text("${item.topics.size} topics", color = Color.White.copy(.68f), fontSize = 10.sp)
            }
        }
    }
}

@Composable private fun TopicPicker(group: TopicGroup, query: String, change: (String) -> Unit, open: (String) -> Unit) {
    SearchBox(query, change, "Search ${group.title.lowercase()}")
    val matches = group.topics.filter { query.isBlank() || it.contains(query, true) }
    LazyVerticalGrid(GridCells.Fixed(2), contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(matches) { item ->
            Surface(onClick = { open(item) }, shape = RoundedCornerShape(17.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).background(group.color, CircleShape)); Text(item.replaceFirstChar(Char::uppercase), Modifier.weight(1f).padding(start = 10.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 2); Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted, modifier = Modifier.size(17.dp)) }
            }
        }
    }
}

@Composable private fun TopicVerses(topic: String) {
    val context = LocalContext.current
    var verses by remember(topic) { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var loading by remember(topic) { mutableStateOf(true) }
    LaunchedEffect(topic) {
        val bible = OfflineBible(context)
        verses = bible.search(searchTerm(topic), 22)
        if (verses.isEmpty()) verses = bible.search("God", 12)
        loading = false
    }
    if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pk.Oxblood) }
    else LazyColumn(contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { Text("${verses.size} VERSES ABOUT ${topic.uppercase()}", fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold, color = Pk.GoldDeep, modifier = Modifier.padding(bottom = 5.dp)) }
        items(verses, key = { it.reference }) { verse ->
            Surface(shape = RoundedCornerShape(22.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair)) {
                Column(Modifier.padding(20.dp)) { Text(verse.reference.uppercase(), color = Pk.Oxblood, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.2.sp); Text("“${verse.text}”", Modifier.padding(top = 11.dp), fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 27.sp, color = Pk.Charcoal) }
            }
        }
    }
}

private fun searchTerm(topic: String): String = when (topic.lowercase()) {
    "anxious", "worried", "stressed", "overwhelmed" -> "fear"
    "depressed", "sad", "grieving", "hopeless" -> "comfort"
    "money management", "wealth", "riches", "provision" -> "riches"
    "marriage", "husbands", "wives", "love in marriage" -> "wife"
    "mental health", "calm", "peaceful" -> "peace"
    "god's promises" -> "promise"
    "god's plan", "god's will for my life", "purpose", "direction" -> "way"
    "spiritual warfare", "the armour of god" -> "armour"
    "burnt out", "exhausted", "at rest" -> "rest"
    "starting over", "new beginnings", "starting again" -> "new"
    else -> topic.substringBefore(" and ").removePrefix("God's ")
}

private fun groupIcon(title: String) = when {
    title.contains("Money") -> Icons.Outlined.WorkOutline
    title.contains("Marriage") -> Icons.Outlined.FavoriteBorder
    title == "Family" -> Icons.Outlined.FamilyRestroom
    title.contains("Health") -> Icons.Outlined.HealthAndSafety
    title.contains("warfare", true) -> Icons.Outlined.Shield
    title.contains("Church") -> Icons.Outlined.Church
    title.contains("Nation") -> Icons.Outlined.Public
    title.contains("moods", true) -> Icons.Outlined.Psychology
    title == "God" -> Icons.Outlined.Flare
    else -> Icons.Outlined.AutoStories
}
