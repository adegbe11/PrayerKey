package com.prayerkey.manna.ui.morning

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Afternoon Prayer — the middle of the day.
 *
 * The shortest of the four, and the only one prayed with your eyes open and
 * something still to finish. Morning hands the day over before it starts;
 * Evening reviews it once it is spent. Nothing stood between them, which is
 * where most days are actually lost - not in how they begin, but somewhere
 * around three o'clock when the morning's intention has quietly worn off.
 *
 * So it is four movements and about two minutes, and it does not ask anyone to
 * sit down. Stop, tell the truth about how it has gone, ask for what the rest
 * of it needs, and go back in.
 */
@Composable
fun AfternoonPrayerScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val palette = DawnPalette

    val speaking = rememberSpeaking()
    val voice = rememberOfficeVoice(speaking)

    OfficeFlip(
        title = "Afternoon Prayer",
        dateLine = today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.UK)),
        movements = AFTERNOON,
        palette = palette,
        voice = voice,
        speaking = speaking.value,
        colophon = "Evening Prayer opens when the work is down.\nReview, thanksgiving, confession, release, rest.",
        background = com.prayerkey.manna.R.drawable.today_prayer_violet,
        onClose = onClose,
        finishLabel = "Finish prayer",
        onFinish = {
            context.getSharedPreferences("home_completed", android.content.Context.MODE_PRIVATE)
                .edit().putBoolean("${today}:afternoon", true).apply()
            onClose()
        },
        length = "Two minutes, standing, in the middle of whatever you are doing.",
    )
}

private val AFTERNOON = listOf(
    OfficeMovement(
        "Stop", "Come off the day for a moment",
        listOf(
            "Father, I am stopping. Not because the work is finished — it is not — but because I have been going since morning and I have not looked up once. Here I am. The half of the day that is gone is Yours, and so is the half that is left.",
        ),
    ),
    OfficeMovement(
        "Tell the truth", "How it has actually gone",
        listOf(
            "You saw all of it. What I got right, and the parts I would rather not go over: the impatience, the sharp answer, the thing I said about someone who was not in the room. I am not going to dress any of it up at three in the afternoon.",
            "Forgive me. Let me not carry it into the rest of today, and let me not repeat it before the day is out.",
        ),
    ),
    OfficeMovement(
        "Ask", "For the hours still in front of you",
        scripture = OfficeVerse(
            "But they that wait upon the LORD shall renew their strength.",
            "Isaiah 40:31",
        ),
        after = listOf(
            "I am tired, and there is more to do. Renew my strength — not so that I can do more, but so that I can do what is actually mine to do, and do it well. Steady my attention. Keep me kind when I am rushed. Give me wisdom for the next conversation, the next decision, the next hour.",
        ),
    ),
    OfficeMovement(
        "Go", "Back into it, with Him",
        listOf(
            "I am going back in now. Go with me. Let the rest of this day be worked as unto You and not as unto myself, and bring me to the evening with nothing I have to hide.",
        ),
    ),
)
