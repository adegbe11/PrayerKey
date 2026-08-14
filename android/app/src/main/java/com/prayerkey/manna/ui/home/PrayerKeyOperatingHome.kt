package com.prayerkey.manna.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.Challenge
import com.prayerkey.manna.data.Devotion
import com.prayerkey.manna.data.devotionalFor
import com.prayerkey.manna.data.passageFor
import com.prayerkey.manna.R
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.share.CardShareRenderer
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private enum class Hub { BIBLE, PRAY, CHURCH, JOURNAL }
private data class Feature(val title: String, val detail: String, val icon: ImageVector, val hub: Hub)

@Composable
fun PrayerKeyOperatingHome(
    streak: Int,
    savedCount: Int,
    journalCount: Int,
    sermonCount: Int,
    devotion: Devotion?,
    bible: Challenge?,
    prayer: Challenge?,
    onOpenWord: () -> Unit,
    onWriteDevotion: () -> Unit,
    onOpenBible: () -> Unit,
    onOpenPrayer: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenChurch: () -> Unit,
    onSettings: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    val dailyDevotional = remember(today) { devotionalFor(today) }
    val dailyPassage = remember(today) { passageFor(today) }
    var devotionalOpen by rememberSaveable { mutableStateOf(false) }
    var topicalBibleOpen by rememberSaveable { mutableStateOf(false) }
    if (devotionalOpen) {
        Dialog(
            onDismissRequest = { devotionalOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            DailyDevotionalReader(dailyDevotional, dailyPassage) { devotionalOpen = false }
        }
    }
    if (topicalBibleOpen) {
        Dialog(
            onDismissRequest = { topicalBibleOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) { TopicalBibleScreen { topicalBibleOpen = false } }
    }
    var selectedFeature by remember { mutableStateOf<Feature?>(null) }
    val go: (Feature) -> Unit = { selectedFeature = it }
    selectedFeature?.let { feature ->
        Dialog(onDismissRequest = { selectedFeature = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            FeatureDestination(feature, onClose = { selectedFeature = null }) { }
        }
    }
    val fixed = remember { fixedFeatures() }
    val discovery = remember(fixed) {
        val start = fixed.indexOfFirst { it.title == "My prayer list" }
        val end = fixed.indexOfLast { it.title == "Prayer partner" }
        if (start >= 0 && end >= start) fixed.subList(start, end + 1) else fixed
    }
    val conditional = remember(today) { conditionalFeatures(today) }

    Column(
        Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding()
            .verticalScroll(rememberScrollState()).padding(bottom = Pk.NavClearance)
    ) {
        Header(today, streak, onSettings)
        HeroRail(dailyDevotional.title) { devotionalOpen = true }
        QuickActions(onOpenPrayer, onOpenBible, onOpenJournal, onOpenChurch)

        Section("TODAY") {
            VerseCard(onOpenWord)
            Spacer(Modifier.height(12.dp))
            PrayerNowCard(onOpenPrayer)
        }

        EmergencyCard(onOpenPrayer)
        TopicalBibleEntry { topicalBibleOpen = true }

        Section("YOUR PRAYER LIFE") {
            PrayerLifePanel(savedCount, journalCount, streak, onOpenPrayer, onOpenJournal)
        }

        conditional.takeIf { it.isNotEmpty() }?.let { items ->
            Section("FOR THIS MOMENT") { MomentCards(items, go) }
        }

        Section("PRAYERKEY PROGRAMMES") { DiscoveryGrid(discovery, go) }

        if (false) Section("CLOSING VERSE") {
            Surface(shape = RoundedCornerShape(24.dp), color = Pk.DeepDark) {
                Column(Modifier.padding(24.dp)) {
                    Text("“The Lord will watch over your coming and going both now and forevermore.”", fontFamily = DisplaySerif, fontSize = 20.sp, lineHeight = 29.sp, color = Color.White)
                    Text("PSALM 121:8", Modifier.padding(top = 14.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.4.sp, color = Pk.GoldLight)
                }
            }
        }
        Section("CLOSING VERSE") { ClosingVerseCard() }
    }
}

@Composable private fun ClosingVerseCard() {
    val context = LocalContext.current
    val verse = "The Lord will watch over your coming and going both now and forevermore."
    val reference = "Psalm 121:8"
    Surface(shape = RoundedCornerShape(26.dp), color = Pk.DeepDark, shadowElevation = 3.dp) {
        Box(Modifier.fillMaxWidth().height(390.dp)) {
            Image(painterResource(R.drawable.closing_verse_violet), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Pk.DeepDark.copy(alpha = .94f)))))
            Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Bottom) {
                Text("A WORD TO CARRY", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.6.sp, color = Pk.GoldLight)
                Text("“$verse”", Modifier.padding(top = 12.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 30.sp, color = Color.White)
                Text(reference.uppercase(), Modifier.padding(top = 12.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.3.sp, color = Pk.GoldLight)
                Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrayerImageAction(Icons.Outlined.Download, "SAVE") { CardShareRenderer.saveClosingVerse(context, verse, reference) }
                    PrayerImageAction(Icons.Outlined.Share, "SHARE") { CardShareRenderer.shareClosingVerse(context, verse, reference) }
                }
            }
        }
    }
}

@Composable private fun Header(date: LocalDate, streak: Int, settings: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("PRAYERKEY", fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Pk.Charcoal)
            Text(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.Muted)
        }
        Surface(shape = RoundedCornerShape(50), color = Pk.Oxblood.copy(alpha = .08f)) {
            Row(Modifier.padding(horizontal = 13.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocalFireDepartment, null, tint = Pk.Oxblood, modifier = Modifier.size(17.dp))
                val days = streak.coerceAtLeast(1)
                Text(" $days ${if (days == 1) "DAY" else "DAYS"}", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Pk.Oxblood)
            }
        }
        IconButton(settings) { Icon(Icons.Outlined.PersonOutline, "Profile", tint = Pk.Charcoal) }
    }
}

