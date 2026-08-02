package com.prayerkey.manna.ui.worlds

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws a verse's world.
 *
 * Vector, so thirteen worlds cost roughly what one photograph would and
 * nothing is downloaded. Depth comes from SceneKit: haze between ranges,
 * reflections in water, grain over the top. Without those a drawn landscape
 * reads as clip art no matter how good the palette is.
 *
 * PERF: [animate] is false for the waiting card and under reduce-motion, so
 * a scene that is not in front does no per-frame work.
 */
@Composable
fun WorldScene(world: VerseWorld, animate: Boolean, modifier: Modifier = Modifier) {
    val phase = if (animate) {
        val t = rememberInfiniteTransition(label = "world")
        val v by t.animateFloat(
            0f, 1f,
            infiniteRepeatable(
                tween(if (world == VerseWorld.THRONE || world == VerseWorld.PROMISE) 26000 else 11000, easing = LinearEasing),
            ),
            label = "world-phase",
        )
        v
    } else 0f

    val seeds = remember(world) { seedsFor(world) }

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(world.sky))
            when (world) {
                VerseWorld.STARFIELD -> starfield(seeds, phase, animate)
                VerseWorld.PASTURE -> pasture()
                VerseWorld.SEA -> sea(seeds, phase, animate)
                VerseWorld.HEIGHTS -> heights(phase, animate)
                VerseWorld.DAWN -> dawn(seeds, phase, animate)
                VerseWorld.WATCH -> watch(seeds, phase, animate)
                VerseWorld.FIRE -> fire(seeds, phase, animate)
                VerseWorld.HARVEST -> harvest(seeds)
                VerseWorld.RIVER -> river(phase, animate)
                VerseWorld.WILDERNESS -> wilderness()
                VerseWorld.CITY -> city(seeds)
                VerseWorld.THRONE -> throne(phase, animate)
                VerseWorld.GARDEN -> garden(seeds)
                VerseWorld.PROMISE -> promise(seeds, phase, animate)
            }
            vignette()
            grain()
        }
    }
}

/* ─────────────────────────── layout seeds ─────────────────────────── */

private class Seeds(val xs: FloatArray, val ys: FloatArray, val rs: FloatArray)

private fun seedsFor(world: VerseWorld): Seeds {
    var s = world.ordinal * 9973 + 12345
    fun next(): Float { s = (s * 1103515245 + 12345) and 0x7fffffff; return (s % 1000) / 1000f }
    val n = 64
    return Seeds(FloatArray(n) { next() }, FloatArray(n) { next() }, FloatArray(n) { next() })
}

/* ───────────────────────────── scenes ───────────────────────────── */

/** Night desert: milky way band, layered dunes, a low moon. */
private fun DrawScope.starfield(seed: Seeds, phase: Float, animate: Boolean) {
    val sky = Color(0xFF141B45)

    // the galactic band, tilted
    rotate(-24f, Offset(size.width * .5f, size.height * .3f)) {
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, Color(0xFF6E7BC4).copy(alpha = .18f), Color.Transparent),
                startY = size.height * .10f, endY = size.height * .46f,
            ),
        )
    }

    for (i in 0 until 90) {
        val x = seed.xs[i % seed.xs.size] * size.width
        val y = seed.ys[i % seed.ys.size] * size.height * .70f
        val big = i % 11 == 0
        val r = if (big) 2.6f else 1.3f
        val tw = if (animate) .35f + .65f * abs(sin((phase * 6.283f * 2 + i))) else .78f
        if (big) drawCircle(Color.White.copy(alpha = tw * .22f), r * 5f, Offset(x, y))
        drawCircle(Color.White.copy(alpha = tw), r, Offset(x, y))
    }

    light(Offset(size.width * .74f, size.height * .17f), size.minDimension * .05f, Color(0xFFF6F1DC), Color(0xFFBFC8F0))

    range(.80f, .10f, Color(0xFF2A2450), sky, .55f, 4, 4411, jag = .5f)
    range(.90f, .09f, Color(0xFF15122C), sky, .22f, 3, 991, jag = .6f)
    rolling(1.02f, .12f, Color(0xFF0B0918), sky, 0f, 0f)
}

