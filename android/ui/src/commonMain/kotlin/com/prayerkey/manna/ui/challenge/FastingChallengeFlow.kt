package com.prayerkey.manna.ui.challenge

import com.prayerkey.manna.resources.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.FAST_PURPOSES
import com.prayerkey.manna.data.FastLength
import com.prayerkey.manna.data.FastType
import com.prayerkey.manna.data.FastingChallenge
import com.prayerkey.manna.data.FastingStore
import com.prayerkey.manna.data.PrayerRhythm
import com.prayerkey.manna.data.fastMoment
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay

/**
 * The Fasting Challenge.
 *
 * Six questions to set it - how long, what kind, what for, when it starts and
 * ends, when you will pray, and a summary - and then one screen that runs
 * against the clock: how long is left, which day of it you are on, the verse
 * for this hour, and a button to pray. Breaking the fast is its own moment,
 * with a prayer before the first meal rather than after it.
 *
 * A fast is the one thing in this app that touches the body, so the summary
 * says plainly that it does not suit everyone.
 */
private enum class FastStage { Length, Type, Purpose, When, Rhythm, Summary, Active, Pray, Journal, Progress, Breaking, Done }

@Composable
fun FastingChallengeRoute(onClose: () -> Unit) {
    val context = LocalContext.current
    var fast by remember { mutableStateOf(FastingStore.load(context)) }
    var stage by remember {
        mutableStateOf(if (FastingStore.load(context).running) FastStage.Active else FastStage.Length)
    }

    fun keep(next: FastingChallenge) {
        fast = next
        FastingStore.save(context, next)
    }

    BackHandler {
        stage = when (stage) {
            FastStage.Length -> return@BackHandler onClose()
            FastStage.Type -> FastStage.Length
            FastStage.Purpose -> FastStage.Type
            FastStage.When -> FastStage.Purpose
            FastStage.Rhythm -> FastStage.When
            FastStage.Summary -> FastStage.Rhythm
            FastStage.Active -> return@BackHandler onClose()
            else -> FastStage.Active
        }
    }

    Box(Modifier.fillMaxSize().background(if (stage == FastStage.Active || stage == FastStage.Pray) Pk.DeepDark else Pk.Cream)) {
        when (stage) {
            FastStage.Length -> LengthStep(fast, ::keep, onClose) { stage = FastStage.Type }
            FastStage.Type -> TypeStep(fast, ::keep, { stage = FastStage.Length }) { stage = FastStage.Purpose }
            FastStage.Purpose -> PurposeStep(fast, ::keep, { stage = FastStage.Type }) { stage = FastStage.When }
            FastStage.When -> WhenStep(fast, ::keep, { stage = FastStage.Purpose }) { stage = FastStage.Rhythm }
            FastStage.Rhythm -> RhythmStep(fast, ::keep, { stage = FastStage.When }) { stage = FastStage.Summary }
            FastStage.Summary -> FastSummary(
                fast,
                onBack = { stage = FastStage.Rhythm },
                onBegin = {
                    val begun = fast.copy(startedAt = System.currentTimeMillis() / 1000)
                    keep(begun)
                    // the machine speaks at the moments of the fast, if reminders are on
                    if (begun.reminders) com.prayerkey.manna.reminder.FastingMilestoneReceiver.schedule(context, begun)
                    val first = fast.rhythms.minByOrNull { it.hour }
                    com.prayerkey.manna.reminder.ReminderReceiver.scheduleFor(
                        context, "fast", first?.hour ?: 12, 0, fast.reminders && first != null,
                    )
                    stage = FastStage.Active
                },
            )
            FastStage.Active -> ActiveFast(
                fast = fast,
                onClose = onClose,
                onPray = { stage = FastStage.Pray },
                onJournal = { stage = FastStage.Journal },
                onProgress = { stage = FastStage.Progress },
                onBreak = { stage = FastStage.Breaking },
                onDone = { stage = FastStage.Done },
            )
            FastStage.Pray -> FastPrayer(
                fast = fast,
                onClose = { stage = FastStage.Active },
                onPrayed = { keep(fast.copy(prayerSessions = fast.prayerSessions + 1, scriptures = fast.scriptures + 1)) },
            )
            FastStage.Journal -> FastJournal(
                fast = fast,
                onBack = { stage = FastStage.Active },
                onSaved = { keep(fast.copy(entries = fast.entries + 1)) },
            )
            FastStage.Progress -> FastProgress(fast, onBack = { stage = FastStage.Active })
            FastStage.Breaking -> BreakingFast(
                fast = fast,
                onBack = { stage = FastStage.Active },
                onBreak = { day ->
                    val kept = fast.kept + day
                    keep(fast.copy(kept = kept))
                    // every day kept: nothing left for the machine to say
                    if (kept.size >= fast.days) com.prayerkey.manna.reminder.FastingMilestoneReceiver.cancel(context)
                    stage = if (kept.size >= fast.days) FastStage.Done else FastStage.Active
                },
            )
            FastStage.Done -> FastComplete(
                fast = fast,
                onTestimony = { keep(fast.copy(testimony = it)) },
                onAnswered = { keep(fast.copy(answered = it)) },
                onAgain = {
                    com.prayerkey.manna.reminder.ReminderReceiver.scheduleFor(context, "fast", 12, 0, false)
                    com.prayerkey.manna.reminder.FastingMilestoneReceiver.cancel(context)
                    FastingStore.clear(context)
                    fast = FastingStore.load(context)
                    stage = FastStage.Length
                },
                onClose = onClose,
            )
        }
    }
}

