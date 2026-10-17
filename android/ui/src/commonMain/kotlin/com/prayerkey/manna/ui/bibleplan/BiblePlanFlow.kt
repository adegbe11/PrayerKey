package com.prayerkey.manna.ui.bibleplan

import com.prayerkey.manna.ui.platform.pickDate
import com.prayerkey.manna.platform.plusDays
import com.prayerkey.manna.platform.format
import com.prayerkey.manna.ui.platform.Reminders
import com.prayerkey.manna.ui.platform.shareText
import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.platform.now
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.resources.*
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WbSunny
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.data.PlanDay
import com.prayerkey.manna.data.PlanProgress
import com.prayerkey.manna.data.READING_PLANS
import com.prayerkey.manna.data.ReadingPlanDef
import com.prayerkey.manna.data.ReadingPlanStore
import com.prayerkey.manna.ui.challenge.Ink
import com.prayerkey.manna.ui.challenge.Line
import com.prayerkey.manna.ui.challenge.PrimaryButton
import com.prayerkey.manna.ui.challenge.Stat
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.datetime.LocalDate

/**
 * Bible plans.
 *
 * A library of short plans on one theme each, a page about the plan, a day to
 * begin, and then the same page every day: the passage, a short devotion on
 * it, a prayer point, a question. Reading the passage opens it whole, in the
 * KJV, with a voice that can read it aloud. Marking the day complete is a
 * small moment of its own, and the plan remembers where you stopped.
 */
private enum class PlanStage { Welcome, Library, Details, Start, Day, Reader, Reflect, DayDone, Progress, Finished }

@Composable
fun BiblePlanRoute(
    onClose: () -> Unit,
    /** Keeps a reflection in the journal: a heading, then what was written. */
    onKeep: (String, String) -> Unit,
) {
    val context = platformContext()
    var progress by remember { mutableStateOf(ReadingPlanStore.load(context)) }
    val active = remember(progress.planId) { ReadingPlanStore.plan(progress.planId) }
    var chosen by remember { mutableStateOf<ReadingPlanDef?>(active) }
    var stage by remember {
        mutableStateOf(
            when {
                active == null -> PlanStage.Welcome
                progress.complete(active.length) -> PlanStage.Finished
                else -> PlanStage.Day
            },
        )
    }
    var day by remember { mutableIntStateOf(active?.let { progress.current(it.length) } ?: 1) }

    fun keep(next: PlanProgress) {
        progress = next
        ReadingPlanStore.save(context, next)
    }

    BackHandler {
        stage = when (stage) {
            PlanStage.Welcome -> return@BackHandler onClose()
            PlanStage.Library -> if (active != null) PlanStage.Day else PlanStage.Welcome
            PlanStage.Details -> PlanStage.Library
            PlanStage.Start -> PlanStage.Details
            PlanStage.Day -> return@BackHandler onClose()
            PlanStage.Reader, PlanStage.Reflect, PlanStage.Progress -> PlanStage.Day
            PlanStage.DayDone -> PlanStage.Day
            PlanStage.Finished -> return@BackHandler onClose()
        }
    }

    val plan = chosen
    Box(Modifier.fillMaxSize().background(Pk.Cream)) {
        when (stage) {
            PlanStage.Welcome -> Welcome(onClose) { stage = PlanStage.Library }
            PlanStage.Library -> Library(
                current = active,
                onBack = { stage = if (active != null) PlanStage.Day else PlanStage.Welcome },
            ) { chosen = it; stage = PlanStage.Details }
            PlanStage.Details -> if (plan != null) Details(plan, { stage = PlanStage.Library }) {
                stage = if (plan.id == active?.id) PlanStage.Day else PlanStage.Start
            }
            PlanStage.Start -> if (plan != null) StartStep(
                onBack = { stage = PlanStage.Details },
            ) { date, reminder ->
                ReadingPlanStore.start(context, plan.id, date, reminder)
                Reminders.scheduleBibleReading(context, reminder)
                progress = ReadingPlanStore.load(context)
                day = 1
                stage = PlanStage.Day
            }
            PlanStage.Day -> if (plan != null) DayPage(
                plan = plan,
                day = day,
                done = day in progress.done,
                onClose = onClose,
                onRead = { stage = PlanStage.Reader },
                onReflect = { stage = PlanStage.Reflect },
                onProgress = { stage = PlanStage.Progress },
                onComplete = {
                    keep(progress.copy(done = progress.done + day))
                    stage = if (progress.complete(plan.length)) {
                        ReadingPlanStore.markFinished(context, plan.id)
                        PlanStage.Finished
                    } else {
                        PlanStage.DayDone
                    }
                },
            )
            PlanStage.Reader -> if (plan != null) Reader(plan.days[day - 1]) { stage = PlanStage.Day }
            PlanStage.Reflect -> if (plan != null) Reflect(
                day = day,
                question = plan.days[day - 1].question,
                onBack = { stage = PlanStage.Day },
            ) { text, prayer, toJournal ->
                if (toJournal && (text.isNotBlank() || prayer.isNotBlank())) {
                    val body = listOf(text, prayer.takeIf { it.isNotBlank() }?.let { "Prayer: $it" })
                        .filterNotNull().filter { it.isNotBlank() }.joinToString("\n\n")
                    onKeep("${plan.title} · day $day", body)
                }
                keep(
                    progress.copy(
                        reflections = progress.reflections + if (text.isNotBlank()) 1 else 0,
                        prayers = progress.prayers + if (prayer.isNotBlank()) 1 else 0,
                    ),
                )
                stage = PlanStage.Day
            }
            PlanStage.DayDone -> if (plan != null) DayDone(
                day = day,
                next = progress.current(plan.length),
                onNext = { day = progress.current(plan.length); stage = PlanStage.Day },
                onProgress = { stage = PlanStage.Progress },
            )
            PlanStage.Progress -> if (plan != null) Progress(
                plan = plan,
                progress = progress,
                onBack = { stage = PlanStage.Day },
                onOpen = { day = it; stage = PlanStage.Day },
                onBrowse = { stage = PlanStage.Library },
            )
            PlanStage.Finished -> if (plan != null) Finished(
                plan = plan,
                progress = progress,
                onAnother = {
                    ReadingPlanStore.clear(context)
                    progress = ReadingPlanStore.load(context)
                    chosen = null
                    stage = PlanStage.Library
                },
                onClose = onClose,
            )
        }
    }
}

