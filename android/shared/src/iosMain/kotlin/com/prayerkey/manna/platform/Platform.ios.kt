package com.prayerkey.manna.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.HTTPBody
import platform.Foundation.HTTPMethod
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.setValue
import platform.Foundation.dataUsingEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

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

private fun documentsPath(name: String): String {
    val dir = platform.Foundation.NSSearchPathForDirectoriesInDomains(
        platform.Foundation.NSDocumentDirectory, platform.Foundation.NSUserDomainMask, true,
    ).first() as String
    return "$dir/$name"
}

@OptIn(ExperimentalForeignApi::class)
actual fun PlatformContext.readFile(name: String): String? =
    NSString.stringWithContentsOfFile(documentsPath(name), NSUTF8StringEncoding, null)

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
actual fun PlatformContext.writeFile(name: String, text: String) {
    // atomically = written to a temporary file and moved into place
    NSString.create(string = text).writeToFile(documentsPath(name), true, NSUTF8StringEncoding, null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun deleteFile(path: String) {
    platform.Foundation.NSFileManager.defaultManager.removeItemAtPath(path, null)
}

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
actual suspend fun httpRequest(url: String, method: String, body: String?, headers: Map<String, String>): HttpResult =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        val nsUrl = platform.Foundation.NSURL.URLWithString(url) ?: run {
            cont.resumeWith(Result.failure(IllegalArgumentException("Bad URL"))); return@suspendCancellableCoroutine
        }
        val request = platform.Foundation.NSMutableURLRequest(
            uRL = nsUrl, cachePolicy = platform.Foundation.NSURLRequestUseProtocolCachePolicy, timeoutInterval = 30.0,
        ).apply {
            HTTPMethod = method
            headers.forEach { (k, v) -> setValue(v, forHTTPHeaderField = k) }
            if (body != null) HTTPBody = NSString.create(string = body).dataUsingEncoding(NSUTF8StringEncoding)
        }
        val task = platform.Foundation.NSURLSession.sharedSession.dataTaskWithRequest(
            request,
            completionHandler = { data: platform.Foundation.NSData?, response: platform.Foundation.NSURLResponse?, error: platform.Foundation.NSError? ->
            if (error != null) {
                cont.resumeWith(Result.failure(IllegalStateException(error.localizedDescription)))
            } else {
                val code = (response as? platform.Foundation.NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
                val text = data?.let { NSString.create(data = it, encoding = NSUTF8StringEncoding)?.toString() }.orEmpty()
                cont.resumeWith(Result.success(HttpResult(code, text)))
            }
            },
        )
        cont.invokeOnCancellation { task.cancel() }
        task.resume()
    }

actual fun fileExists(path: String): Boolean = platform.Foundation.NSFileManager.defaultManager.fileExistsAtPath(path)
