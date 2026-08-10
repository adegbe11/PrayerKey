package com.prayerkey.manna.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/* Apple-white premium system:
   pure #FFFFFF canvas, #1D1D1F ink, #86868B secondary, #F5F5F7 fills. */
val Ink = Color(0xFF12161F)
val Muted = Color(0x8C12161F)
val Canvas = Color(0xFFFAF6EF)
val AppleGray = Color(0xFFF2EBE0)
val Ivory = Color(0xFFFAF6EF)
val Night = Color(0xFF12161F)
/** Historic name retained for call sites; semantic accent is now rubric red. */
val Gold = Color(0xFFA4231C)
val GiltLine = Color(0xFFC9A227)
/** Compatibility accent for older surfaces; intentionally blue-grey, never neon. */
val Electric = Color(0xFFA4231C)
val Hairline = Color(0x1712161F)

/**
 * Type scale.
 *
 * Large serif needs negative tracking or it reads as a word processor;
 * small caps labels need positive tracking or they read as shouting. Both
 * were left at Material defaults, which is a large part of why the app
 * looked stock.
 */
private val NoPad = PlatformTextStyle(includeFontPadding = false)
private val Trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun display(size: Int, line: Int, tracking: Double) = TextStyle(
    fontFamily = DisplaySerif,
    fontWeight = FontWeight.Medium,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
    platformStyle = NoPad,
    lineHeightStyle = Trim,
)

private fun body(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = UtilitySans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = (-0.1).sp,
    /* No colour here. A colour baked into the type scale wins over the
       theme for every Text that does not name one, which is why labels,
       placeholders and chips stayed near-black on the dark themes. Leaving
       it unset lets LocalContentColor — and so the palette — decide. */
    platformStyle = NoPad,
    lineHeightStyle = Trim,
)

private val MannaType = Typography(
    displayLarge = display(40, 46, -1.2),
    displayMedium = display(34, 41, -0.9),
    displaySmall = display(28, 34, -0.6),
    headlineMedium = display(23, 29, -0.4),
    titleLarge = body(19, 25, FontWeight.SemiBold),
    titleMedium = body(16, 22, FontWeight.SemiBold),
    bodyLarge = body(16, 25),
    bodyMedium = body(14, 21),
    bodySmall = body(12, 18),
    labelLarge = body(14, 18, FontWeight.SemiBold),
    labelMedium = TextStyle(
        fontFamily = UtilitySans,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 1.6.sp,
        platformStyle = NoPad,
    ),
)

@Composable
fun MannaTheme(
    theme: com.prayerkey.manna.data.AppTheme = com.prayerkey.manna.data.APP_THEMES.first(),
    content: @Composable () -> Unit,
) {
    // the chosen theme repaints the whole app, not just a preview tile
    val scheme = if (theme.dark) {
        androidx.compose.material3.darkColorScheme(
            primary = theme.accent, onPrimary = Ivory,
            primaryContainer = Color(0xFF2A1718), onPrimaryContainer = Color(0xFFFAF6EF),
            secondary = theme.accent, onSecondary = Color(0xFF10131F),
            secondaryContainer = Color(0xFF2A1718), onSecondaryContainer = Color(0xFFFAF6EF),
            tertiary = GiltLine, onTertiary = Color(0xFF12161F),
            tertiaryContainer = Color(0xFF25221A), onTertiaryContainer = Color(0xFFFAF6EF),
            background = theme.background, onBackground = theme.ink,
            surface = theme.surface, onSurface = theme.ink,
            surfaceVariant = theme.surface, onSurfaceVariant = theme.muted,
            surfaceTint = Color.Transparent,
            outline = theme.muted.copy(alpha = .3f), outlineVariant = theme.muted.copy(alpha = .22f),
            error = Color(0xFFE0796A), onError = Color(0xFF2A0E0A),
        )
    } else {
        lightColorScheme(
            primary = theme.accent, onPrimary = Ivory,
            primaryContainer = Color(0xFFF2DDD8), onPrimaryContainer = Color(0xFF68130F),
            secondary = theme.accent, onSecondary = Ivory,
            secondaryContainer = Color(0xFFF2DDD8), onSecondaryContainer = Color(0xFF68130F),
            tertiary = GiltLine, onTertiary = Ink,
            tertiaryContainer = Color(0xFFF1E8CF), onTertiaryContainer = Color(0xFF4B3A08),
            background = theme.background, onBackground = theme.ink,
            surface = theme.surface, onSurface = theme.ink,
            surfaceVariant = theme.background, onSurfaceVariant = theme.muted,
            surfaceTint = Color.Transparent,
            outline = theme.muted.copy(alpha = .3f), outlineVariant = theme.muted.copy(alpha = .22f),
            error = Color(0xFFB3402A), onError = Ivory,
        )
    }
    MaterialTheme(colorScheme = scheme, typography = MannaType, content = content)
}

@Composable
private fun LegacyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        /* Every slot is named. Leaving the container/tint slots at their
           defaults let Material's baseline purple through on chips, ripples
           and selection states — the single most "stock Android" thing in
           the app. */
        colorScheme = lightColorScheme(
            primary = Electric,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE6EBFF),
            onPrimaryContainer = Color(0xFF10286F),
            inversePrimary = Color(0xFFB9C7FF),

            secondary = Gold,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF6EEDC),
            onSecondaryContainer = Color(0xFF5C4310),

            tertiary = Night,
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFE7E9F2),
            onTertiaryContainer = Night,

            background = Canvas,
            onBackground = Ink,
            surface = Color.White,
            onSurface = Ink,
            surfaceVariant = AppleGray,
            onSurfaceVariant = Muted,
            surfaceTint = Color.Transparent,      // no purple elevation tint
            inverseSurface = Night,
            inverseOnSurface = Ivory,

            outline = Hairline,
            outlineVariant = Hairline,
            scrim = Night.copy(alpha = .32f),

            error = Color(0xFFB3402A),
            onError = Color.White,
            errorContainer = Color(0xFFFBEAE6),
            onErrorContainer = Color(0xFF6E2417),
        ),
        typography = MannaType,
        content = content,
    )
}
