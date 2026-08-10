package com.prayerkey.manna.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.ChipFill
import com.prayerkey.manna.ui.theme.Electric
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.InkSoft
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.Night
import com.prayerkey.manna.ui.theme.NightFill
import com.prayerkey.manna.ui.theme.R
import com.prayerkey.manna.ui.theme.glassPill
import com.prayerkey.manna.ui.theme.topHighlight

/**
 * Our own segmented chip.
 *
 * Material's FilterChip brought its own container colour, its own ripple and
 * its own border weight — which is why the app kept reading as a stock
 * Android build no matter what the layout did. Selected state here is the
 * night gloss used everywhere else in the product, and pressing scales
 * rather than ripples.
 */
@Composable
fun PkChip(
    label: String,
    selected: Boolean,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        if (pressed) .955f else 1f,
        spring(stiffness = Spring.StiffnessHigh), label = "chip-press",
    )
    val content by animateColorAsState(
        if (selected) com.prayerkey.manna.ui.theme.Ivory else InkSoft,
        spring(stiffness = Spring.StiffnessMediumLow), label = "chip-content",
    )

    Row(
        modifier
            .scale(press)
            .height(48.dp)
            .then(
                if (selected) Modifier.shadow(10.dp, R.pill, spotColor = Night.copy(alpha = .3f))
                else Modifier,
            )
            .clip(R.pill)
            .background(if (selected) NightFill else Brush.verticalGradient(listOf(ChipFill, ChipFill)))
            .topHighlight(R.pill, strength = if (selected) .22f else .7f)
            .semantics(mergeDescendants = true) {
                contentDescription = label
                role = Role.Button
            }
            .clickable(interactionSource = interaction, indication = null) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onClick()
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(it, null, tint = if (selected) Gold else Muted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
        }
        Text(label, color = content, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Primary action: glossy gradient, layered shadow, press scale, no ripple. */
@Composable
fun PkButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    /* The theme's accent, not electric blue. This is the app's primary
       button, and blue was the loudest colour on every screen it appeared
       on — it had already been pulled off Home and the sermon note one at a
       time, which is the wrong place to fix it. */
    fill: Brush? = null,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    val cs = androidx.compose.material3.MaterialTheme.colorScheme
    val paint = fill ?: Brush.verticalGradient(listOf(cs.primary, cs.primary))
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        if (pressed && enabled) .975f else 1f,
        spring(stiffness = Spring.StiffnessHigh), label = "btn-press",
    )
    Box(
        modifier
            .scale(press)
            .height(62.dp)
            .then(
                if (enabled) Modifier
                    .shadow(12.dp, R.control, spotColor = Night.copy(alpha = .24f), ambientColor = Color.Transparent)
                    .shadow(3.dp, R.control, spotColor = Night.copy(alpha = .18f), ambientColor = Color.Transparent)
                else Modifier,
            )
            .clip(R.control)
            .background(
                if (enabled) paint
                else Brush.verticalGradient(
                    listOf(cs.onSurface.copy(alpha = .16f), cs.onSurface.copy(alpha = .10f)),
                ),
            )
            .topHighlight(R.control, strength = if (enabled) .42f else .2f)
            .semantics(mergeDescendants = true) {
                contentDescription = label
                role = Role.Button
                if (!enabled) disabled()
            }
            .clickable(enabled = enabled, interactionSource = interaction, indication = null) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Icon(it, null, tint = cs.onPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                label,
                color = if (enabled) cs.onPrimary else cs.onSurface.copy(alpha = .5f),
                fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            )
        }
    }
}

/** A floating pill for counters and back affordances that sit over content. */
@Composable
fun PkGlassPill(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier.glassPill()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(text, color = InkSoft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
