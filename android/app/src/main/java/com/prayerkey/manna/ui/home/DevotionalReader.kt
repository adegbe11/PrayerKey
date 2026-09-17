package com.prayerkey.manna.ui.home

import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.DevoBlock
import com.prayerkey.manna.data.Devotional
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.data.availableRange
import com.prayerkey.manna.data.resolveScripture
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A day of the written year.
 *
 * The shape is fixed and every day fills it: masthead, the reading, body with
 * a scripture pull-quote inside it, the line, prayer, one question, close,
 * tomorrow. Fixed because a reader who opens this every morning should not
 * have to work out where anything is — the variation belongs in the writing,
 * not in the furniture.
 *
 * **Audio sits in the nav, not at the bottom.** Someone who wants to be read
 * to decides that before they start reading, and a control they have to scroll
 * to find is a control for people who already didn't need it.
 */
@Composable
fun DevotionalReader(
    day: Devotional,
    tomorrow: Devotional?,
    onClose: () -> Unit,
    onOpenPassage: (String, Int) -> Unit,
    onAddPrayer: (String) -> Unit,
    onKeepReflection: (String) -> Unit,
    onComplete: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val scroll = rememberScrollState()

    val dateLine = remember {
        LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault()))
            .uppercase(Locale.getDefault())
    }

    /* Scripture comes out of the KJV asset, never out of the content file, so
       a devotional cannot quote a verse that does not exist. */
    var scripture by remember(day.day) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var rangeLabel by remember(day.day) { mutableStateOf(day.reading.rangeLabel) }
    LaunchedEffect(day.day) {
        val bible = OfflineBible(context)
        scripture = withContext(Dispatchers.IO) { resolveScripture(bible, day) }
        rangeLabel = availableRange(bible, day.reading)
    }

    var saved by remember { mutableStateOf(false) }
    var prayerAdded by remember { mutableStateOf(false) }
    var reflection by remember { mutableStateOf("") }
    var completed by remember { mutableStateOf(false) }
    var sharing by remember { mutableStateOf(false) }

    /* What gets read aloud, in the order it appears on screen — including the
       scripture, which is the part most worth hearing. Built from the resolved
       text so the listener and the reader get the same words. */
    val spoken = remember(day.day, scripture) {
        buildList {
            add(day.title)
            add(scripture[day.reading.ref.label].orEmpty())
            day.body.forEach {
                when (it) {
                    is DevoBlock.Para -> add(it.text)
                    is DevoBlock.Quote -> add(scripture[it.ref.label].orEmpty())
                }
            }
            add(day.line.plain)
            addAll(day.after)
            add(day.prayer)
            add(day.question)
        }.filter { it.isNotBlank() }.joinToString(" … ")
    }

    val voice = rememberDevotionalVoice(spoken)

    val progress by remember {
        derivedStateOf {
            val max = scroll.maxValue
            if (max <= 0) 0f else (scroll.value.toFloat() / max).coerceIn(0f, 1f)
        }
    }

    Box(Modifier.fillMaxSize().background(Paper)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {

            // ── progress ──────────────────────────────────────────────────
            Box(Modifier.fillMaxWidth().height(2.5.dp).background(Pk.Charcoal.copy(alpha = .06f))) {
                Box(
                    Modifier.fillMaxWidth(progress).height(2.5.dp).background(Pk.Oxblood),
                )
            }

            // ── nav ───────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                /* Close, not back. This opens straight from Home, the same way the
                   offices do, and they close with an X. An arrow promises a
                   level above that does not exist. */
                NavCircle(Icons.Outlined.Close, "Close", onClick = onClose)
                /* The bar used to carry WEDNESDAY · 3 MIN, and the masthead
                   carries WEDNESDAY 16 SEPTEMBER · 3 MIN READ sixty points
                   below it. The same two facts, twice, in the same eyeline. */
                Spacer(Modifier.weight(1f))
                NavCircle(
                    if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                    if (saved) "Saved" else "Save this devotional",
                    active = saved,
                ) { saved = !saved }
            }

            Column(
                Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 26.dp),
            ) {
                // ── masthead ──────────────────────────────────────────────
                Spacer(Modifier.height(14.dp))
                Text("DEVOTIONAL OF THE DAY", color = Pk.Muted, style = MonoLabel, fontSize = 9.sp)
                Spacer(Modifier.height(12.dp))
                /*
                 * The title turns violet where it turns personal.
                 *
                 * It was forty points of uniform black - correct, and inert.
                 * Splitting it and carrying the brand through the second half
                 * gives the page one deliberate colour event above the fold,
                 * and it lands on the part of the sentence that names what the
                 * day is about: Grace When You / Feel Small.
                 *
                 * Balanced by word, so the break falls where a typesetter would
                 * put it rather than wherever the measure happened to run out.
                 */
                val words = remember(day.title) { day.title.split(" ") }
                val split = remember(words) { (words.size + 1) / 2 }
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = Pk.Charcoal)) {
                            append(words.take(split).joinToString(" "))
                        }
                        if (words.size > split) {
                            append("\n")
                            withStyle(SpanStyle(color = Pk.Oxblood)) {
                                append(words.drop(split).joinToString(" "))
                            }
                        }
                    },
                    fontFamily = DisplaySerif,
                    fontSize = 42.sp, lineHeight = 46.sp,
                    fontWeight = FontWeight.Light, letterSpacing = (-1.2).sp,
                )
                if (day.theme.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        day.theme,
                        color = Body.copy(alpha = .78f), fontFamily = BookSerif,
                        fontWeight = FontWeight.Light, fontSize = 17.sp, lineHeight = 26.sp,
                    )
                }
                Spacer(Modifier.height(18.dp))
                /* Date, then where the day sits in its Key. Reading time left
                   this row - the player directly below already gives the
                   length, in minutes and seconds. */
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, tint = Pk.Muted, modifier = Modifier.size(13.dp))
                    Text(
                        dateLine, color = Faint, style = MonoLabel, fontSize = 8.5.sp,
                        modifier = Modifier.padding(start = 7.dp),
                    )
                    Box(
                        Modifier.padding(horizontal = 11.dp).width(1.dp).height(11.dp)
                            .background(Faint.copy(alpha = .5f)),
                    )
                    Icon(Icons.Outlined.WbSunny, null, tint = Pk.GoldDeep, modifier = Modifier.size(13.dp))
                    Text(
                        "DAY ${day.seriesDay} OF ${day.seriesOf}",
                        color = Faint, style = MonoLabel, fontSize = 8.5.sp,
                        modifier = Modifier.padding(start = 7.dp),
                    )
                }
                /* The player, where the article starts rather than in the
                   chrome. Listening is a way of taking the devotional, not a
                   utility like close or bookmark, and it belongs with the
                   thing it reads. */
                Spacer(Modifier.height(22.dp))
                DevotionalAudioBar(voice)

                Spacer(Modifier.height(22.dp))
                Hair()

                // ── the reading ───────────────────────────────────────────
                Spacer(Modifier.height(26.dp))
                Column(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(Color.White, Color(0xFFF7F3EA))))
                        .padding(22.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.MenuBook, null, tint = Pk.Oxblood, modifier = Modifier.size(17.dp))
                        Text(
                            "Key Scripture", color = Pk.Oxblood, fontFamily = UtilitySans,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 9.dp),
                        )
                    }
                    Spacer(Modifier.height(13.dp))
                    val opening = scripture[day.reading.ref.label]
                    if (opening == null) {
                        CircularProgressIndicator(
                            color = Pk.Oxblood, strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            "“$opening”",
                            color = Body, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Light, fontSize = 20.sp, lineHeight = 30.sp,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier.fillMaxWidth().height(1.dp)
                            .background(Pk.Gold.copy(alpha = .25f)),
                    )
                    Spacer(Modifier.height(13.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${day.reading.ref.label.uppercase(Locale.getDefault())} · KJV",
                            color = Faint, style = MonoLabel, fontSize = 8.5.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "Read $rangeLabel →",
                            color = Pk.Oxblood, style = MonoLabel, fontSize = 8.5.sp,
                            modifier = Modifier.clickable {
                                onOpenPassage(day.reading.book, day.reading.chapter)
                            },
                        )
                    }
                }

                // ── body ──────────────────────────────────────────────────
                Text("TODAY'S MESSAGE", color = Pk.Muted, style = MonoLabel, fontSize = 9.sp)
                Spacer(Modifier.height(16.dp))
                Spacer(Modifier.height(30.dp))
                day.body.forEachIndexed { index, block ->
                    when (block) {
                        is DevoBlock.Para -> {
                            /* The drop cap belongs to the first paragraph only,
                               and it is drawn as an oversized first character
                               rather than a real float — Compose has no float,
                               and a fake one that pushed three lines sideways
                               looked worse than a large letter does. */
                            Text(
                                if (index == 0) dropCapped(block.text) else buildAnnotatedString { append(block.text) },
                                color = Body, fontFamily = BookSerif, fontWeight = FontWeight.Light,
                                fontSize = 19.5.sp, lineHeight = 33.sp,
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                        is DevoBlock.Quote -> {
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth()) {
                                Box(
                                    Modifier.width(2.dp).height(quoteHeight(scripture[block.ref.label]))
                                        .background(Pk.Gold.copy(alpha = .5f)),
                                )
                                Column(Modifier.padding(start = 18.dp)) {
                                    Text(
                                        scripture[block.ref.label] ?: "…",
                                        color = Body.copy(alpha = .82f), fontFamily = BookSerif,
                                        fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light,
                                        fontSize = 18.sp, lineHeight = 28.sp,
                                    )
                                    Spacer(Modifier.height(9.dp))
                                    Text(
                                        block.ref.label.uppercase(Locale.getDefault()),
                                        color = Faint, style = MonoLabel, fontSize = 8.sp,
                                    )
                                }
                            }
                            Spacer(Modifier.height(26.dp))
                        }
                    }
                }

                // ── the line ──────────────────────────────────────────────
                Spacer(Modifier.height(14.dp))
                Column(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Pk.VioletLit, Pk.Oxblood, Pk.VioletDeep),
                            ),
                        )
                        .clickable { sharing = true }
                        .padding(horizontal = 24.dp, vertical = 30.dp),
                ) {
                    Text(
                        buildAnnotatedString {
                            append(day.line.first)
                            append("\n")
                            withStyle(
                                SpanStyle(
                                    color = Pk.GoldLight, fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Light,
                                ),
                            ) { append(day.line.second) }
                        },
                        color = Color.White, fontFamily = DisplaySerif,
                        fontWeight = FontWeight.Light, fontSize = 29.sp, lineHeight = 36.sp,
                        letterSpacing = (-0.6).sp,
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(
                        Modifier.fillMaxWidth().height(1.dp)
                            .background(Pk.GoldLight.copy(alpha = .28f)),
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "SHARE THIS LINE", color = Pk.GoldLight.copy(alpha = .8f),
                            style = MonoLabel, fontSize = 8.5.sp, modifier = Modifier.weight(1f),
                        )
                        Text("→", color = Pk.GoldLight.copy(alpha = .8f), fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(34.dp))
                day.after.forEach {
                    Text(
                        it, color = Body, fontFamily = BookSerif, fontWeight = FontWeight.Light,
                        fontSize = 19.5.sp, lineHeight = 33.sp,
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // ── prayer ────────────────────────────────────────────────
                Spacer(Modifier.height(14.dp))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                        .background(Color.White).padding(24.dp),
                ) {
                    Text("PRAY IT", color = Pk.GoldDeep, style = MonoLabel)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        buildAnnotatedString {
                            /* The Amen is set apart in oxblood italic, so it is
                               drawn here rather than left in the prayer text.
                               Most prayers already close with one, and appending
                               unconditionally gave every written-out prayer
                               "In Jesus' name, Amen. Amen." Take whichever is
                               there, then add the one that is styled. */
                            append(day.prayer.trimEnd().removeSuffix("Amen.").removeSuffix("Amen").trimEnd())
                            append(" ")
                            withStyle(
                                SpanStyle(color = Pk.Oxblood, fontStyle = FontStyle.Italic),
                            ) { append("Amen.") }
                        },
                        color = Body, fontFamily = BookSerif, fontWeight = FontWeight.Light,
                        fontSize = 20.sp, lineHeight = 31.sp,
                    )
                    Spacer(Modifier.height(20.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(
                                if (prayerAdded) Color(0xFF0E9B5C).copy(alpha = .1f)
                                else Pk.Oxblood.copy(alpha = .07f),
                            )
                            .clickable(enabled = !prayerAdded) {
                                prayerAdded = true
                                onAddPrayer(day.title)
                            }
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (prayerAdded) "Added to your list · day 1" else day.addLabel,
                            color = if (prayerAdded) Color(0xFF0A7346) else Pk.Oxblood,
                            fontFamily = UtilitySans, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // ── one question ──────────────────────────────────────────
                Spacer(Modifier.height(34.dp))
                Hair()
                Spacer(Modifier.height(28.dp))
                Text("ONE QUESTION", color = Faint, style = MonoLabel)
                Spacer(Modifier.height(14.dp))
                Text(
                    day.question, color = Pk.Charcoal, fontFamily = DisplaySerif,
                    fontWeight = FontWeight.Light, fontSize = 27.sp, lineHeight = 34.sp,
                    letterSpacing = (-0.5).sp,
                )
                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                        .background(Color.White).padding(17.dp),
                ) {
                    BasicTextField(
                        value = reflection,
                        /* Kept locally and written once on "mark as read" —
                           saving per keystroke would put a hundred revisions of
                           one sentence in the journal. */
                        onValueChange = { reflection = it },
                        textStyle = TextStyle(
                            color = Body, fontFamily = BookSerif,
                            fontWeight = FontWeight.Light, fontSize = 18.sp, lineHeight = 27.sp,
                        ),
                        modifier = Modifier.fillMaxWidth().height(104.dp),
                        decorationBox = { field ->
                            if (reflection.isEmpty()) {
                                Text(
                                    "A sentence is enough.",
                                    color = Pk.Charcoal.copy(alpha = .24f), fontFamily = BookSerif,
                                    fontWeight = FontWeight.Light, fontSize = 18.sp,
                                )
                            }
                            field()
                        },
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text("SAVED", color = Faint, style = MonoLabel, fontSize = 8.sp)

                // ── close ─────────────────────────────────────────────────
                Spacer(Modifier.height(36.dp))
                Hair()
                Spacer(Modifier.height(26.dp))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                        .background(
                            if (completed) Color(0xFF0E9B5C).copy(alpha = .1f) else Pk.Oxblood,
                        )
                        .clickable(enabled = !completed) {
                            completed = true
                            if (reflection.isNotBlank()) onKeepReflection(reflection)
                            onComplete()
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (completed) "Read · ${dateLine.substringAfter(' ')}" else "Mark as read",
                        color = if (completed) Color(0xFF0A7346) else Color.White,
                        fontFamily = UtilitySans, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                    )
                }

                // ── tomorrow ──────────────────────────────────────────────
                if (tomorrow != null) {
                    Spacer(Modifier.height(26.dp))
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(Color.White).padding(18.dp),
                    ) {
                        Text(
                            "TOMORROW · DAY ${tomorrow.seriesDay}",
                            color = Faint, style = MonoLabel, fontSize = 8.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            tomorrow.title, color = Pk.Charcoal, fontFamily = DisplaySerif,
                            fontWeight = FontWeight.Light, fontSize = 19.sp, lineHeight = 23.sp,
                        )
                    }
                }

                Spacer(Modifier.height(44.dp))
                Spacer(Modifier.navigationBarsPadding())
            }
        }

        // ── share sheet ───────────────────────────────────────────────────
        if (sharing) {
            Box(
                Modifier.fillMaxSize().background(Color(0xFF140330).copy(alpha = .78f))
                    .clickable { sharing = false }
                    .padding(30.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(listOf(Pk.VioletLit, Pk.Oxblood, Pk.VioletDeep)),
                        )
                        .padding(horizontal = 28.dp, vertical = 34.dp),
                ) {
                    Text("PRAYERKEY", color = Pk.GoldLight.copy(alpha = .8f), style = MonoLabel, fontSize = 12.sp)
                    Spacer(Modifier.height(34.dp))
                    Text(
                        buildAnnotatedString {
                            append(day.line.first)
                            append("\n")
                            withStyle(
                                SpanStyle(color = Pk.GoldLight, fontStyle = FontStyle.Italic),
                            ) { append(day.line.second) }
                        },
                        color = Color.White, fontFamily = DisplaySerif,
                        fontWeight = FontWeight.Light, fontSize = 31.sp, lineHeight = 38.sp,
                    )
                    Spacer(Modifier.height(34.dp))
                    Text(
                        "${day.title.uppercase(Locale.getDefault())} · " +
                            "${day.reading.book.uppercase(Locale.getDefault())} ${day.reading.chapter}",
                        color = Color.White.copy(alpha = .5f), style = MonoLabel, fontSize = 8.5.sp,
                    )
                }
            }
        }
    }
}

