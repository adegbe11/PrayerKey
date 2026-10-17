package com.prayerkey.manna.ui.screens

import com.prayerkey.manna.resources.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import com.prayerkey.manna.ui.theme.drift
import com.prayerkey.manna.ui.theme.pressable
import com.prayerkey.manna.ui.theme.rise
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
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans

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
        // the dawn drifts slowly closer while you read
        Image(painterResource(Res.drawable.welcome_prayer_dawn), null, Modifier.matchParentSize().drift(), contentScale = ContentScale.Crop)
        Box(Modifier.matchParentSize().background(Pk.DeepDark.copy(alpha = .18f)))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .safeDrawingPadding().padding(horizontal = 28.dp)
                .heightIn(min = (availableHeight - 70.dp).coerceAtLeast(520.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))
            /* The mark itself, lifted from the store icon.
               Not a redrawing of it. Two approximations had been standing in
               for the logo - the notification silhouette, and goldKey, which
               draws a cross-topped key. The real mark is neither: it is a
               praying figure whose robe forms the bow of a key and whose body
               forms the shank. That is not a shape worth reconstructing in
               path commands, so it is the artwork, cut off its violet tile. */
            Image(
                painterResource(Res.drawable.prayerkey_mark),
                contentDescription = "PrayerKey",
                modifier = Modifier.size(72.dp).rise(0, distance = 14f),
            )
            Text("PrayerKey", Modifier.rise(1), fontFamily = DisplaySerif, fontSize = 28.sp, color = Color.White)
            Spacer(Modifier.height(28.dp))
            Text("A closer walk\nwith God.", Modifier.rise(4, distance = 34f), color = Color.White, fontFamily = DisplaySerif,
                fontSize = 38.sp, lineHeight = 41.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
            Text("Prayer, Scripture, and space to reflect.", Modifier.padding(top = 14.dp).rise(6), color = Color.White.copy(alpha = .85f),
                fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height((availableHeight - 570.dp).coerceAtLeast(100.dp)))
            Box(Modifier.rise(9, distance = 40f).pressable()) { WelcomeAction("Get started", primary = true, onClick = onContinue) }
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
