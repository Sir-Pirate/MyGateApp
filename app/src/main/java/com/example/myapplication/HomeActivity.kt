package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val textWelcome = findViewById<TextView>(R.id.textWelcome)
        val textRole = findViewById<TextView>(R.id.textRole)
        val buttonLogout = findViewById<Button>(R.id.buttonLogout)

        // Get current user
        val currentUser = FirebaseHelper.getAuth().currentUser

        // Show email
        textWelcome.text = "Welcome, ${currentUser?.email}"

        // Fetch role from Firestore
        val userId = currentUser?.uid
        if (userId != null) {
            FirebaseHelper.getDatabase()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val role = document.getString("role") ?: "Unknown"
                        runOnUiThread {
                            textRole.text = "Role: $role"
                        }
                    }
                }
                .addOnFailureListener {
                    runOnUiThread {
                        textRole.text = "Role: Could not fetch"
                    }
                }
        }

        // Logout button
        buttonLogout.setOnClickListener {
            AuthManager.logoutUser()
            val intent = Intent(this, mainactivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}