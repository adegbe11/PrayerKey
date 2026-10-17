package com.prayerkey.manna.ui.journal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A picture of a photo or video attached to a journal page, at a size a phone
 * screen can use, read off the main thread. Null when the file is gone, its
 * permission was lost, or it is not a picture at all.
 */
internal suspend fun loadAttachmentPicture(context: Context, uri: Uri, edge: Int = 1080): Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            // a still photo is decoded itself, which is sharper than the system
            // thumbnail; a video has no frame to decode, so it takes the thumbnail
            val video = attachmentType(context, uri).startsWith("video/")
            if (video && Build.VERSION.SDK_INT >= 29) {
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
        }.getOrNull()
    }

internal fun attachmentType(context: Context, uri: Uri): String =
    runCatching { context.contentResolver.getType(uri) }.getOrNull().orEmpty()
