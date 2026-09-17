package com.prayerkey.manna.ui.challenge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.prayerkey.manna.data.ChallengePlan
import com.prayerkey.manna.data.Challenges
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The two ways into a challenge from anywhere else in the app.
 *
 * They exist so the old entry points — Midnight prayer, Warfare prayer, Start a
 * fast, Prayer challenge — can be pointed at written plans without every caller
 * having to load the asset and hold the state itself.
 */

/** Opens one plan by id, at the day you had reached. */
@Composable
fun ChallengeById(id: String, onClose: () -> Unit) {
    val context = LocalContext.current
    var plan by remember(id) { mutableStateOf<ChallengePlan?>(null) }
    LaunchedEffect(id) { plan = withContext(Dispatchers.IO) { Challenges.byId(context, id) } }

    /* Always fill the screen, even before the asset has parsed. A composable
       that occupies nothing inside a Dialog turns the reader's first tap into
       a tap outside it, which closes the screen instead of turning the page. */
    val loaded = plan
    if (loaded == null) ChallengeLoading() else ChallengeDayScreen(loaded, pushed = false, onClose = onClose)
}

/** The shelf, then whichever plan is chosen from it. */
@Composable
fun ChallengeShelfRoute(onClose: () -> Unit) {
    var chosen by remember { mutableStateOf<ChallengePlan?>(null) }
    val plan = chosen
    if (plan == null) {
        ChallengeShelf(onOpen = { chosen = it }, onClose = onClose)
    } else {
        ChallengeDayScreen(plan, pushed = true, onClose = { chosen = null })
    }
}
