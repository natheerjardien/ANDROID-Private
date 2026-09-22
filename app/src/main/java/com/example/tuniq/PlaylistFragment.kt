package com.example.tuniq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.tuniq.adapters.SongRowAdapter
import com.example.tuniq.api.JamendoTrack
import com.example.tuniq.api.PlaylistModel
import com.google.gson.Gson

class PlaylistFragment : Fragment() {

    private lateinit var rvPlaylistSongs: RecyclerView
    private lateinit var songRowAdapter: SongRowAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.playlist_view, container, false)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBackFromPlaylist)
        val tvTitle = view.findViewById<TextView>(R.id.tvPlaylistDetailTitle)
        rvPlaylistSongs = view.findViewById(R.id.rvPlaylistSongs)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Fetches the serialized playlist object passed from the Library adapter
        val playlistJson = arguments?.getString("PLAYLIST_JSON")
        val playlist = Gson().fromJson(playlistJson, PlaylistModel::class.java)

        tvTitle.text = playlist.title

        // Maps the backend SavedSong objects into JamendoTrack models so we can reuse SongRowAdapter
        val jamendoTracks = playlist.songs?.map {
            JamendoTrack(
                id = it.trackId,
                name = it.title,
                artistName = it.artistName,
                albumName = it.albumName,
                duration = 0,
                image = it.coverArtUrl,
                audio = it.audioUrl,
                downloadAllowed = false
            )
        } ?: emptyList()

        songRowAdapter = SongRowAdapter(jamendoTracks)
        rvPlaylistSongs.adapter = songRowAdapter

        return view
    }
}