package com.prayerkey.manna.ui.components

import com.prayerkey.manna.ui.platform.noFontPadding
import com.prayerkey.manna.ui.platform.rememberHaptics
import com.prayerkey.manna.ui.platform.Haptic
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.ui.draw.drawBehind
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
import com.prayerkey.manna.ui.theme.GlassBackdrop
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.liquidGlass

data class DockItem(
    val label: String,
    val icon: ImageVector? = null,
    /** A drawn mark, tinted like a glyph. The Pray tab wears the key. */
    val mark: DrawableResource? = null,
)

/**
 * The Manna Dock — a floating glass capsule rather than a flat tab bar, so
 * the full-bleed card on Home and Bible still shows through underneath.
 *
 * Icon-only, Wallet-style: the active destination is the only labelled
 * thing on screen, and it is labelled by shape rather than text — a night
 * pill with a gold glyph. Names remain available to screen readers.
 */
@Composable
fun MannaDock(
    items: List<DockItem>,
    selected: Int,
    backdrop: GlassBackdrop? = null,
    onSelect: (Int) -> Unit,
) {
    val haptics = rememberHaptics()
    val cs = androidx.compose.material3.MaterialTheme.colorScheme
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        /* Glass in the theme's own colour, not a slab of white. A stark
           white bar under a midnight screen cut the immersion off an inch
           from the bottom of the phone; on a light theme this still reads
           as white, because the theme says so. */
        /* Glass rather than a painted capsule. It was Pk.Oxblood at 93%, which
           is a violet slab — it looked the same over a photograph as over an
           empty page, because it never sampled either. With a backdrop it
           refracts what is actually underneath it, and the tint drops to a
           trace so the content shows through rather than being restated. */
        /*
         * The brand capsule, with the selected tab knocked out of it.
         *
         * I made this white for a while, reasoning from Apple's tab bars that
         * the one element present on every screen should not be the loudest.
         * That is sound for an app whose colour lives in its content - Apple
         * Music has album art on every screen. This app's violet is the brand
         * itself, and a white bar gave it away for a principle that was not
         * ours.
         *
         * The chip stays, inverted: white behind the selected glyph, violet
         * knocked out of it. Same idea, right way round.
         */
        val dockShape = RoundedCornerShape(31.dp)
        /* One white chip that travels. It glides from tab to tab on a spring
           and stretches a little in flight, like a drop of liquid, instead of
           one chip fading out while another fades in. */
        val slide by animateFloatAsState(
            selected.toFloat(),
            spring(dampingRatio = .74f, stiffness = 420f), label = "dock-slide",
        )
        Surface(
            shape = dockShape,
            color = Color.Transparent,
            border = BorderStroke(0.9.dp, Color.White.copy(alpha = .52f)),
            shadowElevation = 18.dp,
        ) {
            Row(
                Modifier.fillMaxWidth()
                    .then(
                        if (backdrop != null) {
                            Modifier.liquidGlass(
                                backdrop = backdrop,
                                shape = dockShape,
                                blurRadius = 34.dp,
                                lens = 1.04f,
                                /* At .82 the tint was paint with a blur behind
                                   it: the violet was the bar, and the page
                                   underneath never showed. Taking it to a third
                                   lets the artwork and the writing travel under
                                   the dock, which is the whole point of a
                                   material - you can see there is a page there,
                                   without being able to read it. */
                                tint = Pk.Oxblood.copy(alpha = .58f),
                            )
                        } else {
                            Modifier.background(Pk.Oxblood.copy(alpha = .93f))
                        },
                    )
                    .drawBehind {
                        if (items.isEmpty()) return@drawBehind
                        val pad = 8.dp.toPx()
                        val gap = 2.dp.toPx()
                        val cell = (size.width - pad * 2 - gap * (items.size - 1)) / items.size
                        val flight = kotlin.math.sin((slide - kotlin.math.floor(slide)) * kotlin.math.PI).toFloat()
                        val w = 44.dp.toPx() + 18.dp.toPx() * flight
                        val h = 32.dp.toPx()
                        val cx = pad + slide * (cell + gap) + cell / 2
                        drawRoundRect(
                            Color.White,
                            topLeft = androidx.compose.ui.geometry.Offset(cx - w / 2, 12.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(w, h),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(11.dp.toPx()),
                        )
                    }
                    .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 11.dp),
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
                        if (active) Pk.Oxblood else Ivory.copy(alpha = .66f),
                        spring(stiffness = Spring.StiffnessMediumLow), label = "dock-tint",
                    )
                    /* The chip. A rounded square behind the glyph of the tab
                       you are on, in the brand colour, with the icon knocked
                       out of it. It is the whole of the selected state - no
                       underline, no dot, no second colour. */
                    val chip by animateColorAsState(
                        Color.Transparent, // the travelling chip is drawn behind the row
                        spring(stiffness = Spring.StiffnessMediumLow), label = "dock-chip",
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
                                    haptics.perform(Haptic.Click)
                                    onSelect(index)
                                }
                            }
                            .padding(top = 2.dp, bottom = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(width = 44.dp, height = 32.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(chip),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (item.mark != null) {
                                androidx.compose.foundation.Image(
                                    org.jetbrains.compose.resources.painterResource(item.mark),
                                    contentDescription = null,
                                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint),
                                    modifier = Modifier.size(23.dp).scale(iconScale),
                                )
                            } else if (item.icon != null) {
                                Icon(
                                    item.icon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(21.dp).scale(iconScale),
                                )
                            }
                        }
                        /* Every tab is named, always. A sparkle and a church
                           glyph are not guessable, and Home carries no other
                           wayfinding — the dock is the only signpost in the
                           app, which is the wrong place to be subtle. */
                        Text(
                            item.label,
                            color = if (active) Color.White else Ivory.copy(alpha = .66f),
                            fontFamily = UtilitySans,
                            fontSize = 10.5.sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                            letterSpacing = 0.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 3.dp),
                            // kill Android's font padding so the label sits
                            // optically centred under the icon
                            style = androidx.compose.ui.text.TextStyle(
                                platformStyle = noFontPadding,
                            ),
                        )
                    }
                }
            }
        }
    }
}
