package com.prayerkey.manna.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.prayerkey.manna.model.VerseCard
import com.prayerkey.manna.ui.theme.*
import kotlin.math.roundToInt
import java.time.LocalTime

private enum class CardState { Waiting, Revealed }

@Composable
fun HomeScreen(
    card: VerseCard,
    name: String,
    reduceMotion: Boolean,
    streak: Int,
    onReceived: () -> Unit,
    onReceiveNext: () -> Unit,
    onSave: (VerseCard) -> Unit,
    onPray: (VerseCard) -> Unit,
    onShare: (VerseCard) -> Unit,
    onProfile: () -> Unit,
    onAsk: () -> Unit,
    onChurch: () -> Unit,
) {
    var state by remember { mutableStateOf(CardState.Waiting) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var pullCount by remember { mutableIntStateOf(0) }
    var lastRevealAt by remember { mutableLongStateOf(0L) }
    var ceremonial by remember { mutableStateOf(true) }
    val hour = remember { LocalTime.now().hour }
    val stillCard = remember { VerseCard("Psalm 46:10", "KJV", "Be still, and know that I am God.", "") }
    val nightCard = remember { VerseCard("Psalm 4:8", "KJV", "I will both lay me down in peace, and sleep: for thou, Lord, only makest me dwell in safety.", "") }
    val activeCard = when {
        state == CardState.Waiting && pullCount > 0 && pullCount % 10 == 0 -> stillCard
        state == CardState.Waiting && pullCount == 0 && hour in 2..4 -> nightCard
        else -> card
    }
    val density = LocalDensity.current
    val view = LocalView.current
    val maxPull = with(density) { (if (ceremonial) 300.dp else 225.dp).toPx() }
    val threshold = maxPull * .36f
    val rawProgress = (dragY / maxPull).coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        if (state == CardState.Revealed) 1f else rawProgress,
        spring(dampingRatio = .72f, stiffness = Spring.StiffnessMediumLow), label = "card-settle",
    )

    LaunchedEffect(card.reference) { state = CardState.Waiting; dragY = 0f }

    /* FULL-BLEED, same anatomy as Bible: the card owns the screen and
       every control floats on top of it. */
    Box(Modifier.fillMaxSize().background(Canvas)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DeckShadow()
            VerseDeckCard(
                card = activeCard,
                revealed = state == CardState.Revealed,
                progress = progress,
                dragY = dragY,
                reduceMotion = reduceMotion,
                modifier = Modifier.pointerInput(card.reference, state) {
                    detectDragGestures(
                        onDragStart = {
                            ceremonial = pullCount == 0 || System.currentTimeMillis() - lastRevealAt > 6_000
                        },
                        onDragEnd = {
                            if (state == CardState.Waiting && dragY >= threshold) {
                                state = CardState.Revealed
                                dragY = 0f
                                pullCount++
                                lastRevealAt = System.currentTimeMillis()
                                onReceived()
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            } else if (state == CardState.Revealed && dragY <= -threshold * .72f) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onSave(activeCard); onReceiveNext()
                            } else dragY = 0f
                        },
                    ) { change, amount ->
                        change.consume()
                        dragY = when (state) {
                            CardState.Waiting -> (dragY + amount.y).coerceIn(0f, maxPull)
                            CardState.Revealed -> (dragY + amount.y).coerceIn(-maxPull, maxPull)
                        }
                    }
                },
            )
        }

        /* The card is full-bleed, so the overlay sits on whichever face is
           showing. Dark card back means the text must go light, or the
           greeting disappears into it. */
        val onDark = state != CardState.Revealed && progress < .5f
        val headline = if (onDark) Ivory else Ink
        val secondary = if (onDark) Ivory.copy(alpha = .68f) else Muted

        /* ── TOP OVERLAY: greeting, date, streak, At-church chip ── */
        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 14.dp).padding(top = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(greeting(hour), color = secondary, fontSize = 13.sp)
                    Text(
                        name.ifBlank { "friend" },
                        color = headline, fontFamily = FontFamily.Serif,
                        fontSize = 26.sp, lineHeight = 30.sp,
                    )
                    Text(todayLabel(), color = if (onDark) Gold else Muted, fontSize = 11.sp, letterSpacing = .4.sp)
                }
                Surface(color = Color.White, shape = RoundedCornerShape(22.dp), shadowElevation = 6.dp) {
                    Text("🔥  $streak", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.size(38.dp).shadow(8.dp, CircleShape, spotColor = Night.copy(alpha = .3f))
                        .clip(CircleShape).background(NightGloss).clickable(onClick = onProfile),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        name.trim().take(1).uppercase().ifBlank { "?" },
                        color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    )
                }
            }

            /* At church chip — the growth trojan horse, one tap from the ritual */
            Surface(
                onClick = onChurch, shape = RoundedCornerShape(99.dp),
                color = Color.White, shadowElevation = 6.dp,
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("◉", color = Gold, fontSize = 12.sp)
                    Spacer(Modifier.width(7.dp))
                    Text("At church? Catch the verses", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        /* ── BOTTOM OVERLAY: actions, prompt, Ask bar — all float ── */
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(horizontal = 14.dp).padding(bottom = 92.dp),
        ) {
            AnimatedVisibility(state == CardState.Revealed) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ActionButton("Pray this", ElectricGloss, Color.White, Modifier.weight(1f)) { onPray(activeCard) }
                        ActionButton("Save", Brush.verticalGradient(listOf(Color.White, Color(0xFFEFEFF3))), Ink, Modifier.weight(1f)) { onSave(activeCard); onReceiveNext() }
                    }
                    TextButton(onClick = { onShare(activeCard) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.Share, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(7.dp)); Text("Share this word")
                    }
                }
            }
            PullPrompt(state == CardState.Revealed, headline, secondary)
            AskBar(onAsk)
        }
    }
}

