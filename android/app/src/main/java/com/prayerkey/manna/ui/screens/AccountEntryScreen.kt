package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.auth.AuthResult
import com.prayerkey.manna.auth.SecureAuthStore
import com.prayerkey.manna.auth.SupabaseAuthClient
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.launch

private enum class AccountMode { SIGN_UP, SIGN_IN }

@Composable
fun AccountEntryScreen(onContinue: (name: String, email: String, kind: String) -> Unit) {
    val context = LocalContext.current
    val client = remember { SupabaseAuthClient() }
    val secureStore = remember { SecureAuthStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(AccountMode.SIGN_UP) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun submit() {
        message = when {
            mode == AccountMode.SIGN_UP && name.trim().length < 2 -> "Enter your name."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email address."
            password.length < 8 -> "Use at least 8 characters for your password."
            else -> null
        }
        if (message != null) return
        loading = true
        scope.launch {
            val result = if (mode == AccountMode.SIGN_UP) client.signUp(name, email, password) else client.signIn(email, password)
            loading = false
            when (result) {
                is AuthResult.SignedIn -> {
                    secureStore.save(result.session)
                    onContinue(name.trim(), result.session.email, "supabase")
                }
                is AuthResult.ConfirmEmail -> message = "Check ${result.email} and confirm your email. Then sign in."
                is AuthResult.Failure -> message = result.message
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding().navigationBarsPadding()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(58.dp).clip(CircleShape).background(Pk.Oxblood), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Key, null, tint = Pk.GoldLight, modifier = Modifier.size(27.dp))
            }
            Text("PRAYERKEY", Modifier.padding(top = 13.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 1.5.sp, color = Pk.Charcoal)
            Text(
                if (mode == AccountMode.SIGN_UP) "Keep your prayers safe" else "Welcome back",
                Modifier.fillMaxWidth().padding(top = 31.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, color = Pk.Charcoal,
            )
            Text(
                if (mode == AccountMode.SIGN_UP) "Save your prayer history, challenges, journal, and answered prayers across your devices." else "Continue your prayer journey.",
                Modifier.fillMaxWidth().padding(top = 9.dp),
                fontFamily = UtilitySans, fontSize = 13.sp, lineHeight = 19.sp, color = Pk.Muted,
            )

            Row(Modifier.fillMaxWidth().padding(top = 20.dp).background(Pk.Charcoal.copy(alpha = .06f), RoundedCornerShape(16.dp)).padding(4.dp)) {
                AccountTab("SIGN UP", mode == AccountMode.SIGN_UP, Modifier.weight(1f)) { mode = AccountMode.SIGN_UP; message = null }
                AccountTab("SIGN IN", mode == AccountMode.SIGN_IN, Modifier.weight(1f)) { mode = AccountMode.SIGN_IN; message = null }
            }

            if (mode == AccountMode.SIGN_UP) {
                AccountField(name, { name = it }, "Your name", Icons.Outlined.Person, Modifier.padding(top = 18.dp))
            }
            AccountField(email, { email = it }, "Email", Icons.Outlined.Email, Modifier.padding(top = if (mode == AccountMode.SIGN_UP) 11.dp else 18.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
                colors = accountFieldColors(),
            )

            message?.let {
                Text(it, Modifier.fillMaxWidth().padding(top = 12.dp), fontFamily = UtilitySans, fontSize = 12.sp, lineHeight = 18.sp, color = if (it.startsWith("Check")) Pk.Charcoal else MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (client.configured) submit()
                    else message = "Online accounts are not available right now. You can continue privately on this device."
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood, contentColor = Color.White),
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(21.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(if (mode == AccountMode.SIGN_UP) "CREATE MY ACCOUNT" else "SIGN IN", fontWeight = FontWeight.Bold, letterSpacing = .5.sp)
            }

            if (mode == AccountMode.SIGN_IN) {
                Text(
                    "Forgot password?",
                    Modifier.padding(top = 17.dp).clickable {
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) message = "Enter your email address first."
                        else scope.launch {
                            loading = true
                            val result = client.resetPassword(email)
                            loading = false
                            message = if (result is AuthResult.ConfirmEmail) "Password reset instructions were sent to ${result.email}." else (result as AuthResult.Failure).message
                        }
                    },
                    color = Pk.Oxblood, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                )
            }

            Spacer(Modifier.weight(1f))
            Text(
                "Keep everything on this device",
                Modifier.padding(top = 14.dp, bottom = 8.dp).clickable { onContinue(name.trim(), email.trim(), "private") }.padding(8.dp),
                color = Pk.Charcoal, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
            )
            Text("By continuing, you agree to PrayerKey's Terms and Privacy Policy.", color = Pk.Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun AccountTab(label: String, selected: Boolean, modifier: Modifier, click: () -> Unit) {
    Surface(onClick = click, modifier = modifier, shape = RoundedCornerShape(13.dp), color = if (selected) Color.White else Color.Transparent) {
        Text(label, Modifier.padding(vertical = 12.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = if (selected) Pk.Oxblood else Pk.Muted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun AccountField(value: String, change: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    OutlinedTextField(value, change, modifier.fillMaxWidth(), label = { Text(label) }, leadingIcon = { Icon(icon, null) }, singleLine = true, shape = RoundedCornerShape(17.dp), colors = accountFieldColors())
}

@Composable
private fun accountFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Pk.Oxblood,
    focusedLabelColor = Pk.Oxblood,
    cursorColor = Pk.Oxblood,
    focusedLeadingIconColor = Pk.Oxblood,
)