@Composable private fun HeroRail(title: String, open: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(430.dp).clickable(onClick = open)
    ) {
        Image(
            painterResource(R.drawable.prayerkey_hero_dawn), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Pk.DeepDark.copy(alpha = .18f), Pk.DeepDark.copy(alpha = .94f)),
                    startY = 90f,
                )
            )
        )
        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = CircleShape, color = Color.White.copy(alpha = .94f), shadowElevation = 10.dp,
        ) {
                Icon(Icons.Outlined.PlayArrow, "Open today's devotional", Modifier.padding(19.dp).size(30.dp), tint = Pk.Oxblood)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 22.dp, vertical = 24.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = .16f)) {
                Text("TODAY'S DEVOTIONAL", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp, color = Color.White)
            }
            Text(title, Modifier.padding(top = 12.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 38.sp, color = Color.White)
            Text("Scripture • reflection • prayer", Modifier.padding(top = 7.dp), fontFamily = UtilitySans, fontSize = 13.sp, color = Color.White.copy(alpha = .78f))
            Row(Modifier.fillMaxWidth().padding(top = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(4) { Box(Modifier.padding(end = 6.dp).size(if (it == 0) 20.dp else 6.dp, 6.dp).background(if (it == 0) Pk.GoldLight else Color.White.copy(alpha = .38f), CircleShape)) }
                Spacer(Modifier.weight(1f))
                Text("8 MIN", fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Pk.GoldLight)
            }
        }
    }
}

@Composable private fun QuickActions(pray: () -> Unit, bible: () -> Unit, journal: () -> Unit, church: () -> Unit) {
    val actions = listOf(
        Triple("PRAY NOW", Icons.Outlined.Bolt, pray),
        Triple("BIBLE", Icons.Outlined.MenuBook, bible),
        Triple("JOURNAL", Icons.Outlined.EditNote, journal),
        Triple("LIVE", Icons.Outlined.GraphicEq, church),
    )
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.forEach { action ->
            Surface(
                Modifier.weight(1f).height(84.dp).clickable(onClick = action.third),
                shape = RoundedCornerShape(20.dp), color = Color.White,
            ) {
                Column(Modifier.padding(vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(action.second, null, tint = Pk.Oxblood, modifier = Modifier.size(24.dp))
                    Text(action.first, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp, color = Pk.Charcoal, maxLines = 1)
                }
            }
        }
    }
}

