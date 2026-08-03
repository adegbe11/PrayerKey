package com.prayerkey.manna.ui.worlds

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Flat scripture-card art, in the manner of printed devotional cards.
 *
 * The painterly worlds this replaces were trying to be photographs and
 * losing — a landscape rendered in vector always reads as a poor painting.
 * Flat geometry does not have that problem, because flat IS the style
 * rather than a failed attempt at realism. Same cost, no apology.
 *
 * Paper-light grounds with dark ink, so the verse is the loudest thing on
 * the card and a screenshot looks like a card rather than a wallpaper.
 */

/** Ground, two shape tones, and the ink that has to read on top of them. */
data class CardPalette(
    val ground: Color,
    val shapeA: Color,
    val shapeB: Color,
    val accent: Color,
    val ink: Color = Color(0xFF1E1B18),
)

enum class CardMotif { PEAKS, ARCH, SUNBURST, WAVES, CIRCLE, COLUMNS, VALLEY, HORIZON }

/** Muted, adult, paper-like. Nothing saturated enough to fight the words. */
val CARD_PALETTES: List<CardPalette> = listOf(
    // dusty rose
    CardPalette(Color(0xFFF3DFDA), Color(0xFFD9A79C), Color(0xFFC98E86), Color(0xFFD9A441)),
    // sage
    CardPalette(Color(0xFFE4E8DC), Color(0xFFA8B79A), Color(0xFF849780), Color(0xFFD9A441)),
    // slate blue
    CardPalette(Color(0xFFDDE5EC), Color(0xFF9DB3C6), Color(0xFF7C97AE), Color(0xFFE0B255)),
    // clay
    CardPalette(Color(0xFFF0E2D4), Color(0xFFC9A489), Color(0xFFAE8467), Color(0xFF6E7F63)),
    // lavender
    CardPalette(Color(0xFFE7E2EC), Color(0xFFB3A8C4), Color(0xFF93869F), Color(0xFFD9A441)),
    // mustard on cream
    CardPalette(Color(0xFFF6EEDC), Color(0xFFE2C067), Color(0xFFC79E3E), Color(0xFF7C8B93)),
    // blush
    CardPalette(Color(0xFFF7E6E4), Color(0xFFE0B3AE), Color(0xFFC98F8A), Color(0xFF8C9B7A)),
    // stone
    CardPalette(Color(0xFFECE8E1), Color(0xFFC2BBB0), Color(0xFF9E9689), Color(0xFFC98F5A)),
    // mint
    CardPalette(Color(0xFFDFEAE4), Color(0xFF9FC4B3), Color(0xFF7BA694), Color(0xFFD9A441)),
    // sand
    CardPalette(Color(0xFFF4EADA), Color(0xFFDCC49B), Color(0xFFC2A578), Color(0xFF88705A)),
    // deep pine, the one dark card
    CardPalette(Color(0xFFE6EBE4), Color(0xFF6E8A72), Color(0xFF4C6B52), Color(0xFFD9A441)),
    // terracotta
    CardPalette(Color(0xFFF5E3D8), Color(0xFFD79A76), Color(0xFFBC7B58), Color(0xFF6E7F63)),
)

/** The gold card keeps its own treatment — it has to feel found. */
val PROMISE_PALETTE = CardPalette(
    ground = Color(0xFFFBF0D4),
    shapeA = Color(0xFFE8C97A),
    shapeB = Color(0xFFD3A94A),
    accent = Color(0xFF8A6A1E),
    ink = Color(0xFF3A2C10),
)

/** Each world keeps its meaning; only the painting changes. */
fun VerseWorld.motif(): CardMotif = when (this) {
    VerseWorld.HEIGHTS, VerseWorld.WILDERNESS -> CardMotif.PEAKS
    VerseWorld.STARFIELD, VerseWorld.THRONE -> CardMotif.SUNBURST
    VerseWorld.SEA, VerseWorld.RIVER -> CardMotif.WAVES
    VerseWorld.DAWN, VerseWorld.HARVEST -> CardMotif.HORIZON
    VerseWorld.PASTURE, VerseWorld.GARDEN -> CardMotif.VALLEY
    VerseWorld.CITY -> CardMotif.COLUMNS
    VerseWorld.WATCH -> CardMotif.CIRCLE
    VerseWorld.FIRE -> CardMotif.ARCH
    VerseWorld.PROMISE -> CardMotif.SUNBURST
}

/** Stable palette per verse, so a verse always wears the same card. */
fun paletteFor(world: VerseWorld, reference: String): CardPalette {
    if (world.isPromise) return PROMISE_PALETTE
    val h = reference.fold(17) { a, c -> a * 31 + c.code } and 0x7fffffff
    return CARD_PALETTES[h % CARD_PALETTES.size]
}

/**
 * Draws the motif into the lower portion of the card, leaving the upper
 * two-thirds clear for the verse. Printed cards do the same thing: the
 * shape supports the words, it never competes with them.
 */
