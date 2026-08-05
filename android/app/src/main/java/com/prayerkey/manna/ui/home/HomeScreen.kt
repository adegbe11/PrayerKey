package com.prayerkey.manna.ui.home

import android.view.HapticFeedbackConstants
import android.speech.tts.TextToSpeech
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
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
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
import com.prayerkey.manna.data.JourneyInsight
import com.prayerkey.manna.ui.theme.*
import kotlin.math.roundToInt
import java.time.LocalTime
import java.util.Locale

private enum class CardState { Waiting, Revealed }

@Composable
fun HomeScreen(
    card: VerseCard,
    journeyInsight: JourneyInsight? = null,
    reduceMotion: Boolean,
    onReceived: () -> Unit,
    onReceiveNext: () -> Unit,
    onSave: (VerseCard) -> Unit,
    onPray: (VerseCard) -> Unit,
    onShare: (VerseCard) -> Unit,
) {
    var state by remember { mutableStateOf(CardState.Waiting) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var pullCount by remember { mutableIntStateOf(0) }
    var lastRevealAt by remember { mutableLongStateOf(0L) }
    var ceremonial by remember { mutableStateOf(true) }
    var showPause by remember { mutableStateOf(false) }
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
    val context = androidx.compose.ui.platform.LocalContext.current
    var speechReady by remember { mutableStateOf(false) }
    val speaker = remember {
        TextToSpeech(context.applicationContext) { status -> speechReady = status == TextToSpeech.SUCCESS }
    }
    LaunchedEffect(speechReady) {
        if (speechReady) { speaker.language = Locale.getDefault(); speaker.setSpeechRate(.88f) }
    }
    DisposableEffect(speaker) {
        onDispose { speaker.stop(); speaker.shutdown() }
    }
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
    Box(Modifier.fillMaxSize().background(dayWash())) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DeckShadow()
            VerseDeckCard(
                card = activeCard,
                revealed = state == CardState.Revealed,
                progress = progress,
                dragY = dragY,
                reduceMotion = reduceMotion,
                speechReady = speechReady,
                onPause = { showPause = true },
                onListen = {
                    speaker.speak(
                        activeCard.reference + ". " + activeCard.verse,
                        TextToSpeech.QUEUE_FLUSH, null, activeCard.reference,
                    )
                },
                onShare = { onShare(activeCard) },
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

        /* Nothing floats over the waiting card. The disc is the whole screen
           and the gesture is the whole instruction. Actions only appear once
           a word has actually been pulled. */
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                // clears the mountain: at 92dp the pills sat on the ridge line
                .padding(horizontal = 14.dp).padding(bottom = 196.dp),
        ) {
            AnimatedVisibility(
                state == CardState.Revealed,
                // crisp, slightly overshooting — Apple's own reveal, not a
                // linear dissolve
                enter = fadeIn(tween(320)) + slideInVertically(
                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                ) { it / 3 },
                exit = fadeOut(tween(160)),
            ) {
                Column {
                    /* Two pills, and only two. Champagne gold and charcoal:
                       electric blue was the loudest thing on a screen whose
                       whole point is quiet. */
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ActionButton(
                            "Pray this",
                            Brush.verticalGradient(listOf(Color(0xFFE0C063), Gold)),
                            Color(0xFF2A1F05), Modifier.weight(1f),
                        ) { onPray(activeCard) }
                        ActionButton(
                            "Save",
                            Brush.verticalGradient(listOf(Color(0xFF2C3350), Night)),
                            Ivory, Modifier.weight(1f),
                        ) { onSave(activeCard); onReceiveNext() }
                    }
                }
            }
        }
    }
    if (showPause) SacredPauseDialog(
        card = activeCard,
        why = journeyInsight?.takeIf { it.recommendedReference == activeCard.reference }?.let {
            "This word was selected because ${it.title.lowercase()} is a pattern in your private journey."
        },
        onDismiss = { showPause = false },
        onPray = { showPause = false; onPray(activeCard) },
    )
}

@Composable
private fun SacredPauseDialog(card: VerseCard, why: String?, onDismiss: () -> Unit, onPray: () -> Unit) {
    val steps = listOf(
        "Be still" to "Take one slow breath. Let your shoulders fall. You do not need to perform here.",
        "Read slowly" to card.verse,
        "Notice" to "Which word or phrase is holding your attention? Stay with it without rushing.",
        "Respond" to "Tell God honestly what this word meets in you today.",
    )
    var step by remember(card.reference) { mutableIntStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.SelfImprovement, null, tint = Gold) },
        title = { Text(steps[step].first, fontFamily = BookSerif) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(steps[step].second, textAlign = TextAlign.Center, fontSize = if (step == 1) 19.sp else 14.sp, lineHeight = if (step == 1) 27.sp else 21.sp)
                if (step == 0 && why != null) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Gold.copy(alpha = .1f), modifier = Modifier.fillMaxWidth().padding(top = 18.dp)) {
                        Text("WHY THIS WORD\n$why", color = InkSoft, fontSize = 11.sp, lineHeight = 17.sp, modifier = Modifier.padding(12.dp))
                    }
                }
                Text("${step + 1} of ${steps.size}", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 18.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = { if (step < steps.lastIndex) step++ else onPray() }) {
                Text(if (step < steps.lastIndex) "Continue" else "Pray this word")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun VerseDeckCard(
    card: VerseCard,
    revealed: Boolean,
    progress: Float,
    dragY: Float,
    reduceMotion: Boolean,
    speechReady: Boolean,
    onPause: () -> Unit,
    onListen: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier,
) {
    val shape = R.card
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
            Box(Modifier.fillMaxSize().graphicsLayer { if (!reduceMotion) rotationX = 180f }) { CardFront(card, speechReady, onPause, onListen, onShare) }
        } else CardBack()
    }
}