/** Shepherd country: haze-stacked hills, a still pond, a warm sun. */
private fun DrawScope.pasture() {
    val sky = Color(0xFFBCD8E8)
    light(Offset(size.width * .74f, size.height * .17f), size.minDimension * .07f, Color(0xFFFFFCEC), Color(0xFFFFE9A8))

    range(.62f, .13f, Color(0xFF7E9AA8), sky, .68f, 5, 771, jag = .55f)
    rolling(.72f, .13f, Color(0xFF6B9A58), sky, .42f, .18f)
    rolling(.80f, .12f, Color(0xFF4A7C42), sky, .20f, -.16f)

    // pond, mirroring the sky
    reflection(.845f, Color(0xFF5E8AA0)) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFFBCD8E8), Color(0xFFDFEAD2))))
        rolling(.80f, .12f, Color(0xFF4A7C42), Color(0xFFBCD8E8), .20f, -.16f)
    }
    ripples(.845f, 0f, false)

    rolling(1.06f, .16f, Color(0xFF2F5A2C), sky, 0f, .05f)
}

/** Galilee at storm: rain, whitecaps, a broken horizon. */
private fun DrawScope.sea(seed: Seeds, phase: Float, animate: Boolean) {
    val sky = Color(0xFF233846)

    // cloud mass
    for (i in 0 until 5) {
        val cy = size.height * (.10f + i * .045f)
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFF0E1A22).copy(alpha = .55f), Color.Transparent),
                center = Offset(size.width * (.15f + seed.xs[i] * .7f), cy),
                radius = size.width * .34f,
            ),
            size.width * .34f,
            Offset(size.width * (.15f + seed.xs[i] * .7f), cy),
        )
    }

    range(.52f, .07f, Color(0xFF2B4453), sky, .72f, 4, 3301, jag = .5f)

    if (animate) {
        for (i in 0 until 34) {
            val x = seed.xs[i] * size.width
            val fall = ((phase * 3.2f + seed.rs[i]) % 1f) * size.height
            drawLine(Color.White.copy(alpha = .26f), Offset(x - 4f, fall), Offset(x, fall + 22f), 1.2f)
        }
    }

    val bob = if (animate) sin(phase * 6.283f) * size.width * .025f else 0f
    swell(.60f, Color(0xFF2F5D6E), bob, .55f)
    swell(.72f, Color(0xFF1C3E4C), -bob, .85f)
    swell(.86f, Color(0xFF0B1B22), bob * .5f, 1f)

    // whitecaps along the nearest swell
    for (i in 0 until 10) {
        val x = seed.xs[i + 20] * size.width
        val y = size.height * (.86f + seed.ys[i] * .05f)
        drawArc(
            Color.White.copy(alpha = .22f), 200f, 140f, false,
            topLeft = Offset(x, y), size = Size(size.width * .12f, size.height * .012f),
            style = Stroke(width = 2.2f),
        )
    }
}

private fun DrawScope.swell(topFrac: Float, colour: Color, shift: Float, alpha: Float) {
    val top = size.height * topFrac
    val p = Path().apply {
        moveTo(-size.width * .3f, size.height)
        lineTo(-size.width * .3f, top)
        cubicTo(
            size.width * .18f + shift, top - size.height * .035f,
            size.width * .68f + shift, top + size.height * .04f,
            size.width * 1.3f, top - size.height * .01f,
        )
        lineTo(size.width * 1.3f, size.height)
        close()
    }
    drawPath(p, colour.copy(alpha = alpha))
}

/** Sunset ridges, four ranges deep, one bird. */
private fun DrawScope.heights(phase: Float, animate: Boolean) {
    val sky = Color(0xFFE0805A)
    light(Offset(size.width * .5f, size.height * .30f), size.minDimension * .10f, Color(0xFFFFF6E0), Color(0xFFFFC978))

    range(.56f, .18f, Color(0xFF8A5A62), sky, .78f, 3, 8123, jag = 1.1f)
    range(.68f, .22f, Color(0xFF6B3F52), sky, .55f, 4, 2277, jag = 1.2f)
    range(.82f, .24f, Color(0xFF3A2438), sky, .30f, 5, 5519, jag = 1.25f)
    range(.98f, .26f, Color(0xFF201322), sky, .08f, 6, 9081, jag = 1.3f)

    val drift = if (animate) sin(phase * 6.283f) * size.width * .09f else 0f
    bird(size.width * .34f + drift, size.height * .27f - drift * .25f, size.width * .075f, Color(0xFF241726))
    bird(size.width * .52f + drift * .6f, size.height * .21f, size.width * .045f, Color(0xFF241726).copy(alpha = .75f))
}

