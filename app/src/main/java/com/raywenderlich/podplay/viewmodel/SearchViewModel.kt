package com.raywenderlich.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.service.PodcastResponse
import com.raywenderlich.podplay.util.DateUtils

class SearchViewModel (application: Application) :
        AndroidViewModel(application) {
    // Property to fetch the info.
      var iTunesRepo: ItunesRepo? = null

    // Defines data class that only has data necessary for the View.
    data class PodcastSummaryViewData(
        var name: String? = "",
        var lastUpdated: String? = "",
        var imageUrl: String? = "",
        var feedUrl: String? = "")

    // Helper method to convert from raw model data to view data.
    private fun itunesPodcastToPodcastSummaryView(
        itunesPodcast: PodcastResponse.ItunesPodcast) :
        PodcastSummaryViewData {
        return PodcastSummaryViewData(
            itunesPodcast.collectionCensoredName,
            DateUtils.jsonDateToShortDate(itunesPodcast.releaseDate),
            itunesPodcast.artworkUrl30,
            itunesPodcast.feedUrl)
    }

    // 1 - Performs the search ( gets called by PodcastActivity).
    fun searchPodcasts(term: String,
         callback: (List<PodcastSummaryViewData>) -> Unit)      {
        // 2 - Used to perform the search asychronously.
        iTunesRepo?.searchByTerm(term) { results ->
            if (results == null) {
                // 3 - If results are null, passes empty list to callback method.
                callback(emptyList())
            } else {
                // 4 - If not null, it maps to PodcastSummaryViewData objects.
                val searchViews = results.map { podcast ->
                    itunesPodcastToPodcastSummaryView(podcast)
                }
                // 5 - Passes mapped results to the callback method to display them.
                callback(searchViews)
            }
        }
    }
}