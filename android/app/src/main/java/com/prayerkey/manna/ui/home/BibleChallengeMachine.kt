package com.prayerkey.manna.ui.home

import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.reminder.ReminderReceiver
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private data class ReadingPlan(val id: String, val title: String, val detail: String, val chapters: Int, val minutes: Int)
private data class BiblePortion(val lane: String, val book: String, val chapter: Int)

private val biblePlans = listOf(
    ReadingPlan("blended", "Blended", "Old Testament, New Testament and one Psalm each day.", 4, 14),
    ReadingPlan("gospels", "The Gospels", "Walk slowly through the life and teaching of Jesus.", 2, 9),
    ReadingPlan("new_testament", "New Testament", "Read from Matthew to Revelation in ninety days.", 3, 12),
    ReadingPlan("slow", "One chapter a day", "Five quiet minutes. Built for people beginning again.", 1, 5),
)

private val blendedDays = listOf(
    listOf(BiblePortion("OLD TESTAMENT", "Genesis", 1), BiblePortion("NEW TESTAMENT", "Matthew", 1), BiblePortion("PSALM", "Psalms", 1)),
    listOf(BiblePortion("OLD TESTAMENT", "Genesis", 2), BiblePortion("NEW TESTAMENT", "Matthew", 2), BiblePortion("PSALM", "Psalms", 2)),
    listOf(BiblePortion("OLD TESTAMENT", "Genesis", 3), BiblePortion("NEW TESTAMENT", "Matthew", 3), BiblePortion("PSALM", "Psalms", 3)),
    listOf(BiblePortion("OLD TESTAMENT", "Genesis", 4), BiblePortion("NEW TESTAMENT", "Matthew", 4), BiblePortion("PSALM", "Psalms", 4)),
    listOf(BiblePortion("OLD TESTAMENT", "Genesis", 5), BiblePortion("NEW TESTAMENT", "Matthew", 5), BiblePortion("PSALM", "Psalms", 5)),
    listOf(BiblePortion("OLD TESTAMENT", "Genesis", 6), BiblePortion("NEW TESTAMENT", "Matthew", 6), BiblePortion("PSALM", "Psalms", 6)),
    listOf(BiblePortion("OLD TESTAMENT", "Genesis", 7), BiblePortion("NEW TESTAMENT", "Matthew", 7), BiblePortion("PSALM", "Psalms", 7)),
)

private fun portionsFor(plan: String, day: Int): List<BiblePortion> {
    val base = blendedDays[(day - 1) % blendedDays.size]
    return when (plan) {
        "gospels" -> listOf(BiblePortion("GOSPEL", "Matthew", day.coerceAtMost(28)))
        "new_testament" -> listOf(BiblePortion("NEW TESTAMENT", "Matthew", day.coerceAtMost(28)))
        "slow" -> listOf(base.first())
        else -> base
    }
}

@Composable
internal fun BibleChallengeMachine(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bible_challenge_machine", Context.MODE_PRIVATE) }
    var active by remember { mutableStateOf(prefs.getBoolean("active", false)) }
    var step by remember { mutableIntStateOf(0) }
    var planId by remember { mutableStateOf(prefs.getString("plan", "blended").orEmpty()) }
    var hour by remember { mutableIntStateOf(prefs.getInt("hour", 6)) }
    var minute by remember { mutableIntStateOf(prefs.getInt("minute", 30)) }
    var group by remember { mutableStateOf(prefs.getString("group", "alone").orEmpty()) }
    var completedDays by remember {
        mutableStateOf(prefs.getStringSet("completed", emptySet()).orEmpty().mapNotNull(String::toIntOrNull).toSet())
    }
    var readerDay by remember { mutableStateOf<Int?>(null) }
    val plan = biblePlans.firstOrNull { it.id == planId } ?: biblePlans.first()

    readerDay?.let { day ->
        BibleChallengeReader(day, plan, portionsFor(plan.id, day), onBack = { readerDay = null }) {
            completedDays = completedDays + day
            prefs.edit().putStringSet("completed", completedDays.map(Int::toString).toSet()).apply()
            readerDay = null
        }
        return
    }

    if (!active) {
        BibleChallengeSetup(
            step = step,
            plan = plan,
            hour = hour,
            minute = minute,
            group = group,
            onPlan = { planId = it },
            onTime = { TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, false).show() },
            onGroup = { group = it },
            onBack = { if (step > 0) step-- else onClose() },
            onClose = onClose,
            onContinue = {
                if (step < 3) step++ else {
                    active = true
                    completedDays = emptySet()
                    prefs.edit().putBoolean("active", true).putString("plan", planId).putInt("hour", hour)
                        .putInt("minute", minute).putString("group", group).putStringSet("completed", emptySet()).apply()
                    ReminderReceiver.schedule(context, hour, minute, true)
                }
            },
        )
    } else {
        BibleChallengeLanding(plan, hour, minute, group, completedDays, onClose) { readerDay = it }
    }
}

