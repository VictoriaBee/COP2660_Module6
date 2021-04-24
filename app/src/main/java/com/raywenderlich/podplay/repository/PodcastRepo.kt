package com.raywenderlich.podplay.repository

import com.raywenderlich.podplay.model.Episode
import com.raywenderlich.podplay.model.Podcast
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.RssFeedResponse
import com.raywenderlich.podplay.service.RssFeedService
import com.raywenderlich.podplay.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Retrieves feed from URL.
class PodcastRepo(private var feedService: FeedService) {

    fun getPodcast(feedUrl: String, callback: (Podcast?) -> Unit) {
        feedService.getFeed(feedUrl) { feedResponse ->
            var podcast: Podcast? = null
            if (feedResponse != null) {
                podcast = rssResponseToPodcast(feedUrl,
                    "", feedResponse)
            }
            GlobalScope.launch(Dispatchers.Main) {
                callback(podcast)
            }
        }
    }

    // Helper method to convert RssResponse data into Episode
    // and Podcast objects.
    private fun rssItemsToEpisodes(episodeResponses:
        List<RssFeedResponse.EpisodeResponse>): List<Episode> {
        return episodeResponses.map {
            Episode(
                    it.guid ?: "",
                    it.title ?: "",
                    it.description ?: "",
                    it.url ?: "",
                    it.type ?: "",
                    DateUtils.xmlDateToDate(it.pubDate),
                    it.duration ?: "")
        }
    }

    // Converts the full RssFeedResponse to a Podcast object.
    private fun rssResponseToPodcast(feedUrl: String,
        imageUrl: String, rssResponse: RssFeedResponse): Podcast? {
        // 1 - Assigns a list of episodes to items provided it's not null;
        // otherwise, method returns null.
        val items = rssResponse.episodes ?: return null
        // 2 - If description is empty, description property is set to the
        // response summary; otherwise, set to response description.
        val description = if (rssResponse.description == "")
            rssResponse.summary else rssResponse.description
        // 3 - Creates new Podcast object using the response data and
        // then returns it to the caller.
        return Podcast(feedUrl, rssResponse.title, description, imageUrl,
            rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
    }
}