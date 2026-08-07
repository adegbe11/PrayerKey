package com.prayerkey.manna.ui.book

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.emboss
import com.prayerkey.manna.ui.theme.goldLeaf
import com.prayerkey.manna.ui.theme.ogee
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.RomanCaps
import kotlin.math.cos
import kotlin.math.sin

/* The board. Redder and deeper than the spine, the way a rebound family
   Bible is, and mottled rather than flat. */
private val Board = Color(0xFF6B3018)
private val BoardDeep = Color(0xFF2B1108)
private val BoardLit = Color(0xFF8C4426)
private val Gold = Color(0xFFD4AF37)
private val GoldLit = Color(0xFFF3E5AB)
private val GoldDim = Color(0xFF7A5A15)

/**
 * The closed book, before you open it.
 *
 * Opening the reader used to drop you straight onto a page, which is not how
 * you meet a Bible — you meet the board first.
 *
 * The first version of this was flat: gold lines drawn on a brown wash. What
 * makes tooled leather read as tooled is not the lines, it is that every line
 * has a dark edge below it and a bright edge above it, so the eye reads a
 * groove pressed into hide. Everything gold here is drawn three times for
 * that reason — shadow, then metal, then highlight — via [emboss].
 *
 * It is all drawn rather than an image: no APK weight, crisp at any density,
 * and it can take the gilt to the edge of any screen. A photographic cover
 * would look more real still, and would need a licensed raster to do it.
 */
@Composable
fun BibleCover(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TooledBoard(centrepiece = true)

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            /* Stamped, not filled. A gradient on the glyphs alone reads as
               bright paint sitting on top of the board. Real hot-foil sits in
               a groove: there is a shadow cast down into the recess, a bright
               lip on the near edge, and the metal between. So the letters are
               drawn three times, the same way every gold line here is. */
            StampedText("HOLY BIBLE", 33.sp, 2.sp, FontWeight.Bold)
            Box(
                Modifier.padding(top = 11.dp, bottom = 9.dp)
                    .height(1.5.dp).fillMaxWidth(.5f)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GoldLit, Gold, Color(0xFF7A5A15), Color.Transparent),
                        ),
                    ),
            )
            StampedText("OLD & NEW TESTAMENTS", 9.5.sp, 2.4.sp, FontWeight.SemiBold)
        }

        Text(
            "Tap to open",
            color = GoldLit.copy(alpha = .32f), fontFamily = BookSerif,
            fontSize = 9.5.sp, letterSpacing = 2.6.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 104.dp),
        )
    }
}

/**
 * Draws [path] as if it were pressed into the leather: a dark line below and
 * right, a bright line above and left, the metal between. This is the whole
 * difference between tooled and printed.
 */

/**
 * Gold leaf rather than golden-yellow paint.
 *
 * Metal is not one colour: it has sharp near-white catches where the light
 * strikes and burnished bronze where it falls away. A flat fill is the single
 * clearest tell of a vector illustration, so every stroke on this board takes
 * this brush, angled across the cover so the catches line up as though one
 * light source were above and to the left.
 */
private fun DrawScope.goldLeaf(): Brush = Brush.linearGradient(
    0f to Color(0xFF6A4A12),
    .12f to Gold,
    .22f to Color(0xFFFFF8DC),
    .34f to Gold,
    .52f to Color(0xFF8A6A1E),
    .66f to Color(0xFFFFF3C4),
    .80f to Gold,
    1f to Color(0xFF5C3F0E),
    start = Offset(0f, 0f),
    end = Offset(size.width * .55f, size.height * .42f),
)

/** Mottled hide rather than a flat wash. */
private fun DrawScope.grain() {
    val w = size.width
    val h = size.height
    // pores, scattered deterministically so it never shimmers between frames
    var seed = 20250806
    fun rnd(): Float {
        seed = seed * 1103515245 + 12345
        return ((seed ushr 16) and 0x7fff) / 32767f
    }
    /* Pebbled grain. The first version scattered 26 large translucent
       circles, which at this scale read as abstract watermark swirls — a
       digital pattern, not hide. Fine pores plus short broken strokes break
       up the gradient instead. */
    for (i in 0 until 5200) {
        val x = rnd() * w
        val y = rnd() * h
        val r = .5f + rnd() * 1.05f
        val t = rnd()
        drawCircle(
            when {
                t > .62f -> Color.Black.copy(alpha = .085f)
                t > .30f -> Color(0xFF2A1108).copy(alpha = .05f)
                else -> GoldLit.copy(alpha = .022f)
            },
            radius = r, center = Offset(x, y),
        )
    }
    // creases, so the pores sit on something
    for (i in 0 until 240) {
        val x = rnd() * w
        val y = rnd() * h
        val len = 4f + rnd() * 16f
        val lift = (rnd() - .5f) * 5f
        drawLine(
            Color.Black.copy(alpha = .045f),
            Offset(x, y), Offset(x + len, y + lift),
            strokeWidth = .9f,
        )
    }
}

