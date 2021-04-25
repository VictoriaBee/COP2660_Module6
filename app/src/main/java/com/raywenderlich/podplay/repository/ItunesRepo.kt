package com.raywenderlich.podplay.repository

import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.service.PodcastResponse
import com.raywenderlich.podplay.service.PodcastResponse.ItunesPodcast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 1 - Service that makes the repository happen.
class ItunesRepo(private val itunesService: ItunesService) {
    // 2 - Defines single parameter as a List of iTunesPodcast objects.
    fun searchByTerm(term: String,
        callBack: (List<ItunesPodcast>?) -> Unit) {
        // 3 - Returns a Retrofit Call obj.
        val podcastCall = itunesService.searchPodcastByTerm(term)
        // 4 - Runs in background to retrieve response from web service.
        podcastCall.enqueue(object: Callback<PodcastResponse> {
            // 5 - Is called if anything goes wrong with the call,
            // like network failure or invalid URL.
            override fun onFailure(call: Call<PodcastResponse>,
                                   t: Throwable?) {
                // 6 - If error, callBack() is null.
                callBack(null)
            }

            // 7 - If call succeeds,
            override fun onResponse(
                call: Call<PodcastResponse>?,
                response: Response<PodcastResponse>) {
                // 8 - retrieves a populated PodcastResponse model.
                val body = response.body()
                // 9 - Results obj from PodcastResponse model.
                callBack(body?.results)
            }
        })
    }
}