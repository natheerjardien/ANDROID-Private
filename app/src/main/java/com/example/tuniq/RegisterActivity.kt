package com.example.tuniq

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


    class RegisterActivity : AppCompatActivity() {

        private lateinit var auth: FirebaseAuth

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_register)

            Log.d("RegisterActivity", "Register screen opened")

            auth = FirebaseAuth.getInstance()

            val name = findViewById<EditText>(R.id.etRegName)
            val email = findViewById<EditText>(R.id.etRegEmail)
            val password = findViewById<EditText>(R.id.etRegPassword)
            val registerButton = findViewById<Button>(R.id.btnRegister)
            val backToLogin = findViewById<TextView>(R.id.tvBackToLogin)

            registerButton.setOnClickListener {

                val nameText = name.text.toString().trim()
                val emailText = email.text.toString().trim()
                val passwordText = password.text.toString()

                if (nameText.isEmpty()) {
                    name.error = "Please enter your name"
                    return@setOnClickListener
                }

                if (emailText.isEmpty()) {
                    email.error = "Please enter your email"
                    return@setOnClickListener
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                    email.error = "Please enter a valid email address"
                    return@setOnClickListener
                }

                if (!isValidPassword(passwordText)) {
                    password.error =
                        "Password must contain 8+ characters, uppercase, lowercase, number and special character"
                    return@setOnClickListener
                }

                registerButton.isEnabled = false

                Log.d("RegisterActivity", "Attempting Firebase registration")

                auth.createUserWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(this) { task ->

                        registerButton.isEnabled = true

                        if (task.isSuccessful) {

                            Log.d("RegisterActivity", "Registration successful")

                            Toast.makeText(
                                this,
                                "Account created successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            finish()

                        } else {

                            Log.e(
                                "RegisterActivity",
                                "Registration failed",
                                task.exception
                            )

                            Toast.makeText(
                                this,
                                task.exception?.message ?: "Registration failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }

            backToLogin.setOnClickListener {
                finish()
            }
        }

        private fun isValidPassword(password: String): Boolean {
            return password.length >= 8 &&
                    password.any { it.isUpperCase() } &&
                    password.any { it.isLowerCase() } &&
                    password.any { it.isDigit() } &&
                    password.any { !it.isLetterOrDigit() }
        }
    }
