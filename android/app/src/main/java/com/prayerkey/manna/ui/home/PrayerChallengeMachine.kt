package com.prayerkey.manna.ui.home

import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.reminder.ReminderReceiver
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class ChallengeSeed(val name: String, val prayer: String)
private data class ChallengeReading(
    val title: String,
    val reference: String,
    val verse: String,
    val points: List<String>,
)

private val challengeSeeds = listOf(
    ChallengeSeed("Marriage", "Father, establish love, truth and faithfulness in my marriage."),
    ChallengeSeed("Family", "Father, let Your peace rest on my family and restore unity."),
    ChallengeSeed("Healing", "Lord, give me strength, wisdom and grace for every step of healing."),
    ChallengeSeed("Open doors", "Lord, lead me to the doors You prepared and close every wrong path."),
    ChallengeSeed("Work and provision", "Father, bless the work of my hands and teach me faithful stewardship."),
    ChallengeSeed("Children", "Lord, guard my children and lead them in wisdom, truth and peace."),
    ChallengeSeed("Freedom from fear", "God of peace, quiet fear and teach me to trust Your presence."),
    ChallengeSeed("Purpose and direction", "Father, order my steps and make the next faithful step clear."),
)

private val readings = listOf(
    ChallengeReading("Ask plainly", "James 4:2", "You do not have, because you do not ask.", listOf(
        "Father, I bring this need to You plainly, without fear or performance.",
        "Show me what I am truly asking for beneath all my anxious words.",
        "Remove every motive that would lead me away from Your will.",
        "Give me courage to ask, patience to wait, and wisdom to obey.",
        "Let my confidence rest in Your character, not in a deadline.",
    )),
    ChallengeReading("Trust the way", "Proverbs 3:5–6", "Trust in the Lord with all your heart, and do not rely on your own understanding.", listOf(
        "Father, I release the part of this journey I cannot understand.",
        "Keep me from forcing a door You have not opened.",
        "Make the next obedient step unmistakably clear.",
        "Correct every plan in me that is built on fear.",
        "Direct my path and keep my heart steady today.",
    )),
    ChallengeReading("Exchange the weight", "Philippians 4:6–7", "In everything, through prayer and petition with thanksgiving, present your requests to God.", listOf(
        "Father, I name the burden I have carried silently.",
        "I exchange anxious rehearsal for honest prayer.",
        "Guard my mind from conclusions You have not spoken.",
        "Teach me to notice what is still worthy of thanks.",
        "Let Your peace govern my thoughts and choices today.",
    )),
    ChallengeReading("Wait with strength", "Isaiah 40:31", "Those who wait for the Lord will renew their strength.", listOf(
        "Renew the strength that delay has drained from me.",
        "Keep disappointment from becoming unbelief.",
        "Train me to wait without abandoning my place.",
        "Give me grace for the work that belongs to today.",
        "Let this waiting deepen me rather than diminish me.",
    )),
    ChallengeReading("Ask for wisdom", "James 1:5", "If any of you lacks wisdom, he should ask God, who gives to all generously.", listOf(
        "Father, give me wisdom that is clean, patient and practical.",
        "Expose the option that only looks attractive from a distance.",
        "Send wise voices I can trust and the humility to listen.",
        "Keep urgency from making the decision for me.",
        "Show me what faithfulness looks like before this day ends.",
    )),
)