// ── 1. how long ─────────────────────────────────────────────────────────

@Composable
private fun OptionRow(
    icon: ImageVector,
    title: String,
    blurb: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(if (selected) Pk.Oxblood.copy(alpha = .07f) else Color.White)
            .border(if (selected) 1.6.dp else 1.dp, if (selected) Pk.Oxblood else Line, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (selected) Pk.Oxblood else Ink, modifier = Modifier.size(21.dp))
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                title,
                color = Ink, fontFamily = UtilitySans, fontSize = 15.5.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
            Text(blurb, Modifier.padding(top = 2.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp)
        }
        if (selected) Icon(Icons.Outlined.CheckCircle, null, tint = Pk.Oxblood, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun LengthStep(
    fast: FastingChallenge,
    keep: (FastingChallenge) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepFrame(
        step = 1, of = 6,
        title = "Choose your fast",
        subtitle = "How long do you want to fast?",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FastLength.entries.forEach { length ->
                OptionRow(
                    icon = when (length) {
                        FastLength.SUNRISE -> Icons.Outlined.WbSunny
                        FastLength.CUSTOM -> Icons.Outlined.EditNote
                        else -> Icons.Outlined.Schedule
                    },
                    title = length.label,
                    blurb = length.blurb,
                    selected = fast.length == length,
                ) { keep(fast.copy(length = length)) }
            }
            if (fast.length == FastLength.CUSTOM) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                        .border(1.dp, Line, RoundedCornerShape(18.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Hours", Modifier.weight(1f), color = Ink, fontFamily = UtilitySans, fontSize = 15.sp)
                    Text(
                        "−",
                        Modifier.clip(CircleShape)
                            .clickable { keep(fast.copy(customHours = (fast.customHours - 6).coerceAtLeast(6))) }
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        color = Pk.Oxblood, fontSize = 20.sp,
                    )
                    Text(
                        "${fast.customHours}",
                        color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 22.sp,
                    )
                    Text(
                        "+",
                        Modifier.clip(CircleShape)
                            .clickable { keep(fast.copy(customHours = (fast.customHours + 6).coerceAtMost(960))) }
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        color = Pk.Oxblood, fontSize = 20.sp,
                    )
                }
            }
        }
    }
}

// ── 2. what kind ────────────────────────────────────────────────────────

@Composable
private fun TypeStep(
    fast: FastingChallenge,
    keep: (FastingChallenge) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepFrame(
        step = 2, of = 6,
        title = "Fasting type",
        subtitle = "Choose the method that is right for you",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FastType.entries.forEach { type ->
                OptionRow(
                    icon = when (type) {
                        FastType.WATER -> Icons.Outlined.Spa
                        FastType.DANIEL -> Icons.Outlined.Restaurant
                        FastType.SOCIAL -> Icons.Outlined.NotificationsNone
                        FastType.CUSTOM -> Icons.Outlined.EditNote
                        else -> Icons.Outlined.Restaurant
                    },
                    title = type.label,
                    blurb = type.blurb,
                    selected = fast.type == type,
                ) { keep(fast.copy(type = type)) }
            }
        }
    }
}

