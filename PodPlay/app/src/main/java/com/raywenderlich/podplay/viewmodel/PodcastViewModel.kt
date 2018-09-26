package com.raywenderlich.podplay.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.telephony.euicc.DownloadableSubscription
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.repository.PodcastRepo
import java.util.*

class PodcastViewModel(application: Application): AndroidViewModel(application) {
    var podcastRepo: PodcastRepo? = null
    var podcastView: PodcastView? = null

    fun getPodcast(podcastSummaryView: SearchViewModel.PodcastSummaryView, callback: (PodcastView?) -> Unit) {
        val repo = podcastRepo ?: return
        val feedUrl = podcastSummaryView.feedUrl ?: return

        repo.getPodcast(feedUrl) { podcast ->
            podcast?.let {
                it.feedTitle = podcastSummaryView.name ?: ""
                it.imageUrl = podcastSummaryView.imageUrl ?: ""
                podcastView = podcastToPodcastView(it)
                callback(podcastView)
            }
        }
    }

    private fun podcastToPodcastView(podcast: Podcast): PodcastView {
        return PodcastView(
                false,
                podcast.feedTitle,
                podcast.feedUrl,
                podcast.feedDesc,
                podcast.imageUrl,
                episodesToEpisodeViews(podcast.episodes)
        )
    }

    private fun episodesToEpisodeViews(episodes: List<Episode>): List<EpisodeView> {
        return episodes.map { EpisodeView(it.guid,
                it.title,
                it.description,
                it.mediaUrl,
                it.releaseDate,
                it.duration)
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
            var duration: String? = ""
    )
}