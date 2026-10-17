package com.prayerkey.manna.platform

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
