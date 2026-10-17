package com.prayerkey.manna.ui.challenge

import kotlinx.datetime.toKotlinLocalDate
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.R
import com.prayerkey.manna.data.CHALLENGE_FOCUSES
import com.prayerkey.manna.data.CHALLENGE_LENGTHS
import com.prayerkey.manna.data.ChallengePractice
import com.prayerkey.manna.data.PrayerChallenge
import com.prayerkey.manna.data.PrayerChallengeStore
import com.prayerkey.manna.data.challengeDayPage
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Prayer + Challenge.
 *
 * Five questions, once: how many days, what you are asking God for, how you
 * want to pray, when, and a summary to check. After that the same short page
 * every day - a verse, today's focus, three things to pray, what to read -
 * and a button you hold to begin. Praying is a timer on a dark screen with
 * nothing else on it. Finishing writes down what God put on your heart, and
 * the day turns from an empty circle into a full one.
 */
private enum class Stage { Welcome, Length, Focus, Practice, Time, Summary, Day, Praying, Reflect, Progress }

internal val Ink = Pk.Charcoal
internal val Line = Color(0xFFE6E2EC)

@Composable
fun PrayerChallengeRoute(onClose: () -> Unit) {
    val context = LocalContext.current
    var challenge by remember { mutableStateOf(PrayerChallengeStore.load(context)) }
    var stage by remember {
        mutableStateOf(if (PrayerChallengeStore.load(context).started) Stage.Day else Stage.Welcome)
    }
    var prayedSeconds by remember { mutableIntStateOf(0) }
    val today = remember { LocalDate.now() }
    val day = challenge.dayFor(today.toKotlinLocalDate())

    fun keep(next: PrayerChallenge) {
        challenge = next
        PrayerChallengeStore.save(context, next)
    }

    BackHandler {
        stage = when (stage) {
            Stage.Welcome -> return@BackHandler onClose()
            Stage.Length -> Stage.Welcome
            Stage.Focus -> Stage.Length
            Stage.Practice -> Stage.Focus
            Stage.Time -> Stage.Practice
            Stage.Summary -> Stage.Time
            Stage.Day -> return@BackHandler onClose()
            Stage.Praying -> Stage.Day
            Stage.Reflect -> Stage.Day
            Stage.Progress -> Stage.Day
        }
    }

    Box(Modifier.fillMaxSize().background(if (stage == Stage.Praying) Pk.DeepDark else Pk.Cream)) {
        when (stage) {
            Stage.Welcome -> Welcome(onClose = onClose) { stage = Stage.Length }
            Stage.Length -> LengthStep(challenge, { keep(it) }, { stage = Stage.Welcome }) { stage = Stage.Focus }
            Stage.Focus -> FocusStep(challenge, { keep(it) }, { stage = Stage.Length }) { stage = Stage.Practice }
            Stage.Practice -> PracticeStep(challenge, { keep(it) }, { stage = Stage.Focus }) { stage = Stage.Time }
            Stage.Time -> TimeStep(challenge, { keep(it) }, { stage = Stage.Practice }) { stage = Stage.Summary }
            Stage.Summary -> Summary(
                challenge,
                onEdit = { stage = Stage.Length },
                onBack = { stage = Stage.Time },
                onBegin = {
                    keep(challenge.copy(startedOn = today.toEpochDay()))
                    com.prayerkey.manna.reminder.ReminderReceiver.scheduleFor(
                        context, "prayer_challenge", challenge.hour, challenge.minute, challenge.reminder,
                    )
                    stage = Stage.Day
                },
            )
            Stage.Day -> DayHome(
                challenge = challenge,
                day = day,
                onClose = onClose,
                onProgress = { stage = Stage.Progress },
                onPray = { prayedSeconds = 0; stage = Stage.Praying },
            )
            Stage.Praying -> PrayingSession(
                challenge = challenge,
                day = day,
                onFinish = { seconds -> prayedSeconds = seconds; stage = Stage.Reflect },
                onBack = { stage = Stage.Day },
            )
            Stage.Reflect -> Reflection(
                challenge = challenge,
                day = day,
                seconds = prayedSeconds,
                onBack = { stage = Stage.Day },
                onComplete = { text, feeling ->
                    PrayerChallengeStore.saveReflection(context, day, text, feeling, prayedSeconds)
                    keep(challenge.copy(done = challenge.done + day))
                    stage = Stage.Progress
                },
            )
            Stage.Progress -> Progress(
                challenge = challenge,
                day = day,
                onAnswered = { keep(challenge.copy(answered = it)) },
                onContinue = { stage = Stage.Day },
                onClose = onClose,
            )
        }
    }
}