private fun DrawScope.bird(cx: Float, cy: Float, w: Float, colour: Color) {
    val p = Path().apply {
        moveTo(cx - w, cy)
        cubicTo(cx - w * .55f, cy - w * .52f, cx - w * .18f, cy - w * .12f, cx, cy)
        cubicTo(cx + w * .18f, cy - w * .12f, cx + w * .55f, cy - w * .52f, cx + w, cy)
        cubicTo(cx + w * .55f, cy - w * .22f, cx + w * .18f, cy + w * .06f, cx, cy + w * .07f)
        cubicTo(cx - w * .18f, cy + w * .06f, cx - w * .55f, cy - w * .22f, cx - w, cy)
        close()
    }
    drawPath(p, colour)
}

/** Sunrise over the camp, manna on the ground catching first light. */
private fun DrawScope.dawn(seed: Seeds, phase: Float, animate: Boolean) {
    val sky = Color(0xFFD98D5F)
    light(Offset(size.width * .5f, size.height * .60f), size.minDimension * .12f, Color(0xFFFFF8E4), Color(0xFFFFD98F))

    range(.58f, .10f, Color(0xFF7A5A72), sky, .74f, 4, 1717, jag = .7f)
    rolling(.70f, .10f, Color(0xFF9A6A5A), sky, .45f, .12f)

    // ground plane with a light gradient running back to the sun
    drawRect(
        Brush.verticalGradient(listOf(Color(0xFFB08A55), Color(0xFF6E5230))),
        topLeft = Offset(0f, size.height * .78f),
        size = Size(size.width, size.height * .22f),
    )

    for (i in 0 until 26) {
        val x = seed.xs[i] * size.width
        val d = seed.ys[i]
        val y = size.height * (.79f + d * .19f)
        val r = 1.6f + d * 2.6f
        val tw = if (animate) .55f + .45f * abs(sin(phase * 6.283f + i)) else .85f
        drawCircle(Color(0xFFFFF2CF).copy(alpha = .30f * tw), r * 4f, Offset(x, y))
        drawCircle(Color(0xFFFFFBEC).copy(alpha = tw), r, Offset(x, y))
    }
}

/** Deep night, a moon, a fire's embers rising. */
private fun DrawScope.watch(seed: Seeds, phase: Float, animate: Boolean) {
    val sky = Color(0xFF101226)
    for (i in 0 until 46) {
        val x = seed.xs[i % seed.xs.size] * size.width
        val y = seed.ys[i % seed.ys.size] * size.height * .58f
        drawCircle(Color.White.copy(alpha = .18f + seed.rs[i % seed.rs.size] * .5f), 1.2f, Offset(x, y))
    }
    light(Offset(size.width * .76f, size.height * .15f), size.minDimension * .055f, Color(0xFFFDF8E8), Color(0xFFCFC6A8))

    range(.74f, .14f, Color(0xFF1E1B3A), sky, .5f, 4, 6161, jag = .8f)
    range(.90f, .14f, Color(0xFF0C0A1A), sky, .12f, 5, 3737, jag = .9f)

    // the watchfire
    val fx = size.width * .22f
    val fy = size.height * .92f
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFF9A3C).copy(alpha = .45f), Color.Transparent), center = Offset(fx, fy), radius = size.width * .30f),
        size.width * .30f, Offset(fx, fy),
    )
    if (animate) {
        for (i in 0 until 16) {
            val prog = ((phase * 1.5f + seed.rs[i]) % 1f)
            val sway = sin(prog * 7f + i) * size.width * .045f
            val x = fx + sway + (seed.xs[i] - .5f) * size.width * .06f
            val y = fy - prog * size.height * .55f
            drawCircle(Color(0xFFFFB45E).copy(alpha = (1f - prog) * .9f), 1.4f + (1f - prog) * 1.8f, Offset(x, y))
        }
    }
}

