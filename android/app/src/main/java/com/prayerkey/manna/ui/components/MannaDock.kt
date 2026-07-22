package com.prayerkey.manna.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.Gold
import com.prayerkey.manna.ui.theme.Muted
import com.prayerkey.manna.ui.theme.Night

data class DockItem(val label: String, val icon: ImageVector)

/**
 * The Manna Dock — a floating glass capsule in place of a tab bar.
 * The active destination morphs into a night-blue pill with a gold
 * icon and ivory label: the sacred card identity living inside the
 * navigation itself. Spring physics + haptics on every change.
 */
@Composable
fun MannaDock(items: List<DockItem>, selected: Int, onSelect: (Int) -> Unit) {
    val view = LocalView.current
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = Color.White.copy(alpha = .96f),
            border = BorderStroke(0.5.dp, Color(0xFFE8E8ED)),
            shadowElevation = 26.dp,
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    val active = index == selected
                    val iconScale by animateFloatAsState(
                        if (active) 1f else .92f,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "dock-scale",
                    )
                    val pillPad by animateDpAsState(
                        if (active) 16.dp else 13.dp,
                        spring(stiffness = Spring.StiffnessMediumLow), label = "dock-pad",
                    )
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                if (active) com.prayerkey.manna.ui.theme.NightGloss
                                else androidx.compose.ui.graphics.SolidColor(Color.Transparent),
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                if (!active) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    onSelect(index)
                                }
                            }
                            .padding(horizontal = pillPad, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            item.icon, contentDescription = item.label,
                            tint = if (active) Gold else Muted,
                            modifier = Modifier.size(23.dp).scale(iconScale),
                        )
                        AnimatedVisibility(
                            visible = active,
                            enter = expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut(),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    item.label,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    // kill Android font padding so the label centers
                                    // on the exact same optical line as the icon
                                    style = androidx.compose.ui.text.TextStyle(
                                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                                    ),
                                )
                            }
                        }
                    }
                    // equal breathing room between every dock element
                    if (index != items.lastIndex) Spacer(Modifier.width(6.dp))
                }
            }
        }
    }
}
