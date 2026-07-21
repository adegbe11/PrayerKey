package com.prayerkey.manna.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF171719)
val Muted = Color(0xFF737376)
val Canvas = Color(0xFFFAFAF8)
val Ivory = Color(0xFFFFFCF4)
val Night = Color(0xFF14182A)
val Gold = Color(0xFFB07C1F)
val Electric = Color(0xFF315CFF)
val Hairline = Color(0xFFEAE8E2)

@Composable
fun MannaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Electric, secondary = Gold, background = Canvas, surface = Color.White, onPrimary = Color.White, onBackground = Ink, onSurface = Ink),
        content = content,
    )
}
