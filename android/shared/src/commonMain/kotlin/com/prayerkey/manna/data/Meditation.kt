package com.prayerkey.manna.data

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn


/**
 * Christian meditation: a verse, sat with.
 *
 * Every session has the same five movements - settle in, hear the Word, sit
 * with it, pray, and close - and what changes is the verse, the questions, and
 * how long the silences are. The spoken lines are short on purpose. Most of a
 * meditation is the quiet between them, and a longer session is mostly longer
 * quiet, not more talking.
 */
enum class MeditationFocus(val label: String, val blurb: String) {
    PRESENCE("God's presence", "Be still and know Him"),
    PEACE("Peace and anxiety", "Find calm in God's hands"),
    GRATITUDE("Gratitude", "A thankful heart"),
    SLEEP("Sleep", "Rest in His promises"),
    HEALING("Healing", "Peace for body and mind"),
    DIRECTION("Direction", "Clarity for your next step"),
    PSALM("Psalm meditation", "Pray the Psalms slowly"),
    CUSTOM("Your own", "Bring what is on your heart"),
}

val MEDITATION_MINUTES = listOf(3, 5, 10, 15, 20, 30)

/**
 * How the guide sounds.
 *
 * The phone's own speech engine reads the meditation, so these are settings of
 * one voice - pitch and pace - rather than four recorded narrators. Named for
 * how they sound, not given people's names they do not have.
 */
enum class MeditationVoice(val label: String, val blurb: String, val pitch: Float, val rate: Float) {
    WARM("Warm", "Calm and unhurried", .92f, .80f),
    GENTLE("Gentle", "Soft and clear", 1.04f, .84f),
    DEEP("Deep", "Low and soothing", .74f, .78f),
    SLOW("Slow", "For winding down", .96f, .70f),
}

/** The sound under the voice, made on the phone - offline, and nobody's recording. */
enum class Ambience(val label: String, val blurb: String) {
    NONE("None", "Just the voice"),
    NATURE("Nature", "Wind and birds"),
    RAIN("Rain", "Gentle rain"),
    OCEAN("Ocean", "Slow waves"),
    PIANO("Piano", "Soft notes"),
    HARP("Harp", "Quiet strings"),
}

data class MeditationSetup(
    val focus: MeditationFocus = MeditationFocus.PEACE,
    val custom: String = "",
    val minutes: Int = 10,
    val voice: MeditationVoice = MeditationVoice.WARM,
    val ambience: Ambience = Ambience.NATURE,
)

/** One movement of a session: its name, the lines spoken, and its share of the time. */
data class MeditationChapter(val title: String, val subtitle: String, val lines: List<String>, val share: Float)

data class MeditationScript(
    val verse: String,
    val ref: String,
    val chapters: List<MeditationChapter>,
) {
    /** Seconds at which each chapter begins, for a session of [totalSeconds]. */
    fun starts(totalSeconds: Int): List<Int> {
        var at = 0f
        return chapters.map { c -> at.toInt().also { at += c.share * totalSeconds } }
    }
}

private data class FocusText(
    val verse: String,
    val ref: String,
    val word: List<String>,
    val reflect: List<String>,
    val pray: List<String>,
    val close: String,
)

