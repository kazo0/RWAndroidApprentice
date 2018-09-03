package com.raywenderlich.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String) {
        val stream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        ImageUtils.saveBytesToFile(context, bytes, fileName)
    }

    fun loadBitmapFromFile(context: Context, fileName: String): Bitmap? {
        val filePath = File(context.filesDir, fileName).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

    private fun saveBytesToFile(context: Context, bytes: ByteArray, fileName: String) {
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(bytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}