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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.NightGloss

data class DockItem(val label: String, val icon: ImageVector)

/**
 * The Manna Dock — a floating glass capsule rather than a flat tab bar, so
 * the full-bleed card on Home and Bible still shows through underneath.
 *
 * Every destination carries its label. Labels used to appear only on the
 * active tab, which left four unlabelled glyphs — and neither the sparkle
 * (AI Pray) nor the church is guessable. Home carries no wayfinding at all
 * now, so the dock is the only signpost in the app and has to be legible
 * at a glance.
 */
@Composable
fun MannaDock(items: List<DockItem>, selected: Int, onSelect: (Int) -> Unit) {
    val view = LocalView.current
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = Color.White.copy(alpha = .96f),
            border = BorderStroke(0.5.dp, Color(0xFFE8E8ED)),
            shadowElevation = 26.dp,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 7.dp),
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
                        if (active) Gold else Muted,
                        spring(stiffness = Spring.StiffnessMediumLow), label = "dock-tint",
                    )
                    val labelColor by animateColorAsState(
                        if (active) Color.White else Muted,
                        spring(stiffness = Spring.StiffnessMediumLow), label = "dock-label",
                    )

                    /* Equal weight per tab: five stacked icon+label cells that
                       always occupy the same width, so nothing shifts sideways
                       when the selection moves. */
                    Column(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (active) NightGloss else SolidColor(Color.Transparent))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                if (!active) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    onSelect(index)
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            item.icon, contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(22.dp).scale(iconScale),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            item.label,
                            color = labelColor,
                            fontSize = 9.5.sp,
                            lineHeight = 11.sp,
                            letterSpacing = .2.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                            // no font padding, so the label hugs the icon evenly
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                            ),
                        )
                    }
                }
            }
        }
    }
}
