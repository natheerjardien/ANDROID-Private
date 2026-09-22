package com.example.tuniq.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tuniq.MainActivity
import com.example.tuniq.R
import com.example.tuniq.SongFragment
import com.example.tuniq.api.JamendoTrack
import com.example.tuniq.media.AudioPlayerManager

/**
 * Binds Jamendo track data to the item_music_card XML layout for dashboard display (Android Developers, 2026).
 */
class TrackAdapter(private var tracks: List<JamendoTrack>) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCoverArt: ImageView = view.findViewById(R.id.ivCoverArt)
        val tvItemTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvItemSubtitle: TextView = view.findViewById(R.id.tvItemSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music_card, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]

        // Bind the song name and artist name
        holder.tvItemTitle.text = track.name
        holder.tvItemSubtitle.text = track.artistName

        // Load the album cover URL provided by the Jamendo API
        Glide.with(holder.itemView.context)
            .load(track.image)
            .placeholder(android.R.color.darker_gray)
            .into(holder.ivCoverArt)

        // Triggers audio playback when the user taps the card
        holder.itemView.setOnClickListener {
            // Injects the full Quick Picks list and the current index into the queue (so the next/prev buttons work)
            AudioPlayerManager.trackQueue = tracks
            AudioPlayerManager.currentIndex = position

            AudioPlayerManager.playTrack(holder.itemView.context, track)

            val context = holder.itemView.context

            if (context is MainActivity)
            {
                context.updateMiniPlayer()
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, SongFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateData(newTracks: List<JamendoTrack>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}

/*
 * Reference List:
 * Android Developers, 2026. Create dynamic lists with RecyclerView. [Online] Available at: <https://developer.android.com/develop/ui/views/layout/recyclerview> [Accessed 19 September 2026].
 */