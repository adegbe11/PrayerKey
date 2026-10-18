package com.prayerkey.manna.ui.screens

import com.prayerkey.manna.platform.keyValues
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.resources.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import com.prayerkey.manna.ui.theme.drift
import com.prayerkey.manna.ui.theme.pressable
import com.prayerkey.manna.ui.theme.rise
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans

private data class WelcomeChoice(val id: String, val title: String, val icon: ImageVector)
private val goals = listOf(
    WelcomeChoice("pray", "Make time for prayer", Icons.Outlined.FavoriteBorder),
    WelcomeChoice("read", "Spend time in Scripture", Icons.Outlined.AutoStories),
    WelcomeChoice("fast", "Begin a fast", Icons.Outlined.Spa),
    WelcomeChoice("grow", "Grow in faith", Icons.Outlined.WbSunny),
    WelcomeChoice("church", "Connect with my church", Icons.Outlined.Church),
    WelcomeChoice("remember", "Remember answered prayers", Icons.Outlined.BookmarkBorder),
    WelcomeChoice("journal", "Keep a prayer journal", Icons.Outlined.EditNote),
)
private val topics = listOf(
    WelcomeChoice("family", "Family", Icons.Outlined.PeopleOutline),
    WelcomeChoice("health", "Health", Icons.Outlined.HealthAndSafety),
    WelcomeChoice("peace", "Peace", Icons.Outlined.Spa),
    WelcomeChoice("guidance", "Guidance", Icons.Outlined.Explore),
    WelcomeChoice("work", "Work & finances", Icons.Outlined.WorkOutline),
    // its own mark: it shared Family's two people
    WelcomeChoice("relationships", "Relationships", Icons.Outlined.Handshake),
    WelcomeChoice("gratitude", "Gratitude", Icons.Outlined.WbSunny),
    WelcomeChoice("church", "My church", Icons.Outlined.Church),
)

