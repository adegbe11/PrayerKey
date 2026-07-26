package com.prayerkey.manna.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalTime

/**
 * One design system for the whole app.
 *
 * The screens had drifted: 14/15/16/17/18/20/22/24/26dp radii across cards
 * that sit next to each other, which reads as pieces assembled from
 * different templates. Everything now draws from these tokens.
 */
object R {
    /** Big surfaces: cards, sheets, the verse deck. */
    val card = RoundedCornerShape(20.dp)
    /** Things you touch: buttons, fields, chips with square-ish ends. */
    val control = RoundedCornerShape(16.dp)
    /** Small inline tags. */
    val tag = RoundedCornerShape(12.dp)
    /** Fully round. */
    val pill = RoundedCornerShape(99.dp)
}

/** Vertical rhythm. One number, used everywhere, so the page breathes evenly. */
object Space {
    val block = 16.dp      // between structural blocks
    val tight = 8.dp
    val loose = 28.dp      // around a focal point
    val dock = 130.dp      // clearance so nothing hides behind the dock
}

/* Softer than pure black — editorial rather than harsh. Apple's own label
   colour is #1D1D1F; body copy sits a touch lighter still. */
val InkSoft = Color(0xFF2C2C2E)

/** Unselected chips: ambient tint, no wireframe border. */
val ChipFill = Color(0xFFF2F2F5)
val ChipFillSelected = Color(0xFFE6EBFF)

/**
 * The canvas is never flat white. It carries a barely-there wash that
 * shifts through the day — warm at dawn, cool at night — so the app feels
 * lit rather than printed. Kept under 4% alpha; it should register as
 * atmosphere, not colour.
 */
fun dayWash(hour: Int = LocalTime.now().hour): Brush = when (hour) {
    in 5..9 -> Brush.verticalGradient(       // dawn
        listOf(Color(0xFFFFF8EC), Color(0xFFFFFFFF), Color(0xFFFFFDF8)),
    )
    in 10..16 -> Brush.verticalGradient(     // daylight
        listOf(Color(0xFFFFFFFF), Color(0xFFFDFDFF), Color(0xFFF9FAFF)),
    )
    in 17..20 -> Brush.verticalGradient(     // evening
        listOf(Color(0xFFFFF6EE), Color(0xFFFFFFFF), Color(0xFFFBF7FF)),
    )
    else -> Brush.verticalGradient(          // night
        listOf(Color(0xFFF7F8FD), Color(0xFFFFFFFF), Color(0xFFF6F7FC)),
    )
}

/** A soft ambient shadow colour — never black, always the ink at low alpha. */
val SoftShadow = Night.copy(alpha = .22f)

/** Fade applied to the right edge of a horizontal carousel, so a cut-off
 *  item reads as "keep swiping" rather than "clipped by accident". */
fun edgeFade(background: Color = Color.White): Brush = Brush.horizontalGradient(
    0f to background.copy(alpha = 0f),
    1f to background,
)
