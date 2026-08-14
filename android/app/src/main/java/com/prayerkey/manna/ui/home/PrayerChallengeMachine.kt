package com.prayerkey.manna.ui.home

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.reminder.ReminderReceiver
import com.prayerkey.manna.ui.theme.*

private data class ChallengeSeed(val name: String, val prayer: String)
private data class ChallengeReading(val theme: String, val reference: String, val reflection: String)

private val challengeSeeds = listOf(
    ChallengeSeed("Marriage", "Father, establish love, patience, truth and faithfulness in my marriage. Heal what is wounded and teach us to honour You and one another."),
    ChallengeSeed("Family", "Father, let Your peace rest on my family. Protect us, restore unity and draw every heart closer to You."),
    ChallengeSeed("Healing", "Lord, I bring my body and mind before You. Give me strength, wisdom for treatment and grace for each step of healing."),
    ChallengeSeed("Open doors", "Lord, guide me to the doors You have prepared. Close every wrong path and give me courage to obey Your direction."),
    ChallengeSeed("Work and provision", "Father, bless the work of my hands. Give me diligence, favour, honest provision and wisdom to steward what You provide."),
    ChallengeSeed("Children", "Lord, guard my children, shape their character and lead them in wisdom, truth and peace."),
    ChallengeSeed("Freedom from fear", "God of peace, quiet every fearful thought. Help me trust Your presence and act today with courage and wisdom."),
    ChallengeSeed("Purpose and direction", "Father, order my steps. Make my assignment clear and give me grace to do the next faithful thing."),
)

private val readings = listOf(
    ChallengeReading("Ask", "Matthew 7:7–8", "Bring the need plainly to God. What are you truly asking Him for?"),
    ChallengeReading("Trust", "Proverbs 3:5–6", "Where are you relying only on your own understanding?"),
    ChallengeReading("Peace", "Philippians 4:6–7", "Name the anxiety you need to exchange for prayer today."),
    ChallengeReading("Strength", "Isaiah 40:29–31", "What would waiting on God look like in this situation?"),
    ChallengeReading("Wisdom", "James 1:5", "What decision needs wisdom rather than haste?"),
    ChallengeReading("Faith", "Mark 11:22–24", "Pray with faith while surrendering the outcome to God's wisdom."),
    ChallengeReading("Perseverance", "Luke 18:1–8", "What has made you tired of praying? Begin again today."),
    ChallengeReading("Grace", "2 Corinthians 12:9", "Where do you need God's strength in your weakness?"),
    ChallengeReading("Courage", "Joshua 1:9", "What faithful action have you delayed because of fear?"),
    ChallengeReading("Rest", "Matthew 11:28–30", "Release the burden you were never meant to carry alone."),
)

