package com.example.tuniq.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secures authentication tokens locally using hardware-backed encryption (Android Developers, 2026).
 */
class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_tuniq_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit().putString("JWT_TOKEN", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("JWT_TOKEN", null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove("JWT_TOKEN").apply()
    }
}

/*
 * Reference List:
 * Android Developers, 2026. Work with data more securely. [Online] Available at: <https://developer.android.com/topic/security/data> [Accessed 21 September 2026].
 */