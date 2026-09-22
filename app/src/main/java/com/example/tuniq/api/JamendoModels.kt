package com.example.tuniq.api

import com.google.gson.annotations.SerializedName

/**
 * Maps the root JSON response from the Jamendo v3.0 API (Jamendo, n.d.).
 */
data class JamendoResponse(
    @SerializedName("results") val results: List<JamendoTrack>
)

/**
 * Data class representing a single song, capturing metadata, cover art and the MP3 stream URL (Google, 2026).
 */
data class JamendoTrack(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("artist_name") val artistName: String,
    @SerializedName("album_name") val albumName: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("image") val image: String,
    @SerializedName("audio") val audio: String,
    @SerializedName("audiodownload_allowed") val downloadAllowed: Boolean
)

/*
 * Reference List:
 * Google, 2026. Gson User Guide. [Online] Available at: <https://github.com/google/gson/blob/master/UserGuide.md> [Accessed 19 September 2026].
 * Jamendo, n.d. Jamendo API v3.0 Documentation. [Online] Available at: <https://developer.jamendo.com/v3.0/tracks> [Accessed 19 September 2026].
 */