package com.prayerkey.manna.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.prayerkey.manna.ui.platform.StatusBar

@Composable
actual fun BarIcons(dark: Boolean) {
    DisposableEffect(dark) {
        val before = StatusBar.darkIcons
        StatusBar.darkIcons = dark
        onDispose { StatusBar.darkIcons = before }
    }
}
