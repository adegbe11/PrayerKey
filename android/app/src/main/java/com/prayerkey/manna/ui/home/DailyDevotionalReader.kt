package com.prayerkey.manna.ui.home

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.DailyDevotional
import com.prayerkey.manna.data.DailyPassage
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Ink
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun DailyDevotionalReader(
    devotional: DailyDevotional,
    passage: DailyPassage,
    onComplete: () -> Unit,
) {
    BackHandler(onBack = onComplete)
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var verses by remember(passage.reference) { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var speechReady by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    val speaker = remember {
        TextToSpeech(context.applicationContext) { speechReady = it == TextToSpeech.SUCCESS }
    }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }
    LaunchedEffect(speechReady) {
        if (speechReady) {
            speaker.language = Locale.getDefault()
            speaker.setSpeechRate(.88f)
            speaker.setPitch(.96f)
        }
    }
    LaunchedEffect(passage.reference) {
        verses = withContext(Dispatchers.IO) {
            OfflineBible(context).chapter(passage.book, passage.chapter)
                .filter { it.verse in passage.firstVerse..passage.lastVerse }
        }
    }
    val scriptureText = verses.joinToString(" ") { "Verse ${it.verse}. ${it.text}" }
    val spokenDevotional = remember(scriptureText, devotional) {
        buildString {
            append(devotional.title).append(". Scripture. ").append(scriptureText)
            append(". Reflection. ").append(devotional.opening).append(' ')
            append(devotional.reading.joinToString(" "))
            append(". Prayer. ").append(devotional.prayer)
            append(". Application. ").append(devotional.reflectionQuestion).append(' ')
            append(devotional.practice)
        }
    }

    Column(Modifier.fillMaxSize().background(cs.surface)) {
        Row(
            Modifier.fillMaxWidth().background(Ink).padding(start = 18.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("DAILY DEVOTIONAL", color = cs.surface.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Text(passage.reference, color = cs.surface, fontFamily = UtilitySans, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
            }
            IconButton(onClick = { speaker.stop(); listening = false; onComplete() }) {
                Icon(Icons.Outlined.Close, "Close", tint = cs.surface)
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Text(devotional.title, color = cs.onSurface, fontFamily = DisplaySerif, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
            Text("5–15 MINUTES · TODAY", color = cs.onSurface.copy(alpha = .52f), fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 10.dp))

            Row(
                Modifier.padding(top = 20.dp).height(48.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (listening) cs.onSurface.copy(alpha = .08f) else cs.primary)
                    .clickable(enabled = speechReady && spokenDevotional.isNotBlank()) {
                        if (listening) {
                            speaker.stop(); listening = false
                        } else {
                            speaker.speak(spokenDevotional, TextToSpeech.QUEUE_FLUSH, null, "daily-devotional")
                            listening = true
                        }
                    }.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(if (listening) Icons.Outlined.Stop else Icons.Outlined.Headphones, null, tint = if (listening) cs.onSurface else cs.onPrimary, modifier = Modifier.size(19.dp))
                Text(if (listening) "  STOP LISTENING" else "  LISTEN", color = if (listening) cs.onSurface else cs.onPrimary, fontFamily = UtilitySans, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
            }

            SectionLabel("01", "SCRIPTURE", "THE ANCHOR")
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.background).padding(18.dp),
            ) {
                Text(passage.reference, color = cs.primary, fontFamily = UtilitySans, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                if (verses.isEmpty()) {
                    LinearProgressIndicator(color = cs.primary, trackColor = cs.onSurface.copy(alpha = .08f), modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(2.dp))
                } else {
                    verses.forEach { verse ->
                        Text("${verse.verse}  ${verse.text}", color = cs.onSurface, fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 28.sp, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }

            SectionLabel("02", "REFLECTION", "THE INSIGHT")
            Text(devotional.opening, color = cs.onSurface, fontFamily = BookSerif, fontSize = 20.sp, lineHeight = 30.sp, fontWeight = FontWeight.Medium)
            devotional.reading.forEach { paragraph ->
                Text(paragraph, color = cs.onSurface.copy(alpha = .86f), fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 28.sp, modifier = Modifier.padding(top = 18.dp))
            }

            SectionLabel("03", "PRAYER", "THE RESPONSE")
            Text("“${devotional.prayer}”", color = cs.onSurface, fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 19.sp, lineHeight = 29.sp)

            SectionLabel("04", "APPLICATION", "THE TAKEAWAY")
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Ink).padding(20.dp)) {
                Text(devotional.reflectionQuestion, color = cs.surface, fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 19.sp, lineHeight = 27.sp)
                HorizontalDivider(color = cs.surface.copy(alpha = .12f), modifier = Modifier.padding(vertical = 16.dp))
                Text("MY ACTION TODAY", color = cs.primary, fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text(devotional.practice, color = cs.surface.copy(alpha = .82f), fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 7.dp))
            }
            Spacer(Modifier.height(28.dp))
        }

        Box(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp).height(56.dp)
                .clip(RoundedCornerShape(17.dp)).background(cs.primary).clickable {
                    speaker.stop(); listening = false; onComplete()
                },
            contentAlignment = Alignment.Center,
        ) { Text("COMPLETE DEVOTIONAL", color = cs.onPrimary, fontFamily = UtilitySans, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp) }
    }
}

@Composable
private fun SectionLabel(number: String, title: String, meaning: String) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(cs.primary), contentAlignment = Alignment.Center) {
            Text(number, color = cs.onPrimary, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Text(title, color = cs.onSurface, fontFamily = DisplaySerif, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .5.sp, modifier = Modifier.padding(start = 10.dp))
        Spacer(Modifier.weight(1f))
        Text(meaning, color = cs.onSurface.copy(alpha = .45f), fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Medium, letterSpacing = .8.sp)
    }
}
