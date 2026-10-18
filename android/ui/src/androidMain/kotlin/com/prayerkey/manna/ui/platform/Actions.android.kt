package com.prayerkey.manna.ui.platform

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.prayerkey.manna.platform.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private fun Context.startAny(intent: Intent) {
    if (this !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

actual fun shareText(context: PlatformContext, text: String, title: String) {
    context.startAny(
        Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), title),
    )
}

actual fun shareImage(context: PlatformContext, png: ByteArray, caption: String, title: String) {
    val directory = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(directory, "manna-${System.currentTimeMillis()}.png")
    file.writeBytes(png)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    context.startAny(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            title,
        ),
    )
}

actual fun saveImageToPhotos(context: PlatformContext, png: ByteArray, name: String): Boolean = runCatching {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$name.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PrayerKey")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
    context.contentResolver.openOutputStream(uri)?.use { it.write(png) }
    values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0)
    context.contentResolver.update(uri, values, null, null)
    true
}.getOrDefault(false)

actual fun showToast(context: PlatformContext, message: String, long: Boolean) {
    Toast.makeText(context, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

@Composable
actual fun PlatformOverlay() = Unit

@Composable
actual fun rememberPermissionRequest(permission: Permission, onResult: (Boolean) -> Unit): PermissionRequest {
    val context = LocalContext.current
    val callback = rememberUpdatedState(onResult)
    val name = when (permission) {
        Permission.Notifications -> Manifest.permission.POST_NOTIFICATIONS
        Permission.Microphone -> Manifest.permission.RECORD_AUDIO
    }
    // notifications needed no permission before Android 13
    val needed = permission != Permission.Notifications || Build.VERSION.SDK_INT >= 33
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { callback.value(it) }
    return remember(launcher, context) {
        object : PermissionRequest {
            override fun granted() = !needed ||
                ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED
            override fun request() { if (granted()) callback.value(true) else launcher.launch(name) }
        }
    }
}

@Composable
actual fun rememberTextFilePicker(onPicked: (String?) -> Unit): Launcher {
    val context = LocalContext.current
    val callback = rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        callback.value(
            uri?.let { runCatching { context.contentResolver.openInputStream(it)?.bufferedReader()?.use { r -> r.readText() } }.getOrNull() },
        )
    }
    return remember(launcher) { Launcher { launcher.launch(arrayOf("application/json", "text/plain", "*/*")) } }
}

@Composable
actual fun rememberMediaPicker(onPicked: (List<String>) -> Unit): MediaPicker {
    val context = LocalContext.current
    val callback = rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        // a lasting grant, or the page shows a broken picture after a restart
        uris.forEach { uri -> runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } }
        callback.value(uris.map { it.toString() })
    }
    return remember(launcher) {
        MediaPicker { kind ->
            launcher.launch(
                when (kind) {
                    MediaKind.Photos -> arrayOf("image/*", "video/*")
                    MediaKind.Attachments -> arrayOf("image/*", "video/*", "audio/*", "application/pdf")
                    MediaKind.AnyFile -> arrayOf("*/*")
                },
            )
        }
    }
}

actual suspend fun loadThumbnail(context: PlatformContext, address: String, edge: Int): ImageBitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(address)
            // a still photo is decoded itself, which is sharper than the system
            // thumbnail; a video has no frame to decode, so it takes the thumbnail
            val video = mimeTypeOf(context, address).startsWith("video/")
            val bitmap = if (video && Build.VERSION.SDK_INT >= 29) {
                context.contentResolver.loadThumbnail(uri, Size(edge, edge), null)
            } else {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= edge) sample *= 2
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
                }
            }
            bitmap?.asImageBitmap()
        }.getOrNull()
    }

actual fun mimeTypeOf(context: PlatformContext, address: String): String =
    runCatching { context.contentResolver.getType(Uri.parse(address)) }.getOrNull().orEmpty()

actual fun openAttachment(context: PlatformContext, address: String) {
    runCatching {
        context.startAny(Intent(Intent.ACTION_VIEW, Uri.parse(address)).apply { addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) })
    }
}

@Composable
actual fun rememberDeviceUnlock(reason: String, onResult: (Boolean) -> Unit): DeviceUnlock {
    val context = LocalContext.current
    val callback = rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        callback.value(result.resultCode == Activity.RESULT_OK)
    }
    return remember(launcher, context) {
        val manager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        object : DeviceUnlock {
            override val available: Boolean get() = manager.isDeviceSecure
            override fun unlock() {
                @Suppress("DEPRECATION")
                manager.createConfirmDeviceCredentialIntent("Unlock PrayerKey Journal", reason)?.let(launcher::launch)
                    ?: callback.value(true)
            }
        }
    }
}

actual fun pinHomeWidget(context: PlatformContext): Boolean {
    if (android.os.Build.VERSION.SDK_INT < 26) return false
    val manager = context.getSystemService(android.appwidget.AppWidgetManager::class.java) ?: return false
    if (!manager.isRequestPinAppWidgetSupported) return false
    // named rather than imported: the widget lives in the app module
    val widget = android.content.ComponentName(context.packageName, "com.prayerkey.manna.widget.VerseWidget")
    return runCatching { manager.requestPinAppWidget(widget, null, null) }.getOrDefault(false)
}
