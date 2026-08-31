package com.prayerkey.manna.ui.morning

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.MannaStore
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
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
 * Three things here are not decoration:
 *
 *  - **The silence.** After the scripture the page stops and says so. A prayer
 *    app that never stops talking has misunderstood what it is for.
 *  - **The ledger.** The Thank movement pulls a real request out of the user's
 *    own prayer journal and asks them to thank God for it if He answered. That
 *    is the one thing on the page nobody else could have written.
 *  - **The ask.** A field with no reader but the person writing.
 *
 * Audio is the device's own speech engine. It is free, it is offline, and it
 * needs no recording session — the alternative was a paid voice service, which
 * this app does not use anywhere.
 */
@Composable
fun MorningPrayerScreen(onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val context = LocalContext.current
    val today = remember { LocalDate.now() }

    var speaking by remember { mutableStateOf(false) }
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }
    var ready by remember { mutableStateOf(false) }
    var ask by remember { mutableStateOf("") }

    /* The most recent thing this person actually asked God for. Absent on a
       first run, and the movement simply omits the note rather than inventing
       one — a fabricated memory would be worse than none. */
    val ledger = remember {
        runCatching { MannaStore(context.applicationContext).prayerJournal().firstOrNull() }.getOrNull()
    }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status -> ready = status == TextToSpeech.SUCCESS }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (utteranceId == "morning-end") speaking = false
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { speaking = false }
        })
        engine = tts
        onDispose { tts.stop(); tts.shutdown(); engine = null }
    }

    fun toggleAudio() {
        val tts = engine ?: return
        if (speaking) { tts.stop(); speaking = false; return }
        if (!ready) return
        speaking = true
        MOVEMENTS.forEachIndexed { i, movement ->
            val mode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts.speak(movement.rubric, mode, null, "rubric-$i")
            movement.body.forEach { tts.speak(it, TextToSpeech.QUEUE_ADD, null, null) }
            movement.scripture?.let { tts.speak(it.text, TextToSpeech.QUEUE_ADD, null, null) }
            movement.declarations.forEach { tts.speak(it, TextToSpeech.QUEUE_ADD, null, null) }
        }
        tts.speak("Amen.", TextToSpeech.QUEUE_ADD, null, "morning-end")
    }

    Column(
        Modifier.fillMaxSize().background(Paper).verticalScroll(rememberScrollState())
            .statusBarsPadding().padding(horizontal = 26.dp).navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(10.dp))

        // ── running head ────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(30.dp).clip(CircleShape).clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.Close, "Close", tint = Ink, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.width(8.dp))
            Text(
                "MORNING PRAYER",
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                letterSpacing = 1.4.sp,
                color = Ink,
            )
            Spacer(Modifier.weight(1f))
            Text(
                today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.UK)),
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                color = Ink2,
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth().height(1.5.dp).background(Ink))
        Spacer(Modifier.height(26.dp))

        // ── title and audio ─────────────────────────────────────────────
        Text(
            "Morning Prayer",
            Modifier.fillMaxWidth(),
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            color = Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().clickable { toggleAudio() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (speaking) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                null, tint = Violet, modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (speaking) "Pause" else "Listen to this prayer",
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 15.sp,
                color = Violet,
            )
        }
        Spacer(Modifier.height(20.dp))
        Box(Modifier.align(Alignment.CenterHorizontally).width(52.dp).height(1.5.dp).background(Violet))
        Spacer(Modifier.height(26.dp))

        // ── the office ──────────────────────────────────────────────────
        MOVEMENTS.forEachIndexed { index, movement ->
            Movement(movement, first = index == 0)
            if (movement.rubric == "Thank" && ledger != null) {
                LedgerNote(
                    when_ = Instant.ofEpochMilli(ledger.createdAt).atZone(ZoneId.systemDefault())
                        .toLocalDate().format(DateTimeFormatter.ofPattern("d MMMM", Locale.UK)),
                    request = ledger.request.ifBlank { ledger.title },
                )
            }
            if (movement.rubric == "Ask") {
                AskField(ask) { ask = it }
            }
            Spacer(Modifier.height(30.dp))
        }

        Text(
            "Amen",
            Modifier.fillMaxWidth(),
            fontFamily = DisplaySerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 36.sp,
            letterSpacing = 1.4.sp,
            color = Violet,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(30.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Rule))
        Spacer(Modifier.height(20.dp))
        Text(
            "Evening Prayer opens at half past nine.\nTonight runs the other way — review, thanksgiving,\nconfession, release, rest.",
            Modifier.fillMaxWidth(),
            fontFamily = BookSerif,
            fontStyle = FontStyle.Italic,
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
            color = Ink2,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Movement(movement: MorningMovement, first: Boolean) {
    Text(
        movement.rubric.uppercase(),
        Modifier.fillMaxWidth(),
        fontFamily = UtilitySans,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.8.sp,
        color = Violet,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(3.dp))
    Text(
        movement.gloss,
        Modifier.fillMaxWidth(),
        fontFamily = BookSerif,
        fontStyle = FontStyle.Italic,
        fontSize = 13.5.sp,
        color = Ink2,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(14.dp))

    movement.body.forEachIndexed { i, paragraph ->
        /* The opening word of the office is set in small capitals and the first
           letter is dropped, the way a printed prayer book opens. It marks the
           beginning of the whole thing, so it happens once. */
        val text = if (first && i == 0) {
            buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 34.sp, color = Violet, fontWeight = FontWeight.SemiBold)) {
                    append(paragraph.first())
                }
                withStyle(SpanStyle(letterSpacing = 1.2.sp)) {
                    append(paragraph.drop(1).takeWhile { it != ',' }.uppercase())
                }
                append(paragraph.dropWhile { it != ',' })
            }
        } else {
            buildAnnotatedString { append(paragraph) }
        }
        Text(
            text,
            fontFamily = BookSerif,
            fontSize = 20.sp,
            lineHeight = 29.sp,
            color = Ink,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(bottom = 11.dp),
        )
    }

    movement.scripture?.let { verse ->
        Text(
            verse.text,
            Modifier.padding(horizontal = 14.dp),
            fontFamily = BookSerif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            fontSize = 19.sp,
            lineHeight = 28.sp,
            color = Ink,
            textAlign = TextAlign.Justify,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            verse.reference,
            Modifier.fillMaxWidth(),
            fontFamily = BookSerif,
            fontStyle = FontStyle.Italic,
            fontSize = 14.5.sp,
            color = Ink2,
            textAlign = TextAlign.End,
        )
        Spacer(Modifier.height(14.dp))
    }

    movement.after.forEach { paragraph ->
        Text(
            paragraph,
            fontFamily = BookSerif,
            fontSize = 20.sp,
            lineHeight = 29.sp,
            color = Ink,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(bottom = 11.dp),
        )
    }

    if (movement.silence) {
        Spacer(Modifier.height(22.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Violet.copy(alpha = .35f)))
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "Stop reading here. Be quiet a while.",
            Modifier.fillMaxWidth(),
            fontFamily = BookSerif,
            fontStyle = FontStyle.Italic,
            fontSize = 15.5.sp,
            color = Violet,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
    }

    movement.declarations.forEach { line ->
        Text(
            line,
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            fontFamily = BookSerif,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            color = Ink,
            textAlign = TextAlign.Center,
        )
    }
}

