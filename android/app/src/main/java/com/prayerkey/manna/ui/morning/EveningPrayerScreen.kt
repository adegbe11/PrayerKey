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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.OfficeStore
import com.prayerkey.manna.ui.theme.BookSerif
import java.time.LocalDate

/**
 * Evening Prayer, the office run the other way.
 *
 * Morning goes out into the day; this one walks back through it and puts it
 * down. Ten movements:
 *
 *     Still · Review · Thank · Confess · Forgive
 *     Release · Word · Intercede · Protect · Rest
 *
 * Two things belong to the evening alone:
 *
 *  - **The lamp.** A page read in a dark room by somebody about to sleep. A
 *    white screen at that hour is a physical objection to the thing being
 *    asked for, so the lamp is at the top of the page rather than buried in a
 *    settings screen.
 *  - **This morning you asked.** The Thank movement reads back what was
 *    written in the morning office and asks how it went. That is the thread
 *    that makes the two one book instead of two screens, and it is the only
 *    line here that could not have been written for somebody else.
 */
@Composable
fun EveningPrayerScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val today = remember { LocalDate.now() }

    /* On by default after dark. Somebody opening this at half past nine should
       not have to find the switch first. */
    var night by remember { mutableStateOf(OfficeStore.isEvening(java.time.LocalTime.now().hour)) }
    val palette = if (night) NightPalette else DawnPalette

    val speaking = rememberSpeaking()
    val voice = rememberOfficeVoice(speaking)

    val morningAsk = remember { OfficeStore.morningAsk(context, today) }
    var release by remember { mutableStateOf(OfficeStore.eveningRelease(context, today)) }
    DisposableEffect(Unit) { onDispose { OfficeStore.setEveningRelease(context, release, today) } }

    OfficeFlip(
        title = "Evening Prayer",
        dateLine = today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", java.util.Locale.UK)),
        movements = EVENING,
        palette = palette,
        voice = voice,
        speaking = speaking.value,
        colophon = "Morning Prayer opens at first light.\nTomorrow runs the other way — enter, praise,\nconfess, thank, and walk out into it.",
        finishLabel = "Finish prayer",
        background = com.prayerkey.manna.R.drawable.today_prayer_violet,
        onClose = onClose,
        onFinish = {
            context.getSharedPreferences("home_completed", android.content.Context.MODE_PRIVATE)
                .edit().putBoolean("${today}:evening", true).apply()
            onClose()
        },
        scale = if (night) NightScale else DayScale,
        after = buildMap {
            if (morningAsk.isNotBlank()) put("Thank", "morning-ask")
            put("Release", "release")
        },
        trailing = {
            Text(
                if (night) "Day" else "Night",
                Modifier.clickable { night = !night },
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 13.5.sp,
                color = palette.accent,
            )
        },
    ) { key ->
        when (key) {
            "morning-ask" -> LedgerNote(
                label = "This morning you asked",
                entry = morningAsk,
                invitation = "Tell Him how it went.",
                palette = palette,
            )
            "release" -> OfficeField(release, "What are you handing over tonight?", palette) { release = it }
        }
    }
}

private val EVENING = listOf(
    OfficeMovement(
        "Still", "The day is finished. Put it down",
        listOf("Father, the day is over. Whatever I did not get to will still be there in the morning, and it can wait there without me. I am putting it down now — all of it — and coming to You before I sleep."),
    ),
    OfficeMovement(
        "Review", "Walk back through the day with Him",
        listOf("Take me back through today. Show me where You were in it — in the ordinary parts I rushed past, in the small mercy I called luck, in the person You put in front of me. Show me what I handled well, and let me be glad about it without pretending it was all my own doing. And show me the moments I would rather not look at."),
        silence = "Stop here. Let the day come back to you.",
    ),
    OfficeMovement(
        "Thank", "For the day You got, not the one you planned",
        listOf("Thank You for what actually happened today. For the work I was able to do and the strength it took. For food, and a roof, and the people who spoke kindly to me without knowing I needed it. Thank You for keeping me through hours I never once thought about."),
    ),
    OfficeMovement(
        "Confess", "The parts you are not proud of",
        listOf(
            "Now the rest of it. The impatience. The words I chose badly and cannot take back. The thing I avoided because avoiding it was easier. The good I could have done today and did not do.",
            "I am not going to explain any of it away tonight. Forgive me. Wash the day off me, and do not let me carry a single hour of it into tomorrow.",
        ),
    ),
    OfficeMovement(
        "Forgive", "Put down what was done to you",
        listOf("And what was done to me today — the slight, the rudeness, the one who took and did not thank me — I am letting that go too. I will not lie in this bed rehearsing it. Help me forgive the way I have just been forgiven, tonight, before it hardens."),
    ),
    OfficeMovement(
        "Release", "Hand over what you cannot fix",
        listOf("There are things I cannot fix tonight. The unfinished work. The conversation still open. The money, the decision I cannot see clearly yet, the person I am worried about. I am handing them to You. You do not sleep — so there is no reason for both of us to be awake with this."),
    ),
    OfficeMovement(
        "Word", "The last thing you read tonight",
        scripture = OfficeVerse(
            "I will both lay me down in peace, and sleep: for thou, LORD, only makest me dwell in safety.",
            "Psalm 4:8",
        ),
        after = listOf("Not because the day was safe. Because You are."),
    ),
    OfficeMovement(
        "Intercede", "Remember those who will not sleep",
        listOf("Before I sleep, remember the ones who will not. The sick, and the people sitting up with them. The grieving, in the worst hours of the night. Those working through the dark so the rest of us can rest. The frightened, and the ones with nowhere safe to lie down. Be near them tonight in a way they can feel."),
    ),
    OfficeMovement(
        "Protect", "Over the house, over the night",
        listOf("Keep this house until morning. Keep my family, wherever they are sleeping tonight. Guard my mind while I am not watching it — no fear, no dread, nothing that leaves a mark. Let me wake whole."),
    ),
    OfficeMovement(
        "Rest", "You are not in charge tonight",
        listOf("Into Your hands I commend my spirit. I am not running anything tonight, and I am glad of it. Thank You for this day — all of it, the good and the hard. Give me sleep, and give me mercy in the morning."),
    ),
)
