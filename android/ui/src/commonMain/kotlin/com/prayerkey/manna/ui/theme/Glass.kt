package com.prayerkey.manna.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import com.prayerkey.manna.ui.platform.supportsBackdropBlur
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Glass that actually samples what is behind it.
 *
 * The usual Android approximation is a translucent fill with a border, and it
 * fails for a reason that is easy to state: the panel does not change when the
 * content beneath it changes. Real glass does. Move a photograph under it and
 * the colour under the panel moves too.
 *
 * So this captures the screen's own content into a [GraphicsLayer] and each
 * glass element re-draws that capture, offset by its own position on screen, so
 * what appears inside the panel is genuinely the pixels behind it.
 *
 * Four properties make it read as thick rather than as a blurred rectangle, and
 * all four are here:
 *
 *  1. **Refraction.** The captured backdrop is drawn slightly enlarged, so the
 *     image inside the panel is displaced outward from the centre — a lens.
 *  2. **Edge lensing.** A second, much larger copy is drawn only near the rim,
 *     so distortion is mild in the middle and heavy at the edge. This is the
 *     single thing most imitations miss, and it is what sells thickness.
 *  3. **A lit edge.** A bright inner hairline along the top and a dimmer one
 *     along the bottom — light entering the top face and bouncing off the base.
 *  4. **A specular.** One travelling highlight, brightest where the light is.
 *
 * Blur needs `RenderEffect`, which is API 31. Below that the refraction, the
 * rim and the specular all still work; only the softening is absent, so the
 * panel reads as polished glass rather than frosted. It never falls back to a
 * flat scrim.
 *
 * Colour is deliberately not introduced here. Every tint is taken from [Pk],
 * and the default is almost none: glass is supposed to show you the thing
 * behind it, not restate the brand over the top of it.
 */
/**
 * Two captures of the same content.
 *
 * [sharp] is what the screen draws. [frosted] is the same pixels with a blur
 * applied, and it exists separately because a `renderEffect` set on the layer
 * the content is drawn from blurs the content itself — the first version of
 * this blurred the entire app.
 */
class GlassBackdrop internal constructor(
    internal val sharp: GraphicsLayer,
    internal val frosted: GraphicsLayer,
)

@Composable
fun rememberGlassBackdrop(): GlassBackdrop {
    val sharp = androidx.compose.ui.graphics.rememberGraphicsLayer()
    val frosted = androidx.compose.ui.graphics.rememberGraphicsLayer()
    return remember(sharp, frosted) { GlassBackdrop(sharp, frosted) }
}

/**
 * Marks the content that glass is allowed to see.
 *
 * Put this on the scrolling body, never on the chrome. Recording the chrome
 * into the same layer would make the glass sample itself, which compounds every
 * frame into a smear.
 */
fun Modifier.glassSource(backdrop: GlassBackdrop): Modifier = drawWithContent {
    val captured = androidx.compose.ui.unit.IntSize(
        size.width.toInt().coerceAtLeast(1),
        size.height.toInt().coerceAtLeast(1),
    )
    backdrop.sharp.record(this, layoutDirection, captured) { this@drawWithContent.drawContent() }
    backdrop.frosted.record(this, layoutDirection, captured) { this@drawWithContent.drawContent() }
    drawLayer(backdrop.sharp)
}

/**
 * A pane of glass.
 *
 * [blurRadius] is the frost. [lens] is how hard the backdrop is magnified —
 * 1.06 is a light bend; past about 1.2 the displacement is visible as a shift
 * rather than as depth.
 */
