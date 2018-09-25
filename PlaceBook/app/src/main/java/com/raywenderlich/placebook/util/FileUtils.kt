package com.raywenderlich.placebook.util

import android.content.Context
import java.io.File

object FileUtils {
    fun deleteFile(context: Context, fileName: String) {
        val file = File(context.filesDir, fileName)
        file.delete()
    }
}