package com.raywenderlich.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

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

    @Throws(IOException::class)
    fun createUniqueImageFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val fileName = "Placebook_$timestamp"
        val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", filesDir)
    }

    fun decodeFileToSize(filePath: String,
                         width: Int, height: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, width, height)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(filePath, options)
    }

    fun decodeUriStreamToSize(uri: Uri,
                              width: Int, height: Int, context: Context): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val options: BitmapFactory.Options
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    options.inSampleSize = calculateInSampleSize(
                            options.outWidth, options.outHeight,
                            width, height)
                    options.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeStream(
                            inputStream, null, options)
                    inputStream.close()
                    return bitmap
                } }
            return null
        } catch (e: Exception) {
            return null
        } finally {
            inputStream?.close()
        }
    }

    private fun calculateInSampleSize(
            width: Int, height: Int,
            reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                    halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            } }
        return inSampleSize
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