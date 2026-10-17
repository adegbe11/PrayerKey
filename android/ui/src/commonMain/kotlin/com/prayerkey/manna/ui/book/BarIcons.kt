package com.prayerkey.manna.ui.book

import androidx.compose.runtime.Composable

/**
 * Light or dark clock and battery over this screen, restored when it closes.
 * The app draws behind the system bars, so only the icons' colour is ours.
 */
@Composable
expect fun BarIcons(dark: Boolean)
