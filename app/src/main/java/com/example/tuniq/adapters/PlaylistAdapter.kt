package com.example.tuniq.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tuniq.PlaylistFragment
import com.example.tuniq.R
import com.example.tuniq.api.PlaylistModel

class PlaylistAdapter(private var playlists: List<PlaylistModel>) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivRowArt: ImageView = view.findViewById(R.id.ivRowArt)
        val tvRowTitle: TextView = view.findViewById(R.id.tvRowTitle)
        val tvRowSubtitle: TextView = view.findViewById(R.id.tvRowSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_row, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]

        holder.tvRowTitle.text = playlist.title

        val songCount = playlist.songs?.size ?: 0
        holder.tvRowSubtitle.text = "Playlist • $songCount songs"

        // Uses a generic icon for playlists since they wont have cover art
        holder.ivRowArt.setImageResource(android.R.drawable.ic_menu_agenda)

        holder.itemView.setOnClickListener {
            // Serializes the playlist object so we can pass it through a Bundle
            val playlistJson = com.google.gson.Gson().toJson(playlist)

            val fragment = PlaylistFragment().apply {
                arguments = Bundle().apply {
                    putString("PLAYLIST_JSON", playlistJson)
                }
            }

            // Opens to the playlist fragment
            val activity = it.context as? androidx.appcompat.app.AppCompatActivity
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragmentContainer, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun updateData(newPlaylists: List<PlaylistModel>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}