package com.example.tuniq

import android.media.audiofx.Equalizer
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EqualizerActivity : AppCompatActivity() {

    private var equalizer: Equalizer? = null // (AndroidDevelopers, 2026)

    // -- Pass audioSessionId to this Activity via Intent extras --
    private var audioSessionId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_equalizer)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.equalizer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Equalizer and check if the device has Equalizer support
        try {
            // If there is support, create an instance of the Equalizer
            equalizer = Equalizer(0, audioSessionId)
        } catch (e: Exception) {
            // If the device does not support the equalizer, send a text and return to the previous screen
            Toast.makeText(this, "Equalizer not supported on this device", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val switchEqEnabled = findViewById<Switch>(R.id.switchEqEnabled)
        val seek60Hz = findViewById<SeekBar>(R.id.seek60Hz)
        val seek230Hz = findViewById<SeekBar>(R.id.seek230Hz)
        val seek910Hz = findViewById<SeekBar>(R.id.seek910Hz)
        val btnResetEq = findViewById<TextView>(R.id.btnResetEq)

        // Get the minimum and maximum decibel range supported by the device's hardware
        val bandLevelRange = equalizer!!.bandLevelRange
        val minEQLevel = bandLevelRange[0]
        val maxEQLevel = bandLevelRange[1]

        // Tag each SeekBar with its corresponding Equalizer band index
        seek60Hz.tag = 0.toShort()
        seek230Hz.tag = 1.toShort()
        seek910Hz.tag = 2.toShort()

        // Sync initial state
        equalizer!!.enabled = switchEqEnabled.isChecked
        seek60Hz.isEnabled = switchEqEnabled.isChecked
        seek230Hz.isEnabled = switchEqEnabled.isChecked
        seek910Hz.isEnabled = switchEqEnabled.isChecked

        switchEqEnabled.setOnCheckedChangeListener { _, isChecked ->
            equalizer?.enabled = isChecked
            seek60Hz.isEnabled = isChecked
            seek230Hz.isEnabled = isChecked
            seek910Hz.isEnabled = isChecked
        }

        btnBack.setOnClickListener { finish() }

        btnResetEq.setOnClickListener {
            seek60Hz.progress = 50
            seek230Hz.progress = 50
            seek910Hz.progress = 50
            Toast.makeText(this, "Equalizer reset to default", Toast.LENGTH_SHORT).show()
        }

        val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && equalizer != null) {
                    val bandIndex = seekBar.tag as Short

                    // Convert 0-100 progress into the specific millibel range
                    val newLevel = (minEQLevel + (maxEQLevel - minEQLevel) * (progress / 100f)).toInt().toShort()

                    // Apply to the audio hardware
                    equalizer?.setBandLevel(bandIndex, newLevel)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        seek60Hz.setOnSeekBarChangeListener(seekBarListener)
        seek230Hz.setOnSeekBarChangeListener(seekBarListener)
        seek910Hz.setOnSeekBarChangeListener(seekBarListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the audio effect engine when done to prevent memory leaks
        equalizer?.release()
        equalizer = null
    }
}

/*
Reference List:

AndroidDevelopers, 2026. Equalizer. [online]. Available at: <https://developer.android.com/reference/android/media/audiofx/Equalizer> [Accessed 17 September 2026].

*/