package com.prayerkey.manna.platform

// Windows native only proves the shared code has no Java in it; it is never shipped.
actual fun PlatformContext.keyValues(name: String): KeyValues = error("not a shipping target")
actual fun PlatformContext.readAsset(path: String): String? = null
actual fun PlatformContext.readFile(name: String): String? = null
actual fun PlatformContext.writeFile(name: String, text: String) = Unit
actual fun deleteFile(path: String) = Unit
actual suspend fun httpRequest(url: String, method: String, body: String?, headers: Map<String, String>): HttpResult =
    error("not a shipping target")
