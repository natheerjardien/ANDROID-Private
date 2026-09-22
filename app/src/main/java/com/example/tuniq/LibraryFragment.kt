package com.example.tuniq

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tuniq.adapters.SongRowAdapter
import com.example.tuniq.api.JamendoTrack
import com.example.tuniq.api.SavedSong
import com.example.tuniq.api.TuniqApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.tuniq.adapters.PlaylistAdapter
import com.example.tuniq.utils.SessionManager
import com.example.tuniq.api.PlaylistModel
import com.google.firebase.auth.FirebaseAuth

/**
 * This fragment is responsible for populating the Library screen and fetching all saved songs from database (Android Developers, 2026b).
 */
class LibraryFragment : Fragment() {
    private lateinit var rvLibraryItems: RecyclerView
    private lateinit var songRowAdapter: SongRowAdapter
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var azureApi: TuniqApiService

    private lateinit var tvTabPlaylists: TextView
    private lateinit var tvTabLikedSongs: TextView

    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.library_view, container, false)

        // Maps to the exact RecyclerView ID in our library_view.xml
        rvLibraryItems = view.findViewById(R.id.rvLibraryItems)
        tvTabPlaylists = view.findViewById(R.id.tvTabPlaylists)
        tvTabLikedSongs = view.findViewById(R.id.tvTabLikedSongs)
        val btnAddPlayList = view.findViewById<ImageButton>(R.id.btnAddPlayList)

        // Initializes the adapter to match the UI design of the Search screen
        songRowAdapter = SongRowAdapter(emptyList())
        playlistAdapter = PlaylistAdapter(emptyList())
        rvLibraryItems.adapter = playlistAdapter

        // Fetches the unique Firebase UID for the currently logged-in user
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        azureApi = com.example.tuniq.api.RetrofitClient.getApiService(requireContext())

        // Only fetch user-specific data if they are actually logged in
        if (currentUserId != null)
        {
            fetchPlaylistsFromAzure()
        }

        setupTabListeners()

        // Launches the dialog to create a playlist when the + icon is tapped
        btnAddPlayList.setOnClickListener {
            if (currentUserId != null)
            {
                showCreatePlaylistDialog()
            }
            else
            {
                Toast.makeText(requireContext(), "Please log in to create playlists", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    /**
     * Does a network call to the custom C# Azure API to fetch saved database records (Square, n.d.).
     */
    private fun fetchLikedSongsFromAzure() {
        azureApi.getLikedSongs().enqueue(object : Callback<List<SavedSong>> {
            override fun onResponse(call: Call<List<SavedSong>>, response: Response<List<SavedSong>>) {
                if (response.isSuccessful)
                {
                    val savedSongs = response.body() ?: emptyList()

                    // Maps the custom Azure models back into JamendoTrack models to reuse the active song UI logic
                    val jamendoTracks = savedSongs.map { it.toJamendoTrack() }

                    songRowAdapter.updateData(jamendoTracks)

                    if (jamendoTracks.isEmpty())
                    {
                        Toast.makeText(requireContext(), "No liked songs yet.", Toast.LENGTH_SHORT).show()
                    }
                }
                else
                {
                    Log.e("AzureAPI", "Failed to load library: HTTP ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to load Library", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<SavedSong>>, t: Throwable) {
                Log.e("AzureAPI", "Network error in Library", t)
                Toast.makeText(requireContext(), "Network Error. Check internet connection.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Configures click listeners for the UI tabs to swap the active RecyclerView adapter
     */
    private fun setupTabListeners() {
        tvTabPlaylists.setOnClickListener {
            tvTabPlaylists.setBackgroundResource(R.drawable.bg_rounded_input)
            tvTabPlaylists.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0097B2"))
            tvTabPlaylists.setTextColor(android.graphics.Color.BLACK)

            tvTabLikedSongs.backgroundTintList = null
            tvTabLikedSongs.setTextColor(android.graphics.Color.WHITE)

            rvLibraryItems.adapter = playlistAdapter
            if (currentUserId != null) fetchPlaylistsFromAzure()
        }

        tvTabLikedSongs.setOnClickListener {
            tvTabLikedSongs.setBackgroundResource(R.drawable.bg_rounded_input)
            tvTabLikedSongs.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0097B2"))
            tvTabLikedSongs.setTextColor(android.graphics.Color.BLACK)

            tvTabPlaylists.backgroundTintList = null
            tvTabPlaylists.setTextColor(android.graphics.Color.WHITE)

            rvLibraryItems.adapter = songRowAdapter
            fetchLikedSongsFromAzure()
        }
    }

    /**
     * Displays a custom AlertDialog capturing text input for the new playlists title (Android Developers, 2026a)
     */
    private fun showCreatePlaylistDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_playlist, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etPlaylistTitle)

        AlertDialog.Builder(requireContext())
            .setTitle("Create Playlist")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val title = etTitle.text.toString().trim()

                if (title.isNotEmpty())
                {
                    createPlaylistInAzure(title)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Runs a POST request to Azure to create a new playlist linked to the user ID
     */
    private fun createPlaylistInAzure(title: String) {
        val activeUser = currentUserId ?: return
        val newPlaylist = PlaylistModel(title = title, userId = activeUser)

        azureApi.createPlaylist(newPlaylist).enqueue(object : Callback<PlaylistModel> {
            override fun onResponse(call: Call<PlaylistModel>, response: Response<PlaylistModel>) {
                if (response.isSuccessful)
                {
                    Toast.makeText(requireContext(), "Playlist '$title' created!", Toast.LENGTH_SHORT).show()
                    fetchPlaylistsFromAzure()
                }
                else
                {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("AzureAPI", "POST Failed: HTTP ${response.code()} - $errorBody")
                    Toast.makeText(requireContext(), "Failed to load playlists: HTTP ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<PlaylistModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error loading playlists.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Fetches the playlist models from the Azure backend for the active user
     */
    private fun fetchPlaylistsFromAzure() {
        val activeUser = currentUserId ?: return

        azureApi.getUserPlaylists(activeUser).enqueue(object : Callback<List<PlaylistModel>> {
            override fun onResponse(call: Call<List<PlaylistModel>>, response: Response<List<PlaylistModel>>) {
                if (response.isSuccessful) {
                    playlistAdapter.updateData(response.body() ?: emptyList())
                }
            }
            override fun onFailure(call: Call<List<PlaylistModel>>, t: Throwable) {
                Log.e("AzureAPI", "Failed to load playlists", t)
            }
        })
    }

    /**
     * This is an extension function that converts the backend SavedSong entity into a functional JamendoTrack object
     */
    private fun SavedSong.toJamendoTrack(): JamendoTrack {
        return JamendoTrack(
            id = this.trackId,
            name = this.title,
            artistName = this.artistName,
            albumName = this.albumName,
            duration = 0,
            image = this.coverArtUrl,
            audio = this.audioUrl,
            downloadAllowed = false
        )
    }
}

/*
 * Reference List:

 * Android Developers, 2026a. Dialogs. [Online] Available at: <https://developer.android.com/develop/ui/views/components/dialogs> [Accessed 20 September 2026].

 * Android Developers, 2026b. Fragments. [Online] Available at: <https://developer.android.com/guide/fragments> [Accessed 20 September 2026].

 * Android Developers, 2026c. Save key-value data. [Online] Available at: <https://developer.android.com/training/data-storage/shared-preferences> [Accessed 20 September 2026].

 * Square, n.d. Retrofit: A type-safe HTTP client for Android and Java. [Online] Available at: <https://square.github.io/retrofit/> [Accessed 20 September 2026].

*/