private fun greeting(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning,"
    in 12..16 -> "Good afternoon,"
    in 17..21 -> "Good evening,"
    else -> "Peace to you tonight,"
}

private fun todayLabel(): String {
    val today = java.time.LocalDate.now()
    return today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")).uppercase()
}

@Composable
private fun VerseDeckCard(card: VerseCard, revealed: Boolean, progress: Float, dragY: Float, reduceMotion: Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(24.dp)
    // Resting card stands perfectly straight and flat, Tinder-style.
    // The 3D tumble only happens DURING the pull (0° -> -180° flip).
    val rotation = if (reduceMotion) 0f else -(180f * progress)
    val front = if (reduceMotion) revealed else progress > .5f || revealed
    Box(
        // Tinder-sized: the card owns the whole stage — full width, all
        // available height between the header and the bottom controls.
        modifier.fillMaxSize()
            .offset { IntOffset(0, ((dragY.coerceAtLeast(0f) * .18f) - 12).roundToInt()) }
            .graphicsLayer {
                rotationX = rotation
                cameraDistance = 18f * density
                scaleX = 1f; scaleY = 1f
                shadowElevation = 34f; this.shape = shape; clip = true
            }
            .background(if (front) Ivory else Night, shape)
            .border(1.dp, if (front) Color(0xFFE8D7B5) else Color(0xFF343A58), shape),
    ) {
        if (front) {
            Box(Modifier.fillMaxSize().graphicsLayer { if (!reduceMotion) rotationX = 180f }) { CardFront(card) }
        } else CardBack()
    }
}

