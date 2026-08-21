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
import androidx.compose.ui.graphics.SolidColor

/* ── The oil: gradients that give every surface depth and sheen ── */

/** Night surfaces — deep blue with a lifted top edge, like lacquer */
val NightGloss: Brush = SolidColor(Pk.Oxblood)

/** Ivory cards — warm light falling from the top */
val IvoryGloss: Brush = SolidColor(Color.White)

/** Electric CTAs — glossy blue with a bright crown */
val ElectricGloss: Brush = SolidColor(Pk.Oxblood)

/** Soft sheen overlay for the top of dark cards */
val TopSheen: Brush = SolidColor(Color.Transparent)

/** Soft sheen overlay for light cards */
val TopSheenLight: Brush = SolidColor(Color.Transparent)

/* ── Pressed-scale physics: everything gives slightly under the thumb ── */
@Composable
fun Modifier.pressScale(interaction: MutableInteractionSource = remember { MutableInteractionSource() }): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .965f else 1f, spring(dampingRatio = .6f), label = "press")
    return this.scale(scale)
}
