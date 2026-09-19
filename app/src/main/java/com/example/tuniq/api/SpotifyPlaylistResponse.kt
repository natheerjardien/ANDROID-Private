package com.example.tuniq.api

import com.google.gson.annotations.SerializedName

/**
 * Maps the paginated JSON response from Spotify's 'v1/me/playlists' endpoint.
 */
data class SpotifyPlaylistResponse(
    @SerializedName("items") val items: List<Playlist>
)

data class Playlist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<SpotifyImage>?
)

data class SpotifyImage(
    @SerializedName("url") val url: String
)