package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable

/**
 * Dark or light clock and battery icons while this is on screen, put back
 * when it leaves. [inDialog] is for full-screen dialogs, which on Android have
 * a window of their own.
 */
@Composable
expect fun StatusBarIcons(dark: Boolean, inDialog: Boolean = false)