/** The furnace. Heat haze, tongues of flame, glowing floor. */
private fun DrawScope.fire(seed: Seeds, phase: Float, animate: Boolean) {
    drawRect(
        Brush.radialGradient(
            listOf(Color(0xFFFFB454).copy(alpha = .6f), Color(0xFF8A3A12).copy(alpha = .25f), Color.Transparent),
            center = Offset(size.width * .5f, size.height * .88f),
            radius = size.minDimension * .95f,
        ),
    )
    for (t in 0 until 7) {
        val bx = size.width * (.16f + t * .12f)
        val lick = if (animate) sin(phase * 6.283f * 1.6f + t) * size.width * .03f else 0f
        val h = size.height * (.22f + seed.rs[t] * .18f)
        val p = Path().apply {
            moveTo(bx - size.width * .05f, size.height)
            cubicTo(
                bx - size.width * .04f + lick, size.height - h * .5f,
                bx + size.width * .05f + lick, size.height - h * .7f,
                bx + lick * 1.4f, size.height - h,
            )
            cubicTo(
                bx + size.width * .06f + lick, size.height - h * .6f,
                bx + size.width * .05f, size.height - h * .3f,
                bx + size.width * .05f, size.height,
            )
            close()
        }
        drawPath(p, Color(0xFFFFC46A).copy(alpha = .30f))
    }
    for (i in 0 until 34) {
        val prog = if (animate) ((phase * 2.1f + seed.rs[i]) % 1f) else seed.rs[i]
        val sway = sin(prog * 8f + i) * size.width * .035f
        val x = size.width * (.14f + seed.xs[i] * .72f) + sway
        val y = size.height * (1f - prog * .92f)
        val a = (1f - prog)
        drawCircle(Color(0xFFFFE0A8).copy(alpha = a * .9f), 1.2f + a * 2.4f, Offset(x, y))
    }
}

/** Ripe field under a high sun, wind-combed. */
private fun DrawScope.harvest(seed: Seeds) {
    val sky = Color(0xFFE4D9A6)
    light(Offset(size.width * .24f, size.height * .16f), size.minDimension * .07f, Color(0xFFFFFDF0), Color(0xFFFFF0B8))

    rolling(.54f, .09f, Color(0xFFB8B27A), sky, .66f, .2f)
    rolling(.62f, .08f, Color(0xFF9E9450), sky, .34f, -.18f)

    drawRect(
        Brush.verticalGradient(listOf(Color(0xFFD7B455), Color(0xFF8A6A20))),
        topLeft = Offset(0f, size.height * .62f),
        size = Size(size.width, size.height * .38f),
    )
    for (i in 0 until 64) {
        val d = seed.ys[i % seed.ys.size]
        val x = seed.xs[i % seed.xs.size] * size.width
        val y = size.height * (.63f + d * .36f)
        val h = size.height * (.03f + d * .075f)
        val lean = (seed.rs[i % seed.rs.size] - .5f) * h * .5f
        drawLine(Color(0xFF6E5316).copy(alpha = .55f + d * .4f), Offset(x, y), Offset(x + lean, y - h), 1.6f + d * 1.6f)
        drawCircle(Color(0xFFF6E3A0).copy(alpha = .7f + d * .3f), 1.6f + d * 1.6f, Offset(x + lean, y - h))
    }
}

/** A river running out of the light, banks either side. */
private fun DrawScope.river(phase: Float, animate: Boolean) {
    val sky = Color(0xFF2C7C82)
    light(Offset(size.width * .5f, size.height * .26f), size.minDimension * .08f, Color(0xFFF2FFFB), Color(0xFF9BE6D4))
    range(.50f, .10f, Color(0xFF2E6F72), sky, .7f, 4, 1201, jag = .6f)

    // water wedge widening toward the viewer
    val p = Path().apply {
        moveTo(size.width * .44f, size.height * .50f)
        lineTo(size.width * .56f, size.height * .50f)
        cubicTo(size.width * .78f, size.height * .72f, size.width * .86f, size.height * .86f, size.width * 1.02f, size.height)
        lineTo(-size.width * .02f, size.height)
        cubicTo(size.width * .14f, size.height * .86f, size.width * .22f, size.height * .72f, size.width * .44f, size.height * .50f)
        close()
    }
    drawPath(p, Brush.verticalGradient(listOf(Color(0xFF7FD3C4), Color(0xFF1C5A62)), startY = size.height * .5f, endY = size.height))

    // banks
    rolling(.70f, .16f, Color(0xFF1E5A50), sky, .18f, -.42f)
    rolling(.70f, .16f, Color(0xFF1E5A50), sky, .18f, .42f)

    ripples(.52f, phase, animate)
}

