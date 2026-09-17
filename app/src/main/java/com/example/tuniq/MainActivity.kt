package com.example.tuniq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "Main screen opened")

        auth = FirebaseAuth.getInstance()

        val logoutButton = findViewById<Button>(R.id.btnLogout)

        logoutButton.setOnClickListener {

            Log.d("MainActivity", "Logging out user")

            auth.signOut()

            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
        }
    }
}