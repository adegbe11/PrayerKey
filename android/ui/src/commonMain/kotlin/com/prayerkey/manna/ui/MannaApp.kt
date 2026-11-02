package com.prayerkey.manna.ui

import com.prayerkey.manna.platform.localDateOfEpochDay
import com.prayerkey.manna.ui.platform.Permission
import com.prayerkey.manna.ui.platform.rememberPermissionRequest
import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.ui.platform.Reminders
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.data.mannaStore
import com.prayerkey.manna.resources.*
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.animation.togetherWith
import com.prayerkey.manna.ui.theme.glassSource
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.Alignment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.prayerkey.manna.model.DailyVerses
import com.prayerkey.manna.ui.screens.BibleScreen
import com.prayerkey.manna.ui.screens.PrayerScreen
import com.prayerkey.manna.ui.screens.ProfileScreen
import com.prayerkey.manna.ui.screens.JournalScreen
import com.prayerkey.manna.ui.screens.ChurchScreen
import com.prayerkey.manna.ui.theme.Canvas
import com.prayerkey.manna.ui.theme.Electric
import com.prayerkey.manna.ui.theme.Muted

private data class Destination(val label: String, val icon: ImageVector, val mark: DrawableResource? = null)

@Composable
fun MannaApp(
    onThemeChange: (String, Boolean) -> Unit = { _, _ -> },
    notificationRoute: String? = null,
    onNotificationRouteConsumed: () -> Unit = {},
) {
    val storeContext = platformContext()
    val viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
        AppViewModel(storeContext, storeContext.mannaStore())
    }
    val storageError by viewModel.storageError.collectAsState()
    if (storageError != null) {
        Box(Modifier.fillMaxSize().background(com.prayerkey.manna.ui.theme.Pk.Cream).padding(32.dp), contentAlignment = Alignment.Center) {
            Text(storageError!!, color = com.prayerkey.manna.ui.theme.Pk.Charcoal)
        }
        return
    }
    val saved by viewModel.saved.collectAsState()
    // the walk: any moment with God counts, and a rest day covers a missed one
    val walk by com.prayerkey.manna.data.Walk.state.collectAsState()
    val streak = walk.current(today().toEpochDays().toLong())
    val memory by viewModel.memory.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val sermons by viewModel.sermons.collectAsState()
    val journal by viewModel.journal.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val journalStreak by viewModel.journalStreak.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val topicsReady by viewModel.topicsReady.collectAsState()
    val hydrated by viewModel.hydrated.collectAsState()
    val sermonNotes by viewModel.sermonNotes.collectAsState()
    val formation by viewModel.formation.collectAsState()
    val context = platformContext()
    val notificationPermission = rememberPermissionRequest(Permission.Notifications) { }
    LaunchedEffect(hydrated, preferences.onboarded, preferences.reminderEnabled, preferences.reminderHour, preferences.reminderMinute) {
        if (hydrated && preferences.onboarded) {
            /* Scheduling touches AlarmManager and the reminder preferences, and
               a LaunchedEffect runs on the main dispatcher - so this was disk
               work on the thread drawing the first frame. Nothing here needs to
               finish before the app is usable. */
            kotlinx.coroutines.withContext(com.prayerkey.manna.platform.ioDispatcher) {
                Reminders.scheduleDailySuite(context, preferences.reminderEnabled)
                Reminders.schedule(
                    context, preferences.reminderHour, preferences.reminderMinute, preferences.reminderEnabled,
                )
            }
        }
        if (hydrated && preferences.onboarded && preferences.reminderEnabled && !notificationPermission.granted()) {
            notificationPermission.request()
        }
    }
    LaunchedEffect(hydrated, preferences.themeId, preferences.followSystemTheme) {
        if (hydrated) onThemeChange(preferences.themeId, preferences.followSystemTheme)
    }
    val destinations = remember {
        listOf(
            Destination("Home", Icons.Outlined.Home),
            Destination("Bible", Icons.Outlined.MenuBook),
            Destination("Pray", Icons.Outlined.AutoAwesome, Res.drawable.ic_notification),
            Destination("Church", Icons.Outlined.Church),
            Destination("Journal", Icons.Outlined.EditNote),
        )
    }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var homeAction by remember { mutableStateOf<String?>(null) }
    var verseIndex by remember { mutableIntStateOf(0) }
    var showProfile by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var prayerDraft by rememberSaveable { mutableStateOf<String?>(null) }
    // dark clock and battery over the light screens; the Bible tab is dark
    com.prayerkey.manna.ui.book.BarIcons(dark = preferences.onboarded && selected != 1)
    LaunchedEffect(notificationRoute, hydrated, preferences.onboarded) {
        if (!hydrated || !preferences.onboarded || notificationRoute == null) return@LaunchedEffect
        when (notificationRoute) {
            "bible" -> { selected = 0; homeAction = "bible_session" }
            /* The prayer reminder no longer lands on the Prayer tab. That tab
               is a library of 543 topics with a dock underneath it, so the
               notification used to end in a browsing decision — which is where
               people leave. It opens the day's ten pulls directly instead. */
            "prayer" -> { selected = 0; homeAction = "prayer_session" }
            "church" -> selected = 3
            "journal" -> selected = 4
            "devotional", "fasting", "prayer_challenge", "midnight", "warfare" -> {
                selected = 0
                homeAction = notificationRoute
            }
            else -> {
                selected = 0
                // "has God answered?" opens on Home, at that prayer
                if (notificationRoute.startsWith("answered:")) homeAction = notificationRoute
            }
        }
        showProfile = false
        onNotificationRouteConsumed()
    }
    val journeyInsight = remember(entries, sermonNotes, journal, saved) {
        com.prayerkey.manna.data.JourneyInsights.build(entries, sermonNotes, journal, saved)
    }
    /* What the dashboard renders from. All of it is either already in the
       database or derived from a shipped asset, so Home works with the radio
       off like the rest of the app. */
    val activeDays = remember(entries) {
        entries.map {
            localDateOfEpochDay(com.prayerkey.manna.data.epochDayOf(it.createdAt))
        }.toSet()
    }
    val today = remember { today() }
    val devotion = remember(topics, today) {
        com.prayerkey.manna.data.devotionFor(
            today,
            DailyVerses.map { Triple(it.reference, it.translation, it.verse) },
            topics,
        )
    }
    LaunchedEffect(Unit) {
        viewModel.loadTopics()
        viewModel.loadChallenge("read-the-gospels")
        viewModel.loadChallenge("twenty-one-days")
    }
    val challengeTicks by viewModel.challengeDays.collectAsState()
    val bibleChallenge = remember(challengeTicks, today) {
        com.prayerkey.manna.data.bibleChallenge(today, challengeTicks["read-the-gospels"].orEmpty())
    }
    val prayerChallenge = remember(challengeTicks, topics, today) {
        com.prayerkey.manna.data.prayerChallenge(today, topics, challengeTicks["twenty-one-days"].orEmpty())
    }

    val todayCard = remember(verseIndex, journeyInsight) {
        if (verseIndex == 0) {
            journeyInsight?.recommendedReference
                ?.let { wanted -> DailyVerses.firstOrNull { it.reference == wanted } }
                ?: DailyVerses.first()
        } else DailyVerses[verseIndex % DailyVerses.size]
    }

    /* The dock floats over the content instead of sitting in a bottomBar.
       As a bottomBar it inset every screen by its own height, which is why the
       Bible and prayer decks could never reach the bottom of the glass — a
       Tinder card that stops short of the edge is just a card. Screens that
       scroll add their own bottom clearance (Pk.NavClearance); screens that are
       a single full-bleed card simply run underneath it. */
    /* What the dock is allowed to see. Recorded from the content layer only —
       putting the chrome in here would make the glass sample itself, and a
       panel that samples its own output smears a little more every frame. */
    val backdrop = com.prayerkey.manna.ui.theme.rememberGlassBackdrop()

    /* Back always goes somewhere inside the app before it leaves it: from You
       to where it was opened, from any tab to Home. Without this the back
       gesture on Bible, Pray, Church or Journal closed PrayerKey outright.
       Screens opened on top of a tab register their own back after this one,
       so they still close first. */
    androidx.compose.ui.backhandler.BackHandler(
        enabled = hydrated && preferences.onboarded && (showProfile || selected != 0),
    ) {
        if (showProfile) showProfile = false else selected = 0
    }

    Box(
        Modifier.fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
    ) {
        Box(
            Modifier.fillMaxSize()
                .padding(
                    bottom = if (hydrated && preferences.onboarded && preferences.accountReady && !showProfile) 88.dp else 0.dp,
                )
                .glassSource(backdrop),
        ) {
            if (!hydrated) {
                com.prayerkey.manna.ui.screens.LaunchPrelude(stage = 0)
            } else if (!preferences.onboarded) {
                com.prayerkey.manna.ui.screens.OnboardingScreen { remindersEnabled, reminderHour ->
                    /* The reminder used to be set here, at the one moment
                       people say yes. It is still on by default — the setting
                       lives in Profile, and defaulting it off would switch
                       off the only thing that brings anyone back tomorrow. */
                    viewModel.updatePreferences(
                        preferences.copy(
                            onboarded = true,
                            /* There is no account. Onboarding used to leave
                               these pending so the sign-up screen would show
                               next; it does not exist any more. */
                            accountReady = true,
                            accountKind = "local",
                            reminderEnabled = remindersEnabled,
                            reminderHour = reminderHour,
                            reminderMinute = context.keyValues("onboarding_plan").getInt("reminder_minute", 0),
                        ),
                    )
                }
            } else if (showProfile) {
                ProfileScreen(
                    savedCount = saved.size, streak = streak, prefs = preferences,
                    entries = entries, sermons = sermonNotes, prayers = journal, saved = saved,
                    memory = memory, formation = formation, onRestoreArchive = viewModel::restoreArchive,
                    onBack = { showProfile = false },
                    onUpdate = { updated ->
                        /* Preferences own persistence; MainActivity owns the
                           Material theme wrapped around this composition.
                           Updating only the store made a chosen palette look
                           inert until the process was recreated. Repaint the
                           running app in the same frame as the selection. */
                        viewModel.updatePreferences(updated)
                        onThemeChange(updated.themeId, updated.followSystemTheme)
                    },
                )
            } else {
                /* A tab change is a quick soft crossfade with the new screen
                   settling forward a touch - fast enough to feel instant,
                   soft enough not to feel like a cut. */
                androidx.compose.animation.AnimatedContent(
                    targetState = selected,
                    transitionSpec = {
                        (androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220, delayMillis = 40)) +
                            androidx.compose.animation.scaleIn(androidx.compose.animation.core.tween(260), initialScale = .985f))
                            .togetherWith(androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(140)))
                    },
                    label = "tab",
                ) { tab ->
                when (tab) {
                    0 -> {
                        /* Today, built from the design system. The previous
                           dashboard is kept in HomeScreen for reference but is
                           no longer routed to — this screen answers "what do I
                           do with God today?" in one scroll, which the old one
                           could not. */
                        com.prayerkey.manna.ui.home.AwardHomeDashboard(
                            name = "",
                            streak = streak,
                            activeDays = activeDays,
                            devotion = devotion,
                            bible = bibleChallenge,
                            prayer = prayerChallenge,
                            savedCount = saved.size,
                            journalCount = entries.size,
                            sermonCount = sermonNotes.size,
                            onOpenWord = { selected = 1 },
                            onWriteDevotion = { selected = 4 },
                            onOpenBible = { selected = 1 },
                            onOpenPrayer = { selected = 2 },
                            onOpenPrayerRequest = { request ->
                                prayerDraft = request
                                selected = 2
                            },
                            onOpenJournal = { selected = 4 },
                            onOpenChurch = { selected = 3 },
                            onKeepPrayer = { title, bringing ->
                                viewModel.keepDevotionalPrayer(title, bringing)
                            },
                            onKeepGuidedPrayer = viewModel::keepGuidedPrayer,
                            onKeepReflection = { question, answer ->
                                viewModel.addEntry("devotional", answer, "", null, question)
                            },
                            onOpenChallenge = { challenge ->
                                viewModel.markChallengeDay(
                                    challenge.id, challenge.today.index - 1, !challenge.today.done,
                                )
                            },
                            onToggleChallenge = { challenge ->
                                viewModel.markChallengeDay(
                                    challenge.id, challenge.today.index - 1, !challenge.today.done,
                                )
                            },
                            onSettings = { showProfile = true },
                            initialAction = homeAction,
                            onInitialActionConsumed = { homeAction = null },
                        )
                    }
                    1 -> BibleScreen(
                        readerTextSize = preferences.readerTextSize,
                        onReaderTextSize = { viewModel.updatePreferences(preferences.copy(readerTextSize = it)) },
                        readerRibbon = preferences.readerRibbon,
                        onReaderRibbon = { viewModel.updatePreferences(preferences.copy(readerRibbon = it)) },
                        memory = memory,
                        saved = saved,
                        entries = entries,
                        prayers = journal,
                        sermons = sermonNotes,
                        translation = preferences.translation,
                        onTranslation = { viewModel.updatePreferences(preferences.copy(translation = it)) },
                        onSave = viewModel::save,
                        onMemorize = viewModel::memorize,
                        onReviewMemory = viewModel::reviewMemory,
                        reduceMotion = preferences.reduceMotion,
                    )
                    2 -> PrayerScreen(
                        journal, topics, topicsReady, viewModel::loadTopics, viewModel::savePrayer,
                        initialRequest = prayerDraft,
                        onInitialRequestConsumed = { prayerDraft = null },
                    )
                    3 -> ChurchScreen(
                        notes = sermonNotes,
                        language = preferences.sermonLanguage,
                        onLanguage = { viewModel.updatePreferences(preferences.copy(sermonLanguage = it)) },
                        onSaveNote = viewModel::saveSermonNote,
                        onRefineNote = { id, transcript ->
                            viewModel.refineSermonNote(id, transcript, preferences.sermonLanguage)
                        },
                        onDeleteNote = viewModel::deleteSermonNote,
                    )
                    4 -> JournalScreen(
                        entries = entries,
                        journalStreak = journalStreak,
                        words = saved,
                        todayCard = todayCard,
                        onAdd = viewModel::addEntry,
                        onUpdate = viewModel::updateEntry,
                        onDelete = viewModel::deleteEntry,
                        onAnswered = viewModel::markAnswered,
                        onProfile = { showProfile = true },
                        sermonNotes = sermonNotes,
                        prayers = journal,
                        onAdd2 = viewModel::addWrite,
                        onUpdate2 = viewModel::updateWrite,
                        onAnswerEntry = viewModel::answerEntry,
                        onUpdatePrayerJourney = viewModel::updatePrayerJourney,
                        formation = formation,
                        onUpdateFormation = viewModel::updateFormation,
                        journalLocked = preferences.journalLock,
                        concealPreviews = preferences.concealJournalPreviews,
                    )
                    else -> Unit
                }
                }
            }
        }

        // a milestone on the walk, celebrated once wherever it was reached
        if (hydrated && preferences.onboarded) com.prayerkey.manna.ui.walk.WalkCelebration()

        // no dock during onboarding — the first screen stays undistracted
        if (hydrated && preferences.onboarded && preferences.accountReady && !showProfile) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                com.prayerkey.manna.ui.components.MannaDock(
                    items = destinations.map { com.prayerkey.manna.ui.components.DockItem(it.label, it.icon, it.mark) },
                    selected = selected,
                    backdrop = backdrop,
                    onSelect = { selected = it },
                )
            }
        }
    }
}
