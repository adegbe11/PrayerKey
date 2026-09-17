package com.prayerkey.manna.ui.morning

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.BookSerif
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Night Prayer — two minutes, and the last thing you read.
 *
 * Four movements with no glosses, because at this hour the rubric is the whole
 * instruction and anything else is one more thing to read:
 *
 *     Commend · Word · Keep Watch · Peace
 *
 * Deliberately short. It is meant to be learned by heart and eventually not
 * need the phone at all, which is an odd thing for an app to want and the right
 * thing for this one.
 *
 * The type is larger and looser than the day offices. This is read tired, in
 * the dark, at arm's length.
 *
 * Below the Amen is a panel for three in the morning, folded away. It opens for
 * the person it was written for — somebody awake at an hour they did not
 * choose, who does not need a full office and does need something to say.
 */
@Composable
fun NightPrayerScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val today = remember { LocalDate.now() }
    var day by remember { mutableStateOf(false) }
    val palette = if (day) DayPalette else NightPalette

    val speaking = rememberSpeaking()
    val voice = rememberOfficeVoice(speaking)

    OfficeFlip(
        title = "Night Prayer",
        dateLine = today.format(DateTimeFormatter.ofPattern("EEEE", Locale.UK)),
        movements = NIGHT,
        palette = palette,
        voice = voice,
        speaking = speaking.value,
        colophon = "Morning Prayer opens at first light.",
        onClose = onClose,
        length = "Two minutes. Short enough to learn by heart.",
        scale = NightScale,
        trailing = { Lamp(day, palette) { day = !day } },
        closeExtra = { WakePanel(palette) },
    )
}

/**
 * Midnight Prayer — the first watch.
 *
 * Seven movements, and the order is the argument:
 *
 *     Praise · Cover · Repent · Word · Prayer Points · Declare · Seal
 *
 * Praise first, always. Nobody should enter this hour cold, and an office that
 * opened on the prayer points would be a fight the reader had walked into
 * without saying who God is. Repent before asking, because the point of the
 * confession is that nothing is left standing on. And Seal at the end —
 * thanksgiving, then sleep, rather than repeating yourself until dawn.
 *
 * The points are numbered and ticked off as they are prayed, with the
 * instruction that matters printed above them: stay on one until you have peace
 * about it. Without that line a set of prayer points becomes a list to get
 * through.
 *
 * The palette is the darkest in the app. This is prayed standing, out loud, in
 * a room where everybody else is asleep.
 */
@Composable
fun MidnightPrayerScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    var day by remember { mutableStateOf(false) }
    val palette = if (day) DayPalette else MidnightPalette

    val speaking = rememberSpeaking()
    val voice = rememberOfficeVoice(speaking)

    OfficeFlip(
        title = "Midnight Prayer",
        dateLine = "The first watch",
        movements = MIDNIGHT,
        palette = palette,
        voice = voice,
        speaking = speaking.value,
        colophon = "Morning Prayer opens at first light.\nSleep now. The watch is His.",
        onClose = onClose,
        length = "Twelve to three. Pray it standing, and pray it out loud.",
        trailing = { Lamp(day, palette) { day = !day } },
    )
}

@Composable
private fun Lamp(day: Boolean, palette: OfficePalette, onToggle: () -> Unit) {
    Text(
        if (day) "Night" else "Day",
        Modifier.clickable(onClick = onToggle),
        fontFamily = BookSerif,
        fontStyle = FontStyle.Italic,
        fontSize = 13.5.sp,
        color = palette.accent,
    )
}

private val NIGHT = listOf(
    OfficeMovement(
        "Commend",
        body = listOf("Into Your hands, Father, I give my spirit, my body, my breath, and everyone I love. You kept me all day without my help. You will keep me all night without my worry."),
    ),
    OfficeMovement(
        "Word",
        scripture = OfficeVerse(
            "He will not suffer thy foot to be moved: he that keepeth thee will not slumber. Behold, he that keepeth Israel shall neither slumber nor sleep.",
            "Psalm 121:3–4",
        ),
    ),
    OfficeMovement(
        "Keep Watch",
        body = listOf("Keep watch tonight over the ones still working, and the ones lying awake afraid, and the ones crying where nobody can hear them. Tend the sick. Rest the tired. Bless the dying. Soothe the suffering. Shield the ones who are happy tonight. And be near to everyone I love, wherever they are lying down."),
    ),
    OfficeMovement(
        "Peace",
        body = listOf("The last thought of this day belongs to You, not to me. Whatever I am still turning over, I am putting it down now. Give me quiet sleep and a mind that rests."),
    ),
)

private val MIDNIGHT = listOf(
    OfficeMovement(
        "Praise", "Never enter this hour cold",
        listOf("Lord, before I say anything else at this hour, I worship You. You are the Most High. There is no power beside You, no name above Yours, nothing in heaven or on earth or under the earth that does not bow when You speak. You do not sleep and You are not intimidated by anything I am about to bring before You. I praise You."),
    ),
    OfficeMovement(
        "Cover", "Blood over you before you pray",
        listOf("I cover myself in the blood of Jesus. My mind, my body, my sleep, my dreams. I cover this house and every room in it. I cover my family, wherever they are lying down tonight. I cover my work, my name, my money, and everything my hands have built. Nothing enters what the blood covers."),
    ),
    OfficeMovement(
        "Repent", "Leave no ground for the enemy to stand on",
        listOf(
            "Before I ask anything, search me. Anything in my life that gives the enemy a legal right — unforgiveness I am nursing, a habit I keep returning to, an agreement I made without knowing what I was agreeing to — show it to me now.",
            "I confess it and I renounce it. Wash me in the blood of Jesus. I am not coming to this hour with anything hidden.",
        ),
    ),
    OfficeMovement(
        "Word", "The weapon you are praying with",
        scripture = OfficeVerse(
            "No weapon that is formed against thee shall prosper; and every tongue that shall rise against thee in judgment thou shalt condemn.",
            "Isaiah 54:17",
        ),
    ),
    OfficeMovement(
        "Prayer Points", "Pray each one out loud",
        instruction = "Do not rush these. Stay on one until you have peace about it before moving to the next.",
        points = listOf(
            "Every altar raised against my destiny, be silenced tonight, in the name of Jesus.",
            "Whatever was spoken over me in the dark, I cancel it now, in the name of Jesus.",
            "Every covenant I did not sign and did not consent to, I break it, in the name of Jesus.",
            "Every gate the enemy has opened into my home, I close it with the blood of Jesus.",
            "Every stagnation over my work, my hands and my finances, break, in the name of Jesus.",
            "Anything assigned against my children and my family, scatter, in the name of Jesus.",
            "Whatever I inherited that is not from God, I renounce it and I hand it back, in the name of Jesus.",
            "Every dream sent to trouble my sleep, I reject it, in the name of Jesus.",
            "Lord, let every door You have opened for me stay open, and let no man shut it.",
        ),
    ),
    OfficeMovement(
        "Declare", "Say these standing",
        declarations = listOf(
            "I am covered by the blood of Jesus.",
            "My house is under God's authority.",
            "What God has settled, no one reopens.",
            "I will not be afraid of the terror by night.",
            "The Lord watches over my going out and my coming in.",
        ),
    ),
    OfficeMovement(
        "Seal", "Thank Him before you see it",
        listOf("Father, I thank You for hearing me at this hour. I am not going to keep repeating myself — You heard me the first time. I thank You for what You have already begun to do while I was still speaking. Now let me lie down and sleep in peace, because You alone make me dwell in safety."),
    ),
)
