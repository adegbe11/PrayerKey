package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable

/**
 * Speaking a journal entry instead of typing it. Android hands this to the
 * phone's speech service and its own listening dialog; iPhone listens with
 * Apple's recogniser and shows the words as they come. Either way only the
 * words come back; no recording is kept.
 */
@Composable
expect fun rememberDictation(prompt: String, onResult: (String) -> Unit): Launcher
