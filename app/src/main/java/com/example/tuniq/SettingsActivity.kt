package com.example.tuniq

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

class SettingsActivity : AppCompatActivity() {

    // Define the user's shared preferences
    private lateinit var sharedPreferences: SharedPreferences
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

        // Bind the buttons and the switch
        val btnExitSettings = findViewById<ImageButton>(R.id.btnExitSettings)
        val btnAccount = findViewById<TextView>(R.id.btnAccount)
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
            Toast.makeText(this, "Manage Account clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,AccountActivity::class.java))
        }

        // Equalizer navigation
        btnEqualizer.setOnClickListener {
            Toast.makeText(this, "Audio Equalizer clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, EqualizerActivity::class.java))
        }

        // Handling logout
        btnLogout.setOnClickListener {

            // -- Clear User session/auth tokens --

            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()

            // -- Redirect to log-in activity and clear this intent --
        }
    }
}