// ── small parts ──────────────────────────────────────────────────────────

@Composable
private fun TopBar(onBack: () -> Unit, icon: ImageVector = Icons.Outlined.ArrowBack, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 8.dp, end = 14.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, "Back", tint = Ink, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun Feature(icon: ImageVector, title: String, blurb: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(18.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Pk.Oxblood.copy(alpha = .08f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp)) }
        Column(Modifier.padding(start = 14.dp)) {
            Text(title, color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(blurb, Modifier.padding(top = 2.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp)
        }
    }
}

@Composable
private fun Section(label: String, icon: ImageVector? = null, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 14.dp).clip(RoundedCornerShape(20.dp))
            .background(Color.White).border(1.dp, Line, RoundedCornerShape(20.dp)).padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.padding(end = 8.dp).size(17.dp))
            Text(label, color = Pk.GoldDeep, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

// ── 1. welcome ──────────────────────────────────────────────────────────

@Composable
private fun Welcome(onClose: () -> Unit, onBrowse: () -> Unit) {
    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.05f)) {
            Image(
                painterResource(Res.drawable.onboarding_read_v3), null,
                Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Transparent, .72f to Color.Transparent, 1f to Pk.Cream),
                ),
            )
            Box(
                Modifier.statusBarsPadding().padding(8.dp).size(40.dp).clip(CircleShape)
                    .background(Color.Black.copy(alpha = .22f)).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Bible plan", color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 31.sp)
            Text(
                "A journey through God's Word for a stronger you.",
                Modifier.padding(top = 7.dp, bottom = 18.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 15.sp, textAlign = TextAlign.Center,
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Feature(Icons.Outlined.AutoStories, "Grow closer to God", "Build a daily habit")
                Feature(Icons.Outlined.WbSunny, "Plans for every season", "Faith, healing, peace, prayer and more")
                Feature(Icons.Outlined.InsertChart, "Stay on track", "See your progress")
            }
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Browse plans", onClick = onBrowse)
        }
    }
}

// ── 2. the library ──────────────────────────────────────────────────────

