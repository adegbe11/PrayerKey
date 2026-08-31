package com.prayerkey.manna.ui.morning

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.BookSerif
import com.prayerkey.manna.ui.theme.DisplaySerif
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The furniture both daily offices are printed on.
 *
 * Morning and Evening are the same book set twice, so the rubric, the gloss,
 * the dropped initial, the scripture, the silence and the ledger note live
 * here. Two copies would have drifted apart the first time either was touched,
 * and the whole point of an office is that it looks the same every day.
 */

/* ── the page ───────────────────────────────────────────────────────── */

data class OfficePalette(
    val paper: Color,
    val ink: Color,
    val ink2: Color,
    val accent: Color,
    val rule: Color,
    val field: Color,
)

/**
 * Daylight.
 *
 * The supplied designs specify #4C2A85, a quieter violet that suits paper.
 * Using it would put a second purple in an app that has exactly one, so the
 * brand colour stands in. One line, if that is the wrong call.
 */
val DayPalette = OfficePalette(
    paper = Color(0xFFFFFFFF),
    ink = Color(0xFF1A1A1A),
    ink2 = Color(0xFF6E6E73),
    accent = Pk.Oxblood,
    rule = Color(0xFFD8D4E0),
    field = Color(0xFFFAF9FC),
)

/**
 * Lamplight, for the evening office.
 *
 * Not a theme setting — a lamp you turn on at the top of the page. The evening
 * prayer is read in a dark room by somebody about to sleep, and a white page at
 * that hour is a physical objection to the thing being asked for. The accent
 * lifts because oxblood on near-black is unreadable at body size.
 */
val NightPalette = OfficePalette(
    paper = Color(0xFF15111F),
    ink = Color(0xFFEDE9F5),
    ink2 = Color(0xFF9B93AE),
    accent = Color(0xFFB9A0E8),
    rule = Color(0xFF3A3050),
    field = Color(0xFF1E1830),
)

/* ── the text ───────────────────────────────────────────────────────── */

data class OfficeVerse(val text: String, val reference: String)

data class OfficeMovement(
    val rubric: String,
    val gloss: String,
    val body: List<String> = emptyList(),
    val scripture: OfficeVerse? = null,
    /** Paragraphs printed after the scripture. */
    val after: List<String> = emptyList(),
    /** The page stops and says so. */
    val silence: String? = null,
    val declarations: List<String> = emptyList(),
)

/* ── the parts ──────────────────────────────────────────────────────── */

@Composable
fun RunningHead(
    title: String,
    date: java.time.LocalDate,
    palette: OfficePalette,
    onClose: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Close, "Close", tint = palette.ink, modifier = Modifier.size(16.dp)) }
        Spacer(Modifier.width(8.dp))
        Text(
            title.uppercase(),
            fontFamily = BookSerif,
            fontStyle = FontStyle.Italic,
            fontSize = 13.sp,
            letterSpacing = 1.4.sp,
            color = palette.ink,
        )
        Spacer(Modifier.weight(1f))
        Text(
            date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.UK)),
            fontFamily = BookSerif,
            fontStyle = FontStyle.Italic,
            fontSize = 13.sp,
            color = palette.ink2,
        )
        trailing?.let { Spacer(Modifier.width(10.dp)); it() }
    }
    Spacer(Modifier.height(5.dp))
    Box(Modifier.fillMaxWidth().height(1.5.dp).background(palette.ink))
}

@Composable
fun OfficeTitle(
    title: String,
    palette: OfficePalette,
    speaking: Boolean,
    onListen: () -> Unit,
) {
    Text(
        title,
        Modifier.fillMaxWidth(),
        fontFamily = DisplaySerif,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        color = palette.ink,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(8.dp))
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onListen),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (speaking) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            null, tint = palette.accent, modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (speaking) "Pause" else "Listen to this prayer",
            fontFamily = BookSerif,
            fontStyle = FontStyle.Italic,
            fontSize = 15.sp,
            color = palette.accent,
        )
    }
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(Modifier.width(52.dp).height(1.5.dp).background(palette.accent))
    }
}

