package com.prayerkey.manna.platform

import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

/**
 * What the shared code needs from the phone it runs on.
 *
 * On Android a PlatformContext is the app's Context, so every store keeps the
 * signature it had - `PrayerChallengeStore.load(context)` - and the Android
 * screens did not change. On iPhone it is a single app-wide object.
 */
expect abstract class PlatformContext

/**
 * Small saved settings, kept under a name. The shape is SharedPreferences'
 * own, so the stores moved here read exactly as they did on Android; on
 * iPhone they are NSUserDefaults.
 */
interface KeyValues {
    fun getString(key: String, defValue: String?): String?
    fun getStringSet(key: String, defValues: Set<String>?): Set<String>?
    fun getInt(key: String, defValue: Int): Int
    fun getLong(key: String, defValue: Long): Long
    fun getFloat(key: String, defValue: Float): Float
    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun contains(key: String): Boolean
    fun edit(): Editor

    interface Editor {
        fun putString(key: String, value: String?): Editor
        fun putStringSet(key: String, values: Set<String>?): Editor
        fun putInt(key: String, value: Int): Editor
        fun putLong(key: String, value: Long): Editor
        fun putFloat(key: String, value: Float): Editor
        fun putBoolean(key: String, value: Boolean): Editor
        fun remove(key: String): Editor
        fun clear(): Editor
        fun apply()
    }
}

expect fun PlatformContext.keyValues(name: String): KeyValues

/** A file shipped inside the app (the prayers, the Bible, the devotionals), or null. */
expect fun PlatformContext.readAsset(path: String): String?

/** Where file reads run: the IO pool on phones; the web has none and uses Default. */
expect val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher

/** Today on the phone's own calendar. */
fun today(): kotlinx.datetime.LocalDate =
    kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())

/** The phone's current time of day and date. */
fun now(): kotlinx.datetime.LocalDateTime =
    kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())

/** A file the app keeps for itself, by name: Android's files folder; the iPhone app's Documents. */
expect fun PlatformContext.readFile(name: String): String?

/** Replaces the whole file, safely: written beside it first, then moved into place. */
expect fun PlatformContext.writeFile(name: String, text: String)

/** Removes a file by its full path, if it is there. */
expect fun deleteFile(path: String)

/** What a web request came back with. [code] is 0 when nothing came back at all. */
class HttpResult(val code: Int, val text: String)

/** One HTTPS request, off the main thread. Throws when there is no connection. */
expect suspend fun httpRequest(url: String, method: String, body: String?, headers: Map<String, String>): HttpResult

/** Percent-encodes [value] for a URL's path or query (spaces become %20). */
fun encodeUrlComponent(value: String): String = buildString {
    for (byte in value.encodeToByteArray()) {
        val c = byte.toInt() and 0xFF
        val ch = c.toChar()
        if (ch.isLetterOrDigit() && c < 128 || ch in "-_.~") append(ch)
        else { append('%'); append("0123456789ABCDEF"[c shr 4]); append("0123456789ABCDEF"[c and 15]) }
    }
}