/** Draft choices survive rotation. Preview never changes saved preferences or alarms. */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GuidedWelcome(preview: Boolean = false, onBack: () -> Unit, onDone: (Boolean, Int) -> Unit) {
    val context = platformContext()
    var step by rememberSaveable { mutableIntStateOf(0) }
    var selectedGoals by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var selectedTopics by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    // on unless they turn it off: the reminder is what brings anyone back tomorrow
    var reminders by rememberSaveable { mutableStateOf(true) }
    var hour by rememberSaveable { mutableIntStateOf(7) }
    var minute by rememberSaveable { mutableIntStateOf(0) }
    var showTime by rememberSaveable { mutableStateOf(false) }
    if (showTime) {
        val time = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = false)
        AlertDialog(onDismissRequest = { showTime = false }, containerColor = Pk.Cream,
            title = { Text("Your prayer time", fontFamily = DisplaySerif, color = Pk.Charcoal) },
            text = { TimeInput(state = time, colors = TimePickerDefaults.colors(
                timeSelectorSelectedContainerColor = Pk.Oxblood, timeSelectorSelectedContentColor = Pk.Crisp,
                timeSelectorUnselectedContainerColor = Pk.Hair, timeSelectorUnselectedContentColor = Pk.Charcoal,
                periodSelectorSelectedContainerColor = Pk.Oxblood, periodSelectorSelectedContentColor = Pk.Crisp)) },
            confirmButton = { TextButton(onClick = { hour = time.hour; minute = time.minute; reminders = true; showTime = false }) { Text("Save", color = Pk.Oxblood) } },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("Cancel", color = Pk.Muted) } })
    }
    fun back() { if (step == 0) onBack() else step-- }
    BackHandler { back() }
    fun finish() {
        if (!preview) context.keyValues("onboarding_plan").edit()
            .putStringSet("goals", selectedGoals.toSet())
            .putStringSet("topics", selectedTopics.toSet())
            .putBoolean("single_reminder", true)
            .putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()
        onDone(reminders, hour)
    }
    Box(Modifier.fillMaxSize().background(Pk.DeepDark)) {
        Image(painterResource(Res.drawable.welcome_prayer_dawn), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
        Box(Modifier.matchParentSize().background(Pk.DeepDark.copy(alpha = if (step == 3) .36f else .86f)))
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { back() }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Pk.Crisp) }
                Row(Modifier.weight(1f).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) { index ->
                        val fill by androidx.compose.animation.core.animateFloatAsState(
                            if (index <= step) 1f else 0f,
                            androidx.compose.animation.core.spring(dampingRatio = .8f, stiffness = 220f), label = "seg$index",
                        )
                        Box(Modifier.weight(1f).height(3.dp).background(Pk.Crisp.copy(alpha = .2f), RoundedCornerShape(3.dp))) {
                            Box(Modifier.fillMaxWidth(fill).height(3.dp).background(Pk.GoldLight, RoundedCornerShape(3.dp)))
                        }
                    }
                }
                if (step < 3) TextButton(onClick = {
                    when (step) { 0 -> selectedGoals = arrayListOf(); 1 -> selectedTopics = arrayListOf(); 2 -> reminders = false }
                    step++
                }) { Text("Skip", color = Pk.Crisp, fontFamily = UtilitySans) }
                else Spacer(Modifier.width(48.dp))
            }
            key(step) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 24.dp, bottom = 20.dp)) {
                    Text(listOf("Your goals", "Your prayers", "Your time", "PRAYERKEY")[step], Modifier.rise(0), color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 13.sp)
                    Text(listOf("Where would you\nlike to begin?", "What’s on\nyour heart?", "A moment\nwith God.", "Same God.\nA new day.")[step], Modifier.padding(top = 14.dp).rise(1, distance = 34f), color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 36.sp, lineHeight = 40.sp)
                    Text(listOf("Choose up to three.", "Choose what matters to you today.", "Choose a time for prayer.", "Let’s pray.")[step], Modifier.padding(top = 12.dp, bottom = 28.dp).rise(2), color = Pk.Crisp.copy(alpha = .8f), fontFamily = UtilitySans, fontSize = 15.sp)
                    when (step) {
                        0 -> goals.forEachIndexed { i, item ->
                            WelcomeOption(item, item.id in selectedGoals, order = 3 + i) {
                                selectedGoals = ArrayList(selectedGoals.toMutableList().apply { if (!remove(item.id) && size < 3) add(item.id) })
                            }
                        }
                        1 -> topics.chunked(2).forEachIndexed { r, row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEachIndexed { c, item ->
                                    Box(Modifier.weight(1f)) {
                                        WelcomeOption(item, item.id in selectedTopics, compact = true, order = 3 + r * 2 + c) {
                                            selectedTopics = ArrayList(selectedTopics.toMutableList().apply { if (!remove(item.id)) add(item.id) })
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Surface(Modifier.fillMaxWidth(), color = Pk.Crisp.copy(alpha = .09f), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Pk.Crisp.copy(alpha = .18f))) {
                                Column(Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        /* The mark, because the real reminder
                                           now carries it as its large icon.
                                           This card is a picture of what will
                                           arrive in the shade, so it has to
                                           keep matching what actually does. */
                                        Image(painterResource(Res.drawable.prayerkey_mark), null, Modifier.size(34.dp))
                                        Text("PrayerKey", Modifier.padding(start = 12.dp), color = Pk.Crisp, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text("Time for prayer", Modifier.padding(top = 18.dp), color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 24.sp)
                                    Text("Bring what’s on your heart.", Modifier.padding(top = 5.dp), color = Pk.Crisp.copy(alpha = .8f), fontFamily = UtilitySans)
                                }
                            }
                            Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Daily prayer reminder", Modifier.weight(1f), color = Pk.Crisp, fontFamily = UtilitySans)
                                Switch(checked = reminders, onCheckedChange = { reminders = it }, colors = SwitchDefaults.colors(checkedTrackColor = Pk.Oxblood, checkedThumbColor = Pk.Crisp))
                            }
                            /* Dim while the reminder is off - lit gold, it said a
                               reminder was set when none was. Choosing a time
                               turns the reminder on. */
                            val lit = if (reminders) 1f else .4f
                            OutlinedButton(onClick = {
                                showTime = true
                            }, Modifier.fillMaxWidth().heightIn(min = 58.dp), border = BorderStroke(1.dp, Pk.GoldLight.copy(alpha = lit))) {
                                Icon(Icons.Outlined.Schedule, null, tint = Pk.GoldLight.copy(alpha = lit))
                                Text("${if (hour % 12 == 0) 12 else hour % 12}:${minute.toString().padStart(2, '0')} ${if (hour < 12) "AM" else "PM"}", Modifier.padding(start = 12.dp), color = Pk.Crisp.copy(alpha = lit), fontSize = 20.sp)
                            }
                        }
                        /* Only the words. The mark and a line of the choices
                           sat on the photograph - the key across her hair, the
                           list across her face, wrapping with a stray dot. The
                           choices are kept; they shape the app, not this page. */
                        3 -> Unit
                    }
                }
            }
            Button(onClick = { if (step == 3) finish() else step++ }, Modifier.fillMaxWidth().padding(bottom = 18.dp).heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood, contentColor = Pk.Crisp), shape = RoundedCornerShape(99.dp)) {
                Text(if (step == 3) "Enter PrayerKey" else "Continue", fontFamily = UtilitySans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun WelcomeOption(item: WelcomeChoice, selected: Boolean, compact: Boolean = false, order: Int = 0, onClick: () -> Unit) {
    // a chosen card swells for a moment, then settles - the choice is felt
    val pop by androidx.compose.animation.core.animateFloatAsState(
        if (selected) 1f else 0f,
        androidx.compose.animation.core.spring(dampingRatio = .45f, stiffness = 500f), label = "chosen",
    )
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    Surface(Modifier.fillMaxWidth().padding(bottom = 10.dp).rise(order).pressable()
        .graphicsLayer { val s = 1f + .025f * kotlin.math.sin(pop * kotlin.math.PI).toFloat(); scaleX = s; scaleY = s }
        .toggleable(selected, role = Role.Checkbox, onValueChange = {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            onClick()
        }),
        color = if (selected) Pk.Oxblood.copy(alpha = .7f) else Pk.Crisp.copy(alpha = .07f), shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (selected) Pk.GoldLight else Pk.Crisp.copy(alpha = .18f))) {
        if (compact) Column(Modifier.padding(16.dp).heightIn(min = 76.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth()) { Icon(item.icon, null, tint = Pk.GoldLight); Spacer(Modifier.weight(1f)); if (selected) Icon(Icons.Outlined.CheckCircle, null, tint = Pk.Crisp, modifier = Modifier.size(18.dp)) }
            Text(item.title, color = Pk.Crisp, fontFamily = UtilitySans, fontSize = 14.sp)
        } else Row(Modifier.padding(17.dp).heightIn(min = 30.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(item.icon, null, tint = Pk.GoldLight)
            Text(item.title, Modifier.weight(1f).padding(horizontal = 14.dp), color = Pk.Crisp, fontFamily = UtilitySans, fontSize = 14.sp)
            Icon(if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null, tint = if (selected) Pk.GoldLight else Pk.Crisp.copy(alpha = .4f), modifier = Modifier.size(20.dp))
        }
    }
}
