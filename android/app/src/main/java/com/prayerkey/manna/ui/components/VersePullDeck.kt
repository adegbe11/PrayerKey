package com.prayerkey.manna.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.RemoteVerse
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Hairline
import com.prayerkey.manna.ui.theme.Ink
import com.prayerkey.manna.ui.theme.Ivory
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.Night
import androidx.compose.ui.graphics.Color

/**
 * The pull-through verse deck — Scripture dealt one card at a time.
 * Pull DOWN to receive the next verse (it slides in from beneath with
 * spring physics + haptic). Push UP to save the current one. Every
 * tenth rapid pull deals the "Be still" card. No scrolling, ever.
 */
@Composable
fun VersePullDeck(
    verses: List<RemoteVerse>,
    onSave: (RemoteVerse) -> Unit,
    onMemorize: (RemoteVerse) -> Unit,
    onOpen: (RemoteVerse) -> Unit,
) {
    if (verses.isEmpty()) return
    val view = LocalView.current
    var index by remember(verses) { mutableIntStateOf(0) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var pulls by remember { mutableIntStateOf(0) }
    var lastPullAt by remember { mutableLongStateOf(0L) }
    var stillMode by remember { mutableStateOf(false) }

    val current = if (stillMode)
        RemoteVerse("Psalm 46:10", "Be still, and know that I am God.", "KJV")
    else verses[index % verses.size]
    val next = verses[(index + 1) % verses.size]

    val threshold = 340f
    val progress = (dragY / threshold).coerceIn(-1f, 1f)
    val settle by animateFloatAsState(
        if (dragY == 0f) 0f else progress,
        spring(dampingRatio = .74f, stiffness = Spring.StiffnessMediumLow), label = "deck-settle",
    )

    Column(Modifier.fillMaxSize()) {
        /* counter + hint line */
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (stillMode) "A word for the hurry" else "${(index % verses.size) + 1} of ${verses.size}",
                color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.KeyboardArrowDown, null, tint = Gold, modifier = Modifier.size(15.dp))
            Text("pull for next", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Icon(Icons.Outlined.KeyboardArrowUp, null, tint = Gold, modifier = Modifier.size(15.dp))
            Text("push to save", color = Muted, fontSize = 11.sp)
        }

        Box(
            Modifier.weight(1f).fillMaxWidth()
                .pointerInput(verses, index, stillMode) {
                    detectDragGestures(
                        onDragEnd = {
                            when {
                                dragY > threshold * .55f -> {
                                    // PULL — receive the next verse
                                    val now = System.currentTimeMillis()
                                    pulls++
                                    val rushing = now - lastPullAt < 1400
                                    lastPullAt = now
                                    if (stillMode) stillMode = false
                                    else if (rushing && pulls % 10 == 0) stillMode = true
                                    else index++
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                }
                                dragY < -threshold * .5f -> {
                                    // PUSH UP — save this word
                                    onSave(current)
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    if (!stillMode) index++ else stillMode = false
                                }
                            }
                            dragY = 0f
                        },
                    ) { change, amount ->
                        change.consume()
                        dragY = (dragY + amount.y).coerceIn(-threshold * 1.3f, threshold * 1.3f)
                    }
                },
        ) {
            /* next card waiting underneath — scales up as you pull */
            if (!stillMode) VerseFace(
                verse = next, dimmed = true,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    val grow = .94f + (.06f * settle.coerceAtLeast(0f))
                    scaleX = grow; scaleY = grow
                    translationY = 14f * (1f - settle.coerceAtLeast(0f))
                },
                onSave = null, onMemorize = null, onOpen = null,
            )

            /* current card — rides your thumb, tilts as it leaves */
            VerseFace(
                verse = current, dimmed = false, still = stillMode,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    translationY = dragY * .92f
                    rotationZ = settle * 2.4f
                    val fade = 1f - (settle.coerceAtLeast(0f) * .25f)
                    alpha = if (dragY < 0f) 1f - (-settle * .35f) else fade
                    val lift = 1f + (-settle.coerceAtMost(0f) * .04f)
                    scaleX = lift; scaleY = lift
                },
                onSave = { onSave(current) },
                onMemorize = { onMemorize(current) },
                onOpen = { onOpen(current) },
            )
        }
    }
}

@Composable
private fun VerseFace(
    verse: RemoteVerse,
    dimmed: Boolean,
    still: Boolean = false,
    modifier: Modifier,
    onSave: (() -> Unit)?,
    onMemorize: (() -> Unit)?,
    onOpen: (() -> Unit)?,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = if (still) Night else Ivory,
        border = BorderStroke(1.dp, if (still) Color(0xFF343A58) else Hairline),
        shadowElevation = if (dimmed) 0.dp else 14.dp,
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    verse.reference,
                    color = if (still) Gold else Ink,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Surface(shape = RoundedCornerShape(99.dp), color = Gold.copy(alpha = if (still) .3f else .15f)) {
                    Text(
                        verse.translation, color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = .6.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                verse.text,
                color = if (still) Color.White else Ink,
                fontFamily = FontFamily.Serif,
                fontSize = if (verse.text.length > 220) 20.sp else 25.sp,
                lineHeight = if (verse.text.length > 220) 29.sp else 36.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
            if (!dimmed && !still) Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                onMemorize?.let { IconButton(onClick = it) { Icon(Icons.Outlined.School, "Memorize", tint = Muted) } }
                onSave?.let { IconButton(onClick = it) { Icon(Icons.Outlined.BookmarkBorder, "Save", tint = Muted) } }
                onOpen?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Outlined.KeyboardArrowUp, "Open", tint = Muted,
                            modifier = Modifier.graphicsLayer { rotationZ = 90f })
                    }
                }
            }
            if (still) Text("Breathe this one. Then keep pulling.", color = Color.White.copy(.6f), fontSize = 12.sp)
        }
    }
}
