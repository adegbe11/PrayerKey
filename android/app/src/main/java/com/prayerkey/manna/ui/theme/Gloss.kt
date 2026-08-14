package com.prayerkey.manna.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/* ── The oil: gradients that give every surface depth and sheen ── */

/** Night surfaces — deep blue with a lifted top edge, like lacquer */
val NightGloss = Brush.verticalGradient(
    listOf(Color(0xFF303030), Color(0xFF292929), Color(0xFF222222))
)

/** Ivory cards — warm light falling from the top */
val IvoryGloss = Brush.verticalGradient(
    listOf(Color(0xFFF9F6F0), Color(0xFFF5F0E8), Color(0xFFF1ECE3))
)

/** Electric CTAs — glossy blue with a bright crown */
val ElectricGloss = Brush.verticalGradient(
    listOf(Color(0xFF5B1520), Color(0xFF4A0E17), Color(0xFF3E0C13))
)

/** Soft sheen overlay for the top of dark cards */
val TopSheen = Brush.verticalGradient(
    0f to Color.White.copy(alpha = .09f),
    .35f to Color.Transparent,
    1f to Color.Transparent,
)

/** Soft sheen overlay for light cards */
val TopSheenLight = Brush.verticalGradient(
    0f to Color.White.copy(alpha = .55f),
    .22f to Color.Transparent,
    1f to Color.Transparent,
)

/* ── Pressed-scale physics: everything gives slightly under the thumb ── */
@Composable
fun Modifier.pressScale(interaction: MutableInteractionSource = remember { MutableInteractionSource() }): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .965f else 1f, spring(dampingRatio = .6f), label = "press")
    return this.scale(scale)
}
