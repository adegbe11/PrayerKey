package com.prayerkey.manna.ui.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import com.prayerkey.manna.reminder.ReminderReceiver
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private data class FaithDay(
    val title: String,
    val reference: String,
    val scripture: String,
    val reflection: String,
    val actions: List<String>,
    val closing: String,
)

private data class FaithProgramme(
    val title: String,
    val eyebrow: String,
    val description: String,
    val choices: List<String>,
    val days: Int,
    val icon: ImageVector,
    val reminderLabel: String,
    val content: List<FaithDay>,
)

private val coreDays = listOf(
    FaithDay("Begin with surrender", "Psalm 37:5", "Commit your way to the Lord; trust in him, and he will act.", "", listOf("Father, I thank You because You have remained faithful through every season.", "Lord, I bring this need before You plainly; nothing about it is hidden from Your sight.", "I surrender the outcome, the timing, and every fear attached to it into Your hands."), "Father, I entrust this matter to You. Order my steps and let Your will be done. In Jesus' name, Amen."),
    FaithDay("Ask with confidence", "Hebrews 4:16", "Let us approach the throne of grace with boldness, so that we may receive mercy.", "", listOf("Merciful Father, look upon me with compassion and answer me according to Your lovingkindness.", "Lord, fulfil Your Word in my life and let Your truth prevail over every contrary report.", "Give me grace to wait without fear, bitterness, or unbelief."), "Lord, I come because You invited me. Give me mercy and help for today. In Jesus' name, Amen."),
    FaithDay("Stand on the Word", "Isaiah 55:11", "My word that comes from my mouth will not return to me empty.", "", listOf("Father, establish Your written Word as the final authority over this situation.", "Let every promise You have spoken produce its appointed fruit in my life.", "Strengthen my faith so that my words and choices agree with Your truth."), "Let Your Word accomplish Your purpose in me and in this situation. In Jesus' name, Amen."),
    FaithDay("Pray without fear", "Philippians 4:6–7", "In everything, through prayer and petition with thanksgiving, present your requests to God.", "", listOf("Father, I reject fear and place every anxious thought under the authority of Jesus Christ.", "Thank You for Your presence, Your mercy, and the help You have already provided.", "Let Your peace guard my heart and mind against panic, confusion, and despair."), "God of peace, guard my heart and mind in Christ Jesus. Amen."),
    FaithDay("Wait with strength", "Isaiah 40:31", "Those who trust in the Lord will renew their strength.", "", listOf("Father, I release every deadline I have allowed to become greater than my trust in You.", "Renew my strength for today's responsibilities and keep discouragement far from me.", "Show me the faithful action I should take while I wait for Your answer."), "Renew my strength, Lord. Keep me faithful while I wait. In Jesus' name, Amen."),
    FaithDay("Walk in obedience", "James 1:22", "Be doers of the word and not hearers only.", "", listOf("Holy Spirit, remind me clearly of every truth You have shown me.", "Give me courage to obey without delay, excuse, or compromise.", "Order my time and my steps so that good intentions become faithful action."), "Give me grace to obey quickly, humbly, and completely. In Jesus' name, Amen."),
    FaithDay("Finish with praise", "Psalm 126:3", "The Lord has done great things for us; we are joyful.", "", listOf("Father, thank You for every change You have begun in me during this journey.", "I praise You for prayers answered, strength supplied, and mercy renewed.", "Preserve the fruit of this time with You and keep my heart steadfast."), "Thank You for carrying me. Keep this work alive in me beyond this programme. In Jesus' name, Amen."),
)

