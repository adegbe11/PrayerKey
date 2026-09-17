package com.prayerkey.manna.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.Challenge
import com.prayerkey.manna.data.Devotion
import com.prayerkey.manna.data.PrayerKeyApi
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
import kotlinx.coroutines.launch

private enum class Hub { BIBLE, PRAY, CHURCH, JOURNAL }
private data class Feature(val title: String, val detail: String, val icon: ImageVector, val hub: Hub)
private data class ProgrammeCategory(val title: String, val items: List<Feature>)
private data class HomeShelfItem(val title: String, val artwork: Int, val open: () -> Unit)

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
    onOpenPrayerRequest: (String) -> Unit,
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
    /* Keep an already-open Home screen honest across midnight. Without this,
       date-keyed content changed on the next app launch but could show
       yesterday's prayer and chapter while the app remained in memory. */
    var today by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            val now = java.time.ZonedDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
            val wait = java.time.Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1_000L)
            kotlinx.coroutines.delay(wait)
            today = LocalDate.now()
        }
    }
    val dailyPassage = remember(today) { passageFor(today) }
    var devotionalOpen by rememberSaveable { mutableStateOf(false) }
    var topicalBibleOpen by rememberSaveable { mutableStateOf(false) }
    var emergencyPrayerOpen by rememberSaveable { mutableStateOf(false) }
    var morningPrayerOpen by rememberSaveable { mutableStateOf(false) }
    var requestedOffice by rememberSaveable { mutableStateOf<String?>(null) }
    var prayerSessionOpen by rememberSaveable { mutableStateOf(false) }
    /* The ten-pull daily session. Separate from prayerSessionOpen above,
       which is the timed guided journey — different thing, same word. */
    var todayPullsOpen by rememberSaveable { mutableStateOf(false) }
    var biblePullsOpen by rememberSaveable { mutableStateOf(false) }
    var challengeShelfOpen by rememberSaveable { mutableStateOf(false) }
    var giveOpen by rememberSaveable { mutableStateOf(false) }
    var openChallengeId by rememberSaveable { mutableStateOf<String?>(null) }
    var prayerCalendarOpen by rememberSaveable { mutableStateOf(false) }
    var exploreOpen by rememberSaveable { mutableStateOf(false) }
    var selectedFeature by remember { mutableStateOf<Feature?>(null) }

    LaunchedEffect(initialAction) {
        when (initialAction) {
            "devotional" -> devotionalOpen = true
            "prayer_session" -> todayPullsOpen = true
            "bible_session" -> biblePullsOpen = true
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
    var todayBadgeText by remember(today) { mutableStateOf<String?>(null) }
    var readBadgeText by remember(today) { mutableStateOf<String?>(null) }
    LaunchedEffect(today, todayPullsOpen, biblePullsOpen) {
        todayBadgeText = com.prayerkey.manna.data.PullsProgress.Prayer.badge(context, today)
        readBadgeText = com.prayerkey.manna.data.PullsProgress.Bible.badge(context, today)
    }
    val morningPrayer = remember(prayerTopics, today) { com.prayerkey.manna.data.dailyPrayerFor(today, prayerTopics) }
    var todayVerse by remember(plan) { mutableStateOf<VerseCard?>(null) }
    LaunchedEffect(plan) {
        val reading = plan?.reading ?: return@LaunchedEffect
        val text = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.prayerkey.manna.data.OfflineBible(context)
                .chapter(reading.book, reading.chapter)
                .firstOrNull { it.verse == reading.verse }?.text
        }
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
                    onComplete = {
                        context.getSharedPreferences("home_completed", android.content.Context.MODE_PRIVATE)
                            .edit().putBoolean("${today}:devotional", true).apply()
                        devotionalOpen = false
                    },
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
            /* One card, four offices. The hour decides which - morning, evening,
               night, or the midnight watch. Four entry points on Home for what
               is one habit at four points of a day would be the clutter; being
               asked to choose between them at eleven at night would be worse. */
            val now = java.time.LocalTime.now()
            val close = { morningPrayerOpen = false }
            when (requestedOffice?.let { com.prayerkey.manna.data.OfficeStore.Office.valueOf(it) } ?: com.prayerkey.manna.data.OfficeStore.officeFor(now.hour, now.minute)) {
                com.prayerkey.manna.data.OfficeStore.Office.MORNING ->
                    com.prayerkey.manna.ui.morning.MorningPrayerScreen(onClose = close)
                com.prayerkey.manna.data.OfficeStore.Office.EVENING ->
                    com.prayerkey.manna.ui.morning.EveningPrayerScreen(onClose = close)
                com.prayerkey.manna.data.OfficeStore.Office.NIGHT ->
                    com.prayerkey.manna.ui.morning.NightPrayerScreen(onClose = close)
                com.prayerkey.manna.data.OfficeStore.Office.MIDNIGHT ->
                    com.prayerkey.manna.ui.morning.MidnightPrayerScreen(onClose = close)
            }
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
                startAt = remember { com.prayerkey.manna.data.PullsProgress.Prayer.resumeAt(context) },
                onProgress = { at -> com.prayerkey.manna.data.PullsProgress.Prayer.record(context, at, session.size) },
                onClose = { todayPullsOpen = false },
            )
        }
    }
    if (biblePullsOpen) {
        /* The chapter is cut here, from the same reading the devotional names,
           so the pulls and the day's prayer are about one subject. */
        var reading by remember(plan) { mutableStateOf<com.prayerkey.manna.data.BibleReading?>(null) }
        LaunchedEffect(plan) {
            val where = plan?.reading ?: return@LaunchedEffect
            val verses = com.prayerkey.manna.data.OfflineBible(context)
                .chapter(where.book, where.chapter)
            reading = com.prayerkey.manna.data.readingFrom(where.book, where.chapter, verses)
        }
        Dialog(
            onDismissRequest = { biblePullsOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            val chapter = reading
            if (chapter == null) {
                com.prayerkey.manna.ui.session.PullSessionLoading(plan?.readingLabel.orEmpty())
            } else {
                com.prayerkey.manna.ui.session.DailyBibleSession(
                    reading = chapter,
                    startAt = remember { com.prayerkey.manna.data.PullsProgress.Bible.resumeAt(context) },
                    onProgress = { at ->
                        com.prayerkey.manna.data.PullsProgress.Bible.record(context, at, chapter.size)
                    },
                    onClose = { biblePullsOpen = false },
                )
            }
        }
    }
    if (giveOpen) {
        Dialog(
            onDismissRequest = { giveOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            com.prayerkey.manna.ui.give.GiveScreen(onClose = { giveOpen = false })
        }
    }
    if (challengeShelfOpen) {
        Dialog(
            onDismissRequest = { challengeShelfOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            com.prayerkey.manna.ui.challenge.ChallengeShelf(
                onOpen = { challenge -> openChallengeId = challenge.id },
                onClose = { challengeShelfOpen = false },
            )
        }
    }
    openChallengeId?.let { id ->
        var challenge by remember(id) { mutableStateOf<com.prayerkey.manna.data.ChallengePlan?>(null) }
        LaunchedEffect(id) {
            challenge = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.prayerkey.manna.data.Challenges.byId(context, id)
            }
        }
        Dialog(
            onDismissRequest = { openChallengeId = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            challenge?.let { plan ->
                com.prayerkey.manna.ui.challenge.ChallengeDayScreen(
                    challenge = plan,
                    onClose = { openChallengeId = null },
                )
            }
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
    var homeCategory by remember { mutableStateOf<ProgrammeCategory?>(null) }
    homeCategory?.let { category ->
        Dialog(onDismissRequest = { homeCategory = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            ProgrammeCategoryScreen(category, { homeCategory = null }, go)
        }
    }
    /* The bell opens reminders. It used to open a dialog whose entire content
       was a sentence saying reminders are in Settings, and whose confirm button
       went to Settings - a signpost in front of a door. */
    val conditional = remember(today) { conditionalFeatures(today) }

    if (exploreOpen) {
        Dialog(
            onDismissRequest = { exploreOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
        ) {
            ExplorePrayerKeyScreen(
                categories = programmes,
                momentItems = conditional,
                go = { feature -> exploreOpen = false; go(feature) },
                startPrayer = { exploreOpen = false; prayerSessionOpen = true },
                prayerHistory = { exploreOpen = false; prayerCalendarOpen = true },
                onClose = { exploreOpen = false },
            )
        }
    }

    /*
     * The masthead stays put and the page runs under it.
     *
     * It used to scroll away with everything else, so the devotional hero rose
     * up and the screen lost its name and its date. iOS 27 addressed exactly
     * this - content passing beneath a floating bar now gets a uniform bar
     * above it, so the words stay legible against whatever is underneath.
     *
     * Uniform is the word that matters. Not a lens: there is nothing to refract
     * over a flat page, and a bar that sampled this screen would be sampling
     * its own output. A diffused ground and a darkened edge, brought in only
     * once something is actually behind it.
     */
    val scroll = rememberScrollState()
    val underneath = (scroll.value / 140f).coerceIn(0f, 1f)

    Box(Modifier.fillMaxSize().background(Pk.Cream)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(scroll).padding(bottom = Pk.NavClearance)
        ) {
        Spacer(Modifier.height(74.dp))
        ShortcutRail(
            todayBadge = todayBadgeText,
            todayPulls = { todayPullsOpen = true },
            readBadge = readBadgeText,
            biblePulls = { biblePullsOpen = true },
            prayNow = onOpenPrayer,
            challenges = { challengeShelfOpen = true },
        )
        CompactPrayerPrompt(onOpenPrayerRequest)
        HeroRail(written?.title.orEmpty()) { if (written != null) devotionalOpen = true }

        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 15.dp)) {
            TodayRhythmPanel(
                devotionalTitle = written?.title.orEmpty(),
                readingLabel = plan?.readingLabel.orEmpty(),
                morningPrayer = { requestedOffice = "MORNING"; morningPrayerOpen = true },
                devotional = { devotionalOpen = true },
                bible = { biblePullsOpen = true },
                eveningPrayer = { requestedOffice = "EVENING"; morningPrayerOpen = true },
                calendar = { prayerCalendarOpen = true },
            )
        }

        PrayerKeyIdentitySection(
            fast = { homeCategory = programmes.first { it.title == "Fast" } },
            grow = { homeCategory = ProgrammeCategory("Grow", listOf(Feature("Prayer challenge", "Build a daily rhythm", Icons.Outlined.LocalFireDepartment, Hub.PRAY), Feature("Memory verse", "Carry Scripture with you", Icons.Outlined.BookmarkBorder, Hub.BIBLE), Feature("Bible challenge", "Grow in the Word", Icons.Outlined.AutoStories, Hub.BIBLE))) },
            topicalBible = { topicalBibleOpen = true },
        )
        GiveSupportCard { giveOpen = true }
        Section("A WORD TO CARRY") {
            ClosingVerseCard(today) { verse, reference ->
                onOpenPrayerRequest("Help me pray $reference: $verse")
            }
        }
        }

        Box(
            /* The ground is painted first and the inset applied after, so it
               covers the status bar strip as well. With the padding first, the
               bar only covered its own height and the page scrolled up through
               the clock and the battery with nothing behind it - which is the
               exact failure edge-to-edge introduces and the reason iOS 27 made
               this bar uniform. */
            Modifier.fillMaxWidth()
                .drawBehind {
                    /* The uniform ground, and the darkened edge under it. Both
                       arrive with the content rather than sitting there from
                       the first frame - at the top of the page there is nothing
                       behind the bar and nothing to separate it from. */
                    drawRect(Pk.Cream.copy(alpha = .94f * underneath))
                    drawRect(
                        Color.Black.copy(alpha = .10f * underneath),
                        topLeft = Offset(0f, size.height - 1.dp.toPx()),
                        size = Size(size.width, 1.dp.toPx()),
                    )
                },
        ) {
            Box(Modifier.statusBarsPadding()) {
                Header(today, streak, onSettings, reminders = onSettings)
            }
        }
    }
}

@Composable
/**
 * The invitation. The asking happens on its own page.
 *
 * This was the whole Stripe checkout - an amount field, four presets, a
 * once-or-monthly switch, an error slot and a button - sitting on Home between
 * the day's prayer and the closing verse. A payment form in the middle of a
 * devotional, and the loudest thing on the page that was not prayer.
 *
 * What stays is the artwork and the sentence. A form on a scrolling page is
 * something to scroll past; a page is a decision, and the people who open it
 * meant to.
 */
private fun GiveSupportCard(onOpen: () -> Unit) {
    Surface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        shape = RoundedCornerShape(30.dp),
        color = Pk.DeepDark,
        shadowElevation = 8.dp,
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 190.dp)) {
            Image(
                painterResource(R.drawable.prayerkey_give_artwork),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
            Box(Modifier.matchParentSize().background(Pk.DeepDark.copy(alpha = .45f)))
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                Text("GIVE", color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 2.sp)
                Text(
                    "Keep PrayerKey free.",
                    Modifier.padding(top = 8.dp), color = Color.White,
                    fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                    fontSize = 27.sp, lineHeight = 31.sp,
                )
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Help keep prayer available to everyone",
                        color = Color.White.copy(alpha = .82f),
                        fontFamily = UtilitySans, fontSize = 12.sp,
                    )
                    Icon(
                        Icons.Outlined.ArrowForward, null,
                        Modifier.padding(start = 7.dp).size(15.dp),
                        tint = Pk.GoldLight,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactPrayerPrompt(onSubmit: (String) -> Unit) {
    var request by rememberSaveable { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 15.dp),
        shape = RoundedCornerShape(99.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Pk.Oxblood.copy(alpha = .12f)),
        shadowElevation = 3.dp,
    ) {
        Row(
            Modifier.fillMaxWidth().height(52.dp).padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.SelfImprovement, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp))
            BasicTextField(
                value = request,
                onValueChange = { request = it },
                modifier = Modifier.weight(1f).padding(horizontal = 11.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Pk.Charcoal, fontFamily = UtilitySans, fontSize = 13.sp,
                ),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (request.isBlank()) Text(
                            "What do you want to pray about?",
                            color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.sp,
                        )
                        inner()
                    }
                },
            )
            Surface(
                modifier = Modifier.size(40.dp).clickable(enabled = request.isNotBlank()) {
                    onSubmit(request.trim())
                },
                shape = CircleShape,
                color = if (request.isBlank()) Pk.Oxblood.copy(alpha = .10f) else Pk.Oxblood,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.ArrowForward, "Pray about this",
                        tint = if (request.isBlank()) Pk.Oxblood.copy(alpha = .42f) else Color.White,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
    }
}

private data class PrayerKeyPillar(
    val title: String,
    val promise: String,
    val icon: ImageVector,
    val image: Int,
    val open: () -> Unit,
)

@Composable
private fun PrayerKeyIdentitySection(
    fast: () -> Unit,
    grow: () -> Unit,
    topicalBible: () -> Unit,
) {
    /*
     * Only what the dock cannot already reach.
     *
     * There were six, and five of them were a third or fourth door to
     * somewhere else on the same screen. Pray was reachable from the dock, the
     * circle rail, this tile, and the "what do you want to pray about" field -
     * four doors, one room, all visible at once. Read was the same. Church and
     * Remember were the dock again in different words.
     *
     * Fasting and the challenges have no tab of their own, so these two are
     * the only ones here that take you somewhere new. The tiles were never the
     * problem - they are the best artwork in the app - the duplication was.
     */
    val pillars = listOf(
        PrayerKeyPillar("FAST", "Seek God with intention.", Icons.Outlined.WaterDrop, R.drawable.discovery_cover_5, fast),
        PrayerKeyPillar("GROW", "Build your life of prayer.", Icons.Outlined.LocalFireDepartment, R.drawable.discovery_cover_4, grow),
    )
    Column(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            pillars.chunked(2).forEach { rowPillars ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowPillars.forEach { pillar ->
                        PrayerKeyPillarCard(pillar, Modifier.weight(1f))
                    }
                }
            }
        }
        TopicalBibleEntry(topicalBible)
    }
}

