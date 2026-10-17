package com.prayerkey.manna.ui.journal

import com.prayerkey.manna.ui.platform.StatusBarIcons
import com.prayerkey.manna.platform.now
import com.prayerkey.manna.platform.today
import com.prayerkey.manna.ui.platform.platformContext
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.FamilyRestroom
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.PROMPT_PACKS
import com.prayerkey.manna.data.PromptPack
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import kotlinx.datetime.LocalDate

/**
 * Prompts.
 *
 * A few recommended questions as large coloured cards you can shuffle, then
 * the packs as a plain list. A pack opens as a sheet of its questions, each
 * one set on its own, and tapping one starts a page with it.
 */
private val Ink = Color(0xFF111014)
private val Wash = Color(0xFFF1EFF4)
private val Hair = Color(0xFFE6E3EA)

private fun iconOf(pack: String): ImageVector = when (pack) {
    "Morning Prayer" -> Icons.Outlined.WbTwilight
    "Evening Prayer" -> Icons.Outlined.NightsStay
    "Praise & Adoration" -> Icons.Outlined.WbSunny
    "Thanksgiving" -> Icons.Outlined.FavoriteBorder
    "Confession" -> Icons.Outlined.WaterDrop
    "Intercession" -> Icons.Outlined.Groups
    "My Requests" -> Icons.Outlined.VolunteerActivism
    "Surrender" -> Icons.Outlined.Spa
    "Waiting on God" -> Icons.Outlined.Schedule
    "Answered Prayer" -> Icons.Outlined.CheckCircle
    "Praying Scripture" -> Icons.Outlined.MenuBook
    "Listening to God" -> Icons.Outlined.Hearing
    "Hard Days" -> Icons.Outlined.Cloud
    "Spiritual Warfare" -> Icons.Outlined.Shield
    else -> Icons.Outlined.FamilyRestroom
}

/**
 * The first card follows the hour - morning prayer before noon, evening
 * prayer after dark - and the rest follow what this person said they came
 * for on the welcome screens.
 */
private fun recommended(asked: List<String>): List<String> {
    val hour = now().hour
    val first = if (hour in 4..11) "Morning Prayer" else if (hour >= 18 || hour < 4) "Evening Prayer" else "Praise & Adoration"
    val rest = (asked + listOf("Thanksgiving", "Intercession", "Waiting on God", "Praying Scripture"))
        .filter { it != first }.distinct().take(4)
    return listOf(first) + rest
}


@Composable
fun PromptLibrary(onClose: () -> Unit, onPrompt: (String, String) -> Unit) {
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler { if (open != null) open = null else onClose() }

    // dark status-bar icons on the white page
    StatusBarIcons(dark = true, inDialog = true)

    val context = platformContext()
    val asked = remember { recommended(com.prayerkey.manna.data.OnboardingPlan.packs(context)) }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 40.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(start = 20.dp, end = 18.dp, top = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Prompts", Modifier.weight(1f), color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 34.sp)
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(Wash).clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Outlined.Close, "Close", tint = Pk.Charcoal, modifier = Modifier.size(18.dp)) }
                }
            }
            item { Heading("Recommended") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(asked) { name ->
                        PROMPT_PACKS.find { it.name == name }?.let { pack ->
                            PromptCard(pack, onOpen = { open = pack.name }, onPrompt = { onPrompt(it, pack.name) })
                        }
                    }
                }
            }
            item { Heading("Prompt Packs") }
            items(PROMPT_PACKS, key = { it.name }) { pack ->
                Row(
                    Modifier.fillMaxWidth().clickable { open = pack.name }.padding(start = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(iconOf(pack.name), null, tint = Ink, modifier = Modifier.size(24.dp))
                    Column(Modifier.weight(1f).padding(start = 18.dp)) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 15.dp, horizontal = 0.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(pack.name, Modifier.weight(1f), color = Ink, fontFamily = UtilitySans, fontSize = 17.sp)
                            Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFFB9B5BF), modifier = Modifier.padding(end = 14.dp).size(20.dp))
                        }
                        HorizontalDivider(thickness = .6.dp, color = Hair)
                    }
                }
            }
        }

        open?.let { name -> PROMPT_PACKS.find { it.name == name } }?.let { pack ->
            PackSheet(pack, onDone = { open = null }, onPrompt = { onPrompt(it, pack.name) })
        }
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text,
        Modifier.padding(start = 20.dp, top = 26.dp, bottom = 12.dp),
        color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 22.sp,
    )
}

