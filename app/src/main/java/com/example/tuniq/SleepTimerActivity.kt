package com.example.tuniq

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tuniq.media.AudioPlayerManager

class SleepTimerActivity : AppCompatActivity() {

    private lateinit var tvSleepTimerStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sleep_timer)

        val btnBack = findViewById<ImageButton>(
            R.id.btnBackSleepTimer
        )

        val btnTest = findViewById<Button>(
            R.id.btnTestTimer
        )

        tvSleepTimerStatus = findViewById(
            R.id.tvSleepTimerStatus
        )

        val btn15 = findViewById<Button>(
            R.id.btn15Minutes
        )

        val btn30 = findViewById<Button>(
            R.id.btn30Minutes
        )

        val btn45 = findViewById<Button>(
            R.id.btn45Minutes
        )

        val btn60 = findViewById<Button>(
            R.id.btn60Minutes
        )

        val btnCancel = findViewById<Button>(
            R.id.btnCancelTimer
        )

        btnBack.setOnClickListener {
            finish()
        }
        btnTest.setOnClickListener {
            startTestTimer()
        }

        btn15.setOnClickListener {
            startTimer(15)
        }

        btn30.setOnClickListener {
            startTimer(30)
        }

        btn45.setOnClickListener {
            startTimer(45)
        }

        btn60.setOnClickListener {
            startTimer(60)
        }

        btnCancel.setOnClickListener {
            AudioPlayerManager.cancelSleepTimer()

            tvSleepTimerStatus.text = "Sleep Timer Off"

            Toast.makeText(
                this,
                "Sleep timer cancelled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startTimer(minutes: Int) {

        val durationMillis =
            minutes * 60_000L

        AudioPlayerManager.startSleepTimer(
            durationMillis
        )

        tvSleepTimerStatus.text =
            "Sleep Timer: $minutes minutes"

        Toast.makeText(
            this,
            "Sleep timer set for $minutes minutes",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun startTestTimer() {

        val durationMillis = 60_000L

        AudioPlayerManager.startSleepTimer(
            durationMillis
        )

        tvSleepTimerStatus.text =
            "Sleep Timer: 1 minute test"

        Toast.makeText(
            this,
            "1 minute sleep timer started",
            Toast.LENGTH_SHORT
        ).show()
    }
}