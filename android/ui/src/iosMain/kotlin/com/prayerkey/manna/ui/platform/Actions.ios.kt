package com.prayerkey.manna.ui.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayerkey.manna.platform.PlatformContext
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.delay
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.stringWithContentsOfURL
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/** The controller on top, which is the one that can present another. */
internal fun topController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}

internal fun onMain(block: () -> Unit) = dispatch_async(dispatch_get_main_queue(), block)

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData =
    if (isEmpty()) NSData() else usePinned { NSData.dataWithBytes(it.addressOf(0), size.toULong()) }

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val out = ByteArray(length.toInt())
    if (out.isNotEmpty()) out.usePinned { platform.posix.memcpy(it.addressOf(0), bytes, length) }
    return out
}

private fun present(items: List<Any>) {
    val top = topController() ?: return
    val sheet = UIActivityViewController(activityItems = items, applicationActivities = null)
    // an iPad shows the sheet as a popover, which needs somewhere to point
    sheet.popoverPresentationController?.sourceView = top.view
    top.presentViewController(sheet, animated = true, completion = null)
}

actual fun shareText(context: PlatformContext, text: String, title: String) = present(listOf(text))

actual fun shareImage(context: PlatformContext, png: ByteArray, caption: String, title: String) {
    val image = UIImage(data = png.toNSData())
    present(if (caption.isBlank()) listOf(image) else listOf(image, caption))
}

actual fun saveImageToPhotos(context: PlatformContext, png: ByteArray, name: String): Boolean {
    UIImageWriteToSavedPhotosAlbum(UIImage(data = png.toNSData()), null, null, null)
    return true
}

/* ── toast ─────────────────────────────────────────────────────────── */

private object IosToast {
    var message by mutableStateOf<String?>(null)
    var long by mutableStateOf(false)
    var serial by mutableStateOf(0)
}

actual fun showToast(context: PlatformContext, message: String, long: Boolean) {
    onMain {
        IosToast.message = message
        IosToast.long = long
        IosToast.serial++
    }
}

@Composable
actual fun PlatformOverlay() {
    DictationPanel()
    PickerHost()
    Toast()
}

@Composable
private fun Toast() {
    val message = IosToast.message ?: return
    LaunchedEffect(IosToast.serial) {
        delay(if (IosToast.long) 3_500 else 2_000)
        IosToast.message = null
    }
    Box(Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 96.dp), contentAlignment = Alignment.BottomCenter) {
        Text(
            message,
            color = Color.White, fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 32.dp).clip(RoundedCornerShape(22.dp))
                .background(Color(0xE6202024)).padding(horizontal = 18.dp, vertical = 11.dp),
        )
    }
}

/* ── permissions ───────────────────────────────────────────────────── */

@Composable
actual fun rememberPermissionRequest(permission: Permission, onResult: (Boolean) -> Unit): PermissionRequest {
    val callback = rememberUpdatedState(onResult)
    return remember(permission) {
        object : PermissionRequest {
            private var known = false

            override fun granted(): Boolean = when (permission) {
                Permission.Microphone -> AVAudioSession.sharedInstance().recordPermission == AVAudioSessionRecordPermissionGranted
                Permission.Notifications -> known
            }

            override fun request() {
                when (permission) {
                    Permission.Microphone -> AVAudioSession.sharedInstance().requestRecordPermission { ok ->
                        onMain { callback.value(ok) }
                    }
                    // asks once; after that iOS answers at once with the standing choice
                    Permission.Notifications -> UNUserNotificationCenter.currentNotificationCenter()
                        .requestAuthorizationWithOptions(
                            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
                        ) { ok, _ -> onMain { known = ok; callback.value(ok) } }
                }
            }
        }
    }
}

