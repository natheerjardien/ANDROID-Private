package com.example.tuniq.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persistent storage of authentication tokens using SharedPreferences (Android Developers, n.d.).
 */
class TokenManager(context: Context) {
    // Initializes private SharedPreferences file specific to this app
    private val prefs: SharedPreferences = context.getSharedPreferences("TuniqAuthPrefs", Context.MODE_PRIVATE)

    // Saves the Spotify Access Token and refreshes it
    fun saveTokens(accessToken: String, refreshToken: String?) {
        val editor = prefs.edit()
        editor.putString("SPOTIFY_ACCESS_TOKEN", accessToken)

        // Spotify doesnt always send a new refresh token on refresh, so we only overwrite it if a new one is provided
        if (refreshToken != null)
        {
            editor.putString("SPOTIFY_REFRESH_TOKEN", refreshToken)
        }
        editor.apply()
    }

    // Retrieves the Spotify Access Token, returning null if it doesn't exist
    fun getSpotifyToken(): String? {
        return prefs.getString("SPOTIFY_ACCESS_TOKEN", null)
    }

    // Retrieves the Spotify Refresh Token, returning null if it doesn't exist
    fun getRefreshToken(): String? {
        return prefs.getString("SPOTIFY_REFRESH_TOKEN", null)
    }

    // Clears all saved tokens (used during logout)
    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}

/*
Reference List:

AndroidDevelopers, (n.d.). Save key-value data. [online]. Available at: <https://developer.android.com/training/data-storage/shared-preferences> [Accessed 17 September 2026].

*/