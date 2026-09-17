package com.example.tuniq

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.widget.Button
    import android.widget.EditText
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.credentials.CredentialManager
    import androidx.credentials.CustomCredential
    import androidx.credentials.GetCredentialRequest
    import androidx.lifecycle.lifecycleScope
    import com.google.android.libraries.identity.googleid.GetGoogleIdOption
    import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
    import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.GoogleAuthProvider
    import kotlinx.coroutines.launch
    class LoginActivity : AppCompatActivity() {

        private lateinit var auth: FirebaseAuth
        private lateinit var credentialManager: CredentialManager

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)

            Log.d("LoginActivity", "Login screen opened")

            auth = FirebaseAuth.getInstance()
            credentialManager = CredentialManager.create(this)

            val email = findViewById<EditText>(R.id.etLoginEmail)
            val password = findViewById<EditText>(R.id.etLoginPassword)
            val loginButton = findViewById<Button>(R.id.btnLogin)
            val googleButton = findViewById<Button>(R.id.btnSsoGoogle)
            val registerLink = findViewById<TextView>(R.id.tvBackToLogin)

            loginButton.setOnClickListener {

                val emailText = email.text.toString().trim()
                val passwordText = password.text.toString()

                if (emailText.isEmpty()) {
                    email.error = "Please enter your email"
                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                    email.error = "Please enter a valid email address"
                    return@setOnClickListener
                }

                if (passwordText.isEmpty()) {
                    password.error = "Please enter your password"
                    return@setOnClickListener
                }

                loginButton.isEnabled = false

                Log.d("LoginActivity", "Attempting Firebase email login")

                auth.signInWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(this) { task ->

                        loginButton.isEnabled = true

                        if (task.isSuccessful) {

                            Log.d("LoginActivity", "Email login successful")

                            Toast.makeText(
                                this,
                                "Login successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            openMainActivity()

                        } else {

                            Log.e(
                                "LoginActivity",
                                "Email login failed",
                                task.exception
                            )

                            Toast.makeText(
                                this,
                                task.exception?.message ?: "Login failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }

            googleButton.setOnClickListener {
                Log.d("LoginActivity", "Google SSO selected")
                signInWithGoogle()
            }

            registerLink.setOnClickListener {
                Log.d("LoginActivity", "Opening registration screen")

                startActivity(
                    Intent(this, RegisterActivity::class.java)
                )
            }
        }
        private fun signInWithGoogle() {

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {

                try {

                    val result = credentialManager.getCredential(
                        context = this@LoginActivity,
                        request = request
                    )

                    handleGoogleCredential(result.credential)

                } catch (exception: Exception) {

                    Log.e(
                        "LoginActivity",
                        "Google SSO failed: ${exception.message}",
                        exception
                    )

                    Toast.makeText(
                        this@LoginActivity,
                        "Google sign-in was failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        private fun handleGoogleCredential(credential: androidx.credentials.Credential) {

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {

                try {

                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    firebaseAuthWithGoogle(
                        googleCredential.idToken
                    )

                } catch (exception: GoogleIdTokenParsingException) {

                    Log.e(
                        "LoginActivity",
                        "Invalid Google ID token",
                        exception
                    )

                    Toast.makeText(
                        this,
                        "Google authentication failed",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {

                Log.e(
                    "LoginActivity",
                    "Credential was not a Google ID credential"
                )

                Toast.makeText(
                    this,
                    "Invalid Google credential",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        private fun firebaseAuthWithGoogle(idToken: String) {

            val credential = GoogleAuthProvider.getCredential(
                idToken,
                null
            )

            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {

                        Log.d(
                            "LoginActivity",
                            "Google Firebase authentication successful"
                        )

                        Toast.makeText(
                            this,
                            "Google login successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        openMainActivity()

                    } else {

                        Log.e(
                            "LoginActivity",
                            "Google Firebase authentication failed",
                            task.exception
                        )

                        Toast.makeText(
                            this,
                            task.exception?.message
                                ?: "Google authentication failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        private fun openMainActivity() {

            startActivity(
                Intent(this, MainActivity::class.java)
            )

            finish()
        }

        override fun onStart() {
            super.onStart()

            val currentUser = auth.currentUser

            if (currentUser != null) {

                Log.d(
                    "LoginActivity",
                    "Existing Firebase user detected"
                )

                openMainActivity()
            }
        }
    }