package com.prayerkey.manna.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The parts every screen is built from.
 *
 * Each screen used to invent its own card, label and pill, which is why eight
 * screens had eight accents — the inconsistency was the absence of a decision,
 * not a decision. These are the only shapes, and none of them takes a size or
 * a colour from its caller.
 */

/** A tracked small-caps label. The app's most-used piece of text. */
@Composable
fun PkLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Pk.Faint,
) = Text(
    text.uppercase(),
    color = color,
    style = PkText.SectionLabel,
    modifier = modifier,
)

/**
 * A card. Every card in the app: one radius, one border, one fill, one padding.
 *
 * No shadow. On warm paper a shadow reads as grime, and the luxury here is
 * meant to come from proportion and type — elevation effects are what a design
 * reaches for when the typography is not carrying it.
 */
@Composable
fun PkCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Pk.Hair,
    content: @Composable () -> Unit,
) = Box(
    modifier
        .clip(RoundedCornerShape(Pk.CardRadius))
        .background(Pk.Sunken)
        .border(Pk.CardBorder, borderColor, RoundedCornerShape(Pk.CardRadius)),
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
        .heightIn(min = 52.dp)
        .clip(RoundedCornerShape(Pk.ButtonRadius))
        .background(Pk.Oxblood)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
) {
    Text(
        label.uppercase(),
        color = Pk.Cream,
        style = PkText.Meta.copy(letterSpacing = 2.4.sp, fontSize = 13.sp),
    )
}

enum class PkTagKind { Done, Invite, Plain }

/**
 * The state of a thing, in one word.
 *
 * Sage means finished, blush means your turn. The word is always present as
 * well as the colour, because colour alone fails for the colour-blind and in
 * direct sun.
 *
 * The DONE tag carries charcoal, not cream: cream on sage measures **2.8:1**
 * and fails badly, while charcoal on the same sage is 5.3:1. The colour from
 * the brief is kept exactly; only the ink changed.
 */
@Composable
fun PkTag(text: String, kind: PkTagKind, modifier: Modifier = Modifier) {
    val fill = when (kind) {
        PkTagKind.Done -> Pk.Sage
        PkTagKind.Invite -> Pk.Blush
        PkTagKind.Plain -> Color.Transparent
    }
    val ink = when (kind) {
        PkTagKind.Done -> Pk.Charcoal
        PkTagKind.Invite -> Pk.Oxblood
        PkTagKind.Plain -> Pk.Muted
    }
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(fill)
            .padding(horizontal = if (kind == PkTagKind.Plain) 0.dp else 10.dp, vertical = 4.dp),
    ) {
        Text(text.uppercase(), color = ink, style = PkText.Meta)
    }
}

/** Where a step sits in the day. Drives every visual difference between rows. */
enum class PkStep { Done, Current, Upcoming }

/** A stack of rows in one card, divided by hairlines. */
@Composable
fun PkRows(
    modifier: Modifier = Modifier,
    borderColor: Color = Pk.Hair,
    content: @Composable PkRowScope.() -> Unit,
) = PkCard(modifier, borderColor) { Column { PkRowScope.content() } }

/** Scope marker, so a row cannot be used outside a stack. */
object PkRowScope

/**
 * One row of a stack.
 *
 * [step] is what makes the day legible at a glance:
 *
 *  - **Current** gets a warm wash, full-strength ink and a chevron. It is the
 *    only row with a directional affordance, so "continue here" needs no copy.
 *  - **Done** keeps its name at full strength but its glyph goes sage with a
 *    tick — finished, not erased.
 *  - **Upcoming** is dimmed and has no chevron. It stays quiet until its turn.
 *
 * Three equally-weighted rows is the failure this replaces: it made the user
 * read all three and decide, every single morning.
 */