@Composable
internal fun PrayerChallengeMachine(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prayer_challenge_machine", Context.MODE_PRIVATE) }
    var active by remember { mutableStateOf(prefs.getBoolean("active", false)) }
    var step by remember { mutableIntStateOf(0) }
    var situation by remember { mutableStateOf(prefs.getString("situation", "Healing").orEmpty()) }
    var focus by remember { mutableStateOf(prefs.getString("focus", "").orEmpty()) }
    var duration by remember { mutableIntStateOf(prefs.getInt("duration", 21)) }
    var hour by remember { mutableIntStateOf(prefs.getInt("hour", 6)) }
    var minute by remember { mutableIntStateOf(prefs.getInt("minute", 0)) }
    var done by remember {
        mutableStateOf(prefs.getStringSet("done", emptySet()).orEmpty().mapNotNull(String::toIntOrNull).toSet())
    }
    var openDay by remember { mutableStateOf<Int?>(null) }

    val seed = challengeSeeds.firstOrNull { it.name == situation }
        ?: ChallengeSeed(situation, "Father, I place $situation before You. Lead me by Your Word and let Your will be done.")
    val prayerFocus = focus.ifBlank { situation }

    openDay?.let { day ->
        ChallengeDayPage(
            day = day,
            total = duration,
            focus = prayerFocus,
            seed = seed,
            hour = hour,
            minute = minute,
            onBack = { openDay = null },
            onComplete = {
                done = done + day
                prefs.edit().putStringSet("done", done.map(Int::toString).toSet()).apply()
                openDay = null
            },
        )
        return
    }

    if (!active) {
        ChallengeSetup(
            step = step,
            situation = situation,
            focus = focus,
            duration = duration,
            hour = hour,
            minute = minute,
            onClose = onClose,
            onSituation = { situation = it },
            onFocus = { focus = it },
            onDuration = { duration = it },
            onTime = {
                TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, false).show()
            },
            onBack = { if (step > 0) step-- else onClose() },
            onContinue = {
                if (step < 3) step++ else {
                    active = true
                    done = emptySet()
                    prefs.edit().putBoolean("active", true).putString("situation", situation)
                        .putString("focus", focus).putInt("duration", duration).putInt("hour", hour)
                        .putInt("minute", minute).putStringSet("done", emptySet()).apply()
                    ReminderReceiver.schedule(context, hour, minute, true)
                }
            },
        )
    } else {
        ChallengeLanding(
            focus = prayerFocus,
            duration = duration,
            hour = hour,
            minute = minute,
            done = done,
            onClose = onClose,
            onOpenDay = { openDay = it },
        )
    }
}

