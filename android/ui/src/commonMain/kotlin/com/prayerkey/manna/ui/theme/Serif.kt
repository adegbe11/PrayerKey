package com.prayerkey.manna.ui.theme

import androidx.compose.ui.text.font.FontFamily

/**
 * The app's serif.
 *
 * Everything sacred in Manna is set in serif — the verse cards, the book,
 * every headline — and until now that serif was whatever Android happened to
 * supply, which is Noto Serif: a competent screen face with no warmth and no
 * history. It is the single reason the app read as ordinary next to the
 * mockups.
 *
 * EB Garamond is a digitisation of Claude Garamont's sixteenth-century
 * romans, the tradition actual Bibles have been set in for four hundred
 * years. It ships under the SIL Open Font License (see res/font/OFL.txt),
 * free to bundle and redistribute, so this costs nothing but the bytes.
 *
 * One variable file carries the whole 400–800 weight range, which is why
 * 831K buys every weight rather than one. minSdk is 26, and variable font
 * axes need 26, so every device that can run Manna can render this.
 */
/** Sacred display titles only. */
val DisplaySerif: FontFamily get() = PkFonts.display

/** Scripture, devotional prose, quotes and prayer. */
val BookSerif: FontFamily get() = PkFonts.book

/** Dates, controls, buttons, metadata and navigation. */
val UtilitySans: FontFamily get() = PkFonts.utility

/**
 * Roman capitals, for the board of the Bible and nothing else.
 *
 * Cinzel is drawn from the inscriptional capitals of the Roman Empire — the
 * Trajan tradition that tooled book covers have used ever since. Garamond's
 * caps are lovely in a paragraph but too fine to carry a stamped title, which
 * is why HOLY BIBLE looked printed rather than pressed.
 *
 * SIL Open Font License, one variable file, 123K.
 */
val RomanCaps: FontFamily get() = PkFonts.romanCaps
