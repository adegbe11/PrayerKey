package com.prayerkey.manna.ui.guided

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PrayerMemory
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay

/**
 * The question the machine comes back with: has God answered?
 *
 * One prayer, the day it was prayed, and two honest answers. "He answered"
 * turns the sky gold and keeps the answer as a testimony; "Still praying"
 * goes straight back into the prayer, and the question returns in a week.
 */
@Composable
fun AnsweredFlow(
    kept: PrayerMemory.Kept,
    onAnswered: () -> Unit,
    onStillPraying: () -> Unit,
    onClose: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var answered by rememberSaveable { mutableStateOf(false) }
    LightIcons()
    BackHandler(onBack = onClose)

    var inhale by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) { while (true) { delay(4000); inhale = !inhale } }
    val breath by animateFloatAsState(if (inhale) 1f else 0f, tween(4000, easing = FastOutSlowInEasing), label = "breath")
    val glow by animateColorAsState(if (answered) Gold else Halo, tween(1600), label = "glow")
    val seal by animateFloatAsState(if (answered) 1f else 0f, tween(1800, easing = FastOutSlowInEasing), label = "seal")

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Deep, Night)))) {
        Heaven(breath = breath, glow = glow, seal = seal)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Box(
                Modifier.padding(12.dp).size(44.dp).clip(CircleShape).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = androidx.compose.ui.graphics.Color.White.copy(alpha = .78f)) }

            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 30.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    answered,
                    transitionSpec = {
                        (fadeIn(tween(1100, delayMillis = 300)) + slideInVertically(tween(1100, delayMillis = 300)) { it / 14 })
                            .togetherWith(fadeOut(tween(450)))
                    },
                    label = "answer",
                ) { done ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (done) "ANSWERED" else "HAS GOD ANSWERED?",
                            color = Gold.copy(alpha = .9f), fontFamily = UtilitySans,
                            fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(22.dp))
                        Text(
                            kept.request.replaceFirstChar { it.uppercase() },
                            color = if (done) GoldPale else androidx.compose.ui.graphics.Color.White,
                            fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                            fontSize = 30.sp, lineHeight = 38.sp, textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (done) "Thank You, Lord. You heard, and You answered." else "You prayed on ${kept.prayedLabel()}.",
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = .72f),
                            fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 26.sp, textAlign = TextAlign.Center,
                        )
                        if (done) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "Kept in your journal",
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = .55f),
                                fontFamily = UtilitySans, fontSize = 13.sp,
                            )
                        }
                    }
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 30.dp).padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (answered) {
                    Pill("Done", filled = false, onClick = onClose)
                } else {
                    Pill("He answered", filled = true, gold = true) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        answered = true
                        onAnswered()
                    }
                    Pill("Still praying", filled = false) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onStillPraying()
                    }
                }
            }
        }
    }
}