@Composable
fun Movement(movement: OfficeMovement, palette: OfficePalette, dropCap: Boolean) {
    Text(
        movement.rubric.uppercase(),
        Modifier.fillMaxWidth(),
        fontFamily = UtilitySans,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.8.sp,
        color = palette.accent,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(3.dp))
    Text(
        movement.gloss,
        Modifier.fillMaxWidth(),
        fontFamily = BookSerif,
        fontStyle = FontStyle.Italic,
        fontSize = 13.5.sp,
        color = palette.ink2,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(14.dp))

    movement.body.forEachIndexed { i, paragraph ->
        /* The office opens with a dropped initial and the first word in small
           capitals, the way a printed prayer book opens. It marks the start of
           the whole thing, so it happens once. */
        val text = if (dropCap && i == 0) {
            buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 34.sp, color = palette.accent, fontWeight = FontWeight.SemiBold)) {
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
            color = palette.ink,
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
            color = palette.ink,
            textAlign = TextAlign.Justify,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            verse.reference,
            Modifier.fillMaxWidth(),
            fontFamily = BookSerif,
            fontStyle = FontStyle.Italic,
            fontSize = 14.5.sp,
            color = palette.ink2,
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
            color = palette.ink,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(bottom = 11.dp),
        )
    }

    movement.silence?.let { line ->
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(palette.accent.copy(alpha = .4f)))
        }
        Spacer(Modifier.height(14.dp))
        Text(
            line,
            Modifier.fillMaxWidth(),
            fontFamily = BookSerif,
            fontStyle = FontStyle.Italic,
            fontSize = 15.5.sp,
            color = palette.accent,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
    }

    movement.declarations.forEach { line ->
        Text(
            line,
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            fontFamily = BookSerif,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            color = palette.ink,
            textAlign = TextAlign.Center,
        )
    }
}

/** The one thing on the page nobody else could have written for this reader. */
@Composable
fun LedgerNote(label: String, entry: String, invitation: String, palette: OfficePalette) {
    Spacer(Modifier.height(14.dp))
    Row {
        Box(Modifier.width(2.dp).heightIn(min = 60.dp).background(palette.accent))
        Column(Modifier.padding(start = 14.dp)) {
            Text(
                label,
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = palette.accent,
            )
            Spacer(Modifier.height(4.dp))
            Text(entry, fontFamily = BookSerif, fontSize = 17.sp, lineHeight = 25.sp, color = palette.ink)
            Spacer(Modifier.height(10.dp))
            Text(
                invitation,
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                color = palette.ink2,
            )
        }
    }
}

@Composable
fun OfficeField(value: String, hint: String, palette: OfficePalette, onChange: (String) -> Unit) {
    Spacer(Modifier.height(6.dp))
    Box(
        Modifier.fillMaxWidth().heightIn(min = 88.dp).background(palette.field)
            .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        if (value.isEmpty()) {
            Text(
                hint,
                fontFamily = BookSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 19.sp,
                color = palette.ink2.copy(alpha = .7f),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(
                fontFamily = BookSerif,
                fontSize = 19.sp,
                lineHeight = 28.sp,
                color = palette.ink,
            ),
            cursorBrush = SolidColor(palette.accent),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Box(Modifier.fillMaxWidth().height(1.5.dp).background(palette.rule))
}

@Composable
fun OfficeClose(colophon: String, palette: OfficePalette) {
    Text(
        "Amen",
        Modifier.fillMaxWidth(),
        fontFamily = DisplaySerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        letterSpacing = 1.4.sp,
        color = palette.accent,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(30.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(palette.rule))
    Spacer(Modifier.height(20.dp))
    Text(
        colophon,
        Modifier.fillMaxWidth(),
        fontFamily = BookSerif,
        fontStyle = FontStyle.Italic,
        fontSize = 14.5.sp,
        lineHeight = 22.sp,
        color = palette.ink2,
        textAlign = TextAlign.Center,
    )
}

/* ── the voice ──────────────────────────────────────────────────────── */

/**
 * Reads an office aloud, rubrics included, and stops on Amen.
 *
 * The device's own engine: free, offline, and needing no recording session.
 * Hearing "Confess" before the confession is the point — the rubric is what
 * tells you what you are about to do.
 */
class OfficeVoice(private val speaking: MutableState<Boolean>) {
    var engine: TextToSpeech? = null
    var ready = false

    fun toggle(movements: List<OfficeMovement>) {
        val tts = engine ?: return
        if (speaking.value) { tts.stop(); speaking.value = false; return }
        if (!ready) return
        speaking.value = true
        movements.forEachIndexed { i, movement ->
            val mode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts.speak(movement.rubric, mode, null, "rubric-$i")
            movement.body.forEach { tts.speak(it, TextToSpeech.QUEUE_ADD, null, null) }
            movement.scripture?.let { tts.speak(it.text, TextToSpeech.QUEUE_ADD, null, null) }
            movement.after.forEach { tts.speak(it, TextToSpeech.QUEUE_ADD, null, null) }
            movement.declarations.forEach { tts.speak(it, TextToSpeech.QUEUE_ADD, null, null) }
        }
        tts.speak("Amen.", TextToSpeech.QUEUE_ADD, null, "office-end")
    }
}

@Composable
fun rememberOfficeVoice(speaking: MutableState<Boolean>): OfficeVoice {
    val context = LocalContext.current
    val voice = remember { OfficeVoice(speaking) }
    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status -> voice.ready = status == TextToSpeech.SUCCESS }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (utteranceId == "office-end") speaking.value = false
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { speaking.value = false }
        })
        voice.engine = tts
        onDispose { tts.stop(); tts.shutdown(); voice.engine = null }
    }
    return voice
}

@Composable
fun rememberSpeaking() = remember { mutableStateOf(false) }
