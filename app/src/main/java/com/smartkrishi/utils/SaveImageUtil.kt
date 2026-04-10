package com.smartkrishi.utils
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore

object SaveImageUtil {

    fun saveImage(bitmap: Bitmap, context: Context): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "leaf_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }

        val uri =
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw RuntimeException("Failed to create new MediaStore record.")

        val outputStream = context.contentResolver.openOutputStream(uri)
            ?: throw RuntimeException("Failed to open output stream.")

        outputStream.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        return uri
    }
}
