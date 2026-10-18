package com.prayerkey.manna.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.prayerkey.manna.platform.PlatformContext

/*
 * The things a screen asks the phone to do. Each is the phone's own: Android's
 * share sheet and pickers, the iPhone's share sheet, photo picker, Face ID.
 */

/** The phone's share sheet, with [text]. [title] heads Android's chooser. */
expect fun shareText(context: PlatformContext, text: String, title: String = "Share")

/** Shares a PNG (a share card) with an optional caption. */
expect fun shareImage(context: PlatformContext, png: ByteArray, caption: String, title: String = "Share")

/** Saves a PNG to the phone's photos. Returns false when it could not. */
expect fun saveImageToPhotos(context: PlatformContext, png: ByteArray, name: String): Boolean

/**
 * Asks the phone to place PrayerKey's verse of the day on the home screen.
 * Returns false where the platform cannot, so the caller can explain how.
 */
expect fun pinHomeWidget(context: PlatformContext): Boolean

/** A short message over the app: Android's toast; a small pill on iPhone. */
expect fun showToast(context: PlatformContext, message: String, long: Boolean = false)

/** Anything the platform draws over every screen (the iPhone's toast). Placed once, at the root. */
@Composable
expect fun PlatformOverlay()

enum class Permission { Notifications, Microphone }

/** Asks for a permission when [request] is called; [granted] answers without asking. */
interface PermissionRequest {
    fun granted(): Boolean
    fun request()
}

@Composable
expect fun rememberPermissionRequest(permission: Permission, onResult: (Boolean) -> Unit): PermissionRequest

/** Something the user picks from outside the app. */
fun interface Launcher {
    fun launch()
}

/** Picks one file and hands back its text (a backup to restore), or null. */
@Composable
expect fun rememberTextFilePicker(onPicked: (String?) -> Unit): Launcher

/**
 * Picks photos (and on Android, any document) for a journal page. Hands back
 * addresses that stay readable later: Android content URIs with a lasting
 * grant; on iPhone, copies kept in the app's own files.
 */
@Composable
expect fun rememberMediaPicker(onPicked: (List<String>) -> Unit): MediaPicker

/** What a journal page can take: photos and videos; those plus audio and PDFs; or any file at all. */
enum class MediaKind { Photos, Attachments, AnyFile }

fun interface MediaPicker {
    fun launch(kind: MediaKind)
    fun launch() = launch(MediaKind.Attachments)
}

/** A small preview of an attachment, or null when it is not an image. */
expect suspend fun loadThumbnail(context: PlatformContext, address: String, edge: Int): ImageBitmap?

/** "image/jpeg" and the like, or "" when unknown. */
expect fun mimeTypeOf(context: PlatformContext, address: String): String

/** Opens an attachment in the phone's own viewer. */
expect fun openAttachment(context: PlatformContext, address: String)

/**
 * The device lock (PIN, pattern, Face ID) guarding the journal. [available]
 * is false when the phone has no lock set, and then the journal is not locked.
 */
interface DeviceUnlock {
    val available: Boolean
    fun unlock()
}

@Composable
expect fun rememberDeviceUnlock(reason: String, onResult: (Boolean) -> Unit): DeviceUnlock
