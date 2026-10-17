package com.prayerkey.manna.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/*
 * The web build exists to prove the shared screens carry no Java and no
 * Android, the way the iPhone build will need. It is not shipped, so settings
 * live in memory and there are no bundled files.
 */
actual abstract class PlatformContext

object AppContext : PlatformContext()

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default

actual fun PlatformContext.keyValues(name: String): KeyValues = stores.getOrPut(name) { MemoryKeyValues() }

actual fun PlatformContext.readAsset(path: String): String? = null

private val stores = mutableMapOf<String, KeyValues>()

private class MemoryKeyValues : KeyValues {
    private val map = mutableMapOf<String, Any?>()
    override fun getString(key: String, defValue: String?) = map[key] as? String ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: Set<String>?) = map[key] as? Set<String> ?: defValues
    override fun getInt(key: String, defValue: Int) = map[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long) = map[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float) = map[key] as? Float ?: defValue
    override fun getBoolean(key: String, defValue: Boolean) = map[key] as? Boolean ?: defValue
    override fun contains(key: String) = key in map
    override fun edit(): KeyValues.Editor = object : KeyValues.Editor {
        private val ops = mutableListOf<() -> Unit>()
        private fun op(f: () -> Unit): KeyValues.Editor { ops += f; return this }
        override fun putString(key: String, value: String?) = op { map[key] = value }
        override fun putStringSet(key: String, values: Set<String>?) = op { map[key] = values }
        override fun putInt(key: String, value: Int) = op { map[key] = value }
        override fun putLong(key: String, value: Long) = op { map[key] = value }
        override fun putFloat(key: String, value: Float) = op { map[key] = value }
        override fun putBoolean(key: String, value: Boolean) = op { map[key] = value }
        override fun remove(key: String) = op { map.remove(key) }
        override fun clear() = op { map.clear() }
        override fun apply() { ops.forEach { it() }; ops.clear() }
    }
}

private val files = mutableMapOf<String, String>()
actual fun PlatformContext.readFile(name: String): String? = files[name]
actual fun PlatformContext.writeFile(name: String, text: String) { files[name] = text }
actual fun deleteFile(path: String) { files.remove(path) }

actual suspend fun httpRequest(url: String, method: String, body: String?, headers: Map<String, String>): HttpResult =
    error("the web build is a compile check")

actual fun fileExists(path: String): Boolean = path in files
