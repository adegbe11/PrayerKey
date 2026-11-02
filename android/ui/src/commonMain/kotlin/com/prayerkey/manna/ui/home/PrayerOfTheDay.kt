package com.prayerkey.manna.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PrayerOfTheDay
import com.prayerkey.manna.data.PrayersOfTheDay
import com.prayerkey.manna.resources.Res
import com.prayerkey.manna.resources.prayerkey_hero_dawn
import com.prayerkey.manna.ui.platform.Haptic
import com.prayerkey.manna.ui.platform.Speaker
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.platform.rememberHaptics
import com.prayerkey.manna.ui.share.Share
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource

private val Night = Color(0xFF1A0540)
private val GoldPale = Color(0xFFF1DDAE)

private val WEEKDAY = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
private val MONTH = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private fun LocalDate.spoken(): String = "${WEEKDAY[dayOfWeek.ordinal]}, $dayOfMonth ${MONTH[monthNumber - 1]}"

/** The line under the address: the first thing actually said to God. */
private fun PrayerOfTheDay.opening(): String =
    lines.drop(1).takeWhile { !it.startsWith("Amen") }.take(2).joinToString(" ")

/**
 * Home's first card. The dawn photograph it always had, carrying today's
 * prayer: its title, its first words, the verse it comes from, and the week.
 */
@Composable
fun PrayerOfTheDayCard(
    prayer: PrayerOfTheDay?,
    date: LocalDate,
    week: List<Pair<LocalDate, Boolean>>,
    scrolled: () -> Int,
    open: () -> Unit,
) {
    val prayedToday = week.lastOrNull()?.second == true
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(455.dp)
            .clip(RoundedCornerShape(34.dp)).clickable(enabled = prayer != null, onClick = open)
    ) {
        Image(
            painterResource(Res.drawable.prayerkey_hero_dawn), null,
            Modifier.fillMaxSize().graphicsLayer {
                val drift = (scrolled() * .34f).coerceAtMost(150f)
                translationY = drift
                val zoom = 1.06f + (drift / 3000f)
                scaleX = zoom
                scaleY = zoom
            },
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Pk.DeepDark.copy(alpha = .30f), Pk.DeepDark.copy(alpha = .96f)),
                    startY = 60f,
                )
            )
        )
        Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 22.dp, vertical = 22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = .16f), border = BorderStroke(.5.dp, Color.White.copy(alpha = .24f))) {
                    Text("Prayer of the day", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Day ${date.dayOfYear}", fontFamily = UtilitySans, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = .7f))
            }
            Text(
                prayer?.title ?: "Today's prayer",
                Modifier.padding(top = 12.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 35.sp, color = Color.White,
            )
            if (prayer != null) {
                Text(
                    prayer.opening(),
                    Modifier.padding(top = 9.dp),
                    fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 23.sp,
                    color = Color.White.copy(alpha = .84f), maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Text(prayer.ref, Modifier.padding(top = 10.dp), fontFamily = UtilitySans, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Pk.GoldLight)
            }
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                WeekDots(week, onDark = true)
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = if (prayedToday) Color.White.copy(alpha = .14f) else Pk.GoldLight,
                ) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (prayedToday) Icon(Icons.Outlined.Check, null, tint = Pk.GoldLight, modifier = Modifier.size(15.dp).padding(end = 2.dp))
                        Text(
                            if (prayedToday) "Prayed" else "Pray",
                            Modifier.padding(start = if (prayedToday) 4.dp else 0.dp),
                            fontFamily = UtilitySans, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            color = if (prayedToday) Pk.GoldLight else Pk.DeepDark,
                        )
                    }
                }
            }
        }
    }
}

/** Seven small marks, Monday-style initials under them; a prayed day is lit gold. */
@Composable
private fun WeekDots(week: List<Pair<LocalDate, Boolean>>, onDark: Boolean, lastPop: Float = 1f) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        week.forEachIndexed { i, (day, prayed) ->
            val isToday = i == week.lastIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(11.dp)
                        .graphicsLayer { if (isToday) { scaleX = lastPop; scaleY = lastPop } }
                        .clip(CircleShape)
                        .background(if (prayed) Pk.GoldLight else Color.White.copy(alpha = if (onDark) .16f else .10f))
                        .border(1.dp, if (isToday && !prayed) Pk.GoldLight.copy(alpha = .8f) else Color.Transparent, CircleShape)
                )
                Text(
                    WEEKDAY[day.dayOfWeek.ordinal].take(1),
                    Modifier.padding(top = 4.dp),
                    fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = if (isToday) .9f else .5f),
                )
            }
        }
    }
}

/**
 * The prayer itself, said line by line.
 *
 * The lines arrive one after another, the way the prayer is prayed, then the
 * verse it came from. Amen marks the day and lights it on the week.
 */
