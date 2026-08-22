package com.prayerkey.manna.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.prayerkey.manna.data.passageFor
import com.prayerkey.manna.R
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.share.CardShareRenderer
import com.prayerkey.manna.ui.theme.DisplaySerif
import androidx.compose.ui.text.font.FontStyle
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.litSurface
import com.prayerkey.manna.ui.theme.litTile
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private enum class Hub { BIBLE, PRAY, CHURCH, JOURNAL }
private data class Feature(val title: String, val detail: String, val icon: ImageVector, val hub: Hub)
private data class ProgrammeCategory(val title: String, val items: List<Feature>)

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
    /** Devotional -> prayer list. The day's title and what it invites you to bring. */
    onKeepPrayer: (String, String) -> Unit,
    /** Devotional -> journal. The day's question, and the answer written to it. */
    onKeepReflection: (String, String) -> Unit,
    onSettings: () -> Unit,
    initialAction: String? = null,
    onInitialActionConsumed: () -> Unit = {},
) {
    val today = remember { LocalDate.now() }
    val dailyPassage = remember(today) { passageFor(today) }
    var devotionalOpen by rememberSaveable { mutableStateOf(false) }
    var topicalBibleOpen by rememberSaveable { mutableStateOf(false) }
    var emergencyPrayerOpen by rememberSaveable { mutableStateOf(false) }
    var morningPrayerOpen by rememberSaveable { mutableStateOf(false) }
    var prayerSessionOpen by rememberSaveable { mutableStateOf(false) }
    /* The ten-pull daily session. Separate from prayerSessionOpen above,
       which is the timed guided journey — different thing, same word. */
    var todayPullsOpen by rememberSaveable { mutableStateOf(false) }
    var prayerCalendarOpen by rememberSaveable { mutableStateOf(false) }
    var selectedFeature by remember { mutableStateOf<Feature?>(null) }

    LaunchedEffect(initialAction) {
        when (initialAction) {
            "devotional" -> devotionalOpen = true
            "prayer_session" -> todayPullsOpen = true
            "fasting" -> selectedFeature = Feature("Start a fast", "", Icons.Outlined.WaterDrop, Hub.PRAY)
            "prayer_challenge" -> selectedFeature = Feature("Prayer challenge", "", Icons.Outlined.LocalFireDepartment, Hub.PRAY)
            "midnight" -> selectedFeature = Feature("Midnight prayer", "", Icons.Outlined.DarkMode, Hub.PRAY)
            "warfare" -> selectedFeature = Feature("Warfare prayer", "", Icons.Outlined.Shield, Hub.PRAY)
        }
        if (initialAction != null) onInitialActionConsumed()
    }

    /* The written year walks from the day you started, not from 1 January, so
       somebody installing in November gets day 1 rather than day 318. */
    val context = androidx.compose.ui.platform.LocalContext.current
    val startedOn = remember {
        val prefs = context.getSharedPreferences("daily_journey", android.content.Context.MODE_PRIVATE)
        prefs.getString("devotional-started", null)?.let(LocalDate::parse)
            ?: today.also { prefs.edit().putString("devotional-started", it.toString()).apply() }
    }
    var written by remember(today) { mutableStateOf<com.prayerkey.manna.data.Devotional?>(null) }
    var writtenNext by remember(today) { mutableStateOf<com.prayerkey.manna.data.Devotional?>(null) }
    LaunchedEffect(today, startedOn) {
        written = com.prayerkey.manna.data.Devotionals.forDate(context, today, startedOn)
        written?.let { writtenNext = com.prayerkey.manna.data.Devotionals.after(context, it) }
    }

    /* Today, assembled from one day rather than four rotations that never met.
       The devotional is the spine: its passage is the day's reading and its
       subject chooses the day's prayer. */
    /* Off the main thread. `remember { load(context) }` opened and parsed a
       543-entry JSON during composition, which is file I/O on the thread that
       draws the frame. */
    var prayerTopics by remember { mutableStateOf<List<com.prayerkey.manna.data.PrayerTopic>>(emptyList()) }
    LaunchedEffect(Unit) {
        prayerTopics = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.prayerkey.manna.data.LocalPrayerTopics.load(context)
        }
    }
    val plan = remember(written, prayerTopics, today) {
        written?.let { com.prayerkey.manna.data.planFor(it, prayerTopics, today) }
    }
    /* Read rather than remembered: the session is usually prayed from the
       notification, so Home is recomposed after the fact and a cached value
       would show 0/10 for a morning that is already done. */
    var todayPulled by remember(today) { mutableIntStateOf(0) }
    LaunchedEffect(todayPullsOpen) {
        todayPulled = com.prayerkey.manna.data.PrayerPullsStore.progress(context, today)
    }
    val morningPrayer = remember(prayerTopics, today) { com.prayerkey.manna.data.dailyPrayerFor(today, prayerTopics) }
    var todayVerse by remember(plan) { mutableStateOf<VerseCard?>(null) }
    LaunchedEffect(plan) {
        val reading = plan?.reading ?: return@LaunchedEffect
        val text = com.prayerkey.manna.data.OfflineBible(context)
            .chapter(reading.book, reading.chapter)
            .firstOrNull { it.verse == reading.verse }?.text
        if (text != null) {
            todayVerse = VerseCard(reading.ref.label, "KJV", text, "")
        }
    }

    if (devotionalOpen) {
        Dialog(
            onDismissRequest = { devotionalOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            val day = written
            if (day != null) {
                DevotionalReader(
                    day = day,
                    tomorrow = writtenNext,
                    onClose = { devotionalOpen = false },
                    onOpenPassage = { _, _ -> devotionalOpen = false; onOpenBible() },
                    onAddPrayer = { onKeepPrayer(day.title, day.addLabel) },
                    onKeepReflection = { onKeepReflection(day.question, it) },
                    onComplete = { devotionalOpen = false },
                )
            }
        }
    }
    if (topicalBibleOpen) {
        Dialog(
            onDismissRequest = { topicalBibleOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) { TopicalBibleScreen { topicalBibleOpen = false } }
    }
    if (emergencyPrayerOpen) {
        Dialog(
            onDismissRequest = { emergencyPrayerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            EmergencyPrayerEngine(
                onClose = { emergencyPrayerOpen = false },
                onSave = onKeepPrayer,
            )
        }
    }
    if (morningPrayerOpen) {
        Dialog(
            onDismissRequest = { morningPrayerOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            DailyRhythmExperience("morning", onClose = { morningPrayerOpen = false }, onComplete = { morningPrayerOpen = false })
        }
    }
    if (prayerSessionOpen) {
        Dialog(
            onDismissRequest = { prayerSessionOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            PrayerSessionScreen(
                onClose = { prayerSessionOpen = false },
                onSaveNote = { note -> onKeepReflection("Prayer session", note) },
            )
        }
    }
    if (todayPullsOpen) {
        /* Resolved here rather than inside the screen so the session is built
           from the same TodayPlan the rest of Today is built from — the pulls
           and the day's reading are about one subject or the feature is just
           another rotation. */
        val session = remember(plan, today) {
            com.prayerkey.manna.data.pullsFor(today, plan?.prayer, plan?.readingLabel)
        }
        Dialog(
            onDismissRequest = { todayPullsOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            com.prayerkey.manna.ui.session.DailyPrayerSession(
                session = session,
                startAt = remember { com.prayerkey.manna.data.PrayerPullsStore.resumeAt(context) },
                onProgress = { at -> com.prayerkey.manna.data.PrayerPullsStore.record(context, at) },
                onClose = { todayPullsOpen = false },
            )
        }
    }
    if (prayerCalendarOpen) {
        Dialog(
            onDismissRequest = { prayerCalendarOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) { PrayerCalendarScreen { prayerCalendarOpen = false } }
    }
    /* Six of these have finished screens already built. They were opening a
       placeholder instead, which is the difference between a feature that works
       and a card that lies. Routed to the real thing. */
    val go: (Feature) -> Unit = { feature ->
        when (feature.title) {
            "Read the Bible" -> onOpenBible()
            "Audio Bible" -> onOpenBible()
            "Topical Bible verses" -> topicalBibleOpen = true
            "Journal" -> onOpenJournal()
            "Live sermon", "Sermon notes" -> onOpenChurch()
            "My prayer list" -> onOpenPrayer()
            else -> selectedFeature = feature
        }
    }
    selectedFeature?.let { feature ->
        Dialog(onDismissRequest = { selectedFeature = null }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            FeatureDestination(feature, onKeep = onKeepPrayer, onClose = { selectedFeature = null }) { }
        }
    }
    val programmes = remember { programmeCategories() }
    val conditional = remember(today) { conditionalFeatures(today) }

    Column(
        Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding()
            .verticalScroll(rememberScrollState()).padding(bottom = Pk.NavClearance)
    ) {
        Header(today, streak, onSettings)
        ShortcutRail(
            streak = streak,
            todayBadge = todayPulled.takeIf { it > 0 }?.let { "$it/10" },
            todayPulls = { todayPullsOpen = true },
            prayNow = onOpenPrayer,
            ledger = onOpenJournal,
            bible = onOpenBible,
            church = onOpenChurch,
            open = go,
        )
        HeroRail(written?.title.orEmpty()) { devotionalOpen = true }

        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 15.dp)) {
            TodayRhythmPanel(
                savedCount, journalCount, streak,
                onOpenPrayer, { devotionalOpen = true }, onOpenBible,
                onOpenJournal, onOpenPrayer, onOpenJournal,
            )
            PrayerSessionEntry(
                start = { prayerSessionOpen = true },
                history = { prayerCalendarOpen = true },
            )
        }

        Section("TODAY") {
            VerseCard(todayVerse, onOpenWord)
            Spacer(Modifier.height(12.dp))
            PrayerNowCard(morningPrayer) { morningPrayerOpen = true }
        }

        EmergencyCard { emergencyPrayerOpen = true }

        TopicalBibleEntry { topicalBibleOpen = true }

        conditional.takeIf { it.isNotEmpty() }?.let { items ->
            Section("FOR THIS MOMENT") { MomentCards(items, go) }
        }

        ProgrammeLibrary(programmes, go)

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
    Box(Modifier.fillMaxWidth().height(74.dp).padding(horizontal = 20.dp)) {
        Surface(
            onClick = settings,
            modifier = Modifier.align(Alignment.CenterStart).size(36.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = .78f),
            border = BorderStroke(.6.dp, Color.White),
            shadowElevation = 6.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Menu, "Open settings", tint = Pk.Charcoal, modifier = Modifier.size(18.dp))
            }
        }
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PRAYERKEY", fontFamily = DisplaySerif, fontWeight = FontWeight.Normal, fontSize = 24.sp, letterSpacing = 1.45.sp, color = Pk.Charcoal)
            Text(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")), Modifier.padding(top = 5.dp), fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.Muted)
        }
        Surface(
            onClick = settings,
            modifier = Modifier.align(Alignment.CenterEnd).size(36.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = .78f),
            border = BorderStroke(.6.dp, Color.White),
            shadowElevation = 6.dp,
        ) {
            BadgedBox(
                modifier = Modifier.padding(9.dp),
                badge = {
                    if (streak > 0) Badge(containerColor = Pk.Oxblood, contentColor = Color.White) {
                        Text(streak.coerceAtMost(99).toString(), fontSize = 8.sp)
                    }
                },
            ) { Icon(Icons.Outlined.NotificationsNone, "Prayer reminders", tint = Pk.Charcoal, modifier = Modifier.size(18.dp)) }
        }
    }
}

private data class HomeShortcut(val label: String, val icon: ImageVector, val badge: String? = null, val open: () -> Unit)

@Composable private fun ShortcutRail(
    streak: Int,
    /** How many of today's ten pulls are prayed, shown on the badge. */
    todayBadge: String?,
    todayPulls: () -> Unit,
    prayNow: () -> Unit,
    ledger: () -> Unit,
    bible: () -> Unit,
    church: () -> Unit,
    open: (Feature) -> Unit,
) {
    // Shortcuts always open at the urgent first action. A saveable scroll state
    // could restore this rail halfway across after process recreation.
    val shortcutScroll = remember { androidx.compose.foundation.ScrollState(0) }
    val shortcuts = listOf(
        HomeShortcut("Today", Icons.Outlined.WbSunny, todayBadge, open = todayPulls),
        HomeShortcut("Pray now", Icons.Outlined.Bolt, open = prayNow),
        HomeShortcut("My prayers", Icons.Outlined.FactCheck, open = ledger),
        HomeShortcut("Prayer points", Icons.Outlined.FormatListBulleted) { open(Feature("Prayer points", "Pray by situation", Icons.Outlined.FormatListBulleted, Hub.PRAY)) },
        HomeShortcut("Prayer challenge", Icons.Outlined.LocalFireDepartment, if (streak > 0) "DAY $streak" else null) { open(Feature("Prayer challenge", "One guided day at a time", Icons.Outlined.LocalFireDepartment, Hub.PRAY)) },
        HomeShortcut("Midnight", Icons.Outlined.DarkMode) { open(Feature("Midnight prayer", "Take the night watch", Icons.Outlined.DarkMode, Hub.PRAY)) },
        HomeShortcut("Fasting", Icons.Outlined.WaterDrop) { open(Feature("Start a fast", "Choose a focus and a safe rhythm", Icons.Outlined.WaterDrop, Hub.PRAY)) },
        HomeShortcut("Bible", Icons.Outlined.MenuBook, open = bible),
        HomeShortcut("Audio", Icons.Outlined.Headphones) { open(Feature("Audio Bible", "Listen chapter by chapter", Icons.Outlined.Headphones, Hub.BIBLE)) },
        HomeShortcut("Warfare", Icons.Outlined.Shield) { open(Feature("Warfare prayer", "Scripture-led prayer sets", Icons.Outlined.Shield, Hub.PRAY)) },
        HomeShortcut("Answered", Icons.Outlined.CheckCircle, open = ledger),
        HomeShortcut("Church", Icons.Outlined.Church, open = church),
    )
    Row(
        Modifier.fillMaxWidth().horizontalScroll(shortcutScroll).padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        shortcuts.forEach { item ->
            Column(
                Modifier.width(76.dp).clickable(onClick = item.open),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(70.dp).border(2.2.dp, Pk.Oxblood.copy(alpha = .10f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            Modifier.size(62.dp), shape = CircleShape, color = Color.White.copy(alpha = .82f),
                            border = BorderStroke(.7.dp, Color.White), shadowElevation = 4.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(item.icon, null, tint = Pk.Oxblood, modifier = Modifier.size(27.dp))
                            }
                        }
                    }
                    item.badge?.let { badge ->
                        Surface(
                            Modifier.align(Alignment.TopEnd), shape = RoundedCornerShape(99.dp),
                            color = Pk.Oxblood, border = BorderStroke(1.dp, Pk.Cream),
                        ) { Text(badge, Modifier.padding(horizontal = 6.dp, vertical = 3.dp), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold) }
                    }
                }
                Text(
                    item.label, Modifier.padding(top = 7.dp),
                    fontFamily = UtilitySans, fontSize = 10.sp, lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium, color = Pk.Charcoal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2,
                )
            }
        }
    }
}

@Composable private fun HeroRail(title: String, open: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(470.dp).clickable(onClick = open)
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
            Surface(shape = CircleShape, color = Color.White.copy(alpha = .16f), border = BorderStroke(.5.dp, Color.White.copy(alpha = .24f))) {
                Text("TODAY'S DEVOTIONAL", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp, color = Color.White)
            }
            Text(title, Modifier.padding(top = 12.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 38.sp, color = Color.White)
            Row(Modifier.fillMaxWidth().padding(top = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(4) { Box(Modifier.padding(end = 6.dp).size(if (it == 0) 20.dp else 6.dp, 6.dp).background(if (it == 0) Pk.GoldLight else Color.White.copy(alpha = .38f), CircleShape)) }
                Spacer(Modifier.weight(1f))
                Text("8 MIN", fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Pk.GoldLight)
            }
        }
    }
}

/**
 * The day's verse.
 *
 * It used to be Matthew 7:7, hardcoded, on every day of the year — a card
 * headed TODAY that had never once been about today. It now shows the verse
 * the day's devotional is built on, resolved from the KJV asset, so the verse,
 * the reading and the writing are the same passage.
 *
 * Falls back to the fixed verse only until the day has loaded, so the card is
 * never empty on a cold start.
 */
@Composable private fun VerseCard(today: VerseCard?, open: () -> Unit) {
    val context = LocalContext.current
    /* The reference and the quote below used to be literal strings, while
       `card` was passed only to SAVE and SHARE. So the card drew Matthew 7:7 on
       every day of the year, and once the day's verse started arriving the two
       diverged: the screen showed one verse and sharing exported another.
       Everything visible now comes from `card`. */
    val fallback = remember {
        VerseCard("Matthew 7:7", "KJV", "Ask, and it will be given to you; seek, and you will find; knock, and the door will be opened to you.", "")
    }
    val card = today ?: fallback
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
                Text(card.reference.uppercase(), Modifier.padding(top = 17.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 27.sp, letterSpacing = 1.1.sp, color = Pk.Oxblood, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Row(Modifier.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.width(42.dp), color = Pk.Gold.copy(alpha = .72f))
                    Box(Modifier.padding(horizontal = 8.dp).size(5.dp).background(Pk.Oxblood, CircleShape))
                    HorizontalDivider(Modifier.width(42.dp), color = Pk.Gold.copy(alpha = .72f))
                }
                Text(
                    "“" + card.verse + "”",
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

/**
 * The day's prayer.
 *
 * This paragraph was hardcoded and identical on every day of the year. It now
 * shows one of the 543 written prayers, chosen from the category the day's
 * devotional is about — so the reading, the writing and the prayer are about
 * the same thing rather than three unrelated things sharing a date.
 */
@Composable private fun PrayerNowCard(prayer: com.prayerkey.manna.model.DailyPrayer, open: () -> Unit) {
    val context = LocalContext.current
    val words = prayer.prayer /*
        ?: "Lord, according to Your Word, order my steps today. Let this day be a blessing to me. Give me wisdom for every decision, peace in every pressure, favour in the work of my hands, and protection wherever I go. Keep my heart close to You and let my life bring hope to someone today. In Jesus’ name, Amen."
    */
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), color = Pk.DeepDark, shadowElevation = 5.dp) {
        Box(Modifier.fillMaxWidth().height(520.dp)) {
            Image(painterResource(R.drawable.today_prayer_violet), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Pk.DeepDark.copy(alpha = .08f), Pk.DeepDark.copy(alpha = .42f), Pk.DeepDark.copy(alpha = .88f)))))
            Column(Modifier.fillMaxSize().padding(horizontal = 23.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = .13f)) {
                    Icon(Icons.Outlined.SelfImprovement, null, Modifier.padding(11.dp).size(25.dp), tint = Pk.GoldLight)
                }
                Text("MORNING PRAYER", Modifier.padding(top = 13.dp), style = labelStyle(), color = Pk.GoldLight)
                Text(
                    words,
                    Modifier.weight(1f).padding(top = 22.dp),
                    fontFamily = DisplaySerif,
                    fontSize = 18.sp,
                    lineHeight = 27.sp,
                    color = Color.White.copy(alpha = .94f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 11,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
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

@Composable private fun PrayerSessionEntry(start: () -> Unit, history: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(25.dp), color = Pk.DeepDark,
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Timer, null, tint = Pk.GoldLight)
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("Prayer session", fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                    Text("Pray with guidance or use a quiet timer", Modifier.padding(top = 3.dp), fontFamily = UtilitySans, fontSize = 12.sp, color = Color.White.copy(alpha = .67f))
                }
            }
            Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(start, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) {
                    Text("START", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(history, Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(15.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = .25f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                    Text("CALENDAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable private fun TodayRhythmPanel(
    saved: Int, journal: Int, streak: Int,
    morningPrayer: () -> Unit, devotional: () -> Unit, bible: () -> Unit,
    reflection: () -> Unit, eveningPrayer: () -> Unit, history: () -> Unit,
) {
    val context = LocalContext.current
    val dateKey = LocalDate.now().toString()
    val prefs = remember { context.getSharedPreferences("today_rhythm", android.content.Context.MODE_PRIVATE) }
    var completed by remember(dateKey) { mutableStateOf(prefs.getStringSet(dateKey, emptySet()).orEmpty()) }
    var openMoment by remember { mutableStateOf<String?>(null) }
    fun finish(id: String) {
        completed = completed + id
        prefs.edit().putStringSet(dateKey, completed).apply()
        openMoment = null
    }
    val items = listOf(
        Triple("morning", "Father, order my steps today", "Morning prayer") to Pair(Icons.Outlined.WbSunny, morningPrayer),
        Triple("devotional", "Lord, speak through Your Word", "Daily devotional") to Pair(Icons.Outlined.AutoStories, devotional),
        Triple("bible", "Open my eyes to understand Your Word", "Bible reading") to Pair(Icons.Outlined.MenuBook, bible),
        Triple("reflection", "Search my heart and lead me", "Prayer and reflection") to Pair(Icons.Outlined.EditNote, reflection),
        Triple("evening", "Father, I return this day to You", "Evening prayer") to Pair(Icons.Outlined.NightsStay, eveningPrayer),
    )
    openMoment?.let { moment ->
        Dialog(onDismissRequest = { openMoment = null }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            DailyRhythmExperience(moment, onClose = { openMoment = null }, onComplete = { finish(moment) })
        }
    }
    /* A lit surface, not a flat white rectangle. Gradient face, bright inner
       hairline where the light lands, bounce along the base, warm and cool
       fringes down the sides, one diagonal specular. */
    val cardShape = RoundedCornerShape(28.dp)
    Surface(shape = cardShape, color = Color.Transparent, shadowElevation = 14.dp) {
        Box(Modifier.litSurface(cardShape)) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Row(Modifier.padding(horizontal = 20.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}", fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 25.sp, color = Pk.Charcoal)
                }
                Box(Modifier.size(44.dp).clip(CircleShape).background(Pk.Oxblood.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.CalendarToday, null, tint = Pk.Oxblood, modifier = Modifier.size(21.dp))
                }
            }
            LinearProgressIndicator(
                progress = { completed.size.toFloat() / items.size },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Pk.Oxblood,
                trackColor = Pk.Charcoal.copy(alpha = .08f),
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Spacer(Modifier.height(6.dp))
            items.forEach { item ->
                val identity = item.first
                val icon = item.second.first
                val destination = item.second.second
                val done = identity.first in completed

                /* Devotional and Bible have real screens, and this panel is
                   already handed them — `{ devotionalOpen = true }` opens the
                   written year. The row ignored them and opened the rhythm's
                   own copy, which renders the retired templated devotional, so
                   the hero and this row showed different content for the same
                   day. Rows with a real destination now go there. */
                val hasOwnScreen = identity.first == "devotional" || identity.first == "bible"

                /* One line, its own soft container, no divider.
                   Two lines per row put ten lines of text in this card and made
                   "Open my eyes to understand Your Word" wrap mid-phrase; the
                   hairline dividers turned it into a settings list. The prayer
                   is not lost — it belongs inside the moment, which is where
                   you read it. The row only has to say what it is. */
                Row(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (done) Pk.Oxblood.copy(alpha = .045f) else Color.White)
                        .clickable {
                            if (hasOwnScreen) {
                                completed = completed + identity.first
                                prefs.edit().putStringSet(dateKey, completed).apply()
                                destination()
                            } else {
                                openMoment = identity.first
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val tileShape = RoundedCornerShape(15.dp)
                    Box(
                        Modifier.size(46.dp)
                            .shadow(
                                elevation = if (done) 10.dp else 2.dp,
                                shape = tileShape,
                                ambientColor = Pk.Oxblood,
                                spotColor = Pk.Oxblood,
                            )
                            .clip(tileShape)
                            .litTile(tileShape, done),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            icon, null,
                            tint = if (done) Color.White else Pk.Oxblood,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Text(
                        identity.third,
                        fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp,
                        color = if (done) Pk.Muted else Pk.Charcoal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                    )
                    Box(
                        Modifier.size(28.dp)
                            .then(
                                if (done) {
                                    Modifier.shadow(8.dp, CircleShape, ambientColor = Pk.Oxblood, spotColor = Pk.Oxblood)
                                } else {
                                    Modifier
                                },
                            )
                            .clip(CircleShape)
                            .background(if (done) Pk.Oxblood.copy(alpha = .10f) else Color.Transparent)
                            .border(1.6.dp, if (done) Pk.Oxblood else Pk.Muted.copy(alpha = .34f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (done) Icon(Icons.Outlined.Check, null, tint = Pk.Oxblood, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        }
    }
    Surface(onClick = history, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Pk.Hair)) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.History, null, tint = Pk.Oxblood)
            Column(Modifier.weight(1f).padding(start = 13.dp)) {
                Text("Prayer history", fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Pk.Charcoal)
                Text("$saved open · $journal entries · ${streak.coerceAtLeast(1)} day streak", Modifier.padding(top = 3.dp), fontFamily = UtilitySans, fontSize = 11.sp, color = Pk.Muted)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted)
        }
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

/**
 * The programmes grid.
 *
 * This used to carry twelve hardcoded colours — steel blue, olive, brown,
 * teal, maroon, forest green and three off-brand purples — of which exactly
 * two were in the app's palette. They were assigned by grid position, so
 * "Fasting" was brown for no reason except that it was sixth, and inserting a
 * card anywhere repainted everything below it.
 *
 * Now the tint comes from the hub a card belongs to, which means it survives
 * reordering and actually tells you something: prayer programmes read as one
 * family, and the four that are not prayer separate themselves. All four
 * tints are from [Pk].
 *
 * The variety is carried by the photography instead, which is the right thing
 * to carry it — see [DiscoveryCard].
 */
@Composable private fun ProgrammeLibrary(categories: List<ProgrammeCategory>, go: (Feature) -> Unit) {
    var selectedCategory by remember { mutableStateOf<ProgrammeCategory?>(null) }
    selectedCategory?.let { category ->
        Dialog(
            onDismissRequest = { selectedCategory = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) { ProgrammeCategoryScreen(category, { selectedCategory = null }, go) }
    }
    Column(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp)) {
        Text("PRAYERKEY PROGRAMMES", Modifier.padding(horizontal = 22.dp), style = labelStyle(), color = Pk.Charcoal)
        val originals = categories.first()
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(originals.title.uppercase(), Modifier.weight(1f), style = labelStyle(), color = Pk.Oxblood)
        }
        Column(Modifier.padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            originals.items.forEachIndexed { index, item -> OriginalProgrammeCard(item, index) { go(item) } }
        }
        Text("EXPLORE BY CATEGORY", Modifier.padding(start = 22.dp, top = 30.dp, bottom = 12.dp), style = labelStyle(), color = Pk.Charcoal)
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            categories.drop(1).chunked(2).forEachIndexed { rowIndex, rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    rowItems.forEachIndexed { columnIndex, category ->
                        val index = rowIndex * 2 + columnIndex
                        Surface(
                            Modifier.weight(1f).height(132.dp).clickable { selectedCategory = category },
                            shape = RoundedCornerShape(22.dp), color = Pk.DeepDark, shadowElevation = 4.dp,
                        ) {
                            Box(Modifier.fillMaxSize()) {
                                Image(
                                    painterResource(listOf(R.drawable.discovery_cover_1, R.drawable.discovery_cover_2, R.drawable.discovery_cover_3, R.drawable.discovery_cover_4, R.drawable.discovery_cover_5, R.drawable.discovery_cover_6)[index % 6]),
                                    null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                                )
                                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Pk.Oxblood.copy(alpha = .10f), Pk.DeepDark.copy(alpha = .88f)))))
                                Text(
                                    category.title,
                                    Modifier.align(Alignment.BottomStart).padding(15.dp),
                                    fontFamily = UtilitySans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    lineHeight = 19.sp,
                                    color = Color.White,
                                    maxLines = 2,
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable private fun ProgrammeCategoryScreen(category: ProgrammeCategory, close: () -> Unit, go: (Feature) -> Unit) {
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(close) { Icon(Icons.Outlined.Close, "Close", tint = Pk.Charcoal) }
                Text("PRAYERKEY", Modifier.weight(1f), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.5.sp, color = Pk.Oxblood)
                Spacer(Modifier.width(48.dp))
            }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 12.dp)) {
                Text(category.title, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 36.sp, color = Pk.Charcoal)
                Spacer(Modifier.height(22.dp))
                DiscoveryGrid(category.items) { feature -> close(); go(feature) }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable private fun OriginalProgrammeCard(item: Feature, index: Int, open: () -> Unit) {
    val cover = listOf(R.drawable.discovery_cover_2, R.drawable.discovery_cover_4, R.drawable.discovery_cover_6)[index % 3]
    Surface(
        Modifier.fillMaxWidth().height(188.dp).clickable(onClick = open),
        shape = RoundedCornerShape(26.dp), color = Pk.DeepDark, shadowElevation = 5.dp,
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(painterResource(cover), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Pk.DeepDark.copy(alpha = .96f), Pk.VioletDeep.copy(alpha = .68f), Color.Transparent))))
            Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = .14f)) {
                    Icon(item.icon, null, Modifier.padding(10.dp).size(22.dp), tint = Pk.GoldLight)
                }
                Column(Modifier.fillMaxWidth(.76f)) {
                    Text(item.title, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 27.sp, color = Color.White)
                    Text("OPEN  →", Modifier.padding(top = 12.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.1.sp, color = Pk.GoldLight)
                }
            }
        }
    }
}

@Composable private fun DiscoveryGrid(items: List<Feature>, go: (Feature) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEachIndexed { row, pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEachIndexed { column, item ->
                    val index = row * 2 + column
                    DiscoveryCard(item, hubTint(item.hub), index, Modifier.weight(1f)) { go(item) }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Four tints, all from the palette, chosen by meaning rather than position. */
private fun hubTint(hub: Hub): Color = when (hub) {
    Hub.PRAY -> Pk.Oxblood
    Hub.BIBLE -> Pk.DeepDark
    Hub.JOURNAL -> Pk.VioletDeep
    Hub.CHURCH -> Pk.VioletLit
}

/**
 * One programme.
 *
 * The artwork is the card. It used to be a 76dp rectangle pinned to the right
 * edge, rotated eight degrees, and then buried under a horizontal gradient
 * that was fully opaque across the first two thirds — so most of every picture
 * was painted over, and what survived read as a sticker rather than a cover.
 *
 * Full bleed, with the tint held at low alpha over the top so the same six
 * covers read differently in different sections, and a scrim from the bottom
 * so the title has something to sit on whatever the photograph is doing there.
 * That scrim, not a flat fill, is what keeps white text legible on art.
 */
@Composable private fun DiscoveryCard(item: Feature, colour: Color, index: Int, modifier: Modifier, open: () -> Unit) {
    Surface(
        modifier.height(116.dp).clickable(onClick = open),
        shape = RoundedCornerShape(14.dp), color = colour,
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
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // the hub's colour, worn lightly, so six covers serve eighteen cards
            Box(Modifier.fillMaxSize().background(colour.copy(alpha = .52f)))
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        .45f to Color(0xFF0E0320).copy(alpha = .28f),
                        1f to Color(0xFF0E0320).copy(alpha = .82f),
                    ),
                ),
            )
            Text(
                item.title,
                Modifier.align(Alignment.BottomStart).padding(start = 13.dp, end = 13.dp, bottom = 12.dp),
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

/**
 * What a programme card opens.
 *
 * This used to be an unconditional `return` followed by thirty-five lines of
 * per-feature copy that could never run — a whole second screen, complete with
 * an "OPEN <FEATURE>" button wired to an empty lambda, that no user has ever
 * seen. Deleted rather than revived: [DiscoveryExperienceScreen] is the screen
 * these actually open, and two rival implementations is how the dead one got
 * there in the first place.
 */
@Composable private fun FeatureDestination(
    feature: Feature,
    onKeep: (String, String) -> Unit,
    onClose: () -> Unit,
    openFull: () -> Unit,
) {
    if (feature.title == "Prayer points") {
        com.prayerkey.manna.ui.screens.PrayerPointsScreen(onClose)
    } else {
        DiscoveryExperienceScreen(feature.title, feature.detail, feature.icon, onKeep, onClose, openFull)
    }
}

/**
 * The programmes on Home.
 *
 * "Prayer wall" and "Prayer partner" were removed rather than fixed. Both are
 * community features and there is no server behind them — the wall shipped
 * hardcoded activity ("Healing before surgery · 41 prayed") presenting invented
 * people as real ones praying for real requests. A facade that convincing is
 * worse than an absent feature, and it cannot be made honest without a backend.
 */
/**
 * The programme library.
 *
 * This held 86 entries. 45 of them opened the same placeholder screen, and six
 * more — Read the Bible, Audio Bible, Topical Bible, Journal, Live sermon,
 * Sermon notes — opened that placeholder despite having finished screens of
 * their own sitting in the app.
 *
 * What is left is what works: the programmes that run the full machine, and the
 * screens that already exist. A card that opens nothing is not a feature, and
 * removing it is the whole difference between a library and a menu of promises.
 *
 * Anything cut is in git. Nothing here is a placeholder.
 */
private fun programmeCategories() = listOf(
    ProgrammeCategory("Pray", listOf(
        Feature("Prayer points", "Pray by situation", Icons.Outlined.FormatListBulleted, Hub.PRAY),
        Feature("Prayer challenge", "One guided day at a time", Icons.Outlined.LocalFireDepartment, Hub.PRAY),
        Feature("Midnight prayer", "Take the night watch", Icons.Outlined.DarkMode, Hub.PRAY),
        Feature("Warfare prayer", "Scripture-led prayer sets", Icons.Outlined.Shield, Hub.PRAY),
        Feature("Declarations", "Speak God's Word", Icons.Outlined.Campaign, Hub.PRAY),
        Feature("My prayer list", "Every prayer in one place", Icons.Outlined.FactCheck, Hub.PRAY),
    )),
    ProgrammeCategory("Bible", listOf(
        Feature("Read the Bible", "Translations and offline reading", Icons.Outlined.MenuBook, Hub.BIBLE),
        Feature("Audio Bible", "Listen chapter by chapter", Icons.Outlined.Headphones, Hub.BIBLE),
        Feature("Bible challenge", "Finish today's portion to continue", Icons.Outlined.AutoStories, Hub.BIBLE),
        Feature("Memory verse", "Carry the Word", Icons.Outlined.BookmarkBorder, Hub.BIBLE),
        Feature("Topical Bible verses", "Scripture for subjects and situations", Icons.Outlined.Search, Hub.BIBLE),
    )),
    ProgrammeCategory("Fast", listOf(
        Feature("Start a fast", "Choose a focus and a safe rhythm", Icons.Outlined.WaterDrop, Hub.PRAY),
        Feature("Daniel fast", "A guided Daniel fast", Icons.Outlined.Spa, Hub.PRAY),
        Feature("Esther fast", "A serious guided fast", Icons.Outlined.Shield, Hub.PRAY),
        Feature("Timed fast", "Choose the hours you will keep", Icons.Outlined.Schedule, Hub.PRAY),
    )),
    ProgrammeCategory("Keep", listOf(
        Feature("Journal", "Write your walk with God", Icons.Outlined.EditNote, Hub.JOURNAL),
    )),
    ProgrammeCategory("Church", listOf(
        Feature("Live sermon", "Capture audio and live notes", Icons.Outlined.MicNone, Hub.CHURCH),
        Feature("Sermon notes", "Keep what was preached", Icons.Outlined.Notes, Hub.CHURCH),
    )),
)

private fun conditionalFeatures(today: LocalDate): List<Feature> = buildList {
    if (today.dayOfMonth == 1) add(Feature("First fruits", "Begin the month with God", Icons.Outlined.Spa, Hub.PRAY))
}
