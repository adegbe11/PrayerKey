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
val Ink = Color(0xFF1A0A2E)
val Muted = Color(0xBD1A0A2E)
val Canvas = Color(0xFFF5F5F7)
val AppleGray = Color(0xFFF5F5F7)
val Ivory = Color(0xFFFFFFFF)
val Night = Color(0xFF1A0A2E)
val Gold = Color(0xFFC9A26D)
val GiltLine = Color(0xFFC9A26D)
/** Historic name retained for call sites; it now means the one primary action. */
val Electric = Color(0xFF6200ED)
val Hairline = Color(0x1A1A0A2E)

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
    // One identity in two reading conditions: warm paper by day, midnight
    // oxblood after dark. Accent and semantic colours never change brands.
    val scheme = if (theme.dark) androidx.compose.material3.darkColorScheme(
        primary = Pk.Oxblood, onPrimary = Pk.Crisp,
        primaryContainer = Pk.Oxblood, onPrimaryContainer = Pk.Crisp,
        secondary = Pk.Sage, onSecondary = Pk.DeepDark,
        secondaryContainer = Pk.Sage.copy(alpha = .24f), onSecondaryContainer = Pk.Crisp,
        tertiary = Pk.Gold, onTertiary = Pk.DeepDark,
        background = Pk.DeepDark, onBackground = Pk.Crisp,
        surface = Pk.DeepDark, onSurface = Pk.Crisp,
        surfaceVariant = Pk.Oxblood.copy(alpha = .42f), onSurfaceVariant = Pk.Crisp.copy(alpha = .74f),
        inverseSurface = Pk.Crisp, inverseOnSurface = Pk.DeepDark,
        surfaceTint = Color.Transparent,
        outline = Pk.Crisp.copy(alpha = .16f), outlineVariant = Pk.Crisp.copy(alpha = .10f),
        scrim = Pk.DeepDark.copy(alpha = .72f),
        error = Pk.Blush, onError = Pk.DeepDark,
    ) else lightColorScheme(
        primary = Pk.Oxblood, onPrimary = Pk.Crisp,
        primaryContainer = Pk.Blush, onPrimaryContainer = Pk.Oxblood,
        secondary = Pk.Sage, onSecondary = Pk.Charcoal,
        secondaryContainer = Pk.Sage.copy(alpha = .24f), onSecondaryContainer = Pk.Charcoal,
        tertiary = Pk.Gold, onTertiary = Pk.Charcoal,
        tertiaryContainer = Pk.Gold.copy(alpha = .14f), onTertiaryContainer = Pk.Charcoal,
        background = Pk.Cream, onBackground = Pk.Charcoal,
        surface = Pk.Cream, onSurface = Pk.Charcoal,
        surfaceVariant = Pk.Sunken, onSurfaceVariant = Pk.Muted,
        inverseSurface = Pk.DeepDark, inverseOnSurface = Pk.Crisp,
        inversePrimary = Pk.Blush,
        surfaceTint = Color.Transparent,
        outline = Pk.Hair, outlineVariant = Pk.Hair,
        scrim = Pk.DeepDark.copy(alpha = .42f),
        error = Pk.Oxblood, onError = Pk.Crisp,
        errorContainer = Pk.Blush, onErrorContainer = Pk.Oxblood,
    )
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
