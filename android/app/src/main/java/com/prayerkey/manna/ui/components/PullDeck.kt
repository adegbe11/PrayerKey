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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Hairline
import com.prayerkey.manna.ui.theme.Night
import com.prayerkey.manna.ui.theme.R
import kotlinx.coroutines.launch
import kotlin.math.abs

/** Lets a floating button trigger the same physics as a swipe. */
class DeckControls(val next: () -> Unit, val keep: () -> Unit)

/** One circular floating control, Tinder's anatomy. */
data class DeckAction(
    val icon: ImageVector,
    val label: String,
    val tint: Color,
    val size: Dp = 50.dp,
    val onClick: () -> Unit,
)

/**
 * The pull deck, extracted so the gesture lives in exactly one place.
 *
 * The card IS the screen — full bleed, edge to edge — and every control
 * floats on top of it. Pull down to move on, push up to keep. Physics are
 * committed on velocity OR distance so a flick and a deliberate drag both
 * work, and the whole thing tracks the thumb 1:1.
 *
 * PERF: live drag is plain state, never a coroutine per touch event; the
 * Animatable only runs flings and spring-backs.
 */
@Composable
fun <T> PullDeck(
    items: List<T>,
    /** Committed downward: "next". */
    onNext: (T) -> Unit = {},
    /** Committed upward: "keep this". */
    onKeep: (T) -> Unit,
    /** Stamp wording, so a verse deck and a prayer deck can differ. */
    pullStamp: String = "NEXT ✦",
    keepStamp: String = "SAVED ♥",
    /** Receives the item plus the deck's own controls, so a button can
     *  fling the card exactly as a gesture would. */
    actions: (T, DeckControls) -> List<DeckAction> = { _, _ -> emptyList() },
    topOverlay: @Composable (Int) -> Unit = {},
    card: @Composable (item: T, front: Boolean, modifier: Modifier) -> Unit,
) {
    if (items.isEmpty()) return
    val view = LocalView.current
    val density = LocalDensity.current
    val screenH = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val scope = rememberCoroutineScope()

    var index by remember(items) { mutableIntStateOf(0) }
    var velocity by remember { mutableFloatStateOf(0f) }
    var lastMoveAt by remember { mutableLongStateOf(0L) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val anim = remember { Animatable(0f) }
    var animating by remember { mutableStateOf(false) }
    val offsetValue = if (animating) anim.value else dragY

    val current = items[index % items.size]
    val next = items[(index + 1) % items.size]

    val threshold = screenH * .18f
    val progress = (offsetValue / threshold).coerceIn(-1f, 1f)
    val pullP = progress.coerceAtLeast(0f)
    val keepP = (-progress).coerceAtLeast(0f)

    fun flingNext() = scope.launch {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        animating = true; anim.snapTo(dragY)
        anim.animateTo(screenH * 1.15f, tween(220))
        onNext(current)
        index++
        dragY = 0f; anim.snapTo(0f); animating = false
    }

    fun flingKeep() = scope.launch {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        onKeep(current)
        animating = true; anim.snapTo(dragY)
        anim.animateTo(-screenH * 1.15f, tween(220))
        index++
        dragY = 0f; anim.snapTo(0f); animating = false
    }

    Box(
        Modifier.fillMaxSize().pointerInput(items) {
            detectDragGestures(
                onDragEnd = {
                    val v = velocity
                    val y = dragY
                    when {
                        y > threshold * .55f || v > 2.4f -> flingNext()
                        y < -threshold * .5f || v < -2.4f -> flingKeep()
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
        /* the waiting card — paints flat, so we are not compositing two
           full-screen gradients every frame */
        card(
            next, false,
            Modifier.fillMaxSize().graphicsLayer {
                val grow = .94f + (.06f * abs(progress))
                scaleX = grow; scaleY = grow
            },
        )

        /* the front card rides the thumb */
        Box(Modifier.fillMaxSize().graphicsLayer { translationY = offsetValue }) {
            card(current, true, Modifier.fillMaxSize())

            Box(
                Modifier.align(Alignment.TopCenter).padding(top = 108.dp)
                    .graphicsLayer { alpha = pullP * 1.5f; rotationZ = -8f }
                    .border(BorderStroke(3.dp, Gold), R.tag)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(pullStamp, color = Gold, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 2.sp)
            }
            Box(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 190.dp)
                    .graphicsLayer { alpha = keepP * 1.5f; rotationZ = 8f }
                    .border(BorderStroke(3.dp, Night), R.tag)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(keepStamp, color = Night, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 2.sp)
            }
        }

        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()) {
            topOverlay(index % items.size)
        }

        val controls = actions(current, DeckControls(next = { flingNext() }, keep = { flingKeep() }))
        /* One dock, matching the verse deck. Five loose white discs with
           coloured glyphs read as a toolbar bolted onto the artwork. */
        if (controls.isNotEmpty()) Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 108.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(Night.copy(alpha = .34f))
                .border(0.7.dp, Color.White.copy(alpha = .16f), RoundedCornerShape(34.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            controls.forEachIndexed { i, action ->
                if (i > 0) Spacer(Modifier.size(12.dp))
                ActionCircle(action)
            }
        }
    }
}

@Composable
private fun ActionCircle(action: DeckAction) {
    Box(
        Modifier.size(action.size)
            .clip(CircleShape).background(Color.White.copy(alpha = .10f))
            .border(0.7.dp, Color.White.copy(alpha = .22f), CircleShape)
            .clickable(onClick = action.onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(action.icon, action.label, tint = action.tint, modifier = Modifier.size(action.size * .44f))
    }
}