private val TEXTS: Map<MeditationFocus, FocusText> = mapOf(
    MeditationFocus.PRESENCE to FocusText(
        "Be still, and know that I am God.", "Psalm 46:10",
        listOf("Be still.", "Be still, and know.", "Be still, and know that I am God."),
        listOf(
            "Notice that God is already here. You did not have to bring Him.",
            "What makes it hard to be still right now? Hand it to Him.",
            "Rest in knowing, not in doing.",
        ),
        listOf(
            "Lord, You are here. I do not need to find You. Let me simply be with You.",
            "Quiet the noise in me, and let Your presence be enough.",
        ),
        "Carry this stillness into the rest of your day. He goes with you.",
    ),
    MeditationFocus.PEACE to FocusText(
        "Cast all your anxiety on Him, because He cares for you.", "1 Peter 5:7",
        listOf(
            "Cast all your anxiety on Him.",
            "Not some of it. All of it.",
            "Because He cares for you.",
        ),
        listOf(
            "Name the thing that has been weighing on you. Say it plainly to God.",
            "Picture yourself setting it down in His hands.",
            "Notice how your breathing changes when you are not holding it.",
        ),
        listOf(
            "Father, I give You what I have been carrying. Hold it, so I do not have to.",
            "Guard my heart with Your peace, the peace I cannot explain.",
        ),
        "When the worry returns, give it back again. He does not tire of taking it.",
    ),
    MeditationFocus.GRATITUDE to FocusText(
        "Give thanks to the Lord, for He is good; His love endures forever.", "Psalm 107:1",
        listOf(
            "Give thanks to the Lord.",
            "For He is good.",
            "His love endures forever.",
        ),
        listOf(
            "Bring to mind one thing from today you are grateful for.",
            "Now a person. Picture their face, and thank God for them.",
            "Now something hard that God carried you through.",
        ),
        listOf(
            "Lord, thank You. For what I noticed, and for what I overlooked.",
            "Let gratitude be the first thing I reach for, and not the last.",
        ),
        "Look for one more thing to be thankful for before the day ends.",
    ),
    MeditationFocus.SLEEP to FocusText(
        "In peace I will lie down and sleep, for You alone, Lord, make me dwell in safety.", "Psalm 4:8",
        listOf(
            "In peace I will lie down and sleep.",
            "For You alone, Lord,",
            "make me dwell in safety.",
        ),
        listOf(
            "Let your body grow heavy. You do not have to hold anything up.",
            "Give God what is unfinished from today. It will keep until morning.",
            "He does not sleep. He keeps watch while you rest.",
        ),
        listOf(
            "Father, I lay down today, and everything in it, in Your hands.",
            "Watch over my home, and give me rest.",
        ),
        "Sleep now. You are safe in Him.",
    ),
    MeditationFocus.HEALING to FocusText(
        "He heals the brokenhearted and binds up their wounds.", "Psalm 147:3",
        listOf(
            "He heals the brokenhearted.",
            "And binds up their wounds.",
            "He heals. He binds up.",
        ),
        listOf(
            "Where do you need healing, in your body, your heart, or your memories?",
            "Invite God into that place. You do not have to fix it first.",
            "Rest there, and let Him be near.",
        ),
        listOf(
            "Lord, You know the wound I carry. Heal it at the root.",
            "Give me patience with how healing takes its time, and trust while I wait.",
        ),
        "Healing is often slow. He is working, even now.",
    ),
    MeditationFocus.DIRECTION to FocusText(
        "Trust in the Lord with all your heart, and lean not on your own understanding.", "Proverbs 3:5",
        listOf(
            "Trust in the Lord with all your heart.",
            "Lean not on your own understanding.",
            "In all your ways acknowledge Him, and He will make your paths straight.",
        ),
        listOf(
            "Bring the decision in front of you to God.",
            "Where are you leaning on your own understanding?",
            "Listen. Is there a peace, or an unrest, about one of the paths?",
        ),
        listOf(
            "Father, I do not see the whole road. Show me the next step.",
            "Give me the humility to wait, and the courage to move when You say go.",
        ),
        "Take one faithful step. He will light the next one.",
    ),
    MeditationFocus.PSALM to FocusText(
        "The Lord is my shepherd; I shall not want.", "Psalm 23:1",
        listOf(
            "The Lord is my shepherd.",
            "I shall not want.",
            "He makes me lie down in green pastures. He leads me beside still waters.",
        ),
        listOf(
            "Which word in this psalm stays with you? Repeat it slowly.",
            "Where do you need a shepherd today?",
            "Let Him lead you to still water.",
        ),
        listOf(
            "Shepherd, lead me today. I lack nothing that You will not provide.",
            "Restore my soul, and walk with me through every valley.",
        ),
        "Goodness and mercy will follow you, all the days of your life.",
    ),
)

private val SETTLE = listOf(
    "Find a comfortable place to sit, and let your shoulders drop.",
    "Breathe in slowly. And breathe out.",
    "You are here, and God is here. There is nowhere else you need to be.",
)

/** The session, written for this focus and this length. */
fun meditationScript(setup: MeditationSetup): MeditationScript {
    val text = TEXTS[setup.focus] ?: TEXTS.getValue(MeditationFocus.PEACE)
    val custom = setup.focus == MeditationFocus.CUSTOM && setup.custom.isNotBlank()
    val pray = if (custom) {
        listOf("Lord, I bring You ${setup.custom.trim()}.", "You know it better than I do. I leave it with You.")
    } else {
        text.pray
    }
    return MeditationScript(
        verse = text.verse,
        ref = text.ref,
        chapters = listOf(
            MeditationChapter("Settle in", "Breathing and presence", SETTLE, .12f),
            MeditationChapter("God's Word", text.ref, listOf(text.verse) + text.word, .22f),
            MeditationChapter("Reflection", "Sit with it", text.reflect, .42f),
            MeditationChapter("Prayer", "A moment to respond", pray, .14f),
            MeditationChapter("Closing", "Rest in His peace", listOf(text.close, "Amen."), .10f),
        ),
    )
}

/** What a person has done with meditation over time. */
data class MeditationRecord(
    val sessions: Int = 0,
    val seconds: Long = 0,
    val topics: Set<String> = emptySet(),
    val verses: Set<String> = emptySet(),
    val days: Set<Long> = emptySet(),
) {
    fun streak(today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): Int {
        var day = today.toEpochDays().toLong()
        if (day !in days) day -= 1
        var run = 0
        while (day in days) {
            run++
            day--
        }
        return run
    }
}


