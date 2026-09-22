package com.example.tuniq

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tuniq.api.RetrofitClient
import com.example.tuniq.api.SavedSong
import com.example.tuniq.api.TuniqApiService
import com.example.tuniq.media.AudioPlayerManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Controls the song.xml UI, displays the active track, and syncs liked songs to Azure (Android Developers, 2026).
 */
class SongFragment : Fragment() {
    private val backendBaseUrl = "https://tuniq-eje4crhtadg4auex.southafricanorth-01.azurewebsites.net/"

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sbProgress: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var ivPlayPause: ImageView

    private val updateProgressAction = object : Runnable {
        override fun run() {
            if (AudioPlayerManager.isPlaying())
            {
                val current = AudioPlayerManager.getCurrentPosition()
                sbProgress.progress = current
                tvCurrentTime.text = formatTime(current)
                ivPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
            else
            {
                ivPlayPause.setImageResource(android.R.drawable.ic_media_play)
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ensure R.layout.song is imported correctly
        val view = inflater.inflate(R.layout.active_song_screen, container, false)

        activity?.findViewById<View>(R.id.includeMiniPlayer)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.bottomNavigation)?.visibility = View.GONE

        val btnCollapse = view.findViewById<ImageButton>(R.id.btnCollapse)
        val tvTitle = view.findViewById<TextView>(R.id.tvNowPlayingTitle)
        val tvArtist = view.findViewById<TextView>(R.id.tvNowPlayingArtist)
        val ivArt = view.findViewById<ImageView>(R.id.ivNowPlayingArt)
        val btnLikeTrack = view.findViewById<ImageButton>(R.id.btnLikeTrack)

        val cvPlayPause = view.findViewById<CardView>(R.id.cvPlayPause)
        ivPlayPause = view.findViewById(R.id.ivPlayPauseIcon)
        sbProgress = view.findViewById(R.id.sbTrackProgress)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)

        val btnNextTrack = view.findViewById<ImageButton>(R.id.btnNextTrack)
        val btnPrevTrack = view.findViewById<ImageButton>(R.id.btnPrevTrack)
        val btnSleepTimer = view.findViewById<ImageButton>(R.id.btnSleepTimer)
        val btnAddToPlaylist = view.findViewById<ImageButton>(R.id.btnAddToPlaylist)

        // Close the full screen and return to the previous fragment
        btnCollapse.setOnClickListener {
            // Restores the bottom navbar when leaving the screen
            parentFragmentManager.popBackStack()
        }

        // Sleep Timer connected to button
        btnSleepTimer.setOnClickListener {
            val intent = android.content.Intent(requireContext(), SleepTimerActivity::class.java)
            startActivity(intent)
        }

        // Helper function to refresh UI when song changes
        fun updateScreenForTrack(track: com.example.tuniq.api.JamendoTrack) {
            tvTitle.text = track.name
            tvArtist.text = track.artistName
            Glide.with(requireContext()).load(track.image).into(ivArt)
            sbProgress.progress = 0
        }

        // Grab the globally playing track from the AudioPlayerManager
        val track = AudioPlayerManager.currentTrack

        if (track != null) {
            tvTitle.text = track.name
            tvArtist.text = track.artistName
            Glide.with(requireContext()).load(track.image).into(ivArt)

            // Syncs the UI when the audio is ready
            AudioPlayerManager.onPreparedListener = {
                val totalDuration = AudioPlayerManager.getDuration()
                sbProgress.max = totalDuration
                tvTotalTime.text = formatTime(totalDuration)
            }

            // Play/pause controls
            cvPlayPause.setOnClickListener {
                if (AudioPlayerManager.isPlaying()) AudioPlayerManager.pause()
                else AudioPlayerManager.resume()
            }

            // Next song listener
            btnNextTrack.setOnClickListener {
                if (AudioPlayerManager.nextTrack(requireContext()))
                {
                    AudioPlayerManager.currentTrack?.let { updateScreenForTrack(it) }
                }
                else
                {
                    Toast.makeText(requireContext(), "End of playlist", Toast.LENGTH_SHORT).show()
                }
            }

            // Previous song listener
            btnPrevTrack.setOnClickListener {
                if (AudioPlayerManager.prevTrack(requireContext()))
                {
                    AudioPlayerManager.currentTrack?.let { updateScreenForTrack(it) }
                }
                else
                {
                    Toast.makeText(requireContext(), "Start of playlist", Toast.LENGTH_SHORT).show()
                }
            }

            // User dragging the timeline
            sbProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) tvCurrentTime.text = formatTime(progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) { handler.removeCallbacks(updateProgressAction) }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    AudioPlayerManager.seekTo(sbProgress.progress)
                    handler.post(updateProgressAction)
                }
            })

            // Makes sure that the Firebase JWT Bearer token is used
            val tuniqApi = RetrofitClient.getApiService(requireContext())

            // Sends track data to C# backend when the Star is clicked
            btnLikeTrack.setOnClickListener {
                val savedSong = SavedSong(
                    trackId = track.id,
                    title = track.name,
                    artistName = track.artistName,
                    albumName = track.albumName ?: "Unknown Album",
                    coverArtUrl = track.image,
                    audioUrl = track.audio
                )

                tuniqApi.saveSong(savedSong).enqueue(object : Callback<SavedSong> {
                    override fun onResponse(call: Call<SavedSong>, response: Response<SavedSong>) {
                        if (response.isSuccessful)
                        {
                            Toast.makeText(requireContext(), "Added to Liked Songs!", Toast.LENGTH_SHORT).show()
                            btnLikeTrack.setColorFilter("#0097B2".toColorInt()) // Turn the star green
                        }
                        else
                        {
                            val errorDetails = response.errorBody()?.string() ?: "Unknown error"
                            Log.e("AzureAPI", "Failed! HTTP Code: ${response.code()} | Details: $errorDetails")
                            Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<SavedSong>, t: Throwable) {
                        Log.e("AzureAPI", "Network error", t)
                        Toast.makeText(requireContext(), "Network Error. Backend running?", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

            // Lets the user choose a playlist to add this song to
            btnAddToPlaylist.setOnClickListener {
                if (currentUserId == null)
                {
                    Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Fetches the users custom playlists
                tuniqApi.getUserPlaylists(currentUserId).enqueue(object : Callback<List<com.example.tuniq.api.PlaylistModel>> {
                    override fun onResponse(call: Call<List<com.example.tuniq.api.PlaylistModel>>, response: Response<List<com.example.tuniq.api.PlaylistModel>>) {
                        val playlists = response.body() ?: emptyList()

                        if (playlists.isEmpty())
                        {
                            Toast.makeText(requireContext(), "Create a playlist in your Library first!", Toast.LENGTH_LONG).show()
                            return
                        }

                        // Extracts the titles for the popup dialog options
                        val playlistTitles = playlists.map { it.title }.toTypedArray()

                        // Shows the AlertDialog
                        android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Add to Playlist")
                            .setItems(playlistTitles) { _, which ->
                                val selectedPlaylist = playlists[which]
                                val savedSong = SavedSong(
                                    trackId = track.id,
                                    title = track.name,
                                    artistName = track.artistName,
                                    albumName = track.albumName ?: "Unknown Album",
                                    coverArtUrl = track.image,
                                    audioUrl = track.audio
                                )

                                // Sends the song to the chosen playlist in Azure
                                selectedPlaylist.playlistId?.let { pid ->
                                    tuniqApi.addSongToPlaylist(pid, savedSong).enqueue(object : Callback<com.example.tuniq.api.PlaylistModel> {
                                        override fun onResponse(c: Call<com.example.tuniq.api.PlaylistModel>, r: Response<com.example.tuniq.api.PlaylistModel>) {
                                            if (r.isSuccessful)
                                            {
                                                Toast.makeText(requireContext(), "Added to ${selectedPlaylist.title}", Toast.LENGTH_SHORT).show()
                                                btnAddToPlaylist.setColorFilter("#0097B2".toColorInt()) // Optional: Highlight the button
                                            }
                                            else
                                            {
                                                Toast.makeText(requireContext(), "Failed to add song", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        override fun onFailure(c: Call<com.example.tuniq.api.PlaylistModel>, t: Throwable) {
                                            Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                            }
                            .show()
                    }

                    override fun onFailure(call: Call<List<com.example.tuniq.api.PlaylistModel>>, t: Throwable) {
                        Toast.makeText(requireContext(), "Failed to load playlists", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateProgressAction)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateProgressAction)
    }

    // Added onDestroyView lifecycle hook to guarantee the miniplayer and bottom nav bar reappear no matter how the user exits the active song screen (back button, swipe or click)
    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<View>(R.id.bottomNavigation)?.visibility = View.VISIBLE
        activity?.findViewById<View>(R.id.includeMiniPlayer)?.visibility = View.VISIBLE
        (activity as? MainActivity)?.updateMiniPlayer()
    }

    // Helper function to format milliseconds to M:SS
    private fun formatTime(millis: Int): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}

/*
 * Reference List:
 * Android Developers, 2026. Fragments Overview. [Online] Available at: <https://developer.android.com/guide/fragments> [Accessed 19 September 2026].
 */