/** The double gold rule and the sunken track between them. */
private fun DrawScope.borderTracks() {
    val w = size.width
    val h = size.height
    val outer = w * .045f
    val track = w * .085f

    // the raised board edge
    drawRoundRect(
        Color.Black.copy(alpha = .35f),
        topLeft = Offset(outer * .35f, outer * .35f),
        size = Size(w - outer * .7f, h - outer * .7f),
        cornerRadius = CornerRadius(w * .022f),
        style = Stroke(width = outer * .7f),
    )

    emboss(
        Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    outer, outer, w - outer, h - outer,
                    CornerRadius(w * .018f),
                ),
            )
        },
        width = 3.2f,
    )
    emboss(
        Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    track, track, w - track, h - track,
                    CornerRadius(w * .014f),
                ),
            )
        },
        width = 2f,
    )
}

/**
 * Filigree in the border track. Repeated interlocking scrolls rather than the
 * empty channel the first version left — that emptiness was most of why the
 * board read as a coloured rectangle.
 */
private fun DrawScope.filigreeBorder() {
    val w = size.width
    val h = size.height
    val a = w * .045f
    val b = w * .085f
    val mid = (a + b) / 2f
    val step = w * .075f

    fun scroll(cx: Float, cy: Float, r: Float, flip: Float) {
        val p = Path().apply {
            moveTo(cx - r, cy)
            cubicTo(cx - r, cy - r * flip, cx + r, cy - r * flip, cx + r, cy)
            cubicTo(cx + r, cy + r * .55f * flip, cx - r * .3f, cy + r * .55f * flip, cx - r * .3f, cy)
        }
        translate(.9f, 1.2f) { drawPath(p, Color(0xFF1C0A03).copy(alpha = .4f), style = Stroke(1.5f)) }
        drawPath(p, goldLeaf(), style = Stroke(1.3f), alpha = .72f)
    }

    var x = a + step * .7f
    var flip = 1f
    while (x < w - a - step * .4f) {
        scroll(x, mid, step * .28f, flip)
        scroll(x, h - mid, step * .28f, -flip)
        x += step; flip = -flip
    }
    var y = a + step * .7f
    flip = 1f
    while (y < h - a - step * .4f) {
        scroll(mid, y, step * .28f, flip)
        scroll(w - mid, y, step * .28f, -flip)
        y += step; flip = -flip
    }
}

/**
 * The central cartouche: a gothic ogee — pointed at top and bottom, shouldered
 * at the sides. The first version used an ellipse, which is what made it read
 * as a sticker rather than a stamped panel.
 */
private fun DrawScope.shield() {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h * .47f
    val hw = w * .355f
    val hh = h * .168f

    val ogee = Path().apply {
        moveTo(cx, cy - hh)
        cubicTo(cx + hw * .52f, cy - hh * .96f, cx + hw, cy - hh * .62f, cx + hw, cy)
        cubicTo(cx + hw, cy + hh * .62f, cx + hw * .52f, cy + hh * .96f, cx, cy + hh)
        cubicTo(cx - hw * .52f, cy + hh * .96f, cx - hw, cy + hh * .62f, cx - hw, cy)
        cubicTo(cx - hw, cy - hh * .62f, cx - hw * .52f, cy - hh * .96f, cx, cy - hh)
        close()
    }

    // the panel is sunk into the board
    drawPath(ogee, Brush.radialGradient(listOf(Color(0xFF7A3A1E), Color(0xFF250E05)), center = Offset(cx, cy - hh * .3f), radius = hw * 2.1f))
    emboss(ogee, width = 2.6f)

    // an inner keeper line
    val inner = Path().apply {
        val iw = hw * .88f
        val ih = hh * .88f
        moveTo(cx, cy - ih)
        cubicTo(cx + iw * .52f, cy - ih * .96f, cx + iw, cy - ih * .62f, cx + iw, cy)
        cubicTo(cx + iw, cy + ih * .62f, cx + iw * .52f, cy + ih * .96f, cx, cy + ih)
        cubicTo(cx - iw * .52f, cy + ih * .96f, cx - iw, cy + ih * .62f, cx - iw, cy)
        cubicTo(cx - iw, cy - ih * .62f, cx - iw * .52f, cy - ih * .96f, cx, cy - ih)
        close()
    }
    drawPath(inner, goldLeaf(), style = Stroke(1f), alpha = .55f)

    // a fleur at the point, top and bottom
    listOf(cy - hh to -1f, cy + hh to 1f).forEach { (at, dir) ->
        val s = w * .026f
        val p = Path().apply {
            moveTo(cx, at + dir * s * 1.5f)
            lineTo(cx - s, at - dir * s * .2f)
            lineTo(cx, at - dir * s * .9f)
            lineTo(cx + s, at - dir * s * .2f)
            close()
        }
        drawPath(p, goldLeaf())
        drawPath(p, GoldLit.copy(alpha = .45f), style = Stroke(.8f))
    }

    // the cross, standing above the cartouche
    val crossY = cy - hh - h * .055f
    val stem = h * .062f
    val arm = w * .062f
    val cross = Path().apply {
        addRect(androidx.compose.ui.geometry.Rect(cx - w * .0055f, crossY - stem * .62f, cx + w * .0055f, crossY + stem * .38f))
        addRect(androidx.compose.ui.geometry.Rect(cx - arm / 2f, crossY - stem * .16f, cx + arm / 2f, crossY - stem * .16f + h * .009f))
    }
    translate(1.4f, 2f) { drawPath(cross, Color(0xFF1C0A03).copy(alpha = .55f)) }
    drawPath(cross, goldLeaf())
    drawPath(cross, GoldLit.copy(alpha = .3f), style = Stroke(.9f))
}

