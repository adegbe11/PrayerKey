package com.prayerkey.manna.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.R
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    var error by remember { mutableStateOf(false) }
    fun account(path: String) {
        error = runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.prayerkey.com/$path")))
        }.isFailure
    }
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
            Icon(painterResource(R.drawable.ic_notification), contentDescription = "PrayerKey", tint = Pk.GoldLight, modifier = Modifier.size(48.dp))
            Text("PrayerKey", fontFamily = DisplaySerif, fontSize = 28.sp, color = Color.White)
            Spacer(Modifier.height(28.dp))
            Text("A closer walk\nwith God.", color = Color.White, fontFamily = DisplaySerif,
                fontSize = 38.sp, lineHeight = 41.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
            Text("Prayer, Scripture, and space to reflect.", Modifier.padding(top = 14.dp), color = Color.White.copy(alpha = .85f),
                fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height((availableHeight - 570.dp).coerceAtLeast(100.dp)))
            WelcomeAction("Create an account", primary = true) { account("register") }
            Spacer(Modifier.height(10.dp))
            WelcomeAction("Sign in", primary = false) { account("login") }
            TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Not now", color = Color.White.copy(alpha = .85f), fontFamily = UtilitySans, fontSize = 14.sp)
            }
            if (error) Text("Couldn't open the account page. Please try again.", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
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
