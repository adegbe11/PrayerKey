package com.prayerkey.manna.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.data.Challenge
import com.prayerkey.manna.data.Devotion
import com.prayerkey.manna.ui.theme.BookSerif
import java.time.LocalDate
import java.time.LocalTime

/**
 * Home, as a place rather than a single card.
 *
 * Home used to be one full-screen card and nothing else: no sense of what day
 * it was, whether you had shown up, or what there was to do beyond pulling the
 * card once. Everything the app could do was behind a tab you had to guess at.
 *
 * The shape here is lifted from a layout that does this well — greeting, a
 * strip of days, then cards you can act on — with the ornament taken out:
 * warm paper instead of cold grey, the theme's accent instead of lime, and a
 * serif for the things worth reading slowly.
 *
 * Everything on it is real. The day strip is drawn from journal entries the
 * person actually wrote, the devotion and both challenges are derived from
 * assets that ship with the app, and nothing here needs a network.
 */
@Composable
fun HomeDashboard(
    name: String,
    streak: Int,
    /** Days in the last fortnight with something written. */
    activeDays: Set<LocalDate>,
    devotion: Devotion?,
    bible: Challenge?,
    prayer: Challenge?,
    onOpenWord: () -> Unit,
    onWriteDevotion: () -> Unit,
    onOpenChallenge: (Challenge) -> Unit,
    onToggleChallenge: (Challenge) -> Unit,
    onOpenBible: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenChurch: () -> Unit,
    onSettings: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val today = LocalDate.now()

    Column(
        Modifier.fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to androidx.compose.ui.graphics.lerp(cs.background, cs.primary, .07f),
                    .35f to cs.background,
                    1f to cs.background,
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 130.dp),
    ) {

        /* ── the greeting ─────────────────────────────────────────────── */
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    greeting(),
                    color = cs.onBackground.copy(alpha = .55f), fontSize = 13.sp,
                )
                Text(
                    if (name.isBlank()) "Let's pray today" else "$name, let's pray today",
                    color = cs.onBackground, fontFamily = BookSerif,
                    fontSize = 27.sp, letterSpacing = (-0.4).sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Box(
                Modifier.size(42.dp).clip(CircleShape)
                    .background(cs.onBackground.copy(alpha = .05f))
                    .clickable(onClick = onSettings),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Settings, "Settings", tint = cs.onBackground.copy(alpha = .6f), modifier = Modifier.size(19.dp))
            }
        }

        /* ── the week ─────────────────────────────────────────────────────
           Seven days ending today, filled where something was written. This
           is the only place in the app that has ever shown whether you turned
           up, which is the thing a streak number claims but never proves. */
        Row(
            Modifier.fillMaxWidth().padding(top = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            (6 downTo 0).forEach { back ->
                val day = today.minusDays(back.toLong())
                val active = day in activeDays
                val isToday = day == today
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        day.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.getDefault()),
                        color = cs.onBackground.copy(alpha = .45f), fontSize = 11.sp,
                    )
                    Box(
                        Modifier.padding(top = 8.dp).size(36.dp).clip(CircleShape)
                            .background(if (active) cs.primary else cs.onBackground.copy(alpha = .05f))
                            .then(
                                if (isToday && !active) {
                                    Modifier.border(1.5.dp, cs.primary, CircleShape)
                                } else Modifier,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            day.dayOfMonth.toString(),
                            color = when {
                                active -> cs.onPrimary
                                isToday -> cs.primary
                                else -> cs.onBackground.copy(alpha = .55f)
                            },
                            fontSize = 13.sp,
                            fontWeight = if (isToday || active) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
        if (streak > 0) {
            Text(
                if (streak == 1) "1 day in a row" else "$streak days in a row",
                color = cs.primary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        /* ── today's word ─────────────────────────────────────────────── */
        Box(
            Modifier.fillMaxWidth().padding(top = 22.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1B2647), Color(0xFF0E1428))))
                .clickable(onClick = onOpenWord)
                .padding(20.dp),
        ) {
            Column {
                Text(
                    "TODAY'S WORD",
                    color = Color(0xFFD4AF37), fontSize = 9.sp,
                    letterSpacing = 2.2.sp, fontWeight = FontWeight.Bold,
                )
                Text(
                    "Pull down to receive",
                    color = Color(0xFFF2EFE6), fontFamily = BookSerif,
                    fontSize = 21.sp, modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    "One verse, once a day",
                    color = Color(0xFFF2EFE6).copy(alpha = .6f), fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Icon(
                Icons.Outlined.KeyboardArrowRight, null,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.align(Alignment.CenterEnd).size(22.dp),
            )
        }

        /* ── the devotion ─────────────────────────────────────────────── */
        devotion?.let { d ->
            SectionLabel("TODAY'S DEVOTION")
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cs.surface)
                    .border(1.dp, cs.outlineVariant, RoundedCornerShape(20.dp))
                    .padding(18.dp),
            ) {
                Text(
                    "“${d.verse}”",
                    color = cs.onBackground, fontFamily = BookSerif,
                    fontSize = 17.sp, lineHeight = 26.sp,
                )
                Text(
                    d.reference,
                    color = cs.primary, fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Box(
                    Modifier.padding(top = 16.dp).fillMaxWidth().height(1.dp)
                        .background(cs.outlineVariant),
                )
                Text(
                    d.reflection,
                    color = cs.onBackground.copy(alpha = .78f), fontSize = 14.sp,
                    lineHeight = 21.sp, modifier = Modifier.padding(top = 16.dp),
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cs.primary.copy(alpha = .10f))
                        .clickable(onClick = onWriteDevotion)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.EditNote, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                    Text(
                        "Write your answer",
                        color = cs.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        /* ── the challenges ──────────────────────────────────────────── */
        if (bible != null || prayer != null) {
            SectionLabel("CHALLENGES")
            bible?.let {
                ChallengeCard(it, onOpen = { onOpenChallenge(it) }, onToggle = { onToggleChallenge(it) })
                Spacer(Modifier.height(10.dp))
            }
            prayer?.let {
                ChallengeCard(it, onOpen = { onOpenChallenge(it) }, onToggle = { onToggleChallenge(it) })
            }
        }

        /* ── the three rooms ─────────────────────────────────────────── */
        SectionLabel("GO TO")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoomTile("Bible", Icons.Outlined.MenuBook, Modifier.weight(1f), onOpenBible)
            RoomTile("Journal", Icons.Outlined.EditNote, Modifier.weight(1f), onOpenJournal)
            RoomTile("Listen", Icons.Outlined.Mic, Modifier.weight(1f), onOpenChurch)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .45f),
        fontSize = 9.5.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 26.dp, bottom = 12.dp),
    )
}

/**
 * A challenge, with today's assignment and a tick.
 *
 * The tick is the whole point: a challenge you cannot mark is a suggestion.
 */
@Composable
private fun ChallengeCard(challenge: Challenge, onOpen: () -> Unit, onToggle: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val done = challenge.today.done

    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cs.surface)
            .border(
                1.dp,
                if (done) cs.primary.copy(alpha = .45f) else cs.outlineVariant,
                RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onOpen)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                challenge.title,
                color = cs.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            )
            Text(
                challenge.today.label,
                color = cs.primary, fontFamily = BookSerif, fontSize = 17.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                challenge.today.detail,
                color = cs.onBackground.copy(alpha = .5f), fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            // how far in, at a glance
            Box(
                Modifier.padding(top = 10.dp).fillMaxWidth().height(4.dp)
                    .clip(CircleShape).background(cs.onBackground.copy(alpha = .08f)),
            ) {
                Box(
                    Modifier.fillMaxWidth(
                        challenge.today.index.toFloat() / challenge.today.total.coerceAtLeast(1),
                    ).height(4.dp).clip(CircleShape).background(cs.primary),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Box(
            Modifier.size(38.dp).clip(CircleShape)
                .background(if (done) cs.primary else Color.Transparent)
                .border(1.5.dp, if (done) cs.primary else cs.outlineVariant, CircleShape)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Check,
                if (done) "Mark today undone" else "Mark today done",
                tint = if (done) cs.onPrimary else cs.onBackground.copy(alpha = .35f),
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun RoomTile(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cs.surface)
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(cs.primary.copy(alpha = .12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = cs.primary, modifier = Modifier.size(21.dp))
        }
        Text(
            label,
            color = cs.onBackground, fontSize = 12.5.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 0..4 -> "Still awake"
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}