@Composable
private fun Library(current: ReadingPlanDef?, onBack: () -> Unit, onPick: (ReadingPlanDef) -> Unit) {
    val context = platformContext()
    val finished = remember { ReadingPlanStore.finished(context) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Faith", "Peace", "Healing", "Prayer", "Life")
    val plans = READING_PLANS.filter { p ->
        (filter == "All" || p.topic == filter) &&
            (query.isBlank() || p.title.contains(query.trim(), true) || p.blurb.contains(query.trim(), true))
    }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Text("Choose a Bible plan", color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text(
                "Find a plan for your season",
                Modifier.padding(top = 5.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp,
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(99.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(99.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Search, null, tint = Pk.Muted, modifier = Modifier.size(19.dp))
                Box(Modifier.weight(1f).padding(start = 10.dp)) {
                    if (query.isEmpty()) Text("Search plans", color = Pk.Muted, fontFamily = UtilitySans, fontSize = 15.sp)
                    BasicTextField(
                        query, { query = it },
                        textStyle = TextStyle(color = Ink, fontFamily = UtilitySans, fontSize = 15.sp),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Row(
                Modifier.padding(top = 14.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                filters.forEach { f ->
                    val on = filter == f
                    Box(
                        Modifier.clip(RoundedCornerShape(99.dp))
                            .background(if (on) Pk.Oxblood else Color.White)
                            .border(1.dp, if (on) Pk.Oxblood else Line, RoundedCornerShape(99.dp))
                            .clickable { filter = f }
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                    ) {
                        Text(
                            f, color = if (on) Color.White else Ink, fontFamily = UtilitySans, fontSize = 13.5.sp,
                            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
            Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plans.forEach { p ->
                    val isCurrent = p.id == current?.id
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                            .background(if (isCurrent) Pk.Oxblood.copy(alpha = .06f) else Color.White)
                            .border(if (isCurrent) 1.6.dp else 1.dp, if (isCurrent) Pk.Oxblood else Line, RoundedCornerShape(18.dp))
                            .clickable { onPick(p) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painterResource(p.art), null,
                            Modifier.size(58.dp).clip(RoundedCornerShape(13.dp)), contentScale = ContentScale.Crop,
                        )
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(p.title, color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp)
                            Text(
                                when {
                                    isCurrent -> "Reading now · ${p.length} days"
                                    p.id in finished -> "Finished · ${p.length} days"
                                    else -> "${p.length} days"
                                },
                                Modifier.padding(top = 3.dp),
                                color = if (isCurrent) Pk.Oxblood else Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp,
                            )
                        }
                        Icon(
                            if (p.id in finished) Icons.Outlined.CheckCircle else Icons.Outlined.ChevronRight, null,
                            tint = if (p.id in finished) Pk.Oxblood else Pk.Muted, modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (plans.isEmpty()) {
                    Text(
                        "No plans match",
                        Modifier.fillMaxWidth().padding(top = 30.dp),
                        color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp, textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── 3. about the plan ───────────────────────────────────────────────────

@Composable
private fun Details(plan: ReadingPlanDef, onBack: () -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.35f)) {
            Image(painterResource(plan.art), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0f to Color.Transparent, .7f to Color.Transparent, 1f to Pk.Cream),
                ),
            )
            Box(
                Modifier.statusBarsPadding().padding(8.dp).size(40.dp).clip(CircleShape)
                    .background(Color.Black.copy(alpha = .22f)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "${plan.length} days",
                color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            )
            Text(
                plan.title,
                Modifier.padding(top = 6.dp),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 29.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                plan.blurb,
                Modifier.padding(top = 8.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.5.sp, lineHeight = 21.sp,
                textAlign = TextAlign.Center,
            )
            Column(
                Modifier.fillMaxWidth().padding(top = 20.dp).clip(RoundedCornerShape(20.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(20.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Included(Icons.Outlined.MenuBook, "Daily readings")
                Included(Icons.Outlined.AutoStories, "Short devotions")
                Included(Icons.Outlined.EditNote, "Reflection questions")
                Included(Icons.Outlined.VolunteerActivism, "Prayer points")
            }
            Column(
                Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(20.dp))
                    .background(Pk.Oxblood.copy(alpha = .05f)).padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    plan.verse, color = Ink, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontSize = 15.5.sp, lineHeight = 23.sp, textAlign = TextAlign.Center,
                )
                Text(plan.verseRef, Modifier.padding(top = 6.dp), color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 12.5.sp)
            }
            Spacer(Modifier.height(20.dp))
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Start plan", onClick = onStart)
        }
    }
}

@Composable
private fun Included(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp))
        Text(label, Modifier.padding(start = 14.dp), color = Ink, fontFamily = UtilitySans, fontSize = 15.sp)
    }
}

// ── 4. when to begin ────────────────────────────────────────────────────

@Composable
private fun StartStep(onBack: () -> Unit, onBegin: (LocalDate, Boolean) -> Unit) {
    val context = platformContext()
    var choice by remember { mutableStateOf("today") }
    var date by remember { mutableStateOf(today()) }
    var reminder by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Text(
                "When do you want to start?",
                Modifier.fillMaxWidth(),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 27.sp,
                lineHeight = 33.sp, textAlign = TextAlign.Center,
            )
            Text(
                "You can always change this later",
                Modifier.fillMaxWidth().padding(top = 7.dp, bottom = 20.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.sp, textAlign = TextAlign.Center,
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StartOption(Icons.Outlined.WbSunny, "Start today", "Begin your plan now", choice == "today") {
                    choice = "today"; date = today()
                }
                StartOption(Icons.Outlined.CalendarMonth, "Tomorrow", "Start fresh in the morning", choice == "tomorrow") {
                    choice = "tomorrow"; date = today().plusDays(1)
                }
                StartOption(
                    Icons.Outlined.CalendarMonth,
                    "Choose a date",
                    if (choice == "date") date.format("EEEE, d MMMM") else "Pick any date",
                    choice == "date",
                ) {
                    pickDate(context, date, earliest = today()) { picked -> date = picked; choice = "date" }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 18.dp).clip(RoundedCornerShape(18.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.NotificationsNone, null, tint = Pk.Oxblood, modifier = Modifier.size(20.dp))
                Text("Daily reminder", Modifier.weight(1f).padding(start = 13.dp), color = Ink, fontFamily = UtilitySans, fontSize = 15.sp)
                Switch(
                    checked = reminder, onCheckedChange = { reminder = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Pk.Oxblood),
                )
            }
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Next") { onBegin(date, reminder) }
        }
    }
}

@Composable
private fun StartOption(icon: ImageVector, title: String, blurb: String, on: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(if (on) Pk.Oxblood.copy(alpha = .07f) else Color.White)
            .border(if (on) 1.6.dp else 1.dp, if (on) Pk.Oxblood else Line, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (on) Pk.Oxblood else Ink, modifier = Modifier.size(21.dp))
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, color = Ink, fontFamily = UtilitySans, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium, fontSize = 15.5.sp)
            Text(blurb, Modifier.padding(top = 2.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.5.sp)
        }
        if (on) Icon(Icons.Outlined.CheckCircle, null, tint = Pk.Oxblood, modifier = Modifier.size(20.dp))
    }
}

// ── 5. the day ──────────────────────────────────────────────────────────

@Composable
private fun DayPage(
    plan: ReadingPlanDef,
    day: Int,
    done: Boolean,
    onClose: () -> Unit,
    onRead: () -> Unit,
    onReflect: () -> Unit,
    onProgress: () -> Unit,
    onComplete: () -> Unit,
) {
    val page = plan.days[day - 1]
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        TopBar(onClose, Icons.Outlined.Close) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onProgress),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.InsertChart, "My plan", tint = Ink, modifier = Modifier.size(20.dp)) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Text(
                "Day $day of ${plan.length}",
                Modifier.fillMaxWidth(),
                color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                page.title,
                Modifier.fillMaxWidth().padding(top = 4.dp),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 27.sp,
                textAlign = TextAlign.Center,
            )
            Image(
                painterResource(plan.art), null,
                Modifier.fillMaxWidth().padding(top = 16.dp).height(150.dp).clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop,
            )

            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp).clip(RoundedCornerShape(18.dp))
                    .background(Pk.Oxblood).clickable(onClick = onRead).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.MenuBook, null, tint = Color.White, modifier = Modifier.size(22.dp))
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("Today's reading", color = Color.White.copy(alpha = .78f), fontFamily = UtilitySans, fontSize = 12.5.sp)
                    Text(page.reference, color = Color.White, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }

            Section("Devotion") {
                Text(page.devotionTitle, color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    page.devotion,
                    Modifier.padding(top = 8.dp),
                    color = Ink.copy(alpha = .86f), fontFamily = BookSerif, fontSize = 16.5.sp, lineHeight = 26.sp,
                )
            }
            Section("Prayer point", Icons.Outlined.VolunteerActivism) {
                Text(page.prayerPoint, color = Ink, fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 22.sp)
            }
            Section("Reflection", Icons.Outlined.EditNote) {
                Text(page.question, color = Ink, fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 22.sp)
                Text(
                    "Write your answer",
                    Modifier.padding(top = 10.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onReflect).padding(vertical = 4.dp),
                    color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                )
            }
            Spacer(Modifier.height(20.dp))
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            if (done) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)).background(Pk.Oxblood.copy(alpha = .08f))
                        .padding(vertical = 17.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp))
                    Text(
                        "Day $day complete",
                        Modifier.padding(start = 8.dp),
                        color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    )
                }
            } else {
                PrimaryButton("Mark as complete", onClick = onComplete)
            }
        }
    }
}

