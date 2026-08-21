package com.prayerkey.manna.ui.home

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.reminder.FastingMilestoneReceiver
import com.prayerkey.manna.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

private data class FastChoice(val name: String, val hours: Int, val note: String)
private val fastChoices = listOf(
    FastChoice("Sunrise to sunset", 12, "Water and a simple evening meal"),
    FastChoice("24-hour water fast", 24, "Water throughout the fast"),
    FastChoice("Daniel fast", 24 * 21, "Simple plant-based meals for 21 days"),
    FastChoice("Esther fast", 24 * 3, "A demanding three-day fast"),
    FastChoice("Custom fast", 6, "Choose a safe number of hours"),
)

@Composable internal fun FastingChallengeMachine(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fasting_challenge", Context.MODE_PRIVATE) }
    var selected by remember { mutableStateOf(fastChoices.first { it.name == prefs.getString("type", it.name) }) }
    var startedAt by remember { mutableLongStateOf(prefs.getLong("started_at", 0L)) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var rescueOpen by remember { mutableStateOf(false) }
    LaunchedEffect(startedAt) { while (startedAt > 0L) { now = System.currentTimeMillis(); delay(1_000) } }
    val totalMs = selected.hours * 3_600_000L
    val elapsed = if (startedAt == 0L) 0L else (now - startedAt).coerceIn(0L, totalMs)
    val remaining = (totalMs - elapsed).coerceAtLeast(0L)
    val progress = if (totalMs == 0L) 0f else elapsed.toFloat() / totalMs

    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Charcoal, modifier = Modifier.size(16.dp)) }
                Text("FASTING", Modifier.weight(1f), textAlign = TextAlign.Center, color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.width(48.dp))
            }
            if (startedAt == 0L) {
                Text("Choose your fast", fontFamily = DisplaySerif, fontSize = 34.sp, color = Pk.Charcoal, modifier = Modifier.padding(top = 18.dp, bottom = 18.dp))
                fastChoices.forEach { choice ->
                    Surface(
                        Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable { selected = choice },
                        shape = RoundedCornerShape(20.dp), color = if (choice == selected) Pk.Oxblood else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (choice == selected) Pk.Oxblood else Pk.Hair),
                    ) { Column(Modifier.padding(17.dp)) { Text(choice.name, color = if (choice == selected) Pk.Crisp else Pk.Charcoal, fontWeight = FontWeight.SemiBold); Text(choice.note, color = if (choice == selected) Pk.Crisp.copy(.65f) else Pk.Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp)) } }
                }
                Text("If you are pregnant, under 18, have diabetes, take medication, or have an eating-disorder history, speak with a qualified clinician first. A non-food fast is always valid.", color = Pk.Muted, fontSize = 11.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 12.dp))
                Spacer(Modifier.weight(1f))
                Button(onClick = {
                    startedAt = System.currentTimeMillis(); prefs.edit().putString("type", selected.name).putLong("started_at", startedAt).apply()
                    FastingMilestoneReceiver.schedule(context, startedAt, selected.hours)
                }, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) { Text("Start my fast") }
            } else {
                Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(250.dp), strokeWidth = 15.dp, color = Pk.Oxblood, trackColor = Pk.Oxblood.copy(.10f), strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.WaterDrop, null, tint = Pk.Oxblood)
                        Text(formatRemaining(remaining), fontFamily = DisplaySerif, fontSize = 37.sp, color = Pk.Charcoal, modifier = Modifier.padding(top = 10.dp))
                        Text("REMAINING", color = Pk.Muted, fontSize = 9.sp, letterSpacing = 1.3.sp)
                    }
                }
                Text(selected.name, fontFamily = DisplaySerif, fontSize = 27.sp, color = Pk.Charcoal, modifier = Modifier.padding(top = 30.dp))
                val hour = (elapsed / 3_600_000L).toInt()
                Surface(Modifier.fillMaxWidth().padding(top = 14.dp), shape = RoundedCornerShape(20.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair)) {
                    Column(Modifier.padding(18.dp)) {
                        Text("HOUR $hour", color = Pk.GoldDeep, fontSize = 9.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
                        Text("PRAYER FOR THIS HOUR", color = Pk.Oxblood, fontSize = 9.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        Text(fastingPrayer(hour), fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 26.sp, modifier = Modifier.padding(top = 9.dp))
                    }
                }
                Button(onClick = { rescueOpen = true }, Modifier.fillMaxWidth().padding(top = 18.dp).height(60.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.VioletDeep)) {
                    Icon(Icons.Outlined.Bolt, null); Spacer(Modifier.width(9.dp)); Text("Pray for strength", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                if (remaining == 0L) Button(onClick = { startedAt = 0L; prefs.edit().remove("started_at").apply() }, Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) { Icon(Icons.Outlined.Check, null); Spacer(Modifier.width(8.dp)); Text("Complete fast") }
            }
        }
    }
    if (rescueOpen) HungerRescue(onClose = { rescueOpen = false })
}

@Composable private fun HungerRescue(onClose: () -> Unit) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val prayer = "Father, meet me in this moment of weakness. Let this hunger remind me that You are my true strength. Steady my body, clear my mind, and help me finish this fast wisely and faithfully. Give me grace for the next minute. Amen."
    DisposableEffect(Unit) { onDispose { tts?.stop(); tts?.shutdown() } }
    AlertDialog(onDismissRequest = onClose, title = { Text("One minute with God", fontFamily = DisplaySerif) }, text = { Text(prayer, fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 27.sp) }, confirmButton = {
        TextButton(onClick = { tts = TextToSpeech(context) { status -> if (status == TextToSpeech.SUCCESS) { tts?.language = Locale.US; tts?.setSpeechRate(.84f); tts?.speak(prayer, TextToSpeech.QUEUE_FLUSH, null, "hunger-rescue") } } }) { Text("Listen to prayer") }
    }, dismissButton = { TextButton(onClick = onClose) { Text("Close") } })
}

private fun formatRemaining(ms: Long): String {
    val totalMinutes = ms / 60_000L; val hours = totalMinutes / 60; val minutes = totalMinutes % 60
    return "%02d:%02d".format(Locale.US, hours, minutes)
}

private fun fastingPrayer(hour: Int): String = when {
    hour < 4 -> "Father, I set apart this time for You. Purify my motives, quiet every distraction, and let this fast draw me nearer to Your presence. Give me grace to seek You with sincerity and faith. In Jesus' name, Amen."
    hour < 8 -> "Lord, as my body feels hunger, awaken a deeper hunger for Your Word and Your presence. Strengthen me to continue wisely, and let every appetite come under the rule of Your Spirit. In Jesus' name, Amen."
    hour < 12 -> "Father, search me and cleanse me. Reveal every habit, attitude, and desire that competes with Your will. Create in me a clean heart and renew a steadfast spirit within me. In Jesus' name, Amen."
    hour < 24 -> "Lord, receive this fast as an act of worship. Hear the petitions I bring before You, grant me wisdom, and establish Your purpose in every matter committed to You. In Jesus' name, Amen."
    hour < 48 -> "Mighty God, renew my strength as I wait upon You. Preserve my body, steady my mind, and keep my spirit attentive to Your voice. Let Your grace be sufficient for me. In Jesus' name, Amen."
    else -> "Father, carry me faithfully to the end of this fast. Let every prayer offered in these hours rise before You, and let the fruit be repentance, clarity, obedience, and lasting transformation. In Jesus' name, Amen."
}
