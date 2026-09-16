package com.prayerkey.manna.ui.give

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.R
import com.prayerkey.manna.data.PrayerKeyApi
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.launch

/**
 * Giving, on its own page.
 *
 * All of this used to sit on Home, between the day's prayer and the closing
 * verse: an amount field, four presets, a once-or-monthly switch, an error
 * slot and a Stripe button. A checkout form in the middle of a devotional.
 *
 * It was the loudest thing on that screen that was not prayer, and it made the
 * page a page about money for the length of a swipe and a half.
 *
 * Home keeps the invitation - the artwork, the sentence, one button. The asking
 * happens here, where somebody has already decided to look. That is not a
 * smaller ask. A form on a scrolling page is a thing to scroll past; a page is
 * a decision, and people who open it meant to.
 */
@Composable
fun GiveScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var monthly by rememberSaveable { mutableStateOf(false) }
    var amount by rememberSaveable { mutableStateOf("10") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val gift = amount.toIntOrNull()?.takeIf { it in 1..10_000 }

    Column(
        Modifier.fillMaxSize().background(Pk.DeepDark)
            .verticalScroll(rememberScrollState()).navigationBarsPadding(),
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = 300.dp)) {
            Image(
                painterResource(R.drawable.prayerkey_give_artwork),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
            Box(Modifier.matchParentSize().background(Pk.DeepDark.copy(alpha = .48f)))

            Box(
                Modifier.statusBarsPadding().padding(14.dp).size(36.dp)
                    .clip(CircleShape).background(Pk.DeepDark.copy(alpha = .55f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp)) }

            Column(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                Text(
                    "GIVE", color = Pk.GoldLight, fontFamily = UtilitySans,
                    fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 2.sp,
                )
                Text(
                    "Keep\nPrayerKey free.", Modifier.padding(top = 10.dp), color = Color.White,
                    fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                    fontSize = 34.sp, lineHeight = 37.sp,
                )
                Text(
                    "No paywall, for everyone — including the ones who could never pay.",
                    Modifier.padding(top = 12.dp), color = Color.White.copy(alpha = .80f),
                    fontFamily = UtilitySans, fontSize = 13.sp, lineHeight = 20.sp,
                )
            }
        }

        Column(Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
            Row(
                Modifier.fillMaxWidth()
                    .background(Color.White.copy(alpha = .08f), RoundedCornerShape(99.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf(false to "Once", true to "Monthly").forEach { (value, label) ->
                    Surface(
                        onClick = { monthly = value }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(99.dp),
                        color = if (monthly == value) Pk.Oxblood else Color.Transparent,
                    ) {
                        Text(
                            label, Modifier.padding(vertical = 11.dp), textAlign = TextAlign.Center,
                            color = Color.White, fontFamily = UtilitySans,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = .08f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = .16f)),
            ) {
                Row(
                    Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("$", color = Color.White, fontFamily = UtilitySans, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    BasicTextField(
                        value = amount,
                        onValueChange = { next -> if (next.length <= 5 && next.all { it in '0'..'9' }) amount = next },
                        modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = Color.White, fontFamily = UtilitySans, fontSize = 25.sp, fontWeight = FontWeight.Bold),
                    )
                    Text("USD", color = Color.White.copy(alpha = .68f), fontFamily = UtilitySans, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 25, 50).forEach { preset ->
                    Surface(
                        onClick = { amount = preset.toString() }, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = if (amount == preset.toString()) Pk.Oxblood else Color.White.copy(alpha = .07f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = .14f)),
                    ) {
                        Text(
                            "$$preset", Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center,
                            color = Color.White, fontFamily = UtilitySans,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            error?.let {
                Text(
                    it, Modifier.padding(top = 12.dp), color = Pk.GoldLight,
                    fontFamily = UtilitySans, fontSize = 11.sp, lineHeight = 16.sp,
                )
            }

            Button(
                onClick = {
                    val chosen = gift ?: return@Button
                    loading = true; error = null
                    scope.launch {
                        try {
                            val url = Uri.parse(PrayerKeyApi.createDonationCheckout(chosen, monthly))
                            /* Stripe or nothing. Opening whatever the server
                               returned would let a compromised response send
                               somebody to a page that looks like checkout. */
                            require(url.scheme == "https" && url.host == "checkout.stripe.com")
                            context.startActivity(Intent(Intent.ACTION_VIEW, url))
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            error = "We couldn't open secure giving. Please try again shortly. No payment has been confirmed."
                        } finally { loading = false }
                    }
                },
                enabled = gift != null && !loading,
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(54.dp),
                shape = RoundedCornerShape(99.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Pk.Oxblood, contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = .10f),
                ),
            ) {
                Text(
                    if (loading) "Opening Stripe…" else "Give ${gift?.let { "$$it" }.orEmpty()}${if (monthly) " monthly" else ""}",
                    fontFamily = UtilitySans, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                )
                if (!loading) Icon(Icons.Outlined.ArrowForward, null, Modifier.padding(start = 8.dp).size(18.dp))
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Lock, null, tint = Color.White.copy(alpha = .55f), modifier = Modifier.size(14.dp))
                Text(
                    " Secure checkout · Always optional",
                    color = Color.White.copy(alpha = .8f), fontFamily = UtilitySans, fontSize = 11.sp,
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}