@Composable
private fun PrayerKeyPillarCard(pillar: PrayerKeyPillar, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 200.dp).clickable(onClick = pillar.open),
        shape = RoundedCornerShape(25.dp), color = Pk.DeepDark, shadowElevation = 4.dp,
    ) {
        Box {
            Image(painterResource(pillar.image), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Pk.DeepDark.copy(alpha = .60f)))
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = .92f)) {
                    Icon(
                        pillar.icon, null, tint = Pk.Oxblood,
                        modifier = Modifier.padding(8.dp).size(18.dp),
                    )
                }
                Column {
                    Text(
                        pillar.title, color = Color.White, fontFamily = DisplaySerif,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    )
                    Text(
                        pillar.promise, modifier = Modifier.padding(top = 4.dp),
                        color = Color.White.copy(alpha = .82f), fontFamily = UtilitySans,
                        fontSize = 12.sp, lineHeight = 17.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContentLibrary(
    openPrayer: () -> Unit,
    openEmergency: () -> Unit,
    openMorning: () -> Unit,
    openDevotional: () -> Unit,
    openVerse: () -> Unit,
    openBible: () -> Unit,
    openTopical: () -> Unit,
    openPrayerList: () -> Unit,
    openJournal: () -> Unit,
    openChurch: () -> Unit,
    openFeature: (Feature) -> Unit,
) {
    fun feature(title: String, hub: Hub = Hub.PRAY, icon: ImageVector = Icons.Outlined.AutoAwesome) =
        { openFeature(Feature(title, "", icon, hub)) }

    Spacer(Modifier.height(8.dp))
    HomeVisualShelf(
        "Continue",
        listOf(
            HomeShelfItem("Today's prayer", R.drawable.today_prayer_violet, openPrayer),
            HomeShelfItem("Read the Bible", R.drawable.bible_cover_red, openBible),
            HomeShelfItem("Prayer challenge", R.drawable.discovery_cover_4, feature("Prayer challenge", icon = Icons.Outlined.LocalFireDepartment)),
        ),
        wide = true,
    )
    HomeVisualShelf(
        "Pray now",
        listOf(
            HomeShelfItem("I need prayer now", R.drawable.prayerkey_hero_dawn, openEmergency),
            HomeShelfItem("Prayer points", R.drawable.discovery_cover_2, feature("Prayer points", icon = Icons.Outlined.FormatListBulleted)),
        ),
    )
    HomeVisualShelf(
        "Prayer times",
        listOf(
            HomeShelfItem("Morning prayer", R.drawable.rhythm_morning, openMorning),
            HomeShelfItem("Midnight prayer", R.drawable.rhythm_evening, feature("Midnight prayer", icon = Icons.Outlined.DarkMode)),
            HomeShelfItem("The three watches", R.drawable.discovery_cover_3, feature("The three watches", icon = Icons.Outlined.Schedule)),
        ),
    )
    HomeVisualShelf(
        "Challenges",
        listOf(
            HomeShelfItem("Prayer challenge", R.drawable.discovery_cover_4, feature("Prayer challenge", icon = Icons.Outlined.LocalFireDepartment)),
            HomeShelfItem("Bible challenge", R.drawable.discovery_cover_6, feature("Bible challenge", Hub.BIBLE, Icons.Outlined.MenuBook)),
            HomeShelfItem("Memory verse", R.drawable.verse_card_botanical, feature("Memory verse", Hub.BIBLE, Icons.Outlined.Bookmark)),
        ),
    )
    HomeVisualShelf(
        "Bible",
        listOf(
            HomeShelfItem("Read the Bible", R.drawable.bible_cover_red, openBible),
            HomeShelfItem("Audio Bible", R.drawable.discovery_cover_6, feature("Audio Bible", Hub.BIBLE, Icons.Outlined.Headphones)),
            HomeShelfItem("Bible verses by topic", R.drawable.verse_card_oxblood, openTopical),
            HomeShelfItem("Verse of the day", R.drawable.verse_card_botanical, openVerse),
        ),
    )
    HomeVisualShelf(
        "Fasting",
        listOf(
            HomeShelfItem("Start a fast", R.drawable.discovery_cover_5, feature("Start a fast", icon = Icons.Outlined.Timer)),
            HomeShelfItem("Daniel fast", R.drawable.discovery_cover_3, feature("Daniel fast", icon = Icons.Outlined.Spa)),
            HomeShelfItem("Esther fast", R.drawable.discovery_cover_4, feature("Esther fast", icon = Icons.Outlined.LocalFireDepartment)),
            HomeShelfItem("Timed fast", R.drawable.rhythm_evening, feature("Timed fast", icon = Icons.Outlined.Schedule)),
        ),
    )
    HomeVisualShelf(
        "Night prayer",
        listOf(
            HomeShelfItem("Midnight prayer", R.drawable.rhythm_evening, feature("Midnight prayer", icon = Icons.Outlined.DarkMode)),
            HomeShelfItem("Warfare prayers", R.drawable.discovery_cover_5, feature("Warfare prayers", icon = Icons.Outlined.Shield)),
        ),
    )
    HomeVisualShelf(
        "Be still",
        listOf(
            HomeShelfItem("Guided silence", R.drawable.rhythm_reflection, feature("Guided silence", icon = Icons.Outlined.SelfImprovement)),
            HomeShelfItem("Lectio Divina", R.drawable.onboarding_read_v3, feature("Lectio Divina", Hub.BIBLE, Icons.Outlined.AutoStories)),
            HomeShelfItem("The Examen", R.drawable.prayerkey_hero_dawn, feature("The Examen", icon = Icons.Outlined.FavoriteBorder)),
        ),
    )
    HomeVisualShelf(
        "Your prayer record",
        listOf(
            HomeShelfItem("Journal", R.drawable.onboarding_journal_v3, openJournal),
            HomeShelfItem("Answered prayers", R.drawable.discovery_cover_4, openJournal),
        ),
    )
    HomeVisualShelf(
        "Church",
        listOf(
            HomeShelfItem("Live sermon", R.drawable.onboarding_church_v3, openChurch),
            HomeShelfItem("Sermon notes", R.drawable.discovery_cover_6, openChurch),
        ),
    )
    HomeVisualShelf(
        "Every day",
        listOf(
            HomeShelfItem("Daily devotional", R.drawable.prayerkey_hero_dawn, openDevotional),
            HomeShelfItem("Morning prayer", R.drawable.rhythm_morning, openMorning),
            HomeShelfItem("Verse of the day", R.drawable.verse_card_dawn, openVerse),
        ),
    )
}

@Composable
private fun HomeVisualShelf(title: String, items: List<HomeShelfItem>, wide: Boolean = false) {
    Column(Modifier.fillMaxWidth().padding(top = 30.dp)) {
        Text(
            title,
            modifier = Modifier.padding(horizontal = 22.dp),
            fontFamily = UtilitySans,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Pk.Charcoal,
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            items.forEach { HomeVisualCard(it, wide) }
        }
    }
}

@Composable
private fun HomeVisualCard(item: HomeShelfItem, wide: Boolean) {
    val width = if (wide) 260.dp else 178.dp
    val height = if (wide) 164.dp else 220.dp
    Box(
        Modifier.width(width).height(height).clip(RoundedCornerShape(24.dp))
            .clickable(onClick = item.open),
    ) {
        Image(
            painterResource(item.artwork),
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = .02f), Color.Black.copy(alpha = .12f), Color.Black.copy(alpha = .84f))
                )
            )
        )
        Text(
            item.title,
            modifier = Modifier.align(Alignment.BottomStart).padding(18.dp),
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.Bold,
            fontSize = if (wide) 24.sp else 21.sp,
            lineHeight = if (wide) 28.sp else 25.sp,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DailyPrayerGoal(streak: Int, startPrayer: () -> Unit) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val sessions = remember { PrayerJourneyStore(context).records() }
    val prayedToday = remember(sessions, today) {
        sessions.filter { record ->
            java.time.Instant.ofEpochMilli(record.startedAt)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate() == today && record.completed
        }.sumOf { it.minutes }
    }
    val prefs = remember { context.getSharedPreferences("daily_prayer_goal", android.content.Context.MODE_PRIVATE) }
    val target = remember { prefs.getInt("minutes", 10).coerceIn(5, 120) }
    val shown = prayedToday.coerceAtMost(target)
    val remaining = (target - prayedToday).coerceAtLeast(0)
    val progress = (prayedToday.toFloat() / target).coerceIn(0f, 1f)

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Pk.Hair),
        shadowElevation = 7.dp,
    ) {
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(CircleShape).background(Pk.Oxblood.copy(alpha = .09f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Timer, null, tint = Pk.Oxblood, modifier = Modifier.size(22.dp))
                }
                Text(
                    if (remaining == 0) "Prayer complete" else "Pray for $target minutes",
                    Modifier.weight(1f).padding(start = 14.dp),
                    fontFamily = DisplaySerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    color = Pk.Charcoal,
                )
                if (streak > 0) {
                    Surface(shape = RoundedCornerShape(50), color = Pk.Oxblood.copy(alpha = .07f)) {
                        Row(Modifier.padding(horizontal = 11.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocalFireDepartment, null, tint = Pk.Oxblood, modifier = Modifier.size(17.dp))
                            Text("$streak", Modifier.padding(start = 4.dp), color = Pk.Oxblood, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(6.dp).clip(RoundedCornerShape(8.dp)),
                color = Pk.Oxblood,
                trackColor = Pk.Oxblood.copy(alpha = .10f),
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            Row(Modifier.fillMaxWidth().padding(top = 9.dp)) {
                Text("$shown min prayed", Modifier.weight(1f), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 11.sp)
                Text("$target min goal", color = Pk.Muted, fontFamily = UtilitySans, fontSize = 11.sp)
            }
            Button(
                onClick = startPrayer,
                modifier = Modifier.fillMaxWidth().padding(top = 17.dp).height(50.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood, contentColor = Color.White),
            ) {
                Text(
                    when {
                        remaining == 0 -> "PRAY AGAIN"
                        prayedToday > 0 -> "RESUME PRAYER"
                        else -> "START PRAYER"
                    },
                    fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = .7.sp,
                )
            }
        }
    }
}

@Composable
private fun ActiveChallengeCard(open: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("prayer_challenge_machine", android.content.Context.MODE_PRIVATE)
    val active = prefs.getBoolean("active", false)
    val duration = prefs.getInt("duration", 21).coerceAtLeast(1)
    val completed = prefs.getStringSet("done", emptySet()).orEmpty().size.coerceAtMost(duration)
    val focus = prefs.getString("focus", "").orEmpty().ifBlank { "Prayer challenge" }

    Section("ACTIVE CHALLENGE") {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = open),
            shape = RoundedCornerShape(27.dp),
            color = Pk.DeepDark,
            shadowElevation = 6.dp,
        ) {
            Column(Modifier.padding(21.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocalFireDepartment, null, tint = Pk.GoldLight, modifier = Modifier.size(27.dp))
                    Column(Modifier.weight(1f).padding(start = 13.dp)) {
                        Text(if (active) focus else "Start a prayer challenge", fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, color = Color.White, maxLines = 2)
                        if (active) Text("Day ${(completed + 1).coerceAtMost(duration)} of $duration", Modifier.padding(top = 4.dp), color = Color.White.copy(alpha = .68f), fontSize = 12.sp)
                    }
                    Icon(Icons.Outlined.ArrowForward, null, tint = Pk.GoldLight)
                }
                if (active) {
                    LinearProgressIndicator(
                        progress = { completed.toFloat() / duration },
                        modifier = Modifier.fillMaxWidth().padding(top = 17.dp).height(7.dp).clip(RoundedCornerShape(7.dp)),
                        color = Pk.GoldLight,
                        trackColor = Color.White.copy(alpha = .15f),
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueCard(reading: String, openBible: () -> Unit, openHistory: () -> Unit) {
    Section("CONTINUE") {
        Surface(shape = RoundedCornerShape(25.dp), color = Color.White, border = BorderStroke(1.dp, Pk.Hair)) {
            Column {
                ContinueRow(Icons.Outlined.MenuBook, "Bible", reading, openBible)
                HorizontalDivider(color = Pk.Hair)
                ContinueRow(Icons.Outlined.History, "Prayer history", "Your saved prayer sessions", openHistory)
            }
        }
    }
}

@Composable
private fun ContinueRow(icon: ImageVector, title: String, detail: String, open: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = open).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(43.dp).clip(RoundedCornerShape(14.dp)).background(Pk.Oxblood.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(21.dp))
        }
        Column(Modifier.weight(1f).padding(start = 13.dp)) {
            Text(title, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Pk.Charcoal)
            Text(detail, Modifier.padding(top = 3.dp), fontFamily = UtilitySans, fontSize = 11.sp, color = Pk.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted)
    }
}

@Composable private fun ClosingVerseCard(date: LocalDate, onPray: (String, String) -> Unit) {
    val context = LocalContext.current
    val words = listOf(
        "Psalm 121:8" to "The LORD shall preserve thy going out and thy coming in from this time forth, and even for evermore.",
        "Psalm 23:1" to "The LORD is my shepherd; I shall not want.",
        "Psalm 46:1" to "God is our refuge and strength, a very present help in trouble.",
        "Proverbs 3:5" to "Trust in the LORD with all thine heart; and lean not unto thine own understanding.",
        "1 Peter 5:7" to "Casting all your care upon him; for he careth for you.",
        "Psalm 119:105" to "Thy word is a lamp unto my feet, and a light unto my path.",
        "Psalm 56:3" to "What time I am afraid, I will trust in thee.",
    )
    val (reference, verse) = words[Math.floorMod(date.toEpochDay(), words.size.toLong()).toInt()]
    Surface(shape = RoundedCornerShape(26.dp), color = Pk.DeepDark, shadowElevation = 3.dp) {
        Box(Modifier.fillMaxWidth().heightIn(min = 330.dp)) {
            Image(painterResource(R.drawable.closing_verse_violet), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Pk.DeepDark.copy(alpha = .65f)))
            Column(Modifier.fillMaxWidth().padding(22.dp).padding(top = 70.dp)) {
                Text("“$verse”", fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 30.sp, color = Color.White)
                Text(reference.uppercase() + " · KJV", Modifier.padding(top = 12.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.3.sp, color = Pk.GoldLight)
                Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrayerImageAction(Icons.Outlined.Download, "SAVE") { CardShareRenderer.saveClosingVerse(context, verse, reference) }
                    PrayerImageAction(Icons.Outlined.Share, "SHARE") { CardShareRenderer.shareClosingVerse(context, verse, reference) }
                }
                Button(onClick = { onPray(verse, reference) }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) { Text("Pray this") }
            }
        }
    }
}

@Composable private fun Header(date: LocalDate, streak: Int, settings: () -> Unit, reminders: () -> Unit) {
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
            onClick = reminders,
            modifier = Modifier.align(Alignment.CenterEnd).size(36.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = .78f),
            border = BorderStroke(.6.dp, Color.White),
            shadowElevation = 6.dp,
        ) {
            BadgedBox(
                modifier = Modifier.padding(9.dp),
                badge = {
                },
            ) { Icon(Icons.Outlined.NotificationsNone, "Prayer reminders", tint = Pk.Charcoal, modifier = Modifier.size(18.dp)) }
        }
    }
}