// ── 6. reading the passage ──────────────────────────────────────────────

@Composable
private fun Reader(page: PlanDay, onBack: () -> Unit) {
    val context = platformContext()
    var verses by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    LaunchedEffect(page) {
        verses = OfflineBible(context).chapter(page.book, page.chapter).filter { it.verse in page.from..page.to }
    }
    var speechReady by remember { mutableStateOf(false) }
    var speaking by remember { mutableStateOf(false) }
    val speaker = remember { Speaker(context) { speechReady = it == Speaker.SUCCESS } }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }
    var size by remember { mutableIntStateOf(19) }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack) {
            Text(
                "Aa",
                Modifier.clip(RoundedCornerShape(10.dp)).clickable { size = if (size >= 23) 17 else size + 2 }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
            )
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Text(page.reference, color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Text("KJV", Modifier.padding(top = 2.dp, bottom = 16.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 12.sp)
            if (verses.isEmpty()) {
                Text("…", color = Pk.Muted, fontSize = 20.sp)
            }
            verses.forEach { v ->
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Pk.Oxblood, fontSize = (size - 7).sp, baselineShift = BaselineShift.Superscript, fontWeight = FontWeight.Bold)) {
                            append("${v.verse} ")
                        }
                        append(v.text)
                    },
                    Modifier.padding(bottom = 10.dp),
                    color = Ink, fontFamily = BookSerif, fontSize = size.sp, lineHeight = (size * 1.55f).sp,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReaderAction(
                if (speaking) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                if (speaking) "Stop" else "Listen",
                Modifier.weight(1f),
            ) {
                if (speaking) {
                    speaker.stop(); speaking = false
                } else if (speechReady && verses.isNotEmpty()) {
                    speaker.language = null
                    speaker.setSpeechRate(.9f)
                    speaker.speak(verses.joinToString(" ") { it.text }, Speaker.QUEUE_FLUSH, null, "plan")
                    speaking = true
                }
            }
            ReaderAction(Icons.Outlined.Share, "Share", Modifier.weight(1f)) {
                val text = verses.joinToString(" ") { it.text } + "\n\n" + page.reference + " (KJV)"
                shareText(context, text, "Share")
            }
        }
    }
}