// ── 3. what for ─────────────────────────────────────────────────────────

@Composable
private fun PurposeStep(
    fast: FastingChallenge,
    keep: (FastingChallenge) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepFrame(
        step = 3, of = 6,
        title = "Why are you fasting?",
        subtitle = "What are you seeking God for?",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FAST_PURPOSES.forEach { (name, blurb) ->
                OptionRow(
                    icon = when (name) {
                        "Healing" -> Icons.Outlined.HealthAndSafety
                        "Family" -> Icons.Outlined.Favorite
                        "Gratitude" -> Icons.Outlined.VolunteerActivism
                        "Personal request" -> Icons.Outlined.EditNote
                        else -> Icons.Outlined.LocalFireDepartment
                    },
                    title = name,
                    blurb = blurb,
                    selected = fast.purpose == name,
                ) { keep(fast.copy(purpose = name)) }
            }
            if (fast.purpose == "Personal request") {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                        .border(1.dp, Line, RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                ) {
                    if (fast.note.isBlank()) {
                        Text("What are you asking God for?", color = Pk.Muted, fontFamily = BookSerif, fontSize = 16.sp)
                    }
                    BasicTextField(
                        fast.note, { keep(fast.copy(note = it)) },
                        textStyle = TextStyle(color = Ink, fontFamily = BookSerif, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ── 4. start and end ────────────────────────────────────────────────────

@Composable
private fun WhenStep(
    fast: FastingChallenge,
    keep: (FastingChallenge) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepFrame(
        step = 4, of = 6,
        title = "Set start and end",
        subtitle = "The fast begins when you tap Begin",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FactRow(Icons.Outlined.Schedule, "Length", if (fast.days > 1) "${fast.days} days" else "${fast.hours} hours")
            FactRow(Icons.Outlined.WbSunny, "Starts", "When you begin")
            FactRow(Icons.Outlined.CheckCircle, "Ends", if (fast.days > 1) fast.endLabel() else "in ${fast.hours} hours")

            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Daily break time", color = Ink, fontFamily = UtilitySans, fontSize = 15.sp)
                    Text(
                        "For a partial fast, or one meal a day",
                        Modifier.padding(top = 2.dp),
                        color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp,
                    )
                }
                Switch(
                    checked = fast.dailyBreak,
                    onCheckedChange = { keep(fast.copy(dailyBreak = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Pk.Oxblood),
                )
            }
            if (fast.dailyBreak) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                        .border(1.dp, Line, RoundedCornerShape(18.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Break at", Modifier.weight(1f), color = Ink, fontFamily = UtilitySans, fontSize = 15.sp)
                    Text(
                        "−",
                        Modifier.clip(CircleShape)
                            .clickable { keep(fast.copy(breakHour = (fast.breakHour + 23) % 24)) }
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        color = Pk.Oxblood, fontSize = 20.sp,
                    )
                    Text(
                        fast.breakLabel(),
                        color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                    )
                    Text(
                        "+",
                        Modifier.clip(CircleShape)
                            .clickable { keep(fast.copy(breakHour = (fast.breakHour + 1) % 24)) }
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        color = Pk.Oxblood, fontSize = 20.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun FactRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp))
        Text(label, Modifier.weight(1f).padding(start = 14.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.5.sp)
        Text(value, color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

// ── 5. when you pray ────────────────────────────────────────────────────

@Composable
private fun RhythmStep(
    fast: FastingChallenge,
    keep: (FastingChallenge) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepFrame(
        step = 5, of = 6,
        title = "Prayer rhythm",
        subtitle = "When would you like to pray?",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PrayerRhythm.entries.forEach { rhythm ->
                val on = rhythm in fast.rhythms
                OptionRow(
                    icon = when (rhythm) {
                        PrayerRhythm.MORNING -> Icons.Outlined.WbSunny
                        PrayerRhythm.MIDDAY -> Icons.Outlined.Schedule
                        PrayerRhythm.EVENING -> Icons.Outlined.Spa
                        PrayerRhythm.MIDNIGHT -> Icons.Outlined.AutoStories
                    },
                    title = rhythm.label,
                    blurb = rhythm.blurb,
                    selected = on,
                ) {
                    keep(
                        fast.copy(
                            rhythms = if (on) fast.rhythms - rhythm else fast.rhythms + rhythm,
                        ),
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.NotificationsNone, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp))
                Text(
                    "Remind me to pray",
                    Modifier.weight(1f).padding(start = 14.dp),
                    color = Ink, fontFamily = UtilitySans, fontSize = 15.sp,
                )
                Switch(
                    checked = fast.reminders,
                    onCheckedChange = { keep(fast.copy(reminders = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Pk.Oxblood),
                )
            }
        }
    }
}

// ── 6. the summary ──────────────────────────────────────────────────────

@Composable
private fun FastSummary(fast: FastingChallenge, onBack: () -> Unit, onBegin: () -> Unit) {
    StepFrame(
        step = 6, of = 6,
        title = "Your fast",
        subtitle = "Review, then begin",
        onBack = onBack,
        footer = { PrimaryButton("Begin fast", onClick = onBegin) },
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SummaryLine(Icons.Outlined.Schedule, fast.title, "Duration")
            SummaryLine(Icons.Outlined.Restaurant, fast.type.label, "Fasting type")
            SummaryLine(Icons.Outlined.LocalFireDepartment, fast.note.ifBlank { fast.purpose }, "Purpose")
            SummaryLine(
                Icons.Outlined.VolunteerActivism,
                fast.rhythms.sortedBy { it.hour }.joinToString(", ") { it.label }.ifBlank { "Whenever you can" },
                "Prayer times",
            )
            if (fast.dailyBreak) SummaryLine(Icons.Outlined.WbSunny, fast.breakLabel(), "Daily break time")
        }

        /* A fast is the one thing here that touches the body. Said once,
           plainly, where it is about to be agreed to. */
        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(18.dp))
                .background(Pk.GoldDeep.copy(alpha = .09f)).padding(16.dp),
        ) {
            Icon(Icons.Outlined.HealthAndSafety, null, tint = Pk.GoldDeep, modifier = Modifier.size(19.dp))
            Text(
                "Fasting does not suit everyone. Choose a fast that fits your health, and ask a doctor if you are unsure.",
                Modifier.padding(start = 12.dp),
                color = Ink.copy(alpha = .78f), fontFamily = UtilitySans, fontSize = 13.sp, lineHeight = 19.sp,
            )
        }
    }
}

// ── 7. the fast, running ────────────────────────────────────────────────

@Composable
private fun ActiveFast(
    fast: FastingChallenge,
    onClose: () -> Unit,
    onPray: () -> Unit,
    onJournal: () -> Unit,
    onProgress: () -> Unit,
    onBreak: () -> Unit,
    onDone: () -> Unit,
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis() / 1000) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis() / 1000
        }
    }
    val day = fast.dayOf(now).coerceAtMost(fast.days)
    val moment = remember(day) { fastMoment(fast, day) }
    val remaining = fast.remaining(now)
    val fraction = fast.fraction(now)

    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(Res.drawable.rhythm_morning), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Pk.DeepDark.copy(alpha = .80f), Pk.DeepDark.copy(alpha = .95f)),
                ),
            ),
        )
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onProgress),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.CheckCircle, "Progress", tint = Color.White, modifier = Modifier.size(20.dp)) }
            }

            Text(
                if (fast.days > 1) "Day $day of ${fast.days}" else fast.title,
                Modifier.padding(top = 4.dp),
                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 27.sp,
            )
            Text(
                if (remaining > 0) "Keep going. God is with you." else "Your fast is complete.",
                Modifier.padding(top = 5.dp),
                color = Color.White.copy(alpha = .70f), fontFamily = UtilitySans, fontSize = 13.5.sp,
            )

            Box(Modifier.padding(top = 26.dp).size(236.dp), contentAlignment = Alignment.Center) {
                val sweep by animateFloatAsState(fraction, tween(600), label = "fast")
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color.White.copy(alpha = .14f), style = Stroke(width = 6.dp.toPx()))
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Pk.GoldDeep, Pk.GoldLight, Pk.GoldDeep)),
                        startAngle = -90f, sweepAngle = 360f * sweep, useCenter = false,
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Time remaining",
                        color = Color.White.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 12.sp,
                    )
                    Text(
                        "%02d:%02d:%02d".format(remaining / 3600, (remaining % 3600) / 60, remaining % 60),
                        Modifier.padding(top = 6.dp),
                        color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 36.sp,
                    )
                    Text(
                        "${(fraction * 100).toInt()}% complete",
                        Modifier.padding(top = 6.dp),
                        color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 13.sp,
                    )
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(top = 24.dp).clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = .10f)).padding(18.dp),
            ) {
                Text(
                    "Praying for",
                    color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                )
                Text(
                    moment.focus,
                    Modifier.padding(top = 6.dp),
                    color = Color.White, fontFamily = UtilitySans, fontSize = 16.sp, lineHeight = 23.sp,
                )
                Text(
                    moment.verse,
                    Modifier.padding(top = 14.dp),
                    color = Color.White.copy(alpha = .82f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontSize = 15.5.sp, lineHeight = 24.sp,
                )
                Text(
                    moment.ref,
                    Modifier.padding(top = 6.dp),
                    color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 12.sp,
                )
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton("Pray now", onClick = onPray)

            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FastAction(Icons.Outlined.EditNote, "Journal", Modifier.weight(1f), onJournal)
                FastAction(Icons.Outlined.MenuBook, "Bible", Modifier.weight(1f), onPray)
                FastAction(Icons.Outlined.Favorite, "Strength", Modifier.weight(1f), onPray)
            }

            Box(
                Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .border(1.dp, Color.White.copy(alpha = .30f), RoundedCornerShape(99.dp))
                    .clickable { if (remaining == 0L && fast.kept.size + 1 >= fast.days) onDone() else onBreak() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (fast.days > 1) "Break today's fast" else "Break the fast",
                    color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun FastAction(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = .10f))
            .clickable(onClick = onClick).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = Pk.GoldLight, modifier = Modifier.size(21.dp))
        Text(
            label,
            Modifier.padding(top = 7.dp),
            color = Color.White.copy(alpha = .86f), fontFamily = UtilitySans, fontSize = 12.5.sp,
        )
    }
}

