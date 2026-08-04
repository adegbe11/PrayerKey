package com.prayerkey.manna.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.APP_THEMES
import com.prayerkey.manna.data.AppTheme
import kotlin.math.absoluteValue

/**
 * Twelve themes, chosen at the end of onboarding.
 *
 * The layout follows the horizontal-carousel pattern: the focused card sits
 * centre with its neighbours peeking either side, and the whole background
 * washes to the focused theme's palette so you are standing inside the
 * choice rather than looking at a swatch.
 *
 * Each card is a real miniature of the app — header art bleeding into entry
 * cards — because a flat colour chip tells you nothing about how the app
 * will actually feel to read.
 */
@Composable
fun ThemePicker(
    initialId: String,
    onLater: () -> Unit,
    onUse: (AppTheme) -> Unit,
) {
    val start = remember(initialId) { APP_THEMES.indexOfFirst { it.id == initialId }.coerceAtLeast(0) }
    val pager = rememberPagerState(initialPage = start) { APP_THEMES.size }
    val focused = APP_THEMES[pager.currentPage]

    /* The aura shift: the page morphs to the focused theme, so the choice is
       felt at full size before it is committed. */
    val bg by animateColorAsState(focused.background, tween(300), label = "aura-bg")
    val wash by animateColorAsState(focused.header.first(), tween(300), label = "aura-wash")
    val ink by animateColorAsState(focused.ink, tween(300), label = "aura-ink")
    val muted by animateColorAsState(focused.muted, tween(300), label = "aura-muted")
    val accent by animateColorAsState(focused.accent, tween(300), label = "aura-accent")

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(wash.copy(alpha = .55f), bg, bg))),
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(52.dp))

            Text(
                "Choose your theme",
                color = ink, fontFamily = FontFamily.Serif,
                fontSize = 27.sp, letterSpacing = (-0.4).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalPager(
                state = pager,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 68.dp),
                pageSpacing = 16.dp,
            ) { page ->
                val offset = ((pager.currentPage - page) + pager.currentPageOffsetFraction).absoluteValue
                val scale by animateFloatAsState(
                    if (page == pager.currentPage) 1f else .92f,
                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                    label = "card-scale",
                )
                ThemeCard(
                    theme = APP_THEMES[page],
                    modifier = Modifier
                        .fillMaxHeight(if (page == pager.currentPage) .96f else .88f)
                        .scale(scale)
                        .padding(vertical = 18.dp)
                        .graphicsLayer { alpha = 1f - (offset * .35f).coerceIn(0f, .45f) },
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 22.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(18.dp))
                        .border(1.dp, muted.copy(alpha = .35f), RoundedCornerShape(18.dp))
                        .clickable(onClick = onLater),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Later", color = muted, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Box(
                    Modifier.weight(1.4f).height(54.dp)
                        .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = accent.copy(alpha = .5f))
                        .clip(RoundedCornerShape(18.dp)).background(accent)
                        .clickable { onUse(focused) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Use this theme",
                        color = if (focused.dark) Color(0xFF10131F) else Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

/**
 * A miniature of the real app: cover art at the top bleeding into a stack
 * of entry cards. The last stop of the header gradient equals the theme
 * background, which is what makes the blend seamless rather than a seam.
 */
@Composable
private fun ThemeCard(theme: AppTheme, modifier: Modifier = Modifier) {
    Box(
        modifier
            .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = .45f))
            .clip(RoundedCornerShape(24.dp))
            .background(theme.background)
            .border(1.dp, theme.muted.copy(alpha = .18f), RoundedCornerShape(24.dp)),
    ) {
        Column(Modifier.fillMaxSize()) {
            // cover art — an aura wash, not a cartoon
            Box(
                Modifier.fillMaxWidth().fillMaxHeight(.34f)
                    .background(Brush.verticalGradient(theme.header)),
            ) {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.radialGradient(
                            listOf(theme.accent.copy(alpha = .22f), Color.Transparent),
                            radius = 420f,
                        ),
                    ),
                )
                Text(
                    "⚿",
                    color = theme.accent,
                    fontSize = 22.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Column(
                Modifier.fillMaxSize().padding(horizontal = 12.dp).padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                repeat(3) { i ->
                    Column(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(theme.surface)
                            .padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            Modifier.fillMaxWidth(.34f).height(7.dp)
                                .clip(CircleShape).background(theme.accent.copy(alpha = .75f)),
                        )
                        repeat(if (i == 1) 3 else 2) { line ->
                            Box(
                                Modifier.fillMaxWidth(if (line == 1) .72f else .92f).height(5.dp)
                                    .clip(CircleShape).background(theme.ink.copy(alpha = .18f)),
                            )
                        }
                    }
                }
            }
        }
    }
}
