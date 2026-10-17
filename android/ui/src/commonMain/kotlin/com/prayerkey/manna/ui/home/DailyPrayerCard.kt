package com.prayerkey.manna.ui.home

import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.ui.platform.platformContext
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.model.DailyPrayer
import com.prayerkey.manna.ui.worlds.VerseWorld
import com.prayerkey.manna.ui.worlds.WorldScene

@Composable
fun DailyPrayerCard(prayer: DailyPrayer, entrance: Int, onComplete: () -> Unit) {
    BackHandler(onBack = onComplete)
    val context = platformContext()
    var speechReady by remember { mutableStateOf(false) }
    val speaker = remember { Speaker(context) { speechReady = it == Speaker.SUCCESS } }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }
    LaunchedEffect(speechReady) { if (speechReady) { speaker.language = null; speaker.setSpeechRate(.82f) } }
    val worlds = VerseWorld.entries
    val world = worlds[(entrance * 5 + prayer.ref.hashCode()).mod(worlds.size)]
    val shape = RoundedCornerShape(30.dp)

    Box(Modifier.fillMaxSize().background(Color(0xFF091421)).padding(14.dp)) {
        Box(
            Modifier.fillMaxSize().clip(shape).border(1.dp, Color.White.copy(alpha = .28f), shape),
        ) {
            WorldScene(world, animate = true, modifier = Modifier.fillMaxSize())
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = .16f),
                        .28f to Color.Transparent,
                        .72f to Color.Black.copy(alpha = .18f),
                        1f to Color.Black.copy(alpha = .62f),
                    ),
                ),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = .18f), Color.Transparent, Color.White.copy(alpha = .04f)),
                    ),
                ),
            )

            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("PRAYERKEY", color = world.onScene, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onComplete) { Icon(Icons.Outlined.Close, "Close", tint = world.onScene) }
            }

            Column(
                Modifier.fillMaxSize().padding(top = 78.dp, bottom = 20.dp)
                    .verticalScroll(rememberScrollState()).padding(horizontal = 26.dp),
            ) {
                Text(prayer.title, color = world.onScene.copy(alpha = .72f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(prayer.ref, color = world.onScene, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 7.dp))
                Text(
                    prayer.prayer,
                    color = world.onScene, fontSize = 21.sp, lineHeight = 31.sp,
                    fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 24.dp),
                )
                Row(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        enabled = speechReady,
                        onClick = { speaker.speak(prayer.prayer, Speaker.QUEUE_FLUSH, null, prayer.ref) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = world.onScene),
                        border = androidx.compose.foundation.BorderStroke(1.dp, world.onScene.copy(alpha = .48f)),
                    ) {
                        Icon(Icons.Outlined.VolumeUp, null, modifier = Modifier.size(18.dp))
                        Text("  LISTEN", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onComplete, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = world.onScene, contentColor = Color(0xFF101820)),
                    ) { Text("Complete", fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}
