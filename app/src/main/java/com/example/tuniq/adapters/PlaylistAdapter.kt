package com.example.tuniq.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tuniq.R
import com.example.tuniq.api.Playlist

class PlaylistAdapter(private var playlists: List<Playlist>) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    // Defines the UI elements based on your existing item_music_card.xml
    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCoverArt: ImageView = view.findViewById(R.id.ivCoverArt)
        val tvItemTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvItemSubtitle: TextView = view.findViewById(R.id.tvItemSubtitle)
    }

    // Inflates your existing item_music_card.xml layout for each playlist
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music_card, parent, false)
        return PlaylistViewHolder(view)
    }

    // Binds the Spotify data to the views
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]

        // Set the primary text to the playlist name
        holder.tvItemTitle.text = playlist.name

        // Set the secondary text
        holder.tvItemSubtitle.text = "Spotify Playlist"

        // Checks if the playlist has an image array and loads the first image URL using Glide
        if (!playlist.images.isNullOrEmpty()) {
            val imageUrl = playlist.images[0].url
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.ivCoverArt)
        } else {
            // Clears the image if no cover art exists
            holder.ivCoverArt.setImageDrawable(null)
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    // Helper function to update the list when the network call finishes
    fun updateData(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}