/** A question on its pack's colour. The arrow in the corner offers another one. */
@Composable
private fun PromptCard(pack: PromptPack, onOpen: () -> Unit, onPrompt: (String) -> Unit) {
    var index by rememberSaveable(pack.name) { mutableIntStateOf(today().dayOfYear % pack.prompts.size) }
    val question = pack.prompts[index]
    val colour = Color(pack.colour)
    Column(
        Modifier.width(300.dp).clip(RoundedCornerShape(20.dp)).background(Wash),
    ) {
        Box(
            Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(20.dp)).background(colour)
                .clickable { onPrompt(question) },
        ) {
            Text(
                question,
                Modifier.align(Alignment.Center).padding(horizontal = 26.dp),
                color = Color.White, fontFamily = BookSerif, fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp, lineHeight = 26.sp, textAlign = TextAlign.Center,
            )
            Box(
                Modifier.align(Alignment.TopEnd).padding(12.dp).size(32.dp).clip(CircleShape)
                    .background(Color.Black.copy(alpha = .16f))
                    .clickable { index = (index + 1) % pack.prompts.size },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Refresh, "Another", tint = Color.White, modifier = Modifier.size(17.dp)) }
        }
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(iconOf(pack.name), null, tint = Ink, modifier = Modifier.size(22.dp))
            Text(pack.name, Modifier.weight(1f).padding(start = 12.dp), color = Ink, fontFamily = UtilitySans, fontSize = 17.sp)
            Icon(Icons.Outlined.ChevronRight, null, tint = Color(0xFFB9B5BF), modifier = Modifier.size(20.dp))
        }
    }
}

/** A pack: its name, how many, and every question set on its own. */
@Composable
private fun PackSheet(pack: PromptPack, onDone: () -> Unit, onPrompt: (String) -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .28f)).clickable(interactionSource = null, indication = null, onClick = onDone)) {
        LazyColumn(
            Modifier.fillMaxSize().statusBarsPadding().padding(top = 14.dp)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(Color.White)
                .clickable(interactionSource = null, indication = null) {},
            contentPadding = PaddingValues(bottom = 40.dp),
        ) {
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                    Text(
                        "Done",
                        Modifier.align(Alignment.End).padding(top = 14.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onDone).padding(6.dp),
                        color = Pk.Oxblood, fontFamily = UtilitySans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                    )
                    Box(
                        Modifier.padding(top = 4.dp).size(46.dp).clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, Ink, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(iconOf(pack.name), null, tint = Ink, modifier = Modifier.size(24.dp)) }
                    Text(pack.name, Modifier.padding(top = 12.dp), color = Ink, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    Text("${pack.prompts.size} prompts", Modifier.padding(top = 2.dp, bottom = 16.dp), color = Color(0xFF8E8B94), fontFamily = UtilitySans, fontSize = 13.sp)
                    HorizontalDivider(thickness = .6.dp, color = Hair)
                }
            }
            items(pack.prompts) { q ->
                Column(Modifier.fillMaxWidth().clickable { onPrompt(q) }.padding(horizontal = 22.dp)) {
                    Text(
                        q,
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 26.dp),
                        color = Ink, fontFamily = BookSerif, fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp, lineHeight = 24.sp, textAlign = TextAlign.Center,
                    )
                    HorizontalDivider(thickness = .6.dp, color = Hair)
                }
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}
