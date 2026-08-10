package com.bradmir.pescapr.utils

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, folder: String): String? {
    return try {
        val filename = "${UUID.randomUUID()}.jpg"
        val directory = File(context.filesDir, folder)
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