// ── 8. praying through it ───────────────────────────────────────────────

@Composable
private fun FastPrayer(fast: FastingChallenge, onClose: () -> Unit, onPrayed: () -> Unit) {
    val day = fast.dayOf(System.currentTimeMillis() / 1000).coerceAtMost(fast.days)
    val moment = remember(day) { fastMoment(fast, day) }
    var seconds by remember { mutableIntStateOf(0) }
    var praying by remember { mutableStateOf(false) }

    LaunchedEffect(praying) {
        while (praying) {
            delay(1000)
            seconds++
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(Res.drawable.onboarding_pray_v3), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(Modifier.fillMaxSize().background(Pk.DeepDark.copy(alpha = .88f)))
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.align(Alignment.Start).size(40.dp).clip(CircleShape).clickable {
                    if (seconds > 0) onPrayed()
                    onClose()
                },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp)) }

            Text(
                "Take a moment to pray",
                Modifier.padding(top = 10.dp),
                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 26.sp, lineHeight = 33.sp, textAlign = TextAlign.Center,
            )
            Text(
                moment.verse,
                Modifier.padding(top = 14.dp),
                color = Color.White.copy(alpha = .76f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 15.sp, lineHeight = 23.sp, textAlign = TextAlign.Center,
            )
            Text(
                moment.ref,
                Modifier.padding(top = 6.dp),
                color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 12.sp,
            )

            Spacer(Modifier.height(30.dp))
            Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color.White.copy(alpha = .16f), style = Stroke(width = 4.dp.toPx()))
                    drawArc(
                        color = Pk.GoldLight,
                        startAngle = -90f, sweepAngle = 360f * ((seconds % 60) / 60f), useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
                Box(
                    Modifier.size(150.dp).clip(CircleShape).background(Pk.Oxblood)
                        .clickable { praying = !praying },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (praying) "Praying" else "Tap to pray",
                            color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        )
                        Text(
                            "%d:%02d".format(seconds / 60, seconds % 60),
                            Modifier.padding(top = 4.dp),
                            color = Color.White.copy(alpha = .86f), fontFamily = DisplaySerif, fontSize = 22.sp,
                        )
                    }
                }
            }

            Text(
                moment.prayer,
                Modifier.padding(top = 26.dp),
                color = Color.White.copy(alpha = .88f), fontFamily = BookSerif,
                fontSize = 16.sp, lineHeight = 26.sp, textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))
            Box(Modifier.padding(bottom = 26.dp).fillMaxWidth()) {
                PrimaryButton("Done praying") {
                    if (seconds > 0) onPrayed()
                    onClose()
                }
            }
        }
    }
}