private fun programme(name: String): FaithProgramme = when (name) {
    "Prayer points" -> FaithProgramme(name, "PRAY BY SITUATION", "Choose what you are standing for. Each day gives Scripture and focused prayer points to pray aloud.", listOf("Marriage and family", "Healing and health", "Work and provision", "Open doors", "Children", "Protection", "Freedom from fear", "Justice and restoration", "Purpose and direction", "Spiritual growth"), 21, Icons.Outlined.FormatListBulleted, "Prayer points", coreDays)
    "Fasting" -> FaithProgramme(name, "PRAYER WITH CONSECRATION", "Choose a safe fast, define its purpose, and follow a daily rhythm of Scripture, prayer, reflection, and breaking the fast wisely.", listOf("Single day", "Timed hours", "Daniel fast", "Three-day fast", "Seven-day fast", "Esther fast", "Lent", "First fruits", "Church-wide", "Custom"), 7, Icons.Outlined.WaterDrop, "Fasting prayer", coreDays.mapIndexed { i, d -> d.copy(title = listOf("Prepare the heart", "Hunger for God", "Repent and return", "Pray for others", "Listen in stillness", "Receive direction", "Break with thanksgiving")[i]) })
    "Memory verse" -> FaithProgramme(name, "HIDE THE WORD", "Learn one passage through reading, listening, phrase practice, missing words, reciting, and spaced review.", listOf("Philippians 4:6–7", "Psalm 23:1–4", "Proverbs 3:5–6", "Romans 8:28", "Isaiah 41:10", "John 3:16"), 7, Icons.Outlined.BookmarkBorder, "Memory verse review", coreDays.mapIndexed { i, d -> d.copy(title = listOf("Read slowly", "Listen and repeat", "Learn the first phrase", "Hide key words", "Write from memory", "Recite without help", "Final review")[i]) })
    "Midnight prayer" -> FaithProgramme(name, "THE NIGHT WATCH", "A focused seven-night watch. Complete tonight's Scripture, prayer points, and declaration before the next watch unlocks.", listOf("Personal consecration", "Family", "Deliverance", "Open doors", "Healing", "Nation and church", "Thanksgiving"), 7, Icons.Outlined.DarkMode, "Midnight watch", coreDays)
    "Warfare prayer" -> FaithProgramme(name, "STAND IN SCRIPTURE", "Pray against fear, temptation, oppression, and destructive patterns. People are never treated as the enemy.", listOf("Freedom from fear", "Breaking destructive habits", "Protection of my home", "Peace over confusion", "Strength against temptation", "Justice and vindication"), 7, Icons.Outlined.Shield, "Warfare prayer", coreDays)
    else -> FaithProgramme("Declarations", "SPEAK THE WORD", "Build seven days of Scripture-shaped declarations. These words express faith; they do not replace prayer, wisdom, or action.", listOf("Identity in Christ", "Peace", "Courage", "Wisdom", "Provision", "Healing", "Family"), 7, Icons.Outlined.Campaign, "Daily declaration", coreDays)
}

