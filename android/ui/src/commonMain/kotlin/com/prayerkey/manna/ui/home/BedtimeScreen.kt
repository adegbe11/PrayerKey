package com.prayerkey.manna.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.Ambience
import com.prayerkey.manna.data.hourPrayer
import com.prayerkey.manna.resources.Res
import com.prayerkey.manna.resources.rhythm_evening
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans
import org.jetbrains.compose.resources.painterResource

private val Gold = Color(0xFFFFD68C)

/** A chapter to fall asleep to, and the line that makes it a night reading. */
private data class SleepReading(val book: String, val chapter: Int, val title: String, val line: String)

/* KJV, word for word: each line is a phrase from its own chapter. */
private val SleepReadings = listOf(
    SleepReading("Psalms", 4, "Psalm 4", "I will both lay me down in peace, and sleep"),
    SleepReading("Psalms", 23, "Psalm 23", "He maketh me to lie down in green pastures"),
    SleepReading("Psalms", 91, "Psalm 91", "Thou shalt not be afraid for the terror by night"),
    SleepReading("Psalms", 121, "Psalm 121", "My help cometh from the LORD"),
    SleepReading("Matthew", 6, "Matthew 6", "Take therefore no thought for the morrow"),
    SleepReading("John", 14, "John 14", "Let not your heart be troubled"),
)

/**
 * Bedtime: the night prayer, then Scripture read aloud to fall asleep to.
 *
 * It begins with the day's night prayer, short enough to pray lying down, and
 * then offers six chapters that were written for the dark. Each plays with
 * rain under the voice and a half-hour timer already set, so nothing has to
 * be chosen with eyes half closed.
 */
@Composable
fun BedtimeScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    var listening by remember { mutableStateOf<SleepReading?>(null) }
    listening?.let { r ->
        com.prayerkey.manna.ui.book.AudioBibleScreen(
            startBook = r.book, startChapter = r.chapter,
            onClose = { listening = null },
            sleepMinutes = 30, ambience = Ambience.RAIN,
            subtitle = "Sleep with Scripture",
        )
        return
    }
    val prayer = remember { hourPrayer("night") }

    Box(Modifier.fillMaxSize().background(Color(0xFF0B0118))) {
        Image(
            painterResource(Res.drawable.rhythm_evening), null,
            Modifier.fillMaxWidth().height(420.dp), contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxWidth().height(420.dp)
                .background(Brush.verticalGradient(listOf(Color(0x5512002A), Color(0xFF0B0118)))),
        )
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
            Box(
                Modifier.padding(top = 12.dp).size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Color.White, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.height(150.dp))
            Text("BEDTIME", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp, color = Gold)
            Text(
                prayer.title, Modifier.padding(top = 8.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp, color = Color.White,
            )
            Text(
                prayer.prayer, Modifier.padding(top = 16.dp),
                fontFamily = DisplaySerif, fontSize = 20.sp, lineHeight = 31.sp, color = Color.White.copy(alpha = .9f),
            )

            Text(
                "Fall asleep to Scripture", Modifier.padding(top = 40.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Color.White,
            )
            Text(
                "Read aloud over soft rain. It stops by itself after thirty minutes.",
                Modifier.padding(top = 4.dp, bottom = 16.dp),
                fontFamily = UtilitySans, fontSize = 14.sp, lineHeight = 20.sp, color = Color.White.copy(alpha = .65f),
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SleepReadings.forEach { r ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .07f))
                            .clickable { listening = r }.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(r.title, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = Gold)
                            Text(
                                r.line, Modifier.padding(top = 4.dp),
                                fontFamily = DisplaySerif, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 24.sp, color = Color.White,
                            )
                        }
                        Box(
                            Modifier.padding(start = 12.dp).size(42.dp).clip(CircleShape).background(Gold),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Outlined.Headphones, "Listen", tint = Color(0xFF2E0078), modifier = Modifier.size(20.dp)) }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
