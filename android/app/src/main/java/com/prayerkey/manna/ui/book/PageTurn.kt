package com.prayerkey.manna.ui.book

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

/**
 * A page being turned.
 *
 * The first version of this rotated the whole leaf about the spine with
 * `rotationY`, capped at 70°, and sprang back. That reads as a tilt, not a
 * turn: the sheet never goes over, nothing is revealed underneath, and the
 * text simply changes when it snaps flat.
 *
 * A real turn is not a 3D rotation at all — it is a **reflection**. Fold a
 * sheet of paper and the part beyond the crease is that same sheet mirrored
 * about the crease, showing its reverse. So:
 *
 *  - [under] is drawn whole. It is the page being revealed.
 *  - [folding] is drawn twice: once clipped to the part still lying flat,
 *    and once mirrored about the fold line, clipped to where the flap lands.
 *    That second copy is the back of the sheet, and it is geometrically exact
 *    rather than an approximation of one.
 *  - A shadow falls from the crease onto the page beneath, and the flap
 *    carries its own shading — darkest where it lifts, catching light at its
 *    free edge.
 *
 * The crease here is straight and vertical, which is what a bound book held
 * in one hand does. The corner-curl of a paperback needs the crease to tilt,
 * and that buys a second axis of geometry for something a Bible does not do.
 *
 * Both pages are recorded into graphics layers and never drawn directly, so
 * the composition is laid out once and the whole turn is draw-phase work.
 *
 * @param progress how far the sheet has gone over: 0 is flat, 1 is fully
 *   turned. A fraction rather than a pixel crease on purpose — the caller
 *   measures the text column, this measures the canvas, and passing pixels
 *   between the two put the crease in the wrong place.
 */
@Composable
fun FoldedPage(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    under: @Composable () -> Unit,
    folding: @Composable () -> Unit,
) {
    val foldingLayer = rememberGraphicsLayer()
    val underLayer = rememberGraphicsLayer()

    Box(modifier.fillMaxSize()) {
        /* Recorded, not shown. The Canvas below decides where each one
           lands, and drawing them here as well would put a flat copy
           underneath the folded one. */
        Box(
            Modifier.fillMaxSize().drawWithContent {
                underLayer.record { this@drawWithContent.drawContent() }
            },
        ) { under() }

        Box(
            Modifier.fillMaxSize().drawWithContent {
                foldingLayer.record { this@drawWithContent.drawContent() }
            },
        ) { folding() }

        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // read inside the draw phase, so a moving fold never recomposes
            val fold = w * (1f - progress().coerceIn(0f, 1f))

            drawLayer(underLayer)

            // the crease casts onto the page beneath, ahead of the fold
            if (fold < w) {
                val reach = (w * .07f).coerceAtMost(w - fold)
                drawRect(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = .32f),
                        1f to Color.Transparent,
                        startX = fold,
                        endX = fold + reach,
                    ),
                    topLeft = Offset(fold, 0f),
                    size = Size(reach, h),
                )
            }

            // the part of the sheet still lying flat
            clipRect(0f, 0f, fold, h) { drawLayer(foldingLayer) }

            if (fold < w) {
                /* The flap. The sheet from [fold, w] reflects about the
                   crease and lands on [2·fold − w, fold], so its content is
                   this same layer mirrored horizontally about the crease. */
                val flapLeft = (2f * fold - w).coerceAtLeast(0f)
                clipRect(flapLeft, 0f, fold, h) {
                    scale(scaleX = -1f, scaleY = 1f, pivot = Offset(fold, h / 2f)) {
                        drawLayer(foldingLayer)
                    }
                    /* The reverse of a leaf is never as bright as its face:
                       the ink of the other side only shows through the
                       paper. */
                    drawRect(Color(0xFFEDE2C6).copy(alpha = .55f))
                    drawRect(
                        Brush.horizontalGradient(
                            0f to Color.White.copy(alpha = .10f),
                            .45f to Color.Black.copy(alpha = .06f),
                            1f to Color.Black.copy(alpha = .26f),
                            startX = flapLeft,
                            endX = fold,
                        ),
                    )
                }

                // the crease itself, where the paper doubles over
                drawLine(
                    Color(0xFF6B5A34).copy(alpha = .55f),
                    Offset(fold, 0f), Offset(fold, h),
                    strokeWidth = 1.3f,
                )
            }
        }
    }
}
