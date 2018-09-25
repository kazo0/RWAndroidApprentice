package com.raywenderlich.placebook.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import com.google.android.gms.location.places.Place
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

class BookmarkRepo(private val context: Context) {
    val categories: List<String>
        get() = categoryResourceMap.keys.toList()

    private var db: PlaceBookDatabase = PlaceBookDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()
    private var categoryMap = buildCategoryMap()
    private var categoryResourceMap = buildCategoryResources()

    val allBookmarks: LiveData<List<Bookmark>>
        get() = bookmarkDao.loadAll()

    fun addBookmark(bookmark: Bookmark): Long? {
        val newId = bookmarkDao.insertBookmark(bookmark)

        bookmark.id = newId
        return newId
    }

    fun getLiveBookmark(bookmarkId: Long) : LiveData<Bookmark> {
        val bookmark = bookmarkDao.loadLiveBookmark(bookmarkId)
        return bookmark
    }

    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(bookmarkId: Long) : Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }

    fun createBookmark() = Bookmark()

    fun deleteBookmark(bookmark: Bookmark) {
        bookmark.deleteImage(context)
        bookmarkDao.deleteBookmark(bookmark)
    }

    fun placeTypeToCategory(placeType: Int): String {
        return categoryMap[placeType] ?: "Other"
    }

    fun getCategoryResourceId(placeCategory: String): Int? {
        return categoryResourceMap[placeCategory]
    }

    private fun buildCategoryMap() : HashMap<Int, String> {
        return hashMapOf(
                Place.TYPE_BAKERY to "Restaurant",
                Place.TYPE_BAR to "Restaurant",
                Place.TYPE_CAFE to "Restaurant",
                Place.TYPE_FOOD to "Restaurant",
                Place.TYPE_RESTAURANT to "Restaurant",
                Place.TYPE_MEAL_DELIVERY to "Restaurant",
                Place.TYPE_MEAL_TAKEAWAY to "Restaurant",
                Place.TYPE_GAS_STATION to "Gas",
                Place.TYPE_CLOTHING_STORE to "Shopping",
                Place.TYPE_DEPARTMENT_STORE to "Shopping",
                Place.TYPE_FURNITURE_STORE to "Shopping",
                Place.TYPE_GROCERY_OR_SUPERMARKET to "Shopping",
                Place.TYPE_HARDWARE_STORE to "Shopping",
                Place.TYPE_HOME_GOODS_STORE to "Shopping",
                Place.TYPE_JEWELRY_STORE to "Shopping",
                Place.TYPE_SHOE_STORE to "Shopping",
                Place.TYPE_SHOPPING_MALL to "Shopping",
                Place.TYPE_STORE to "Shopping",
                Place.TYPE_LODGING to "Lodging",
                Place.TYPE_ROOM to "Lodging"
        )
    }

    private fun buildCategoryResources() : HashMap<String, Int> {
        return hashMapOf(
                "Gas" to R.drawable.ic_gas,
                "Lodging" to R.drawable.ic_lodging,
                "Other" to R.drawable.ic_other,
                "Restaurant" to R.drawable.ic_restaurant,
                "Shopping" to R.drawable.ic_shopping
        )
    }
}