package com.example.tuniq

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tuniq.adapters.PlaylistAdapter
import com.example.tuniq.api.SpotifyApiService
import com.example.tuniq.api.SpotifyPlaylistResponse
import com.example.tuniq.auth.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_dashboard, container, false)

        val settingsButton = view.findViewById<ImageButton>(R.id.btnSettings)
        val notificationsButton = view.findViewById<ImageButton>(R.id.btnNotifications)

        // Opens the Settings screen
        settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        // Opens the Notifications screen
        notificationsButton.setOnClickListener {
            val intent = Intent(requireContext(), NotificationsActivity::class.java)
            startActivity(intent)
        }

        // Initialize the RecyclerView for playlists (Android Developers, 2026).
        rvPlaylists = view.findViewById(R.id.rvPlaylists)

        // Forces the list to scroll horizontally
        rvPlaylists.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Initialize adapter with an empty list and attach it to the RecyclerView
        playlistAdapter = PlaylistAdapter(emptyList())
        rvPlaylists.adapter = playlistAdapter

        // Initialize TokenManager to grab the saved Spotify token
        tokenManager = TokenManager(requireContext())

        // Fetch playlists immediately upon opening the dashboard
        fetchUserPlaylists()

        return view
    }

    /**
     * Executes an asynchronous network request to fetch the user's Spotify playlists (Square, n.d.).
     */
    private fun fetchUserPlaylists() {
        val token = tokenManager.getSpotifyToken()

        // If the user hasnt logged in yet, we abort the network call
        if (token == null)
        {
            Log.d("SpotifyAPI", "No token found. User needs to connect Spotify.")
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(SpotifyApiService::class.java)
        val call = service.getUserPlaylists("Bearer $token")

        call.enqueue(object : Callback<SpotifyPlaylistResponse> {
            override fun onResponse(call: Call<SpotifyPlaylistResponse>, response: Response<SpotifyPlaylistResponse>) {
                if (response.isSuccessful)
                {
                    val playlists = response.body()?.items

                    if (playlists != null)
                    {
                        // Pushes the fetched data into the RecyclerView adapter so it appears on screen
                        playlistAdapter.updateData(playlists)
                        Log.d("SpotifyAPI", "Successfully loaded ${playlists.size} playlists.")
                    }
                }
                else if (response.code() == 401)
                {
                    // 401 Unauthorized means the token expired. Trigger the 1hr refresh function in MainActivity.
                    Log.e("SpotifyAPI", "Token expired! Attempting silent refresh...")
                    (activity as? MainActivity)?.refreshSpotifyToken()
                }
                else
                {
                    Log.e("SpotifyAPI", "Failed to fetch playlists: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SpotifyPlaylistResponse>, t: Throwable) {
                Log.e("SpotifyAPI", "Network error fetching playlists", t)
            }
        })
    }
}

/*
 * Reference List:

 * Android Developers, 2026. Create dynamic lists with RecyclerView. [Online] Available at: < https://developer.android.com/develop/ui/views/layout/recyclerview > [Accessed 18 September 2026].

 * Square, n.d. Retrofit: A type-safe HTTP client for Android and Java. [Online] Available at: < https://square.github.io/retrofit/ > [Accessed 18 September 2026].

*/