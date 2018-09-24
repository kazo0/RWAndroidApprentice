package com.raywenderlich.placebook.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import android.graphics.Bitmap
import com.raywenderlich.placebook.util.ImageUtils

@Entity
data class Bookmark(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var placeId: String? = null,
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = "",
    var address: String = "",
    var notes: String = ""
) {
    fun setImage(image: Bitmap, context: Context) {
        id?.let {
            ImageUtils.saveBitmap(context, image, generateImageFileName(it))
        }
    }

    companion object {
        fun generateImageFileName(id: Long) : String {
            return "bookmark$id.png"
        }
    }

}