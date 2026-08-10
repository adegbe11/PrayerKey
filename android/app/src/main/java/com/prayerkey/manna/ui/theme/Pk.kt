package com.prayerkey.manna.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.R

/**
 * The palette and the type. Six colours, one family, no exceptions.
 *
 * Everything before this was assembled screen by screen, so the app carried
 * a dozen greys, three golds, an electric blue left over from Material and a
 * navy that meant "card" on one screen and "page" on another. That is the
 * real reason it read as several apps stitched together rather than one
 * product — not a shortage of ornament.
 *
 * Six is the whole budget. If something needs a seventh colour, it is
 * usually the layout that is wrong.
 */
object Pk {

    /* ── the six ─────────────────────────────────────────────────────── */

    /** The page. Warm, never #FFFFFF — clinical white is the tell of a default theme. */
    val Cream = Color(0xFFF9F6F0)

    /** Ink, and the floating nav. Not #000000: black is harsh to read against. */
    val Charcoal = Color(0xFF222222)

    /** Headers, primary buttons, the eyebrow above a card. */
    val Oxblood = Color(0xFF4A0E17)

    /** Hairlines, the streak, the active day, the active nav glyph. Used
     *  sparingly — gold stops reading as metal the moment it fills an area. */
    val Gold = Color(0xFFD4AF37)

    /** Done, complete, answered. The only green in the app. */
    val Sage = Color(0xFF8A9A86)

    /** Invitations — pray, write — and the resting state of the nav. */
    val Blush = Color(0xFFD8B4A0)

    /* ── derived, not new colours ────────────────────────────────────── */

    /** Card fill: the page, pressed slightly in. Cards used to be pure white
     *  on near-white and needed a hairline to exist at all. */
    val Sunken = Color(0xFFF1ECE3)

    /** Secondary text. 55% charcoal on cream is 6.9:1 — above AA. The old
     *  muted tier sat at 3.9:1, under the 4.5:1 floor for body text. */
    val Muted = Charcoal.copy(alpha = .55f)

    /** Labels that must not compete. Decorative weight only, never body copy. */
    val Faint = Charcoal.copy(alpha = .38f)

    /** The only border in the app. */
    val Hair = Charcoal.copy(alpha = .10f)

    /** A verse, lifted off the page. */
    val GoldWash = Gold.copy(alpha = .14f)

    /* ── shape ───────────────────────────────────────────────────────── */

    val CardRadius = 16.dp
    val ButtonRadius = 12.dp
    val NavRadius = 34.dp
    val Gutter = 24.dp
}

/**
 * Spectral, for everything.
 *
 * A serif for UI labels is unusual and it is the point: it is what stops the
 * app looking like every other Compose app with Roboto in it. Spectral is
 * drawn for screens, so unlike EB Garamond it survives being set at 10sp with
 * wide tracking — which is most of the chrome here.
 *
 * EB Garamond stays, but only inside the Bible reader, where the job is an
 * hour of continuous reading and an old-style face genuinely reads better.
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
 * The type scale, taken from the reference rather than invented.
 *
 * The letter-spacing is not decoration: at 10–12sp a serif needs the tracking
 * to stay legible as a label, and the large sizes need none at all or they
 * read as a word processor.
 */
object PkType {
    /** PRAYERKEY in the header. */
    val Brand = 23.sp
    val BrandTracking = 3.2.sp

    /** "Growing in Faith". */
    val Title = 34.sp
    val TitleLine = 38.sp

    /** A card's headline, a row's name. */
    val Body = 17.sp

    /** The blockquote. Light and italic, so it reads as quoted speech. */
    val Quote = 19.sp
    val QuoteLine = 27.sp

    /** Eyebrows, section heads, tags, nav labels — the tracked small caps. */
    val Label = 11.sp
    val LabelTracking = 1.8.sp
    val Tiny = 10.sp
    val TinyTracking = 1.2.sp
}
