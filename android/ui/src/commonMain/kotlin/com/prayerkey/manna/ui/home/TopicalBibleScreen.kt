package com.prayerkey.manna.ui.home

import androidx.activity.compose.BackHandler
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class TopicGroup(val title: String, val topics: List<String>, val color: Color)

private fun words(value: String) = value.split(" · ")
/**
 * The subjects, ordered by colour rather than by nothing.
 *
 * These were ten unrelated colours - dark green, maroon, brown, teal, slate,
 * navy, rust - with no relationship to each other or to what they labelled, and
 * one of them, Faith & doctrine, was full brand violet among nine muted tones,
 * so it read as a mistake rather than as emphasis.
 *
 * Now it is a single ramp, and the ramp carries meaning: amber where life is
 * daily and tangible, through rose where it is about people, into violet for
 * God and faith, indigo for the heavy things, and out to teal and green for
 * creation and the world. Scrolling the page moves through it in order, which
 * is what makes a grid of twenty tiles feel composed instead of scattered.
 */
private val topicalGroups = listOf(
    TopicGroup("Everyday", words("morning · evening · night · sleep · travel · safety · study · exams · decisions · conversations · food · money management · time"), Color(0xFFB4712A)),
    TopicGroup("Life stages & seasons", words("birth · childhood · youth · graduation · new job · marriage · pregnancy · parenting · midlife · aging · retirement · death · new year · new month · birthdays · anniversaries · harvest"), Color(0xFFA9612E)),
    TopicGroup("Money & work", words("money · wealth · riches · poverty · debt · borrowing · lending · tithing · giving · offering · firstfruits · generosity · saving · investing · greed · contentment · stewardship · work · business · employees · employers · success · ambition · hard work · laziness · rest · retirement · provision · inheritance"), Color(0xFF9E5233)),
    TopicGroup("Family", words("family · children · parenting · fathers · mothers · sons · daughters · siblings · in-laws · honouring parents · discipline · prodigal children · widows · orphans · adoption · the home · generational blessing · generational curses"), Color(0xFF93443B)),
    TopicGroup("Health & body", words("health · healing · sickness · disease · the body · food · eating · fasting · exercise · beauty · appearance · aging · sleep · rest · medicine · mental health · addiction"), Color(0xFF8A3A47)),
    TopicGroup("Marriage & relationships", words("marriage · husbands · wives · dating · courtship · engagement · divorce · remarriage · adultery · sex · lust · purity · singleness · finding a spouse · unequally yoked · submission · love in marriage · friendship · betrayal · gossip · toxic people · enemies · neighbours · reconciliation · community · unity · fellowship"), Color(0xFF7E3455)),
    TopicGroup("Character", words("love · kindness · humility · pride · patience · self-control · honesty · integrity · wisdom · discernment · discipline · gentleness · generosity · compassion · forgiveness · mercy · purity · repentance · truth · courage · faithfulness · gossip · slander · lying · anger · greed · laziness · jealousy · envy"), Color(0xFF6F3163)),
    TopicGroup("Light moods", words("hopeful · peaceful · calm · joyful · happy · grateful · thankful · content · confident · courageous · strong · steady · loved · secure · forgiven · free · restored · expectant · at rest · assured · trusting · encouraged"), Color(0xFF5F3070)),
    TopicGroup("Identity & purpose", words("identity · purpose · calling · destiny · gifts · talents · direction · decisions · God's will for my life · change · moving · starting over"), Color(0xFF533075)),
    TopicGroup("Faith & doctrine", words("faith · grace · mercy · salvation · being born again · baptism · communion · the Holy Spirit · spiritual gifts · fruit of the Spirit · tongues · holiness · sanctification · righteousness · works · the law · predestination · eternal security · backsliding · the Trinity · the blood of Jesus · the name of Jesus · the cross · resurrection"), Color(0xFF46307E)),
    TopicGroup("God", words("God's love · God's promises · God's timing · God's plan · God's will · God's protection · God's provision · God's presence · God's faithfulness · God's grace · God's mercy · God's power · God's goodness · God's holiness · God's justice · the fear of the Lord · hearing God · trusting God · knowing God"), Color(0xFF3A3389)),
    TopicGroup("Prayer & practice", words("prayer · fasting · worship · praise · thanksgiving · meditation · Bible reading · memorisation · obedience · discipleship · evangelism · missions · serving · giving · vows · anointing"), Color(0xFF33377F)),
    TopicGroup("Church & leadership", words("the church · going to church · pastors · elders · deacons · leadership · authority · submission · false prophets · false teachers · prophecy · unity · division · church discipline · giving to the church"), Color(0xFF2F3C78)),
    TopicGroup("Waiting", words("waiting · impatient · uncertain · in transition · praying and nothing is happening · about to give up · starting again"), Color(0xFF2C4271)),
    TopicGroup("Heavy moods", words("anxious · afraid · worried · stressed · overwhelmed · depressed · sad · grieving · lonely · abandoned · rejected · angry · bitter · jealous · ashamed · guilty · regretful · doubting · confused · discouraged · burnt out · exhausted · restless · hopeless · numb · empty · broken · lost · stuck · tempted · weak · unworthy · misunderstood · betrayed · homesick"), Color(0xFF2A4A6B)),
    TopicGroup("Suffering & trouble", words("suffering · trials · pain · loss · grief · death · dying · injustice · persecution · oppression · poverty · hunger · waiting · endurance · perseverance · deliverance · restoration · comfort · new beginnings"), Color(0xFF275165)),
    TopicGroup("Spiritual warfare", words("spiritual warfare · the devil · demons · witchcraft · curses · covenants · soul ties · deliverance · evil spirits · dreams · nightmares · night attacks · principalities · the armour of God · binding and loosing · angels · temptation · protection · covering"), Color(0xFF245A5F)),
    TopicGroup("Nation & society", words("government · leaders · politics · voting · authority · taxes · war · peace · violence · justice · the poor · strangers · immigrants · refugees · slavery · racism · the persecuted church · the nations"), Color(0xFF216459)),
    TopicGroup("Creation & the world", words("creation · the environment · animals · nature · the earth · seasons · time · work of God's hands"), Color(0xFF2A6E4E)),
)

