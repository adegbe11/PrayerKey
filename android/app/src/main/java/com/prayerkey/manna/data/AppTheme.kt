package com.prayerkey.manna.data

import androidx.compose.ui.graphics.Color

/**
 * Five themes.
 *
 * There were twelve, in four moods of three. Twelve is a worse offer than
 * five: it is a wall of near-identical swatches, it triples the surface area
 * where contrast can go wrong, and nobody browses past the third. Three is
 * the floor — light, dark, and one accent. Five is the room to have a mood
 * without the app becoming a colour picker.
 *
 * The discipline that makes five work: **backgrounds stay neutral and only
 * the accent moves.** Grace and Vigil set the two papers, warm white and deep
 * navy. Peace, Joy and Spirit are Grace's paper with a different accent, so
 * body text is the same charcoal on the same warm white in all four light
 * themes and cannot be made unreadable by a colour choice.
 *
 * No pure black and no pure white. #000000 is harsh to read against for long
 * and #FFFFFF is clinical; both are the giveaway of an app that picked its
 * colours from a default palette.
 */
data class AppTheme(
    val id: String,
    val name: String,
    /** One line, shown under the name in settings. */
    val note: String,
    val dark: Boolean,
    /** Page behind everything. */
    val background: Color,
    /** Cards and sheets. */
    val surface: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    /** Header wash, top to bottom; the last stop must meet [background] so a
     *  header bleeds into the cards below it rather than banding. */
    val header: List<Color>,
)

/* The two papers. Every light theme shares Grace's, so text contrast is
   settled once rather than five times. */
private val WarmWhite = Color(0xFFFAF7F2)
private val WarmCard = Color(0xFFF2EDE6)
private val Charcoal = Color(0xFF222222)
private val WarmMuted = Color(0xBD222222)

private val Navy = Color(0xFF1A0508)
private val NavyCard = Color(0xFF260A0F)
private val OffWhite = Color(0xFFFDFBF7)
private val NavyMuted = Color(0x99FDFBF7)

val APP_THEMES: List<AppTheme> = listOf(

    AppTheme(
        id = "grace",
        name = "Grace",
        note = "Ink binding and warm scripture page",
        dark = false,
        background = WarmWhite,
        surface = WarmCard,
        ink = Charcoal,
        muted = WarmMuted,
        // terracotta gold: the warm metal of a tooled board, not lime
        accent = Color(0xFF4A0E17),
        header = listOf(Charcoal, Color(0xFF2C2C2E), WarmWhite),
    ),

    AppTheme(
        id = "vigil",
        name = "Vigil",
        note = "Deep navy, for reading at night",
        dark = true,
        background = Navy,
        surface = NavyCard,
        ink = OffWhite,
        muted = NavyMuted,
        accent = Color(0xFF4A0E17),
        header = listOf(Color(0xFF2C2C2E), Charcoal, Navy),
    ),

    AppTheme(
        id = "peace",
        name = "Peace",
        note = "Sage, to settle",
        dark = false,
        background = WarmWhite,
        surface = WarmCard,
        ink = Charcoal,
        muted = WarmMuted,
        accent = Color(0xFF4A0E17),
        header = listOf(Charcoal, Color(0xFF2C2C2E), WarmWhite),
    ),

    AppTheme(
        id = "joy",
        name = "Joy",
        note = "Terracotta, to lift",
        dark = false,
        background = WarmWhite,
        surface = WarmCard,
        ink = Charcoal,
        muted = WarmMuted,
        accent = Color(0xFF4A0E17),
        header = listOf(Charcoal, Color(0xFF2C2C2E), WarmWhite),
    ),

    AppTheme(
        id = "spirit",
        name = "Spirit",
        note = "Lavender, to reflect",
        dark = false,
        background = WarmWhite,
        surface = WarmCard,
        ink = Charcoal,
        muted = WarmMuted,
        accent = Color(0xFF4A0E17),
        header = listOf(Charcoal, Color(0xFF2C2C2E), WarmWhite),
    ),
)

/** Grace, unless the phone is in dark mode and the user follows the system. */
const val DEFAULT_THEME_ID = "grace"
const val DARK_THEME_ID = "vigil"

fun themeById(id: String): AppTheme =
    APP_THEMES.firstOrNull { it.id == id } ?: APP_THEMES.first()

/**
 * Resolves what to actually paint.
 *
 * With [followSystem] on, the phone decides light or dark and the chosen
 * theme only supplies the accent — so someone who picked Peace gets sage on
 * warm white by day and sage on navy at night, rather than being dragged
 * back to a light page at 2am.
 */
fun resolveTheme(id: String, followSystem: Boolean, systemDark: Boolean): AppTheme {
    val chosen = themeById(id)
    if (!followSystem) return chosen
    return if (systemDark) {
        themeById(DARK_THEME_ID).copy(accent = chosen.accent)
    } else {
        themeById(DEFAULT_THEME_ID).copy(accent = chosen.accent)
    }
}
