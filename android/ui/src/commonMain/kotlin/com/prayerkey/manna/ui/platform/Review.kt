package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable

/**
 * Asks for a store review in the store's own sheet, without leaving the app.
 * Only ever called at a moment of peace - after Amen on a milestone - never at
 * launch and never in the middle of a prayer. The stores decide whether the
 * sheet actually appears, and limit how often.
 */
@Composable
expect fun rememberReviewAsk(): Launcher
