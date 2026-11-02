package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable
import com.prayerkey.manna.platform.AppContext
import com.prayerkey.manna.platform.PlatformContext

// The web build is a compile check only.
@Composable
actual fun platformContext(): PlatformContext = AppContext

@Composable
actual fun rememberHaptics(): Haptics = Haptics { }

actual val supportsBackdropBlur: Boolean = true

actual val noFontPadding: androidx.compose.ui.text.PlatformTextStyle? = null

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
actual fun screenHeightDp(): Int {
    val density = androidx.compose.ui.platform.LocalDensity.current
    return with(density) { androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.height.toDp().value.toInt() }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
actual fun fullScreenDialog(): androidx.compose.ui.window.DialogProperties =
    androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, usePlatformInsets = false)

@Composable
actual fun SecureScreen(enabled: Boolean) = Unit