// ── 9. writing through it ───────────────────────────────────────────────

@Composable
private fun FastJournal(fast: FastingChallenge, onBack: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val day = fast.dayOf(System.currentTimeMillis() / 1000).coerceAtMost(fast.days)
    var text by remember { mutableStateOf(FastingStore.journal(context, day)) }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Ink, modifier = Modifier.size(20.dp)) }
        }
        Column(Modifier.weight(1f).padding(horizontal = 22.dp)) {
            Text(
                "Journal",
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 28.sp,
            )
            Text(
                if (fast.days > 1) "Day $day of ${fast.days}" else fast.title,
                Modifier.padding(top = 4.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.sp,
            )
            Text(
                "How is your fast going today?",
                Modifier.padding(top = 20.dp, bottom = 10.dp),
                color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            )
            Box(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(22.dp)).background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(22.dp)).padding(18.dp),
            ) {
                if (text.isBlank()) {
                    Text(
                        "Write your thoughts, prayers, or what God is teaching you…",
                        color = Pk.Muted, fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 25.sp,
                    )
                }
                BasicTextField(
                    text, { text = it },
                    textStyle = TextStyle(color = Ink, fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 25.sp),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(14.dp))
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Save entry", enabled = text.isNotBlank()) {
                FastingStore.saveJournal(context, day, text)
                onSaved()
                onBack()
            }
        }
    }
}

