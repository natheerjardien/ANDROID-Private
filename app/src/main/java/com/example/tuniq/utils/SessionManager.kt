package com.example.tuniq.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user session data using Android SharedPreferences for local storage (Android Developers, 2026).
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("TuniqSession", Context.MODE_PRIVATE)

    /**
     * Saves the active users unique ID to local storage
     */
    fun saveUserId(userId: String) {
        prefs.edit().putString("USER_ID", userId).apply()
    }

    /**
     * Fetches the active users ID, returning null if no user is logged in
     */
    fun getUserId(): String? {
        return prefs.getString("USER_ID", null)
    }

    /**
     * Clears session data when logging out
     */
    fun logout() {
        prefs.edit().remove("USER_ID").apply()
    }
}

/*
 * Reference List:

 * Android Developers, 2026. Save key-value data. [Online] Available at: <https://developer.android.com/training/data-storage/shared-preferences> [Accessed 20 September 2026].

*/