/** Corner ornaments in the outer track. */
private fun DrawScope.cornerFleurons() {
    val w = size.width
    val h = size.height
    val a = w * .045f
    val r = w * .062f

    listOf(
        Triple(a, a, 0f),
        Triple(w - a, a, 90f),
        Triple(w - a, h - a, 180f),
        Triple(a, h - a, 270f),
    ).forEach { (x, y, rot) ->
        val p = Path()
        val rad = Math.toRadians(rot.toDouble())
        fun at(dx: Float, dy: Float): Offset {
            val c = cos(rad).toFloat()
            val s = sin(rad).toFloat()
            return Offset(x + dx * c - dy * s, y + dx * s + dy * c)
        }
        val o = at(r * .1f, r * .1f)
        p.moveTo(o.x, o.y)
        val c1 = at(r * .95f, r * .12f)
        val c2 = at(r * .55f, r * .95f)
        val e = at(r * .12f, r * .95f)
        p.cubicTo(c1.x, c1.y, c2.x, c2.y, e.x, e.y)
        translate(.9f, 1.2f) { drawPath(p, Color(0xFF1C0A03).copy(alpha = .45f), style = Stroke(1.6f)) }
        drawPath(p, goldLeaf(), style = Stroke(1.4f), alpha = .8f)
    }
}

/** The gilt page block down the fore edge, and the ribbons at the tail. */
private fun DrawScope.giltAndRibbons() {
    val w = size.width
    val h = size.height

    val edge = w * .05f
    drawRect(
        Brush.horizontalGradient(
            0f to Color(0xFF8A6526), .22f to Color(0xFFE6CA65),
            .5f to Color(0xFFF6E9B4), .78f to Color(0xFFB8860B), 1f to Color(0xFF6A4C1B),
        ),
        topLeft = Offset(w - edge, h * .028f),
        size = Size(edge, h - h * .056f),
    )
    // leaves catching the light
    var y = h * .04f
    while (y < h * .96f) {
        drawLine(
            Color(0xFF8A6526).copy(alpha = .35f),
            Offset(w - edge, y), Offset(w, y), strokeWidth = .8f,
        )
        y += 3.4f
    }

    /* Satin, not bars. Straight-sided rectangles with a flat fill were the
       last thing on the board still reading as vector: real ribbon catches a
       highlight down its length, hangs slightly off-square, and does not end
       in a machined edge. */
    listOf(
        Triple(.27f, Color(0xFFDAA520), .052f),
        Triple(.355f, Color(0xFF8B0000), .078f),
        Triple(.44f, Color(0xFF141C4B), .063f),
    ).forEach { (at, colour, length) ->
        val rw = w * .027f
        val drop = h * length
        val top = h - drop
        val lean = rw * .28f * (if (at < .35f) -1f else 1f)

        val ribbon = Path().apply {
            moveTo(w * at, top)
            lineTo(w * at + rw, top)
            // the hanging edge drifts as fabric does
            cubicTo(
                w * at + rw + lean * .6f, top + drop * .45f,
                w * at + rw + lean, top + drop * .78f,
                w * at + rw + lean, h,
            )
            // a soft swallowtail rather than a machined notch
            lineTo(w * at + rw * .62f + lean, h - h * .011f)
            lineTo(w * at + lean * .8f, h)
            cubicTo(
                w * at + lean * .8f, top + drop * .78f,
                w * at + lean * .5f, top + drop * .45f,
                w * at, top,
            )
            close()
        }
        drawPath(
            ribbon,
            Brush.horizontalGradient(
                0f to colour.copy(alpha = .62f),
                .26f to lighten(colour),
                .5f to colour,
                .78f to darken(colour),
                1f to colour.copy(alpha = .7f),
                startX = w * at,
                endX = w * at + rw + lean,
            ),
        )
        // the fold where it leaves the block
        drawLine(
            Color.Black.copy(alpha = .35f),
            Offset(w * at, top + 1f), Offset(w * at + rw, top + 1f),
            strokeWidth = 1.6f,
        )
    }
}

