package com.prayerkey.manna.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.model.DailyVerses
import com.prayerkey.manna.ui.theme.Canvas
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.NightFill
import com.prayerkey.manna.ui.theme.R
import com.prayerkey.manna.ui.worlds.WorldVerseFace
import kotlin.math.roundToInt

private val Ivory = Color(0xFFF6F0E1)

/**
 * Onboarding by doing, not by telling.
 *
 * The previous version asked for a name on a form and described the gesture
 * in words. Two problems: the name was never displayed anywhere in the app,
 * so it was data collected for nothing at the most sensitive moment; and
 * reading about a gesture teaches nobody. A carousel of marketing slides
 * would be worse — every slide is a screen between a person and their first
 * verse.
 *
 * So: pull the card. That is the whole app, learned in four seconds, and it
 * pays out immediately. Then one screen that earns its place — the morning
 * reminder, which is the only thing that brings anyone back tomorrow.
 */
@Composable
fun OnboardingScreen(onDone: (Boolean, Int) -> Unit) {
    // 0..2 the three slides, 3 the gesture, 4 the reminder.
    // Skip jumps the slides but NOT the pull — reading about a gesture
    // teaches nobody, so everyone still does it once.
    var step by remember { mutableIntStateOf(0) }
    when (step) {
        in 0..2 -> OnboardingSlides(
            step = step,
            onStep = { step = it },
            onSkip = { step = 3 },
            onDone = { step = 3 },
        )
        3 -> PullToLearn { step = 4 }
        else -> ReminderStep(onDone)
    }
}

/* ─────────────────────── 1. the gesture itself ─────────────────────── */

@Composable
private fun PullToLearn(onPulled: () -> Unit) {
    val view = LocalView.current
    val density = LocalDensity.current
    val card = remember { DailyVerses.first() }

    var dragY by remember { mutableFloatStateOf(0f) }
    var revealed by remember { mutableStateOf(false) }
    val maxPull = with(density) { 300.dp.toPx() }
    val threshold = maxPull * .34f
    val progress by animateFloatAsState(
        if (revealed) 1f else (dragY / maxPull).coerceIn(0f, 1f),
        spring(dampingRatio = .72f, stiffness = Spring.StiffnessMediumLow),
        label = "onboard-pull",
    )

    Box(Modifier.fillMaxSize().background(Canvas)) {
        Box(
            Modifier.fillMaxSize().padding(horizontal = 14.dp)
                // the card has to clear the reveal copy AND the button, or
                // it lands on top of them
                .padding(top = 60.dp, bottom = if (revealed) 250.dp else 150.dp)
                .offset { IntOffset(0, (dragY.coerceAtLeast(0f) * .16f).roundToInt()) }
                .clip(R.card)
                .pointerInput(revealed) {
                    if (revealed) return@pointerInput
                    detectDragGestures(
                        onDragEnd = {
                            if (dragY >= threshold) {
                                revealed = true
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            } else dragY = 0f
                        },
                    ) { change, amount ->
                        change.consume()
                        dragY = (dragY + amount.y).coerceIn(0f, maxPull)
                    }
                },
        ) {
            if (progress > .5f || revealed) {
                WorldVerseFace(
                    reference = card.reference,
                    text = card.verse,
                    translation = card.translation,
                    front = true,
                    reduceMotion = false,
                    bottomPadding = 40.dp,
                )
            } else {
                CardBackFace()
            }
        }

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(horizontal = 30.dp).padding(bottom = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(!revealed) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.KeyboardArrowDown, null, tint = Gold, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Pull it down", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
            AnimatedVisibility(revealed, enter = fadeIn()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "That is the whole app.",
                        fontFamily = FontFamily.Serif, fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    com.prayerkey.manna.ui.components.PkButton(
                        label = "Continue",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onPulled,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBackFace() {
    Box(Modifier.fillMaxSize().background(NightFill), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚿", color = Gold, fontSize = 40.sp)
            Spacer(Modifier.height(14.dp))
            Text("MANNA", color = Ivory, fontSize = 13.sp, letterSpacing = 5.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/* ───────────── 2. the only thing that brings anyone back ───────────── */

@Composable
private fun ReminderStep(onDone: (Boolean, Int) -> Unit) {
    var hour by remember { mutableIntStateOf(7) }

    Column(
        Modifier.fillMaxSize().background(Canvas).padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Text("⚿", color = Gold, fontSize = 34.sp)
        Spacer(Modifier.height(26.dp))
        Text(
            "One word,\nevery morning.",
            fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
            fontSize = 30.sp, lineHeight = 38.sp, textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(36.dp))

        // a row of hours rather than a picker dialog: one tap, no modal
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(5, 6, 7, 8, 9).forEach { h ->
                val on = h == hour
                Box(
                    Modifier.size(54.dp).clip(R.pill)
                        .background(
                            if (on) NightFill
                            else Brush.verticalGradient(listOf(Color(0xFFF0F0F3), Color(0xFFF0F0F3))),
                        )
                        .clickable { hour = h },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$h",
                        color = if (on) Ivory else Muted,
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("AM", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)

        Spacer(Modifier.weight(1f))

        com.prayerkey.manna.ui.components.PkButton(
            label = "Wake me at $hour AM",
            modifier = Modifier.fillMaxWidth(),
        ) { onDone(true, hour) }

        Text(
            "Not now",
            color = Muted, fontSize = 13.sp,
            modifier = Modifier.padding(top = 18.dp, bottom = 38.dp)
                .clickable { onDone(false, hour) },
        )
    }
}
