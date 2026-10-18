package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.PrayingFor
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.resources.Res
import com.prayerkey.manna.resources.onboarding_prayer
import com.prayerkey.manna.resources.prayerkey_discovery_world
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val Violet = Color(0xFF6200ED)
private val Gold = Color(0xFFFFD68C)
private val Ink = Color(0xFF1D1D1F)
private val Muted = Color(0xFF6E6E73)

/** The needs people come to pray about, in the order they are most asked for. */
private val Needs: List<Pair<String, ImageVector>> = listOf(
    "Health" to Icons.Outlined.Favorite,
    "Family" to Icons.Outlined.FamilyRestroom,
    "Mental Health" to Icons.Outlined.Psychology,
    "Finance" to Icons.Outlined.Payments,
    "Protection" to Icons.Outlined.Shield,
    "Work" to Icons.Outlined.WorkOutline,
    "Faith" to Icons.Outlined.LocalFlorist,
    "Nation" to Icons.Outlined.Public,
)

/**
 * What the Pray tab holds under the question: the people you are praying
 * for, the written prayers by need, and the two quieter ways to pray. The tab
 * used to end at four suggestion chips, with the 543 prayers behind a small
 * icon in the corner.
 */
@Composable
fun PrayHub(
    topics: List<PrayerTopic>,
    onCategory: (String) -> Unit,
    onAll: () -> Unit,
    onPrayingFor: () -> Unit,
    onLanguages: () -> Unit,
    onBedtime: () -> Unit,
) {
    val context = platformContext()
    LaunchedEffect(Unit) { PrayingFor.load(context) }
    val people by PrayingFor.people.collectAsState()
    val day = today().toEpochDays().toLong()
    val waiting = people.filter { it.answered == 0L }

    Column(Modifier.fillMaxWidth()) {
        // ── the people ─────────────────────────────────────────────
        Row(
            Modifier.padding(top = 30.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF2C0A5C), Color(0xFF4A12A8))))
                .clickable(onClick = onPrayingFor).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("PRAYING FOR", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.5.sp, color = Gold)
                Text(
                    when {
                        waiting.isEmpty() -> "Who are you praying for?"
                        waiting.all { it.prayedOn(day) } -> "Everyone prayed for today"
                        else -> "${waiting.count { !it.prayedOn(day) }} waiting for today's prayer"
                    },
                    Modifier.padding(top = 4.dp),
                    fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color.White,
                )
                Text(
                    if (waiting.isEmpty()) "Keep the names you promised to pray for" else waiting.take(3).joinToString(", ") { it.name },
                    Modifier.padding(top = 2.dp), maxLines = 1,
                    fontFamily = UtilitySans, fontSize = 13.sp, color = Color.White.copy(alpha = .7f),
                )
            }
            // the first few faces, overlapping, like people standing together
            Box(Modifier.padding(start = 10.dp)) {
                waiting.take(3).forEachIndexed { i, p ->
                    Box(
                        Modifier.offset(x = (-14 * i).dp).size(38.dp).clip(CircleShape)
                            // gold while waiting, white once prayed for today
                            .background(if (p.prayedOn(day)) Color.White else Gold)
                            .border(2.dp, Color(0xFF3A0E86), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            p.name.take(1).uppercase(), fontFamily = DisplaySerif, fontWeight = FontWeight.Bold,
                            color = if (p.prayedOn(day)) Violet else Color(0xFF2E0078),
                        )
                    }
                }
                if (waiting.isEmpty()) Icon(Icons.Outlined.ChevronRight, null, tint = Gold)
            }
        }

        // ── the written prayers, by need ───────────────────────────
        Row(Modifier.padding(top = 30.dp, bottom = 12.dp).fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text("Prayers for every need", Modifier.weight(1f), fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Ink)
            Text(
                "All ${topics.size.takeIf { it > 0 } ?: 543}", Modifier.clickable(onClick = onAll).padding(4.dp),
                fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Violet,
            )
        }
        Needs.chunked(2).forEach { pair ->
            Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { (name, icon) ->
                    val count = topics.count { it.category.equals(name, true) }
                    Row(
                        Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(Color.White)
                            .border(1.dp, Color(0x12000000), RoundedCornerShape(20.dp))
                            .clickable { onCategory(name) }.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF1E9FF)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(icon, null, tint = Violet, modifier = Modifier.size(19.dp)) }
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(name, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink, maxLines = 1)
                            Text(if (count > 0) "$count prayers" else "Prayers", fontFamily = UtilitySans, fontSize = 11.sp, color = Muted)
                        }
                    }
                }
            }
        }

        // ── two quieter ways ──────────────────────────────────────
        Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WayTile(Res.drawable.prayerkey_discovery_world, "Your language", "Pray in Pidgin, Yoruba, Igbo, Hausa or French", Modifier.weight(1f), onLanguages)
            WayTile(Res.drawable.onboarding_prayer, "Bedtime", "Tonight's prayer, then Scripture to sleep to", Modifier.weight(1f), onBedtime)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun WayTile(art: DrawableResource, title: String, line: String, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.aspectRatio(.85f).clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick)) {
        Image(painterResource(art), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Transparent, .3f to Color.Transparent, 1f to Color(0xEE12002A))))
        Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
            Text(title, fontFamily = DisplaySerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color.White)
            Text(line, Modifier.padding(top = 3.dp), fontFamily = UtilitySans, fontSize = 12.sp, lineHeight = 16.sp, color = Color.White.copy(alpha = .8f))
        }
    }
}
