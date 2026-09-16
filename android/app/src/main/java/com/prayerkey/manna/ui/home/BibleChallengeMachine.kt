package com.prayerkey.manna.ui.home

import android.app.TimePickerDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.HapticFeedbackConstants
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.reminder.ReminderReceiver
import com.prayerkey.manna.share.ChallengeBadgeRenderer
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

private data class ReadingPlan(val id: String, val title: String, val detail: String, val chapters: Int, val minutes: Int, val days: Int)
private data class BiblePortion(val lane: String, val book: String, val chapter: Int)

private val biblePlans = listOf(
    ReadingPlan("mindset", "7-day mindset shift", "One five-minute chapter each day to renew your thinking.", 1, 5, 7),
    ReadingPlan("blended", "Whole Bible", "Read the Old Testament, New Testament and Psalms together.", 3, 14, 365),
    ReadingPlan("gospels", "The Gospels", "Read the life and teaching of Jesus from Matthew to John.", 1, 9, 89),
    ReadingPlan("new_testament", "New Testament", "Read from Matthew to Revelation in ninety days.", 3, 12, 90),
    ReadingPlan("slow", "One chapter a day", "Read one clear chapter each day.", 1, 5, 365),
)

private val oldBooks = listOf("Genesis" to 50, "Exodus" to 40, "Leviticus" to 27, "Numbers" to 36, "Deuteronomy" to 34, "Joshua" to 24, "Judges" to 21, "Ruth" to 4, "1 Samuel" to 31, "2 Samuel" to 24, "1 Kings" to 22, "2 Kings" to 25, "1 Chronicles" to 29, "2 Chronicles" to 36, "Ezra" to 10, "Nehemiah" to 13, "Esther" to 10, "Job" to 42, "Psalms" to 150, "Proverbs" to 31, "Ecclesiastes" to 12, "Song of Solomon" to 8, "Isaiah" to 66, "Jeremiah" to 52, "Lamentations" to 5, "Ezekiel" to 48, "Daniel" to 12, "Hosea" to 14, "Joel" to 3, "Amos" to 9, "Obadiah" to 1, "Jonah" to 4, "Micah" to 7, "Nahum" to 3, "Habakkuk" to 3, "Zephaniah" to 3, "Haggai" to 2, "Zechariah" to 14, "Malachi" to 4)
private val gospelBooks = listOf("Matthew" to 28, "Mark" to 16, "Luke" to 24, "John" to 21)
private val newBooks = gospelBooks + listOf("Acts" to 28, "Romans" to 16, "1 Corinthians" to 16, "2 Corinthians" to 13, "Galatians" to 6, "Ephesians" to 6, "Philippians" to 4, "Colossians" to 4, "1 Thessalonians" to 5, "2 Thessalonians" to 3, "1 Timothy" to 6, "2 Timothy" to 4, "Titus" to 3, "Philemon" to 1, "Hebrews" to 13, "James" to 5, "1 Peter" to 5, "2 Peter" to 3, "1 John" to 5, "2 John" to 1, "3 John" to 1, "Jude" to 1, "Revelation" to 22)

private fun chapterAt(books: List<Pair<String, Int>>, offset: Int, lane: String): BiblePortion {
    var remaining = offset.mod(books.sumOf { it.second })
    books.forEach { (book, chapters) ->
        if (remaining < chapters) return BiblePortion(lane, book, remaining + 1)
        remaining -= chapters
    }
    return BiblePortion(lane, books.first().first, 1)
}

private fun portionsFor(plan: String, day: Int): List<BiblePortion> {
    return when (plan) {
        "mindset" -> listOf(listOf(BiblePortion("RENEW YOUR MIND", "Romans", 12), BiblePortion("PEACE", "Philippians", 4), BiblePortion("WISDOM", "James", 1), BiblePortion("COURAGE", "Joshua", 1), BiblePortion("TRUST", "Proverbs", 3), BiblePortion("IDENTITY", "Ephesians", 1), BiblePortion("HOPE", "Psalms", 42))[(day - 1).mod(7)])
        "gospels" -> listOf(chapterAt(gospelBooks, day - 1, "GOSPEL"))
        "new_testament" -> (0..2).map { chapterAt(newBooks, (day - 1) * 3 + it, "NEW TESTAMENT") }
        "slow" -> listOf(chapterAt(oldBooks + newBooks, day - 1, "BIBLE"))
        else -> listOf(chapterAt(oldBooks, day - 1, "OLD TESTAMENT"), chapterAt(newBooks, day - 1, "NEW TESTAMENT"), BiblePortion("PSALM", "Psalms", ((day - 1) % 150) + 1))
    }
}