@Composable
private fun CardBack() {
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF2A304D), Night))), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().background(TopSheen))
        Canvas(Modifier.fillMaxSize().padding(16.dp)) {
            drawRoundRect(Gold.copy(alpha = .65f), cornerRadius = CornerRadius(68f), style = Stroke(1.2f))
            drawCircle(Gold.copy(alpha = .11f), radius = size.minDimension * .29f)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚿", color = Gold, fontSize = 42.sp)
            Spacer(Modifier.height(14.dp))
            Text("MANNA", color = Color.White, fontSize = 13.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Medium)
            Text("FRESH EVERY MORNING", color = Gold, fontSize = 9.sp, letterSpacing = 1.8.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun CardFront(card: VerseCard) {
    Box(Modifier.fillMaxSize()) {
        MountainScene(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(170.dp))
        // top/bottom padding clears the floating overlays
        Column(Modifier.fillMaxSize().padding(horizontal = 30.dp).padding(top = 168.dp, bottom = 210.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.WbSunny, null, tint = Gold, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(24.dp))
            Text(card.verse, color = Ink, fontFamily = FontFamily.Serif, fontSize = 31.sp, lineHeight = 40.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text("${card.reference}   |   ${card.translation}", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            // sits on ivory, not on the mountain — needs a dark ink, not white
            Text("Pulled by ${card.receivedBy} people today", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun MountainScene(modifier: Modifier) {
    Canvas(modifier.background(Brush.verticalGradient(listOf(Color(0xFFFFF1CE), Color(0xFFD49E62))))) {
        val back = Path().apply {
            moveTo(0f, size.height); lineTo(0f, size.height * .58f); lineTo(size.width * .22f, size.height * .25f)
            lineTo(size.width * .42f, size.height * .64f); lineTo(size.width * .66f, size.height * .16f)
            lineTo(size.width, size.height * .62f); lineTo(size.width, size.height); close()
        }
        drawPath(back, Color(0xFFB58A64).copy(alpha = .65f))
        val front = Path().apply {
            moveTo(0f, size.height); lineTo(0f, size.height * .78f); lineTo(size.width * .25f, size.height * .53f)
            lineTo(size.width * .5f, size.height * .85f); lineTo(size.width * .75f, size.height * .48f)
            lineTo(size.width, size.height * .72f); lineTo(size.width, size.height); close()
        }
        drawPath(front, Color(0xFF765F52).copy(alpha = .8f))
        drawCircle(Color.White.copy(alpha = .85f), 5.dp.toPx(), Offset(size.width / 2, size.height * .34f))
    }
}

@Composable
private fun DeckShadow() {
    Box(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp).offset(y = 12.dp).graphicsLayer { rotationZ = 2.2f }
        .shadow(16.dp, RoundedCornerShape(28.dp)).background(AppleGray, RoundedCornerShape(28.dp)))
}

@Composable
private fun PullPrompt(revealed: Boolean, headline: Color, secondary: Color) {
    Column(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(if (revealed) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, null, tint = Gold)
        Text(
            if (revealed) "Push up to save" else "Pull down to receive",
            color = headline, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
        )
        Text(if (revealed) "Keep this word close" else "God's Word for you today", color = secondary, fontSize = 11.sp)
    }
}

@Composable
private fun ActionButton(label: String, brush: Brush, content: Color, modifier: Modifier, onClick: () -> Unit) {
    // glossy gradient pill with a real shadow — no flat Material button
    Box(
        modifier.height(50.dp)
            .shadow(10.dp, RoundedCornerShape(15.dp), spotColor = Night.copy(alpha = .28f))
            .clip(RoundedCornerShape(15.dp)).background(brush)
            .border(0.5.dp, Color.White.copy(alpha = .35f), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = content, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AskBar(onAsk: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(52.dp).shadow(12.dp, RoundedCornerShape(17.dp), spotColor = Night.copy(alpha = .22f)).clip(RoundedCornerShape(17.dp)).background(Color.White).border(0.5.dp, Hairline, RoundedCornerShape(17.dp)).clickable(onClick = onAsk).padding(start = 16.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Ask PrayerKey anything…", color = Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(40.dp).clip(CircleShape).background(ElectricGloss), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.AutoAwesome, "Ask PrayerKey", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