@Composable
internal fun PrayerChallengeMachine(onClose: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prayer_challenge_machine", Context.MODE_PRIVATE) }
    var active by remember { mutableStateOf(prefs.getBoolean("active", false)) }
    var situation by remember { mutableStateOf(prefs.getString("situation", "Marriage").orEmpty()) }
    var custom by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(prefs.getInt("duration", 30)) }
    var hour by remember { mutableIntStateOf(prefs.getInt("hour", 6)) }
    var minute by remember { mutableIntStateOf(prefs.getInt("minute", 0)) }
    var done by remember { mutableStateOf(prefs.getStringSet("done", emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.toSet()) }
    var openDay by remember { mutableStateOf<Int?>(null) }
    val seed = challengeSeeds.firstOrNull { it.name == situation } ?: ChallengeSeed(situation, "Father, I place $situation before You. Lead me by Your Word, strengthen my faith and let Your will be done.")

    openDay?.let { day ->
        ChallengeDayPage(day, duration, seed, hour, minute, day in done, onBack = { openDay = null }) {
            done = done + day
            prefs.edit().putStringSet("done", done.map(Int::toString).toSet()).apply()
            openDay = null
        }
        return
    }

    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Close") }
                Text("PRAYER CHALLENGE", Modifier.weight(1f), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp, color = Pk.Oxblood)
                if (active) Text("${done.size}/$duration", color = Pk.Oxblood, fontWeight = FontWeight.Bold)
            }
            if (!active) {
                Column(Modifier.weight(1f).padding(horizontal = 22.dp)) {
                    Text("What are you praying for?", fontFamily = DisplaySerif, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, color = Pk.Charcoal)
                    Text("PrayerKey will build your daily Scripture, reflection, prayer time and complete prayer.", Modifier.padding(top = 10.dp, bottom = 18.dp), fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, color = Pk.Muted)
                    LazyVerticalGrid(GridCells.Fixed(2), Modifier.height(290.dp), horizontalArrangement = Arrangement.spacedBy(9.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(challengeSeeds) { item ->
                            val selected = situation == item.name
                            Surface(onClick = { situation = item.name }, shape = RoundedCornerShape(16.dp), color = if (selected) Pk.Oxblood else Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Pk.Oxblood else Pk.Hair)) {
                                Text(item.name, Modifier.padding(14.dp), color = if (selected) Color.White else Pk.Charcoal, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                    OutlinedTextField(custom, { custom = it }, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Or write your own prayer need") }, singleLine = true, shape = RoundedCornerShape(16.dp))
                    Text("CHALLENGE LENGTH", Modifier.padding(top = 18.dp, bottom = 8.dp), fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold, color = Pk.Muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(7, 14, 21, 30).forEach { n -> FilterChip(duration == n, { duration = n }, { Text("$n days") }) } }
                    OutlinedButton(onClick = { TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, false).show() }, Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(16.dp)) {
                        Icon(Icons.Outlined.Schedule, null); Spacer(Modifier.width(8.dp)); Text("Pray daily at ${timeLabel(hour, minute)}")
                    }
                }
                Button(onClick = {
                    if (custom.isNotBlank()) situation = custom.trim()
                    active = true; done = emptySet()
                    prefs.edit().putBoolean("active", true).putString("situation", if (custom.isNotBlank()) custom.trim() else situation).putInt("duration", duration).putInt("hour", hour).putInt("minute", minute).putStringSet("done", emptySet()).apply()
                    ReminderReceiver.schedule(context, hour, minute, true)
                }, Modifier.fillMaxWidth().padding(20.dp).height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) { Text("BUILD MY CHALLENGE", fontWeight = FontWeight.Bold) }
            } else {
                Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
                    Text("$duration Days for $situation", fontFamily = DisplaySerif, fontSize = 31.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold)
                    Text("Complete today's prayer to unlock tomorrow.", Modifier.padding(top = 7.dp, bottom = 16.dp), color = Pk.Muted)
                    LinearProgressIndicator({ done.size.toFloat() / duration }, Modifier.fillMaxWidth().height(7.dp), color = Pk.Oxblood, trackColor = Pk.Hair)
                    LazyVerticalGrid(GridCells.Fixed(4), Modifier.weight(1f).padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items((1..duration).toList()) { day ->
                            val completed = day in done
                            val unlocked = day <= done.size + 1
                            Surface(onClick = { if (unlocked) openDay = day }, enabled = unlocked, shape = RoundedCornerShape(16.dp), color = when { completed -> Pk.Oxblood; unlocked -> Color.White; else -> Pk.Hair.copy(.55f) }) {
                                Column(Modifier.aspectRatio(.82f).padding(9.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (!unlocked) Icon(Icons.Outlined.Lock, null, Modifier.size(15.dp), tint = Pk.Muted)
                                    Text("$day", fontFamily = DisplaySerif, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = if (completed) Color.White else Pk.Charcoal)
                                    Text(readings[(day - 1) % readings.size].theme, fontSize = 9.sp, maxLines = 1, color = if (completed) Color.White.copy(.8f) else Pk.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun ChallengeDayPage(day: Int, total: Int, seed: ChallengeSeed, hour: Int, minute: Int, complete: Boolean, onBack: () -> Unit, onComplete: () -> Unit) {
    val reading = readings[(day - 1) % readings.size]
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Challenge") }; Text("DAY $day OF $total", color = Pk.Oxblood, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp) }
            Column(Modifier.weight(1f).padding(horizontal = 24.dp)) {
                Text(reading.theme, fontFamily = DisplaySerif, fontSize = 39.sp, fontWeight = FontWeight.Bold)
                Text("FOR ${seed.name.uppercase()}", color = Pk.GoldDeep, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                DailyBlock("BIBLE TO READ", reading.reference, Icons.Outlined.MenuBook)
                DailyBlock("REFLECTION", reading.reflection, Icons.Outlined.AutoAwesome)
                DailyBlock("TIME TO PRAY", timeLabel(hour, minute), Icons.Outlined.Schedule)
                DailyBlock("PRAYER TO SAY", "${seed.prayer} Today, help me walk in ${reading.theme.lowercase()} and obey what Your Word shows me. In Jesus' name, amen.", Icons.Outlined.SelfImprovement)
            }
            Button(
                onClick = onComplete,
                enabled = !complete,
                modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood),
            ) { Text(if (complete) "DAY COMPLETED" else "I HAVE PRAYED · COMPLETE DAY $day", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        }
    }
}

@Composable private fun DailyBlock(label: String, text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(Modifier.fillMaxWidth().padding(top = 14.dp), shape = RoundedCornerShape(20.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Pk.Hair)) {
        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.Top) { Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(21.dp)); Column(Modifier.padding(start = 13.dp)) { Text(label, color = Pk.Oxblood, fontSize = 9.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Bold); Text(text, Modifier.padding(top = 6.dp), fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 24.sp, color = Pk.Charcoal) } }
    }
}

private fun timeLabel(hour: Int, minute: Int): String { val h = if (hour % 12 == 0) 12 else hour % 12; return "%d:%02d %s".format(h, minute, if (hour < 12) "AM" else "PM") }