// ── the frame every setup step shares ───────────────────────────────────

@Composable
internal fun StepFrame(
    step: Int,
    of: Int = 5,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    footer: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Ink, modifier = Modifier.size(20.dp)) }
            Text(
                "Step $step of $of",
                Modifier.weight(1f),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.width(40.dp))
        }
        // how far in, as a line rather than a count
        Box(
            Modifier.padding(horizontal = 22.dp).fillMaxWidth().height(4.dp)
                .clip(RoundedCornerShape(99.dp)).background(Line),
        ) {
            val fraction by animateFloatAsState(step / 5f, tween(420), label = "steps")
            Box(
                Modifier.fillMaxWidth(fraction).height(4.dp)
                    .clip(RoundedCornerShape(99.dp)).background(Pk.Oxblood),
            )
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        ) {
            Text(
                title,
                Modifier.fillMaxWidth().padding(top = 26.dp),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 27.sp, lineHeight = 33.sp, textAlign = TextAlign.Center,
            )
            Text(
                subtitle,
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 22.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            content()
            Spacer(Modifier.height(24.dp))
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) { footer() }
    }
}

@Composable
internal fun PrimaryButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp))
            .background(if (enabled) Pk.Oxblood else Pk.Oxblood.copy(alpha = .35f))
            .clickable(enabled = enabled) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Icon(Icons.Outlined.ArrowForward, null, Modifier.padding(start = 9.dp).size(17.dp), tint = Color.White)
        }
    }
}

// ── 1. welcome ──────────────────────────────────────────────────────────

@Composable
private fun Welcome(onClose: () -> Unit, onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Box(
            Modifier.size(40.dp).padding(start = 8.dp).clip(CircleShape).clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Close, "Close", tint = Ink, modifier = Modifier.size(20.dp)) }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Prayer + Challenge",
                Modifier.padding(top = 10.dp),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 31.sp,
            )
            Text(
                "Pray with purpose. Stay with it.",
                Modifier.padding(top = 8.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 15.sp,
            )
            Box(
                Modifier.padding(top = 22.dp).fillMaxWidth().aspectRatio(.82f)
                    .clip(RoundedCornerShape(28.dp)),
            ) {
                Image(
                    painterResource(R.drawable.welcome_prayer_dawn), null,
                    Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Pk.DeepDark.copy(alpha = .10f), Pk.DeepDark.copy(alpha = .78f)),
                        ),
                    ),
                )
                Column(Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    listOf("Small steps", "Big prayers", "Real change").forEach {
                        Text(
                            it,
                            color = Color.White, fontFamily = DisplaySerif,
                            fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 31.sp,
                        )
                    }
                }
            }
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Continue", onClick = onContinue)
        }
    }
}

// ── 2. how many days ────────────────────────────────────────────────────

