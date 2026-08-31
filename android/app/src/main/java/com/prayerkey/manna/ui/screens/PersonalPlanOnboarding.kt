package com.prayerkey.manna.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

private data class PrayerNeed(val title: String, val key: String, val icon: ImageVector, val scripture: String)
private data class FirstJourney(val title: String, val key: String, val detail: String, val minutes: Int, val icon: ImageVector)

private val prayerNeeds = listOf(
    PrayerNeed("My family", "family", Icons.Outlined.FamilyRestroom, "Psalm 121"),
    PrayerNeed("Health and healing", "healing", Icons.Outlined.FavoriteBorder, "Psalm 103"),
    PrayerNeed("Work and money", "work", Icons.Outlined.WorkOutline, "Philippians 4"),
    PrayerNeed("Marriage or relationships", "relationships", Icons.Outlined.PeopleOutline, "1 Corinthians 13"),
    PrayerNeed("Direction", "direction", Icons.Outlined.Explore, "Proverbs 3"),
    PrayerNeed("Spiritual growth", "growth", Icons.Outlined.Spa, "John 15"),
    PrayerNeed("Protection", "protection", Icons.Outlined.Shield, "Psalm 91"),
    PrayerNeed("Something else", "personal", Icons.Outlined.EditNote, "Psalm 46"),
)

private val firstJourneys = listOf(
    FirstJourney("Daily prayer", "daily_prayer", "A short Bible-grounded prayer each day.", 5, Icons.Outlined.WbSunny),
    FirstJourney("7-day prayer challenge", "seven_day", "Pray consistently about what matters most.", 7, Icons.Outlined.LocalFireDepartment),
    FirstJourney("Bible and prayer", "bible_prayer", "Read one passage and turn it into prayer.", 6, Icons.Outlined.MenuBook),
)

@Composable
fun PersonalPlanOnboarding(onDone: (remindersEnabled: Boolean, hour: Int) -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }
    var need by remember { mutableStateOf(prayerNeeds.first()) }
    var rhythm by remember { mutableStateOf("Morning") }
    var reminderHour by remember { mutableIntStateOf(7) }
    var journey by remember { mutableStateOf(firstJourneys[1]) }
    BackHandler(enabled = step > 0) { step-- }

    fun finish(reminders: Boolean) {
        context.getSharedPreferences("onboarding_plan", android.content.Context.MODE_PRIVATE).edit()
            .putString("need", need.key)
            .putString("need_title", need.title)
            .putString("scripture", need.scripture)
            .putString("rhythm", rhythm)
            .putString("journey", journey.key)
            .putString("journey_title", journey.title)
            .putInt("prayer_minutes", journey.minutes)
            .putInt("reminder_hour", reminderHour)
            .apply()
        context.getSharedPreferences("daily_prayer_goal", android.content.Context.MODE_PRIVATE).edit()
            .putInt("minutes", journey.minutes).apply()
        if (journey.key == "seven_day") {
            context.getSharedPreferences("prayer_challenge_machine", android.content.Context.MODE_PRIVATE).edit()
                .putBoolean("active", true)
                .putInt("duration", 7)
                .putString("focus", "Prayer for ${need.title.removePrefix("My ")}")
                .putString("need", need.key)
                .putStringSet("done", emptySet())
                .apply()
        }
        onDone(reminders, reminderHour)
    }

    Box(Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding().navigationBarsPadding()) {
        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "prayerkey-onboarding",
        ) { page ->
            when (page) {
                0 -> PromiseStep { step = 1 }
                1 -> NeedStep(need, { need = it }) { step = 2 }
                2 -> RhythmStep(rhythm, { rhythm = it }, reminderHour, { reminderHour = it }) { step = 3 }
                3 -> JourneyStep(journey, { journey = it }) { step = 4 }
                4 -> PlanRevealStep(need, journey, reminderHour) { step = 5 }
                else -> ReminderPermissionStep(reminderHour, { reminderHour = it }, { finish(true) }, { finish(false) })
            }
        }
        if (step in 1..5) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { step-- }) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Pk.Charcoal) }
                Spacer(Modifier.weight(1f))
                Text("${step + 1} of 6", color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PromiseStep(next: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.onboarding_pray_v3), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x551A0A2E), Color(0xF21A0A2E)))))
        Column(
            Modifier.align(Alignment.BottomStart).padding(horizontal = 26.dp).padding(bottom = 32.dp),
        ) {
            Text("PRAYERKEY", color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 3.sp)
            Text("Prayer for every\npart of life.", Modifier.padding(top = 12.dp), color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 39.sp, lineHeight = 44.sp)
            Text("Pray with Scripture. Build lasting habits. Remember what God has done.", Modifier.padding(top = 14.dp), color = Color.White.copy(alpha = .82f), fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 23.sp)
            PrimaryAction("Continue", Modifier.padding(top = 26.dp), next)
        }
    }
}

