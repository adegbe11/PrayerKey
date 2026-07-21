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

    Column(Modifier.fillMaxSize().background(Canvas).padding(horizontal = 14.dp)) {
        Header(name, streak, onProfile)
        Text("TODAY'S WORD", color = Muted, fontSize = 11.sp, letterSpacing = 1.4.sp, modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.height(10.dp))
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
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
        AnimatedVisibility(state == CardState.Revealed) {
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("Pray this", Electric, Color.White, Modifier.weight(1f)) { onPray(activeCard) }
                ActionButton("Save", Color(0xFFF0F0F2), Ink, Modifier.weight(1f)) { onSave(activeCard); onReceiveNext() }
            }
        }
        AnimatedVisibility(state == CardState.Revealed) {
            TextButton(onClick = { onShare(activeCard) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Share, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(7.dp)); Text("Share this word")
            }
        }
        PullPrompt(state == CardState.Revealed)
        AskBar()
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun Header(name: String, streak: Int, onProfile: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Good morning,", color = Muted, fontSize = 14.sp)
            Text("$name 👋", color = Ink, fontFamily = FontFamily.Serif, fontSize = 31.sp, lineHeight = 35.sp)
        }
        Surface(color = Color.White, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)) {
            Text("🔥  $streak", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = {}) { Icon(Icons.Outlined.NotificationsNone, "Notifications", tint = Ink) }
        Box(Modifier.size(38.dp).clip(CircleShape).background(Night).clickable(onClick = onProfile), contentAlignment = Alignment.Center) {
            Text("CA", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VerseDeckCard(card: VerseCard, revealed: Boolean, progress: Float, dragY: Float, reduceMotion: Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(28.dp)
    val rotation = if (reduceMotion) 0f else 48f - (228f * progress)
    val front = if (reduceMotion) revealed else progress > .52f || revealed
    Box(
        // Tinder-sized: the card owns the whole stage — full width, all
        // available height between the header and the bottom controls.
        modifier.fillMaxSize()
            .offset { IntOffset(0, ((dragY.coerceAtLeast(0f) * .18f) - 12).roundToInt()) }
            .graphicsLayer {
                rotationX = rotation
                cameraDistance = 18f * density
                scaleX = .94f + (.06f * progress); scaleY = .94f + (.06f * progress)
                shadowElevation = 22f; this.shape = shape; clip = true
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
        Column(Modifier.fillMaxSize().padding(horizontal = 30.dp, vertical = 34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.WbSunny, null, tint = Gold, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(40.dp))
            Text(card.verse, color = Ink, fontFamily = FontFamily.Serif, fontSize = 31.sp, lineHeight = 40.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text("${card.reference}   |   ${card.translation}", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text("Pulled by ${card.receivedBy} people today", color = Color.White, fontSize = 11.sp)
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
        .shadow(16.dp, RoundedCornerShape(28.dp)).background(Color(0xFFF2EEE5), RoundedCornerShape(28.dp)))
}

@Composable
private fun PullPrompt(revealed: Boolean) {
    Column(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(if (revealed) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, null, tint = Gold)
        Text(if (revealed) "Push up to save" else "Pull down to receive", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(if (revealed) "Keep this word close" else "God's Word for you today", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun ActionButton(label: String, color: Color, content: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick, modifier.height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = content), shape = RoundedCornerShape(15.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AskBar() {
    Row(Modifier.fillMaxWidth().height(52.dp).background(Color.White, RoundedCornerShape(17.dp)).border(1.dp, Hairline, RoundedCornerShape(17.dp)).padding(start = 16.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Ask PrayerKey anything…", color = Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(40.dp).clip(CircleShape).background(Electric), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.AutoAwesome, "Ask PrayerKey", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
