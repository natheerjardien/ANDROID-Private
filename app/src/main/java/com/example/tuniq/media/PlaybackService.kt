package com.example.tuniq.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.VolumeShaper
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tuniq.R
import com.example.tuniq.api.JamendoTrack

class PlaybackService : Service() {

    companion object {
        private const val TAG = "PlaybackService"
        private const val CHANNEL_ID = "tuniq_playback"
        private const val NOTIFICATION_ID = 1001

        private var instance: PlaybackService? = null

        fun getInstance(): PlaybackService? = instance
    }

    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: JamendoTrack? = null

    private var volumeShaper: VolumeShaper? = null

    private val handler = Handler(Looper.getMainLooper())

    private var sleepTimerRunnable: Runnable? = null
    private var stopAfterFadeRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()

        instance = this

        Log.d(TAG, "Playback service created")

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        Log.d(TAG, "Playback service started")

        return START_STICKY
    }

    private fun releaseVolumeShaper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            volumeShaper?.close()
            volumeShaper = null
        }
    }

    fun playTrack(track: JamendoTrack) {

        currentTrack = track

        try {
            releaseVolumeShaper()

            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setDataSource(track.audio)

                prepareAsync()

                setOnPreparedListener {

                    start()

                    Log.d(
                        TAG,
                        "Playback started: ${track.name}"
                    )

                    AudioPlayerManager.onPreparedListener?.invoke()
                }

                setOnCompletionListener {

                    Log.d(
                        TAG,
                        "Playback completed: ${track.name}"
                    )
                }

                setOnErrorListener { _, what, extra ->

                    Log.e(
                        TAG,
                        "MediaPlayer error. What: $what Extra: $extra"
                    )

                    true
                }
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error starting playback",
                e
            )
        }
    }

    fun pausePlayback() {
        mediaPlayer?.pause()

        Log.d(TAG, "Playback paused")
    }

    fun resumePlayback() {
        mediaPlayer?.start()

        Log.d(TAG, "Playback resumed")
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getAudioSessionId(): Int {
        return mediaPlayer?.audioSessionId ?: 0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun stopPlayback() {

        cancelSleepTimer()

        releaseVolumeShaper()

        mediaPlayer?.release()
        mediaPlayer = null

        currentTrack = null

        Log.d(TAG, "Playback stopped")
    }

    /*
     * Starts the sleep timer.
     *
     * The music continues playing until the fade period begins.
     * VolumeShaper then gradually lowers the volume before playback stops.
     */
    fun startSleepTimer(
        durationMillis: Long,
        fadeDurationMillis: Long = 30_000L
    ) {

        cancelSleepTimer()

        Log.d(
            TAG,
            "Sleep timer started: $durationMillis ms"
        )

        val fadeStartDelay =
            (durationMillis - fadeDurationMillis).coerceAtLeast(0L)

        sleepTimerRunnable = Runnable {

            startVolumeFade(fadeDurationMillis)

        }

        handler.postDelayed(
            sleepTimerRunnable!!,
            fadeStartDelay
        )

        stopAfterFadeRunnable = Runnable {

            stopPlayback()

            stopSelf()

            Log.d(
                TAG,
                "Sleep timer finished. Playback service stopped."
            )
        }

        handler.postDelayed(
            stopAfterFadeRunnable!!,
            fadeStartDelay + fadeDurationMillis
        )
    }

    /*
     * Gradually reduces the MediaPlayer volume to zero.
     */
    private fun startVolumeFade(
        fadeDurationMillis: Long
    ) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            Log.w(
                TAG,
                "VolumeShaper requires Android 8.0 or higher."
            )

            return
        }

        val player = mediaPlayer ?: return

        try {

            volumeShaper?.close()

            val configuration =
                VolumeShaper.Configuration.Builder(
                    VolumeShaper.Configuration.LINEAR_RAMP
                )
                    .setDuration(fadeDurationMillis)
                    .setCurve(
                        floatArrayOf(0f, 1f),
                        floatArrayOf(1f, 0f)
                    )
                    .build()

            volumeShaper =
                player.createVolumeShaper(configuration)

            volumeShaper?.apply(VolumeShaper.Operation.PLAY)

            Log.d(
                TAG,
                "Volume fade started for $fadeDurationMillis ms"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Failed to start volume fade",
                e
            )
        }
    }

    fun cancelSleepTimer() {

        sleepTimerRunnable?.let {
            handler.removeCallbacks(it)
        }

        stopAfterFadeRunnable?.let {
            handler.removeCallbacks(it)
        }

        sleepTimerRunnable = null
        stopAfterFadeRunnable = null

        releaseVolumeShaper()

        Log.d(TAG, "Sleep timer cancelled")
    }

    override fun onDestroy() {

        Log.d(TAG, "Playback service destroyed")

        cancelSleepTimer()

        mediaPlayer?.release()
        mediaPlayer = null

        instance = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tuniq Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("Tuniq")
            .setContentText("Music playback active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }
}