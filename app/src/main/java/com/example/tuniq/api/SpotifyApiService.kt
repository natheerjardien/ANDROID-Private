package com.example.tuniq.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface defining endpoints for the Spotify Web API (Square, n.d.).
 */
interface SpotifyApiService {

    // Executes the Authorization Code exchange via a POST request (Spotify, n.d.)
    @FormUrlEncoded
    @POST("api/token")
    fun getAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Call<SpotifyTokenResponse>

    // Swaps a valid refresh token for a new access token
    @FormUrlEncoded
    @POST("api/token")
    fun refreshToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Call<SpotifyTokenResponse>

    // Retrieves the authenticated user's profile data
    @GET("v1/me")
    fun getUserProfile(
        @Header("Authorization") bearerToken: String
    ): Call<SpotifyProfileResponse>

    // Fetches the users saved playlists on Spotify
    @GET("v1/me/playlists")
    fun getUserPlaylists(
        @Header("Authorization") bearerToken: String
    ): Call<SpotifyPlaylistResponse>
}

/*
Reference List:

Spotify, (n.d.). Authorization Code Flow. [online]. Available at: <https://developer.spotify.com/documentation/web-api/tutorials/code-flow> [Accessed 17 September 2026].

Square, (n.d.). Retrofit: A type-safe HTTP client for Android and Java. [online]. Available at: <https://square.github.io/retrofit/> [Accessed 17 September 2026].

*/