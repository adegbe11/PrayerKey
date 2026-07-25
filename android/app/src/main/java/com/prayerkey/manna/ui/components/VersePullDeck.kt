package com.prayerkey.manna.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.prayerkey.manna.ui.theme.IvoryGloss
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.Night
import com.prayerkey.manna.ui.theme.NightGloss
import com.prayerkey.manna.ui.theme.TopSheen
import com.prayerkey.manna.ui.theme.TopSheenLight
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Tinder anatomy, exactly: the CARD IS THE SCREEN — full-bleed, edge to
 * edge — and every control floats ON TOP of it. Top overlay slot for
 * chips/search; five circular action buttons at the bottom; a scrim so
 * both stay readable. Z-stack + 1:1 drag + velocity fling + stamps.
 */
@Composable
fun VersePullDeck(
    verses: List<RemoteVerse>,
    topOverlay: @Composable () -> Unit = {},
    onSave: (RemoteVerse) -> Unit,
    onMemorize: (RemoteVerse) -> Unit,
    onShare: (RemoteVerse) -> Unit,
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
    // PERF: live thumb tracking is plain state (no coroutine per touch
    // event); the Animatable only runs fling / spring-back animations.
    var dragY by remember { mutableFloatStateOf(0f) }
    val anim = remember { Animatable(0f) }
    var animating by remember { mutableStateOf(false) }
    val offsetValue = if (animating) anim.value else dragY

    val current = if (stillMode)
        RemoteVerse("Psalm 46:10", "Be still, and know that I am God.", "KJV")
    else verses[index % verses.size]
    val next = verses[(index + 1) % verses.size]

    val threshold = screenH * .18f
    val progress = (offsetValue / threshold).coerceIn(-1f, 1f)
    val pullP = progress.coerceAtLeast(0f)
    val saveP = (-progress).coerceAtLeast(0f)

    fun advance(rushed: Boolean) {
        pulls++
        if (stillMode) stillMode = false
        else if (rushed && pulls % 10 == 0) stillMode = true
        else index++
    }

    fun flingNext() = scope.launch {
        val now = System.currentTimeMillis()
        val rushed = now - lastPullAt < 1400
        lastPullAt = now
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        animating = true; anim.snapTo(dragY)
        anim.animateTo(screenH * 1.15f, tween(220))
        advance(rushed)
        dragY = 0f; anim.snapTo(0f); animating = false
    }

    fun flingSave() = scope.launch {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        onSave(current)
        animating = true; anim.snapTo(dragY)
        anim.animateTo(-screenH * 1.15f, tween(220))
        advance(rushed = false)
        dragY = 0f; anim.snapTo(0f); animating = false
    }

    Box(
        Modifier.fillMaxSize()
            .pointerInput(verses, stillMode) {
                detectDragGestures(
                    onDragEnd = {
                        val v = velocity; val y = dragY
                        when {
                            y > threshold * .55f || v > 2.4f -> flingNext()
                            y < -threshold * .5f || v < -2.4f -> flingSave()
                            else -> scope.launch {
                                animating = true; anim.snapTo(dragY)
                                anim.animateTo(0f, spring(dampingRatio = .62f, stiffness = Spring.StiffnessMedium))
                                dragY = 0f; animating = false
                            }
                        }
                    },
                ) { change, amount ->
                    change.consume()
                    val now = System.currentTimeMillis()
                    val dt = (now - lastMoveAt).coerceAtLeast(1)
                    velocity = amount.y / dt * 16f
                    lastMoveAt = now
                    dragY += amount.y   // direct state write, no coroutine
                }
            },
    ) {
        /* back card — waits in the Z-stack, grows as the front leaves */
        if (!stillMode) VerseFace(
            verse = next, dimmed = true,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                val take = abs(progress)
                val grow = .94f + (.06f * take)
                scaleX = grow; scaleY = grow
            },
        )

        /* front card — full-bleed, rides the thumb straight down */
        Box(Modifier.fillMaxSize().graphicsLayer { translationY = offsetValue }) {
            VerseFace(
                verse = current, dimmed = false, still = stillMode,
                modifier = Modifier.fillMaxSize().clickable { onOpen(current) },
            )

            /* stamps — opacity rides the gesture, Tinder-style */
            Box(
                Modifier.align(Alignment.TopCenter).padding(top = 108.dp)
                    .graphicsLayer { alpha = pullP * 1.5f; rotationZ = -8f }
                    .border(BorderStroke(3.dp, Gold), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text("RECEIVE ✦", color = Gold, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 2.sp)
            }
            Box(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 190.dp)
                    .graphicsLayer { alpha = saveP * 1.5f; rotationZ = 8f }
                    .border(BorderStroke(3.dp, Night), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text("SAVED ♥", color = Night, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 2.sp)
            }
        }

        /* ── TOP OVERLAY: floats on the card (chips / search) ── */
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()) {
            topOverlay()
            Row(
                Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    if (stillMode) "A word for the hurry" else "${(index % verses.size) + 1} of ${verses.size}",
                    color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.1.sp,
                )
            }
        }

        /* ── BOTTOM ACTION BAR: floats on the card, Tinder's five ── */
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 96.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            ActionCircle(Icons.Outlined.Refresh, "Previous verse", Gold, 50.dp) {
                if (index > 0) { index--; view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK) }
            }
            Spacer(Modifier.size(12.dp))
            ActionCircle(Icons.Outlined.Close, "Skip", Color(0xFFE0526B), 58.dp) { flingNext() }
            Spacer(Modifier.size(12.dp))
            ActionCircle(Icons.Outlined.School, "Memorize", Color(0xFF3C7BE0), 50.dp) {
                onMemorize(current); view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }
            Spacer(Modifier.size(12.dp))
            ActionCircle(Icons.Outlined.BookmarkBorder, "Save", Color(0xFF2E9E63), 58.dp) { flingSave() }
            Spacer(Modifier.size(12.dp))
            ActionCircle(Icons.Outlined.Share, "Share", Color(0xFF3C7BE0), 50.dp) { onShare(current) }
        }
    }
}