@Composable
internal fun BibleChallengeMachine(onClose: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bible_challenge_machine", Context.MODE_PRIVATE) }
    var active by remember { mutableStateOf(prefs.getBoolean("active", false)) }
    var step by remember { mutableIntStateOf(0) }
    var planId by remember { mutableStateOf(prefs.getString("plan", "blended").orEmpty()) }
    var hour by remember { mutableIntStateOf(prefs.getInt("hour", 6)) }
    var minute by remember { mutableIntStateOf(prefs.getInt("minute", 30)) }
    var startedOn by remember { mutableStateOf(LocalDate.parse(prefs.getString("started_on", LocalDate.now().toString()))) }
    var lastCompletedOn by remember { mutableStateOf(prefs.getString("last_completed_on", "").orEmpty()) }
    var completedDays by remember {
        mutableStateOf(prefs.getStringSet("completed", emptySet()).orEmpty().mapNotNull(String::toIntOrNull).toSet())
    }
    var readerDay by remember { mutableStateOf<Int?>(null) }

    BackHandler {
        when {
            readerDay != null -> readerDay = null
            !active && step > 0 -> step--
            else -> onClose()
        }
    }
    val plan = biblePlans.firstOrNull { it.id == planId } ?: biblePlans.first()

    readerDay?.let { day ->
        BibleChallengeReader(day, plan, portionsFor(plan.id, day), onBack = { readerDay = null }) {
            completedDays = completedDays + day
            lastCompletedOn = LocalDate.now().toString()
            prefs.edit().putStringSet("completed", completedDays.map(Int::toString).toSet()).putString("last_completed_on", lastCompletedOn).apply()
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
            onPlan = { planId = it },
            onTime = { TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, false).show() },
            onBack = { if (step > 0) step-- else onClose() },
            onClose = onClose,
            onContinue = {
                if (step < 2) step++ else {
                    active = true
                    completedDays = emptySet()
                    startedOn = LocalDate.now()
                    lastCompletedOn = ""
                    prefs.edit().putBoolean("active", true).putString("plan", planId).putInt("hour", hour)
                        .putInt("minute", minute).putString("started_on", startedOn.toString()).putString("last_completed_on", "").putStringSet("completed", emptySet()).apply()
                    ReminderReceiver.schedule(context, hour, minute, true)
                }
            },
        )
    } else {
        BibleChallengeLanding(plan, hour, minute, completedDays, startedOn, lastCompletedOn, onClose, onUseGrace = {
            val month = java.time.YearMonth.now().toString()
            if (prefs.getString("grace_month", "") != month) {
                val missed = completedDays.size + 1
                completedDays = completedDays + missed
                prefs.edit().putStringSet("completed", completedDays.map(Int::toString).toSet()).putString("grace_month", month).apply()
            }
        }) { readerDay = it }
    }
}

