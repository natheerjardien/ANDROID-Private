package com.example.tuniq.api

import com.google.gson.annotations.SerializedName

/**
 * Data class representing the JSON response from Spotify's token endpoint.
 * SerializedName maps the exact JSON key to our Kotlin properties (Spotify, n.d.).
 */
data class SpotifyTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("scope") val scope: String
)

/*
Reference List:

Spotify, (n.d.). Authorization Code Flow. [online]. Available at: <https://developer.spotify.com/documentation/web-api/tutorials/code-flow> [Accessed 17 September 2026].

*/