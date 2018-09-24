package com.raywenderlich.placebook.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.raywenderlich.placebook.model.Bookmark

@Database(entities = [Bookmark::class], version = 2)
abstract class PlaceBookDatabase: RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        private var instance: PlaceBookDatabase? = null

        fun getInstance(context: Context): PlaceBookDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                        context.applicationContext,
                        PlaceBookDatabase::class.java,
                        "PlaceBook")
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return instance as PlaceBookDatabase
        }
    }
}