package com.prayerkey.manna.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * One design system for the whole app.
 *
 * Named Corners, not R: Android drops any class called R from a library,
 * taking it for generated resources, and this one vanished at runtime.
 *
 * The screens had drifted: 14/15/16/17/18/20/22/24/26dp radii across cards
 * that sit next to each other, which reads as pieces assembled from
 * different templates. Everything now draws from these tokens.
 */
object Corners {
    /** Big surfaces: cards, sheets, the verse deck. */
    val card = RoundedCornerShape(Pk.CardRadius)
    /** Things you touch: buttons, fields, chips with square-ish ends. */
    val control = RoundedCornerShape(Pk.ButtonRadius)
    /** Small inline tags. */
    val tag = RoundedCornerShape(15.dp)
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
val InkSoft = Color(0xFF222222)

/** Unselected chips: ambient tint, no wireframe border. */
val ChipFill = Pk.Charcoal.copy(alpha = .055f)
val ChipFillSelected = Pk.Oxblood.copy(alpha = .10f)

/**
 * The canvas is never flat white. It carries a barely-there wash that
 * shifts through the day — warm at dawn, cool at night — so the app feels
 * lit rather than printed. Kept under 4% alpha; it should register as
 * atmosphere, not colour.
 */
fun dayWash(hour: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour): Brush = SolidColor(Pk.Cream)

/** A soft ambient shadow colour — never black, always the ink at low alpha. */
val SoftShadow = Night.copy(alpha = .22f)

/** Fade applied to the right edge of a horizontal carousel, so a cut-off
 *  item reads as "keep swiping" rather than "clipped by accident". */
fun edgeFade(background: Color = Pk.Cream): Brush = SolidColor(background)
