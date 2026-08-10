package com.prayerkey.manna.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Church
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.prayerkey.manna.share.CardShareRenderer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.prayerkey.manna.model.DailyVerses
import com.prayerkey.manna.ui.home.HomeScreen
import com.prayerkey.manna.ui.screens.BibleScreen
import com.prayerkey.manna.ui.screens.PrayerScreen
import com.prayerkey.manna.ui.screens.ProfileScreen
import com.prayerkey.manna.ui.screens.JournalScreen
import com.prayerkey.manna.ui.screens.ChurchScreen
import com.prayerkey.manna.ui.theme.Canvas
import com.prayerkey.manna.ui.theme.Electric
import com.prayerkey.manna.ui.theme.Muted

private data class Destination(val label: String, val icon: ImageVector)

@Composable
fun MannaApp(onThemeChange: (String, Boolean) -> Unit = { _, _ -> }) {
    val viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val saved by viewModel.saved.collectAsState()
    val streak by viewModel.streak.collectAsState()
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
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(hydrated, preferences.onboarded, preferences.reminderEnabled) {
        if (hydrated && preferences.onboarded && preferences.reminderEnabled &&
            android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    LaunchedEffect(hydrated, preferences.themeId, preferences.followSystemTheme) {
        if (hydrated) onThemeChange(preferences.themeId, preferences.followSystemTheme)
    }
    val destinations = remember {
        listOf(
            Destination("Home", Icons.Outlined.Home),
            Destination("Bible", Icons.Outlined.Book),
            Destination("Pray", Icons.Outlined.AutoAwesome),
            Destination("Church", Icons.Outlined.Church),
            Destination("Journal", Icons.Outlined.BookmarkBorder),
        )
    }
    var selected by remember { mutableIntStateOf(0) }
    var verseIndex by remember { mutableIntStateOf(0) }
    var showProfile by remember { androidx.compose.runtime.mutableStateOf(false) }
    val journeyInsight = remember(entries, sermonNotes, journal, saved) {
        com.prayerkey.manna.data.JourneyInsights.build(entries, sermonNotes, journal, saved)
    }
    /* What the dashboard renders from. All of it is either already in the
       database or derived from a shipped asset, so Home works with the radio
       off like the rest of the app. */
    val activeDays = remember(entries) {
        entries.map {
            java.time.Instant.ofEpochMilli(it.createdAt)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        }.toSet()
    }
    val today = remember { java.time.LocalDate.now() }
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

    Scaffold(
        // the theme runs to the bottom edge of the phone; a white Scaffold
        // left a pale band showing under the dock
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            // no dock during onboarding — the first screen stays undistracted
            if (hydrated && preferences.onboarded) com.prayerkey.manna.ui.components.MannaDock(
                items = destinations.map { com.prayerkey.manna.ui.components.DockItem(it.label, it.icon) },
                selected = selected,
                onSelect = { selected = it },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (!hydrated) {
                com.prayerkey.manna.ui.screens.LaunchPrelude(stage = 0)
            } else if (!preferences.onboarded) {
                com.prayerkey.manna.ui.screens.OnboardingScreen {
                    /* The reminder used to be set here, at the one moment
                       people say yes. It is still on by default — the setting
                       lives in Profile, and defaulting it off would switch
                       off the only thing that brings anyone back tomorrow. */
                    viewModel.updatePreferences(
                        preferences.copy(onboarded = true, reminderEnabled = true),
                    )
                    com.prayerkey.manna.reminder.ReminderReceiver.schedule(
                        context, preferences.reminderHour, preferences.reminderMinute, true,
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
                // instant tab switch — no transition animation, zero delay
                when (selected) {
                    0 -> HomeScreen(
                        card = todayCard,
                        name = preferences.name,
                        streak = streak,
                        activeDays = activeDays,
                        devotion = devotion,
                        bibleChallenge = bibleChallenge,
                        prayerChallenge = prayerChallenge,
                        savedCount = saved.size,
                        journalCount = entries.size,
                        sermonCount = sermonNotes.size,
                        onToggleChallenge = { c ->
                            viewModel.markChallengeDay(c.id, c.today.index - 1, !c.today.done)
                        },
                        onOpenChallenge = { c ->
                            // the Bible plan opens the Bible, the prayer plan the deck
                            selected = if (c.id == "read-the-gospels") 1 else 2
                        },
                        onWriteDevotion = { selected = 4 },
                        onOpenBible = { selected = 1 },
                        onOpenJournal = { selected = 4 },
                        onOpenChurch = { selected = 3 },
                        onSettings = { showProfile = true },
                        journeyInsight = journeyInsight,
                        reduceMotion = preferences.reduceMotion,
                        onReceived = viewModel::recordDailyPull,
                        onReceiveNext = { verseIndex++ },
                        onSave = viewModel::save,
                        onPray = { selected = 2 },
                        onShare = { verse ->
                            CardShareRenderer.share(context, verse)
                        },
                    )
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
                    2 -> PrayerScreen(journal, topics, topicsReady, viewModel::loadTopics, viewModel::savePrayer)
                    3 -> ChurchScreen(
                        notes = sermonNotes,
                        language = preferences.sermonLanguage,
                        onLanguage = { viewModel.updatePreferences(preferences.copy(sermonLanguage = it)) },
                        onSaveNote = viewModel::saveSermonNote,
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
}
