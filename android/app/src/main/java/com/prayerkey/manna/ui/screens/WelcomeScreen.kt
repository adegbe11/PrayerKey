package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.R
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import com.prayerkey.manna.ui.theme.goldKey

/**
 * The first screen, and now only a door.
 *
 * It used to open with "Create an account", "Sign in", and beneath them, in
 * quiet grey, "Not now" - which was the only one of the three that led into
 * the app. The other two left it, opening prayerkey.com in a browser.
 *
 * PrayerKey has no accounts. There is no token, no session, no user: the
 * journal, the streak and the saved prayers live in this install and nowhere
 * else. So signing in bought nothing and returned you to the same screen, and
 * the offer carried a suggestion it could not keep - that your writing was
 * being kept somewhere safe, and would follow you to a new phone.
 *
 * A gate that guards nothing still costs a decision, and this one asked for it
 * before anybody had seen a single verse.
 */
@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Pk.DeepDark)) {
        val availableHeight = maxHeight
        Image(painterResource(R.drawable.welcome_prayer_dawn), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        Box(Modifier.matchParentSize().background(Pk.DeepDark.copy(alpha = .18f)))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .safeDrawingPadding().padding(horizontal = 28.dp)
                .heightIn(min = (availableHeight - 70.dp).coerceAtLeast(520.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))
            /* The mark, drawn the way the splash draws it.
               This was ic_notification - the status-bar icon, which Android
               reduces to a flat alpha silhouette because that is all it will
               render at 18 px. Blown up to 48dp and tinted gold it was a
               different symbol entirely, a praying figure rather than the
               cross-topped key, and a crude one. goldKey is the brand mark and
               the same call the launch screen makes, so the icon a person sees
               opening the app is now the icon they saw a second earlier. */
            Canvas(Modifier.size(66.dp)) { goldKey(center, size.height) }
            Text("PrayerKey", fontFamily = DisplaySerif, fontSize = 28.sp, color = Color.White)
            Spacer(Modifier.height(28.dp))
            Text("A closer walk\nwith God.", color = Color.White, fontFamily = DisplaySerif,
                fontSize = 38.sp, lineHeight = 41.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
            Text("Prayer, Scripture, and space to reflect.", Modifier.padding(top = 14.dp), color = Color.White.copy(alpha = .85f),
                fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height((availableHeight - 570.dp).coerceAtLeast(100.dp)))
            WelcomeAction("Get started", primary = true, onClick = onContinue)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun WelcomeAction(label: String, primary: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(99.dp), color = if (primary) Pk.Oxblood else Pk.DeepDark.copy(alpha = .65f),
        border = if (primary) null else BorderStroke(1.dp, Color.White.copy(alpha = .4f))) {
        Box(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), contentAlignment = Alignment.Center) {
            Text(label, Modifier.padding(horizontal = 20.dp), color = Color.White, fontFamily = UtilitySans, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.align(Alignment.CenterEnd).size(18.dp), tint = Color.White)
        }
    }
}
