package com.prayerkey.manna.ui.others

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.BibleVerse
import com.prayerkey.manna.data.Devotional
import com.prayerkey.manna.data.Devotionals
import com.prayerkey.manna.data.OfflineBible
import com.prayerkey.manna.data.PrayingFor
import com.prayerkey.manna.data.Walk
import com.prayerkey.manna.platform.ioDispatcher
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.resources.Res
import com.prayerkey.manna.resources.discovery_cover_6
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource

private val Night = Color(0xFF12002A)

/**
 * The family altar: ten minutes together in the evening.
 *
 * Built from the day's devotional, so a household is reading what each of them
 * read that morning: the passage aloud, one question for the table, then
 * prayer for each other by name from the family marked in Praying For, and a
 * prayer said together to close.
 */
@Composable
fun FamilyAltarScreen(onClose: () -> Unit, onOpenList: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = platformContext()
    var day by remember { mutableStateOf<Devotional?>(null) }
    var passage by remember { mutableStateOf<List<BibleVerse>>(emptyList()) }
    LaunchedEffect(Unit) {
        PrayingFor.load(context)
        val d = Devotionals.forDate(context, today(), null)
        day = d
        if (d != null) {
            passage = withContext(ioDispatcher) {
                OfflineBible(context).chapter(d.reading.book, d.reading.chapter)
                    .filter { it.verse in d.reading.from..d.reading.to }
            }
        }
    }
    val people by PrayingFor.people.collectAsState()
    val family = people.filter { it.family && it.answered == 0L }
    var amen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Night)) {
        Image(painterResource(Res.drawable.discovery_cover_6), null, Modifier.fillMaxWidth().height(380.dp), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxWidth().height(380.dp).background(Brush.verticalGradient(listOf(Color(0x3312002A), Night))))
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        ) {
            Box(
                Modifier.padding(top = 12.dp).size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f)).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.height(170.dp))
            Text("TONIGHT", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp, color = Gold)
            Text("Family altar", Modifier.padding(top = 6.dp), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 38.sp, color = Color.White)
            Text(
                "Ten minutes together. Gather everyone, put the phones face down, and take it in turns.",
                Modifier.padding(top = 6.dp), fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 22.sp, color = Color.White.copy(alpha = .75f),
            )

            val d = day
            Step(1, "Read together", d?.reading?.rangeLabel?.let { "$it, King James Version" }) {
                if (passage.isEmpty()) {
                    Text("Opening the Bible...", fontFamily = UtilitySans, fontSize = 15.sp, color = Color.White.copy(alpha = .6f))
                } else {
                    Text(
                        buildAnnotatedString {
                            passage.forEach { v ->
                                withStyle(SpanStyle(color = Gold, fontSize = 12.sp, baselineShift = BaselineShift(.3f), fontWeight = FontWeight.Bold)) { append("${v.verse} ") }
                                append(v.text)
                                append(" ")
                            }
                        },
                        fontFamily = DisplaySerif, fontSize = 19.sp, lineHeight = 29.sp, color = Color.White,
                    )
                }
            }
            Step(2, "Talk about it", "One question for the table") {
                Text(
                    d?.question?.ifBlank { null } ?: "What is God showing us in this passage, and what will we do about it this week?",
                    fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 21.sp, lineHeight = 29.sp, color = Color.White,
                )
            }
            Step(3, "Pray for each other", "Each person prays for the one beside them") {
                if (family.isEmpty()) {
                    Text(
                        "Add your family to Praying For and mark them as family, and their names and needs will be here every evening.",
                        fontFamily = UtilitySans, fontSize = 15.sp, lineHeight = 22.sp, color = Color.White.copy(alpha = .75f),
                    )
                    Text(
                        "Add your family", Modifier.padding(top = 12.dp).clip(RoundedCornerShape(99.dp)).background(Color.White.copy(alpha = .12f))
                            .clickable(onClick = onOpenList).padding(horizontal = 18.dp, vertical = 10.dp),
                        fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Gold,
                    )
                } else {
                    family.forEach { p ->
                        Row(Modifier.padding(bottom = 12.dp)) {
                            Box(
                                Modifier.size(34.dp).clip(CircleShape).background(Violet),
                                contentAlignment = Alignment.Center,
                            ) { Text(p.name.take(1).uppercase(), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold, color = Color.White) }
                            Column(Modifier.padding(start = 12.dp)) {
                                Text(p.name, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, color = Color.White)
                                if (p.request.isNotBlank()) Text(p.request, fontFamily = UtilitySans, fontSize = 14.sp, color = Color.White.copy(alpha = .7f))
                            }
                        }
                    }
                }
            }
            Step(4, "Pray together", "Read it aloud as one") {
                Text(d?.prayer.orEmpty(), fontFamily = DisplaySerif, fontSize = 19.sp, lineHeight = 29.sp, color = Color.White)
            }

            Text(
                if (amen) "Amen. Go in peace." else "Amen",
                Modifier.padding(top = 26.dp).fillMaxWidth().clip(RoundedCornerShape(99.dp))
                    .background(if (amen) Color.White.copy(alpha = .12f) else Gold)
                    .clickable(enabled = !amen) {
                        amen = true
                        val today = today().toEpochDays().toLong()
                        family.forEach { PrayingFor.prayed(context, it.id, today) }
                        runCatching { Walk.mark(context) }
                    }.padding(vertical = 16.dp),
                fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = if (amen) Gold else DeepViolet, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Step(n: Int, title: String, kicker: String?, body: @Composable () -> Unit) {
    Column(Modifier.padding(top = 26.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = .06f)).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Gold), contentAlignment = Alignment.Center) {
                Text("$n", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepViolet)
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, color = Color.White)
                if (kicker != null) Text(kicker, fontFamily = UtilitySans, fontSize = 12.sp, color = Color.White.copy(alpha = .55f))
            }
        }
        Spacer(Modifier.height(14.dp))
        body()
    }
}
