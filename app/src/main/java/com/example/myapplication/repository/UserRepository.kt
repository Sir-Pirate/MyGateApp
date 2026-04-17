package com.example.myapplication.repository

import com.example.myapplication.FirebaseHelper
import com.example.myapplication.model.User

class UserRepository {

    private val db = FirebaseHelper.getDatabase()
    private val usersCollection = db.collection("users")

    // ── SAVE user profile after signup ─────────────────────────────
    fun saveUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        usersCollection
            .document(user.userId)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // ── GET a single user's profile ────────────────────────────────
    fun getUser(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        usersCollection
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                onSuccess(user)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }
}