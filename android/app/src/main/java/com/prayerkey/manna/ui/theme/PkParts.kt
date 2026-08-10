package com.prayerkey.manna.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The parts every screen is built from.
 *
 * Each screen used to invent its own card, its own label, its own pill. That
 * is why the eight screens had eight visual accents — the inconsistency was
 * never a decision, it was the absence of one. These are the only shapes.
 */

/** A tracked small-caps label. The app's most-used piece of text. */
@Composable
fun PkLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Pk.Muted,
) = Text(
    text.uppercase(),
    color = color,
    fontFamily = Spectral,
    fontSize = PkType.Label,
    letterSpacing = PkType.LabelTracking,
    fontWeight = FontWeight.Normal,
    modifier = modifier,
)

/**
 * A sunken card: the page pressed in, with one hairline.
 *
 * Not a white card with a shadow. On a warm page, white cards float and look
 * like a different app's component; recessing them keeps everything on one
 * surface.
 */
@Composable
fun PkCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = Box(
    modifier
        .clip(RoundedCornerShape(Pk.CardRadius))
        .background(Pk.Sunken)
        .border(1.dp, Pk.Hair, RoundedCornerShape(Pk.CardRadius)),
) { content() }

/** The primary action. Oxblood, full width, tracked caps. */
@Composable
fun PkButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Box(
    modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(Pk.ButtonRadius))
        .background(Pk.Oxblood)
        .clickable(onClick = onClick)
        .padding(vertical = 15.dp),
    contentAlignment = Alignment.Center,
) {
    Text(
        label.uppercase(),
        color = Pk.Cream,
        fontFamily = Spectral,
        fontSize = 13.sp,
        letterSpacing = 2.4.sp,
        fontWeight = FontWeight.Medium,
    )
}

enum class PkTagKind { Done, Invite, Plain }

/**
 * The state of a thing, in one word.
 *
 * Sage means finished, blush means your turn. Colour carries the meaning, so
 * the word can stay short — but the word is always there, because colour
 * alone fails for the colour-blind and in bright sun.
 */
@Composable
fun PkTag(text: String, kind: PkTagKind, modifier: Modifier = Modifier) {
    val fill = when (kind) {
        PkTagKind.Done -> Pk.Sage
        PkTagKind.Invite -> Pk.Blush
        PkTagKind.Plain -> Color.Transparent
    }
    val ink = when (kind) {
        PkTagKind.Done -> Pk.Cream
        PkTagKind.Invite -> Pk.Oxblood
        PkTagKind.Plain -> Pk.Muted
    }
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(fill)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text.uppercase(),
            color = ink,
            fontFamily = Spectral,
            fontSize = PkType.Tiny,
            letterSpacing = PkType.TinyTracking,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * A stack of rows in one sunken card, divided by hairlines.
 *
 * Rows are 56dp tall so the whole row is the touch target — the audit found
 * dozens of controls under Android's 48dp minimum, almost all of them because
 * only the icon was tappable.
 */
@Composable
fun PkRows(modifier: Modifier = Modifier, content: @Composable ColumnScopeMarker.() -> Unit) {
    PkCard(modifier) { Column { ColumnScopeMarker.content() } }
}

/** Marker so [PkRow] can only be used inside [PkRows]. */
object ColumnScopeMarker

@Composable
fun ColumnScopeMarker.PkRow(
    icon: ImageVector,
    name: String,
    onClick: () -> Unit,
    first: Boolean = false,
    trailing: @Composable () -> Unit = {},
) {
    if (!first) Box(Modifier.fillMaxWidth().height(1.dp).background(Pk.Hair))
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon, null,
            tint = Pk.Charcoal.copy(alpha = .75f),
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            name,
            color = Pk.Charcoal,
            fontFamily = Spectral,
            fontSize = PkType.Body,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

/**
 * A verse set apart: gold wash, a gold rule down the left, italic.
 *
 * The one place scripture appears as an aside rather than the subject, so it
 * needs to look quoted without competing with the headline above it.
 */
@Composable
fun PkVerse(text: String, modifier: Modifier = Modifier) = Box(
    modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp))
        .background(Pk.GoldWash),
) {
    Box(Modifier.width(3.dp).height(1000.dp).background(Pk.Gold))
    Text(
        text,
        color = Pk.Charcoal.copy(alpha = .85f),
        fontFamily = Spectral,
        fontStyle = FontStyle.Italic,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 16.dp),
    )
}

/** Section head with an optional action on the right. */
@Composable
fun PkSectionHead(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) = Row(
    modifier.fillMaxWidth().padding(top = 26.dp, bottom = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
) {
    PkLabel(title)
    trailing()
}

/** The blush circle that holds a single glyph — bookmark, share, and so on. */
@Composable
fun PkRoundAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Box(
    modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(Pk.Blush.copy(alpha = .5f))
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
) {
    Icon(icon, label, tint = Pk.Oxblood, modifier = Modifier.size(18.dp))
}