/* The page is warmer than the app's cream: this screen is a piece of writing
   and it should read like paper rather than like a settings pane. */
private val Paper = Color(0xFFFBFAF7)
private val Body = Color(0xFF241238)
private val Faint = Color(0xFF1A0A2E).copy(alpha = .3f)
private val Mono = Color(0xFF1A0A2E).copy(alpha = .3f)

private val MonoLabel = TextStyle(
    fontFamily = UtilitySans, fontSize = 8.5.sp,
    fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp,
)

@Composable
private fun Hair() = Box(
    Modifier.fillMaxWidth().height(1.dp).background(Pk.Charcoal.copy(alpha = .09f)),
)

@Composable
private fun NavCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    active: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = Box(
    Modifier.size(31.dp).clip(CircleShape)
        .background(Pk.Charcoal.copy(alpha = .055f))
        .clickable(enabled = enabled, onClick = onClick),
    contentAlignment = Alignment.Center,
) {
    Icon(
        icon, description,
        tint = when {
            !enabled -> Pk.Charcoal.copy(alpha = .22f)
            active -> Pk.Oxblood
            else -> Pk.Charcoal
        },
        modifier = Modifier.size(15.dp),
    )
}

/**
 * The opening letter, set large.
 *
 * Not a true drop cap — Compose cannot flow text around a float, and the
 * version that faked it by indenting the first three lines broke whenever the
 * font scale changed. An oversized initial is what a magazine does when the
 * column is too narrow to wrap anyway.
 */
private fun dropCapped(text: String) = buildAnnotatedString {
    if (text.isEmpty()) return@buildAnnotatedString
    withStyle(
        SpanStyle(
            color = Color(0xFF6200ED), fontSize = 46.sp, fontWeight = FontWeight.Light,
        ),
    ) { append(text.first()) }
    append(text.drop(1))
}

/** Rough height for the quote's gold rule, which cannot use fillMaxHeight in a
 *  Row that sizes itself from its content. */
private fun quoteHeight(text: String?): androidx.compose.ui.unit.Dp {
    val chars = text?.length ?: 40
    return (28 + (chars / 34) * 28).dp
}
