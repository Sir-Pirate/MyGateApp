package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.AuthManager.loginUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonGoToRegister: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupLoginButton()
        setupRegisterButton()
    }

    private fun initializeViews() {
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister)
    }

    private fun setupLoginButton() {

        buttonLogin.setOnClickListener {

            val email = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter both email and password",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            loginUser(
                email,
                password,

                onSuccess = {

                    val currentUser = auth.currentUser

                    if (currentUser != null) {

                        Toast.makeText(
                            this,
                            "Welcome back!",
                            Toast.LENGTH_SHORT
                        ).show()

                        fetchUserRoleAndNavigate(currentUser.uid)

                    } else {

                        Toast.makeText(
                            this,
                            "Authentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },

                onError = { errorMessage ->

                    val message = when {

                        errorMessage.contains("badly formatted", true) ->
                            "Invalid email format"

                        errorMessage.contains("password is invalid", true) ->
                            "Incorrect password"

                        errorMessage.contains("no user record", true) ->
                            "User not found. Please register first"

                        errorMessage.contains("network error", true) ->
                            "Check your internet connection"

                        else ->
                            "Login failed. Please try again"
                    }

                    Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun fetchUserRoleAndNavigate(uid: String) {

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val role = document.getString("role") ?: "resident"

                    navigateUserByRole(role)

                } else {

                    Toast.makeText(
                        this,
                        "User profile not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Failed to retrieve user role",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun navigateUserByRole(role: String) {

        val normalizedRole = role.trim().lowercase()

        val destination = when (normalizedRole) {

            "admin" -> HomeActivity::class.java

            "guard" -> GuardHomeActivity::class.java

            "resident" -> ResidentHomeActivity::class.java

            else -> HomeActivity::class.java
        }

        val intent = Intent(this, destination)

        intent.putExtra("role", normalizedRole)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }

    private fun setupRegisterButton() {

        buttonGoToRegister.setOnClickListener {

            try {

                val intent = Intent(
                    this@MainActivity,
                    RegisterActivity::class.java
                )

                startActivity(intent)

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Register page not available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}