@Composable
private fun ReaderAction(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(99.dp)).background(Color.White).border(1.dp, Line, RoundedCornerShape(99.dp))
            .clickable(onClick = onClick).padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Pk.Oxblood, modifier = Modifier.size(19.dp))
        Text(label, Modifier.padding(start = 8.dp), color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
    }
}

// ── 7. what stood out ───────────────────────────────────────────────────

@Composable
private fun Reflect(day: Int, question: String, onBack: () -> Unit, onSave: (String, String, Boolean) -> Unit) {
    var text by remember { mutableStateOf("") }
    var prayer by remember { mutableStateOf("") }
    var toJournal by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        TopBar(onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Text("Day $day reflection", color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 27.sp)
            Text(
                question,
                Modifier.padding(top = 8.dp, bottom = 14.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 14.5.sp, lineHeight = 21.sp,
            )
            Field(text, { text = it }, "Write your thoughts…", 170)
            Text(
                "A prayer",
                Modifier.padding(top = 18.dp, bottom = 10.dp),
                color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            )
            Field(prayer, { prayer = it }, "Write a prayer…", 110)
            Row(
                Modifier.padding(top = 14.dp).clip(RoundedCornerShape(10.dp)).clickable { toJournal = !toJournal }
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (toJournal) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank, null,
                    tint = Pk.Oxblood, modifier = Modifier.size(21.dp),
                )
                Text("Save to my journal", Modifier.padding(start = 10.dp), color = Ink, fontFamily = UtilitySans, fontSize = 14.sp)
            }
            Spacer(Modifier.height(20.dp))
        }
        Box(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Save", enabled = text.isNotBlank() || prayer.isNotBlank()) { onSave(text, prayer, toJournal) }
        }
    }
}

