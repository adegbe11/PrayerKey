package com.prayerkey.manna.ui.home

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.DailyPassage
import com.prayerkey.manna.data.OfflineBible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun DailyPassageReader(passage: DailyPassage, onComplete: () -> Unit) {
    BackHandler(onBack = onComplete)
    val context = LocalContext.current
    var verses by remember(passage.reference) { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var page by remember(passage.reference) { mutableIntStateOf(-1) }
    var largeText by remember { mutableStateOf(false) }
    var speechReady by remember { mutableStateOf(false) }
    val speaker = remember { TextToSpeech(context.applicationContext) { speechReady = it == TextToSpeech.SUCCESS } }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }
    LaunchedEffect(speechReady) { if (speechReady) { speaker.language = Locale.getDefault(); speaker.setSpeechRate(.86f) } }
    LaunchedEffect(passage.reference) {
        verses = withContext(Dispatchers.IO) {
            OfflineBible(context).chapter(passage.book, passage.chapter)
                .filter { it.verse in passage.firstVerse..passage.lastVerse }
        }
    }
    val total = verses.size + 1
    val progress = ((page + 2).toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f)
    val cs = MaterialTheme.colorScheme

    Box(
        Modifier.fillMaxSize().background(cs.surface).clickable {
            if (page < verses.lastIndex) page++ else onComplete()
        },
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(progress = { progress }, color = cs.primary, trackColor = cs.outlineVariant, modifier = Modifier.weight(1f).height(3.dp))
                IconButton(onClick = onComplete, modifier = Modifier.padding(start = 12.dp)) { Icon(Icons.Outlined.Close, "Close") }
            }
            Text(passage.reference, color = cs.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))

            if (page == -1) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(passage.title, color = cs.onSurface, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(passage.reference.uppercase(), color = cs.onSurface.copy(alpha = .58f), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    Text(passage.introduction, color = cs.onSurface, fontSize = 20.sp, lineHeight = 31.sp, modifier = Modifier.padding(top = 28.dp))
                }
            } else if (verses.isNotEmpty()) {
                val verse = verses[page.coerceIn(0, verses.lastIndex)]
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text(verse.verse.toString(), color = cs.primary, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 9.dp))
                    Text(verse.text, color = cs.onSurface, fontSize = if (largeText) 29.sp else 24.sp, lineHeight = if (largeText) 40.sp else 34.sp)
                }
            } else Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cs.primary) }

            Text(if (page == -1) "TAP ANYWHERE TO CONTINUE" else "BIBLE VERSION: KJV", color = cs.onSurface.copy(alpha = .72f), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 14.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Aa", color = cs.onSurface, fontSize = 24.sp, modifier = Modifier.clickable { largeText = !largeText })
                Spacer(Modifier.weight(1f))
                IconButton(
                    enabled = speechReady && page >= 0 && verses.isNotEmpty(),
                    onClick = {
                        val text = verses.getOrNull(page)?.let { "Verse ${it.verse}. ${it.text}" }.orEmpty()
                        speaker.speak(text, TextToSpeech.QUEUE_FLUSH, null, passage.reference)
                    },
                ) { Icon(Icons.Outlined.VolumeUp, "Listen") }
            }
        }
    }
}
