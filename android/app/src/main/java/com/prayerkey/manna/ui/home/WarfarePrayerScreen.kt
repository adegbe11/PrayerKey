package com.prayerkey.manna.ui.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.*

private data class WarfarePrayer(
    val id: String, val title: String, val subtitle: String,
    val reference: String, val scripture: String, val prayer: String,
)

private val warfarePrayers = listOf(
    WarfarePrayer(
        "midnight-anxiety", "Breaking Midnight Anxiety", "Fear, panic, sleeplessness, and intrusive thoughts",
        "2 Timothy 1:7", "For God has not given us a spirit of fear, but of power, love, and a sound mind.",
        """Father, I come before You in the name of Jesus. Tonight, I take my stand upon Your Word. You have not given me a spirit of fear, but of power, love, and a sound mind. I refuse every thought that magnifies danger above the power of God.

In the name of Jesus, I command fear, panic, torment, and every oppressive thought to release my mind. You will not rule my imagination, disturb my sleep, or steal the peace Christ has given me. My mind belongs to Jesus Christ.

Lord, cover my thoughts with Your truth. Let Your presence fill this room and Your peace guard my heart and mind. I declare that I am safe in Your hands, my mind is sound, and my night shall be peaceful. In Jesus' name, Amen."""
    ),
    WarfarePrayer(
        "home-protection", "Protection Over My Home", "Peace and protection for your household",
        "Psalm 91:10-11", "No evil shall befall you, nor shall any plague come near your dwelling.",
        """Most High God, I place my home and everyone within it under Your authority. Let Your presence rest in every room and let Your peace govern every heart.

In the name of Jesus, I reject fear, violence, confusion, sickness, and every work of darkness directed against this household. Let every plan of evil fail. Station Your angels around this dwelling and preserve our going out and our coming in.

Lord, make this home a place of prayer, truth, forgiveness, and rest. I declare that Jesus Christ is Lord over this household and that no weapon formed against us shall prosper. In Jesus' name, Amen."""
    ),
    WarfarePrayer(
        "destructive-cycles", "Breaking Destructive Cycles", "Habits, repeated defeat, and patterns that must end",
        "John 8:36", "If the Son makes you free, you shall be free indeed.",
        """Lord Jesus, You purchased my freedom at the cross. I confess every agreement I have made with sin, shame, secrecy, and defeat, and I turn away from it now.

In the name of Jesus, I renounce every destructive cycle operating in my thoughts, choices, relationships, and body. The past will not dictate my future. Every chain must yield to the authority of Christ, and every hidden root must be exposed and removed.

Holy Spirit, strengthen me to choose truth, seek wise help, and walk in obedience. Restore what these patterns have damaged. I declare that whom the Son sets free is free indeed. In Jesus' name, Amen."""
    ),
    WarfarePrayer(
        "confusion", "Peace Over Confusion", "Mental pressure, uncertainty, and spiritual distraction",
        "1 Corinthians 14:33", "God is not the author of confusion but of peace.",
        """Father, You are not the author of confusion. I bring every competing voice, fearful conclusion, and uncertain decision before You.

In the name of Jesus, I reject confusion, deception, haste, and mental oppression. Let every lie lose its influence over me. Silence every voice that contradicts Your written Word, and close every door that is not from You.

Lord, give me wisdom that is pure and peaceable. Make the next obedient step clear. I receive a disciplined mind, a guarded heart, and the peace of Christ. Your truth will lead me and Your will shall stand. In Jesus' name, Amen."""
    ),
    WarfarePrayer(
        "temptation", "Strength Against Temptation", "Purity, self-control, and escape from temptation",
        "1 Corinthians 10:13", "God is faithful, who will not allow you to be tempted beyond what you are able.",
        """Faithful God, I acknowledge my weakness and depend upon Your strength. Thank You for providing a way of escape in every temptation.

In the name of Jesus, I reject every thought, invitation, and appetite that draws me away from holiness. Sin will not rule over me. Break the power of secrecy and give me courage to leave every place, relationship, and habit that feeds temptation.

Holy Spirit, fill me with self-control. Renew my desires, guard my eyes, order my steps, and connect me with trustworthy people who will help me remain faithful. I belong to Jesus Christ. In His name, Amen."""
    ),
    WarfarePrayer(
        "justice", "Justice and Vindication", "False accusation, oppression, and matters requiring God's justice",
        "Psalm 43:1", "Vindicate me, O God, and plead my cause.",
        """Righteous Judge, I place this matter in Your hands. You see every fact, every motive, and every hidden action. Let truth come fully into the light.

In the name of Jesus, I reject intimidation, bitterness, retaliation, and despair. Let every false accusation fail, every dishonest scheme be exposed, and every lawful process be guided by truth. Protect the innocent and restrain those who would misuse power.

Lord, defend my cause without allowing hatred to rule my heart. Give me wise counsel, truthful words, and patience while justice unfolds. I trust You to judge rightly and to establish peace. In Jesus' name, Amen."""
    ),
)

