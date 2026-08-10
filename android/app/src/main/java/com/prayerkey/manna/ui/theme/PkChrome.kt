package com.prayerkey.manna.ui.theme

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/**
 * The masthead: oxblood, closed by a single gold hairline.
 *
 * It is the one heavy element on the page and it does three jobs at once —
 * says where you are, shows the streak, and puts the week within reach. The
 * gold line is what separates it from the cream rather than a shadow; a
 * shadow under a dark band on warm paper reads as grime.
 */
@Composable
fun PkHeader(
    title: String,
    streakDays: Int,
    /** Sunday-first, seven entries ending today. True where the day was kept. */
    week: List<Boolean>,
    today: LocalDate = LocalDate.now(),
    trailingLabel: String? = null,
) {
    Column(
        Modifier.fillMaxWidth()
            .background(Pk.Oxblood)
            .statusBarsPadding()
            .padding(start = Pk.Gutter, end = Pk.Gutter, top = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                title.uppercase(),
                color = Pk.Cream,
                fontFamily = Spectral,
                fontSize = PkType.Brand,
                letterSpacing = PkType.BrandTracking,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            trailingLabel?.let {
                Text(
                    it.uppercase(),
                    color = Pk.Cream.copy(alpha = .6f),
                    fontFamily = Spectral,
                    fontSize = PkType.Label,
                    letterSpacing = PkType.LabelTracking,
                )
            }
        }

        /* The streak is gold and it is the only gold text on the screen, so it
           reads as the reward it is. Hidden below two days: "1 DAY STREAK" is
           not an achievement, it is a guilt trip waiting to happen. */
        if (streakDays >= 2) {
            Row(
                Modifier.padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Bolt, null,
                    tint = Pk.Gold, modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "$streakDays DAY STREAK",
                    color = Pk.Gold,
                    fontFamily = Spectral,
                    fontSize = 12.sp,
                    letterSpacing = 1.4.sp,
                )
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val letters = remember(today) {
                (6 downTo 0).map { back ->
                    today.minusDays(back.toLong()).dayOfWeek
                        .getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.getDefault())
                }
            }
            letters.forEachIndexed { i, letter ->
                val isToday = i == letters.lastIndex
                val kept = week.getOrElse(i) { false }
                Column(
                    Modifier.width(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        letter,
                        color = if (isToday) Pk.Cream else Pk.Cream.copy(alpha = .55f),
                        fontFamily = Spectral,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    /* Today gets the gold rule. A day you kept gets a dot —
                       the reference underlined only today, which meant the
                       strip showed the date and nothing about your week. */
                    Box(
                        Modifier.height(2.dp).fillMaxWidth()
                            .background(
                                when {
                                    isToday -> Pk.Gold
                                    kept -> Pk.Gold.copy(alpha = .45f)
                                    else -> androidx.compose.ui.graphics.Color.Transparent
                                },
                            ),
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Pk.Gold))
    }
}

data class PkNavItem(val label: String, val icon: ImageVector)

/**
 * The floating nav: charcoal, ringed in gold at half strength.
 *
 * Blush at rest, cream when selected, and the glyph turns gold — so the
 * active tab is marked three ways over. The previous dock signalled it with
 * a filled pill, which on a busy screen was just another shape.
 */
@Composable
fun PkNav(
    items: List<PkNavItem>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val view = LocalView.current
    Box(
        /* No navigationBarsPadding here. MainActivity sets
           decorFitsSystemWindows(true), so the window is already inset and
           adding it again floated the bar a finger's width off the bottom. */
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
    ) {
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(Pk.NavRadius))
                .background(Pk.Charcoal)
                .border(1.dp, Pk.Gold.copy(alpha = .5f), RoundedCornerShape(Pk.NavRadius))
                .padding(vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEachIndexed { index, item ->
                val on = index == selected
                val tint by animateColorAsState(
                    if (on) Pk.Gold else Pk.Blush, tween(180), label = "nav-icon",
                )
                val label by animateColorAsState(
                    if (on) Pk.Cream else Pk.Blush, tween(180), label = "nav-label",
                )
                Column(
                    Modifier
                        // 64dp wide and the whole cell is tappable, so every
                        // target clears the 48dp minimum
                        .width(64.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (!on) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                onSelect(index)
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(item.icon, null, tint = tint, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        item.label.uppercase(),
                        color = label,
                        fontFamily = Spectral,
                        fontSize = PkType.Tiny,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
