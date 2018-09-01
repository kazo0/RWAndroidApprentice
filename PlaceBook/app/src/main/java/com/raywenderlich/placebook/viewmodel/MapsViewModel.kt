package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.location.places.Place
import com.raywenderlich.placebook.repository.BookmarkRepo

class MapsViewModel(application: Application): AndroidViewModel(application) {
    companion object {
        const val TAG = "MapsViewModel"
    }

    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())

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
}