package com.prayerkey.manna.ui.home

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.UtilitySans
import java.util.Locale

/**
 * The devotional read aloud.
 *
 * This replaces a headphones glyph in the top bar, which started the voice and
 * then gave nothing back: no sign it had begun until sound arrived, no
 * position, no speed, and no way to pause and keep your place.
 *
 * WHAT IS REAL HERE, AND WHAT CANNOT BE
 *
 * The voice is the device's own text-to-speech. There is no audio file, and
 * that rules out one thing people expect from a player.
 *
 * A waveform is a picture of a recording's amplitude. With nothing recorded,
 * drawing one would be decoration in the shape of information, and a progress
 * control must not invent its own reading. The track here is plain, the way
 * Apple's own scrubber is whenever it has no waveform to show.
 *
 * Position is recoverable, and is real. Android reports onRangeStart for each
 * span it is about to speak, so the bar follows the character the voice has
 * actually reached rather than a timer animating hopefully alongside it.
 * Duration is derived from the word count at the chosen rate, so it is an
 * estimate; it is honest about being one and settles once speech begins.
 *
 * Pause is stop-and-resume, because TTS has no pause. Holding the character
 * offset makes that invisible: play continues the sentence you were on instead
 * of starting the devotional again, which is the behaviour that matters and
 * the reason the old button got pressed once and never twice.
 */

private const val WORDS_PER_MINUTE = 155f
private val SPEEDS = listOf(0.75f, 1f, 1.25f, 1.5f)

class DevotionalVoice internal constructor(private val text: String) {
    internal var engine: TextToSpeech? = null

    var ready by mutableStateOf(false)
        internal set
    var speaking by mutableStateOf(false)
        internal set
    var spokenChars by mutableIntStateOf(0)
        internal set
    var speed by mutableFloatStateOf(1f)
        internal set

    val total: Int get() = text.length.coerceAtLeast(1)
    val fraction: Float get() = (spokenChars.toFloat() / total).coerceIn(0f, 1f)

    /** Seconds, from the word count at the chosen rate. An estimate, and only that. */
    val estimatedSeconds: Int
        get() = ((text.split(' ').size / (WORDS_PER_MINUTE * speed)) * 60f).toInt()

    val elapsedSeconds: Int get() = (estimatedSeconds * fraction).toInt()

    fun toggle() {
        val tts = engine ?: return
        if (speaking) {
            tts.stop()
            speaking = false
        } else {
            // a finished reading starts again; an interrupted one continues
            if (spokenChars >= text.length) spokenChars = 0
            tts.speak(
                text.substring(spokenChars.coerceIn(0, text.length)),
                TextToSpeech.QUEUE_FLUSH, null, UTTERANCE,
            )
            speaking = true
        }
    }

    fun cycleSpeed() {
        val index = SPEEDS.indexOf(speed).takeIf { it >= 0 } ?: 1
        speed = SPEEDS[(index + 1) % SPEEDS.size]
        engine?.setSpeechRate(BASE_RATE * speed)
        if (speaking) {
            // re-issue at the new rate, from where the voice had reached
            engine?.speak(
                text.substring(spokenChars.coerceIn(0, text.length)),
                TextToSpeech.QUEUE_FLUSH, null, UTTERANCE,
            )
        }
    }

    internal companion object {
        const val UTTERANCE = "devotional-voice"
        const val BASE_RATE = .88f
    }
}

@Composable
fun rememberDevotionalVoice(text: String): DevotionalVoice {
    val context = LocalContext.current
    val voice = remember(text) { DevotionalVoice(text) }

    DisposableEffect(text) {
        /* The offset the listener reports is relative to whatever was handed to
           speak(), which after a resume is a substring - so the base is added
           back to keep progress absolute. */
        var base = 0
        val tts = TextToSpeech(context.applicationContext) { status ->
            voice.ready = status == TextToSpeech.SUCCESS
        }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                base = voice.spokenChars
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                voice.spokenChars = base + end
            }

            override fun onDone(utteranceId: String?) {
                voice.spokenChars = voice.total
                voice.speaking = false
            }

            @Deprecated("required by UtteranceProgressListener")
            override fun onError(utteranceId: String?) {
                voice.speaking = false
            }
        })
        voice.engine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
            voice.engine = null
        }
    }

    LaunchedEffect(voice.ready) {
        if (voice.ready) {
            voice.engine?.language = Locale.getDefault()
            voice.engine?.setSpeechRate(DevotionalVoice.BASE_RATE * voice.speed)
            voice.engine?.setPitch(.97f)
        }
    }
    return voice
}

private fun clock(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return m.toString() + ":" + (if (s < 10) "0" else "") + s
}

@Composable
fun DevotionalAudioBar(voice: DevotionalVoice, modifier: Modifier = Modifier) {
    val fill by animateFloatAsState(voice.fraction, tween(220), label = "audio-progress")

    /*
     * Apple's proportions, not a row of equal-weight controls.
     *
     * The first pass put a 52dp saturated play button and a large grey speed
     * block at either end of the row, squeezing the track between them. Two
     * heavy objects, one of them a secondary control, competing across the
     * card - so the eye had no idea where to start.
     *
     * Apple's players are laid out by rank: one filled circle for the primary
     * action, the scrubber given the full width of the card underneath, times
     * in the scrubber's own small type at its ends, and the rate control
     * demoted to plain tinted text. Nothing else is filled, because only one
     * thing here is the action.
     */
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(CircleShape)
                    .background(Pk.Oxblood)
                    .clickable(enabled = voice.ready) { voice.toggle() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (voice.speaking) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (voice.speaking) "Pause" else "Listen to this devotional",
                    tint = Color.White,
                    modifier = Modifier.size(23.dp),
                )
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(
                    "Listen to this devotional",
                    color = Pk.Charcoal, fontFamily = UtilitySans,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Read aloud on this device",
                    color = Pk.Muted, fontFamily = UtilitySans, fontSize = 11.5.sp,
                )
            }
            /* Plain tinted text. A rate control is a preference, not an action,
               and it was reading as the second most important thing on the
               card because it had a filled background. */
            Text(
                (if (voice.speed == 1f) "1" else voice.speed.toString().removeSuffix(".0")) + "×",
                color = Pk.Oxblood, fontFamily = UtilitySans,
                fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .clickable(enabled = voice.ready) { voice.cycleSpeed() }
                    .padding(horizontal = 9.dp, vertical = 6.dp),
            )
        }

        Spacer(Modifier.height(15.dp))
        Box(
            Modifier.fillMaxWidth().height(4.dp)
                .clip(CircleShape)
                .background(Pk.Charcoal.copy(alpha = .09f)),
        ) {
            Box(
                Modifier.fillMaxWidth(fill).fillMaxHeight()
                    .clip(CircleShape).background(Pk.Oxblood),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(clock(voice.elapsedSeconds), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 10.5.sp)
            Text(clock(voice.estimatedSeconds), color = Pk.Muted, fontFamily = UtilitySans, fontSize = 10.5.sp)
        }
    }
}
