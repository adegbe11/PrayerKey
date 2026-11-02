package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable
import com.prayerkey.manna.platform.PlatformContext

/**
 * What a screen reached for through LocalContext on Android: the context the
 * stores and loaders take. On Android it is the same Context; on iPhone, the
 * one app object.
 */
@Composable
expect fun platformContext(): PlatformContext

/** The handful of feels the app uses. Android's view haptics; the Taptic Engine on iPhone. */
enum class Haptic { Tick, Tap, Click, Confirm, Reject, LongPress }

fun interface Haptics {
    fun perform(kind: Haptic)
}

@Composable
expect fun rememberHaptics(): Haptics

/** Whether frosted glass can blur what is behind it. Android 12 and up; always on iPhone. */
expect val supportsBackdropBlur: Boolean

/** Text without the extra font padding Android adds above and below; other platforms never add it. */
expect val noFontPadding: androidx.compose.ui.text.PlatformTextStyle?

/** The screen's height in dp, as the screens measured it on Android. */
@Composable
expect fun screenHeightDp(): Int

/** A dialog that covers the whole screen, edge to edge: no platform width, no system insets of its own. */
expect fun fullScreenDialog(): androidx.compose.ui.window.DialogProperties

/** Keeps this screen out of screenshots and the recent-apps preview while [enabled]. Android only; iOS gives apps no way to block a screenshot. */
@Composable
expect fun SecureScreen(enabled: Boolean)
