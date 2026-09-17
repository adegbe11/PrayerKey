package com.prayerkey.manna.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.DevoBlock
import com.prayerkey.manna.data.Devotional
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import java.util.Locale

/**
 * The devotional as pages you turn, not a page you scroll.
 *
 * WHY THE SCROLL HAD TO GO
 *
 * Everything the devotional contains was true and well set, and stacking it
 * into one column made it feel like a lot - because it was a lot, all visible
 * at once. Scrolling shows you the seam between two ideas and asks you to
 * carry the first past the second. The cards were an attempt to fix that by
 * drawing boxes round each idea, which only added edges: a card inside a
 * scroll inside a card.
 *
 * Turning pages removes the problem rather than decorating it. One idea holds
 * the screen, nothing else competes, and the next arrives only when you ask
 * for it. That is also why nothing here needs a card: a box exists to separate
 * one thing from its neighbours, and on a page with a single thing there are
 * no neighbours. Almost every border, fill and rule in this reader was doing
 * work the empty space now does for free.
 *
 * Pages are built from the day rather than authored, so a devotional with two
 * paragraphs and one with six both come out right.
 */

sealed class DevoPage {
    object Cover : DevoPage()
    object Scripture : DevoPage()
    data class Passage(val block: DevoBlock) : DevoPage()
    data class After(val text: String) : DevoPage()
    object Line : DevoPage()
    object Prayer : DevoPage()
    object Question : DevoPage()
    object Close : DevoPage()
}

@Composable
fun rememberDevotionalPages(day: Devotional): List<DevoPage> = remember(day.day) {
    buildList {
        add(DevoPage.Cover)
        add(DevoPage.Scripture)
        day.body.forEach { add(DevoPage.Passage(it)) }
        day.after.forEach { add(DevoPage.After(it)) }
        if (day.line.first.isNotBlank()) add(DevoPage.Line)
        add(DevoPage.Prayer)
        if (day.question.isNotBlank()) add(DevoPage.Question)
        add(DevoPage.Close)
    }
}

/**
 * How far through, drawn as one thin rule that fills.
 *
 * A segment per page was the first instinct and it was wrong: a devotional
 * runs to a dozen pages, so the rail became a row of tally marks - a count of
 * how much was left rather than a sense of progress, which is the opposite of
 * what a reader wants to feel.
 */
@Composable
fun FlipRail(page: Int, count: Int, modifier: Modifier = Modifier) {
    val fraction = if (count <= 1) 1f else (page + 1f) / count
    Box(
        modifier.fillMaxWidth().height(2.dp)
            .background(Pk.Crisp.copy(alpha = .18f)),
    ) {
        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(Pk.Oxblood))
    }
}

/** Every page sits in the same measure, and is centred in what is left. */
@Composable
private fun Page(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 30.dp),
        verticalArrangement = Arrangement.Center,
        content = content,
    )
}