fun DrawScope.cardMotif(motif: CardMotif, p: CardPalette, seedRef: String) {
    var s = seedRef.fold(7) { a, c -> a * 31 + c.code } and 0x7fffffff
    fun rnd(): Float { s = (s * 1103515245 + 12345) and 0x7fffffff; return (s % 1000) / 1000f }

    val w = size.width
    // the art lives in the lower band only, and fades back into the ground
    // at the very bottom so the floating controls sit on calm paper.
    // .96 rather than .88: the shorter band left a hole under short verses.
    val h = size.height * .96f

    when (motif) {
        CardMotif.PEAKS -> {
            tri(w * .16f, h, w * .52f, h * .52f, w * .88f, h, p.shapeA)
            tri(w * .48f, h, w * .80f, h * .64f, w * 1.12f, h, p.shapeB)
            tri(w * -.08f, h, w * .22f, h * .72f, w * .52f, h, p.accent)
        }
        CardMotif.VALLEY -> {
            val path = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * .70f)
                cubicTo(w * .3f, h * .52f, w * .7f, h * .52f, w, h * .70f)
                lineTo(w, h)
                close()
            }
            drawPath(path, p.shapeA)
            val near = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * .84f)
                cubicTo(w * .35f, h * .70f, w * .65f, h * .70f, w, h * .84f)
                lineTo(w, h)
                close()
            }
            drawPath(near, p.shapeB)
            drawCircle(p.accent, w * .07f, Offset(w * .74f, h * .44f))
        }
        CardMotif.SUNBURST -> {
            val c = Offset(w * .5f, h * .78f)
            for (i in 0 until 9) {
                val a0 = Math.toRadians((i * 20.0) + 180.0)
                val a1 = a0 + Math.toRadians(9.0)
                val r = w * 1.3f
                tri(
                    c.x, c.y,
                    c.x + (Math.cos(a0) * r).toFloat(), c.y + (Math.sin(a0) * r).toFloat(),
                    c.x + (Math.cos(a1) * r).toFloat(), c.y + (Math.sin(a1) * r).toFloat(),
                    if (i % 2 == 0) p.shapeA else p.shapeB,
                )
            }
            drawCircle(p.accent, w * .11f, c)
        }
        CardMotif.WAVES -> {
            for (i in 0 until 3) {
                val top = h * (.62f + i * .13f)
                val path = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, top)
                    cubicTo(w * .28f, top - h * .06f, w * .72f, top + h * .06f, w, top)
                    lineTo(w, h)
                    close()
                }
                drawPath(path, listOf(p.shapeA, p.shapeB, p.accent)[i])
            }
        }
        CardMotif.CIRCLE -> {
            drawCircle(p.shapeA, w * .34f, Offset(w * .5f, h * .80f))
            drawCircle(p.ground, w * .26f, Offset(w * .5f, h * .80f))
            drawCircle(p.shapeB, w * .10f, Offset(w * .5f, h * .80f))
            drawCircle(p.accent, w * .05f, Offset(w * .78f, h * .60f))
        }
        CardMotif.ARCH -> {
            drawArc(
                p.shapeA, 180f, 180f, true,
                topLeft = Offset(w * .12f, h * .58f),
                size = Size(w * .76f, h * .52f),
            )
            drawArc(
                p.shapeB, 180f, 180f, true,
                topLeft = Offset(w * .26f, h * .70f),
                size = Size(w * .48f, h * .40f),
            )
            drawArc(
                p.accent, 180f, 180f, false,
                topLeft = Offset(w * .04f, h * .50f),
                size = Size(w * .92f, h * .62f),
                style = Stroke(width = w * .012f),
            )
        }
        CardMotif.COLUMNS -> {
            var x = w * .08f
            var i = 0
            while (x < w * .95f) {
                val ch = h * (.14f + rnd() * .22f)
                drawRect(
                    if (i % 3 == 0) p.shapeB else p.shapeA,
                    topLeft = Offset(x, h - ch),
                    size = Size(w * .10f, ch),
                )
                if (i % 3 == 0) {
                    drawArc(
                        p.shapeB, 180f, 180f, true,
                        topLeft = Offset(x, h - ch - w * .05f),
                        size = Size(w * .10f, w * .10f),
                    )
                }
                x += w * .135f
                i++
            }
            drawCircle(p.accent, w * .06f, Offset(w * .82f, h * .56f))
        }
        CardMotif.HORIZON -> {
            drawRect(p.shapeA, topLeft = Offset(0f, h * .74f), size = Size(w, h * .26f))
            drawCircle(p.accent, w * .13f, Offset(w * .5f, h * .74f))
            drawRect(p.shapeB, topLeft = Offset(0f, h * .88f), size = Size(w, h * .12f))
        }
    }
}

/** Fades the motif back into the card so controls have clean ground. */
fun DrawScope.settleMotif(ground: Color) {
    drawRect(
        androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(Color.Transparent, ground.copy(alpha = .92f), ground),
            startY = size.height * .74f,
            endY = size.height,
        ),
        topLeft = Offset(0f, size.height * .74f),
        size = Size(size.width, size.height * .26f),
    )
}

private fun DrawScope.tri(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, c: Color) {
    drawPath(
        Path().apply {
            moveTo(x1, y1); lineTo(x2, y2); lineTo(x3, y3); close()
        },
        c,
    )
}
