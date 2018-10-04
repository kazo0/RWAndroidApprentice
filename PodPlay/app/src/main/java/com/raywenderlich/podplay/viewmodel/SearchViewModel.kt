package com.raywenderlich.podplay.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.service.PodcastResponse
import com.raywenderlich.podplay.util.DateUtils

class SearchViewModel(application: Application): AndroidViewModel(application) {
    var itunesRepo: ItunesRepo? = null

    fun searchPodcasts(term: String, callback: (List<PodcastSummaryView>) -> Unit) {
        itunesRepo?.searchByTerm(term) { results ->
            if (results == null) {
                callback(emptyList())
            } else {
                val searchViews = results.map { itunesPodcastToPodcastSummaryView(it) }
                searchViews.let { callback(it) }
            }
        }
    }

    private fun itunesPodcastToPodcastSummaryView(
            itunesPodcast: PodcastResponse.ItunesPodcast):
            PodcastSummaryView {

        return PodcastSummaryView(
                itunesPodcast.collectionCensoredName,
                DateUtils.jsonDateToShortDate(itunesPodcast.releaseDate),
                itunesPodcast.artworkUrl100,
                itunesPodcast.feedUrl
        )
    }

    data class PodcastSummaryView(
            var name: String? = "",
            var lastUpdated: String? = "",
            var imageUrl: String? = "",
            var feedUrl: String? = ""
    )
}