@Composable
internal fun FaithProgrammeMachine(title: String, onClose: () -> Unit) {
    val spec = remember(title) { programme(title) }
    val context = LocalContext.current
    val prefs = remember(title) { context.getSharedPreferences("faith_programme_${title.lowercase().replace(' ', '_')}", Context.MODE_PRIVATE) }
    var selected by remember { mutableStateOf(prefs.getString("selected", "").orEmpty()) }
    var started by remember { mutableStateOf(prefs.getBoolean("started", false)) }
    var openDay by remember { mutableStateOf<Int?>(null) }
    var done by remember { mutableStateOf(prefs.getStringSet("done", emptySet()).orEmpty().mapNotNull(String::toIntOrNull).toSet()) }
    var hour by remember { mutableIntStateOf(prefs.getInt("hour", if (title == "Midnight prayer") 0 else 6)) }
    var startedOn by remember { mutableStateOf(LocalDate.parse(prefs.getString("started_on", LocalDate.now().toString()))) }
    var lastCompletedOn by remember { mutableStateOf(prefs.getString("last_completed_on", "").orEmpty()) }

    BackHandler {
        if (openDay != null) openDay = null else onClose()
    }

    openDay?.let { day ->
        val base = spec.content[(day - 1) % spec.content.size]
        val personalised = if (selected.isBlank()) base else base.copy(closing = "Father, concerning $selected: ${base.closing}")
        FaithProgrammeDay(spec, day, personalised, onBack = { openDay = null }) {
            done = done + day
            lastCompletedOn = LocalDate.now().toString()
            prefs.edit().putStringSet("done", done.map(Int::toString).toSet()).putString("last_completed_on", lastCompletedOn).apply()
            openDay = null
        }
        return
    }

    if (!started) {
        ProgrammeSetup(spec, selected, hour, onSelect = { selected = it }, onHour = { hour = it }, onClose = onClose) {
            if (selected.isNotBlank()) {
                started = true
                startedOn = LocalDate.now()
                lastCompletedOn = ""
                prefs.edit().putString("selected", selected).putInt("hour", hour).putBoolean("started", true).putString("started_on", startedOn.toString()).putString("last_completed_on", "").apply()
                ReminderReceiver.schedule(context, hour, 0, true)
            }
        }
    } else {
        ProgrammeHome(spec, selected, done, startedOn, lastCompletedOn, onClose, onReset = {
            prefs.edit().clear().apply(); selected = ""; done = emptySet(); started = false
        }) { openDay = it }
    }
}

/**
 * The programme landing.
 *
 * It was a white form reached by tapping a full-bleed artwork card — the only
 * screen in the app that looked like a settings pane, so the transition into it
 * fell off a cliff. It also said the programme's name three times before any
 * content: in the bar, as a gold eyebrow, and as the title.
 *
 * It now opens the way every other surface here opens: artwork, the name over
 * it, and the commitment in the same breath — how many days, and that a
 * reminder will be set — so nobody starts a 21-day watch without knowing it is
 * 21 days.
 */
@Composable
private fun ProgrammeSetup(spec: FaithProgramme, selected: String, hour: Int, onSelect: (String) -> Unit, onHour: (Int) -> Unit, onClose: () -> Unit, onStart: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                Box(Modifier.fillMaxWidth().height(272.dp)) {
                    Image(
                        painterResource(programmeArt(spec.title)), null,
                        Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                0f to Pk.DeepDark.copy(alpha = .34f),
                                .5f to Pk.DeepDark.copy(alpha = .55f),
                                1f to Pk.DeepDark.copy(alpha = .92f),
                            ),
                        ),
                    )
                    Box(
                        Modifier.statusBarsPadding().padding(start = 8.dp, top = 4.dp)
                            .size(42.dp).clip(CircleShape).clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Close, "Back", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 22.dp, vertical = 20.dp)) {
                        Text(
                            spec.title, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                            fontSize = 38.sp, lineHeight = 42.sp, color = Color.White,
                        )
                        Text(
                            "${spec.days} days \u00B7 one each day \u00B7 reminder set",
                            Modifier.padding(top = 8.dp), fontFamily = UtilitySans,
                            fontSize = 12.sp, color = Pk.GoldLight,
                        )
                    }
                }

                Column(Modifier.padding(horizontal = 22.dp)) {
                    Text(
                        "CHOOSE YOUR FOCUS", Modifier.padding(top = 24.dp),
                        fontFamily = UtilitySans, fontWeight = FontWeight.Bold,
                        fontSize = 10.sp, letterSpacing = 1.2.sp, color = Pk.Muted,
                    )
                    spec.choices.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { choice -> ChoiceCard(choice, selected == choice, Modifier.weight(1f)) { onSelect(choice) } }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    Text(
                        "REMIND ME", Modifier.padding(top = 26.dp), fontFamily = UtilitySans,
                        fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.2.sp, color = Pk.Muted,
                    )
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "MIDNIGHT", 6 to "MORNING", 12 to "MIDDAY", 21 to "EVENING").forEach { (value, label) ->
                            FilterChip(selected = hour == value, onClick = { onHour(value) }, label = { Text(label, fontSize = 9.sp) })
                        }
                    }
                    if (spec.title == "Fasting") Text("If you are pregnant, under 18, have diabetes, take medication, or have an eating-disorder history, speak with a qualified clinician before fasting. Choose a non-food fast when appropriate.", Modifier.padding(top = 20.dp), fontFamily = UtilitySans, fontSize = 11.sp, lineHeight = 17.sp, color = Pk.Muted)
                    Spacer(Modifier.height(24.dp))
                }
            }

            /* The button sat grey and disabled on arrival with nothing saying
               why. It now names what it is waiting for, and then names the
               choice back to the user. */
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp).navigationBarsPadding()) {
                Button(
                    onStart, enabled = selected.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood),
                ) {
                    Text(
                        if (selected.isBlank()) "CHOOSE A FOCUS TO BEGIN" else "BEGIN \u00B7 ${selected.uppercase()}",
                        fontFamily = UtilitySans, fontWeight = FontWeight.Bold,
                        fontSize = 11.sp, letterSpacing = .7.sp,
                    )
                }
            }
        }
    }
}

