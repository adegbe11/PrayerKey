package com.prayerkey.manna.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.BookSerif

private val scriptureLines = listOf(
    listOf("Ask,", "and", "it", "will"),
    listOf("be", "given", "to", "you;"),
    listOf("seek,", "and", "you", "will", "find;"),
    listOf("knock,", "and", "it", "will"),
    listOf("be", "opened", "to", "you."),
)

/**
 * A cinematic threshold before onboarding. [stage] zero holds the wordmark;
 * every subsequent value reveals one more word while previous words remain.
 */
@Composable
fun LaunchPrelude(stage: Int) {
    val cs = MaterialTheme.colorScheme
    AnimatedContent(
        targetState = stage == 0,
        transitionSpec = { fadeIn(tween(520)) togetherWith fadeOut(tween(360)) },
        label = "opening-scene",
    ) { showingBrand ->
        Column(
            Modifier.fillMaxSize().background(cs.background).padding(horizontal = 26.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showingBrand) {
                Text(
                    "PRAYERKEY",
                    color = cs.onBackground,
                    fontFamily = BookSerif,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 7.sp,
                )
            } else {
                var wordIndex = 0
                scriptureLines.forEach { line ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        line.forEach { word ->
                            val index = wordIndex++
                            AnimatedVisibility(
                                visible = stage > index,
                                enter = fadeIn(tween(360)) + slideInVertically(tween(460)) { it / 2 },
                            ) {
                                Text(
                                    word,
                                    color = cs.onBackground,
                                    fontFamily = BookSerif,
                                    fontSize = 28.sp,
                                    lineHeight = 38.sp,
                                    fontWeight = if (index == 0 || index == 8 || index == 13) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = stage > 21,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(520)) { it / 3 },
                ) {
                    Text(
                        "MATTHEW 7:7",
                        color = cs.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.6.sp,
                        modifier = Modifier.padding(top = 28.dp),
                    )
                }
            }
        }
    }
}
