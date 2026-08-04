package com.prayerkey.manna.data

import androidx.compose.ui.graphics.Color

/**
 * The twelve themes, in four moods of three.
 *
 * Every value here is a real palette the app renders from — the picker
 * changes the whole product, not just a preview tile. A theme chooser that
 * does not repaint the app is theatre.
 *
 * One rule throughout: no pure black. #000000 reads as cheap on OLED and is
 * harsh to look at for long. Dark themes are tinted — navy, moss, indigo,
 * teal — which is also what makes gold and ivory sit well on them.
 */
data class AppTheme(
    val id: String,
    val name: String,
    val mood: String,
    val dark: Boolean,
    /** Page behind everything. */
    val background: Color,
    /** Cards and sheets. */
    val surface: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    /** The cover art gradient, top to bottom; the last stop must meet
     *  [background] so the header bleeds into the cards below it. */
    val header: List<Color>,
)

const val MOOD_MIDNIGHT = "Midnight Sanctuary"
const val MOOD_CATHEDRAL = "Cathedral Heritage"
const val MOOD_DAWN = "The Dawn of Hope"
const val MOOD_EARTH = "Creation & Earth"

val APP_THEMES: List<AppTheme> = listOf(

    /* ── Mood 1 · Midnight Sanctuary ─────────────────────────────── */

    AppTheme(
        id = "imperial_key",
        name = "The Imperial Key",
        mood = MOOD_MIDNIGHT,
        dark = true,
        background = Color(0xFF0A1128),
        surface = Color(0xFF141C3A),
        ink = Color(0xFFF6F0E1),
        muted = Color(0xFF8E97B8),
        accent = Color(0xFFD4AF37),
        header = listOf(Color(0xFF1B2A5B), Color(0xFF131E42), Color(0xFF0A1128)),
    ),
    AppTheme(
        id = "gethsemane",
        name = "Gethsemane Shadows",
        mood = MOOD_MIDNIGHT,
        dark = true,
        background = Color(0xFF14201A),
        surface = Color(0xFF1E2C24),
        ink = Color(0xFFEDEFE4),
        muted = Color(0xFF8B9A88),
        accent = Color(0xFFB6C48A),
        header = listOf(Color(0xFF2A3C2E), Color(0xFF1D2B22), Color(0xFF14201A)),
    ),
    AppTheme(
        id = "eternal_grace",
        name = "Eternal Grace",
        mood = MOOD_MIDNIGHT,
        dark = true,
        background = Color(0xFF120E22),
        surface = Color(0xFF1D1734),
        ink = Color(0xFFF1ECF8),
        muted = Color(0xFF9A8FB8),
        accent = Color(0xFFB98BE0),
        header = listOf(Color(0xFF3A2260), Color(0xFF241844), Color(0xFF120E22)),
    ),

    /* ── Mood 2 · Cathedral Heritage ─────────────────────────────── */

    AppTheme(
        id = "vatican_scroll",
        name = "The Vatican Scroll",
        mood = MOOD_CATHEDRAL,
        dark = false,
        background = Color(0xFFFDFBF7),
        surface = Color(0xFFFFFFFF),
        ink = Color(0xFF2C1A1C),
        muted = Color(0xFF8A7A72),
        accent = Color(0xFF7B2233),
        header = listOf(Color(0xFFF3E3E0), Color(0xFFFAF1EC), Color(0xFFFDFBF7)),
    ),
    AppTheme(
        id = "monastery",
        name = "Monastery Library",
        mood = MOOD_CATHEDRAL,
        dark = false,
        background = Color(0xFFF6EFE2),
        surface = Color(0xFFFFFBF3),
        ink = Color(0xFF3A2A1C),
        muted = Color(0xFF8C7A64),
        accent = Color(0xFF6B4A28),
        header = listOf(Color(0xFFE2D2B6), Color(0xFFEEE3CC), Color(0xFFF6EFE2)),
    ),
    AppTheme(
        id = "ancient_covenant",
        name = "Ancient Covenant",
        mood = MOOD_CATHEDRAL,
        dark = false,
        background = Color(0xFFFFFFFC),
        surface = Color(0xFFFFFFFF),
        ink = Color(0xFF1E1B18),
        muted = Color(0xFF8E8A82),
        accent = Color(0xFFB08A2E),
        header = listOf(Color(0xFFF3EAD4), Color(0xFFFAF6EA), Color(0xFFFFFFFC)),
    ),

    /* ── Mood 3 · The Dawn of Hope ───────────────────────────────── */

    AppTheme(
        id = "rising_grace",
        name = "Rising Grace",
        mood = MOOD_DAWN,
        dark = false,
        background = Color(0xFFFFFDFC),
        surface = Color(0xFFFFFFFF),
        ink = Color(0xFF2A1E22),
        muted = Color(0xFF9A8288),
        accent = Color(0xFFE8734A),
        header = listOf(Color(0xFFF8A07A), Color(0xFFFBC9AE), Color(0xFFFFFDFC)),
    ),
    AppTheme(
        id = "mercy_dew",
        name = "Mercy Dew",
        mood = MOOD_DAWN,
        dark = false,
        background = Color(0xFFFAFCFA),
        surface = Color(0xFFFFFFFF),
        ink = Color(0xFF1E2A24),
        muted = Color(0xFF88998E),
        accent = Color(0xFF5FA383),
        header = listOf(Color(0xFFBEDDCB), Color(0xFFDCEDE2), Color(0xFFFAFCFA)),
    ),
    AppTheme(
        id = "seraphim_sky",
        name = "Seraphim Sky",
        mood = MOOD_DAWN,
        dark = false,
        background = Color(0xFFFCFBFF),
        surface = Color(0xFFFFFFFF),
        ink = Color(0xFF23203A),
        muted = Color(0xFF8E8AA8),
        accent = Color(0xFF7C7BD8),
        header = listOf(Color(0xFFC4C6F0), Color(0xFFDEDFF8), Color(0xFFFCFBFF)),
    ),

    /* ── Mood 4 · Creation & Earth ───────────────────────────────── */

    AppTheme(
        id = "quiet_mountain",
        name = "The Quiet Mountain",
        mood = MOOD_EARTH,
        dark = true,
        background = Color(0xFF16202C),
        surface = Color(0xFF1F2B3A),
        ink = Color(0xFFEAF0F6),
        muted = Color(0xFF8496A8),
        accent = Color(0xFF7FA8C9),
        header = listOf(Color(0xFF33506B), Color(0xFF223448), Color(0xFF16202C)),
    ),
    AppTheme(
        id = "living_water",
        name = "Living Water",
        mood = MOOD_EARTH,
        dark = true,
        background = Color(0xFF0E2226),
        surface = Color(0xFF163038),
        ink = Color(0xFFE6F3F0),
        muted = Color(0xFF7EA09C),
        accent = Color(0xFF6FCBB0),
        header = listOf(Color(0xFF1D4A4E), Color(0xFF143338), Color(0xFF0E2226)),
    ),
    AppTheme(
        id = "autumn_devotion",
        name = "Autumn Devotion",
        mood = MOOD_EARTH,
        dark = false,
        background = Color(0xFFFBF4EE),
        surface = Color(0xFFFFFBF7),
        ink = Color(0xFF31201A),
        muted = Color(0xFF987868),
        accent = Color(0xFFB35A38),
        header = listOf(Color(0xFFDE9B78), Color(0xFFEFC5AA), Color(0xFFFBF4EE)),
    ),
)

/** Falls back to the first theme rather than crashing on a stale id. */
fun themeById(id: String): AppTheme = APP_THEMES.firstOrNull { it.id == id } ?: APP_THEMES.first()
