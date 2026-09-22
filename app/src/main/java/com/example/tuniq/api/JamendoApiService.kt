package com.example.tuniq.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for executing HTTP requests to the Jamendo API (Square, n.d.).
 */
interface JamendoApiService {

    // Fetches a list of public songs. The audioformat is set to mp32 (high quality VBR) by default
    @GET("v3.0/tracks/")
    fun getTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("tags") tags: String? = null,
        @Query("audioformat") audioFormat: String = "mp32"
    ): Call<JamendoResponse>

    // Fetches tracks specifically matching a search query
    @GET("v3.0/tracks/")
    fun searchTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("search") searchQuery: String,
        @Query("audioformat") audioFormat: String = "mp32"
    ): Call<JamendoResponse>
}

/*
 * Reference List:
 * Square, n.d. Retrofit: A type-safe HTTP client for Android and Java. [Online] Available at: <https://square.github.io/retrofit/> [Accessed 19 September 2026].
 */