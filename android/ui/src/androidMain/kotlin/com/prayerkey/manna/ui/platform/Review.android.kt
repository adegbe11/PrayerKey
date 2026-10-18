package com.prayerkey.manna.ui.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManagerFactory

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

@Composable
actual fun rememberReviewAsk(): Launcher {
    val context = LocalContext.current
    return remember(context) {
        Launcher {
            val activity = context.activity() ?: return@Launcher
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { request ->
                if (request.isSuccessful) manager.launchReviewFlow(activity, request.result)
            }
        }
    }
}
