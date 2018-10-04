package com.raywenderlich.podplay.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.telephony.euicc.DownloadableSubscription
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.util.DateUtils
import com.raywenderlich.podplay.viewmodel.SearchViewModel.PodcastSummaryView
import java.util.*

class PodcastViewModel(application: Application): AndroidViewModel(application) {
    var podcastRepo: PodcastRepo? = null
    var podcastView: PodcastView? = null
    var activePodcast: Podcast? = null
    var livePodcastData: LiveData<List<PodcastSummaryView>>? = null
    var activeEpisodeView: EpisodeView? = null

    fun getPodcast(podcastSummaryView: PodcastSummaryView, callback: (PodcastView?) -> Unit) {
        val repo = podcastRepo ?: return
        val feedUrl = podcastSummaryView.feedUrl ?: return

        repo.getPodcast(feedUrl) { podcast ->
            podcast?.let {
                it.feedTitle = podcastSummaryView.name ?: ""
                it.imageUrl = podcastSummaryView.imageUrl ?: ""
                podcastView = podcastToPodcastView(it)
                activePodcast = it
                callback(podcastView)
            }
        }
    }

    fun getPodcasts(): LiveData<List<PodcastSummaryView>>? {
        val repo = podcastRepo ?: return null
        if (livePodcastData == null) {
            val liveData = repo.getAll()
            livePodcastData = Transformations.map(liveData) { podcastList ->
                podcastList.map { podcast ->
                    podcastToSummaryView(podcast)
                }
            } }
        return livePodcastData
    }

    fun saveActivePodcast() {
        activePodcast?.let {
            it.episodes.drop(1)
            podcastRepo?.save(it)
        }
    }

    fun deleteActivePodcast() {
        activePodcast?.let {
            podcastRepo?.delete(it)
        }
    }

    fun setActivePodcast(feedUrl: String, callback: (PodcastSummaryView?) -> Unit) {
        val repo = podcastRepo ?: return

        repo.getPodcast(feedUrl) { podcast ->
            if (podcast == null) {
                callback(null)
            } else {
                podcastView = podcastToPodcastView(podcast)
                activePodcast = podcast
                callback(podcastToSummaryView(podcast))
            }
        }
    }

    private fun podcastToSummaryView(podcast: Podcast): PodcastSummaryView {
        return PodcastSummaryView(
                podcast.feedTitle,
                DateUtils.dateToShortDate(podcast.lastUpdated),
                podcast.imageUrl,
                podcast.feedUrl)
    }

    private fun podcastToPodcastView(podcast: Podcast): PodcastView {
        return PodcastView(
                podcast.id != null,
                podcast.feedTitle,
                podcast.feedUrl,
                podcast.feedDesc,
                podcast.imageUrl,
                episodesToEpisodeViews(podcast.episodes)
        )
    }

    private fun episodesToEpisodeViews(episodes: List<Episode>): List<EpisodeView> {
        return episodes.map {
            val isVideo = it.mimeType.startsWith("video")
            EpisodeView(it.guid,
                it.title,
                it.description,
                it.mediaUrl,
                it.releaseDate,
                it.duration,
                    isVideo)
        }
    }

    data class PodcastView(
            var subscribed: Boolean = false,
            var feedTitle: String? = "",
            var feedUrl: String? = "",
            var feedDesc: String? = "",
            var imageUrl: String? = "",
            var episodes: List<EpisodeView>
    )

    data class EpisodeView(
            var guid: String? = "",
            var title: String? = "",
            var description: String? = "",
            var mediaUrl: String? = "",
            var releaseDate: Date? = null,
            var duration: String? = "",
            var isVideo: Boolean = false
    )
}