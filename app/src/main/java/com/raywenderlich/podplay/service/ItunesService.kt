package com.raywenderlich.podplay.service

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesService {
    // Retrofit annotation.
    @GET("/search?media=podcast")
    // 2 - @Query annotation tells Retrofit that this parameter should
    // be added as a query term in the path defined by @GET.
    fun searchPodcastByTerm(@Query("term") term: String):
            Call<PodcastResponse>
    // 3 - Companions object for the ItunesService interface.
    companion object {
        // 4 - Allows instance property to return a Singleton object.
        val instance: ItunesService by lazy {
            // 5 - Lambda method; Builder() creates a retrofit builder obj.
            val retrofit = Retrofit.Builder()
                .baseUrl("https://itunes.apple.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            // 6 - Creates the ItunesService instance.
            retrofit.create<ItunesService>(ItunesService::class.java)
        }
    }
}