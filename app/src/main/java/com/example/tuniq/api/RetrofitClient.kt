package com.example.tuniq.api

import android.content.Context
import com.example.tuniq.utils.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Centralized HTTP client configuration using OkHttp with Retrofit for secure routing (Square, 2026).
 */
object RetrofitClient {
    private const val BASE_URL = "https://tuniq-eje4crhtadg4auex.southafricanorth-01.azurewebsites.net/"

    fun getApiService(context: Context): TuniqApiService {
        val tokenManager = TokenManager(context)
        val authInterceptor = AuthInterceptor(tokenManager)

        // Attaches the interceptor to the HTTP client
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Uses the client with the injected headers
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(TuniqApiService::class.java)
    }
}

/*
 * Reference List:
 * Square, 2026. Retrofit. [Online] Available at: <https://square.github.io/retrofit/> [Accessed 21 September 2026].
 */