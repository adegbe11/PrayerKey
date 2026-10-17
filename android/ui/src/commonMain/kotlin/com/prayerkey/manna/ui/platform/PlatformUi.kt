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
enum class Haptic { Tick, Tap, Confirm, Reject, LongPress }

fun interface Haptics {
    fun perform(kind: Haptic)
}

@Composable
expect fun rememberHaptics(): Haptics
