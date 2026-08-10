package com.prayerkey.manna.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.model.DailyPrayer
import com.prayerkey.manna.ui.theme.BookSerif

enum class FaithTool { Midnight, Points, Someone }

fun midnightPrayer(base: DailyPrayer): DailyPrayer = base.copy(
    title = "Midnight Prayer",
    ref = "Psalm 91:1–2",
    prayer = """Heavenly Father,

As this day becomes quiet, I come under the shelter of the Most High and choose to rest in the shadow of the Almighty. You are my refuge, my fortress, and my God. I place my complete trust in You tonight.

Forgive every sin I committed knowingly or unknowingly today. Cleanse my heart, renew my mind, and remove anything that has created distance between us. Let the blood of Jesus speak mercy, protection, and peace over my life.

Watch over me, my family, my home, and everyone connected to me. Keep us from danger, evil, sickness, troubling dreams, and every plan formed against us. Let Your angels guard every door and every place where we rest.

Break every chain of fear, delay, confusion, and discouragement. Restore what has been lost. Heal what has been wounded. Open the doors that agree with Your will, and close every door that would lead me away from You.

As I sleep, give me deep rest. Renew my strength and prepare me for tomorrow. Let me wake with a clear mind, a healthy body, fresh mercy, and new opportunities. May tomorrow bring good news, divine favour, wise decisions, and visible answers to prayer.

I surrender the battles I cannot fight and the questions I cannot answer. You neither sleep nor slumber, so I will rest while You watch over me.

In Jesus’ name, Amen.""",
)

private val prayerPoints = listOf(
    "Thank God for life, mercy, and everything that carried you today.",
    "Ask God to forgive your sins and help you walk in a new direction.",
    "Pray for protection over yourself, your family, your home, and your journey.",
    "Ask for healing in your body, mind, emotions, and relationships.",
    "Pray for wisdom, clear decisions, and freedom from confusion.",
    "Ask God to bless your work, studies, business, finances, and responsibilities.",
    "Pray for open doors, helpful relationships, and favour that agrees with God’s will.",
    "Ask for strength against temptation, fear, discouragement, and harmful habits.",
    "Pray for your church, leaders, community, and country.",
    "Place tomorrow in God’s hands and ask for peace while you wait.",
)

@Composable
fun PrayerPointsScreen(reference: String, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val cs = MaterialTheme.colorScheme
    val points = remember(reference) { com.prayerkey.manna.data.prayerPointsFor(java.time.LocalDate.now(), reference) }
    val checked = remember(reference) { mutableStateListOf<Int>() }
    Column(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(cs.primary.copy(alpha = .20f), cs.background, cs.background)))) {
        ToolHeader("50 powerful prayer points", "Pray each one aloud and with faith", onClose)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 10.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(bottom = 14.dp).clip(RoundedCornerShape(20.dp))
                    .background(cs.onBackground).padding(20.dp),
            ) {
                Text("TODAY’S PRAYER BATTLE", color = cs.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
                Text("Stand firm. Pray the Word. Refuse fear.", color = cs.background, fontFamily = BookSerif, fontSize = 23.sp, lineHeight = 29.sp, modifier = Modifier.padding(top = 8.dp))
                Text(reference, color = cs.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            }
            points.forEachIndexed { index, point ->
                val done = index in checked
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 9.dp).clip(RoundedCornerShape(16.dp))
                        .background(cs.surface.copy(alpha = .88f))
                        .clickable { if (done) checked.remove(index) else checked.add(index) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(if (done) cs.primary else cs.background), contentAlignment = Alignment.Center) {
                        if (done) Icon(Icons.Outlined.Check, null, tint = cs.onPrimary, modifier = Modifier.size(17.dp)) else Text("${index + 1}", color = cs.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.padding(start = 14.dp).weight(1f)) {
                        Text("PRAYER ${index + 1}", color = cs.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(point, color = cs.onSurface, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }
            Text("${checked.size} of ${points.size} prayed", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp))
        }
    }
}

@Composable
fun PrayForSomeoneScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val cs = MaterialTheme.colorScheme
    var name by remember { mutableStateOf("") }
    var need by remember { mutableStateOf("") }
    var prayer by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().background(cs.background)) {
        ToolHeader("Pray for someone", "Stand with someone you care about", onClose)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(22.dp)) {
            if (prayer == null) {
                Text("Who are you praying for?", color = cs.onBackground, fontFamily = BookSerif, fontSize = 26.sp)
                OutlinedTextField(name, { name = it }, label = { Text("Their name") }, modifier = Modifier.fillMaxWidth().padding(top = 22.dp), shape = RoundedCornerShape(15.dp))
                OutlinedTextField(need, { need = it }, label = { Text("What do they need prayer for?") }, minLines = 4, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(15.dp))
                Button(
                    enabled = name.isNotBlank() && need.isNotBlank(),
                    onClick = {
                        prayer = "Heavenly Father, I bring ${name.trim()} before You today. You know them completely and You see their need concerning ${need.trim()}. Please come near to them, give them strength, wisdom, protection, and peace. Make a way where they cannot see one. Provide the right help at the right time, guard their heart from fear, and let them know they are not alone. According to Your perfect will, bring healing, restoration, favour, and a testimony from this situation. Show me how I can support them with love and patience. In Jesus’ name, Amen."
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(54.dp),
                ) { Text("WRITE THE PRAYER") }
            } else {
                Icon(Icons.Outlined.SelfImprovement, null, tint = cs.primary, modifier = Modifier.size(28.dp))
                Text("A prayer for ${name.trim()}", color = cs.onBackground, fontFamily = BookSerif, fontSize = 28.sp, modifier = Modifier.padding(top = 14.dp))
                Text(prayer!!, color = cs.onBackground, fontSize = 18.sp, lineHeight = 29.sp, modifier = Modifier.padding(top = 22.dp))
                OutlinedButton(onClick = { prayer = null }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) { Text("PRAY FOR SOMEONE ELSE") }
            }
        }
    }
}

@Composable
private fun ToolHeader(title: String, subtitle: String, onClose: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose) { Icon(Icons.Outlined.ArrowBack, "Back") }
        Column(Modifier.padding(start = 4.dp)) {
            Text(title, color = cs.onBackground, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
    }
}
