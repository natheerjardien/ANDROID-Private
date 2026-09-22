package com.example.tuniq.media

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.tuniq.api.JamendoTrack

/**
 * Controls playback through PlaybackService (Android Developers, n.d.).
 */
object AudioPlayerManager {

    var currentTrack: JamendoTrack? = null

    var onPreparedListener: (() -> Unit)? = null

    // Song queue and index for next/previous functionality
    var trackQueue: List<JamendoTrack> = emptyList()
    var currentIndex: Int = -1

    fun playTrack(
        context: Context,
        track: JamendoTrack
    ) {

        currentTrack = track

        startPlaybackService(context)

        val service = PlaybackService.getInstance()

        if (service != null) {

            service.playTrack(track)

        } else {

            Log.w(
                "AudioPlayer",
                "PlaybackService is not ready yet."
            )
        }
    }

    // Moves to next song in queue
    fun nextTrack(context: Context): Boolean {
        if (trackQueue.isNotEmpty() && currentIndex < trackQueue.size - 1)
        {
            currentIndex++
            playTrack(context, trackQueue[currentIndex])
            return true
        }
        return false
    }

    // Goes back to previous song in queue
    fun prevTrack(context: Context): Boolean {
        if (trackQueue.isNotEmpty() && currentIndex > 0)
        {
            currentIndex--
            playTrack(context, trackQueue[currentIndex])
            return true
        }
        return false
    }

    fun pause() {

        PlaybackService
            .getInstance()
            ?.pausePlayback()
    }

    fun resume() {

        PlaybackService
            .getInstance()
            ?.resumePlayback()
    }

    fun isPlaying(): Boolean {

        return PlaybackService
            .getInstance()
            ?.isPlaying()
            ?: false
    }

    fun getDuration(): Int {

        return PlaybackService
            .getInstance()
            ?.getDuration()
            ?: 0
    }

    fun getCurrentPosition(): Int {

        return PlaybackService
            .getInstance()
            ?.getCurrentPosition()
            ?: 0
    }
    fun getAudioSessionId(): Int {
        return PlaybackService
            .getInstance()
            ?.getAudioSessionId()
            ?: 0
    }

    fun seekTo(position: Int) {

        PlaybackService
            .getInstance()
            ?.seekTo(position)
    }

    // Stops playback and clears the memory
    fun stopTrack() {

        PlaybackService
            .getInstance()
            ?.stopPlayback()

        currentTrack = null
    }

    fun startSleepTimer(
        durationMillis: Long
    ) {

        PlaybackService
            .getInstance()
            ?.startSleepTimer(durationMillis)
    }

    fun cancelSleepTimer() {

        PlaybackService
            .getInstance()
            ?.cancelSleepTimer()
    }

    private fun startPlaybackService(
        context: Context
    ) {

        val intent =
            Intent(context, PlaybackService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.startForegroundService(intent)

        } else {

            context.startService(intent)
        }

        Log.d(
            "AudioPlayer",
            "Playback service started"
        )
    }
}