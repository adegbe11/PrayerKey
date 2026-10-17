package com.prayerkey.manna.ui.home

import com.prayerkey.manna.ui.share.Share
import com.prayerkey.manna.ui.platform.shareText
import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.ui.platform.platformContext
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.DevoBlock
import com.prayerkey.manna.data.Devotional
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.launch

/**
 * The day's devotional, as two cards.
 *
 * The front is the card you would keep on a desk: deep violet, a gold frame,
 * the day's title and the verse it rests on. Turn it and the back is the page
 * you read - the passage set as a quotation, the devotion, and a prayer that
 * ends in Amen. Nothing else is on either side. It turns like a card: tap the
 * front, or swipe either way, and it rotates about its own spine.
 */
private val Gold = Color(0xFFD9B26A)
private val GoldPale = Color(0xFFF1DDAE)
private val Parchment = Color(0xFFFBF6EA)
private val Night = Color(0xFF1A0540)

@Composable
fun DevotionalCards(
    day: Devotional,
    onClose: () -> Unit,
    onComplete: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val scope = rememberCoroutineScope()

    // the verse the day is built on, and the one quoted inside it
    var verse by remember(day) { mutableStateOf("") }
    val quoted = remember(day) { mutableStateOf(mapOf<String, String>()) }
    LaunchedEffect(day) {
        val bible = OfflineBible(context)
        verse = bible.chapter(day.reading.book, day.reading.chapter)
            .firstOrNull { it.verse == day.reading.verse }?.text.orEmpty()
        quoted.value = day.body.filterIsInstance<DevoBlock.Quote>().associate { q ->
            q.ref.label to bible.chapter(q.ref.book, q.ref.chapter)
                .firstOrNull { it.verse == q.ref.verse }?.text.orEmpty()
        }
    }

    // 0 = front, 180 = back
    val turn = remember { Animatable(0f) }
    fun flip(toBack: Boolean) = scope.launch {
        turn.animateTo(if (toBack) 180f else 0f, spring(dampingRatio = .82f, stiffness = 170f))
    }
    val back = turn.value > 90f

    var speechReady by remember { mutableStateOf(false) }
    var speaking by remember { mutableStateOf(false) }
    val speaker = remember { Speaker(context) { speechReady = it == Speaker.SUCCESS } }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }

    val reference = day.reading.ref.label.replace("Psalms ", "Psalm ")

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(
                listOf(Color(0xFF3A0C9E), Night),
                center = Offset(540f, 700f), radius = 1500f,
            ),
        ),
    ) {
        GoldDust()
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 8.dp, end = 18.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Close, "Close", tint = GoldPale, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.weight(1f))
                Text(
                    "Day ${day.seriesDay} of ${day.seriesOf}",
                    color = GoldPale.copy(alpha = .8f), fontFamily = UtilitySans, fontSize = 13.sp,
                )
            }

            Box(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp)
                    .pointerInput(Unit) {
                        var dragged = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragged = 0f },
                            onDragEnd = { if (kotlin.math.abs(dragged) > 60f) flip(turn.value < 90f) },
                        ) { _, delta -> dragged += delta }
                    }
                    .graphicsLayer {
                        rotationY = turn.value
                        cameraDistance = 14f * density
                    },
            ) {
                if (!back) {
                    FrontCard(day, verse, reference, Modifier.fillMaxSize().clickable { flip(true) })
                } else {
                    // the back is drawn mirrored so it reads the right way round once turned
                    BackCard(
                        day, verse, reference, quoted.value,
                        Modifier.fillMaxSize().graphicsLayer { rotationY = 180f },
                    )
                }
            }

            // what you can do with the page once it is showing
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!back) {
                    CardAction(Icons.Outlined.PlayArrow, "Turn the card", Modifier.weight(1f), filled = true) { flip(true) }
                } else {
                    CardAction(
                        if (speaking) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                        if (speaking) "Stop" else "Listen",
                        Modifier.weight(1f),
                    ) {
                        if (speaking) {
                            speaker.stop(); speaking = false
                        } else if (speechReady) {
                            speaker.language = null
                            speaker.setSpeechRate(.9f)
                            val text = buildString {
                                append(day.title).append(". ")
                                append(verse).append(" ").append(reference).append(". ")
                                day.body.forEach { b -> if (b is DevoBlock.Para) append(b.text).append(" ") }
                                append(day.prayer)
                            }
                            speaker.speak(text, Speaker.QUEUE_FLUSH, null, "devotional")
                            speaking = true
                        }
                    }
                    CardAction(Icons.Outlined.Share, "Share", Modifier.weight(1f)) {
                        val text = "${day.line.plain}\n\n$verse\n$reference (KJV)\n\n— PrayerKey"
                        shareText(context, text, "Share")
                    }
                    CardAction(Icons.Outlined.Check, "Done", Modifier.weight(1f), filled = true) {
                        speaker.stop()
                        onComplete()
                    }
                }
            }
        }
    }
}

