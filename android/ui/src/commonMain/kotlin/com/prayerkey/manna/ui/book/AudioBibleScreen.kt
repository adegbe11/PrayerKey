package com.prayerkey.manna.ui.book

import com.prayerkey.manna.ui.platform.postToMain
import com.prayerkey.manna.ui.platform.StreamPlayer
import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.platform.pad2
import com.prayerkey.manna.ui.platform.UtteranceListener
import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.ui.platform.platformContext
import androidx.compose.ui.backhandler.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.PkText
import com.prayerkey.manna.ui.theme.Spectral
import com.prayerkey.manna.data.OfflineBible
import kotlinx.coroutines.withContext

private const val PUBLIC_DOMAIN_KJV_BASE = "https://publicdomainaudiobibles.com/content/mp3/KJV"

private val audioBookCodes = mapOf(
    "Genesis" to "GEN", "Exodus" to "EXO", "Leviticus" to "LEV", "Numbers" to "NUM",
    "Deuteronomy" to "DEU", "Joshua" to "JOS", "Judges" to "JDG", "Ruth" to "RUT",
    "1 Samuel" to "1SA", "2 Samuel" to "2SA", "1 Kings" to "1KI", "2 Kings" to "2KI",
    "1 Chronicles" to "1CH", "2 Chronicles" to "2CH", "Ezra" to "EZR", "Nehemiah" to "NEH",
    "Esther" to "EST", "Job" to "JOB", "Psalms" to "PSA", "Proverbs" to "PRO",
    "Ecclesiastes" to "ECC", "Song of Solomon" to "SNG", "Isaiah" to "ISA", "Jeremiah" to "JER",
    "Lamentations" to "LAM", "Ezekiel" to "EZK", "Daniel" to "DAN", "Hosea" to "HOS",
    "Joel" to "JOL", "Amos" to "AMO", "Obadiah" to "OBA", "Jonah" to "JON",
    "Micah" to "MIC", "Nahum" to "NAM", "Habakkuk" to "HAB", "Zephaniah" to "ZEP",
    "Haggai" to "HAG", "Zechariah" to "ZEC", "Malachi" to "MAL", "Matthew" to "MAT",
    "Mark" to "MRK", "Luke" to "LUK", "John" to "JHN", "Acts" to "ACT",
    "Romans" to "ROM", "1 Corinthians" to "1CO", "2 Corinthians" to "2CO", "Galatians" to "GAL",
    "Ephesians" to "EPH", "Philippians" to "PHP", "Colossians" to "COL",
    "1 Thessalonians" to "1TH", "2 Thessalonians" to "2TH", "1 Timothy" to "1TI",
    "2 Timothy" to "2TI", "Titus" to "TIT", "Philemon" to "PHM", "Hebrews" to "HEB",
    "James" to "JAS", "1 Peter" to "1PE", "2 Peter" to "2PE", "1 John" to "1JN",
    "2 John" to "2JN", "3 John" to "3JN", "Jude" to "JUD", "Revelation" to "REV",
)

private fun publicDomainChapterUrl(book: String, chapter: Int): String? =
    audioBookCodes[book]?.let { "$PUBLIC_DOMAIN_KJV_BASE/$it${chapter.toString().padStart(2, '0')}.mp3" }

