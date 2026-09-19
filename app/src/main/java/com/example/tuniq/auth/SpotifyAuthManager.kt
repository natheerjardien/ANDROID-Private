package com.example.tuniq.auth

import android.net.Uri
import android.util.Base64

object SpotifyAuthManager {
    // Replace this with the Client ID from your Spotify Developer Dashboard
    const val CLIENT_ID = "78345122ead74e16ac0e6ec2310977be"

    // Must exactly match the URI registered in the dashboard and your AndroidManifest
    const val REDIRECT_URI = "tuniq://callback"

    const val CLIENT_SECRET = "113e02e5a1c84cc8b585c46ca16340f2"

    private const val SCOPES = "user-read-private user-read-email playlist-read-private playlist-modify-public playlist-modify-private"

    fun getAuthorizationUrl(): String {
        return Uri.Builder()
            .scheme("https")
            .authority("accounts.spotify.com")
            .appendPath("authorize")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("show_dialog", "true")
            .build()
            .toString()
    }

    // Encodes your credentials securely for the token exchange
    fun getAuthHeader(): String {
        val auth = "$CLIENT_ID:$CLIENT_SECRET"
        val base64Auth = Base64.encodeToString(auth.toByteArray(), Base64.NO_WRAP)
        return "Basic $base64Auth"
    }
}

/*
Reference List:

AndroidDevelopers, 2026. Equalizer. [online]. Available at: <https://developer.android.com/reference/android/media/audiofx/Equalizer> [Accessed 17 September 2026].

*/
