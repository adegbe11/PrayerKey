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
fun MannaApp() {
    val viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val saved by viewModel.saved.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val memory by viewModel.memory.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val sermons by viewModel.sermons.collectAsState()
    val journal by viewModel.journal.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val journalStreak by viewModel.journalStreak.collectAsState()
    val context = LocalContext.current
    val destinations = remember {
        listOf(
            Destination("Home", Icons.Outlined.Home),
            Destination("Bible", Icons.Outlined.Book),
            Destination("AI Pray", Icons.Outlined.AutoAwesome),
            Destination("Church", Icons.Outlined.Church),
            Destination("Journal", Icons.Outlined.BookmarkBorder),
        )
    }
    var selected by remember { mutableIntStateOf(0) }
    var verseIndex by remember { mutableIntStateOf(0) }
    var showProfile by remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        containerColor = Canvas,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Electric, selectedTextColor = Electric,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Muted, unselectedTextColor = Muted,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (showProfile) {
                ProfileScreen(saved.size, streak, preferences, onBack = { showProfile = false }, onUpdate = viewModel::updatePreferences)
            } else AnimatedContent(targetState = selected, label = "destination") { page ->
                when (page) {
                    0 -> HomeScreen(
                        card = DailyVerses[verseIndex % DailyVerses.size],
                        name = preferences.name,
                        reduceMotion = preferences.reduceMotion,
                        streak = streak,
                        onReceived = viewModel::recordDailyPull,
                        onReceiveNext = { verseIndex++ },
                        onSave = viewModel::save,
                        onPray = { selected = 2 },
                        onShare = { verse ->
                            CardShareRenderer.share(context, verse)
                        },
                        onProfile = { showProfile = true },
                    )
                    1 -> BibleScreen(
                        memory = memory,
                        translation = preferences.translation,
                        onTranslation = { viewModel.updatePreferences(preferences.copy(translation = it)) },
                        onSave = viewModel::save,
                        onMemorize = viewModel::memorize,
                        onAdvanceMemory = viewModel::advanceMemory,
                    )
                    2 -> PrayerScreen(journal, viewModel::savePrayer)
                    3 -> ChurchScreen(
                        sermons = sermons,
                        onStartSession = viewModel::startSermon,
                        onVerseDetected = viewModel::addSermonVerse,
                        onEndSession = viewModel::endSermon,
                        onSave = viewModel::save,
                    )
                    4 -> JournalScreen(
                        entries = entries,
                        journalStreak = journalStreak,
                        words = saved,
                        todayCard = DailyVerses[verseIndex % DailyVerses.size],
                        onAdd = viewModel::addEntry,
                        onUpdate = viewModel::updateEntry,
                        onDelete = viewModel::deleteEntry,
                        onAnswered = viewModel::markAnswered,
                    )
                    else -> Unit
                }
            }
        }
    }
}