fun Modifier.liquidGlass(
    backdrop: GlassBackdrop,
    shape: Shape,
    /*
     * iOS 27 retuned this material. The first version leaned on refraction -
     * you could see what was behind a pane bending through it - and text laid
     * over busy content suffered for it. Apple's fix was to diffuse complex
     * content harder and let the depth come from the edge instead.
     *
     * So the blur goes up and the lensing comes down. What is behind a pane is
     * now a suggestion of colour rather than a readable image, which is what
     * makes it safe to put words on.
     */
    blurRadius: androidx.compose.ui.unit.Dp = 34.dp,
    lens: Float = 1.04f,
    tint: Color = Pk.Oxblood.copy(alpha = .05f),
): Modifier = composed {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    val blurred = remember(blurRadius, density) {
        if (supportsBackdropBlur) {
            val px = with(density) { blurRadius.toPx() }
            BlurEffect(px, px, TileMode.Clamp)
        } else {
            null
        }
    }

    this
        .onGloballyPositioned { origin = it.positionInRoot() }
        .drawWithContent {
            val outline = shape.createOutline(size, LayoutDirection.Ltr, this)
            val path = Path().apply { addOutline(outline) }

            clipPath(path) {
                /* The backdrop, put back exactly where it was on screen and then
                   enlarged about this pane's own centre. Enlarging is the whole
                   trick: it pushes the sampled image outward, which is what a
                   lens does to what is behind it. */
                backdrop.frosted.renderEffect = blurred

                withTransform({
                    scale(lens, lens, pivot = center)
                    translate(-origin.x, -origin.y)
                }) {
                    drawLayer(backdrop.frosted)
                }

                /* Edge lensing. The same backdrop again, much larger, faded in
                   only as it approaches the rim, so the middle of the pane stays
                   readable and the border compresses hard. */
                withTransform({
                    scale(lens * 1.8f, lens * 1.8f, pivot = center)
                    translate(-origin.x, -origin.y)
                }) {
                    drawLayer(backdrop.frosted)
                }
                /* The compressed band is narrower than it was - solid through
                   72% of the pane rather than 58% - so the lensing is a rim
                   effect and the middle stays flat enough to read on. */
                drawRect(
                    brush = Brush.radialGradient(
                        0f to Color.Black,
                        .72f to Color.Black,
                        1f to Color.Transparent,
                        center = center,
                        radius = maxOf(size.width, size.height) * .78f,
                    ),
                    blendMode = BlendMode.DstOut,
                )

                if (tint.alpha > 0f) drawRect(tint)

                /* The body, kept light. An earlier version washed white over
                   the whole pane at 20%, which turned a brand-violet dock into
                   a pale lilac one — the colour value never changed, but it
                   stopped reading as the brand. Glass is meant to be tinted by
                   what it is made of, not bleached. */
                drawRect(
                    Brush.linearGradient(
                        0f to Color.White.copy(alpha = .10f),
                        .5f to Color.Transparent,
                        1f to Color.White.copy(alpha = .05f),
                    ),
                )

                /* The specular, brightened for iOS 27.
                   With less refraction happening the pane needs the highlight
                   to carry more of the impression of a curved surface, so it
                   is stronger and tighter than the diffuse version it
                   replaces. */
                drawRect(
                    Brush.radialGradient(
                        0f to Color.White.copy(alpha = .34f),
                        1f to Color.Transparent,
                        center = Offset(size.width * .3f, size.height * .14f),
                        radius = size.minDimension * .62f,
                    ),
                )
            }

            // the rim: lit top, bounce along the base, and the chromatic sides
            drawGlassEdge(size, path)

            drawContent()
        }
}

/**
 * The edge.
 *
 * A dark ring, two lit hairlines, and two faint colour fringes.
 *
 * The dark ring is the iOS 27 addition and the one that does the most work.
 * Without it a pane dissolves into whatever it sits on - which is fine over a
 * photograph, where the refraction alone separates it, and useless over a flat
 * page, where there is nothing to bend. The ring gives the glass an outside,
 * so it reads as an object resting on the page rather than a lighter patch of
 * it, and it is why iOS 27 glass works on plain backgrounds where iOS 26 glass
 * looked like a smudge.
 *
 * The fringes are warm on one side and cool on the other, which is what a thick
 * transparent edge does to light passing through it, and it is the detail that
 * stops the rim looking drawn on.
 */
