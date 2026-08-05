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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
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
fun MannaApp(onThemeChange: (String) -> Unit = {}) {
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
                // one frame of brand, never a flash of the wrong screen
                Box(Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.background))
            } else if (!preferences.onboarded) {
                com.prayerkey.manna.ui.screens.OnboardingScreen(
                    themeId = preferences.themeId,
                    onTheme = { t ->
                        viewModel.updatePreferences(preferences.copy(themeId = t))
                        onThemeChange(t)
                    },
                ) { wantsReminder, hour ->
                    /* The reminder is set at the one moment people say yes.
                       It used to default off and hide behind Profile, which
                       is the strongest retention lever switched off. */
                    viewModel.updatePreferences(
                        preferences.copy(
                            onboarded = true,
                            reminderEnabled = wantsReminder,
                            reminderHour = hour,
                            reminderMinute = 0,
                        ),
                    )
                    if (wantsReminder) {
                        com.prayerkey.manna.reminder.ReminderReceiver.schedule(context, hour, 0, true)
                    }
                }
            } else if (showProfile) {
                ProfileScreen(
                    savedCount = saved.size, streak = streak, prefs = preferences,
                    entries = entries, sermons = sermonNotes, prayers = journal, saved = saved,
                    memory = memory, formation = formation, onRestoreArchive = viewModel::restoreArchive,
                    onBack = { showProfile = false }, onUpdate = viewModel::updatePreferences,
                )
            } else {
                // instant tab switch — no transition animation, zero delay
                when (selected) {
                    0 -> HomeScreen(
                        card = todayCard,
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