/** Dry country: sand ranges, heat haze, no water anywhere. */
private fun DrawScope.wilderness() {
    val sky = Color(0xFFD9A46E)
    light(Offset(size.width * .68f, size.height * .18f), size.minDimension * .085f, Color(0xFFFFF6DF), Color(0xFFFFD9A0))

    range(.56f, .11f, Color(0xFFC49A6A), sky, .74f, 3, 5151, jag = .5f)
    rolling(.68f, .12f, Color(0xFFB8834A), sky, .48f, .22f)
    rolling(.80f, .14f, Color(0xFF8A5E30), sky, .24f, -.24f)
    rolling(1.04f, .18f, Color(0xFF5C3A1E), sky, 0f, .06f)

    // one dead tree, for scale — kept high and left so it never sits in
    // the text column or behind the action buttons
    val tx = size.width * .17f
    val ty = size.height * .565f
    drawLine(Color(0xFF3A2412).copy(alpha = .7f), Offset(tx, ty), Offset(tx, ty - size.height * .055f), 2.5f)
    drawLine(Color(0xFF3A2412).copy(alpha = .7f), Offset(tx, ty - size.height * .038f), Offset(tx + size.width * .03f, ty - size.height * .06f), 2f)
    drawLine(Color(0xFF3A2412).copy(alpha = .7f), Offset(tx, ty - size.height * .044f), Offset(tx - size.width * .028f, ty - size.height * .065f), 2f)
}

/** Jerusalem at dusk: walls, towers, lit windows. */
private fun DrawScope.city(seed: Seeds) {
    val sky = Color(0xFF4A3A6B)
    light(Offset(size.width * .5f, size.height * .30f), size.minDimension * .09f, Color(0xFFFFEFD0), Color(0xFFE8B87A))
    range(.58f, .09f, Color(0xFF54406E), sky, .72f, 4, 6420, jag = .6f)

    // wall
    drawRect(Color(0xFF241B3E), topLeft = Offset(0f, size.height * .74f), size = Size(size.width, size.height * .26f))
    var bx = 0f
    var i = 0
    while (bx < size.width) {
        drawRect(Color(0xFF241B3E), topLeft = Offset(bx, size.height * .715f), size = Size(size.width * .035f, size.height * .03f))
        bx += size.width * .07f; i++
    }

    // towers and houses behind the wall
    var x = 0f
    i = 0
    while (x < size.width) {
        val w = size.width * (.07f + seed.rs[i % seed.rs.size] * .07f)
        val h = size.height * (.08f + seed.ys[i % seed.ys.size] * .16f)
        val top = size.height * .74f - h
        drawRect(Color(0xFF2E2350), topLeft = Offset(x, top), size = Size(w * .9f, h))
        if (i % 4 == 0) {
            // a dome
            drawArc(
                Color(0xFF3A2C62), 180f, 180f, true,
                topLeft = Offset(x, top - w * .38f), size = Size(w * .9f, w * .76f),
            )
        }
        var wy = top + size.height * .022f
        var k = 0
        while (wy < size.height * .72f) {
            if ((i + k) % 3 != 0) {
                drawRect(
                    Color(0xFFFFD98F).copy(alpha = .5f),
                    topLeft = Offset(x + w * .22f, wy), size = Size(w * .18f, size.height * .009f),
                )
            }
            wy += size.height * .028f; k++
        }
        x += w; i++
    }
}

