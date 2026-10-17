package com.prayerkey.manna.platform

import android.content.Context
import android.content.SharedPreferences

actual typealias PlatformContext = Context

actual fun PlatformContext.keyValues(name: String): KeyValues =
    AndroidKeyValues(getSharedPreferences(name, Context.MODE_PRIVATE))

actual fun PlatformContext.readAsset(path: String): String? =
    runCatching { assets.open(path).bufferedReader().use { it.readText() } }.getOrNull()

private class AndroidKeyValues(private val p: SharedPreferences) : KeyValues {
    override fun getString(key: String, defValue: String?): String? = p.getString(key, defValue)
    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = p.getStringSet(key, defValues)
    override fun getInt(key: String, defValue: Int): Int = p.getInt(key, defValue)
    override fun getLong(key: String, defValue: Long): Long = p.getLong(key, defValue)
    override fun getFloat(key: String, defValue: Float): Float = p.getFloat(key, defValue)
    override fun getBoolean(key: String, defValue: Boolean): Boolean = p.getBoolean(key, defValue)
    override fun contains(key: String): Boolean = p.contains(key)
    override fun edit(): KeyValues.Editor = Ed(p.edit())

    private class Ed(private val e: SharedPreferences.Editor) : KeyValues.Editor {
        override fun putString(key: String, value: String?): KeyValues.Editor { e.putString(key, value); return this }
        override fun putStringSet(key: String, values: Set<String>?): KeyValues.Editor { e.putStringSet(key, values); return this }
        override fun putInt(key: String, value: Int): KeyValues.Editor { e.putInt(key, value); return this }
        override fun putLong(key: String, value: Long): KeyValues.Editor { e.putLong(key, value); return this }
        override fun putFloat(key: String, value: Float): KeyValues.Editor { e.putFloat(key, value); return this }
        override fun putBoolean(key: String, value: Boolean): KeyValues.Editor { e.putBoolean(key, value); return this }
        override fun remove(key: String): KeyValues.Editor { e.remove(key); return this }
        override fun clear(): KeyValues.Editor { e.clear(); return this }
        override fun apply() = e.apply()
    }
}
