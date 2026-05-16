package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.AuthManager.loginUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private var editTextUsername: EditText? = null
    private var editTextPassword: EditText? = null
    private var buttonLogin: Button? = null
    private var buttonGoToRegister: Button? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister)

        // Login button
        buttonLogin!!.setOnClickListener {
            val email = editTextUsername!!.text.toString().trim()
            val password = editTextPassword!!.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Please enter both fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            loginUser(
                email,
                password,
                {
                    runOnUiThread {

                        Toast.makeText(
                            applicationContext,
                            "Welcome back!",
                            Toast.LENGTH_SHORT
                        ).show()

                        val user = FirebaseAuth.getInstance().currentUser

                        if (user != null) {
                            val uid = user.uid

                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->

                                    if (document.exists()) {
                                        val roleFromFirestore =
                                            document.getString("role") ?: "resident"

                                        val intent = Intent(
                                            this@MainActivity,
                                            HomeActivity::class.java
                                        )
                                        intent.putExtra("role", roleFromFirestore)
                                        startActivity(intent)
                                        finish()

                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "User data not found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Something went wrong. Try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "User not logged in",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                { errorMessage: String? ->

                    runOnUiThread {

                        val message = when {
                            errorMessage?.contains("badly formatted", true) == true ->
                                "Invalid email format"

                            errorMessage?.contains("password is invalid", true) == true ->
                                "Incorrect password"

                            errorMessage?.contains("no user record", true) == true ->
                                "User not found. Please register first"

                            errorMessage?.contains("network error", true) == true ->
                                "Check your internet connection"

                            else ->
                                "Login failed. Please try again"
                        }

                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Go to Register button
        buttonGoToRegister!!.setOnClickListener {
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}