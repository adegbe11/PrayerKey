package com.prayerkey.manna.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.R

/**
 * The palette, the spacing scale and the type scale. One source for all three.
 *
 * Six colours is the whole budget. If something seems to need a seventh, it is
 * usually the layout that is wrong.
 *
 * Every alpha here was chosen by measuring contrast, not by eye. The numbers
 * are in the comments so the next person changing them knows what they are
 * spending.
 */
object Pk {

    /* ── the six ─────────────────────────────────────────────────────── */

    /** The page. Warm, never #FFFFFF — clinical white is the tell of a default theme. */
    val Cream = Color(0xFFF9F6F0)

    /** Ink, and the floating nav. Not #000000: black is harsh to read against. */
    val Charcoal = Color(0xFF222222)

    /** Masthead, primary buttons, eyebrows. 14.3:1 on cream. */
    val Oxblood = Color(0xFF4A0E17)

    /** Hairlines, the streak, today. Used sparingly — gold stops reading as
     *  metal the moment it fills an area. */
    val Gold = Color(0xFFD4AF37)

    /** Done, complete, answered. The only green in the app. */
    val Sage = Color(0xFF8A9A86)

    /** Invitations, and the nav at rest. */
    val Blush = Color(0xFFD8B4A0)

    /* ── derived, not new colours ────────────────────────────────────── */

    /** Card fill: the page pressed slightly in. */
    val Sunken = Color(0xFFF1ECE3)

    /**
     * Secondary text — metadata, supporting lines.
     *
     * 74% charcoal: 6.2:1 on a card, 6.5:1 on the page. This was 55%, which
     * measures **3.5:1** and fails AA for body text. The muted look survives
     * the correction; illegibility was never part of the aesthetic.
     */
    val Muted = Charcoal.copy(alpha = .74f)

    /**
     * The quietest legible tier — section labels.
     *
     * 66% charcoal: 4.8:1 on a card. Was 38% (2.4:1), which is decoration
     * pretending to be a label. Still visibly softer than [Muted], so the
     * hierarchy holds without dropping below the floor.
     */
    val Faint = Charcoal.copy(alpha = .66f)

    /** The only border in the app. */
    val Hair = Charcoal.copy(alpha = .10f)

    /** A verse, lifted off the page. */
    val GoldWash = Gold.copy(alpha = .13f)

    /** The current step in the journey: warm, not loud. */
    val CurrentWash = Blush.copy(alpha = .22f)

    /* ── shape ───────────────────────────────────────────────────────── */

    /** One radius for every card in the app. */
    val CardRadius = 16.dp
    val ButtonRadius = 12.dp
    val NavRadius = 30.dp
    val CardBorder = 1.dp

    /* ── an 8pt scale, so no gap is chosen by eye ─────────────────────── */

    val S1 = 4.dp
    val S2 = 8.dp
    val S3 = 12.dp
    val S4 = 16.dp
    val S5 = 20.dp
    val S6 = 24.dp
    val S7 = 28.dp
    val S8 = 32.dp

    /** Screen horizontal padding. */
    val Gutter = 22.dp

    /** Between one section and the next. */
    val SectionGap = 30.dp

    /** A section label to the card it introduces. */
    val HeadingGap = 12.dp

    /** Inside every card. */
    val CardPad = 20.dp

    /** Row height, so the whole row clears the 48dp touch minimum twice over. */
    val RowHeight = 66.dp

    /** Clearance under the last element so the floating nav never covers it. */
    val NavClearance = 108.dp
}

/**
 * Spectral, for everything including the chrome.
 *
 * A serif for UI labels is unusual and it is the point: it is what stops the
 * app looking like every other Compose app with Roboto in it. Spectral is
 * drawn for screens, so unlike EB Garamond it survives 11sp with wide
 * tracking, which is most of the labels here.
 *
 * EB Garamond stays, but only in the Bible reader, where the job is an hour of
 * continuous reading and an old-style face genuinely reads better.
 *
 * SIL Open Font License; see app/licenses/Spectral-OFL.txt.
 */
val Spectral = FontFamily(
    Font(R.font.spectral_light, FontWeight.Light),
    Font(R.font.spectral_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.spectral_regular, FontWeight.Normal),
    Font(R.font.spectral_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.spectral_medium, FontWeight.Medium),
    Font(R.font.spectral_semi_bold, FontWeight.SemiBold),
)

/**
 * Six named styles. Screens use these and never set a font size.
 *
 * Per-element resizing is how the app ended up with four sizes of the same
 * label on four screens; naming the roles makes that impossible to do by
 * accident.
 */
object PkText {

    /** PRAYERKEY. The strongest type in the app. */
    val Brand = TextStyle(
        fontFamily = Spectral, fontSize = 23.sp,
        fontWeight = FontWeight.Medium, letterSpacing = 3.2.sp,
    )

    /** TODAY'S DEVOTION, GO DEEPER — tracked caps that organise the page. */
    val SectionLabel = TextStyle(
        fontFamily = Spectral, fontSize = 11.sp,
        fontWeight = FontWeight.Normal, letterSpacing = 1.8.sp,
    )

    /** The dated headline. */
    val Display = TextStyle(
        fontFamily = Spectral, fontSize = 34.sp, lineHeight = 39.sp,
        fontWeight = FontWeight.Medium, letterSpacing = (-0.2).sp,
    )

    /** A card's own heading. */
    val CardTitle = TextStyle(
        fontFamily = Spectral, fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** A row's name. */
    val RowTitle = TextStyle(
        fontFamily = Spectral, fontSize = 17.sp,
        fontWeight = FontWeight.Normal,
    )

    /** Minutes, counts, states. 11sp, not 10 — tiny metadata still has to be read. */
    val Meta = TextStyle(
        fontFamily = Spectral, fontSize = 11.sp,
        fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp,
    )

    /** Scripture, and the devotional quote. Light italic: quoted speech. */
    val Scripture = TextStyle(
        fontFamily = Spectral, fontSize = 19.sp, lineHeight = 28.sp,
        fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic,
    )

    /** A verse reference. */
    val Reference = TextStyle(
        fontFamily = Spectral, fontSize = 12.sp,
        fontWeight = FontWeight.Medium, letterSpacing = 1.4.sp,
    )

    /** Under a nav glyph. */
    val NavLabel = TextStyle(
        fontFamily = Spectral, fontSize = 10.sp,
        fontWeight = FontWeight.Normal, letterSpacing = 1.sp,
    )
}
