package com.prayerkey.manna.ui.morning

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.prayerkey.manna.data.MannaStore
import com.prayerkey.manna.data.OfficeStore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Morning Prayer, set as a printed office rather than a screen.
 *
 * Eleven movements, each announced by a rubric and a gloss — the shape a prayer
 * book has used for four hundred years, and the reason one is usable at six in
 * the morning by somebody who has never prayed formally. You are never asked
 * what to do next; the page tells you.
 *
 *     Enter · Praise · Confess · Thank · Word · Surrender
 *     Ask · Intercede · Protect · Declare · Yield
 *
 * The page furniture is in [Office]; Evening Prayer is the same book set the
 * other way round.
 *
 * What is written in the Ask movement is kept, because tonight the evening
 * office reads it back and asks how it went. That thread is the reason these
 * are one book rather than two screens.
 */
@Composable
fun MorningPrayerScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val palette = DayPalette

    val speaking = rememberSpeaking()
    val voice = rememberOfficeVoice(speaking)

    var ask by remember { mutableStateOf(OfficeStore.morningAsk(context, today)) }
    DisposableEffect(Unit) { onDispose { OfficeStore.setMorningAsk(context, ask, today) } }

    /* The most recent thing this person actually asked God for. Absent on a
       first run, and the movement simply omits the note rather than inventing
       one — a fabricated memory would be worse than none. */
    val ledger = remember {
        runCatching { MannaStore.of(context).prayerJournal().firstOrNull() }.getOrNull()
    }

    Column(
        Modifier.fillMaxSize().background(palette.paper).verticalScroll(rememberScrollState())
            .statusBarsPadding().padding(horizontal = 26.dp).navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(10.dp))
        RunningHead("Morning Prayer", today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.UK)), palette, onClose)
        Spacer(Modifier.height(26.dp))
        OfficeTitle("Morning Prayer", palette, speaking.value) { voice.toggle(MORNING) }
        Spacer(Modifier.height(26.dp))

        MORNING.forEachIndexed { index, movement ->
            Movement(movement, palette, dropCap = index == 0)

            if (movement.rubric == "Thank" && ledger != null) {
                val when_ = Instant.ofEpochMilli(ledger.createdAt).atZone(ZoneId.systemDefault())
                    .toLocalDate().format(DateTimeFormatter.ofPattern("d MMMM", Locale.UK))
                LedgerNote(
                    label = "From your ledger, $when_",
                    entry = ledger.request.ifBlank { ledger.title },
                    invitation = "Thank Him for it now if He did it.",
                    palette = palette,
                )
            }
            if (movement.rubric == "Ask") {
                OfficeField(ask, "Write it plainly. Nobody sees this but you.", palette) { ask = it }
            }
            Spacer(Modifier.height(30.dp))
        }

        OfficeClose(
            "Return tonight for Evening Prayer.\nReview, thanksgiving, confession, release, rest.",
            palette,
        )
        androidx.compose.material3.TextButton(onClick = {
            context.getSharedPreferences("home_completed", android.content.Context.MODE_PRIVATE).edit().putBoolean("${today}:morning", true).apply()
            onClose()
        }) { androidx.compose.material3.Text("Finish prayer") }
        Spacer(Modifier.height(40.dp))
    }
}

private val MORNING = listOf(
    OfficeMovement(
        "Enter", "Become aware of God",
        listOf("Father, before this day takes me, I am here. I am putting down the phone, the list, and the worry I woke up with. For the next few minutes there is nothing else — only You and me."),
    ),
    OfficeMovement(
        "Praise", "Worship Him for who He is",
        listOf("You were God before I woke, and You will be God long after this day is spent. Holy. Faithful. Merciful. Unchanging. Kingdoms rise and fall and You do not move. You do not need me, and still You want me. Before I ask You for a single thing, let me say plainly what You are: good. I worship You."),
    ),
    OfficeMovement(
        "Confess", "Examine your heart, then receive mercy",
        listOf(
            "Now search my heart and tell me what You find. Show me what is in me that should not go into this day — the resentment I have been feeding, the sharp word I have been rehearsing, the thing I keep doing and keep excusing. Forgive what I have thought, said and done, and forgive what I left undone.",
            "I am not going to argue with You about any of it. Have mercy on me — not because I have earned it, but because mercy is who You are. Blot it out, and let me walk into this day clean.",
        ),
    ),
    OfficeMovement(
        "Thank", "Remember what He has done",
        listOf("Thank You for waking me. Many did not wake this morning. Thank You for carrying me through the night while I was not watching, for breath in my body and strength in my legs, for the people under this roof and the ones far from it that I love anyway. Thank You for the prayers You have already answered that I never came back to acknowledge. Thank You that none of it was earned."),
    ),
    OfficeMovement(
        "Word", "Stop talking. Hear from Him",
        scripture = OfficeVerse(
            "Cause me to hear thy lovingkindness in the morning; for in thee do I trust: cause me to know the way wherein I should walk; for I lift up my soul unto thee.",
            "Psalm 143:8",
        ),
        after = listOf("Let this be the one thing I carry: You will show me the way. I do not have to see the whole road to take the next step on it."),
        silence = "Stop reading here. Be quiet a while.",
    ),
    OfficeMovement(
        "Surrender", "Your kingdom come, Your will be done",
        listOf("So this day is Yours before it is mine. My plans, my schedule, the decisions I have made and the ones I am still arguing with myself about — take them. Your will, not mine. If You want to change the order of today, change it. Order my steps, and go into every room before I walk into it."),
    ),
    OfficeMovement(
        "Ask", "Bring Him what you actually need",
        listOf("Now I bring You what I need. Wisdom — the real kind, that knows when to speak and when to keep quiet. Provision for what today requires, and freedom from anxiety about the gap. Strength in my body and clarity in my head. Bless the work of my hands, and let what I build stand and be useful to somebody. And this, which You already know:"),
    ),
    OfficeMovement(
        "Intercede", "Carry somebody other than yourself",
        listOf("Remember the people I love, and meet them where I cannot reach. Remember my family and my friends, the ones who asked me to pray and the ones too tired to ask. Remember the widow, the prisoner, the refugee and the sick. Remember Your church. Remember the leaders of this land — give them a conscience they cannot ignore."),
    ),
    OfficeMovement(
        "Protect", "Lead us not into temptation",
        listOf("Lead me not into temptation. Deliver me from evil. Keep me from danger seen and unseen, and from the traps I walk into with my eyes wide open. Cover my mind, my body, my road, my home, my work and my name. Let nothing sent against me find me."),
    ),
    OfficeMovement(
        "Declare", "Carry His word into the day, out loud",
        declarations = listOf(
            "The Lord is with me today.",
            "He gives me wisdom when I ask.",
            "I will not be controlled by fear.",
            "I will walk in love and integrity.",
            "My strength comes from the Lord.",
        ),
    ),
    OfficeMovement(
        "Yield", "Trust Him with the outcome",
        listOf("Father, I have brought this day to You. I am not going to keep asking. I trust You with what I understand and with what I cannot see. Whatever comes, help me follow where You lead."),
    ),
)
