package com.example.tuniq

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tuniq.api.RetrofitClient
import com.example.tuniq.api.TuniqApiService
import com.example.tuniq.api.UserModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var azureApi: TuniqApiService

    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        auth = FirebaseAuth.getInstance()
        azureApi = RetrofitClient.getApiService(this)

        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnUpdateEmail = findViewById<TextView>(R.id.btnUpdateEmail)
        val btnChangePassword = findViewById<TextView>(R.id.btnChangePassword)

        loadCurrentUserData()

        btnBack.setOnClickListener {
            finish()
        }

        // Update Email
        btnUpdateEmail.setOnClickListener {
            val newEmail = editEmail.text.toString().trim()
            val currentUser = auth.currentUser

            if (newEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                editEmail.error = "Enter a valid email address"
                return@setOnClickListener
            }

            if (currentUser != null) {
                currentUser.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("AccountActivity", "Firebase email updated successfully")

                        // Sync updated email to Azure SQL Database
                        val userId = currentUser.uid
                        val name = currentUser.displayName ?: "User"
                        val updatedUser = UserModel(userId = userId, name = name, email = newEmail)

                        azureApi.createUser(updatedUser).enqueue(object : Callback<UserModel> {
                            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@AccountActivity, "Email updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@AccountActivity, "Email updated in Firebase, but backend sync failed", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                                Toast.makeText(this@AccountActivity, "Network error during backend sync", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(this@AccountActivity, "Failed to update email", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Update Password
        btnChangePassword.setOnClickListener {
            val newPassword = editPassword.text.toString().trim()
            val currentUser = auth.currentUser

            if (newPassword.length < 8) {
                editPassword.error = "Password must be at least 8 characters"
                return@setOnClickListener
            }

            if (currentUser != null) {
                currentUser.updatePassword(newPassword).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("AccountActivity", "Password updated successfully")
                        Toast.makeText(this@AccountActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        editPassword.text?.clear()
                    } else {
                        Toast.makeText(this@AccountActivity, "Failed to update password", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loadCurrentUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            editEmail.setText(currentUser.email ?: "")
            // Passwords cannot be retrieved securely from Firebase Auth for privacy reasons,
            // so we leave the password field blank or ask the user to input a new one.
            editPassword.setText("")
            editPassword.hint = "Enter new password to change"
        }
    }
}