@Composable
private fun ChallengeSetup(
    step: Int,
    situation: String,
    focus: String,
    duration: Int,
    hour: Int,
    minute: Int,
    onClose: () -> Unit,
    onSituation: (String) -> Unit,
    onFocus: (String) -> Unit,
    onDuration: (Int) -> Unit,
    onTime: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = Pk.Oxblood) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp).clip(CircleShape).background(Pk.Crisp.copy(alpha = .12f))) {
                    Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Crisp, modifier = Modifier.size(15.dp))
                }
                Row(Modifier.weight(1f).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(4) { index ->
                        Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(if (index <= step) Pk.Crisp else Pk.Crisp.copy(alpha = .18f)))
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) { Icon(Icons.Outlined.Close, "Close", tint = Pk.Crisp) }
            }

            Column(Modifier.weight(1f).padding(horizontal = 22.dp)) {
                when (step) {
                    0 -> {
                        SetupHeading("What are you praying for?", "Choose the situation closest to your need.")
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth().height(390.dp).padding(top = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp),
                        ) {
                            items(challengeSeeds) { item ->
                                val selected = situation == item.name
                                Box(
                                    Modifier.fillMaxWidth().height(76.dp).clip(RoundedCornerShape(17.dp))
                                        .background(if (selected) Pk.Crisp else Pk.Crisp.copy(alpha = .09f))
                                        .clickable { onSituation(item.name) }.padding(15.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Text(item.name, color = if (selected) Pk.Oxblood else Pk.Crisp, fontFamily = UtilitySans, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    1 -> {
                        SetupHeading("Name it plainly.", "A private sentence is enough. PrayerKey will carry it through every day.")
                        BasicTextField(
                            value = focus,
                            onValueChange = onFocus,
                            textStyle = TextStyle(color = Pk.Crisp, fontFamily = BookSerif, fontSize = 22.sp, lineHeight = 30.sp),
                            modifier = Modifier.fillMaxWidth().padding(top = 26.dp).height(132.dp)
                                .clip(RoundedCornerShape(18.dp)).background(Pk.Crisp.copy(alpha = .1f)).padding(18.dp),
                            decorationBox = { field ->
                                Box {
                                    if (focus.isBlank()) Text("My father's healing…", color = Pk.Crisp.copy(alpha = .35f), fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 21.sp)
                                    field()
                                }
                            },
                        )
                    }
                    2 -> {
                        SetupHeading("How long will you keep watch?", "Choose a rhythm you can honour faithfully.")
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth().height(240.dp).padding(top = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(listOf(7, 14, 21, 40)) { days ->
                                val selected = duration == days
                                Column(
                                    Modifier.fillMaxWidth().height(105.dp).clip(RoundedCornerShape(18.dp))
                                        .background(if (selected) Pk.Crisp else Pk.Crisp.copy(alpha = .09f))
                                        .clickable { onDuration(days) }.padding(17.dp),
                                ) {
                                    Text("$days", color = if (selected) Pk.Oxblood else Pk.Crisp, fontFamily = DisplaySerif, fontSize = 31.sp)
                                    Text("DAYS", color = if (selected) Pk.Oxblood.copy(alpha = .58f) else Pk.Crisp.copy(alpha = .5f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp)
                                }
                            }
                        }
                    }
                    else -> {
                        SetupHeading("Choose your prayer hour.", "One reminder each day. Nothing else.")
                        Box(
                            Modifier.fillMaxWidth().padding(top = 26.dp).clip(RoundedCornerShape(20.dp))
                                .background(Pk.Crisp.copy(alpha = .1f)).clickable(onClick = onTime).padding(22.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.NotificationsNone, null, tint = Pk.GoldLight)
                                Column(Modifier.padding(start = 16.dp)) {
                                    Text(timeLabel(hour, minute), color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 30.sp)
                                    Text("DAILY REMINDER", color = Pk.Crisp.copy(alpha = .45f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp)
                                }
                            }
                        }
                        Column(Modifier.fillMaxWidth().padding(top = 22.dp).clip(RoundedCornerShape(20.dp)).background(Pk.Gold.copy(alpha = .13f)).padding(20.dp)) {
                            Text("YOUR CHALLENGE", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.3.sp)
                            Text(focus.ifBlank { situation }, color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 25.sp, lineHeight = 31.sp, modifier = Modifier.padding(top = 10.dp))
                            Text("$duration DAYS  ·  ${timeLabel(hour, minute)}", color = Pk.Crisp.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 10.sp, letterSpacing = .8.sp, modifier = Modifier.padding(top = 13.dp))
                        }
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
private fun SetupHeading(title: String, subtitle: String) {
    Text(title, color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 35.sp, lineHeight = 39.sp, fontWeight = FontWeight.Normal, modifier = Modifier.padding(top = 18.dp))
    Text(subtitle, color = Pk.Crisp.copy(alpha = .57f), fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 12.dp))
}

@Composable
private fun ChallengeLanding(
    focus: String,
    duration: Int,
    hour: Int,
    minute: Int,
    done: Set<Int>,
    onClose: () -> Unit,
    onOpenDay: (Int) -> Unit,
) {
    val current = (done.size + 1).coerceAtMost(duration)
    val reading = readings[(current - 1) % readings.size]
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        LazyColumn(Modifier.fillMaxSize().navigationBarsPadding()) {
            item {
                Column(Modifier.fillMaxWidth().background(Pk.Oxblood).statusBarsPadding().padding(horizontal = 22.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose, modifier = Modifier.size(34.dp).clip(CircleShape).background(Pk.Crisp.copy(alpha = .12f))) {
                            Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Crisp, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        Row(Modifier.clip(CircleShape).background(Pk.Crisp.copy(alpha = .12f)).padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.NotificationsNone, null, tint = Pk.GoldLight, modifier = Modifier.size(14.dp))
                            Text(timeLabel(hour, minute), color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, modifier = Modifier.padding(start = 5.dp))
                        }
                    }
                    Text("PRAYING FOR", color = Pk.GoldLight.copy(alpha = .72f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.4.sp, modifier = Modifier.padding(top = 23.dp))
                    Text(focus, color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 32.sp, lineHeight = 37.sp, modifier = Modifier.padding(top = 8.dp))
                    Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                        ChallengeStat(done.size.toString(), "DAYS PRAYED")
                        ChallengeStat((duration - done.size).toString(), "TO GO")
                    }
                    LinearProgressIndicator(
                        progress = { done.size.toFloat() / duration },
                        modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(3.dp).clip(CircleShape),
                        color = Pk.Gold,
                        trackColor = Pk.Crisp.copy(alpha = .16f),
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            item {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 16.dp).clip(RoundedCornerShape(22.dp))
                        .background(Pk.Sunken).clickable { onOpenDay(current) }.padding(20.dp),
                ) {
                    Text("TODAY  ·  DAY $current", color = Pk.GoldDeep, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.3.sp)
                    Text(reading.title, color = Pk.Charcoal, fontFamily = DisplaySerif, fontSize = 25.sp, lineHeight = 30.sp, modifier = Modifier.padding(top = 10.dp))
                    Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        MetaPill("5 POINTS"); MetaPill("9 MIN"); MetaPill(timeLabel(hour, minute))
                    }
                    HorizontalDivider(color = Pk.Hair, modifier = Modifier.padding(top = 16.dp, bottom = 14.dp))
                    Text("Open today's prayer  →", color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 28.dp, bottom = 12.dp), verticalAlignment = Alignment.Bottom) {
                    Text("The days", color = Pk.Charcoal, fontFamily = DisplaySerif, fontSize = 21.sp)
                    Spacer(Modifier.weight(1f))
                    Text("$current OF $duration", color = Pk.Faint, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.sp)
                }
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().height((((duration + 6) / 7) * 48).dp).padding(horizontal = 18.dp),
                    userScrollEnabled = false,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items((1..duration).toList()) { day ->
                        val complete = day in done
                        val unlocked = day <= current
                        Box(
                            Modifier.aspectRatio(1f).clip(RoundedCornerShape(10.dp))
                                .background(when { complete -> Pk.Oxblood; day == current -> Pk.GoldWash; else -> Pk.Charcoal.copy(alpha = .055f) })
                                .clickable(enabled = unlocked) { onOpenDay(day) },
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                complete -> Icon(Icons.Outlined.Check, null, tint = Pk.Crisp, modifier = Modifier.size(15.dp))
                                !unlocked -> Icon(Icons.Outlined.Lock, null, tint = Pk.Faint.copy(alpha = .6f), modifier = Modifier.size(12.dp))
                                else -> Text("$day", color = if (day == current) Pk.GoldDeep else Pk.Faint, fontFamily = UtilitySans, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
            item {
                Column(Modifier.fillMaxWidth().padding(18.dp).clip(RoundedCornerShape(20.dp)).background(Pk.Sunken).padding(19.dp)) {
                    Text("IN YOUR PRAYER LIST", color = Pk.GoldDeep, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp)
                    Text(focus, color = Pk.Charcoal, fontFamily = BookSerif, fontSize = 19.sp, lineHeight = 25.sp, modifier = Modifier.padding(top = 9.dp))
                    Text("OPENED ${LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())).uppercase(Locale.getDefault())}", color = Pk.Faint, fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = .8.sp, modifier = Modifier.padding(top = 9.dp))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ChallengeStat(number: String, label: String) {
    Column {
        Text(number, color = Pk.GoldLight, fontFamily = DisplaySerif, fontSize = 27.sp)
        Text(label, color = Pk.Crisp.copy(alpha = .46f), fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = 1.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun MetaPill(text: String) {
    Text(text, color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = .6.sp, modifier = Modifier.clip(CircleShape).background(Pk.Oxblood.copy(alpha = .07f)).padding(horizontal = 9.dp, vertical = 6.dp))
}

@Composable
private fun ChallengeDayPage(
    day: Int,
    total: Int,
    focus: String,
    seed: ChallengeSeed,
    hour: Int,
    minute: Int,
    onBack: () -> Unit,
    onComplete: () -> Unit,
) {
    val reading = readings[(day - 1) % readings.size]
    var checked by remember(day) { mutableStateOf<Set<Int>>(emptySet()) }
    var note by remember(day) { mutableStateOf("") }
    Surface(Modifier.fillMaxSize(), color = Pk.DeepDark) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBackIosNew, "Challenge", tint = Pk.Crisp, modifier = Modifier.size(15.dp)) }
                Text("DAY $day OF $total", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.3.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.width(48.dp))
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
                Text("FOR ${focus.uppercase(Locale.getDefault())}", color = Pk.GoldLight.copy(alpha = .7f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 14.dp))
                Text(reading.title, color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 36.sp, lineHeight = 40.sp, modifier = Modifier.padding(top = 9.dp))
                Text("${timeLabel(hour, minute)}  ·  5 POINTS  ·  9 MIN", color = Pk.Crisp.copy(alpha = .48f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = .8.sp, modifier = Modifier.padding(top = 13.dp))

                Column(Modifier.fillMaxWidth().padding(top = 24.dp).clip(RoundedCornerShape(18.dp)).background(Pk.Gold.copy(alpha = .1f)).padding(18.dp)) {
                    Text("“${reading.verse}”", color = Pk.Crisp.copy(alpha = .92f), fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 27.sp)
                    Text(reading.reference.uppercase(Locale.getDefault()), color = Pk.GoldLight.copy(alpha = .68f), fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 11.dp))
                }

                Text("TODAY'S PRAYER", color = Pk.GoldLight.copy(alpha = .72f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.4.sp, modifier = Modifier.padding(top = 28.dp, bottom = 5.dp))
                reading.points.forEachIndexed { index, point ->
                    val selected = index in checked
                    Row(
                        Modifier.fillMaxWidth().clickable { checked = if (selected) checked - index else checked + index }.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text("${index + 1}", color = Pk.Crisp.copy(alpha = .3f), fontFamily = UtilitySans, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp).width(24.dp))
                        Text(point, color = if (selected) Pk.Crisp.copy(alpha = .38f) else Pk.Crisp, fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 26.sp, modifier = Modifier.weight(1f))
                        Box(Modifier.padding(start = 12.dp, top = 3.dp).size(22.dp).clip(CircleShape).background(if (selected) Pk.Gold else Color.Transparent).then(if (!selected) Modifier.background(Color.Transparent).clip(CircleShape) else Modifier), contentAlignment = Alignment.Center) {
                            if (selected) Icon(Icons.Outlined.Check, null, tint = Pk.DeepDark, modifier = Modifier.size(14.dp))
                        }
                    }
                    if (index != reading.points.lastIndex) HorizontalDivider(color = Pk.Crisp.copy(alpha = .09f))
                }

                Text(seed.prayer, color = Pk.GoldLight, fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 27.sp, modifier = Modifier.padding(top = 22.dp))

                Text("A NOTE FROM TODAY", color = Pk.GoldLight.copy(alpha = .72f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 28.dp))
                BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    textStyle = TextStyle(color = Pk.Crisp, fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(88.dp).clip(RoundedCornerShape(16.dp)).background(Pk.Crisp.copy(alpha = .08f)).padding(15.dp),
                    decorationBox = { field -> Box { if (note.isBlank()) Text("What became clear while you prayed?", color = Pk.Crisp.copy(alpha = .28f), fontFamily = BookSerif, fontSize = 17.sp); field() } },
                )
                Spacer(Modifier.height(24.dp))
            }

            Button(
                onClick = onComplete,
                enabled = checked.size == reading.points.size,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pk.Crisp, contentColor = Pk.DeepDark, disabledContainerColor = Pk.Crisp.copy(alpha = .14f), disabledContentColor = Pk.Crisp.copy(alpha = .38f)),
            ) {
                Text(if (checked.size == reading.points.size) "Complete day $day" else "Pray each point to complete", fontFamily = UtilitySans, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun timeLabel(hour: Int, minute: Int): String {
    val h = if (hour % 12 == 0) 12 else hour % 12
    return "%d:%02d %s".format(Locale.US, h, minute, if (hour < 12) "AM" else "PM")
}
