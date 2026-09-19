package com.example.tuniq

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tuniq.auth.SpotifyAuthManager
import com.example.tuniq.auth.TokenManager
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    // Define the user's shared preferences
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tokenManager: TokenManager
    private val SETTINGS_PREF = "TuniqSettings"
    private val KEY_OFFLINE_SYNC = "offline_sync_enabled"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.app_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the SharedPreferences
        sharedPreferences = getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE)
        tokenManager = TokenManager(this)

        // Bind the buttons and the switch
        val btnExitSettings = findViewById<ImageButton>(R.id.btnExitSettings)
        val btnAccount = findViewById<TextView>(R.id.btnAccount)
        val btnConnectSpotify = findViewById<TextView>(R.id.btnConnectSpotify)
        val switchOfflineSync = findViewById<Switch>(R.id.switchOfflineSync)
        val btnEqualizer = findViewById<TextView>(R.id.btnEqualizer)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Initialize the switch state from saved preferences
        switchOfflineSync.isChecked = sharedPreferences.getBoolean(KEY_OFFLINE_SYNC, false)

        // Handle the switch toggles
        switchOfflineSync.setOnCheckedChangeListener { _, isChecked ->
            // Save the new state persistently
            sharedPreferences.edit().putBoolean(KEY_OFFLINE_SYNC, isChecked).apply()

            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Offline Sync $status", Toast.LENGTH_SHORT).show()
        }

        // Exit button
        btnExitSettings.setOnClickListener {
            finish() // Return to the previous screen
        }

        // Account navigation
        btnAccount.setOnClickListener {
            startActivity(Intent(this,AccountActivity::class.java))
        }

        // Triggers the Spotify OAuth Flow
        btnConnectSpotify.setOnClickListener {
            val existingToken = tokenManager.getSpotifyToken()

            if (existingToken != null)
            {
                Toast.makeText(this, "Spotify is already connected!", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val authUrl = SpotifyAuthManager.getAuthorizationUrl()
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                startActivity(browserIntent)
            }
        }

        // Equalizer navigation
        btnEqualizer.setOnClickListener {
            startActivity(Intent(this, EqualizerActivity::class.java))
        }

        // Execute Firebase Logout and clear activity stack
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()

            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            // FLAG_ACTIVITY_CLEAR_TASK ensures the user cannot hit the device "Back" button to return to the app after logging out
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}