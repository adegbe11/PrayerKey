package com.prayerkey.manna.ui.home

import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.platform.format
import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.resources.*
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
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
import kotlinx.datetime.LocalDate
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
    val context = platformContext()

    val dateLine = remember {
        today()
            .format("EEEE d MMMM")
            
    }

    /* Scripture comes out of the KJV asset, never out of the content file, so
       a devotional cannot quote a verse that does not exist. */
    var scripture by remember(day.day) { mutableStateOf<Map<String, String>>(emptyMap()) }
    var rangeLabel by remember(day.day) { mutableStateOf(day.reading.rangeLabel) }
    LaunchedEffect(day.day) {
        val bible = OfflineBible(context)
        scripture = withContext(ioDispatcher) { resolveScripture(bible, day) }
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

    /*
     * The dawn artwork carries the reader, and the white is gone.
     *
     * Every page was cream with white cards on it, so the devotional looked
     * like a form: boxes of text stacked on a background whose only job was to
     * be lighter than they were. The app already owns a picture that says what
     * this screen is for, and it is the one people meet on the way in.
     *
     * The scrim is doing real work and is heavier than the welcome screen's.
     * There a name and a button sit over the image; here whole paragraphs do,
     * and body text over a photograph is the fastest way to make something
     * beautiful unreadable. It deepens towards the foot, where the longest
     * lines fall and the picture is busiest.
     */
    Box(Modifier.fillMaxSize().background(Pk.DeepDark)) {
        Image(
            painterResource(Res.drawable.welcome_prayer_dawn),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(
                        Pk.DeepDark.copy(alpha = .58f),
                        Pk.DeepDark.copy(alpha = .78f),
                        Pk.DeepDark.copy(alpha = .90f),
                    ),
                ),
            ),
        )
        Column(Modifier.fillMaxSize().statusBarsPadding()) {

            // ── progress ──────────────────────────────────────────────────
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

            val pages = rememberDevotionalPages(day)
            val pager = androidx.compose.foundation.pager.rememberPagerState { pages.size }

            /* How far through, now that there is no scroll to measure. The old
               rule tracked scroll.value against scroll.maxValue, which after
               the flip meant a bar that never moved. */
            FlipRail(pager.currentPage, pages.size)
            Spacer(Modifier.height(10.dp))

            androidx.compose.foundation.pager.HorizontalPager(
                state = pager,
                modifier = Modifier.weight(1f),
                pageSpacing = 0.dp,
            ) { index ->
                DevotionalPageContent(
                    page = pages[index],
                    day = day,
                    scripture = scripture,
                    dateLine = dateLine,
                    rangeLabel = rangeLabel,
                    voice = voice,
                    prayerAdded = prayerAdded,
                    completed = completed,
                    onAddPrayer = { prayerAdded = true; onAddPrayer(day.title) },
                    onOpenPassage = { onOpenPassage(day.reading.book, day.reading.chapter) },
                    onShareLine = { sharing = true },
                    onComplete = { completed = true; onComplete() },
                )
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
                        "${day.title} · " +
                            "${day.reading.book} ${day.reading.chapter}",
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

private val MonoLabel: TextStyle get() = TextStyle(
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
    // dark ground now, so the circles are lit rather than shaded
    Modifier.size(31.dp).clip(CircleShape)
        .background(Pk.Crisp.copy(alpha = .16f))
        .clickable(enabled = enabled, onClick = onClick),
    contentAlignment = Alignment.Center,
) {
    Icon(
        icon, description,
        tint = when {
            !enabled -> Pk.Crisp.copy(alpha = .3f)
            active -> Pk.GoldLight
            else -> Pk.Crisp
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