/** Specks of gold light, the same every time, drifting behind the cards. */
@Composable
private fun GoldDust() {
    val specks = remember {
        val r = kotlin.random.Random(7)
        List(70) { Triple(r.nextFloat(), r.nextFloat(), r.nextFloat()) }
    }
    Canvas(Modifier.fillMaxSize()) {
        specks.forEach { (x, y, s) ->
            drawCircle(
                Gold.copy(alpha = .10f + s * .35f),
                radius = (1f + s * 2.4f) * density,
                center = Offset(x * size.width, (.35f + y * .65f) * size.height),
            )
        }
    }
}

/** The small gold flourish that crowns each card. */
@Composable
private fun Ornament(color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(46.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, color))))
        Text("✦", Modifier.padding(horizontal = 10.dp), color = color, fontSize = 13.sp)
        Box(Modifier.width(46.dp).height(1.dp).background(Brush.horizontalGradient(listOf(color, Color.Transparent))))
    }
}

// ── the front: the card you would keep ───────────────────────────────────

@Composable
private fun FrontCard(day: Devotional, verse: String, reference: String, modifier: Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier.clip(shape)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF4A12C9), Color(0xFF2A0878), Night)),
            )
            .border(1.5.dp, Gold.copy(alpha = .85f), shape),
    ) {
        // the light behind the words
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(Color.White.copy(alpha = .14f), Color.Transparent)),
            ),
        )
        // an inner gold rule, a card within the card
        Box(
            Modifier.fillMaxSize().padding(12.dp)
                .border(1.dp, Gold.copy(alpha = .45f), RoundedCornerShape(16.dp)),
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Ornament(Gold)
            Text(
                day.series,
                Modifier.padding(top = 18.dp),
                color = GoldPale.copy(alpha = .85f), fontFamily = UtilitySans, fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                day.title,
                Modifier.padding(top = 14.dp),
                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 34.sp, lineHeight = 40.sp, textAlign = TextAlign.Center,
            )
            Box(
                Modifier.padding(vertical = 22.dp).width(120.dp).height(1.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Gold, Color.Transparent))),
            )
            /* The Bible is read from disk the first time it is asked for, so
               on a cold open the verse can land a moment after the card.
               It fades in rather than appearing with a jump. */
            val shown by androidx.compose.animation.core.animateFloatAsState(
                if (verse.isBlank()) 0f else 1f,
                androidx.compose.animation.core.tween(500), label = "verse",
            )
            Text(
                verse,
                Modifier.graphicsLayer { alpha = shown },
                color = Color.White.copy(alpha = .92f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 18.sp, lineHeight = 28.sp, textAlign = TextAlign.Center,
            )
            Text(
                reference,
                Modifier.padding(top = 14.dp),
                color = Gold, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
            )
            Spacer(Modifier.height(26.dp))
            Ornament(Gold.copy(alpha = .7f))
        }
    }
}

// ── the back: the page you read ─────────────────────────────────────────

