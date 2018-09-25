package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import android.graphics.Bitmap
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

class BookmarkDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarkDetailsView: LiveData<BookmarkDetailsView>? = null


    fun getBookmark(bookmarkId: Long): LiveData<BookmarkDetailsView>? {
        if (bookmarkDetailsView == null) {
            mapBookmarkToBookmarkDetailsView(bookmarkId)
        }
        return bookmarkDetailsView
    }

    fun updateBookmark(bookmarkDetailsView: BookmarkDetailsView) {
        launch(CommonPool) {
            val bookmark = bookmarkDetailsViewToBookmark(bookmarkDetailsView)
            bookmark?.let {
                bookmarkRepo.updateBookmark(it)
            }
        }
    }

    fun getCategoryResourceId(category: String): Int? {
        return bookmarkRepo.getCategoryResourceId(category)
    }

    fun getCategories(): List<String> {
        return bookmarkRepo.categories
    }

    fun deleteBookmark(bookmarkDetailsView: BookmarkDetailsView) {
        launch(CommonPool) {
            val bookmark = bookmarkDetailsView.id?.let {
                bookmarkRepo.getBookmark(it)
            }

            bookmark?.let {
                bookmarkRepo.deleteBookmark(it)
            }
        }
    }

    private fun bookmarkToBookmarkDetailsView(bookmark: Bookmark) : BookmarkDetailsView {
        return BookmarkDetailsView(
                id = bookmark.id,
                name = bookmark.name,
                phone = bookmark.phone,
                address = bookmark.address,
                notes = bookmark.notes,
                category = bookmark.category,
                longitude = bookmark.longitude,
                latitiude = bookmark.latitude,
                placeId = bookmark.placeId
        )
    }

    private fun bookmarkDetailsViewToBookmark(bookmarkDetailsView: BookmarkDetailsView) : Bookmark? {
        val bookmark = bookmarkDetailsView.id?.let {
            bookmarkRepo.getBookmark(it)
        }

        if (bookmark != null) {
            bookmark.id = bookmarkDetailsView.id
            bookmark.name = bookmarkDetailsView.name
            bookmark.phone = bookmarkDetailsView.phone
            bookmark.address = bookmarkDetailsView.address
            bookmark.notes = bookmarkDetailsView.notes
            bookmark.category = bookmarkDetailsView.category
        }
        return bookmark
    }

    private fun mapBookmarkToBookmarkDetailsView(bookmarkId: Long) {
        val bookmark = bookmarkRepo.getLiveBookmark(bookmarkId)
        bookmarkDetailsView = Transformations.map(bookmark) { bookmark ->
            bookmark?.let {
                val bookmarkDetailsView = bookmarkToBookmarkDetailsView(bookmark)
                bookmarkDetailsView
            }
        }
    }

    data class BookmarkDetailsView(
            var id: Long? = null,
            var name: String = "",
            var phone: String = "",
            var address: String = "",
            var notes: String = "",
            var category: String = "",
            var longitude: Double = 0.0,
            var latitiude: Double = 0.0,
            var placeId: String? = null)
    {
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(context, Bookmark.generateImageFileName(it))
            }
            return null
        }

        fun setImage(context: Context, bitmap: Bitmap) {
            id?.let {
                ImageUtils.saveBitmap(context, bitmap, Bookmark.generateImageFileName(it))
            }
        }
    }
}