package com.example.myapplication

object AuthManager {

    // REGISTER — creates a new user account
    fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: String,
        flatNo: String,
        tower: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val db = FirebaseHelper.getDatabase()

        // 🔍 Step 1: Check if phone already exists
        db.collection("users")
            .whereEqualTo("phone", phone)
            .get()
            .addOnSuccessListener { documents ->

                if (!documents.isEmpty) {
                    // ❌ Phone already exists
                    onError("Phone number already registered")
                    return@addOnSuccessListener
                }

                // ✅ Step 2: Create user in Firebase Auth
                FirebaseHelper.getAuth()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->

                        val userId = result.user?.uid ?: return@addOnSuccessListener

                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "phone" to phone,
                            "role" to role,
                            "userId" to userId,
                            "flatNo" to flatNo,
                            "tower" to tower
                        )

                        // ✅ Step 3: Save in Firestore
                        db.collection("users")
                            .document(userId)
                            .set(userMap)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener {
                                onError(it.message ?: "Database error")
                            }
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Registration failed")
                    }
            }
            .addOnFailureListener {
                onError(it.message ?: "Error checking phone number")
            }
    }

    // LOGIN — signs in an existing user
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseHelper.getAuth()
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Login failed") }
    }

    // LOGOUT — signs out the current user
    fun logoutUser() {
        FirebaseHelper.getAuth().signOut()
    }
}