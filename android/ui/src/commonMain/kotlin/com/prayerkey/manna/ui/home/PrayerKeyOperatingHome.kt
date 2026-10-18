package com.prayerkey.manna.ui.home

import com.prayerkey.manna.platform.toLocalDate
import com.prayerkey.manna.ui.platform.fullScreenDialog
import com.prayerkey.manna.platform.atStartOfDay
import com.prayerkey.manna.platform.plusDays
import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.platform.format
import com.prayerkey.manna.ui.share.Share
import com.prayerkey.manna.ui.platform.Reminders
import com.prayerkey.manna.platform.now
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.ui.platform.platformContext
import org.jetbrains.compose.resources.DrawableResource
import com.prayerkey.manna.resources.*
import com.prayerkey.manna.ui.theme.pressable
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import com.prayerkey.manna.model.todaysPrayer
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
import org.jetbrains.compose.resources.painterResource
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
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.ui.theme.DisplaySerif
import androidx.compose.ui.text.font.FontStyle
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.litSurface
import com.prayerkey.manna.ui.theme.litTile
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.coroutines.launch

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
    onOpenPrayerRequest: (String) -> Unit,
    onOpenJournal: () -> Unit,
    onOpenChurch: () -> Unit,
    /** Devotional -> prayer list. The day's title and what it invites you to bring. */
    onKeepPrayer: (String, String) -> Unit,
    onKeepGuidedPrayer: (String, String, String, String?) -> Unit = { _, _, _, _ -> },
    /** Devotional -> journal. The day's question, and the answer written to it. */
    onKeepReflection: (String, String) -> Unit,
    onSettings: () -> Unit,
    initialAction: String? = null,
    onInitialActionConsumed: () -> Unit = {},
) {
    /* Keep an already-open Home screen honest across midnight. Without this,
       date-keyed content changed on the next app launch but could show
       yesterday's prayer and chapter while the app remained in memory. */
    var today by remember { mutableStateOf(today()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(com.prayerkey.manna.platform.millisUntilMidnight())
            today = today()
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
    /* Which hour's prayer card is open: "morning", "afternoon", "evening",
       "night", "midnight" - or null. */
    var hourCard by rememberSaveable { mutableStateOf<String?>(null) }
    var challengeShelfOpen by rememberSaveable { mutableStateOf(false) }
    var giveOpen by rememberSaveable { mutableStateOf(false) }
    var openChallengeId by rememberSaveable { mutableStateOf<String?>(null) }
    var prayerCalendarOpen by rememberSaveable { mutableStateOf(false) }
    var exploreOpen by rememberSaveable { mutableStateOf(false) }
    var prayerPointsOpen by rememberSaveable { mutableStateOf(false) }
    var namedChallengesOpen by rememberSaveable { mutableStateOf(false) }
    var selectedFeature by remember { mutableStateOf<Feature?>(null) }

    LaunchedEffect(initialAction) {
        when (initialAction) {
            "devotional" -> devotionalOpen = true
            "prayer_session" -> todayPullsOpen = true
            "bible_session" -> biblePullsOpen = true
            "midnight" -> hourCard = "midnight"
            "warfare" -> selectedFeature = Feature("Warfare prayer", "", Icons.Outlined.Shield, Hub.PRAY)
        }
        if (initialAction != null) onInitialActionConsumed()
    }

    /* The written year walks from the day you started, not from 1 January, so
       somebody installing in November gets day 1 rather than day 318. */
    val context = platformContext()
    val startedOn = remember {
        val prefs = context.keyValues("daily_journey")
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
        prayerTopics = kotlinx.coroutines.withContext(com.prayerkey.manna.platform.ioDispatcher) {
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
    LaunchedEffect(today, todayPullsOpen, biblePullsOpen) {
        todayBadgeText = com.prayerkey.manna.data.PullsProgress.Prayer.badge(context, today)
    }
    val morningPrayer = remember(prayerTopics, today) { com.prayerkey.manna.data.dailyPrayerFor(today, prayerTopics) }
    var todayVerse by remember(plan) { mutableStateOf<VerseCard?>(null) }
    // the chapter's own length, the same measure the chapter card uses
    var chapterMinutes by remember(plan) { mutableStateOf(4) }
    LaunchedEffect(plan) {
        val reading = plan?.reading ?: return@LaunchedEffect
        val chapter = kotlinx.coroutines.withContext(com.prayerkey.manna.platform.ioDispatcher) {
            com.prayerkey.manna.data.OfflineBible(context).chapter(reading.book, reading.chapter)
        }
        if (chapter.isNotEmpty()) {
            chapterMinutes = ((chapter.sumOf { it.text.split(' ').size } + 199) / 200).coerceAtLeast(1)
        }
        val text = chapter.firstOrNull { it.verse == reading.verse }?.text
        if (text != null) {
            todayVerse = VerseCard(reading.ref.label, "KJV", text, "")
            // the home-screen widget shows the same verse, without opening the Bible
            com.prayerkey.manna.data.Today.save(
                context,
                com.prayerkey.manna.data.TodayCard(today.toString(), reading.ref.label, text, written?.title.orEmpty()),
            )
        }
    }

    if (devotionalOpen) {
        Dialog(
            onDismissRequest = { devotionalOpen = false },
            properties = fullScreenDialog(),
        ) {
            val day = written
            if (day != null) {
                DevotionalCards(
                    day = day,
                    onClose = { devotionalOpen = false },
                    onComplete = {
                        context.keyValues("home_completed")
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
            properties = fullScreenDialog(),
        ) { TopicalBibleScreen { topicalBibleOpen = false } }
    }
    if (emergencyPrayerOpen) {
        Dialog(
            onDismissRequest = { emergencyPrayerOpen = false },
            properties = fullScreenDialog(),
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
            properties = fullScreenDialog(),
        ) {
            /* One card, four offices. The hour decides which - morning, evening,
               night, or the midnight watch. Four entry points on Home for what
               is one habit at four points of a day would be the clutter; being
               asked to choose between them at eleven at night would be worse. */
            val now = com.prayerkey.manna.platform.now()
            val close = { morningPrayerOpen = false }
            when (requestedOffice?.let { com.prayerkey.manna.data.OfficeStore.Office.valueOf(it) } ?: com.prayerkey.manna.data.OfficeStore.officeFor(now.hour, now.minute)) {
                com.prayerkey.manna.data.OfficeStore.Office.MORNING ->
                    com.prayerkey.manna.ui.morning.MorningPrayerScreen(onClose = close)
                com.prayerkey.manna.data.OfficeStore.Office.AFTERNOON ->
                    com.prayerkey.manna.ui.morning.AfternoonPrayerScreen(onClose = close)
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
            properties = fullScreenDialog(),
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
            properties = fullScreenDialog(),
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
        /* One chapter a day, as two cards: the chapter the day's devotional is
           built on, with its key verse on the front and the whole of it on the
           back. Done counts the day's reading kept. */
        Dialog(
            onDismissRequest = { biblePullsOpen = false },
            properties = fullScreenDialog(),
        ) {
            val where = plan?.reading
            if (where != null) {
                ChapterCards(
                    reading = where,
                    onClose = { biblePullsOpen = false },
                    onDone = {
                        com.prayerkey.manna.data.PullsProgress.Bible.record(context, 1, 1)
                        biblePullsOpen = false
                    },
                )
            }
        }
    }
    hourCard?.let { hour ->
        /* Each hour's prayer as a card: a short prayer written for that hour,
           the kind people keep and send, rather than an office to work
           through. Amen ticks the hour on Home. */
        Dialog(
            onDismissRequest = { hourCard = null },
            properties = fullScreenDialog(),
        ) {
            val (label, mark) = when (hour) {
                "afternoon" -> "Afternoon prayer" to "\u263C"
                "evening" -> "Evening prayer" to "\u263E"
                "night" -> "Night prayer" to "\u2605"
                "midnight" -> "Midnight prayer" to "\u2727"
                else -> "Morning prayer" to "\u2600"
            }
            MorningPrayerCards(
                prayer = remember(hour, today) { com.prayerkey.manna.data.hourPrayer(hour, today) },
                label = label,
                mark = mark,
                onClose = { hourCard = null },
                onDone = {
                    context.keyValues("home_completed")
                        .edit().putBoolean("${today}:$hour", true).apply()
                    hourCard = null
                },
            )
        }
    }
    if (giveOpen) {
        Dialog(
            onDismissRequest = { giveOpen = false },
            properties = fullScreenDialog(),
        ) {
            com.prayerkey.manna.ui.give.GiveScreen(onClose = { giveOpen = false })
        }
    }
    openChallengeId?.let { id ->
        var challenge by remember(id) { mutableStateOf<com.prayerkey.manna.data.ChallengePlan?>(null) }
        LaunchedEffect(id) {
            challenge = kotlinx.coroutines.withContext(com.prayerkey.manna.platform.ioDispatcher) {
                com.prayerkey.manna.data.Challenges.byId(context, id)
            }
        }
        Dialog(
            onDismissRequest = { openChallengeId = null },
            properties = fullScreenDialog(),
        ) {
            challenge?.let { plan ->
                com.prayerkey.manna.ui.challenge.ChallengeDayScreen(
                    challenge = plan,
                    onClose = { openChallengeId = null },
                )
            }
        }
    }
    if (prayerPointsOpen) {
        Dialog(onDismissRequest = { prayerPointsOpen = false }, properties = fullScreenDialog()) {
            com.prayerkey.manna.ui.screens.PrayerPointsScreen(onClose = { prayerPointsOpen = false })
        }
    }
    if (namedChallengesOpen) {
        Dialog(onDismissRequest = { namedChallengesOpen = false }, properties = fullScreenDialog()) {
            com.prayerkey.manna.ui.challenge.ChallengeShelfRoute(onClose = { namedChallengesOpen = false })
        }
    }
    if (prayerCalendarOpen) {
        Dialog(
            onDismissRequest = { prayerCalendarOpen = false },
            properties = fullScreenDialog(),
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
        Dialog(onDismissRequest = { selectedFeature = null }, properties = fullScreenDialog()) {
            FeatureDestination(feature, onKeep = onKeepPrayer, onClose = { selectedFeature = null }) { }
        }
    }
    val programmes = remember { programmeCategories() }
    /* The bell opens reminders. It used to open a dialog whose entire content
       was a sentence saying reminders are in Settings, and whose confirm button
       went to Settings - a signpost in front of a door. */
    val conditional = remember(today) { conditionalFeatures(today) }

    if (exploreOpen) {
        Dialog(
            onDismissRequest = { exploreOpen = false },
            properties = fullScreenDialog(),
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
    /* Derived, so Home recomposes only while this actually changes - over
       the first 140px - not on every frame of every scroll. Read raw, the
       whole screen rebuilt sixty times a second as you scrolled. */
    val underneath by remember { derivedStateOf { (scroll.value / 140f).coerceIn(0f, 1f) } }

    var challengeOpen by rememberSaveable { mutableStateOf(false) }
    val challengeContext = platformContext()
    /* Re-read on every open, so the card shows the day the flow left behind. */
    val challenge = remember(challengeOpen) {
        com.prayerkey.manna.data.PrayerChallengeStore.load(challengeContext)
    }
    var fastOpen by rememberSaveable { mutableStateOf(false) }
    var meditationOpen by rememberSaveable { mutableStateOf(false) }
    /* A tapped reminder for the challenge or the fast opens the flow it came
       from - where the day or the countdown already is - not the older
       screens those notifications used to land on. */
    LaunchedEffect(initialAction) {
        when (initialAction) {
            "fasting" -> fastOpen = true
            "prayer_challenge" -> challengeOpen = true
        }
    }
    var biblePlanOpen by rememberSaveable { mutableStateOf(false) }
    // the black Bible: a whole book to sit and read, not a card of the day
    var monoBibleOpen by rememberSaveable { mutableStateOf(false) }
    // what was brought to the guided prayer, while it is open
    var guidedRequest by rememberSaveable { mutableStateOf<String?>(null) }
    // a remembered prayer the machine has come back to ask about
    var answeredId by rememberSaveable { mutableStateOf<Long?>(null) }
    LaunchedEffect(initialAction) {
        initialAction?.takeIf { it.startsWith("answered:") }?.removePrefix("answered:")?.toLongOrNull()?.let { answeredId = it }
    }
    // bumped on close, so the card reads the day the plan was left on
    var biblePlanSeen by remember { mutableIntStateOf(0) }
    val fast = remember(fastOpen) {
        com.prayerkey.manna.data.FastingStore.load(challengeContext)
    }

    Box(Modifier.fillMaxSize().background(Pk.Cream)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(scroll).padding(bottom = Pk.NavClearance)
        ) {
        // room for the walk under the date, only once there is a walk to show
        val walkShown = com.prayerkey.manna.data.Walk.state.collectAsState().value.current(today.toEpochDays().toLong()) > 0
        Spacer(Modifier.height(if (walkShown) 98.dp else 74.dp))
        ShortcutRail(
            todayBadge = todayBadgeText,
            todayPulls = { todayPullsOpen = true },
            prayNow = onOpenPrayer,
            challenges = { challengeShelfOpen = true },
        )
        /* What you want to pray about opens the guided prayer: the prayer
           machine finds the written prayer for it and walks you through it -
           still, the Word, worship, confession, asking, listening, Amen. */
        CompactPrayerPrompt { request -> guidedRequest = request }
        Box(Modifier.pressable(.985f)) { HeroRail(written?.title.orEmpty(), { scroll.value }) { if (written != null) devotionalOpen = true } }

        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 15.dp)) {
            TodayRhythmPanel(
                devotionalTitle = written?.title.orEmpty(),
                devotionalMinutes = written?.minutes ?: 0,
                readingLabel = plan?.readingLabel.orEmpty(),
                readingMinutes = chapterMinutes,
                morningPrayer = { hourCard = "morning" },
                afternoonPrayer = { hourCard = "afternoon" },
                nightPrayer = { hourCard = "night" },
                midnightPrayer = { hourCard = "midnight" },
                devotional = { devotionalOpen = true },
                bible = { biblePullsOpen = true },
                eveningPrayer = { hourCard = "evening" },
                calendar = { prayerCalendarOpen = true },
            )
        }

        /* Prayer + Challenge: a number of days, a thing to ask God for, and
           a time to show up. The card says which day you are on once it has
           begun, because at that point the question is not whether to start. */
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp)) {
            Box(Modifier.pressable()) { com.prayerkey.manna.ui.challenge.StartChallengeCard(
                started = challenge.started,
                day = challenge.dayFor(),
                of = challenge.days,
            ) { challengeOpen = true } }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.pressable()) { com.prayerkey.manna.ui.challenge.StartFastCard(
                running = fast.running,
                title = fast.title,
                day = fast.dayOf(kotlinx.datetime.Clock.System.now().toEpochMilliseconds() / 1000),
                of = fast.days,
            ) { fastOpen = true } }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.pressable()) { com.prayerkey.manna.ui.meditation.MeditationCard { meditationOpen = true } }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.pressable()) { com.prayerkey.manna.ui.bibleplan.BiblePlanCard(refresh = biblePlanSeen) { biblePlanOpen = true } }
            // the same 14dp to the prayer card as between every card above
            Spacer(Modifier.height(14.dp))
        }
        /* The prayers written for particular hours and moments. They were
           finished and could not be reached; now they are one swipe away. */
        MomentsShelf(
            remember {
                prayerMoments(
                    emergency = { emergencyPrayerOpen = true },
                    office = { requestedOffice = it; morningPrayerOpen = true },
                    tenMinutes = { prayerSessionOpen = true },
                    prayerPoints = { prayerPointsOpen = true },
                )
            },
        )
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(14.dp))
        }

        /* The morning prayer and the verse of the day, as cards.
           Both were written to be shared - SAVE and SHARE render a portrait
           image with the artwork, the words and the mark on it - and both
           stopped being drawn when Home was rebuilt, leaving the renderer
           with nothing to call. A card nobody can reach shares nothing. */
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(Modifier.pressable()) { PrayerNowCard(todaysPrayer(), open = onOpenPrayer) }
            Box(Modifier.pressable()) { VerseCard(todayVerse, open = { biblePullsOpen = true }) }
        }
        Spacer(Modifier.height(18.dp))

        /* The FAST and GROW tiles are gone: the fast and the prayer challenge
           have their own cards above now, and these opened the older versions
           of both - two doors to one room, and the far door led somewhere
           else. */
        Column(Modifier.padding(vertical = 18.dp).pressable()) { TopicalBibleEntry { topicalBibleOpen = true } }
        /* The page closes on the black Bible - the whole book, where the
           verse card used to hand over one line of it. */
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp)) {
            com.prayerkey.manna.ui.book.MonochromeBibleCard(
                onOpen = { monoBibleOpen = true },
                modifier = Modifier.fillMaxWidth().aspectRatio(.78f).pressable(.98f),
            )
        }
        }

        if (challengeOpen) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { challengeOpen = false },
                properties = fullScreenDialog(),
            ) {
                com.prayerkey.manna.ui.challenge.PrayerChallengeRoute { challengeOpen = false }
            }
        }

        if (fastOpen) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { fastOpen = false },
                properties = fullScreenDialog(),
            ) {
                com.prayerkey.manna.ui.challenge.FastingChallengeRoute { fastOpen = false }
            }
        }

        if (challengeShelfOpen) {
            Dialog(
                onDismissRequest = { challengeShelfOpen = false },
                properties = fullScreenDialog(),
            ) {
                com.prayerkey.manna.ui.challenge.ChallengeChooser(
                    prayer = challenge,
                    fast = fast,
                    // the chosen flow opens over Home, and the chooser steps aside
                    onPrayer = { challengeShelfOpen = false; challengeOpen = true },
                    onFast = { challengeShelfOpen = false; fastOpen = true },
                    onShort = { challengeShelfOpen = false; namedChallengesOpen = true },
                    onClose = { challengeShelfOpen = false },
                )
            }
        }

        guidedRequest?.let { request ->
            val guided = remember(request, prayerTopics) { com.prayerkey.manna.data.PrayerMachine.guide(request, prayerTopics) }
            if (guided != null) androidx.compose.ui.window.Dialog(
                onDismissRequest = { guidedRequest = null },
                properties = fullScreenDialog(),
            ) {
                com.prayerkey.manna.ui.guided.GuidedPrayerFlow(
                    guided = guided,
                    onKeep = {
                        onKeepGuidedPrayer(
                            guided.request, guided.topic.title, guided.topic.prayer,
                            guided.topic.scripture.firstOrNull()?.first,
                        )
                        // remembered, and asked about in a week
                        val kept = com.prayerkey.manna.data.PrayerMemory.remember(challengeContext, guided.request, guided.topic.title)
                        Reminders.schedulePrayerMemory(challengeContext, kept)
                    },
                    onClose = { guidedRequest = null },
                )
            }
        }

        answeredId?.let { id ->
            val kept = remember(id) { com.prayerkey.manna.data.PrayerMemory.find(challengeContext, id) }
            if (kept == null) {
                LaunchedEffect(id) { answeredId = null }
            } else androidx.compose.ui.window.Dialog(
                onDismissRequest = { answeredId = null },
                properties = fullScreenDialog(),
            ) {
                com.prayerkey.manna.ui.guided.AnsweredFlow(
                    kept = kept,
                    onAnswered = {
                        com.prayerkey.manna.data.PrayerMemory.markAnswered(challengeContext, id)
                        Reminders.cancelPrayerMemory(challengeContext, id)
                        // the answer becomes a testimony in the journal
                        onKeepReflection(
                            "Answered prayer",
                            "${kept.request.replaceFirstChar { it.uppercase() }}\n\nPrayed on ${kept.prayedLabel()}. Answered on ${today().format("EEEE d MMMM")}.",
                        )
                    },
                    onStillPraying = {
                        answeredId = null
                        // back into the prayer; keeping it again moves the question a week on
                        guidedRequest = kept.request
                    },
                    onClose = { answeredId = null },
                )
            }
        }

        if (monoBibleOpen) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { monoBibleOpen = false },
                properties = fullScreenDialog(),
            ) {
                val bookPrefs = remember { context.keyValues("mono_bible") }
                var monoSize by remember { mutableIntStateOf(bookPrefs.getInt("size", 18)) }
                var monoRibbon by remember { mutableStateOf(bookPrefs.getString("ribbon", null)) }
                com.prayerkey.manna.ui.book.BibleBookScreen(
                    bible = remember { com.prayerkey.manna.data.OfflineBible(context) },
                    textSize = monoSize,
                    onTextSize = { monoSize = it; bookPrefs.edit().putInt("size", it).apply() },
                    ribbon = monoRibbon,
                    onRibbon = { monoRibbon = it; bookPrefs.edit().putString("ribbon", it).apply() },
                    onReadPlain = null,
                    onClose = { monoBibleOpen = false },
                    edition = com.prayerkey.manna.ui.book.BookEdition.Monochrome,
                    placeKey = "mono",
                )
            }
        }

        if (biblePlanOpen) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { biblePlanOpen = false; biblePlanSeen++ },
                properties = fullScreenDialog(),
            ) {
                com.prayerkey.manna.ui.bibleplan.BiblePlanRoute(
                    onClose = { biblePlanOpen = false; biblePlanSeen++ },
                    onKeep = { heading, text -> onKeepReflection(heading, text) },
                )
            }
        }

        if (meditationOpen) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { meditationOpen = false },
                properties = fullScreenDialog(),
            ) {
                com.prayerkey.manna.ui.meditation.MeditationRoute(
                    onClose = { meditationOpen = false },
                    onKeep = { heading, text -> onKeepReflection(heading, text) },
                )
            }
        }

        Box(
            /* The ground is painted first and the inset applied after, so it
               covers the status bar strip as well. With the padding first, the
               bar only covered its own height and the page scrolled up through
               the clock and the battery with nothing behind it - which is the
               exact failure edge-to-edge introduces and the reason iOS 27 made
               this bar uniform.

               The dock's liquid glass was tried here and taken out again: it
               samples correctly, but a material built for a violet dock over
               dark artwork reads as a grey slab over a cream page. */
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
                Header(today, streak, underneath, onSettings, reminders = onSettings)
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
            Icon(Icons.Outlined.VolunteerActivism, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp))
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

