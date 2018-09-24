package com.raywenderlich.placebook.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.raywenderlich.placebook.model.Bookmark

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM Bookmark ORDER BY name")
    fun loadAll(): LiveData<List<Bookmark>>

    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    fun loadBookmark(bookmarkId: Long): Bookmark

    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    fun loadLiveBookmark(bookmarkId: Long): LiveData<Bookmark>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBookmark(bookmark: Bookmark): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBookmark(bookmark: Bookmark)

    @Delete
    fun deleteBookmark(bookmark: Bookmark)
}