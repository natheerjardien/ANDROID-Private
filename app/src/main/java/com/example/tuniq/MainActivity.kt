package com.example.tuniq

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.example.tuniq.api.SpotifyApiService
import com.example.tuniq.api.SpotifyTokenResponse
import com.example.tuniq.auth.SpotifyAuthManager
import com.example.tuniq.auth.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.tuniq.api.SpotifyProfileResponse

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "Main screen opened")

        auth = FirebaseAuth.getInstance()
        tokenManager = TokenManager(this)

        handleIntent(intent)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Loads the home_dashboard.xml by default when MainActivity starts
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Swaps out the screens based on which navigation tab the user taps (Android Developers, 2026a).
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_search -> replaceFragment(SearchFragment())
                R.id.nav_library -> replaceFragment(LibraryFragment())
            }
            true
        }
    }

    // Helper function to swap XML layouts in the fragmentContainer
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Catch intent if activity is already running (singleTask mode)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val data: Uri? = intent.data

        // Catches the tuniq://callback redirect from Spotify (Android Developers, 2026b).
        if (Intent.ACTION_VIEW == action && data != null && data.scheme == "tuniq") {
            val code = data.getQueryParameter("code")
            if (code != null) {
                Log.d("SpotifyAuth", "Authorization Code retrieved: $code")
                exchangeCodeForToken(code)
            }
        }
    }

    // Executes a network call via Retrofit to exchange the auth code for an access token.
    private fun exchangeCodeForToken(code: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(SpotifyApiService::class.java)

        val call = service.getAccessToken(
            authorization = SpotifyAuthManager.getAuthHeader(),
            grantType = "authorization_code",
            code = code,
            redirectUri = SpotifyAuthManager.REDIRECT_URI
        )

        call.enqueue(object : Callback<SpotifyTokenResponse> {
            override fun onResponse(call: Call<SpotifyTokenResponse>, response: Response<SpotifyTokenResponse>) {
                if (response.isSuccessful) {
                    val accessToken = response.body()?.accessToken
                    if (accessToken != null) {
                        Log.d("SpotifyAuth", "SUCCESS! Access Token: $accessToken")

                        val refreshToken = response.body()?.refreshToken
                        tokenManager.saveTokens(accessToken, refreshToken)

                        // Immediately fetch the user profile using the new token
                        fetchSpotifyUserProfile(accessToken)

                        Toast.makeText(this@MainActivity, "Spotify Connected!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("SpotifyAuth", "Token exchange failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SpotifyTokenResponse>, t: Throwable) {
                Log.e("SpotifyAuth", "Network error during token exchange", t)
            }
        })
    }

    // Refreshes the Spotify access token in the background
    fun refreshSpotifyToken() {
        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken == null)
        {
            Log.e("SpotifyAuth", "No refresh token found. User must log in manually.")
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(SpotifyApiService::class.java)

        val call = service.refreshToken(
            authorization = SpotifyAuthManager.getAuthHeader(),
            grantType = "refresh_token",
            refreshToken = refreshToken
        )

        call.enqueue(object : Callback<SpotifyTokenResponse> {
            override fun onResponse(call: Call<SpotifyTokenResponse>, response: Response<SpotifyTokenResponse>) {
                if (response.isSuccessful)
                {
                    val newAccessToken = response.body()?.accessToken
                    val newRefreshToken = response.body()?.refreshToken

                    if (newAccessToken != null)
                    {
                        Log.d("SpotifyAuth", "SUCCESS! Token silently refreshed.")
                        // Save the new access token (and the new refresh token, if Spotify gave one)
                        tokenManager.saveTokens(newAccessToken, newRefreshToken ?: refreshToken)
                    }
                } else {
                    Log.e("SpotifyAuth", "Failed to refresh token: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SpotifyTokenResponse>, t: Throwable) {
                Log.e("SpotifyAuth", "Network error during token refresh", t)
            }
        })
    }

    // Fetches the authenticated user's Spotify profile using the saved access token.
    private fun fetchSpotifyUserProfile(token: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(SpotifyApiService::class.java)

        // Spotify requires the word "Bearer " before the token in the header
        val call = service.getUserProfile("Bearer $token")

        call.enqueue(object : Callback<SpotifyProfileResponse> {
            override fun onResponse(call: Call<SpotifyProfileResponse>, response: Response<SpotifyProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        Log.d("SpotifyProfile", "Welcome, ${profile.displayName}! (Email: ${profile.email})")
                        Toast.makeText(this@MainActivity, "Logged into Spotify as ${profile.displayName}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("SpotifyProfile", "Failed to fetch profile: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SpotifyProfileResponse>, t: Throwable) {
                Log.e("SpotifyProfile", "Network error fetching profile", t)
            }
        })
    }
}

/*
 * Reference List:
 * Android Developers, 2026a. Bottom navigation. [Online] Available at: < https://material.io/components/bottom-navigation/android > [Accessed 17 September 2026].

 * Android Developers, 2026b. Handling Android App Links. [Online] Available at: < https://developer.android.com/training/app-links/deep-linking#handling-intents > [Accessed 17 September 2026].
 */