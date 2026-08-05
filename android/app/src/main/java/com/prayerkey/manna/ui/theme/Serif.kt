package com.prayerkey.manna.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.prayerkey.manna.R

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
@OptIn(ExperimentalTextApi::class)
private fun garamond(weight: Int) = Font(
    R.font.eb_garamond,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val BookSerif = FontFamily(
    garamond(400),
    garamond(500),
    garamond(600),
    garamond(700),
)