@Composable private fun VerseCard(open: () -> Unit) {
    val context = LocalContext.current
    val card = remember { VerseCard("Matthew 7:7", "KJV", "Ask, and it will be given to you; seek, and you will find; knock, and the door will be opened to you.", "") }
    Surface(
        Modifier.fillMaxWidth().clickable(onClick = open),
        shape = RoundedCornerShape(30.dp), color = Pk.DeepDark,
        border = BorderStroke(1.dp, Pk.Oxblood.copy(alpha = .14f)), shadowElevation = 4.dp,
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(.56f)) {
            Image(
                painterResource(R.drawable.verse_card_oxblood), null,
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 46.dp).padding(top = 120.dp, bottom = 105.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Outlined.MenuBook, null, tint = Pk.Oxblood, modifier = Modifier.size(25.dp))
                Text("VERSE OF THE DAY", Modifier.padding(top = 10.dp), style = labelStyle(), color = Pk.GoldDeep)
                Text("MATTHEW 7:7", Modifier.padding(top = 17.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 27.sp, letterSpacing = 1.1.sp, color = Pk.Oxblood)
                Row(Modifier.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.width(42.dp), color = Pk.Gold.copy(alpha = .72f))
                    Box(Modifier.padding(horizontal = 8.dp).size(5.dp).background(Pk.Oxblood, CircleShape))
                    HorizontalDivider(Modifier.width(42.dp), color = Pk.Gold.copy(alpha = .72f))
                }
                Text(
                    "“Ask, and it will be given to you; seek, and you will find; knock, and the door will be opened to you.”",
                    fontFamily = DisplaySerif, fontSize = 19.sp, lineHeight = 27.sp, color = Pk.Charcoal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusAction(Icons.Outlined.Download, "SAVE") { CardShareRenderer.saveStatus(context, card) }
                    StatusAction(Icons.Outlined.Share, "SHARE") { CardShareRenderer.shareStatus(context, card) }
                }
            }
        }
    }
}

@Composable private fun StatusAction(icon: ImageVector, label: String, action: () -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = Pk.Oxblood.copy(alpha = .09f), modifier = Modifier.clickable(onClick = action)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(16.dp))
            Text(label, Modifier.padding(start = 7.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = .8.sp, color = Pk.Oxblood)
        }
    }
}

@Composable private fun PrayerNowCard(open: () -> Unit) {
    val context = LocalContext.current
    val words = "Lord, according to Your Word, order my steps today. Let this day be a blessing to me. Give me wisdom for every decision, peace in every pressure, favour in the work of my hands, and protection wherever I go. Keep my heart close to You and let my life bring hope to someone today. In Jesus’ name, Amen."
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), color = Pk.DeepDark, shadowElevation = 5.dp) {
        Box(Modifier.fillMaxWidth().height(520.dp)) {
            Image(painterResource(R.drawable.today_prayer_violet), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Pk.DeepDark.copy(alpha = .08f), Pk.DeepDark.copy(alpha = .42f), Pk.DeepDark.copy(alpha = .88f)))))
            Column(Modifier.fillMaxSize().padding(horizontal = 23.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = .13f)) {
                    Icon(Icons.Outlined.SelfImprovement, null, Modifier.padding(11.dp).size(25.dp), tint = Pk.GoldLight)
                }
                Text("TODAY'S PRAYER", Modifier.padding(top = 13.dp), style = labelStyle(), color = Pk.GoldLight)
                Text("Order My Steps", Modifier.padding(top = 8.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 27.sp, color = Color.White)
                Text(words, Modifier.weight(1f).padding(top = 18.dp), fontFamily = DisplaySerif, fontSize = 17.sp, lineHeight = 25.sp, color = Color.White.copy(alpha = .94f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                    PrayerImageAction(Icons.Outlined.Download, "SAVE") { CardShareRenderer.savePrayerStatus(context, words) }
                    PrayerImageAction(Icons.Outlined.Share, "SHARE") { CardShareRenderer.sharePrayerStatus(context, words) }
                    PrayerImageAction(Icons.Outlined.SelfImprovement, "PRAY", open)
                }
            }
        }
    }
}

@Composable private fun PrayerImageAction(icon: ImageVector, label: String, action: () -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .14f), modifier = Modifier.clickable(onClick = action)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Pk.GoldLight, modifier = Modifier.size(15.dp))
            Text(label, Modifier.padding(start = 6.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 8.sp, letterSpacing = .7.sp, color = Color.White)
        }
    }
}

