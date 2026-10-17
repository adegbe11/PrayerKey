package com.prayerkey.manna.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay

/**
 * The app's motion, in one place, so every screen moves the same way.
 *
 * Fast and weighted, the way iOS feels: things arrive on a spring rather than
 * a timer, settle without wobbling, and the ones that matter most arrive
 * first. Nothing loops except what is meant to feel alive.
 */
object Motion {
    /** The gap between one element and the next in an entrance. */
    const val STAGGER_MS = 70L
}

/**
 * Arrives once, when first drawn: fades in and rises a little on a spring.
 * [order] staggers it after the elements before it, so a screen assembles
 * itself top to bottom instead of appearing all at once.
 */
fun Modifier.rise(order: Int = 0, distance: Float = 26f): Modifier = composed {
    val density = LocalDensity.current
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(order * Motion.STAGGER_MS)
        progress.animateTo(1f, spring(dampingRatio = .86f, stiffness = 260f))
    }
    val px = distance * density.density
    graphicsLayer {
        alpha = progress.value.coerceIn(0f, 1f)
        translationY = (1f - progress.value) * px
        val s = .97f + .03f * progress.value
        scaleX = s
        scaleY = s
    }
}

/**
 * Presses in under the finger and springs back - the thing that makes a card
 * feel like an object. It only watches the touch; taps still go to the card.
 */
fun Modifier.pressable(scaleTo: Float = .965f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) scaleTo else 1f,
        spring(dampingRatio = .55f, stiffness = 650f),
        label = "press",
    )
    this
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
        .graphicsLayer { scaleX = scale; scaleY = scale }
}

/** A photograph that slowly drifts closer, so a still image breathes. */
fun Modifier.drift(seconds: Int = 22, depth: Float = .07f): Modifier = composed {
    val t = rememberInfiniteTransition(label = "drift")
    val z by t.animateFloat(0f, 1f, infiniteRepeatable(tween(seconds * 1000, easing = LinearEasing), RepeatMode.Reverse), label = "z")
    graphicsLayer {
        val s = 1.02f + depth * z
        scaleX = s
        scaleY = s
        translationY = -12f * z * density
    }
}