@Composable
fun PrayerOfTheDayScreen(
    prayer: PrayerOfTheDay,
    date: LocalDate,
    onClose: () -> Unit,
    onPrayed: () -> Unit,
) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()

    var prayed by remember(date) { mutableStateOf(PrayersOfTheDay.prayed(context, date)) }
    var week by remember(date, prayed) { mutableStateOf(PrayersOfTheDay.week(context, date)) }

    // each line rises in on its own beat
    val shown = remember(prayer) { prayer.lines.map { Animatable(0f) } }
    val verseIn = remember(prayer) { Animatable(0f) }
    LaunchedEffect(prayer) {
        delay(260)
        shown.forEach { a ->
            launch { a.animateTo(1f, tween(620, easing = FastOutSlowInEasing)) }
            delay(210)
        }
        delay(150)
        verseIn.animateTo(1f, tween(700))
    }

    // the light that opens behind the words on Amen
    val glow = remember { Animatable(if (prayed) .55f else 0f) }
    val pop = remember { Animatable(1f) }

    var speechReady by remember { mutableStateOf(false) }
    var speaking by remember { mutableStateOf(false) }
    val speaker = remember { Speaker(context) { speechReady = it == Speaker.SUCCESS } }
    DisposableEffect(speaker) { onDispose { speaker.stop(); speaker.shutdown() } }

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0xFF3A0C9E), Night), center = Offset(540f, 620f), radius = 1500f),
        ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (glow.value > 0f) {
                drawCircle(
                    Brush.radialGradient(
                        listOf(Pk.GoldLight.copy(alpha = .30f * glow.value), Color.Transparent),
                        center = Offset(size.width / 2, size.height * .42f),
                        radius = size.width * (.45f + .55f * glow.value),
                    ),
                    radius = size.width * (.45f + .55f * glow.value),
                    center = Offset(size.width / 2, size.height * .42f),
                )
            }
        }
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
                Text("Day ${date.dayOfYear}", color = GoldPale.copy(alpha = .8f), fontFamily = UtilitySans, fontSize = 13.sp)
            }

            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(18.dp))
                Text(
                    "PRAYER OF THE DAY", color = Pk.GoldLight, fontFamily = UtilitySans,
                    fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 2.2.sp,
                )
                Text(date.spoken(), Modifier.padding(top = 6.dp), color = GoldPale.copy(alpha = .6f), fontFamily = UtilitySans, fontSize = 13.sp)
                Text(
                    prayer.title, Modifier.padding(top = 14.dp),
                    color = Color.White, fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                    fontSize = 32.sp, lineHeight = 38.sp, textAlign = TextAlign.Center,
                )
                Row(Modifier.padding(top = 18.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(46.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, Pk.Gold))))
                    Text("✦", Modifier.padding(horizontal = 10.dp), color = Pk.Gold, fontSize = 13.sp)
                    Box(Modifier.width(46.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Pk.Gold, Color.Transparent))))
                }
                prayer.lines.forEachIndexed { i, line ->
                    val accent = i == 0 || line.startsWith("Amen") || line.endsWith("Amen.")
                    Text(
                        line,
                        Modifier.padding(vertical = 3.dp).graphicsLayer {
                            val t = shown[i].value
                            alpha = t
                            translationY = (1f - t) * 14.dp.toPx()
                        },
                        color = if (accent) Pk.GoldLight else Color.White.copy(alpha = .94f),
                        fontFamily = DisplaySerif,
                        fontStyle = if (accent) FontStyle.Italic else FontStyle.Normal,
                        fontSize = 19.sp, lineHeight = 28.sp, textAlign = TextAlign.Center,
                    )
                }
                Column(
                    Modifier.padding(top = 30.dp).fillMaxWidth().graphicsLayer { alpha = verseIn.value }
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = .06f))
                        .border(1.dp, Pk.Gold.copy(alpha = .28f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        prayer.verse, color = GoldPale.copy(alpha = .92f), fontFamily = DisplaySerif,
                        fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center,
                    )
                    Text(
                        "${prayer.ref} · KJV", Modifier.padding(top = 10.dp),
                        color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    )
                }
                if (prayed) {
                    Column(Modifier.padding(top = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        WeekDots(week, onDark = true, lastPop = pop.value)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 8.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PrayerAction(
                    if (speaking) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                    if (speaking) "Stop" else "Listen",
                    Modifier.weight(1f),
                ) {
                    if (speaking) {
                        speaker.stop(); speaking = false
                    } else if (speechReady) {
                        speaker.language = null
                        speaker.setSpeechRate(.85f)
                        speaker.speak("${prayer.title}. ${prayer.text} ${prayer.verse} ${prayer.ref}.", Speaker.QUEUE_FLUSH, null, "prayer-of-the-day")
                        speaking = true
                    }
                }
                PrayerAction(Icons.Outlined.Share, "Share", Modifier.weight(1f)) {
                    Share.cards.sharePrayerStatus(context, "${prayer.lines.joinToString("\n")}\n\n${prayer.ref}")
                }
                PrayerAction(Icons.Outlined.Check, if (prayed) "Prayed" else "Amen", Modifier.weight(1f), filled = !prayed) {
                    if (!prayed) {
                        haptics.perform(Haptic.Confirm)
                        PrayersOfTheDay.markPrayed(context, date)
                        prayed = true
                        week = PrayersOfTheDay.week(context, date)
                        onPrayed()
                        scope.launch { glow.animateTo(1f, tween(520)); glow.animateTo(.55f, tween(900)) }
                        scope.launch {
                            pop.snapTo(.2f)
                            pop.animateTo(1f, spring(dampingRatio = .38f, stiffness = 260f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerAction(icon: ImageVector, label: String, modifier: Modifier, filled: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(99.dp))
            .background(if (filled) Pk.Gold else Color.White.copy(alpha = .10f))
            .border(1.dp, if (filled) Pk.Gold else Pk.Gold.copy(alpha = .45f), RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (filled) Night else GoldPale, modifier = Modifier.size(18.dp))
        Text(
            label, Modifier.padding(start = 8.dp),
            color = if (filled) Night else GoldPale, fontFamily = UtilitySans,
            fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp,
        )
    }
}
