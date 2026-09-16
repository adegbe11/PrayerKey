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
                var previewWelcome by remember { mutableStateOf(BuildConfig.DEBUG && intent.getBooleanExtra("preview_welcome", false)) }
                if (previewWelcome) {
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
