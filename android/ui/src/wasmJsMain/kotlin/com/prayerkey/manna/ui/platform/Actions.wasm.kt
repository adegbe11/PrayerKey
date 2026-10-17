package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.prayerkey.manna.platform.PlatformContext

// The web build is a compile check only; none of these do anything there.
actual fun shareText(context: PlatformContext, text: String, title: String) = Unit
actual fun shareImage(context: PlatformContext, png: ByteArray, caption: String, title: String) = Unit
actual fun saveImageToPhotos(context: PlatformContext, png: ByteArray, name: String): Boolean = false
actual fun showToast(context: PlatformContext, message: String, long: Boolean) = Unit

@Composable
actual fun PlatformOverlay() = Unit

@Composable
actual fun rememberPermissionRequest(permission: Permission, onResult: (Boolean) -> Unit): PermissionRequest =
    object : PermissionRequest {
        override fun granted() = false
        override fun request() = onResult(false)
    }

@Composable
actual fun rememberTextFilePicker(onPicked: (String?) -> Unit): Launcher = Launcher { onPicked(null) }

@Composable
actual fun rememberMediaPicker(onPicked: (List<String>) -> Unit): MediaPicker = MediaPicker { onPicked(emptyList()) }

actual suspend fun loadThumbnail(context: PlatformContext, address: String, edge: Int): ImageBitmap? = null
actual fun mimeTypeOf(context: PlatformContext, address: String): String = ""
actual fun openAttachment(context: PlatformContext, address: String) = Unit

@Composable
actual fun rememberDeviceUnlock(reason: String, onResult: (Boolean) -> Unit): DeviceUnlock =
    object : DeviceUnlock {
        override val available = false
        override fun unlock() = onResult(true)
    }
