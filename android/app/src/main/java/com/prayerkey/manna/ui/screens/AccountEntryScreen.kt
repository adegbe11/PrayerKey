package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans

@Composable
fun AccountEntryScreen(onContinue: (name: String, email: String, kind: String) -> Unit) {
    Box(Modifier.fillMaxSize().background(Pk.DeepDark).statusBarsPadding()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(72.dp).background(Pk.Oxblood, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Key, null, tint = Pk.GoldLight, modifier = Modifier.size(34.dp))
            }
            Text("PRAYERKEY", Modifier.padding(top = 18.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = 1.4.sp, color = Color.White)
            Text(
                "Keep your prayers with you",
                Modifier.padding(top = 50.dp).fillMaxWidth(), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 39.sp, color = Color.White,
            )
            Text(
                "Your prayers, journal, notes and progress stay privately on this device. No account is required.",
                Modifier.padding(top = 14.dp).fillMaxWidth(), fontFamily = UtilitySans, fontSize = 16.sp, lineHeight = 24.sp, color = Color.White.copy(alpha = .72f),
            )
            Spacer(Modifier.weight(1f))
            AccountButton("CONTINUE ON THIS DEVICE") { onContinue("", "", "private") }
            Text(
                "You can export a private backup from Settings at any time.",
                Modifier.padding(top = 20.dp, bottom = 18.dp),
                fontSize = 11.sp,
                lineHeight = 17.sp,
                color = Color.White.copy(alpha = .48f),
            )
        }
    }
}

@Composable private fun AccountButton(label: String, click: () -> Unit) {
    Button(click, Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood, contentColor = Color.White)) { Text(label, fontWeight = FontWeight.Bold) }
}