@Composable
internal fun TopicalBibleScreen(onClose: () -> Unit) {
    var group by remember { mutableStateOf<TopicGroup?>(null) }
    var topic by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    val back = { when { topic != null -> topic = null; group != null -> group = null; else -> onClose() } }
    BackHandler { back() }
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = back) { Icon(if (group == null && topic == null) Icons.Outlined.Close else Icons.Outlined.ArrowBack, "Back") }
                Column(Modifier.weight(1f)) {
                    Text(topic ?: group?.title ?: "Topical Bible Verses", fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = if (topic == null) 25.sp else 23.sp, color = Pk.Charcoal)
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
    LazyVerticalGrid(GridCells.Fixed(2), contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 100.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(matches, key = { it.title }) { item ->
            /* One word on a colour, and nothing else.
               The icon was a book, a heart or a shield repeated across twenty
               tiles beside a label that already said Family, or Money & work -
               decoration competing with the only thing worth reading. The count
               went with it: "29 topics" does not help anyone choose between
               money and marriage when they are looking for their own life.
               Wide and short, so twenty of them make a rhythm down the page
               rather than twenty blocks. */
            Column(
                Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(22.dp))
                    .background(item.color).clickable { open(item) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    item.title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    maxLines = 2,
                )
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
        verses = withContext(Dispatchers.IO) { bible.search(searchTerm(topic), 22) }
        if (verses.isEmpty()) verses = bible.search("God", 12)
        loading = false
    }
    if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Pk.Oxblood) }
    else LazyColumn(contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { Text("${verses.size} VERSES ABOUT ${topic}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Pk.GoldDeep, modifier = Modifier.padding(bottom = 5.dp)) }
        items(verses, key = { it.reference }) { verse ->
            Surface(shape = RoundedCornerShape(22.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair)) {
                Column(Modifier.padding(20.dp)) { Text(verse.reference, color = Pk.Oxblood, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(verse.text, Modifier.padding(top = 11.dp), fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 27.sp, color = Pk.Charcoal) }
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

