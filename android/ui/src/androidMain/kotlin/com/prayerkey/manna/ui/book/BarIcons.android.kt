package com.prayerkey.manna.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light or dark clock and battery over this screen, restored when it closes.
 *
 * The app draws behind the system bars, so the bars have no colour of their
 * own to set - only their icons, which have to read against whatever is drawn
 * beneath them. These screens used to set window.statusBarColor, which is
 * deprecated and ignored from Android 15, and which Play flags.
 */
@Composable
actual fun BarIcons(dark: Boolean) {
    val view = LocalView.current
    DisposableEffect(dark) {
        val window = (view.context as? android.app.Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val beforeStatus = controller?.isAppearanceLightStatusBars
        val beforeNav = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = dark
        controller?.isAppearanceLightNavigationBars = dark
        onDispose {
            if (beforeStatus != null) controller.isAppearanceLightStatusBars = beforeStatus
            if (beforeNav != null) controller.isAppearanceLightNavigationBars = beforeNav
        }
    }
}
