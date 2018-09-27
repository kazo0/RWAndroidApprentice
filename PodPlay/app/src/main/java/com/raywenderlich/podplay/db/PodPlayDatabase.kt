package com.raywenderlich.podplay.db

import android.arch.persistence.room.*
import android.content.Context
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }
    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return (date?.time)
    }
}

@Database(entities = [Podcast::class, Episode::class], version = 1)
@TypeConverters(Converters::class)
abstract class PodPlayDatabase: RoomDatabase() {
    abstract fun podcastDao(): PodcastDao

    companion object {
        private var instance: PodPlayDatabase? = null

        fun getInstance(context: Context): PodPlayDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, PodPlayDatabase::class.java, "PodPlayer")
                        .build()
            }

            return instance as PodPlayDatabase
        }
    }
}