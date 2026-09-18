package com.prayerkey.manna.ui.journal

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.JournalTemplate
import com.prayerkey.manna.data.TemplatePrompt
import com.prayerkey.manna.ui.book.FoldedPage
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A template, as a book.
 *
 * WHAT WAS WRONG BEFORE
 *
 * The first version handed a template's questions to the editor as a run of
 * markdown headings. Ten questions arrived on one scrolling page with ten
 * boxes under them, which is a form. Nobody prays down a form.
 *
 * WHAT A PRINTED PRAYER JOURNAL ACTUALLY DOES
 *
 * It gives a page one thing. A name in small capitals, one question, a line of
 * direction, and then space - and the space is most of the page, on purpose.
 * You do not see the next question until you turn to it. The good ones are
 * explicit that this is the point: the structure exists so that somebody at
 * seven in the morning does not have to decide what to pray about, and the
 * pacing is what stops a prayer becoming a list of requests.
 *
 * So this turns pages, with the same fold the Bible and the offices use -
 * a reflection about a crease, not a slide. One book, one gesture.
 *
 * WHAT PAPER CANNOT DO, AND THIS CAN
 *
 * A held silence that actually counts down. A prayer spoken instead of
 * written. The page you stopped on, kept, so leaving at Gratitude loses
 * nothing. And everything written here becomes one entry, searchable years
 * later, which is the whole argument for doing this on a phone at all.
 */

private sealed class BookPage {
    object Cover : BookPage()
    data class Still(val seconds: Int) : BookPage()
    data class Write(val index: Int, val prompt: TemplatePrompt) : BookPage()
    data class Listen(val seconds: Int) : BookPage()
    object Amen : BookPage()
}

private fun pagesOf(t: JournalTemplate): List<BookPage> = buildList {
    add(BookPage.Cover)
    if (t.stillness > 0) add(BookPage.Still(t.stillness))
    t.prompts.forEachIndexed { i, prompt -> add(BookPage.Write(i, prompt)) }
    if (t.listen > 0) add(BookPage.Listen(t.listen))
    add(BookPage.Amen)
}

