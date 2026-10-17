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

/**
 * The status bar's icon colour on iPhone. iOS asks the hosting view
 * controller for it, so the controller reads this (and is told to ask again
 * when it changes) rather than a screen setting it directly.
 */
object StatusBar {
    var onChange: () -> Unit = {}
    var darkIcons: Boolean = true
        set(value) { field = value; onChange() }
}

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
actual fun screenHeightDp(): Int {
    val density = androidx.compose.ui.platform.LocalDensity.current
    return with(density) { androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.height.toDp().value.toInt() }
}