@Composable
fun PkRowScope.PkRow(
    icon: ImageVector,
    name: String,
    onClick: () -> Unit,
    first: Boolean = false,
    step: PkStep = PkStep.Upcoming,
    trailing: @Composable () -> Unit = {},
) {
    val current = step == PkStep.Current
    val fill by animateColorAsState(
        if (current) Pk.CurrentWash else Color.Transparent, tween(260), label = "row-fill",
    )
    val nameInk by animateColorAsState(
        when (step) {
            PkStep.Upcoming -> Pk.Muted
            else -> Pk.Charcoal
        },
        tween(260), label = "row-ink",
    )
    val glyph by animateColorAsState(
        when (step) {
            PkStep.Done -> Pk.Sage
            PkStep.Current -> Pk.Oxblood
            PkStep.Upcoming -> Pk.Charcoal.copy(alpha = .45f)
        },
        tween(260), label = "row-glyph",
    )

    if (!first) Box(Modifier.fillMaxWidth().height(1.dp).background(Pk.Hair))
    Row(
        Modifier.fillMaxWidth()
            .background(fill)
            .clickable(onClick = onClick)
            .heightIn(min = Pk.RowHeight)
            .padding(horizontal = Pk.CardPad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // one optical size and one bounding box for every glyph in the app
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            if (step == PkStep.Done) {
                Icon(Icons.Outlined.Check, null, tint = glyph, modifier = Modifier.size(18.dp))
            } else {
                Icon(icon, null, tint = glyph, modifier = Modifier.size(19.dp))
            }
        }
        Spacer(Modifier.width(Pk.S3))
        Text(
            name,
            color = nameInk,
            style = if (current) PkText.RowTitle.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
            else PkText.RowTitle,
            modifier = Modifier.weight(1f),
        )
        trailing()
        // the chevron belongs to the current step alone
        AnimatedVisibility(current, enter = fadeIn(tween(220)) + scaleIn(initialScale = .8f), exit = fadeOut(tween(140))) {
            Icon(
                Icons.Outlined.ChevronRight, null,
                tint = Pk.Oxblood,
                modifier = Modifier.padding(start = Pk.S2).size(20.dp),
            )
        }
    }
}

/**
 * Scripture, set apart.
 *
 * A label, the verse in italic serif, then the reference. Tight vertical
 * padding — the previous version was a tall utility card that happened to
 * contain a verse.
 */
@Composable
fun PkScripture(
    label: String,
    verse: String,
    reference: String,
    modifier: Modifier = Modifier,
) = Box(
    modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topEnd = Pk.CardRadius, bottomEnd = Pk.CardRadius))
        .background(Pk.GoldWash)
        /* The rule is drawn, not laid out. As a 3dp Box with fillMaxHeight
           inside an intrinsically-sized Row it collapsed to nothing, so the
           card lost the one mark that says "scripture". drawBehind cannot
           collapse — it paints the full measured height. */
        .drawBehind {
            drawRect(
                Pk.Gold,
                size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height),
            )
        },
) {
    Column(Modifier.padding(start = Pk.S5, end = Pk.S5, top = Pk.S4, bottom = Pk.S4)) {
        PkLabel(label, color = Pk.Oxblood.copy(alpha = .85f))
        Spacer(Modifier.height(Pk.S2))
        Text(
            "“$verse”",
            color = Pk.Charcoal.copy(alpha = .9f),
            style = PkText.Scripture.copy(fontSize = 16.sp, lineHeight = 25.sp),
        )
        Spacer(Modifier.height(Pk.S3))
        Text(reference, color = Pk.Oxblood, style = PkText.Reference)
    }
}

/** Section head, optionally with an action on the right. */
@Composable
fun PkSectionHead(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) = Row(
    modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
) {
    PkLabel(title)
    trailing()
}

/** A blush circle holding one glyph — bookmark, share, and so on. */
@Composable
fun PkRoundAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Box(
    modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(Pk.Blush.copy(alpha = .45f))
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
) {
    Icon(icon, label, tint = Pk.Oxblood, modifier = Modifier.size(18.dp))
}
