package com.prayerkey.manna.ui.worlds

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.sin

/**
 * The pieces that make a drawn landscape stop looking like clip art.
 *
 * Flat silhouettes on a gradient read as vector shapes no matter how nice
 * the colours are. Real depth comes from four things, and the first one
 * does most of the work:
 *
 *  1. ATMOSPHERE — distant things are washed toward the sky colour. This is
 *     aerial perspective, and it is the single biggest cue the eye uses for
 *     depth. Silhouettes without it always look pasted on.
 *  2. REFLECTION — water repeats the sky and the ridge above it, softened.
 *  3. GRAIN — a faint dither breaks up perfectly smooth gradients, which
 *     otherwise band on cheap panels and look synthetic.
 *  4. LIGHT — a sun is not a disc, it is a disc plus scatter plus a wash
 *     across everything downstream of it.
 */

/** Distance haze. [depth] 0 = foreground, 1 = far away. */
internal fun DrawScope.haze(topFrac: Float, depth: Float, skyColour: Color) {
    if (depth <= 0f) return
    val y = size.height * topFrac
    drawRect(
        Brush.verticalGradient(
            listOf(skyColour.copy(alpha = .34f * depth), skyColour.copy(alpha = .02f * depth)),
            startY = y - size.height * .12f,
            endY = y + size.height * .22f,
        ),
        topLeft = Offset(0f, y - size.height * .12f),
        size = Size(size.width, size.height * .34f),
    )
}

/**
 * A ridge line built from a seeded profile. [depth] fades it toward the sky
 * so successive ranges sit behind one another instead of stacking flat.
 */
internal fun DrawScope.range(
    baseFrac: Float,
    heightFrac: Float,
    colour: Color,
    skyColour: Color,
    depth: Float,
    peaks: Int,
    seed: Int,
    jag: Float = 1f,
) {
    val base = size.height * baseFrac
    val top = base - size.height * heightFrac
    var s = seed
    fun rnd(): Float { s = (s * 1103515245 + 12345) and 0x7fffffff; return (s % 1000) / 1000f }

    val p = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, base - size.height * heightFrac * rnd() * .5f)
        val step = 1f / peaks
        for (i in 0..peaks) {
            val x = size.width * (i * step)
            val h = (0.25f + rnd() * 0.75f * jag)
            lineTo(x, top + (base - top) * (1f - h))
            // a small shoulder so peaks are not perfect triangles
            lineTo(x + size.width * step * .3f, top + (base - top) * (1f - h * .72f))
        }
        lineTo(size.width, base)
        lineTo(size.width, size.height)
        close()
    }
    // washed toward the sky by distance
    val washed = Color(
        red = colour.red + (skyColour.red - colour.red) * depth * .55f,
        green = colour.green + (skyColour.green - colour.green) * depth * .55f,
        blue = colour.blue + (skyColour.blue - colour.blue) * depth * .55f,
        alpha = 1f,
    )
    drawPath(p, washed)
    haze(baseFrac - heightFrac * .35f, depth, skyColour)
}

/** Sun or moon: core, scatter, and a wash over the sky beneath it. */
internal fun DrawScope.light(
    centre: Offset,
    radius: Float,
    core: Color,
    glow: Color,
    washDown: Boolean = true,
) {
    if (washDown) {
        drawRect(
            Brush.radialGradient(
                listOf(glow.copy(alpha = .30f), Color.Transparent),
                center = centre,
                radius = radius * 7f,
            ),
        )
    }
    drawCircle(
        Brush.radialGradient(listOf(glow.copy(alpha = .55f), Color.Transparent), center = centre, radius = radius * 3.2f),
        radius * 3.2f, centre,
    )
    drawCircle(
        Brush.radialGradient(listOf(core, core.copy(alpha = .0f)), center = centre, radius = radius * 1.25f),
        radius * 1.25f, centre,
    )
    drawCircle(core, radius * .62f, centre)
}

/**
 * Mirrors whatever [above] draws into the water below [waterTop], flipped
 * and softened. Cheap, and it is the detail that sells a lake.
 */
internal fun DrawScope.reflection(waterTop: Float, tint: Color, above: DrawScope.() -> Unit) {
    val y = size.height * waterTop
    clipRect(top = y, bottom = size.height) {
        scale(scaleX = 1f, scaleY = -1f, pivot = Offset(size.width / 2f, y)) {
            above()
        }
    }
    // water body colour + the vertical fade that kills the hard mirror edge
    drawRect(
        Brush.verticalGradient(
            listOf(tint.copy(alpha = .35f), tint.copy(alpha = .88f)),
            startY = y, endY = size.height,
        ),
        topLeft = Offset(0f, y),
        size = Size(size.width, size.height - y),
    )
}

/** Horizontal ripple lines, brightest near the far shore. */
internal fun DrawScope.ripples(waterTop: Float, phase: Float, animate: Boolean, tint: Color = Color.White) {
    val y = size.height * waterTop
    val span = size.height - y
    for (i in 0 until 16) {
        val f = i / 16f
        val ly = y + span * (f * f)                       // denser near the shore
        val drift = if (animate) sin((phase * 6.283f) + i) * size.width * .012f else 0f
        val w = size.width * (.25f + f * .6f)
        val x = size.width * .5f - w / 2f + drift
        drawLine(
            tint.copy(alpha = .16f * (1f - f)),
            Offset(x, ly), Offset(x + w, ly),
            strokeWidth = 1.5f + f * 2f,
        )
    }
}

/**
 * Film grain. Deterministic, drawn once per size, and the fastest way to
 * stop a smooth gradient from looking like a computer made it.
 */
internal fun DrawScope.grain(strength: Float = .05f, dots: Int = 900) {
    var s = 20250802
    fun rnd(): Float { s = (s * 1103515245 + 12345) and 0x7fffffff; return (s % 1000) / 1000f }
    for (i in 0 until dots) {
        val x = rnd() * size.width
        val y = rnd() * size.height
        val bright = rnd() > .5f
        drawCircle(
            if (bright) Color.White.copy(alpha = strength) else Color.Black.copy(alpha = strength * .8f),
            1f, Offset(x, y),
        )
    }
}

/** A soft vignette so the eye lands in the middle, not the corners. */
internal fun DrawScope.vignette(strength: Float = .34f) {
    drawRect(
        Brush.radialGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = strength)),
            center = Offset(size.width / 2f, size.height * .42f),
            radius = size.maxDimension * .78f,
        ),
    )
}

/** Rolling hills — smooth where mountains are jagged. */
internal fun DrawScope.rolling(
    baseFrac: Float,
    heightFrac: Float,
    colour: Color,
    skyColour: Color,
    depth: Float,
    shift: Float,
) {
    val base = size.height * baseFrac
    val top = base - size.height * heightFrac
    val p = Path().apply {
        moveTo(-size.width * .3f, size.height)
        lineTo(-size.width * .3f, base)
        cubicTo(
            size.width * (.15f + shift), top,
            size.width * (.75f + shift), top,
            size.width * 1.3f, base,
        )
        lineTo(size.width * 1.3f, size.height)
        close()
    }
    val washed = Color(
        red = colour.red + (skyColour.red - colour.red) * depth * .7f,
        green = colour.green + (skyColour.green - colour.green) * depth * .7f,
        blue = colour.blue + (skyColour.blue - colour.blue) * depth * .7f,
        alpha = 1f,
    )
    drawPath(p, washed)
    haze(baseFrac - heightFrac * .4f, depth, skyColour)
}
