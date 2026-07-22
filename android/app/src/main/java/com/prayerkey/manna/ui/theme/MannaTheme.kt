package com.prayerkey.manna.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

@Composable
fun MannaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Electric, secondary = Gold, background = Canvas, surface = Color.White, onPrimary = Color.White, onBackground = Ink, onSurface = Ink),
        content = content,
    )
}
