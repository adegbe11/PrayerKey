package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.StoreKit.SKStoreReviewController

@Composable
actual fun rememberReviewAsk(): Launcher = remember {
    Launcher { SKStoreReviewController.requestReview() }
}