@Composable
private fun Field(value: String, onChange: (String) -> Unit, hint: String, height: Int) {
    Box(
        Modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(20.dp))
            .background(Color.White).border(1.dp, Line, RoundedCornerShape(20.dp)).padding(16.dp),
    ) {
        if (value.isBlank()) Text(hint, color = Pk.Muted, fontFamily = BookSerif, fontSize = 16.sp)
        BasicTextField(
            value, onChange,
            textStyle = TextStyle(color = Ink, fontFamily = BookSerif, fontSize = 16.sp, lineHeight = 25.sp),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

// ── 8. the day, kept ────────────────────────────────────────────────────

@Composable
private fun DayDone(day: Int, next: Int, onNext: () -> Unit, onProgress: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(
            Modifier.weight(1f).padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(Pk.Oxblood),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(46.dp)) }
            Text(
                "Day $day complete",
                Modifier.padding(top = 22.dp),
                color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 30.sp,
            )
            Text(
                "God is working in you.",
                Modifier.padding(top = 7.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 15.sp,
            )
            Column(
                Modifier.fillMaxWidth().padding(top = 28.dp).clip(RoundedCornerShape(20.dp))
                    .background(Pk.Oxblood.copy(alpha = .05f)).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Thy word is a lamp unto my feet, and a light unto my path.",
                    color = Ink, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontSize = 15.5.sp, lineHeight = 23.sp, textAlign = TextAlign.Center,
                )
                Text("Psalm 119:105", Modifier.padding(top = 6.dp), color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 12.5.sp)
            }
        }
        Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Go to day $next", onClick = onNext)
            Box(
                Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(99.dp))
                    .clickable(onClick = onProgress).padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("View my progress", color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

// ── 9. my plan ──────────────────────────────────────────────────────────

@Composable
private fun Progress(
    plan: ReadingPlanDef,
    progress: PlanProgress,
    onBack: () -> Unit,
    onOpen: (Int) -> Unit,
    onBrowse: () -> Unit,
) {
    val fraction = progress.done.size / plan.length.toFloat()
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        TopBar(onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Text("My plan", color = Ink, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(plan.art), null,
                    Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop,
                )
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(plan.title, color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        "${progress.done.size} of ${plan.length} days",
                        Modifier.padding(top = 2.dp),
                        color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.sp,
                    )
                }
            }
            Box(
                Modifier.fillMaxWidth().padding(top = 14.dp).height(6.dp).clip(RoundedCornerShape(99.dp)).background(Line),
            ) {
                Box(
                    Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(99.dp))
                        .background(Brush.horizontalGradient(listOf(Pk.Oxblood, Pk.GoldDeep))),
                )
            }
            Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${progress.done.size}", "Days read", Modifier.weight(1f))
                Stat("${progress.reflections}", "Reflections", Modifier.weight(1f))
                Stat("${progress.prayers}", "Prayers", Modifier.weight(1f))
            }
            Column(
                Modifier.fillMaxWidth().padding(top = 18.dp).clip(RoundedCornerShape(20.dp))
                    .background(Color.White).border(1.dp, Line, RoundedCornerShape(20.dp)).padding(vertical = 6.dp),
            ) {
                plan.days.forEachIndexed { i, d ->
                    val n = i + 1
                    val done = n in progress.done
                    Row(
                        Modifier.fillMaxWidth().clickable { onOpen(n) }.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(24.dp).clip(CircleShape)
                                .background(if (done) Pk.Oxblood else Color.Transparent)
                                .border(1.5.dp, if (done) Pk.Oxblood else Line, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { if (done) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                        Text("Day $n", Modifier.width(64.dp).padding(start = 12.dp), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 13.sp)
                        Text(d.title, Modifier.weight(1f), color = Ink, fontFamily = UtilitySans, fontSize = 14.5.sp)
                    }
                }
            }
            Text(
                "He who began a good work in you will carry it on to completion.",
                Modifier.fillMaxWidth().padding(top = 20.dp),
                color = Pk.Muted, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 15.sp, textAlign = TextAlign.Center,
            )
            Text(
                "Philippians 1:6",
                Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp),
                color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 12.sp, textAlign = TextAlign.Center,
            )
        }
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp).clip(RoundedCornerShape(99.dp))
                .border(1.dp, Line, RoundedCornerShape(99.dp)).clickable(onClick = onBrowse).padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Browse other plans", color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

