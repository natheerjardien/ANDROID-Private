package com.example.tuniq.api

import com.google.gson.annotations.SerializedName

/**
 * Data model representing the JSON response from Spotify's 'v1/me' endpoint.
 * Nullable types (?) are used in case a user hasn't set a display name or email (Spotify, n.d.).
 */
data class SpotifyProfileResponse(
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("id") val id: String,
    @SerializedName("uri") val uri: String
)

/*
Reference List:

Spotify, (n.d.). Authorization Code Flow. [online]. Available at: <https://developer.spotify.com/documentation/web-api/tutorials/code-flow> [Accessed 17 September 2026].

*/