@Composable
private fun BibleChallengeSetup(
    step: Int,
    plan: ReadingPlan,
    hour: Int,
    minute: Int,
    onPlan: (String) -> Unit,
    onTime: () -> Unit,
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
                    repeat(3) { index -> Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(if (index <= step) Pk.Crisp else Pk.Crisp.copy(alpha = .18f))) }
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
                    else -> {
                        BibleSetupHeading("Your plan is ready.", "Finish today's reading before tomorrow's reading opens.")
                        Column(Modifier.fillMaxWidth().padding(top = 25.dp).clip(RoundedCornerShape(20.dp)).background(Pk.Gold.copy(alpha = .12f)).padding(20.dp)) {
                            Text("YOUR READING PLAN", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp)
                            Text(plan.title, color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 28.sp, modifier = Modifier.padding(top = 9.dp))
                            Text("${timeLabelBible(hour, minute)}  ·  KJV  ·  ${plan.minutes} MIN", color = Pk.Crisp.copy(alpha = .6f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = .8.sp, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            }

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pk.Crisp, contentColor = Pk.Oxblood),
            ) { Text(if (step == 2) "Begin day 1" else "Continue", fontFamily = UtilitySans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun BibleSetupHeading(title: String, subtitle: String) {
    Text(title, color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 35.sp, lineHeight = 39.sp, modifier = Modifier.padding(top = 18.dp))
}

@Composable
private fun BibleChallengeLanding(
    plan: ReadingPlan,
    hour: Int,
    minute: Int,
    completed: Set<Int>,
    startDate: LocalDate,
    lastCompletedOn: String,
    onClose: () -> Unit,
    onUseGrace: () -> Unit,
    onRead: (Int) -> Unit,
) {
    val context = LocalContext.current
    val nextDay = completed.size + 1
    val calendarDay = (ChronoUnit.DAYS.between(startDate, LocalDate.now()).toInt() + 1).coerceIn(1, plan.days)
    val day = nextDay.coerceAtMost(plan.days)
    val finished = completed.size >= plan.days
    val availableToday = !finished && nextDay <= calendarDay && lastCompletedOn != LocalDate.now().toString()
    val missedDay = calendarDay > nextDay
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
                Text(plan.title.uppercase(Locale.getDefault()), color = Pk.GoldLight.copy(alpha = .7f), fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 22.dp))
                Text(if (finished) "Plan complete." else "Day $day of ${plan.days}.", color = Pk.Crisp, fontFamily = DisplaySerif, fontSize = 35.sp, modifier = Modifier.padding(top = 8.dp))
                Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    BibleStat((completed.size * plan.chapters).toString(), "CHAPTERS")
                    BibleStat("${(completed.size / plan.days.toFloat() * 100).toInt()}%", "COMPLETE")
                    BibleStat((plan.days - completed.size).coerceAtLeast(0).toString(), "DAYS LEFT")
                }
                LinearProgressIndicator(
                    progress = { completed.size / plan.days.toFloat() },
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 12.dp).height(3.dp).clip(CircleShape),
                    color = Pk.Gold,
                    trackColor = Pk.Crisp.copy(alpha = .16f),
                )
            }

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (finished) Button(
                    onClick = { ChallengeBadgeRenderer.share(context, plan.title, "${plan.days} days completed") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp).height(54.dp),
                    shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.VioletDeep),
                ) { Text("Share my unlocked badge", fontWeight = FontWeight.Bold) }
                if (missedDay) Surface(
                    onClick = onUseGrace,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(20.dp), color = Pk.GoldWash,
                ) { Column(Modifier.padding(18.dp)) { Text("MISSED A DAY?", color = Pk.GoldDeep, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("Keep your progress and continue today. You can do this once each month.", color = Pk.Charcoal, fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 24.sp, modifier = Modifier.padding(top = 7.dp)); Text("KEEP MY PROGRESS", color = Pk.Oxblood, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 11.dp)) } }
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 16.dp).clip(RoundedCornerShape(22.dp))
                        .background(Pk.Sunken).clickable(enabled = availableToday) { onRead(day) }.padding(20.dp),
                ) {
                    Text(if (finished) "PLAN COMPLETE" else if (availableToday) "TODAY  ·  DAY $day  ·  ${plan.minutes} MIN" else "DAY $day OPENS TOMORROW", color = Pk.GoldDeep, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.2.sp)
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
                    Text(if (finished) "Every reading is complete" else if (availableToday) "Open today's reading  →" else "Come back tomorrow", color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Text("Your progress", color = Pk.Charcoal, fontFamily = DisplaySerif, fontSize = 21.sp, modifier = Modifier.padding(horizontal = 20.dp).padding(top = 28.dp, bottom = 10.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp).clip(RoundedCornerShape(20.dp)).background(Pk.Sunken).padding(19.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(58.dp).clip(CircleShape).background(Pk.Oxblood.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                        Text("$day", color = Pk.Oxblood, fontFamily = DisplaySerif, fontSize = 23.sp)
                    }
                    Column(Modifier.padding(start = 16.dp)) {
                        Text("${(plan.days - completed.size).coerceAtLeast(0)} days left", color = Pk.Charcoal, fontFamily = BookSerif, fontSize = 20.sp)
                        Text("Started ${startDate.month.name.lowercase().replaceFirstChar(Char::uppercase)} ${startDate.dayOfMonth}.", color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
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
    val view = LocalView.current
    var selected by remember(day) { mutableIntStateOf(0) }
    var opened by remember(day) { mutableStateOf(setOf(0)) }
    var verses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var speaking by remember { mutableStateOf(false) }
    var spokenIndex by remember { mutableIntStateOf(-1) }
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember { TextToSpeech(context) { status -> ttsReady = status == TextToSpeech.SUCCESS } }
    DisposableEffect(Unit) { onDispose { tts.stop(); tts.shutdown() } }
    LaunchedEffect(ttsReady) { if (ttsReady) { tts.language = Locale.US; tts.setSpeechRate(.86f); tts.setPitch(.92f) } }
    LaunchedEffect(speaking, spokenIndex, verses) {
        if (speaking && ttsReady && spokenIndex in verses.indices) {
            val index = spokenIndex
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onError(utteranceId: String?) = onDone(utteranceId)
                override fun onDone(utteranceId: String?) { Handler(Looper.getMainLooper()).post { if (index < verses.lastIndex) spokenIndex = index + 1 else speaking = false } }
            })
            tts.speak("Verse ${verses[index].verse}. ${verses[index].text}", TextToSpeech.QUEUE_FLUSH, null, "bible-$day-$selected-$index")
        }
    }
    val portion = portions[selected]
    LaunchedEffect(portion.book, portion.chapter) {
        verses = withContext(Dispatchers.IO) { OfflineBible(context).chapter(portion.book, portion.chapter) }
    }

    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBackIosNew, "Plan", tint = Pk.Charcoal, modifier = Modifier.size(15.dp)) }
                Text("DAY $day  ·  ${plan.title.uppercase(Locale.getDefault())}", color = Pk.Faint, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.1.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = { if (speaking) { speaking = false; tts.stop() } else if (verses.isNotEmpty()) { spokenIndex = 0; speaking = true } }) { Icon(Icons.Outlined.Headphones, if (speaking) "Stop listening" else "Listen", tint = if (speaking) Pk.GoldDeep else Pk.Oxblood, modifier = Modifier.size(19.dp)) }
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
                    verses.forEachIndexed { index, verse ->
                        Surface(color = if (speaking && index == spokenIndex) Pk.Oxblood.copy(alpha = .08f) else Color.Transparent, shape = RoundedCornerShape(10.dp)) { Text(
                            "${verse.verse}  ${verse.text}",
                            color = Pk.Charcoal,
                            fontFamily = BookSerif,
                            fontSize = 19.sp,
                            lineHeight = 31.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp).padding(top = if (verse.verse == 1) 14.dp else 6.dp),
                        ) }
                    }
                }
                /* The question sits after the chapter now.
                   Two cards used to stand between the title and the reading: a
                   paragraph on how to approach the book, and this question.
                   You came to read Mark 4; you got told about Mark 4 first.
                   The paragraph is gone, and the question, which is a reflection
                   rather than an instruction, waits until there is something to
                   reflect on. */
                Surface(Modifier.fillMaxWidth().padding(top = 20.dp), shape = RoundedCornerShape(16.dp), color = Pk.Sunken) {
                    Column(Modifier.padding(16.dp)) {
                        Text(bibleChallengePrompt(portion.book), color = Pk.Charcoal, fontFamily = DisplaySerif, fontSize = 20.sp, lineHeight = 27.sp)
                    }
                }
                Surface(Modifier.fillMaxWidth().padding(top = 18.dp), shape = RoundedCornerShape(16.dp), color = Pk.Oxblood) {
                    Column(Modifier.padding(17.dp)) {
                        Text("Prayer", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Father, open my understanding as I read. Show me what this chapter reveals about You, correct what needs to change in me, and help me live what I learn today. Amen.", color = Pk.Crisp, fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            Button(
                onClick = { view.performHapticFeedback(HapticFeedbackConstants.CONFIRM); onComplete() },
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

private fun bibleChallengePrompt(book: String): String = when (book) {
    "Psalms" -> "Which words in this chapter can become your own prayer today?"
    "Proverbs" -> "What is one wise action you can practise before today ends?"
    "Matthew", "Mark", "Luke", "John" -> "What do you notice about Jesus, and how will you respond to Him?"
    "Acts" -> "What does this chapter show you about courageous, Spirit-led faith?"
    else -> "What does this chapter reveal about God, and what faithful response does it invite from you?"
}

private fun timeLabelBible(hour: Int, minute: Int): String {
    val h = if (hour % 12 == 0) 12 else hour % 12
    return "%d:%02d %s".format(Locale.US, h, minute, if (hour < 12) "AM" else "PM")
}
