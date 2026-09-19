package com.prayerkey.manna.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.prayerkey.manna.ui.theme.Pk
import kotlin.random.Random

/** Actual UI on a drawn paper surface: no rasterised text, no repeating image seams. */
@Composable
internal fun JournalPaper(content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Pk.DeepDark).padding(start = 7.dp, end = 9.dp, top = 5.dp, bottom = 8.dp)) {
        Box(Modifier.fillMaxSize().padding(start = 3.dp, top = 3.dp)
            .shadow(12.dp, RoundedCornerShape(3.dp, 12.dp, 12.dp, 3.dp))
            .background(Pk.JournalEdge, RoundedCornerShape(3.dp, 12.dp, 12.dp, 3.dp)))
        Box(Modifier.fillMaxSize().padding(end = 5.dp, bottom = 5.dp)
            .background(Pk.JournalPaper, RoundedCornerShape(2.dp, 9.dp, 9.dp, 2.dp))
            .drawWithCache {
                val rng = Random(73)
                val fibers = List(1800) { Offset(rng.nextFloat() * size.width, rng.nextFloat() * size.height) }
                val ribbon = Path().apply {
                    val right = size.width - 23.dp.toPx(); val left = right - 15.dp.toPx()
                    moveTo(left, 0f); lineTo(right, 0f); lineTo(right, 67.dp.toPx())
                    lineTo((left + right) / 2f, 60.dp.toPx()); lineTo(left, 67.dp.toPx()); close()
                }
                onDrawBehind {
                    fibers.forEachIndexed { i, at -> drawLine(Pk.GoldDeep.copy(alpha = if (i % 3 == 0) .09f else .035f), at, at + Offset(2.dp.toPx(), .4.dp.toPx()), .45.dp.toPx()) }
                    // Binding compression, crease and thin layered page edges.
                    drawRect(Pk.JournalInk.copy(alpha = .07f), size = Size(15.dp.toPx(), size.height))
                    drawLine(Pk.JournalInk.copy(alpha = .15f), Offset(10.dp.toPx(), 0f), Offset(10.dp.toPx(), size.height), 1.dp.toPx())
                    drawLine(Pk.Crisp.copy(alpha = .6f), Offset(17.dp.toPx(), 0f), Offset(17.dp.toPx(), size.height), 1.dp.toPx())
                    for (n in 1..3) drawLine(Pk.JournalRule.copy(alpha = .55f), Offset(size.width - n * 1.5.dp.toPx(), 12.dp.toPx()), Offset(size.width - n * 1.5.dp.toPx(), size.height - 10.dp.toPx()), .5.dp.toPx())
                    drawPath(ribbon, Pk.Oxblood)
                }
            }, content = content)
    }
}
