package com.raywenderlich.placebook.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

class BookmarkRepo(private val context: Context) {
    private var db: PlaceBookDatabase = PlaceBookDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()

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

    fun createBookmark() = Bookmark()
}