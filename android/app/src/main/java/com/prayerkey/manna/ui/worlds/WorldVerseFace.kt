package com.prayerkey.manna.ui.worlds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.R

/**
 * A verse standing inside its world.
 *
 * Text sits at the bottom over a scrim rather than centred, so the scene
 * stays visible — that is the whole point of drawing it. Promise cards
 * announce themselves with a tag; everything else names its world quietly.
 */
@Composable
fun WorldVerseFace(
    reference: String,
    text: String,
    translation: String,
    front: Boolean,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 210.dp,
) {
    // The painterly scene is gone: flat printed-card art reads better and
    // does not pretend to be a photograph. front/reduceMotion are kept in
    // the signature because the deck still passes them, but nothing here
    // animates now — which is also why it costs nothing to draw.
    ScriptureCard(
        reference = reference,
        text = text,
        translation = translation,
        modifier = modifier,
        bottomPadding = bottomPadding,
    )
}