@Composable
private fun BackCard(
    day: Devotional,
    verse: String,
    reference: String,
    quoted: Map<String, String>,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier.clip(shape).background(Parchment).border(1.5.dp, Gold.copy(alpha = .8f), shape),
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Ornament(Pk.GoldDeep)
            Text(
                day.title,
                Modifier.padding(top = 14.dp),
                color = Pk.Oxblood, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                fontSize = 28.sp, lineHeight = 33.sp, textAlign = TextAlign.Center,
            )
            Text(
                verse,
                Modifier.padding(top = 12.dp),
                color = Pk.Charcoal.copy(alpha = .78f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center,
            )
            Text(
                reference.uppercase() + " KJV",
                Modifier.padding(top = 6.dp),
                color = Pk.GoldDeep, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp, letterSpacing = 1.2.sp,
            )
            Box(Modifier.padding(vertical = 18.dp).fillMaxWidth().height(1.dp).background(Pk.GoldDeep.copy(alpha = .22f)))

            day.body.forEach { block ->
                when (block) {
                    is DevoBlock.Para -> Text(
                        block.text,
                        Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        color = Pk.Charcoal, fontFamily = BookSerif,
                        fontSize = 16.5.sp, lineHeight = 26.sp,
                    )
                    is DevoBlock.Quote -> {
                        val text = quoted[block.ref.label].orEmpty()
                        if (text.isNotBlank() && block.ref != day.reading.ref) {
                            Column(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp).padding(bottom = 10.dp).padding(start = 14.dp),
                            ) {
                                Text(
                                    text,
                                    color = Pk.Oxblood, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                                    fontSize = 16.sp, lineHeight = 25.sp,
                                )
                                Text(
                                    block.ref.label.replace("Psalms ", "Psalm "),
                                    Modifier.padding(top = 4.dp),
                                    color = Pk.GoldDeep, fontFamily = UtilitySans, fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }

            // the prayer, set apart and signed off
            Text(
                day.prayer.removeSuffix("Amen.").removeSuffix("Amen").trim(),
                Modifier.fillMaxWidth().padding(top = 10.dp),
                color = Pk.Charcoal, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                fontSize = 16.5.sp, lineHeight = 26.sp, textAlign = TextAlign.Center,
            )
            Text(
                "Amen",
                Modifier.padding(top = 6.dp),
                color = Pk.Oxblood, fontFamily = DisplaySerif, fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold, fontSize = 24.sp,
            )

            // the line worth keeping, on the violet band the front was made of
            Column(
                Modifier.fillMaxWidth().padding(top = 22.dp).clip(RoundedCornerShape(16.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF3A0C9E), Night)))
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    day.line.first,
                    color = Color.White, fontFamily = BookSerif, fontSize = 15.5.sp, lineHeight = 23.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    day.line.second,
                    Modifier.padding(top = 4.dp),
                    color = Gold, fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                    fontSize = 15.5.sp, lineHeight = 23.sp, textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CardAction(icon: ImageVector, label: String, modifier: Modifier, filled: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(99.dp))
            .background(if (filled) Gold else Color.White.copy(alpha = .10f))
            .border(1.dp, if (filled) Gold else Gold.copy(alpha = .45f), RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (filled) Night else GoldPale, modifier = Modifier.size(18.dp))
        Text(
            label,
            Modifier.padding(start = 8.dp),
            color = if (filled) Night else GoldPale, fontFamily = UtilitySans,
            fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp,
        )
    }
}


/**
 * Today's chapter, as two cards.
 *
 * One chapter a day: short enough to finish in a few minutes, which is what
 * makes it a habit rather than homework. The front names the chapter and holds
 * its key verse - the one the day's devotional is built on - with how long it
 * will take. Turn it and the back is the chapter entire, with the key verse
 * picked out where it falls. Done marks the day's reading kept.
 */
@Composable
fun ChapterCards(
    reading: com.prayerkey.manna.data.DevoReading,
    onClose: () -> Unit,
    onDone: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val scope = rememberCoroutineScope()
    var verses by remember(reading) { mutableStateOf<List<com.prayerkey.manna.data.BibleVerse>>(emptyList()) }
    LaunchedEffect(reading) {
        verses = OfflineBible(context).chapter(reading.book, reading.chapter)
    }
    val key = verses.firstOrNull { it.verse == reading.verse }
    val words = verses.sumOf { it.text.split(' ').size }
    val minutes = ((words + 199) / 200).coerceAtLeast(1)
    val name = (if (reading.book == "Psalms") "Psalm" else reading.book) + " " + reading.chapter

    val turn = remember { Animatable(0f) }
    fun flip(toBack: Boolean) = scope.launch {
        turn.animateTo(if (toBack) 180f else 0f, spring(dampingRatio = .82f, stiffness = 170f))
    }
    val back = turn.value > 90f

    var speechReady by remember { mutableStateOf(false) }
    var speaking by remember { mutableStateOf(false) }
    val speaker = remember { Speaker(context) { speechReady = it == Speaker.SUCCESS } }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0xFF3A0C9E), Night), center = Offset(540f, 700f), radius = 1500f),
        ),
    ) {
        GoldDust()
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 8.dp, end = 18.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Close, "Close", tint = GoldPale, modifier = Modifier.size(20.dp)) }
            }

            Box(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp)
                    .pointerInput(Unit) {
                        var dragged = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragged = 0f },
                            onDragEnd = { if (kotlin.math.abs(dragged) > 60f) flip(turn.value < 90f) },
                        ) { _, delta -> dragged += delta }
                    }
                    .graphicsLayer { rotationY = turn.value; cameraDistance = 14f * density },
            ) {
                if (!back) {
                    val shape = RoundedCornerShape(22.dp)
                    Box(
                        Modifier.fillMaxSize().clip(shape)
                            .background(Brush.verticalGradient(listOf(Color(0xFF4A12C9), Color(0xFF2A0878), Night)))
                            .border(1.5.dp, Gold.copy(alpha = .85f), shape)
                            .clickable { flip(true) },
                    ) {
                        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color.White.copy(alpha = .14f), Color.Transparent))))
                        Box(Modifier.fillMaxSize().padding(12.dp).border(1.dp, Gold.copy(alpha = .45f), RoundedCornerShape(16.dp)))
                        Column(
                            Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 38.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Ornament(Gold)
                            Text(
                                "Today’s chapter",
                                Modifier.padding(top = 18.dp),
                                color = GoldPale.copy(alpha = .85f), fontFamily = UtilitySans, fontSize = 13.sp,
                            )
                            Text(
                                name,
                                Modifier.padding(top = 12.dp),
                                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                                fontSize = 46.sp, lineHeight = 52.sp, textAlign = TextAlign.Center,
                            )
                            Box(
                                Modifier.padding(vertical = 22.dp).width(120.dp).height(1.dp)
                                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Gold, Color.Transparent))),
                            )
                            val shown by androidx.compose.animation.core.animateFloatAsState(
                                if (key == null) 0f else 1f, androidx.compose.animation.core.tween(500), label = "key",
                            )
                            Text(
                                key?.text.orEmpty(),
                                Modifier.graphicsLayer { alpha = shown },
                                color = Color.White.copy(alpha = .92f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                                fontSize = 18.sp, lineHeight = 28.sp, textAlign = TextAlign.Center,
                            )
                            if (key != null) {
                                Text(
                                    "$name:${reading.verse}",
                                    Modifier.padding(top = 14.dp),
                                    color = Gold, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                )
                            }
                            Spacer(Modifier.height(26.dp))
                            if (verses.isNotEmpty()) {
                                Text(
                                    "${verses.size} verses · $minutes min",
                                    color = GoldPale.copy(alpha = .7f), fontFamily = UtilitySans, fontSize = 12.5.sp,
                                )
                                Spacer(Modifier.height(14.dp))
                            }
                            Ornament(Gold.copy(alpha = .7f))
                        }
                    }
                } else {
                    val shape = RoundedCornerShape(22.dp)
                    Box(
                        Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }
                            .clip(shape).background(Parchment).border(1.5.dp, Gold.copy(alpha = .8f), shape),
                    ) {
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                .padding(horizontal = 26.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Ornament(Pk.GoldDeep)
                            Text(
                                name,
                                Modifier.padding(top = 14.dp),
                                color = Pk.Oxblood, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, fontSize = 32.sp,
                            )
                            Text(
                                "KJV",
                                Modifier.padding(top = 2.dp, bottom = 18.dp),
                                color = Pk.GoldDeep, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp, letterSpacing = 1.2.sp,
                            )
                            verses.forEach { v ->
                                val isKey = v.verse == reading.verse
                                Text(
                                    androidx.compose.ui.text.buildAnnotatedString {
                                        withStyle(
                                            androidx.compose.ui.text.SpanStyle(
                                                color = Pk.GoldDeep, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                                baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript,
                                            ),
                                        ) { append("${v.verse} ") }
                                        append(v.text)
                                    },
                                    Modifier.fillMaxWidth().padding(bottom = 9.dp)
                                        .then(
                                            // the verse the day turns on, picked out where it falls
                                            if (isKey) {
                                                Modifier.clip(RoundedCornerShape(10.dp))
                                                    .background(Pk.Oxblood.copy(alpha = .07f))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            } else {
                                                Modifier
                                            },
                                        ),
                                    color = if (isKey) Pk.Oxblood else Pk.Charcoal,
                                    fontFamily = BookSerif,
                                    fontStyle = if (isKey) FontStyle.Italic else FontStyle.Normal,
                                    fontSize = 17.sp, lineHeight = 27.sp,
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Ornament(Pk.GoldDeep)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!back) {
                    CardAction(Icons.Outlined.PlayArrow, "Read the chapter", Modifier.weight(1f), filled = true) { flip(true) }
                } else {
                    CardAction(
                        if (speaking) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                        if (speaking) "Stop" else "Listen",
                        Modifier.weight(1f),
                    ) {
                        if (speaking) {
                            speaker.stop(); speaking = false
                        } else if (speechReady && verses.isNotEmpty()) {
                            speaker.language = null
                            speaker.setSpeechRate(.9f)
                            speaker.speak("$name. " + verses.joinToString(" ") { it.text }, Speaker.QUEUE_FLUSH, null, "chapter")
                            speaking = true
                        }
                    }
                    CardAction(Icons.Outlined.Share, "Share", Modifier.weight(1f)) {
                        val text = (key?.text ?: verses.firstOrNull()?.text.orEmpty()) +
                            "\n$name:${key?.verse ?: 1} (KJV)\n\n— PrayerKey"
                        shareText(context, text, "Share")
                    }
                    CardAction(Icons.Outlined.Check, "Done", Modifier.weight(1f), filled = true) {
                        speaker.stop()
                        onDone()
                    }
                }
            }
        }
    }
}


