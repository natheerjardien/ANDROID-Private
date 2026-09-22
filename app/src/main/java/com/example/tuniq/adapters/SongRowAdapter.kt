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

class SongRowAdapter(private var tracks: List<JamendoTrack>) : RecyclerView.Adapter<SongRowAdapter.SongRowViewHolder>() {

    class SongRowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivRowArt: ImageView = view.findViewById(R.id.ivRowArt)
        val tvRowTitle: TextView = view.findViewById(R.id.tvRowTitle)
        val tvRowSubtitle: TextView = view.findViewById(R.id.tvRowSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongRowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_row, parent, false)
        return SongRowViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongRowViewHolder, position: Int) {
        val track = tracks[position]

        holder.tvRowTitle.text = track.name
        holder.tvRowSubtitle.text = track.artistName

        Glide.with(holder.itemView.context)
            .load(track.image)
            .placeholder(android.R.color.darker_gray)
            .into(holder.ivRowArt)

        holder.itemView.setOnClickListener {
            // Passes the full playlist queue and the clicked index into the manager
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