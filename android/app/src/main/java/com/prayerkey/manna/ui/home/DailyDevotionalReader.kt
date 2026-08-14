package com.prayerkey.manna.ui.home

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DailyDevotionalReader(
    devotional: DailyDevotional,
    passage: DailyPassage,
    onComplete: () -> Unit,
) {
    BackHandler(onBack = onComplete)
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val today = remember { LocalDate.now() }
    val date = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())).uppercase(Locale.getDefault())
    }
    var verses by remember(passage.reference) { mutableStateOf<List<BibleVerse>>(emptyList()) }
    var speechReady by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var prayerAdded by remember { mutableStateOf(false) }
    var reflection by remember { mutableStateOf("") }
    var completed by remember { mutableStateOf(false) }

    val speaker = remember {
        TextToSpeech(context.applicationContext) { speechReady = it == TextToSpeech.SUCCESS }
    }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }
    LaunchedEffect(speechReady) {
        if (speechReady) {
            speaker.language = Locale.getDefault()
            speaker.setSpeechRate(.88f)
            speaker.setPitch(.97f)
        }
    }
    LaunchedEffect(passage.reference) {
        verses = withContext(Dispatchers.IO) {
            OfflineBible(context).chapter(passage.book, passage.chapter)
                .filter { it.verse in passage.firstVerse..passage.lastVerse }
        }
    }

    val scripture = verses.joinToString(" ") { "${it.verse} ${it.text}" }
    val spokenDevotional = remember(scripture, devotional) {
        listOf(
            devotional.title,
            scripture,
            devotional.opening,
            devotional.reading.joinToString(" "),
            devotional.prayer,
            devotional.reflectionQuestion,
        ).filter { it.isNotBlank() }.joinToString(". ")
    }

    Box(Modifier.fillMaxSize().background(Pk.Cream)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(Pk.Charcoal.copy(alpha = .06f))) {
                val progress = if (scroll.maxValue == 0) 0f else scroll.value.toFloat() / scroll.maxValue
                Box(Modifier.fillMaxWidth(progress).height(2.dp).background(Pk.Oxblood))
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuietIcon(onClick = { speaker.stop(); onComplete() }) {
                    Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Charcoal, modifier = Modifier.size(15.dp))
                }
                Text(
                    "$date  ·  4 MIN",
                    color = Pk.Charcoal.copy(alpha = .58f),
                    fontFamily = UtilitySans,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.1.sp,
                    modifier = Modifier.weight(1f).padding(start = 14.dp),
                )
                QuietIcon(onClick = {
                    if (listening) {
                        speaker.stop(); listening = false
                    } else if (speechReady) {
                        speaker.speak(spokenDevotional, TextToSpeech.QUEUE_FLUSH, null, "devotional")
                        listening = true
                    }
                }) {
                    Icon(
                        if (listening) Icons.Outlined.Stop else Icons.Outlined.Headphones,
                        if (listening) "Stop listening" else "Listen",
                        tint = if (listening) Pk.Oxblood else Pk.Charcoal,
                        modifier = Modifier.size(17.dp),
                    )
                }
                Spacer(Modifier.size(7.dp))
                QuietIcon(onClick = { saved = !saved }) {
                    Icon(
                        Icons.Outlined.BookmarkBorder,
                        if (saved) "Saved" else "Save",
                        tint = if (saved) Pk.Oxblood else Pk.Charcoal,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }

            Column(
                Modifier.weight(1f).verticalScroll(scroll).padding(horizontal = 26.dp),
            ) {
                Text(
                    "DAILY DEVOTIONAL",
                    color = Pk.GoldDeep,
                    fontFamily = UtilitySans,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.7.sp,
                    modifier = Modifier.padding(top = 18.dp),
                )
                Text(
                    devotional.title,
                    color = Pk.Charcoal,
                    fontFamily = DisplaySerif,
                    fontSize = 39.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 13.dp),
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(date, color = Pk.Charcoal.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.sp)
                    Box(Modifier.size(3.dp).clip(CircleShape).background(Pk.Charcoal.copy(alpha = .3f)))
                    Text("4 MIN READ", color = Pk.Charcoal.copy(alpha = .62f), fontFamily = UtilitySans, fontSize = 9.sp, letterSpacing = 1.sp)
                }
                HorizontalDivider(color = Pk.Charcoal.copy(alpha = .09f))

                ScriptureCard(passage = passage, verses = verses)

                Text(
                    devotional.opening,
                    color = Pk.Charcoal,
                    fontFamily = BookSerif,
                    fontSize = 20.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 30.dp),
                )
                devotional.reading.forEach { paragraph ->
                    Text(
                        paragraph,
                        color = Pk.Charcoal.copy(alpha = .9f),
                        fontFamily = BookSerif,
                        fontSize = 19.sp,
                        lineHeight = 32.sp,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }

                Box(
                    Modifier.fillMaxWidth().padding(top = 34.dp).clip(RoundedCornerShape(24.dp))
                        .background(Pk.Oxblood).padding(horizontal = 24.dp, vertical = 28.dp),
                ) {
                    Column {
                        Text(
                            devotional.declaration,
                            color = Pk.Crisp,
                            fontFamily = DisplaySerif,
                            fontSize = 27.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.Normal,
                        )
                        HorizontalDivider(color = Pk.GoldLight.copy(alpha = .32f), modifier = Modifier.padding(top = 20.dp, bottom = 14.dp))
                        Text("KEEP THIS WITH YOU", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.3.sp)
                    }
                }

                Column(
                    Modifier.fillMaxWidth().padding(top = 34.dp).clip(RoundedCornerShape(22.dp))
                        .background(Pk.Sunken).padding(24.dp),
                ) {
                    EditorialLabel("PRAY IT")
                    Text(
                        devotional.prayer,
                        color = Pk.Charcoal,
                        fontFamily = BookSerif,
                        fontSize = 20.sp,
                        lineHeight = 31.sp,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                    Text("Amen.", color = Pk.Oxblood, fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 20.sp, modifier = Modifier.padding(top = 6.dp))
                    Box(
                        Modifier.fillMaxWidth().padding(top = 20.dp).clip(RoundedCornerShape(16.dp))
                            .background(if (prayerAdded) Pk.GoldWash else Pk.Oxblood.copy(alpha = .07f))
                            .clickable { prayerAdded = true }.padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (prayerAdded) "Added to my prayer list" else "Add this to my prayer list",
                            color = if (prayerAdded) Pk.GoldDeep else Pk.Oxblood,
                            fontFamily = UtilitySans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Column(Modifier.fillMaxWidth().padding(top = 34.dp)) {
                    HorizontalDivider(color = Pk.Charcoal.copy(alpha = .09f))
                    EditorialLabel("ONE QUESTION", Modifier.padding(top = 28.dp))
                    Text(
                        devotional.reflectionQuestion,
                        color = Pk.Charcoal,
                        fontFamily = DisplaySerif,
                        fontSize = 27.sp,
                        lineHeight = 34.sp,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                    BasicTextField(
                        value = reflection,
                        onValueChange = { reflection = it },
                        textStyle = TextStyle(color = Pk.Charcoal, fontFamily = BookSerif, fontSize = 18.sp, lineHeight = 27.sp),
                        modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(112.dp)
                            .clip(RoundedCornerShape(17.dp)).background(Pk.Sunken).padding(17.dp),
                        decorationBox = { field ->
                            Box {
                                if (reflection.isBlank()) Text("A sentence is enough.", color = Pk.Charcoal.copy(alpha = .4f), fontFamily = BookSerif, fontSize = 18.sp)
                                field()
                            }
                        },
                    )
                    Text("SAVED TO YOUR PRIVATE JOURNAL", color = Pk.Charcoal.copy(alpha = .52f), fontFamily = UtilitySans, fontSize = 8.sp, letterSpacing = 1.sp, modifier = Modifier.padding(top = 10.dp))
                }

                Box(
                    Modifier.fillMaxWidth().padding(top = 36.dp).height(56.dp).clip(RoundedCornerShape(17.dp))
                        .background(if (completed) Pk.GoldWash else Pk.Oxblood)
                        .clickable {
                            completed = true
                            speaker.stop()
                            listening = false
                            onComplete()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (completed) "Read today" else "Mark as read",
                        color = if (completed) Pk.GoldDeep else Pk.Crisp,
                        fontFamily = UtilitySans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun ScriptureCard(passage: DailyPassage, verses: List<BibleVerse>) {
    Column(
        Modifier.fillMaxWidth().padding(top = 26.dp).clip(RoundedCornerShape(20.dp))
            .background(Pk.Sunken).padding(22.dp),
    ) {
        EditorialLabel("TODAY'S READING")
        if (verses.isEmpty()) {
            CircularProgressIndicator(color = Pk.Oxblood, strokeWidth = 2.dp, modifier = Modifier.padding(top = 18.dp).size(22.dp))
        } else {
            Text(
                verses.joinToString(" ") { it.text },
                color = Pk.Charcoal,
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                lineHeight = 30.sp,
                modifier = Modifier.padding(top = 13.dp),
            )
        }
        HorizontalDivider(color = Pk.Gold.copy(alpha = .25f), modifier = Modifier.padding(top = 15.dp, bottom = 13.dp))
        // the range actually on screen, now that the verses are in hand
        Text(passage.rangeOf(verses.map { it.verse }).uppercase(Locale.getDefault()), color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.1.sp)
    }
}

@Composable
private fun EditorialLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, color = Pk.GoldDeep, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, modifier = modifier)
}

@Composable
private fun QuietIcon(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(32.dp).clip(CircleShape).background(Pk.Charcoal.copy(alpha = .055f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