@Composable private fun EmergencyCard(open: () -> Unit) {
    Surface(Modifier.padding(horizontal = 22.dp, vertical = 8.dp).fillMaxWidth().clickable(onClick = open), shape = RoundedCornerShape(22.dp), color = Pk.Oxblood) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Bolt, null, tint = Pk.GoldLight, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text("I NEED PRAYER NOW", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text("Start a private guided prayer", fontFamily = UtilitySans, fontSize = 12.sp, color = Color.White.copy(alpha = .76f))
            }
            Icon(Icons.Outlined.ArrowForward, null, tint = Color.White)
        }
    }
}

@Composable private fun TopicalBibleEntry(open: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 13.dp)) {
        Text("FIND IT IN THE WORD", style = labelStyle(), color = Pk.Charcoal, modifier = Modifier.padding(bottom = 11.dp))
        Box(
            Modifier.fillMaxWidth().height(236.dp)
                .shadow(16.dp, RoundedCornerShape(27.dp), spotColor = Pk.Oxblood.copy(alpha = .24f))
                .clip(RoundedCornerShape(27.dp)).clickable(onClick = open),
        ) {
            Image(
                painterResource(R.drawable.closing_verse_violet), null,
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        listOf(Pk.DeepDark.copy(alpha = .98f), Pk.Oxblood.copy(alpha = .88f), Pk.DeepDark.copy(alpha = .90f)),
                    ),
                ),
            )

            /* The Bible is a physical object here, not an icon. Its edge,
               leather and gold type make the feature feel worth opening. */
            Image(
                painterResource(R.drawable.bible_cover_red), "Holy Bible",
                Modifier.align(Alignment.CenterStart).padding(start = 18.dp)
                    .width(118.dp).height(196.dp)
                    .shadow(14.dp, RoundedCornerShape(9.dp), spotColor = Color.Black.copy(alpha = .7f))
                    .clip(RoundedCornerShape(9.dp)),
                contentScale = ContentScale.Crop,
            )

            Column(
                Modifier.fillMaxHeight().padding(start = 157.dp, end = 18.dp, top = 27.dp, bottom = 22.dp),
            ) {
                Text("TOPICAL BIBLE VERSES", color = Pk.GoldLight, fontFamily = UtilitySans,
                    fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                Text("Find the Word\nfor this moment", Modifier.padding(top = 8.dp),
                    color = Color.White, fontFamily = DisplaySerif, fontSize = 25.sp,
                    lineHeight = 29.sp, fontWeight = FontWeight.Bold)
                Text("Subjects · moods · everyday life", Modifier.padding(top = 7.dp),
                    color = Color.White.copy(alpha = .68f), fontSize = 10.5.sp, maxLines = 1)
                Spacer(Modifier.weight(1f))
                Surface(onClick = open, shape = RoundedCornerShape(14.dp), color = Color.White) {
                    Row(Modifier.padding(horizontal = 15.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("EXPLORE VERSES", color = Pk.Oxblood, fontSize = 10.sp,
                            letterSpacing = .7.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Outlined.ArrowForward, null, tint = Pk.Oxblood,
                            modifier = Modifier.padding(start = 6.dp).size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable private fun MetricRail(saved: Int, journal: Int, sermons: Int, pray: () -> Unit, write: () -> Unit, church: () -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Metric("OPEN PRAYERS", saved, Icons.Outlined.FavoriteBorder, pray)
        Metric("JOURNAL", journal, Icons.Outlined.EditNote, write)
        Metric("SERMONS", sermons, Icons.Outlined.GraphicEq, church)
        Metric("PRAYING NOW", 247, Icons.Outlined.Groups, pray)
    }
}

@Composable private fun PrayerLifePanel(saved: Int, journal: Int, streak: Int, pray: () -> Unit, write: () -> Unit) {
    Surface(shape = RoundedCornerShape(28.dp), color = Pk.DeepDark) {
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Your quiet history with God", fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Color.White)
                    Text("Every prayer matters—even while you wait.", Modifier.padding(top = 5.dp), fontFamily = UtilitySans, fontSize = 12.sp, color = Color.White.copy(alpha = .66f))
                }
                Surface(shape = CircleShape, color = Pk.GoldLight.copy(alpha = .12f)) {
                    Icon(Icons.Outlined.FavoriteBorder, null, Modifier.padding(12.dp), tint = Pk.GoldLight)
                }
            }
            Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LifeStat("OPEN", saved, Modifier.weight(1f))
                LifeStat("ENTRIES", journal, Modifier.weight(1f))
                LifeStat("DAY STREAK", streak.coerceAtLeast(1), Modifier.weight(1f))
            }
            Row(Modifier.padding(top = 15.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(pray, Modifier.weight(1f), colors = ButtonDefaults.filledTonalButtonColors(containerColor = Pk.Oxblood, contentColor = Color.White)) { Text("MY PRAYERS", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                OutlinedButton(write, Modifier.weight(1f), border = BorderStroke(1.dp, Color.White.copy(alpha = .22f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("JOURNAL", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable private fun LifeStat(label: String, value: Int, modifier: Modifier) {
    Column(modifier.background(Color.White.copy(alpha = .08f), RoundedCornerShape(17.dp)).padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 25.sp, color = Pk.GoldLight)
        Text(label, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 8.sp, letterSpacing = .8.sp, color = Color.White.copy(alpha = .58f))
    }
}

@Composable private fun MomentCards(items: List<Feature>, go: (Feature) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEachIndexed { index, item ->
            val violet = index % 2 == 0
            Surface(Modifier.fillMaxWidth().height(104.dp).clickable { go(item) }, shape = RoundedCornerShape(22.dp), color = if (violet) Pk.Oxblood else Color.White, border = BorderStroke(1.dp, if (violet) Color.Transparent else Pk.Hair)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = if (violet) Color.White.copy(alpha = .14f) else Pk.Oxblood.copy(alpha = .09f)) { Icon(item.icon, null, Modifier.padding(11.dp), tint = if (violet) Pk.GoldLight else Pk.Oxblood) }
                    Column(Modifier.weight(1f).padding(horizontal = 15.dp)) {
                        Text(item.title, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, color = if (violet) Color.White else Pk.Charcoal)
                        Text(item.detail, fontFamily = UtilitySans, fontSize = 11.sp, color = if (violet) Color.White.copy(alpha = .68f) else Pk.Muted)
                    }
                    Icon(Icons.Outlined.ArrowForward, null, tint = if (violet) Pk.GoldLight else Pk.Oxblood)
                }
            }
        }
    }
}

@Composable private fun PrayerPracticeShelf(items: List<Feature>, go: (Feature) -> Unit) {
    Column {
        Surface(Modifier.fillMaxWidth().height(208.dp).clickable { items.firstOrNull()?.let(go) }, shape = RoundedCornerShape(27.dp), color = Pk.Oxblood) {
            Box {
                Column(Modifier.padding(22.dp).align(Alignment.BottomStart)) {
                    Text("BEGIN HERE", style = labelStyle(), color = Pk.GoldLight)
                    Text("Prayer points for\nwhat you face today", Modifier.padding(top = 8.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 25.sp, lineHeight = 29.sp, color = Color.White)
                    Text("17 life situations  →", Modifier.padding(top = 12.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White.copy(alpha = .75f))
                }
                Icon(Icons.Outlined.AutoAwesome, null, tint = Pk.GoldLight.copy(alpha = .65f), modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).size(54.dp))
            }
        }
        CompactFeatureRail(items.drop(1), go)
    }
}

@Composable private fun GrowthShelf(items: List<Feature>, go: (Feature) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEachIndexed { i, item ->
            Surface(Modifier.width(238.dp).height(218.dp).clickable { go(item) }, shape = RoundedCornerShape(26.dp), color = if (i % 2 == 0) Pk.Oxblood.copy(alpha = .08f) else Color.White, border = BorderStroke(1.dp, Pk.Hair)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(item.icon, null, tint = Pk.Oxblood, modifier = Modifier.size(34.dp))
                    Column {
                        Text(item.title, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 23.sp, color = Pk.Charcoal)
                        Text(item.detail, Modifier.padding(top = 5.dp), fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.Muted)
                        Text(if (i == 0) "DAY 1 OF 365" else "OPEN", Modifier.padding(top = 15.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp, color = Pk.Oxblood)
                    }
                }
            }
        }
    }
}

@Composable private fun RemembrancePanel(items: List<Feature>, go: (Feature) -> Unit) {
    Surface(shape = RoundedCornerShape(28.dp), color = Color.White, border = BorderStroke(1.dp, Pk.Hair)) {
        Column {
            Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Your remembrance book", fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Pk.Charcoal)
                    Text("Keep what God is doing", fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.Muted)
                }
                Icon(Icons.Outlined.AutoStories, null, tint = Pk.Oxblood)
            }
            items.take(5).forEachIndexed { i, item ->
                if (i > 0) HorizontalDivider(color = Pk.Hair)
                Row(Modifier.fillMaxWidth().clickable { go(item) }.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(item.icon, null, tint = Pk.Oxblood, modifier = Modifier.size(21.dp))
                    Text(item.title, Modifier.weight(1f).padding(start = 14.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Pk.Charcoal)
                    Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable private fun CommunityPanel(items: List<Feature>, go: (Feature) -> Unit) {
    Column {
        Surface(Modifier.fillMaxWidth().clickable { items.firstOrNull()?.let(go) }, shape = RoundedCornerShape(27.dp), color = Pk.DeepDark) {
            Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("247 people are praying now", fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 23.sp, color = Color.White)
                    Text("Join the PrayerKey community", Modifier.padding(top = 5.dp), fontFamily = UtilitySans, fontSize = 12.sp, color = Color.White.copy(alpha = .64f))
                }
                Surface(shape = CircleShape, color = Pk.Oxblood) { Icon(Icons.Outlined.Groups, null, Modifier.padding(14.dp), tint = Color.White) }
            }
        }
        CompactFeatureRail(items, go)
    }
}

@Composable private fun CompactFeatureRail(items: List<Feature>, go: (Feature) -> Unit) {
    Row(Modifier.padding(top = 12.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Surface(Modifier.width(164.dp).height(116.dp).clickable { go(item) }, shape = RoundedCornerShape(21.dp), color = Color.White, border = BorderStroke(1.dp, Pk.Hair)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(item.icon, null, tint = Pk.Oxblood, modifier = Modifier.size(23.dp))
                    Text(item.title, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Pk.Charcoal, maxLines = 2)
                }
            }
        }
    }
}

@Composable private fun DiscoveryGrid(items: List<Feature>, go: (Feature) -> Unit) {
    val colours = listOf(
        Color(0xFF6200ED), Color(0xFF1A0A2E), Color(0xFF7C3AED), Color(0xFF355C7D),
        Color(0xFF6F5528), Color(0xFF4338CA), Color(0xFF5B2A86), Color(0xFF805B36),
        Color(0xFF2D6A6A), Color(0xFF6D3B58), Color(0xFF344E41), Color(0xFF725AC1),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEachIndexed { row, pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEachIndexed { column, item ->
                    val index = row * 2 + column
                    DiscoveryCard(item, colours[index % colours.size], index, Modifier.weight(1f)) { go(item) }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable private fun DiscoveryCard(item: Feature, colour: Color, index: Int, modifier: Modifier, open: () -> Unit) {
    Surface(
        modifier.height(108.dp).clickable(onClick = open),
        shape = RoundedCornerShape(12.dp), color = colour,
        shadowElevation = 2.dp,
    ) {
        Box(Modifier.fillMaxSize()) {
            val cover = when (index % 6) {
                0 -> R.drawable.discovery_cover_1
                1 -> R.drawable.discovery_cover_2
                2 -> R.drawable.discovery_cover_3
                3 -> R.drawable.discovery_cover_4
                4 -> R.drawable.discovery_cover_5
                else -> R.drawable.discovery_cover_6
            }
            Image(
                painterResource(cover), null,
                Modifier.align(Alignment.CenterEnd).offset(x = 9.dp).width(76.dp).height(98.dp)
                    .rotate(if (index % 2 == 0) 8f else -8f),
                contentScale = ContentScale.Crop,
            )
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(colour, colour, Color.Transparent))))
            Text(
                item.title,
                Modifier.align(Alignment.CenterStart).padding(start = 13.dp, end = 65.dp),
                fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                lineHeight = 18.sp, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable private fun Metric(title: String, count: Int, icon: ImageVector, open: () -> Unit) {
    Surface(Modifier.width(148.dp).clickable(onClick = open), shape = RoundedCornerShape(22.dp), color = Color.White) {
        Column(Modifier.padding(18.dp)) {
            Icon(icon, null, tint = Pk.Oxblood)
            Text(count.toString(), Modifier.padding(top = 16.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Pk.Charcoal)
            Text(title, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp, color = Pk.Muted)
        }
    }
}

@Composable private fun FeatureGrid(items: List<Feature>, go: (Hub) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.forEachIndexed { index, item ->
            FeatureCard(item, index, Modifier.width(224.dp)) { go(item.hub) }
        }
    }
}

@Composable private fun FeatureCard(item: Feature, index: Int, modifier: Modifier, open: () -> Unit) {
    val dark = index % 4 == 0
    val surface = if (dark) Pk.DeepDark else Color.White
    val title = if (dark) Color.White else Pk.Charcoal
    val supporting = if (dark) Color.White.copy(alpha = .70f) else Pk.Muted
    val accent = if (dark) Pk.GoldLight else Pk.Oxblood
    Surface(
        modifier.height(176.dp).clickable(onClick = open),
        shape = RoundedCornerShape(26.dp),
        color = surface,
        border = BorderStroke(1.dp, if (dark) Pk.Gold.copy(alpha = .32f) else Pk.Hair),
        shadowElevation = if (dark) 4.dp else 1.dp,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Surface(shape = CircleShape, color = accent.copy(alpha = if (dark) .16f else .10f)) {
                Icon(item.icon, null, tint = accent, modifier = Modifier.padding(11.dp).size(24.dp))
            }
            Column {
                Text(item.title, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.detail, Modifier.weight(1f), fontFamily = UtilitySans, fontSize = 12.sp, color = supporting, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Icon(Icons.Outlined.ArrowForward, null, tint = accent, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable private fun PremiumCard(open: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = open), shape = RoundedCornerShape(26.dp), color = Color.White, border = BorderStroke(1.dp, Pk.Hair), shadowElevation = 2.dp) {
        Column(Modifier.padding(22.dp), content = content)
    }
}

@Composable private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 15.dp)) {
        Text(title, Modifier.padding(bottom = 12.dp), style = labelStyle(), color = Pk.Charcoal)
        content()
    }
}

private fun labelStyle() = androidx.compose.ui.text.TextStyle(fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp)

@Composable private fun FeatureDestination(feature: Feature, onClose: () -> Unit, openFull: () -> Unit) {
    DiscoveryExperienceScreen(feature.title, feature.detail, feature.icon, onClose, openFull)
    return
    val content = when (feature.title) {
        "Declarations" -> "I am guided by God's wisdom. I am strengthened for today's work. Fear will not govern my decisions. The grace of God is sufficient for me."
        "Prayer points" -> "Begin with worship. Thank God for His faithfulness. Ask for mercy and cleansing. Present the situation plainly. Pray the promise of Scripture. Finish with thanksgiving."
        "Prayer times" -> "Morning — surrender the day. Midday — return your attention to God. Evening — give thanks and review. Midnight — stand in prayer with focus."
        "Fasting" -> "Choose the purpose, duration, start time, end time, Scripture focus, and safe meal plan before beginning. Keep prayer—not hunger—as the centre."
        "Memory verse" -> "Read it aloud three times. Hide one phrase. Recite it. Write it from memory. Review tomorrow before unlocking the next verse."
        "Dreams" -> "Record what you remember before interpreting it: people, places, words, emotions, colours, and what happened immediately before waking."
        "Answered" -> "Move a prayer here when the answer becomes clear. Record the date, what changed, and the testimony you want to remember."
        "Prayer wall" -> "Read one request slowly. Pray before reacting. Mark it prayed only after you have genuinely stood with that person."
        else -> feature.detail + ". This space keeps your progress, history, and next faithful step together."
    }
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClose) { Icon(Icons.Outlined.Close, "Close", tint = Pk.Charcoal) }
                Text("PRAYERKEY", Modifier.weight(1f), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.6.sp, color = Pk.Oxblood)
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                Surface(shape = CircleShape, color = Pk.Oxblood.copy(alpha = .09f)) { Icon(feature.icon, null, Modifier.padding(16.dp).size(30.dp), tint = Pk.Oxblood) }
                Text(feature.title, Modifier.padding(top = 22.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp, color = Pk.Charcoal)
                Text(feature.detail.uppercase(), Modifier.padding(top = 9.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.2.sp, color = Pk.GoldDeep)
                HorizontalDivider(Modifier.padding(vertical = 24.dp), color = Pk.Hair)
                Text(content, fontFamily = com.prayerkey.manna.ui.theme.BookSerif, fontSize = 20.sp, lineHeight = 31.sp, color = Pk.Charcoal)
                Surface(Modifier.fillMaxWidth().padding(top = 28.dp), shape = RoundedCornerShape(22.dp), color = Color.White, border = BorderStroke(1.dp, Pk.Hair)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CheckCircleOutline, null, tint = Pk.Oxblood)
                        Text("Your activity here is saved privately on this device.", Modifier.padding(start = 13.dp), fontFamily = UtilitySans, fontSize = 12.sp, lineHeight = 18.sp, color = Pk.Muted)
                    }
                }
            }
            Button(openFull, Modifier.fillMaxWidth().padding(22.dp).height(56.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) {
                Text("OPEN ${feature.title.uppercase()}", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = .7.sp)
            }
        }
    }
}

private fun fixedFeatures() = listOf(
    Feature("My prayer list", "Bring every name", Icons.Outlined.FormatListBulleted, Hub.PRAY),
    Feature("Prayer points", "Pray by situation", Icons.Outlined.AutoAwesome, Hub.PRAY),
    Feature("Prayer times", "Morning to midnight", Icons.Outlined.Schedule, Hub.PRAY),
    Feature("Prayer challenge", "One guided day at a time", Icons.Outlined.LocalFireDepartment, Hub.PRAY),
    Feature("Fasting", "Purpose, prayer and progress", Icons.Outlined.WaterDrop, Hub.PRAY),
    Feature("Bible challenge", "Read with purpose", Icons.Outlined.MenuBook, Hub.BIBLE),
    Feature("Memory verse", "Carry the word", Icons.Outlined.BookmarkBorder, Hub.BIBLE),
    Feature("Voice prayers", "Pray aloud and keep it", Icons.Outlined.MicNone, Hub.PRAY),
    Feature("Prayer timer", "Stay in His presence", Icons.Outlined.Timer, Hub.PRAY),
    Feature("Midnight prayer", "Take the night watch", Icons.Outlined.DarkMode, Hub.PRAY),
    Feature("Warfare prayer", "Scripture-led prayer sets", Icons.Outlined.Shield, Hub.PRAY),
    Feature("Declarations", "Speak God's word", Icons.Outlined.Campaign, Hub.PRAY),
    Feature("Answered", "Remember His faithfulness", Icons.Outlined.CheckCircle, Hub.JOURNAL),
    Feature("Thanksgiving", "Name the goodness", Icons.Outlined.Celebration, Hub.JOURNAL),
    Feature("Dreams", "Record, pray and discern", Icons.Outlined.Nightlight, Hub.JOURNAL),
    Feature("Testimonies", "Tell what God did", Icons.Outlined.RecordVoiceOver, Hub.JOURNAL),
    Feature("Prayer wall", "Pray together", Icons.Outlined.ViewAgenda, Hub.CHURCH),
    Feature("Prayer partner", "Never pray alone", Icons.Outlined.PeopleOutline, Hub.CHURCH),
)

private fun conditionalFeatures(today: LocalDate): List<Feature> = buildList {
    add(Feature("Prayed for you", "Someone stood with you", Icons.Outlined.Favorite, Hub.PRAY))
    if (LocalTime.now().hour >= 21) add(Feature("Midnight watch", "The watch is open", Icons.Outlined.DarkMode, Hub.PRAY))
    if (today.dayOfWeek.value == 7 || today.dayOfWeek.value == 1) add(Feature("New sermon", "Sunday continues", Icons.Outlined.GraphicEq, Hub.CHURCH))
    if (today.dayOfMonth == 1) add(Feature("First fruits", "Begin the month with God", Icons.Outlined.Spa, Hub.PRAY))
}