@Composable
private fun BibleChallengeSetup(
    step: Int,
    plan: ReadingPlan,
    hour: Int,
    minute: Int,
    group: String,
    onPlan: (String) -> Unit,
    onTime: () -> Unit,
    onGroup: (String) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onContinue: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = Pk.Oxblood) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp).clip(CircleShape).background(Pk.Crisp.copy(alpha = .12f))) {
                    Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Crisp, modifier = Modifier.size(15.dp))
                }
                Row(Modifier.weight(1f).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(4) { index -> Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(if (index <= step) Pk.Crisp else Pk.Crisp.copy(alpha = .18f))) }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.Close, "Close", tint = Pk.Crisp) }
            }

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
                when (step) {
                    0 -> {
                        BibleSetupHeading("How do you want to read it?", "Choose a pace you can return to without carrying a backlog.")
                        biblePlans.forEach { option ->
                            val selected = option.id == plan.id
                            Column(
                                Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) Pk.Crisp else Pk.Crisp.copy(alpha = .09f))
                                    .clickable { onPlan(option.id) }.padding(17.dp),
                            ) {
                                Text(option.title, color = if (selected) Pk.Oxblood else Pk.Crisp, fontFamily = UtilitySans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(option.detail, color = if (selected) Pk.Charcoal.copy(alpha = .62f) else Pk.Crisp.copy(alpha = .55f), fontFamily = UtilitySans, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 6.dp))
                                Text("${option.chapters} CHAPTER${if (option.chapters > 1) "S" else ""}  ·  ${option.minutes} MIN", color = if (selected) Pk.Oxblood else Pk.GoldLight, fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = 1.sp, modifier = Modifier.padding(top = 10.dp))
                            }
                        }
                    }
                    1 -> {
                        BibleSetupHeading("Choose your reading hour.", "One reminder at the same time each day. No chasing.")
                        Column(
                            Modifier.fillMaxWidth().padding(top = 25.dp).clip(RoundedCornerShape(20.dp))
                                .background(Pk.Crisp.copy(alpha = .1f)).clickable(onClick = onTime).padding(22.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.NotificationsNone, null, tint = Pk.GoldLight)
                                Column(Modifier.padding(start = 16.dp)) {
                                    Text(timeLabelBible(hour, minute), color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 31.sp)
                                    Text("DAILY READING", color = Pk.Crisp.copy(alpha = .45f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp)
                                }
                            }
                        }
                        Column(Modifier.fillMaxWidth().padding(top = 14.dp).clip(RoundedCornerShape(18.dp)).background(Pk.Gold.copy(alpha = .11f)).padding(18.dp)) {
                            Text("TRANSLATION", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp)
                            Text("KJV · available offline", color = Pk.Crisp, fontFamily = BookSerif, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    2 -> {
                        BibleSetupHeading("Who are you reading with?", "Accountability can be quiet. Choose what feels honest.")
                        listOf(
                            "church" to ("My church" to "Read alongside your church community."),
                            "cohort" to ("An open group" to "Begin with people following the same plan."),
                            "alone" to ("On my own" to "Keep this plan private. You can change later."),
                        ).forEach { (id, copy) ->
                            val selected = group == id
                            Column(
                                Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(18.dp))
                                    .background(if (selected) Pk.Crisp else Pk.Crisp.copy(alpha = .09f))
                                    .clickable { onGroup(id) }.padding(18.dp),
                            ) {
                                Text(copy.first, color = if (selected) Pk.Oxblood else Pk.Crisp, fontFamily = UtilitySans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(copy.second, color = if (selected) Pk.Charcoal.copy(alpha = .62f) else Pk.Crisp.copy(alpha = .5f), fontFamily = UtilitySans, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                            }
                        }
                    }
                    else -> {
                        BibleSetupHeading("Ninety days. Then decide again.", "This is one clear window, not a promise you must carry for a year.")
                        Column(Modifier.fillMaxWidth().padding(top = 25.dp).clip(RoundedCornerShape(20.dp)).background(Pk.Gold.copy(alpha = .12f)).padding(20.dp)) {
                            Text("YOUR FIRST WINDOW", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp)
                            Text(plan.title, color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 28.sp, modifier = Modifier.padding(top = 9.dp))
                            Text("${timeLabelBible(hour, minute)}  ·  KJV  ·  ${plan.minutes} MIN", color = Pk.Crisp.copy(alpha = .6f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = .8.sp, modifier = Modifier.padding(top = 12.dp))
                        }
                        Text("Miss a day and continue with the next portion. PrayerKey never turns Scripture into debt.", color = Pk.Crisp.copy(alpha = .58f), fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 20.dp))
                    }
                }
            }

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pk.Crisp, contentColor = Pk.Oxblood),
            ) { Text(if (step == 3) "Begin day 1" else "Continue", fontFamily = UtilitySans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun BibleSetupHeading(title: String, subtitle: String) {
    Text(title, color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 35.sp, lineHeight = 39.sp, modifier = Modifier.padding(top = 18.dp))
    Text(subtitle, color = Pk.Crisp.copy(alpha = .57f), fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))
}

@Composable
private fun BibleChallengeLanding(
    plan: ReadingPlan,
    hour: Int,
    minute: Int,
    group: String,
    completed: Set<Int>,
    onClose: () -> Unit,
    onRead: (Int) -> Unit,
) {
    val day = (completed.size + 1).coerceAtMost(90)
    val portions = portionsFor(plan.id, day)
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().navigationBarsPadding()) {
            Column(Modifier.fillMaxWidth().background(Pk.Oxblood).statusBarsPadding().padding(horizontal = 22.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose, modifier = Modifier.size(34.dp).clip(CircleShape).background(Pk.Crisp.copy(alpha = .12f))) {
                        Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Crisp, modifier = Modifier.size(14.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Row(Modifier.clip(CircleShape).background(Pk.Crisp.copy(alpha = .12f)).padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.NotificationsNone, null, tint = Pk.GoldLight, modifier = Modifier.size(14.dp))
                        Text(timeLabelBible(hour, minute), color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, modifier = Modifier.padding(start = 5.dp))
                    }
                }
                Text("${plan.title.uppercase(Locale.getDefault())}  ·  WINDOW 1 OF 4", color = Pk.GoldLight.copy(alpha = .7f), fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 22.dp))
                Text("Day $day of ninety.", color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 35.sp, modifier = Modifier.padding(top = 8.dp))
                Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    BibleStat((completed.size * plan.chapters).toString(), "CHAPTERS")
                    BibleStat("${(completed.size / 90f * 100).toInt()}%", "OF WINDOW")
                    BibleStat(if (group == "alone") "1" else "63", "READ TODAY")
                }
                LinearProgressIndicator(
                    progress = { completed.size / 90f },
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 12.dp).height(3.dp).clip(CircleShape),
                    color = Pk.Gold,
                    trackColor = Pk.Crisp.copy(alpha = .16f),
                )
            }

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 16.dp).clip(RoundedCornerShape(22.dp))
                        .background(Pk.Sunken).clickable { onRead(day) }.padding(20.dp),
                ) {
                    Text("TODAY  ·  DAY $day  ·  ${plan.minutes} MIN", color = Pk.GoldDeep, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp)
                    portions.forEach { portion ->
                        Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(if (portion.lane == "OLD TESTAMENT") Pk.Gold else Pk.Oxblood))
                            Column(Modifier.padding(start = 11.dp)) {
                                Text("${portion.book} ${portion.chapter}", color = Pk.Charcoal, fontFamily = BookSerif, fontSize = 20.sp)
                                Text(portion.lane, color = Pk.Faint, fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = 1.sp)
                            }
                        }
                    }
                    Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        BibleMeta("${portions.size} PORTIONS"); BibleMeta("READ OR LISTEN"); BibleMeta("KJV")
                    }
                    HorizontalDivider(color = Pk.Hair, modifier = Modifier.padding(top = 16.dp, bottom = 14.dp))
                    Text("Open today's portion  →", color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Text("This window", color = Pk.Charcoal, fontFamily = DisplaySerif, fontSize = 21.sp, modifier = Modifier.padding(horizontal = 20.dp).padding(top = 28.dp, bottom = 10.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp).clip(RoundedCornerShape(20.dp)).background(Pk.Sunken).padding(19.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(58.dp).clip(CircleShape).background(Pk.Oxblood.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                        Text("$day", color = Pk.Oxblood, fontFamily = DisplaySerif, fontSize = 23.sp)
                    }
                    Column(Modifier.padding(start = 16.dp)) {
                        Text("${90 - completed.size} days left", color = Pk.Charcoal, fontFamily = BookSerif, fontSize = 20.sp)
                        Text("Renew the plan at day 90.", color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                Text("Your pace", color = Pk.Charcoal, fontFamily = DisplaySerif, fontSize = 21.sp, modifier = Modifier.padding(horizontal = 20.dp).padding(top = 26.dp, bottom = 10.dp))
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp).clip(RoundedCornerShape(20.dp)).background(Pk.Sunken).padding(19.dp)) {
                    Text("${plan.minutes}", color = Pk.Oxblood, fontFamily = DisplaySerif, fontSize = 39.sp)
                    Text("minutes a day", color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.sp)
                    HorizontalDivider(color = Pk.Hair, modifier = Modifier.padding(vertical = 14.dp))
                    Text("Your hour     ${timeLabelBible(hour, minute)}", color = Pk.Charcoal, fontFamily = UtilitySans, fontSize = 13.sp)
                    Text("Days read     ${completed.size} of $day", color = Pk.Charcoal, fontFamily = UtilitySans, fontSize = 13.sp, modifier = Modifier.padding(top = 11.dp))
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable private fun BibleStat(value: String, label: String) {
    Column { Text(value, color = Pk.GoldLight, fontFamily = DisplaySerif, fontSize = 25.sp); Text(label, color = Pk.Crisp.copy(alpha = .44f), fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = .9.sp, modifier = Modifier.padding(top = 4.dp)) }
}

@Composable private fun BibleMeta(text: String) {
    Text(text, color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = .5.sp, modifier = Modifier.clip(CircleShape).background(Pk.Oxblood.copy(alpha = .07f)).padding(horizontal = 9.dp, vertical = 6.dp))
}

@Composable
private fun BibleChallengeReader(
    day: Int,
    plan: ReadingPlan,
    portions: List<BiblePortion>,
    onBack: () -> Unit,
    onComplete: () -> Unit,
) {
    val context = LocalContext.current
    var selected by remember(day) { mutableIntStateOf(0) }
    var opened by remember(day) { mutableStateOf(setOf(0)) }
    var verses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    val portion = portions[selected]
    LaunchedEffect(portion.book, portion.chapter) {
        verses = withContext(Dispatchers.IO) { OfflineBible(context).chapter(portion.book, portion.chapter) }
    }

    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBackIosNew, "Plan", tint = Pk.Charcoal, modifier = Modifier.size(15.dp)) }
                Text("DAY $day  ·  ${plan.title.uppercase(Locale.getDefault())}", color = Pk.Faint, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.1.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = {}) { Icon(Icons.Outlined.Headphones, "Listen", tint = Pk.Oxblood, modifier = Modifier.size(19.dp)) }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                portions.forEachIndexed { index, item ->
                    val isSelected = selected == index
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(13.dp))
                            .background(if (isSelected) Pk.Oxblood else Pk.Charcoal.copy(alpha = .055f))
                            .clickable { selected = index; opened = opened + index; verses = emptyList() }
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(item.lane, color = if (isSelected) Pk.Crisp.copy(alpha = .68f) else Pk.Faint, fontFamily = UtilitySans, fontSize = 7.sp, maxLines = 1)
                        Text("${item.book} ${item.chapter}", color = if (isSelected) Pk.Crisp else Pk.Charcoal, fontFamily = BookSerif, fontSize = 14.sp, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                        if (index in opened) Icon(Icons.Outlined.Check, null, tint = if (isSelected) Pk.Crisp else Pk.Oxblood, modifier = Modifier.padding(top = 3.dp).size(11.dp))
                    }
                }
            }

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                Text("${portion.book} ${portion.chapter}", color = Pk.Charcoal, fontFamily = DisplaySerif, fontSize = 33.sp, modifier = Modifier.padding(top = 24.dp))
                if (verses.isEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(2.dp), color = Pk.Oxblood, trackColor = Pk.Hair)
                } else {
                    verses.forEach { verse ->
                        Text(
                            "${verse.verse}  ${verse.text}",
                            color = Pk.Charcoal,
                            fontFamily = BookSerif,
                            fontSize = 19.sp,
                            lineHeight = 31.sp,
                            modifier = Modifier.padding(top = if (verse.verse == 1) 20.dp else 12.dp),
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            Button(
                onClick = onComplete,
                enabled = opened.size == portions.size,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood, disabledContainerColor = Pk.Charcoal.copy(alpha = .08f), disabledContentColor = Pk.Faint),
            ) {
                Text(if (opened.size == portions.size) "Mark today read" else "Read every portion to finish", fontFamily = UtilitySans, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun timeLabelBible(hour: Int, minute: Int): String {
    val h = if (hour % 12 == 0) 12 else hour % 12
    return "%d:%02d %s".format(Locale.US, h, minute, if (hour < 12) "AM" else "PM")
}
