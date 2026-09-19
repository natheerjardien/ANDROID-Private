package com.example.tuniq

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notifications_view)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Closes the notifications screen and goes back to the dashboard
        btnBack.setOnClickListener {
            finish()
        }
    }
}