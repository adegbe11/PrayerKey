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
val Ink = Color(0xFF1D1D1F)
val Muted = Color(0xFF86868B)
val Canvas = Color(0xFFFFFFFF)
val AppleGray = Color(0xFFF5F5F7)
val Ivory = Color(0xFFFFFCF4)
val Night = Color(0xFF14182A)
val Gold = Color(0xFFB07C1F)
val Electric = Color(0xFF315CFF)
val Hairline = Color(0xFFE8E8ED)

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
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Medium,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
    platformStyle = NoPad,
    lineHeightStyle = Trim,
)

private fun body(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = (-0.1).sp,
    color = InkSoft,
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
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 1.6.sp,
        color = Muted,
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
            primary = theme.accent, onPrimary = if (theme.dark) Color(0xFF10131F) else Color.White,
            secondary = theme.accent, onSecondary = Color(0xFF10131F),
            background = theme.background, onBackground = theme.ink,
            surface = theme.surface, onSurface = theme.ink,
            surfaceVariant = theme.surface, onSurfaceVariant = theme.muted,
            surfaceTint = Color.Transparent,
            outline = theme.muted.copy(alpha = .3f), outlineVariant = theme.muted.copy(alpha = .22f),
            error = Color(0xFFE0796A), onError = Color(0xFF2A0E0A),
        )
    } else {
        lightColorScheme(
            primary = theme.accent, onPrimary = Color.White,
            secondary = theme.accent, onSecondary = Color.White,
            background = theme.background, onBackground = theme.ink,
            surface = theme.surface, onSurface = theme.ink,
            surfaceVariant = theme.background, onSurfaceVariant = theme.muted,
            surfaceTint = Color.Transparent,
            outline = theme.muted.copy(alpha = .3f), outlineVariant = theme.muted.copy(alpha = .22f),
            error = Color(0xFFB3402A), onError = Color.White,
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