@Composable
fun DevotionalPageContent(
    page: DevoPage,
    day: Devotional,
    scripture: Map<String, String>,
    dateLine: String,
    rangeLabel: String,
    voice: DevotionalVoice,
    prayerAdded: Boolean,
    completed: Boolean,
    onAddPrayer: () -> Unit,
    onOpenPassage: () -> Unit,
    onShareLine: () -> Unit,
    onComplete: () -> Unit,
) {
    when (page) {
        DevoPage.Cover -> Page {
            Text("DEVOTIONAL OF THE DAY", color = Pk.Crisp.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            val words = day.title.split(" ")
            val split = (words.size + 1) / 2
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Pk.Crisp)) { append(words.take(split).joinToString(" ")) }
                    if (words.size > split) {
                        append("\n")
                        withStyle(SpanStyle(color = Pk.VioletLit)) { append(words.drop(split).joinToString(" ")) }
                    }
                },
                fontFamily = DisplaySerif, fontSize = 44.sp, lineHeight = 48.sp,
                fontWeight = FontWeight.Light, letterSpacing = (-1.2).sp,
            )
            if (day.theme.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    day.theme, color = Pk.Crisp.copy(alpha = .72f), fontFamily = BookSerif,
                    fontWeight = FontWeight.Light, fontSize = 17.sp, lineHeight = 26.sp,
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, null, tint = Pk.Crisp.copy(alpha = .62f), modifier = Modifier.size(12.dp))
                Text(dateLine, color = Pk.Crisp.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 8.5.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 7.dp))
                Box(Modifier.padding(horizontal = 11.dp).width(1.dp).height(10.dp).background(Pk.Crisp.copy(alpha = .3f)))
                Icon(Icons.Outlined.WbSunny, null, tint = Pk.GoldDeep, modifier = Modifier.size(12.dp))
                Text("DAY ${day.seriesDay} OF ${day.seriesOf}", color = Pk.Crisp.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 8.5.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 7.dp))
            }
            Spacer(Modifier.height(34.dp))
            DevotionalAudioBar(voice)
        }

        DevoPage.Scripture -> Page {
            Text("KEY SCRIPTURE", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            val opening = scripture[day.reading.ref.label]
            Text(
                if (opening == null) "…" else "“$opening”",
                color = Pk.Crisp, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Light, fontSize = 26.sp, lineHeight = 40.sp,
                letterSpacing = (-0.4).sp,
            )
            Spacer(Modifier.height(28.dp))
            Text(
                "${day.reading.ref.label.uppercase(Locale.getDefault())} · KJV",
                color = Pk.Crisp.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 9.sp,
                letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(22.dp))
            Text(
                "Read $rangeLabel →",
                color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onOpenPassage),
            )
        }

        is DevoPage.Passage -> Page {
            when (val block = page.block) {
                is DevoBlock.Para -> Text(
                    block.text, color = Pk.Crisp.copy(alpha = .93f), fontFamily = BookSerif,
                    fontWeight = FontWeight.Light, fontSize = 21.sp, lineHeight = 36.sp,
                )
                is DevoBlock.Quote -> {
                    Text(
                        scripture[block.ref.label] ?: "…",
                        color = Pk.Crisp, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light, fontSize = 24.sp, lineHeight = 38.sp,
                    )
                    Spacer(Modifier.height(22.dp))
                    Text(
                        block.ref.label.uppercase(Locale.getDefault()),
                        color = Pk.Crisp.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 9.sp,
                        letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        is DevoPage.After -> Page {
            Text(
                page.text, color = Pk.Crisp.copy(alpha = .93f), fontFamily = BookSerif,
                fontWeight = FontWeight.Light, fontSize = 21.sp, lineHeight = 36.sp,
            )
        }

        DevoPage.Line -> Page {
            Text(
                buildAnnotatedString {
                    append(day.line.first)
                    append("\n")
                    withStyle(SpanStyle(color = Pk.GoldLight, fontStyle = FontStyle.Italic)) {
                        append(day.line.second)
                    }
                },
                color = Pk.Crisp, fontFamily = DisplaySerif, fontWeight = FontWeight.Light,
                fontSize = 33.sp, lineHeight = 43.sp, letterSpacing = (-0.8).sp,
            )
            Spacer(Modifier.height(30.dp))
            Text(
                "Share this line →",
                color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onShareLine),
            )
        }

        DevoPage.Prayer -> Page {
            Text("PRAY IT", color = Pk.GoldDeep, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Text(
                buildAnnotatedString {
                    append(day.prayer.trimEnd().removeSuffix("Amen.").removeSuffix("Amen").trimEnd())
                    append(" ")
                    withStyle(SpanStyle(color = Pk.GoldLight, fontStyle = FontStyle.Italic)) { append("Amen.") }
                },
                color = Pk.Crisp.copy(alpha = .93f), fontFamily = BookSerif,
                fontWeight = FontWeight.Light, fontSize = 21.sp, lineHeight = 34.sp,
            )
            Spacer(Modifier.height(30.dp))
            Text(
                if (prayerAdded) "Added to your list" else day.addLabel,
                color = if (prayerAdded) Color(0xFF0A7346) else Pk.Oxblood,
                fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(enabled = !prayerAdded, onClick = onAddPrayer),
            )
        }

        DevoPage.Question -> Page {
            Text("ONE QUESTION", color = Pk.Crisp.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Text(
                day.question, color = Pk.Crisp, fontFamily = DisplaySerif,
                fontWeight = FontWeight.Light, fontSize = 30.sp, lineHeight = 40.sp,
                letterSpacing = (-0.6).sp,
            )
        }

        DevoPage.Close -> Page {
            Text(
                if (completed) "Read" else "That is today",
                color = Pk.Crisp, fontFamily = DisplaySerif, fontWeight = FontWeight.Light,
                fontSize = 36.sp, letterSpacing = (-0.8).sp,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(34.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp))
                    .background(if (completed) Pk.Crisp.copy(alpha = .14f) else Pk.Oxblood)
                    .clickable(enabled = !completed, onClick = onComplete)
                    .padding(vertical = 17.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (completed) "Marked as read" else "Mark as read",
                    color = if (completed) Pk.Muted else Color.White,
                    fontFamily = UtilitySans, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
