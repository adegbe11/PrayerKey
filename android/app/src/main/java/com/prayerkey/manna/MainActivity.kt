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
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.prayerkey.manna.data.DEFAULT_THEME_ID
import com.prayerkey.manna.data.resolveTheme
import com.prayerkey.manna.ui.MannaApp
import com.prayerkey.manna.ui.screens.LaunchPrelude
import com.prayerkey.manna.ui.theme.MannaTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            // Never open the database before the first frame. The launch
            // experience must appear immediately; the saved palette is
            // applied as soon as background hydration completes.
            var themeId by remember { mutableStateOf(DEFAULT_THEME_ID) }
            var followSystem by remember { mutableStateOf(true) }
            var appReady by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(850)
                appReady = true
            }

            val resolvedTheme = resolveTheme(themeId, followSystem, isSystemInDarkTheme())
            SideEffect {
                @Suppress("DEPRECATION")
                window.statusBarColor = android.graphics.Color.rgb(18, 22, 31)
                @Suppress("DEPRECATION")
                window.navigationBarColor = android.graphics.Color.rgb(18, 22, 31)
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }

            MannaTheme(resolvedTheme) {
                if (appReady) {
                    MannaApp(
                        onThemeChange = { id, follow -> themeId = id; followSystem = follow },
                    )
                } else {
                    LaunchPrelude(stage = 0)
                }
            }
        }
    }
}
