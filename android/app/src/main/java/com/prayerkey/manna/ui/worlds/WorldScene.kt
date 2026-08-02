package com.prayerkey.manna.ui.worlds

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import kotlin.math.sin

/**
 * Draws a verse's world.
 *
 * Everything is vector, so there are no image assets, no download and no
 * APK weight — thirteen worlds cost about as much as one photograph would.
 *
 * PERF: [animate] is false for the waiting card in the deck and whenever
 * the user has reduce-motion on. A scene that is not in front does no work
 * per frame, which matters because these run full screen.
 */
@Composable
fun WorldScene(world: VerseWorld, animate: Boolean, modifier: Modifier = Modifier) {
    val phase = if (animate) {
        val t = rememberInfiniteTransition(label = "world")
        val v by t.animateFloat(
            0f, 1f,
            infiniteRepeatable(tween(if (world == VerseWorld.THRONE || world == VerseWorld.PROMISE) 24000 else 9000, easing = LinearEasing)),
            label = "world-phase",
        )
        v
    } else 0f

    // one stable random layout per world, never re-rolled on recomposition
    val seeds = remember(world) { seedsFor(world) }

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(world.sky))
            when (world) {
                VerseWorld.STARFIELD -> starfield(seeds, phase, animate)
                VerseWorld.PASTURE -> pasture()
                VerseWorld.SEA -> sea(seeds, phase, animate)
                VerseWorld.HEIGHTS -> heights(phase, animate)
                VerseWorld.DAWN -> dawn(seeds)
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
        }
    }
}

/* ─────────────────────────── layout seeds ─────────────────────────── */

private class Seeds(val xs: FloatArray, val ys: FloatArray, val rs: FloatArray)

private fun seedsFor(world: VerseWorld): Seeds {
    // deterministic per world so a scene never reshuffles mid-session
    var s = world.ordinal * 9973 + 12345
    fun next(): Float { s = (s * 1103515245 + 12345) and 0x7fffffff; return (s % 1000) / 1000f }
    val n = 48
    return Seeds(FloatArray(n) { next() }, FloatArray(n) { next() }, FloatArray(n) { next() })
}

/* ───────────────────────────── scenes ───────────────────────────── */

private fun DrawScope.starfield(seed: Seeds, phase: Float, animate: Boolean) {
    for (i in 0 until 44) {
        val x = seed.xs[i % seed.xs.size] * size.width
        val y = seed.ys[i % seed.ys.size] * size.height * .66f
        val base = if (i % 9 == 0) 2.4f else 1.5f
        val tw = if (animate) .45f + .55f * abs(sin((phase * 2 * PI + i).toFloat())) else .8f
        drawCircle(Color.White.copy(alpha = tw), base.dp(), Offset(x, y))
        if (i % 9 == 0) drawCircle(Color.White.copy(alpha = tw * .3f), base.dp() * 3f, Offset(x, y))
    }
    // dune
    val p = Path().apply {
        moveTo(0f, size.height)
        cubicTo(size.width * .25f, size.height * .78f, size.width * .75f, size.height * .78f, size.width, size.height)
        close()
    }
    drawPath(p, Color(0xFF0D0A18))
}

private fun DrawScope.pasture() {
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFF8E0), Color.Transparent), radius = size.minDimension * .18f),
        size.minDimension * .18f,
        Offset(size.width * .78f, size.height * .16f),
    )
    hill(.30f, Color(0xFF6B9A58), .10f)
    hill(.22f, Color(0xFF4A7C42), -.15f)
    hill(.16f, Color(0xFF2F5A2C), .05f)
}

private fun DrawScope.hill(heightFrac: Float, color: Color, shift: Float) {
    val top = size.height * (1f - heightFrac)
    val p = Path().apply {
        moveTo(-size.width * .2f, size.height)
        cubicTo(
            size.width * (.15f + shift), top,
            size.width * (.85f + shift), top,
            size.width * 1.2f, size.height,
        )
        close()
    }
    drawPath(p, color)
}

