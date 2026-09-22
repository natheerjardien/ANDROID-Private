package com.example.tuniq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tuniq.adapters.CategoryAdapter
import com.example.tuniq.adapters.PlaylistAdapter
import com.example.tuniq.adapters.TrackAdapter
import com.example.tuniq.api.JamendoApiService
import com.example.tuniq.api.JamendoResponse
import com.example.tuniq.api.PlaylistModel
import com.example.tuniq.api.RetrofitClient
import com.example.tuniq.auth.JamendoAuthManager
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {
    private lateinit var rvQuickPicks: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    private lateinit var rvHomePlaylists: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter

    private lateinit var rvCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

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

        // Initialize the RecyclerView for the Quick Picks section
        rvQuickPicks = view.findViewById(R.id.rvQuickPicks)
        rvQuickPicks.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        trackAdapter = TrackAdapter(emptyList())
        rvQuickPicks.adapter = trackAdapter

        rvHomePlaylists = view.findViewById(R.id.rvPlaylists)
        rvHomePlaylists.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        playlistAdapter = PlaylistAdapter(emptyList())
        rvHomePlaylists.adapter = playlistAdapter

        rvCategories = view.findViewById(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val genreList = listOf("Hip-hop", "Jazz", "Electronic", "Classical", "R&B")

        categoryAdapter = CategoryAdapter(genreList) { selectedGenre ->
            val searchFragment = SearchFragment().apply {
                arguments = Bundle().apply {
                    putString("PREFILLED_SEARCH_QUERY", selectedGenre)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, searchFragment) // Make sure this matches your container ID
                .addToBackStack(null)
                .commit()
        }
        rvCategories.adapter = categoryAdapter

        fetchTrendingTracks()

        fetchUserPlaylists()

        return view
    }

    /**
     * Executes an asynchronous network request to fetch public tracks from Jamendo
     */
    private fun fetchTrendingTracks() {
        val retrofit = Retrofit.Builder()
            .baseUrl(JamendoAuthManager.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(JamendoApiService::class.java)

        // Fetching 20 public tracks using our registered Client ID
        val call = service.getTracks(clientId = JamendoAuthManager.CLIENT_ID)

        call.enqueue(object : Callback<JamendoResponse> {
            override fun onResponse(call: Call<JamendoResponse>, response: Response<JamendoResponse>) {
                if (response.isSuccessful)
                {
                    val tracks = response.body()?.results

                    if (tracks != null)
                    {
                        trackAdapter.updateData(tracks)
                        Log.d("JamendoAPI", "Successfully loaded ${tracks.size} tracks.")
                    }
                }
                else
                {
                    Log.e("JamendoAPI", "Failed to fetch tracks: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<JamendoResponse>, t: Throwable) {
                Log.e("JamendoAPI", "Network error fetching tracks", t)
            }
        })
    }

    // Fetches the custom playlists from our backend
    private fun fetchUserPlaylists() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val azureApi = RetrofitClient.getApiService(requireContext())

        azureApi.getUserPlaylists(currentUserId).enqueue(object : Callback<List<PlaylistModel>> {
            override fun onResponse(call: Call<List<PlaylistModel>>, response: Response<List<PlaylistModel>>) {
                if (response.isSuccessful)
                {
                    val playlists = response.body() ?: emptyList()
                    playlistAdapter.updateData(playlists)
                }
            }
            override fun onFailure(call: Call<List<PlaylistModel>>, t: Throwable) {
                Log.e("AzureAPI", "Failed to load home playlists", t)
            }
        })
    }
}

/*
 * Reference List:

 * Android Developers, 2026. Create dynamic lists with RecyclerView. [Online] Available at: < https://developer.android.com/develop/ui/views/layout/recyclerview > [Accessed 18 September 2026].

 * Square, n.d. Retrofit: A type-safe HTTP client for Android and Java. [Online] Available at: < https://square.github.io/retrofit/ > [Accessed 18 September 2026].

*/