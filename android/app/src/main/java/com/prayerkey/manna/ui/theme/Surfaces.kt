package com.prayerkey.manna.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Depth, the way it is actually done in premium interfaces.
 *
 * A flat fill plus one shadow reads as a rectangle. Real surfaces get three
 * things: a TIGHT shadow that grounds the edge, a WIDE soft shadow that
 * lifts the whole thing off the page, and a hairline highlight along the
 * top edge where light would catch. Every card in the app went through a
 * single flat `color =` before this.
 */
fun Modifier.premiumCard(
    shape: RoundedCornerShape = R.card,
    fill: Brush = Brush.verticalGradient(listOf(Color(0xFFFAF6EF), Color(0xFFF7F1E8))),
    lift: Boolean = true,
): Modifier = this
    // wide, soft — the lift
    .then(if (lift) Modifier.shadow(18.dp, shape, spotColor = Night.copy(alpha = .16f), ambientColor = Night.copy(alpha = .05f)) else Modifier)
    // tight, close — the edge
    .shadow(3.dp, shape, spotColor = Night.copy(alpha = .16f), ambientColor = Color.Transparent)
    .clip(shape)
    .background(fill)
    .topHighlight(shape)

/** Warm paper, for scripture and prayer. */
val PaperFill = Brush.verticalGradient(listOf(Color(0xFFFAF6EF), Color(0xFFF2EBE0)))

/** The sacred layer. */
val NightFill = Brush.verticalGradient(listOf(Color(0xFF1A1F2A), Color(0xFF12161F)))

/** A gold-leaf wash, for anything answered or kept. */
val GoldFill = Brush.verticalGradient(listOf(Color(0xFFFAF6EF), Color(0xFFF2EBE0)))

/**
 * The one-pixel catch of light along a surface's upper edge. This is the
 * detail that separates "a coloured rectangle" from "an object".
 */
fun Modifier.topHighlight(
    shape: RoundedCornerShape = R.card,
    strength: Float = .75f,
): Modifier = this
    .border(
        width = 0.7.dp,
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = strength),
            .45f to Color.White.copy(alpha = strength * .18f),
            1f to Color.Black.copy(alpha = .045f),
        ),
        shape = shape,
    )

/** Same idea on a dark surface, where the highlight is gold rather than white. */
fun Modifier.goldEdge(shape: RoundedCornerShape = R.card): Modifier = this
    .border(
        width = 0.8.dp,
        brush = Brush.verticalGradient(
            0f to GiltLine.copy(alpha = .75f),
            .5f to GiltLine.copy(alpha = .25f),
            1f to Color.White.copy(alpha = .05f),
        ),
        shape = shape,
    )

/**
 * A faint radial bloom behind a focal element, so the eye is led without a
 * border doing the leading.
 */
fun Modifier.bloom(color: Color = Gold, alpha: Float = .10f): Modifier =
    this.drawWithContent {
        drawContent()
        drawCircle(
            brush = Brush.radialGradient(
                listOf(color.copy(alpha = alpha), Color.Transparent),
                center = Offset(size.width / 2f, size.height * .38f),
                radius = size.minDimension * .78f,
            ),
            radius = size.minDimension * .78f,
            center = Offset(size.width / 2f, size.height * .38f),
        )
    }

/** Floating glass, for pills that sit over content. */
fun Modifier.glassPill(): Modifier = this
    .shadow(9.dp, R.pill, spotColor = Night.copy(alpha = .12f))
    .clip(R.pill)
    .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF7F7FA))))
    .topHighlight(R.pill, strength = .9f)

@Suppress("unused")
private val unusedSize = Size.Zero
