package com.example.tuniq

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.account)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bind Buttons
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnChangeUsername = findViewById<TextView>(R.id.btnChangeUsername)
        val btnUpdateEmail = findViewById<TextView>(R.id.btnUpdateEmail)
        val btnChangePassword = findViewById<TextView>(R.id.btnChangePassword)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)

        // Bind Input Fields
        val editUsername = findViewById<TextInputEditText>(R.id.editUsername)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)

        // Handle Navigation
        btnBack.setOnClickListener {
            finish()
        }

        // Handle Submissions
        btnChangeUsername.setOnClickListener {
            val newUsername = editUsername.text.toString().trim()
            if (newUsername.isNotEmpty()) {

                // -- Send new username to ASP.NET Core API --

                Toast.makeText(this, "Updating username to: $newUsername", Toast.LENGTH_SHORT).show()
                editUsername.text?.clear() // Clear field after saving
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        btnUpdateEmail.setOnClickListener {
            val newEmail = editEmail.text.toString().trim()

            if (newEmail.isEmpty()) {
                editEmail.error = "Email cannot be empty"
                editEmail.requestFocus()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                editEmail.error = "Please enter a valid email address"
                editEmail.requestFocus()
            } else {

                // -- Send new email to ASP.NET Core API --

                Toast.makeText(this, "Updating email to: $newEmail", Toast.LENGTH_SHORT).show()
                editEmail.error = null // Clear any previous errors
                editEmail.text?.clear()
            }
        }

        btnChangePassword.setOnClickListener {
            val newPassword = editPassword.text.toString()

            if (newPassword.isEmpty()) {
                editPassword.error = "Password cannot be empty"
                editPassword.requestFocus()
            } else if (newPassword.length < 6) {
                editPassword.error = "Password must be at least 6 characters long"
                editPassword.requestFocus()
            } else {

                // -- Send new password to ASP.NET Core API --

                Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                editPassword.error = null
                editPassword.text?.clear()
            }
        }

        btnDeleteAccount.setOnClickListener {
            Toast.makeText(this, "Warning: Delete Account initiated", Toast.LENGTH_LONG).show()
        }
    }
}