/* ── pickers ───────────────────────────────────────────────────────── */

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberTextFilePicker(onPicked: (String?) -> Unit): Launcher {
    val callback = rememberUpdatedState(onPicked)
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                callback.value(url?.let { NSString.stringWithContentsOfURL(it, NSUTF8StringEncoding, null) })
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                callback.value(null)
            }
        }
    }
    return remember {
        Launcher {
            val picker = UIDocumentPickerViewController(
                forOpeningContentTypes = listOf(UTTypeJSON, UTTypePlainText, UTTypeData),
                asCopy = true,
            )
            picker.delegate = delegate
            topController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private fun mediaFolder(): String {
    val docs = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String
    val folder = "$docs/journal_media"
    NSFileManager.defaultManager.createDirectoryAtPath(folder, true, null, null)
    return folder
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberMediaPicker(onPicked: (List<String>) -> Unit): MediaPicker {
    val callback = rememberUpdatedState(onPicked)
    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, completion = null)
                val results = didFinishPicking.filterIsInstance<PHPickerResult>()
                if (results.isEmpty()) return
                val kept = mutableListOf<String>()
                var waiting = results.size
                /* The picker hands out a file that is deleted when the
                   callback returns, so each is copied into the app's own
                   folder - the journal must still have it next year. */
                results.forEach { result ->
                    result.itemProvider.loadFileRepresentationForTypeIdentifier("public.image") { url, _ ->
                        val source = url as NSURL?
                        if (source != null) {
                            val ext = source.pathExtension ?: "jpg"
                            val target = "${mediaFolder()}/${NSUUID().UUIDString}.$ext"
                            if (NSFileManager.defaultManager.copyItemAtPath(source.path!!, target, null)) {
                                kept += NSURL.fileURLWithPath(target).absoluteString!!
                            }
                        }
                        onMain {
                            waiting--
                            if (waiting == 0) callback.value(kept.toList())
                        }
                    }
                }
            }
        }
    }
    // any file comes through the Files app, copied in as the photos are
    val files = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                val kept = didPickDocumentsAtURLs.filterIsInstance<NSURL>().mapNotNull { source ->
                    val target = "${mediaFolder()}/${NSUUID().UUIDString}.${source.pathExtension ?: "dat"}"
                    if (NSFileManager.defaultManager.copyItemAtPath(source.path!!, target, null)) NSURL.fileURLWithPath(target).absoluteString else null
                }
                callback.value(kept)
            }
        }
    }
    return remember {
        MediaPicker { kind ->
            if (kind == MediaKind.AnyFile) {
                val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeItem), asCopy = true)
                picker.allowsMultipleSelection = true
                picker.delegate = files
                topController()?.presentViewController(picker, animated = true, completion = null)
                return@MediaPicker
            }
            val config = PHPickerConfiguration().apply {
                selectionLimit = 30
                filter = PHPickerFilter.imagesFilter
            }
            val picker = PHPickerViewController(configuration = config)
            picker.delegate = delegate
            topController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private fun pathOf(address: String): String? = NSURL.URLWithString(address)?.path ?: address.takeIf { it.startsWith("/") }

@OptIn(ExperimentalForeignApi::class)
actual suspend fun loadThumbnail(context: PlatformContext, address: String, edge: Int): ImageBitmap? = runCatching {
    val path = pathOf(address) ?: return null
    val image = UIImage.imageWithContentsOfFile(path) ?: return null
    // drawn down to the size it is shown at, so a 12-megapixel photo is not decoded whole
    val (w, h) = image.size.useContents { width to height }
    val scale = minOf(1.0, edge / maxOf(w, h))
    val size = CGSizeMake(w * scale, h * scale)
    val small = UIGraphicsImageRenderer(size).imageWithActions { _ ->
        image.drawInRect(CGRectMake(0.0, 0.0, w * scale, h * scale))
    }
    val bytes = UIImageJPEGRepresentation(small, 0.88)?.toByteArray() ?: return null
    org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
}.getOrNull()

actual fun mimeTypeOf(context: PlatformContext, address: String): String =
    when (address.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "heic", "heif" -> "image/heic"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "mp4", "mov", "m4v" -> "video/mp4"
        "m4a", "aac", "mp3", "wav" -> "audio/mp4"
        "pdf" -> "application/pdf"
        else -> ""
    }

private val previewer = object : NSObject(), UIDocumentInteractionControllerDelegateProtocol {
    override fun documentInteractionControllerViewControllerForPreview(controller: UIDocumentInteractionController): UIViewController =
        topController()!!
}
private var openPreview: UIDocumentInteractionController? = null

actual fun openAttachment(context: PlatformContext, address: String) {
    val url = NSURL.URLWithString(address) ?: return
    openPreview = UIDocumentInteractionController.interactionControllerWithURL(url).also {
        it.delegate = previewer
        it.presentPreviewAnimated(true)
    }
}

/* ── device lock ───────────────────────────────────────────────────── */

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberDeviceUnlock(reason: String, onResult: (Boolean) -> Unit): DeviceUnlock {
    val callback = rememberUpdatedState(onResult)
    return remember(reason) {
        object : DeviceUnlock {
            override val available: Boolean
                get() = LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error = null)

            override fun unlock() {
                // Face ID or Touch ID, falling back to the passcode
                LAContext().evaluatePolicy(LAPolicyDeviceOwnerAuthentication, localizedReason = reason) { ok, _ ->
                    onMain { callback.value(ok) }
                }
            }
        }
    }
}