private fun DrawScope.sea(seed: Seeds, phase: Float, animate: Boolean) {
    if (animate) {
        for (i in 0 until 22) {
            val x = seed.xs[i] * size.width
            val fall = ((phase * 2f + seed.rs[i]) % 1f) * size.height
            drawLine(Color.White.copy(alpha = .28f), Offset(x, fall), Offset(x, fall + 16f), 1.2f)
        }
    }
    val bob = if (animate) sin((phase * 2 * PI).toFloat()) * size.width * .02f else 0f
    wave(.26f, Color(0xFF3C6E7D).copy(alpha = .45f), bob)
    wave(.18f, Color(0xFF1C3E4C).copy(alpha = .85f), -bob)
    wave(.10f, Color(0xFF0B1B22), bob * .5f)
}

private fun DrawScope.wave(heightFrac: Float, color: Color, shift: Float) {
    val top = size.height * (1f - heightFrac)
    val p = Path().apply {
        moveTo(-size.width * .3f, size.height)
        cubicTo(
            size.width * .1f + shift, top - size.height * .04f,
            size.width * .9f + shift, top + size.height * .05f,
            size.width * 1.3f, size.height,
        )
        close()
    }
    drawPath(p, color)
}

private fun DrawScope.heights(phase: Float, animate: Boolean) {
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFF3D6), Color(0xFFFFCE7E), Color.Transparent), radius = size.minDimension * .26f),
        size.minDimension * .26f,
        Offset(size.width * .5f, size.height * .22f),
    )
    ridge(.30f, Color(0xFF3A2438), listOf(.0f, .2f, .38f, .58f, .78f, 1f), listOf(.55f, .18f, .62f, .12f, .68f, .35f))
    ridge(.20f, Color(0xFF241426), listOf(.0f, .22f, .45f, .68f, .88f, 1f), listOf(.7f, .45f, .8f, .4f, .75f, .6f))
    // the bird
    val drift = if (animate) sin((phase * 2 * PI).toFloat()) * size.width * .06f else 0f
    val cx = size.width * .32f + drift
    val cy = size.height * .30f - drift * .3f
    val w = size.width * .07f
    val p = Path().apply {
        moveTo(cx - w, cy)
        quadraticBezierTo(cx - w * .5f, cy - w * .5f, cx, cy - w * .12f)
        quadraticBezierTo(cx + w * .5f, cy - w * .5f, cx + w, cy)
        quadraticBezierTo(cx + w * .5f, cy - w * .16f, cx, cy + w * .12f)
        quadraticBezierTo(cx - w * .5f, cy - w * .16f, cx - w, cy)
        close()
    }
    drawPath(p, Color(0xFF241726))
}

private fun DrawScope.ridge(heightFrac: Float, color: Color, xs: List<Float>, ys: List<Float>) {
    val base = size.height
    val top = size.height * (1f - heightFrac)
    val p = Path().apply {
        moveTo(0f, base)
        xs.forEachIndexed { i, x -> lineTo(size.width * x, top + (base - top) * ys[i]) }
        lineTo(size.width, base)
        close()
    }
    drawPath(p, color)
}

private fun DrawScope.dawn(seed: Seeds) {
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFF6DD), Color(0xFFFFD98F), Color.Transparent), radius = size.minDimension * .3f),
        size.minDimension * .3f,
        Offset(size.width * .5f, size.height * .66f),
    )
    drawRect(Color(0xFF8A6A42), Offset(0f, size.height * .82f), Size(size.width, size.height * .18f))
    for (i in 0 until 18) {
        val x = seed.xs[i] * size.width
        val y = size.height * (.80f + seed.ys[i] * .17f)
        drawCircle(Color(0xFFFFF2CF), 2.6f.dp(), Offset(x, y))
        drawCircle(Color(0xFFFFF2CF).copy(alpha = .35f), 6f.dp(), Offset(x, y))
    }
}

