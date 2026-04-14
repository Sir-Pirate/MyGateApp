package com.example.myapplication

object AuthManager {

    // REGISTER — creates a new user account
    fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        role: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseHelper.getAuth()
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener
                val userMap = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "role" to role,
                    "phone" to phone,
                    "userId" to userId
                )
                FirebaseHelper.getDatabase()
                    .collection("users")
                    .document(userId)
                    .set(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Database error") }
            }
            .addOnFailureListener { onError(it.message ?: "Registration failed") }
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