package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity






class MainActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonGoToRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister)

        buttonLogin.setOnClickListener {
            val email = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please enter both fields")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Enter valid email")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showToast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            AuthManager.loginUser(
                email,
                password,
                onSuccess = {
                    showToast("Login Successful!")

                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onError = { error ->
                    val message = when {
                        error?.contains("password", true) == true -> "Incorrect password"
                        error?.contains("no user", true) == true -> "User not found"
                        else -> "Login failed"
                    }
                    showToast(message)
                }
            )
        }

        buttonGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}