@Composable
private fun ActionCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Box(
        Modifier.size(size)
            .shadow(10.dp, CircleShape, spotColor = Night.copy(alpha = .3f))
            .clip(CircleShape).background(Color.White)
            .border(0.5.dp, Hairline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, label, tint = tint, modifier = Modifier.size(size * .44f))
    }
}

@Composable
private fun VerseFace(
    verse: RemoteVerse,
    dimmed: Boolean,
    still: Boolean = false,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = if (dimmed) Ivory else Color.Transparent,
        border = BorderStroke(1.dp, if (still) Color(0xFF343A58) else Hairline),
        shadowElevation = if (dimmed) 0.dp else 24.dp,
    ) {
        // PERF: the waiting card paints flat — gloss/sheen/scrim only on
        // the front card, halving full-screen gradient overdraw.
        Box(Modifier.fillMaxSize().let { if (dimmed) it else it.background(if (still) NightGloss else IvoryGloss) }) {
            if (!dimmed) {
                Box(Modifier.fillMaxSize().background(if (still) TopSheen else TopSheenLight))
                /* bottom scrim so the floating action bar stays readable */
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(220.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, if (still) Night.copy(alpha = .55f) else Color(0xFFF3EDDC).copy(alpha = .92f)),
                            ),
                        ),
                )
            }

            Column(
                Modifier.fillMaxSize().padding(horizontal = 30.dp)
                    .padding(top = 150.dp, bottom = 190.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(0.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        verse.reference,
                        color = if (still) Gold else Ink,
                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
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
                    fontSize = if (verse.text.length > 220) 22.sp else 28.sp,
                    lineHeight = if (verse.text.length > 220) 32.sp else 40.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.weight(1.1f))
                if (still) Text("Breathe this one. Then keep pulling.", color = Color.White.copy(.6f), fontSize = 12.sp)
            }
        }
    }
}