@Composable
private fun CardBack() {
    // the aura breathes, so the disc reads as lit rather than printed
    val pulse = rememberInfiniteTransition(label = "aura")
    val bloom by pulse.animateFloat(
        initialValue = .30f, targetValue = .52f,
        animationSpec = infiniteRepeatable(tween(3600), RepeatMode.Reverse),
        label = "bloom",
    )

    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF2A304D), Night))), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().background(TopSheen))
        Canvas(Modifier.fillMaxSize().padding(16.dp)) {
            drawRoundRect(Gold.copy(alpha = .65f), cornerRadius = CornerRadius(68f), style = Stroke(1.2f))

            /* The disc was a flat wash of gold at 11%. It is a glass
               container now: a gold aura bloomed behind it, a translucent
               dark body, and a bright rim where the light catches. */
            val r = size.minDimension * .30f
            drawCircle(
                Brush.radialGradient(
                    listOf(Gold.copy(alpha = bloom * .55f), Gold.copy(alpha = bloom * .12f), Color.Transparent),
                    center = center, radius = r * 1.95f,
                ),
                radius = r * 1.95f,
            )
            drawCircle(Color.White.copy(alpha = .05f), radius = r)
            drawCircle(Gold.copy(alpha = .30f), radius = r, style = Stroke(1.1f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚿", color = Gold, fontSize = 42.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "MANNA",
                color = Ivory, fontFamily = BookSerif,
                fontSize = 19.sp, letterSpacing = 9.sp, fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(20.dp))
            // was 68% white and disappeared into the disc behind it
            Text("Pull down to receive today’s Word", color = Color.White.copy(alpha = .92f), fontSize = 13.sp)
            Icon(Icons.Outlined.KeyboardArrowDown, null, tint = Gold, modifier = Modifier.padding(top = 6.dp).size(20.dp))
        }
    }
}

@Composable
private fun CardFront(
    card: VerseCard,
    speechReady: Boolean,
    onPause: () -> Unit,
    onListen: () -> Unit,
    onShare: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        MountainScene(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(170.dp))
        // top/bottom padding clears the floating overlays
        /* Bottom padding clears the mountain completely. Listen and Share
           used to be text links sitting at the foot of the screen, where
           the ridge line ran straight through them. */
        Column(
            Modifier.fillMaxSize().padding(horizontal = 30.dp).padding(top = 84.dp, bottom = 210.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Outlined.WbSunny, null, tint = Gold, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(24.dp))
            Text(card.verse, color = Ink, fontFamily = BookSerif, fontSize = 31.sp, lineHeight = 40.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text("${card.reference}   |   ${card.translation}", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

            // the shortcuts live with the reference now, as marks not links
            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuietAction(Icons.Outlined.SelfImprovement, "Sacred pause", onPause)
                QuietAction(Icons.Outlined.VolumeUp, "Listen to this Scripture", onListen, enabled = speechReady)
                QuietAction(Icons.Outlined.Share, "Share this Scripture", onShare)
            }
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
    }
}

@Composable
private fun DeckShadow() {
    Box(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp).offset(y = 12.dp).graphicsLayer { rotationZ = 2.2f }
        .shadow(16.dp, RoundedCornerShape(28.dp)).background(AppleGray, RoundedCornerShape(28.dp)))
}

@Composable
private fun ActionButton(label: String, brush: Brush, content: Color, modifier: Modifier, onClick: () -> Unit) {
    // glossy gradient pill with a real shadow — no flat Material button
    Box(
        // 44dp, not 50 — two full-bleed slabs dominated the reveal
        modifier.height(44.dp)
            .shadow(12.dp, R.control, spotColor = SoftShadow)
            .clip(R.control).background(brush)
            .border(0.5.dp, Color.White.copy(alpha = .35f), R.control)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = content, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

/**
 * A shortcut that does not shout. Three of these sit under the reference
 * where the eye already is, instead of a row of gold text links down at
 * the foot of the card.
 */
@Composable
private fun QuietAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Box(
        Modifier.size(42.dp)
            .clip(CircleShape)
            .background(Ink.copy(alpha = .05f))
            .border(0.7.dp, Ink.copy(alpha = .10f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon, label,
            tint = if (enabled) Ink.copy(alpha = .62f) else Ink.copy(alpha = .22f),
            modifier = Modifier.size(18.dp),
        )
    }
}