@Composable
fun TemplateBook(
    template: JournalTemplate,
    onClose: () -> Unit,
    onFinish: (title: String, body: String, journal: String, prayer: Boolean) -> Unit,
) {
    BackHandler(onBack = onClose)
    val pages = remember(template.id) { pagesOf(template) }
    val answers = remember(template.id) { mutableStateMapOf<Int, String>() }
    val startedAt = remember(template.id) { System.currentTimeMillis() }
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    var index by remember(template.id) { mutableIntStateOf(0) }
    val crease = remember { Animatable(0f) }
    var direction by remember { mutableIntStateOf(0) }
    var turning by remember { mutableStateOf(false) }

    fun settle(commit: Boolean, forward: Boolean) {
        scope.launch {
            crease.animateTo(if (commit) 1f else 0f, tween(330))
            if (commit) {
                index = (index + if (forward) 1 else -1).coerceIn(0, pages.lastIndex)
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
            direction = 0
            crease.snapTo(0f)
            turning = false
        }
    }

    fun turn(forward: Boolean) {
        if (turning) return
        if (forward && index >= pages.lastIndex) return
        if (!forward && index <= 0) return
        turning = true
        direction = if (forward) 1 else -1
        scope.launch { crease.snapTo(0f) }
        settle(commit = true, forward = forward)
    }

    Box(Modifier.fillMaxSize().background(Pk.Cream)) {
        Box(
            Modifier.fillMaxSize().pointerInput(pages.size) {
                var travelled = 0f
                detectHorizontalDragGestures(
                    onDragStart = { travelled = 0f },
                    onDragEnd = { if (direction != 0) settle(crease.value > .3f, direction > 0) },
                    onDragCancel = { if (direction != 0) settle(false, direction > 0) },
                ) { change, drag ->
                    change.consume()
                    travelled -= drag
                    if (direction == 0 && !turning && kotlin.math.abs(travelled) > 8f) {
                        val forward = travelled > 0
                        val blocked = (forward && index >= pages.lastIndex) || (!forward && index <= 0)
                        if (!blocked) { direction = if (forward) 1 else -1; turning = true }
                    }
                    if (direction != 0) {
                        val f = (kotlin.math.abs(travelled) / size.width).coerceIn(0f, 1f)
                        scope.launch { crease.snapTo(f) }
                    }
                }
            },
        ) {
            /*
             * The fold is only mounted while a page is actually going over.
             *
             * FoldedPage records both leaves into graphics layers on every
             * frame, which is free for the offices because their pages are
             * static text. These pages hold live text fields, and keeping two
             * of them recorded continuously was enough to wedge the main
             * thread - the app stopped answering touches within seconds of
             * opening a template.
             *
             * At rest there is one page and no recording. The fold appears for
             * the 330ms of the turn and goes away again, which is the only time
             * it has anything to show.
             */
            if (direction == 0) {
                Leaf(template, pages, index, answers, startedAt,
                    onTurn = { turn(true) }, onFinish = onFinish, onClose = onClose)
            } else {
                FoldedPage(
                    progress = { crease.value },
                    under = {
                        Leaf(template, pages, if (direction < 0) index else index + 1, answers, startedAt,
                            onTurn = {}, onFinish = onFinish, onClose = onClose)
                    },
                    folding = {
                        Leaf(template, pages, if (direction < 0) index - 1 else index, answers, startedAt,
                            onTurn = {}, onFinish = onFinish, onClose = onClose)
                    },
                )
            }
        }

        /* The page number and the way out, on every leaf. A book you cannot
           leave is a trap, and one that does not say where you are is a scroll
           with extra steps. */
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, "Close", tint = Pk.Charcoal, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.weight(1f))
            if (index in 1 until pages.lastIndex) {
                Text(
                    "%02d".format(index) + " / " + "%02d".format(pages.size - 2),
                    color = Pk.Muted, fontFamily = UtilitySans,
                    fontSize = 10.sp, letterSpacing = 1.4.sp,
                    modifier = Modifier.padding(end = 16.dp),
                )
            }
        }
    }
}

/** Every leaf sits in the same measure, centred, on paper. */
@Composable
private fun Sheet(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Pk.Cream)
            .statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 34.dp, vertical = 46.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
