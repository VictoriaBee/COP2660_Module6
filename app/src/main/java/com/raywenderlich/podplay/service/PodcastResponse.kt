package com.raywenderlich.podplay.service

// Defines data class that directly mirrors the layout
// and hierarchy of the JSON data returned by the iTunes search API.
data class PodcastResponse(
    val resultCount: Int,
    val results: List<ItunesPodcast>) {

    data class ItunesPodcast(
        val collectionCensoredName: String,
        val feedUrl: String,
        val artworkUrl30: String,
        val releaseDate: String)
}