/** Each programme gets a face, so the landing continues the card that opened it. */
private fun programmeArt(title: String): Int = when (title) {
    "Prayer points" -> com.prayerkey.manna.R.drawable.discovery_cover_1
    "Fasting" -> com.prayerkey.manna.R.drawable.discovery_cover_3
    "Memory verse" -> com.prayerkey.manna.R.drawable.discovery_cover_2
    "Midnight prayer" -> com.prayerkey.manna.R.drawable.discovery_cover_4
    "Warfare prayer" -> com.prayerkey.manna.R.drawable.discovery_cover_5
    else -> com.prayerkey.manna.R.drawable.discovery_cover_6
}

@Composable
private fun ProgrammeHome(spec: FaithProgramme, selected: String, done: Set<Int>, startedOn: LocalDate, lastCompletedOn: String, onClose: () -> Unit, onReset: () -> Unit, onDay: (Int) -> Unit) {
    val next = (done.size + 1).coerceAtMost(spec.days)
    val calendarDay = (ChronoUnit.DAYS.between(startedOn, LocalDate.now()).toInt() + 1).coerceIn(1, spec.days)
    val availableToday = done.size < spec.days && next <= calendarDay && lastCompletedOn != LocalDate.now().toString()
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            ProgrammeBar(onClose, spec.title)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 10.dp)) {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = Pk.VioletDeep) {
                    Column(Modifier.padding(24.dp)) {
                        Text(spec.eyebrow, fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp, color = Pk.GoldLight)
                        Text(selected, Modifier.padding(top = 8.dp), fontFamily = DisplaySerif, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, color = Pk.Crisp)
                        Text("${done.size} of ${spec.days} days complete", Modifier.padding(top = 18.dp), fontFamily = UtilitySans, fontSize = 12.sp, color = Pk.Crisp.copy(alpha = .75f))
                        LinearProgressIndicator(progress = { done.size.toFloat() / spec.days }, Modifier.fillMaxWidth().padding(top = 10.dp).height(6.dp), color = Pk.GoldLight, trackColor = Pk.Crisp.copy(alpha = .18f))
                    }
                }
                Text("Your days", Modifier.padding(top = 28.dp, bottom = 12.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Pk.Charcoal)
                (1..spec.days).forEach { day ->
                    val locked = day !in done && (day != next || !availableToday)
                    val complete = day in done
                    Surface(Modifier.fillMaxWidth().padding(bottom = 9.dp).clickable(enabled = !locked) { onDay(day) }, shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, Pk.Hair)) {
                        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = if (complete) Pk.Sage else if (day == next) Pk.Oxblood else Pk.Sunken) {
                                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { if (complete) Icon(Icons.Outlined.Check, null, tint = Color.White) else Text(day.toString(), fontWeight = FontWeight.Bold, color = if (day == next) Color.White else Pk.Muted) }
                            }
                            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                                Text("DAY $day", fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Pk.Muted)
                                Text(spec.content[(day - 1) % spec.content.size].title, Modifier.padding(top = 3.dp), fontFamily = BookSerif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (locked) Pk.Muted else Pk.Charcoal)
                            }
                            Icon(if (locked) Icons.Outlined.Lock else Icons.Outlined.ChevronRight, null, tint = Pk.Muted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                TextButton(onClick = onReset, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp)) { Text("Choose another programme", color = Pk.Muted, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun FaithProgrammeDay(spec: FaithProgramme, day: Int, content: FaithDay, onBack: () -> Unit, onComplete: () -> Unit) {
    var checked by remember(day) { mutableStateOf(emptySet<Int>()) }
    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            ProgrammeBar(onBack, "DAY $day OF ${spec.days}")
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                Text(content.title, Modifier.padding(top = 18.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 39.sp, color = Pk.Charcoal)
                Text(content.reference.uppercase(), Modifier.padding(top = 24.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.2.sp, color = Pk.Oxblood)
                Text("“${content.scripture}”", Modifier.padding(top = 10.dp), fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 22.sp, lineHeight = 32.sp, color = Pk.Charcoal)
                HorizontalDivider(Modifier.padding(vertical = 24.dp), color = Pk.Hair)
                Text("Pray through each point", Modifier.padding(bottom = 8.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Pk.GoldDeep)
                content.actions.forEachIndexed { index, action ->
                    Surface(Modifier.fillMaxWidth().padding(top = 9.dp).clickable { checked = if (index in checked) checked - index else checked + index }, shape = RoundedCornerShape(17.dp), color = Color.White, border = BorderStroke(1.dp, if (index in checked) Pk.Sage else Pk.Hair)) {
                        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (index in checked) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null, tint = if (index in checked) Pk.Sage else Pk.Oxblood)
                            Text(action, Modifier.padding(start = 13.dp), fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 20.sp, color = Pk.Charcoal)
                        }
                    }
                }
                Surface(Modifier.fillMaxWidth().padding(top = 24.dp), shape = RoundedCornerShape(22.dp), color = Pk.DeepDark) {
                    Text(content.closing, Modifier.padding(22.dp), fontFamily = BookSerif, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center, fontSize = 18.sp, lineHeight = 27.sp, color = Pk.Crisp)
                }
                Spacer(Modifier.height(22.dp))
            }
            Button(onComplete, enabled = checked.isNotEmpty(), modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Pk.Oxblood)) {
                Text(if (checked.isNotEmpty()) "Finish day $day" else "Pray at least one point", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable private fun ProgrammeBar(onBack: () -> Unit, title: String) = Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
    IconButton(onBack) { Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Charcoal, modifier = Modifier.size(18.dp)) }
    Text(title.uppercase(), Modifier.weight(1f), textAlign = TextAlign.Center, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.3.sp, color = Pk.Charcoal)
    Spacer(Modifier.width(48.dp))
}

/* Ten identical boxes of 12sp grey gave the eye nothing to choose between.
   Bigger type, and a selected state visible from across the room. */
@Composable private fun ChoiceCard(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) = Surface(
    modifier.height(84.dp).clickable(onClick = onClick),
    shape = RoundedCornerShape(18.dp),
    color = if (selected) Pk.Oxblood else Color.White,
    border = BorderStroke(if (selected) 0.dp else 1.dp, if (selected) Pk.Oxblood else Pk.Hair),
    shadowElevation = if (selected) 6.dp else 0.dp,
) {
    Box(Modifier.padding(horizontal = 15.dp, vertical = 13.dp), contentAlignment = Alignment.CenterStart) {
        Text(
            label, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp, lineHeight = 20.sp,
            color = if (selected) Color.White else Pk.Charcoal,
        )
    }
}