/** Light from a centre that never resolves into an object. */
private fun DrawScope.throne(phase: Float, animate: Boolean) {
    val centre = Offset(size.width * .5f, size.height * .34f)
    rotate(if (animate) phase * 360f else 0f, centre) {
        for (i in 0 until 22) {
            val a = (i * 16.36f) * PI.toFloat() / 180f
            val p = Path().apply {
                moveTo(centre.x, centre.y)
                lineTo(centre.x + cos(a) * size.maxDimension, centre.y + sin(a) * size.maxDimension)
                lineTo(centre.x + cos(a + .13f) * size.maxDimension, centre.y + sin(a + .13f) * size.maxDimension)
                close()
            }
            drawPath(p, Color.White.copy(alpha = .06f))
        }
    }
    // steps up into the light
    for (i in 0 until 5) {
        val y = size.height * (.72f + i * .06f)
        val inset = size.width * (.30f - i * .06f)
        drawRect(
            Color(0xFF2A1E52).copy(alpha = .5f + i * .1f),
            topLeft = Offset(inset, y), size = Size(size.width - inset * 2f, size.height * .06f),
        )
    }
    light(centre, size.minDimension * .13f, Color(0xFFFFF6DC), Color(0xFFE8CFF6))
}

/** Vine country: trellis rows receding, fruit catching light. */
private fun DrawScope.garden(seed: Seeds) {
    val sky = Color(0xFF6FA87C)
    light(Offset(size.width * .30f, size.height * .16f), size.minDimension * .07f, Color(0xFFFFFDEC), Color(0xFFDFF3C8))
    rolling(.52f, .10f, Color(0xFF6E9E78), sky, .68f, .2f)
    rolling(.62f, .10f, Color(0xFF4E8258), sky, .38f, -.2f)

    drawRect(
        Brush.verticalGradient(listOf(Color(0xFF3F7A52), Color(0xFF1A3A28))),
        topLeft = Offset(0f, size.height * .62f),
        size = Size(size.width, size.height * .38f),
    )
    for (row in 0 until 4) {
        val d = row / 3f
        val y = size.height * (.66f + d * .3f)
        val h = size.height * (.05f + d * .07f)
        for (k in 0 until (6 - row)) {
            val x = size.width * ((k + .5f) / (6f - row))
            drawLine(Color(0xFF23412C), Offset(x, y), Offset(x, y - h), 2f + d * 3f)
            drawCircle(Color(0xFF2E5C3C), h * .55f, Offset(x, y - h - h * .25f))
            if ((k + row) % 2 == 0) {
                drawCircle(Color(0xFF7A2E48).copy(alpha = .85f), 2f + d * 2.5f, Offset(x + h * .2f, y - h * .5f))
            }
        }
    }
}

/** The gold card: rays, motes, and a horizon of pure light. */
private fun DrawScope.promise(seed: Seeds, phase: Float, animate: Boolean) {
    val centre = Offset(size.width * .5f, size.height * .30f)
    rotate(if (animate) phase * 360f else 0f, centre) {
        for (i in 0 until 30) {
            val a = (i * 12f) * PI.toFloat() / 180f
            val p = Path().apply {
                moveTo(centre.x, centre.y)
                lineTo(centre.x + cos(a) * size.maxDimension, centre.y + sin(a) * size.maxDimension)
                lineTo(centre.x + cos(a + .1f) * size.maxDimension, centre.y + sin(a + .1f) * size.maxDimension)
                close()
            }
            drawPath(p, Color.White.copy(alpha = .13f))
        }
    }
    range(.78f, .12f, Color(0xFFB98C36), Color(0xFFE8BD62), .5f, 4, 7777, jag = .7f)
    rolling(1.0f, .14f, Color(0xFF7C561A), Color(0xFFE8BD62), .15f, .04f)
    for (i in 0 until 26) {
        val x = seed.xs[i] * size.width
        val y = seed.ys[i] * size.height * .82f
        val tw = if (animate) .3f + .7f * abs(sin(phase * 6.283f * 3 + i)) else .8f
        drawCircle(Color.White.copy(alpha = tw * .3f), 6f, Offset(x, y))
        drawCircle(Color.White.copy(alpha = tw), 2.2f, Offset(x, y))
    }
    light(centre, size.minDimension * .11f, Color(0xFFFFFDF2), Color(0xFFFFE9A8))
}