// ── 10. the plan, finished ──────────────────────────────────────────────

@Composable
private fun Finished(plan: ReadingPlanDef, progress: PlanProgress, onAnother: () -> Unit, onClose: () -> Unit) {
    val context = platformContext()
    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.3f)) {
            Image(painterResource(plan.art), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Pk.DeepDark.copy(alpha = .55f)))
            Box(
                Modifier.statusBarsPadding().padding(8.dp).size(40.dp).clip(CircleShape).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(20.dp)) }
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(62.dp).clip(CircleShape).background(Color.White.copy(alpha = .22f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.MenuBook, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                Text(
                    "You did it",
                    Modifier.padding(top = 12.dp),
                    color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 30.sp,
                )
                Text(
                    "You completed ${plan.title}.",
                    Modifier.padding(top = 5.dp),
                    color = Color.White.copy(alpha = .86f), fontFamily = UtilitySans, fontSize = 14.sp,
                )
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${progress.done.size}/${plan.length}", "Days completed", Modifier.weight(1f))
                Stat("${plan.length}", "Passages read", Modifier.weight(1f))
            }
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${progress.prayers}", "Prayers written", Modifier.weight(1f))
                Stat("${progress.reflections}", "Reflections", Modifier.weight(1f))
            }
            Column(
                Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(20.dp))
                    .background(Pk.Oxblood.copy(alpha = .05f)).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Thy word is a lamp unto my feet, and a light unto my path.",
                    color = Ink, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontSize = 15.5.sp, lineHeight = 23.sp, textAlign = TextAlign.Center,
                )
                Text("Psalm 119:105", Modifier.padding(top = 6.dp), color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 12.5.sp)
            }
            Spacer(Modifier.height(20.dp))
        }
        Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
            PrimaryButton("Start another plan", onClick = onAnother)
            Box(
                Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(99.dp))
                    .clickable {
                        shareText(context, "I just finished the ${plan.length}-day ${plan.title} Bible plan on PrayerKey.", "Share")
                    }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Share my journey", color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}


/** The card on Home: the plan being read and the day, or an invitation. */
@Composable
fun BiblePlanCard(refresh: Int, onOpen: () -> Unit) {
    val context = platformContext()
    val state = remember(refresh) {
        val progress = ReadingPlanStore.load(context)
        progress to ReadingPlanStore.plan(progress.planId)
    }
    val (progress, plan) = state
    Box(
        Modifier.fillMaxWidth().height(168.dp).clip(RoundedCornerShape(28.dp)).clickable(onClick = onOpen),
    ) {
        Image(
            painterResource(plan?.art ?: Res.drawable.onboarding_read_v3), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Pk.DeepDark.copy(alpha = .90f), Pk.Oxblood.copy(alpha = .28f))),
            ),
        )
        Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 24.dp)) {
            Text(
                if (plan != null) "${plan.title} · day ${progress.current(plan.length)} of ${plan.length}" else "Bible plan",
                color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1,
            )
            Text(
                if (plan != null) "Continue reading" else "God's Word for everyday life",
                Modifier.padding(top = 8.dp),
                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 25.sp, lineHeight = 30.sp,
            )
            Text(
                "Read. Reflect. Grow.",
                Modifier.padding(top = 10.dp),
                color = Color.White.copy(alpha = .82f), fontFamily = UtilitySans, fontSize = 13.sp,
            )
        }
    }
}
