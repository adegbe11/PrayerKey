package com.prayerkey.manna.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PrayerTopic
import com.prayerkey.manna.data.PrayingFor
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.ui.platform.platformContext
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.UtilitySans

private val Violet = Color(0xFF6200ED)
private val Ink = Color(0xFF1D1D1F)
private val Muted = Color(0xFF6E6E73)
private val Hairline = Color(0x14000000)

/**
 * What sits under the question on the Pray tab: one quiet list of three rows,
 * so the question stays the only big thing on the screen.
 */
@Composable
fun PrayHub(
    topics: List<PrayerTopic>,
    onCategory: (String) -> Unit,
    onAll: () -> Unit,
    onPrayingFor: () -> Unit,
    onBedtime: () -> Unit,
) {
    val context = platformContext()
    LaunchedEffect(Unit) { PrayingFor.load(context) }
    val people by PrayingFor.people.collectAsState()
    val day = today().toEpochDays().toLong()
    val waiting = people.filter { it.answered == 0L }
    val due = waiting.count { !it.prayedOn(day) }

    Column(
        Modifier.padding(top = 30.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(Color.White).border(1.dp, Hairline, RoundedCornerShape(22.dp)),
    ) {
        HubRow(
            Icons.Outlined.People, "Praying for",
            when {
                waiting.isEmpty() -> ""
                due == 0 -> "All prayed for today"
                else -> "$due waiting today"
            },
            onPrayingFor,
        ) {
            Box {
                waiting.take(3).forEachIndexed { i, p ->
                    Box(
                        Modifier.offset(x = (-10 * i).dp).size(26.dp).clip(CircleShape)
                            .background(if (p.prayedOn(day)) Color(0xFFF1E9FF) else Violet)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            p.name.take(1).uppercase(), fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 11.sp,
                            color = if (p.prayedOn(day)) Violet else Color.White,
                        )
                    }
                }
            }
        }
        Divider()
        HubRow(Icons.Outlined.AutoStories, "Prayers for every need", "", onAll)
        Divider()
        HubRow(Icons.Outlined.Bedtime, "Bedtime", "", onBedtime)
    }
}

@Composable
private fun HubRow(
    icon: ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFF1E9FF)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Violet, modifier = Modifier.size(18.dp))
        }
        Text(
            title, Modifier.weight(1f).padding(start = 12.dp),
            fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Ink, maxLines = 1,
        )
        trailing()
        if (detail.isNotEmpty()) Text(detail, Modifier.padding(start = 8.dp), fontFamily = UtilitySans, fontSize = 13.sp, color = Muted, maxLines = 1)
        Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFFB8B8BD), modifier = Modifier.padding(start = 4.dp).size(20.dp))
    }
}

@Composable
private fun Divider() {
    Box(Modifier.padding(start = 62.dp).fillMaxWidth().height(1.dp).background(Hairline))
}