/**
 * A prayer for the hour, as two cards - morning, afternoon, evening, night
 * or midnight.
 *
 * The kind of card people keep and send: the front is the day's title and the
 * verse it stands on, the back is one short prayer, whole, signed off with
 * Amen. Nothing to work through and nothing to set up - a minute with God
 * before the day begins. Share sends the prayer as an image, the way these
 * cards travel.
 */
@Composable
fun MorningPrayerCards(
    prayer: com.prayerkey.manna.model.DailyPrayer,
    /** "Morning prayer", "Evening prayer"... */
    label: String = "Morning prayer",
    /** The hour's mark at the top of the front. */
    mark: String = "\u2600",
    onClose: () -> Unit,
    onDone: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val scope = rememberCoroutineScope()
    val words = prayer.prayer.trim().removeSuffix("Amen.").removeSuffix("Amen").trim()
    val reference = prayer.ref.replace("Psalms ", "Psalm ")

    val turn = remember { Animatable(0f) }
    fun flip(toBack: Boolean) = scope.launch {
        turn.animateTo(if (toBack) 180f else 0f, spring(dampingRatio = .82f, stiffness = 170f))
    }
    val back = turn.value > 90f

    var speechReady by remember { mutableStateOf(false) }
    var speaking by remember { mutableStateOf(false) }
    val speaker = remember { Speaker(context) { speechReady = it == Speaker.SUCCESS } }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0xFF3A0C9E), Night), center = Offset(540f, 700f), radius = 1500f),
        ),
    ) {
        GoldDust()
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 8.dp, end = 18.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Close, "Close", tint = GoldPale, modifier = Modifier.size(20.dp)) }
            }

            Box(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp)
                    .pointerInput(Unit) {
                        var dragged = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragged = 0f },
                            onDragEnd = { if (kotlin.math.abs(dragged) > 60f) flip(turn.value < 90f) },
                        ) { _, delta -> dragged += delta }
                    }
                    .graphicsLayer { rotationY = turn.value; cameraDistance = 14f * density },
            ) {
                if (!back) {
                    val shape = RoundedCornerShape(22.dp)
                    Box(
                        Modifier.fillMaxSize().clip(shape)
                            .background(Brush.verticalGradient(listOf(Color(0xFF4A12C9), Color(0xFF2A0878), Night)))
                            .border(1.5.dp, Gold.copy(alpha = .85f), shape)
                            .clickable { flip(true) },
                    ) {
                        // the first light of the day, from the top of the card
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.radialGradient(listOf(Gold.copy(alpha = .22f), Color.Transparent)),
                            ),
                        )
                        Box(Modifier.fillMaxSize().padding(12.dp).border(1.dp, Gold.copy(alpha = .45f), RoundedCornerShape(16.dp)))
                        Column(
                            Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 38.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(mark, color = Gold, fontSize = 30.sp)
                            Text(
                                label,
                                Modifier.padding(top = 12.dp),
                                color = GoldPale.copy(alpha = .85f), fontFamily = UtilitySans, fontSize = 13.sp,
                            )
                            Text(
                                prayer.title,
                                Modifier.padding(top = 12.dp),
                                color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                                fontSize = 36.sp, lineHeight = 42.sp, textAlign = TextAlign.Center,
                            )
                            Box(
                                Modifier.padding(vertical = 22.dp).width(120.dp).height(1.dp)
                                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Gold, Color.Transparent))),
                            )
                            Text(
                                prayer.verse,
                                color = Color.White.copy(alpha = .92f), fontFamily = BookSerif, fontStyle = FontStyle.Italic,
                                fontSize = 18.sp, lineHeight = 28.sp, textAlign = TextAlign.Center,
                            )
                            Text(
                                reference,
                                Modifier.padding(top = 14.dp),
                                color = Gold, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                            )
                            Spacer(Modifier.height(26.dp))
                            Ornament(Gold.copy(alpha = .7f))
                        }
                    }
                } else {
                    val shape = RoundedCornerShape(22.dp)
                    Box(
                        Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }
                            .clip(shape).background(Parchment).border(1.5.dp, Gold.copy(alpha = .8f), shape),
                    ) {
                        Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                .padding(horizontal = 30.dp, vertical = 34.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Ornament(Pk.GoldDeep)
                            Text(
                                prayer.title,
                                Modifier.padding(top = 16.dp),
                                color = Pk.Oxblood, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                                fontSize = 26.sp, lineHeight = 32.sp, textAlign = TextAlign.Center,
                            )
                            /* A one-line prayer set at the size of a long one left
                               most of the card empty. The fewer the words, the
                               larger they are set, so every card reads as full. */
                            val count = words.split(' ').size
                            val size = when {
                                count <= 16 -> 27
                                count <= 26 -> 24
                                count <= 40 -> 21
                                else -> 19
                            }
                            Text(
                                words,
                                Modifier.padding(top = 20.dp),
                                color = Pk.Charcoal, fontFamily = BookSerif,
                                fontSize = size.sp, lineHeight = (size * 1.55f).sp, textAlign = TextAlign.Center,
                            )
                            Text(
                                "Amen",
                                Modifier.padding(top = 14.dp),
                                color = Pk.Oxblood, fontFamily = DisplaySerif, fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold, fontSize = 30.sp,
                            )
                            Text(
                                reference,
                                Modifier.padding(top = 18.dp),
                                color = Pk.GoldDeep, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp,
                            )
                            Spacer(Modifier.height(18.dp))
                            Ornament(Pk.GoldDeep)
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!back) {
                    CardAction(Icons.Outlined.PlayArrow, "Pray", Modifier.weight(1f), filled = true) { flip(true) }
                } else {
                    CardAction(
                        if (speaking) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                        if (speaking) "Stop" else "Listen",
                        Modifier.weight(1f),
                    ) {
                        if (speaking) {
                            speaker.stop(); speaking = false
                        } else if (speechReady) {
                            speaker.language = null
                            speaker.setSpeechRate(.86f)
                            speaker.speak("$words Amen.", Speaker.QUEUE_FLUSH, null, "morning")
                            speaking = true
                        }
                    }
                    CardAction(Icons.Outlined.Share, "Share", Modifier.weight(1f)) {
                        Share.cards.sharePrayerStatus(context, "$words Amen.")
                    }
                    CardAction(Icons.Outlined.Check, "Amen", Modifier.weight(1f), filled = true) {
                        speaker.stop()
                        onDone()
                    }
                }
            }
        }
    }
}