@Composable
private fun LengthStep(
    challenge: PrayerChallenge,
    keep: (PrayerChallenge) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepFrame(
        step = 1,
        title = "Choose your challenge",
        subtitle = "How many days would you like to commit to?",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CHALLENGE_LENGTHS.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { days ->
                        val on = challenge.days == days
                        Box(
                            Modifier.weight(1f).aspectRatio(1.05f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (on) Pk.Oxblood.copy(alpha = .08f) else Color.White)
                                .border(
                                    if (on) 1.6.dp else 1.dp,
                                    if (on) Pk.Oxblood else Line,
                                    RoundedCornerShape(20.dp),
                                )
                                .clickable { keep(challenge.copy(days = days)) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$days",
                                    color = if (on) Pk.Oxblood else Ink,
                                    fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 30.sp,
                                )
                                Text(
                                    "days",
                                    color = if (on) Pk.Oxblood else Pk.Muted,
                                    fontFamily = UtilitySans, fontSize = 13.sp,
                                )
                            }
                            if (on) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).padding(9.dp).size(20.dp)
                                        .clip(CircleShape).background(Pk.Oxblood),
                                    contentAlignment = Alignment.Center,
                                ) { Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(13.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── 3. what for ─────────────────────────────────────────────────────────

@Composable
private fun FocusStep(
    challenge: PrayerChallenge,
    keep: (PrayerChallenge) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    StepFrame(
        step = 2,
        title = "What are you trusting God for?",
        subtitle = "In your own words, or choose one",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 15.dp),
        ) {
            if (challenge.focus.isBlank()) {
                Text(
                    "Peace, direction, healing, provision…",
                    color = Pk.Muted, fontFamily = BookSerif, fontSize = 16.sp,
                )
            }
            BasicTextField(
                challenge.focus,
                { keep(challenge.copy(focus = it)) },
                textStyle = TextStyle(color = Ink, fontFamily = BookSerif, fontSize = 16.sp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                "Or choose a heading",
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.sp,
            )
        }
        Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            CHALLENGE_FOCUSES.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    row.forEach { name ->
                        val on = challenge.category == name
                        Box(
                            Modifier.clip(RoundedCornerShape(99.dp))
                                .background(if (on) Pk.Oxblood else Color.White)
                                .border(1.dp, if (on) Pk.Oxblood else Line, RoundedCornerShape(99.dp))
                                .clickable { keep(challenge.copy(category = if (on) "" else name)) }
                                .padding(horizontal = 15.dp, vertical = 10.dp),
                        ) {
                            Text(
                                name,
                                color = if (on) Color.White else Ink,
                                fontFamily = UtilitySans, fontSize = 13.5.sp,
                                fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 4. how you want to pray ─────────────────────────────────────────────

@Composable
private fun PracticeStep(
    challenge: PrayerChallenge,
    keep: (PrayerChallenge) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val icons = mapOf(
        ChallengePractice.PRAYER to Icons.Outlined.VolunteerActivism,
        ChallengePractice.FASTING to Icons.Outlined.Spa,
        ChallengePractice.BIBLE to Icons.Outlined.MenuBook,
        ChallengePractice.ALL to Icons.Outlined.AutoStories,
    )
    StepFrame(
        step = 3,
        title = "How would you like to do this?",
        subtitle = "Choose your practice",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            ChallengePractice.entries.forEach { practice ->
                val on = challenge.practice == practice
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(if (on) Pk.Oxblood.copy(alpha = .07f) else Color.White)
                        .border(if (on) 1.6.dp else 1.dp, if (on) Pk.Oxblood else Line, RoundedCornerShape(18.dp))
                        .clickable { keep(challenge.copy(practice = practice)) }
                        .padding(horizontal = 17.dp, vertical = 17.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icons[practice]!!, null, tint = if (on) Pk.Oxblood else Ink, modifier = Modifier.size(22.dp))
                    Text(
                        practice.label,
                        Modifier.weight(1f).padding(start = 15.dp),
                        color = Ink, fontFamily = UtilitySans, fontSize = 15.5.sp,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    if (on) Icon(Icons.Outlined.CheckCircle, null, tint = Pk.Oxblood, modifier = Modifier.size(21.dp))
                }
            }
        }
    }
}

// ── 5. when ─────────────────────────────────────────────────────────────

@Composable
private fun TimeStep(
    challenge: PrayerChallenge,
    keep: (PrayerChallenge) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val slots = listOf("Morning" to 6, "Afternoon" to 13, "Evening" to 19, "Night" to 21)
    StepFrame(
        step = 4,
        title = "When will you show up?",
        subtitle = "Set a time for your daily prayer",
        onBack = onBack,
        footer = { PrimaryButton("Next", onClick = onNext) },
    ) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)).background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(99.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            slots.forEach { (label, hour) ->
                val on = challenge.hour == hour
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(99.dp))
                        .background(if (on) Pk.Oxblood else Color.Transparent)
                        .clickable { keep(challenge.copy(hour = hour)) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = if (on) Color.White else Ink,
                        fontFamily = UtilitySans, fontSize = 13.sp,
                        fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }

        // the hour itself, set by hand
        Row(
            Modifier.fillMaxWidth().padding(top = 18.dp).clip(RoundedCornerShape(22.dp))
                .background(Color.White).border(1.dp, Line, RoundedCornerShape(22.dp))
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Stepper(challenge.timeLabel().substringBefore(":")) {
                keep(challenge.copy(hour = (challenge.hour + it + 24) % 24))
            }
            Text(":", Modifier.padding(horizontal = 6.dp), color = Ink, fontFamily = DisplaySerif, fontSize = 30.sp)
            Stepper("%02d".format(challenge.minute)) {
                keep(challenge.copy(minute = (challenge.minute + it * 5 + 60) % 60))
            }
            Text(
                if (challenge.hour < 12) "AM" else "PM",
                Modifier.padding(start = 12.dp),
                color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 15.sp,
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(18.dp))
                .background(Color.White).border(1.dp, Line, RoundedCornerShape(18.dp))
                .padding(horizontal = 17.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.NotificationsNone, null, tint = Pk.Oxblood, modifier = Modifier.size(20.dp))
            Text(
                "Daily reminder",
                Modifier.weight(1f).padding(start = 13.dp),
                color = Ink, fontFamily = UtilitySans, fontSize = 15.sp,
            )
            Switch(
                checked = challenge.reminder,
                onCheckedChange = { keep(challenge.copy(reminder = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Pk.Oxblood,
                ),
            )
        }
    }
}

@Composable
private fun Stepper(value: String, onStep: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "+",
            Modifier.clip(CircleShape).clickable { onStep(1) }.padding(horizontal = 12.dp, vertical = 2.dp),
            color = Pk.Muted, fontFamily = UtilitySans, fontSize = 18.sp,
        )
        Text(value, color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 34.sp)
        Text(
            "−",
            Modifier.clip(CircleShape).clickable { onStep(-1) }.padding(horizontal = 12.dp, vertical = 2.dp),
            color = Pk.Muted, fontFamily = UtilitySans, fontSize = 18.sp,
        )
    }
}

// ── 6. the summary ──────────────────────────────────────────────────────

@Composable
private fun Summary(
    challenge: PrayerChallenge,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    onBegin: () -> Unit,
) {
    StepFrame(
        step = 5,
        title = "Your challenge is ready",
        subtitle = "Here is what you have chosen",
        onBack = onBack,
        footer = {
            Column {
                PrimaryButton("Begin challenge", onClick = onBegin)
                Box(
                    Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(99.dp))
                        .clickable(onClick = onEdit).padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Edit", color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        },
    ) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(24.dp)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SummaryLine(Icons.Outlined.LocalFireDepartment, "${challenge.days} day challenge", "How long")
            SummaryLine(Icons.Outlined.VolunteerActivism, challenge.heading, "What you are trusting God for")
            SummaryLine(Icons.Outlined.MenuBook, challenge.practice.label, "Practice")
            SummaryLine(Icons.Outlined.Schedule, challenge.timeLabel(), "Time")
            SummaryLine(
                Icons.Outlined.NotificationsNone,
                if (challenge.reminder) "On" else "Off",
                "Daily reminder",
            )
        }
        Text(
            "Same God. A new you.",
            Modifier.fillMaxWidth().padding(top = 18.dp),
            color = Pk.Muted, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
            fontSize = 15.sp, textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun SummaryLine(icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Pk.Oxblood.copy(alpha = .08f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(18.dp)) }
        Column(Modifier.padding(start = 14.dp)) {
            Text(label, color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.sp)
            Text(
                value,
                Modifier.padding(top = 1.dp),
                color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp,
            )
        }
    }
}

// ── 7. the day ──────────────────────────────────────────────────────────

@Composable
private fun DayHome(
    challenge: PrayerChallenge,
    day: Int,
    onClose: () -> Unit,
    onProgress: () -> Unit,
    onPray: () -> Unit,
) {
    val content = remember(challenge, day) { challengeDayPage(challenge, day) }
    val doneToday = day in challenge.done
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 14.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Ink, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.clip(RoundedCornerShape(99.dp)).clickable(onClick = onProgress)
                    .background(Pk.Oxblood.copy(alpha = .08f)).padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.LocalFireDepartment, null, tint = Pk.Oxblood, modifier = Modifier.size(15.dp))
                Text(
                    "${challenge.streak()}",
                    Modifier.padding(start = 5.dp),
                    color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                )
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        ) {
            Text(
                "Day $day of ${challenge.days}",
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 30.sp,
            )
            Text(
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                Modifier.padding(top = 4.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.sp,
            )
            Box(
                Modifier.padding(top = 14.dp).fillMaxWidth().height(6.dp)
                    .clip(RoundedCornerShape(99.dp)).background(Line),
            ) {
                val fraction by animateFloatAsState(
                    challenge.done.size / challenge.days.toFloat(), tween(500), label = "days",
                )
                Box(
                    Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(99.dp))
                        .background(Brush.horizontalGradient(listOf(Pk.Oxblood, Pk.GoldDeep))),
                )
            }

            DayCard("Verse of the day") {
                Text(
                    content.verse,
                    color = Ink, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontSize = 17.sp, lineHeight = 26.sp,
                )
                Text(
                    content.verseRef,
                    Modifier.padding(top = 8.dp),
                    color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                )
            }

            DayCard("Today's focus") {
                Text(
                    content.focus,
                    color = Ink, fontFamily = UtilitySans, fontSize = 16.sp, lineHeight = 23.sp,
                )
            }

            DayCard("Prayer points") {
                content.points.forEachIndexed { i, point ->
                    Row(Modifier.padding(top = if (i == 0) 0.dp else 12.dp)) {
                        Text(
                            "${i + 1}",
                            Modifier.size(22.dp).clip(CircleShape).background(Pk.Oxblood.copy(alpha = .10f))
                                .padding(top = 2.dp),
                            color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.Bold,
                            fontSize = 12.sp, textAlign = TextAlign.Center,
                        )
                        Text(
                            point,
                            Modifier.padding(start = 12.dp),
                            color = Ink, fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 22.sp,
                        )
                    }
                }
            }

            if (challenge.practice.readsScripture) {
                DayCard("Bible reading") {
                    Text(
                        content.reading,
                        color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                    )
                }
            }
            if (challenge.practice.fasts) {
                DayCard("Fasting") {
                    Text(
                        "Until ${challenge.timeLabel()}, give the hunger to God each time you feel it.",
                        color = Ink, fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 22.sp,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        Column(Modifier.padding(horizontal = 22.dp, vertical = 12.dp)) {
            HoldToPray(done = doneToday, onPray = onPray)
            Box(
                Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(99.dp))
                    .clickable(onClick = onProgress).padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "See my progress",
                    color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
internal fun DayCard(label: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(22.dp))
            .background(Color.White).border(1.dp, Line, RoundedCornerShape(22.dp)).padding(18.dp),
    ) {
        Text(
            label,
            Modifier.padding(bottom = 10.dp),
            color = Pk.GoldDeep, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
        )
        content()
    }
}

/**
 * Hold to pray.
 *
 * A tap is how you dismiss things; holding is how you mean them. The ring
 * fills while the thumb is down and the session opens when it closes, so
 * beginning to pray takes a second of intent rather than a stray touch.
 */
@Composable
internal fun HoldToPray(done: Boolean, onPray: () -> Unit) {
    var holding by remember { mutableStateOf(false) }
    val fill by animateFloatAsState(if (holding) 1f else 0f, tween(if (holding) 900 else 220), label = "hold")
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(fill, holding) {
        if (holding && fill >= 1f) {
            holding = false
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onPray()
        }
    }

    Box(
        Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(99.dp))
            .background(Pk.Oxblood)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        holding = true
                        tryAwaitRelease()
                        holding = false
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.fillMaxWidth(fill).height(58.dp)
                .background(Brush.horizontalGradient(listOf(Pk.GoldDeep, Pk.Gold))),
        )
        Text(
            if (done) "Pray again" else "Hold to pray",
            color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.Bold,
            fontSize = 16.sp, letterSpacing = .4.sp,
        )
    }
}

// ── 8. the session ──────────────────────────────────────────────────────

@Composable
private fun PrayingSession(
    challenge: PrayerChallenge,
    day: Int,
    onFinish: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val content = remember(challenge, day) { challengeDayPage(challenge, day) }
    var seconds by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(true) }

    LaunchedEffect(running) {
        while (running) {
            delay(1000)
            seconds++
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.rhythm_reflection), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Pk.DeepDark.copy(alpha = .86f), Pk.DeepDark.copy(alpha = .94f)),
                ),
            ),
        )
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.align(Alignment.Start).size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp)) }

            Text(
                if (running) "Praying" else "Paused",
                Modifier.padding(top = 6.dp),
                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 27.sp,
            )
            Text(
                content.verse,
                Modifier.padding(top = 14.dp),
                color = Color.White.copy(alpha = .72f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 15.sp, lineHeight = 23.sp, textAlign = TextAlign.Center,
            )
            Text(
                content.verseRef,
                Modifier.padding(top = 6.dp),
                color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 12.sp,
            )

            Box(Modifier.padding(top = 26.dp).size(220.dp), contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color.White.copy(alpha = .14f), style = Stroke(width = 3.dp.toPx()))
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Pk.GoldDeep, Pk.GoldLight, Pk.GoldDeep)),
                        startAngle = -90f,
                        sweepAngle = 360f * ((seconds % 60) / 60f),
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset.Zero,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.VolunteerActivism, null, tint = Pk.GoldLight, modifier = Modifier.size(28.dp))
                    Text(
                        "%d:%02d".format(seconds / 60, seconds % 60),
                        Modifier.padding(top = 8.dp),
                        color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 44.sp,
                    )
                }
            }

            Text(
                content.prayer,
                Modifier.padding(top = 24.dp),
                color = Color.White.copy(alpha = .88f), fontFamily = BookSerif,
                fontSize = 16.sp, lineHeight = 26.sp, textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))
            Row(
                Modifier.padding(bottom = 26.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SessionAction(
                    if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    if (running) "Pause" else "Resume",
                    Color.White.copy(alpha = .16f),
                ) { running = !running }
                SessionAction(Icons.Outlined.Stop, "Finish", Pk.Oxblood) {
                    running = false
                    onFinish(seconds)
                }
            }
        }
    }
}

