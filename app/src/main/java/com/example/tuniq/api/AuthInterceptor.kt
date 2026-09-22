package com.example.tuniq.api

import com.example.tuniq.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Automates the injection of the Bearer token into the HTTP Authorization header for all API calls (Square, 2026).
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // Retrieves the decrypted token
        val token = tokenManager.getToken()

        // If a token exists, it attaches it to the header
        if (!token.isNullOrEmpty())
        {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

/*
 * Reference List:
 * Square, 2026. OkHttp Interceptors. [Online] Available at: <https://square.github.io/okhttp/features/interceptors/> [Accessed 21 September 2026].
 */