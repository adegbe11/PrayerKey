package com.prayerkey.manna.platform

// Windows native only proves the shared code has no Java in it; it is never shipped.
actual fun PlatformContext.keyValues(name: String): KeyValues = error("not a shipping target")
actual fun PlatformContext.readAsset(path: String): String? = null