@Composable
internal fun WarfarePrayerScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("warfare_prayer_history", Context.MODE_PRIVATE) }
    var completed by remember { mutableStateOf(prefs.getStringSet("completed", emptySet()).orEmpty()) }
    var selected by remember { mutableStateOf<WarfarePrayer?>(null) }
    var declared by remember { mutableStateOf(false) }

    BackHandler { if (selected != null) { selected = null; declared = false } else onClose() }
    selected?.let { prayer ->
        WarfarePrayerDetail(prayer, declared, {
            declared = true
            completed = completed + prayer.id
            prefs.edit().putStringSet("completed", completed).apply()
        }) { selected = null; declared = false }
        return
    }

    Surface(Modifier.fillMaxSize(), color = Pk.Cream) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Outlined.ArrowBackIosNew, "Back", tint = Pk.Charcoal, modifier = Modifier.size(17.dp)) }
                Text("WARFARE PRAYER", Modifier.weight(1f), color = Pk.Oxblood, fontFamily = UtilitySans, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Spacer(Modifier.width(48.dp))
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 26.dp)) {
                Text("What battle are you facing?", fontFamily = DisplaySerif, fontSize = 35.sp, lineHeight = 40.sp, color = Pk.Charcoal, modifier = Modifier.padding(top = 14.dp))
                Spacer(Modifier.height(22.dp))
                warfarePrayers.forEach { prayer ->
                    val done = prayer.id in completed
                    Surface(
                        Modifier.fillMaxWidth().padding(bottom = 11.dp).clickable { selected = prayer },
                        shape = RoundedCornerShape(24.dp), color = Color.White,
                        border = BorderStroke(1.dp, if (done) Pk.Gold.copy(alpha = .45f) else Pk.Hair), shadowElevation = 2.dp,
                    ) {
                        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(if (done) Pk.GoldWash else Pk.Oxblood.copy(alpha = .08f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(if (done) Icons.Outlined.Check else Icons.Outlined.Shield, null, tint = if (done) Pk.GoldDeep else Pk.Oxblood)
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                                Text(prayer.title, fontFamily = DisplaySerif, fontSize = 19.sp, color = Pk.Charcoal)
                                Text(prayer.subtitle, color = Pk.Muted, fontFamily = UtilitySans, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                            Icon(Icons.Outlined.ChevronRight, null, tint = Pk.Muted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                if (completed.isNotEmpty()) {
                    Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.History, null, tint = Pk.GoldDeep)
                        Text("Prayer history", color = Pk.Charcoal, fontFamily = UtilitySans, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WarfarePrayerDetail(prayer: WarfarePrayer, declared: Boolean, onDeclare: () -> Unit, onBack: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = Pk.Oxblood) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBackIosNew, "Prayer types", tint = Color.White, modifier = Modifier.size(17.dp)) }
                Text("3–5 MINUTES", Modifier.weight(1f), color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.width(48.dp))
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
                Text("POWERFUL PRAYER", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 14.dp))
                Text(prayer.title, color = Color.White, fontFamily = DisplaySerif, fontSize = 37.sp, lineHeight = 41.sp, modifier = Modifier.padding(top = 9.dp))
                Surface(Modifier.fillMaxWidth().padding(top = 24.dp), shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = .10f), border = BorderStroke(.7.dp, Color.White.copy(alpha = .22f))) {
                    Column(Modifier.padding(18.dp)) {
                        Text("“${prayer.scripture}”", color = Color.White, fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontSize = 18.sp, lineHeight = 27.sp)
                        Text(prayer.reference.uppercase(), color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp, modifier = Modifier.padding(top = 10.dp))
                    }
                }
                Text("READ THIS OUT LOUD", color = Pk.GoldLight, fontFamily = UtilitySans, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 26.dp, bottom = 12.dp))
                Text(prayer.prayer, color = Color.White, fontFamily = BookSerif, fontSize = 20.sp, lineHeight = 31.sp)
                Spacer(Modifier.height(30.dp))
            }
            AnimatedVisibility(declared, enter = fadeIn()) {
                Text("DECLARATION SAVED TO PRAYER HISTORY", Modifier.fillMaxWidth().padding(horizontal = 22.dp), color = Pk.GoldLight, fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center)
            }
            Button(
                onClick = if (declared) onBack else onDeclare,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp).height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (declared) Pk.GoldLight else Color.White, contentColor = Pk.Oxblood),
            ) {
                Icon(Icons.Outlined.Check, null, modifier = Modifier.size(19.dp))
                Text(if (declared) " SAVED · RETURN" else " I DECLARED IT", fontFamily = UtilitySans, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = .7.sp)
            }
        }
    }
}