private fun Leaf(
    template: JournalTemplate,
    pages: List<BookPage>,
    at: Int,
    answers: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, String>,
    startedAt: Long,
    onTurn: () -> Unit,
    onFinish: (String, String, String, Boolean) -> Unit,
    onClose: () -> Unit,
) {
    when (val page = pages.getOrNull(at) ?: return) {

        BookPage.Cover -> Sheet {
            Text(
                template.title.uppercase(Locale.UK),
                color = Pk.Charcoal, fontFamily = UtilitySans, fontSize = 11.sp,
                letterSpacing = 3.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.UK)),
                Modifier.padding(top = 10.dp),
                color = Pk.Muted, fontFamily = BookSerif, fontSize = 13.sp,
            )
            Spacer(Modifier.height(44.dp))
            Text("✹", color = Pk.GoldDeep, fontSize = 17.sp)
            Spacer(Modifier.height(44.dp))
            if (template.verse.isNotBlank()) {
                Text(
                    "“" + template.verse + "”",
                    color = Pk.Charcoal, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light, fontSize = 19.sp, lineHeight = 31.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    template.verseRef,
                    Modifier.padding(top = 18.dp),
                    color = Pk.Muted, fontFamily = UtilitySans, fontSize = 10.sp,
                    letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(44.dp))
            }
            Text(
                template.summary,
                color = Pk.Charcoal.copy(alpha = .70f), fontFamily = BookSerif,
                fontSize = 15.sp, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(36.dp))
            Turn("Begin", onTurn)
        }

        is BookPage.Still -> Sheet {
            Numeral(at)
            Heading("BE STILL")
            Text(
                "Before you ask for anything, become still before God.",
                Modifier.padding(top = 18.dp),
                color = Pk.Charcoal.copy(alpha = .72f), fontFamily = BookSerif,
                fontSize = 16.sp, lineHeight = 25.sp, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            HeldSilence(page.seconds)
            Spacer(Modifier.height(40.dp))
            Text(
                "“Be still, and know that I am God.”",
                color = Pk.Muted, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 14.sp, textAlign = TextAlign.Center,
            )
            Text(
                "PSALM 46:10",
                Modifier.padding(top = 8.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.6.sp,
            )
            Spacer(Modifier.height(36.dp))
            Turn("Continue", onTurn)
        }

        is BookPage.Write -> Sheet {
            Numeral(at)
            Heading(page.prompt.heading.ifBlank { "" })
            Text(
                page.prompt.question,
                Modifier.padding(top = if (page.prompt.heading.isBlank()) 0.dp else 16.dp),
                color = Pk.Charcoal, fontFamily = DisplaySerif, fontWeight = FontWeight.Light,
                fontSize = 27.sp, lineHeight = 35.sp, letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center,
            )
            if (page.prompt.guide.isNotBlank()) {
                Text(
                    page.prompt.guide,
                    Modifier.padding(top = 14.dp),
                    color = Pk.Muted, fontFamily = BookSerif, fontSize = 14.sp,
                    lineHeight = 21.sp, textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(30.dp))
            /* Ruled lines, because that is what the page under your pen looks
               like and because an empty box says "fill me in" where a ruled
               page says "there is room". */
            Ruled(
                value = answers[page.index].orEmpty(),
                onChange = { answers[page.index] = it },
            )

            if (page.prompt.private) {
                Row(
                    Modifier.padding(top = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Lock, null, tint = Pk.Muted, modifier = Modifier.size(13.dp))
                    Text(
                        "FOR YOUR EYES ONLY",
                        Modifier.padding(start = 7.dp),
                        color = Pk.Muted, fontFamily = UtilitySans,
                        fontSize = 9.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (page.prompt.starter.isNotBlank() && answers[page.index].isNullOrBlank()) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Need a beginning?",
                    color = Pk.Muted, fontFamily = UtilitySans, fontSize = 11.sp,
                )
                Text(
                    "“" + page.prompt.starter + "”",
                    Modifier.padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Pk.Oxblood.copy(alpha = .06f))
                        .clickable { answers[page.index] = page.prompt.starter + " " }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Pk.Oxblood, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontSize = 15.sp, textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(30.dp))
            Turn(if (page.prompt.skippable && answers[page.index].isNullOrBlank()) "Skip for today" else "Continue", onTurn)
        }

        is BookPage.Listen -> Sheet {
            Numeral(at)
            Heading("LISTEN")
            Text(
                "Do not rush away. Sit quietly for a moment.",
                Modifier.padding(top = 18.dp),
                color = Pk.Charcoal.copy(alpha = .72f), fontFamily = BookSerif,
                fontSize = 16.sp, lineHeight = 25.sp, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(36.dp))
            HeldSilence(page.seconds)
            Spacer(Modifier.height(36.dp))
            Text(
                "What came to mind?",
                color = Pk.Charcoal, fontFamily = DisplaySerif,
                fontWeight = FontWeight.Light, fontSize = 22.sp,
            )
            Spacer(Modifier.height(18.dp))
            Ruled(
                value = answers[-1].orEmpty(),
                onChange = { answers[-1] = it },
                lines = 3,
            )
            /* The app records what the writer noticed. It does not tell them
               God said it - that is theirs to weigh, not a phone's to assert. */
            Text(
                "A thought · a verse · a conviction · a question · nothing",
                Modifier.padding(top = 14.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 10.5.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(30.dp))
            Turn("Continue", onTurn)
        }

        BookPage.Amen -> Sheet {
            /* Whether anything was actually written. A book you opened, turned
               through and wrote nothing in should close without leaving an
               empty entry behind - the first version saved one, and a journal
               that fills up with blank pages is worse than one you forgot to
               write in. */
            val written = answers.values.any { it.isNotBlank() }
            Text("✹", color = Pk.GoldDeep, fontSize = 17.sp)
            Spacer(Modifier.height(36.dp))
            Text(
                "Amen.",
                color = Pk.Charcoal, fontFamily = DisplaySerif,
                fontWeight = FontWeight.Light, fontSize = 40.sp,
            )
            Spacer(Modifier.height(22.dp))
            Text(
                if (written) "You have placed this day before God."
                else "Nothing written. Nothing kept.",
                color = Pk.Charcoal.copy(alpha = .70f), fontFamily = BookSerif,
                fontSize = 16.sp, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(44.dp))
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM", Locale.UK)).uppercase(Locale.UK),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 10.sp, letterSpacing = 2.sp,
            )
            val minutes = ((System.currentTimeMillis() - startedAt) / 60000L).toInt()
            Text(
                if (minutes < 1) "LESS THAN A MINUTE" else "$minutes MINUTE" + (if (minutes == 1) "" else "S"),
                Modifier.padding(top = 7.dp),
                color = Pk.Muted, fontFamily = UtilitySans, fontSize = 10.sp, letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(44.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp))
                    .background(if (written) Pk.Oxblood else Pk.Charcoal.copy(alpha = .12f))
                    .clickable {
                        val body = com.prayerkey.manna.data.composeOf(template, answers.toMap())
                        if (body.isBlank()) onClose()
                        else onFinish(template.title, body, template.journal, template.prayer)
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (written) "Close the book" else "Close",
                    color = if (written) Color.White else Pk.Muted, fontFamily = UtilitySans,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable private fun Numeral(n: Int) {
    Text(
        "%02d".format(n),
        color = Pk.GoldDeep, fontFamily = UtilitySans,
        fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(20.dp))
}

@Composable private fun Heading(text: String) {
    if (text.isBlank()) return
    Text(
        text,
        color = Pk.Charcoal, fontFamily = UtilitySans, fontSize = 12.sp,
        letterSpacing = 3.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
    )
}

@Composable private fun Turn(label: String, onClick: () -> Unit) {
    Text(
        label + "  →",
        color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    )
}

/**
 * Writing space with rules under it.
 *
 * A bordered text box is a form control. Ruled lines are what a journal looks
 * like, and they say "there is room here" where a box says "fill this in".
 */
@Composable private fun Ruled(value: String, onChange: (String) -> Unit, lines: Int = 5) {
    Box(Modifier.fillMaxWidth()) {
        Column {
            repeat(lines) {
                Spacer(Modifier.height(33.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Pk.Charcoal.copy(alpha = .13f)))
            }
        }
        BasicTextField(
            value, onChange,
            textStyle = TextStyle(
                color = Pk.Charcoal, fontFamily = BookSerif,
                fontSize = 17.sp, lineHeight = 34.sp,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * A silence you can actually hold.
 *
 * Thirty seconds is a long time on a phone, which is the point. It counts down
 * once, started deliberately, and does not nag when it ends.
 */
@Composable private fun HeldSilence(seconds: Int) {
    var left by remember { mutableIntStateOf(seconds) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (left > 0) { delay(1000); left -= 1 }
        running = false
    }

    Text(
        "%d:%02d".format(left / 60, left % 60),
        color = if (running) Pk.Oxblood else Pk.Charcoal,
        fontFamily = DisplaySerif, fontWeight = FontWeight.Light, fontSize = 44.sp,
    )
    Spacer(Modifier.height(24.dp))
    if (!running && left > 0) {
        Text(
            "Begin",
            color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(CircleShape)
                .background(Pk.Oxblood.copy(alpha = .08f))
                .clickable { running = true }
                .padding(horizontal = 26.dp, vertical = 12.dp),
        )
    } else if (left == 0) {
        Text("✹", color = Pk.GoldDeep, fontSize = 15.sp)
    }
}
