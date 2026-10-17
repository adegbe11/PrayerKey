package com.prayerkey.manna.ui.book

import com.prayerkey.manna.resources.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.BookSerif
import kotlin.random.Random

/** Brushed silver: bright where the light catches, grey where it falls away. */
private val Silver = Brush.linearGradient(
    0f to Color(0xFF8E8E93),
    .3f to Color(0xFFF2F2F7),
    .55f to Color(0xFFC7C7CC),
    .8f to Color(0xFFFFFFFF),
    1f to Color(0xFF9A9AA0),
)

/**
 * The black edition's board: black leather, a cross laid on in one white brush
 * stroke, and the title in silver at the foot.
 *
 * The leather and the title are drawn, so they are sharp at any size - it is
 * the same board on the Home card and full screen; the brushed cross is a
 * small rendered image. The grain uses a fixed seed, so the board looks the
 * same every time it is opened, the way a real one would.
 */
@Composable
fun MonochromeCover(onOpen: () -> Unit, modifier: Modifier = Modifier, full: Boolean = true) {
    // light icons over the black board
    if (full) BarIcons(dark = false)
    BoxWithConstraints(
        modifier.fillMaxSize()
            .background(Color(0xFF0B0B0C))
            .clickable(onClick = onOpen),
    ) {
        val w = maxWidth
        Canvas(Modifier.fillMaxSize()) { leather() }
        /* The cross is one dry white brush stroke - a solid body of paint,
           bristle-broken edges, the beam thinning as the brush lifts. It is
           rendered once as an image: drawn live from lines it read as a
           barcode, and paint needs hundreds of overlapping bristles. */
        androidx.compose.foundation.Image(
            org.jetbrains.compose.resources.painterResource(Res.drawable.mono_cross),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        // the title, set at the foot of the board on the right
        Column(
            Modifier.align(Alignment.BottomEnd)
                .padding(end = w * .09f, bottom = w * .14f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "The",
                style = TextStyle(brush = Silver),
                fontFamily = FontFamily.Cursive, fontStyle = FontStyle.Italic,
                fontSize = (w.value * .085f).sp,
                modifier = Modifier.align(Alignment.End).padding(end = w * .06f),
            )
            Text(
                "BIBLE",
                style = TextStyle(brush = Silver, letterSpacing = (w.value * .006f).sp),
                fontFamily = BookSerif, fontWeight = FontWeight.Bold,
                fontSize = (w.value * .15f).sp, lineHeight = (w.value * .15f).sp,
            )
            Text(
                "KING JAMES VERSION",
                style = TextStyle(brush = Silver, letterSpacing = (w.value * .006f).sp),
                fontFamily = BookSerif, textAlign = TextAlign.Center,
                fontSize = (w.value * .029f).sp,
                modifier = Modifier.padding(top = w * .02f),
            )
        }
    }
}

/** Black hide: a soft light from above left, and a fine pebbled grain. */
private fun DrawScope.leather() {
    drawRect(
        Brush.radialGradient(
            listOf(Color(0xFF232325), Color(0xFF0B0B0C), Color(0xFF040404)),
            center = Offset(size.width * .3f, size.height * .25f),
            radius = size.maxDimension * .95f,
        ),
    )
    val grain = Random(7)
    val dots = (size.width * size.height / 260f).toInt().coerceAtMost(9000)
    repeat(dots) {
        val x = grain.nextFloat() * size.width
        val y = grain.nextFloat() * size.height
        val light = grain.nextBoolean()
        drawCircle(
            if (light) Color.White.copy(alpha = .035f) else Color.Black.copy(alpha = .28f),
            radius = 0.6f + grain.nextFloat() * 1.4f,
            center = Offset(x, y),
        )
    }
    // the board's bevelled edge
    drawRect(Color.White.copy(alpha = .05f), size = Size(size.width, 2f))
    drawRect(Color.Black.copy(alpha = .5f), topLeft = Offset(0f, size.height - 3f), size = Size(size.width, 3f))
}

/** The black Bible on Home: its cover, and nothing written over it. */
@Composable
fun MonochromeBibleCard(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .shadow(18.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp)),
    ) {
        MonochromeCover(onOpen = onOpen, full = false)
    }
}
