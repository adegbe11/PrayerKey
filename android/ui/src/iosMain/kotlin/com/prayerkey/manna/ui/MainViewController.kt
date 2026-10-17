package com.prayerkey.manna.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.prayerkey.manna.data.DEFAULT_THEME_ID
import com.prayerkey.manna.data.resolveTheme
import com.prayerkey.manna.ui.platform.PlatformOverlay
import com.prayerkey.manna.ui.screens.LaunchPrelude
import com.prayerkey.manna.ui.theme.MannaTheme
import platform.UIKit.UIViewController

/** Where a tapped notification asked to go; the app consumes it once it is ready. */
object NotificationRoutes {
    var pending by mutableStateOf<String?>(null)

    fun open(route: String) {
        pending = route
    }
}

/**
 * The iPhone's MainActivity: the theme, the launch prelude for one frame, then
 * the app - the same steps Android takes, with the overlay iOS draws itself
 * (toasts, dictation, pickers) on top.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    var themeId by remember { mutableStateOf(DEFAULT_THEME_ID) }
    var followSystem by remember { mutableStateOf(true) }
    var appReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // the prelude draws once, then the app composes
        withFrameNanos { }
        appReady = true
    }
    val theme = resolveTheme(themeId, followSystem, isSystemInDarkTheme())
    MannaTheme(theme) {
        Box(Modifier.fillMaxSize()) {
            if (appReady) {
                MannaApp(
                    onThemeChange = { id, follow -> themeId = id; followSystem = follow },
                    notificationRoute = NotificationRoutes.pending,
                    onNotificationRouteConsumed = { NotificationRoutes.pending = null },
                )
            } else {
                LaunchPrelude(stage = 0)
            }
            PlatformOverlay()
        }
    }
}
