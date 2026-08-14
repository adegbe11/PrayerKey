package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.R
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.ui.theme.DisplaySerif

private data class SituationStyle(val color: Color, val cover: Int)

private val situationStyles = listOf(
    SituationStyle(Color(0xFF5C02D8), R.drawable.discovery_cover_1),
    SituationStyle(Color(0xFF214E78), R.drawable.discovery_cover_2),
    SituationStyle(Color(0xFF8A3D62), R.drawable.discovery_cover_3),
    SituationStyle(Color(0xFF396B5A), R.drawable.discovery_cover_4),
    SituationStyle(Color(0xFF9B5A32), R.drawable.discovery_cover_5),
    SituationStyle(Color(0xFF3E3A78), R.drawable.discovery_cover_6),
    SituationStyle(Color(0xFF6D4C41), R.drawable.discovery_cover_2),
    SituationStyle(Color(0xFF2A6873), R.drawable.discovery_cover_4),
)

@Composable
fun PrayerSituationLibrary(
    topics: List<PrayerTopic>, loading: Boolean, onRetry: () -> Unit,
    onBack: () -> Unit, onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val grouped = remember(topics, query) {
        topics.groupBy { it.category }
            .map { (category, prayers) -> category to prayers.size }
            .filter { query.isBlank() || it.first.contains(query, true) }
            .sortedWith(compareBy<Pair<String, Int>> { categoryRank(it.first) }.thenBy { it.first })
    }
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().background(cs.background).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Column(Modifier.weight(1f).padding(start = 6.dp)) {
                Text("Prayer situations", fontFamily = DisplaySerif, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                Text("Choose what you need prayer for", color = cs.onBackground.copy(.58f), fontSize = 13.sp)
            }
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            placeholder = { Text("Search marriage, healing, family…") },
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        )
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = cs.primary)
            }
            topics.isEmpty() -> Column(
                Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Prayer decks could not load", fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onRetry) { Text("Try again") }
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                items(grouped, key = { it.first }) { (category, count) ->
                    val style = situationStyles[kotlin.math.abs(category.hashCode()) % situationStyles.size]
                    Box(
                        Modifier.fillMaxWidth().height(116.dp)
                            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = style.color.copy(.26f))
                            .clip(RoundedCornerShape(18.dp)).background(style.color)
                            .clickable { onSelect(category) },
                    ) {
                        Image(
                            painterResource(style.cover), null, contentScale = ContentScale.Crop,
                            modifier = Modifier.align(Alignment.CenterEnd).offset(x = 22.dp, y = 5.dp)
                                .size(width = 90.dp, height = 104.dp).graphicsLayer {
                                    rotationZ = 7f; alpha = .92f
                                }.clip(RoundedCornerShape(14.dp)),
                        )
                        Box(Modifier.matchParentSize().background(Color.Black.copy(.10f)))
                        Column(
                            Modifier.align(Alignment.BottomStart).width(112.dp).padding(14.dp),
                        ) {
                            Text(category, color = Color.White, fontSize = 16.sp, lineHeight = 19.sp,
                                fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("$count prayers", color = Color.White.copy(.76f), fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerDeckCategoryHeader(category: String, position: Int, total: Int, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 15.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(onClick = onBack, shape = CircleShape, color = Color.Black.copy(.36f)) {
            Icon(Icons.Outlined.ArrowBack, "All prayer situations", tint = Color.White,
                modifier = Modifier.padding(11.dp).size(21.dp))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(category, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("Pull down for the next prayer", color = Color.White.copy(.72f), fontSize = 11.sp)
        }
        Text("$position / $total", color = Color.White.copy(.82f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun categoryRank(category: String): Int = when (category.lowercase()) {
    "marriage" -> 0
    "family" -> 1
    "health" -> 2
    "relationships" -> 3
    "work" -> 4
    "finance" -> 5
    "faith", "spiritual life" -> 6
    "protection" -> 7
    else -> 20
}