// ── 10. how far ─────────────────────────────────────────────────────────

@Composable
private fun FastProgress(fast: FastingChallenge, onBack: () -> Unit) {
    val day = fast.dayOf(System.currentTimeMillis() / 1000).coerceAtMost(fast.days)
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Ink, modifier = Modifier.size(20.dp)) }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        ) {
            Text("My fast", color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text(
                fast.note.ifBlank { fast.purpose },
                Modifier.padding(top = 4.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.5.sp,
            )

            if (fast.days > 1) {
                Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..fast.days).chunked(7).forEach { week ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            week.forEach { n ->
                                val kept = n in fast.kept
                                Box(
                                    Modifier.size(38.dp).clip(CircleShape)
                                        .background(if (kept) Pk.Oxblood else Color.White)
                                        .border(1.dp, if (n == day && !kept) Pk.Oxblood else Line, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (kept) {
                                        Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    } else {
                                        Text(
                                            "$n",
                                            color = if (n == day) Pk.Oxblood else Pk.Muted,
                                            fontFamily = UtilitySans, fontSize = 12.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${fast.kept.size}/${fast.days}", "Days kept", Modifier.weight(1f))
                Stat("${fast.prayerSessions}", "Prayers", Modifier.weight(1f))
            }
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${fast.scriptures}", "Scriptures", Modifier.weight(1f))
                Stat("${fast.entries}", "Journal entries", Modifier.weight(1f))
            }

            Text(
                "Let us not grow weary in doing good.",
                Modifier.fillMaxWidth().padding(top = 24.dp),
                color = Pk.Muted, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 15.sp, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── 11. breaking it ─────────────────────────────────────────────────────

@Composable
private fun BreakingFast(fast: FastingChallenge, onBack: () -> Unit, onBreak: (Int) -> Unit) {
    val context = LocalContext.current
    val day = fast.dayOf(System.currentTimeMillis() / 1000).coerceAtMost(fast.days)
    var text by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Box(Modifier.fillMaxWidth().height(230.dp)) {
            Image(
                painterResource(Res.drawable.rhythm_evening), null,
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
            Box(Modifier.fillMaxSize().background(Pk.DeepDark.copy(alpha = .58f)))
            Box(
                Modifier.size(40.dp).padding(start = 8.dp, top = 6.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp)) }
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Before you break",
                    color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 26.sp,
                )
                Text(
                    "The Lord gives food to the hungry.",
                    Modifier.padding(top = 10.dp, start = 24.dp, end = 24.dp),
                    color = Color.White.copy(alpha = .84f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontSize = 15.sp, textAlign = TextAlign.Center,
                )
                Text(
                    "Psalm 146:7",
                    Modifier.padding(top = 6.dp),
                    color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 12.sp,
                )
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(top = 18.dp).clip(RoundedCornerShape(22.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(18.dp),
            ) {
                Text(
                    "Father, thank You for sustaining me through these hours. Use what I have given up for Your glory, and let what I asked for be answered in Your time. Amen.",
                    color = Ink, fontFamily = BookSerif, fontSize = 16.5.sp, lineHeight = 26.sp,
                )
            }
            Text(
                "How did today go?",
                Modifier.padding(top = 22.dp, bottom = 10.dp),
                color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            )
            Box(
                Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(20.dp)).background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(20.dp)).padding(16.dp),
            ) {
                if (text.isBlank()) {
                    Text("Write a short reflection…", color = Pk.Muted, fontFamily = BookSerif, fontSize = 16.sp)
                }
                BasicTextField(
                    text, { text = it },
                    textStyle = TextStyle(color = Ink, fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 25.sp),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(20.dp))
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton(if (fast.days > 1) "Break today's fast" else "Break the fast") {
                if (text.isNotBlank()) FastingStore.saveJournal(context, day, text)
                onBreak(day)
            }
        }
    }
}

// ── 12. finished ────────────────────────────────────────────────────────

@Composable
private fun FastComplete(
    fast: FastingChallenge,
    onTestimony: (String) -> Unit,
    onAnswered: (Boolean) -> Unit,
    onAgain: () -> Unit,
    onClose: () -> Unit,
) {
    var testimony by remember { mutableStateOf(fast.testimony) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Box(Modifier.fillMaxWidth().height(240.dp)) {
            Image(
                painterResource(Res.drawable.discovery_cover_4), null,
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
            Box(Modifier.fillMaxSize().background(Pk.DeepDark.copy(alpha = .52f)))
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = .22f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(30.dp)) }
                Text(
                    "You did it",
                    Modifier.padding(top = 14.dp),
                    color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 29.sp,
                )
                Text(
                    "You completed your ${fast.title}.",
                    Modifier.padding(top = 6.dp),
                    color = Color.White.copy(alpha = .86f), fontFamily = UtilitySans, fontSize = 14.sp,
                )
            }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${fast.kept.size}", "Days completed", Modifier.weight(1f))
                Stat("${fast.prayerSessions}", "Prayer sessions", Modifier.weight(1f))
            }
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${fast.scriptures}", "Scriptures read", Modifier.weight(1f))
                Stat("${fast.entries}", "Journal entries", Modifier.weight(1f))
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(20.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(20.dp))
                    .padding(horizontal = 17.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Answered prayer", color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        fast.note.ifBlank { fast.purpose },
                        Modifier.padding(top = 2.dp),
                        color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp,
                    )
                }
                Switch(
                    checked = fast.answered,
                    onCheckedChange = onAnswered,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Pk.Oxblood),
                )
            }

            Text(
                "What did God do?",
                Modifier.padding(top = 22.dp, bottom = 10.dp),
                color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            )
            Box(
                Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(20.dp)).background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(20.dp)).padding(16.dp),
            ) {
                if (testimony.isBlank()) {
                    Text(
                        "Write how God moved during your fast…",
                        color = Pk.Muted, fontFamily = BookSerif, fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    testimony, { testimony = it; onTestimony(it) },
                    textStyle = TextStyle(color = Ink, fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 25.sp),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Start another fast", onClick = onAgain)
            Box(
                Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(99.dp))
                    .clickable(onClick = onClose).padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Save and close", color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

/** The card on Home that starts a fast, or returns to the one running. */
@Composable
fun StartFastCard(running: Boolean, title: String, day: Int, of: Int, onOpen: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(168.dp).clip(RoundedCornerShape(28.dp)).clickable(onClick = onOpen),
    ) {
        Image(
            painterResource(Res.drawable.rhythm_morning), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    listOf(Pk.DeepDark.copy(alpha = .90f), Pk.GoldDeep.copy(alpha = .42f)),
                ),
            ),
        )
        Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 24.dp)) {
            Text(
                if (running) (if (of > 1) "Day $day of $of" else title) else "Fasting challenge",
                color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            )
            Text(
                if (running) "Your fast is running" else "Start a fast",
                Modifier.padding(top = 8.dp),
                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 25.sp, lineHeight = 30.sp,
            )
            Text(
                if (running) "See how long is left" else "Fast with purpose. Pray with focus.",
                Modifier.padding(top = 10.dp),
                color = Color.White.copy(alpha = .82f), fontFamily = UtilitySans, fontSize = 13.sp,
            )
        }
    }
}

