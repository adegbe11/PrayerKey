package com.prayerkey.manna.ui.platform

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.prayerkey.manna.platform.PlatformContext

@Composable
actual fun platformContext(): PlatformContext = LocalContext.current

@Composable
actual fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) {
        Haptics { kind ->
            val constant = when (kind) {
                Haptic.Tick -> HapticFeedbackConstants.CLOCK_TICK
                Haptic.Tap -> HapticFeedbackConstants.VIRTUAL_KEY
                Haptic.Click -> HapticFeedbackConstants.CONTEXT_CLICK
                Haptic.LongPress -> HapticFeedbackConstants.LONG_PRESS
                Haptic.Confirm -> if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY
                Haptic.Reject -> if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.LONG_PRESS
            }
            view.performHapticFeedback(constant)
        }
    }
}

actual val supportsBackdropBlur: Boolean = Build.VERSION.SDK_INT >= 31

actual val noFontPadding: androidx.compose.ui.text.PlatformTextStyle? = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)

@Composable
actual fun screenHeightDp(): Int = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp
