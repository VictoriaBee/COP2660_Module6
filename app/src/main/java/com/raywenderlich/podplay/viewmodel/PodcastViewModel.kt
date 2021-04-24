package com.raywenderlich.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.repository.PodcastRepo
import java.util.*

// Defines the PodcastViewModel for the detail Fragment.
class PodcastViewModel(application: Application) :
        AndroidViewModel (application) {

    var podcastRepo: PodcastRepo? = null
    var activePodcastViewData: PodcastViewData? = null

    data class PodcastViewData(
        var subscribed: Boolean = false,
        var feedTitle: String? = "",
        var feedUrl: String? = "",
        var feedDesc: String? = "",
        var imageUrl: String? = "",
        var episodes: List<EpisodeViewData>)

    data class EpisodeViewData(
        var guid: String? = "",
        var title: String? = "",
        var description: String? = "",
        var mediaUrl: String? = "",
        var releaseDate: Date? = null,
        var duration: String? = "")

    // Iterates over a list of Episode models.
    private fun episodeToEpisodesView(episodes: List<Episode>):
            List<EpisodeViewData> {
        // Converts Episode models to EpisodeViewData objects,
        // and collects everything into a list.
        return episodes.map {
            EpisodeViewData(it.guid, it.title, it.description,
                it.mediaUrl, it.releaseDate, it.duration)
        }
    }

    // Converts a Podcast model to a PodcastViewData obj.
    private fun podcastToPodcastView(podcast: Podcast):
            PodcastViewData {
        return PodcastViewData(
            false,
            podcast.feedTitle,
            podcast.feedUrl,
            podcast.feedDesc,
            podcast.imageUrl,
            episodeToEpisodesView(podcast.episodes))
    }

    // 1 - Takes a PodcastSummaryViewData obj and a callback method.
    fun getPodcast(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData,
        callback: (PodcastViewData?) -> Unit) {
        // 2 - Local variables are assigned to podcastRepo
        // and podcastSummaryViewData.feedUrl.
        // If neither is null, method returns early.
        val repo = podcastRepo ?: return
        val feedUrl = podcastSummaryViewData.feedUrl ?: return
        // 3 - Calls getPodcast() from the podcast repo with the feed URL.
        repo.getPodcast(feedUrl) {
            // 4 - Checks podcast detail obj to make sure not null.
            it?.let {
                // 5 - Sets the podcast title to podcast summary name.
                it.feedTitle = podcastSummaryViewData.name ?: ""
                // 6 - Sets podcast detail image to match podcast
                // summary image URL if not null.
                it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
                // 7 - Converts Podcast obj to PodcastViewData obj and
                // assigns it to activePodcastViewData.
                activePodcastViewData = podcastToPodcastView(it)
                // 8 - Calls the callback method and passes podcast view data.
                callback(activePodcastViewData)
            }
        }
    }
}