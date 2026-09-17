package com.prayerkey.manna.ui.morning

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
    scale: OfficeScale = DayScale,
    after: Map<String, String> = emptyMap(),
    /** Evening hangs its Day/Night switch here. */
    trailing: @Composable (() -> Unit)? = null,
    extra: @Composable (String) -> Unit = {},
) {
    val pages = remember(movements) { officePages(movements, after) }
    val pager = rememberPagerState { pages.size }

    /* The voice turns the page. Only while it is actually speaking - otherwise
       a finished utterance from a stopped office would walk the reader forward
       on its own. */
    DisposableEffect(voice) {
        voice.onMovementDone = {
            val next = pager.currentPage + 1
            if (next < pages.size) voice.pendingPage = next else voice.stop()
        }
        onDispose { voice.onMovementDone = null }
    }

    LaunchedEffect(voice.pendingPage) {
        voice.pendingPage?.let {
            pager.animateScrollToPage(it)
            voice.pendingPage = null
        }
    }

    /* And the page turns the voice. Swiping while it speaks reads the movement
       you moved to, rather than leaving the voice behind on the one you left. */
    LaunchedEffect(pager.currentPage, speaking) {
        if (!speaking) return@LaunchedEffect
        when (val page = pages.getOrNull(pager.currentPage)) {
            is OfficePage.Move -> voice.speakMovement(page.movement)
            OfficePage.Close -> voice.finish()
            else -> Unit
        }
    }

    Column(
        Modifier.fillMaxSize().background(palette.paper)
            .statusBarsPadding().navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(10.dp))
        Box(Modifier.padding(horizontal = 26.dp)) {
            RunningHead(title, dateLine, palette, onClose, trailing)
        }
        Spacer(Modifier.height(12.dp))
        OfficeRail(pager.currentPage, pages.size, palette)

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
            when (val page = pages[index]) {
                OfficePage.Title -> OfficeSheet {
                    OfficeTitle(title, palette, speaking, length = length) {
                        if (speaking) voice.stop() else voice.startFrom(pager.currentPage, pages)
                    }
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
                        /* This was a bare Material TextButton - the one
                           unstyled control in the app, sitting where the
                           office ends. */
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp))
                                .background(palette.accent)
                                .clickable {
                                    voice.stop()
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
}
