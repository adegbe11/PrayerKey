package com.prayerkey.manna.ui.morning

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import android.view.HapticFeedbackConstants
import com.prayerkey.manna.ui.book.FoldedPage
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.UtilitySans

/**
 * An office, one movement to a page.
 *
 * WHY THIS AND NOT A SCROLL
 *
 * Morning Prayer is eleven movements. Stacked in one column they are a wall of
 * prayer you scroll through, and scrolling asks you to carry each movement past
 * the next one. An office is not read that way. You pray a movement, and then
 * you move on - which is a page turn, and is how a printed office has always
 * worked.
 *
 * THE VOICE HAD TO CHANGE WITH IT
 *
 * This is the part that would have broken quietly. OfficeVoice used to queue
 * the whole liturgy in one go, every movement back to back, closing with Amen.
 * That was right while the text sat still and the voice travelled down it. Page
 * the office without touching the voice and the two come apart at the second
 * movement: you are looking at Adore while the voice is somewhere inside Ask,
 * and nothing on screen admits it.
 *
 * So the voice speaks one movement, and the page turns when it finishes. The
 * office reads itself aloud hands-free, which is what reading an office aloud
 * is for, and the page you are on is always the words being said.
 *
 * Turning the page yourself while it speaks moves the voice with you, because
 * the alternative is the same desynchronisation arriving by a different route.
 */

sealed class OfficePage {
    object Title : OfficePage()
    data class Move(val movement: OfficeMovement, val first: Boolean) : OfficePage()

    /** A page the office itself draws - an ask field, a note from the ledger. */
    data class Extra(val key: String) : OfficePage()
    object Close : OfficePage()
}

/**
 * Builds the running order: title, movements, and whatever the office wants to
 * insert after a given rubric.
 */
fun officePages(
    movements: List<OfficeMovement>,
    after: Map<String, String> = emptyMap(),
): List<OfficePage> = buildList {
    add(OfficePage.Title)
    movements.forEachIndexed { index, movement ->
        add(OfficePage.Move(movement, first = index == 0))
        after[movement.rubric]?.let { add(OfficePage.Extra(it)) }
    }
    add(OfficePage.Close)
}

/** How far through the office, as one rule that fills. */
@Composable
private fun OfficeRail(page: Int, count: Int, palette: OfficePalette) {
    val fraction = if (count <= 1) 1f else (page + 1f) / count
    Box(
        Modifier.fillMaxWidth().height(2.dp)
            .background(palette.ink2.copy(alpha = .18f)),
    ) {
        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(palette.accent))
    }
}

/**
 * A page holds one movement, centred, and scrolls only if that movement is
 * genuinely taller than the screen - the long midnight declarations are, and
 * cutting them off to preserve the gesture would be the wrong trade.
 */
@Composable
private fun OfficeSheet(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp),
        verticalArrangement = Arrangement.Center,
        content = content,
    )
}

