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
