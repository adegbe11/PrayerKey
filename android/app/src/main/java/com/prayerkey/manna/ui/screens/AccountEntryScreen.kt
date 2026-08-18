package com.prayerkey.manna.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans

@Composable
fun AccountEntryScreen(onContinue: (name: String, email: String, kind: String) -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    BackHandler(enabled = page != 0) { page = 0; error = "" }

    Box(Modifier.fillMaxSize().background(Pk.DeepDark).statusBarsPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(72.dp).background(Pk.Oxblood, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Key, null, tint = Pk.GoldLight, modifier = Modifier.size(34.dp))
            }
            Text("PRAYERKEY", Modifier.padding(top = 18.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = 1.4.sp, color = Color.White)
            Text(
                when (page) { 1 -> "Create your account"; 2 -> "Welcome back"; else -> "Keep your prayers with you" },
                Modifier.padding(top = 50.dp).fillMaxWidth(), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 39.sp, color = Color.White,
            )
            Text(
                if (page == 0) "Save your prayer history, notes and progress on this phone. You can connect an online account later."
                else if (page == 1) "Your name and email identify your PrayerKey profile. Your prayer notes stay private on this phone."
                else "Sign in when PrayerKey cloud sync is available. For now, continue with your private profile.",
                Modifier.padding(top = 14.dp).fillMaxWidth(), fontFamily = UtilitySans, fontSize = 16.sp, lineHeight = 24.sp, color = Color.White.copy(alpha = .72f),
            )
            if (page != 0) {
                if (page == 1) AccountField(name, { name = it }, "Your name")
                AccountField(email, { email = it }, "Email")
                OutlinedTextField(
                    password, { password = it }, Modifier.fillMaxWidth().padding(top = 12.dp),
                    label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
                    colors = accountFieldColors(), shape = RoundedCornerShape(17.dp),
                )
                if (error.isNotBlank()) Text(error, Modifier.fillMaxWidth().padding(top = 10.dp), color = Color(0xFFFFB4AB), fontSize = 13.sp)
            }
            Spacer(Modifier.height(28.dp))
            when (page) {
                0 -> {
                    AccountButton("CREATE ACCOUNT") { page = 1 }
                    OutlinedButton({ page = 2 }, Modifier.fillMaxWidth().padding(top = 12.dp).height(56.dp), shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .28f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("SIGN IN", fontWeight = FontWeight.Bold) }
                    TextButton({ onContinue("", "", "private") }, Modifier.padding(top = 12.dp)) { Text("CONTINUE PRIVATELY", color = Pk.GoldLight, fontWeight = FontWeight.Bold) }
                }
                1 -> AccountButton("CREATE MY PROFILE") {
                    when {
                        name.trim().length < 2 -> error = "Enter your name."
                        !email.contains("@") -> error = "Enter a valid email address."
                        password.length < 6 -> error = "Use at least 6 characters for your password."
                        else -> onContinue(name.trim(), email.trim(), "local")
                    }
                }
                else -> {
                    AccountButton("CONTINUE WITH PRIVATE PROFILE") { onContinue("", email.trim(), "private") }
                    TextButton({ page = 0 }, Modifier.padding(top = 10.dp)) { Text("BACK", color = Pk.GoldLight) }
                }
            }
            Text("PrayerKey never stores your password in the app.", Modifier.padding(top = 30.dp), fontSize = 11.sp, color = Color.White.copy(alpha = .48f))
        }
    }
}

@Composable private fun AccountField(value: String, change: (String) -> Unit, label: String) {
    OutlinedTextField(value, change, Modifier.fillMaxWidth().padding(top = 22.dp), label = { Text(label) }, singleLine = true, colors = accountFieldColors(), shape = RoundedCornerShape(17.dp))
}

@Composable private fun accountFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = Pk.GoldLight, unfocusedBorderColor = Color.White.copy(alpha = .25f),
    focusedLabelColor = Pk.GoldLight, unfocusedLabelColor = Color.White.copy(alpha = .58f),
    cursorColor = Pk.GoldLight,
)

@Composable private fun AccountButton(label: String, click: () -> Unit) {
    Button(click, Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood, contentColor = Color.White)) { Text(label, fontWeight = FontWeight.Bold) }
}