/**
 * Challenges: the two there are, and nothing else.
 *
 * Tapping Challenges on Home used to open a shelf of named reading plans that
 * had nothing to do with the prayer and fasting challenges people had just
 * been shown. It now asks the one question that matters - pray, or fast - and
 * each card goes straight into its own flow, or back to the day it left off.
 */
@Composable
fun ChallengeChooser(
    prayer: com.prayerkey.manna.data.PrayerChallenge,
    fast: FastingChallenge,
    onPrayer: () -> Unit,
    onFast: () -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    Column(
        Modifier.fillMaxSize().background(Pk.Cream).statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 22.dp),
    ) {
        Box(
            Modifier.padding(top = 6.dp).size(40.dp).clip(CircleShape).clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Close, "Close", tint = Ink, modifier = Modifier.size(20.dp)) }
        Text(
            "Challenges",
            Modifier.padding(top = 10.dp),
            color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 32.sp,
        )
        Text(
            "Choose how you want to seek God",
            Modifier.padding(top = 6.dp, bottom = 22.dp),
            color = Pk.Muted, fontFamily = UtilitySans, fontSize = 15.sp,
        )
        StartChallengeCard(
            started = prayer.started,
            day = prayer.dayFor(),
            of = prayer.days,
            onOpen = onPrayer,
        )
        Spacer(Modifier.height(14.dp))
        StartFastCard(
            running = fast.running,
            title = fast.title,
            day = fast.dayOf(System.currentTimeMillis() / 1000),
            of = fast.days,
            onOpen = onFast,
        )
    }
}
