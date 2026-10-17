package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
actual fun StatusBarIcons(dark: Boolean, inDialog: Boolean) {
    DisposableEffect(dark) {
        val before = StatusBar.darkIcons
        StatusBar.darkIcons = dark
        onDispose { StatusBar.darkIcons = before }
    }
}
