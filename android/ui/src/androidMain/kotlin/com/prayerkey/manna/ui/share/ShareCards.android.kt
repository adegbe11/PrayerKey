package com.prayerkey.manna.ui.share

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.createFontFamilyResolver
import com.prayerkey.manna.platform.PlatformContext
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.encodePng(): ByteArray =
    ByteArrayOutputStream().also { asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 96, it) }.toByteArray()

actual fun cardFontResolver(context: PlatformContext): FontFamily.Resolver = createFontFamilyResolver(context)

/** Android's renderers are installed by the app at launch (AndroidShareCards); until then, nothing. */
actual fun defaultShareCards(): ShareCards = NoShareCards
