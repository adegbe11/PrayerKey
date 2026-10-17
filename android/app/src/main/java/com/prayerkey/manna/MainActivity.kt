package com.prayerkey.manna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.prayerkey.manna.data.DEFAULT_THEME_ID
import com.prayerkey.manna.data.resolveTheme
import com.prayerkey.manna.ui.MannaApp
import com.prayerkey.manna.ui.screens.LaunchPrelude
import com.prayerkey.manna.ui.theme.MannaTheme
import android.content.Intent

class MainActivity : ComponentActivity() {
    private var notificationRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        /*
         * Debug builds fail loudly on main-thread disk reads.
         *
         * The app ships five JSON assets and one of them is the King James
         * Version - 4.5 MB, 31,102 verses. Reading it takes long enough to
         * drop frames, and LaunchedEffect runs on the main dispatcher, so
         * `LaunchedEffect { bible.chapter(...) }` reads and parses it on the
         * thread that draws. That reads as perfectly ordinary Compose and is
         * why eight of them accumulated without anyone noticing.
         *
         * Grep found those eight. StrictMode finds the ninth, at the moment it
         * is written, instead of leaving it to be felt as "the app is a bit
         * slow sometimes". Debug only - penaltyLog, never penaltyDeath, since
         * a first-launch read is unavoidable and should not be fatal.
         */
        if (BuildConfig.DEBUG) {
            android.os.StrictMode.setThreadPolicy(
                android.os.StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build(),
            )
            android.os.StrictMode.setVmPolicy(
                android.os.StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build(),
            )
        }
        com.prayerkey.manna.data.PrefsWarmer.warm(this)
        com.prayerkey.manna.ui.platform.Reminders.scheduler = com.prayerkey.manna.reminder.AndroidReminders
        com.prayerkey.manna.ui.share.Share.cards = com.prayerkey.manna.share.AndroidShareCards
        super.onCreate(savedInstanceState)
        notificationRoute = intent?.getStringExtra("prayerkey_destination")
        /*
         * Edge to edge, deliberately.
         *
         * This said `true` - fit the content inside the system bars - while
         * the app targets SDK 36, where Android 15 draws edge to edge whatever
         * the app asks for. So the request was ignored and the insets were
         * never handled on purpose, which is what put a black band above the
         * status bar on several screens.
         *
         * enableEdgeToEdge() was already imported and never called.
         */
        enableEdgeToEdge()
        setContent {
            // Never open the database before the first frame. The launch
            // experience must appear immediately; the saved palette is
            // applied as soon as background hydration completes.
            var themeId by remember { mutableStateOf(DEFAULT_THEME_ID) }
            var followSystem by remember { mutableStateOf(true) }
            var appReady by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                // Let the lightweight branded prelude draw once, then compose
                // the application. The former fixed 850 ms delay made every
                // launch feel slow even on a fast phone and delayed handling
                // notification deep links for no functional reason.
                withFrameNanos { }
                appReady = true
            }

            val resolvedTheme = resolveTheme(themeId, followSystem, isSystemInDarkTheme())
            SideEffect {
                /*
                 * window.statusBarColor and navigationBarColor are deprecated
                 * in API 35 and do nothing there. What still works is the icon
                 * appearance, and that is all a transparent bar needs: the app
                 * is dark behind both bars, so the icons stay light.
                 *
                 * Every screen already applies statusBarsPadding() and
                 * navigationBarsPadding(), so the content sits clear of them.
                 */
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }

            MannaTheme(resolvedTheme) {
                var previewJournal by remember { mutableStateOf(BuildConfig.DEBUG && intent.getBooleanExtra("preview_journal", false)) }
                var previewWelcome by remember { mutableStateOf(BuildConfig.DEBUG && intent.getBooleanExtra("preview_welcome", false)) }
                if (previewJournal) {
                    val previewId = intent.getStringExtra("template") ?: "prayer-morning"
                    if (previewId == "prompts") com.prayerkey.manna.ui.journal.PromptLibrary(
                        onClose = { previewJournal = false }, onPrompt = { _, _ -> },
                    ) else if (previewId == "gallery") com.prayerkey.manna.ui.journal.TemplateGallery(
                        recent = emptyList(), onPick = {}, onClose = { previewJournal = false },
                    ) else com.prayerkey.manna.ui.journal.TemplateSheet(
                        template = com.prayerkey.manna.data.JOURNAL_TEMPLATES.firstOrNull { it.id == previewId }
                            ?: com.prayerkey.manna.data.JOURNAL_TEMPLATES.first(),
                        onClose = { previewJournal = false }, onFinish = { _, _, _, _ -> previewJournal = false },
                    )
                } else if (previewWelcome) {
                    com.prayerkey.manna.ui.screens.GuidedWelcome(preview = true, onBack = { previewWelcome = false }) { _, _ -> previewWelcome = false }
                } else if (appReady) {
                    MannaApp(
                        onThemeChange = { id, follow -> themeId = id; followSystem = follow },
                        notificationRoute = notificationRoute,
                        onNotificationRouteConsumed = { notificationRoute = null },
                    )
                } else {
                    LaunchPrelude(stage = 0)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationRoute = intent.getStringExtra("prayerkey_destination")
    }
}
