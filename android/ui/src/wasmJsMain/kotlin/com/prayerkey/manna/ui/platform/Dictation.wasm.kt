package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable

// The web build is a compile check only.
@Composable
actual fun rememberDictation(prompt: String, onResult: (String) -> Unit): Launcher = Launcher { }
