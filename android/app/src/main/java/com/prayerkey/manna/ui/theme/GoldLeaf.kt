package com.prayerkey.manna.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * Gold leaf, and things stamped in it.
 *
 * This lived inside the Bible cover, which was fine while the cover was the
 * only tooled thing in the app. Home wants the same treatment on its own
 * title and frame, and two copies of a look this specific drift apart within
 * a week — so it moved here and the cover reads from it.
 *
 * The governing idea: metal is not one colour. Real leaf has near-white
 * catches where the light strikes and burnished bronze where it falls away,
 * and every tooled line has a dark groove below it and a bright lip above.
 * Flat yellow with no groove is the clearest possible tell of a vector
 * illustration pretending to be a bound book.
 */
val Leaf = Color(0xFF4D84EA)
val LeafLit = Color(0xFFC9DEFF)
val LeafDim = Color(0xFF174EA6)
private val Groove = Color(0xFF1C0A03)

/** A metallic sweep, lit from above and to the left. */
fun DrawScope.goldLeaf(): Brush = Brush.linearGradient(
    0f to Color(0xFF6A4A12),
    .12f to Leaf,
    .22f to Color(0xFFFFF8DC),
    .34f to Leaf,
    .52f to Color(0xFF8A6A1E),
    .66f to Color(0xFFFFF3C4),
    .80f to Leaf,
    1f to Color(0xFF5C3F0E),
    start = Offset(0f, 0f),
    end = Offset(size.width * .55f, size.height * .42f),
)

/**
 * Draws [path] as though pressed into hide: a dark line below and right, a
 * bright lip above and left, the metal between.
 */
fun DrawScope.emboss(path: Path, width: Float) {
    translate(1.6f, 2.2f) { drawPath(path, Groove.copy(alpha = .55f), style = Stroke(width * 1.15f)) }
    translate(-1.2f, -1.6f) { drawPath(path, LeafLit.copy(alpha = .35f), style = Stroke(width * .9f)) }
    drawPath(path, goldLeaf(), style = Stroke(width))
}

/**
 * A gothic ogee — pointed top and bottom, shouldered at the sides. The shape
 * a stamped panel takes on a bound board, and the reason the cover's
 * centrepiece stopped reading as a sticker when it replaced an ellipse.
 */
fun ogee(cx: Float, cy: Float, hw: Float, hh: Float): Path = Path().apply {
    moveTo(cx, cy - hh)
    cubicTo(cx + hw * .52f, cy - hh * .96f, cx + hw, cy - hh * .62f, cx + hw, cy)
    cubicTo(cx + hw, cy + hh * .62f, cx + hw * .52f, cy + hh * .96f, cx, cy + hh)
    cubicTo(cx - hw * .52f, cy + hh * .96f, cx - hw, cy + hh * .62f, cx - hw, cy)
    cubicTo(cx - hw, cy - hh * .62f, cx - hw * .52f, cy - hh * .96f, cx, cy - hh)
    close()
}

/**
 * The double rule of a tooled board: a blind-stamped outer edge, then two
 * gold lines with a sunken track between them.
 */
fun DrawScope.tooledBorder(outer: Float, track: Float, radius: Float) {
    val w = size.width
    val h = size.height

    drawRoundRect(
        Color.Black.copy(alpha = .30f),
        topLeft = Offset(outer * .35f, outer * .35f),
        size = androidx.compose.ui.geometry.Size(w - outer * .7f, h - outer * .7f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 1.2f),
        style = Stroke(width = outer * .7f),
    )
    emboss(
        Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    outer, outer, w - outer, h - outer,
                    androidx.compose.ui.geometry.CornerRadius(radius),
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
                    androidx.compose.ui.geometry.CornerRadius(radius * .85f),
                ),
            )
        },
        width = 2f,
    )
}

/**
 * Type stamped in leaf.
 *
 * Compose has no inner bevel for text, but the effect is only three passes: a
 * shadow cast down-right into the groove, a bright lip up-left on the near
 * edge, and the metal between. A gradient on the glyphs alone reads as bright
 * paint sitting on top.
 */
@Composable
fun StampedGold(
    text: String,
    size: TextUnit,
    tracking: TextUnit,
    weight: FontWeight = FontWeight.Bold,
    family: FontFamily = RomanCaps,
    modifier: Modifier = Modifier,
) {
    // the groove scales with the type: a fixed 2dp offset is a shadow at 33sp
    // and a doubled line at 9sp
    val depth = (size.value / 33f).coerceIn(.34f, 1f)
    val shared = TextStyle(
        fontFamily = family,
        fontSize = size,
        fontWeight = weight,
        letterSpacing = tracking,
        textAlign = TextAlign.Center,
    )

    /* Only the metal copy is real text. The groove and the lip are paint, and
       leaving them in the tree made a screen reader say "MANNA" three times
       on every screen this is used. */
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text, style = shared.copy(color = Color(0xFF160700).copy(alpha = .9f)),
            modifier = Modifier
                .offset(x = (1.6f * depth).dp, y = (2.2f * depth).dp)
                .clearAndSetSemantics {},
        )
        Text(
            text, style = shared.copy(color = LeafLit.copy(alpha = .55f)),
            modifier = Modifier
                .offset(x = (-1.1f * depth).dp, y = (-1.5f * depth).dp)
                .clearAndSetSemantics {},
        )
        Text(
            text,
            style = shared.merge(
                TextStyle(
                    brush = Brush.linearGradient(
                        0f to LeafDim,
                        .16f to Leaf,
                        .3f to Color(0xFFFFF8DC),
                        .46f to Leaf,
                        .64f to Color(0xFF8A6A1E),
                        .82f to Color(0xFFFFF3C4),
                        1f to Color(0xFF6A4A12),
                    ),
                ),
            ),
        )
    }
}
