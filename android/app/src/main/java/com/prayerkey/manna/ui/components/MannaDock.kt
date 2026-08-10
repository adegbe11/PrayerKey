package com.prayerkey.manna.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.NightGloss
import com.prayerkey.manna.ui.theme.GiltLine
import com.prayerkey.manna.ui.theme.Ivory
import com.prayerkey.manna.ui.theme.Electric
import com.prayerkey.manna.ui.theme.UtilitySans

data class DockItem(val label: String, val icon: ImageVector)

/**
 * The Manna Dock — a floating glass capsule rather than a flat tab bar, so
 * the full-bleed card on Home and Bible still shows through underneath.
 *
 * Icon-only, Wallet-style: the active destination is the only labelled
 * thing on screen, and it is labelled by shape rather than text — a night
 * pill with a gold glyph. Names remain available to screen readers.
 */
@Composable
fun MannaDock(items: List<DockItem>, selected: Int, onSelect: (Int) -> Unit) {
    val view = LocalView.current
    val cs = androidx.compose.material3.MaterialTheme.colorScheme
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        /* Glass in the theme's own colour, not a slab of white. A stark
           white bar under a midnight screen cut the immersion off an inch
           from the bottom of the phone; on a light theme this still reads
           as white, because the theme says so. */
        Surface(
            shape = RoundedCornerShape(31.dp),
            color = Color(0xFF1A1F2A),
            border = BorderStroke(0.6.dp, GiltLine.copy(alpha = .48f)),
            shadowElevation = 24.dp,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val active = index == selected
                    val iconScale by animateFloatAsState(
                        if (active) 1f else .92f,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "dock-scale",
                    )
                    val tint by animateColorAsState(
                        if (active) Electric else Ivory.copy(alpha = .60f),
                        spring(stiffness = Spring.StiffnessMediumLow), label = "dock-tint",
                    )

                    /* Equal weight per tab: five stacked icon+label cells that
                       always occupy the same width, so nothing shifts sideways
                       when the selection moves. */
                    Column(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(21.dp))
                            .background(SolidColor(Color.Transparent))
                            .semantics(mergeDescendants = true) {
                                contentDescription = item.label
                                role = Role.Tab
                                this.selected = active
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                if (!active) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    onSelect(index)
                                }
                            }
                            .padding(top = 7.dp, bottom = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            item.icon, contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(21.dp).scale(iconScale),
                        )
                        /* Every tab is named, always. A sparkle and a church
                           glyph are not guessable, and Home carries no other
                           wayfinding — the dock is the only signpost in the
                           app, which is the wrong place to be subtle. */
                        Text(
                            if (item.label == "Home") "TODAY" else item.label.uppercase(),
                            color = if (active) Ivory else Ivory.copy(alpha = .60f),
                            fontFamily = UtilitySans,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 4.dp),
                            // kill Android's font padding so the label sits
                            // optically centred under the icon
                            style = androidx.compose.ui.text.TextStyle(
                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                            ),
                        )
                    }
                }
            }
        }
    }
}
