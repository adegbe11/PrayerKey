package com.prayerkey.manna.ui.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

@Composable
actual fun StatusBarIcons(dark: Boolean, inDialog: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, dark, inDialog) {
        val window = if (inDialog) (view.parent as? DialogWindowProvider)?.window else (view.context as? Activity)?.window
        val bars = window?.let { WindowCompat.getInsetsController(it, view) }
        val was = bars?.isAppearanceLightStatusBars
        bars?.isAppearanceLightStatusBars = dark
        onDispose { if (was != null) bars.isAppearanceLightStatusBars = was }
    }
}
