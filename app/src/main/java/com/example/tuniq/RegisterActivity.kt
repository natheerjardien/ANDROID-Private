package com.example.tuniq

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tuniq.api.RetrofitClient
import com.example.tuniq.api.TuniqApiService
import com.example.tuniq.api.UserModel
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


    class RegisterActivity : AppCompatActivity() {

        private lateinit var auth: FirebaseAuth
        private lateinit var azureApi: TuniqApiService

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_register)

            Log.d("RegisterActivity", "Register screen opened")

            auth = FirebaseAuth.getInstance()

            // Centralized client instantiation ensures the AuthInterceptor injects the Bearer token (Square, 2026).
            azureApi = RetrofitClient.getApiService(this)

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
                        if (task.isSuccessful)
                        {
                            Log.d("RegisterActivity", "Registration successful")
                            // Grabs the Firebase UID that was just generated
                            val firebaseUid = auth.currentUser?.uid ?: ""
                            val firebaseUser = auth.currentUser
                            // Forces Firebase to fully generate the new JWT Bearer token
                            firebaseUser?.getIdToken(true)?.addOnSuccessListener {

                                // Reinitializes Retrofit now that the user is logged in so the AuthInterceptor can grab the fresh token
                                azureApi = RetrofitClient.getApiService(this@RegisterActivity)

                                // Syncs to Azure
                                syncUserToAzure(firebaseUid, nameText, emailText)

                            }?.addOnFailureListener {
                                Toast.makeText(this@RegisterActivity, "Failed to fetch auth token", Toast.LENGTH_SHORT).show()
                                registerButton.isEnabled = true
                                registerButton.text = "Sign Up"
                            }
                        }
                        else
                        {
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

        /**
         * Runs the POST request to register the user inside the SQL database through the .NET API
         */
        private fun syncUserToAzure(uid: String, name: String, email: String) {
            val newUser = UserModel(userId = uid, name = name, email = email)

            azureApi.createUser(newUser).enqueue(object : Callback<UserModel> {
                override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                    if (response.isSuccessful || response.code() == 409)
                    {
                        Toast.makeText(this@RegisterActivity, "Account created & synced successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else
                    {
                        Log.e("AzureAPI", "Failed to sync user: HTTP ${response.code()}")
                        Toast.makeText(this@RegisterActivity, "Account created, but database sync failed.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<UserModel>, t: Throwable) {
                    Log.e("AzureAPI", "Network error during sync", t)
                    Toast.makeText(this@RegisterActivity, "Network error during database sync.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        }

        private fun isValidPassword(password: String): Boolean {
            return password.length >= 8 &&
                    password.any { it.isUpperCase() } &&
                    password.any { it.isLowerCase() } &&
                    password.any { it.isDigit() } &&
                    password.any { !it.isLetterOrDigit() }
        }
    }
