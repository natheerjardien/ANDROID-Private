package com.example.tuniq

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.bumptech.glide.Glide
import com.example.tuniq.media.AudioPlayerManager

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var pbMiniPlayerProgress: ProgressBar
    private lateinit var btnMiniPlayerPlayPause: ImageButton

    // Timer to update the progress bar every 500ms
    private val progressRunnable = object : Runnable {
        override fun run() {
            if (AudioPlayerManager.isPlaying())
            {
                val current = AudioPlayerManager.getCurrentPosition()
                val total = AudioPlayerManager.getDuration()

                if (total > 0)
                {
                    pbMiniPlayerProgress.max = total
                    pbMiniPlayerProgress.progress = current
                }
                btnMiniPlayerPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
            else
            {
                btnMiniPlayerPlayPause.setImageResource(android.R.drawable.ic_media_play)
            }
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "Main screen opened")

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        pbMiniPlayerProgress = findViewById(R.id.pbMiniPlayerProgress)
        btnMiniPlayerPlayPause = findViewById(R.id.btnMiniPlayerPlayPause)

        // Loads the home_dashboard.xml by default when MainActivity starts
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Swaps out the screens based on which navigation tab the user taps (Android Developers, 2026a).
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_search -> replaceFragment(SearchFragment())
                R.id.nav_library -> replaceFragment(LibraryFragment())
            }
            true
        }

        btnMiniPlayerPlayPause.setOnClickListener {
            if (AudioPlayerManager.isPlaying()) AudioPlayerManager.pause()
            else AudioPlayerManager.resume()
        }
    }

    // Forces the mini player to check for active music every time user returns to the dashboard
    override fun onResume() {
        super.onResume()
        updateMiniPlayer()
        handler.post(progressRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(progressRunnable)
    }

    // Helper function to swap XML layouts in the fragmentContainer
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * Globally updates the mini player UI based on the active audio stream.
     */
    fun updateMiniPlayer() {
        val track = AudioPlayerManager.currentTrack
        val miniPlayerContainer = findViewById<View>(R.id.includeMiniPlayer)

        // Hides the miniplayer if the fullscreen SongFragment is active
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment is SongFragment)
        {
            miniPlayerContainer.visibility = View.GONE
            return
        }

        if (track != null)
        {
            miniPlayerContainer.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvMiniPlayerTitle).text = track.name
            findViewById<TextView>(R.id.tvMiniPlayerArtist).text = track.artistName

            val ivArt = findViewById<ImageView>(R.id.ivMiniPlayerArt)
            Glide.with(this).load(track.image).into(ivArt)

            // Tapping the mini player also opens the SongFragment
            miniPlayerContainer.setOnClickListener {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, SongFragment())
                    .addToBackStack(null)
                    .commit()
                miniPlayerContainer.visibility = View.GONE
            }
        }
        else
        {
            miniPlayerContainer.visibility = View.GONE
        }
    }
}

/*
 * Reference List:
 * Android Developers, 2026a. Bottom navigation. [Online] Available at: < https://material.io/components/bottom-navigation/android > [Accessed 17 September 2026].

 * Android Developers, 2026b. Handling Android App Links. [Online] Available at: < https://developer.android.com/training/app-links/deep-linking#handling-intents > [Accessed 17 September 2026].
 */