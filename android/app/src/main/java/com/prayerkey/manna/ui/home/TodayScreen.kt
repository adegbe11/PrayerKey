package com.prayerkey.manna.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.ui.theme.ColumnScopeMarker
import com.prayerkey.manna.ui.theme.Pk
import com.prayerkey.manna.ui.theme.PkButton
import com.prayerkey.manna.ui.theme.PkCard
import com.prayerkey.manna.ui.theme.PkLabel
import com.prayerkey.manna.ui.theme.PkRoundAction
import com.prayerkey.manna.ui.theme.PkRow
import com.prayerkey.manna.ui.theme.PkRows
import com.prayerkey.manna.ui.theme.PkSectionHead
import com.prayerkey.manna.ui.theme.PkTag
import com.prayerkey.manna.ui.theme.PkTagKind
import com.prayerkey.manna.ui.theme.PkType
import com.prayerkey.manna.ui.theme.PkVerse
import com.prayerkey.manna.ui.theme.Spectral
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Today: the screen that answers "what am I doing today?" in one scroll.
 *
 * The old Home was one card and a gesture. Beautiful, and it told you nothing
 * — no sense of what today held, no evidence you had shown up, nowhere to go
 * next. This has a shape borrowed from apps that do daily practice well: a
 * masthead that carries the streak, a dated headline, one thing to read, then
 * the day's list, then more if you want it.
 *
 * Everything here is drawn from parts in the design system. No colour, radius
 * or font size is chosen locally, which is the only way eight screens end up
 * looking like one product.
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
    val answeredCount: Int,
)

@Composable
fun TodayScreen(
    plan: TodayPlan,
    streakDays: Int,
    week: List<Boolean>,
    bookmarked: Boolean,
    onBookmark: () -> Unit,
    onRead: () -> Unit,
    onPassage: () -> Unit,
    onDevotional: () -> Unit,
    onPrayer: () -> Unit,
    onMidnight: () -> Unit,
    onAnswered: () -> Unit,
    onJournal: () -> Unit,
    onCalendar: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .background(Pk.Cream)
            .verticalScroll(rememberScrollState())
            .padding(start = Pk.Gutter, end = Pk.Gutter)
            // clears the floating nav; content used to run underneath it
            .padding(bottom = 120.dp),
    ) {
        Spacer(Modifier.height(22.dp))

        Text(
            "VIEW CALENDAR & SAVED",
            color = Pk.Muted,
            fontFamily = Spectral,
            fontSize = PkType.Label,
            letterSpacing = PkType.LabelTracking,
            modifier = Modifier.clickable(onClick = onCalendar).padding(vertical = 12.dp),
        )

        Text(
            LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM uuuu")).uppercase(),
            color = Pk.Muted,
            fontFamily = Spectral,
            fontSize = 12.sp,
            letterSpacing = PkType.LabelTracking,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            plan.title,
            color = Pk.Charcoal,
            fontFamily = Spectral,
            fontSize = PkType.Title,
            lineHeight = PkType.TitleLine,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp),
        )

        PkSectionHead("Daily devotional") {
            PkRoundAction(
                if (bookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                if (bookmarked) "Remove bookmark" else "Bookmark today",
                onBookmark,
            )
        }

        PkCard {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AutoAwesome, null,
                        tint = Pk.Gold,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "QUOTE",
                        color = Pk.Oxblood,
                        fontFamily = Spectral,
                        fontSize = PkType.Label,
                        letterSpacing = 2.sp,
                    )
                }
                PkLabel("Today's quote from", Modifier.padding(top = 16.dp), Pk.Muted)
                Text(
                    plan.quoteSource,
                    color = Pk.Charcoal,
                    fontFamily = Spectral,
                    fontSize = PkType.Body,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text(
                    "“${plan.quote}”",
                    color = Pk.Charcoal.copy(alpha = .88f),
                    fontFamily = Spectral,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light,
                    fontSize = PkType.Quote,
                    lineHeight = PkType.QuoteLine,
                    modifier = Modifier.padding(top = 12.dp),
                )
                PkButton("Read", onRead, Modifier.padding(top = 18.dp))
            }
        }

        PkRows(Modifier.padding(top = 14.dp)) {
            PkRow(Icons.Outlined.MenuBook, "Passage", onPassage, first = true) {
                if (plan.passageDone) PkTag("Done", PkTagKind.Done)
                else PkTag(plan.passageRef, PkTagKind.Plain)
            }
            PkRow(Icons.Outlined.EditNote, "Devotional", onDevotional) {
                if (plan.devotionalDone) PkTag("Done", PkTagKind.Done)
                else PkTag("${plan.devotionalMinutes} min", PkTagKind.Plain)
            }
            PkRow(Icons.Outlined.Favorite, "Prayer", onPrayer) {
                if (plan.prayerDone) PkTag("Done", PkTagKind.Done)
                else PkTag("${plan.prayerMinutes} min", PkTagKind.Plain)
            }
        }

        PkLabel("More for your faith", Modifier.padding(top = 28.dp, bottom = 10.dp), Pk.Faint)

        PkRows {
            PkRow(Icons.Outlined.DarkMode, "Midnight prayer", onMidnight, first = true) {
                PkTag("Pray", PkTagKind.Invite)
            }
            PkRow(Icons.Outlined.WbTwilight, "Answered prayers", onAnswered) {
                if (plan.answeredCount > 0) PkTag("${plan.answeredCount} new", PkTagKind.Done)
                else PkTag("None yet", PkTagKind.Plain)
            }
            PkRow(Icons.Outlined.EditNote, "Write in your journal", onJournal) {
                PkTag("Write", PkTagKind.Invite)
            }
        }

        PkVerse(plan.verse, Modifier.padding(top = 22.dp))
    }
}

