package com.prayerkey.manna.ui.screens

import com.prayerkey.manna.resources.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.coroutines.delay

private data class MannaAction(val text: String, val x: Int, val y: Int)

private val actions = listOf(
    MannaAction("Read a devotional", -78, 18),
    MannaAction("Begin a fast", 76, 69),
    MannaAction("Open the Bible", -84, 121),
    MannaAction("Bring your prayers", 80, 174),
    MannaAction("Join your church", -78, 228),
    MannaAction("Keep a journal", 77, 280),
)

/**
 * The single closing beat after the promise card. A real photograph keeps the
 * welcome human; the product and words remain native Compose so they stay
 * sharp, localisable and accessible. The stagger is decorative and never
 * delays the Continue action.
 */
@Composable
fun OnboardingSlides(
    onSkip: () -> Unit,
    onDone: () -> Unit,
    step: Int,
    onStep: (Int) -> Unit,
) {
    var visibleCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        actions.indices.forEach {
            delay(if (it == 0) 180 else 145)
            visibleCount = it + 1
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color(0xFFF7F5FA)),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 14.dp, top = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "PRAYERKEY",
                    color = Pk.Oxblood,
                    fontFamily = BookSerif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Skip",
                    color = Color(0xFF6E6E73),
                    fontFamily = UtilitySans,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onSkip)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }

            Text(
                "Walk with God,\nevery day.",
                color = Color(0xFF1D1D1F),
                fontFamily = BookSerif,
                fontSize = 35.sp,
                lineHeight = 39.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 25.dp, top = 14.dp),
            )

            Box(
                Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp, vertical = 18.dp)
                    .shadow(22.dp, RoundedCornerShape(34.dp), spotColor = Pk.Oxblood.copy(alpha = .18f))
                    .clip(RoundedCornerShape(34.dp))
                    .border(1.dp, Color.White.copy(alpha = .85f), RoundedCornerShape(34.dp)),
            ) {
                Image(
                    painter = painterResource(Res.drawable.onboarding_manna_woman),
                    contentDescription = "A woman listening peacefully through headphones",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(Modifier.fillMaxSize().background(Pk.Oxblood.copy(alpha = .14f)))

                actions.forEachIndexed { index, action ->
                    Box(
                        Modifier.align(Alignment.TopCenter)
                            .offset(x = action.x.dp, y = action.y.dp),
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = index < visibleCount,
                            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                                slideInVertically(
                                    initialOffsetY = { -it * 5 },
                                    animationSpec = spring(
                                        dampingRatio = .72f,
                                        stiffness = Spring.StiffnessLow,
                                    ),
                                ),
                        ) {
                            Box(
                                Modifier.shadow(10.dp, CircleShape, spotColor = Color.Black.copy(alpha = .13f))
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = .88f))
                                    .border(1.dp, Color.White, CircleShape)
                                    .padding(horizontal = 13.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    action.text,
                                    color = Pk.Oxblood,
                                    fontFamily = UtilitySans,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }

            com.prayerkey.manna.ui.components.PkButton(
                label = "Continue",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 22.dp),
                onClick = onDone,
            )
        }
    }
}