private fun DrawScope.watch(seed: Seeds, phase: Float, animate: Boolean) {
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFDF6E3), Color(0xFFCFC6A8), Color.Transparent), radius = size.minDimension * .16f),
        size.minDimension * .16f,
        Offset(size.width * .76f, size.height * .16f),
    )
    for (i in 0 until 24) {
        val x = seed.xs[i] * size.width
        val y = seed.ys[i] * size.height * .55f
        drawCircle(Color.White.copy(alpha = .5f), 1.1f.dp(), Offset(x, y))
    }
    if (animate) {
        for (i in 0 until 12) {
            val prog = ((phase * 1.4f + seed.rs[i]) % 1f)
            val x = seed.xs[i] * size.width + prog * size.width * .04f
            val y = size.height * (1f - prog * .75f)
            drawCircle(Color(0xFFFFB45E).copy(alpha = (1f - prog) * .9f), 2f.dp(), Offset(x, y))
        }
    }
}

private fun DrawScope.fire(seed: Seeds, phase: Float, animate: Boolean) {
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFB454).copy(alpha = .55f), Color.Transparent), radius = size.minDimension * .5f),
        size.minDimension * .5f,
        Offset(size.width * .5f, size.height * .84f),
    )
    for (i in 0 until 26) {
        val prog = if (animate) ((phase * 1.7f + seed.rs[i]) % 1f) else seed.rs[i]
        val sway = sin((prog * 6f + i).toDouble()).toFloat() * size.width * .03f
        val x = size.width * (.18f + seed.xs[i] * .64f) + sway
        val y = size.height * (1f - prog * .85f)
        val a = (1f - prog)
        drawCircle(Color(0xFFFFD08A).copy(alpha = a * .85f), (1.5f + a * 2.2f).dp(), Offset(x, y))
    }
}

private fun DrawScope.harvest(seed: Seeds) {
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFF6D8), Color.Transparent), radius = size.minDimension * .22f),
        size.minDimension * .22f,
        Offset(size.width * .22f, size.height * .18f),
    )
    drawRect(Color(0xFFC9A648), Offset(0f, size.height * .58f), Size(size.width, size.height * .42f))
    for (i in 0 until 40) {
        val x = seed.xs[i] * size.width
        val h = size.height * (.10f + seed.rs[i] * .10f)
        val y = size.height * (.62f + seed.ys[i] * .34f)
        drawLine(Color(0xFF8A6A20).copy(alpha = .8f), Offset(x, y), Offset(x, y - h), 2f)
        drawCircle(Color(0xFFF0D98A), 2.2f.dp(), Offset(x, y - h))
    }
}

private fun DrawScope.river(phase: Float, animate: Boolean) {
    val shift = if (animate) sin((phase * 2 * PI).toFloat()) * size.width * .015f else 0f
    for (i in 0 until 5) {
        val y = size.height * (.52f + i * .11f)
        val p = Path().apply {
            moveTo(-size.width * .1f, y)
            cubicTo(
                size.width * .3f + shift, y - size.height * .04f,
                size.width * .7f - shift, y + size.height * .04f,
                size.width * 1.1f, y,
            )
        }
        drawPath(p, Color.White.copy(alpha = .16f - i * .02f), style = Stroke(width = (5 - i).toFloat().dp()))
    }
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFBFF0E4).copy(alpha = .35f), Color.Transparent), radius = size.minDimension * .3f),
        size.minDimension * .3f,
        Offset(size.width * .5f, size.height * .3f),
    )
}

private fun DrawScope.wilderness() {
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFF3D0), Color.Transparent), radius = size.minDimension * .2f),
        size.minDimension * .2f,
        Offset(size.width * .68f, size.height * .2f),
    )
    hill(.26f, Color(0xFFB8834A), .18f)
    hill(.18f, Color(0xFF8A5E30), -.2f)
    hill(.11f, Color(0xFF5C3A1E), .0f)
}

