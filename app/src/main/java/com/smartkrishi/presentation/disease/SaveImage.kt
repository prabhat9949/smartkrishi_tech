package com.smartkrishi.presentation.disease

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.OutputStream

fun saveImage(bitmap: Bitmap, context: Context): Uri {

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "leaf_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.WIDTH, bitmap.width)
        put(MediaStore.Images.Media.HEIGHT, bitmap.height)
    }

    val uri: Uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    ) ?: throw IllegalStateException("Failed to create new MediaStore record")

    val outputStream: OutputStream = context.contentResolver.openOutputStream(uri)
        ?: throw IllegalStateException("Failed to open output stream for URI: $uri")

    outputStream.use { stream ->
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
            throw IllegalStateException("Bitmap failed to compress")
        }
    }

    return uri
}
