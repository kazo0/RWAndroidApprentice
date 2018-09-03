package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo

class MapsViewModel(application: Application): AndroidViewModel(application) {
    companion object {
        const val TAG = "MapsViewModel"
    }

    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarks: LiveData<List<BookmarkView>>? = null

    fun addBookmarkFromPlace(place: Place, image: Bitmap) {
        val bookmark = bookmarkRepo.createBookmark().apply {
            placeId = place.id
            name = place.name.toString()
            longitude = place.latLng.longitude
            latitude = place.latLng.latitude
            phone = place.phoneNumber.toString()
            address = place.address.toString()
        }

        val newId = bookmarkRepo.addBookmark(bookmark)

        Log.i(TAG, "New bookmark $newId added to the database.")

    }

    fun getBookmarkViews(): LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkViews()
        }
        return bookmarks
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark): BookmarkView {
        return BookmarkView(bookmark.id, LatLng(bookmark.latitude, bookmark.longitude))
    }

    private fun mapBookmarksToBookmarkViews() {
        val allBookmarks = bookmarkRepo.allBookmarks

        bookmarks = Transformations.map(allBookmarks) { bookmarks ->
            val bookmarkViews = bookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
            bookmarkViews
        }
    }

    data class BookmarkView(val id: Long? = null, var location: LatLng = LatLng(0.0, 0.0))
}