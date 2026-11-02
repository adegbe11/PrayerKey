package com.prayerkey.manna.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.platform.screenHeightDp
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import com.prayerkey.manna.ui.theme.pressable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** One thing on Home below the two daily cards. */
data class HomeItem(
    val eyebrow: String,
    val title: String,
    val art: DrawableResource,
    /** How long it takes, shown with a small play mark. */
    val meta: String? = null,
    /** 0..1 for something already under way; null for something not started. */
    val progress: Float? = null,
    /** "Day 4 of 21" for something under way. */
    val day: String? = null,
    /** The one row that matters right now, drawn dark with a Pray button. */
    val featured: Boolean = false,
    val open: () -> Unit,
)

/**
 * A heading and rows that share one shape: words on the left, a picture on
 * the right. Long lists show [visible] rows and a See all.
 */
@Composable
fun HomeSection(title: String, items: List<HomeItem>, visible: Int = items.size) {
    if (items.isEmpty()) return
    var all by rememberSaveable(title) { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            title, Modifier.padding(start = 4.dp, top = 30.dp, bottom = 4.dp),
            fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 27.sp, color = Pk.Charcoal,
        )
        (if (all) items else items.take(visible)).forEach { item ->
            Rise { if (item.featured) FeaturedRow(item) else HomeRow(item) }
        }
        if (items.size > visible) {
            Text(
                if (all) "Show less" else "See all",
                Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .border(1.dp, Pk.Oxblood.copy(alpha = .22f), RoundedCornerShape(99.dp))
                    .clickable { all = !all }.padding(horizontal = 26.dp, vertical = 11.dp),
                fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Pk.Oxblood,
            )
        }
    }
}

/** Fades and lifts its content in the first time it scrolls onto the screen. */
@Composable
private fun Rise(content: @Composable () -> Unit) {
    var seen by remember { mutableStateOf(false) }
    val screen = with(LocalDensity.current) { screenHeightDp().dp.toPx() }
    val t by animateFloatAsState(if (seen) 1f else 0f, tween(560, easing = FastOutSlowInEasing))
    Box(
        // watched only until it has risen, so a settled row costs nothing to scroll
        (if (seen) Modifier else Modifier.onGloballyPositioned { if (it.boundsInWindow().top < screen * .96f) seen = true })
            .graphicsLayer {
                alpha = t
                translationY = (1f - t) * 22.dp.toPx()
            },
    ) { content() }
}

@Composable
private fun Eyebrow(text: String, color: Color) {
    Text(
        text.uppercase(), fontFamily = UtilitySans, fontWeight = FontWeight.Bold,
        fontSize = 11.sp, letterSpacing = 1.4.sp, color = color, maxLines = 1,
    )
}

@Composable
private fun HomeRow(item: HomeItem) {
    Surface(
        Modifier.fillMaxWidth().pressable(.98f).clip(RoundedCornerShape(24.dp)).clickable(onClick = item.open),
        shape = RoundedCornerShape(24.dp), color = Pk.Crisp, shadowElevation = 2.dp,
        border = BorderStroke(.5.dp, Pk.Charcoal.copy(alpha = .06f)),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(start = 8.dp, end = 12.dp)) {
                Eyebrow(item.day ?: item.eyebrow, Pk.Oxblood)
                Text(
                    item.title, Modifier.padding(top = 6.dp),
                    fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 24.sp,
                    color = Pk.Charcoal, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                if (item.meta != null && item.progress == null) {
                    Row(Modifier.padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, null, tint = Pk.Charcoal.copy(alpha = .45f), modifier = Modifier.size(14.dp))
                        Text(item.meta, Modifier.padding(start = 3.dp), fontFamily = UtilitySans, fontSize = 13.sp, color = Pk.Charcoal.copy(alpha = .5f))
                    }
                }
            }
            Box(Modifier.size(88.dp)) {
                Image(
                    painterResource(item.art), null,
                    Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop,
                )
                item.progress?.let { ProgressRing(it, Modifier.align(Alignment.BottomEnd).padding(6.dp)) }
            }
        }
    }
}

/** How far through, as a gold arc on a small dark disc in the picture's corner. */
@Composable
private fun ProgressRing(progress: Float, modifier: Modifier) {
    Box(modifier.size(30.dp).background(Pk.DeepDark.copy(alpha = .72f), CircleShape), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(22.dp)) {
            val stroke = 2.6.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(Color.White.copy(alpha = .22f), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(
                Pk.GoldLight, -90f, 360f * progress.coerceIn(.04f, 1f), false, Offset(inset, inset), arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
        }
    }
}

/** The prayer for this hour: dark, with the picture bleeding in from the right. */
@Composable
private fun FeaturedRow(item: HomeItem) {
    Box(
        Modifier.fillMaxWidth().height(150.dp).pressable(.98f)
            .clip(RoundedCornerShape(26.dp)).clickable(onClick = item.open)
            .background(Pk.DeepDark),
    ) {
        Image(
            painterResource(item.art), null,
            Modifier.align(Alignment.CenterEnd).width(210.dp).height(150.dp),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    0f to Pk.DeepDark, .45f to Pk.DeepDark.copy(alpha = .92f), 1f to Pk.DeepDark.copy(alpha = .05f),
                ),
            ),
        )
        Column(Modifier.align(Alignment.CenterStart).padding(horizontal = 22.dp)) {
            Eyebrow(item.eyebrow, Pk.GoldLight)
            Text(
                item.title, Modifier.padding(top = 7.dp),
                fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.sp,
                color = Color.White, maxLines = 1,
            )
            Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(RoundedCornerShape(99.dp)).background(Pk.GoldLight)
                        .padding(start = 12.dp, end = 16.dp, top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.PlayArrow, null, tint = Pk.DeepDark, modifier = Modifier.size(16.dp))
                    Text("Pray", Modifier.padding(start = 4.dp), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Pk.DeepDark)
                }
                item.meta?.let {
                    Text(it, Modifier.padding(start = 12.dp), fontFamily = UtilitySans, fontSize = 13.sp, color = Color.White.copy(alpha = .6f))
                }
            }
        }
    }
}