private fun ContentDrawScope.drawGlassEdge(size: Size, path: Path) {
    // the dark ring, outside everything else
    drawPath(
        path,
        Color.Black.copy(alpha = .18f),
        style = Stroke(width = 1.2.dp.toPx()),
    )
    clipPath(path) {
        val w = 1.5.dp.toPx()
        // lit top face
        drawRect(
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = .95f),
                1f to Color.Transparent,
            ),
            size = Size(size.width, w * 2f),
        )
        // bounce off the base
        drawRect(
            Brush.verticalGradient(
                0f to Color.Transparent,
                1f to Color.White.copy(alpha = .30f),
            ),
            topLeft = Offset(0f, size.height - w * 2f),
            size = Size(size.width, w * 2f),
        )
        // chromatic fringes
        drawRect(
            Color(0xFFFFBEDC).copy(alpha = .30f),
            size = Size(w, size.height),
        )
        drawRect(
            Color(0xFFB4DCFF).copy(alpha = .30f),
            topLeft = Offset(size.width - w, 0f),
            size = Size(w, size.height),
        )
    }
}

/**
 * A lit surface — an opaque card that still has a material.
 *
 * The reference card is not flat white. It is a gradient, with a bright inner
 * hairline along the top where light lands, a dimmer one along the base where
 * it bounces, a warm fringe down one side and a cool one down the other, and a
 * single diagonal specular sweeping across the face. Those five things are the
 * whole difference between a card that looks pressed out of paper and one that
 * looks like a physical object.
 *
 * This is not [liquidGlass]. Glass shows you what is behind it; a card does
 * not. It only has to catch the light.
 */
fun Modifier.litSurface(
    shape: Shape,
    base: Color = Color.White,
): Modifier = drawWithContent {
    val outline = shape.createOutline(size, LayoutDirection.Ltr, this)
    val path = Path().apply { addOutline(outline) }

    clipPath(path) {
        // the face, brighter at the top-left corner the light comes from
        drawRect(
            Brush.linearGradient(
                0f to base,
                1f to base.copy(alpha = .82f).compositeOverWhite(),
            ),
        )
        // one specular band, running across the face
        drawRect(
            Brush.linearGradient(
                0f to Color.Transparent,
                .34f to Color.White.copy(alpha = .55f),
                .52f to Color.Transparent,
                start = Offset(0f, size.height),
                end = Offset(size.width, 0f),
            ),
        )
    }
    drawGlassEdge(size, path)
    drawContent()
}

/** Cards sit on a light page, so a translucent white reads as this. */
private fun Color.compositeOverWhite(): Color {
    val a = alpha
    fun mix(c: Float) = c * a + 1f * (1f - a)
    return Color(mix(red), mix(green), mix(blue), 1f)
}

/**
 * The icon tile on a rhythm row.
 *
 * Flat fills read as stickers. This gives the tile the same lit top edge the
 * card has, a tint pooling at its base, and — when the step is done — a violet
 * glow underneath, which is what makes a completed row legible from across the
 * screen rather than only on inspection.
 */
fun Modifier.litTile(shape: Shape, done: Boolean): Modifier = drawWithContent {
    val outline = shape.createOutline(size, LayoutDirection.Ltr, this)
    val path = Path().apply { addOutline(outline) }
    clipPath(path) {
        drawRect(
            if (done) {
                Brush.linearGradient(listOf(Pk.VioletLit, Pk.Oxblood))
            } else {
                Brush.linearGradient(
                    listOf(Pk.Oxblood.copy(alpha = .13f), Pk.Oxblood.copy(alpha = .05f)),
                )
            },
        )
        // light entering the top face
        drawRect(
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = if (done) .40f else .85f),
                .34f to Color.Transparent,
            ),
        )
        // the tint settling at the base
        drawRect(
            Brush.verticalGradient(
                .62f to Color.Transparent,
                1f to Pk.Oxblood.copy(alpha = if (done) .30f else .14f),
            ),
        )
    }
    drawContent()
}