private fun lighten(c: Color) = Color(
    (c.red + (1f - c.red) * .45f), (c.green + (1f - c.green) * .45f),
    (c.blue + (1f - c.blue) * .45f), c.alpha,
)

private fun darken(c: Color) = Color(c.red * .55f, c.green * .55f, c.blue * .55f, c.alpha)

/**
 * A title pressed into the board.
 *
 * Compose has no inner-bevel on text, but the effect is only three passes: a
 * shadow cast down-right into the groove, a bright lip up-left where the near
 * edge catches, and the leaf between. Same principle as [emboss], applied to
 * glyphs instead of paths.
 */
@Composable
private fun StampedText(
    text: String,
    size: androidx.compose.ui.unit.TextUnit,
    tracking: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight,
) {
    // the brush and colour overloads of TextStyle are distinct constructors,
    // so the shared settings are applied on top of each rather than passed in
    val shared = TextStyle(
        fontFamily = RomanCaps,
        fontSize = size,
        fontWeight = weight,
        letterSpacing = tracking,
        textAlign = TextAlign.Center,
    )

    /* The groove scales with the type. A fixed 2dp offset is a shadow at
       33sp and a doubled line at 9.5sp — the subtitle came out looking
       printed twice. */
    val depth = (size.value / 33f).coerceIn(.34f, 1f)

    Box(contentAlignment = Alignment.Center) {
        Text(
            text, style = shared.copy(color = Color(0xFF160700).copy(alpha = .9f)),
            modifier = Modifier.offset(x = (1.6f * depth).dp, y = (2.2f * depth).dp),
        )
        Text(
            text, style = shared.copy(color = GoldLit.copy(alpha = .55f)),
            modifier = Modifier.offset(x = (-1.1f * depth).dp, y = (-1.5f * depth).dp),
        )
        Text(
            text,
            style = shared.merge(
                TextStyle(
                    brush = Brush.linearGradient(
                        0f to Color(0xFF7A5A15),
                        .16f to Gold,
                        .3f to Color(0xFFFFF8DC),
                        .46f to Gold,
                        .64f to Color(0xFF8A6A1E),
                        .82f to Color(0xFFFFF3C4),
                        1f to Color(0xFF6A4A12),
                    ),
                ),
            ),
        )
    }
}

/**
 * The board: hide, tooling, filigree — and optionally the centrepiece.
 *
 * Both faces of the book need this. The cover is the board with its cartouche
 * and cross; the open book shows the same board above and below the page
 * block, because that is what you are looking at when a book is open in your
 * hands. Drawing plain brown there made the leaves look pasted onto card.
 *
 * Rendered once into a bitmap and blitted. Drawing it live cost about 5,500
 * shapes a frame — 5,200 leather pores, the filigree, and every gold line
 * three times over for the emboss — and the cover animates open over 760ms.
 * It ANR'd the app. None of it changes between frames.
 */
@Composable
fun TooledBoard(centrepiece: Boolean, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val layout = LocalLayoutDirection.current
    var px by remember { mutableStateOf(IntSize.Zero) }

    val board = remember(px, centrepiece) {
        if (px.width <= 0 || px.height <= 0) null
        else ImageBitmap(px.width, px.height).also { bitmap ->
            CanvasDrawScope().draw(
                density, layout, androidx.compose.ui.graphics.Canvas(bitmap),
                Size(px.width.toFloat(), px.height.toFloat()),
            ) {
                drawRect(
                    Brush.radialGradient(
                        0f to BoardLit, .45f to Board, 1f to BoardDeep,
                        radius = size.maxDimension * .9f,
                    ),
                )
                grain()
                borderTracks()
                filigreeBorder()
                cornerFleurons()
                if (centrepiece) {
                    shield()
                    giltAndRibbons()
                }
            }
        }
    }

    Canvas(modifier.fillMaxSize().onSizeChanged { px = it }) {
        board?.let { drawImage(it) }
    }
}