@Composable
fun OfficeFlip(
    title: String,
    dateLine: String,
    movements: List<OfficeMovement>,
    palette: OfficePalette,
    voice: OfficeVoice,
    speaking: Boolean,
    colophon: String,
    onClose: () -> Unit,
    /* The night offices simply end - there is nothing to mark complete at
       midnight - so the button is optional rather than a no-op. */
    finishLabel: String? = null,
    onFinish: (() -> Unit)? = null,
    /** The line under the office title: how long it takes, how to pray it. */
    length: String = "",
    /** Night Prayer closes with the wake panel. */
    closeExtra: @Composable () -> Unit = {},
    /** The brand artwork behind the office, or null for plain paper. */
    background: Int? = null,
    scale: OfficeScale = DayScale,
    after: Map<String, String> = emptyMap(),
    /** Evening hangs its Day/Night switch here. */
    trailing: @Composable (() -> Unit)? = null,
    extra: @Composable (String) -> Unit = {},
) {
    val pages = remember(movements) { officePages(movements, after) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    /*
     * A leaf going over, not a pane sliding sideways.
     *
     * The pager was a swipe: the page translated off and the next one arrived
     * behind it. Correct for a carousel, wrong for an office, which is a book.
     * FoldedPage is the Bible's turn and it is a reflection rather than a
     * rotation - the part of the sheet past the crease is that same sheet
     * mirrored about the crease, showing its reverse. The offices use it, so
     * the two books in this app turn the same way.
     *
     * Only the crease animates, and it is read inside the draw phase, so a
     * page in mid-turn never recomposes the prayer on it.
     */
    var index by remember(movements) { mutableIntStateOf(0) }
    val crease = remember { Animatable(0f) }
    var direction by remember { mutableIntStateOf(0) }

    fun settle(commit: Boolean, forward: Boolean) {
        scope.launch {
            crease.animateTo(if (commit) 1f else 0f, tween(330))
            if (commit) {
                index = (index + if (forward) 1 else -1).coerceIn(0, pages.lastIndex)
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
            direction = 0
            crease.snapTo(0f)
        }
    }

    fun turn(forward: Boolean) {
        if (forward && index >= pages.lastIndex) return
        if (!forward && index <= 0) return
        direction = if (forward) 1 else -1
        scope.launch { crease.snapTo(0f) }
        settle(commit = true, forward = forward)
    }

    /* The voice turns the page, exactly as it did with the pager. */
    DisposableEffect(voice) {
        voice.onMovementDone = {
            if (index < pages.lastIndex) voice.pendingPage = index + 1 else voice.stop()
        }
        onDispose { voice.onMovementDone = null }
    }

    LaunchedEffect(voice.pendingPage) {
        voice.pendingPage?.let { target ->
            if (target == index + 1) turn(forward = true) else index = target
            voice.pendingPage = null
        }
    }

    /* And the page turns the voice. */
    LaunchedEffect(index, speaking) {
        if (!speaking) return@LaunchedEffect
        when (val page = pages.getOrNull(index)) {
            is OfficePage.Move -> voice.speakMovement(page.movement)
            OfficePage.Close -> voice.finish()
            else -> Unit
        }
    }

    Box(Modifier.fillMaxSize().background(palette.paper)) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(10.dp))
        Box(Modifier.padding(horizontal = 26.dp)) {
            RunningHead(title, dateLine, palette, onClose, trailing)
        }
        Spacer(Modifier.height(12.dp))
        OfficeRail(index, pages.size, palette)

        Box(
            Modifier.weight(1f).pointerInput(pages.size) {
                var travelled = 0f
                detectHorizontalDragGestures(
                    onDragStart = { travelled = 0f },
                    onDragEnd = {
                        if (direction != 0) {
                            settle(commit = crease.value > .3f, forward = direction > 0)
                        }
                    },
                    onDragCancel = { if (direction != 0) settle(false, direction > 0) },
                ) { change, dragAmount ->
                    change.consume()
                    travelled -= dragAmount
                    if (direction == 0 && kotlin.math.abs(travelled) > 6f) {
                        val forward = travelled > 0
                        val blocked = (forward && index >= pages.lastIndex) ||
                            (!forward && index <= 0)
                        if (!blocked) direction = if (forward) 1 else -1
                    }
                    if (direction != 0) {
                        val fraction = (kotlin.math.abs(travelled) / size.width).coerceIn(0f, 1f)
                        scope.launch { crease.snapTo(fraction) }
                    }
                }
            },
        ) {
            FoldedPage(
                progress = { if (direction == 0) 0f else crease.value },
                under = {
                    OfficeLeafHost(
                        pages, if (direction < 0) index else index + 1,
                        title, palette, scale, speaking, length, colophon,
                        finishLabel, onFinish,
                        onListen = { if (speaking) voice.stop() else voice.startFrom(index, pages) },
                        onStopVoice = { voice.stop() },
                        closeExtra = closeExtra, extra = extra, background = background,
                    )
                },
                folding = {
                    OfficeLeafHost(
                        pages, if (direction < 0) index - 1 else index,
                        title, palette, scale, speaking, length, colophon,
                        finishLabel, onFinish,
                        onListen = { if (speaking) voice.stop() else voice.startFrom(index, pages) },
                        onStopVoice = { voice.stop() },
                        closeExtra = closeExtra, extra = extra, background = background,
                    )
                },
            )
        }
    }
    }
}

/**
 * One leaf of the office.
 *
 * Pulled out of the pager's lambda because a folded turn draws two of them at
 * once - the sheet going over and the one appearing beneath it - so a page has
 * to be something that can be asked for by number.
 */
@Composable
private fun OfficeLeafHost(
    pages: List<OfficePage>,
    index: Int,
    title: String,
    palette: OfficePalette,
    scale: OfficeScale,
    speaking: Boolean,
    length: String,
    colophon: String,
    finishLabel: String?,
    onFinish: (() -> Unit)?,
    onListen: () -> Unit,
    onStopVoice: () -> Unit,
    closeExtra: @Composable () -> Unit,
    extra: @Composable (String) -> Unit,
    background: Int?,
) {
    /*
     * The ground belongs to the leaf.
     *
     * It was painted once behind the whole stack, which looked identical until
     * a page actually turned - and then both leaves were transparent and the
     * office read as two prayers printed over each other. Paper is opaque, and
     * a fold only means anything if the sheet going over hides what is beneath
     * it. So every page carries the artwork and its scrim.
     */
    Box(Modifier.fillMaxSize().background(palette.paper)) {
    if (background != null) {
        Image(
            painterResource(background),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        /* Heavier than the picture wants, because whole movements of prayer sit
           on it. The office is read, not glanced at. */
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(
                        palette.paper.copy(alpha = .60f),
                        palette.paper.copy(alpha = .80f),
                        palette.paper.copy(alpha = .92f),
                    ),
                ),
            ),
        )
    }
    when (val page = pages.getOrNull(index) ?: return) {
        OfficePage.Title -> OfficeSheet {
            OfficeTitle(title, palette, speaking, length = length, onListen)
        }

        is OfficePage.Move -> OfficeSheet {
            Movement(page.movement, palette, dropCap = page.first, scale = scale)
        }

        is OfficePage.Extra -> OfficeSheet { extra(page.key) }

        OfficePage.Close -> OfficeSheet {
            Text(
                colophon,
                Modifier.fillMaxWidth(),
                fontFamily = UtilitySans,
                fontSize = 13.sp,
                lineHeight = 21.sp,
                color = palette.ink2,
                textAlign = TextAlign.Center,
            )
            if (finishLabel != null && onFinish != null) {
                Spacer(Modifier.height(34.dp))
                /* This was a bare Material TextButton - the one unstyled
                   control in the app, sitting where the office ends. */
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp))
                        .background(palette.accent)
                        .clickable {
                            onStopVoice()
                            onFinish()
                        }
                        .padding(vertical = 17.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        finishLabel,
                        color = palette.paper,
                        fontFamily = UtilitySans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
            closeExtra()
        }
    }
    }
}
