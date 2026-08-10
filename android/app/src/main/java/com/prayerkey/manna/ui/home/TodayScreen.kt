package com.prayerkey.manna.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.PkButton
import com.prayerkey.manna.ui.theme.PkCard
import com.prayerkey.manna.ui.theme.PkLabel
import com.prayerkey.manna.ui.theme.PkRoundAction
import com.prayerkey.manna.ui.theme.PkRow
import com.prayerkey.manna.ui.theme.PkRows
import com.prayerkey.manna.ui.theme.PkScripture
import com.prayerkey.manna.ui.theme.PkSectionHead
import com.prayerkey.manna.ui.theme.PkStep
import com.prayerkey.manna.ui.theme.PkTag
import com.prayerkey.manna.ui.theme.PkTagKind
import com.prayerkey.manna.ui.theme.PkText
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Today.
 *
 * One rule governed every decision here: a person should open the app and know
 * what to do next in under a second. So the screen has exactly one hero — the
 * day's sequence — and everything else is either the reason for it above, or a
 * way to go deeper below.
 *
 * The hierarchy top to bottom is deliberate and fixed:
 *
 *   PRAYERKEY → streak → TODAY'S DEVOTION → the current step
 *
 * Nothing on this screen exists unless it answers "what should I do with God
 * today?" or "where can I go deeper?". No feeds, banners, carousels or extra
 * statistics — the calm is the product.
 */
data class TodayPlan(
    val title: String,
    val quote: String,
    val quoteSource: String,
    val passageRef: String,
    val passageDone: Boolean,
    val devotionalMinutes: Int,
    val devotionalDone: Boolean,
    val prayerMinutes: Int,
    val prayerDone: Boolean,
    val verse: String,
    val verseReference: String,
    val answeredCount: Int,
) {
    /** The one step to continue with. Null once the day is complete. */
    val current: Int?
        get() = when {
            !passageDone -> 0
            !devotionalDone -> 1
            !prayerDone -> 2
            else -> null
        }

    val complete: Boolean get() = current == null

    fun stepFor(index: Int, done: Boolean): PkStep = when {
        done -> PkStep.Done
        current == index -> PkStep.Current
        else -> PkStep.Upcoming
    }
}

@Composable
fun TodayScreen(
    plan: TodayPlan,
    bookmarked: Boolean,
    onBookmark: () -> Unit,
    onRead: () -> Unit,
    onPassage: () -> Unit,
    onDevotional: () -> Unit,
    onPrayer: () -> Unit,
    onMidnight: () -> Unit,
    onMyPrayers: () -> Unit,
    onJournal: () -> Unit,
    onCalendar: () -> Unit,
) {
    val view = LocalView.current

    /* The completion moment: the journey card's hairline warms to gold and the
       phone gives one tick. No confetti — this is meant to feel reverent, and
       a celebration animation would be the single most out-of-place thing in
       the app. */
    val journeyBorder by animateColorAsState(
        if (plan.complete) Pk.Gold.copy(alpha = .55f) else Pk.Hair,
        tween(700), label = "journey-border",
    )
    LaunchedEffect(plan.complete) {
        if (plan.complete) view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    Column(
        Modifier.fillMaxSize()
            .background(Pk.Cream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Pk.Gutter)
            // the floating nav must never sit on top of the last card
            .padding(bottom = Pk.NavClearance),
    ) {
        Spacer(Modifier.height(Pk.S5))

        Text(
            "VIEW CALENDAR & SAVED",
            color = Pk.Faint,
            style = PkText.SectionLabel,
            modifier = Modifier
                .clickable(onClick = onCalendar)
                // a 12dp vertical pad turns a text line into a real target
                .padding(vertical = Pk.S3),
        )

        Spacer(Modifier.height(Pk.S4))
        Text(
            LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM uuuu")).uppercase(),
            color = Pk.Muted,
            style = PkText.SectionLabel,
        )
        Spacer(Modifier.height(Pk.S2))
        Text(plan.title, color = Pk.Charcoal, style = PkText.Display)

        /* ── the hero: today's sequence ─────────────────────────────── */

        Spacer(Modifier.height(Pk.SectionGap))
        PkSectionHead("Today's devotion") {
            PkRoundAction(
                if (bookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                if (bookmarked) "Remove bookmark" else "Bookmark today",
                onBookmark,
            )
        }
        Spacer(Modifier.height(Pk.HeadingGap))

        PkRows(borderColor = journeyBorder) {
            PkRow(
                Icons.Outlined.MenuBook, "Passage", onPassage,
                first = true, step = plan.stepFor(0, plan.passageDone),
            ) {
                if (plan.passageDone) PkTag("Done", PkTagKind.Done)
                else PkTag(plan.passageRef, PkTagKind.Plain)
            }
            PkRow(
                Icons.Outlined.EditNote, "Devotional", onDevotional,
                step = plan.stepFor(1, plan.devotionalDone),
            ) {
                if (plan.devotionalDone) PkTag("Done", PkTagKind.Done)
                else PkTag("${plan.devotionalMinutes} min", PkTagKind.Plain)
            }
            PkRow(
                Icons.Outlined.FavoriteBorder, "Prayer", onPrayer,
                step = plan.stepFor(2, plan.prayerDone),
            ) {
                if (plan.prayerDone) PkTag("Done", PkTagKind.Done)
                else PkTag("${plan.prayerMinutes} min", PkTagKind.Plain)
            }
        }

        /* ── the reason for it ──────────────────────────────────────── */

        Spacer(Modifier.height(Pk.S4))
        PkCard {
            Column(Modifier.padding(Pk.CardPad)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AutoAwesome, null,
                        tint = Pk.Gold, modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(Pk.S2))
                    PkLabel("Quote", color = Pk.Oxblood)
                }
                Spacer(Modifier.height(Pk.S4))
                PkLabel("Today's quote from", color = Pk.Muted)
                Spacer(Modifier.height(Pk.S1))
                Text(plan.quoteSource, color = Pk.Charcoal, style = PkText.CardTitle)
                Spacer(Modifier.height(Pk.S3))
                Text(
                    "“${plan.quote}”",
                    color = Pk.Charcoal.copy(alpha = .9f),
                    style = PkText.Scripture,
                )
                Spacer(Modifier.height(Pk.S5))
                PkButton("Read", onRead)
            }
        }

        /* ── where to go deeper ─────────────────────────────────────── */

        Spacer(Modifier.height(Pk.SectionGap))
        PkLabel("Go deeper")
        Spacer(Modifier.height(Pk.HeadingGap))

        PkRows {
            PkRow(Icons.Outlined.DarkMode, "Midnight prayer", onMidnight, first = true) {
                PkTag("Pray", PkTagKind.Invite)
            }
            /* "3 NEW" said nothing about whose prayers or what happened to
               them. These are the user's own, and the number that matters is
               how many God has answered. */
            PkRow(Icons.Outlined.FavoriteBorder, "My prayers", onMyPrayers) {
                if (plan.answeredCount > 0) PkTag("${plan.answeredCount} answered", PkTagKind.Done)
                else PkTag("None yet", PkTagKind.Plain)
            }
            PkRow(Icons.Outlined.EditNote, "Write in your journal", onJournal) {
                PkTag("Write", PkTagKind.Invite)
            }
        }

        Spacer(Modifier.height(Pk.SectionGap))
        PkScripture("Today's word", plan.verse, plan.verseReference)
    }
}