@Composable
private fun DailyPrayerGoal(streak: Int, startPrayer: () -> Unit) {
    val context = platformContext()
    val today = today()
    val sessions = remember { PrayerJourneyStore(context).records() }
    val prayedToday = remember(sessions, today) {
        sessions.filter { record ->
            com.prayerkey.manna.platform.localDateOfMillis(record.startedAt) == today && record.completed
        }.sumOf { it.minutes }
    }
    val prefs = remember { context.keyValues("daily_prayer_goal") }
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
    val context = platformContext()
    val prefs = context.keyValues("prayer_challenge_machine")
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

/**
 * The wordmark, condensing.
 *
 * At the top of the page it is the full mark with the date under it. As the
 * page rises the name draws in - smaller, tighter, the date fading out from
 * under it - until it is the line of type a toolbar carries. Nothing appears
 * or disappears; one thing changes size, which is the difference between a
 * title that condenses and a title that is swapped for another.
 *
 * [condensed] is 0 at the top of the page and 1 once it has scrolled.
 */
@Composable private fun Header(date: LocalDate, streak: Int, condensed: Float, settings: () -> Unit, reminders: () -> Unit) {
    val t = androidx.compose.animation.core.FastOutSlowInEasing.transform(condensed)
    val markSize = (24f - 7f * t).sp
    val tracking = (1.45f - 0.5f * t).sp
    val walk by com.prayerkey.manna.data.Walk.state.collectAsState()
    val walkShown = walk.current(date.toEpochDays().toLong()) > 0
    val barHeight = ((if (walkShown) 98f else 74f) - (if (walkShown) 42f else 18f) * t).dp
    var walkOpen by remember { mutableStateOf(false) }
    if (walkOpen) com.prayerkey.manna.ui.walk.WalkSheet(walk) { walkOpen = false }
    Box(Modifier.fillMaxWidth().height(barHeight).padding(horizontal = 20.dp)) {
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
            Text("PRAYERKEY", fontFamily = DisplaySerif, fontWeight = FontWeight.Normal, fontSize = markSize, letterSpacing = tracking, color = Pk.Charcoal)
            Text(
                date.format("EEEE, MMM d"),
                Modifier.padding(top = (5f - 4f * t).dp).graphicsLayer { alpha = (1f - t * 1.6f).coerceAtLeast(0f) },
                fontFamily = UtilitySans, fontSize = (12f - 2f * t).sp, color = Pk.Muted,
            )
            com.prayerkey.manna.ui.walk.WalkChip(
                walk, onClick = { walkOpen = true },
                modifier = Modifier.padding(top = 6.dp).graphicsLayer { alpha = (1f - t * 1.6f).coerceAtLeast(0f) },
            )
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
    val image: DrawableResource,
    val badge: String? = null,
    val open: () -> Unit,
)

@Composable private fun ShortcutRail(
    /** How many of today's ten pulls are prayed, shown on the badge. */
    todayBadge: String?,
    todayPulls: () -> Unit,
    prayNow: () -> Unit,
    /** The named-challenge shelf. */
    challenges: () -> Unit,
) {
    val shortcuts = listOf(
        HomeShortcut("Today", Icons.Outlined.WbSunny, Res.drawable.prayerkey_hero_dawn, todayBadge, todayPulls),
        HomeShortcut("Pray now", Icons.Outlined.Bolt, Res.drawable.onboarding_pray_v3, open = prayNow),
        HomeShortcut("Challenges", Icons.Outlined.LocalFireDepartment, Res.drawable.discovery_cover_3, open = challenges),
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

@Composable private fun HeroRail(title: String, scrolled: () -> Int, open: () -> Unit) {
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
        /* The picture travels at about two thirds of the page's speed and
           leans in a little as it goes, so the card has a depth to it: the
           words sit on the glass at the front and the dawn is somewhere
           behind them. Held to a third of the card's height, so the frame
           never runs out of photograph to show. */
        Image(
            painterResource(Res.drawable.prayerkey_hero_dawn), null,
            Modifier.fillMaxSize().graphicsLayer {
                // read here, in the layer, so a scroll only redraws the picture
                val drift = (scrolled() * .34f).coerceAtMost(150f)
                translationY = drift
                val zoom = 1.06f + (drift / 3000f)
                scaleX = zoom
                scaleY = zoom
            },
            contentScale = ContentScale.Crop,
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
                Text("Today's devotional", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    val context = platformContext()
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
                painterResource(Res.drawable.verse_card_oxblood), null,
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
            Column(
                // inside the paper's own margins: at 46dp the words touched its torn edges
                Modifier.fillMaxWidth().padding(horizontal = 62.dp).padding(top = 120.dp, bottom = 105.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Verse of the day", style = labelStyle(), color = Pk.GoldDeep)
                Text(card.reference, Modifier.padding(top = 17.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 27.sp, color = Pk.Oxblood, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Row(Modifier.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.width(42.dp), color = Pk.Gold.copy(alpha = .72f))
                    Box(Modifier.padding(horizontal = 8.dp).size(5.dp).background(Pk.Oxblood, CircleShape))
                    HorizontalDivider(Modifier.width(42.dp), color = Pk.Gold.copy(alpha = .72f))
                }
                Text(
                    card.verse,
                    fontFamily = DisplaySerif, fontSize = 19.sp, lineHeight = 27.sp, color = Pk.Charcoal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusAction(Icons.Outlined.Download, "Save") { Share.cards.saveStatus(context, card) }
                    StatusAction(Icons.Outlined.Share, "Share") { Share.cards.shareStatus(context, card) }
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
    val context = platformContext()
    val words = prayer.prayer /*
        ?: "Lord, according to Your Word, order my steps today. Let this day be a blessing to me. Give me wisdom for every decision, peace in every pressure, favour in the work of my hands, and protection wherever I go. Keep my heart close to You and let my life bring hope to someone today. In Jesus’ name, Amen."
    */
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), color = Pk.DeepDark, shadowElevation = 5.dp) {
        // sized to the prayer: a fixed 470dp left a band of empty violet under the buttons
        Box(Modifier.fillMaxWidth()) {
            Image(painterResource(Res.drawable.today_prayer_violet), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Pk.DeepDark.copy(alpha = .16f), Pk.DeepDark.copy(alpha = .52f), Pk.DeepDark.copy(alpha = .90f)))))
            Column(Modifier.fillMaxWidth().padding(horizontal = 23.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // the PrayerKey mark - hands at prayer - not a meditation pose
                Image(painterResource(Res.drawable.prayerkey_mark), null, Modifier.padding(top = 4.dp).size(38.dp))
                /* Its own title, then the prayer entire.
                   The card used to say "Morning prayer" over two hundred
                   words cut off at eleven lines with three dots - a prayer
                   nobody could finish and nobody would send. The prayers are
                   written short now, so the card carries one from the address
                   to the Amen. */
                Text(
                    prayer.title,
                    Modifier.padding(top = 12.dp),
                    fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                    fontSize = 21.sp, color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Text(
                    prayer.ref,
                    Modifier.padding(top = 5.dp),
                    fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.GoldLight,
                )
                Text(
                    words,
                    /* No weight here. This column sizes itself to its content,
                       and a weighted child in a column with no bounded height
                       is given nothing - which drew the prayer as an empty
                       space with the buttons floating above it. */
                    Modifier.padding(top = 18.dp, bottom = 20.dp),
                    fontFamily = DisplaySerif,
                    fontSize = 17.sp,
                    lineHeight = 27.sp,
                    color = Color.White.copy(alpha = .94f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                    PrayerImageAction(Icons.Outlined.Download, "Save") { Share.cards.savePrayerStatus(context, words) }
                    PrayerImageAction(Icons.Outlined.Share, "Share") { Share.cards.sharePrayerStatus(context, words) }
                    PrayerImageAction(Icons.Outlined.VolunteerActivism, "Pray", open)
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
        Text("Bible verses by topic", style = labelStyle(), color = Pk.Charcoal, modifier = Modifier.padding(bottom = 11.dp))
        Box(
            Modifier.fillMaxWidth().height(236.dp)
                .shadow(16.dp, RoundedCornerShape(27.dp), spotColor = Pk.Oxblood.copy(alpha = .24f))
                .clip(RoundedCornerShape(27.dp)).clickable(onClick = open),
        ) {
            Image(
                painterResource(Res.drawable.closing_verse_violet), null,
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
                painterResource(Res.drawable.bible_cover_red), "Holy Bible",
                Modifier.align(Alignment.CenterStart).padding(start = 18.dp)
                    .width(118.dp).height(196.dp)
                    .shadow(14.dp, RoundedCornerShape(9.dp), spotColor = Color.Black.copy(alpha = .7f))
                    .clip(RoundedCornerShape(9.dp)),
                contentScale = ContentScale.Crop,
            )

            Column(
                Modifier.fillMaxHeight().padding(start = 157.dp, end = 18.dp, top = 27.dp, bottom = 22.dp),
            ) {
                Text("Holy Bible", color = Pk.GoldLight, fontFamily = UtilitySans,
                    fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                Text("Find a verse\nfor today", Modifier.padding(top = 8.dp),
                    color = Color.White, fontFamily = DisplaySerif, fontSize = 25.sp,
                    lineHeight = 29.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Surface(onClick = open, shape = RoundedCornerShape(14.dp), color = Color.White) {
                    Row(Modifier.padding(horizontal = 15.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Open", color = Pk.Oxblood, fontSize = 13.sp,
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
                FilledTonalButton(pray, Modifier.weight(1f), colors = ButtonDefaults.filledTonalButtonColors(containerColor = Pk.Oxblood, contentColor = Color.White)) { Text("My prayers", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                OutlinedButton(write, Modifier.weight(1f), border = BorderStroke(1.dp, Color.White.copy(alpha = .22f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("Journal", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
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
                Text("Start prayer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable private fun TodayRhythmPanelLegacy(
    saved: Int, journal: Int, streak: Int,
    morningPrayer: () -> Unit, devotional: () -> Unit, bible: () -> Unit,
    reflection: () -> Unit, eveningPrayer: () -> Unit, history: () -> Unit,
) {
    val context = platformContext()
    val dateKey = today().toString()
    val prefs = remember { context.keyValues("today_rhythm") }
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
        Dialog(onDismissRequest = { openMoment = null }, properties = fullScreenDialog()) {
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
                    Text("Today, ${today().format("MMM d")}", fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 25.sp, color = Pk.Charcoal)
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
    devotionalMinutes: Int,
    readingLabel: String,
    readingMinutes: Int,
    morningPrayer: () -> Unit,
    afternoonPrayer: () -> Unit,
    nightPrayer: () -> Unit,
    midnightPrayer: () -> Unit,
    devotional: () -> Unit,
    bible: () -> Unit,
    eveningPrayer: () -> Unit,
    calendar: () -> Unit,
) {
    val context = platformContext()
    var clockNow by remember { mutableStateOf(now()) }
    val today = clockNow.toLocalDate()
    val dateKey = today.toString()
    val prefs = remember { context.keyValues("home_completed") }
    fun keptOn(date: LocalDate): Set<String> = buildSet {
        listOf("morning", "afternoon", "evening", "night", "midnight", "devotional").forEach { if (prefs.getBoolean("${date}:$it", false)) add(it) }
        if (com.prayerkey.manna.data.PullsProgress.Bible.isComplete(context, date)) add("bible")
    }
    // read now, not a second from now: an empty first frame made every kept
    // stop "complete" again - and celebrate - each time Home was opened
    var completed by remember { mutableStateOf(keptOn(today())) }
    val hour = clockNow.hour
    LaunchedEffect(Unit) {
        while (true) {
            /* Checked every second, changed once a minute: the whole panel
               used to rebuild each second for a clock it only reads by the
               hour. A kept stop is still picked up within the second. */
            val now = now()
            if (now.hour != clockNow.hour || now.minute != clockNow.minute || now.toLocalDate() != clockNow.toLocalDate()) clockNow = now
            completed = keptOn(now.toLocalDate())
            // a kept stop on today's path is a day walked with God
            if (completed.isNotEmpty()) com.prayerkey.manna.data.Walk.mark(context)
            kotlinx.coroutines.delay(1_000)
        }
    }

    /*
     * The last slot follows the clock, which is how Night and Midnight became
     * reachable at all.
     *
     * NightPrayerScreen had no entry point anywhere in the app. The office
     * exists, it is written, it now turns pages - and nothing opened it. Its
     * only route was a Discovery title "Night prayer" that no shelf item
     * carries, inside HomeContentLibrary, which is itself never called.
     * Midnight was barely better: reachable only by tapping the 23:55
     * notification, so if you dismissed that, the watch was gone for the night.
     *
     * OfficeStore.officeFor already knows which office an hour belongs to -
     * that is its whole job, and the office screens have always trusted it. The
     * row now asks it the same question, so one slot is Evening Prayer at six,
     * Night Prayer at ten, and the Midnight watch at one in the morning, and
     * each opens the office whose name it is showing.
     */
    /* The late office is chosen by the clock, so it is the office of this very
       hour - its window is this hour alone, which makes it the one "Now" after
       dark rather than losing to the devotional, which is open all day. */
    val lateRow = when (com.prayerkey.manna.data.OfficeStore.officeFor(hour, clockNow.minute)) {
        com.prayerkey.manna.data.OfficeStore.Office.NIGHT ->
            RhythmRow("night", "Night Prayer", "", Icons.Outlined.Bedtime, hour..hour, minutes = 2, open = nightPrayer)
        com.prayerkey.manna.data.OfficeStore.Office.MIDNIGHT ->
            RhythmRow("midnight", "Midnight Prayer", "", Icons.Outlined.DarkMode, hour..hour, minutes = 5, open = midnightPrayer)
        else ->
            RhythmRow("evening", "Evening Prayer", "", Icons.Outlined.DarkMode, 17..23, minutes = 3, open = eveningPrayer)
    }

    val rows = listOf(
        /* Title, then whatever is actually today's, then how long it takes.
           The strap lines went: "Start your day with God" under Morning Prayer
           told a reader nothing the title had not, and three of those stacked
           turned a schedule back into a page to read. The devotional and the
           reading keep their line because it changes daily and is the reason
           you would open one rather than the other. */
        // in the order the day is lived: the path runs from morning to night
        RhythmRow("morning", "Morning Prayer", "", Icons.Outlined.WbSunny, 4..11, minutes = 3, open = morningPrayer),
        RhythmRow("devotional", "Daily Devotional", devotionalTitle, Icons.Outlined.AutoStories, 0..23, minutes = devotionalMinutes, reservesDetail = true, open = devotional),
        RhythmRow("bible", "Bible Reading", readingLabel, Icons.Outlined.MenuBook, 0..23, minutes = readingMinutes, reservesDetail = true, open = bible),
        RhythmRow("afternoon", "Afternoon Prayer", "", Icons.Outlined.WbTwilight, 12..16, minutes = 2, open = afternoonPrayer),
        lateRow,
    )
    val doneCount = rows.count { it.id in completed }

    /*
     * One row is recommended, and only one.
     *
     * The badge first went on every row whose hour matched, which put "Now" on
     * three of the four: the devotional and the reading are open all day, so
     * their range is 0..23 and always matches. A recommendation that points at
     * most of the list is not a recommendation.
     *
     * So: the narrowest unfinished window that contains this hour - which is
     * always an office, since only the offices have one - and failing that, the
     * first thing still to do.
     */
    val recommended = rows
        .filter { it.id !in completed }
        .let { open ->
            open.filter { hour in it.hours }.minByOrNull { it.hours.last - it.hours.first }
                ?: open.firstOrNull()
        }?.id

    val complete = rows.isNotEmpty() && doneCount == rows.size

    /* The day as a path.
       It was a checklist: five rows of equal weight, a violet ring on each
       finished one - so the loudest things on the card were the ones already
       done - a chevron button on every row, and a progress bar at the foot,
       away from what it counted. Now one line runs from morning to night and
       fills as the day is kept; what is done shrinks to a dot, what is next is
       a quiet line, and only the hour you are in opens out, with its picture
       and one way in. The count lives in the ring at the head of the card. */
    /* The whole panel in the brand violet: the day is the thing on Home that
       is yours, so it is the one surface wearing the colour. */
    Surface(
        modifier = Modifier.fillMaxWidth()
            .shadow(22.dp, RoundedCornerShape(34.dp), ambientColor = Pk.Oxblood, spotColor = Pk.Oxblood),
        shape = RoundedCornerShape(34.dp),
        color = Color.Transparent,
    ) {
        Column(
            // sized first, padded inside: the resize clips, and padded outside
            // it cut the Now card's glow off in a hard line
            Modifier.panelViolet()
                .animateContentSize(spring(dampingRatio = .86f, stiffness = 320f))
                .padding(top = 24.dp, bottom = 10.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Today, " + today.format("MMM d"),
                        fontFamily = DisplaySerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp,
                        color = Color.White,
                    )
                    val weekday = today.format("EEEE")
                    Text(
                        if (complete) "$weekday  ·  The day is kept" else weekday,
                        Modifier.padding(top = 3.dp),
                        fontFamily = UtilitySans,
                        fontSize = 12.sp,
                        fontWeight = if (complete) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (complete) Pk.GoldLight else Color.White.copy(alpha = .72f),
                    )
                }
                DayRing(done = doneCount, of = rows.size, onClick = calendar)
            }
            Spacer(Modifier.height(16.dp))
            rows.forEachIndexed { index, row ->
                PathStop(
                    row = row,
                    done = row.id in completed,
                    current = row.id == recommended,
                    first = index == 0,
                    last = index == rows.lastIndex,
                    reachedFromDone = index > 0 && rows[index - 1].id in completed,
                )
            }
        }
    }
}

/**
 * The panel's violet, fitted to the panel: the brand's own deepening gently
 * from top to foot, with a soft light from the top left. It was a diagonal
 * gradient set in pixels, which laid a visible seam across the middle.
 */
private fun Modifier.panelViolet(): Modifier = drawBehind {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF7E35F8), Color(0xFF6A14F0), Color(0xFF5A00DC))))
    drawRect(
        Brush.radialGradient(
            listOf(Color.White.copy(alpha = .16f), Color.Transparent),
            center = Offset(size.width * .12f, 0f),
            radius = size.width * 1.1f,
        ),
    )
}

private data class RhythmRow(
    val id: String,
    val title: String,
    /** Today's own - the devotional's title, the chapter. Blank on the offices. */
    val detail: String,
    val icon: ImageVector,
    val hours: IntRange,
    /** How long it takes, so the card can be chosen between rather than read. */
    val minutes: Int = 0,
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

/** The picture each part of the day wears. */
private fun artOf(id: String): DrawableResource = when (id) {
    // not the devotional hero's picture: the two sit one above the other
    "devotional" -> Res.drawable.today_manuscript_hero
    "bible" -> Res.drawable.onboarding_read_v3
    "morning" -> Res.drawable.welcome_prayer_dawn
    "afternoon" -> Res.drawable.discovery_cover_4
    "evening" -> Res.drawable.rhythm_evening
    "night" -> Res.drawable.onboarding_prayer
    else -> Res.drawable.rhythm_reflection
}

/**
 * The day's count, at the head of the card: a ring that closes as the day is
 * kept, and turns gold when it is. Tapping it opens the prayer calendar, which
 * is the history of these rings.
 */
@Composable private fun DayRing(done: Int, of: Int, onClick: () -> Unit) {
    val progress by animateFloatAsState(
        if (of == 0) 0f else done / of.toFloat(),
        spring(dampingRatio = .72f, stiffness = 180f), label = "dayRing",
    )
    val complete = of > 0 && done == of
    Box(
        Modifier.size(50.dp).clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Prayer calendar, $done of $of kept today" },
        contentAlignment = Alignment.Center,
    ) {
        // the gold breath only runs once the day is kept
        val breath = if (complete) {
            rememberInfiniteTransition(label = "kept")
                .animateFloat(.35f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "breath")
        } else null
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            val stroke = 3.6.dp.toPx()
            if (breath != null) drawCircle(Pk.GoldLight.copy(alpha = .28f * breath.value), radius = size.minDimension / 2 + 3.dp.toPx())
            drawCircle(Color.White.copy(alpha = .22f), style = Stroke(stroke))
            if (progress > 0f) drawArc(
                if (complete) Pk.GoldLight else Color.White,
                startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
        if (complete) {
            Icon(Icons.Outlined.Check, null, tint = Pk.GoldLight, modifier = Modifier.size(19.dp))
        } else {
            Text(
                "$done/$of",
                fontFamily = UtilitySans, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, color = Color.White,
            )
        }
    }
}

/**
 * One stop on the day's path.
 *
 * The rail on the left is drawn per stop: the line coming in is violet when
 * the stop before it is kept, the line going out is violet when this one is,
 * so the path fills from the top as the day goes. A kept stop is a filled dot
 * and its name in grey; the moment it becomes kept - you come back from the
 * prayer - the dot springs in with a burst of gold and a small tick you can
 * feel. The stop you are in opens into the hour's sky; every other open stop
 * is a single line that presses in under the thumb.
 */
@Composable private fun PathStop(
    row: RhythmRow,
    done: Boolean,
    current: Boolean,
    first: Boolean,
    last: Boolean,
    reachedFromDone: Boolean,
) {
    /* Not tappable until it has something to open. Tapping "Bible reading"
       before the plan resolves used to open an empty chapter. */
    val ready = !row.reservesDetail || row.detail.isNotBlank()
    val haptics = LocalHapticFeedback.current
    val press = remember { MutableInteractionSource() }
    val pressed by press.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .965f else 1f, spring(dampingRatio = .5f, stiffness = 620f), label = "stop")
    val railDone = Color.White
    val railAhead = Color.White.copy(alpha = .26f)
    // only the stop you are in pulses, so only it runs an animation
    val halo = if (current && !done) {
        val pulse = rememberInfiniteTransition(label = "now")
        pulse.animateFloat(0f, 1f, infiniteRepeatable(tween(2000), RepeatMode.Restart), label = "halo")
    } else null

    /* The reward. Only for a change seen on screen - a stop that was already
       kept when Home opened just sits there, kept. */
    var wasDone by remember(row.id) { mutableStateOf(done) }
    val pop = remember(row.id) { androidx.compose.animation.core.Animatable(if (done) 1f else 0f) }
    val burst = remember(row.id) { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(done) {
        if (done && !wasDone) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            pop.snapTo(0f)
            burst.snapTo(0f)
            launch { pop.animateTo(1f, spring(dampingRatio = .38f, stiffness = 260f)) }
            burst.animateTo(1f, tween(900))
        } else if (!done) {
            pop.snapTo(0f)
        }
        wasDone = done
    }

    Row(
        Modifier.fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = press, indication = null, enabled = ready) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                row.open()
            }
            .padding(start = 20.dp, end = 20.dp),
    ) {
        // the rail and this stop's place on it
        val nodeTop = if (current && !done) 44.dp else 0.dp
        Box(
            Modifier.width(24.dp).fillMaxHeight()
                .drawBehind {
                    val cx = size.width / 2
                    val y = if (current && !done) nodeTop.toPx() else size.height / 2
                    val w = 2.4.dp.toPx()
                    val gap = (if (done || current) 11f else 8f).dp.toPx()
                    if (!first) drawLine(if (reachedFromDone) railDone else railAhead, Offset(cx, 0f), Offset(cx, (y - gap).coerceAtLeast(0f)), w)
                    if (!last) drawLine(if (done) railDone else railAhead, Offset(cx, y + gap), Offset(cx, size.height), w)
                    val c = Offset(cx, y)
                    when {
                        done -> {
                            // gold sparks thrown out as the dot lands
                            if (burst.value < 1f) {
                                val t = burst.value
                                repeat(10) { i ->
                                    val angle = ((i * 36.0 + 18.0) * kotlin.math.PI / 180.0)
                                    val dist = (10f + 26f * t).dp.toPx()
                                    drawCircle(
                                        (if (i % 2 == 0) Pk.GoldLight else Color.White).copy(alpha = 1f - t),
                                        radius = (2.6f * (1f - t) + .6f).dp.toPx(),
                                        center = Offset(cx + dist * kotlin.math.cos(angle).toFloat(), y + dist * kotlin.math.sin(angle).toFloat()),
                                    )
                                }
                                drawCircle(Pk.GoldLight.copy(alpha = .4f * (1f - t)), (9f + 16f * t).dp.toPx(), c)
                            }
                            drawCircle(railDone, 9.dp.toPx() * pop.value.coerceAtLeast(0f), c)
                        }
                        current -> {
                            val h = halo?.value ?: 0f
                            drawCircle(Color.White.copy(alpha = .3f * (1f - h)), (9f + 11f * h).dp.toPx(), c)
                            drawCircle(Color.White, 9.dp.toPx(), c, style = Stroke(3.dp.toPx()))
                            drawCircle(Color.White, 3.5.dp.toPx(), c)
                        }
                        else -> {
                            drawCircle(Color.White.copy(alpha = .55f), 6.dp.toPx(), c, style = Stroke(2.dp.toPx()))
                        }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (done) Icon(
                Icons.Outlined.Check, null, tint = Pk.Oxblood,
                modifier = Modifier.size(12.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value },
            )
        }

        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            when {
                current && !done -> NowStop(row, ready, open = row.open)
                done -> Text(
                    row.title,
                    Modifier.padding(vertical = 12.dp),
                    fontFamily = UtilitySans, fontSize = 15.sp,
                    fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = .62f),
                    maxLines = 1,
                )
                else -> Row(
                    Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            row.title,
                            fontFamily = UtilitySans, fontSize = 15.5.sp,
                            fontWeight = FontWeight.SemiBold, color = Color.White,
                            maxLines = 1,
                        )
                        if (row.detail.isNotBlank()) Text(
                            row.detail,
                            Modifier.padding(top = 1.dp),
                            fontFamily = UtilitySans, fontSize = 12.5.sp, color = Color.White.copy(alpha = .72f),
                            maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                    }
                    if (row.minutes > 0) Text(
                        "${row.minutes} min",
                        Modifier.padding(start = 10.dp),
                        fontFamily = UtilitySans, fontSize = 12.sp, color = Color.White.copy(alpha = .72f),
                    )
                    // a door, not a label
                    Icon(
                        Icons.Outlined.ChevronRight, null,
                        tint = Color.White.copy(alpha = .8f),
                        modifier = Modifier.padding(start = 4.dp).size(20.dp),
                    )
                }
            }
        }
    }
}

/** The sky each part of the day is prayed under: dawn, day, dusk, the dark. */
private fun skyOf(id: String): List<Color> = when (id) {
    "morning" -> listOf(Color(0xFFFFB86B), Color(0xFFE9728C), Color(0xFF6A2BE8))
    "afternoon" -> listOf(Color(0xFFFFC857), Color(0xFFFF8F5A), Color(0xFF7B3FF2))
    "evening" -> listOf(Color(0xFFFF8A5B), Color(0xFFA23CB8), Color(0xFF2A1273))
    "night", "midnight" -> listOf(Color(0xFF3B1FA8), Color(0xFF1A1060), Color(0xFF07061F))
    "bible" -> listOf(Color(0xFF8B5CF6), Color(0xFF6200ED), Color(0xFF2A0B78))
    else -> listOf(Color(0xFFA56BFF), Color(0xFF6200ED), Color(0xFF30108C))
}

/**
 * The stop you are in, opened out: its name, today's line, and Begin, on
 * glass over the violet panel. Nothing drawn in the corner - a sun there sat
 * on top of the words and cut the devotional's title short.
 */
@Composable private fun NowStop(row: RhythmRow, ready: Boolean, open: () -> Unit) {
    val sky = skyOf(row.id)
    val alive = rememberInfiniteTransition(label = "sky")
    val sweep by alive.animateFloat(-0.4f, 1.6f, infiniteRepeatable(tween(2600, delayMillis = 900)), label = "sweep")
    val press = remember { MutableInteractionSource() }
    val pressed by press.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .96f else 1f, spring(dampingRatio = .45f, stiffness = 600f), label = "now")
    val haptics = LocalHapticFeedback.current

    Box(
        Modifier.padding(top = 6.dp, bottom = 12.dp).fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(24.dp))
            // on the violet panel the stop is glass - lit a little by its hour
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = .22f), sky[0].copy(alpha = .16f), Color.White.copy(alpha = .08f)),
                    start = Offset(0f, 0f), end = Offset(900f, 700f),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = .34f), RoundedCornerShape(24.dp))
            .clickable(interactionSource = press, indication = null, enabled = ready) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                open()
            },
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 14.dp, top = 16.dp, bottom = 14.dp)) {
            Text(
                "NOW",
                fontFamily = UtilitySans, fontSize = 10.sp, letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = .78f),
            )
            Text(
                row.title,
                Modifier.padding(top = 3.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 24.sp, lineHeight = 28.sp, color = Color.White,
            )
            if (row.detail.isNotBlank()) Text(
                row.detail,
                Modifier.padding(top = 2.dp),
                fontFamily = UtilitySans, fontSize = 13.sp,
                color = Color.White.copy(alpha = .85f),
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (row.minutes > 0) Text(
                    "${row.minutes} min",
                    Modifier.weight(1f),
                    fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = .85f),
                ) else Spacer(Modifier.weight(1f))
                // the one thing to do, and it shines
                Row(
                    Modifier.clip(RoundedCornerShape(99.dp))
                        .background(Color.White)
                        .drawWithContent {
                            drawContent()
                            val x = size.width * sweep
                            drawRect(
                                Brush.linearGradient(
                                    listOf(Color.Transparent, Pk.Oxblood.copy(alpha = .22f), Color.Transparent),
                                    start = Offset(x - 60f, 0f), end = Offset(x + 60f, size.height),
                                ),
                            )
                        }
                        .clickable(enabled = ready) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            open()
                        }
                        .padding(horizontal = 22.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Begin",
                        fontFamily = UtilitySans, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = Pk.Oxblood,
                    )
                    Icon(
                        Icons.Outlined.ArrowForward, null, tint = Pk.Oxblood,
                        modifier = Modifier.padding(start = 6.dp).size(17.dp),
                    )
                }
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
                    Text("Begin here", style = labelStyle(), color = Pk.GoldLight)
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
                        Text(if (i == 0) "DAY 1 OF 365" else "Open", Modifier.padding(top = 15.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Pk.Oxblood)
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
            properties = fullScreenDialog(),
        ) { ProgrammeCategoryScreen(category, { selectedCategory = null }, go) }
    }
    Column(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp)) {
        Text("PRAYERKEY programmes", Modifier.padding(horizontal = 22.dp), style = labelStyle(), color = Pk.Charcoal)
        val originals = categories.first()
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(originals.title, Modifier.weight(1f), style = labelStyle(), color = Pk.Oxblood)
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
    val cover = listOf(Res.drawable.discovery_cover_2, Res.drawable.discovery_cover_4, Res.drawable.discovery_cover_6)[index % 3]
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
                0 -> Res.drawable.discovery_cover_1
                1 -> Res.drawable.discovery_cover_2
                2 -> Res.drawable.discovery_cover_3
                3 -> Res.drawable.discovery_cover_4
                4 -> Res.drawable.discovery_cover_5
                else -> Res.drawable.discovery_cover_6
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

@Composable private fun VerseAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(99.dp))
            /* Glass on the photograph. A solid white pill was the brightest
               thing in the card and outranked the verse. */
            .background(Color.White.copy(alpha = .15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Pk.GoldLight, modifier = Modifier.size(14.dp))
        Text(
            label,
            Modifier.padding(start = 7.dp),
            fontFamily = UtilitySans, fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold, color = Color.White,
        )
    }
}

/** One list, so the head of the Today card and the closing card agree. */
fun verseOfDay(date: LocalDate): Pair<String, String> {
    val words = listOf(
        "Psalm 121:8" to "The LORD shall preserve thy going out and thy coming in from this time forth, and even for evermore.",
        "Psalm 23:1" to "The LORD is my shepherd; I shall not want.",
        "Psalm 46:1" to "God is our refuge and strength, a very present help in trouble.",
        "Proverbs 3:5" to "Trust in the LORD with all thine heart; and lean not unto thine own understanding.",
        "1 Peter 5:7" to "Casting all your care upon him; for he careth for you.",
        "Psalm 119:105" to "Thy word is a lamp unto my feet, and a light unto my path.",
        "Psalm 56:3" to "What time I am afraid, I will trust in thee.",
    )
    return words[(date.toEpochDays().toLong()).mod(words.size.toLong()).toInt()]
}
