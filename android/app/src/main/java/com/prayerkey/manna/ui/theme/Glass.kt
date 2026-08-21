package com.prayerkey.manna.ui.theme

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
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
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
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
    blurRadius: androidx.compose.ui.unit.Dp = 26.dp,
    lens: Float = 1.07f,
    tint: Color = Pk.Oxblood.copy(alpha = .05f),
): Modifier = composed {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    val blurred = remember(blurRadius, density) {
        if (Build.VERSION.SDK_INT >= 31) {
            val px = with(density) { blurRadius.toPx() }
            RenderEffect.createBlurEffect(px, px, Shader.TileMode.CLAMP).asComposeRenderEffect()
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
                    scale(lens * 2.4f, lens * 2.4f, pivot = center)
                    translate(-origin.x, -origin.y)
                }) {
                    drawLayer(backdrop.frosted)
                }
                drawRect(
                    brush = Brush.radialGradient(
                        0f to Color.Black,
                        .58f to Color.Black,
                        1f to Color.Transparent,
                        center = center,
                        radius = maxOf(size.width, size.height) * .78f,
                    ),
                    blendMode = BlendMode.DstOut,
                )

                if (tint.alpha > 0f) drawRect(tint)

                /* The body of the glass: brighter where light enters, dimmer
                   where it leaves. Without this the pane has no thickness. */
                drawRect(
                    Brush.linearGradient(
                        0f to Color.White.copy(alpha = .20f),
                        .5f to Color.White.copy(alpha = .05f),
                        1f to Color.White.copy(alpha = .12f),
                    ),
                )

                // one specular, upper left, where the light is
                drawRect(
                    Brush.radialGradient(
                        0f to Color.White.copy(alpha = .38f),
                        1f to Color.Transparent,
                        center = Offset(size.width * .3f, size.height * .16f),
                        radius = size.minDimension * 1.1f,
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
 * Two hairlines and two faint colour fringes. The fringes are warm on one side
 * and cool on the other, which is what a thick transparent edge does to light
 * passing through it, and it is the detail that stops the rim looking drawn on.
 */
private fun ContentDrawScope.drawGlassEdge(size: Size, path: Path) {
    clipPath(path) {
        val w = 1.5.dp.toPx()
        // lit top face
        drawRect(
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = .85f),
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