@Composable
fun AudioBibleScreen(
    book: String,
    chapter: Int,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    var player by remember { mutableStateOf<StreamPlayer?>(null) }
    var narrator by remember { mutableStateOf<Speaker?>(null) }
    var narratorReady by remember { mutableStateOf(false) }
    var narration by remember { mutableStateOf<List<String>>(emptyList()) }
    var builtInNarration by remember { mutableStateOf(false) }
    var chapterUri by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var position by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(1) }
    var speed by remember { mutableFloatStateOf(1f) }
    var retry by remember { mutableIntStateOf(0) }

    DisposableEffect(context) {
        val engine = Speaker(context) { status ->
            narratorReady = status == Speaker.SUCCESS
        }
        engine.setOnUtteranceProgressListener(object : UtteranceListener() {
            override fun onStart(utteranceId: String?) { postToMain { playing = true } }
            override fun onDone(utteranceId: String?) {
                if (utteranceId == "chapter-end") postToMain { playing = false }
            }
            override fun onError(utteranceId: String?) { postToMain { playing = false } }
        })
        narrator = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            narrator = null
        }
    }

    LaunchedEffect(book, chapter, retry) {
        player?.release()
        player = null
        playing = false
        position = 0
        duration = 1
        error = null
        chapterUri = null
        narration = withContext(ioDispatcher) {
            OfflineBible(context).chapter(book, chapter).map { "Verse ${it.verse}. ${it.text}" }
        }
        loading = true
        chapterUri = publicDomainChapterUrl(book, chapter)
        builtInNarration = chapterUri == null
        loading = chapterUri != null
    }

    LaunchedEffect(narratorReady, builtInNarration, narration) {
        if (narratorReady && builtInNarration && narration.isNotEmpty()) {
            speakChapter(narrator, narration)
        }
    }

    DisposableEffect(chapterUri, retry) {
        val source = chapterUri
        if (source == null) return@DisposableEffect onDispose { }
        val media = StreamPlayer()
        media.setOnPreparedListener {
            duration = media.duration.coerceAtLeast(1)
            loading = false
            media.start()
            playing = true
        }
        media.setOnCompletionListener { playing = false; position = duration }
        media.setOnErrorListener {
            loading = false
            playing = false
            error = "Audio is unavailable for this chapter."
        }
        media.prepare(source)
        player = media
        onDispose {
            media.release()
            player = null
        }
    }

    LaunchedEffect(playing, player) {
        while (player != null) {
            position = runCatching { player?.currentPosition ?: position }.getOrDefault(position)
            kotlinx.coroutines.delay(500)
        }
    }

    Box(Modifier.fillMaxSize().background(Pk.Charcoal)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Audio", color = Pk.Cream.copy(alpha = .65f), style = PkText.SectionLabel, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose) {
                    Icon(Icons.Outlined.Close, "Close audio", tint = Pk.Cream)
                }
            }

            Spacer(Modifier.weight(.7f))
            Box(
                Modifier.size(112.dp).clip(CircleShape).background(Pk.Cream.copy(alpha = .09f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Headphones, null, tint = Pk.Gold, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(30.dp))
            Text(book, color = Pk.Cream, fontFamily = Spectral, fontSize = 34.sp, fontWeight = FontWeight.Medium)
            Text("CHAPTER $chapter · KJV", color = Pk.Cream.copy(alpha = .58f), style = PkText.Reference, modifier = Modifier.padding(top = 8.dp))
            Text(
                "Listen without leaving the page",
                color = Pk.Cream.copy(alpha = .7f),
                fontFamily = Spectral,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 18.dp),
            )

            Spacer(Modifier.weight(.55f))
            if (error != null) {
                Text(error!!, color = Pk.Blush, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(18.dp)).background(Pk.Oxblood)
                        .clickable { error = null; loading = true; retry++ }
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                ) {
                    Text("Try again", color = Pk.Cream, style = PkText.SectionLabel)
                }
            } else {
                if (builtInNarration) {
                    Text(
                        "Built-in chapter narration",
                        color = Pk.Cream.copy(alpha = .55f),
                        style = PkText.Meta,
                        modifier = Modifier.padding(vertical = 18.dp),
                    )
                } else {
                    Slider(
                        value = position.toFloat().coerceIn(0f, duration.toFloat()),
                        onValueChange = { position = it.toInt() },
                        onValueChangeFinished = { runCatching { player?.seekTo(position) } },
                        valueRange = 0f..duration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Pk.Cream,
                            activeTrackColor = Pk.Oxblood,
                            inactiveTrackColor = Pk.Cream.copy(alpha = .18f),
                        ),
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(time(position), color = Pk.Cream.copy(alpha = .55f), style = PkText.Meta)
                        Text(time(duration), color = Pk.Cream.copy(alpha = .55f), style = PkText.Meta)
                    }
                }

                Spacer(Modifier.height(26.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(Pk.Cream.copy(alpha = .1f))
                            .clickable {
                                speed = when (speed) { 1f -> 1.25f; 1.25f -> 1.5f; else -> 1f }
                                player?.setSpeed(speed)
                                narrator?.setSpeechRate(speed)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(
                            "${speed}×",
                            color = Pk.Cream,
                            style = PkText.Meta,
                            modifier = Modifier.clip(RoundedCornerShape(18.dp)).padding(2.dp),
                        )
                    }
                    Box(
                        Modifier.size(78.dp).clip(CircleShape).background(Pk.Cream),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (loading) CircularProgressIndicator(color = Pk.Oxblood, modifier = Modifier.size(30.dp))
                        else IconButton(
                            onClick = {
                                if (builtInNarration) {
                                    if (playing) {
                                        narrator?.stop()
                                        playing = false
                                    } else {
                                        speakChapter(narrator, narration)
                                    }
                                } else player?.let {
                                    if (it.isPlaying) it.pause() else it.start()
                                    playing = it.isPlaying
                                }
                            },
                            modifier = Modifier.size(78.dp),
                        ) {
                            Icon(
                                if (playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                if (playing) "Pause" else "Play",
                                tint = Pk.Oxblood,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                    }
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(Pk.Cream.copy(alpha = .1f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text("KJV", color = Pk.Cream, style = PkText.Meta)
                    }
                }
            }
            Spacer(Modifier.weight(.7f))
            Text(
                if (builtInNarration) "INSTANT CHAPTER NARRATION" else "PUBLIC-DOMAIN KJV · STREAMING",
                color = Pk.Cream.copy(alpha = .38f),
                style = PkText.Meta.copy(fontSize = 9.sp),
                modifier = Modifier.padding(bottom = 122.dp),
            )
        }
    }
}

private fun speakChapter(engine: Speaker?, verses: List<String>) {
    if (engine == null || verses.isEmpty()) return
    engine.stop()
    verses.forEachIndexed { index, verse ->
        engine.speak(
            verse,
            if (index == 0) Speaker.QUEUE_FLUSH else Speaker.QUEUE_ADD,
            null,
            if (index == verses.lastIndex) "chapter-end" else "verse-$index",
        )
    }
}

private fun time(milliseconds: Int): String {
    val seconds = milliseconds.coerceAtLeast(0) / 1000
    return "${seconds / 60}:${(seconds % 60).pad2()}"
}
