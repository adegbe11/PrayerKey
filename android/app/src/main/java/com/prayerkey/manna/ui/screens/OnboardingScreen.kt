package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.AppleGray
import com.prayerkey.manna.ui.theme.Canvas
import com.prayerkey.manna.ui.theme.ElectricGloss
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Hairline
import com.prayerkey.manna.ui.theme.Ink
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.Night
import com.prayerkey.manna.ui.theme.NightGloss
import com.prayerkey.manna.ui.theme.TopSheen

/**
 * The first thing a stranger sees. Names the app, teaches the one
 * gesture in a single line, and asks for their name — so Home never
 * greets anyone with a hardcoded stranger's name again.
 */
@Composable
fun OnboardingScreen(onDone: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().background(Canvas).padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(.7f))

        /* the card, in miniature — the object the whole app revolves around */
        Box(
            Modifier.fillMaxWidth(.62f).height(240.dp)
                .shadow(26.dp, RoundedCornerShape(24.dp), spotColor = Night.copy(alpha = .4f))
                .clip(RoundedCornerShape(24.dp)).background(NightGloss),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.fillMaxSize().background(TopSheen))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚿", color = Gold, fontSize = 38.sp)
                Spacer(Modifier.height(12.dp))
                Text("MANNA", color = Color.White, fontSize = 13.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Medium)
                Text("FRESH EVERY MORNING", color = Gold, fontSize = 8.sp, letterSpacing = 1.6.sp, modifier = Modifier.padding(top = 7.dp))
            }
            Icon(
                Icons.Outlined.KeyboardArrowDown, null, tint = Gold,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp).size(22.dp),
            )
        }

        Spacer(Modifier.height(34.dp))
        Text(
            "Pull your word\ndown from above.",
            fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
            fontSize = 30.sp, lineHeight = 37.sp, textAlign = TextAlign.Center, color = Ink,
        )
        Text(
            "One verse waits for you every morning.\nPull it down. Push it up to keep it.",
            color = Muted, fontSize = 14.sp, lineHeight = 21.sp,
            textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp),
        )

        Spacer(Modifier.weight(.5f))

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("What should we call you?", fontSize = 15.sp, color = Muted) },
            singleLine = true, shape = RoundedCornerShape(17.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold, unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = AppleGray, unfocusedContainerColor = AppleGray,
            ),
        )

        val ready = name.isNotBlank()
        Box(
            Modifier.fillMaxWidth().padding(top = 12.dp).height(56.dp)
                .shadow(if (ready) 14.dp else 0.dp, RoundedCornerShape(17.dp), spotColor = Night.copy(alpha = .4f))
                .clip(RoundedCornerShape(17.dp))
                .background(if (ready) NightGloss else androidx.compose.ui.graphics.SolidColor(Color(0xFFD9D9DE)))
                .border(0.5.dp, Color.White.copy(alpha = .3f), RoundedCornerShape(17.dp))
                .clickable(enabled = ready) { onDone(name.trim()) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Receive my first word",
                color = if (ready) Gold else Color.White,
                fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 26.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("Free forever · No account · Works offline", color = Muted, fontSize = 11.sp)
        }
    }
}
