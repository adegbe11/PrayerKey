package com.prayerkey.manna.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    /** "4 min" style line, shown with a play mark. */
    val meta: String? = null,
    /** 0..1 for something already under way; null for something not started. */
    val progress: Float? = null,
    /** "Day 4 of 21" shown above the title for something under way. */
    val day: String? = null,
    val open: () -> Unit,
)

/**
 * A heading and a list of rows that all share one shape: words on the left,
 * a picture on the right. Long lists show [visible] rows and a See all.
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
            title, Modifier.padding(start = 4.dp, top = 22.dp, bottom = 2.dp),
            fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Pk.Charcoal,
        )
        (if (all) items else items.take(visible)).forEach { HomeRow(it) }
        if (items.size > visible) {
            Text(
                if (all) "Show less" else "See all",
                Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(99.dp))
                    .clickable { all = !all }.padding(horizontal = 18.dp, vertical = 10.dp),
                fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Pk.Oxblood,
            )
        }
    }
}

@Composable
private fun HomeRow(item: HomeItem) {
    Surface(
        Modifier.fillMaxWidth().pressable(.98f).clip(RoundedCornerShape(26.dp)).clickable(onClick = item.open),
        shape = RoundedCornerShape(26.dp), color = Pk.Crisp, shadowElevation = 1.dp,
    ) {
        Row(Modifier.padding(16.dp).height(104.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(start = 6.dp, end = 14.dp)) {
                Text(
                    item.day ?: item.eyebrow,
                    fontFamily = UtilitySans, fontSize = 14.sp, color = Pk.Charcoal.copy(alpha = .58f), maxLines = 1,
                )
                Text(
                    item.title, Modifier.padding(top = 4.dp),
                    fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 23.sp,
                    color = Pk.Charcoal, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                when {
                    item.progress != null -> Box(
                        Modifier.padding(top = 12.dp).width(96.dp).height(5.dp).clip(RoundedCornerShape(99.dp))
                            .background(Pk.Charcoal.copy(alpha = .10f)),
                    ) {
                        Box(
                            Modifier.fillMaxHeight().fillMaxWidth(item.progress.coerceIn(.06f, 1f))
                                .clip(RoundedCornerShape(99.dp)).background(Pk.Oxblood),
                        )
                    }
                    item.meta != null -> Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, null, tint = Pk.Charcoal.copy(alpha = .7f), modifier = Modifier.size(16.dp))
                        Text(item.meta, Modifier.padding(start = 4.dp), fontFamily = UtilitySans, fontSize = 14.sp, color = Pk.Charcoal.copy(alpha = .6f))
                    }
                }
            }
            Image(
                painterResource(item.art), null,
                Modifier.size(104.dp).clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
