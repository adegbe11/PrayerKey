package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.prayerkey.manna.platform.AppContext
import com.prayerkey.manna.platform.PlatformContext
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

@Composable
actual fun platformContext(): PlatformContext = AppContext

@Composable
actual fun rememberHaptics(): Haptics = remember {
    Haptics { kind ->
        when (kind) {
            Haptic.Tick -> UISelectionFeedbackGenerator().selectionChanged()
            Haptic.Click -> UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleSoft).impactOccurred()
            Haptic.Tap -> UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
            Haptic.LongPress -> UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
            Haptic.Confirm -> UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
            Haptic.Reject -> UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
        }
    }
}

actual val supportsBackdropBlur: Boolean = true

actual val noFontPadding: androidx.compose.ui.text.PlatformTextStyle? = null
