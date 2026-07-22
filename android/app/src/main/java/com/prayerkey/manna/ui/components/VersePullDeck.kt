package com.prayerkey.manna.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * True Tinder mechanics, vertical axis, applied to Scripture:
 *  - Z-stack: next card waits beneath, scaling from 93% as the front leaves
 *  - 1:1 gesture tracking; rotation proportional to drag distance
 *  - Velocity OR distance commits; physics fling throws the card off-screen
 *  - Overlay stamps: RECEIVE (gold) fades in on pull, SAVED (night) on push
 *  - The holy governor: 10 rushed pulls deal "Be still" on night-blue
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
    val density = LocalDensity.current
    val screenH = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val scope = rememberCoroutineScope()

    var index by remember(verses) { mutableIntStateOf(0) }
    var pulls by remember { mutableIntStateOf(0) }
    var lastPullAt by remember { mutableLongStateOf(0L) }
    var stillMode by remember { mutableStateOf(false) }
    var velocity by remember { mutableFloatStateOf(0f) }
    var lastMoveAt by remember { mutableLongStateOf(0L) }
    val offsetY = remember { Animatable(0f) }

    val current = if (stillMode)
        RemoteVerse("Psalm 46:10", "Be still, and know that I am God.", "KJV")
    else verses[index % verses.size]
    val next = verses[(index + 1) % verses.size]

    val threshold = screenH * .22f
    val progress = (offsetY.value / threshold).coerceIn(-1f, 1f)
    val pullP = progress.coerceAtLeast(0f)   // 0..1 pulling down
    val saveP = (-progress).coerceAtLeast(0f) // 0..1 pushing up

    fun advance(rushed: Boolean) {
        pulls++
        if (stillMode) stillMode = false
        else if (rushed && pulls % 10 == 0) stillMode = true
        else index++
    }

    Box(
        Modifier.fillMaxSize()
            .pointerInput(verses, stillMode) {
                detectDragGestures(
                    onDragEnd = {
                        val v = velocity
                        val y = offsetY.value
                        when {
                            y > threshold * .55f || v > 2.6f -> scope.launch {
                                // PULL COMMIT — fling off the bottom, deal next
                                val now = System.currentTimeMillis()
                                val rushed = now - lastPullAt < 1400
                                lastPullAt = now
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                offsetY.animateTo(screenH * 1.15f, tween(230))
                                advance(rushed)
                                offsetY.snapTo(0f)
                            }
                            y < -threshold * .5f || v < -2.6f -> scope.launch {
                                // SAVE COMMIT — fling off the top into the kept pile
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onSave(current)
                                offsetY.animateTo(-screenH * 1.15f, tween(230))
                                advance(rushed = false)
                                offsetY.snapTo(0f)
                            }
                            else -> scope.launch {
                                // below threshold — spring back to center
                                offsetY.animateTo(0f, spring(dampingRatio = .62f, stiffness = Spring.StiffnessMedium))
                            }
                        }
                    },
                ) { change, amount ->
                    change.consume()
                    val now = System.currentTimeMillis()
                    val dt = (now - lastMoveAt).coerceAtLeast(1)
                    velocity = amount.y / dt * 16f
                    lastMoveAt = now
                    scope.launch { offsetY.snapTo(offsetY.value + amount.y) }
                }
            },
    ) {
        /* ── back card: waits in the Z-stack, grows as the front leaves ── */
        if (!stillMode) VerseFace(
            verse = next, dimmed = true,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                val take = abs(progress)
                val grow = .93f + (.07f * take)
                scaleX = grow; scaleY = grow
                translationY = 16f * (1f - take)
            },
            onSave = null, onMemorize = null, onOpen = null,
        )

        /* ── front card: rides the thumb 1:1 — STRAIGHT down, no tilt ── */
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                translationY = offsetY.value
            },
        ) {
            VerseFace(
                verse = current, dimmed = false, still = stillMode,
                modifier = Modifier.fillMaxSize(),
                onSave = { onSave(current) },
                onMemorize = { onMemorize(current) },
                onOpen = { onOpen(current) },
            )

            /* ── Tinder stamps: opacity rides the gesture ── */
            Box(
                Modifier.align(Alignment.TopCenter).padding(top = 70.dp)
                    .graphicsLayer { alpha = pullP * 1.4f; rotationZ = -8f }
                    .border(BorderStroke(3.dp, Gold), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text("RECEIVE ✦", color = Gold, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 2.sp)
            }
            Box(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp)
                    .graphicsLayer { alpha = saveP * 1.4f; rotationZ = 8f }
                    .border(BorderStroke(3.dp, Night), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text("SAVED ♥", color = Night, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 2.sp)
            }

            /* counter + hints, overlaid — zero vertical cost */
            Row(
                Modifier.align(Alignment.TopCenter).padding(top = 46.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (stillMode) "A word for the hurry" else "${(index % verses.size) + 1} of ${verses.size}",
                    color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.1.sp,
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                Icon(Icons.Outlined.KeyboardArrowDown, null, tint = Gold, modifier = Modifier.size(13.dp))
                Text("pull", color = Muted, fontSize = 10.sp)
                Spacer(Modifier.padding(horizontal = 2.dp))
                Icon(Icons.Outlined.KeyboardArrowUp, null, tint = Gold, modifier = Modifier.size(13.dp))
                Text("save", color = Muted, fontSize = 10.sp)
            }
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
        color = Color.Transparent,
        border = BorderStroke(1.dp, if (still) Color(0xFF343A58) else Hairline),
        shadowElevation = if (dimmed) 0.dp else 24.dp,
    ) {
        Box(
            Modifier.fillMaxSize().background(
                if (still) com.prayerkey.manna.ui.theme.NightGloss
                else com.prayerkey.manna.ui.theme.IvoryGloss,
            ),
        ) {
        Box(Modifier.fillMaxSize().background(
            if (still) com.prayerkey.manna.ui.theme.TopSheen
            else com.prayerkey.manna.ui.theme.TopSheenLight,
        ))
        Column(
            Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 22.dp),
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
                fontSize = if (verse.text.length > 220) 21.sp else 26.sp,
                lineHeight = if (verse.text.length > 220) 30.sp else 37.sp,
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
}
