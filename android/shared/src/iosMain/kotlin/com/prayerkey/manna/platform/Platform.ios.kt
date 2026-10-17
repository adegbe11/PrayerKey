package com.prayerkey.manna.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.stringWithContentsOfFile

actual fun PlatformContext.keyValues(name: String): KeyValues = IosKeyValues(NSUserDefaults(suiteName = name))

@OptIn(ExperimentalForeignApi::class)
actual fun PlatformContext.readAsset(path: String): String? {
    val base = path.substringBeforeLast('.')
    val ext = path.substringAfterLast('.', "")
    val file = NSBundle.mainBundle.pathForResource(base, ext) ?: return null
    return NSString.stringWithContentsOfFile(file, NSUTF8StringEncoding, null)
}

/** NSUserDefaults with SharedPreferences' shape; a string set is stored as an array. */
private class IosKeyValues(private val d: NSUserDefaults) : KeyValues {
    override fun getString(key: String, defValue: String?): String? = d.stringForKey(key) ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? =
        (d.arrayForKey(key) as? List<String>)?.toSet() ?: defValues

    override fun getInt(key: String, defValue: Int): Int = if (contains(key)) d.integerForKey(key).toInt() else defValue
    override fun getLong(key: String, defValue: Long): Long = if (contains(key)) d.integerForKey(key) else defValue
    override fun getFloat(key: String, defValue: Float): Float = if (contains(key)) d.floatForKey(key) else defValue
    override fun getBoolean(key: String, defValue: Boolean): Boolean = if (contains(key)) d.boolForKey(key) else defValue
    override fun contains(key: String): Boolean = d.objectForKey(key) != null
    override fun edit(): KeyValues.Editor = Ed(d)

    /** Writes land when apply() is called, as on Android. */
    private class Ed(private val d: NSUserDefaults) : KeyValues.Editor {
        private val ops = mutableListOf<() -> Unit>()
        private fun op(f: () -> Unit): KeyValues.Editor { ops += f; return this }
        override fun putString(key: String, value: String?) = op { if (value == null) d.removeObjectForKey(key) else d.setObject(value, key) }
        override fun putStringSet(key: String, values: Set<String>?) = op { if (values == null) d.removeObjectForKey(key) else d.setObject(values.toList(), key) }
        override fun putInt(key: String, value: Int) = op { d.setInteger(value.toLong(), key) }
        override fun putLong(key: String, value: Long) = op { d.setInteger(value, key) }
        override fun putFloat(key: String, value: Float) = op { d.setFloat(value, key) }
        override fun putBoolean(key: String, value: Boolean) = op { d.setBool(value, key) }
        override fun remove(key: String) = op { d.removeObjectForKey(key) }
        override fun clear() = op { d.dictionaryRepresentation().keys.forEach { d.removeObjectForKey(it as String) } }
        override fun apply() { ops.forEach { it() }; ops.clear() }
    }
}
