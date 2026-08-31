package com.prayerkey.manna.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.goldKey

/** PrayerKey's cinematic threshold. Short, legible and silent by design. */
@Composable
fun LaunchPrelude(stage: Int) {
    Box(Modifier.fillMaxSize().background(Pk.Oxblood), contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = stage, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "prayerkey-opening") { frame ->
            when (frame) {
                1 -> OpeningWord("Ask.")
                2 -> OpeningWord("Seek.")
                3 -> OpeningWord("Knock.")
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(54.dp)) { goldKey(center, size.height) }
                    Text("PRAYERKEY", Modifier.padding(top = 20.dp), color = Color.White, fontFamily = BookSerif, fontWeight = FontWeight.Bold, fontSize = 31.sp, letterSpacing = 5.sp)
                    Text("MATTHEW 7:7", Modifier.padding(top = 13.dp), color = Pk.GoldLight, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 2.4.sp)
                }
            }
        }
    }
}

@Composable private fun OpeningWord(word: String) {
    Text(word, color = Color.White, fontFamily = BookSerif, fontWeight = FontWeight.Bold, fontSize = 48.sp, textAlign = TextAlign.Center)
}
