package com.prayerkey.manna.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.prayerkey.manna.resources.*
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

/**
 * The typefaces, held where every screen can read them.
 *
 * The fonts are shared resources now, the same files on Android and iPhone,
 * and a shared font can only be opened from inside a composable. So the theme
 * opens them once at the top of the tree ([LoadPkFonts]) and puts them here,
 * and the named families - Spectral, BookSerif and the rest - read from here.
 *
 * Held as state on purpose. On Android the fonts are ready on the first frame
 * and nothing changes. On iPhone they arrive a moment after it, and every
 * piece of text that read a family redraws in the right face by itself.
 */
object PkFonts {
    var spectral by mutableStateOf<FontFamily>(FontFamily.Serif)
    var display by mutableStateOf<FontFamily>(FontFamily.Serif)
    var book by mutableStateOf<FontFamily>(FontFamily.Serif)
    var utility by mutableStateOf<FontFamily>(FontFamily.SansSerif)
    var romanCaps by mutableStateOf<FontFamily>(FontFamily.Serif)
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun variable(res: FontResource, weight: Int, italic: Boolean = false) = Font(
    res,
    weight = FontWeight(weight),
    style = if (italic) FontStyle.Italic else FontStyle.Normal,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Opens every typeface and hands it to [PkFonts]. Called once, by the theme. */
@Composable
fun LoadPkFonts() {
    val spectral = FontFamily(
        Font(Res.font.spectral_light, FontWeight.Light),
        Font(Res.font.spectral_light_italic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.spectral_regular, FontWeight.Normal),
        Font(Res.font.spectral_italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.spectral_medium, FontWeight.Medium),
        Font(Res.font.spectral_semi_bold, FontWeight.SemiBold),
    )
    val display = FontFamily(variable(Res.font.playfair_display, 600), variable(Res.font.playfair_display, 700))
    val book = FontFamily(
        variable(Res.font.lora, 400), variable(Res.font.lora, 500), variable(Res.font.lora, 600), variable(Res.font.lora, 700),
        variable(Res.font.lora_italic, 400, true), variable(Res.font.lora_italic, 500, true), variable(Res.font.lora_italic, 600, true),
    )
    val utility = FontFamily(
        variable(Res.font.inter, 400), variable(Res.font.inter, 500), variable(Res.font.inter, 600), variable(Res.font.inter, 700),
    )
    val caps = FontFamily(
        variable(Res.font.cinzel, 400), variable(Res.font.cinzel, 600), variable(Res.font.cinzel, 700), variable(Res.font.cinzel, 900),
    )
    // equal families are not written again, so recomposing the theme costs nothing
    if (PkFonts.spectral != spectral) PkFonts.spectral = spectral
    if (PkFonts.display != display) PkFonts.display = display
    if (PkFonts.book != book) PkFonts.book = book
    if (PkFonts.utility != utility) PkFonts.utility = utility
    if (PkFonts.romanCaps != caps) PkFonts.romanCaps = caps
}
