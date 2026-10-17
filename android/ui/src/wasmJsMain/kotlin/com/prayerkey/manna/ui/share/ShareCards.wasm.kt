package com.prayerkey.manna.ui.share

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.createFontFamilyResolver
import com.prayerkey.manna.platform.PlatformContext

// The web build is a compile check only.
actual fun ImageBitmap.encodePng(): ByteArray = ByteArray(0)
actual fun cardFontResolver(context: PlatformContext): FontFamily.Resolver = createFontFamilyResolver()
actual fun defaultShareCards(): ShareCards = NoShareCards
