package com.raywenderlich.podplay.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast

@Dao
interface PodcastDao {
    @Query("SELECT * FROM Podcast ORDER BY FeedTitle")
    fun loadPodcasts(): LiveData<List<Podcast>>

    @Query("SELECT * FROM Podcast ORDER BY FeedTitle")
    fun loadPodcastsStatic(): List<Podcast>

    @Query("SELECT * FROM Podcast WHERE FeedUrl = :feedUrl")
    fun loadPodcast(feedUrl: String): Podcast?

    @Query("SELECT * FROM Episode WHERE podcastId = :podcastId ORDER BY releaseDate DESC")
    fun loadEpisodes(podcastId: Long): List<Episode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPodcast(podcast: Podcast): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisode(episode: Episode): Long

    @Delete
    fun deletePodcat(podcast: Podcast)
}