@Composable
private fun NeedStep(selected: PrayerNeed, choose: (PrayerNeed) -> Unit, next: () -> Unit) {
    OnboardingPage(
        title = "What do you need prayer for right now?",
        body = "Choose one. You can add more later.",
        button = "Continue",
        onButton = next,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            prayerNeeds.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    row.forEach { item ->
                        val active = item == selected
                        Surface(
                            onClick = { choose(item) }, modifier = Modifier.weight(1f).height(92.dp),
                            color = if (active) Pk.Oxblood.copy(alpha = .10f) else Color.White,
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(if (active) 1.5.dp else 1.dp, if (active) Pk.Oxblood else Pk.Hair),
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                Icon(item.icon, null, tint = Pk.Oxblood, modifier = Modifier.size(22.dp))
                                Text(item.title, color = Pk.Charcoal, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RhythmStep(selected: String, choose: (String) -> Unit, hour: Int, chooseHour: (Int) -> Unit, next: () -> Unit) {
    OnboardingPage("When would you like to pray?", "Choose a rhythm that fits your day.", "Continue", next) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Morning", "Evening", "Morning and evening", "I’ll choose later").forEach { label ->
                SelectRow(label, selected == label, Icons.Outlined.Schedule) { choose(label) }
            }
            if (selected != "I’ll choose later") {
                Text("Reminder time", Modifier.padding(top = 10.dp), color = Pk.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    val hours = if (selected == "Evening") listOf(18, 19, 20, 21) else listOf(5, 6, 7, 8)
                    hours.forEach { value ->
                        FilterChip(
                            selected = hour == value, onClick = { chooseHour(value) },
                            label = { Text(if (value > 12) "${value - 12} PM" else "$value AM") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Pk.Oxblood, selectedLabelColor = Color.White),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyStep(selected: FirstJourney, choose: (FirstJourney) -> Unit, next: () -> Unit) {
    OnboardingPage("How would you like to begin?", "Start with one simple journey.", "Build my plan", next) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            firstJourneys.forEach { item ->
                val active = item == selected
                Surface(
                    onClick = { choose(item) }, shape = RoundedCornerShape(24.dp),
                    color = if (active) Pk.Oxblood else Color.White,
                    border = BorderStroke(1.dp, if (active) Pk.Oxblood else Pk.Hair),
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).clip(CircleShape).background(if (active) Color.White.copy(alpha = .15f) else Pk.Oxblood.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                            Icon(item.icon, null, tint = if (active) Pk.GoldLight else Pk.Oxblood)
                        }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(item.title, color = if (active) Color.White else Pk.Charcoal, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(item.detail, Modifier.padding(top = 4.dp), color = if (active) Color.White.copy(alpha = .75f) else Pk.Muted, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                        if (active) Icon(Icons.Outlined.CheckCircle, null, tint = Pk.GoldLight)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanRevealStep(need: PrayerNeed, journey: FirstJourney, hour: Int, next: () -> Unit) {
    OnboardingPage("Your first journey is ready.", "Built around what you need today.", "Continue", next) {
        Surface(Modifier.fillMaxWidth(), color = Pk.DeepDark, shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(22.dp)) {
                Text(journey.title.uppercase(), color = Pk.GoldLight, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.8.sp)
                Text(if (journey.key == "seven_day") "7 Days of Prayer for ${need.title.removePrefix("My ")}" else need.title, Modifier.padding(top = 10.dp), color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 25.sp, lineHeight = 31.sp)
                Spacer(Modifier.height(22.dp))
                PlanItem("1", "Read ${need.scripture}")
                PlanItem("2", "Reflect for one minute")
                PlanItem("3", "Pray three guided prayer points")
                PlanItem("4", "Write one personal request")
                HorizontalDivider(Modifier.padding(vertical = 18.dp), color = Color.White.copy(alpha = .12f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = Pk.GoldLight, modifier = Modifier.size(18.dp))
                    Text("About ${journey.minutes} minutes", Modifier.padding(start = 9.dp), color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
                    Spacer(Modifier.weight(1f))
                    Text(if (hour > 12) "${hour - 12}:00 PM" else "$hour:00 AM", color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ReminderPermissionStep(hour: Int, chooseHour: (Int) -> Unit, enable: () -> Unit, skip: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(96.dp))
        Box(Modifier.size(82.dp).clip(CircleShape).background(Pk.Oxblood.copy(alpha = .10f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.NotificationsActive, null, tint = Pk.Oxblood, modifier = Modifier.size(35.dp))
        }
        Text("Would you like a reminder?", Modifier.padding(top = 25.dp), textAlign = TextAlign.Center, color = Pk.Charcoal, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp)
        Text("We’ll remind you once at the time you choose. No promotions.", Modifier.padding(top = 12.dp), textAlign = TextAlign.Center, color = Pk.Muted, fontSize = 14.sp, lineHeight = 21.sp)
        Row(Modifier.padding(top = 26.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(6, 7, 8, 20).forEach { value ->
                FilterChip(
                    selected = hour == value, onClick = { chooseHour(value) },
                    label = { Text(if (value > 12) "8 PM" else "$value AM") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Pk.Oxblood, selectedLabelColor = Color.White),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryAction("Turn on my reminder", Modifier.fillMaxWidth(), enable)
        Text("Not now", Modifier.padding(vertical = 20.dp).clickable(onClick = skip).padding(8.dp), color = Pk.Charcoal, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun OnboardingPage(title: String, body: String, button: String, onButton: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 72.dp, bottom = 22.dp)) {
        Text(title, color = Pk.Charcoal, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp)
        Text(body, Modifier.padding(top = 8.dp, bottom = 22.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 20.sp)
        content()
        Spacer(Modifier.weight(1f))
        PrimaryAction(button, Modifier.fillMaxWidth(), onButton)
    }
}

@Composable
private fun SelectRow(title: String, active: Boolean, icon: ImageVector, click: () -> Unit) {
    Surface(onClick = click, color = if (active) Pk.Oxblood.copy(alpha = .09f) else Color.White, shape = RoundedCornerShape(20.dp), border = BorderStroke(if (active) 1.5.dp else 1.dp, if (active) Pk.Oxblood else Pk.Hair)) {
        Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Pk.Oxblood)
            Text(title, Modifier.weight(1f).padding(start = 13.dp), color = Pk.Charcoal, fontWeight = FontWeight.SemiBold)
            RadioButton(active, click, colors = RadioButtonDefaults.colors(selectedColor = Pk.Oxblood))
        }
    }
}

@Composable
private fun PlanItem(number: String, title: String) {
    Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = .10f)), contentAlignment = Alignment.Center) { Text(number, color = Pk.GoldLight, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
        Text(title, Modifier.padding(start = 12.dp), color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun PrimaryAction(label: String, modifier: Modifier = Modifier, click: () -> Unit) {
    Button(onClick = click, modifier = modifier.height(58.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood, contentColor = Color.White)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Icon(Icons.Outlined.ArrowForward, null, Modifier.padding(start = 10.dp).size(18.dp))
    }
}