private fun DrawScope.city(seed: Seeds) {
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFE9C0).copy(alpha = .5f), Color.Transparent), radius = size.minDimension * .34f),
        size.minDimension * .34f,
        Offset(size.width * .5f, size.height * .34f),
    )
    var x = 0f
    var i = 0
    while (x < size.width) {
        val w = size.width * (.06f + seed.rs[i % seed.rs.size] * .07f)
        val h = size.height * (.12f + seed.ys[i % seed.ys.size] * .22f)
        drawRect(Color(0xFF1B1430), Offset(x, size.height - h), Size(w * .92f, h))
        // a few lit windows
        var wy = size.height - h + 10f
        var k = 0
        while (wy < size.height - 12f) {
            if ((i + k) % 3 == 0) {
                drawRect(Color(0xFFFFD98F).copy(alpha = .55f), Offset(x + w * .25f, wy), Size(w * .16f, 5f))
            }
            wy += 18f; k++
        }
        x += w; i++
    }
}

private fun DrawScope.throne(phase: Float, animate: Boolean) {
    val centre = Offset(size.width * .5f, size.height * .34f)
    rotate(if (animate) phase * 360f else 0f, centre) {
        for (i in 0 until 20) {
            val a = (i * 18f) * PI.toFloat() / 180f
            val p = Path().apply {
                moveTo(centre.x, centre.y)
                lineTo(centre.x + kotlin.math.cos(a) * size.maxDimension, centre.y + kotlin.math.sin(a) * size.maxDimension)
                lineTo(
                    centre.x + kotlin.math.cos(a + .12f) * size.maxDimension,
                    centre.y + kotlin.math.sin(a + .12f) * size.maxDimension,
                )
                close()
            }
            drawPath(p, Color.White.copy(alpha = .07f))
        }
    }
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFF0C8).copy(alpha = .8f), Color.Transparent), radius = size.minDimension * .3f),
        size.minDimension * .3f, centre,
    )
}

private fun DrawScope.garden(seed: Seeds) {
    drawRect(Color(0xFF1E4430), Offset(0f, size.height * .72f), Size(size.width, size.height * .28f))
    for (i in 0 until 16) {
        val x = seed.xs[i] * size.width
        val y = size.height * (.74f + seed.ys[i] * .22f)
        val r = size.minDimension * (.05f + seed.rs[i] * .05f)
        drawCircle(Color(0xFF3F7A52), r, Offset(x, y - r))
        drawLine(Color(0xFF2A5638), Offset(x, y), Offset(x, y - r * .8f), 3f)
    }
    drawCircle(
        Brush.radialGradient(listOf(Color(0xFFFFFBE0).copy(alpha = .45f), Color.Transparent), radius = size.minDimension * .26f),
        size.minDimension * .26f,
        Offset(size.width * .3f, size.height * .2f),
    )
}

private fun DrawScope.promise(seed: Seeds, phase: Float, animate: Boolean) {
    val centre = Offset(size.width * .5f, size.height * .3f)
    rotate(if (animate) phase * 360f else 0f, centre) {
        for (i in 0 until 26) {
            val a = (i * 13.8f) * PI.toFloat() / 180f
            val p = Path().apply {
                moveTo(centre.x, centre.y)
                lineTo(centre.x + kotlin.math.cos(a) * size.maxDimension, centre.y + kotlin.math.sin(a) * size.maxDimension)
                lineTo(
                    centre.x + kotlin.math.cos(a + .09f) * size.maxDimension,
                    centre.y + kotlin.math.sin(a + .09f) * size.maxDimension,
                )
                close()
            }
            drawPath(p, Color.White.copy(alpha = .16f))
        }
    }
    for (i in 0 until 14) {
        val x = seed.xs[i] * size.width
        val y = seed.ys[i] * size.height * .8f
        val tw = if (animate) .4f + .6f * abs(sin((phase * 4 * PI + i).toFloat())) else .8f
        drawCircle(Color.White.copy(alpha = tw), 2.4f.dp(), Offset(x, y))
    }
}

private fun Float.dp() = this * 2.6f
