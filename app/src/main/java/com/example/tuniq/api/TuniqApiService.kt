package com.example.tuniq.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Data class matching the C# ASP.NET Core Song model to ensure clean JSON serialization (Google, 2026).
 */
data class SavedSong(
    val trackId: String,
    val title: String,
    val artistName: String,
    val albumName: String,
    val coverArtUrl: String,
    val audioUrl: String
)

/**
 * Data class matching the C# ASP.NET Core Playlist model (Google, 2026).
 */
data class PlaylistModel(
    val playlistId: String? = null,
    val title: String,
    val userId: String,
    val description: String = "",
    val songs: List<SavedSong>? = emptyList()
)

data class UserModel(
    val userId: String,
    val name: String,
    val email: String
)

/**
 * Retrofit interface for executing HTTP requests to our custom Azure C# Backend (Square, n.d.).
 */
interface TuniqApiService {

    // POST request to send a liked track to the Azure SQL Database
    @Headers("Content-Type: application/json")
    @POST("api/Songs")
    fun saveSong(@Body song: SavedSong): Call<SavedSong>

    // Fetches all the liked songs from the Azure database
    @GET("api/Songs")
    fun getLikedSongs(): Call<List<SavedSong>>

    // Creates a new playlist
    @Headers("Content-Type: application/json")
    @POST("api/Playlists")
    fun createPlaylist(@Body playlist: PlaylistModel): Call<PlaylistModel>

    // Gets all the playlists for a specific user
    @GET("api/Playlists/user/{userId}")
    fun getUserPlaylists(@Path("userId") userId: String): Call<List<PlaylistModel>>

    // Adds a song to a custom playlist
    @Headers("Content-Type: application/json")
    @POST("api/Playlists/{playlistId}/songs")
    fun addSongToPlaylist(
        @Path("playlistId") playlistId: String,
        @Body song: SavedSong
    ): Call<PlaylistModel>

    // Sends the Firebase authenticated user profile to Azure
    @Headers("Content-Type: application/json")
    @POST("api/Users")
    fun createUser(@Body user: UserModel): Call<UserModel>
}

/*
 * Reference List:
 * Google, 2026. Gson User Guide. [Online] Available at: <https://github.com/google/gson/blob/master/UserGuide.md> [Accessed 19 September 2026].
 * Square, n.d. Retrofit: A type-safe HTTP client for Android and Java. [Online] Available at: <https://square.github.io/retrofit/> [Accessed 19 September 2026].
 */