/** The one thing on this page nobody else could have written for this reader. */
@Composable
private fun LedgerNote(when_: String, request: String) {
    Spacer(Modifier.height(14.dp))
    Row {
        Box(Modifier.width(2.dp).heightIn(min = 60.dp).background(Violet))
        Column(Modifier.padding(start = 14.dp)) {
            Text(
                "From your ledger, $when_",
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = Violet,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                request,
                fontFamily = BookSerif,
                fontSize = 17.sp,
                lineHeight = 25.sp,
                color = Ink,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Thank Him for it now if He did it.",
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                color = Ink2,
            )
        }
    }
}

@Composable
private fun AskField(value: String, onChange: (String) -> Unit) {
    Spacer(Modifier.height(6.dp))
    Box(
        Modifier.fillMaxWidth().heightIn(min = 88.dp).background(Field)
            .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        if (value.isEmpty()) {
            Text(
                "Write it plainly. Nobody sees this but you.",
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 19.sp,
                color = Placeholder,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = BookSerif,
                fontSize = 19.sp,
                lineHeight = 28.sp,
                color = Ink,
            ),
            cursorBrush = SolidColor(Violet),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Box(Modifier.fillMaxWidth().height(1.5.dp).background(Rule))
}

/* ── the office itself ─────────────────────────────────────────────── */

private data class Verse(val text: String, val reference: String)

private data class MorningMovement(
    val rubric: String,
    val gloss: String,
    val body: List<String>,
    val scripture: Verse? = null,
    /** Paragraphs printed after the scripture. */
    val after: List<String> = emptyList(),
    val silence: Boolean = false,
    val declarations: List<String> = emptyList(),
)

private val MOVEMENTS = listOf(
    MorningMovement(
        "Enter", "Become aware of God",
        listOf("Father, before this day takes me, I am here. I am putting down the phone, the list, and the worry I woke up with. For the next few minutes there is nothing else — only You and me."),
    ),
    MorningMovement(
        "Praise", "Worship Him for who He is",
        listOf("You were God before I woke, and You will be God long after this day is spent. Holy. Faithful. Merciful. Unchanging. Kingdoms rise and fall and You do not move. You do not need me, and still You want me. Before I ask You for a single thing, let me say plainly what You are: good. I worship You."),
    ),
    MorningMovement(
        "Confess", "Examine your heart, then receive mercy",
        listOf(
            "Now search my heart and tell me what You find. Show me what is in me that should not go into this day — the resentment I have been feeding, the sharp word I have been rehearsing, the thing I keep doing and keep excusing. Forgive what I have thought, said and done, and forgive what I left undone.",
            "I am not going to argue with You about any of it. Have mercy on me — not because I have earned it, but because mercy is who You are. Blot it out, and let me walk into this day clean.",
        ),
    ),
    MorningMovement(
        "Thank", "Remember what He has done",
        listOf("Thank You for waking me. Many did not wake this morning. Thank You for carrying me through the night while I was not watching, for breath in my body and strength in my legs, for the people under this roof and the ones far from it that I love anyway. Thank You for the prayers You have already answered that I never came back to acknowledge. Thank You that none of it was earned."),
    ),
    MorningMovement(
        "Word", "Stop talking. Hear from Him",
        emptyList(),
        scripture = Verse(
            "Cause me to hear thy lovingkindness in the morning; for in thee do I trust: cause me to know the way wherein I should walk; for I lift up my soul unto thee.",
            "Psalm 143:8",
        ),
        after = listOf("Let this be the one thing I carry: You will show me the way. I do not have to see the whole road to take the next step on it."),
        silence = true,
    ),
    MorningMovement(
        "Surrender", "Your kingdom come, Your will be done",
        listOf("So this day is Yours before it is mine. My plans, my schedule, the decisions I have made and the ones I am still arguing with myself about — take them. Your will, not mine. If You want to change the order of today, change it. Order my steps, and go into every room before I walk into it."),
    ),
    MorningMovement(
        "Ask", "Bring Him what you actually need",
        listOf("Now I bring You what I need. Wisdom — the real kind, that knows when to speak and when to keep quiet. Provision for what today requires, and freedom from anxiety about the gap. Strength in my body and clarity in my head. Bless the work of my hands, and let what I build stand and be useful to somebody. And this, which You already know:"),
    ),
    MorningMovement(
        "Intercede", "Carry somebody other than yourself",
        listOf("Remember the people I love, and meet them where I cannot reach. Remember my family and my friends, the ones who asked me to pray and the ones too tired to ask. Remember the widow, the prisoner, the refugee and the sick. Remember Your church. Remember the leaders of this land — give them a conscience they cannot ignore."),
    ),
    MorningMovement(
        "Protect", "Lead us not into temptation",
        listOf("Lead me not into temptation. Deliver me from evil. Keep me from danger seen and unseen, and from the traps I walk into with my eyes wide open. Cover my mind, my body, my road, my home, my work and my name. Let nothing sent against me find me."),
    ),
    MorningMovement(
        "Declare", "Carry His word into the day, out loud",
        emptyList(),
        declarations = listOf(
            "The Lord is with me today.",
            "He gives me wisdom when I ask.",
            "I will not be controlled by fear.",
            "I will walk in love and integrity.",
            "My strength comes from the Lord.",
        ),
    ),
    MorningMovement(
        "Yield", "Trust Him with the outcome",
        listOf("Father, I have brought this day to You. I am not going to keep asking. I trust You with what I understand and with what I cannot see. Whatever comes, help me follow where You lead."),
    ),
)

/* ── the page's own palette ─────────────────────────────────────────── */

private val Paper = Color(0xFFFFFFFF)
private val Ink = Color(0xFF1A1A1A)
private val Ink2 = Color(0xFF6E6E73)
private val Rule = Color(0xFFD8D4E0)
private val Field = Color(0xFFFAF9FC)
private val Placeholder = Color(0xFFB0AAC0)

/**
 * The accent, taken from the brand rather than the mockup.
 *
 * The supplied design specified #4C2A85, a quieter violet that suits printed
 * paper. Using it here would put a second purple in an app that has exactly
 * one, and the brand colour is the thing I have been told twice not to move.
 * Say the word and this becomes 0xFF4C2A85 — it is one line.
 */
private val Violet = Pk.Oxblood