@Composable
internal fun SessionAction(icon: ImageVector, label: String, colour: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(colour).clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
        Text(
            label,
            Modifier.padding(top = 8.dp),
            color = Color.White.copy(alpha = .8f), fontFamily = UtilitySans, fontSize = 13.sp,
        )
    }
}

// ── 9. what God put on your heart ───────────────────────────────────────

private val FEELINGS = listOf("Peace", "Hope", "Strength", "Gratitude")

@Composable
private fun Reflection(
    challenge: PrayerChallenge,
    day: Int,
    seconds: Int,
    onBack: () -> Unit,
    onComplete: (String, String) -> Unit,
) {
    val content = remember(challenge, day) { challengeDayPage(challenge, day) }
    var text by remember { mutableStateOf("") }
    var feeling by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Box(
            Modifier.size(40.dp).padding(start = 8.dp).clip(CircleShape).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Ink, modifier = Modifier.size(20.dp)) }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.padding(top = 8.dp).size(76.dp).clip(CircleShape)
                    .background(Pk.Oxblood.copy(alpha = .10f)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Check, null, tint = Pk.Oxblood, modifier = Modifier.size(34.dp)) }

            Text(
                "You prayed on day $day",
                Modifier.padding(top = 16.dp),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 25.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                "You showed up today.",
                Modifier.padding(top = 6.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp,
            )

            Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Fact(Icons.Outlined.Schedule, "%d:%02d".format(seconds / 60, seconds % 60), "Prayer time")
                if (challenge.practice.readsScripture) {
                    Fact(Icons.Outlined.MenuBook, content.reading, "Scripture")
                }
            }

            Text(
                "What did God put on your heart today?",
                Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 10.dp),
                color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            )
            Box(
                Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(20.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(20.dp))
                    .padding(16.dp),
            ) {
                if (text.isBlank()) {
                    Text(
                        "Write it down before it goes…",
                        color = Pk.Muted, fontFamily = BookSerif, fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    text, { text = it },
                    textStyle = TextStyle(color = Ink, fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 25.sp),
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Text(
                "How are you feeling?",
                Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 10.dp),
                color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                FEELINGS.forEach { name ->
                    val on = feeling == name
                    Box(
                        Modifier.clip(RoundedCornerShape(99.dp))
                            .background(if (on) Pk.Oxblood else Color.White)
                            .border(1.dp, if (on) Pk.Oxblood else Line, RoundedCornerShape(99.dp))
                            .clickable { feeling = if (on) "" else name }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        Text(
                            name,
                            color = if (on) Color.White else Ink,
                            fontFamily = UtilitySans, fontSize = 13.sp,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Mark day complete") { onComplete(text, feeling) }
        }
    }
}

@Composable
internal fun Fact(icon: ImageVector, value: String, label: String) {
    Row(
        Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(17.dp))
        Column(Modifier.padding(start = 10.dp)) {
            Text(value, color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(label, color = Pk.Muted, fontFamily = UtilitySans, fontSize = 11.5.sp)
        }
    }
}

// ── 10. the journey so far ──────────────────────────────────────────────

@Composable
private fun Progress(
    challenge: PrayerChallenge,
    day: Int,
    onAnswered: (Boolean) -> Unit,
    onContinue: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val minutes = remember(challenge.done) { PrayerChallengeStore.minutesPrayed(context, challenge.days) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onContinue),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Ink, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Ink, modifier = Modifier.size(20.dp)) }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        ) {
            Text(
                "${challenge.days} day challenge",
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 28.sp,
            )
            Text(
                if (challenge.complete) "You finished. God was working." else "Keep going. God is working.",
                Modifier.padding(top = 5.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp,
            )

            // every day as a circle: full, today, or still to come
            Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..challenge.days).chunked(7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        week.forEach { n ->
                            val done = n in challenge.done
                            Box(
                                Modifier.size(36.dp).clip(CircleShape)
                                    .background(if (done) Pk.Oxblood else Color.White)
                                    .border(
                                        1.dp,
                                        if (n == day && !done) Pk.Oxblood else Line,
                                        CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (done) {
                                    Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                } else {
                                    Text(
                                        "$n",
                                        color = if (n == day) Pk.Oxblood else Pk.Muted,
                                        fontFamily = UtilitySans, fontSize = 12.sp,
                                        fontWeight = if (n == day) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${challenge.done.size}", "Days prayed", Modifier.weight(1f))
                Stat("${(challenge.days - challenge.done.size).coerceAtLeast(0)}", "To go", Modifier.weight(1f))
                Stat("$minutes", "Minutes", Modifier.weight(1f))
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 18.dp).clip(RoundedCornerShape(20.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(20.dp))
                    .padding(horizontal = 17.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Answered prayer",
                        color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    )
                    Text(
                        challenge.heading,
                        Modifier.padding(top = 2.dp),
                        color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp,
                    )
                }
                Switch(
                    checked = challenge.answered,
                    onCheckedChange = onAnswered,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Pk.Oxblood,
                    ),
                )
            }

            if (challenge.complete) {
                Column(
                    Modifier.fillMaxWidth().padding(top = 18.dp).clip(RoundedCornerShape(24.dp))
                        .background(Brush.verticalGradient(listOf(Pk.Oxblood, Pk.DeepDark)))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Pk.GoldLight, modifier = Modifier.size(34.dp))
                    Text(
                        "Challenge complete",
                        Modifier.padding(top = 12.dp),
                        color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 23.sp,
                    )
                    Text(
                        "${challenge.days} days with God",
                        Modifier.padding(top = 5.dp),
                        color = Color.White.copy(alpha = .78f), fontFamily = UtilitySans, fontSize = 14.sp,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton(if (challenge.complete) "Start another challenge" else "Continue tomorrow") {
                if (challenge.complete) {
                    com.prayerkey.manna.reminder.ReminderReceiver.scheduleFor(context, "prayer_challenge", 6, 30, false)
                    PrayerChallengeStore.clear(context)
                    onClose()
                } else {
                    onContinue()
                }
            }
        }
    }
}

@Composable
internal fun Stat(value: String, label: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(18.dp)).padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = Pk.Oxblood, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Text(label, Modifier.padding(top = 3.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.sp)
    }
}

/** The card on Home that starts the whole thing. */
@Composable
fun StartChallengeCard(started: Boolean, day: Int, of: Int, onOpen: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(168.dp).clip(RoundedCornerShape(28.dp)).clickable(onClick = onOpen),
    ) {
        Image(
            painterResource(R.drawable.discovery_cover_5), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    listOf(Pk.DeepDark.copy(alpha = .90f), Pk.Oxblood.copy(alpha = .58f)),
                ),
            ),
        )
        Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 24.dp)) {
            Text(
                if (started) "Day $day of $of" else "Prayer + Challenge",
                color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            )
            Text(
                if (started) "Continue your challenge" else "Start a prayer challenge",
                Modifier.padding(top = 8.dp),
                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 25.sp, lineHeight = 30.sp,
            )
            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                // no arrow: the fast, meditation and Bible plan cards beside it carry none
                Text(
                    if (started) "Pray today" else "Pray with purpose. Stay with it.",
                    color = Color.White.copy(alpha = .82f), fontFamily = UtilitySans, fontSize = 13.sp,
                )
            }
        }
    }
}