private data class HomeShortcut(
    val label: String,
    val icon: ImageVector,
    val image: Int,
    val badge: String? = null,
    val open: () -> Unit,
)

@Composable private fun ShortcutRail(
    /** How many of today's ten pulls are prayed, shown on the badge. */
    todayBadge: String?,
    todayPulls: () -> Unit,
    /** The same, for the day's chapter. */
    readBadge: String?,
    biblePulls: () -> Unit,
    prayNow: () -> Unit,
    /** The named-challenge shelf. */
    challenges: () -> Unit,
) {
    val shortcuts = listOf(
        HomeShortcut("Today", Icons.Outlined.WbSunny, R.drawable.prayerkey_hero_dawn, todayBadge, todayPulls),
        HomeShortcut("Read", Icons.Outlined.AutoStories, R.drawable.onboarding_read_v3, readBadge, biblePulls),
        HomeShortcut("Pray now", Icons.Outlined.Bolt, R.drawable.onboarding_pray_v3, open = prayNow),
        HomeShortcut("Challenges", Icons.Outlined.LocalFireDepartment, R.drawable.discovery_cover_3, open = challenges),
    )
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp).padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        shortcuts.forEach { item ->
            CircularImageShortcut(item, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CircularImageShortcut(item: HomeShortcut, modifier: Modifier = Modifier) {
    Column(
        modifier.clickable(onClick = item.open),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier.padding(horizontal = 4.dp).widthIn(max = 80.dp).fillMaxWidth().aspectRatio(1f)
                    .shadow(5.dp, CircleShape, spotColor = Pk.Oxblood.copy(alpha = .22f))
                    .background(Pk.Oxblood, CircleShape)
                    .padding(3.dp)
                    .background(Pk.Cream, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape),
            ) {
                Image(
                    painterResource(item.image), item.label,
                    Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                )
                Box(Modifier.fillMaxSize().background(Pk.Oxblood.copy(alpha = .10f)))
                /* Each circle carried a small white glyph badge - a sun, a
                   book, a bolt, a flame - clipped over the bottom-left of the
                   artwork, directly above a label that already read Today,
                   Read, Pray now, Challenges. Four little stickers on four
                   paintings, saying what was written underneath them. */
            }
            item.badge?.let { badge ->
                Surface(
                    Modifier.align(Alignment.TopEnd),
                    shape = RoundedCornerShape(99.dp),
                    color = Pk.Oxblood,
                    border = BorderStroke(1.5.dp, Pk.Cream),
                ) {
                    Text(
                        badge, Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                        color = Color.White, fontFamily = UtilitySans,
                        fontSize = 7.sp, fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Text(
            item.label, Modifier.padding(top = 7.dp),
            fontFamily = UtilitySans, fontSize = 10.sp,
            fontWeight = FontWeight.Medium, color = Pk.Charcoal,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable private fun HeroRail(title: String, open: () -> Unit) {
    /*
     * Inset and rounded, like the rest of the page.
     *
     * Everything else on Home floats on the cream - the rail circles, the ask
     * field, the Today card, the dock. The hero alone ran edge to edge with
     * square corners, so the ground simply stopped at it and started again
     * underneath. It read as a seam rather than a card.
     *
     * The photograph has not changed and has not been cropped smaller; it is
     * the same image in a frame that agrees with its neighbours.
     */
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(455.dp)
            .clip(RoundedCornerShape(34.dp)).clickable(onClick = open)
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
        /* A white medallion used to float dead centre, over the reader's face,
           carrying a book icon - on a card already labelled TODAY'S DEVOTIONAL
           and signed "Read · Reflect · Pray". It marked the artwork like a
           watermark to say a third time what the words say. */
        Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 22.dp, vertical = 24.dp)) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = .16f), border = BorderStroke(.5.dp, Color.White.copy(alpha = .24f))) {
                Text("TODAY'S DEVOTIONAL", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp, color = Color.White)
            }
            Text(title.ifBlank { "Preparing today's devotional…" }, Modifier.padding(top = 12.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 35.sp, color = Color.White)
            Row(Modifier.fillMaxWidth().padding(top = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Read · Reflect · Pray", fontFamily = UtilitySans, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Pk.GoldLight)
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

@Composable private fun TopicalBibleEntry(open: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 13.dp)) {
        Text("BIBLE VERSES BY TOPIC", style = labelStyle(), color = Pk.Charcoal, modifier = Modifier.padding(bottom = 11.dp))
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
                Text("HOLY BIBLE", color = Pk.GoldLight, fontFamily = UtilitySans,
                    fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                Text("Find a verse\nfor today", Modifier.padding(top = 8.dp),
                    color = Color.White, fontFamily = DisplaySerif, fontSize = 25.sp,
                    lineHeight = 29.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Surface(onClick = open, shape = RoundedCornerShape(14.dp), color = Color.White) {
                    Row(Modifier.padding(horizontal = 15.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("OPEN", color = Pk.Oxblood, fontSize = 10.sp,
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
        Metric("PRAYER WALL", 0, Icons.Outlined.Groups, pray)
    }
}

@Composable private fun PrayerLifePanel(saved: Int, journal: Int, streak: Int, pray: () -> Unit, write: () -> Unit) {
    Surface(shape = RoundedCornerShape(28.dp), color = Pk.DeepDark) {
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Prayer history", fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Color.White)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Pk.Hair),
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(CircleShape).background(Pk.Oxblood.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Timer, null, tint = Pk.Oxblood)
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("Continue praying", fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Pk.Charcoal)
                }
                IconButton(history) { Icon(Icons.Outlined.History, "Prayer history", tint = Pk.Muted) }
            }
            Button(start, Modifier.fillMaxWidth().padding(top = 16.dp).height(50.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) {
                Text("START PRAYER", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable private fun TodayRhythmPanelLegacy(
    saved: Int, journal: Int, streak: Int,
    morningPrayer: () -> Unit, devotional: () -> Unit, bible: () -> Unit,
    reflection: () -> Unit, eveningPrayer: () -> Unit, history: () -> Unit,
) {
    val context = LocalContext.current
    val dateKey = LocalDate.now().toString()
    val prefs = remember { context.getSharedPreferences("today_rhythm", android.content.Context.MODE_PRIVATE) }
    var completed by remember(dateKey) { mutableStateOf(prefs.getStringSet(dateKey, emptySet()).orEmpty()) }
    var openMoment by remember { mutableStateOf<String?>(null) }
    /* Morning and Evening open the daily office rather than a card of their
       own. Two things called Morning prayer on one screen, one of which was
       the office and one of which was a generated card, is exactly the kind of
       duplication that makes an app feel like a menu. */
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
                /* Morning and Evening now have their own screens too - the daily
                   office. They used to open a generated card here, which put a
                   second "Morning prayer" on Home beside the real one. */
                val hasOwnScreen = identity.first in setOf("devotional", "bible", "morning", "evening")

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
}

/**
 * Today, as a list rather than a carousel.
 *
 * What this replaces was a horizontal rail of five full-bleed image tiles. It
 * showed one and a half of them, which put Evening prayer off the right-hand
 * edge — the one you actually want after dark — and spent five photographs
 * saying five short words.
 *
 * A list answers the only question the panel exists to answer: what is left
 * today. All of it is visible at once.
 *
 * The risk in a checklist is the one I named about somebody else's app: three
 * boxes that know nothing about each other. The defence is not the shape, it
 * is the content — every row here comes from the same day, so the devotional
 * names its own title and the reading names its own chapter. Without those
 * second lines this is five generic nouns that could belong to any app.
 *
 * Rows outside their hour are dimmed rather than hidden. Evening prayer at
 * seven in the morning is not something to do now, but it is something to
 * know is coming.
 *
 * The circle is not a control. It fills because a screen was finished, never
 * because a ring was tapped — prayer that becomes box-ticking has lost, and a
 * tappable checkbox is an invitation to tick it.
 */
@Composable private fun TodayRhythmPanel(
    devotionalTitle: String,
    readingLabel: String,
    morningPrayer: () -> Unit,
    devotional: () -> Unit,
    bible: () -> Unit,
    eveningPrayer: () -> Unit,
    calendar: () -> Unit,
) {
    val context = LocalContext.current
    var clockNow by remember { mutableStateOf(java.time.LocalDateTime.now()) }
    val today = clockNow.toLocalDate()
    val dateKey = today.toString()
    val prefs = remember { context.getSharedPreferences("home_completed", android.content.Context.MODE_PRIVATE) }
    var completed by remember { mutableStateOf(emptySet<String>()) }
    val hour = clockNow.hour
    LaunchedEffect(Unit) {
        while (true) {
            clockNow = java.time.LocalDateTime.now()
            val date = clockNow.toLocalDate()
            completed = buildSet {
                listOf("morning", "evening", "devotional").forEach { if (prefs.getBoolean("${date}:$it", false)) add(it) }
                if (com.prayerkey.manna.data.PullsProgress.Bible.isComplete(context, date)) add("bible")
            }
            kotlinx.coroutines.delay(1_000)
        }
    }

    val rows = listOf(
        RhythmRow("morning", "Morning prayer", "", Icons.Outlined.WbSunny, 4..16, open = morningPrayer),
        RhythmRow("devotional", "Daily devotional", devotionalTitle, Icons.Outlined.AutoStories, 0..23, reservesDetail = true, open = devotional),
        RhythmRow("bible", "Bible reading", readingLabel, Icons.Outlined.MenuBook, 0..23, reservesDetail = true, open = bible),
        RhythmRow("evening", "Evening prayer", "", Icons.Outlined.DarkMode, 17..23, open = eveningPrayer),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        /* Thicker. A larger radius and more air inside it is most of what
           reads as iOS - not the blur, which people notice second. 28 with
           13dp rows was a tidy card; 34 with 17 is a considered one. */
        shape = RoundedCornerShape(34.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Pk.Hair),
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(vertical = 24.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Today, " + today.format(DateTimeFormatter.ofPattern("MMM d", java.util.Locale.UK)),
                    Modifier.weight(1f),
                    fontFamily = DisplaySerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    color = Pk.Charcoal,
                )
                Box(
                    Modifier.size(44.dp).clip(CircleShape)
                        .background(Pk.Oxblood.copy(alpha = .08f))
                        .clickable(onClick = calendar),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.CalendarToday, "Prayer calendar", tint = Pk.Oxblood, modifier = Modifier.size(19.dp)) }
            }
            Box(
                Modifier.padding(horizontal = 22.dp).padding(top = 16.dp, bottom = 4.dp)
                    .fillMaxWidth().height(2.dp).clip(RoundedCornerShape(99.dp))
                    .background(Pk.Charcoal.copy(alpha = .07f)),
            )
            rows.forEach { row ->
                RhythmEntry(
                    row = row,
                    done = row.id in completed,
                    now = hour in row.hours,
                    open = row.open,
                )
            }
        }
    }
}

private data class RhythmRow(
    val id: String,
    val title: String,
    /** Today's own - the devotional's title, the chapter. Blank on the offices. */
    val detail: String,
    val icon: ImageVector,
    val hours: IntRange,
    /**
     * Whether this row will have a detail once the day has loaded.
     *
     * The devotional and the reading are read from an asset, so for the first
     * second they are blank. Rendering nothing and then appearing makes the
     * card jump under the reader's thumb; rendering a placeholder puts words
     * on screen that are not true. The line is reserved instead - empty, the
     * right height, and filled in place when the day arrives.
     */
    val reservesDetail: Boolean = false,
    val open: () -> Unit,
)

@Composable private fun RhythmEntry(row: RhythmRow, done: Boolean, now: Boolean, open: () -> Unit) {
    /* Dimmed outside its hour, and never below the legibility floor - this is
       a row you are meant to read and decide against, not a disabled control. */
    val alpha = if (now || done) 1f else .45f
    /* Not tappable until it has something to open. Tapping "Bible reading"
       before the plan resolves used to open an empty chapter. */
    val ready = !row.reservesDetail || row.detail.isNotBlank()
    Row(
        Modifier.fillMaxWidth().clickable(enabled = ready, onClick = open)
            .padding(horizontal = 22.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            /* 16 on 50 is the proportion iOS uses for a rounded square - about
               a third of the side. At 15 on 46 it was very slightly too tight
               and read as a rounded rectangle rather than a squircle. */
            Modifier.size(50.dp).clip(RoundedCornerShape(16.dp))
                .background(Pk.Oxblood.copy(alpha = .10f * alpha)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(row.icon, null, tint = Pk.Oxblood.copy(alpha = alpha), modifier = Modifier.size(23.dp))
        }
        Column(Modifier.weight(1f).padding(start = 15.dp)) {
            Text(
                row.title,
                fontFamily = UtilitySans,
                fontSize = 16.sp,
                fontWeight = if (now && !done) FontWeight.SemiBold else FontWeight.Normal,
                color = Pk.Charcoal.copy(alpha = alpha),
            )
            if (row.detail.isNotBlank() || row.reservesDetail) {
                Text(
                    row.detail,
                    Modifier.padding(top = 2.dp),
                    fontFamily = UtilitySans,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = Pk.Muted.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
        /* Not a control. It fills because a screen was finished. */
        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
            if (done) {
                Box(
                    Modifier.size(26.dp).clip(CircleShape).background(Pk.Oxblood),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(15.dp)) }
            } else {
                Box(
                    Modifier.size(26.dp).clip(CircleShape)
                        .border(1.5.dp, Pk.Charcoal.copy(alpha = .18f * alpha), CircleShape),
                )
            }
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

@Composable
private fun ExplorePrayerKeyEntry(open: () -> Unit) {
    Surface(
        onClick = open,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Pk.Hair),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 19.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(Pk.Oxblood.copy(alpha = .09f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.GridView, null, tint = Pk.Oxblood, modifier = Modifier.size(23.dp))
            }
            Text(
                "Explore PrayerKey",
                modifier = Modifier.weight(1f).padding(start = 15.dp),
                fontFamily = DisplaySerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 21.sp,
                color = Pk.Charcoal,
            )
            Icon(Icons.Outlined.ChevronRight, "Open", tint = Pk.Oxblood)
        }
    }
}

@Composable
private fun ExplorePrayerKeyScreen(
    categories: List<ProgrammeCategory>,
    momentItems: List<Feature>,
    go: (Feature) -> Unit,
    startPrayer: () -> Unit,
    prayerHistory: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Charcoal, modifier = Modifier.size(18.dp))
                }
                Text(
                    "Explore PrayerKey",
                    modifier = Modifier.weight(1f),
                    fontFamily = DisplaySerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = Pk.Charcoal,
                )
                Spacer(Modifier.width(48.dp))
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 28.dp)) {
                Column(Modifier.padding(horizontal = 22.dp, vertical = 8.dp)) {
                    PrayerSessionEntry(start = startPrayer, history = prayerHistory)
                }
                ProgrammeLibrary(categories, go)
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

/**
 * Nothing, now.
 *
 * This held one entry: First fruits, on the first of the month. So the band it
 * fed was empty on thirty days out of thirty-one and then appeared unannounced
 * on the thirty-first - a section the reader had never seen, between two they
 * had. Kept as an empty list rather than deleted, because the plumbing is
 * harmless and the next conditional feature has somewhere to go.
 */
private fun conditionalFeatures(today: LocalDate): List<Feature> = emptyList()
